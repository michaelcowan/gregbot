/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault.connector;

import io.blt.gregbot.plugin.connector.Connector;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.AuthUrlRequest;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.AuthUrlResponse;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.CallbackRequest;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.CallbackResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;
import org.apache.hc.core5.net.URIBuilder;

public class VaultConnector extends Connector {

    public VaultConnector(String host) {
        super(host);
    }

    public Result<AuthUrlResponse> fetchAuthUrl(AuthUrlRequest authUrlRequest) throws IOException {
        var request = HttpRequest.newBuilder()
                .uri(uriForPath("/v1/auth/" + authUrlRequest.mount() + "/oidc/auth_url"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(formFromMap(Map.of(
                        "role", authUrlRequest.role(),
                        "redirect_uri", authUrlRequest.redirectUrl(),
                        "client_nonce", authUrlRequest.clientNonce())))
                .build();

        return send(request, AuthUrlResponse.class);
    }

    public Result<CallbackResponse> fetchCallback(CallbackRequest callbackRequest) throws IOException {
        var uri = new URIBuilder(uriForPath("/v1/auth/" + callbackRequest.mount() + "/oidc/callback"))
                .addParameter("state", callbackRequest.state())
                .addParameter("code", callbackRequest.code())
                .addParameter("id_token", callbackRequest.idToken())
                .addParameter("client_nonce", callbackRequest.clientNonce())
                .toString();

        var request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();

        return send(request, CallbackResponse.class);
    }

}
