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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import jcs.entities.TileBean;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.ui.layout.LayoutCanvas;
import jcs.ui.layout.tiles.Tile;
import static jcs.ui.layout.tiles.Tile.DEFAULT_BACKGROUND_COLOR;
import static jcs.ui.layout.tiles.Tile.DEFAULT_TRACK_COLOR;
import static jcs.ui.layout.tiles.Tile.GRID;
import jcs.ui.layout.tiles.TileModel;
import org.imgscalr.Scalr;
import org.tinylog.Logger;

/**
 * The drawing functionality of a Tile.
 */
public abstract class TileUI extends ComponentUI {

  protected static final int RENDER_GRID = GRID * 10;
  protected static final int RENDER_WIDTH = RENDER_GRID * 2;
  protected static final int RENDER_HEIGHT = RENDER_GRID * 2;

  public static final String UI_CLASS_ID = "jcs.ui.layout.tiles.ui.TileUI";

  protected int renderWidth;
  protected int renderHeight;

  protected Color backgroundColor;
  protected Color trackColor;
  protected Color trackRouteColor;

  protected BufferedImage tileImage;

  protected TileUI() {
    this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
    this.trackColor = DEFAULT_TRACK_COLOR;
  }

  private void setRenderSize(JComponent c) {
    Tile tile = (Tile) c;

    switch (tile.getTileType()) {
      case CROSS -> {
        if (tile.isHorizontal()) {
          this.renderWidth = RENDER_GRID * 4;
          this.renderHeight = RENDER_GRID * 2;
        } else {
          this.renderWidth = RENDER_GRID * 2;
          this.renderHeight = RENDER_GRID * 4;
        }
      }
      case BLOCK -> {
        if (tile.isHorizontal()) {
          this.renderWidth = RENDER_WIDTH * 3;
          this.renderHeight = RENDER_HEIGHT;
        } else {
          this.renderWidth = RENDER_WIDTH;
          this.renderHeight = RENDER_HEIGHT * 3;
        }
      }
      default -> {
        this.renderWidth = RENDER_WIDTH;
        this.renderHeight = RENDER_HEIGHT;
      }
    }
  }

  abstract void renderTile(Graphics2D g2d, JComponent c);

  abstract void renderTileRoute(Graphics2D g2d, JComponent c);

  public int getRenderWidth() {
    return renderWidth;
  }

  public void setRenderWidth(int renderWidth) {
    this.renderWidth = renderWidth;
  }

  public int getRenderHeight() {
    return renderHeight;
  }

  public void setRenderHeight(int renderHeight) {
    this.renderHeight = renderHeight;
  }

  public Color getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public Color getTrackColor() {
    return trackColor;
  }

  public void setTrackColor(Color trackColor) {
    this.trackColor = trackColor;
  }

  public Color getTrackRouteColor() {
    return trackRouteColor;
  }

  public void setTrackRouteColor(Color trackRouteColor) {
    this.trackRouteColor = trackRouteColor;
  }

  protected BufferedImage createImage() {
    return new BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_RGB);
  }

  public BufferedImage getTileImage() {
    return tileImage;
  }

  public void drawTile(Graphics2D g2d, JComponent c) {
    // by default and image is rendered in the EAST orientation
    setRenderSize(c);

    Tile tile = (Tile) c;
    TileModel model = ((Tile) c).getModel();
    TileBean.Orientation tileOrientation = model.getTileOrienation();
    BufferedImage bf = createImage();
    Graphics2D g2di = bf.createGraphics();

    //Avoid errors
    if (model.isShowRoute() && model.getIncomingSide() == null) {
      model.setIncomingSide(tileOrientation);
    }

    if (model.isSelected()) {
      g2di.setBackground(model.getSelectedColor());
    } else {
      g2di.setBackground(backgroundColor);
    }

    g2di.clearRect(0, 0, renderWidth, renderHeight);
    int ox = 0, oy = 0;

    AffineTransform trans = new AffineTransform();
    switch (tileOrientation) {
      case SOUTH -> {
        trans.rotate(Math.PI / 2, renderWidth / 2, renderHeight / 2);
        ox = (renderHeight - renderWidth) / 2;
        oy = (renderWidth - renderHeight) / 2;
        trans.translate(-ox, -oy);
      }
      case WEST -> {
        trans.rotate(Math.PI, renderWidth / 2, renderHeight / 2);
        trans.translate(ox, oy);
      }
      case NORTH -> {
        trans.rotate(-Math.PI / 2, renderWidth / 2, renderHeight / 2);
        ox = (renderHeight - renderWidth) / 2;
        oy = (renderWidth - renderHeight) / 2;
        trans.translate(-ox, -oy);
      }
      default -> {
        trans.rotate(0.0, renderWidth / 2, renderHeight / 2);
        trans.translate(ox, oy);
      }
    }

    g2di.setTransform(trans);

    renderTile(g2di, c);

    if (model.isShowRoute()) {
      renderTileRoute(g2di, c);
    }

    if (model.isShowCenter()) {
      drawCenterPoint(g2di, c);
    }

    // Scale the image back...
    if (model.isScaleImage()) {
      tileImage = Scalr.resize(bf, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, tile.getWidth(), tile.getHeight(), Scalr.OP_ANTIALIAS);
    } else {
      tileImage = bf;
    }
    g2di.dispose();
  }

  /**
   * Render a tile image Always starts at (0,0) used the default width and height
   *
   * @param g2 the Graphic context
   * @param c
   */
  protected void drawName(Graphics2D g2, JComponent c) {
  }

  protected void drawCenterPoint(Graphics2D g2d, JComponent c) {
    drawCenterPoint(g2d, Color.magenta, c);
  }

  protected void drawCenterPoint(Graphics2D g2, Color color, JComponent c) {
    drawCenterPoint(g2, color, 60, c);
  }

  protected void drawCenterPoint(Graphics2D g2d, Color color, double size, JComponent c) {
    double dX = (renderWidth / 2 - size / 2);
    double dY = (renderHeight / 2 - size / 2);

    g2d.setColor(color);
    g2d.fill(new Ellipse2D.Double(dX, dY, size, size));
  }

  protected static void drawRotate(Graphics2D g2d, double x, double y, int angle, String text) {
    g2d.translate((float) x, (float) y);
    g2d.rotate(Math.toRadians(angle));
    g2d.drawString(text, 0, 0);
    g2d.rotate(-Math.toRadians(angle));
    g2d.translate(-x, -y);
  }

  @Override
  public void paint(Graphics g, JComponent c) {
    long started = System.currentTimeMillis();
    //  We don't want to paint inside the insets or borders.
    Insets insets = c.getInsets();
    g.translate(insets.left, insets.top);

    Graphics2D g2 = (Graphics2D) g.create();

    drawTile(g2, c);
    g2.dispose();

    g.drawImage(tileImage, 0, 0, null);
    g.translate(-insets.left, -insets.top);

    if (Logger.isTraceEnabled() && 1 == 2) {
      Tile tile = (Tile) c;
      TileModel model = tile.getModel();
      long now = System.currentTimeMillis();
      Logger.trace(tile.getId() + " Duration: " + (now - started) + " ms. Cp: " + tile.xyToString() + " O: " + model.getTileOrienation());
    }
  }

  protected void redispatchToParent(MouseEvent e) {
    Component source = (Component) e.getSource();
    Point cp = SwingUtilities.convertPoint(source, e.getPoint(), source.getParent());
    MouseEvent me = new MouseEvent(source, e.getID(), e.getWhen(), e.getModifiersEx(), cp.x, cp.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
    //Logger.trace("ME @ (" + me.getXOnScreen() + "," + me.getYOnScreen() + ") PE @ (" + cp.x + "," + cp.y + ")");
    source.getParent().dispatchEvent(me);
  }

  protected boolean isControlMode(Component c) {
    if (c.getParent() != null && c.getParent() instanceof LayoutCanvas) {
      return ((LayoutCanvas) c.getParent()).isReadonly();
    } else {
      return false;
    }
  }

}
