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

public class CrossingUI extends StraightUI {

  public CrossingUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new CrossingUI();
  }

  protected void renderVerticalAndDividers(Graphics2D g2, JComponent c) {
    Tile tile = (Tile) c;
    //Color trackColor = tile.getTrackColor();

    int xxn = 175;
    int yyn = 0;
    int xxs = 175;
    int yys = 325;
    int w = 50;
    int h = 75;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);

    //North
    g2.fillRect(xxn, yyn, w, h);
    //South
    g2.fillRect(xxs, yys, w, h);

    //Dividers
    int[] xNorthPoly = new int[]{85, 115, 285, 315};
    int[] yNorthPoly = new int[]{85, 125, 125, 85};

    int[] xSouthPoly = new int[]{85, 115, 285, 315};
    int[] ySouthPoly = new int[]{315, 275, 275, 315};

    g2.setPaint(Color.darkGray);
    g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    g2.drawPolyline(xNorthPoly, yNorthPoly, xNorthPoly.length);
    g2.drawPolyline(xSouthPoly, ySouthPoly, xSouthPoly.length);
  }

  protected void renderRouteVertical(Graphics2D g2, JComponent c) {
    Tile tile = (Tile) c;
    //Color trackRouteColor = tile.getTrackRouteColor();

    int xxn, yyn, xxs, yys, w, h;
    xxn = 190;
    yyn = 0;
    xxs = 190;
    yys = 325;
    w = 20;
    h = 75;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackRouteColor);

    //North
    g2.fillRect(xxn, yyn, w, h);
    //South
    g2.fillRect(xxs, yys, w, h);
  }

  @Override
  public void renderTile(Graphics2D g2, JComponent c) {
    renderStraight(g2, c);
    renderVerticalAndDividers(g2, c);
  }

}
