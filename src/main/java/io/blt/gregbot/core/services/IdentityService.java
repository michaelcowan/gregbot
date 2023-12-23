/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.services;

import io.blt.gregbot.core.plugin.PluginLoader;
import io.blt.gregbot.core.properties.Properties.Identity;
import io.blt.gregbot.core.properties.Properties.Secret;
import io.blt.gregbot.plugin.PluginContext;
import io.blt.gregbot.plugin.PluginException;
import io.blt.gregbot.plugin.identities.IdentityPlugin;
import io.blt.gregbot.plugin.secrets.SecretPlugin;
import io.blt.util.Ctr;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Manages the loading of identity plugins and any secret plugin dependencies.
 */
public class IdentityService {

    private final Map<String, SecretPlugin> secretPlugins;
    private final Map<String, IdentityPlugin> identityPlugins;

    public IdentityService(
            Map<String, Secret> secrets,
            Map<String, Identity> identities) throws PluginException {
        this.secretPlugins = loadSecretPlugins(secrets);
        this.identityPlugins = loadIdentityPlugins(identities);
    }

    /**
     * Finds a fully loaded {@link IdentityPlugin} instance based on the provided identity name.
     *
     * @param identity the identity name to search for
     * @return the found {@link IdentityPlugin} or empty if not found
     */
    public Optional<IdentityPlugin> find(String identity) {
        return Optional.ofNullable(identityPlugins.get(identity));
    }

    private Map<String, SecretPlugin> loadSecretPlugins(Map<String, Secret> secrets) throws PluginException {
        var loader = new PluginLoader<>(SecretPlugin.class);
        return Ctr.transformValues(secrets, s -> loader.load(s.plugin()));
    }

    private Map<String, IdentityPlugin> loadIdentityPlugins(Map<String, Identity> identities) throws PluginException {
        var loader = new PluginLoader<>(IdentityPlugin.class);
        return Ctr.transformValues(identities, i -> {
            var secretPlugin = nonNull(i.secrets()) ? findSecretPlugin(i.secrets()) : null;
            return loader.load(new PluginContext(secretPlugin), i.plugin());
        });
    }

    private SecretPlugin findSecretPlugin(String name) {
        var result = secretPlugins.get(name);
        if (isNull(result)) {
            throw new IllegalArgumentException("Cannot find secret plugin " + name);
        }
        return result;
    }

}
