/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import java.awt.*;
import javax.swing.*;

public final class ApplicationResources {

    public enum ToolIcon {
        TRASH("icons/tools/trash.svg"),
        WRAP("icons/tools/wrap.svg"),
        ALIGN_BOTTOM("icons/tools/align-bottom.svg"),
        ;

        private final String filename;

        ToolIcon(String filename) {
            this.filename = filename;
        }
    }

    private static final List<String> ICONS = List.of(
            "/icons/512.png",
            "/icons/256.png",
            "/icons/128.png",
            "/icons/64.png",
            "/icons/32.png",
            "/icons/16.png");

    private ApplicationResources() {
        throw new IllegalAccessError("Utility class should be accessed statically and never constructed");
    }

    public static Icon toolIcon(ToolIcon toolIcon) {
        return new FlatSVGIcon(toolIcon.filename, 18, 18)
                .setColorFilter(new FlatSVGIcon.ColorFilter(
                        color -> UIManager.getColor("Button.foreground")));
    }

    public static List<Image> icons() {
        return ICONS.stream()
                .map(ApplicationResources::getResourceAsImage)
                .toList();
    }

    public static Image largestIcon() {
        return getResourceAsImage(ICONS.get(0));
    }

    public static Image getResourceAsImage(String resource) {
        return new ImageIcon(getResource(resource)).getImage();
    }

    public static InputStream getResourceAsStream(String resource) {
        return ApplicationResources.class.getResourceAsStream(resource);
    }

    public static URL getResource(String resource) {
        return ApplicationResources.class.getResource(resource);
    }

}
