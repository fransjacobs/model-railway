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
package jcs.ui;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.awt.Color;
import jcs.commandStation.lifecycle.ConnectionState;
import jcs.commandStation.lifecycle.ConnectionStateEvent;
import jcs.JCS;
import jcs.commandStation.automation.RailController;
import jcs.commandStation.events.MeasurementEvent;
import jcs.commandStation.events.MeasurementEventListener;
import org.tinylog.Logger;
import jcs.commandStation.automation.RailControllerStatusListener;

/**
 *
 * The Status Panel displays the Connection Status of the Command Station,<br>
 * The Measured Values and<br>
 * The status of the RailController
 */
public class StatusPanel extends javax.swing.JPanel implements MeasurementEventListener, RailControllerStatusListener {

  private static final long serialVersionUID = 7132844382832996985L;

  private boolean supportMeasuments;

  private volatile ConnectionState displayedConnectionState = ConnectionState.DISCONNECTED;

  private static final String STATE_ICON_PREFIX = "/media/connection-state-";
  private static final Color STATE_CONNECTED_COLOR = new Color(24, 116, 58);
  private static final Color STATE_TRANSITION_COLOR = new Color(154, 105, 0);
  private static final Color STATE_DISCONNECTED_COLOR = new Color(165, 45, 45);

  private static final String CRUISE_CONTROL_B = "/media/cruise-control-on-black.png";
  private static final String CRUISE_CONTROL_G = "/media/cruise-control-on-green.png";
  private static final String CRUISE_CONTROL_R = "/media/cruise-control-on-red.png";
  private static final String CRUISE_CONTROL_Y = "/media/cruise-control-on-yellow.png";

  /**
   * Creates new form StatusPanel
   */
  public StatusPanel() {
    this(true);
  }

  /**
   * Allows isolated Swing tests to create the panel without initializing
   * application-wide JCS singletons or registering event listeners.
   * NetBeans-generated initComponents() remains unchanged.
   */
  StatusPanel(boolean registerApplicationListeners) {
    initComponents();
    if (registerApplicationListeners) {
      postInit();
    } else {
      virtualConnectionLbl.setVisible(false);
      railControllerStatusLbl.setVisible(false);
      currentLbl.setVisible(false);
      voltageLbl.setVisible(false);
      tempLbl.setVisible(false);
      updateConnectionState(ConnectionState.DISCONNECTED, null);
    }
  }

  private void postInit() {
    virtualConnectionLbl.setVisible(false);
    railControllerStatusLbl.setVisible(false);

    currentLbl.setVisible(supportMeasuments);
    voltageLbl.setVisible(supportMeasuments);
    tempLbl.setVisible(supportMeasuments);

    if (JCS.getJcsCommandStation() != null) {
      var station = JCS.getJcsCommandStation();

      station.addConnectionStateListener(this::onConnectionStateChanged);
      station.addMeasurementEventListener(this);

      Logger.trace("Added StatusPanel as ConnectionStateListener and MeasurementEventListener");
      JCS.getRailController().addStatusListener(this);

      updateConnectionState(station.getConnectionState(), null);
      virtualConnectionLbl.setVisible(station.isVirtual());
    } else {
      updateConnectionState(ConnectionState.DISCONNECTED, null);
    }
  }

  private void onConnectionStateChanged(ConnectionStateEvent event) {
    updateConnectionState(event.state(), event.message());
  }

  /**
   * Render the lifecycle state; never mutate Swing components on a transport thread,<br>
   * the lifecycle scheduler, or the railway controller thread.
   */
  private void updateConnectionState(ConnectionState state, String message) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> updateConnectionState(state, message));
      return;
    }
    final ConnectionState effectiveState = state != null ? state : ConnectionState.DISCONNECTED;
    displayedConnectionState = effectiveState;

    String label = switch (effectiveState) {
      case DISCONNECTED ->
        "Disconnected";
      case DISCOVERING ->
        "Discovering...";
      case CONNECTING ->
        "Connecting...";
      case CONNECTED ->
        "Connected";
      case RECONNECTING ->
        "Reconnecting...";
      case STOPPING ->
        "Disconnecting...";
    };

    String iconName = effectiveState.name().toLowerCase(java.util.Locale.ROOT);
    java.net.URL iconUrl = getClass().getResource(STATE_ICON_PREFIX + iconName + ".png");
    if (iconUrl != null) {
      statusLbl.setText(label);
      statusIconLbl.setIcon(new ImageIcon(iconUrl));

    }

    Color color = switch (effectiveState) {
      case CONNECTED ->
        STATE_CONNECTED_COLOR;
      case DISCONNECTED ->
        STATE_DISCONNECTED_COLOR;
      default ->
        STATE_TRANSITION_COLOR;
    };

    statusLbl.setForeground(color);
    String detail = (message == null || message.isBlank()) ? label : label + " — " + message;
    statusIconLbl.setToolTipText(detail);
    statusLbl.setToolTipText(detail);
    statusIconLbl.setVisible(true);

    boolean showMeasurements = supportMeasuments && effectiveState == ConnectionState.CONNECTED;
    currentLbl.setVisible(showMeasurements);
    voltageLbl.setVisible(showMeasurements);
    tempLbl.setVisible(showMeasurements);
    if (effectiveState != ConnectionState.CONNECTED) {
      // Measurements from the former session must not look live while
      // disconnected, discovering or retrying.
      currentLbl.setText("-");
      voltageLbl.setText("-");
      tempLbl.setText("-");
    }
  }

  @Override
  public void onMeasurement(MeasurementEvent event) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> onMeasurement(event));
      return;
    }
    supportMeasuments = event.isMeasurementsEnabled();
    boolean displayMeasurements = supportMeasuments
            && displayedConnectionState == ConnectionState.CONNECTED;
    currentLbl.setVisible(displayMeasurements);
    voltageLbl.setVisible(displayMeasurements);
    tempLbl.setVisible(displayMeasurements);

    if (!displayMeasurements) {
      currentLbl.setText("-");
      voltageLbl.setText("-");
      tempLbl.setText("-");
      return;
    }

    currentLbl.setText(event.getMain() != null
            ? event.getMain().getDisplayValue() + " " + event.getMain().getUnit() : "-");
    voltageLbl.setText(event.getVolt() != null
            ? event.getVolt().getDisplayValue() + " " + event.getVolt().getUnit() : "-");
    tempLbl.setText(event.getTemp() != null
            ? event.getTemp().getDisplayValue() + " " + event.getTemp().getUnit() : "-");
  }

  @Override
  public void onControllerStatusChange(String status) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> onControllerStatusChange(status));
      return;
    }
    Logger.trace(status);

    if (status == null) {
      return;
    }

    switch (status) {
      case RailController.PENDING -> {
        this.railControllerStatusLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource(CRUISE_CONTROL_B)));
      }
      case RailController.STARTED -> {
        this.railControllerStatusLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource(CRUISE_CONTROL_G)));
      }
      case RailController.STOPPING -> {
        this.railControllerStatusLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource(CRUISE_CONTROL_Y)));
      }
      case RailController.STOPPED -> {
        this.railControllerStatusLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource(CRUISE_CONTROL_R)));
      }
    }

    this.railControllerStatusLbl.setVisible(true);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    statusPanel = new javax.swing.JPanel();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(40, 0), new java.awt.Dimension(40, 0), new java.awt.Dimension(20, 32767));
    statusIconLbl = new javax.swing.JLabel();
    statusLbl = new javax.swing.JLabel();
    virtualConnectionLbl = new javax.swing.JLabel();
    miscPanel = new javax.swing.JPanel();
    railControllerStatusLbl = new javax.swing.JLabel();
    filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(100, 0), new java.awt.Dimension(50, 0), new java.awt.Dimension(100, 32767));
    measurePanel = new javax.swing.JPanel();
    voltageLbl = new javax.swing.JLabel();
    currentLbl = new javax.swing.JLabel();
    tempLbl = new javax.swing.JLabel();

    setMinimumSize(new java.awt.Dimension(1200, 45));
    setPreferredSize(new java.awt.Dimension(1200, 45));
    setLayout(new java.awt.BorderLayout());

    statusPanel.setPreferredSize(new java.awt.Dimension(400, 45));
    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10);
    flowLayout1.setAlignOnBaseline(true);
    statusPanel.setLayout(flowLayout1);
    statusPanel.add(filler1);

    statusIconLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/connection-state-disconnected.png"))); // NOI18N
    statusIconLbl.setToolTipText("Command station connection state");
    statusPanel.add(statusIconLbl);

    statusLbl.setText("Disconnected");
    statusLbl.setToolTipText("Command station connection state");
    statusLbl.setPreferredSize(new java.awt.Dimension(100, 17));
    statusPanel.add(statusLbl);

    virtualConnectionLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/virtual-24.png"))); // NOI18N
    virtualConnectionLbl.setText("Virtual Connection");
    virtualConnectionLbl.setToolTipText("Virtual Connection");
    statusPanel.add(virtualConnectionLbl);

    add(statusPanel, java.awt.BorderLayout.WEST);

    miscPanel.setPreferredSize(new java.awt.Dimension(100, 45));
    java.awt.FlowLayout flowLayout4 = new java.awt.FlowLayout();
    flowLayout4.setAlignOnBaseline(true);
    miscPanel.setLayout(flowLayout4);

    railControllerStatusLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/cruise-control-on-green.png"))); // NOI18N
    railControllerStatusLbl.setToolTipText("Autopilot running");
    miscPanel.add(railControllerStatusLbl);
    miscPanel.add(filler3);

    add(miscPanel, java.awt.BorderLayout.CENTER);

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

    add(measurePanel, java.awt.BorderLayout.EAST);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel currentLbl;
  private javax.swing.Box.Filler filler1;
  private javax.swing.Box.Filler filler3;
  private javax.swing.JPanel measurePanel;
  private javax.swing.JPanel miscPanel;
  private javax.swing.JLabel railControllerStatusLbl;
  private javax.swing.JLabel statusIconLbl;
  private javax.swing.JLabel statusLbl;
  private javax.swing.JPanel statusPanel;
  private javax.swing.JLabel tempLbl;
  private javax.swing.JLabel virtualConnectionLbl;
  private javax.swing.JLabel voltageLbl;
  // End of variables declaration//GEN-END:variables

}
