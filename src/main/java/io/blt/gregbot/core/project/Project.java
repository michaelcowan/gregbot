/*
 * Copyright (c) 2023-2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.project;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents all data of the system with non-null and immutable guarantees.
 * This is to both simplify code and allow the records to be safely passed and accessed.
 * <p>
 * Also provides deserialization (currently only JSON) and validation capabilities e.g.
 * <pre>{@code
 * var project = Project.loadFromJson("project.json");
 * }</pre>
 * Which will:
 * <ol>
 *     <li>Load the resource file</li>
 *     <li>Deserialize into {@link Project} and nested {@code record}s</li>
 *     <li>Perform validation, throwing on failure</li>
 * </ol>
 * </p>
 */
public record Project(
        @Valid @NotNull Client client,
        @Valid @NotNull Map<@NotEmpty String, Secret> secrets,
        @Valid @NotNull Map<@NotEmpty String, Environment> environments,
        @Valid @NotNull Map<@NotEmpty String, Identity> identities,
        @Valid @NotNull Map<@NotEmpty String, Collection> collections) {

    public record Client(
            @NotNull Version version,
            @NotNull Redirect redirect,
            @Positive int connectionTimeout,
            @Positive int requestTimeout) {

        public enum Version {
            HTTP_1_1
        }

        public enum Redirect {
            NORMAL
        }
    }

    public record Secret(
            @Valid @NotNull Plugin plugin) {
    }

    public record Environment(
            @Valid @NotNull Map<@NotEmpty String, String> variables) {
    }

    public record Identity(
            String category,
            String secrets,
            @Valid @NotNull Map<@NotEmpty String, String> variables,
            @Valid Plugin plugin) {
    }

    public record Collection(
            @Valid @NotNull Map<@NotEmpty String, Request> requests,
            @Valid @NotNull Folder layout) {

        public record Request(
                @Valid @NotNull Map<@NotEmpty String, String> headers,
                @NotNull Verb verb,
                @NotEmpty String path) {

            public enum Verb {
                GET,
                HEAD,
                POST,
                PUT,
                DELETE,
                CONNECT,
                OPTIONS,
                TRACE,
                PATCH,
            }
        }

        public record Folder(
                @Valid @NotNull List<String> requests,
                @Valid @NotNull Map<@NotEmpty String, Folder> folders) {
        }
    }

    public record Plugin(
            @NotEmpty String type,
            @Valid @NotNull Map<@NotEmpty String, String> properties) {
    }

    private static final ObjectMapper MAPPER = buildMapper();
    private static final Validator VALIDATOR = buildValidator();

    /**
     * Returns an instance of {@link Project} created from a JSON resource file.
     * <p>
     * This method will:
     *     <ol>
     *         <li>Deserialize {@code stream} into {@link Project} and nested {@code record}s</li>
     *         <li>Perform validation, throwing on failure</li>
     *     </ol>
     * </p>
     *
     * @param stream resource file to load
     * @return instance of {@link Project}
     * @throws IOException if the stream cannot be read or there is a validation failure
     */
    public static Project loadFromJson(InputStream stream) throws IOException {
        return validateAndReturn(MAPPER.readValue(stream, Project.class));
    }

    /**
     * Overload of {@link Project#loadFromJson(InputStream)} that loads from a file.
     *
     * @param filename file to load
     * @return instance of {@link Project}
     * @throws IOException if the file cannot be read or there is a validation failure
     */
    public static Project loadFromJson(String filename) throws IOException {
        try (var stream = new FileInputStream(filename)) {
            return loadFromJson(stream);
        }
    }

    private static Project validateAndReturn(Project project) throws IOException {
        var violations = VALIDATOR.validate(project)
                .stream()
                .map(Project::describeViolation)
                .collect(Collectors.joining(", "));

        if (!violations.isEmpty()) {
            throw new IOException("Failed validation because " + violations);
        }

        return project;
    }

    private static <T> String describeViolation(ConstraintViolation<T> violation) {
        return String.format("'%s' %s", violation.getPropertyPath(), violation.getMessage());
    }

    private static ObjectMapper buildMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                .addModule(new ProjectModule())
                .build();
    }

    private static Validator buildValidator() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }
}
