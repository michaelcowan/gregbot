/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesModuleTest {

    ObjectMapper mapper;

    @BeforeEach
    void beforeEach() {
        mapper = new ObjectMapper();
        mapper.registerModule(new PropertiesModule());
    }

    @Test
    void shouldDeserializeNonNullMapAsMap() throws JsonProcessingException {
        var result = mapper.readValue("""
                {
                    "map": {
                        "Greg": "Seiko",
                        "Phil": "Tissot"
                    }
                }
                """, MapType.class);

        assertThat(result.map())
                .containsExactly(
                        Map.entry("Greg", "Seiko"),
                        Map.entry("Phil", "Tissot"));
    }

    @Test
    void shouldDeserializeMapWithPojoValueType() throws JsonProcessingException {
        var result = mapper.readValue("""
                {
                    "map":
                    {
                        "key":
                        {
                            "string": "value"
                        }
                    }
                }
                """, MapTypeWithPojoValueType.class);

        assertThat(result.map().get("key"))
                .extracting(PojoType::string)
                .isEqualTo("value");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"map\": null}",
            "{\"map\": {}}"
    })
    void shouldDeserializeNullMapAsEmpty(String json) throws JsonProcessingException {
        var result = mapper.readValue(json, MapType.class);

        assertThat(result.map()).isNotNull().isEmpty();
    }

    @Test
    void shouldDeserializeMapMaintainingKeyOrder() throws JsonProcessingException {
        var keys = listOfRandomStrings(100);
        var keysAsJson = listOfStringsToJsonMapKeys(keys);

        var result = mapper.readValue("""
                {
                    "map": {
                        %s
                    }
                }
                """.formatted(keysAsJson), MapType.class);

        assertThat(result.map().keySet()).containsExactlyElementsOf(keys);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"map\": null}",
            "{\"map\": {}}",
            "{\"map\": {\"key\": {\"string\": \"value\" }}}"
    })
    void shouldDeserializeMapAsUnmodifiable(String json) throws JsonProcessingException {
        var result = mapper.readValue(json, MapTypeWithPojoValueType.class);

        assertThat(result.map()).isUnmodifiable();
    }

    @Test
    void shouldDeserializeNonNullListAsList() throws JsonProcessingException {
        var result = mapper.readValue("""
                {
                    "list": [
                        "Sven",
                        "Greg",
                        "Phil",
                        "Louis"
                    ]
                }
                """, ListType.class);

        assertThat(result.list()).containsExactly("Sven", "Greg", "Phil", "Louis");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"list\": null}",
            "{\"list\": []}",
    })
    void shouldDeserializeNullListAsEmpty(String json) throws JsonProcessingException {
        var result = mapper.readValue(json, ListType.class);

        assertThat(result.list()).isNotNull().isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"list\": null}",
            "{\"list\": []}",
            "{\"list\": [\"value\"]}"
    })
    void shouldDeserializeListAsUnmodifiable(String json) throws JsonProcessingException {
        var result = mapper.readValue(json, ListType.class);

        assertThat(result.list()).isUnmodifiable();
    }

    @Nested
    class UnsupportedTypes {

        @Test
        void shouldDeserializeNonNullSetAsSet() throws JsonProcessingException {
            var result = mapper.readValue("""
                    {
                        "set": [
                            "Sven",
                            "Greg",
                            "Phil",
                            "Louis"
                        ]
                    }
                    """, SetType.class);

            assertThat(result.set()).containsExactly("Sven", "Greg", "Phil", "Louis");
        }

    }

    private List<String> listOfRandomStrings(int listSize) {
        return IntStream.range(0, listSize)
                .mapToObj(i -> UUID.randomUUID())
                .map(UUID::toString)
                .toList();
    }

    private String listOfStringsToJsonMapKeys(List<String> strings) {
        return strings.stream()
                .map("""
                        "%s": ""
                        """::formatted)
                .collect(Collectors.joining(","));
    }

    record MapType(Map<String, String> map) {}
    record ListType(List<String> list) {}
    record SetType(Set<String> set) {}
    record PojoType(String string) {}
    record MapTypeWithPojoValueType(Map<String, PojoType> map) {}

}
