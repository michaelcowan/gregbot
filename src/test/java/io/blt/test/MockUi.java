/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;

@ExtendWith(MockitoExtension.class)
public abstract class MockUi {

    @Mock
    protected Taskbar mockTaskbar;

    @BeforeEach
    void beforeEach() {
        System.setProperty("java.awt.headless", "true");
    }

    protected void doWithMockedUi(Runnable runnable) {
        try (var taskbar = Mockito.mockStatic(Taskbar.class)) {
            taskbar.when(Taskbar::getTaskbar)
                    .thenReturn(mockTaskbar);

            runnable.run();

        }
    }

}
