/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.blt.gregbot.plugin.secrets.SecretException;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.SoftAssertionsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@WireMockTest(proxyMode = true)
@ExtendWith(MockitoExtension.class)
class VaultOidcTest {

    @Mock
    Desktop desktop;

    final Map<String, String> requiredProperties = Map.of(
            "host", "http://mock-host"
    );

    @ParameterizedTest
    @ValueSource(strings = {
            "host"
    })
    void loadShouldThrowWhenPropertiesIsMissingKey(String key) {
        var properties = requiredPropertiesWithout(key);

        assertThatNullPointerException()
                .isThrownBy(() -> new VaultOidc().load(properties));
    }

    @Test
    void loadShouldNotThrowWhenPropertiesArePresent() throws Exception {
        mockVault();

        doWithMockedDesktop(() -> new VaultOidc().load(requiredProperties));
    }

    @ParameterizedTest
    @CsvSource({
            "engine, not-a-number"
    })
    void loadShouldThrowWhenPropertyIsInvalid(String key, String value) {
        var properties = requiredPropertiesWith(key, value);

        assertThatException()
                .isThrownBy(() -> new VaultOidc().load(properties))
                .withMessageContaining(value);
    }

    @Test
    void loadShouldUsePropertiesWhenConfiguringOidc() {
        mockVault();

        var properties = requiredPropertiesWith("listenPort", "8251");

        assertThatExceptionOfType(SecretException.class)
                .isThrownBy(() -> doWithMockedDesktop(() -> new VaultOidc().load(properties)))
                .withMessageContaining("Failed to load using properties: {host=http://mock-host, listenPort=8251}");
    }

    @Test
    void secretsForPathShouldReturnResultFromVaultDefaultingToEngineVersion2() throws Exception {
        mockVault();

        var plugin = new VaultOidc();
        doWithMockedDesktop(() -> plugin.load(requiredProperties));

        var result = plugin.secretsForPath("mock/path");

        assertThat(result)
                .containsOnly(
                        entry("mock-secret-key1", "mock-secret-value1"),
                        entry("mock-secret-key2", "mock-secret-value2")
                );
    }

    @Test
    void secretsForPathShouldReturnResultFromVaultUsingEngineVersion2() throws Exception {
        mockVault();

        var properties = requiredPropertiesWith("engine", "2");

        var plugin = new VaultOidc();
        doWithMockedDesktop(() -> plugin.load(properties));

        var result = plugin.secretsForPath("mock/path");

        assertThat(result)
                .containsOnly(
                        entry("mock-secret-key1", "mock-secret-value1"),
                        entry("mock-secret-key2", "mock-secret-value2")
                );
    }

    @Test
    void secretsForPathShouldReturnResultFromVaultUsingEngineVersion1() throws Exception {
        mockVault(true);

        var properties = requiredPropertiesWith("engine", "1");

        var plugin = new VaultOidc();
        doWithMockedDesktop(() -> plugin.load(properties));

        var result = plugin.secretsForPath("mock/path");

        assertThat(result)
                .containsOnly(
                        entry("mock-secret-key1", "mock-secret-value1"),
                        entry("mock-secret-key2", "mock-secret-value2")
                );
    }

    private Map<String, String> requiredPropertiesWithout(String key) {
        var properties = new HashMap<>(requiredProperties);
        properties.remove(key);
        return properties;
    }

    private Map<String, String> requiredPropertiesWith(String key, String value) {
        var properties = new HashMap<>(requiredProperties);
        properties.put(key, value);
        return properties;
    }

    private void mockVault() {
        mockVault(false);
    }

    private void mockVault(boolean useEngineVersion1) {
        stubFor(post("/v1/auth/oidc/oidc/auth_url")
                .withHost(equalTo("mock-host"))
                .willReturn(okJson("""
                            {
                              "data": {
                                "auth_url": "http://localhost:8250/oidc/callback?state=mock-state&code=mock-code&id_token=mock-id-token"
                              }
                            }
                            """)));


        stubFor(get(urlMatching("/v1/auth/oidc/oidc/callback\\?state=mock-state&code=mock-code&id_token=mock-id-token&client_nonce=[\\w-]+"))
                .withHost(equalTo("mock-host"))
                .willReturn(okJson("""
                            {
                              "auth": {
                                "client_token": "mock-token"
                              }
                            }
                            """)));

        stubFor(post("/v1/auth/token/renew-self")
                .withHost(equalTo("mock-host"))
                .willReturn(okJson("""
                            {
                              "auth": {
                                "policies": [],
                                "lease_duration": 3600
                              },
                              "renewable": true
                            }
                            """)));

        stubFor(get(useEngineVersion1 ? "/v1/mock/path" : "/v1/mock/data/path")
                .withHost(equalTo("mock-host"))
                .willReturn(okJson(useEngineVersion1 ? """
                            {
                              "data": {
                                "mock-secret-key1": "mock-secret-value1",
                                "mock-secret-key2": "mock-secret-value2"
                              }
                            }
                            """ : """
                            {
                              "data": {
                                "data": {
                                  "mock-secret-key1": "mock-secret-value1",
                                  "mock-secret-key2": "mock-secret-value2"
                                }
                              }
                            }
                            """
                        )));
    }

    private void doWithMockedDesktop(SoftAssertionsProvider.ThrowingRunnable runnable) throws Exception {
        mockDesktopBrowseToFetchUri();
        try (var mock = Mockito.mockStatic(Desktop.class)) {
            mock.when(Desktop::getDesktop).thenReturn(desktop);
            runnable.run();
        }
    }

    private void mockDesktopBrowseToFetchUri() throws IOException {
        doAnswer(invocation -> {
            URI uri = invocation.getArgument(0);
            makeGetHttpCall(uri);
            return null;
        }).when(desktop).browse(any());
    }

    private void makeGetHttpCall(URI uri) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpClient.newBuilder().build()
                .send(request, HttpResponse.BodyHandlers.discarding());
    }

}
