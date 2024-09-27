/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.panels;

import java.util.Map;
import java.util.stream.Stream;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

public class CollectionPanel extends JPanel {

    private JPanel contentPane;
    private JList<String> namesList;
    private JTree requestsTree;

    private Map<String, DefaultTreeModel> collections;

    public CollectionPanel() {
        Stream.of(namesList, requestsTree)
                .forEach(c -> c.setFont(UIManager.getFont("large.font")));

        namesList.addListSelectionListener(e -> setCollectionTo(namesList.getSelectedValue()));

        requestsTree.setModel(null);
        requestsTree.setRootVisible(false);
        requestsTree.setShowsRootHandles(true);
    }

    public void setModels(ListModel<String> names, Map<String, DefaultTreeModel> collections) {
        this.collections = collections;

        namesList.setModel(names);
        requestsTree.setModel(null);

        namesList.setSelectedIndex(0);
    }

    private void setCollectionTo(String name) {
        requestsTree.setModel(collections.get(name));
    }

    {
        // GUI initializer generated by IntelliJ IDEA GUI Designer
        // >>> IMPORTANT!! <<<
        // DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setResizeWeight(0.4);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(splitPane1, gbc);
        namesList = new JList();
        namesList.setSelectionMode(0);
        splitPane1.setLeftComponent(namesList);
        requestsTree = new JTree();
        splitPane1.setRightComponent(requestsTree);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
