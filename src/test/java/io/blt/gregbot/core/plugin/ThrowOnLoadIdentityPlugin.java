/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.plugin;

import io.blt.gregbot.plugin.PluginException;
import io.blt.gregbot.plugin.identities.IdentityException;
import io.blt.gregbot.plugin.identities.IdentityPlugin;
import java.util.Map;
import java.util.Set;

public class ThrowOnLoadIdentityPlugin implements IdentityPlugin {
    
    static String TYPE = "io.blt.gregbot.core.plugin.ThrowOnLoadIdentityPlugin";

    @Override
    public Set<String> requiredProperties() {
        return null;
    }

    @Override
    public void load(Map<String, String> properties) throws PluginException {
        throw new PluginException("identity plugin test load exception", null);
    }

    @Override
    public Map<String, String> variables() throws IdentityException {
        return null;
    }
}
