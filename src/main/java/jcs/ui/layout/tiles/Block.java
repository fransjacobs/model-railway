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
import java.awt.Image;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jcs.entities.BlockBean.BlockState;
import static jcs.entities.BlockBean.BlockState.ARRIVING;
import static jcs.entities.BlockBean.BlockState.DEPARTING;
import static jcs.entities.BlockBean.BlockState.LOCKED;
import static jcs.entities.BlockBean.BlockState.OCCUPIED;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.ui.layout.events.TileEvent;
import static jcs.ui.layout.tiles.AbstractTile.drawRotate;
import static jcs.ui.layout.tiles.Tile.DEFAULT_HEIGHT;
import static jcs.ui.layout.tiles.Tile.DEFAULT_WIDTH;
import static jcs.ui.layout.tiles.Tile.RENDER_GRID;
import static jcs.ui.layout.tiles.Tile.RENDER_HEIGHT;
import static jcs.ui.layout.tiles.Tile.RENDER_WIDTH;
import jcs.ui.util.ImageUtil;
import org.tinylog.Logger;

public class Block extends AbstractTile implements Tile {

  public static final int BLOCK_WIDTH = DEFAULT_WIDTH * 3;
  public static final int BLOCK_HEIGHT = DEFAULT_HEIGHT * 3;

  protected BlockState routeBlockState;

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
    this.type = tileBean.getType();
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
    int cx = getCenterX();
    int cy = getCenterY();
    if ("+".equals(suffix)) {
      return switch (getOrientation()) {
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
      return switch (getOrientation()) {
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
      return getOrientation();
    } else {
      return switch (getOrientation()) {
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
      if (Orientation.EAST == getOrientation() && Orientation.EAST == match) {
        suffix = "+";
      }
      if (Orientation.WEST == getOrientation() && Orientation.WEST == match) {
        suffix = "+";
      }
      if (Orientation.EAST == getOrientation() && Orientation.WEST == match) {
        suffix = "-";
      }
      if (Orientation.WEST == getOrientation() && Orientation.EAST == match) {
        suffix = "-";
      }
      if (Orientation.NORTH == getOrientation() && Orientation.NORTH == match) {
        suffix = "+";
      }
      if (Orientation.NORTH == getOrientation() && Orientation.SOUTH == match) {
        suffix = "-";
      }
      if (Orientation.SOUTH == getOrientation() && Orientation.SOUTH == match) {
        suffix = "+";
      }
      if (Orientation.SOUTH == getOrientation() && Orientation.NORTH == match) {
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

      this.renderWidth = RENDER_WIDTH * 3;
      this.renderHeight = RENDER_HEIGHT;
    } else {
      this.width = DEFAULT_WIDTH;
      this.height = DEFAULT_HEIGHT * 3;

      this.renderWidth = RENDER_WIDTH;
      this.renderHeight = RENDER_HEIGHT * 3;
    }
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setRenderWidth(int renderWidth) {
    this.renderWidth = renderWidth;
  }

  public void setRenderHeight(int renderHeight) {
    this.renderHeight = renderHeight;
  }

  public void setRenderOffsetX(int renderOffsetX) {
    this.renderOffsetX = renderOffsetX;
  }

  public void setRenderOffsetY(int renderOffsetY) {
    this.renderOffsetY = renderOffsetY;
  }

  /**
   * Depending on the block status change the background color<br>
   * - Red: Occupied<br>
   * - Green: Departure<br>
   * - Magenta: Arrival / entering<br>
   * - Yellow: reserved<br>
   * - White: all clear / default<br>
   *
   * @return the Colour which belong with the current Block State
   */
  public Color getBlockStateColor() {
    if (blockBean != null) {
      BlockState blockState = blockBean.getBlockState();
      return getBlockStateColor(blockState);
    } else {
      return Color.white;
    }
  }

  public Color getBlockStateColor(BlockState blockState) {
    return switch (blockState) {
      case GHOST ->
        new Color(250, 0, 0);
      case LOCKED ->
        new Color(250, 250, 210);
      case OCCUPIED ->
        new Color(250, 210, 210);
      case LEAVING ->
        new Color(190, 190, 190);
      case DEPARTING ->
        new Color(210, 250, 210);
      case ARRIVING ->
        new Color(250, 210, 250);
      default ->
        new Color(255, 255, 255);
    };
  }

  public void setRouteBlockState(BlockState routeBlockState) {
    this.routeBlockState = routeBlockState;
  }

  public BlockState getRouteBlockState() {
    return routeBlockState;
  }

  public String getBlockState() {
    if (blockBean != null) {
      return blockBean.getBlockState().getState();
    } else {
      return BlockState.FREE.getState();
    }
  }

  @Override
  public void renderTile(Graphics2D g2) {
    int xx = 20;
    int yy = 50;
    int rw = RENDER_WIDTH * 3 - 40;
    int rh = 300;

    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));

    g2.setPaint(Color.darkGray);
    g2.drawRoundRect(xx, yy, rw, rh, 15, 15);

    Color blockStateColor = getBlockStateColor();
    g2.setPaint(blockStateColor);
    g2.fillRoundRect(xx, yy, rw, rh, 15, 15);

    g2.setStroke(new BasicStroke(20, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
    g2.setPaint(Color.darkGray);
    g2.drawLine(rw + 20, yy - 0, rw + 20, yy + 300);

    //When there is a locomotive in the block mark the direction of travel.
    //The default, forwards is in the direction of the block orientation, i.e. the +
    if (getBlockBean() != null && getBlockBean().getLocomotive() != null && getBlockBean().getLocomotive().getName() != null) {
      boolean reverseArrival = getBlockBean().isReverseArrival();
      if (getBlockBean().getDepartureSuffix() == null || "".equals(getBlockBean().getDepartureSuffix())) {
        if ((LocomotiveBean.Direction.FORWARDS == getBlockBean().getLocomotive().getDirection() && !reverseArrival)
                || (LocomotiveBean.Direction.BACKWARDS == getBlockBean().getLocomotive().getDirection() && reverseArrival)) {
          g2.fillPolygon(new int[]{1180, 1130, 1130,}, new int[]{200, 150, 250}, 3);
        } else {
          g2.fillPolygon(new int[]{0, 50, 50,}, new int[]{200, 150, 250}, 3);
        }
      } else {
        //A departure side is set by the dispatcher
        if ("+".equals(getBlockBean().getDepartureSuffix())) {
          g2.fillPolygon(new int[]{1180, 1130, 1130,}, new int[]{200, 150, 250}, 3);
        } else {
          g2.fillPolygon(new int[]{0, 50, 50,}, new int[]{200, 150, 250}, 3);
        }
      }
    }

    drawName(g2);
  }

  @Override
  public void renderTileRoute(Graphics2D g2d) {
    if (routeBlockState != null) {
      backgroundColor = getBlockStateColor(routeBlockState);
    }
  }

  protected void overlayLocImage(Graphics2D g2d) {
    Image locImage = getLocImage();
    String departureSuffix;
    if (getBlockBean() != null) {
      departureSuffix = getBlockBean().getDepartureSuffix();
    } else {
      departureSuffix = "+";
    }

    if (locImage != null) {
      // scale it to max h of 45
      int size = 45;
      float aspect = (float) locImage.getHeight(null) / (float) locImage.getWidth(null);
      //TODO: Use Scalr?
      locImage = locImage.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH);

      int w = locImage.getWidth(null);
      int h = locImage.getHeight(null);
      //Logger.trace("LocImage w: " + w + " h: " + h);

      //Depending on the block orientation the image needs to be rotated and flipped
      switch (getOrientation()) {
        case WEST -> {
          int xx;
          if ("+".equals(departureSuffix)) {
            int minX = x - width / 2 + 10;
            xx = minX;
          } else {
            int maxX = x + width / 2 - 10;
            xx = maxX - w;
          }
          int yy = y - h / 2;

          //locImage = ImageUtil.flipVertically(locImage);

          g2d.drawImage(locImage, xx, yy, null);
        }
        case SOUTH -> {
          locImage = ImageUtil.rotate(locImage, 270);
          //locImage = ImageUtil.flipHorizontally(locImage);

          w = locImage.getWidth(null);
          h = locImage.getHeight(null);

          int xx = x - w / 2;
          int yy;
          if ("+".equals(departureSuffix)) {
            int maxY = y + height / 2 - 10;
            yy = maxY - h;
          } else {
            int minY = y - height / 2 + 10;
            yy = minY;
          }
          g2d.drawImage(locImage, xx, yy, null);
        }
        case NORTH -> {
          locImage = ImageUtil.rotate(locImage, 270);

          w = locImage.getWidth(null);
          h = locImage.getHeight(null);

          int xx = x - w / 2;
          int yy;
          if ("+".equals(departureSuffix)) {
            int minY = y - height / 2 + 10;
            yy = minY;
          } else {
            int maxY = y + height / 2 - 10;
            yy = maxY - h;
          }
          g2d.drawImage(locImage, xx, yy, null);
        }
        default -> {
          //EAST
          int xx;
          if ("+".equals(departureSuffix)) {
            int maxX = x + width / 2 - 10;
            xx = maxX - w;
          } else {
            int minX = x - width / 2 + 10;
            xx = minX;
          }
          int yy = y - h / 2;
          g2d.drawImage(locImage, xx, yy, null);
        }
      }
    }
  }

  /**
   * Overridden to overlay a locomotive Icon
   *
   * @param g2d The graphics handle
   * @param drawOutline
   */
  @Override
  public void drawTile(Graphics2D g2d, boolean drawOutline) {
    super.drawTile(g2d, drawOutline);
    if (getLocImage() != null) {
      overlayLocImage(g2d);
    }
  }

  @Override
  public void onTileChange(TileEvent tileEvent) {
    //TODO: Does not yet work for route block status change
    Color pb = this.backgroundColor;
    super.onTileChange(tileEvent);
    if (!pb.equals(backgroundColor)) {
      repaintTile();
    }
  }

  private Image getLocImage() {
    if (blockBean != null && blockBean.getLocomotive() != null && blockBean.getLocomotive().getLocIcon() != null) {
      //Do not show the image in block state FREE, LEAVING and LOCKED
      BlockState blockState = blockBean.getBlockState();
      boolean showImage = !(BlockState.FREE == blockState || BlockState.LOCKED == blockState || BlockState.LEAVING == blockState || BlockState.GHOST == blockState);
      if (showImage) {
        if (LocomotiveBean.Direction.BACKWARDS == blockBean.getLocomotive().getDirection()) {
          return ImageUtil.flipVertically(blockBean.getLocomotive().getLocIcon());
        } else {
          return blockBean.getLocomotive().getLocIcon();
        }
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public String getBlockText() {
    String blockText;
    if (!drawOutline && getBlockBean() != null && getBlockBean().getDescription() != null) {
      if (blockBean.getLocomotive() != null && blockBean.getLocomotive().getName() != null && BlockState.GHOST != blockBean.getBlockState()) {
        blockText = getBlockBean().getLocomotive().getName();
      } else {
        if (getBlockBean().getDescription().length() > 0) {
          blockText = getBlockBean().getDescription();
        } else {
          blockText = getId();
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

  @Override
  public void drawName(Graphics2D g2d) {
    Image locImage = getLocImage();

    if (locImage == null) {
      g2d.setPaint(Color.black);

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
          drawRotate(g2d, ((RENDER_WIDTH * 3) / 2) + textWidth / 2, RENDER_GRID - textHeight / 3, 180, blockText);
        }
        case NORTH -> {
          drawRotate(g2d, ((RENDER_WIDTH * 3) / 2) - textWidth / 2, RENDER_GRID + textHeight / 3, 0, blockText);
        }
        case SOUTH -> {
          drawRotate(g2d, ((RENDER_WIDTH * 3) / 2) + textWidth / 2, RENDER_GRID - textHeight / 3, 180, blockText);
        }
      }
      // reset to the original font
      newFont = currentFont.deriveFont(currentFont.getSize() * 1.0F);
      g2d.setFont(newFont);
    }
  }
}
