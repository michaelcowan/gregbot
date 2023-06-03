/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.config;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blt.gregbot.core.config.Properties;
import io.blt.gregbot.core.identity.IdentityProperties;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static io.blt.test.JsonUtils.parseJsonTo;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

class PropertiesTest {

    @Nested
    class Load {

        @Test
        void shouldThrowWhenFileIsEmpty() {
            assertThatIOException()
                    .isThrownBy(() -> Properties.load("empty.txt"))
                    .withMessageStartingWith("No content to map due to end-of-input");
        }

        @Test
        void shouldThrowWhenFileDoesntExist() {
            assertThatIOException()
                    .isThrownBy(() -> Properties.load("unknown.file"))
                    .withMessage("Cannot read file 'unknown.file'");
        }

        static Stream<Arguments> shouldThrowWhenFileFailsValidation() {
            return Stream.of(
                    Arguments.of("empty.json", List.of(
                            "'identities' must not be null")),
                    Arguments.of("empty-identity.json", List.of(
                            "'identities[JsonBlob Prod].variables' must not be null",
                            "'identities[JsonBlob Prod].plugin' must not be null")),
                    Arguments.of("empty-plugin.json", List.of(
                            "'identities[JsonBlob Prod].plugin.type' must not be null",
                            "'identities[JsonBlob Prod].plugin.properties' must not be null"))
            );
        }

        @ParameterizedTest
        @MethodSource
        void shouldThrowWhenFileFailsValidation(String filename, List<String> messages) {
            assertThatIOException()
                    .isThrownBy(() -> Properties.load(filename))
                    .withMessageStartingWith("Failed validation because ")
                    .withMessageContainingAll(messages.toArray(new String[0]));
        }

        @Test
        void shouldLoadIdentityProperties() throws IOException {
            var identity = new IdentityProperties(
                    "Production",
                    "Blt Vault",
                    Map.of("api_key", "json_blob"),
                    new Properties.Plugin(
                            "io.blt.gregbot.plugin.Auth",
                            parseJsonTo(ObjectNode.class, """
                                    {
                                        "host": "https://auth.blt.io",
                                        "secret": "[blt/jsonblob/prod/secret]",
                                        "variable": "auth_token"
                                    }
                                    """)
                    )
            );

            var properties = Properties.load("test-properties.json");

            assertThat(properties.identities())
                    .containsExactly(entry("JsonBlob Prod", identity));
        }
    }

    @Nested
    class ReadPluginProperties {

        record AuthProperties(
                @NotEmpty String host,
                @NotEmpty String secret,
                String variable) {}


        static Stream<Arguments> shouldWhenPropertiesFailValidation() {
            return Stream.of(
                    Arguments.of("""
                                    {
                                        "host": "",
                                        "secret": "[blt/jsonblob/prod/secret]"
                                    }""",
                            "Failed validation because 'host' must not be empty"
                    ),
                    Arguments.of("""
                                    {
                                        "secret": "[blt/jsonblob/prod/secret]"
                                    }""",
                            "Failed validation because 'host' must not be empty"
                    ),
                    Arguments.of(
                            """
                                    {
                                        "host": "https://auth.blt.io",
                                        "secret": null
                                    }""",
                            "Failed validation because 'secret' must not be empty"
                    )
            );
        }

        @ParameterizedTest
        @MethodSource
        void shouldWhenPropertiesFailValidation(String json, String expectedMessage) throws IOException {
            var plugin = new Properties.Plugin("mock-type",
                    parseJsonTo(ObjectNode.class, json));

            assertThatIOException()
                    .isThrownBy(() -> plugin.readPropertiesAs(AuthProperties.class))
                    .withMessage(expectedMessage);
        }

        @Test
        void shouldReadPluginProperties() throws IOException {
            var plugin = new Properties.Plugin("mock-type",
                    parseJsonTo(ObjectNode.class, """
                            {
                                "host": "https://auth.blt.io",
                                "secret": "[blt/jsonblob/prod/secret]",
                                "variable": "auth_token"
                            }
                            """));

            var authProperties = plugin.readPropertiesAs(AuthProperties.class);

            assertThat(authProperties)
                    .extracting(p -> p.host, p -> p.secret, p -> p.variable)
                    .containsExactly("https://auth.blt.io", "[blt/jsonblob/prod/secret]", "auth_token");
        }

    }

}
