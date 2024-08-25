/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.properties;

import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.stream.Stream;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;

import static io.blt.test.TestUtils.loadAsString;
import static io.blt.test.TestUtils.pojoToJson;
import static io.blt.test.TestUtils.streamFieldsOfContainerTypes;
import static io.blt.test.TestUtils.streamFieldsOfNestedTypes;
import static io.blt.test.assertj.AnnotationAssertions.assertHasAnnotation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class PropertiesTest {

    static Stream<Field> shouldAnnotateWithValid() {
        return Stream.of(Properties.class.getNestMembers())
                .flatMap(t -> Stream.concat(
                        streamFieldsOfNestedTypes(t)
                                .filter(f -> !f.getType().isEnum()),
                        streamFieldsOfContainerTypes(t)));
    }

    @ParameterizedTest
    @MethodSource
    void shouldAnnotateWithValid(Field field) {
        assertHasAnnotation(field, Valid.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "minimal.json", "full.json"
    })
    void loadFromJsonShouldPassValidationFor(String filename) throws IOException {
        var result = Properties.loadFromJson(fileToStream(filename));

        assertThat(result).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "doesnt.exist", "empty.txt", "empty.json"
    })
    void loadFromJsonShouldFailValidationFor(String filename) {
        assertThatException().isThrownBy(() -> Properties.loadFromJson(fileToStream(filename)));
    }

    @Test
    void loadFromJsonShouldCreateEmptyMaps() throws IOException {
        var result = Properties.loadFromJson(fileToStream("minimal.json"));

        assertThat(result.secrets()).isNotNull().isEmpty();
        assertThat(result.environments()).isNotNull().isEmpty();
        assertThat(result.identities()).isNotNull().isEmpty();
        assertThat(result.collections()).isNotNull().isEmpty();
    }

    @Test
    void loadFromJsonShouldCreateUnmodifiableMaps() throws IOException {
        var result = Properties.loadFromJson(fileToStream("full.json"));

        assertThat(result.environments()).isUnmodifiable();
        assertThat(result.collections().get("Skynet").requests()).isUnmodifiable();
    }

    @Test
    void loadFromJsonShouldMaintainMapOrder() throws IOException {
        var result = Properties.loadFromJson(fileToStream("full.json"));

        assertThat(result.environments().keySet())
                .containsExactly("Cyberdyne Local", "Cyberdyne Stage", "Cyberdyne Prod");

        assertThat(result.collections().get("Skynet").requests().keySet())
                .containsExactly(
                        "Health Check",
                        "List Terminators",
                        "Fetch Terminator",
                        "Emergency Shutdown Terminator",
                        "List Aerials",
                        "Fetch Aerial",
                        "Emergency Shutdown Aerial");
    }

    @Test
    void loadFromJsonShouldCreateEmptyCollections() throws IOException {
        var result = Properties.loadFromJson(fileToStream("full.json"));
        var layout = result.collections().get("Skynet").layout();

        assertThat(layout.folders().get("Emergency").folders()).isNotNull().isEmpty();
    }

    @Test
    void loadFromJsonShouldCreateUnmodifiableCollections() throws IOException {
        var result = Properties.loadFromJson(fileToStream("full.json"));
        var layout = result.collections().get("Skynet").layout();

        assertThat(layout.requests()).isUnmodifiable();
        assertThat(layout.folders().get("Emergency").requests()).isUnmodifiable();
    }

    @Test
    void loadFromJsonShouldProduceObjectEqualToExpectedJson() throws IOException, JSONException {
        var expected = loadAsString(getClass(), "full-expected.json");

        var properties = Properties.loadFromJson(fileToStream("full.json"));

        var json = pojoToJson(properties);

        JSONAssert.assertEquals(expected, json, true);
    }

    private InputStream fileToStream(String filename) {
        return Properties.class.getResourceAsStream(filename);
    }

}
