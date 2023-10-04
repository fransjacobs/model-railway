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
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean.Orientation;
import org.tinylog.Logger;

/**
 * @author Frans Jacobs
 */
public class BlockTileTester extends JFrame {

  private final Tile tileEast;
  private final Tile tileSouth;
  private final Tile tileWest;
  private final Tile tileNorth;

  @SuppressWarnings("OverridableMethodCallInConstructor")
  public BlockTileTester(String title) {
    super(title);

    LocomotiveBean lok1 =
        new LocomotiveBean(
            2L,
            "BR 81 002",
            2L,
            0L,
            2,
            "DB BR 81 008",
            "mm_prg",
            null,
            120,
            1,
            0,
            0,
            false,
            null,
            true);

    LocomotiveBean lok2 =
        new LocomotiveBean(
            12L,
            "BR 141 015-08",
            12L,
            null,
            12,
            "DB BR 141 136-2",
            "mm_prg",
            null,
            120,
            0,
            0,
            2,
            false,
            null,
            true);

    tileEast = new Block(Orientation.EAST, 70, 190);
    tileEast.setId("bk-1");

    BlockBean bbe = new BlockBean();
    bbe.setId(tileEast.getId());
    bbe.setTileId(tileEast.getId());
    bbe.setLocomotive(lok1);
    //bbe.setReverseArrival(true);
    ((Block) tileEast).setBlockBean(bbe);

    //
    tileSouth = new Block(Orientation.SOUTH, 160, 190);
    tileSouth.setId("bk-2");
    BlockBean bbs = new BlockBean();
    bbs.setId(tileSouth.getId());
    bbs.setTileId(tileSouth.getId());
    lok2.setDirection(lok2.getDirection().toggle());
    //bbs.setLocomotive(lok2);
    bbs.setReverseArrival(true);
    ((Block) tileSouth).setBlockBean(bbs);

    tileWest = new Block(Orientation.WEST, 250, 190);
    tileWest.setId("bk-3");
    BlockBean bbw = new BlockBean();
    bbw.setId(tileWest.getId());
    bbw.setTileId(tileWest.getId());
    bbw.setLocomotive(lok2);
    // bbw.setReverseArrival(true);
    ((Block) tileWest).setBlockBean(bbw);

    tileNorth = new Block(Orientation.NORTH, 340, 190);
    tileNorth.setId("bk-4");
    BlockBean bbn = new BlockBean();
    bbn.setId(tileNorth.getId());
    bbn.setTileId(tileNorth.getId());
    //bbn.setLocomotive(lok2);
    bbn.setReverseArrival(true);
    ((Block) tileNorth).setBlockBean(bbn);
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    
    tileEast.drawTile(g2d, true);
    tileEast.drawBounds(g2d);
    tileEast.drawCenterPoint(g2d, Color.red);

    tileSouth.drawTile(g2d, false);
    tileSouth.drawBounds(g2d);
    tileSouth.drawCenterPoint(g2d, Color.blue);

    tileWest.drawTile(g2d, false);
    tileWest.drawBounds(g2d);
    tileWest.drawCenterPoint(g2d, Color.red);
    
    tileNorth.drawTile(g2d, true);
    tileNorth.drawBounds(g2d);
    tileNorth.drawCenterPoint(g2d, Color.cyan);
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

    BlockTileTester app = new BlockTileTester("Block Tile Tester");

    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    app.setSize(370, 300);
    app.setLocation(
        dim.width / 2 - app.getSize().width / 2, dim.height / 2 - app.getSize().height / 2);

    app.setVisible(true);

    app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
}
