/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.plugin;

import io.blt.gregbot.core.properties.Properties;
import io.blt.gregbot.plugin.Plugin;
import io.blt.gregbot.plugin.PluginException;
import io.blt.gregbot.plugin.identities.IdentityPlugin;
import io.blt.gregbot.plugin.secrets.SecretPlugin;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PluginLoaderTest {

    @Test
    void pluginsShouldReturnEmptyForInterfaceWithNoImplementations() {
        var plugins = new PluginLoader<>(InterfaceWithNoImplementations.class)
                .plugins();

        assertThat(plugins)
                .isEmpty();
    }

    @Nested
    class SecretPluginInterface {

        PluginLoader<?> loader = new PluginLoader<>(SecretPlugin.class);

        @Test
        void pluginsShouldReturnListOfAllPluginTypes() {
            var plugins = loader.plugins();

            assertThat(plugins)
                    .isNotEmpty()
                    .contains(TestableSecretPlugin.TYPE);
        }

        @Test
        void loadShouldReturnInstanceOfSecretPlugin() throws PluginException {
            var plugin = new Properties.Plugin(TestableSecretPlugin.TYPE, Map.of());

            var result = loader.load(plugin);

            assertThat(result)
                    .isInstanceOf(SecretPlugin.class);
        }

        @Test
        void loadShouldReturnDifferentInstanceOfSecretPluginOnEachCall() throws PluginException {
            var plugin = new Properties.Plugin(TestableSecretPlugin.TYPE, Map.of());

            var result1 = loader.load(plugin);
            var result2 = loader.load(plugin);

            assertThat(result1)
                    .isNotEqualTo(result2);
        }

        @Test
        void loadShouldCallSecretPluginLoadPassingProperties() throws PluginException {
            var properties = Map.of("mock-key", "mock-value");
            var plugin = new Properties.Plugin(TestableSecretPlugin.TYPE, properties);

            var result = ((TestableSecretPlugin) loader.load(plugin));

            assertThat(result.loadedProperties())
                    .containsExactlyEntriesOf(properties);
        }

        @Test
        void loadShouldThrowWhenSpecifiedPluginIsNull() {
            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> loader.load(null));
        }

        @Test
        void loadShouldThrowWhenPluginTypeCannotBeFound() {
            var plugin = new Properties.Plugin("UnknownSecretPluginType", Map.of());

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> loader.load(plugin))
                    .withMessage("Cannot find plugin 'UnknownSecretPluginType'");
        }

        @Test
        void loadShouldThrowWhenPluginLoadThrows() {
            var plugin = new Properties.Plugin(ThrowOnLoadSecretPlugin.TYPE, Map.of());

            assertThatExceptionOfType(PluginException.class)
                    .isThrownBy(() -> loader.load(plugin))
                    .withMessage("secret plugin test load exception");
        }
    }

    @Nested
    class IdentityPluginInterface {

        PluginLoader<?> loader = new PluginLoader<>(IdentityPlugin.class);

        @Test
        void pluginsShouldReturnListOfAllPluginTypes() {
            var plugins = loader.plugins();

            assertThat(plugins)
                    .isNotEmpty()
                    .contains(TestableIdentityPlugin.TYPE);
        }

        @Test
        void loadShouldReturnInstanceOfIdentityPlugin() throws PluginException {
            var plugin = new Properties.Plugin(TestableIdentityPlugin.TYPE, Map.of());

            var result = loader.load(plugin);

            assertThat(result)
                    .isInstanceOf(IdentityPlugin.class);
        }

        @Test
        void loadShouldReturnDifferentInstanceOfIdentityPluginOnEachCall() throws PluginException {
            var plugin = new Properties.Plugin(TestableIdentityPlugin.TYPE, Map.of());

            var result1 = loader.load(plugin);
            var result2 = loader.load(plugin);

            assertThat(result1)
                    .isNotEqualTo(result2);
        }

        @Test
        void loadShouldCallIdentityPluginLoadPassingProperties() throws PluginException {
            var properties = Map.of("mock-key", "mock-value");
            var plugin = new Properties.Plugin(TestableIdentityPlugin.TYPE, properties);

            var result = ((TestableIdentityPlugin) loader.load(plugin));

            assertThat(result.loadedProperties())
                    .containsExactlyEntriesOf(properties);
        }

        @Test
        void loadShouldThrowWhenSpecifiedPluginIsNull() {
            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> loader.load(null));
        }

        @Test
        void loadShouldThrowWhenPluginTypeCannotBeFound() {
            var plugin = new Properties.Plugin("UnknownIdentityPluginType", Map.of());

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> loader.load(plugin))
                    .withMessage("Cannot find plugin 'UnknownIdentityPluginType'");
        }

        @Test
        void loadShouldThrowWhenPluginLoadThrows() {
            var plugin = new Properties.Plugin(ThrowOnLoadIdentityPlugin.TYPE, Map.of());

            assertThatExceptionOfType(PluginException.class)
                    .isThrownBy(() -> loader.load(plugin))
                    .withMessage("identity plugin test load exception");
        }
    }

    interface InterfaceWithNoImplementations extends Plugin {}

}
