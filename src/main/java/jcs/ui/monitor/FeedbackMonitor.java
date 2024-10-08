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
package jcs.ui.monitor;

import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Taskbar;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import jcs.JCS;
import jcs.ui.DriverCabFrame;
import jcs.ui.options.table.SensorTableModel;
import jcs.ui.util.FrameMonitor;
import jcs.ui.util.MacOsAdapter;
import jcs.util.RunUtil;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class FeedbackMonitor extends JFrame {

  private final SensorTableModel sensorTableModel;

  /**
   * Creates new form FeedbackMonitor
   */
  public FeedbackMonitor() {
    sensorTableModel = new SensorTableModel();

    if (RunUtil.isMacOSX()) {
      System.setProperty("apple.awt.application.name", "JCS Sensor Monitor");
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("apple.awt.application.appearance", "system");
    }

//    if (SystemInfo.isMacFullWindowContentSupported) {
//      this.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
//      this.getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
//      this.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
//    }
    initComponents();

    URL iconUrl = JCS.class.getResource("/media/jcs-train-64.png");
    if (iconUrl != null) {
      this.setIconImage(new ImageIcon(iconUrl).getImage());
    }

    alignSensorTable();
  }

  private void alignSensorTable() {
    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    this.sensorTable.getColumnModel().getColumn(0).setPreferredWidth(40);
    this.sensorTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

    this.sensorTable.getColumnModel().getColumn(1).setPreferredWidth(50);

    this.sensorTable.getColumnModel().getColumn(2).setPreferredWidth(40);
    this.sensorTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

    this.sensorTable.getColumnModel().getColumn(3).setPreferredWidth(60);
    this.sensorTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

    this.sensorTable.getColumnModel().getColumn(4).setPreferredWidth(40);
    this.sensorTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

    this.sensorTable.getColumnModel().getColumn(5).setPreferredWidth(50);
    this.sensorTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

    this.sensorTable.getColumnModel().getColumn(6).setPreferredWidth(50);
    this.sensorTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
  }

  public void showMonitor() {
    this.sensorTableModel.clear();
    //this.pack();
    //this.setLocationRelativeTo(null);
    this.setVisible(true);

  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    mainPanel = new javax.swing.JPanel();
    sensorTableSP = new javax.swing.JScrollPane();
    sensorTable = new javax.swing.JTable();
    topPanel = new javax.swing.JPanel();
    clearButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("JCS Sensor Monitor");
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowActivated(java.awt.event.WindowEvent evt) {
        formWindowActivated(evt);
      }
      public void windowClosed(java.awt.event.WindowEvent evt) {
        formWindowClosed(evt);
      }
    });

    mainPanel.setPreferredSize(new java.awt.Dimension(750, 300));

    sensorTableSP.setPreferredSize(new java.awt.Dimension(750, 345));

    sensorTable.setModel(sensorTableModel);
    sensorTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    sensorTable.setDoubleBuffered(true);
    sensorTable.setFillsViewportHeight(true);
    sensorTable.setGridColor(new java.awt.Color(204, 204, 204));
    sensorTable.setName(""); // NOI18N
    sensorTable.setRowHeight(18);
    sensorTable.setShowGrid(true);
    sensorTableSP.setViewportView(sensorTable);

    mainPanel.add(sensorTableSP);

    getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT);
    flowLayout1.setAlignOnBaseline(true);
    topPanel.setLayout(flowLayout1);

    clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/delete-24.png"))); // NOI18N
    clearButton.setToolTipText("Clear");
    clearButton.setMaximumSize(new java.awt.Dimension(40, 40));
    clearButton.setMinimumSize(new java.awt.Dimension(40, 40));
    clearButton.setPreferredSize(new java.awt.Dimension(40, 40));
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        clearButtonActionPerformed(evt);
      }
    });
    topPanel.add(clearButton);

    getContentPane().add(topPanel, java.awt.BorderLayout.PAGE_START);

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
      Logger.trace(evt.getActionCommand());
      sensorTableModel.clear();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
      if (JCS.getJcsCommandStation() != null) {
        JCS.getJcsCommandStation().removeSensorEventListener(sensorTableModel);
        Logger.trace(evt.getNewState() + " Removed sensor listener");
      }
      this.sensorTableModel.clear();
    }//GEN-LAST:event_formWindowClosed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
      if (JCS.getJcsCommandStation() != null) {
        JCS.getJcsCommandStation().addSensorEventListener(sensorTableModel);
        Logger.trace(evt.getNewState() + " Added sensor listener");
      }
    }//GEN-LAST:event_formWindowActivated

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton clearButton;
  private javax.swing.JPanel mainPanel;
  private javax.swing.JTable sensorTable;
  private javax.swing.JScrollPane sensorTableSP;
  private javax.swing.JPanel topPanel;
  // End of variables declaration//GEN-END:variables

  public static void main(String args[]) {
    try {
      String plaf = System.getProperty("jcs.plaf", "com.formdev.flatlaf.FlatLightLaf");
      UIManager.setLookAndFeel(plaf);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error(ex);
    }

    java.awt.EventQueue.invokeLater(() -> {
      FeedbackMonitor monitorFrame = new FeedbackMonitor();
      FrameMonitor.registerFrame(monitorFrame, FeedbackMonitor.class.getName());

      //String frameImageUrl = "/media/jcs-train-64.png";
      String frameImageUrl = "/media/jcs-train-2-512.png";

      if (RunUtil.isMacOSX()) {
        MacOsAdapter.setMacOsProperties();
        Taskbar taskbar = Taskbar.getTaskbar();
        try {
          BufferedImage img = ImageIO.read(DriverCabFrame.class.getResource(frameImageUrl));
          taskbar.setIconImage(img);
        } catch (IOException | UnsupportedOperationException | SecurityException ex) {
          Logger.warn("Error: " + ex.getMessage());
        }
      }
      URL iconUrl = FeedbackMonitor.class.getResource(frameImageUrl);
      if (iconUrl != null) {
        monitorFrame.setIconImage(new ImageIcon(iconUrl).getImage());
      }

      monitorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
          System.exit(0);
        }
      });

      monitorFrame.showMonitor();
    });
  }
}
