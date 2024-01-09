/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.connector;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.mockito.Mockito.when;

@WireMockTest(proxyMode = true)
@ExtendWith(MockitoExtension.class)
class ConnectorTest {

    @Mock
    HttpResponse<byte[]> httpResponse;

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowOnConstructionWhenHostIsNullOrEmpty(String host) {
        assertThatException()
                .isThrownBy(() -> new Connector(host))
                .withMessage("'host' must not be empty");
    }

    @Test
    void sendShouldReturnResponseAndEmptyDataForEndpointWithNoBody() throws IOException {
        mockEndpointReturning(ok());

        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://mock.domain/mock/path"))
                .GET()
                .build();

        var result = new Connector("not-using-uri-builders")
                .send(request);

        assertThat(result.getResponse())
                .extracting(HttpResponse::statusCode)
                .isEqualTo(200);

        assertThat(result.getData())
                .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 302, 404, 504})
    void sendShouldReturnResponseAndDataForEndpointWithJsonBodyAndStatus(int status) throws IOException {
        mockEndpointReturning(okJson("{ \"name\": \"Greg\" }")
                .withStatus(status));

        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://mock.domain/mock/path"))
                .GET()
                .build();

        var result = new Connector("not-using-uri-builders")
                .send(request, User.class);

        assertThat(result.getResponse())
                .extracting(HttpResponse::statusCode)
                .isEqualTo(status);

        assertThat(result.getData())
                .containsInstanceOf(User.class)
                .get()
                .extracting(User::name)
                .isEqualTo("Greg");
    }

    @Test
    void sendShouldReturnResponseAndEmptyDataForEndpointWithUnsupportedBody() throws IOException {
        mockEndpointReturning(ok("raw text"));

        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://mock.domain/mock/path"))
                .GET()
                .build();

        var result = new Connector("not-using-uri-builders")
                .send(request, User.class);

        assertThat(result.getResponse())
                .extracting(HttpResponse::statusCode)
                .isEqualTo(200);

        assertThat(result.getData())
                .isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "http://mock.domain,  /mock/path",
            "http://mock.domain/, /mock/path",
            "http://mock.domain,  mock/path",
            "http://mock.domain/, mock/path"
    })
    void uriForPathShouldBuildUriFromConnectorHost(String host, String path) {
        var result = new Connector(host)
                .uriForPath(path);

        assertThat(result)
                .isEqualTo(URI.create("http://mock.domain/mock/path"));
    }

    @Test
    void formFromMapShouldGenerateValidFormRequest() throws IOException {
        mockFormEndpoint(m -> m
                .withFormParam("player1", equalTo("Greg"))
                .withFormParam("player2", equalTo("Louis"))
                .withFormParam("player3", equalTo("Phil"))
        );

        var form = Connector.formFromMap(Map.of(
                "player1", "Greg",
                "player2", "Louis",
                "player3", "Phil"));

        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://mock.domain/mock/path"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(form)
                .build();

        var result = new Connector("not-using-uri-builders")
                .send(request);

        assertThat(result.getResponse())
                .extracting(HttpResponse::statusCode)
                .isEqualTo(200);
    }

    @Nested
    class WhenInterrupted {

        final Connector connector = new Connector("http://mock");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://mock.domain/mock/path"))
                .GET()
                .build();

        @BeforeEach
        void beforeEach() {
            Thread.currentThread().interrupt();
        }

        @Test
        void sendShouldBubbleUpInterruptedExceptionAsIOException() {
            assertThatIOException()
                    .isThrownBy(() -> connector.send(request))
                    .havingCause()
                    .isInstanceOf(InterruptedException.class);
        }

        @Test
        void sendWithResponseTypeShouldBubbleUpInterruptedExceptionAsIOException() {
            assertThatIOException()
                    .isThrownBy(() -> connector.send(request, Class.class))
                    .havingCause()
                    .isInstanceOf(InterruptedException.class);
        }

    }

    @Nested
    class ConnectorResult {

        @ParameterizedTest
        @MethodSource("status1xx")
        void is1xxInformationalShouldReturnTrue(int status) {
            var result = buildResultWithStatus(status)
                    .is1xxInformational();

            assertThat(result)
                    .isTrue();
        }

        @ParameterizedTest
        @MethodSource({"status2xx", "status3xx", "status4xx", "status5xx"})
        void is1xxInformationalShouldReturnFalse(int status) {
            var result = buildResultWithStatus(status)
                    .is1xxInformational();

            assertThat(result)
                    .isFalse();
        }

        @ParameterizedTest
        @MethodSource("status2xx")
        void is2xxSuccessShouldReturnTrue(int status) {
            var result = buildResultWithStatus(status)
                    .is2xxSuccess();

            assertThat(result)
                    .isTrue();
        }

        @ParameterizedTest
        @MethodSource({"status1xx", "status3xx", "status4xx", "status5xx"})
        void is2xxSuccessShouldReturnFalse(int status) {
            var result = buildResultWithStatus(status)
                    .is2xxSuccess();

            assertThat(result)
                    .isFalse();
        }

        @ParameterizedTest
        @MethodSource("status3xx")
        void is3xxRedirectionShouldReturnTrue(int status) {
            var result = buildResultWithStatus(status)
                    .is3xxRedirection();

            assertThat(result)
                    .isTrue();
        }

        @ParameterizedTest
        @MethodSource({"status1xx", "status2xx", "status4xx", "status5xx"})
        void is3xxRedirectionShouldReturnFalse(int status) {
            var result = buildResultWithStatus(status)
                    .is3xxRedirection();

            assertThat(result)
                    .isFalse();
        }

        @ParameterizedTest
        @MethodSource("status4xx")
        void is4xxClientErrorShouldReturnTrue(int status) {
            var result = buildResultWithStatus(status)
                    .is4xxClientError();

            assertThat(result)
                    .isTrue();
        }

        @ParameterizedTest
        @MethodSource({"status1xx", "status2xx", "status3xx", "status5xx"})
        void is4xxClientErrorShouldReturnFalse(int status) {
            var result = buildResultWithStatus(status)
                    .is4xxClientError();

            assertThat(result)
                    .isFalse();
        }

        @ParameterizedTest
        @MethodSource("status5xx")
        void is5xxServerErrorShouldReturnTrue(int status) {
            var result = buildResultWithStatus(status)
                    .is5xxServerError();

            assertThat(result)
                    .isTrue();
        }

        @ParameterizedTest
        @MethodSource({"status1xx", "status2xx", "status3xx", "status4xx"})
        void is5xxServerErrorShouldReturnFalse(int status) {
            var result = buildResultWithStatus(status)
                    .is5xxServerError();

            assertThat(result)
                    .isFalse();
        }

        Connector.Result<String> buildResultWithStatus(int status) {
            when(httpResponse.statusCode()).thenReturn(status);

            return new Connector.Result<>(httpResponse, null);
        }

        private static Stream<Integer> status1xx() {
            return IntStream.range(100, 200).boxed();
        }

        static Stream<Integer> status2xx() {
            return IntStream.range(200, 300).boxed();
        }

        static Stream<Integer> status3xx() {
            return IntStream.range(300, 400).boxed();
        }

        static Stream<Integer> status4xx() {
            return IntStream.range(400, 500).boxed();
        }

        static Stream<Integer> status5xx() {
            return IntStream.range(500, 600).boxed();
        }

    }

    private void mockEndpointReturning(ResponseDefinitionBuilder responseDefinitionBuilder) {
        stubFor(get("/mock/path")
                .withHost(equalTo("mock.domain"))
                .willReturn(responseDefinitionBuilder));
    }

    private void mockFormEndpoint(Consumer<MappingBuilder> mappingBuilderConsumer) {
        var builder = post("/mock/path")
                .withHost(equalTo("mock.domain"))
                .willReturn(ok());

        mappingBuilderConsumer.accept(builder);

        stubFor(builder);
    }

    record User(String name) {}

}
