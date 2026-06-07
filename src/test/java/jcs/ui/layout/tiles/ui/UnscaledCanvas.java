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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import javax.swing.JPanel;
import jcs.ui.layout.tiles.Tile;
import org.tinylog.Logger;

public class UnscaledCanvas extends JPanel {

  private boolean expanded;
  static final int DOT_GRID = 0;
  static final int LINE_GRID = 1;

  private int gridType = LINE_GRID;

  private static final BasicStroke GRID_STROKE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

  public UnscaledCanvas() {
    setLayout(null);
    setOpaque(true);
    setDoubleBuffered(true);
  }

  @Override
  public Component add(Component component) {
    super.add(component);
    if (component instanceof Tile tile) {
      tile.setBounds(tile.getTileBounds());
    }
    return component;
  }

  private void paintDotGrid(Graphics g) {
    Graphics2D gc = (Graphics2D) g;
    Paint p = gc.getPaint();
    gc.setPaint(Color.BLACK);
    int w = getWidth(), h = getHeight();
    for (int x = 0; x < w; x += 40) {
      for (int y = 0; y < h; y += 40) {
        gc.fillOval(x - 2, y - 2, 4, 4);
      }
    }
    gc.setPaint(p);
  }

  private void paintLineGrid(Graphics g) {
    int width = getWidth();
    int height = getHeight();
    Graphics2D gc = (Graphics2D) g;
    Paint p = gc.getPaint();
    gc.setPaint(Color.lightGray);

    gc.setStroke(GRID_STROKE);

    for (int x = 0; x < width; x += 40) {
      gc.drawLine(x, 0, x, height);
    }
    for (int y = 0; y < height; y += 40) {
      gc.drawLine(0, y, width, y);
    }
    gc.setPaint(p);
  }

  void setGridType(int gridType) {
    this.gridType = gridType;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    long started = System.currentTimeMillis();
    super.paintComponent(g);

    if(this.gridType == LINE_GRID) {
      paintLineGrid(g);
    } else {
      paintDotGrid(g);
    }
    
    //paintGrid(g);
    long now = System.currentTimeMillis();
    Logger.trace("Duration: " + (now - started) + " ms.");
  }

//  private void paintGrid(Graphics g) {
//    int width = this.getWidth();
//    int height = this.getHeight();
//
//    int xOffset = 0;
//    int yOffset = 0;
//
//    Graphics2D gc = (Graphics2D) g;
//    Paint p = gc.getPaint();
//    gc.setPaint(Color.black);
//
//    int grid;
//    if (expanded) {
//      grid = 20;
//    } else {
//      grid = 20;
//    }
//
//    for (int r = 0; r < width; r++) {
//      for (int c = 0; c < height; c++) {
//        gc.drawOval((r * grid * 2) + xOffset - 2, (c * grid * 2) + yOffset - 2, 4, 4);
//      }
//    }
//    gc.setPaint(p);
//  }

  public boolean isExpanded() {
    return expanded;
  }

  public void setExpanded(boolean expanded) {
    this.expanded = expanded;
    this.repaint();
  }
}
