/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.plugin;

import io.blt.gregbot.plugin.secrets.SecretException;
import io.blt.gregbot.plugin.secrets.SecretPlugin;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class SecretRendererTest {

    final Map<String, Map<String, String>> secrets = Map.of(
            "secret/birthday", Map.of(
                    "Greg", "November",
                    "Phil", "October",
                    "Louis", "February"),
            "secret/watch", Map.of(
                    "Greg", "Seiko",
                    "Phil", "Tissot",
                    "Louis", "Apple")
    );

    SecretPlugin plugin = spy(new SecretPlugin() {
        @Override
        public Map<String, String> secretsForPath(String path) {
            return secrets.get(path);
        }

        @Override
        public void load(Map<String, String> properties) {}
    });

    SecretRenderer renderer = new SecretRenderer(plugin);

    @ParameterizedTest
    @CsvSource({
            "[secret/birthday/Greg],                                      November",
            "[secret/birthday/Phil],                                      October",
            "[secret/birthday/Louis],                                     February",
            "[secret/watch/Greg],                                         Seiko",
            "[secret/watch/Phil],                                         Tissot",
            "[secret/watch/Louis],                                        Apple",
            "Greg has a [secret/watch/Greg] watch,                        Greg has a Seiko watch",
            "Phil has a [secret/watch/Phil] watch,                        Phil has a Tissot watch",
            "No more [secret/watch/Louis] after [secret/birthday/Louis]?, No more Apple after February?",
    })
    void renderShouldReplaceTokensWithSecrets(String template, String expected) throws SecretRenderException {
        var result = renderer.render(template);

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "[secret/birthday/Greg] [secret/birthday/Phil] [secret/birthday/Louis], secret/birthday",
            "[secret/watch/Greg]    [secret/watch/Phil]    [secret/watch/Louis],    secret/watch",
    })
    void renderShouldCacheResultFromSecretsForPath(String template, String expectedPath) throws Exception {
        renderer.render(template);

        verify(plugin, times(1)).secretsForPath(expectedPath);
        verifyNoMoreInteractions(plugin);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plain old string",
            "string with a [",
            "string with a ]"
    })
    @EmptySource
    void renderShouldReturnTemplateWhenTemplateContainsNoToken(String template)
            throws SecretRenderException {
        var result = renderer.render(template);

        assertThat(result)
                .isEqualTo(template);
    }

    @Test
    void renderShouldThrowWhenTemplateIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> renderer.render(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "[no path and no key]",
            "[/no path]",
            "[no key/]"
    })
    void renderShouldThrowWhenTemplateContainsInvalidToken(String template) {
        assertThatExceptionOfType(SecretRenderException.class)
                .isThrownBy(() -> renderer.render(template));
    }

    @Test
    void renderShouldBubbleUpSecretExceptionAsSecretRenderException() throws SecretException {
        var exception = new SecretException("mock plugin exception", null);

        var plugin = mock(SecretPlugin.class);
        when(plugin.secretsForPath(any()))
                .thenThrow(exception);

        assertThatExceptionOfType(SecretRenderException.class)
                .isThrownBy(() -> new SecretRenderer(plugin).render("[path/key]"))
                .withCause(exception);
    }

    @Test
    void renderShouldThrowWhenSecretValueIsNull() {
        var plugin = new SecretPlugin() {
            @Override
            public Map<String, String> secretsForPath(String path) {
                return new HashMap<>();
            }

            @Override
            public void load(Map<String, String> properties) {}
        };

        assertThatExceptionOfType(SecretRenderException.class)
                .isThrownBy(() -> new SecretRenderer(plugin).render("[path/key]"))
                .withMessage("Context does not contain a value for token : path/key");
    }

}
