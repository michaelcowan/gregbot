/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault.oidc;

import io.blt.gregbot.plugin.connector.Connector;
import io.blt.gregbot.plugin.secrets.vault.connector.VaultConnector;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.AuthUrlRequest;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.AuthUrlResponse;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.CallbackRequest;
import io.blt.gregbot.plugin.secrets.vault.connector.dto.CallbackResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.blt.test.TestUtils.doIgnoreExceptions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OidcTest {

    @Mock
    VaultConnector connector;

    @Captor
    ArgumentCaptor<AuthUrlRequest> authUrlRequestCaptor;

    @Mock
    Connector.Result<AuthUrlResponse> authUrlResponse;

    @Captor
    ArgumentCaptor<CallbackRequest> callbackRequestCaptor;

    @Mock
    Connector.Result<CallbackResponse> callbackResponse;

    final OidcConfig config = OidcConfig.from(Map.of("listenTimeout", "1"));

    Oidc oidc;

    @BeforeEach
    void beforeEach() throws IOException {
        this.oidc = new Oidc(config, connector);

        when(authUrlResponse.getData())
                .thenReturn(Optional.of(new AuthUrlResponse(
                        null,
                        null,
                        false,
                        0,
                        new AuthUrlResponse.Data("http://localhost:8250/oidc/callback"),
                        null,
                        null,
                        null
                )));

        when(connector.fetchAuthUrl(authUrlRequestCaptor.capture()))
                .thenReturn(authUrlResponse);

        lenient().when(callbackResponse.getData())
                .thenReturn(Optional.of(new CallbackResponse(
                        new CallbackResponse.Auth("mock-token")
                )));

        lenient().when(connector.fetchCallback(callbackRequestCaptor.capture()))
                .thenReturn(callbackResponse);
    }

    @Test
    void fetchAuthTokenShouldThrowTimeoutExceptionWhenCallbackNotCalled() {
        assertThatExceptionOfType(TimeoutException.class)
                .isThrownBy(() -> oidc.fetchAuthToken(uri -> {}))
                .withMessage("Timeout of 1 seconds expired while waiting for callback");
    }

    @Test
    void fetchAuthTokenShouldCallConnectorFetchAuthUrl() {
        doIgnoreExceptions(() -> oidc.fetchAuthToken(uri -> {}));

        assertThat(authUrlRequestCaptor.getAllValues())
                .hasSize(1)
                .first()
                .extracting(AuthUrlRequest::mount, AuthUrlRequest::role, AuthUrlRequest::redirectUrl)
                .containsExactly("oidc", "", "http://localhost:8250/oidc/callback");

        assertThat(authUrlRequestCaptor.getValue().clientNonce())
                .matches("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$");
    }

    @Test
    void fetchAuthTokenShouldInvokeConsumerWithAuthUrl() {
        var fetchAuthUri = new AtomicReference<URI>();

        doIgnoreExceptions(() -> oidc.fetchAuthToken(fetchAuthUri::set));

        assertThat(fetchAuthUri.get())
                .isEqualTo(URI.create("http://localhost:8250/oidc/callback"));
    }

    @Test
    void fetchAuthTokenShouldReturnTokenWhenCallingAuthUrl()
            throws IOException, InterruptedException, TimeoutException {
        var result = oidc.fetchAuthToken(this::makeHttpCall);

        assertThat(result)
                .isEqualTo("mock-token");
    }

    private void makeHttpCall(URI uri) {
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        try {
            HttpClient.newBuilder().build()
                    .send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
