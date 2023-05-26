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

import jcs.ui.layout.Tile;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import jcs.controller.cs3.events.AccessoryMessageEvent;
import jcs.entities.AccessoryBean;
import jcs.entities.TileBean;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.SignalValue;
import static jcs.entities.enums.SignalValue.Hp0;
import static jcs.entities.enums.SignalValue.Hp0Sh1;
import static jcs.entities.enums.SignalValue.Hp1;
import static jcs.entities.enums.SignalValue.Hp2;
import jcs.entities.enums.SignalType;
import jcs.entities.enums.TileType;
import jcs.trackservice.events.AccessoryListener;

public class Signal extends Straight implements Tile, AccessoryListener {

  private static int idSeq;

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

  @Override
  protected final String getNewId() {
    idSeq++;
    return "si-" + idSeq;
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

  /**
   * Render a Signal with 2 lights
   *
   * @param g2d the graphics context
   */
  protected void renderSignal2(Graphics2D g2d) {
    int rx = GRID;
    int ry = GRID + 5;
    int rw = 18;
    int rh = 10;
    int l1x = GRID + 2;
    int l1y = GRID + 7;
    int l2x = GRID + 10;
    int l2y = GRID + 7;

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
      default -> {
      }
    }

    g2d.setStroke(new BasicStroke(10f));
    g2d.setPaint(Color.darkGray);
    g2d.fillRoundRect(rx, ry, rw, rh, 0, 0);

    g2d.setPaint(color1);
    g2d.fillOval(l1x, l1y, 5, 5);
    g2d.setPaint(color2);
    g2d.fillOval(l2x, l2y, 5, 5);
  }

  //TODO
  protected void renderSignal3(Graphics2D g2d) {
    renderSignal4(g2d);
  }

  /**
   * Render a entry Signal which can show 4 light images
   *
   * @param g2d the Graphics context
   */
  protected void renderSignal4(Graphics2D g2d) {
    int rx = GRID - 5;
    int ry = GRID + 5;
    int rw = 24;
    int rh = 12;
    int c1x = GRID + 14;
    int c1y = GRID + 6;
    int c2x = GRID + 9;
    int c2y = GRID + 6;
    int c3x = GRID + 9;
    int c3y = GRID + 11;
    int c4x = GRID + 5;
    int c4y = GRID + 12;
    int c5x = GRID + 2;
    int c5y = GRID + 7;
    int c6x = GRID - 4;
    int c6y = GRID + 6;

    //Initialize all "lights"
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
    g2d.fillRoundRect(rx, ry, rw, rh, 0, 0);

    g2d.setPaint(color1);
    g2d.fillOval(c1x, c1y, 5, 5);
    g2d.setPaint(color2);
    g2d.fillOval(c2x, c2y, 5, 5);
    g2d.setPaint(color3);
    g2d.fillOval(c3x, c3y, 5, 5);
    g2d.setPaint(color4);
    g2d.fillOval(c4x, c4y, 3, 3);
    g2d.setPaint(color5);
    g2d.fillOval(c5x, c5y, 3, 3);
    g2d.setPaint(color6);
    g2d.fillOval(c6x, c6y, 5, 5);
  }

  /**
   * Render a midget Signal
   *
   * @param g2d the Graphics context
   */
  protected void renderSignal2m(Graphics2D g2d) {
    int rx = GRID + 5;
    int ry = GRID + 5;
    int rw = 12;
    int rh = 12;
    int c1x = GRID + 12;
    int c1y = GRID + 5;
    int c2x = GRID + 12;
    int c2y = GRID + 12;
    int c3x = GRID + 12;
    int c3y = GRID + 10;
    int c4x = GRID + 6;
    int c4y = GRID + 6;

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
      default -> {
      }
    }

    g2d.setStroke(new BasicStroke(10f));
    g2d.setPaint(Color.darkGray);
    g2d.fillRoundRect(rx, ry, rw, rh, 0, 0);

    g2d.setPaint(color1);
    g2d.fillOval(c1x, c1y, 5, 5);

    g2d.setPaint(color2);
    g2d.fillOval(c2x, c2y, 5, 5);

    g2d.setPaint(color3);
    g2d.fillOval(c3x, c3y, 4, 4);

    g2d.setPaint(color4);
    g2d.fillOval(c4x, c4y, 4, 4);
  }

  @Override
  public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
    Graphics2D g2d = (Graphics2D) g2.create();

    renderStraight(g2d, trackColor, backgroundColor);

    switch (signalType) {
      case HP012 ->
        renderSignal3(g2d);
      case HP012SH1 ->
        renderSignal4(g2d);
      case HP0SH1 ->
        renderSignal2m(g2d);
      default -> //HP01
        renderSignal2(g2d);
    }

    g2d.dispose();
  }

  @Override
  public void onChange(AccessoryMessageEvent event) {
    if (this.getAccessoryBean() != null && this.getAccessoryId().equals(event.getAccessoryBean().getId())) {
      this.setSignalValue(event.getAccessoryBean().getSignalValue());
      repaintTile();
    }
  }

}
