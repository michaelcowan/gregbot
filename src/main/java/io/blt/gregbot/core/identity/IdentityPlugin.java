/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.identity;

public interface IdentityPlugin<T> {

    Class<T> propertyType();

    // TODO The plugin needs to do whatever it needs to and to populate values
    //
    void load(T properties);

}
