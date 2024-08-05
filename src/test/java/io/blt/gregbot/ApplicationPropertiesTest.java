/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot;

import java.util.Properties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.blt.test.AssertUtils.assertValidUtilityClass;
import static io.blt.test.MockUtils.spyStatic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@ExtendWith(MockitoExtension.class)
class ApplicationPropertiesTest {

    @Test
    void shouldBeValidUtilityClass() throws NoSuchMethodException {
        assertValidUtilityClass(ApplicationProperties.class);
    }

    @Test
    void loadPropertiesShouldReturnLoadedProperties() {
        var result = ApplicationProperties.loadProperties("/application.properties");

        assertThat(result)
                .containsOnly(
                        entry("project.version", "mock-project-version"),
                        entry("project.name", "mock-project-name"),
                        entry("project.timestamp", "mock-project-timestamp"),
                        entry("project.copyright", "mock-project-copyright"));
    }

    @Test
    void loadPropertiesShouldReturnEmptyWhenFileDoesNotExist() {
        var result = ApplicationProperties.loadProperties("/does-not-exist");

        assertThat(result)
                .isEmpty();
    }

    @Test
    void propertiesShouldReturnLoadedProperties() {
        var result = ApplicationProperties.properties();

        assertThat(result)
                .containsOnly(
                        entry("project.version", "mock-project-version"),
                        entry("project.name", "mock-project-name"),
                        entry("project.timestamp", "mock-project-timestamp"),
                        entry("project.copyright", "mock-project-copyright"));
    }

    @Test
    void versionShouldReturnPropertyValue() {
        var result = ApplicationProperties.version();

        assertThat(result)
                .isEqualTo("mock-project-version");
    }

    @Test
    void nameShouldReturnPropertyValue() {
        var result = ApplicationProperties.name();

        assertThat(result)
                .isEqualTo("mock-project-name");
    }

    @Test
    void timestampShouldReturnPropertyValue() {
        var result = ApplicationProperties.timestamp();

        assertThat(result)
                .isEqualTo("mock-project-timestamp");
    }

    @Test
    void copyrightShouldReturnPropertyValue() {
        var result = ApplicationProperties.copyright();

        assertThat(result)
                .isEqualTo("mock-project-copyright");
    }

    @Nested
    class WithEmptyProperties {

        @Test
        void versionShouldReturnDefaultValue() {
            doWithEmptyProperties(() -> {
                var result = ApplicationProperties.version();

                assertThat(result)
                        .isEqualTo("DEVELOPMENT");
            });
        }

        @Test
        void nameShouldReturnDefaultValue() {
            doWithEmptyProperties(() -> {
                var result = ApplicationProperties.name();

                assertThat(result)
                        .isEqualTo("untitled");
            });
        }

        @Test
        void timestampShouldReturnDefaultValue() {
            doWithEmptyProperties(() -> {
                var result = ApplicationProperties.timestamp();

                assertThat(result)
                        .isEqualTo("");
            });
        }

        @Test
        void copyrightShouldReturnDefaultValue() {
            doWithEmptyProperties(() -> {
                var result = ApplicationProperties.copyright();

                assertThat(result)
                        .isEqualTo("");
            });
        }

        private void doWithEmptyProperties(Runnable runnable) {
            try (var mock = spyStatic(ApplicationProperties.class)) {
                mock.when(ApplicationProperties::properties)
                        .thenReturn(new Properties());

                runnable.run();
            }
        }
    }

}
