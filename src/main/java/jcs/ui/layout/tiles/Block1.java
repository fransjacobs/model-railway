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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import jcs.commandStation.events.BlockEvent;
import jcs.commandStation.events.BlockEventListener;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import static jcs.ui.layout.tiles.AbstractTile.drawRotate;
import static jcs.ui.layout.tiles.Tile.DEFAULT_HEIGHT;
import static jcs.ui.layout.tiles.Tile.DEFAULT_TRACK_COLOR;
import static jcs.ui.layout.tiles.Tile.DEFAULT_WIDTH;
import static jcs.ui.layout.tiles.Tile.RENDER_GRID;
import static jcs.ui.layout.tiles.Tile.RENDER_HEIGHT;
import static jcs.ui.layout.tiles.Tile.RENDER_WIDTH;
import jcs.ui.util.ImageUtil;
import org.imgscalr.Scalr;
import org.tinylog.Logger;

public class Block1 extends AbstractTile implements Tile, BlockEventListener {

  public static final int BLOCK_WIDTH = DEFAULT_WIDTH * 3;
  public static final int BLOCK_HEIGHT = DEFAULT_HEIGHT * 3;

  Block1(TileBean tileBean) {
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

  Block1(Orientation orientation, Point center) {
    this(orientation, center.x, center.y);
  }

  Block1(Orientation orientation, int x, int y) {
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

    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));

    Color r = new Color(250, 210, 210);
    Color g = new Color(210, 250, 210);
    Color m = new Color(250, 210, 250);
    Color y = new Color(250, 250, 210);

    g2.setPaint(y);

    g2.fillRoundRect(xx, yy, rw, rh, 15, 15);

    g2.setPaint(Color.black);
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

  /**
   * Draw the AbstractTile
   *
   * @param g2d The graphics handle
   * @param drawOutline
   */
  @Override
  public void drawTile(Graphics2D g2d, boolean drawOutline) {
    // by default and image is rendered in the EAST orientation
    Orientation o = getOrientation();
    if (o == null) {
      o = Orientation.EAST;
    }

    if (!TileImageCache.contains(this)) {
      BufferedImage nbi = createImage();

      Graphics2D g2di = nbi.createGraphics();
      if (trackColor == null) {
        trackColor = DEFAULT_TRACK_COLOR;
      }

      if (backgroundColor == null) {
        backgroundColor = Color.white;
      }

      AffineTransform trans = new AffineTransform();

      g2di.setBackground(backgroundColor);
      g2di.clearRect(0, 0, this.getRenderWidth(), this.getRenderHeight());

      int ox = 0, oy = 0;

      switch (o) {
        case SOUTH -> {
          trans.rotate(Math.PI / 2, this.renderWidth / 2, this.renderHeight / 2);
          ox = (this.renderHeight - this.renderWidth) / 2;
          oy = (this.renderWidth - this.renderHeight) / 2;
          trans.translate(-ox, -oy);
        }
        case WEST -> {
          trans.rotate(Math.PI, this.renderWidth / 2, this.renderHeight / 2);
          trans.translate(ox, oy);
        }
        case NORTH -> {
          trans.rotate(-Math.PI / 2, this.renderWidth / 2, this.renderHeight / 2);
          ox = (this.renderHeight - this.renderWidth) / 2;
          oy = (this.renderWidth - this.renderHeight) / 2;
          trans.translate(-ox, -oy);
        }
        default -> {
          trans.rotate(0.0, this.renderWidth / 2, this.renderHeight / 2);
          trans.translate(ox, oy);
        }
      }

      g2di.setTransform(trans);
      renderTile(g2di, trackColor, backgroundColor);

      //When the line grid is one the scale tile must be a little smaller
      int sw, sh;
      if (drawOutline) {
        sw = this.getWidth() - 2;
        sh = this.getHeight() - 2;
      } else {
        sw = this.getWidth();
        sh = this.getHeight();
      }
      // Scale the image back...
      if (scaleImage) {
        nbi = Scalr.resize(nbi, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, sw, sh, Scalr.OP_ANTIALIAS);
      }

      g2di.dispose();
      TileImageCache.put(this, nbi);
    }

    BufferedImage cbi = TileImageCache.get(this);

    int ox, oy;
    if (scaleImage) {
      ox = this.offsetX;
      oy = this.offsetY;
    } else {
      ox = this.renderOffsetX;
      oy = this.renderOffsetY;
    }

    g2d.drawImage(cbi, (x - cbi.getWidth() / 2) + ox, (y - cbi.getHeight() / 2) + oy, null);

    //if there is a lock image 
    Image locImage = getLocImage();
    if (locImage != null) {
      // real size 
      //scale it to max h of 20
      int size = 35;
      float aspect = (float) locImage.getHeight(null) / (float) locImage.getWidth(null);
      locImage = locImage.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH);

      int w = locImage.getWidth(null);
      int h = locImage.getHeight(null);
      Logger.trace("LocImage w: " + w + " h: " + h);

      int xx = x - w / 2;
      int yy = y - h / 2;

      g2d.drawImage(locImage, xx, yy, null);
    }
  }

  @Override
  public void onBlockChange(BlockEvent blockEvent) {
    BlockBean bb = blockEvent.getBlockBean();

    if (blockBean.getId().equals(bb.getId())) {
      blockBean = bb;
      repaintTile();
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

      boolean reverseArrival = getBlockBean().isReverseArrival();
      if (getLocImage() != null) {
        //when there is an image only show the direction arrow
        blockText = "";
      }

      String f = String.valueOf((char) 0x25b8);
      String r = String.valueOf((char) 0x25c2);

      String direction;
      if (locInBlock) {
        direction = (LocomotiveBean.Direction.FORWARDS == getBlockBean().getLocomotive().getDirection() ? f : r);
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
    Image locImage = getLocImage();

    if (locImage == null) {
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
    } else {
      //Draw an arrow in the drive direction
      g2d.setPaint(Color.black);

      //char d = LocomotiveBean.Direction.FORWARDS == getBlockBean().getLocomotive().getDirection() ? (char) 0x25ba : (char) 0x25c4;
      char d = LocomotiveBean.Direction.FORWARDS == getBlockBean().getLocomotive().getDirection() ? (char) 0x25b6 : (char) 0x25c0;
      //String direction = String.valueOf(d);
      String direction = String.valueOf((char) 0x25c0)+" "+String.valueOf((char) 0x25b8);
      int textWidth = g2d.getFontMetrics().stringWidth(direction);
//      double fontscale = 10.0;
//      if (textWidth > 845) {
//        fontscale = fontscale * 847.0 / textWidth;
//        newFont = currentFont.deriveFont(currentFont.getSize() * (float) fontscale);
//        g2d.setFont(newFont);
//        textWidth = g2d.getFontMetrics().stringWidth(blockText);
//      }
      int textHeight = g2d.getFontMetrics().getHeight();

      //TODO place the arrow near the + or -
      switch (getOrientation()) {
        case EAST -> {
          drawRotate(g2d, ((RENDER_WIDTH * 3) / 2) - textWidth / 2 + 400, RENDER_GRID + textHeight / 3, 0, direction);
        }
        case WEST -> {
          drawRotate(g2d, ((RENDER_WIDTH * 3) / 2) + textWidth / 2, RENDER_GRID - textHeight / 3, 180, direction);
        }
        case NORTH -> {
          drawRotate(g2d, ((RENDER_WIDTH * 3) / 2) - textWidth / 2, RENDER_GRID + textHeight / 3, 0, direction);
        }
        case SOUTH -> {
          drawRotate(g2d, ((RENDER_WIDTH * 3) / 2) + textWidth / 2, RENDER_GRID - textHeight / 3, 180, direction);
        }
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

    //TODO also append something for the loc image when avalaible
    return sb.toString();
  }
}
