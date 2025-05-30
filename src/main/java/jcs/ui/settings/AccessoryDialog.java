/*
 * Copyright 2025 fransjacobs.
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
package jcs.ui.settings;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class AccessoryDialog extends javax.swing.JDialog {

  private static final long serialVersionUID = 1666127249735678517L;

  /**
   * Creates new form AccessoryDialog
   */
  public AccessoryDialog(java.awt.Frame parent, boolean modal) {
    super(parent, modal);
    initComponents();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    accessorySettingsPanel1 = new jcs.ui.settings.AccessorySettingsPanel();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    getContentPane().add(accessorySettingsPanel1, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    /* Set the FlatLightLaf look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.warn("Can't set the LookAndFeel: " + ex);
    }
    //</editor-fold>

    /* Create and display the dialog */
    java.awt.EventQueue.invokeLater(() -> {
      AccessoryDialog dialog = new AccessoryDialog(new javax.swing.JFrame(), true);
      dialog.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
          System.exit(0);
        }
      });
      dialog.setTitle("Accessory Settings");
      dialog.pack();
      dialog.setLocationRelativeTo(null);

      dialog.setVisible(true);
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private jcs.ui.settings.AccessorySettingsPanel accessorySettingsPanel1;
  // End of variables declaration//GEN-END:variables
}
