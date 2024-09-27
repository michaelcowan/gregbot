/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.utils;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

@ExtendWith(MockitoExtension.class)
class TreeExpansionStateTest {

    @Mock
    JTree tree;

    @Mock
    TreeModel model;

    TreeExpansionState state = new TreeExpansionState();

    @BeforeEach
    void beforeEach() {
        when(tree.getModel())
                .thenReturn(model);
    }

    @Test
    void shouldStoreAndRestoreExpandedDescendantsFromRoot() {
        var root = new Object();
        var paths = List.of(mock(TreePath.class), mock(TreePath.class), mock(TreePath.class));

        when(model.getRoot())
                .thenReturn(root);

        when(tree.getExpandedDescendants(
                argThat(treePath -> treePath.getLastPathComponent().equals(root))))
                .thenReturn(Collections.enumeration(paths));

        state.store(tree);

        verify(tree)
                .getExpandedDescendants(any());

        state.restore(tree);

        var expandedPaths = ArgumentCaptor.forClass(TreePath.class);
        verify(tree, times(paths.size()))
                .expandPath(expandedPaths.capture());

        assertThat(expandedPaths.getAllValues())
                .hasSameElementsAs(paths);
    }

    @Test
    void shouldStoreAndClearExpandedDescendantsFromRoot() {
        var root = new Object();
        var paths = List.of(mock(TreePath.class), mock(TreePath.class), mock(TreePath.class));

        when(model.getRoot())
                .thenReturn(root);

        when(tree.getExpandedDescendants(
                argThat(treePath -> treePath.getLastPathComponent().equals(root))))
                .thenReturn(Collections.enumeration(paths));

        state.store(tree);

        state.clear();

        state.restore(tree);

        verify(tree, never())
                .expandPath(any());
    }

    @Test
    void shouldNoOpWhenTreeModelIsNull() {
        when(tree.getModel())
                .thenReturn(null);

        state.store(tree);

        state.restore(tree);

        verify(tree, never())
                .expandPath(any());
    }

}
