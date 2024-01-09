/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.blt.util.Ex;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.requireNonEmpty;

public class Connector {

    private static final HttpClient CLIENT = HttpClient.newBuilder().build();

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final String host;

    public Connector(String host) {
        requireNonEmpty(host, "'host' must not be empty");

        var lastCharIndex = host.length() - 1;
        this.host = host.charAt(lastCharIndex) == '/' ? host.substring(0, lastCharIndex) : host;
    }

    public <T> Result<T> send(HttpRequest request) throws IOException {
        return Ex.transformExceptions(
                () -> send(request, null),
                toIoException());
    }

    public <T> Result<T> send(HttpRequest request, Class<T> responseType) throws IOException {
        var result = Ex.transformExceptions(
                () -> CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray()),
                toIoException());
        return new Result<>(result, responseType);
    }

    public URI uriForPath(String path) {
        return URI.create(host + (path.charAt(0) == '/' ? path : "/" + path));
    }

    public static HttpRequest.BodyPublisher formFromMap(Map<String, String> map) {
        return HttpRequest.BodyPublishers.ofString(mapAsFormDataString(map));
    }

    private static String mapAsFormDataString(Map<String, String> map) {
        return map.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static String encode(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    private Function<? super Exception, IOException> toIoException() {
        return e -> e instanceof IOException ? (IOException) e : new IOException(e);
    }

    public static class Result<T> {
        private final HttpResponse<byte[]> response;
        private final T data;

        protected Result(HttpResponse<byte[]> response, Class<T> type) {
            this.response = response;
            this.data = decodeResponseElseNull(response, type);
        }

        public HttpResponse<byte[]> getResponse() {
            return response;
        }

        public Optional<T> getData() {
            return Optional.ofNullable(data);
        }

        public boolean is1xxInformational() {
            return response.statusCode() / 100 == 1;
        }

        public boolean is2xxSuccess() {
            return response.statusCode() / 100 == 2;
        }

        public boolean is3xxRedirection() {
            return response.statusCode() / 100 == 3;
        }

        public boolean is4xxClientError() {
            return response.statusCode() / 100 == 4;
        }

        public boolean is5xxServerError() {
            return response.statusCode() / 100 == 5;
        }

        public Optional<T> successData() {
            return is2xxSuccess() ? getData() : Optional.empty();
        }

        private T decodeResponseElseNull(HttpResponse<byte[]> response, Class<T> type) {
            return nonNull(type) ? tryDecodeAsJson(response, type).orElse(null) : null;
        }

        private Optional<T> tryDecodeAsJson(HttpResponse<byte[]> response, Class<T> type) {
            try {
                return Optional.of(MAPPER.readValue(response.body(), type));
            } catch (IOException e) {
                return Optional.empty();
            }
        }
    }

}
