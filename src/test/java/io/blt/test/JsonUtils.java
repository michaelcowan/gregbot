/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {

    private static final ObjectMapper MAPPER = buildMapper();

    public JsonUtils() {
        throw new IllegalAccessError("Utility class should be accessed statically and never constructed");
    }

    public static <T> T parseJsonTo(Class<T> valueType, String json) throws JsonProcessingException {
        return MAPPER.readValue(json, valueType);
    }

    private static ObjectMapper buildMapper() {
        return new ObjectMapper();
    }

}
