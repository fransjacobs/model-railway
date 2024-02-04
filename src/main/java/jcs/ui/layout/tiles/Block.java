/*
 * Copyright 2024 Frans Jacobs.
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import static jcs.ui.layout.tiles.AbstractTile.drawRotate;
import static jcs.ui.layout.tiles.Tile.DEFAULT_HEIGHT;
import static jcs.ui.layout.tiles.Tile.DEFAULT_WIDTH;
import static jcs.ui.layout.tiles.Tile.RENDER_GRID;
import static jcs.ui.layout.tiles.Tile.RENDER_HEIGHT;
import static jcs.ui.layout.tiles.Tile.RENDER_WIDTH;

public class Block extends AbstractTile implements Tile {

  public static final int BLOCK_WIDTH = DEFAULT_WIDTH * 3;
  public static final int BLOCK_HEIGHT = DEFAULT_HEIGHT * 3;

  Block(TileBean tileBean) {
    super(tileBean);
    if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
      this.width = BLOCK_WIDTH;
      this.renderWidth = RENDER_WIDTH * 3;
      this.height = DEFAULT_HEIGHT;
      this.renderHeight = RENDER_HEIGHT;
    } else {
      this.width = DEFAULT_WIDTH;
      this.renderWidth = RENDER_WIDTH;
      this.height = BLOCK_HEIGHT;
      this.renderHeight = RENDER_HEIGHT * 3;
    }
    this.blockBean = tileBean.getBlockBean();
  }

  Block(Orientation orientation, Point center) {
    this(orientation, center.x, center.y);
  }

  Block(Orientation orientation, int x, int y) {
    super(orientation, Direction.CENTER, x, y);

    if (Orientation.EAST == getOrientation() || Orientation.WEST == getOrientation()) {
      this.width = BLOCK_WIDTH;
      this.renderWidth = RENDER_WIDTH * 3;
      this.height = DEFAULT_HEIGHT;
      this.renderHeight = RENDER_HEIGHT;
    } else {
      this.width = DEFAULT_WIDTH;
      this.renderWidth = RENDER_WIDTH;
      this.height = BLOCK_HEIGHT;
      this.renderHeight = RENDER_HEIGHT * 3;
    }
    this.type = TileType.BLOCK.getTileType();
  }

  @Override
  public Set<Point> getAltPoints() {
    int xx = this.x;
    int yy = this.y;
    Set<Point> alternatives = new HashSet<>();

    if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
      // West
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
  public Set<Point> getAllPoints() {
    Set<Point> aps = new HashSet<>();
    aps.add(getCenter());
    aps.addAll(getAltPoints());
    return aps;
  }

  public Point getAltPoint(String suffix) {
    int cx = this.getCenterX();
    int cy = this.getCenterY();
    if ("+".equals(suffix)) {
      return switch (this.getOrientation()) {
        case WEST ->
          new Point(cx - Tile.GRID * 2, cy);
        case NORTH ->
          new Point(cx, cy - Tile.GRID * 2);
        case SOUTH ->
          new Point(cx, cy + Tile.GRID * 2);
        default ->
          new Point(cx + Tile.GRID * 2, cy);
      };
    } else {
      return switch (this.getOrientation()) {
        case EAST ->
          new Point(cx - Tile.GRID * 2, cy);
        case SOUTH ->
          new Point(cx, cy - Tile.GRID * 2);
        case NORTH ->
          new Point(cx, cy + Tile.GRID * 2);
        default ->
          new Point(cx + Tile.GRID * 2, cy);
      };
    }
  }

  @Override
  public boolean isBlock() {
    return true;
  }

  @Override
  public Map<Orientation, Point> getNeighborPoints() {
    Map<Orientation, Point> neighbors = new HashMap<>();
    Orientation orientation = this.getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    // Horizontal
    if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
      neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 4, cy));
      neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 4, cy));
    } else {
      // Vertical
      neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 4));
      neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 4));
    }
    return neighbors;
  }

  @Override
  public Map<Orientation, Point> getEdgePoints() {
    Map<Orientation, Point> edgeConnections = new HashMap<>();
    Orientation orientation = this.getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    // Horizontal
    if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
      edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID * 3, cy));
      edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID * 3, cy));
    } else {
      // Vertical
      edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 3));
      edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 3));
    }
    return edgeConnections;
  }

  public Point getNeighborPoint(String suffix) {
    int cx = this.getCenterX();
    int cy = this.getCenterY();
    if ("+".equals(suffix)) {
      return switch (this.getOrientation()) {
        case WEST ->
          new Point(cx - Tile.GRID * 4, cy);
        case NORTH ->
          new Point(cx, cy - Tile.GRID * 4);
        case SOUTH ->
          new Point(cx, cy + Tile.GRID * 4);
        default ->
          new Point(cx + Tile.GRID * 4, cy);
      };
    } else {
      return switch (this.getOrientation()) {
        case EAST ->
          new Point(cx - Tile.GRID * 4, cy);
        case SOUTH ->
          new Point(cx, cy - Tile.GRID * 4);
        case NORTH ->
          new Point(cx, cy + Tile.GRID * 4);
        default ->
          new Point(cx + Tile.GRID * 4, cy);
      };
    }
  }

  public Orientation getTravelDirection(String suffix) {
    if ("+".equals(suffix)) {
      return this.getOrientation();
    } else {
      return switch (this.getOrientation()) {
        case EAST ->
          Orientation.WEST;
        case SOUTH ->
          Orientation.NORTH;
        case NORTH ->
          Orientation.SOUTH;
        default ->
          Orientation.EAST;
      };
    }
  }

  @Override
  public String getIdSuffix(Tile other) {
    String suffix = null;
    Orientation match = null;
    if (isAdjacent(other)) {
      Map<Orientation, Point> blockSides = this.getEdgePoints();
      Map<Orientation, Point> otherSides = other.getEdgePoints();

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
    int xx = 20;
    int yy = 100;
    int rw = RENDER_WIDTH * 3 - 40;
    int rh = 200;

    g2.setPaint(Color.black);
    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
    g2.drawRoundRect(xx, yy, rw, rh, 15, 15);

    // A block has a direction of travel. Hence it has a plus (+) and a Minus(-) side.
    // The default the EAST direction, the block will look like: -[ - bk-nn + ]-
    g2.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
    g2.setPaint(Color.black);

    // a - at the start and end of the block
    g2.drawLine(xx + 40, yy + 100, xx + 100, yy + 100);
    g2.drawLine(rw - 80, yy + 100, rw - 20, yy + 100);
    // a | at the end of the block
    g2.drawLine(rw - 50, yy + 70, rw - 50, yy + 130);

    drawName(g2);
  }

  public String getBlockText() {
    String blockText;

    if (!drawOutline && getBlockBean() != null && getBlockBean().getDescription() != null) {
      boolean locInBlock = false;
      if (getBlockBean().getLocomotive() != null && getBlockBean().getLocomotive().getName() != null) {
        blockText = getBlockBean().getLocomotive().getName();
        locInBlock = true;
      } else {
        if (getBlockBean().getDescription().length() > 0) {
          blockText = getBlockBean().getDescription();
        } else {
          blockText = getId();
        }
      }

      boolean reverseArrival = this.getBlockBean().isReverseArrival();

      String direction;
      if (locInBlock) {
        direction = (LocomotiveBean.Direction.FORWARDS == this.getBlockBean().getLocomotive().getDirection() ? ">" : "<");
      } else {
        direction = "";
      }

      // Depending on the arrival direction the direction is to the - or +
      // The default is always the + direction
      switch (getOrientation()) {
        case EAST -> {
          if (reverseArrival) {
            blockText = direction + blockText;
          } else {
            blockText = blockText + direction;
          }
        }
        case WEST -> {
          if (reverseArrival) {
            blockText = blockText + direction;
          } else {
            blockText = direction + blockText;
          }
        }
        case SOUTH -> {
          if (reverseArrival) {
            blockText = blockText + direction;
          } else {
            blockText = direction + blockText;
          }
        }
        case NORTH -> {
          if (reverseArrival) {
            blockText = "<" + blockText;
          } else {
            blockText = blockText + ">";
          }
        }
      }
    } else {
      // Design mode show description when available
      if (getBlockBean() != null && getBlockBean().getDescription() != null && getBlockBean().getDescription().length() > 0) {
        blockText = getBlockBean().getDescription();
      } else {
        blockText = getId();
      }
    }

    return blockText;
  }

  public String getLocomotiveBlockSuffix() {
    String blockSuffix = "";
    if (getBlockBean().getLocomotive() != null && getBlockBean().getLocomotive().getDirection() != null) {
      LocomotiveBean.Direction locDir = getBlockBean().getLocomotive().getDirection();
      boolean reverseArrival = this.getBlockBean().isReverseArrival();

      switch (getOrientation()) {
        case EAST -> {
          if (LocomotiveBean.Direction.FORWARDS == locDir) {
            blockSuffix = reverseArrival ? "-" : "+";
          } else {
            blockSuffix = reverseArrival ? "+" : "-";
          }
        }
        case WEST -> {
          if (LocomotiveBean.Direction.FORWARDS == locDir) {
            blockSuffix = reverseArrival ? "+" : "-";
          } else {
            blockSuffix = reverseArrival ? "-" : "+";
          }
        }
        case SOUTH -> {
          if (LocomotiveBean.Direction.FORWARDS == locDir) {
            blockSuffix = reverseArrival ? "+" : "-";
          } else {
            blockSuffix = reverseArrival ? "-" : "+";
          }
        }
        case NORTH -> {
          if (LocomotiveBean.Direction.FORWARDS == locDir) {
            blockSuffix = reverseArrival ? "-" : "+";
          } else {
            blockSuffix = reverseArrival ? "+" : "-";
          }
        }
      }
    }
    return blockSuffix;
  }

  @Override
  public void drawName(Graphics2D g2d) {
    g2d.setPaint(Color.darkGray);

    Font currentFont = g2d.getFont();
    Font newFont = currentFont.deriveFont(currentFont.getSize() * 10.0F);
    g2d.setFont(newFont);

    String blockText = getBlockText();
    // Scale the text if necessary
    int textWidth = g2d.getFontMetrics().stringWidth(blockText);
    double fontscale = 10.0;
    if (textWidth > 845) {
      fontscale = fontscale * 847.0 / textWidth;
      newFont = currentFont.deriveFont(currentFont.getSize() * (float) fontscale);
      g2d.setFont(newFont);
      textWidth = g2d.getFontMetrics().stringWidth(blockText);
    }

    int textHeight = g2d.getFontMetrics().getHeight();

    switch (getOrientation()) {
      case EAST -> {
        drawRotate(g2d, ((RENDER_WIDTH * 3) / 2) - textWidth / 2, RENDER_GRID + textHeight / 3, 0, blockText);
      }
      case WEST -> {
        drawRotate(
                g2d,
                ((RENDER_WIDTH * 3) / 2) + textWidth / 2,
                RENDER_GRID - textHeight / 3,
                180,
                blockText);
      }
      case NORTH -> {
        drawRotate(
                g2d,
                ((RENDER_WIDTH * 3) / 2) - textWidth / 2,
                RENDER_GRID + textHeight / 3,
                0,
                blockText);
      }
      case SOUTH -> {
        drawRotate(
                g2d,
                ((RENDER_WIDTH * 3) / 2) + textWidth / 2,
                RENDER_GRID - textHeight / 3,
                180,
                blockText);
      }
    }

    // reset to the original font
    newFont = currentFont.deriveFont(currentFont.getSize() * 1.0F);
    g2d.setFont(newFont);
  }

  @Override
  public String getImageKey() {
    StringBuilder sb = getImageKeyBuilder();
    sb.append(getBlockText());

    return sb.toString();
  }
}
