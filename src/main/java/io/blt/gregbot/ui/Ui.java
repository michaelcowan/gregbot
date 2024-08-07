/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import io.blt.gregbot.ui.forms.MainForm;
import io.blt.gregbot.ui.forms.SplashScreen;

import javax.swing.*;

public class Ui {

    public static void start() {
        SwingUtilities.invokeLater(Ui::new);
    }

    Ui() {
        configureLookAndFeel();

        new SplashScreen("/splash/octogreg/2048.png", 1500)
                .setVisible(true);

        var mainForm = new MainForm();
        mainForm.setVisible(true);
    }

    private void configureLookAndFeel() {
        FlatDarculaLaf.setup();
    }

}
