/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.plugin;

import io.blt.gregbot.plugin.PluginException;
import io.blt.gregbot.plugin.secrets.SecretException;
import io.blt.gregbot.plugin.secrets.SecretPlugin;
import java.util.Map;

public class ThrowOnSecretsForPathSecretPlugin implements SecretPlugin {

    public static final String MESSAGE = "secret plugin exception on secretsForPath";

    @Override
    public void load(Map<String, String> properties) throws PluginException {
        // Don't need the test plugin to load properties
    }

    @Override
    public Map<String, String> secretsForPath(String path) throws SecretException {
        throw new SecretException(MESSAGE, null);
    }

}
