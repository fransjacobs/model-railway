/*
 * Copyright (C) 2019 frans.
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
package jcs.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.text.SimpleDateFormat;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.JCS;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.commandStation.marklin.cs3.events.CanMessageEvent;
import jcs.commandStation.marklin.cs3.events.CanMessageListener;
import jcs.ui.widgets.FeedbackPanel;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class SensorModulePanel extends JPanel {

  /**
   * Creates new form FeedbackMonitorPanel
   */
  public SensorModulePanel() {
    initComponents();
    postInit();
  }

  void registerListeners() {
    if (JCS.getJcsCommandStation() != null) {
      this.bus0Mod1Panel.registerSensorListeners();
      //this.csModule2Panel.registerSensorListeners();
      //this.csModule3Panel.registerSensorListeners();
      //this.csModule4Panel.registerSensorListeners();

      this.bus1Module1Panel.registerSensorListeners();
      this.bus1Module2Panel.registerSensorListeners();
      this.bus1Module3Panel.registerSensorListeners();
      //this.bus1Module4Panel.registerSensorListeners();

      this.bus2Module1Panel.registerSensorListeners();
      //this.bus2Module2Panel.registerSensorListeners();
      //this.bus2Module3Panel.registerSensorListeners();
      //this.bus2Module4Panel.registerSensorListeners();

      this.bus3Mod1Panel.registerSensorListeners();
      //this.bus3Module2Panel.registerSensorListeners();
      //this.bus3Module3Panel.registerSensorListeners();
      //this.bus3Module4Panel.registerSensorListeners();
      Logger.trace("Added Sensor Listeners...");
    }
  }

  void removeListeners() {
    if (JCS.getJcsCommandStation() != null) {
      this.bus0Mod1Panel.removeSensorListeners();
      //this.csModule2Panel.removeSensorListeners();
      //this.csModule3Panel.removeSensorListeners();
      //this.csModule4Panel.removeSensorListeners();

      this.bus1Module1Panel.removeSensorListeners();
      this.bus1Module2Panel.removeSensorListeners();
      this.bus1Module3Panel.removeSensorListeners();
      //this.bus1Module4Panel.removeSensorListeners();

      this.bus2Module1Panel.removeSensorListeners();
      //this.bus2Module2Panel.removeSensorListeners();
      //this.bus2Module3Panel.removeSensorListeners();
      //this.bus2Module4Panel.removeSensorListeners();

      this.bus3Mod1Panel.removeSensorListeners();
      //this.bus3Module2Panel.removeSensorListeners();
      //this.bus3Module3Panel.removeSensorListeners();
      //this.bus3Module4Panel.removeSensorListeners();
      Logger.trace("Removed Sensor Listeners...");
    }
  }

  //TODO !
  private void postInit() {
    //Find the number of feedback modules from the JCSCommandStation LinkS88 busses
//    if (CommandStationFactory.getTrackController() != null && CommandStationFactory.getTrackController().getLinkSxx() != null) {
// 
//      LinkSxx linkSxx = CommandStationFactory.getTrackController().getLinkSxx();
//      int deviceId = linkSxx.getDeviceId();
//      //For now support only a max of 4 modules per bus, which
//      //should be sufficient for most tracks ;)
//      this.csModule1Panel.setContactIdOffset(0);
//      this.csModule1Panel.setDeviceId(deviceId);
//
//      this.csModule2Panel.setContactIdOffset(0);
//      this.csModule2Panel.setDeviceId(deviceId);
//
//      this.csModule3Panel.setContactIdOffset(0);
//      this.csModule3Panel.setDeviceId(deviceId);
//
//      this.csModule4Panel.setContactIdOffset(0);
//      this.csModule4Panel.setDeviceId(deviceId);
//
//      int bus1Offset = linkSxx.getContactIdOffset(1);
//      this.bus1Module1Panel.setContactIdOffset(bus1Offset);
//      this.bus1Module1Panel.setDeviceId(deviceId);
//
//      this.bus1Module2Panel.setContactIdOffset(bus1Offset);
//      this.bus1Module2Panel.setDeviceId(deviceId);
//
//      this.bus1Module3Panel.setContactIdOffset(bus1Offset);
//      this.bus1Module3Panel.setDeviceId(deviceId);
//
//      this.bus1Module4Panel.setContactIdOffset(bus1Offset);
//      this.bus1Module4Panel.setDeviceId(deviceId);
//
//      int bus2Offset = linkSxx.getContactIdOffset(2);
//      this.bus2Module1Panel.setContactIdOffset(bus2Offset);
//      this.bus2Module1Panel.setDeviceId(deviceId);
//
//      this.bus2Module2Panel.setContactIdOffset(bus2Offset);
//      this.bus2Module2Panel.setDeviceId(deviceId);
//      this.bus2Module2Panel.registerSensorListeners();
//
//      this.bus2Module3Panel.setContactIdOffset(bus2Offset);
//      this.bus2Module3Panel.setDeviceId(deviceId);
//
//      this.bus2Module4Panel.setContactIdOffset(bus2Offset);
//      this.bus2Module4Panel.setDeviceId(deviceId);
//
//      int bus3Offset = linkSxx.getContactIdOffset(3);
//      this.bus3Module1Panel.setContactIdOffset(bus3Offset);
//      this.bus3Module1Panel.setDeviceId(deviceId);
//
//      this.bus3Module2Panel.setContactIdOffset(bus3Offset);
//      this.bus3Module2Panel.setDeviceId(deviceId);
//
//      this.bus3Module3Panel.setContactIdOffset(bus3Offset);
//      this.bus3Module3Panel.setDeviceId(deviceId);
//
//      this.bus3Module4Panel.setContactIdOffset(bus3Offset);
//      this.bus3Module4Panel.setDeviceId(deviceId);
//    }
  }

  private class MessageListener implements CanMessageListener {

    private final SensorModulePanel diagnosticPanel;

    MessageListener(SensorModulePanel panel) {
      diagnosticPanel = panel;
    }

    @Override
    public void onCanMessage(CanMessageEvent canEvent) {
      CanMessage msg = canEvent.getCanMessage();

      StringBuilder sb = new StringBuilder();
      SimpleDateFormat sdf = new SimpleDateFormat("HH24:mm:ss.S");
      sb.append(sdf.format(canEvent.getEventDate()));
      sb.append(" ");
      sb.append(canEvent.getSourceAddress().getHostName());
      sb.append(" ");
      //sb.append(canEvent.getCanMessage().getMessageName());
      sb.append(" ");
      sb.append(canEvent.getCanMessage());
      sb.append("\n");

    }

  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    feedbackSensorTP = new JTabbedPane();
    bus0SP = new JScrollPane();
    bus0Panel = new JPanel();
    bus0Mod1Panel = new FeedbackPanel(1);
    bus1SP = new JScrollPane();
    bus1Panel = new JPanel();
    bus1Module1Panel = new FeedbackPanel(5);
    bus1Module2Panel = new FeedbackPanel(6);
    bus1Module3Panel = new FeedbackPanel(7);
    bus2SP = new JScrollPane();
    bus2Panel = new JPanel();
    bus2Module1Panel = new FeedbackPanel(5);
    bus3SP = new JScrollPane();
    bus3Panel = new JPanel();
    bus3Mod1Panel = new FeedbackPanel(5);

    setName("Form"); // NOI18N
    setPreferredSize(new Dimension(780, 150));
    addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent evt) {
        formComponentHidden(evt);
      }
      public void componentShown(ComponentEvent evt) {
        formComponentShown(evt);
      }
    });
    FlowLayout flowLayout4 = new FlowLayout(FlowLayout.CENTER, 0, 0);
    flowLayout4.setAlignOnBaseline(true);
    setLayout(flowLayout4);

    feedbackSensorTP.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    feedbackSensorTP.setToolTipText("");
    feedbackSensorTP.setDoubleBuffered(true);
    feedbackSensorTP.setMinimumSize(new Dimension(760, 200));
    feedbackSensorTP.setName("feedbackSensorTP"); // NOI18N
    feedbackSensorTP.setPreferredSize(new Dimension(762, 200));

    bus0SP.setToolTipText("");
    bus0SP.setName("bus0SP"); // NOI18N
    bus0SP.setPreferredSize(new Dimension(760, 110));
    bus0SP.setViewportView(bus0Panel);

    bus0Panel.setName("bus0Panel"); // NOI18N
    bus0Panel.setPreferredSize(new Dimension(760, 110));
    FlowLayout flowLayout6 = new FlowLayout(FlowLayout.LEFT, 1, 1);
    flowLayout6.setAlignOnBaseline(true);
    bus0Panel.setLayout(flowLayout6);

    bus0Mod1Panel.setName("bus0Mod1Panel"); // NOI18N
    bus0Mod1Panel.setTitle("Module 1");
    bus0Panel.add(bus0Mod1Panel);

    bus0SP.setViewportView(bus0Panel);

    feedbackSensorTP.addTab("Bus 0", bus0SP);

    bus1SP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    bus1SP.setMinimumSize(new Dimension(760, 110));
    bus1SP.setName("bus1SP"); // NOI18N
    bus1SP.setPreferredSize(new Dimension(760, 110));
    bus1SP.setViewportView(bus1Panel);

    bus1Panel.setMinimumSize(new Dimension(760, 110));
    bus1Panel.setName("bus1Panel"); // NOI18N
    bus1Panel.setPreferredSize(new Dimension(760, 110));
    FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT, 1, 1);
    flowLayout1.setAlignOnBaseline(true);
    bus1Panel.setLayout(flowLayout1);

    bus1Module1Panel.setName("bus1Module1Panel"); // NOI18N
    bus1Module1Panel.setTitle("Module 1");
    bus1Panel.add(bus1Module1Panel);

    bus1Module2Panel.setModuleNumber(2);
    bus1Module2Panel.setName("bus1Module2Panel"); // NOI18N
    bus1Module2Panel.setTitle("Module 2");
    bus1Panel.add(bus1Module2Panel);

    bus1Module3Panel.setModuleNumber(3);
    bus1Module3Panel.setName("bus1Module3Panel"); // NOI18N
    bus1Module3Panel.setTitle("Module 3");
    bus1Panel.add(bus1Module3Panel);

    bus1SP.setViewportView(bus1Panel);

    feedbackSensorTP.addTab("Bus 1", bus1SP);

    bus2SP.setName("bus2SP"); // NOI18N
    bus2SP.setPreferredSize(new Dimension(760, 110));
    bus2SP.setViewportView(bus2Panel);

    bus2Panel.setMinimumSize(new Dimension(760, 110));
    bus2Panel.setName("bus2Panel"); // NOI18N
    bus2Panel.setPreferredSize(new Dimension(760, 110));
    FlowLayout flowLayout3 = new FlowLayout(FlowLayout.LEFT, 1, 1);
    flowLayout3.setAlignOnBaseline(true);
    bus2Panel.setLayout(flowLayout3);

    bus2Module1Panel.setName("bus2Module1Panel"); // NOI18N
    bus2Module1Panel.setTitle("Module 1");
    bus2Panel.add(bus2Module1Panel);

    bus2SP.setViewportView(bus2Panel);

    feedbackSensorTP.addTab("Bus 2", bus2SP);

    bus3SP.setName("bus3SP"); // NOI18N
    bus3SP.setPreferredSize(new Dimension(760, 110));
    bus3SP.setViewportView(bus3Panel);

    bus3Panel.setMinimumSize(new Dimension(760, 110));
    bus3Panel.setName("bus3Panel"); // NOI18N
    bus3Panel.setPreferredSize(new Dimension(760, 110));
    FlowLayout flowLayout5 = new FlowLayout(FlowLayout.LEFT, 1, 1);
    flowLayout5.setAlignOnBaseline(true);
    bus3Panel.setLayout(flowLayout5);

    bus3Mod1Panel.setName("bus3Mod1Panel"); // NOI18N
    bus3Mod1Panel.setTitle("Module 1");
    bus3Panel.add(bus3Mod1Panel);

    bus3SP.setViewportView(bus3Panel);

    feedbackSensorTP.addTab("Bus 3", bus3SP);

    add(feedbackSensorTP);
  }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
      registerListeners();
    }//GEN-LAST:event_formComponentShown

    private void formComponentHidden(ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
      removeListeners();
    }//GEN-LAST:event_formComponentHidden

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private FeedbackPanel bus0Mod1Panel;
  private JPanel bus0Panel;
  private JScrollPane bus0SP;
  private FeedbackPanel bus1Module1Panel;
  private FeedbackPanel bus1Module2Panel;
  private FeedbackPanel bus1Module3Panel;
  private JPanel bus1Panel;
  private JScrollPane bus1SP;
  private FeedbackPanel bus2Module1Panel;
  private JPanel bus2Panel;
  private JScrollPane bus2SP;
  private FeedbackPanel bus3Mod1Panel;
  private JPanel bus3Panel;
  private JScrollPane bus3SP;
  private JTabbedPane feedbackSensorTP;
  // End of variables declaration//GEN-END:variables

  //Testing
  public static void main(String args[]) {
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error("Can't set the LookAndFeel: " + ex);
    }

    java.awt.EventQueue.invokeLater(() -> {
      SensorModulePanel testPanel = new SensorModulePanel();
      JFrame testFrame = new JFrame("ControllerPanel Tester");
      //this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/media/jcs-train-64.png")));
      URL iconUrl = SensorModulePanel.class.getResource("/media/jcs-train-2-512.png");
      if (iconUrl != null) {
        testFrame.setIconImage(new ImageIcon(iconUrl).getImage());
      }

      JFrame.setDefaultLookAndFeelDecorated(true);
      testFrame.add(testPanel);

      testFrame.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
          System.exit(0);
        }
      });
      testFrame.pack();
      testFrame.setLocationRelativeTo(null);
      testFrame.setVisible(true);
    });
  }
}
