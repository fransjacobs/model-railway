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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import lan.wervel.jcs.entities.SignalBean;
import lan.wervel.jcs.entities.enums.Orientation;
import lan.wervel.jcs.entities.enums.SignalValue;

/**
 *
 * @author Frans Jacobs
 */
public class TileTester extends JFrame {

    private final AbstractTile tileEast;
    private final AbstractTile tileSouth;
    private final AbstractTile tileWest;
    private final AbstractTile tileNorth;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public TileTester() {
        super("Test a Tile");

//        tileEast = new StraightTrack(Orientation.EAST, 50, 50);
//        tileSouth = new StraightTrack(Orientation.SOUTH, 100, 50);
//        tileWest = new StraightTrack(Orientation.WEST, 150, 50);
//        tileNorth = new StraightTrack(Orientation.NORTH, 200, 50);
//        tileEast = new DiagonalTrack(Orientation.EAST, 50, 50);
//        tileSouth = new DiagonalTrack(Orientation.SOUTH, 100, 50);
//        tileWest = new DiagonalTrack(Orientation.WEST, 150, 50);
//        tileNorth = new DiagonalTrack(Orientation.NORTH, 200, 50);
//        tileEast = new Sensor(Orientation.EAST, 50, 50);
//        tileSouth = new Sensor(Orientation.SOUTH, 100, 50);
//        ((Sensor)tileSouth).setActive(true);
//        tileWest = new Sensor(Orientation.WEST, 150, 50);
//        tileNorth = new Sensor(Orientation.NORTH, 200, 50);
//        ((Sensor)tileNorth).setActive(true);
//        tileEast = new Block(Orientation.EAST, 50, 80);
//        tileSouth = new Block(Orientation.SOUTH, 120, 80);
//        ((Block)tileSouth).setActive(true);
//        tileWest = new Block(Orientation.WEST, 190, 80);
//        tileNorth = new Block(Orientation.NORTH, 260, 80);
//        ((Block)tileNorth).setActive(true);

//        tileEast = new SwitchTile(Orientation.EAST, Direction.LEFT, 50, 80);
//        tileSouth = new SwitchTile(Orientation.SOUTH, Direction.LEFT, 140, 80);
//        ((SwitchTile) tileSouth).setAccessoryValue(AccessoryValue.RED);
//        tileWest = new SwitchTile(Orientation.WEST, Direction.RIGHT, 230, 80);
//        ((SwitchTile) tileWest).setAccessoryValue(AccessoryValue.GREEN);
//        tileNorth = new SwitchTile(Orientation.NORTH, Direction.RIGHT, 320, 80);

        
        tileEast = new SignalTile(Orientation.EAST, 50, 80);
        tileSouth = new SignalTile(Orientation.SOUTH, 120, 80);
        ((SignalTile)tileSouth).setSignalType(SignalBean.HP012SH1);

        tileWest = new SignalTile(Orientation.WEST, 190, 80);
        ((SignalTile)tileWest).setMidget(true);
        tileNorth = new SignalTile(Orientation.NORTH, 260, 80);
        ((SignalTile)tileNorth).setSignalValue(SignalValue.Hp1);
        
        //this.tile = new SignalTile(Rotation.R0, 100, 100);
        //((SignalTile)tile).setSignalType(SignalBean.HP012SH1);
        //((SignalTile)tile).setMidget(true);
        //((SignalTile)tile).setSignalValue(SignalValue.Hp1);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        setSize(400, 160);
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        // create 2D by casting g to Graphics2D
        Graphics2D g2d = (Graphics2D) g;

        tileEast.drawTile(g2d);

        tileEast.drawBounds(g2d);
        tileEast.drawCenterPoint(g2d, Color.red);

        tileSouth.drawTile(g2d);
        tileSouth.drawBounds(g2d);
        tileSouth.drawCenterPoint(g2d, Color.blue);

        tileWest.drawTile(g2d);
        tileWest.drawBounds(g2d);
        tileWest.drawCenterPoint(g2d, Color.red);

        tileNorth.drawTile(g2d);
        tileNorth.drawBounds(g2d);
        tileNorth.drawCenterPoint(g2d, Color.cyan);

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
