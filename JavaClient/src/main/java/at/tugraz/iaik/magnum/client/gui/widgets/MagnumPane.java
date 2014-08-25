/*******************************************************************************
 * Copyright 2013 Alexander Jesner, Bernd Prünster
 * Copyright 2013, 2014 Bernd Prünster
 *
 *     This file is part of Magnum PI.
 *
 *     Magnum PI is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Magnum PI is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Magnum PI.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package at.tugraz.iaik.magnum.client.gui.widgets;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDesktopPane;

import at.tugraz.iaik.magnum.client.gui.GuiConstants;

public class MagnumPane extends JDesktopPane {

  private static final long serialVersionUID = -1536285761810515183L;
  private Image magnumBG, resizedBG;

  public MagnumPane() {
    try {
      magnumBG = ImageIO.read(this.getClass().getResourceAsStream(GuiConstants.PATH_RES_ICONS + "magnum_bg.png"));
    } catch (IOException e) {
      magnumBG = new BufferedImage(0, 0, BufferedImage.TYPE_BYTE_BINARY);
    }

    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        resizedBG = getBackgroundImage();
        repaint();
      }
    });
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(resizedBG, 0, 0, null);

  }

  private BufferedImage getBackgroundImage() {
    BufferedImage resizedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = resizedImage.createGraphics();
    Point dim = getImageDimensions();
    int x = (getWidth() - dim.x) / 2;
    int y = (getHeight() - dim.y) / 2;
    // g.dispose();
    // g.setComposite(AlphaComposite.Src);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.drawImage(magnumBG, x, y, dim.x, dim.y, null);
    return resizedImage;
  }

  private Point getImageDimensions() {
    double magW = magnumBG.getWidth(null);
    double magH = magnumBG.getHeight(null);
    double width = getWidth();
    double height = getHeight();
    boolean horz = height > width;
    double ratio = height / magH;
    if (horz)
      ratio = width / magW;
    return new Point((int) (magW * ratio), (int) (magH * ratio));
  }
}
