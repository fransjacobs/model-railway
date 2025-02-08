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
import jcs.ui.layout.tiles.Tile;

public class CurvedUI extends TileUI {

  public CurvedUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new CurvedUI();
  }

  @Override
  public void renderTile(Graphics2D g2, JComponent c) {
    int[] xPoints = new int[]{400, 400, 170, 230};
    int[] yPoints = new int[]{230, 170, 400, 400};

    g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);

    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  @Override
  public void renderTileRoute(Graphics2D g2, JComponent c) {
    int[] xPoints = new int[]{400, 400, 190, 210};
    int[] yPoints = new int[]{210, 190, 400, 400};

    g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackRouteColor);

    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

}
