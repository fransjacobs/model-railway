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
public class UnScaledTileTester extends JFrame {

  private final Tile tileEast;
//  private final Tile tileSouth;
//  private final Tile tileWest;
//  private final Tile tileNorth;


  @SuppressWarnings("OverridableMethodCallInConstructor")
  public UnScaledTileTester(String title) {
    super(title);

    //tileEast = new Straight1(Orientation.EAST, 250, 250);
    
   tileEast = new Signal(Orientation.EAST, 250, 250, SignalType.HP0SH1);
    ((AbstractTile) tileEast).setScaleImage(false);
    ((Signal) tileEast).setSignalValue(SignalValue.Hp0);
    
//    tileSouth = new Straight1(Orientation.SOUTH, 160, 60);
//    tileWest = new Straight1(Orientation.WEST, 250, 60);
//    tileNorth = new Straight1(Orientation.NORTH, 340, 60);

  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    //
    tileEast.drawTile(g2d, true);
    tileEast.drawBounds(g2d);
    tileEast.drawCenterPoint(g2d, Color.red);

//    tileSouth.drawTile(g2d, false);
//    tileSouth.drawBounds(g2d);
//    tileSouth.drawCenterPoint(g2d, Color.blue);
//
//    tileWest.drawTile(g2d, false);
//    tileWest.drawBounds(g2d);
//    tileWest.drawCenterPoint(g2d, Color.red);
//
//    tileNorth.drawTile(g2d, false);
//    tileNorth.drawBounds(g2d);
//    tileNorth.drawCenterPoint(g2d, Color.cyan);
    //
 
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

    UnScaledTileTester app = new UnScaledTileTester("Tile Tester 2");

    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    //app.pack();
    app.setSize(500, 500);

    app.setLocation(
        dim.width / 2 - app.getSize().width / 2, dim.height / 2 - app.getSize().height / 2);

    app.setVisible(true);

    app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
}
