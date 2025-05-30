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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import javax.swing.JPanel;
import org.tinylog.Logger;

public class UnscaledBlockCanvas extends JPanel {

  private boolean expanded;

  public UnscaledBlockCanvas() {
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

  @Override
  protected void paintComponent(Graphics g) {
    long started = System.currentTimeMillis();
    super.paintComponent(g);

    paintGrid(g);
    long now = System.currentTimeMillis();
    Logger.trace("Duration: " + (now - started) + " ms.");
  }

  private void paintGrid(Graphics g) {
    int width = this.getWidth();
    int height = this.getHeight();

    int xOffset = 0;
    int yOffset = 0;

    //Logger.trace("W: " + width + " H: " + height + " X: " + this.getX() + " Y: " + this.getY());
    Graphics2D gc = (Graphics2D) g;
    Paint p = gc.getPaint();
    gc.setPaint(Color.black);

    int grid;
    if (expanded) {
      grid = 20;
    } else {
      grid = 20;
    }

    for (int r = 0; r < width; r++) {
      for (int c = 0; c < height; c++) {
        gc.drawOval((r * grid * 2) + xOffset - 2, (c * grid * 2) + yOffset - 2, 4, 4);
      }
    }
    gc.setPaint(p);
  }

  public boolean isExpanded() {
    return expanded;
  }

  public void setExpanded(boolean expanded) {
    this.expanded = expanded;
    this.repaint();
  }
}
