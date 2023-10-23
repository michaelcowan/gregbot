/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.plugin;

import io.blt.gregbot.plugin.Plugin;
import io.blt.gregbot.plugin.secrets.SecretPlugin;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
