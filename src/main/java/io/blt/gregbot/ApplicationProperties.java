/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class ApplicationProperties {

    private static Properties PROPERTIES;

    private ApplicationProperties() {
        throw new IllegalAccessError("Utility class should be accessed statically and never constructed");
    }

    public static String version() {
        return properties().getProperty("project.version", "DEVELOPMENT");
    }

    public static String name() {
        return properties().getProperty("project.name", "untitled");
    }

    public static String timestamp() {
        return properties().getProperty("project.timestamp", "");
    }

    public static String copyright() {
        return properties().getProperty("project.copyright", "");
    }

    public static Properties properties() {
        if (PROPERTIES == null) {
            PROPERTIES = loadProperties("/application.properties");
        }
        return PROPERTIES;
    }

    public static Properties loadProperties(String name) {
        try (var stream = ApplicationProperties.class.getResourceAsStream(name)) {
            var properties = new Properties();
            if (stream != null) {
                properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            }
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load and parse properties file '" + name + "'", e);
        }
    }

}
