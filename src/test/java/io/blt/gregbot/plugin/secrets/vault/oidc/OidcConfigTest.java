/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault.oidc;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class OidcConfigTest {

    @Test
    void fromShouldThrowNullPointerExceptionWhenPropertiesIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> OidcConfig.from(null));
    }

    @Test
    void fromShouldUsePassedProperties() {
        var result = OidcConfig.from(Map.of(
                "role", "mock-role",
                "mount", "mock-mount",
                "listenHost", "mock-listen-host",
                "listenPort", "1",
                "listenPath", "mock-listen-path",
                "listenTimeout", "2",
                "callbackScheme", "mock-callback-scheme",
                "callbackHost", "mock-callback-host",
                "callbackPort", "3",
                "callbackPath", "mock-callback-path"));

        assertThat(result)
                .isEqualTo(new OidcConfig(
                        "mock-role",
                        "mock-mount",
                        "mock-listen-host",
                        1,
                        "mock-listen-path",
                        2,
                        "mock-callback-scheme",
                        "mock-callback-host",
                        3,
                        "mock-callback-path"));
    }

    @Test
    void fromShouldUseDefaultValuesWhenNoPropertiesArePassed() {
        var result = OidcConfig.from(Map.of());

        assertThat(result)
                .isEqualTo(new OidcConfig(
                        "",
                        "oidc",
                        "localhost",
                        8250,
                        "/oidc/callback",
                        10,
                        "http",
                        "localhost",
                        8250,
                        "/oidc/callback"));
    }

    @Test
    void fromShouldIgnoreUnknownProperties() {
        var result = OidcConfig.from(Map.of("unknown-key", "unknown-value"));

        assertThat(result)
                .isEqualTo(new OidcConfig(
                        "",
                        "oidc",
                        "localhost",
                        8250,
                        "/oidc/callback",
                        10,
                        "http",
                        "localhost",
                        8250,
                        "/oidc/callback"));
    }

    @Test
    void fromShouldDefaultCallbackPropertiesToSameValueAsListenProperties() {
        var result = OidcConfig.from(Map.of(
                "listenHost", "mock-listen-host",
                "listenPort", "1234",
                "listenPath", "mock-listen-path"));

        assertThat(result)
                .extracting(
                        OidcConfig::callbackHost,
                        OidcConfig::callbackPort,
                        OidcConfig::callbackPath)
                .containsExactly(
                        "mock-listen-host",
                        1234,
                        "mock-listen-path");
    }

    @Test
    void fromShouldDefaultListenPathUsingMountVault() {
        var result = OidcConfig.from(Map.of("mount", "mock-mount"));

        assertThat(result)
                .extracting(OidcConfig::listenPath)
                .isEqualTo("/mock-mount/callback");
    }

}
