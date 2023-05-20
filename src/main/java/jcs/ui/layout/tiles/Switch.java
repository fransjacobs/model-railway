/*
 * Copyright 2023 Frans Jacobs.
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

import jcs.ui.layout.Tile;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import jcs.controller.cs3.events.AccessoryMessageEvent;
import jcs.entities.TileBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;
import jcs.trackservice.events.AccessoryListener;
import jcs.ui.layout.tiles.enums.Direction;

public class Switch extends AbstractTile implements Tile, AccessoryListener {

  private static int idSeq;

  protected AccessoryValue value;
  protected AccessoryValue routeValue;
  protected Color routeColor;

  public Switch(TileBean tileBean) {
    super(tileBean);
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
  }

  public Switch(Orientation orientation, Direction direction, int x, int y) {
    this(orientation, direction, new Point(x, y));
  }

  public Switch(Orientation orientation, Direction direction, Point center) {
    super(orientation, direction, center.x, center.y);
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
    this.type = TileType.SWITCH.getTileType();
  }

  @Override
  protected String getNewId() {
    idSeq++;
    return "sw-" + idSeq;
  }

  public void setValue(AccessoryValue value) {
    this.value = value;
    this.image = null;
  }

  public void setRouteValue(AccessoryValue value, Color routeColor) {
    this.routeValue = value;
    this.routeColor = routeColor;
    this.image = null;
  }

  protected void renderStraight(Graphics2D g2, Color trackColor, Color backgroundColor) {
    int xx, yy, w, h;
    xx = 0;
    yy = 17;
    w = DEFAULT_WIDTH;
    h = 6;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillRect(xx, yy, w, h);
  }

  protected void renderDiagonal(Graphics2D g2, Color trackColor, Color backgroundColor) {
    int[] xPoints, yPoints;
    if (Direction.RIGHT.equals(getDirection())) {
      xPoints = new int[]{40, 40, 16, 24};
      yPoints = new int[]{16, 24, 0, 0};
    } else {
      xPoints = new int[]{40, 40, 16, 24};
      yPoints = new int[]{24, 16, 40, 40};
    }

    g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  @Override
  public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
    if (value == null) {
      this.value = AccessoryValue.OFF;
    }
    if (routeValue == null) {
      this.routeValue = AccessoryValue.OFF;
    }

    if (this.routeColor == null) {
      this.routeColor = trackColor;
    }

    switch (this.value) {
      case RED -> {
        renderStraight(g2, trackColor, backgroundColor);
        renderDiagonal(g2, Color.red, backgroundColor);
      }
      case GREEN -> {
        renderDiagonal(g2, trackColor, backgroundColor);
        renderStraight(g2, Color.green, backgroundColor);
      }
      default -> {
        switch (this.routeValue) {
          case RED -> {
            renderStraight(g2, trackColor, backgroundColor);
            renderDiagonal(g2, this.routeColor, backgroundColor);
          }
          case GREEN -> {
            renderDiagonal(g2, trackColor, backgroundColor);
            renderStraight(g2, this.routeColor, backgroundColor);
          }
          default -> {
            renderStraight(g2, trackColor, backgroundColor);
            renderDiagonal(g2, trackColor, backgroundColor);
          }
        }
      }
    }
  }

  @Override
  protected void setIdSeq(int id) {
    idSeq = id;
  }

  @Override
  public void onChange(AccessoryMessageEvent event) {
    if (this.getAccessoryBean() != null && this.getAccessoryId().equals(event.getAccessoryBean().getId())) {
      setValue(event.getAccessoryBean().getAccessoryValue());
      repaintTile();
    }
  }

}
