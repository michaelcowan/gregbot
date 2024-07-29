/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.blt.test.MockUtils.captureSwingInvokeLaterWhile;
import static io.blt.test.MockUtils.doWithMockedConstructor;
import static org.assertj.core.api.Assertions.assertThat;

class UiTest {

    @Nested
    class Start {

        @Test
        void shouldUseInvokeLaterToCallUiConstructor() {
            var invokeLater = captureSwingInvokeLaterWhile(Ui::start)
                    .getValue();

            doWithMockedConstructor(Ui.class, ctor -> {
                invokeLater.run();

                assertThat(ctor.constructed())
                        .hasSize(1);
            });
        }

    }

}
