/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.test;

import javax.swing.tree.TreeModel;

public final class ModelTestUtils {

    private ModelTestUtils() {
        throw new IllegalAccessError("Utility class should be accessed statically and never constructed");
    }

    public static String asString(TreeModel model) {
        return printTree(model, model.getRoot(), "");
    }

    private static String printTree(TreeModel model, Object object, String indent) {
        var node = new StringBuilder(indent + object + "\n");
        for (var i = 0; i < model.getChildCount(object); i++) {
            node.append(printTree(model, model.getChild(object, i), indent + "  "));
        }
        return node.toString();
    }

}
