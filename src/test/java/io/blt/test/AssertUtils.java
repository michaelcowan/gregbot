/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public final class AssertUtils {

    private AssertUtils() {}

    public static void assertValidUtilityClass(Class<?> clazz) throws NoSuchMethodException {
        assertThat(clazz)
                .withFailMessage("Utility class must be final")
                .isFinal();

        assertThat(clazz.getDeclaredConstructors())
                .withFailMessage("Utility class must only have a zero argument constructor")
                .hasSize(1)
                .extracting(Constructor::getParameterCount)
                .hasSize(1);

        var constructor = clazz.getDeclaredConstructor();

        assertThat(constructor.getModifiers())
                .withFailMessage("Constructor must be private")
                .matches(Modifier::isPrivate);

        constructor.setAccessible(true);

        assertThatExceptionOfType(InvocationTargetException.class)
                .describedAs("Utility class should throw if constructed")
                .isThrownBy(constructor::newInstance)
                .havingCause()
                .isInstanceOf(IllegalAccessError.class)
                .withMessage("Utility class should be accessed statically and never constructed");
    }

}
