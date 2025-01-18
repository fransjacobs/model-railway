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
package jcs.ui.layout;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.JCS;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class LayoutPanelTester {

  public static void main(String args[]) {
    //System.setProperty("trackServiceAlwaysUseDemo", "true");
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error(ex);
    }

    java.awt.EventQueue.invokeLater(() -> {
      JFrame f = new JFrame("LayoutPanel Tester");
      LayoutPanel layoutPanel = new LayoutPanel();
      f.add(layoutPanel);

      URL iconUrl = JCS.class.getResource("/media/jcs-train-64.png");
      if (iconUrl != null) {
        f.setIconImage(new ImageIcon(iconUrl).getImage());
      }

      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      layoutPanel.loadLayout();
      f.pack();

      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      f.setLocation(dim.width / 2 - f.getSize().width / 2, dim.height / 2 - f.getSize().height / 2);
      f.setVisible(true);
    });
  }

}
