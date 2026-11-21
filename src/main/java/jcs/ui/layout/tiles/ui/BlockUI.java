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
import static jcs.ui.layout.tiles.LayoutScale.GRID;
import jcs.ui.layout.tiles.Tile;
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

  @Override
  public void preRender(JComponent c) {
    renderCachedBlockImage(c);
  }

  private void renderCachedBlockImage(JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();

    // Render the block background, state, text, etc.
    drawTile(null, c);

    // Include the locomotive overlay in the cached image.
    if (model.isOverlayImage()) {
      overlayLocImage(c);
    }
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
    int tw = tileImage.getWidth();
    int th = tileImage.getHeight();

    TileBean.Orientation orientation = model.getTileOrienation();

    // Determine the scale from the actual rendered block image.
    // This keeps the overlay in sync with the image it is drawn onto.
    boolean horizontal = orientation == EAST || orientation == WEST;

    float scale = horizontal ? tw / (float) BLOCK_WIDTH : th / (float) BLOCK_HEIGHT;

    int sourceWidth = locImage.getWidth(null);
    int sourceHeight = locImage.getHeight(null);

    if (sourceWidth <= 0 || sourceHeight <= 0) {
      return;
    }

    float ratio = sourceHeight / (float) sourceWidth;

    // The original icon width of 45 pixels applies at 100% zoom.
    int iconWidth = Math.max(1, Math.round(LOC_IMAGE_WIDTH * scale));
    int iconHeight = Math.max(1, Math.round(iconWidth * ratio));
    locImage = locImage.getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);

    // Preserve the original positions:
    // 20 pixels for the near side and 55 for the far side at 100%.
    int nearOffset = Math.round(20 * scale);
    int farOffset = Math.round(55 * scale);
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
    int xx, yy, w, h;
    boolean reverseImage = LocomotiveBean.Direction.BACKWARDS == logicalDirection;
    //int w = locImage.getWidth(null);
    //int h = locImage.getHeight(null);

    switch (blockOrientation) {
      case SOUTH -> {
        locImage = ImageUtil.rotate(locImage, 90);

        w = locImage.getWidth(null);
        //h = locImage.getHeight(null);

        if ("+".equals(arrivalSide)) {
          yy = nearOffset;
          reverseImage = LocomotiveBean.Direction.FORWARDS == logicalDirection;
        } else {
          yy = farOffset;
        }

        xx = (tw - w) / 2;

        if (reverseImage) {
          locImage = ImageUtil.flipHorizontally(locImage);
        }
      }
      case WEST -> {
        locImage = ImageUtil.flipVertically(locImage);

        h = locImage.getHeight(null);

        if ("-".equals(arrivalSide)) {
          xx = nearOffset;
          reverseImage = LocomotiveBean.Direction.BACKWARDS == logicalDirection;
        } else {
          xx = farOffset;
          reverseImage = LocomotiveBean.Direction.FORWARDS == logicalDirection;
        }

        yy = (th - h) / 2;

        if (reverseImage) {
          locImage = ImageUtil.flipVertically(locImage);
        }
      }
      case NORTH -> {
        locImage = ImageUtil.rotate(locImage, 270);
        locImage = ImageUtil.flipVertically(locImage);

        w = locImage.getWidth(null);
        //h = locImage.getHeight(null);

        if ("-".equals(arrivalSide)) {
          yy = nearOffset;
          reverseImage = LocomotiveBean.Direction.BACKWARDS == logicalDirection;
        } else {
          yy = farOffset;
          reverseImage = LocomotiveBean.Direction.FORWARDS == logicalDirection;
        }

        xx = (tw - w) / 2;

        if (reverseImage) {
          locImage = ImageUtil.flipHorizontally(locImage);
        }
      }
      default -> {
        // EAST
        h = locImage.getHeight(null);
        if ("+".equals(arrivalSide)) {
          xx = nearOffset;
          reverseImage = LocomotiveBean.Direction.FORWARDS == logicalDirection;
        } else {
          xx = farOffset;
        }

        yy = (th - h) / 2;

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

  /**
   *
   * @param tile
   * @return
   */
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

//  @Override
//  public void paint(Graphics g, JComponent c) {
//    Tile tile = (Tile) c;
//    TileModel model = tile.getModel();
//
//    long started = System.currentTimeMillis();
//    //  We don't want to paint inside the insets or borders.
//    Insets insets = c.getInsets();
//    g.translate(insets.left, insets.top);
//
//    Graphics2D g2 = (Graphics2D) g.create();
//    drawTile(g2, c);
//
//    if (model.isOverlayImage()) {
//      overlayLocImage(c);
//    }
//
//    g2.dispose();
//
//    g.drawImage(tileImage, 0, 0, null);
//
//    g.translate(-insets.left, -insets.top);
//
//    if (Logger.isTraceEnabled()) {
//      long now = System.currentTimeMillis();
//      Logger.trace(tile.getId() + " Duration: " + (now - started) + " ms. Cp: " + tile.xyToString() + " O: " + model.getTileOrienation());
//    }
//  }
  @Override
  public void paint(Graphics g, JComponent c) {
    // Only rebuild the image when necessary.
    if (tileImage == null || imageDirty) {
      renderCachedBlockImage(c);
    }

    Insets insets = c.getInsets();

    // Draw the cached image.
    g.drawImage(tileImage, insets.left, insets.top, null);
  }

}
