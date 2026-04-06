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
import jcs.commandStation.automation.RailController;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.commandStation.events.MeasurementEvent;
import jcs.commandStation.events.MeasurementEventListener;
import org.tinylog.Logger;
import jcs.commandStation.automation.RailControllerStatusListener;

/**
 *
 * @author frans
 */
public class StatusPanel extends javax.swing.JPanel implements MeasurementEventListener, RailControllerStatusListener, ConnectionEventListener {

  private static final long serialVersionUID = 7132844382832996985L;

  boolean supportMeasuments;

  private static String CONNECTION_G = "/media/connection-green-24.png";
  private static String CONNECTION_R = "/media/connection-green-24.png";
  private static String CONNECTION_Y = "/media/connection-yellow-24.png";

  private static String CRUISE_CONTROL_B = "/media/cruise-control-on-black.png";
  private static String CRUISE_CONTROL_G = "/media/cruise-control-on-green.png";
  private static String CRUISE_CONTROL_R = "/media/cruise-control-on-red.png";
  private static String CRUISE_CONTROL_Y = "/media/cruise-control-on-yellow.png";

  /**
   * Creates new form StatusPanel
   */
  public StatusPanel() {
    initComponents();
    postInit();
  }

  private void postInit() {
    if (JCS.getJcsCommandStation() != null) {
      JCS.getJcsCommandStation().addConnectionEventListener(this);

      JCS.getJcsCommandStation().addMeasurementEventListener(this);
      Logger.trace("Added StatusPanel as MeasurementEventListener");

      JCS.getRailController().addStatusListener(this);

      this.connectedLbl.setVisible(false);
      this.virtualConnectionLbl.setVisible(false);

      this.autopilotLbl.setVisible(false);

      this.currentLbl.setVisible(supportMeasuments);
      this.voltageLbl.setVisible(supportMeasuments);
      this.tempLbl.setVisible(supportMeasuments);
    }
  }

  @Override
  public void onMeasurement(MeasurementEvent event) {
    supportMeasuments = event.isMeasurementsEnabled();
    this.currentLbl.setVisible(supportMeasuments);
    this.voltageLbl.setVisible(supportMeasuments);
    this.tempLbl.setVisible(supportMeasuments);

    if (event.getMain() != null) {
      this.currentLbl.setText(event.getMain().getDisplayValue() + " " + event.getMain().getUnit());
    } else {
      this.currentLbl.setText("-");
    }

    if (event.getVolt() != null) {
      this.voltageLbl.setText(event.getVolt().getDisplayValue() + " " + event.getVolt().getUnit());
    } else {
      this.voltageLbl.setText("-");
    }

    if (event.getTemp() != null) {
      this.tempLbl.setText(event.getTemp().getDisplayValue() + " " + event.getTemp().getUnit());
    } else {
      this.tempLbl.setText("-");
    }
  }

  @Override
  public void onConnectionChange(ConnectionEvent event) {

    if (event.isConnected()) {
      connectedLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/connection-green-24.png")));
    } else {
      connectedLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/connection-red-24.png")));
    }

    this.virtualConnectionLbl.setVisible(event.isVirtual());
    this.connectedLbl.setVisible(true);
  }

  @Override
  public void onControllerStatusChange(String status) {
    Logger.trace(status);
    
    if(status == null) {
      return;
    }

    switch (status) {
      case RailController.PENDING -> {
        this.autopilotLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource(CRUISE_CONTROL_B)));
      }
      case RailController.STARTED -> {
        this.autopilotLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource(CRUISE_CONTROL_G)));

      }
      case RailController.STOPPING -> {
        this.autopilotLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource(CRUISE_CONTROL_Y)));

      }
      case RailController.STOPPED -> {
        this.autopilotLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource(CRUISE_CONTROL_R)));

      }
    }

    this.autopilotLbl.setVisible(true);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    statusPanel = new javax.swing.JPanel();
    connectedLbl = new javax.swing.JLabel();
    virtualConnectionLbl = new javax.swing.JLabel();
    autopilotLbl = new javax.swing.JLabel();
    measurePanel = new javax.swing.JPanel();
    voltageLbl = new javax.swing.JLabel();
    currentLbl = new javax.swing.JLabel();
    tempLbl = new javax.swing.JLabel();
    miscPanel = new javax.swing.JPanel();

    setMinimumSize(new java.awt.Dimension(1200, 45));
    setPreferredSize(new java.awt.Dimension(1200, 45));
    java.awt.FlowLayout flowLayout2 = new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0);
    flowLayout2.setAlignOnBaseline(true);
    setLayout(flowLayout2);

    statusPanel.setPreferredSize(new java.awt.Dimension(600, 45));
    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10);
    flowLayout1.setAlignOnBaseline(true);
    statusPanel.setLayout(flowLayout1);

    connectedLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/connection-green-24.png"))); // NOI18N
    connectedLbl.setToolTipText("Connected");
    statusPanel.add(connectedLbl);

    virtualConnectionLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/virtual-24.png"))); // NOI18N
    virtualConnectionLbl.setToolTipText("Virtual Connection");
    statusPanel.add(virtualConnectionLbl);

    autopilotLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/cruise-control-on-green.png"))); // NOI18N
    autopilotLbl.setToolTipText("Autopilot running");
    statusPanel.add(autopilotLbl);

    add(statusPanel);

    measurePanel.setPreferredSize(new java.awt.Dimension(600, 45));
    java.awt.FlowLayout flowLayout3 = new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 10);
    flowLayout3.setAlignOnBaseline(true);
    measurePanel.setLayout(flowLayout3);

    voltageLbl.setText("-");
    voltageLbl.setToolTipText("Track Voltage");
    voltageLbl.setPreferredSize(new java.awt.Dimension(55, 20));
    measurePanel.add(voltageLbl);

    currentLbl.setText("-");
    currentLbl.setToolTipText("Track Current");
    currentLbl.setPreferredSize(new java.awt.Dimension(55, 20));
    measurePanel.add(currentLbl);

    tempLbl.setText("-");
    tempLbl.setToolTipText("Command Station Temperature");
    tempLbl.setPreferredSize(new java.awt.Dimension(55, 20));
    measurePanel.add(tempLbl);

    add(measurePanel);

    miscPanel.setPreferredSize(new java.awt.Dimension(100, 45));
    java.awt.FlowLayout flowLayout4 = new java.awt.FlowLayout();
    flowLayout4.setAlignOnBaseline(true);
    miscPanel.setLayout(flowLayout4);
    add(miscPanel);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel autopilotLbl;
  private javax.swing.JLabel connectedLbl;
  private javax.swing.JLabel currentLbl;
  private javax.swing.JPanel measurePanel;
  private javax.swing.JPanel miscPanel;
  private javax.swing.JPanel statusPanel;
  private javax.swing.JLabel tempLbl;
  private javax.swing.JLabel virtualConnectionLbl;
  private javax.swing.JLabel voltageLbl;
  // End of variables declaration//GEN-END:variables

  //For standalone testing only
  public static void main(String args[]) {
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error(ex);
    }

    JCS.getJcsCommandStation().connect();

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
