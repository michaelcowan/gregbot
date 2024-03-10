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
import io.blt.gregbot.plugin.PluginException;
import io.blt.gregbot.plugin.identities.IdentityPlugin;
import io.blt.gregbot.plugin.secrets.SecretPlugin;
import io.blt.util.Ctr;
import io.blt.util.Ex;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.Validate;

import static java.util.Objects.isNull;

/**
 * Manages the loading of identity plugins.
 * This includes loading and rendering any secrets.
 */
public class IdentityService {

    private final Map<String, Secret> secrets;
    private final Map<String, Identity> identities;
    private final Map<Secret, SecretRenderer> secretRenderers = new HashMap<>();
    private final Map<Identity, IdentityPlugin> identityPlugins = new HashMap<>();

    public IdentityService(
            Map<String, Secret> secrets,
            Map<String, Identity> identities) {
        this.secrets = secrets;
        this.identities = identities;
    }

    /**
     * Finds a fully loaded {@link IdentityPlugin} instance based on the provided identity name.
     *
     * @param identity the identity name to search for
     * @return the found {@link IdentityPlugin} or empty if not found
     */
    public Optional<IdentityPlugin> find(String identity) throws IdentityServiceException {
        var i = identities.get(identity);
        if (isNull(i)) {
            return Optional.empty();
        }
        return Optional.ofNullable(getOrComputeIdentityPlugin(i));
    }

    private IdentityPlugin getOrComputeIdentityPlugin(Identity identity) throws IdentityServiceException {
        return Ctr.computeIfAbsent(identityPlugins, identity, i ->
                Ex.transformExceptions(
                        () -> loadIdentityPlugin(i),
                        IdentityServiceException::new));
    }

    private IdentityPlugin loadIdentityPlugin(Identity identity) throws PluginException, SecretRenderException {
        var loader = new PluginLoader<>(IdentityPlugin.class);
        return loader.load(renderedPluginProperties(identity));
    }

    private Properties.Plugin renderedPluginProperties(Identity identity)
            throws SecretRenderException, PluginException {
        if (isNull(identity.secrets())) {
            return identity.plugin();
        }

        var secretRenderer = getOrComputeSecretRenderer(findSecret(identity));

        var properties = Ctr.transformValues(identity.plugin().properties(), secretRenderer::render);

        return new Properties.Plugin(identity.plugin().type(), properties);
    }

    private Secret findSecret(Identity identity) {
        var name = identity.secrets();
        return Validate.notNull(secrets.get(name), "Cannot find secret plugin '%s'", name);
    }

    private SecretRenderer getOrComputeSecretRenderer(Secret secret) throws PluginException {
        return Ctr.computeIfAbsent(secretRenderers, secret, s -> new SecretRenderer(loadSecretPlugin(s)));
    }

    private SecretPlugin loadSecretPlugin(Secret secret) throws PluginException {
        var loader = new PluginLoader<>(SecretPlugin.class);
        return loader.load(secret.plugin());
    }

}
