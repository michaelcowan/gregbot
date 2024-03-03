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

    @Test
    void shouldLoadAndFindIdentityPluginWithoutSecretPlugin() throws IdentityServiceException {
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
    void shouldLoadAndFindIdentityPluginWithSecretPlugin() throws IdentityServiceException {
        var service = new IdentityService(
                Map.of("SecretPlugin", new Secret(
                        new Plugin(TestableSecretPlugin.class.getName(), Map.of()))),
                Map.of("IdentityPlugin", new Identity(
                        null, "SecretPlugin", Map.of(),
                        new Plugin(TestableIdentityPlugin.class.getName(), Map.of()))));

        var result = service.find("IdentityPlugin");

        assertThat(result)
                .isNotEmpty().get()
                .isInstanceOf(TestableIdentityPlugin.class);
    }

    @Test
    void shouldRenderSecret() throws IdentityServiceException {
        var service = new IdentityService(
                Map.of("SecretPlugin", new Secret(
                        new Plugin(TestableSecretPlugin.class.getName(), Map.of()))),
                Map.of("IdentityPlugin", new Identity(
                        null, "SecretPlugin", Map.of(),
                        new Plugin(TestableIdentityPlugin.class.getName(),
                                Map.of("secret", "[my/secret/path/test-secret]")))));

        var result = (TestableIdentityPlugin) service.find("IdentityPlugin").get();

        assertThat(result.loadedProperties())
                .containsEntry("secret", "my/secret/path-test-value");
    }

    @Test
    void findShouldReturnEmptyWhenSpecifiedIdentityDoesNotExist() throws IdentityServiceException {
        var service = new IdentityService(Map.of(), Map.of());

        var result = service.find("DoesNotExist");

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldThrowWhenSpecifiedSecretPluginDoesNotExist() {
        var service = new IdentityService(
                Map.of("SecretPlugin", new Secret(
                        new Plugin(TestableSecretPlugin.class.getName(), Map.of()))),
                Map.of("IdentityPlugin", new Identity(
                        null, "DoesNotExist", Map.of(),
                        new Plugin(TestableIdentityPlugin.class.getName(), Map.of()))));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.find("IdentityPlugin"))
                .withMessage("Cannot find secret plugin DoesNotExist");
    }

    @Test
    void shouldRaiseExceptionWhenIdentityPluginThrows() {
        var service = new IdentityService(
                Map.of(),
                Map.of("IdentityPlugin", new Identity(
                        null, null, Map.of(),
                        new Plugin(ThrowOnLoadIdentityPlugin.class.getName(), Map.of()))));

        assertThatExceptionOfType(IdentityServiceException.class)
                .isThrownBy(() -> service.find("IdentityPlugin"))
                .havingCause()
                .isInstanceOf(PluginException.class)
                .withMessage("identity plugin test load exception");
    }

    @Test
    void shouldRaiseExceptionWhenSecretPluginThrows() {
        var service = new IdentityService(
                Map.of("SecretPlugin", new Secret(
                        new Plugin(ThrowOnLoadSecretPlugin.class.getName(), Map.of()))),
                Map.of("IdentityPlugin", new Identity(
                        null, "SecretPlugin", Map.of(),
                        new Plugin(TestableIdentityPlugin.class.getName(), Map.of()))));

        assertThatExceptionOfType(IdentityServiceException.class)
                .isThrownBy(() -> service.find("IdentityPlugin"))
                .havingCause()
                .isInstanceOf(PluginException.class)
                .withMessage("secret plugin test load exception");
    }

}
