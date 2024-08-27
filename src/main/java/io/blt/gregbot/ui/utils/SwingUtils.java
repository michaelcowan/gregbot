/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.utils;

import java.awt.*;
import javax.swing.*;

public final class SwingUtils {

    private SwingUtils() {
        throw new IllegalAccessError("Utility class should be accessed statically and never constructed");
    }

    public static void autoVerticalScroll(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar()
                .addAdjustmentListener(
                        e -> {
                            if ((diffFromMax(e.getAdjustable())) < scrollHeightOf(scrollPane)) {
                                setToMax(e.getAdjustable());
                            }
                        });
    }

    private static int diffFromMax(Adjustable adjustable) {
        return adjustable.getMaximum() - adjustable.getValue();
    }

    private static void setToMax(Adjustable adjustable) {
        adjustable.setValue(adjustable.getMaximum());
    }

    private static int scrollHeightOf(JScrollPane scrollPane) {
        return scrollPane.getHeight() + scrollPane.getVerticalScrollBar().getBlockIncrement();
    }

}
