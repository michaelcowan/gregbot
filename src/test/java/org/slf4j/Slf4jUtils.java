/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.slf4j;

public final class Slf4jUtils {

    private Slf4jUtils() {
        throw new IllegalAccessError("Utility class should be accessed statically and never constructed");
    }

    public static void reset() {
        LoggerFactory.reset();
    }

}
