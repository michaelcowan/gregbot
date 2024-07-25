/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault.oidc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

public class OidcHttpListener {

    @FunctionalInterface
    public interface RequestConsumer {
        record Request(String state, String code, String idToken) {}
        void accept(Request request) throws IOException;
    }

    private final CountDownLatch latch = new CountDownLatch(1);
    private final RequestConsumer requestConsumer;
    private final byte[] successResponse;
    private final HttpServer server;

    public OidcHttpListener(
            String hostname, int port, String path, RequestConsumer requestConsumer) throws IOException {
        this.requestConsumer = requestConsumer;
        this.successResponse = loadResource("success.html");
        this.server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
        this.server.createContext(path, httpHandler());
        this.server.start();
    }

    public boolean await(int timeoutInSeconds) throws InterruptedException {
        var result = latch.await(timeoutInSeconds, TimeUnit.SECONDS);
        server.stop(0);
        return result;
    }

    private HttpHandler httpHandler() {
        return exchange -> {
            try {
                requestConsumer.accept(parseRequest(exchange));
                write(exchange, 200, successResponse);
            } finally {
                latch.countDown();
            }
        };
    }

    private RequestConsumer.Request parseRequest(HttpExchange exchange) {
        return buildRequest(paramsToMap(exchange.getRequestURI()));
    }

    private Map<String, String> paramsToMap(URI uri) {
        return new URIBuilder(uri)
                .getQueryParams()
                .stream()
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
    }

    private RequestConsumer.Request buildRequest(Map<String, String> params) {
        return new RequestConsumer.Request(params.get("state"), params.get("code"), params.get("id_token"));
    }

    private void write(HttpExchange exchange, int code, byte[] response) throws IOException {
        exchange.sendResponseHeaders(code, response.length);
        var os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }

    private byte[] loadResource(String filename) throws IOException {
        try (var resource = OidcHttpListener.class.getResourceAsStream(filename)) {
            return Objects.requireNonNull(resource).readAllBytes();
        }
    }

}
