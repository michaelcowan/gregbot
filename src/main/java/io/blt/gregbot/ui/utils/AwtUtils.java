/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.utils;

import java.awt.*;

public final class AwtUtils {

    private AwtUtils() {
        throw new IllegalAccessError("Utility class should be accessed statically and never constructed");
    }

    public static Dimension screenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public static Dimension scaleDimension(Dimension dimension, double scale) {
        return new Dimension(
                (int) Math.ceil(dimension.getWidth() * scale),
                (int) Math.ceil(dimension.getHeight() * scale));
    }

}
