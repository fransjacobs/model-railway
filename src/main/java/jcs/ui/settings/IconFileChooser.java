/*
 * Copyright 2023 frans.
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

import java.io.File;
import jcs.entities.CommandStationBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class IconFileChooser extends javax.swing.JDialog {

  /**
   * Creates new form LocomotiveIconFileChooser
   * @param parent
   * @param modal
   */
  public IconFileChooser(java.awt.Frame parent, boolean modal) {
    super(parent, modal);
    initComponents();
    postInit();
  }

  private void postInit() {
    String defaultPath = System.getProperty("user.home") + File.separator + "jcs";
    //The path depends also on the sn of the commans station
    CommandStationBean csb = PersistenceFactory.getService().getDefaultCommandStation();
    String pathpart = "cache";
    if(csb != null && csb.getLastUsedSerial() != null) {
      pathpart = pathpart + File.separator + csb.getLastUsedSerial();
    }
    String path = defaultPath + File.separator + pathpart;
    File jcsDir = new File(path);
    this.fileChooser.setCurrentDirectory(jcsDir);
    this.setLocationRelativeTo(null);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    fileChooser = new javax.swing.JFileChooser();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setAlwaysOnTop(true);

    fileChooser.setDialogTitle("Select Locomotive Icon");
    fileChooser.setDoubleBuffered(true);
    fileChooser.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        fileChooserActionPerformed(evt);
      }
    });
    getContentPane().add(fileChooser, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void fileChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserActionPerformed
    Logger.trace(evt.getActionCommand());
    Logger.trace("Selected: " + this.fileChooser.getSelectedFile());

    this.dispose();
  }//GEN-LAST:event_fileChooserActionPerformed

  public File getSelectedIconFile() {
    return this.fileChooser.getSelectedFile();
  }
  
  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);
  }
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JFileChooser fileChooser;
  // End of variables declaration//GEN-END:variables
}
