/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.services;

import io.blt.gregbot.core.plugin.TestableIdentityPlugin;
import io.blt.gregbot.core.plugin.TestableSecretPlugin;
import io.blt.gregbot.core.plugin.ThrowOnLoadIdentityPlugin;
import io.blt.gregbot.core.plugin.ThrowOnLoadSecretPlugin;
import io.blt.gregbot.core.properties.Properties.Identity;
import io.blt.gregbot.core.properties.Properties.Plugin;
import io.blt.gregbot.core.properties.Properties.Secret;
import io.blt.gregbot.plugin.identities.IdentityPlugin;
import io.blt.gregbot.plugin.secrets.SecretPlugin;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.MapEntry.entry;

class IdentityServiceTest {

    @Test
    void shouldNotLoadSecretPluginInCtor() {
        new IdentityService(
                Map.of("SecretPlugin", new Secret(
                        new Plugin(ThrowOnLoadSecretPlugin.class.getName(), Map.of()))),
                Map.of());
    }

    @Test
    void shouldNotLoadIdentityPluginInCtor() {
        new IdentityService(
                Map.of(),
                Map.of("IdentityPlugin", new Identity(
                        null, null, Map.of(),
                        new Plugin(ThrowOnLoadIdentityPlugin.class.getName(), Map.of()))));
    }

    @Nested
    class VariablesFor {

        @Test
        void shouldReturnIdentityVariablesWithoutPlugin() throws Exception {
            var service = new IdentityService(
                    Map.of(),
                    Map.of("MockIdentity",
                            new Identity(null, null, Map.of("plain-key", "plain-value"), null)));

            var variables = service.variablesFor("MockIdentity");

            assertThat(variables)
                    .containsOnly(
                            entry("plain-key", "plain-value"));
        }

        @Test
        void shouldReturnIdentityVariablesWithPlugin() throws Exception {
            var service = new IdentityService(
                    Map.of(),
                    Map.of("MockIdentity",
                            new Identity(null, null, Map.of("plain-key", "plain-value"),
                                    new Plugin(TestableIdentityPlugin.class.getName(), Map.of()))));

            var variables = service.variablesFor("MockIdentity");

            assertThat(variables)
                    .containsOnly(
                            entry("plain-key", "plain-value"),
                            entry("identity-plugin-key", "identity-plugin-value"));
        }

        @Test
        void shouldReturnIdentityVariablesWithPluginWithSecretPlugin() throws Exception {
            var service = new IdentityService(
                    Map.of("MockSecret",
                            new Secret(new Plugin(TestableSecretPlugin.class.getName(), Map.of()))),
                    Map.of("MockIdentity",
                            new Identity(null, "MockSecret", Map.of("plain-key", "plain-value"),
                                    new Plugin(TestableIdentityPlugin.class.getName(), Map.of()))));

            var variables = service.variablesFor("MockIdentity");

            assertThat(variables)
                    .containsOnly(
                            entry("plain-key", "plain-value"),
                            entry("identity-plugin-key", "identity-plugin-value"));
        }

        @Test
        void shouldLoadIdentityPluginWithIdentityPluginProperties() throws Exception {
            var service = new IdentityService(
                    Map.of(),
                    Map.of("MockIdentity",
                            new Identity(null, null, Map.of(),
                                    new Plugin(TestableIdentityPlugin.class.getName(),
                                            Map.of("plugin-key", "plugin-value")))));

            service.variablesFor("MockIdentity");

            assertThat(TestableIdentityPlugin.loadedProperties())
                    .containsOnly(
                            entry("plugin-key", "plugin-value"));
        }

        @Test
        void shouldLoadIdentityPluginWithRenderedSecrets() throws Exception {
            var service = new IdentityService(
                    Map.of("MockSecret",
                            new Secret(new Plugin(TestableSecretPlugin.class.getName(), Map.of()))),
                    Map.of("MockIdentity",
                            new Identity(null, "MockSecret", Map.of(),
                                    new Plugin(TestableIdentityPlugin.class.getName(),
                                            Map.of("rendered-key", "rendered-[secret-path/secret-key]")))));

            service.variablesFor("MockIdentity");

            assertThat(TestableIdentityPlugin.loadedProperties())
                    .containsOnly(
                            entry("rendered-key", "rendered-secret-path/secret-value"));
        }

        @Test
        void shouldReturnIdentityVariablesFromDifferentIdentities() throws Exception {
            var service = new IdentityService(
                    Map.of(),
                    Map.of(
                            "MockIdentity1",
                            new Identity(null, null, Map.of("plain-key-1", "plain-value-1"),
                                    new Plugin(TestableIdentityPlugin.class.getName(), Map.of())),
                            "MockIdentity2",
                            new Identity(null, null, Map.of("plain-key-2", "plain-value-2"),
                                    new Plugin(TestableIdentityPlugin.class.getName(), Map.of()))));

            var variables1 = service.variablesFor("MockIdentity1");
            var variables2 = service.variablesFor("MockIdentity2");

            assertThat(variables1)
                    .containsOnly(
                            entry("plain-key-1", "plain-value-1"),
                            entry("identity-plugin-key", "identity-plugin-value"));

            assertThat(variables2)
                    .containsOnly(
                            entry("plain-key-2", "plain-value-2"),
                            entry("identity-plugin-key", "identity-plugin-value"));
        }

        @Test
        void shouldThrowWhenIdentityIsUnknown() {
            var service = new IdentityService(
                    Map.of(),
                    Map.of("MockIdentity",
                            new Identity(null, null, Map.of("plain-key", "plain-value"), null)));

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> service.variablesFor("UnknownIdentity"))
                    .withMessage("Cannot find identity for 'UnknownIdentity'");
        }

        @Test
        void shouldThrowWhenSecretIsUnknown() {
            var service = new IdentityService(
                    Map.of(),
                    Map.of("MockIdentity",
                            new Identity(null, "UnknownSecrets", Map.of("plain-key", "plain-value"), null)));

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> service.variablesFor("MockIdentity"))
                    .withMessage("Cannot find secret plugin 'UnknownSecrets'");
        }

        @Test
        void shouldThrowWhenIdentityPluginIsUnknown() {
            var service = new IdentityService(
                    Map.of(),
                    Map.of("MockIdentity",
                            new Identity(null, null, Map.of(),
                                    new Plugin(UnknownIdentityPlugin.class.getName(), Map.of()))));

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> service.variablesFor("MockIdentity"))
                    .withMessage(
                            "Cannot find plugin 'io.blt.gregbot.core.services.IdentityServiceTest$UnknownIdentityPlugin'");
        }

        @Test
        void shouldThrowWhenSecretPluginIsUnknown() {
            var service = new IdentityService(
                    Map.of("MockSecret",
                            new Secret(new Plugin(UnknownSecretPlugin.class.getName(), Map.of()))),
                    Map.of("MockIdentity",
                            new Identity(null, "MockSecret", Map.of(),
                                    new Plugin(TestableIdentityPlugin.class.getName(), Map.of()))));

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> service.variablesFor("MockIdentity"))
                    .withMessage(
                            "Cannot find plugin 'io.blt.gregbot.core.services.IdentityServiceTest$UnknownSecretPlugin'");
        }

        @Nested
        class PluginInstances {

            @BeforeEach
            void beforeEach() {
                // I don't like having state between tests but the alternative that I see isn't much better; I can
                // imagine pulling out the getOrComputeIdentityPlugin method to a class and testing its result. This is
                // great, but it doesn't solve the same issue with SecretPlugin without adding a public method to expose
                // it just for testing. Modifying the interface of an implementation for testing purposes opens the
                // class to abuse and is worse in my opinion.
                TestableIdentityPlugin.resetInstanceCount();
                TestableSecretPlugin.resetInstanceCount();
            }

            @Test
            void shouldUseSameInstanceOfIdentityPluginOnMultipleCalls() throws Exception {
                var service = new IdentityService(
                        Map.of(),
                        Map.of("MockIdentity1",
                                new Identity(null, null, Map.of(),
                                        new Plugin(TestableIdentityPlugin.class.getName(), Map.of()))));

                service.variablesFor("MockIdentity1");
                service.variablesFor("MockIdentity1");

                assertThat(TestableIdentityPlugin.instanceCount())
                        .isOne();
            }

            @Test
            void shouldUseSameInstancesOfIdentityPluginForIdenticalIdentityProperties() throws Exception {
                var service = new IdentityService(
                        Map.of(),
                        Map.of(
                                "MockIdentity1",
                                new Identity(null, null, Map.of(),
                                        new Plugin(TestableIdentityPlugin.class.getName(), Map.of())),
                                "MockIdentity2",
                                new Identity(null, null, Map.of(),
                                        new Plugin(TestableIdentityPlugin.class.getName(), Map.of()))));

                service.variablesFor("MockIdentity1");
                service.variablesFor("MockIdentity2");

                assertThat(TestableIdentityPlugin.instanceCount())
                        .isOne();
            }

            @Test
            void shouldUseDifferentInstanceOfIdentityPluginForDifferentIdentityProperties() throws Exception {
                var plugin = new Plugin(TestableIdentityPlugin.class.getName(), Map.of());

                var service = new IdentityService(
                        Map.of(),
                        Map.of(
                                "MockIdentity1",
                                new Identity(null, null, Map.of("k", "v"), plugin),
                                "MockIdentity2",
                                new Identity(null, null, Map.of("k", "different"), plugin)));

                service.variablesFor("MockIdentity1");
                service.variablesFor("MockIdentity2");

                assertThat(TestableIdentityPlugin.instanceCount())
                        .isEqualTo(2);
            }

            @Test
            void shouldUseDifferentInstanceOfIdentityPluginForDifferentIdentityPluginProperties() throws Exception {
                var service = new IdentityService(
                        Map.of(),
                        Map.of(
                                "MockIdentity1",
                                new Identity(null, null, Map.of(),
                                        new Plugin(TestableIdentityPlugin.class.getName(), Map.of("k", "v"))),
                                "MockIdentity2",
                                new Identity(null, null, Map.of(),
                                        new Plugin(TestableIdentityPlugin.class.getName(), Map.of("k", "different")))));

                service.variablesFor("MockIdentity1");
                service.variablesFor("MockIdentity2");

                assertThat(TestableIdentityPlugin.instanceCount())
                        .isEqualTo(2);
            }

            @Test
            void shouldUseSameInstanceOfSecretPluginOnMultipleCalls() throws Exception {
                var identityPlugin = new Plugin(TestableIdentityPlugin.class.getName(), Map.of());

                var service = new IdentityService(
                        Map.of("MockSecret",
                                new Secret(new Plugin(TestableSecretPlugin.class.getName(), Map.of()))),
                        Map.of(
                                "MockIdentity1",
                                new Identity(null, "MockSecret", Map.of(), identityPlugin),
                                "MockIdentity2",
                                new Identity(null, "MockSecret", Map.of(), identityPlugin)
                        ));

                service.variablesFor("MockIdentity1");
                service.variablesFor("MockIdentity2");

                assertThat(TestableSecretPlugin.instanceCount())
                        .isOne();
            }

            @Test
            void shouldUseSameInstancesOfSecretPluginForIdenticalSecretProperties() throws Exception {
                var identityPlugin = new Plugin(TestableIdentityPlugin.class.getName(), Map.of());

                var service = new IdentityService(
                        Map.of(
                                "MockSecret1",
                                new Secret(new Plugin(TestableSecretPlugin.class.getName(), Map.of("k", "v"))),
                                "MockSecret2",
                                new Secret(new Plugin(TestableSecretPlugin.class.getName(), Map.of("k", "v")))),
                        Map.of(
                                "MockIdentity1",
                                new Identity(null, "MockSecret1", Map.of(), identityPlugin),
                                "MockIdentity2",
                                new Identity(null, "MockSecret2", Map.of(), identityPlugin)));

                service.variablesFor("MockIdentity1");
                service.variablesFor("MockIdentity2");

                assertThat(TestableSecretPlugin.instanceCount())
                        .isOne();
            }

            @Test
            void shouldUseDifferentInstancesOfSecretPluginForDifferentSecretPluginProperties() throws Exception {
                var identityPlugin = new Plugin(TestableIdentityPlugin.class.getName(), Map.of());

                var service = new IdentityService(
                        Map.of(
                                "MockSecret1",
                                new Secret(new Plugin(TestableSecretPlugin.class.getName(), Map.of("k", "v"))),
                                "MockSecret2",
                                new Secret(new Plugin(TestableSecretPlugin.class.getName(), Map.of("k", "different")))),
                        Map.of(
                                "MockIdentity1",
                                new Identity(null, "MockSecret1", Map.of(), identityPlugin),
                                "MockIdentity2",
                                new Identity(null, "MockSecret2", Map.of(), identityPlugin)));

                service.variablesFor("MockIdentity1");
                service.variablesFor("MockIdentity2");

                assertThat(TestableSecretPlugin.instanceCount())
                        .isEqualTo(2);
            }

        }

    }

    static class UnknownIdentityPlugin implements IdentityPlugin {

        @Override
        public Map<String, String> variables() {
            return Map.of();
        }

        @Override
        public void load(Map<String, String> properties) {
        }
    }

    static class UnknownSecretPlugin implements SecretPlugin {
        @Override
        public Map<String, String> secretsForPath(String path) {
            return Map.of();
        }

        @Override
        public void load(Map<String, String> properties) {
        }
    }

}
