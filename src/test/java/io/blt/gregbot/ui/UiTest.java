/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui;

import io.blt.gregbot.ApplicationResources;
import io.blt.test.MockUi;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.blt.test.MockUtils.captureSwingInvokeLaterWhile;
import static io.blt.test.MockUtils.doWithMockedConstructor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.*;

class UiTest extends MockUi {

    @Nested
    class Start {

        static Stream<Arguments> shouldSetPropertiesToSupportMacOs() {
            return Stream.of(
                    Arguments.of("apple.laf.useScreenMenuBar", "true"),
                    Arguments.of("apple.awt.application.name", "mock-project-name"),
                    Arguments.of("apple.awt.application.appearance", "system")
            );
        }

        @ParameterizedTest
        @MethodSource
        void shouldSetPropertiesToSupportMacOs(String property, String expected) {
            doWithMockedUi(() -> {
                Ui.start();

                var result = System.getProperty(property);

                assertThat(result)
                        .isEqualTo(expected);
            });
        }

        @Test
        void shouldUseInvokeLaterToCallUiConstructor() {
            doWithMockedUi(() -> {
                var invokeLater = captureSwingInvokeLaterWhile(Ui::start)
                        .getValue();

                doWithMockedConstructor(Ui.class, ctor -> {
                    invokeLater.run();

                    assertThat(ctor.constructed())
                            .hasSize(1);
                });
            });
        }

    }

    @Nested
    class Constructor {

        @Test
        void shouldSetIconImageWhenSupported() {
            doWithMockedUi(() -> {
                when(mockTaskbar.isSupported(Taskbar.Feature.ICON_IMAGE))
                        .thenReturn(true);

                new Ui();

                verify(mockTaskbar)
                        .setIconImage(ApplicationResources.largestIcon());
            });
        }

        @Test
        void shouldLogUnexpectedExceptions() {
            try (var loggerFactory = Mockito.mockStatic(LoggerFactory.class)) {
                var logger = mock(Logger.class);

                loggerFactory.when(() -> LoggerFactory.getLogger(Ui.class))
                        .thenReturn(logger);

                // This will inherently throw because the tests are running in headless mode
                new Ui();

                var exception = ArgumentCaptor.forClass(Exception.class);
                verify(logger)
                        .error(eq("Unexpected exception"), exception.capture());

                assertThat(exception.getValue())
                        .isInstanceOf(HeadlessException.class);
            }
        }

    }

}
