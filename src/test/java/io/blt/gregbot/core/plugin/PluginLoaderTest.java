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
import io.blt.gregbot.plugin.secrets.SecretPlugin;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        void loadShouldCallSecretPluginLoadPassingProperties() throws PluginException {
            var properties = Map.of("mock-key", "mock-value");
            var plugin = new Properties.Plugin(TestableSecretPlugin.TYPE, properties);

            var loadedProperties = ((TestableSecretPlugin) loader.load(plugin))
                    .loadedProperties();

            assertThat(loadedProperties)
                    .containsExactlyEntriesOf(properties);
        }
    }

    interface InterfaceWithNoImplementations extends Plugin {}

}
