/*
 * Copyright 2025 Frans Jacobs.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import javax.swing.JPanel;
import jcs.ui.layout.tiles.Tile;
import org.tinylog.Logger;


//  jcs.ui.layout.tiles.DotGridCanvas

public class DotGridCanvas extends JPanel {

  private boolean expanded;

  public DotGridCanvas() {
    super(null, false);
    setOpaque(true);
    //setBackground(Color.white);
  }

  @Override
  public void paint(Graphics g) {
    long started = System.currentTimeMillis();
    super.paint(g);

    paintDotGrid(g);
    long now = System.currentTimeMillis();
    Logger.trace("Duration: " + (now - started) + " ms.");
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
    int width = this.getWidth();
    int height = this.getHeight();

    int grid;
    if (expanded) {
      grid = 20;
    } else {
      grid = 20;
    }

    Graphics2D gc = (Graphics2D) g;
    Paint p = gc.getPaint();
    gc.setPaint(Color.black);

    for (int r = 0; r < width; r++) {
      for (int c = 0; c < height; c++) {
        gc.drawOval((r * grid * 2) - 2, (c * grid * 2) - 2, 4, 4);
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
