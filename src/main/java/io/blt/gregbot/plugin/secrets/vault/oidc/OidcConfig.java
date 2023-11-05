/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault.oidc;

import java.util.HashMap;
import java.util.Map;

import static io.blt.gregbot.plugin.utils.PluginUtils.propertiesToType;

public record OidcConfig(
        String role,
        String mount,
        String listenHost,
        int listenPort,
        String listenPath,
        int listenTimeout,
        String callbackScheme,
        String callbackHost,
        int callbackPort,
        String callbackPath) {

    /**
     * Creates an instance of {@link OidcConfig} by copying values from the passed {@code properties}.
     * If a value is not present in {@code properties} then a default is used.
     * <p>
     *     Supported properties and their default values:
     *     <ul>
     *         <li>{@code role} - {@code ""}</li>
     *         <li>{@code mount} - {@code "oidc"}</li>
     *         <li>{@code listenHost} - {@code "localhost"}</li>
     *         <li>{@code listenPort} - {@code "8250"}</li>
     *         <li>{@code listenPath} - '/{mount}/callback' e.g. {@code "/oidc/callback"}</li>
     *         <li>{@code listenTimeout} - {@code "10"}</li>
     *         <li>{@code callbackScheme} - {@code "http"}</li>
     *         <li>{@code callbackHost} - same as {@code listenHost}</li>
     *         <li>{@code callbackPort} - same as {@code listenPort}</li>
     *         <li>{@code callbackPath} - same as {@code listenPath}</li>
     *     </ul>
     * </p>
     * <p>
     *     These values are equivalent to the Vault CLI OIDC login options
     *     {@see <a href="https://developer.hashicorp.com/vault/docs/auth/jwt#oidc-login-cli">Vault OIDC Login CLI</a>}
     * </p>
     *
     * @param properties {@link Map} that may contain configuration values
     * @return an instance of {@link OidcConfig} constructed from defaults and passed {@code properties}
     * @throws NullPointerException if properties is null.
     */
    public static OidcConfig from(Map<String, String> properties) {
        var config = new HashMap<>(properties);

        config.putIfAbsent("role", "");
        config.putIfAbsent("mount", "oidc");
        config.putIfAbsent("listenHost", "localhost");
        config.putIfAbsent("listenPort", "8250");
        config.putIfAbsent("listenPath", "/" + config.get("mount") + "/callback");
        config.putIfAbsent("listenTimeout", "10");
        config.putIfAbsent("callbackScheme", "http");
        config.putIfAbsent("callbackHost", config.get("listenHost"));
        config.putIfAbsent("callbackPort", config.get("listenPort"));
        config.putIfAbsent("callbackPath", config.get("listenPath"));

        return propertiesToType(OidcConfig.class, config);
    }
}
