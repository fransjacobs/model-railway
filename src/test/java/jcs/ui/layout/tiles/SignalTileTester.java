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
import jcs.entities.TileBean.Orientation;
import jcs.entities.enums.SignalType;
import jcs.entities.enums.SignalValue;
import org.tinylog.Logger;

/**
 * @author Frans Jacobs
 */
public class SignalTileTester extends JFrame {

  private final Tile signal2East;
  private final Tile signal2South;
  private final Tile signal2West;
  private final Tile signal2North;

  private final Tile signal2MEast;
  private final Tile signal2MSouth;
  private final Tile signal2MWest;
  private final Tile signal2MNorth;

  private final Tile signal3East;
  private final Tile signal3South;
  private final Tile signal3West;
  private final Tile signal3North;

  private final Tile signal4East;
  private final Tile signal4South;
  private final Tile signal4West;
  private final Tile signal4North;

  @SuppressWarnings("OverridableMethodCallInConstructor")
  public SignalTileTester(String title) {
    super(title);

    signal2East = new Signal(Orientation.EAST, 70, 60, SignalType.HP01);
    ((Signal) signal2East).setSignalValue(SignalValue.Hp0);

    signal2South = new Signal(Orientation.SOUTH, 160, 60, SignalType.HP01);
    ((Signal) signal2South).setSignalValue(SignalValue.Hp1);

    signal2West = new Signal(Orientation.WEST, 250, 60, SignalType.HP01);
    ((Signal) signal2West).setSignalValue(SignalValue.Hp0);

    signal2North = new Signal(Orientation.NORTH, 340, 60, SignalType.HP01);
    ((Signal) signal2North).setSignalValue(SignalValue.Hp1);

    //
    signal2MEast = new Signal(Orientation.EAST, 70, 110, SignalType.HP0SH1);
    ((Signal) signal2MEast).setSignalValue(SignalValue.Hp0);

    signal2MSouth = new Signal(Orientation.SOUTH, 160, 110, SignalType.HP0SH1);
    ((Signal) signal2MSouth).setSignalValue(SignalValue.Hp1);

    signal2MWest = new Signal(Orientation.WEST, 250, 110, SignalType.HP0SH1);
    ((Signal) signal2MWest).setSignalValue(SignalValue.Hp0);

    signal2MNorth = new Signal(Orientation.NORTH, 340, 110, SignalType.HP0SH1);
    ((Signal) signal2MNorth).setSignalValue(SignalValue.Hp1);

    //
    signal3East = new Signal(Orientation.EAST, 70, 160, SignalType.HP012);
    ((Signal) signal3East).setSignalValue(SignalValue.Hp0);

    signal3South = new Signal(Orientation.SOUTH, 160, 160, SignalType.HP012);
    ((Signal) signal3South).setSignalValue(SignalValue.Hp1);

    signal3West = new Signal(Orientation.WEST, 250, 160, SignalType.HP012);
    ((Signal) signal3West).setSignalValue(SignalValue.Hp2);

    signal3North = new Signal(Orientation.NORTH, 340, 160, SignalType.HP012);
    ((Signal) signal3North).setSignalValue(SignalValue.Hp0);

    //
    signal4East = new Signal(Orientation.EAST, 70, 210, SignalType.HP012SH1);
    ((Signal) signal4East).setSignalValue(SignalValue.Hp0);

    signal4South = new Signal(Orientation.SOUTH, 160, 210, SignalType.HP012SH1);
    ((Signal) signal4South).setSignalValue(SignalValue.Hp1);

    signal4West = new Signal(Orientation.WEST, 250, 210, SignalType.HP012SH1);
    ((Signal) signal4West).setSignalValue(SignalValue.Hp2);

    signal4North = new Signal(Orientation.NORTH, 340, 210, SignalType.HP012SH1);
    ((Signal) signal4North).setSignalValue(SignalValue.Hp0Sh1);

  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    //
    signal2East.drawTile(g2d, false);
    signal2East.drawBounds(g2d);
    signal2East.drawCenterPoint(g2d, Color.red);

    signal2South.drawTile(g2d, false);
    signal2South.drawBounds(g2d);
    signal2South.drawCenterPoint(g2d, Color.blue);

    signal2West.drawTile(g2d, false);
    signal2West.drawBounds(g2d);
    signal2West.drawCenterPoint(g2d, Color.red);

    signal2North.drawTile(g2d, false);
    signal2North.drawBounds(g2d);
    signal2North.drawCenterPoint(g2d, Color.cyan);

    //
    signal2MEast.drawTile(g2d, true);
    signal2MEast.drawBounds(g2d);
    signal2MEast.drawCenterPoint(g2d, Color.red);

    signal2MSouth.drawTile(g2d, true);
    signal2MSouth.drawBounds(g2d);
    signal2MSouth.drawCenterPoint(g2d, Color.blue);

    signal2MWest.drawTile(g2d, true);
    signal2MWest.drawBounds(g2d);
    signal2MWest.drawCenterPoint(g2d, Color.red);

    signal2MNorth.drawTile(g2d, true);
    signal2MNorth.drawBounds(g2d);
    signal2MNorth.drawCenterPoint(g2d, Color.cyan);

    //
    signal3East.drawTile(g2d, false);
    signal3East.drawBounds(g2d);
    signal3East.drawCenterPoint(g2d, Color.red);

    signal3South.drawTile(g2d, false);
    signal3South.drawBounds(g2d);
    signal3South.drawCenterPoint(g2d, Color.blue);

    signal3West.drawTile(g2d, false);
    signal3West.drawBounds(g2d);
    signal3West.drawCenterPoint(g2d, Color.red);

    signal3North.drawTile(g2d, false);
    signal3North.drawBounds(g2d);
    signal3North.drawCenterPoint(g2d, Color.cyan);

    //
    signal4East.drawTile(g2d, true);
    signal4East.drawBounds(g2d);
    signal4East.drawCenterPoint(g2d, Color.red);

    signal4South.drawTile(g2d, true);
    signal4South.drawBounds(g2d);
    signal4South.drawCenterPoint(g2d, Color.blue);

    signal4West.drawTile(g2d, true);
    signal4West.drawBounds(g2d);
    signal4West.drawCenterPoint(g2d, Color.red);

    signal4North.drawTile(g2d, true);
    signal4North.drawBounds(g2d);
    signal4North.drawCenterPoint(g2d, Color.cyan);

  }

  public static void main(String args[]) {

    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException
            | InstantiationException
            | IllegalAccessException
            | UnsupportedLookAndFeelException ex) {
      Logger.error(ex);
    }

    SignalTileTester app = new SignalTileTester("Signal Tile Tester");

    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    app.setSize(400, 250);

    app.setLocation(
            dim.width / 2 - app.getSize().width / 2, dim.height / 2 - app.getSize().height / 2);

    app.setVisible(true);

    app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
}
