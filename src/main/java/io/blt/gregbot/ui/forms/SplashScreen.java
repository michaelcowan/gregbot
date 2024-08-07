/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.forms;

import io.blt.gregbot.ApplicationProperties;
import io.blt.gregbot.ApplicationResources;
import io.blt.gregbot.ui.components.ScaledImagePanel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static io.blt.gregbot.ui.utils.AwtUtils.scaleDimension;
import static io.blt.gregbot.ui.utils.AwtUtils.screenSize;

import javax.swing.*;

public class SplashScreen extends JDialog {

    private final String splashPath;

    private JPanel contentPane;
    private ScaledImagePanel scaledImagePanel;
    private JLabel lowerTopLabel;
    private JLabel lowerBottomLabel;

    public SplashScreen(String path, long timeoutInMillis) {
        this.splashPath = path;

        $$$setupUI$$$();
        setContentPane(contentPane);

        setModalityType(ModalityType.APPLICATION_MODAL);
        setUndecorated(true);

        lowerTopLabel.setText("%s born on %s".formatted(
                ApplicationProperties.version(),
                ApplicationProperties.timestamp()));

        lowerBottomLabel.setText(ApplicationProperties.copyright());

        pack();

        setLocationRelativeTo(null);

        contentPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        contentPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });

        new Thread(() -> {
            try {
                Thread.sleep(timeoutInMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            dispose();
        }).start();
    }

    private void createUIComponents() {
        var desiredWidthAndHeight = (int) scaleDimension(screenSize(), 0.3).getWidth();

        var image = ApplicationResources.getResourceAsImage(splashPath);

        scaledImagePanel = new ScaledImagePanel(image, desiredWidthAndHeight, desiredWidthAndHeight);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.add(scaledImagePanel, BorderLayout.CENTER);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        contentPane.add(panel1, BorderLayout.SOUTH);
        lowerTopLabel = new JLabel();
        lowerTopLabel.setHorizontalAlignment(4);
        lowerTopLabel.setText("");
        panel1.add(lowerTopLabel, BorderLayout.NORTH);
        lowerBottomLabel = new JLabel();
        lowerBottomLabel.setHorizontalAlignment(4);
        lowerBottomLabel.setText("");
        panel1.add(lowerBottomLabel, BorderLayout.SOUTH);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
