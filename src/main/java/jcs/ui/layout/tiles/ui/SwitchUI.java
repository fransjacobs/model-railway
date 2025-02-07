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
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import static jcs.entities.AccessoryBean.AccessoryValue.GREEN;
import static jcs.entities.AccessoryBean.AccessoryValue.RED;
import jcs.entities.TileBean;
import jcs.ui.layout.tiles.Tile;

public class SwitchUI extends TileUI {

  public SwitchUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new SwitchUI();
  }

  protected void renderStraight(Graphics2D g2, Color color, JComponent c) {
    int xx = 0;
    int yy = 170;
    int w = RENDER_WIDTH;
    int h = 60;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);

    g2.fillRect(xx, yy, w, h);
  }

  protected void renderDiagonal(Graphics2D g2, Color color, JComponent c) {
    Tile tile = (Tile) c;
    TileBean.Direction direction = tile.getDirection();

    int[] xPoints, yPoints;
    if (TileBean.Direction.RIGHT.equals(direction)) {
      xPoints = new int[]{400, 400, 170, 230};
      yPoints = new int[]{170, 230, 0, 0};
    } else {
      xPoints = new int[]{400, 400, 170, 230};
      yPoints = new int[]{230, 170, 400, 400};
    }

    g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  protected void renderRouteStraight(Graphics2D g2, Color color, JComponent c) {
    int xx = 0;
    int yy = 190;
    int w = RENDER_WIDTH;
    int h = 20;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);

    g2.fillRect(xx, yy, w, h);
  }

  protected void renderRouteDiagonal(Graphics2D g2, Color color, JComponent c) {
    Tile tile = (Tile) c;
    TileBean.Direction direction = tile.getDirection();

    int[] xPoints, yPoints;
    if (TileBean.Direction.RIGHT.equals(direction)) {
      xPoints = new int[]{400, 400, 190, 210};
      yPoints = new int[]{190, 210, 0, 0};
    } else {
      xPoints = new int[]{400, 400, 190, 210};
      yPoints = new int[]{210, 190, 400, 400};
    }

    g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);

    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  @Override
  public void renderTile(Graphics2D g2, JComponent c) {
    Tile tile = (Tile) c;
    AccessoryValue accessoryValue = tile.getAccessoryValue();
    //Color trackColor = tile.getTrackColor();

    if (accessoryValue == null) {
      accessoryValue = AccessoryBean.AccessoryValue.OFF;
    }

    switch (accessoryValue) {
      case RED -> {
        renderStraight(g2, trackColor, c);
        renderDiagonal(g2, Color.red, c);
      }
      case GREEN -> {
        renderDiagonal(g2, trackColor, c);
        renderStraight(g2, Color.green, c);
      }
      default -> {
        renderStraight(g2, trackColor, c);
        renderDiagonal(g2, trackColor, c);
      }
    }
  }

  @Override
  public void renderTileRoute(Graphics2D g2, JComponent c) {
    Tile tile = (Tile) c;
    AccessoryValue routeValue = tile.getRouteValue();
    //Color trackRouteColor = tile.getTrackRouteColor();

    if (routeValue == null) {
      routeValue = AccessoryBean.AccessoryValue.OFF;
    }
    switch (routeValue) {
      case RED -> {
        renderRouteDiagonal(g2, trackRouteColor, c);
      }
      case GREEN -> {
        renderRouteStraight(g2, trackRouteColor, c);
      }
      default -> {
      }
    }
  }
}
