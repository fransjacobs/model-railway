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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import jcs.entities.BlockBean;
import static jcs.entities.BlockBean.BlockState.GHOST;
import static jcs.entities.BlockBean.BlockState.INBOUND;
import static jcs.entities.BlockBean.BlockState.LOCKED;
import static jcs.entities.BlockBean.BlockState.OCCUPIED;
import static jcs.entities.BlockBean.BlockState.OUTBOUND;
import static jcs.entities.BlockBean.BlockState.OUT_OF_ORDER;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.ui.layout.tiles.Block;
import static jcs.ui.layout.tiles.Block.BLOCK_HEIGHT;
import static jcs.ui.layout.tiles.Block.BLOCK_WIDTH;
import jcs.ui.layout.tiles.Tile;
import static jcs.ui.layout.tiles.Tile.DEFAULT_HEIGHT;
import static jcs.ui.layout.tiles.Tile.DEFAULT_WIDTH;
import static jcs.ui.layout.tiles.Tile.GRID;
import jcs.ui.layout.tiles.TileModel;
import jcs.ui.util.ImageUtil;
import org.tinylog.Logger;

public class BlockUI extends TileUI {

  public BlockUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new BlockUI();
  }

  protected Color getBlockStateColor(BlockBean.BlockState blockState) {
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

  /**
   * A block is rendered in xx steps:<br>
   * - 1 the block contour and background color, based on the block status.<br>
   * - 2 in case the block is occupied or reserved by a locomotive, draw a travel direction arrow<br>. - 3 in case the block is Occupied or Reserved and a loc icon is available draw/overlay a loc icon
   * in the block.<br>
   *
   * @param g2 the render graphics
   * @param c to Block Component to render
   */
  @Override
  public void renderTile(Graphics2D g2, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();

    int xx = 20;
    int yy = 50;
    int rw = RENDER_WIDTH * 3 - 40;
    int rh = 300;

    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));

    g2.setPaint(Color.darkGray);
    g2.drawRoundRect(xx, yy, rw, rh, 15, 15);

    Color blockStateColor = getBlockStateColor(model.getBlockState());
    //Logger.trace("Block " + this.id + " State: " + this.getBlockBean().getBlockState().getState() + " Color: " + blockStateColor.toString());
    g2.setPaint(blockStateColor);
    g2.fillRoundRect(xx, yy, rw, rh, 15, 15);

    g2.setStroke(new BasicStroke(20, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
    g2.setPaint(Color.darkGray);
    g2.drawLine(rw + GRID, yy - 0, rw + GRID, yy + 300);

    //When there is a locomotive in the block mark the direction of travel.
    //The default, forwards is in the direction of the block orientation, i.e. the +
    if (model.getLocomotive() != null && model.getLocomotive().getName() != null) {
      renderDirectionArrow(g2, c);
    }

    drawName(g2, c);

  }

  private void renderDirectionArrow(Graphics2D g2, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();
    //The default, forwards is in the direction of the block orientation, i.e. the +
    TileBean.Orientation blockOrientation = model.getTileOrienation();
    BlockBean bb = tile.getBlockBean();

    LocomotiveBean.Direction logicalDirection;
    if (bb.getLogicalDirection() != null) {
      logicalDirection = model.getLogicalDirection();
    } else {
      logicalDirection = model.getLocomotive().getDirection();
    }

    String arrivalSide = bb.getArrivalSuffix();
    if (arrivalSide == null) {
      //Use the default for the block which is from - to plus
      arrivalSide = Block.getDefaulArrivalSuffix(blockOrientation, logicalDirection);
    }

    switch (blockOrientation) {
      case TileBean.Orientation.WEST -> {
        switch (logicalDirection) {
          case LocomotiveBean.Direction.FORWARDS -> {
            if ("-".equals(arrivalSide)) {
              renderRightArrow(g2);
            } else {
              renderLeftArrow(g2);
            }
          }
          case LocomotiveBean.Direction.BACKWARDS -> {
            if ("-".equals(arrivalSide)) {
              renderRightArrow(g2);
            } else {
              renderLeftArrow(g2);
            }
          }
        }
      }
      case TileBean.Orientation.NORTH -> {
        switch (logicalDirection) {
          case LocomotiveBean.Direction.FORWARDS -> {
            if ("-".equals(arrivalSide)) {
              renderRightArrow(g2);
            } else {
              renderLeftArrow(g2);
            }
          }
          case LocomotiveBean.Direction.BACKWARDS -> {
            if ("-".equals(arrivalSide)) {
              renderRightArrow(g2);
            } else {
              renderLeftArrow(g2);
            }
          }
        }
      }
      case TileBean.Orientation.SOUTH -> {
        switch (logicalDirection) {
          case LocomotiveBean.Direction.FORWARDS -> {
            if ("-".equals(arrivalSide)) {
              renderRightArrow(g2);
            } else {
              renderLeftArrow(g2);
            }
          }
          case LocomotiveBean.Direction.BACKWARDS -> {
            if ("-".equals(arrivalSide)) {
              renderRightArrow(g2);
            } else {
              renderLeftArrow(g2);
            }
          }
        }
      }
      default -> {
        //East
        switch (logicalDirection) {
          case LocomotiveBean.Direction.FORWARDS -> {
            if ("-".equals(arrivalSide)) {
              renderRightArrow(g2);
            } else {
              renderLeftArrow(g2);
            }
          }
          case LocomotiveBean.Direction.BACKWARDS -> {
            if ("-".equals(arrivalSide)) {
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
  public void renderTileRoute(Graphics2D g2d, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();

    if (model.isShowBlockState()) {
      backgroundColor = getBlockStateColor(model.getBlockState());
    }
  }

  /**
   * Called to draw the locomotive icon is a block.<br>
   * The tile Image is already rendered, so the locomotive image is put on top.
   *
   * @param c the block component
   */
  protected void overlayLocImage(JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();

    Image locImage;
    if (model.getLocomotive() != null && model.getLocomotive().getLocIcon() != null) {
      locImage = model.getLocomotive().getLocIcon().getImage();
    } else {
      return;
    }

    Logger.trace("LocImage w: " + locImage.getWidth(null) + " h: " + locImage.getHeight(null));
    // scale it to max w of 45
    float ratio = (float) locImage.getHeight(null) / (float) locImage.getWidth(null);
    //TODO: Use Scalr?
    locImage = locImage.getScaledInstance(LOC_IMAGE_WIDTH, (int) (LOC_IMAGE_WIDTH * ratio), Image.SCALE_SMOOTH);

    //for orientations E and W, N and S the image mus be rotated first
    int w = locImage.getWidth(null);
    int h = locImage.getHeight(null);

    int tw = tileImage.getWidth();
    int th = tileImage.getHeight();
    BufferedImage overlay = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2i = overlay.createGraphics();

    //The default, forwards is in the direction of the block orientation, i.e. the +
    TileBean.Orientation blockOrientation = model.getTileOrienation();
    BlockBean bb = tile.getBlockBean();

    LocomotiveBean.Direction logicalDirection;

    if (bb.getLogicalDirection() != null) {
      logicalDirection = model.getLogicalDirection();
    } else {
      if (model.getLocomotive() != null && model.getLocomotive().getDirection() != null) {
        logicalDirection = model.getLocomotive().getDirection();
      } else {
        Logger.warn("Can't obtain Locomotive direction");
        logicalDirection = LocomotiveBean.Direction.FORWARDS;
      }
    }

    String arrivalSide = bb.getArrivalSuffix();
    if (arrivalSide == null) {
      //Use the default for the block which is from - to plus
      arrivalSide = Block.getDefaulArrivalSuffix(blockOrientation, logicalDirection);
    }

    //Depending on the block orientation,
    //travel direction and locomotive direction the image needs to be rotated and flipped
    int xx, yy;
    boolean reverseImage = LocomotiveBean.Direction.BACKWARDS == logicalDirection;
    switch (blockOrientation) {
      case TileBean.Orientation.SOUTH -> {
        //Rotate the image 90 deg. to point to the South
        locImage = ImageUtil.rotate(locImage, 90);

        w = locImage.getWidth(null);
        h = locImage.getHeight(null);

        //Calculate the offset from center.
        //When arrival is - place the image nearer to the + and vice versa
        switch (arrivalSide) {
          case "+" -> {
            yy = BLOCK_HEIGHT / 2 - th / 2 + h - 25;
            reverseImage = LocomotiveBean.Direction.FORWARDS == logicalDirection;
          }
          default -> {
            yy = BLOCK_HEIGHT / 2 - th / 2 + h + 10;
          }
        }
        xx = DEFAULT_WIDTH / 2 - w / 2;

        if (reverseImage) {
          locImage = ImageUtil.flipHorizontally(locImage);
        }
      }
      case TileBean.Orientation.WEST -> {
        //default should be flipped
        locImage = ImageUtil.flipVertically(locImage);

        switch (arrivalSide) {
          case "-" -> {
            xx = BLOCK_WIDTH / 2 - tw / 2 + w - 25;
            reverseImage = LocomotiveBean.Direction.BACKWARDS == logicalDirection;
          }
          default -> {
            xx = BLOCK_WIDTH / 2 - tw / 2 + w + 10;
            reverseImage = LocomotiveBean.Direction.FORWARDS == logicalDirection;
          }
        }
        yy = DEFAULT_HEIGHT / 2 - h / 2;

        if (reverseImage) {
          locImage = ImageUtil.flipVertically(locImage);
        }
      }
      case TileBean.Orientation.NORTH -> {
        locImage = ImageUtil.rotate(locImage, 270);
        locImage = ImageUtil.flipVertically(locImage);

        w = locImage.getWidth(null);
        h = locImage.getHeight(null);

        switch (arrivalSide) {
          case "-" -> {
            yy = BLOCK_HEIGHT / 2 - th / 2 + h - 25;
            reverseImage = LocomotiveBean.Direction.BACKWARDS == logicalDirection;
          }
          default -> {
            yy = BLOCK_HEIGHT / 2 - th / 2 + h + 10;
            reverseImage = LocomotiveBean.Direction.FORWARDS == logicalDirection;
          }
        }
        xx = DEFAULT_WIDTH / 2 - w / 2;

        if (reverseImage) {
          locImage = ImageUtil.flipHorizontally(locImage);
        }
      }

      default -> {
        //East
        switch (arrivalSide) {
          case "+" -> {
            xx = BLOCK_WIDTH / 2 - tw / 2 + w - 25;
            reverseImage = LocomotiveBean.Direction.FORWARDS == logicalDirection;
          }
          default -> {
            xx = BLOCK_WIDTH / 2 - tw / 2 + w + 10;
          }
        }
        yy = DEFAULT_HEIGHT / 2 - h / 2;

        if (reverseImage) {
          locImage = ImageUtil.flipVertically(locImage);
        }
      }
    }

    g2i.drawImage(tileImage, 0, 0, null);
    g2i.drawImage(locImage, xx, yy, null);
    g2i.dispose();

    tileImage = overlay;
  }

  protected void overlayLocImageOld(JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();

    int ww = tileImage.getWidth();
    int hh = tileImage.getHeight();
    TileBean.Orientation tileOrientation = model.getTileOrienation();

    LocomotiveBean.Direction logicalDirection;
    if (model.getLogicalDirection() != null) {
      logicalDirection = model.getLogicalDirection();
    } else {
      logicalDirection = model.getLocomotive().getDirection();
    }

    BufferedImage overlay = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2i = overlay.createGraphics();

    Image locImage;
    if (model.getLocomotive() != null && model.getLocomotive().getLocIcon() != null) {
      locImage = model.getLocomotive().getLocIcon().getImage();
    } else {
      locImage = null;
    }

    if (locImage != null) {
      String departureSuffix = model.getDepartureSuffix();
      if (departureSuffix == null) {
        departureSuffix = Block.getDepartureSuffix(tileOrientation, logicalDirection);
      }

      String defaultDepartureSuffix = Block.getDepartureSuffix(tileOrientation, logicalDirection);

      boolean reverseImage = !departureSuffix.equals(defaultDepartureSuffix);

//      LocomotiveBean.Direction dir = model.getLogicalDirection();
//      if (dir == null) {
//        dir = model.getLocomotive().getDirection();
//      }
      Logger.trace("LocImage w: " + locImage.getWidth(null) + " h: " + locImage.getHeight(null));
      // scale it to max h of 45
      int size = 45;
      float aspect = (float) locImage.getHeight(null) / (float) locImage.getWidth(null);
      //TODO: Use Scalr?
      locImage = locImage.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH);

      //Depending on the block orientation the image needs to be rotated and flipped
      //Incase the departure suffix is NOT set center the locomotive image
      int w, h, xx, yy;
      switch (tileOrientation) {
        case WEST -> {
          w = locImage.getWidth(null);
          h = locImage.getHeight(null);

          //if (null == departureSuffix) {
          //  xx = BLOCK_WIDTH / 2 - tile.getWidth() / 2 + w;
          //} else {
          switch (departureSuffix) {
            case "+" -> {
              xx = BLOCK_WIDTH / 2 - tile.getWidth() / 2 + w - 25;
            }
            default -> {
              xx = BLOCK_WIDTH / 2 - tile.getWidth() / 2 + w + 10;
            }
          }
          //}
          yy = DEFAULT_HEIGHT / 2 - h / 2;

          if (!reverseImage) {
            locImage = ImageUtil.flipVertically(locImage);
          }
        }
        case SOUTH -> {
          locImage = ImageUtil.flipHorizontally(locImage);
          locImage = ImageUtil.rotate(locImage, 90);

          w = locImage.getWidth(null);
          h = locImage.getHeight(null);
          xx = DEFAULT_WIDTH / 2 - w / 2;

          //if (null == departureSuffix) {
          //  yy = BLOCK_HEIGHT / 2 - tile.getHeight() / 2 + h;
          //} else {
          switch (departureSuffix) {
            case "-" -> {
              yy = BLOCK_HEIGHT / 2 - tile.getHeight() / 2 + h - 25;
            }
            default -> {
              yy = BLOCK_HEIGHT / 2 - tile.getHeight() / 2 + h + 10;
            }
          }
          //}
          if (reverseImage) {
            locImage = ImageUtil.flipHorizontally(locImage);
          }
        }
        case NORTH -> {
          locImage = ImageUtil.flipHorizontally(locImage);

          if (reverseImage) {
            locImage = ImageUtil.rotate(locImage, -90);
          } else {
            locImage = ImageUtil.rotate(locImage, 90);
          }

          w = locImage.getWidth(null);
          h = locImage.getHeight(null);
          xx = DEFAULT_WIDTH / 2 - w / 2;

          //if (null == departureSuffix) {
          //int minY = BLOCK_HEIGHT / 2 - tile.getHeight() / 2 + h;
          //  yy = minY;
          //} else {
          switch (departureSuffix) {
            case "+" -> {
              yy = BLOCK_HEIGHT / 2 - tile.getHeight() / 2 + h - 25;
            }
            default -> {
              yy = BLOCK_HEIGHT / 2 - tile.getHeight() / 2 + h + 10;
            }
          }
          //}
          locImage = ImageUtil.flipHorizontally(locImage);
        }
        default -> {
          w = locImage.getWidth(null);
          h = locImage.getHeight(null);
          //if (null == departureSuffix) {
          //  xx = BLOCK_WIDTH / 2 - tile.getWidth() / 2 + w;
          //} else {
          switch (departureSuffix) {
            case "-" -> {
              xx = BLOCK_WIDTH / 2 - tile.getWidth() / 2 + w - 25;
            }
            default -> {
              xx = BLOCK_WIDTH / 2 - tile.getWidth() / 2 + w + 10;
            }
          }
          //}
          yy = DEFAULT_HEIGHT / 2 - h / 2;

          if (reverseImage) {
            locImage = ImageUtil.flipVertically(locImage);
          }
        }
      }

      g2i.drawImage(tileImage, 0, 0, null);
      g2i.drawImage(locImage, xx, yy, null);
      g2i.dispose();
      tileImage = overlay;
    }
  }

  public String getBlockText(Tile tile) {
    BlockBean blockBean = tile.getBlockBean();
    String blockText;
    if (blockBean != null && blockBean.getDescription() != null) {
      if (blockBean.getLocomotive() != null && blockBean.getLocomotive().getName() != null && BlockBean.BlockState.GHOST != blockBean.getBlockState()) {
        blockText = blockBean.getLocomotive().getName();
      } else {
        if (blockBean.getDescription().length() > 0) {
          blockText = blockBean.getDescription();
        } else {
          blockText = tile.getId();
        }
      }
    } else {
      // Design mode show description when available
      if (blockBean != null && blockBean.getDescription() != null && blockBean.getDescription().length() > 0) {
        blockText = blockBean.getDescription();
      } else {
        blockText = tile.getId();
      }
    }
    return blockText;
  }

  @Override
  public void drawName(Graphics2D g2d, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();

    if (!model.isOverlayImage()) {
      g2d.setPaint(Color.black);

      Font currentFont = g2d.getFont();
      Font newFont = currentFont.deriveFont(currentFont.getSize() * 10.0F);
      g2d.setFont(newFont);

      String blockText = getBlockText(tile);

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
      TileBean.Orientation tileOrientation = model.getTileOrienation();

      switch (tileOrientation) {
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
  protected void drawCenterPoint(Graphics2D g2d, Color color, double size, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();
    //A block has 2 alternate points
    //1st square 
    //2nd square holds the centerpoint
    //3rd square
    TileBean.Orientation tileOrientation = model.getTileOrienation();
    double dX1, dX2, dX3, dY1, dY2, dY3;
    if (TileBean.Orientation.EAST == tileOrientation || TileBean.Orientation.WEST == tileOrientation) {
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
  }

  @Override
  public void paint(Graphics g, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();

    long started = System.currentTimeMillis();
    //  We don't want to paint inside the insets or borders.
    Insets insets = c.getInsets();
    g.translate(insets.left, insets.top);

    Graphics2D g2 = (Graphics2D) g.create();
    drawTile(g2, c);

    if (model.isOverlayImage()) {
      overlayLocImage(c);
    }

    g2.dispose();

    g.drawImage(tileImage, 0, 0, null);

    g.translate(-insets.left, -insets.top);

    if (Logger.isTraceEnabled()) {
      long now = System.currentTimeMillis();
      Logger.trace(tile.getId() + " Duration: " + (now - started) + " ms. Cp: " + tile.xyToString() + " O: " + model.getTileOrienation());
    }
  }

}
