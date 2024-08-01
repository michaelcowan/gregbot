/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.utils;

import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.blt.gregbot.ui.utils.SwingUtils.scaleIcon;
import static io.blt.test.AssertUtils.assertValidUtilityClass;
import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.*;

@ExtendWith(MockitoExtension.class)
class SwingUtilsTest {

    @Test
    void shouldBeValidUtilityClass() throws NoSuchMethodException {
        assertValidUtilityClass(SwingUtils.class);
    }

    static Stream<Arguments> scaleIconShouldReturnImageIconWithWidthAndHeightMultipliedByScale() {
        return Stream.of(
                Arguments.of(2.0, 132, 88),
                Arguments.of(0.5, 33, 22),
                Arguments.of(0.2, 13, 9)
        );
    }

    @ParameterizedTest
    @MethodSource
    void scaleIconShouldReturnImageIconWithWidthAndHeightMultipliedByScale(
            double scale, int expectedWidth, int expectedHeight) {
        var icon = loadTestIcon();

        var result = scaleIcon(icon, scale);

        assertThat(result)
                .extracting(
                        ImageIcon::getIconWidth,
                        ImageIcon::getIconHeight)
                .containsExactly(
                        expectedWidth,
                        expectedHeight);
    }

    private ImageIcon loadTestIcon() {
        var url = SwingUtilsTest.class.getResource("66x44.png");
        return new ImageIcon(Objects.requireNonNull(url));
    }

}
