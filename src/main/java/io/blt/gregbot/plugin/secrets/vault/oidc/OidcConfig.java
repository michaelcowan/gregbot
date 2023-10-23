/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.plugin.secrets.vault.oidc;

public class OidcConfig {
    private String role = "";
    private String mount = "oidc";
    private String listenHost = "localhost";
    private int listenPort = 8250;
    private String listenPath = "/" + mount + "/callback";
    private int listenTimeout = 10;
    private String callbackScheme = "http";
    private String callbackHost = listenHost;
    private int callbackPort = listenPort;
    private String callbackPath = listenPath;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMount() {
        return mount;
    }

    public void setMount(String mount) {
        this.mount = mount;
    }

    public String getListenHost() {
        return listenHost;
    }

    public void setListenHost(String listenHost) {
        this.listenHost = listenHost;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public String getListenPath() {
        return listenPath;
    }

    public void setListenPath(String listenPath) {
        this.listenPath = listenPath;
    }

    public int getListenTimeout() {
        return listenTimeout;
    }

    public void setListenTimeout(int listenTimeout) {
        this.listenTimeout = listenTimeout;
    }

    public String getCallbackScheme() {
        return callbackScheme;
    }

    public void setCallbackScheme(String callbackScheme) {
        this.callbackScheme = callbackScheme;
    }

    public int getCallbackPort() {
        return callbackPort;
    }

    public void setCallbackPort(int callbackPort) {
        this.callbackPort = callbackPort;
    }

    public String getCallbackHost() {
        return callbackHost;
    }

    public void setCallbackHost(String callbackHost) {
        this.callbackHost = callbackHost;
    }

    public String getCallbackPath() {
        return callbackPath;
    }

    public void setCallbackPath(String callbackPath) {
        this.callbackPath = callbackPath;
    }
}
