/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.controllers;

import io.blt.gregbot.core.project.Project;
import java.util.Map;

import javax.swing.*;

public class ProjectController {

    private final DefaultListModel<String> collectionNames = new DefaultListModel<>();

    public ProjectController load(Project project) {
        loadCollections(project.collections());
        return this;
    }

    public DefaultListModel<String> collectionNamesListModel() {
        return collectionNames;
    }

    private void loadCollections(Map<String, Project.Collection> collections) {
        collectionNames.clear();
        collections.keySet().forEach(collectionNames::addElement);
    }

}
