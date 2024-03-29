/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.plugin;

import io.blt.gregbot.plugin.secrets.SecretPlugin;
import java.util.HashMap;
import java.util.Map;

public class TestableSecretPlugin implements SecretPlugin {
    
    static String TYPE = "io.blt.gregbot.core.plugin.TestableSecretPlugin";

    private final Map<String, String> loadedProperties = new HashMap<>();

    @Override
    public void load(Map<String, String> properties) {
        loadedProperties.putAll(properties);
    }

    @Override
    public Map<String, String> secretsForPath(String path) {
        return Map.of("test-secret", path + "-test-value");
    }

    public Map<String, String> loadedProperties() {
        return loadedProperties;
    }
}
