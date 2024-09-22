/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.controllers;

import io.blt.gregbot.core.project.Project;
import io.blt.util.Ctr;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.blt.gregbot.core.project.Project.Collection;
import static io.blt.gregbot.core.project.Project.Collection.Folder;
import static java.util.Objects.nonNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ProjectController {

    private final DefaultListModel<String> collectionNames = new DefaultListModel<>();
    private final Map<String, DefaultTreeModel> collections = new HashMap<>();

    public ProjectController load(Project project) {
        loadCollections(project.collections());
        return this;
    }

    public DefaultListModel<String> collectionNamesListModel() {
        return collectionNames;
    }

    public Map<String, DefaultTreeModel> collectionsTreeModel() {
        return Collections.unmodifiableMap(collections);
    }

    private void loadCollections(Map<String, Collection> collections) {
        collectionNames.clear();
        collections.keySet().forEach(collectionNames::addElement);

        this.collections.clear();
        this.collections.putAll(Ctr.transformValues(collections, v ->
                new DefaultTreeModel(addFolders(null, "root", v.layout()), true)
        ));
    }

    private DefaultMutableTreeNode addFolders(DefaultMutableTreeNode parent, String name, Folder folder) {
        var folderNode = new DefaultMutableTreeNode(name, true);
        if (nonNull(parent)) {
            parent.add(folderNode);
        }

        folder.folders()
                .forEach((key, value) -> addFolders(folderNode, key, value));

        folder.requests()
                .forEach(request -> folderNode.add(new DefaultMutableTreeNode(request, false)));

        return folderNode;
    }

}
