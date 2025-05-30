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
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import jcs.commandStation.events.AccessoryEvent;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.SignalType;
import static jcs.entities.AccessoryBean.SignalType.HP012;
import static jcs.entities.AccessoryBean.SignalType.HP012SH1;
import static jcs.entities.AccessoryBean.SignalType.HP0SH1;
import jcs.entities.AccessoryBean.SignalValue;
import static jcs.entities.AccessoryBean.SignalValue.Hp0;
import static jcs.entities.AccessoryBean.SignalValue.Hp0Sh1;
import static jcs.entities.AccessoryBean.SignalValue.Hp1;
import static jcs.entities.AccessoryBean.SignalValue.Hp2;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import jcs.ui.layout.tiles.TileModel;
import org.tinylog.Logger;

public class SignalUI extends StraightUI implements MouseListener, MouseMotionListener {

  public SignalUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new SignalUI();
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

  /**
   * Render a Signal with 2 lights
   *
   * @param g2d the graphics context
   * @param c
   */
  protected void renderSignal2(Graphics2D g2d, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();
    SignalValue signalValue = model.getSignalValue();

    int rx = RENDER_GRID;
    int ry = RENDER_GRID + 60;
    int rw = 180;
    int rh = 100;
    int l1x = RENDER_GRID + 20;
    int l1y = RENDER_GRID + 80;
    int l2x = RENDER_GRID + 100;
    int l2y = RENDER_GRID + 80;

    Color color1 = Color.gray;
    Color color2 = Color.gray;

    if (signalValue == null) {
      signalValue = AccessoryBean.SignalValue.OFF;
    }

    switch (signalValue) {
      case Hp0 -> {
        color1 = Color.red;
        color2 = Color.gray;
      }
      case Hp1 -> {
        color1 = Color.gray;
        color2 = Color.green;
      }
      default -> {
      }
    }

    g2d.setStroke(new BasicStroke(10f));
    g2d.setPaint(Color.darkGray);
    g2d.fillRoundRect(rx, ry, rw, rh, 30, 30);

    g2d.setPaint(color1);
    g2d.fillOval(l1x, l1y, 60, 60);
    g2d.setPaint(color2);
    g2d.fillOval(l2x, l2y, 60, 60);
  }

  protected void renderSignal3(Graphics2D g2d, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();
    SignalValue signalValue = model.getSignalValue();

    int rx = RENDER_GRID;
    int ry = RENDER_GRID + 60;
    int rw = 180;
    int rh = 100;

    int c1x = RENDER_GRID + 130;
    int c1y = RENDER_GRID + 115;

    int c2x = RENDER_GRID + 10;
    int c2y = RENDER_GRID + 115;

    int c3x = RENDER_GRID + 10;
    int c3y = RENDER_GRID + 65;

    // Initialize all "lights"
    Color color1 = Color.gray;
    Color color2 = Color.gray;
    Color color3 = Color.gray;

    if (signalValue == null) {
      signalValue = AccessoryBean.SignalValue.OFF;
    }

    switch (signalValue) {
      case Hp0 -> {
        color3 = Color.red;
      }
      case Hp1 ->
        color1 = Color.green;
      case Hp2 -> {
        color1 = Color.green;
        color2 = Color.yellow;
      }
      default -> {
      }
    }

    g2d.setStroke(new BasicStroke(10f));
    g2d.setPaint(Color.darkGray);
    g2d.fillRoundRect(rx, ry, rw, rh, 30, 30);

    g2d.setPaint(color1);
    g2d.fillOval(c1x, c1y, 40, 40);

    g2d.setPaint(color2);
    g2d.fillOval(c2x, c2y, 40, 40);

    g2d.setPaint(color3);
    g2d.fillOval(c3x, c3y, 40, 40);
  }

  /**
   * Render a entry Signal which can show 4 light images
   *
   * @param g2d the Graphics context
   * @param c
   */
  protected void renderSignal4(Graphics2D g2d, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();
    SignalValue signalValue = model.getSignalValue();

    int rx = RENDER_GRID - 50;
    int ry = RENDER_GRID + 50;
    int rw = 240;
    int rh = 120;
    int c1x = RENDER_GRID + 140;
    int c1y = RENDER_GRID + 60;

    int c2x = RENDER_GRID + 90;
    int c2y = RENDER_GRID + 60;

    int c3x = RENDER_GRID + 90;
    int c3y = RENDER_GRID + 120;

    int c4x = RENDER_GRID + 60;
    int c4y = RENDER_GRID + 130;

    int c5x = RENDER_GRID + 10;
    int c5y = RENDER_GRID + 70;

    int c6x = RENDER_GRID - 40;
    int c6y = RENDER_GRID + 60;

    // Initialize all "lights"
    Color color1 = Color.gray;
    Color color2 = Color.gray;
    Color color3 = Color.gray;
    Color color4 = Color.gray;
    Color color5 = Color.gray;
    Color color6 = Color.gray;

    if (signalValue == null) {
      signalValue = AccessoryBean.SignalValue.OFF;
    }

    switch (signalValue) {
      case Hp0 -> {
        color2 = Color.red;
        color3 = Color.red;
      }
      case Hp1 ->
        color1 = Color.green;
      case Hp2 -> {
        color1 = Color.green;
        color6 = Color.yellow;
      }
      case Hp0Sh1 -> {
        color2 = Color.red;
        color4 = Color.white;
        color5 = Color.white;
      }
      default -> {
      }
    }

    g2d.setStroke(new BasicStroke(10f));
    g2d.setPaint(Color.darkGray);
    g2d.fillRoundRect(rx, ry, rw, rh, 30, 30);

    g2d.setPaint(color1);
    g2d.fillOval(c1x, c1y, 40, 40);
    g2d.setPaint(color2);
    g2d.fillOval(c2x, c2y, 40, 40);
    g2d.setPaint(color3);
    g2d.fillOval(c3x, c3y, 40, 40);
    g2d.setPaint(color4);
    g2d.fillOval(c4x, c4y, 20, 20);
    g2d.setPaint(color5);
    g2d.fillOval(c5x, c5y, 20, 20);
    g2d.setPaint(color6);
    g2d.fillOval(c6x, c6y, 40, 40);
  }

  /**
   * Render a midget Signal
   *
   * @param g2d the Graphics context
   * @param c
   */
  protected void renderSignal2m(Graphics2D g2d, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();
    SignalValue signalValue = model.getSignalValue();

    int[] xps = new int[]{RENDER_GRID + 80, +RENDER_GRID + 150, +RENDER_GRID + 170, RENDER_GRID + 170, +RENDER_GRID + 150, RENDER_GRID + 80};

    int[] yps = new int[]{RENDER_GRID + 60, RENDER_GRID + 60, RENDER_GRID + 80, +RENDER_GRID + 160, +RENDER_GRID + 180, RENDER_GRID + 180};

    Polygon signalOutline = new Polygon(xps, yps, xps.length);

    int c1x = RENDER_GRID + 130;
    int c1y = RENDER_GRID + 70;

    int c2x = RENDER_GRID + 130;
    int c2y = RENDER_GRID + 140;

    int c3x = RENDER_GRID + 130;
    int c3y = RENDER_GRID + 105;

    int c4x = RENDER_GRID + 85;
    int c4y = RENDER_GRID + 70;

    Color color1 = Color.gray;
    Color color2 = Color.gray;
    Color color3 = Color.gray;
    Color color4 = Color.gray;

    if (signalValue == null) {
      signalValue = AccessoryBean.SignalValue.OFF;
    }

    switch (signalValue) {
      case Hp0 -> {
        color1 = Color.red;
        color2 = Color.red;
      }
      case Hp1 -> {
        color3 = Color.white;
        color4 = Color.white;
      }
      default -> {
      }
    }

    g2d.setStroke(new BasicStroke(10f));
    g2d.setPaint(Color.darkGray);

    g2d.fillPolygon(signalOutline);

    g2d.setPaint(color1);
    g2d.fillOval(c1x, c1y, 30, 30);

    g2d.setPaint(color2);
    g2d.fillOval(c2x, c2y, 30, 30);

    g2d.setPaint(color3);
    g2d.fillOval(c3x, c3y, 30, 30);

    g2d.setPaint(color4);
    g2d.fillOval(c4x, c4y, 30, 30);
  }

  @Override
  public void renderTile(Graphics2D g2, JComponent c) {
    Tile tile = (Tile) c;
    SignalType signalType = tile.getSignalType();

    Graphics2D g2d = (Graphics2D) g2.create();
    renderStraight(g2d, c);

    if (signalType == null) {
      signalType = AccessoryBean.SignalType.NONE;
    }

    switch (signalType) {
      case HP012 ->
        renderSignal3(g2d, c);
      case HP012SH1 ->
        renderSignal4(g2d, c);
      case HP0SH1 ->
        renderSignal2m(g2d, c);
      default ->
        renderSignal2(g2d, c);
    }

    g2d.dispose();
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
        tile.setSignalValue(ab.getSignalValue());

        AccessoryEvent aae = new AccessoryEvent(ab);
        TileCache.enqueTileAction(aae);
        Logger.trace("Changing Tile " + tile.getId() + " Accessory " + ab.getId() + " to " + ab.getSignalValue().getSignalValue() + "...");
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
    Tile tile = (Tile) e.getSource();
    tile.setToolTipText(null);
    redispatchToParent(e);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    redispatchToParent(e);
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    redispatchToParent(e);
  }

}
