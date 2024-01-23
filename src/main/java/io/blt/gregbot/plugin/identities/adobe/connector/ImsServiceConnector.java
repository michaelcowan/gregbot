/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.identities.adobe.connector;

import io.blt.gregbot.plugin.connector.Connector;
import io.blt.gregbot.plugin.identities.adobe.connector.dto.Token;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.Map;

public class ImsServiceConnector extends Connector implements ImsConnector {

    private final String id;
    private final String secret;
    private final String code;
    private final String scope;

    private Token token;
    private long expiresAt;

    public ImsServiceConnector(String host, String id, String secret, String code, String scope) throws IOException {
        super(host);

        this.id = id;
        this.secret = secret;
        this.code = code;
        this.scope = scope;

        issueAccessToken();
    }

    @Override
    public String token() throws IOException {
        if (System.currentTimeMillis() >= expiresAt) {
            refreshAccessToken();
        }

        return token.accessToken();
    }

    private void issueAccessToken() throws IOException {
        var request = HttpRequest.newBuilder()
                .uri(uriForPath("/ims/token/v1"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(formFromMap(Map.of(
                        "client_id", id,
                        "client_secret", secret,
                        "code", code,
                        "scope", scope,
                        "grant_type", "authorization_code")))
                .build();

        processResult(send(request, Token.class));
    }

    private void refreshAccessToken() throws IOException {
        var request = HttpRequest.newBuilder()
                .uri(uriForPath("/ims/token/v1"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(formFromMap(Map.of(
                        "client_id", id,
                        "client_secret", secret,
                        "refresh_token", token.refreshToken(),
                        "grant_type", "refresh_token")))
                .build();

        processResult(send(request, Token.class));
    }

    private void processResult(Result<Token> result) throws IOException {
        var now = System.currentTimeMillis();

        token = result
                .successData()
                .orElseThrow(() -> new IOException("Failed to issue auth token"));

        expiresAt = now + token.expiresIn();
    }
}
