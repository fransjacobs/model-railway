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
import org.tinylog.Logger;

/**
 * @author Frans Jacobs
 */
public class EndTileTester extends JFrame {

  private final Tile trackEast;
  private final Tile trackSouth;
  private final Tile trackWest;
  private final Tile trackNorth;

  public EndTileTester(String title) {
    super(title);

    trackEast = new End(Orientation.EAST, 70, 60);
    trackSouth = new End(Orientation.SOUTH, 160, 60);
    trackWest = new End(Orientation.WEST, 250, 60);
    trackNorth = new End(Orientation.NORTH, 340, 60);
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    trackEast.drawTile(g2d, false);
    trackEast.drawBounds(g2d);
    trackEast.drawCenterPoint(g2d, Color.red);

    trackSouth.drawTile(g2d, false);
    trackSouth.drawBounds(g2d);
    trackSouth.drawCenterPoint(g2d, Color.blue);

    trackWest.drawTile(g2d, false);
    trackWest.drawBounds(g2d);
    trackWest.drawCenterPoint(g2d, Color.red);

    trackNorth.drawTile(g2d, false);
    trackNorth.drawBounds(g2d);
    trackNorth.drawCenterPoint(g2d, Color.cyan);
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

    EndTileTester app = new EndTileTester("End Tile Tester");

    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    app.setSize(400, 100);

    app.setLocation(
        dim.width / 2 - app.getSize().width / 2, dim.height / 2 - app.getSize().height / 2);

    app.setVisible(true);

    app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
}
