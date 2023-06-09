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
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;
import jcs.ui.layout.tiles.enums.Direction;
import org.tinylog.Logger;

public class Block extends AbstractTile implements Tile {

  private static int idSeq;

  public static final int BLOCK_WIDTH = DEFAULT_WIDTH * 3;
  public static final int BLOCK_HEIGHT = DEFAULT_HEIGHT * 3;

  public Block(TileBean tileBean) {
    super(tileBean);
    if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
      this.width = BLOCK_WIDTH;
      this.height = DEFAULT_HEIGHT;
    } else {
      this.width = DEFAULT_WIDTH;
      this.height = BLOCK_HEIGHT;
    }
  }

  public Block(int x, int y) {
    this(Orientation.EAST, x, y);
  }

  public Block(Orientation orientation, int x, int y) {
    this(orientation, new Point(x, y));
  }

  public Block(Orientation orientation, Point center) {
    super(orientation, Direction.CENTER, center.x, center.y);
    if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
      this.width = DEFAULT_WIDTH * 3;
      this.height = DEFAULT_HEIGHT;
    } else {
      this.width = DEFAULT_WIDTH;
      this.height = DEFAULT_HEIGHT * 3;
    }
    this.type = TileType.BLOCK.getTileType();
  }

  @Override
  protected final String getNewId() {
    idSeq++;
    return "bk-" + idSeq;
  }

  @Override
  protected void setIdSeq(int id) {
    idSeq = id;
  }

  @Override
  public Set<Point> getAltPoints() {
    int xx = this.x;
    int yy = this.y;
    Set<Point> alternatives = new HashSet<>();

    if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
      //West
      Point wp = new Point((xx - DEFAULT_WIDTH), yy);
      Point ep = new Point((xx + DEFAULT_WIDTH), yy);
      alternatives.add(wp);
      alternatives.add(ep);
    } else {
      Point np = new Point(xx, (yy - DEFAULT_HEIGHT));
      Point sp = new Point(xx, (yy + DEFAULT_HEIGHT));
      alternatives.add(np);
      alternatives.add(sp);
    }

    return alternatives;
  }

  @Override
  public boolean isBlock() {
    return true;
  }

  @Override
  public String getIdSuffix(Tile other) {
    String suffix = null;
    Orientation match = null;
    if (canTraverseTo(other)) {
      Map<Orientation, Point> blockSides = this.getEdgeConnections();
      Map<Orientation, Point> otherSides = other.getEdgeConnections();

      for (Orientation bo : Orientation.values()) {
        Point bp = blockSides.get(bo);

        if (bp != null) {
          for (Orientation oo : Orientation.values()) {
            Point op = otherSides.get(oo);
            if (op != null) {
              if (op.equals(bp)) {
                match = bo;
                break;
              }
            }
          }
        }
      }
    }

    if (match != null) {
      if (Orientation.EAST == this.getOrientation() && Orientation.EAST == match) {
        suffix = "+";
      }
      if (Orientation.WEST == this.getOrientation() && Orientation.WEST == match) {
        suffix = "+";
      }
      if (Orientation.EAST == this.getOrientation() && Orientation.WEST == match) {
        suffix = "-";
      }
      if (Orientation.WEST == this.getOrientation() && Orientation.EAST == match) {
        suffix = "-";
      }
      if (Orientation.NORTH == this.getOrientation() && Orientation.NORTH == match) {
        suffix = "+";
      }
      if (Orientation.NORTH == this.getOrientation() && Orientation.SOUTH == match) {
        suffix = "-";
      }
      if (Orientation.SOUTH == this.getOrientation() && Orientation.SOUTH == match) {
        suffix = "+";
      }
      if (Orientation.SOUTH == this.getOrientation() && Orientation.NORTH == match) {
        suffix = "-";
      }
    }
    return suffix;
  }

  @Override
  public void rotate() {
    super.rotate();
    if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
      this.width = DEFAULT_WIDTH * 3;
      this.height = DEFAULT_HEIGHT;
    } else {
      this.width = DEFAULT_WIDTH;
      this.height = DEFAULT_HEIGHT * 3;
    }
  }

  @Override
  public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
    int xx, yy, w, h;

    xx = 2;
    yy = 10;
    w = DEFAULT_WIDTH * 3 - 4;
    h = 20;

    g2.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
    g2.setPaint(Color.darkGray);
    g2.drawRect(xx, yy, w, h);

    //Block needs to have a direction so travel from a to b or from - to +
    //so in east direction the block is -[ - bk-nn + ]- 
    g2.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
    g2.setPaint(Color.black);

    //a - at the start and end of the block
    g2.drawLine(xx + 4, yy + 10, xx + 10, yy + 10);
    g2.drawLine(w - 10, yy + 10, w - 4, yy + 10);
    //a | at the end of the block 
    g2.drawLine(w - 7, yy + 7, w - 7, yy + 13);

    drawName(g2);
  }

  @Override
  public void drawName(Graphics2D g2d) {
    g2d.setPaint(Color.darkGray);
    switch (getOrientation()) {
      case EAST ->
        drawRotate(g2d, 16, GRID + 4, 0, getId());
      case WEST ->
        drawRotate(g2d, 104, GRID - 4, 180, getId());
      case NORTH ->
        drawRotate(g2d, 20, GRID + 4, 0, getId());
      case SOUTH ->
        drawRotate(g2d, 100, GRID - 4, 180, getId());
    }
  }

}
