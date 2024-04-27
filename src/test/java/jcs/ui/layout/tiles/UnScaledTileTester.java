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
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean.Orientation;
import jcs.entities.TileBean.TileType;
import org.tinylog.Logger;

/**
 * @author Frans Jacobs
 */
public class UnScaledTileTester extends JFrame {

  private final Tile tile;

  @SuppressWarnings("OverridableMethodCallInConstructor")
  public UnScaledTileTester(String title) {
    super(title);

    // tile = new Straight(Orientation.EAST, 250, 250);
    //tile = new StraightDirection(Orientation.EAST, 250, 250);
    // tile = new Signal(Orientation.EAST, 250, 250, SignalType.HP0SH1);
    // ((Signal) tile).setSignalValue(SignalValue.Hp0);
    // tile = new Curved(Orientation.EAST, 250, 250);
    // tile = new Sensor(Orientation.EAST, 250, 250);
    // tile = new Switch(Orientation.EAST, TileBean.Direction.LEFT, 250, 250);
    // tile = new End(Orientation.EAST, 250, 250);
    // tile = new Crossing(Orientation.NORTH, 250, 250);
    //tile = new Cross(Orientation.NORTH, Direction.LEFT, 250, 750);
    tile = new Block(Orientation.EAST, 750, 250);
    tile.setId("bk-1");

    LocomotiveBean lok = new LocomotiveBean(12L, "BR 141 015-08", 12L, 12, "DB BR 141 136-2", "mm_prg", 120, 0, 0, 2, false, true);

    BlockBean bbe = new BlockBean();
    bbe.setId(tile.getId());
    bbe.setTileId(tile.getId());
    bbe.setLocomotive(lok);
    //bbe.setReverseArrival(true);
    //((Block) tile).setBlockBean(bbe);

    ((AbstractTile) tile).setScaleImage(false);
  }


  
  @Override
  public void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    //
    //tile.setTrackRouteColor(Color.black, Orientation.WEST);
    //((Cross) tile).setRouteValue(AccessoryValue.RED, Color.black);

    tile.drawTile(g2d, true);
    tile.drawBounds(g2d);
    tile.drawCenterPoint(g2d, Color.red);

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

    UnScaledTileTester app = new UnScaledTileTester("UNSCALED Tile Tester");

    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    // app.pack();
    //app.setSize(500, 500);
    app.setSize(1500, 500);
    //app.setSize(500, 1000);

    app.setLocation(
            dim.width / 2 - app.getSize().width / 2, dim.height / 2 - app.getSize().height / 2);

    app.setVisible(true);

    app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
  
  
  
  
  
}
