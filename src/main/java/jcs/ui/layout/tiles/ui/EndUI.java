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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import jcs.ui.layout.tiles.Tile;

public class EndUI extends TileUI {

  public EndUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new EndUI();
  }

  protected void renderEnd(Graphics2D g2, JComponent c) {
    int xx = 0;
    int yy = 175;
    int w = RENDER_GRID;
    int h = 50;

    g2.setStroke(new BasicStroke(40, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillRect(xx, yy, w, h);

    xx = RENDER_GRID;
    yy = 100;

    w = 30;
    h = 200;

    g2.setPaint(Color.DARK_GRAY);
    g2.fillRect(xx, yy, w, h);
  }

  @Override
  public void renderTile(Graphics2D g2, JComponent c) {
    renderEnd(g2, c);
  }

  @Override
  public void renderTileRoute(Graphics2D g2d, JComponent c) {
  }

}
