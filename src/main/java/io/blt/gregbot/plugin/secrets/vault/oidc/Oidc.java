/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault.oidc;

import io.blt.gregbot.plugin.secrets.vault.connector.VaultConnector;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.AuthResponse;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.AuthUrlRequest;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.CallbackRequest;
import io.blt.util.functional.ThrowingConsumer;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.net.URIBuilder;

public class Oidc {

    private final OidcConfig config;
    private final VaultConnector connector;

    public Oidc(OidcConfig config, VaultConnector connector) {
        this.config = config;
        this.connector = connector;
    }

    public String fetchAuthTokenUsingDesktopBrowse() throws IOException, InterruptedException, TimeoutException {
        return fetchAuthToken(uri -> Desktop.getDesktop().browse(uri));
    }

    public String fetchAuthToken(ThrowingConsumer<URI, IOException> fetchAuth)
            throws IOException, InterruptedException, TimeoutException {
        var nonce = generateNonce();
        var authUrl = fetchAuthURL(nonce);
        return listenForToken(nonce, authUrl, fetchAuth);
    }

    private String generateNonce() {
        return UUID.randomUUID().toString();
    }

    private String fetchAuthURL(String nonce) throws IOException {
        var result = connector.fetchAuthUrl(
                new AuthUrlRequest(
                        config.mount(),
                        config.role(),
                        buildRedirectUrl(),
                        nonce));

        return result.getData()
                .map(AuthResponse::data)
                .map(AuthResponse.Data::authUrl)
                .orElseThrow(() -> new IllegalStateException("Failed to fetch Vault auth URL"));
    }

    private String buildRedirectUrl() {
        return new URIBuilder()
                .setScheme(config.callbackScheme())
                .setHost(config.callbackHost())
                .setPort(config.callbackPort())
                .setPath(config.callbackPath())
                .toString();
    }

    private String listenForToken(String nonce, String authUrl, ThrowingConsumer<URI, IOException> fetchAuth)
            throws IOException, InterruptedException, TimeoutException {
        var token = new AtomicReference<String>();

        var listener = new OidcHttpListener(
                config.listenHost(),
                config.listenPort(),
                config.listenPath(), request -> {
            var result = connector.fetchCallback(new CallbackRequest(
                    config.mount(),
                    request.state(),
                    request.code(),
                    request.idToken(),
                    nonce
            ));

            token.set(result.getData()
                    .map(AuthResponse::auth)
                    .map(AuthResponse.Auth::clientToken)
                    .orElseThrow());
        });

        fetchAuth.accept(URI.create(authUrl));

        if (!listener.await(config.listenTimeout())) {
            throw new TimeoutException(
                    "Timeout of " + config.listenTimeout() + " seconds expired while waiting for callback");
        }

        return token.get();
    }

}
