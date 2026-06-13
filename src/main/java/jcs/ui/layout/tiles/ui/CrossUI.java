/*
 * Copyright 2026 Frans Jacobs.
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
import java.awt.geom.Ellipse2D;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileModel;

public class CrossUI extends TileUI {

  public CrossUI() {
    super();
  }

  public static ComponentUI createUI(JComponent c) {
    return new CrossUI();
  }

  protected void renderEastWest(Graphics2D g2, JComponent c) {
    int[] xPoints = new int[]{165, 235, 635, 565};
    int[] yPoints = new int[]{400, 400, 0, 0};

    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  protected void renderRouteEastWest(Graphics2D g2, JComponent c) {
    int[] xPoints = new int[]{185, 215, 615, 585};
    int[] yPoints = new int[]{400, 400, 0, 0};

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackRouteColor);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  protected void renderNorthSouth(Graphics2D g2, JComponent c) {
    int[] xPoints = new int[]{165, 235, 635, 565};
    int[] yPoints = new int[]{0, 0, 400, 400};

    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  protected void renderRouteNorthSouth(Graphics2D g2, JComponent c) {
    int[] xPoints = new int[]{185, 215, 615, 585};
    int[] yPoints = new int[]{0, 0, 400, 400};

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackRouteColor);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  @Override
  public void renderTile(Graphics2D g2, JComponent c) {
    renderEastWest(g2, c);
    renderNorthSouth(g2, c);
  }

  @Override
  public void renderTileRoute(Graphics2D g2, JComponent c) {
    Tile tile = (Tile) c;
    Orientation incomingSide = tile.getIncomingSide();
    switch (incomingSide) {
      case NORTH -> {
        if (tile.isHorizontal()) {
          renderRouteNorthSouth(g2, c);
        } else {
          renderRouteEastWest(g2, c);
        }
      }
      case WEST -> {
        if (tile.isHorizontal()) {
          renderRouteEastWest(g2, c);
        } else {
          renderRouteNorthSouth(g2, c);
        }
      }
      case SOUTH -> {
        if (tile.isHorizontal()) {
          renderRouteNorthSouth(g2, c);
        } else {
          renderRouteEastWest(g2, c);
        }
      }
      default -> {
        //EAST
        if (tile.isHorizontal()) {
          renderRouteEastWest(g2, c);
        } else {
          renderRouteNorthSouth(g2, c);
        }
      }
    }
  }

  @Override
  protected void drawCenterPoint(Graphics2D g2d, Color color, double size, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();
    TileBean.Orientation tileOrientation = model.getTileOrienation();

    //A Cross has 1 alternate point
    //1st square holds the centerpoint
    //2nd square 
    double dX1, dX2, dY1, dY2;
    switch (tileOrientation) {
      case SOUTH -> {
        dX1 = renderWidth / 2 - size / 2;
        dY1 = renderHeight / 2 - renderHeight / 4 - size / 2;
        dX2 = renderWidth / 2 + renderWidth - size / 4;
        dY2 = renderHeight / 2 - renderHeight / 4 - size / 4;
      }
      case WEST -> {
        dX1 = renderWidth / 2 - renderWidth / 4 - size / 2;
        dY1 = renderHeight / 2 - size / 2;
        dX2 = renderWidth / 2 + renderWidth / 4 - size / 4;
        dY2 = renderHeight / 2 - size / 4;
      }
      case NORTH -> {
        dX1 = renderWidth / 2 - size / 2;
        dY1 = renderHeight / 2 - renderHeight / 4 - size / 2;
        dX2 = renderWidth / 2 + renderWidth - size / 4;
        dY2 = renderHeight / 2 - renderHeight / 4 - size / 4;
      }
      default -> {
        //East
        dX1 = renderWidth / 2 - renderWidth / 4 - size / 2;
        dY1 = renderHeight / 2 - size / 2;
        dX2 = renderWidth / 2 + renderWidth / 4 - size / 4;
        dY2 = renderHeight / 2 - size / 4;
      }
    }

    g2d.setColor(color);
    g2d.fill(new Ellipse2D.Double(dX1, dY1, size, size));
    g2d.fill(new Ellipse2D.Double(dX2, dY2, size / 2, size / 2));
  }

}
