/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.identities.adobe;

import io.blt.gregbot.plugin.PluginException;
import io.blt.gregbot.plugin.identities.IdentityException;
import io.blt.gregbot.plugin.identities.IdentityPlugin;
import io.blt.gregbot.plugin.identities.adobe.connector.ImsConnector;
import io.blt.gregbot.plugin.identities.adobe.connector.ImsServiceConnector;
import io.blt.util.Ex;
import java.util.Map;
import java.util.Objects;

public class AdobeIms implements IdentityPlugin {

    private enum Type {
        SERVICE
    }

    private ImsConnector connector;
    private String variableKey;

    @Override
    public void load(Map<String, String> properties) throws PluginException {
        var host = properties.getOrDefault("host", "https://ims-na1.adobelogin.com");
        variableKey = properties.getOrDefault("variable", "token");

        switch (parseType(properties)) {
            case SERVICE -> {
                var id = requireProperty(properties, "id");
                var secret = requireProperty(properties, "secret");
                var code = requireProperty(properties, "code");
                var scope = properties.getOrDefault("scope", "");

                connector = Ex.transformExceptions(
                        () -> new ImsServiceConnector(host, id, secret, code, scope),
                        e -> new PluginException("Failed to authenticate using " + host, e));
            }
        }
    }

    @Override
    public Map<String, String> variables() throws IdentityException {
        var token = Ex.transformExceptions(
                () -> connector.token(),
                e -> new IdentityException("Failed to refresh authentication", e));

        return Map.of(variableKey, token);
    }

    private String requireProperty(Map<String, String> properties, String key) {
        return Objects.requireNonNull(properties.get(key), "must specify '" + key + "' property");
    }

    private Type parseType(Map<String, String> properties) {
        return Type.valueOf(requireProperty(properties, "type").toUpperCase());
    }

}
