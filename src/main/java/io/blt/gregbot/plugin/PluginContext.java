/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin;

import io.blt.gregbot.plugin.secrets.SecretPlugin;

public record PluginContext(
        SecretPlugin secretPlugin) {

    public PluginContext() {
        this(null);
    }

}