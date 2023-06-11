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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jcs.entities.TileBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import static jcs.entities.enums.Orientation.NORTH;
import static jcs.entities.enums.Orientation.SOUTH;
import static jcs.entities.enums.Orientation.WEST;
import jcs.entities.enums.TileType;
import static jcs.entities.enums.TileType.BLOCK;
import static jcs.entities.enums.TileType.CROSS;
import static jcs.entities.enums.TileType.END;
import static jcs.entities.enums.TileType.SWITCH;
import jcs.ui.layout.tiles.enums.Direction;

public class Cross extends Switch implements Tile {

  private static int idSeq;

  public static final int CROSS_WIDTH = DEFAULT_WIDTH * 2;
  public static final int CROSS_HEIGHT = DEFAULT_HEIGHT * 2;
  public static final int CROSS_OFFSET = GRID;

  public Cross(TileBean tileBean) {
    super(tileBean);
    if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
      this.width = CROSS_WIDTH;
      this.height = DEFAULT_HEIGHT;
      this.offsetX = Tile.GRID;
    } else {
      this.width = DEFAULT_WIDTH;
      this.height = CROSS_HEIGHT;
      this.offsetY = Tile.GRID;
    }
  }

  public Cross(Orientation orientation, Direction direction, int x, int y) {
    this(orientation, direction, new Point(x, y));
  }

  public Cross(Orientation orientation, Direction direction, Point center) {
    super(orientation, direction, center);
    this.type = TileType.CROSS.getTileType();
    // As a cross is asymetrical an offset is necessary
    switch (orientation) {
      case SOUTH -> {
        this.offsetX = 0;
        this.offsetY = CROSS_OFFSET;
        this.width = DEFAULT_WIDTH;
        this.height = CROSS_HEIGHT;
      }
      case WEST -> {
        this.offsetX = -CROSS_OFFSET;
        this.offsetY = 0;
        this.width = CROSS_WIDTH;
        this.height = DEFAULT_HEIGHT;
      }
      case NORTH -> {
        this.offsetX = 0;
        this.offsetY = -CROSS_OFFSET;
        this.width = DEFAULT_WIDTH;
        this.height = CROSS_HEIGHT;
      }
      default -> {
        //East so default
        this.offsetX = +CROSS_OFFSET;
        this.offsetY = 0;
        this.width = CROSS_WIDTH;
        this.height = DEFAULT_HEIGHT;
      }
    }
  }

  @Override
  protected String getNewId() {
    idSeq++;
    return "cs-" + idSeq;
  }

  @Override
  protected void setIdSeq(int id) {
    idSeq = id;
  }

  @Override
  public Map<Orientation, Point> getNeighborPoints() {
    Map<Orientation, Point> neighbors = new HashMap<>();
    TileType tiletype = this.getTileType();
    Orientation orientation = this.getOrientation();
    Direction direction = this.getDirection();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    //TODO
    return neighbors;
  }

  @Override
  public Set<Point> getAltPoints() {
    int xx = this.x;
    int yy = this.y;
    Set<Point> alternatives = new HashSet<>();

    switch (getOrientation()) {
      case SOUTH -> {
        Point sp = new Point(xx, (yy + DEFAULT_HEIGHT));
        alternatives.add(sp);
      }
      case WEST -> {
        Point wp = new Point((xx - DEFAULT_WIDTH), yy);
        alternatives.add(wp);
      }
      case NORTH -> {
        Point np = new Point(xx, (yy - DEFAULT_HEIGHT));
        alternatives.add(np);
      }
      default -> {
        //East so default 
        Point ep = new Point((x + DEFAULT_WIDTH), yy);
        alternatives.add(ep);
      }
    }
    return alternatives;
  }

  @Override
  public boolean isJunction() {
    return true;
  }

  @Override
  public void rotate() {
    super.rotate();

    //Due to the asymetical shape (center is on the left)
    //the offset has to be changedr the rotation
    switch (getOrientation()) {
      case SOUTH -> {
        this.offsetX = 0;
        this.offsetY = CROSS_OFFSET;
        this.width = DEFAULT_WIDTH;
        this.height = CROSS_HEIGHT;
      }
      case WEST -> {
        this.offsetX = -CROSS_OFFSET;
        this.offsetY = 0;
        this.width = CROSS_WIDTH;
        this.height = DEFAULT_HEIGHT;
      }
      case NORTH -> {
        this.offsetX = 0;
        this.offsetY = -CROSS_OFFSET;
        this.width = DEFAULT_WIDTH;
        this.height = CROSS_HEIGHT;
      }
      default -> {
        //East so default 
        this.offsetX = +CROSS_OFFSET;
        this.offsetY = 0;
        this.width = CROSS_WIDTH;
        this.height = DEFAULT_HEIGHT;
      }
    }
  }

  @Override
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

  protected void renderStraight2(Graphics2D g2, Color trackColor, Color backgroundColor) {
    int xx, yy, w, h;
    xx = DEFAULT_WIDTH;
    yy = 17;
    w = DEFAULT_WIDTH;
    h = 6;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillRect(xx, yy, w, h);
  }

  @Override
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

  protected void renderDiagonal2(Graphics2D g2, Color trackColor, Color backgroundColor) {
    int[] xPoints, yPoints;
    if (Direction.RIGHT.equals(getDirection())) {
      xPoints = new int[]{40, 40, 56, 64};
      yPoints = new int[]{16, 24, 40, 40};
    } else {
      xPoints = new int[]{40, 40, 56, 64};
      yPoints = new int[]{24, 16, 0, 0};
    }

    g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  @Override
  public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
    if (accessoryValue == null) {
      this.accessoryValue = AccessoryValue.OFF;
    }

    switch (this.accessoryValue) {
      case RED -> {
        renderStraight2(g2, trackColor, backgroundColor);
        renderDiagonal(g2, trackColor, backgroundColor);

        renderStraight(g2, Color.red, backgroundColor);
        renderDiagonal2(g2, Color.red, backgroundColor);
      }
      case GREEN -> {
        renderDiagonal(g2, trackColor, backgroundColor);
        renderDiagonal2(g2, trackColor, backgroundColor);
        renderStraight(g2, Color.green, backgroundColor);
        renderStraight2(g2, Color.green, backgroundColor);
      }
      default -> {
        renderStraight(g2, trackColor, backgroundColor);
        renderStraight2(g2, trackColor, backgroundColor);
        renderDiagonal(g2, trackColor, backgroundColor);
        renderDiagonal2(g2, trackColor, backgroundColor);
      }
    }
  }

}
