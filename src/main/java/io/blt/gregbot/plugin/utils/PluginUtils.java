/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.Map;

public final class PluginUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);

    private PluginUtils() {
        throw new IllegalAccessError("Utility class should be accessed statically and never constructed");
    }

    /**
     * Returns an instance of {@code type} constructed from values in {@code properties}.
     * <p>
     *     If {@code properties} is missing a member of {@code type}, the value will default e.g. {@code null} or {@code 0}.
     *     If {@code properties} contains a key that does not match a member, the value will be ignored.
     * </p>
     *
     * @param type the target type to instantiate
     * @param properties {@link Map} containing values for the members of {@code type}
     * @return instance of {@code T}
     * @param <T> type variable for {@code type}.
     */
    public static <T> T propertiesToType(Class<T> type, Map<String, String> properties) {
        return MAPPER.convertValue(properties, type);
    }

}
