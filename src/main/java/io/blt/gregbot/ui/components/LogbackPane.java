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
import javax.swing.text.Document;

public class LogbackPane extends JTextPane implements DocumentAppender.Listener {

    public LogbackPane() {
        DocumentAppender.register("io.blt.gregbot", "PANEL", this);
        setEditable(false);
    }

    @Override
    public void updateDocument(Document document) {
        SwingUtilities.invokeLater(() -> {
            setDocument(document);
            setCaretPosition(document.getLength());
        });
    }
}
