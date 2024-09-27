/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.controllers;

import io.blt.gregbot.core.project.Project;
import io.blt.test.ModelTestUtils;
import io.blt.util.Ctr;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

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

        @Test
        void shouldBuildCollectionsTreeModel() {
            var models = controller.collectionsTreeModel();

            var assertableModels = Ctr.transformValues(models, ModelTestUtils::asString);

            assertThat(assertableModels)
                    .containsExactly(
                            entry("Skynet", """
                                    root
                                      Terminator
                                        Emergency
                                          Emergency Shutdown Terminator
                                        List Terminators
                                        Fetch Terminator
                                      Aerial
                                        Emergency
                                          Emergency Shutdown Aerial
                                        List Aerials
                                        Fetch Aerial
                                      Emergency
                                        Emergency Shutdown Terminator
                                        Emergency Shutdown Aerial
                                      Health Check
                                    """)
                    );
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

            @Test
            void shouldReplaceCollectionsTreeModel() {
                var models = controller.collectionsTreeModel();

                var assertableModels = Ctr.transformValues(models, ModelTestUtils::asString);

                assertThat(assertableModels)
                        .containsOnly(
                                entry("Transporter", """
                                        root
                                          Crew
                                            Beam Down Crew
                                            Transport Crew
                                          Planet
                                            Scan Planet
                                          Health Check
                                        """),
                                entry("Warp Drive", """
                                        root
                                          Operations
                                            Engage Warp Drive
                                            Disengage Warp Drive
                                          Health Check
                                        """)
                        );
            }

        }

    }


}
