/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class TreeExpansionState {

    private final Map<TreeModel, List<TreePath>> state = new HashMap<>();

    public void clear() {
        state.clear();
    }

    public void store(JTree tree) {
        var model = tree.getModel();
        if (model != null) {
            var rootPath = new TreePath(model.getRoot());
            var paths = tree.getExpandedDescendants(rootPath);
            state.put(model, Collections.list(paths));
        }
    }

    public void restore(JTree tree) {
        var model = tree.getModel();
        if (model != null) {
            var paths = state.get(model);
            if (paths != null) {
                paths.forEach(tree::expandPath);
            }
        }
    }

}
