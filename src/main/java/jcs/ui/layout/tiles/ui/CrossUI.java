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
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import jcs.commandStation.events.AccessoryEvent;
import jcs.entities.AccessoryBean;
import static jcs.entities.AccessoryBean.AccessoryValue.GREEN;
import static jcs.entities.AccessoryBean.AccessoryValue.RED;
import jcs.entities.TileBean;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.ui.layout.tiles.Cross;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import jcs.ui.layout.tiles.TileModel;
import org.tinylog.Logger;

public class CrossUI extends TileUI implements MouseListener, MouseMotionListener {

  public CrossUI() {
    super();
  }

  public static ComponentUI createUI(JComponent c) {
    return new CrossUI();
  }

  @Override
  public void installUI(JComponent c) {
    Tile tile = (Tile) c;
    tile.addMouseListener(this);
    tile.addMouseMotionListener(this);
  }

  @Override
  public void uninstallUI(JComponent c) {
    Tile tile = (Tile) c;
    tile.removeMouseListener(this);
    tile.removeMouseMotionListener(this);
  }

  protected void renderStraight(Graphics2D g2, Color color) {
    int xx = 0;
    int yy = 170;
    int w = RENDER_WIDTH;
    int h = 60;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillRect(xx, yy, w, h);
  }

  protected void renderRouteStraight(Graphics2D g2, Color color) {
    int xx = 0;
    int yy = 190;
    int w = RENDER_WIDTH;
    int h = 20;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillRect(xx, yy, w, h);
  }

  protected void renderStraight2(Graphics2D g2, Color color) {
    int xx = RENDER_WIDTH;
    int yy = 170;
    int w = RENDER_WIDTH;
    int h = 60;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillRect(xx, yy, w, h);
  }

  protected void renderRouteStraight2(Graphics2D g2, Color color) {
    int xx = RENDER_WIDTH;
    int yy = 190;
    int w = RENDER_WIDTH;
    int h = 20;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillRect(xx, yy, w, h);
  }

  protected void renderDiagonal(Graphics2D g2, Color color, JComponent c) {
    Tile tile = (Tile) c;
    TileBean.Direction direction = tile.getDirection();

    int[] xPoints, yPoints;
    if (TileBean.Direction.RIGHT.equals(direction)) {
      xPoints = new int[]{400, 400, 167, 230};
      yPoints = new int[]{170, 230, 0, 0};
    } else {
      xPoints = new int[]{400, 400, 170, 230};
      yPoints = new int[]{230, 170, 400, 400};
    }

    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  protected void renderRouteDiagonal(Graphics2D g2, Color color, JComponent c) {
    Tile tile = (Tile) c;
    TileBean.Direction direction = tile.getDirection();

    int[] xPoints, yPoints;
    if (TileBean.Direction.RIGHT.equals(direction)) {
      xPoints = new int[]{420, 400, 190, 210};
      yPoints = new int[]{210, 210, 0, 0};
    } else {
      xPoints = new int[]{400, 400, 190, 210};
      yPoints = new int[]{210, 190, 400, 400};
    }

    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  protected void renderDiagonal2(Graphics2D g2, Color color, JComponent c) {
    Tile tile = (Tile) c;
    TileBean.Direction direction = tile.getDirection();

    int[] xPoints, yPoints;
    if (TileBean.Direction.RIGHT.equals(direction)) {
      xPoints = new int[]{400, 400, 570, 630};
      yPoints = new int[]{170, 230, 400, 400};
    } else {
      xPoints = new int[]{400, 400, 570, 630};
      yPoints = new int[]{230, 170, 0, 0};
    }

    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  protected void renderRouteDiagonal2(Graphics2D g2, Color color, JComponent c) {
    Tile tile = (Tile) c;
    TileBean.Direction direction = tile.getDirection();

    int[] xPoints, yPoints;
    if (TileBean.Direction.RIGHT.equals(direction)) {
      xPoints = new int[]{400, 380, 590, 610};
      yPoints = new int[]{190, 190, 400, 400};
    } else {
      xPoints = new int[]{400, 380, 590, 610};
      yPoints = new int[]{210, 210, 0, 0};
    }

    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.setPaint(Color.cyan);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  @Override
  public void renderTile(Graphics2D g2, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();
    AccessoryBean.AccessoryValue accessoryValue = model.getAccessoryValue();

    if (accessoryValue == null) {
      accessoryValue = AccessoryBean.AccessoryValue.OFF;
    }

    switch (accessoryValue) {
      case RED -> {
        renderStraight2(g2, Cross.LIGHT_RED);
        renderDiagonal(g2, Cross.LIGHT_RED, c);
        renderStraight(g2, Cross.DARK_RED);
        renderDiagonal2(g2, Cross.DARK_RED, c);
      }
      case GREEN -> {
        renderDiagonal(g2, Cross.VERY_LIGHT_GREEN, c);
        renderDiagonal2(g2, Cross.VERY_LIGHT_GREEN, c);
        renderStraight(g2, Cross.DARK_GREEN);
        renderStraight2(g2, Cross.DARK_GREEN);
      }
      default -> {
        renderStraight(g2, trackColor);
        renderStraight2(g2, trackColor);
        renderDiagonal(g2, trackColor, c);
        renderDiagonal2(g2, trackColor, c);
      }
    }
  }

  @Override
  public void renderTileRoute(Graphics2D g2, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();
    TileBean.Direction direction = tile.getDirection();
    TileBean.Orientation orientation = tile.getOrientation();

    if (model.getIncomingSide() == null) {
      model.setIncomingSide(model.getTileOrienation());
    }
    TileBean.Orientation incomingSide = model.getIncomingSide();

    AccessoryBean.AccessoryValue routeValue = tile.getRouteValue();
    if (routeValue == null) {
      routeValue = AccessoryBean.AccessoryValue.OFF;
    }

    if (tile.isHorizontal()) {
      if (AccessoryBean.AccessoryValue.GREEN == routeValue && (TileBean.Orientation.NORTH == incomingSide || TileBean.Orientation.SOUTH == incomingSide)) {
        renderRouteDiagonal(g2, trackRouteColor, c);
        renderRouteDiagonal2(g2, trackRouteColor, c);
      } else if (AccessoryBean.AccessoryValue.GREEN == routeValue && (TileBean.Orientation.EAST == incomingSide || TileBean.Orientation.WEST == incomingSide)) {
        renderRouteStraight(g2, trackRouteColor);
        renderRouteStraight2(g2, trackRouteColor);
      } else if (AccessoryBean.AccessoryValue.RED == routeValue && TileBean.Orientation.EAST == orientation) {
        if (TileBean.Direction.RIGHT == direction && (TileBean.Orientation.EAST == incomingSide || TileBean.Orientation.NORTH == incomingSide)) {
          renderRouteStraight2(g2, trackRouteColor);
          renderRouteDiagonal(g2, trackRouteColor, c);
        } else if (TileBean.Direction.RIGHT == direction && (TileBean.Orientation.WEST == incomingSide || TileBean.Orientation.SOUTH == incomingSide)) {
          renderRouteDiagonal2(g2, trackRouteColor, c);
          renderRouteStraight(g2, trackRouteColor);
        } else if (TileBean.Direction.LEFT == direction && (TileBean.Orientation.EAST == incomingSide || TileBean.Orientation.SOUTH == incomingSide)) {
          renderRouteStraight2(g2, trackRouteColor);
          renderRouteDiagonal(g2, trackRouteColor, c);
        } else if (TileBean.Direction.LEFT == direction && (TileBean.Orientation.WEST == incomingSide || TileBean.Orientation.NORTH == incomingSide)) {
          renderRouteStraight(g2, trackColor);
          renderRouteDiagonal2(g2, trackColor, c);
        }
      } else if (AccessoryBean.AccessoryValue.RED == routeValue && TileBean.Orientation.WEST == orientation) {
        if (TileBean.Direction.RIGHT == direction && (TileBean.Orientation.EAST == incomingSide || TileBean.Orientation.NORTH == incomingSide)) {
          renderRouteStraight(g2, trackRouteColor);
          renderRouteDiagonal2(g2, trackRouteColor, c);
        } else if (TileBean.Direction.RIGHT == direction && (TileBean.Orientation.WEST == incomingSide || TileBean.Orientation.SOUTH == incomingSide)) {
          renderRouteDiagonal(g2, trackRouteColor, c);
          renderRouteStraight2(g2, trackRouteColor);
        } else if (TileBean.Direction.LEFT == direction && (TileBean.Orientation.EAST == incomingSide || TileBean.Orientation.SOUTH == incomingSide)) {
          renderRouteStraight(g2, trackRouteColor);
          renderRouteDiagonal2(g2, trackRouteColor, c);
        } else if (TileBean.Direction.LEFT == direction && (TileBean.Orientation.WEST == incomingSide || TileBean.Orientation.NORTH == incomingSide)) {
          renderRouteStraight2(g2, trackColor);
          renderRouteDiagonal(g2, trackColor, c);
        }
      }
    } else {
      if (AccessoryBean.AccessoryValue.GREEN == routeValue && (TileBean.Orientation.NORTH == incomingSide || TileBean.Orientation.SOUTH == incomingSide)) {
        renderRouteStraight(g2, trackRouteColor);
        renderRouteStraight2(g2, trackRouteColor);
      } else if (AccessoryBean.AccessoryValue.GREEN == routeValue && (TileBean.Orientation.EAST == incomingSide || TileBean.Orientation.WEST == incomingSide)) {
        renderRouteDiagonal(g2, trackRouteColor, c);
        renderRouteDiagonal2(g2, trackRouteColor, c);
      } else if (AccessoryBean.AccessoryValue.RED == routeValue && TileBean.Orientation.SOUTH == orientation) {
        if (TileBean.Direction.RIGHT == direction && (TileBean.Orientation.EAST == incomingSide || TileBean.Orientation.SOUTH == incomingSide)) {
          renderRouteStraight2(g2, trackRouteColor);
          renderRouteDiagonal(g2, trackRouteColor, c);
        } else if (TileBean.Direction.RIGHT == direction && (TileBean.Orientation.WEST == incomingSide || TileBean.Orientation.NORTH == incomingSide)) {
          renderRouteDiagonal2(g2, trackRouteColor, c);
          renderRouteStraight(g2, trackRouteColor);
        } else if (TileBean.Direction.LEFT == direction && (TileBean.Orientation.EAST == incomingSide || TileBean.Orientation.NORTH == incomingSide)) {
          renderRouteStraight(g2, trackRouteColor);
          renderRouteDiagonal2(g2, trackRouteColor, c);
        } else if (TileBean.Direction.LEFT == direction && (TileBean.Orientation.WEST == incomingSide || TileBean.Orientation.SOUTH == incomingSide)) {
          renderRouteStraight2(g2, trackColor);
          renderRouteDiagonal(g2, trackColor, c);
        }
      } else if (AccessoryBean.AccessoryValue.RED == routeValue && TileBean.Orientation.NORTH == orientation) {
        if (TileBean.Direction.RIGHT == direction && (TileBean.Orientation.EAST == incomingSide || TileBean.Orientation.SOUTH == incomingSide)) {
          renderRouteStraight(g2, trackRouteColor);
          renderRouteDiagonal2(g2, trackRouteColor, c);
        } else if (TileBean.Direction.RIGHT == direction && (TileBean.Orientation.WEST == incomingSide || TileBean.Orientation.NORTH == incomingSide)) {
          renderRouteDiagonal(g2, trackRouteColor, c);
          renderRouteStraight2(g2, trackRouteColor);
        } else if (TileBean.Direction.LEFT == direction && (TileBean.Orientation.EAST == incomingSide || TileBean.Orientation.NORTH == incomingSide)) {
          renderRouteStraight2(g2, trackRouteColor);
          renderRouteDiagonal(g2, trackRouteColor, c);
        } else if (TileBean.Direction.LEFT == direction && (TileBean.Orientation.WEST == incomingSide || TileBean.Orientation.SOUTH == incomingSide)) {
          renderRouteStraight(g2, trackColor);
          renderRouteDiagonal2(g2, trackColor, c);
        }
      }
    }
  }

  @Override
  protected void drawCenterPoint(Graphics2D g2d, Color color, double size, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();
    TileBean.Orientation tileOrientation = model.getTileOrienation();
    //int renderWidth = tile.getRenderWidth();
    //int renderHeight = tile.getRenderHeight();

    //A Cross has 1 alternate point
    //1st square holds the centerpoint
    //2nd square 
    double dX1, dX2, dY1, dY2;
    switch (tileOrientation) {
      case SOUTH -> {
        dX1 = renderWidth / 2 - size / 2;
        dY1 = renderHeight / 2 - renderHeight / 4 - size / 2;
        dX2 = renderWidth / 2 + renderWidth - size / 4;
        dY2 = renderHeight / 2 - renderHeight / 4 - size / 4;
      }
      case WEST -> {
        dX1 = renderWidth / 2 - renderWidth / 4 - size / 2;
        dY1 = renderHeight / 2 - size / 2;
        dX2 = renderWidth / 2 + renderWidth / 4 - size / 4;
        dY2 = renderHeight / 2 - size / 4;
      }
      case NORTH -> {
        dX1 = renderWidth / 2 - size / 2;
        dY1 = renderHeight / 2 - renderHeight / 4 - size / 2;
        dX2 = renderWidth / 2 + renderWidth - size / 4;
        dY2 = renderHeight / 2 - renderHeight / 4 - size / 4;
      }
      default -> {
        //East
        dX1 = renderWidth / 2 - renderWidth / 4 - size / 2;
        dY1 = renderHeight / 2 - size / 2;
        dX2 = renderWidth / 2 + renderWidth / 4 - size / 4;
        dY2 = renderHeight / 2 - size / 4;
      }
    }

    g2d.setColor(color);
    g2d.fill(new Ellipse2D.Double(dX1, dY1, size, size));
    g2d.fill(new Ellipse2D.Double(dX2, dY2, size / 2, size / 2));
  }

  @Override
  public void mousePressed(MouseEvent e) {
    //Only JCS is in CONTROL mode (readonly) activate accessory action events. 
    if (isControlMode((Component) e.getSource()) && e.getButton() == MouseEvent.BUTTON1) {
      Tile tile = (Tile) e.getSource();
      tile.setActive(!tile.isActive());

      if (tile.getAccessoryBean() != null) {
        AccessoryBean ab = tile.getAccessoryBean();
        ab.toggle();
        tile.setAccessoryValue(ab.getAccessoryValue());

        AccessoryEvent aae = new AccessoryEvent(ab);
        TileCache.enqueTileAction(aae);
        Logger.trace("Changing Tile " + tile.getId() + " Accessory " + ab.getId() + " to " + ab.getAccessoryValue().getValue() + "...");
      }
    } else {
      redispatchToParent(e);
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    redispatchToParent(e);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    redispatchToParent(e);
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    Tile tile = (Tile) e.getSource();
    String toolTipText = tile.getId();
    if (tile.getAccessoryBean() != null) {
      toolTipText = toolTipText + "; Id: " + tile.getAccessoryBean().getId();
    }
    tile.setToolTipText(toolTipText);
    redispatchToParent(e);
  }

  @Override
  public void mouseExited(MouseEvent e) {
    //Logger.trace("Mouse button " + e.getButton() + " @ (" + e.getXOnScreen() + ",");
    Tile tile = (Tile) e.getSource();
    tile.setToolTipText(null);
    redispatchToParent(e);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    //Logger.trace("Mouse button " + e.getButton() + " @ (" + e.getXOnScreen() + "," + e.getYOnScreen());
    redispatchToParent(e);
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    //Logger.trace("Mouse button " + e.getButton() + " @ (" + e.getXOnScreen() + "," + e.getYOnScreen());
    redispatchToParent(e);
  }

}
