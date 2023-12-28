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
import java.util.Objects;
import java.util.regex.Pattern;

import static io.blt.util.Ctr.computeIfAbsent;
import static io.blt.util.Obj.throwIf;

/**
 * Manage rendering secret templates including calling SecretPlugin.
 * Results from calling {@code SecretPlugin} are cached where possible for performance reasons.
 */
public class SecretRenderer {

    private final SecretPlugin plugin;

    private final Pattern pattern = Pattern.compile("\\[(.+?)]");

    private final Map<String, Map<String, String>> cache = new HashMap<>();

    public SecretRenderer(SecretPlugin plugin) {
        this.plugin = plugin;
    }

    public String render(String template) throws SecretRenderException {
        var matcher = pattern.matcher(template);
        var result = new StringBuilder();
        while (matcher.find()) {
            var token = matcher.group(1);
            var value = renderSecretFor(token);
            matcher.appendReplacement(result, value);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String renderSecretFor(String token) throws SecretRenderException {
        int index = throwIf(token.lastIndexOf('/'), i -> i == -1,
                () -> new SecretRenderException("Token must be of the form '[path/key]'. Missing '/' : " + token));
        var path = throwIf(token.substring(0, index), String::isEmpty,
                () -> new SecretRenderException("Token must be of the form '[path/key]'. Missing 'path/' : " + token));
        var key = throwIf(token.substring(index + 1), String::isEmpty,
                () -> new SecretRenderException("Token must be of the form '[path/key]'. Missing '/key' : " + token));

        try {
            var secrets = computeIfAbsent(cache, path, plugin::secretsForPath);
            return throwIf(secrets.get(key), Objects::isNull,
                    () -> new SecretRenderException("Context does not contain a value for token : " + token));
        } catch (SecretException e) {
            throw new SecretRenderException("Failed to fetch secret for token : " + token, e);
        }
    }

}
