/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.services;

import io.blt.gregbot.core.plugin.PluginLoader;
import io.blt.gregbot.core.plugin.SecretRenderException;
import io.blt.gregbot.core.plugin.SecretRenderer;
import io.blt.gregbot.core.properties.Properties;
import io.blt.gregbot.core.properties.Properties.Identity;
import io.blt.gregbot.core.properties.Properties.Secret;
import io.blt.gregbot.plugin.identities.IdentityPlugin;
import io.blt.gregbot.plugin.secrets.SecretPlugin;
import io.blt.util.Ctr;
import io.blt.util.Ex;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;

/**
 * Manages the loading of identity plugins.
 * This includes loading and rendering any secrets.
 */
public class IdentityService {

    private final Map<String, SecretRenderer> secretRenderers;
    private final Map<String, IdentityPlugin> identityPlugins;

    public IdentityService(
            Map<String, Secret> secrets,
            Map<String, Identity> identities) throws IdentityServiceException {
        this.secretRenderers = Ctr.transformValues(loadSecretPlugins(secrets), SecretRenderer::new);
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

    private Map<String, SecretPlugin> loadSecretPlugins(Map<String, Secret> secrets)
            throws IdentityServiceException {
        var loader = new PluginLoader<>(SecretPlugin.class);
        return Ctr.transformValues(secrets,
                s -> Ex.transformExceptions(() -> loader.load(s.plugin()), IdentityServiceException::new));
    }

    private Map<String, IdentityPlugin> loadIdentityPlugins(Map<String, Identity> identities)
            throws IdentityServiceException {
        var loader = new PluginLoader<>(IdentityPlugin.class);
        return Ctr.transformValues(identities,
                i -> Ex.transformExceptions(() -> loader.load(resolvedPlugin(i)), IdentityServiceException::new));
    }

    private Properties.Plugin resolvedPlugin(Identity identity) throws SecretRenderException {
        if (isNull(identity.secrets())) {
            return identity.plugin();
        }

        var secretRenderer = findSecretRenderer(identity.secrets());

        var properties = Ctr.transformValues(identity.plugin().properties(), secretRenderer::render);

        return new Properties.Plugin(identity.plugin().type(), properties);
    }

    private SecretRenderer findSecretRenderer(String name) {
        var result = secretRenderers.get(name);
        if (isNull(result)) {
            throw new IllegalArgumentException("Cannot find secret plugin " + name);
        }
        return result;
    }

}
