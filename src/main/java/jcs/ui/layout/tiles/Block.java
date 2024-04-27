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
import jcs.commandStation.events.BlockEvent;
import jcs.commandStation.events.BlockEventListener;
import jcs.entities.BlockBean;
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
import static jcs.ui.layout.tiles.AbstractTile.drawRotate;
import static jcs.ui.layout.tiles.Tile.DEFAULT_HEIGHT;
import static jcs.ui.layout.tiles.Tile.DEFAULT_WIDTH;
import static jcs.ui.layout.tiles.Tile.RENDER_GRID;
import static jcs.ui.layout.tiles.Tile.RENDER_HEIGHT;
import static jcs.ui.layout.tiles.Tile.RENDER_WIDTH;
import jcs.ui.util.ImageUtil;
import org.tinylog.Logger;

public class Block extends AbstractTile implements Tile, BlockEventListener {

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

  /**
   * Depending on the block status change the background color<br>
   * - Red: Occupied<br>
   * - Green: Departure<br>
   * - Magenta: Arrival / entering<br>
   * - Yellow: reserved<br>
   * - White: all clear / default<br>
   *
   * @return the Color which belong with the current Block State
   */
  public Color getBlockStateColor() {
    if (blockBean != null) {
      BlockState blockState = blockBean.getBlockState();
      return switch (blockState) {
        case LOCKED ->
          new Color(250, 250, 210);
        case OCCUPIED ->
          new Color(250, 210, 210);
        case DEPARTING ->
          new Color(210, 250, 210);
        case ARRIVING ->
          new Color(250, 210, 250);
        default ->
          new Color(255, 255, 255);
      };

    } else {
      return Color.white;
    }
  }

  private String getBlockState() {
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

    // A block has a direction of travel. At first a plus (+) and a Minus(-) at the side
    // was used, but to give more room the - is ommitted and the plus is replaced by a thicker line
    // The default the EAST direction, the block will was like: -[ - bk-nn + ]-
    // The new default the EAST direction, the block will look like: -[ bk-nn  ]]-
    g2.setStroke(new BasicStroke(20, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
    g2.setPaint(Color.darkGray);
    g2.drawLine(rw + 20, yy - 0, rw + 20, yy + 300);

    //When there is a train in the block mark the direction of travel, ie whethe the train goes
    //forwards or backwards by drawing a little triangle in the the block 
    if (getBlockBean() != null && getBlockBean().getLocomotive() != null && getBlockBean().getLocomotive().getName() != null) {
      boolean reverseArrival = getBlockBean().isReverseArrival();
      if (reverseArrival) {
        g2.fillPolygon(new int[]{0, 50, 50,}, new int[]{200, 150, 250}, 3);
      } else {
        g2.fillPolygon(new int[]{1180, 1130, 1130,}, new int[]{200, 150, 250}, 3);
      }
    }

    drawName(g2);
  }

  @Override
  public void renderTileRoute(Graphics2D g2d) {
    
  }

  protected void overlayLocImage(Graphics2D g2d) {
    //Overlay a scaled loc image incase a loc is in the block
    //Overlay is on the scaled image as a loc image is rather small
    //so the lock image only nee a bit more scaling
    //if there is a lock image 
    Image locImage = getLocImage();
    if (locImage != null) {
      // scale it to max h of 45
      int size = 45;
      float aspect = (float) locImage.getHeight(null) / (float) locImage.getWidth(null);
      //TODO: Use Scalr?
      locImage = locImage.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH);

      int w = locImage.getWidth(null);
      int h = locImage.getHeight(null);
      Logger.trace("LocImage w: " + w + " h: " + h);

      //Depending on the block orientation the image need to be rotated and flipped
      switch (getOrientation()) {
        case WEST -> {
          //Image need to be flipped
          locImage = ImageUtil.flipVertically(locImage);
          int xx = x - w / 2;
          int yy = y - h / 2;
          g2d.drawImage(locImage, xx, yy, null);
        }
        case SOUTH -> {
          locImage = ImageUtil.rotate(locImage, 270);
          //loc image has changed direction now so the image need to be flipped horizontally
          locImage = ImageUtil.flipHorizontally(locImage);
          w = locImage.getWidth(null);
          h = locImage.getHeight(null);
          int xx = x - w / 2;
          int yy = y - h / 2;
          g2d.drawImage(locImage, xx, yy, null);
        }
        case NORTH -> {
          locImage = ImageUtil.rotate(locImage, 270);
          w = locImage.getWidth(null);
          h = locImage.getHeight(null);
          int xx = x - w / 2;
          int yy = y - h / 2;
          g2d.drawImage(locImage, xx, yy, null);
        }
        default -> {
          //EAST
          int xx = x - w / 2;
          int yy = y - h / 2;
          g2d.drawImage(locImage, xx, yy, null);
        }
      }
    }
  }

  /**
   * Overridden to overlay a loc Icon
   *
   * @param g2d The graphics handle
   * @param drawOutline
   */
  @Override
  public void drawTile(Graphics2D g2d, boolean drawOutline) {
    super.drawTile(g2d, drawOutline);
    overlayLocImage(g2d);
  }

  @Override
  public void onBlockChange(BlockEvent blockEvent) {
    BlockBean bb = blockEvent.getBlockBean();

    if (blockBean.getId().equals(bb.getId())) {
      blockBean = bb;
      repaintTile();
    }
  }

  private String getLocImageName() {
    if (this.blockBean != null && this.blockBean.getLocomotive() != null && this.blockBean.getLocomotive().getLocIcon() != null) {
      return blockBean.getLocomotive().getIcon();
    } else {
      return null;
    }
  }

  private Image getLocImage() {
    if (this.blockBean != null && this.blockBean.getLocomotive() != null && this.blockBean.getLocomotive().getLocIcon() != null) {
      if (LocomotiveBean.Direction.BACKWARDS == blockBean.getLocomotive().getDirection()) {
        return ImageUtil.flipVertically(blockBean.getLocomotive().getLocIcon());
      } else {
        return blockBean.getLocomotive().getLocIcon();
      }
    } else {
      return null;
    }
  }

  public String getBlockText() {
    String blockText;
    if (!drawOutline && getBlockBean() != null && getBlockBean().getDescription() != null) {
      if (getBlockBean().getLocomotive() != null && getBlockBean().getLocomotive().getName() != null) {
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

  public String getLocomotiveBlockSuffix() {
    String blockSuffix = "";

    if (getBlockBean() != null && getBlockBean().getLocomotive() != null && getBlockBean().getLocomotive().getDirection() != null) {
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

  @Override
  public String getImageKey() {
    StringBuilder sb = getImageKeyBuilder();
    sb.append("~");
    sb.append(getBlockState());

    if (getBlockText() != null) {
      sb.append("~");
      sb.append(getBlockText());
    }

//    if (!"".equals(getLocomotiveBlockSuffix())) {
//      sb.append(getLocomotiveBlockSuffix());
//      sb.append("~");
//      sb.append(getBlockState());
//    }
//    if (getLocImage() != null) {
//      sb.append("~");
//      sb.append(getLocImageName());
//    }
    return sb.toString();
  }
}
