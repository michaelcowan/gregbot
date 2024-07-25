/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.plugin;

import io.blt.gregbot.plugin.identities.IdentityPlugin;
import java.util.HashMap;
import java.util.Map;

public class TestableIdentityPlugin implements IdentityPlugin {

    static final String TYPE = "io.blt.gregbot.core.plugin.TestableIdentityPlugin";

    private static Map<String, String> loadedProperties;
    private static int instanceCount = 0;

    public TestableIdentityPlugin() {
        instanceCount++;
    }

    @Override
    public void load(Map<String, String> properties) {
        loadedProperties = new HashMap<>();
        loadedProperties.putAll(properties);
    }

    @Override
    public Map<String, String> variables() {
        return Map.of("identity-plugin-key", "identity-plugin-value");
    }

    public static Map<String, String> loadedProperties() {
        return loadedProperties;
    }

    public static int instanceCount() {
        return instanceCount;
    }

    public static void resetInstanceCount() {
        instanceCount = 0;
    }
}
