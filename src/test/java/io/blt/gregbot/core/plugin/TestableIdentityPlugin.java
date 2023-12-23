/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.plugin;

import io.blt.gregbot.plugin.PluginContext;
import io.blt.gregbot.plugin.identities.IdentityPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestableIdentityPlugin implements IdentityPlugin {

    static String TYPE = "io.blt.gregbot.core.plugin.TestableIdentityPlugin";

    private PluginContext loadedContext;
    private final Map<String, String> loadedProperties = new HashMap<>();

    @Override
    public Set<String> requiredProperties() {
        return null;
    }

    @Override
    public void load(PluginContext context, Map<String, String> properties) {
        loadedContext = context;
        loadedProperties.putAll(properties);
    }

    @Override
    public Map<String, String> variables() {
        return null;
    }

    public PluginContext loadedContext() {
        return loadedContext;
    }

    public Map<String, String> loadedProperties() {
        return loadedProperties;
    }
}
