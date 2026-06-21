/*
 * Copyright 2026 Frans Jacobs
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
package jcs.ui.layout.tiles.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import javax.swing.JPanel;
import jcs.ui.layout.tiles.LayoutScale;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import org.tinylog.Logger;

public class UnscaledCanvas extends JPanel {

  private static final long serialVersionUID = -6011606131297336781L;

  private boolean expanded;
  static final int DOT_GRID = 0;
  static final int LINE_GRID = 1;

  private int gridType = LINE_GRID;

  private int scalePercent;

  private static final BasicStroke GRID_STROKE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

  public UnscaledCanvas() {
    setLayout(null);
    setOpaque(true);
    setDoubleBuffered(true);
    scalePercent = LayoutScale.getInstance().getScalePercent();
  }

  public void changeScale(int scalePercent) {
    //if (scalePercent == scalePercent) {
    //  return;
    //}
    this.scalePercent = scalePercent;
    LayoutScale scale = LayoutScale.getInstance();
    scale.setScalePercent(scalePercent);

    // Resize and reposition every tile on the canvas
    for (Component c : getComponents()) {
      if (c instanceof Tile tile) {
        tile.setScaleImage(true);              // recalculates size
        tile.setBounds(tile.getTileBounds());  // recalculates position
      }
    }

    // Update canvas preferred size (TileCache uses canonical, so scale it)
    Dimension canonicalSize = TileCache.getMinCanvasSize();
    setPreferredSize(new Dimension(
            scale.toDisplay(canonicalSize.width),
            scale.toDisplay(canonicalSize.height)
    ));

    revalidate();
    repaint();
  }

  @Override
  public Component add(Component component) {
    super.add(component);
    if (component instanceof Tile tile) {
      tile.setBounds(tile.getTileBounds());
    }
    return component;
  }

//  private void paintDotGrid(Graphics g) {
//    Graphics2D gc = (Graphics2D) g;
//    Paint p = gc.getPaint();
//    gc.setPaint(Color.BLACK);
//    int w = getWidth(), h = getHeight();
//    for (int x = 0; x < w; x += 40) {
//      for (int y = 0; y < h; y += 40) {
//        gc.fillOval(x - 2, y - 2, 4, 4);
//      }
//    }
//    gc.setPaint(p);
//  }
  private void paintDotGrid(Graphics g) {
    int step = LayoutScale.getInstance().scaledTileSize();
    int width = getWidth();
    int height = getHeight();
    Graphics2D gc = (Graphics2D) g;
    Paint p = gc.getPaint();
    gc.setPaint(Color.BLACK);

    for (int x = 0; x < width; x += step) {
      for (int y = 0; y < height; y += step) {
        gc.fillOval(x - 2, y - 2, 4, 4);
      }
    }
  }

//  private void paintLineGrid(Graphics g) {
//    int width = getWidth();
//    int height = getHeight();
//    Graphics2D gc = (Graphics2D) g;
//    Paint p = gc.getPaint();
//    gc.setPaint(Color.lightGray);
//
//    gc.setStroke(GRID_STROKE);
//
//    for (int x = 0; x < width; x += 40) {
//      gc.drawLine(x, 0, x, height);
//    }
//    for (int y = 0; y < height; y += 40) {
//      gc.drawLine(0, y, width, y);
//    }
//    gc.setPaint(p);
//  }
  private void paintLineGrid(Graphics g) {
    int width = getWidth();
    int height = getHeight();

    Graphics2D gc = (Graphics2D) g;
    Paint p = gc.getPaint();
    gc.setPaint(Color.lightGray);
    gc.setStroke(GRID_STROKE);

    int grid = LayoutScale.getInstance().scaledGrid() * 2;

    for (int x = 0; x < width; x += grid) {
      gc.drawLine(x, 0, x, height);
    }

    for (int y = 0; y < height; y += grid) {
      gc.drawLine(0, y, width, y);
    }
  }

  void setGridType(int gridType) {
    this.gridType = gridType;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    long started = System.currentTimeMillis();
    super.paintComponent(g);

    if (this.gridType == LINE_GRID) {
      paintLineGrid(g);
    } else {
      paintDotGrid(g);
    }

    //paintGrid(g);
    long now = System.currentTimeMillis();
    Logger.trace("Duration: " + (now - started) + " ms.");
  }

  public boolean isExpanded() {
    return expanded;
  }

  public void setExpanded(boolean expanded) {
    this.expanded = expanded;
    this.repaint();
  }
}
