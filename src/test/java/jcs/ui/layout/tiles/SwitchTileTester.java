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
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.TileBean.Direction;
import jcs.entities.TileBean.Orientation;
import org.tinylog.Logger;

/**
 * @author Frans Jacobs
 */
public class SwitchTileTester extends JFrame {

  private final Tile switchEastR;
  private final Tile switchSouthR;
  private final Tile switchWestR;
  private final Tile switchNorthR;

  private final Tile switchEastL;
  private final Tile switchSouthL;
  private final Tile switchWestL;
  private final Tile switchNorthL;

  public SwitchTileTester(String title) {
    super(title);

    switchEastR = new Switch(Orientation.EAST, Direction.RIGHT, 70, 60);
    switchSouthR = new Switch(Orientation.SOUTH, Direction.RIGHT, 160, 60);
    switchWestR = new Switch(Orientation.WEST, Direction.RIGHT, 250, 60);
    switchNorthR = new Switch(Orientation.NORTH, Direction.RIGHT, 340, 60);

    switchEastL = new Switch(Orientation.EAST, Direction.LEFT, 70, 110);
    switchSouthL = new Switch(Orientation.SOUTH, Direction.LEFT, 160, 110);
    switchWestL = new Switch(Orientation.WEST, Direction.LEFT, 250, 110);
    switchNorthL = new Switch(Orientation.NORTH, Direction.LEFT, 340, 110);
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    switchEastR.drawTile(g2d, true);
    switchEastR.drawBounds(g2d);
    switchEastR.drawCenterPoint(g2d, Color.red);
    ((Switch) switchEastR).setValue(AccessoryValue.RED);

    switchSouthR.drawTile(g2d, false);
    switchSouthR.drawBounds(g2d);
    switchSouthR.drawCenterPoint(g2d, Color.blue);

    switchWestR.drawTile(g2d, true);
    switchWestR.drawBounds(g2d);
    switchWestR.drawCenterPoint(g2d, Color.red);
    ((Switch) switchWestR).setValue(AccessoryValue.GREEN);

    switchNorthR.drawTile(g2d, false);
    switchNorthR.drawBounds(g2d);
    switchNorthR.drawCenterPoint(g2d, Color.cyan);
    //
    switchEastL.drawTile(g2d, false);
    switchEastL.drawBounds(g2d);
    switchEastL.drawCenterPoint(g2d, Color.red);

    switchSouthL.drawTile(g2d, true);
    switchSouthL.drawBounds(g2d);
    switchSouthL.drawCenterPoint(g2d, Color.blue);
    ((Switch) switchSouthL).setValue(AccessoryValue.GREEN);

    switchWestL.drawTile(g2d, false);
    switchWestL.drawBounds(g2d);
    switchWestL.drawCenterPoint(g2d, Color.red);

    switchNorthL.drawTile(g2d, true);
    switchNorthL.drawBounds(g2d);
    switchNorthL.drawCenterPoint(g2d, Color.cyan);
    ((Switch) switchNorthL).setValue(AccessoryValue.RED);

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

    SwitchTileTester app = new SwitchTileTester("Turnout Tile Tester");

    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    app.setSize(400, 150);

    app.setLocation(
            dim.width / 2 - app.getSize().width / 2, dim.height / 2 - app.getSize().height / 2);

    app.setVisible(true);

    app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
}
