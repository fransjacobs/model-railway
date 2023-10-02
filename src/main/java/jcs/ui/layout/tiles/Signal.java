/*
 * Copyright 2023 Frans Jacobs.
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

import static jcs.entities.enums.SignalType.HP012;
import static jcs.entities.enums.SignalType.HP012SH1;
import static jcs.entities.enums.SignalType.HP0SH1;
import static jcs.entities.enums.SignalValue.Hp0;
import static jcs.entities.enums.SignalValue.Hp0Sh1;
import static jcs.entities.enums.SignalValue.Hp1;
import static jcs.entities.enums.SignalValue.Hp2;
import static jcs.ui.layout.tiles.Tile.RENDER_GRID;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import jcs.controller.events.AccessoryEvent;
import jcs.controller.events.AccessoryEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.TileBean;
import jcs.entities.enums.SignalType;
import jcs.entities.enums.SignalValue;

public class Signal extends Straight implements Tile, AccessoryEventListener {

  private SignalValue signalValue;
  private SignalType signalType;

  public Signal(TileBean tileBean) {
    super(tileBean);
    if (tileBean.getAccessoryBean() != null) {
      AccessoryBean ab = tileBean.getAccessoryBean();
      this.signalType = SignalType.getSignalType(ab.getType());
    }
  }

  public Signal(Orientation orientation, int x, int y) {
    this(orientation, new Point(x, y), SignalType.HP01);
  }

  public Signal(Orientation orientation, int x, int y, SignalType signalType) {
    this(orientation, new Point(x, y), signalType);
  }

  public Signal(Orientation orientation, Point center) {
    this(orientation, center, SignalType.HP01);
  }

  public Signal(Orientation orientation, Point center, SignalType signalType) {
    super(orientation, center);
    this.signalType = signalType;
    this.signalValue = SignalValue.OFF;
    this.type = TileType.SIGNAL.getTileType();
  }

  public SignalValue getSignalValue() {
    return signalValue;
  }

  @Override
  public SignalType getSignalType() {
    return signalType;
  }

  public void setSignalValue(SignalValue signalValue) {
    this.signalValue = signalValue;
  }

  @Override
  public void setAccessoryBean(AccessoryBean accessoryBean) {
    this.accessoryBean = accessoryBean;
    if (accessoryBean != null) {
      this.accessoryId = accessoryBean.getId();
      this.signalValue = accessoryBean.getSignalValue();
      this.signalType = SignalType.getSignalType(accessoryBean.getType());
    } else {
      this.accessoryId = null;
      this.signalType = SignalType.NONE;
      this.signalValue = SignalValue.OFF;
    }
  }

  /**
   * Render a Signal with 2 lights
   *
   * @param g2d the graphics context
   */
  protected void renderSignal2(Graphics2D g2d) {
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

    if (this.signalValue == null) {
      this.signalValue = SignalValue.OFF;
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
      default -> {}
    }

    g2d.setStroke(new BasicStroke(10f));
    g2d.setPaint(Color.darkGray);
    g2d.fillRoundRect(rx, ry, rw, rh, 30, 30);

    g2d.setPaint(color1);
    g2d.fillOval(l1x, l1y, 60, 60);
    g2d.setPaint(color2);
    g2d.fillOval(l2x, l2y, 60, 60);
  }

  protected void renderSignal3(Graphics2D g2d) {
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

    if (this.signalValue == null) {
      this.signalValue = SignalValue.OFF;
    }

    switch (this.signalValue) {
      case Hp0 -> {
        color3 = Color.red;
      }
      case Hp1 -> color1 = Color.green;
      case Hp2 -> {
        color1 = Color.green;
        color2 = Color.yellow;
      }
      default -> {}
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
   */
  protected void renderSignal4(Graphics2D g2d) {
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

    if (this.signalValue == null) {
      this.signalValue = SignalValue.OFF;
    }

    switch (this.signalValue) {
      case Hp0 -> {
        color2 = Color.red;
        color3 = Color.red;
      }
      case Hp1 -> color1 = Color.green;
      case Hp2 -> {
        color1 = Color.green;
        color6 = Color.yellow;
      }
      case Hp0Sh1 -> {
        color2 = Color.red;
        color4 = Color.white;
        color5 = Color.white;
      }
      default -> {}
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
   */
  protected void renderSignal2m(Graphics2D g2d) {
    int rx = RENDER_GRID + 50;
    int ry = RENDER_GRID + 50;
    int rw = 120;
    int rh = 120;

    int[] xps =
        new int[] {
          RENDER_GRID + 100,
          +RENDER_GRID + 140,
          +RENDER_GRID + 160,
          RENDER_GRID + 160,
          +RENDER_GRID + 140,
          RENDER_GRID + 100
        };
    int[] yps =
        new int[] {
          RENDER_GRID + 80,
          RENDER_GRID + 80,
          RENDER_GRID + 100,
          +RENDER_GRID + 140,
          +RENDER_GRID + 160,
          RENDER_GRID + 160
        };

    Polygon signalOutline = new Polygon(xps, yps, xps.length);

    int c1x = RENDER_GRID + 130;
    int c1y = RENDER_GRID + 87;

    int c2x = RENDER_GRID + 130;
    int c2y = RENDER_GRID + 138;

    int c3x = RENDER_GRID + 130;
    int c3y = RENDER_GRID + 112;

    int c4x = RENDER_GRID + 105;
    int c4y = RENDER_GRID + 87;

    Color color1 = Color.gray;
    Color color2 = Color.gray;
    Color color3 = Color.gray;
    Color color4 = Color.gray;

    if (this.signalValue == null) {
      this.signalValue = SignalValue.OFF;
    }

    switch (this.signalValue) {
      case Hp0 -> {
        color1 = Color.red;
        color2 = Color.red;
      }
      case Hp1 -> {
        color3 = Color.white;
        color4 = Color.white;
      }
      default -> {}
    }

    g2d.setStroke(new BasicStroke(10f));
    g2d.setPaint(Color.darkGray);

    g2d.fillPolygon(signalOutline);

    g2d.setPaint(color1);
    g2d.fillOval(c1x, c1y, 15, 15);

    g2d.setPaint(color2);
    g2d.fillOval(c2x, c2y, 15, 15);

    g2d.setPaint(color3);
    g2d.fillOval(c3x, c3y, 15, 15);

    g2d.setPaint(color4);
    g2d.fillOval(c4x, c4y, 15, 15);
  }

  @Override
  public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
    Graphics2D g2d = (Graphics2D) g2.create();
    renderStraight(g2d, trackColor, backgroundColor);

    if (signalType == null) {
      signalType = SignalType.NONE;
    }

    switch (signalType) {
      case HP012 -> renderSignal3(g2d);
      case HP012SH1 -> renderSignal4(g2d);
      case HP0SH1 -> renderSignal2m(g2d);
      default -> renderSignal2(g2d);
    }

    g2d.dispose();
  }

  @Override
  public void onAccessoryChange(AccessoryEvent event) {
    if (this.getAccessoryBean() != null && event.isEventFor(accessoryBean)) {
      this.setSignalValue(event.getAccessoryBean().getSignalValue());
      repaintTile();
    }
  }
}
