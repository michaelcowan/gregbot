/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public final class TestUtils {

    private TestUtils() {
        throw new IllegalAccessError("Utility class should be accessed statically and never constructed");
    }

    public static <T> Stream<Field> streamFieldsOfNestedTypes(Class<T> type) {
        var memberTypes = Stream.of(type.getNestMembers()).toList();
        return Stream.of(type.getDeclaredFields())
                .filter(f -> memberTypes.contains(f.getType()));
    }

    public static <T> Stream<Field> streamFieldsOfContainerTypes(Class<T> type) {
        return Stream.concat(streamFieldsOfCollectionTypes(type), streamFieldsOfMapTypes(type));
    }

    public static <T> Stream<Field> streamFieldsOfCollectionTypes(Class<T> type) {
        return Stream.of(type.getDeclaredFields())
                .filter(f -> Collection.class.isAssignableFrom(f.getType()));
    }

    public static <T> Stream<Field> streamFieldsOfMapTypes(Class<T> type) {
        return Stream.of(type.getDeclaredFields())
                .filter(f -> Map.class.isAssignableFrom(f.getType()));
    }

}
