/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.components;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import io.blt.gregbot.ApplicationResources.ToolIcon;

import static io.blt.gregbot.ApplicationResources.toolIcon;
import static java.awt.EventQueue.invokeLater;

import java.awt.event.ItemEvent;
import javax.swing.*;

public class FlatlafThemeToggle extends JToggleButton {

    private final Icon sun = toolIcon(ToolIcon.SUN);
    private final Icon moon = toolIcon(ToolIcon.MOON);

    public FlatlafThemeToggle() {
        addItemListener(l -> invokeLater(() -> {
            FlatAnimatedLafChange.showSnapshot();
            if (l.getStateChange() == ItemEvent.SELECTED) {
                setIcon(sun);
                FlatIntelliJLaf.setup();
            } else {
                setIcon(moon);
                FlatDarculaLaf.setup();
            }
            FlatLaf.updateUI();
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        }));

        setIcon(moon);
    }
}
