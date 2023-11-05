/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault.connector;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.AuthResponse;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.AuthUrlRequest;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.CallbackRequest;
import java.io.IOException;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest(proxyMode = true)
class VaultConnectorTest {

    @Test
    void fetchAuthUrlShouldPostAuthUrlRequestAndReturnAuthUrlResponse() throws IOException {
        stubFor(post("/v1/auth/mock-mount/oidc/auth_url")
                .withHost(equalTo("mock.vault"))
                .withFormParam("role", equalTo("mock-role"))
                .withFormParam("redirect_uri", equalTo("mock-redirect-url"))
                .withFormParam("client_nonce", equalTo("mock-client-nonce"))
                .willReturn(okJson("""
                        {
                          "request_id": "mock-request-id",
                          "lease_id": "mock-lease-id",
                          "renewable": false,
                          "least_duration": 10,
                          "data": {
                            "auth_url": "mock-auth-url"
                          }
                        }
                        """)));

        var result = new VaultConnector("http://mock.vault")
                .fetchAuthUrl(new AuthUrlRequest(
                        "mock-mount",
                        "mock-role",
                        "mock-redirect-url",
                        "mock-client-nonce"));

        assertThat(result.getResponse())
                .extracting(HttpResponse::statusCode)
                .isEqualTo(200);

        assertThat(result.getData())
                .containsInstanceOf(AuthResponse.class)
                .get()
                .extracting(
                        AuthResponse::requestId,
                        AuthResponse::leaseId,
                        AuthResponse::renewable,
                        AuthResponse::leastDuration,
                        r -> r.data().authUrl())
                .containsExactly(
                        "mock-request-id",
                        "mock-lease-id",
                        false,
                        10,
                        "mock-auth-url");
    }

    @Test
    void fetchCallbackShouldPostCallbackRequestAndReturnCallbackResponse() throws IOException {
        stubFor(get("/v1/auth/mock-mount/oidc/callback" +
                    "?state=mock-state" +
                    "&code=mock-code" +
                    "&id_token=mock-id-token" +
                    "&client_nonce=mock-client-nonce")
                .withHost(equalTo("mock.vault"))
                .willReturn(okJson("""
                        {
                          "auth": {
                            "client_token": "mock-client-token"
                          }
                        }
                        """)));

        var result = new VaultConnector("http://mock.vault")
                .fetchCallback(new CallbackRequest(
                        "mock-mount",
                        "mock-state",
                        "mock-code",
                        "mock-id-token",
                        "mock-client-nonce"));

        assertThat(result.getResponse())
                .extracting(HttpResponse::statusCode)
                .isEqualTo(200);

        assertThat(result.getData())
                .containsInstanceOf(AuthResponse.class)
                .get()
                .extracting(r -> r.auth().clientToken())
                .isEqualTo("mock-client-token");
    }

}
