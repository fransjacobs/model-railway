/*
 * Copyright 2024 Frans Jacobs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.ui.layout.tiles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;

public class UnscaledBlockCanvas extends JPanel implements PropertyChangeListener {

  private Tile block;
  private boolean showCenter;

  private final Dimension originalSize;

  /**
   * Creates new form BlockCanvas
   */
  public UnscaledBlockCanvas() {
    this.setSize(1240, 440);
    this.originalSize = this.getSize();
    this.setPreferredSize(originalSize);

    this.setLayout(new BorderLayout());
  }

  public Tile getBlock() {
    return block;
  }

  public void setBlock(Tile block) {
    this.block = block;
  }

  public boolean isShowCenter() {
    return showCenter;
  }

  public void setShowCenter(boolean showCenter) {
    this.showCenter = showCenter;
  }

  private BufferedImage paintBlock() {
    if (block == null) {
      return null;
    }
    boolean expand = !((AbstractTile) block).isScaleImage();

    int bw = block.getWidth() * (expand ? 10 : 1);
    int bh = block.getHeight() * (expand ? 10 : 1);

    int w = bw + Tile.DEFAULT_WIDTH;
    int h = bh + Tile.DEFAULT_HEIGHT;

    BufferedImage blockImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = blockImage.createGraphics();
    g2d.setBackground(Color.white);
    g2d.clearRect(0, 0, w, h);

    int x = (w / 2);
    int y = (h / 2);
    Point blockCenter = new Point(x, y);
    //Logger.trace("BlockImageSize w: " + w + " h: " + h + " BlockCenter: (" + blockCenter.x + "," + blockCenter.y + ")");

    block.setCenter(blockCenter);
    block.drawTile(g2d, true);
    if (this.showCenter) {
      block.drawBounds(g2d);
      block.drawCenterPoint(g2d, Color.red);
    }
    g2d.dispose();
    return blockImage;
  }

  @Override
  protected void paintComponent(Graphics g) {
    //super.paintComponent(g);

    Graphics2D g2d = (Graphics2D) g;
    BufferedImage blockImage = this.paintBlock();

    //paint the image in the middle so
    int w = this.getSize().width;
    int h = this.getSize().height;

    int cx = w / 2;
    int cy = h / 2;

    int bw = blockImage.getWidth();
    int bh = blockImage.getHeight();

    this.setPreferredSize(new Dimension(bw, bh));

    int x = cx - (bw / 2);
    int y = cy - (bh / 2);

    g2d.drawImage(blockImage, null, x, y);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("repaintTile".equals(evt.getPropertyName())) {
      Tile tile = (Tile) evt.getNewValue();
      this.repaint(tile.getBounds());
    }
  }

  @Override
  public Dimension getPreferredSize() {
    if (block != null) {
      boolean expand = !((AbstractTile) block).isScaleImage();
      int bw = block.getWidth() * (expand ? 10 : 1);
      int bh = block.getHeight() * (expand ? 10 : 1);

      int w = bw + Tile.DEFAULT_WIDTH;
      int h = bh + Tile.DEFAULT_HEIGHT;
      return new Dimension(w, h);

    } else {
      return this.originalSize;
    }
  }

//  @Override
//  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
//    Logger.trace("visibleRect: " + visibleRect.toString() + " orientation: " + orientation + " direction" + direction);
//    return Tile.GRID;
//  }
//  @Override
//  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
//    Logger.trace("visibleRect: " + visibleRect.toString() + " orientation: " + orientation + " direction" + direction);
//    return Tile.GRID;
//  }
//  @Override
//  public boolean getScrollableTracksViewportWidth() {
//    int w;
//    if (getParent() instanceof JViewport jViewport) {
//      w = jViewport.getWidth();
//      Logger.trace("Viewport w: " + w);
//    } else {
//      w = this.getSize().width;
//      Logger.trace("Size w: " + w);
//    }
//
//    if (this.block != null) {
//      boolean expand = !((AbstractTile) block).isScaleImage();
//      int bw = block.getWidth() * (expand ? 10 : 1);
//      Logger.trace("Min Canvas w: " + w + " Block w: " + bw + "; " + !(bw > w));
//      return !(bw > w);
//    } else {
//      return true;
//    }
//  }
//  @Override
//  public boolean getScrollableTracksViewportHeight() {
//    int h;
//    if (getParent() instanceof JViewport jViewport) {
//      h = jViewport.getHeight();
//      Logger.trace("Viewport h: " + h);
//    } else {
//      h = this.getSize().height;
//      Logger.trace("Size h: " + h);
//    }
//
//    if (this.block != null) {
//      boolean expand = !((AbstractTile) block).isScaleImage();
//      int bh = block.getHeight() * (expand ? 10 : 1);
//      Logger.trace("Min Canvas h: " + h + " Block h: " + bh + "; " + !(bh > h));
//
//      return !(bh > h);
//    } else {
//      return true;
//    }
//  }
}
