/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.identities;

import io.blt.gregbot.plugin.Plugin;
import java.util.Map;

public interface IdentityPlugin extends Plugin {

    Map<String, String> variables() throws IdentityException;

}
