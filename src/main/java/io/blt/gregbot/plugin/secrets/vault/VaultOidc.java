/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault;

import io.blt.gregbot.plugin.secrets.SecretPlugin;
import java.util.Map;
import java.util.Objects;

public class VaultOidc implements SecretPlugin {

    private String host;

    @Override
    public void load(Map<String, String> properties) {
        host = Objects.requireNonNull(properties.get("host"), "must specify 'host' property");
    }

    @Override
    public Map<String, String> secretsForPath(String path) {
        return null;
    }
}
