/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.plugin;

import io.blt.gregbot.plugin.PluginException;
import io.blt.gregbot.plugin.identities.IdentityPlugin;
import java.util.Map;

public class ThrowOnLoadIdentityPlugin implements IdentityPlugin {

    public static final String MESSAGE = "identity plugin exception on load";

    @Override
    public void load(Map<String, String> properties) throws PluginException {
        throw new PluginException(MESSAGE, null);
    }

    @Override
    public Map<String, String> variables() {
        return null;
    }
}
