/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.identity;

import io.blt.gregbot.core.config.Properties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record IdentityProperties(
        String category,
        String secrets,
        @NotNull Map<String, String> variables,
        @NotNull @Valid Properties.Plugin plugin) {

}
