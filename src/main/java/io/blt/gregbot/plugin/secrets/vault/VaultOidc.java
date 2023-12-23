/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import io.blt.gregbot.plugin.PluginContext;
import io.blt.gregbot.plugin.secrets.SecretException;
import io.blt.gregbot.plugin.secrets.SecretPlugin;
import io.blt.gregbot.plugin.secrets.vault.connector.VaultConnector;
import io.blt.gregbot.plugin.secrets.vault.oidc.Oidc;
import io.blt.gregbot.plugin.secrets.vault.oidc.OidcConfig;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class VaultOidc implements SecretPlugin {

    private Vault vault;

    @Override
    public Set<String> requiredProperties() {
        return Set.of("host");
    }

    @Override
    public void load(PluginContext context, Map<String, String> properties) throws SecretException {
        var host = Objects.requireNonNull(properties.get("host"), "must specify 'host' property");
        var engineVersion = Integer.valueOf(properties.getOrDefault("engine", "2"));

        var connector = new VaultConnector(host);

        try {
            var token = new Oidc(OidcConfig.from(properties), connector)
                    .fetchAuthTokenUsingDesktopBrowse();

            vault = new Vault(new VaultConfig()
                    .address(host)
                    .engineVersion(engineVersion)
                    .token(token)
                    .build());

            // Ensure the token is good
            vault.auth().renewSelf();
        } catch (IOException | InterruptedException | TimeoutException | VaultException e) {
            throw new SecretException("Failed to load using properties: " + properties, e);
        }
    }

    @Override
    public Map<String, String> secretsForPath(String path) throws SecretException {
        try {
            return vault.logical()
                    .read(path)
                    .getData();
        } catch (VaultException e) {
            throw new SecretException("Failed to fetch a secret for path: " + path, e);
        }
    }
}
