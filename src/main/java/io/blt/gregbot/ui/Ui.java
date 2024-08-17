/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import io.blt.gregbot.ApplicationProperties;
import io.blt.gregbot.ApplicationResources;
import io.blt.gregbot.ui.forms.MainForm;
import io.blt.gregbot.ui.forms.SplashScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import javax.swing.*;

public class Ui {

    private static final Logger LOG = LoggerFactory.getLogger(Ui.class);

    public static void start() {
        LOG.info("Starting {} {}",
                ApplicationProperties.name(),
                ApplicationProperties.version());

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", ApplicationProperties.name());
        System.setProperty("apple.awt.application.appearance", "system");

        if (Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
            Taskbar.getTaskbar().setIconImage(ApplicationResources.largestIcon());
        }

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
