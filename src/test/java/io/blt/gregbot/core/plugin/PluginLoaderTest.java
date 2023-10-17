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
import io.blt.gregbot.plugin.secrets.SecretPlugin;
import io.blt.gregbot.plugin.secrets.vault.VaultOidc;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class PluginLoaderTest {

    interface InterfaceWithNoImplementations extends Plugin { }

    @Test
    void pluginsShouldReturnEmptyForInterfaceWithNoImplementations() {
        var plugins = new PluginLoader<>(InterfaceWithNoImplementations.class)
                .plugins();

        assertThat(plugins)
                .isEmpty();
    }

    @Nested
    class SecretPluginInterface {

        static Stream<Arguments> loadShouldReturnInstanceOfPluginType() {
            return Stream.of(
                    Arguments.of(
                            "io.blt.gregbot.plugin.secrets.vault.VaultOidc",
                            Map.of("host", "https://vault.cyberdyne.com"),
                            VaultOidc.class));
        }

        @ParameterizedTest
        @MethodSource
        void loadShouldReturnInstanceOfPluginType(
                String type, Map<String, String> properties, Class<? extends Plugin> expected) throws Exception {
            var plugin = new PluginLoader<>(SecretPlugin.class)
                    .load(new Properties.Plugin(type, properties));

            assertThat(plugin)
                    .isNotNull()
                    .isInstanceOf(expected);
        }

        @Test
        void pluginsShouldReturnListOfAllPluginTypes() {
            var plugins = new PluginLoader<>(SecretPlugin.class)
                    .plugins();

            assertThat(plugins)
                    .isNotEmpty()
                    .containsExactlyInAnyOrder(
                            "io.blt.gregbot.plugin.secrets.vault.VaultOidc"
                    );
        }
    }

}
