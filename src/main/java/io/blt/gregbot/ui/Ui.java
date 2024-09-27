/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import io.blt.gregbot.ApplicationProperties;
import io.blt.gregbot.ApplicationResources;
import io.blt.gregbot.ui.dialogs.SplashScreen;
import io.blt.gregbot.ui.frames.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import javax.swing.*;

public class Ui {

    private final Logger log = LoggerFactory.getLogger(Ui.class);

    public static void start() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", ApplicationProperties.name());
        System.setProperty("apple.awt.application.appearance", "system");

        SwingUtilities.invokeLater(Ui::new);
    }

    Ui() {
        log.info("Starting {} {} born on {}",
                ApplicationProperties.name(),
                ApplicationProperties.version(),
                ApplicationProperties.timestamp());

        try {
            if (Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                Taskbar.getTaskbar().setIconImage(ApplicationResources.largestIcon());
            }

            FlatJetBrainsMonoFont.install();

            configureLookAndFeel();

            new SplashScreen("/splash/octogreg/2048.png", 1500)
                    .setVisible(true);

            var mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        } catch (Exception e) {
            log.error("Unexpected exception", e);
        }
    }

    private void configureLookAndFeel() {
        FlatDarculaLaf.setup();
    }

}
