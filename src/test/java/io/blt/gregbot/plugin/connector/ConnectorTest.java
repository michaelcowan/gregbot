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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

@WireMockTest(proxyMode = true)
class ConnectorTest {

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowOnConstructionWhenHostIsNullOrEmpty(String host) {
        assertThatException()
                .isThrownBy(() -> new Connector(host))
                .withMessage("'host' must not be empty");
    }

    @Test
    void sendShouldReturnResponseAndEmptyDataForEndpointWithNoBody() throws IOException, InterruptedException {
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

    @Test
    void sendShouldReturnResponseAndDataForEndpointWithJsonBody() throws IOException, InterruptedException {
        mockEndpointReturning(okJson("{ \"name\": \"Greg\" }"));

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
                .containsInstanceOf(User.class)
                .get()
                .extracting(User::getName)
                .isEqualTo("Greg");
    }

    @Test
    void sendShouldReturnResponseAndEmptyDataForEndpointWithUnsupportedBody() throws IOException, InterruptedException {
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

    @Test
    void sendShouldResponseAndEmptyBodyForEndpointWithBadRequest() throws IOException, InterruptedException {
        mockEndpointReturning(badRequest());

        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://mock.domain/mock/path"))
                .GET()
                .build();

        var result = new Connector("not-using-uri-builders")
                .send(request, null);

        assertThat(result.getResponse())
                .extracting(HttpResponse::statusCode)
                .isEqualTo(400);

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
    void formFromMapShouldGenerateValidFormRequest() throws IOException, InterruptedException {
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

    public static class User {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


}
