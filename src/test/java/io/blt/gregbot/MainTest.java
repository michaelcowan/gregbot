/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot;

import io.blt.gregbot.ui.Ui;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MainTest {

    @Test
    void shouldCallUiStart() {
        try (var ui = Mockito.mockStatic(Ui.class)) {
            Main.main(new String[]{});
            ui.verify(Ui::start);
        }
    }

}
