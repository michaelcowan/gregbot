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
import java.util.NoSuchElementException;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Manages the loading of identity plugins.
 * This includes loading and rendering any secrets.
 */
public class IdentityService {

    private final Map<String, Secret> secrets;
    private final Map<String, Identity> identities;
    private final Map<Secret, SecretRenderer> secretRenderers = new HashMap<>();
    private final Map<Identity, IdentityPlugin> identityPlugins = new HashMap<>();
    private final PluginLoader<SecretPlugin> secretLoader = new PluginLoader<>(SecretPlugin.class);
    private final PluginLoader<IdentityPlugin> identityLoader = new PluginLoader<>(IdentityPlugin.class);

    public IdentityService(
            Map<String, Secret> secrets,
            Map<String, Identity> identities) {
        this.secrets = secrets;
        this.identities = identities;
    }

    /**
     * Returns a fully rendered variable map for the specified identity.
     *
     * @param identity Identity to return variables for
     * @return fully rendered variable map
     * @throws IdentityServiceException if there is an error resolving identity variables
     * @throws NoSuchElementException   if an identity, secret or plugin cannot be found
     */
    public Map<String, String> variablesFor(String identity) throws IdentityServiceException {
        var i = Ex.throwIf(identities.get(identity), Objects::isNull,
                () -> new NoSuchElementException("Cannot find identity for '%s'".formatted(identity)));

        var variables = new HashMap<>(i.variables());

        var plugin = getOrComputeIdentityPlugin(i);
        if (nonNull(plugin)) {
            variables.putAll(
                    Ex.transformExceptions(
                            plugin::variables,
                            IdentityServiceException::new));
        }

        return variables;
    }

    private IdentityPlugin getOrComputeIdentityPlugin(Identity identity) throws IdentityServiceException {
        return Ctr.computeIfAbsent(identityPlugins, identity, i ->
                Ex.transformExceptions(
                        () -> loadIdentityPlugin(i),
                        IdentityServiceException::new));
    }

    private IdentityPlugin loadIdentityPlugin(Identity identity) throws PluginException, SecretRenderException {
        var rendered = renderedPluginProperties(identity);
        return isNull(rendered) ? null : identityLoader.load(rendered);
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
        return Ex.throwIf(secrets.get(name), Objects::isNull,
                () -> new NoSuchElementException("Cannot find secret plugin '%s'".formatted(name)));
    }

    private SecretRenderer getOrComputeSecretRenderer(Secret secret) throws PluginException {
        return Ctr.computeIfAbsent(secretRenderers, secret, s -> new SecretRenderer(loadSecretPlugin(s)));
    }

    private SecretPlugin loadSecretPlugin(Secret secret) throws PluginException {
        return secretLoader.load(secret.plugin());
    }

}
