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

public class ThrowOnVariablesIdentityPlugin implements IdentityPlugin {

    public static final String MESSAGE = "identity plugin exception on variables";

    @Override
    public void load(Map<String, String> properties) throws PluginException {
        // Don't need the test plugin to load properties
    }

    @Override
    public Map<String, String> variables() throws IdentityException {
        throw new IdentityException(MESSAGE, null);
    }
}
