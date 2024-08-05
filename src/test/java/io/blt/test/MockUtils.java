/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.test;

import java.util.function.Consumer;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.withSettings;

import javax.swing.*;

public final class MockUtils {

    private MockUtils() {
        throw new IllegalAccessError("Utility class should be accessed statically and never constructed");
    }

    public static ArgumentCaptor<Runnable> captureSwingInvokeLaterWhile(Runnable runnable) {
        var captor = ArgumentCaptor.forClass(Runnable.class);

        try (var swingUtilities = Mockito.mockStatic(SwingUtilities.class)) {
            runnable.run();

            swingUtilities.verify(() -> SwingUtilities.invokeLater(captor.capture()));
        }

        return captor;
    }

    public static <T> void doWithMockedConstructor(Class<T> type, Consumer<MockedConstruction<T>> consumer) {
        try (var ui = Mockito.mockConstruction(type)) {
            consumer.accept(ui);
        }
    }

    public static <T> MockedStatic<T> spyStatic(Class<T> type) {
        return Mockito.mockStatic(type, withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS));
    }

}
