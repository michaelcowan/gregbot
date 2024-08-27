/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.logging;

import ch.qos.logback.classic.Logger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.slf4j.Slf4jUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

class DocumentAppenderTest {

    @AfterEach
    void afterEach() {
        Slf4jUtils.reset();
    }

    @Test
    void shouldLoadAppenderProperties() {
        var appender = findDocumentAppender();

        assertThat(appender)
                .extracting(
                        DocumentAppender::getLineLimit,
                        DocumentAppender::getLayout)
                .map(Object::toString)
                .contains(
                        "3",
                        "ch.qos.logback.classic.PatternLayout(\"test-pattern %msg%n\")");
    }

    @ParameterizedTest
    @CsvSource({
            "unknown, TEST",
            "test, UNKNOWN",
            "test, STDOUT"
    })
    void documentShouldReturnNullOnUnknownLoggerOrAppenderOrWrongAppenderType(String loggerName, String appenderName) {
        var logger = LoggerFactory.getLogger("test");

        logger.info("test-message-before-register");
        var document = DocumentAppender.document(loggerName, appenderName);
        logger.info("test-message-after-register");

        assertThat(document)
                .isNull();
    }

    @Test
    void shouldUpdateDocumentWhenLoggingOccurs() {
        var logger = LoggerFactory.getLogger("test");

        logger.info("test-message-before-register");
        var document = DocumentAppender.document("test", "TEST");
        logger.info("test-message-after-register");

        assertThat(asText(document))
                .isEqualTo("test-pattern test-message-before-register%ntest-pattern test-message-after-register%n"
                        .formatted());
    }

    @Test
    void shouldGenerateDocumentNotExceedingLineLimit() {
        var document = DocumentAppender.document("test", "TEST");

        var logger = LoggerFactory.getLogger("test");
        IntStream.rangeClosed(1, 10)
                .forEach(i -> logger.info("log {}", i));

        assertThat(asText(document))
                .asString()
                .isEqualTo("test-pattern log 8%ntest-pattern log 9%ntest-pattern log 10%n"
                        .formatted());
    }

    private String asText(Document document) {
        assertThat(document)
                .isNotNull();
        try {
            return document.getText(0, document.getLength());
        } catch (BadLocationException e) {
            fail(e);
        }
        return null;
    }

    private DocumentAppender findDocumentAppender() {
        if (LoggerFactory.getLogger("test") instanceof Logger logger) {
            return (DocumentAppender) logger.getAppender("TEST");
        }
        throw new IllegalStateException("Cannot find DocumentAppender instance");
    }

    private int countLines(String s) {
        return s.split("\r\n|\r|\n").length;
    }

}
