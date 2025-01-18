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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

public class UnscaledBlockCanvas extends JComponent implements PropertyChangeListener {

  private boolean showCenter;

  private final List<Tile> tiles;

  public UnscaledBlockCanvas() {
    tiles = new ArrayList<>();
  }

  public void addTile(Tile block) {
    this.tiles.add(block);
    this.add(block);
  }

  public boolean isShowCenter() {
    return showCenter;
  }

  public void setShowCenter(boolean showCenter) {
    this.showCenter = showCenter;
  }

  private Dimension getMinCanvasSize() {
    int minX = this.getSize().width;
    int maxX = 0;
    int minY = this.getSize().height;
    int maxY = 0;

    for (Tile tile : this.tiles) {
      Point tc = tile.getCenter();
      boolean expand = !tile.isScaleImage();
      int tw = tile.getWidth() * (expand ? 10 : 1);
      int th = tile.getHeight() * (expand ? 10 : 1);

      if (minX > tc.x - (tw / 2)) {
        minX = tc.x - (tw / 2);
      }
      if (maxX < tc.x + (tw / 2)) {
        maxX = tc.x + (tw / 2);
      }
      if (minY > tc.y - (th / 2)) {
        minY = tc.y - (th / 2);
      }
      if (maxY < tc.y + (th / 2)) {
        maxY = tc.y + (th / 2);
      }
    }

    int totalWidth = maxX - minX;
    if (totalWidth <= 120) {
      totalWidth = totalWidth + 20;
    }

    int totalHeight = maxY - minY;
    if (totalHeight <= 40) {
      totalHeight = totalHeight + 20;
    }

    //Logger.trace("MinX: " + minX + " maxX: " + maxX + " minY: " + minY + " maxY: " + maxY + " Width: " + totalWidth + " Height: " + totalHeight);
    return new Dimension(Math.abs(totalWidth), Math.abs(totalHeight));
  }

  private Dimension getMinRenderCanvasSize() {
    int minX = this.getSize().width;
    int maxX = 0;
    int minY = this.getSize().height;
    int maxY = 0;

    for (Tile tile : this.tiles) {
      Point tc = tile.getCenter();

      int tw = ((Block) tile).getRenderWidth();
      int th = ((Block) tile).getRenderHeight();

      if (minX > tc.x - (tw / 2)) {
        minX = tc.x - (tw / 2);
      }
      if (maxX < tc.x + (tw / 2)) {
        maxX = tc.x + (tw / 2);
      }
      if (minY > tc.y - (th / 2)) {
        minY = tc.y - (th / 2);
      }
      if (maxY < tc.y + (th / 2)) {
        maxY = tc.y + (th / 2);
      }
    }

    int totalWidth = maxX - minX;
    int totalHeight = maxY - minY;

    //Logger.trace("MinX: " + minX + " maxX: " + maxX + " minY: " + minY + " maxY: " + maxY + " Width: " + totalWidth + " Height: " + totalHeight);
    return new Dimension(Math.abs(totalWidth), Math.abs(totalHeight));
  }

  private BufferedImage paintTiles() {
    Dimension canvasSize = getMinCanvasSize();
    BufferedImage canvasImage = new BufferedImage(Math.abs(canvasSize.width), Math.abs(canvasSize.height), BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = canvasImage.createGraphics();

    g2d.setBackground(Color.white);
    g2d.clearRect(0, 0, canvasSize.width, canvasSize.height);

    for (Tile tile : tiles) {
//      tile.setDrawOutline(showCenter);

      //tile.drawTile(g2d, true);
 
      if (showCenter) {
        tile.drawCenterPoint(g2d, Color.red);
      }
    }
    g2d.dispose();

    return canvasImage;
  }

//  @Override
//  protected void paintComponent(Graphics g) {
//    Graphics2D g2d = (Graphics2D) g;
//    BufferedImage canvasImage = paintTiles();
//
//    //paint the image in the middle so
//    int w = this.getSize().width;
//    int h = this.getSize().height;
//
//    int cx = w / 2;
//    int cy = h / 2;
//
//    int bw = canvasImage.getWidth();
//    int bh = canvasImage.getHeight();
//
//    int pw = w;
//    int ph = h;
//    
//    if(bw > w) {
//       pw = w;
//    }
//    if(bh > h) {
//      ph = h;
//    }
//    
//    setPreferredSize(new Dimension(bw, bh));
//    //setPreferredSize(new Dimension(pw, ph));
//
//    int x = cx - (bw / 2);
//    int y = cy - (bh / 2);
//    g2d.drawImage(canvasImage, null, x, y);
//  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("repaintTile".equals(evt.getPropertyName())) {
      Tile tile = (Tile) evt.getNewValue();
      this.repaint(tile.getBounds());
    }
  }

  @Override
  public Dimension getPreferredSize() {
    if (!tiles.isEmpty()) {
      return getMinCanvasSize();
    } else {
      return this.getSize();
    }
  }

}
