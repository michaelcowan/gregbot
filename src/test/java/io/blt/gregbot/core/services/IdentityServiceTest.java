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
import io.blt.gregbot.plugin.PluginException;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class IdentityServiceTest {

    @Test
    void shouldLoadAndFindIdentityPluginWithoutSecretPlugin() throws PluginException {
        var service = new IdentityService(
                Map.of("SecretPlugin", new Secret(
                        new Plugin(TestableSecretPlugin.class.getName(), Map.of()))),
                Map.of("IdentityPlugin", new Identity(
                        null, null, Map.of(),
                        new Plugin(TestableIdentityPlugin.class.getName(), Map.of()))));

        var result = service.find("IdentityPlugin");

        assertThat(result)
                .isNotEmpty().get()
                .isInstanceOf(TestableIdentityPlugin.class);
    }

    @Test
    void shouldLoadAndFindIdentityPluginWithSecretPlugin() throws PluginException {
        var service = new IdentityService(
                Map.of("SecretPlugin", new Secret(
                        new Plugin(TestableSecretPlugin.class.getName(), Map.of()))),
                Map.of("IdentityPlugin", new Identity(
                        null, "SecretPlugin", Map.of(),
                        new Plugin(TestableIdentityPlugin.class.getName(), Map.of()))));

        var result = service.find("IdentityPlugin");

        assertThat(result)
                .isNotEmpty().get()
    }

    @Test
    void findShouldReturnEmptyWhenSpecifiedIdentityDoesNotExist() throws PluginException {
        var service = new IdentityService(Map.of(), Map.of());

        var result = service.find("DoesNotExist");

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldThrowWhenSpecifiedSecretPluginDoesNotExist() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new IdentityService(
                        Map.of("SecretPlugin", new Secret(
                                new Plugin(TestableSecretPlugin.class.getName(), Map.of()))),
                        Map.of("IdentityPlugin", new Identity(
                                null, "DoesNotExist", Map.of(),
                                new Plugin(TestableIdentityPlugin.class.getName(), Map.of())))))
                .withMessage("Cannot find secret plugin DoesNotExist");
    }

    @Test
    void shouldBubbleUpPluginExceptionFromIdentityPlugin() {
        assertThatExceptionOfType(PluginException.class)
                .isThrownBy(() -> new IdentityService(
                        Map.of(),
                        Map.of("IdentityPlugin", new Identity(
                                null, null, Map.of(),
                                new Plugin(ThrowOnLoadIdentityPlugin.class.getName(), Map.of())))))
                .withMessage("identity plugin test load exception");
    }

    @Test
    void shouldBubbleUpPluginExceptionFromSecretPlugin() {
        assertThatExceptionOfType(PluginException.class)
                .isThrownBy(() -> new IdentityService(
                        Map.of("SecretPlugin", new Secret(
                                new Plugin(ThrowOnLoadSecretPlugin.class.getName(), Map.of()))),
                        Map.of("IdentityPlugin", new Identity(
                                null, "SecretPlugin", Map.of(),
                                new Plugin(TestableIdentityPlugin.class.getName(), Map.of())))))
                .withMessage("secret plugin test load exception");
    }

}
