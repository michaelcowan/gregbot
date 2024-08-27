/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.blt.test.AssertUtils.assertValidUtilityClass;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.*;

@ExtendWith(MockitoExtension.class)
class SwingUtilsTest {

    @Test
    void shouldBeValidUtilityClass() throws NoSuchMethodException {
        assertValidUtilityClass(SwingUtils.class);
    }

    @Nested
    class AutoVerticalScroll {

        @Mock
        JScrollPane scrollPane;

        @Mock
        JScrollBar scrollBar;

        @Mock
        Adjustable adjustable;

        @BeforeEach
        void beforeEach() {
            when(scrollPane.getVerticalScrollBar())
                    .thenReturn(scrollBar);
        }

        @Test
        void shouldRegisterAdjustmentListener() {
            SwingUtils.autoVerticalScroll(scrollPane);

            var listener = captureAdjustmentListener();

            assertThat(listener)
                    .isNotNull();
        }

        @ParameterizedTest
        @ValueSource(ints = {91, 92, 100, 1000})
        void shouldSetAdjustableToMaxWhenScrollHeightPlusIncrementIsGreaterThanAdjustableDifference(int scrollHeight) {
            SwingUtils.autoVerticalScroll(scrollPane);

            when(adjustable.getMaximum())
                    .thenReturn(1000);

            when(adjustable.getValue())
                    .thenReturn(900);

            when(scrollPane.getHeight())
                    .thenReturn(scrollHeight);

            when(scrollBar.getBlockIncrement())
                    .thenReturn(10);

            mockEvent(new AdjustmentEvent(adjustable, 0, 0, 0));

            verify(adjustable)
                    .setValue(1000);
        }

        @ParameterizedTest
        @ValueSource(ints = {90, 89, 50, 0})
        void shouldNotModifyAdjustableWhenScrollHeightPlusIncrementIsLessThanAdjustableDifference(int scrollHeight) {
            SwingUtils.autoVerticalScroll(scrollPane);

            when(adjustable.getMaximum())
                    .thenReturn(1000);

            when(adjustable.getValue())
                    .thenReturn(900);

            when(scrollPane.getHeight())
                    .thenReturn(scrollHeight);

            when(scrollBar.getBlockIncrement())
                    .thenReturn(10);

            mockEvent(new AdjustmentEvent(adjustable, 0, 0, 0));

            verifyNoMoreInteractions(adjustable);
        }

        private AdjustmentListener captureAdjustmentListener() {
            var listener = ArgumentCaptor.forClass(AdjustmentListener.class);
            verify(scrollBar)
                    .addAdjustmentListener(listener.capture());
            return listener.getValue();
        }

        private void mockEvent(AdjustmentEvent event) {
            captureAdjustmentListener()
                    .adjustmentValueChanged(event);
        }
    }

}
