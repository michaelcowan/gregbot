/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.identities.adobe;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.blt.gregbot.plugin.PluginException;
import io.blt.gregbot.plugin.identities.IdentityException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.entry;

@WireMockTest(proxyMode = true)
class AdobeImsTest {

    @Nested
    class Service {

        final Map<String, String> requiredProperties = Map.of(
                "type", "SERVICE",
                "id", "mock-id",
                "secret", "mock-secret",
                "code", "mock-code"
        );

        @ParameterizedTest
        @CsvSource({"type", "id", "secret", "code"})
        void loadShouldThrowWhenPropertiesIsMissingKey(String key) {
            var properties = requiredPropertiesWithout(key);

            assertThatNullPointerException()
                    .isThrownBy(() -> new AdobeIms().load(properties));
        }

        @Test
        void loadShouldDefaultHostProperty() {
            assertThatException()
                    .isThrownBy(() -> new AdobeIms().load(requiredProperties))
                    .withMessage("Failed to authenticate using https://ims-na1.adobelogin.com");
        }

        @Test
        void loadShouldOverrideDefaultHostProperty() {
            var properties = requiredPropertiesWith("host", "https://mock-host");

            assertThatException()
                    .isThrownBy(() -> new AdobeIms().load(properties))
                    .withMessage("Failed to authenticate using https://mock-host");
        }

        @Test
        void variablesShouldReturnResultUsingDefaultPropertyValues() throws PluginException {
            mockIms("");

            var plugin = new AdobeIms();
            // Mocking the default https address requires modifying HttpClient which I want to avoid.
            // Instead, I'll test the host in loadShouldDefaultHostProperty and loadShouldOverrideDefaultHostProperty
            plugin.load(requiredPropertiesWith("host", "http://mock-host"));
            var result = plugin.variables();

            assertThat(result)
                    .containsOnly(entry("token", "mock-access-token"));
        }

        @Test
        void variablesShouldReturnResultUsingOverriddenPropertyValues() throws PluginException {
            mockIms("mock-scope");

            var properties = new HashMap<>(requiredProperties);
            properties.putAll(Map.of(
                    "host", "http://mock-host",
                    "variable", "mock-variable",
                    "scope", "mock-scope"));

            var plugin = new AdobeIms();
            plugin.load(properties);
            var result = plugin.variables();

            assertThat(result)
                    .containsOnly(entry("mock-variable", "mock-access-token"));
        }

        @Test
        void variablesShouldBubbleUpConnectorExceptionWrappedInIdentityException() throws PluginException {
            mockImsSeverError();

            var plugin = new AdobeIms();
            plugin.load(requiredPropertiesWith("host", "http://mock-host"));

            assertThatExceptionOfType(IdentityException.class)
                    .isThrownBy(plugin::variables);
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

        private void mockIms(String scope) {
            stubFor(post("/ims/token/v1")
                    .withHost(equalTo("mock-host"))
                    .withFormParam("client_id", equalTo("mock-id"))
                    .withFormParam("client_secret", equalTo("mock-secret"))
                    .withFormParam("code", equalTo("mock-code"))
                    .withFormParam("scope", equalTo(scope))
                    .withFormParam("grant_type", equalTo("authorization_code"))
                    .willReturn(okJson("""
                            {
                              "access_token": "mock-access-token",
                              "expires_in": 1000
                            }
                            """)));
        }

        private void mockImsSeverError() {
            stubFor(post("/ims/token/v1")
                    .withHost(equalTo("mock-host"))
                    .withFormParam("client_id", equalTo("mock-id"))
                    .withFormParam("client_secret", equalTo("mock-secret"))
                    .withFormParam("code", equalTo("mock-code"))
                    .withFormParam("scope", equalTo(""))
                    .withFormParam("grant_type", equalTo("authorization_code"))
                    .willReturn(okJson("""
                            {
                              "access_token": "mock-access-token",
                              "refresh_token" : "mock-refresh-token",
                              "expires_in": 0
                            }
                            """)));

            stubFor(post("/ims/token/v1")
                    .withHost(equalTo("mock-host"))
                    .withFormParam("client_id", equalTo("mock-id"))
                    .withFormParam("client_secret", equalTo("mock-secret"))
                    .withFormParam("refresh_token", equalTo("mock-refresh-token"))
                    .withFormParam("grant_type", equalTo("refresh_token"))
                    .willReturn(serverError()));
        }

    }

}
