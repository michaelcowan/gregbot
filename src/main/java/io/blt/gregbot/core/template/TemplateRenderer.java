/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.template;

import io.blt.util.functional.ThrowingFunction;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

public class TemplateRenderer {

    private final Pattern pattern;

    private TemplateRenderer(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public static TemplateRenderer square() {
        return new TemplateRenderer("\\[(.+?)\\]");
    }

    public static TemplateRenderer curly() {
        return new TemplateRenderer("\\{(.+?)\\}");
    }

    public String render(String template, Map<String, String> context) throws UnknownTokenException {
        var matcher = pattern.matcher(template);
        var result = new StringBuilder();
        while (matcher.find()) {
            var token = matcher.group(1);
            var value = context.get(token);
            if (isNull(value)) {
                throw new UnknownTokenException("Context does not contain a value for token " + token);
            }
            matcher.appendReplacement(result, value);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public <E extends Exception> String render(String template, ThrowingFunction<String, String, E> valueResolver)
            throws E, UnknownTokenException {
        var matcher = pattern.matcher(template);
        var result = new StringBuilder();
        while (matcher.find()) {
            var token = matcher.group(1);
            var value = valueResolver.apply(token);
            if (isNull(value)) {
                throw new UnknownTokenException("Context does not contain a value for token " + token);
            }
            matcher.appendReplacement(result, value);
        }
        matcher.appendTail(result);
        return result.toString();
    }

}
