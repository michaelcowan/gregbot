/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.controllers;

import io.blt.gregbot.core.project.Project;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectControllerTest {

    ProjectController controller = new ProjectController();

    @BeforeEach
    void beforeEach() throws IOException {
        var project = Project.loadFromJson(
                Project.class.getResourceAsStream("full.json"));

        controller.load(project);
    }

    @Nested
    class ProjectCollection {

        @Test
        void shouldBuildNamesListModel() {
            var model = controller.collectionNamesListModel();

            assertThat(model.elements().asIterator())
                    .toIterable()
                    .containsOnly("Skynet");
        }

        @Nested
        class OnReload {

            @BeforeEach
            void beforeEach() throws IOException {
                var newProject = Project.loadFromJson(
                        ProjectControllerTest.class.getResourceAsStream("project.json"));

                controller.load(newProject);
            }

            @Test
            void shouldReplaceNamesListModel() {
                var model = controller.collectionNamesListModel();

                assertThat(model.elements().asIterator())
                        .toIterable()
                        .containsOnly("Transporter", "Warp Drive");
            }

        }

    }

}
