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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import jcs.ui.layout.tiles.enums.Direction;
import jcs.entities.enums.SignalType;
import jcs.entities.enums.SignalValue;
import jcs.ui.layout.Tile;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class TileTester extends JFrame {

  private final Tile tileEast;
  private final Tile tileSouth;
  private final Tile tileWest;
  private final Tile tileNorth;

  private final Tile tileEast1;
  private final Tile tileSouth1;
  private final Tile tileWest1;
  private final Tile tileNorth1;

  private final Tile tileEast2;
  private final Tile tileSouth2;
  private final Tile tileWest2;
  private final Tile tileNorth2;

  private final Tile tileEast3;
  private final Tile tileSouth3;
  private final Tile tileWest3;
  private final Tile tileNorth3;

  private final Tile tileEast4;
  private final Tile tileSouth4;
  private final Tile tileWest4;
  private final Tile tileNorth4;

  private final Tile tileEast5;
  private final Tile tileSouth5;
  private final Tile tileWest5;
  private final Tile tileNorth5;

  private final Tile tileEast6;
  private final Tile tileSouth6;
  private final Tile tileWest6;
  private final Tile tileNorth6;

  private final Tile tileEast7;
  private final Tile tileSouth7;
  private final Tile tileWest7;
  private final Tile tileNorth7;

  private final Tile tileEast8;
  private final Tile tileSouth8;
  private final Tile tileWest8;
  private final Tile tileNorth8;

  @SuppressWarnings("OverridableMethodCallInConstructor")
  public TileTester(String title) {
    super(title);

    tileEast = new Straight(Orientation.EAST, 70, 50);
    tileSouth = new Straight(Orientation.SOUTH, 160, 50);
    tileWest = new Straight(Orientation.WEST, 250, 50);
    tileNorth = new Straight(Orientation.NORTH, 340, 50);

    tileEast1 = new Curved(Orientation.EAST, 70, 100);
    tileSouth1 = new Curved(Orientation.SOUTH, 160, 100);
    tileWest1 = new Curved(Orientation.WEST, 250, 100);
    tileNorth1 = new Curved(Orientation.NORTH, 340, 100);

    tileEast2 = new Block(Orientation.EAST, 70, 190);
    tileSouth2 = new Block(Orientation.SOUTH, 160, 190);
    tileWest2 = new Block(Orientation.WEST, 250, 190);
    tileNorth2 = new Block(Orientation.NORTH, 340, 190);

    tileEast3 = new Sensor(Orientation.EAST, 70, 280);
    tileSouth3 = new Sensor(Orientation.SOUTH, 160, 280);
    tileWest3 = new Sensor(Orientation.WEST, 250, 280);
    tileNorth3 = new Sensor(Orientation.NORTH, 340, 280);

    tileEast4 = new Switch(Orientation.EAST, Direction.RIGHT, 70, 330);
    tileSouth4 = new Switch(Orientation.SOUTH, Direction.RIGHT, 160, 330);
    ((Switch) tileSouth4).setValue(AccessoryValue.RED);
    tileWest4 = new Switch(Orientation.WEST, Direction.RIGHT, 250, 330);
    tileNorth4 = new Switch(Orientation.NORTH, Direction.RIGHT, 340, 330);
    ((Switch) tileNorth4).setValue(AccessoryValue.GREEN);

    tileEast5 = new Switch(Orientation.EAST, Direction.LEFT, 70, 380);
    ((Switch) tileEast5).setValue(AccessoryValue.GREEN);
    tileSouth5 = new Switch(Orientation.SOUTH, Direction.LEFT, 160, 380);
    tileWest5 = new Switch(Orientation.WEST, Direction.LEFT, 250, 380);
    ((Switch) tileWest5).setValue(AccessoryValue.RED);
    tileNorth5 = new Switch(Orientation.NORTH, Direction.LEFT, 340, 380);

    tileEast6 = new Cross(Orientation.EAST, Direction.RIGHT, 50, 470);
    tileSouth6 = new Cross(Orientation.SOUTH, Direction.RIGHT, 160, 470);
    ((Cross) tileSouth6).setValue(AccessoryValue.RED);
    tileWest6 = new Cross(Orientation.WEST, Direction.RIGHT, 270, 470);
    tileNorth6 = new Cross(Orientation.NORTH, Direction.RIGHT, 340, 470);
    ((Switch) tileNorth6).setValue(AccessoryValue.GREEN);

    tileEast7 = new Cross(Orientation.EAST, Direction.LEFT, 50, 560);
    ((Cross) tileEast7).setValue(AccessoryValue.RED);
    tileSouth7 = new Cross(Orientation.SOUTH, Direction.LEFT, 160, 560);
    tileWest7 = new Cross(Orientation.WEST, Direction.LEFT, 270, 560);
    ((Switch) tileWest7).setValue(AccessoryValue.GREEN);
    tileNorth7 = new Cross(Orientation.NORTH, Direction.LEFT, 340, 560);

    tileEast8 = new Signal(Orientation.EAST, 70, 650, SignalType.HP01);
    ((Signal) tileEast8).setSignalValue(SignalValue.Hp1);
    tileSouth8 = new Signal(Orientation.SOUTH, 160, 650, SignalType.HP012);
    ((Signal) tileSouth8).setSignalValue(SignalValue.Hp2);
    tileWest8 = new Signal(Orientation.WEST, 250, 650, SignalType.HP012SH1);
    ((Signal) tileWest8).setSignalValue(SignalValue.Hp0Sh1);
    tileNorth8 = new Signal(Orientation.NORTH, 340, 650, SignalType.HP0SH1);
    ((Signal) tileNorth8).setSignalValue(SignalValue.Hp0);
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    //
    tileEast.drawTile(g2d, false);
    tileEast.drawBounds(g2d);
    tileEast.drawCenterPoint(g2d, Color.red);

    tileSouth.drawTile(g2d, false);
    tileSouth.drawBounds(g2d);
    tileSouth.drawCenterPoint(g2d, Color.blue);

    tileWest.drawTile(g2d, false);
    tileWest.drawBounds(g2d);
    tileWest.drawCenterPoint(g2d, Color.red);

    tileNorth.drawTile(g2d, false);
    tileNorth.drawBounds(g2d);
    tileNorth.drawCenterPoint(g2d, Color.cyan);
    //
    tileEast1.drawTile(g2d, false);
    tileEast1.drawBounds(g2d);
    tileEast1.drawCenterPoint(g2d, Color.red);

    tileSouth1.drawTile(g2d, false);
    tileSouth1.drawBounds(g2d);
    tileSouth1.drawCenterPoint(g2d, Color.blue);

    tileWest1.drawTile(g2d, false);
    tileWest1.drawBounds(g2d);
    tileWest1.drawCenterPoint(g2d, Color.red);

    tileNorth1.drawTile(g2d, false);
    tileNorth1.drawBounds(g2d);
    tileNorth1.drawCenterPoint(g2d, Color.cyan);
    //
    tileEast2.drawTile(g2d, false);
    tileEast2.drawBounds(g2d);
    tileEast2.drawCenterPoint(g2d, Color.red);

    tileSouth2.drawTile(g2d, false);
    tileSouth2.drawBounds(g2d);
    tileSouth2.drawCenterPoint(g2d, Color.blue);

    tileWest2.drawTile(g2d, false);
    tileWest2.drawBounds(g2d);
    tileWest2.drawCenterPoint(g2d, Color.red);

    tileNorth2.drawTile(g2d, false);
    tileNorth2.drawBounds(g2d);
    tileNorth2.drawCenterPoint(g2d, Color.cyan);
    //
    tileEast3.drawTile(g2d, false);
    tileEast3.drawBounds(g2d);
    tileEast3.drawCenterPoint(g2d, Color.red);

    ((Sensor) tileSouth3).setActive(true);
    tileSouth3.drawTile(g2d, false);
    tileSouth3.drawBounds(g2d);
    tileSouth3.drawCenterPoint(g2d, Color.blue);

    tileWest3.drawTile(g2d, false);
    tileWest3.drawBounds(g2d);
    tileWest3.drawCenterPoint(g2d, Color.red);

    ((Sensor) tileNorth3).setActive(true);
    tileNorth3.drawTile(g2d, false);
    tileNorth3.drawBounds(g2d);
    tileNorth3.drawCenterPoint(g2d, Color.cyan);
    //
    tileEast4.drawTile(g2d, false);
    tileEast4.drawBounds(g2d);
    tileEast4.drawCenterPoint(g2d, Color.red);

    tileSouth4.drawTile(g2d, false);
    tileSouth4.drawBounds(g2d);
    tileSouth4.drawCenterPoint(g2d, Color.blue);

    tileWest4.drawTile(g2d, false);
    tileWest4.drawBounds(g2d);
    tileWest4.drawCenterPoint(g2d, Color.red);

    tileNorth4.drawTile(g2d, false);
    tileNorth4.drawBounds(g2d);
    tileNorth4.drawCenterPoint(g2d, Color.cyan);
    //
    tileEast5.drawTile(g2d, false);
    tileEast5.drawBounds(g2d);
    tileEast5.drawCenterPoint(g2d, Color.red);

    tileSouth5.drawTile(g2d, false);
    tileSouth5.drawBounds(g2d);
    tileSouth5.drawCenterPoint(g2d, Color.blue);

    tileWest5.drawTile(g2d, false);
    tileWest5.drawBounds(g2d);
    tileWest5.drawCenterPoint(g2d, Color.red);

    tileNorth5.drawTile(g2d, false);
    tileNorth5.drawBounds(g2d);
    tileNorth5.drawCenterPoint(g2d, Color.cyan);
    //
    tileEast6.drawTile(g2d, false);
    tileEast6.drawBounds(g2d);
    tileEast6.drawCenterPoint(g2d, Color.red);

    tileSouth6.drawTile(g2d, false);
    tileSouth6.drawBounds(g2d);
    tileSouth6.drawCenterPoint(g2d, Color.blue);

    tileWest6.drawTile(g2d, false);
    tileWest6.drawBounds(g2d);
    tileWest6.drawCenterPoint(g2d, Color.red);

    tileNorth6.drawTile(g2d, false);
    tileNorth6.drawBounds(g2d);
    tileNorth6.drawCenterPoint(g2d, Color.cyan);
    //
    tileEast7.drawTile(g2d, false);
    tileEast7.drawBounds(g2d);
    tileEast7.drawCenterPoint(g2d, Color.red);

    tileSouth7.drawTile(g2d, false);
    tileSouth7.drawBounds(g2d);
    tileSouth7.drawCenterPoint(g2d, Color.blue);

    tileWest7.drawTile(g2d, false);
    tileWest7.drawBounds(g2d);
    tileWest7.drawCenterPoint(g2d, Color.red);

    tileNorth7.drawTile(g2d, false);
    tileNorth7.drawBounds(g2d);
    tileNorth7.drawCenterPoint(g2d, Color.cyan);
    //
    tileEast8.drawTile(g2d, false);
    tileEast8.drawBounds(g2d);
    tileEast8.drawCenterPoint(g2d, Color.red);

    tileSouth8.drawTile(g2d, false);
    tileSouth8.drawBounds(g2d);
    tileSouth8.drawCenterPoint(g2d, Color.blue);

    tileWest8.drawTile(g2d, false);
    tileWest8.drawBounds(g2d);
    tileWest8.drawCenterPoint(g2d, Color.red);

    tileNorth8.drawTile(g2d, false);
    tileNorth8.drawBounds(g2d);
    tileNorth8.drawCenterPoint(g2d, Color.cyan);

  }

  public static void main(String args[]) {

    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error(ex);
    }

    TileTester app = new TileTester("Tile Tester 2");

    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    app.setSize(400, 710);

    app.setLocation(dim.width / 2 - app.getSize().width / 2, dim.height / 2 - app.getSize().height / 2);

    app.setVisible(true);

    app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

}
