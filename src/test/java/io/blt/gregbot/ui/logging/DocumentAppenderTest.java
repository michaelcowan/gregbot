/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.logging;

import ch.qos.logback.classic.Logger;
import java.util.ArrayList;
import java.util.List;
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

    TestableListener listener = new TestableListener();

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
    void registerShouldNoOpOnUnknownLoggerOrAppenderOrWrongAppenderType(String loggerName, String appenderName) {
        var logger = LoggerFactory.getLogger("test");

        logger.info("test-message-before-register");
        DocumentAppender.register(loggerName, appenderName, listener);
        logger.info("test-message-after-register");

        assertThat(listener.documentTexts)
                .isEmpty();
    }

    @Test
    void shouldRegisterAndCallListenerWhenLoggingOccurs() {
        var logger = LoggerFactory.getLogger("test");

        logger.info("test-message-before-register");
        DocumentAppender.register("test", "TEST", listener);
        logger.info("test-message-after-register");

        assertThat(listener.documentTexts)
                .containsExactly(
                        "test-pattern test-message-before-register%n"
                                .formatted(),
                        "test-pattern test-message-before-register%ntest-pattern test-message-after-register%n"
                                .formatted());
    }

    @Test
    void shouldGenerateDocumentNotExceedingLineLimit() {
        DocumentAppender.register("test", "TEST", listener);

        var logger = LoggerFactory.getLogger("test");
        IntStream.rangeClosed(1, 10)
                .forEach(i -> logger.info("log {}", i));

        assertThat(listener.documentTexts)
                .extracting(this::countLines)
                .allMatch(length -> length <= 3);

        assertThat(listener.documentTexts)
                .last()
                .asString()
                .isEqualTo("test-pattern log 8%ntest-pattern log 9%ntest-pattern log 10%n"
                        .formatted());
    }

    static class TestableListener implements DocumentAppender.Listener {

        final List<String> documentTexts = new ArrayList<>();

        @Override
        public void updateDocument(Document document) {
            var length = document.getLength();
            try {
                documentTexts.add(document.getText(0, length));
            } catch (BadLocationException e) {
                fail("Cannot extract document text", e);
            }
        }
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
