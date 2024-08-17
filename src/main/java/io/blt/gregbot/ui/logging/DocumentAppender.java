/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import io.blt.util.Obj;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;

import static javax.swing.text.StyleConstants.Foreground;

import java.awt.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

/**
 * Logback appender that updates a {@link Document} for use with Swing components.
 */
public class DocumentAppender extends AppenderBase<ILoggingEvent> {

    /**
     * Called when {@code Document} is updated and should be rendered
     */
    @FunctionalInterface
    public interface Listener {
        void updateDocument(Document document);
    }

    private final Document document = new DefaultStyledDocument();
    private final List<Listener> listeners = new ArrayList<>();

    private final Map<Level, SimpleAttributeSet> styles = Map.of(
            Level.ERROR, style(new Color(0xFF, 0x3B, 0x30)),
            Level.WARN, style(new Color(0xFF, 0x95, 0x00)),
            Level.INFO, style(new Color(0x26, 0x75, 0xBF)),
            Level.DEBUG, style(new Color(0xBF, 0x5A, 0xF2)),
            Level.TRACE, style(new Color(0xA2, 0x84, 0x5E)));
    private final SimpleAttributeSet defaultStyle = new SimpleAttributeSet();

    private int lineLimit = 2000;
    private PatternLayout layout = Obj.tap(PatternLayout::new,
            p -> p.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} -%kvp- %msg%n"));

    /**
     * Adds a {@link Listener} to the specified appender if the appender is an instance of {@code DocumentAppender},
     * otherwise will no-op.
     *
     * @param loggerName   logger name e.g,. from {@code logback.xml} {@code <logger name="io.blt.gregbot" ...>}
     * @param appenderName appender name e.g,. from {@code logback.xml} {@code <appender name="PANEL" ...>}
     * @param listener     listener instance to add
     */
    public static void register(String loggerName, String appenderName, Listener listener) {
        if (LoggerFactory.getLogger(loggerName) instanceof Logger logger) {
            if (logger.getAppender(appenderName) instanceof DocumentAppender appender) {
                appender.addListener(listener);
            }
        }
    }

    @Override
    public void start() {
        layout.setContext(context);
        layout.start();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        int overLimit = document.getDefaultRootElement().getElementCount() - lineLimit;

        try {
            if (overLimit > 0) {
                document.remove(0, document.getDefaultRootElement().getElement(overLimit - 1).getEndOffset());
            }
            document.insertString(document.getLength(), layout.doLayout(event),
                    styles.getOrDefault(event.getLevel(), defaultStyle));
        } catch (BadLocationException ignored) {
            // Should be unreachable
        }

        listeners.forEach(l -> l.updateDocument(document));
    }

    public int getLineLimit() {
        return lineLimit;
    }

    public void setLineLimit(int lineLimit) {
        this.lineLimit = lineLimit;
    }

    public PatternLayout getLayout() {
        return layout;
    }

    public void setLayout(PatternLayout layout) {
        this.layout = layout;
    }

    private void addListener(Listener listener) {
        listeners.add(listener);
        listener.updateDocument(document);
    }

    private static SimpleAttributeSet style(Color foreground) {
        var attributes = new SimpleAttributeSet();
        attributes.addAttribute(Foreground, foreground);
        return attributes;
    }

}
