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
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

public class StraightUI extends TileUI {

  public StraightUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new StraightUI();
  }

  protected void renderStraight(Graphics2D g2, JComponent c) {
    int xx = 0;
    int yy = 170;
    int w = RENDER_WIDTH;
    int h = 60;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);

    g2.fillRect(xx, yy, w, h);
  }

  protected void renderRouteStraight(Graphics2D g2, JComponent c) {
    int xx, yy, w, h;
    xx = 0;
    yy = 190;
    w = RENDER_WIDTH;
    h = 20;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackRouteColor);

    g2.fillRect(xx, yy, w, h);
  }

  @Override
  public void renderTileRoute(Graphics2D g2, JComponent c) {
    renderRouteStraight(g2, c);
  }

  @Override
  public void renderTile(Graphics2D g2, JComponent c) {
    renderStraight(g2, c);
  }

}
