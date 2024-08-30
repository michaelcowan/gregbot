/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.frames;

import com.formdev.flatlaf.util.SystemInfo;
import io.blt.gregbot.ApplicationProperties;
import io.blt.gregbot.ApplicationResources;
import io.blt.gregbot.ui.components.FlatlafThemeToggle;
import io.blt.gregbot.ui.components.HorizontalGlue;
import io.blt.gregbot.ui.dialogs.About;
import io.blt.gregbot.ui.panels.LogPanel;
import io.blt.util.Obj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.blt.gregbot.ui.utils.AwtUtils.scaleDimension;
import static io.blt.gregbot.ui.utils.AwtUtils.screenSize;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public class MainFrame extends JFrame {

    private final Logger log = LoggerFactory.getLogger(MainFrame.class);

    private JPanel contentPane;
    private JSplitPane feedbackSplitPane;
    private JTabbedPane feedbackTabbedPane;
    private JToolBar toolBar;

    public MainFrame() {
        setJMenuBar(buildMenuBar());
        setContentPane(contentPane);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setIconImages(ApplicationResources.icons());

        setTitle(ApplicationProperties.name());
        setSize(scaleDimension(screenSize(), 0.9));
        setLocationRelativeTo(null);

        if (SystemInfo.isMacFullWindowContentSupported) {
            getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
            getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
            getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);

            toolBar.add(Box.createHorizontalStrut(70), 0);
        }
    }

    private JMenuBar buildMenuBar() {
        var menuBar = new JMenuBar();

        var file = menuBar.add(new JMenu("File"));
        var help = menuBar.add(new JMenu("Help"));

        if (!SystemInfo.isMacOS) {
            file.add(buildMenuItemWithAction("Exit", e -> {
                if (shouldExit()) {
                    System.exit(0);
                }
            }));

            help.add(buildMenuItemWithAction("About", e -> new About()));
        }

        var desktop = Desktop.getDesktop();

        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler(e -> new About());
        }

        if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            desktop.setQuitHandler((e, response) -> {
                if (shouldExit()) {
                    response.performQuit();
                } else {
                    response.cancelQuit();
                }
            });
        }

        return menuBar;
    }

    private JMenuItem buildMenuItemWithAction(String text, ActionListener action) {
        return Obj.poke(new JMenuItem(text), i -> i.addActionListener(action));
    }

    private boolean shouldExit() {
        // TODO Add popup and condition around exiting
        return true;
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
        feedbackSplitPane = new JSplitPane();
        feedbackSplitPane.setOrientation(0);
        feedbackSplitPane.setResizeWeight(0.8);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(feedbackSplitPane, gbc);
        feedbackTabbedPane = new JTabbedPane();
        feedbackSplitPane.setRightComponent(feedbackTabbedPane);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        feedbackTabbedPane.addTab("Log", panel1);
        final LogPanel nestedForm1 = new LogPanel();
        panel1.add(nestedForm1.$$$getRootComponent$$$(), BorderLayout.CENTER);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        feedbackSplitPane.setLeftComponent(panel2);
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(11);
        label1.setText("main area");
        panel2.add(label1, BorderLayout.CENTER);
        toolBar = new JToolBar();
        panel2.add(toolBar, BorderLayout.NORTH);
        final HorizontalGlue horizontalGlue1 = new HorizontalGlue();
        toolBar.add(horizontalGlue1);
        final FlatlafThemeToggle flatlafThemeToggle1 = new FlatlafThemeToggle();
        toolBar.add(flatlafThemeToggle1);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
