/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.components;

import io.blt.gregbot.ui.logging.DocumentAppender;

import javax.swing.*;

public class LogbackPane extends JTextPane {

    public LogbackPane() {
        setDocument(DocumentAppender.document("io.blt.gregbot", "PANEL"));
        setEditable(false);
    }

}
