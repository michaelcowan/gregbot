/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.forms;

import io.blt.gregbot.ApplicationProperties;
import java.awt.*;

import static io.blt.gregbot.ui.utils.AwtUtils.scaleDimension;
import static io.blt.gregbot.ui.utils.AwtUtils.screenSize;

import javax.swing.*;

public class MainForm extends JFrame {

    private JPanel contentPane;

    public MainForm() {
        setContentPane(contentPane);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setTitle(ApplicationProperties.name());
        setSize(scaleDimension(screenSize(), 0.9));
        setLocationRelativeTo(null);
    }

    {
        // GUI initializer generated by IntelliJ IDEA GUI Designer
        // >>> IMPORTANT!! <<<
        // DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
