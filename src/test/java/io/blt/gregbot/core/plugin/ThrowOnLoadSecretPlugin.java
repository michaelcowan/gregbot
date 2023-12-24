/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.plugin;

import io.blt.gregbot.plugin.PluginException;
import io.blt.gregbot.plugin.secrets.SecretPlugin;
import java.util.Map;
import java.util.Set;

public class ThrowOnLoadSecretPlugin implements SecretPlugin {
    
    static String TYPE = "io.blt.gregbot.core.plugin.ThrowOnLoadSecretPlugin";

    @Override
    public Set<String> requiredProperties() {
        return null;
    }

    @Override
    public void load(Map<String, String> properties) throws PluginException {
        throw new PluginException("secret plugin test load exception", null);
    }

    @Override
    public Map<String, String> secretsForPath(String path) {
        return null;
    }

}
