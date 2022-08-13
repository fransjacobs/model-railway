/*
 * Copyright (C) 2022 fransjacobs.
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
package jcs.ui.layout;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class LayoutPanelTesterRO {
           public static void main(String args[]) {
        //System.setProperty("trackServiceAlwaysUseDemo", "true");
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.error(ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            JFrame f = new JFrame("LayoutPanel Tester");
            LayoutPanel layoutPanel = new LayoutPanel(true);
            f.add(layoutPanel);

            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            layoutPanel.loadLayout();
            f.pack();

            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            f.setLocation(dim.width / 2 - f.getSize().width / 2, dim.height / 2 - f.getSize().height / 2);
            f.setVisible(true);
        });
    }

}
