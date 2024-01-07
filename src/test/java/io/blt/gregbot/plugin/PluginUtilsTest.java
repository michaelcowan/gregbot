/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin;

import io.blt.gregbot.plugin.utils.PluginUtils;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static io.blt.gregbot.plugin.utils.PluginUtils.propertiesToType;
import static io.blt.test.AssertUtils.assertValidUtilityClass;
import static org.assertj.core.api.Assertions.assertThat;

class PluginUtilsTest {

    private record TestType(String name, int age, String favouriteFood) {}

    @Test
    void shouldBeValidUtilityClass() throws NoSuchMethodException {
        assertValidUtilityClass(PluginUtils.class);
    }

    @Test
    void propertiesToTypeShouldCreateAnInstanceOfTheTargetTypeFromPassedProperties() {
        var result = propertiesToType(TestType.class, Map.of(
                "name", "Mike",
                "age", "80",
                "favouriteFood", "pizza"));

        assertThat(result)
                .isInstanceOf(TestType.class)
                .extracting(TestType::name, TestType::age, TestType::favouriteFood)
                .containsExactly("Mike", 80, "pizza");
    }

    @Test
    void propertiesToTypeShouldDefaultMissingValuesToNull() {
        var result = propertiesToType(TestType.class, Map.of(
                "name", "Mike"));

        assertThat(result)
                .isInstanceOf(TestType.class)
                .extracting(TestType::name, TestType::age, TestType::favouriteFood)
                .containsExactly("Mike", 0, null);
    }

    @Test
    void propertiesToTypeShouldIgnoreUnknownKeys() {
        var result = propertiesToType(TestType.class, Map.of(
                "name", "Mike",
                "height", "extra medium",
                "age", "80",
                "favouriteFood", "pizza"));

        assertThat(result)
                .isInstanceOf(TestType.class)
                .extracting(TestType::name, TestType::age, TestType::favouriteFood)
                .containsExactly("Mike", 80, "pizza");
    }

}
