/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.utils;

import java.awt.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.blt.gregbot.ui.utils.AwtUtils.blankImage;
import static io.blt.gregbot.ui.utils.AwtUtils.scaleDimension;
import static io.blt.gregbot.ui.utils.AwtUtils.screenSize;
import static io.blt.test.AssertUtils.assertValidUtilityClass;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AwtUtilsTest {

    @Test
    void shouldBeValidUtilityClass() throws NoSuchMethodException {
        assertValidUtilityClass(AwtUtils.class);
    }

    @Test
    void screenSizeShouldReturnDefaultToolkitScreenSize() {
        var dimension = new Dimension(1, 2);

        var defaultToolkit = mock(Toolkit.class);
        when(defaultToolkit.getScreenSize())
                .thenReturn(dimension);

        try (var toolkit = Mockito.mockStatic(Toolkit.class)) {
            toolkit.when(Toolkit::getDefaultToolkit)
                    .thenReturn(defaultToolkit);

            var result = screenSize();

            assertThat(result)
                    .isEqualTo(dimension);
        }
    }

    static Stream<Arguments> scaleDimensionShouldReturnDimensionWithWidthAndHeightMultipliedByScale() {
        return Stream.of(
                Arguments.of(new Dimension(100, 200), 0.8, new Dimension(80, 160)),
                Arguments.of(new Dimension(1920, 1080), 1.25, new Dimension(2400, 1350)),
                Arguments.of(new Dimension(2560, 1440), 1.5, new Dimension(3840, 2160)),
                Arguments.of(new Dimension(123, 456), 1.1, new Dimension(136, 502)),
                Arguments.of(new Dimension(123, 456), 1.2, new Dimension(148, 548))
        );
    }

    @ParameterizedTest
    @MethodSource
    void scaleDimensionShouldReturnDimensionWithWidthAndHeightMultipliedByScale(
            Dimension dimension, double scale, Dimension expected) {

        var result = scaleDimension(dimension, scale);

        assertThat(result)
                .isEqualTo(expected);
    }

    @Test
    void blankImageShouldReturnSmallEmptyImage() {
        var result = blankImage();

        assertThat(result)
                .extracting(i -> i.getWidth(null), i -> i.getHeight(null))
                .containsOnly(1, 1);
    }

}
