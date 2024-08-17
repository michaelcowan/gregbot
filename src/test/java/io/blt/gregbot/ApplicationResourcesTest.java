/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot;

import java.io.IOException;
import org.junit.jupiter.api.Test;

import static io.blt.test.AssertUtils.assertValidUtilityClass;
import static org.assertj.core.api.Assertions.assertThat;

import java.awt.*;

class ApplicationResourcesTest {

    @Test
    void shouldBeValidUtilityClass() throws NoSuchMethodException {
        assertValidUtilityClass(ApplicationResources.class);
    }

    @Test
    void getResourceShouldReturnUrlToResource() {
        var result = ApplicationResources.getResource("/root-level-resource");

        assertThat(result)
                .asString()
                .endsWith("/target/test-classes/root-level-resource");
    }

    @Test
    void getResourceAsStreamShouldReturnInputStreamOfResource() throws IOException {
        try (var result = ApplicationResources.getResourceAsStream("/root-level-resource")) {

            assertThat(result)
                    .hasContent("Root level resource content");
        }
    }

    @Test
    void getResourceAsImageShouldReturnImage() {
        var result = ApplicationResources.getResourceAsImage("/test-image-16.png");

        assertThat(result)
                .extracting( i-> i.getWidth(null))
                .isEqualTo(16);
    }

    @Test
    void iconsShouldReturnIconImages() {
        var result = ApplicationResources.icons();

        assertThat(result)
                .hasOnlyElementsOfType(Image.class)
                .extracting(i -> i.getWidth(null))
                .containsOnly(16, 32, 64, 128, 256, 512);
    }

    @Test
    void largestIconShouldReturnLargeIconImage() {
        var result = ApplicationResources.largestIcon();

        assertThat(result)
                .extracting(i -> i.getWidth(null))
                .isEqualTo(512);
    }

}
