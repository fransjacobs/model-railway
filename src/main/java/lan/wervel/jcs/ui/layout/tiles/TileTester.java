/*
 * Copyright (C) 2020 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package lan.wervel.jcs.ui.layout.tiles;

import lan.wervel.jcs.ui.layout.tiles.enums.Rotation;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import lan.wervel.jcs.entities.Signal;
import lan.wervel.jcs.entities.enums.SignalValue;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;

/**
 *
 * @author Frans Jacobs
 */
public class TileTester extends JFrame {

  private final AbstractTile tile;

  @SuppressWarnings("OverridableMethodCallInConstructor")
  public TileTester() {
    super("Test a Tile");

    //this.tile = new StraightTrack(Rotation.R0,50,50);
    //this.tile = new DiagonalTrack(Rotation.R90,50,50);
    //this.tile = new FeedbackPort(Rotation.R90,50,50);
    //((FeedbackPort)tile).setValue(true);
    //this.tile = new OccupancyDetector(Rotation.R90,100,100);
    //((OccupancyDetector)tile).setValue(true);
    //this.tile = new TurnoutTile(Rotation.R0,Direction.LEFT,100,100);
    //((TurnoutTile)tile).setStatus(TurnoutTile.STRAIGHT);
    this.tile = new SignalTile(Rotation.R0, 100, 100);
    //((SignalTile)tile).setSignalType(Signal.HP012SH1);
    ((SignalTile)tile).setMidget(true);
    ((SignalTile)tile).setSignalValue(SignalValue.Hp1);

    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

    setSize(200, 200);
    setVisible(true);
  }

  @Override
  public void paint(Graphics g) {
    // create 2D by casting g to Graphics2D
    Graphics2D g2d = (Graphics2D) g;

    tile.drawTile(g2d);

    tile.drawBounds(g2d);
    tile.drawCenterPoint(g2d, Color.red);
  }

  public static void main(String args[]) {
    TileTester app = new TileTester();

    app.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
  }

}
