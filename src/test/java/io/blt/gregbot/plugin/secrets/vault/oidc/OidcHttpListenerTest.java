/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault.oidc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static io.blt.gregbot.plugin.secrets.vault.oidc.OidcHttpListener.RequestConsumer.Request;
import static org.assertj.core.api.Assertions.assertThat;

class OidcHttpListenerTest {

    @Test
    void awaitShouldReturnFalseOnTimeout() throws IOException, InterruptedException {
        var result = new OidcHttpListener("localhost", 8250, "/", r -> {})
                .await(1);

        assertThat(result).isFalse();
    }

    @Test
    void awaitShouldReturnTrueOnConnection() throws IOException, InterruptedException {
        var listener = new OidcHttpListener("localhost", 8250, "/", r -> {});

        makeHttpCall("http://localhost:8250/oidc/callback");

        var result = listener.await(10);

        assertThat(result).isTrue();
    }

    @Test
    void httpRequestShouldReceiveSuccessHtml() throws IOException, InterruptedException {
        var listener = new OidcHttpListener("localhost", 8250, "/oidc/callback", r -> {});

        var result = makeHttpCall("http://localhost:8250/oidc/callback");

        listener.await(10);

        assertThat(result.statusCode())
                .isEqualTo(200);

        assertThat(result.body())
                .containsIgnoringCase("You have successfully logged into Vault");
    }

    @Test
    void requestConsumerShouldConsumeRequestContainingUrlDetails() throws IOException, InterruptedException {
        var request = new AtomicReference<Request>();

        var listener = new OidcHttpListener("localhost", 8250, "/oidc/callback", request::set);
        makeHttpCall("http://localhost:8250/oidc/callback?state=mock-state&code=mock-code&id_token=mock-id-token");
        listener.await(10);

        assertThat(request).hasValue(new Request("mock-state", "mock-code", "mock-id-token"));
    }

    private HttpResponse<String> makeHttpCall(String uri) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();

        try {
            return HttpClient.newBuilder().build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
