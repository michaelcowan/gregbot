/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.identities.adobe.connector;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest(proxyMode = true)
class ImsServiceConnectorTest {

    @Test
    void tokenShouldReturnIssuedAccessToken() throws IOException {
        stubFor(issueAccessToken()
                .willReturn(okJson("""
                        {
                          "access_token": "mock-access-token",
                          "expires_in": 1000
                        }
                        """)));

        var result = new ImsServiceConnector("http://mock.ims", "mock-id", "mock-secret", "mock-code", "mock-scope")
                .token();

        assertThat(result)
                .isEqualTo("mock-access-token");
    }

    @Test
    void tokenShouldRefreshAccessTokenWhenExpires() throws IOException {
        stubFor(issueAccessToken()
                .willReturn(okJson("""
                        {
                          "refresh_token": "mock-refresh-token",
                          "expires_in": 0
                        }
                        """)));

        stubFor(refreshAccessToken("mock-refresh-token")
                .willReturn(okJson("""
                        {
                          "access_token": "mock-refreshed-access-token",
                          "expires_in": 1000
                        }
                        """)));

        var result = new ImsServiceConnector("http://mock.ims", "mock-id", "mock-secret", "mock-code", "mock-scope")
                .token();

        assertThat(result)
                .isEqualTo("mock-refreshed-access-token");
    }

    private MappingBuilder issueAccessToken() {
        return post("/ims/token/v1")
                .withHost(equalTo("mock.ims"))
                .withFormParam("client_id", equalTo("mock-id"))
                .withFormParam("client_secret", equalTo("mock-secret"))
                .withFormParam("code", equalTo("mock-code"))
                .withFormParam("scope", equalTo("mock-scope"))
                .withFormParam("grant_type", equalTo("authorization_code"));
    }

    private MappingBuilder refreshAccessToken(String refreshToken) {
        return post("/ims/token/v1")
                .withHost(equalTo("mock.ims"))
                .withFormParam("client_id", equalTo("mock-id"))
                .withFormParam("client_secret", equalTo("mock-secret"))
                .withFormParam("refresh_token", equalTo(refreshToken))
                .withFormParam("grant_type", equalTo("refresh_token"));
    }

}
