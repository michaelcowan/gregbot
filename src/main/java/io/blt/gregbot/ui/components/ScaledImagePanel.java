/*
 * Copyright (c) 2024 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.ui.components;

import java.awt.*;
import javax.swing.*;

public class ScaledImagePanel extends JPanel {

    private final Image image;

    private final int desiredWidth;
    private final int desiredHeight;

    public ScaledImagePanel(Image image, int desiredWidth, int desiredHeight) {
        this.desiredWidth = desiredWidth;
        this.desiredHeight = desiredHeight;

        this.image = scaleImage(image);

        setPreferredSizeToMatchImage();
    }

    public void setPreferredSizeToMatchImage() {
        this.setPreferredSize(new Dimension(desiredWidth, desiredHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(image, 0, 0, desiredWidth, desiredHeight, this);
    }

    private Image scaleImage(Image image) {
        var screenScale = getDefaultScreenScale();
        return image.getScaledInstance(
                multiply(desiredWidth, screenScale),
                multiply(desiredHeight, screenScale),
                Image.SCALE_SMOOTH);
    }

    private double getDefaultScreenScale() {
        return GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .getDefaultTransform()
                .getScaleX();
    }

    private int multiply(int i, double b) {
        return (int) Math.round(i * b);
    }
}
