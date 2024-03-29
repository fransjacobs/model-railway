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
package jcs.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.JCS;
import jcs.commandStation.events.MeasurementEvent;
import jcs.commandStation.events.MeasurementEventListener;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class StatusPanel extends javax.swing.JPanel implements MeasurementEventListener {

  /**
   * Creates new form StatusPanel
   */
  public StatusPanel() {
    initComponents();
    postInit();
  }

  private void postInit() {
    if (JCS.getJcsCommandStation() != null) {
      JCS.getJcsCommandStation().addMeasurementEventListener(this);
    }
  }

  @Override
  public void onMeasurement(MeasurementEvent event) {

    switch (event.getCannel()) {
      case 1:
        this.currentLbl.setText(event.getFormattedValue());
        break;
      case 3:
        this.voltageLbl.setText(event.getFormattedValue());
        break;
      case 4:
        this.tempLbl.setText(event.getFormattedValue());
      default:
        break;
    }

  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    measurePanel = new javax.swing.JPanel();
    voltageLbl = new javax.swing.JLabel();
    currentLbl = new javax.swing.JLabel();
    tempLbl = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();

    setMinimumSize(new java.awt.Dimension(600, 45));
    setPreferredSize(new java.awt.Dimension(1200, 45));
    setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));

    voltageLbl.setText("-");
    voltageLbl.setToolTipText("");
    voltageLbl.setPreferredSize(new java.awt.Dimension(55, 20));
    measurePanel.add(voltageLbl);

    currentLbl.setText("-");
    currentLbl.setPreferredSize(new java.awt.Dimension(55, 20));
    measurePanel.add(currentLbl);

    tempLbl.setText("-");
    tempLbl.setPreferredSize(new java.awt.Dimension(55, 20));
    measurePanel.add(tempLbl);

    add(measurePanel);

    jPanel2.setPreferredSize(new java.awt.Dimension(100, 45));
    add(jPanel2);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel currentLbl;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel measurePanel;
  private javax.swing.JLabel tempLbl;
  private javax.swing.JLabel voltageLbl;
  // End of variables declaration//GEN-END:variables

  //For standalone testing only
  public static void main(String args[]) {
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error(ex);
    }

    java.awt.EventQueue.invokeLater(() -> {
      JFrame f = new JFrame("StatusPanel Tester");
      StatusPanel statusPanel = new StatusPanel();
      f.add(statusPanel);

      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      f.pack();

      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      f.setLocation(dim.width / 2 - f.getSize().width / 2, dim.height / 2 - f.getSize().height / 2);
      f.setVisible(true);
    });
  }

}
