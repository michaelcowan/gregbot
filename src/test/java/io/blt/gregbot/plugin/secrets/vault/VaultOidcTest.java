/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

class VaultOidcTest {

    final Map<String, String> requiredProperties = Map.of(
            "host", "mock-host"
    );

    @Test
    void loadShouldNotThrowWhenPropertiesArePresent() {
        assertThatNoException()
                .isThrownBy(() -> new VaultOidc().load(requiredProperties));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "host"
    })
    void loadShouldThrowWhenPropertiesIsMissingKey(String key) {
        var properties = requiredPropertiesWithout(key);
        assertThatNullPointerException()
                .isThrownBy(() -> new VaultOidc().load(properties));
    }

    private Map<String, String> requiredPropertiesWithout(String key) {
        var properties = new HashMap<>(requiredProperties);
        properties.remove(key);
        return properties;
    }

}
