/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets;

import io.blt.gregbot.plugin.PluginException;

public class SecretException extends PluginException {

    public SecretException(String message, Throwable cause) {
        super(message, cause);
    }

}
