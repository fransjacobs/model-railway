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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jcs.entities.BlockBean;
import jcs.entities.BlockBean.BlockState;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.entities.TileBean.TileType;
import jcs.ui.layout.LayoutUtil;
import static jcs.ui.layout.LayoutUtil.blockHeight;
import static jcs.ui.layout.LayoutUtil.blockWidth;
import jcs.ui.layout.events.TileEvent;
import static jcs.ui.layout.tiles.Tile.DEFAULT_HEIGHT;
import static jcs.ui.layout.tiles.Tile.DEFAULT_WIDTH;
import static jcs.ui.layout.tiles.Tile.RENDER_HEIGHT;
import static jcs.ui.layout.tiles.Tile.RENDER_WIDTH;
import jcs.ui.util.ImageUtil;
import org.tinylog.Logger;

public class Block extends Tile {

  public static final int BLOCK_WIDTH = DEFAULT_WIDTH * 3;
  public static final int BLOCK_HEIGHT = DEFAULT_HEIGHT * 3;

  protected BlockState routeBlockState;

  public Block(TileBean tileBean) {
    super(tileBean, LayoutUtil.blockWidth(tileBean.getOrientation()), LayoutUtil.blockHeight(tileBean.getOrientation()));
    setModel(new DefaultTileModel());
    changeRenderSize();
  }

  public Block(Orientation orientation, Point center) {
    this(orientation, center.x, center.y);
  }

  public Block(Orientation orientation, int x, int y) {
    this(orientation, x, y, LayoutUtil.blockWidth(orientation), LayoutUtil.blockHeight(orientation));
  }

  public Block(Orientation orientation, int x, int y, int width, int height) {
    super(TileType.BLOCK, orientation, x, y, width, height);
    setModel(new DefaultTileModel());
    changeRenderSize();
  }

  private void changeRenderSize() {
    if (Orientation.EAST == tileOrientation || Orientation.WEST == tileOrientation) {
      this.renderWidth = RENDER_WIDTH * 3;
      this.renderHeight = RENDER_HEIGHT;
    } else {
      this.renderWidth = RENDER_WIDTH;
      this.renderHeight = RENDER_HEIGHT * 3;
    }
  }

  @Override
  public Set<Point> getAltPoints() {
    int xx = this.tileX;
    int yy = this.tileY;
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
      if (Orientation.EAST == tileOrientation && Orientation.EAST == match) {
        suffix = "+";
      }
      if (Orientation.WEST == tileOrientation && Orientation.WEST == match) {
        suffix = "+";
      }
      if (Orientation.EAST == tileOrientation && Orientation.WEST == match) {
        suffix = "-";
      }
      if (Orientation.WEST == tileOrientation && Orientation.EAST == match) {
        suffix = "-";
      }
      if (Orientation.NORTH == tileOrientation && Orientation.NORTH == match) {
        suffix = "+";
      }
      if (Orientation.NORTH == tileOrientation && Orientation.SOUTH == match) {
        suffix = "-";
      }
      if (Orientation.SOUTH == tileOrientation && Orientation.SOUTH == match) {
        suffix = "+";
      }
      if (Orientation.SOUTH == tileOrientation && Orientation.NORTH == match) {
        suffix = "-";
      }
    }
    return suffix;
  }

  @Override
  public Orientation rotate() {
    rotate(false);
    int w = LayoutUtil.blockWidth(tileOrientation);
    int h = LayoutUtil.blockHeight(tileOrientation);

    Dimension d = new Dimension(w, h);
    setPreferredSize(d);
    setSize(d);
    changeRenderSize();

    repaint();
    return tileOrientation;
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
  Color getBlockStateColor() {
    return getBlockStateColor(this.model.getBlockState());
  }

  protected Color getBlockStateColor(BlockState blockState) {
    return switch (blockState) {
      case GHOST ->
        new Color(250, 0, 0);
      case LOCKED ->
        new Color(250, 250, 210);
      case OCCUPIED ->
        new Color(250, 210, 210);
      case OUT_OF_ORDER ->
        new Color(190, 190, 190);
      case OUTBOUND ->
        new Color(210, 250, 210);
      case INBOUND ->
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

  public static String getDepartureSuffix(Orientation tileOrientation, boolean reverseArrival, LocomotiveBean.Direction direction) {
    if (LocomotiveBean.Direction.FORWARDS == direction) {
      if (Orientation.EAST == tileOrientation || Orientation.SOUTH == tileOrientation) {
        if (reverseArrival) {
          return "-";
        } else {
          return "+";
        }
      } else {
        if (reverseArrival) {
          return "+";
        } else {
          return "-";
        }
      }
    } else {
      if (Orientation.EAST == tileOrientation || Orientation.SOUTH == tileOrientation) {
        if (reverseArrival) {
          return "+";
        } else {
          return "-";
        }
      } else {
        if (reverseArrival) {
          return "-";
        } else {
          return "+";
        }
      }
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
    //Logger.trace("Block " + this.id + " State: " + this.getBlockBean().getBlockState().getState() + " Color: " + blockStateColor.toString());
    g2.setPaint(blockStateColor);
    g2.fillRoundRect(xx, yy, rw, rh, 15, 15);

    g2.setStroke(new BasicStroke(20, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
    g2.setPaint(Color.darkGray);
    g2.drawLine(rw + GRID, yy - 0, rw + GRID, yy + 300);

    //When there is a locomotive in the block mark the direction of travel.
    //The default, forwards is in the direction of the block orientation, i.e. the +
    if (getBlockBean() != null && getBlockBean().getLocomotive() != null && getBlockBean().getLocomotive().getName() != null) {
      renderDirectionArrow(g2);
    }

    drawName(g2);
  }

  private void renderDirectionArrow(Graphics2D g2) {
    //The default, forwards is in the direction of the block orientation, i.e. the +
    Orientation orientation = this.getOrientation();
    BlockBean bb = this.getBlockBean();
    boolean reverseArrival = bb.isReverseArrival();

    LocomotiveBean.Direction logicalDirection;
    if (bb.getLogicalDirection() != null) {
      logicalDirection = LocomotiveBean.Direction.get(bb.getLogicalDirection());
    } else {
      logicalDirection = bb.getLocomotive().getDirection();
    }

    String departureSuffix = bb.getDepartureSuffix();
    if (departureSuffix == null) {
      departureSuffix = Block.getDepartureSuffix(orientation, reverseArrival, logicalDirection);
    }

    //Logger.trace(this.getId()+" LogicalDirection is " + (bb.getLogicalDirection() != null ? "Set" : "Not Set") + " Dir: " + logicalDirection.getDirection() + " Orientation: " + orientation.getOrientation() + " departureSuffix: " + departureSuffix);
    if ("+".equals(departureSuffix)) {
      if (Orientation.EAST == orientation || Orientation.SOUTH == orientation) {
        switch (logicalDirection) {
          case LocomotiveBean.Direction.FORWARDS -> {
            if (reverseArrival) {
              renderLeftArrow(g2);
            } else {
              renderRightArrow(g2);
            }
          }
          case LocomotiveBean.Direction.BACKWARDS -> {
            if (reverseArrival) {
              renderRightArrow(g2);
            } else {
              renderLeftArrow(g2);
            }
          }
        }
      } else {
        switch (logicalDirection) {
          case LocomotiveBean.Direction.BACKWARDS -> {
            if (reverseArrival) {
              renderLeftArrow(g2);
            } else {
              renderRightArrow(g2);
            }
          }
          case LocomotiveBean.Direction.FORWARDS -> {
            if (reverseArrival) {
              renderRightArrow(g2);
            } else {
              renderLeftArrow(g2);
            }
          }
        }
      }
    } else {
      if (Orientation.EAST == orientation || Orientation.SOUTH == orientation) {
        switch (logicalDirection) {
          case LocomotiveBean.Direction.FORWARDS -> {
            if (reverseArrival) {
              renderLeftArrow(g2);
            } else {
              renderRightArrow(g2);
            }
          }
          case LocomotiveBean.Direction.BACKWARDS -> {
            if (reverseArrival) {
              renderRightArrow(g2);
            } else {
              renderLeftArrow(g2);
            }
          }
        }
      } else {
        switch (logicalDirection) {
          case LocomotiveBean.Direction.BACKWARDS -> {
            if (reverseArrival) {
              renderLeftArrow(g2);
            } else {
              renderRightArrow(g2);
            }
          }
          case LocomotiveBean.Direction.FORWARDS -> {
            if (reverseArrival) {
              renderRightArrow(g2);
            } else {
              renderLeftArrow(g2);
            }
          }
        }
      }
    }
  }

  private void renderLeftArrow(Graphics2D g2) {
    //Logger.trace(this.getId()+" LogicalDirection is " + this.getBlockBean().getLogicalDirection() + " Orientation: " + this.getOrientation() + " departureSuffix: " + this.getBlockBean().getDepartureSuffix());
    g2.fillPolygon(new int[]{0, 50, 50,}, new int[]{200, 150, 250}, 3);
  }

  private void renderRightArrow(Graphics2D g2) {
    //Logger.trace(this.getId()+" LogicalDirection is " + this.getBlockBean().getLogicalDirection() + " Orientation: " + this.getOrientation() + " departureSuffix: " + this.getBlockBean().getDepartureSuffix());
    g2.fillPolygon(new int[]{1180, 1130, 1130,}, new int[]{200, 150, 250}, 3);
  }

  @Override
  public void renderTileRoute(Graphics2D g2d) {
    if (routeBlockState != null) {
      backgroundColor = getBlockStateColor(routeBlockState);
    }
  }

  protected void overlayLocImage(Graphics2D g2d) {
    Image locImage = getLocImage();
    String departureSuffix = null;
    boolean reverseImage = true;
    if (getBlockBean() != null) {
      reverseImage = getBlockBean().isReverseArrival();
    }

    if (getBlockBean() != null) {
      departureSuffix = getBlockBean().getDepartureSuffix();
    }
    if (locImage != null) {
      // scale it to max h of 45
      int size = 45;
      float aspect = (float) locImage.getHeight(null) / (float) locImage.getWidth(null);
      //TODO: Use Scalr?
      locImage = locImage.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH);

      //Logger.trace("LocImage w: " + w + " h: " + h);
      //Depending on the block orientation the image needs to be rotated and flipped
      //Incase the departure suffix is NOT set center the locomotive image
      switch (tileOrientation) {
        case WEST -> {
          int xx;
          int w = locImage.getWidth(null);
          int h = locImage.getHeight(null);

          if (null == departureSuffix) {
            xx = tileX - getWidth() / 2 + w;
          } else {
            switch (departureSuffix) {
              case "+" -> {
                xx = tileX - getWidth() / 2 + w - 25;
              }
              default -> {
                xx = tileX - getWidth() / 2 + w + 10;
              }
            }
          }
          int yy = tileY - h / 2;

          if (reverseImage) {
            locImage = ImageUtil.flipVertically(locImage);
          }
          g2d.drawImage(locImage, xx, yy, null);
        }
        case SOUTH -> {
          locImage = ImageUtil.flipHorizontally(locImage);
          locImage = ImageUtil.rotate(locImage, 90);

          int w = locImage.getWidth(null);
          int h = locImage.getHeight(null);

          int xx = tileX - w / 2;
          int yy;
          if (null == departureSuffix) {
            yy = tileY - getHeight() / 2 + h;
          } else {
            switch (departureSuffix) {
              case "-" -> {
                yy = tileY - getHeight() / 2 + h - 25;
              }
              default -> {
                yy = tileY - getHeight() / 2 + h + 10;
              }
            }
          }
          if (reverseImage) {
            locImage = ImageUtil.flipHorizontally(locImage);
          }
          g2d.drawImage(locImage, xx, yy, null);
        }
        case NORTH -> {
          locImage = ImageUtil.flipHorizontally(locImage);
          locImage = ImageUtil.rotate(locImage, 90);

          int w = locImage.getWidth(null);
          int h = locImage.getHeight(null);

          int xx = tileX - w / 2;
          int yy;
          if (null == departureSuffix) {
            int minY = tileY - getHeight() / 2 + h;
            yy = minY;
          } else {
            switch (departureSuffix) {
              case "+" -> {
                yy = tileY - getHeight() / 2 + h - 25;
              }
              default -> {
                yy = tileY - getHeight() / 2 + h + 10;
              }
            }
          }
          if (reverseImage) {
            locImage = ImageUtil.flipHorizontally(locImage);
          }
          g2d.drawImage(locImage, xx, yy, null);
        }
        default -> {
          //EAST
          int xx;
          int w = locImage.getWidth(null);
          int h = locImage.getHeight(null);

          if (null == departureSuffix) {
            xx = tileX - getWidth() / 2 + w;
          } else {
            switch (departureSuffix) {
              case "-" -> {
                xx = tileX - getWidth() / 2 + w - 25;
              }
              default -> {
                xx = tileX - getWidth() / 2 + w + 10;
              }
            }
          }
          int yy = tileY - h / 2;

          if (reverseImage) {
            locImage = ImageUtil.flipVertically(locImage);
          }
          g2d.drawImage(locImage, xx, yy, null);
        }
      }
    }
  }

  /**
   * Overridden to overlay a locomotive Icon
   *
   * @param g2d The graphics handle
   */
  @Override
  public void drawTile(Graphics2D g2d) {
    super.drawTile(g2d);
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
      repaint();
    }
  }

  private Image getLocImage() {
    if (blockBean != null && blockBean.getLocomotive() != null && blockBean.getLocomotive().getLocIcon() != null) {
      //Do not show the image in block state FREE, OUT_OF_ORDER and LOCKED
      BlockState blockState = blockBean.getBlockState();
      boolean showImage = !(BlockState.FREE == blockState || BlockState.LOCKED == blockState || BlockState.OUT_OF_ORDER == blockState || BlockState.GHOST == blockState);
      if (showImage) {
        return blockBean.getLocomotive().getLocIcon();
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public String getBlockText() {
    String blockText;
    if (getBlockBean() != null && getBlockBean().getDescription() != null) {
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

  @Override
  protected void paintComponent(Graphics g) {
    long started = System.currentTimeMillis();
    super.paintComponent(g);

    int multiplier = (model.isScaleImage() ? 1 : 10);
    int xx, yy;
    if (tileOrientation == Orientation.EAST || tileOrientation == Orientation.WEST) {
      xx = tileX - GRID * multiplier - GRID * multiplier * 2;
      yy = tileY - GRID * multiplier;
    } else {
      xx = tileX - GRID * multiplier;
      yy = tileY - GRID * multiplier - GRID * multiplier * 2;
    }

    if (model.isScaleImage()) {
      setBounds(xx, yy, LayoutUtil.blockWidth(tileOrientation), LayoutUtil.blockHeight(tileOrientation));
    } else {
      setBounds(xx, yy, renderWidth, renderHeight);
    }

    Logger.trace(id + ": " + tileOrientation + " W: " + getWidth() + " H: " + getHeight() + " tX: " + tileX + ", tY: " + tileY + " xx: " + xx + " yy: " + yy + " Scale factor: " + multiplier);

    Graphics2D g2 = (Graphics2D) g.create();
    //Graphics2D g2 = (Graphics2D) g;
    drawTile(g2);
    g2.dispose();

    g.drawImage(tileImage, 0, 0, null);

    long now = System.currentTimeMillis();
    Logger.trace(id + " Duration: " + (now - started) + " ms.");
  }

  @Override
  protected void drawCenterPoint(Graphics2D g2d, Color color, double size) {
    //A block has 2 alternate points
    //1st square 
    //2nd square holds the centerpoint
    //3rd square
    double dX1, dX2, dX3, dY1, dY2, dY3;
    if (Orientation.EAST == tileOrientation || Orientation.WEST == tileOrientation) {
      dX1 = renderWidth / 3 / 2 - size / 2 / 2;
      dY1 = renderHeight / 2 - size / 2 / 2;
      dX2 = renderWidth / 2 - size / 2;
      dY2 = renderHeight / 2 - size / 2;
      dX3 = renderWidth - renderWidth / 3 / 2 - size / 2 / 2;
      dY3 = renderHeight / 2 - size / 2 / 2;
    } else {
      dX1 = renderWidth / 2 - size / 2 / 2;
      dY1 = renderHeight / 3 / 2 - size / 2 / 2;
      dX2 = renderHeight / 2 - size / 2;
      dY2 = renderWidth / 2 - size / 2;
      dY3 = renderWidth / 2 - size / 2 / 2;
      dX3 = renderHeight - renderHeight / 3 / 2 - size / 2 / 2;
    }

    g2d.setColor(color);
    g2d.fill(new Ellipse2D.Double(dX1, dY1, size / 2, size / 2));
    g2d.fill(new Ellipse2D.Double(dX2, dY2, size, size));
    g2d.fill(new Ellipse2D.Double(dX3, dY3, size / 2, size / 2));

//    g2d.setPaint(Color.black);
//    g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
//    g2d.drawOval(renderWidth / 2 - 5, renderHeight / 2 - 5, 10, 10);

    Logger.trace(id + " dX2: " + dX2 + " dY2: " + dY2 + " O: " + tileOrientation + " rW: " + renderWidth + " rH:" + renderHeight + " Size: " + size);
    Logger.trace(id + " dX1: " + dX1 + " dY1: " + dY1);
    Logger.trace(id + " dX3: " + dX3 + " dY3: " + dY3);
  }

}
