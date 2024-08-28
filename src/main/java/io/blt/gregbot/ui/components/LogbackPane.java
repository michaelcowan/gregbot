/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.components;

import io.blt.gregbot.ui.logging.DocumentAppender;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class LogbackPane extends JTextPane {

    private boolean autoCaretToBottom = false;
    private boolean lineWrap = false;

    public LogbackPane() {
        setDocument(DocumentAppender.document("io.blt.gregbot", "PANEL"));
        setEditable(false);

        getDocument().addDocumentListener(caretToBottomListener());
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return lineWrap ? super.getScrollableTracksViewportWidth() :
                getUI().getPreferredSize(this).width <= getParent().getSize().width;
    }

    @Override
    public Dimension getPreferredSize() {
        return getUI().getPreferredSize(this);
    }

    public void autoCaretToBottom(boolean autoCaretToBottom) {
        this.autoCaretToBottom = autoCaretToBottom;
    }

    public void toggleAutoCaretToBottom() {
        autoCaretToBottom(!autoCaretToBottom);
    }

    public void lineWrap(boolean lineWrap) {
        this.lineWrap = lineWrap;
    }

    public void toggleLineWrap() {
        lineWrap(!lineWrap);
    }

    public void setLineWrap(boolean lineWrap) {
        this.lineWrap = lineWrap;
    }

    public void setCaretToBottom() {
        setCaretPosition(getDocument().getLength());
    }

    private void setCaretToBottomIfAutoEnabled() {
        if (autoCaretToBottom) {
            setCaretToBottom();
        }
    }

    private DocumentListener caretToBottomListener() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setCaretToBottomIfAutoEnabled();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setCaretToBottomIfAutoEnabled();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setCaretToBottomIfAutoEnabled();
            }
        };
    }

}
