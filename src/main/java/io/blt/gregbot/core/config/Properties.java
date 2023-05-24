/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blt.gregbot.core.identity.IdentityProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public record Properties(
        @NotNull @Valid Map<String, IdentityProperties> identities) {

    public record Plugin(
            @NotNull String type,
            @NotNull ObjectNode properties) {}

    private static final ObjectMapper MAPPER = buildMapper();
    private static final Validator VALIDATOR = buildValidator();

    public static Properties load(String filename) throws IOException {
        try (var stream = Properties.class.getResourceAsStream(filename)) {
            if (isNull(stream)) {
                throw new IOException("Cannot read file '" + filename + "'");
            }

            return validateAndReturn(MAPPER.readValue(stream, Properties.class));
        }
    }

    private static Properties validateAndReturn(Properties properties) throws IOException {
        var violations = VALIDATOR.validate(properties)
                .stream()
                .map(Properties::describe)
                .collect(Collectors.joining(", "));

        if (!violations.isEmpty()) {
            throw new IOException("Failed validation because " + violations);
        }

        return properties;
    }

    private static <T> String describe(ConstraintViolation<T> violation) {
        return String.format("'%s' %s", violation.getPropertyPath(), violation.getMessage());
    }

    private static ObjectMapper buildMapper() {
        return new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static Validator buildValidator() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }

}
