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
package jcs.ui.widgets;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.entities.CommandStationBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class SensorPanelWide extends JPanel {

  private final List<FeedbackPanel> feedbackPanels;

  /**
   * Creates new form FeedbackMonitorPanel
   */
  public SensorPanelWide() {
    feedbackPanels = new ArrayList<>();
    initComponents();
    postInit();
  }

  private void postInit() {
    if (PersistenceFactory.getService() != null) {
      List<CommandStationBean> csl = PersistenceFactory.getService().getCommandStations();
      int bus0len = 0;
      int bus1len = 0;
      int bus2len = 0;
      int bus3len = 0;
      int deviceId = 0;
      for (CommandStationBean fbc : csl) {
        if (fbc != null && fbc.isFeedbackSupport()) {
          if (fbc.getFeedbackBus0ModuleCount() != null) {
            bus0len = bus0len + fbc.getFeedbackBus0ModuleCount();
          }
          if (fbc.getFeedbackBus1ModuleCount() != null) {
            bus1len = bus1len + fbc.getFeedbackBus1ModuleCount();
          }
          if (fbc.getFeedbackBus2ModuleCount() != null) {
            bus2len = bus2len + fbc.getFeedbackBus2ModuleCount();
          }
          if (fbc.getFeedbackBus3ModuleCount() != null) {
            bus3len = bus3len + fbc.getFeedbackBus3ModuleCount();
          }
          if (fbc.getFeedbackModuleIdentifier() != null) {
            deviceId = Integer.parseInt(fbc.getFeedbackModuleIdentifier());
          }
        }
      }
      Logger.trace("Device Id: " + deviceId + " bus 0: " + bus0len + " bus 1: " + bus1len + " bus 2: " + bus2len + " bus 3: " + bus3len);

      //Assume there is always 1 module for bus 0
      this.feedbackPanels.add(bus0Mod1Panel);
      //Bus 0
      bus0Mod1Panel.setEnabled(bus0len > 0);
      bus0Mod1Panel.setTitle("Bus 0 Module 1");
      bus0Mod1Panel.setDeviceId(deviceId);
      bus0Mod1Panel.setContactIdOffset(0);
      if (bus0len > 0) {
        bus0Mod1Panel.registerSensorListeners();
      }
      for (int i = 1; i < bus0len; i++) {
        FeedbackPanel bus0ModXPanel = new FeedbackPanel((i + 1), deviceId, 0);
        bus0ModXPanel.setTitle("Bus 0 Module " + (i + 1));
        feedbackPanels.add(bus0ModXPanel);
        bus0ModXPanel.registerSensorListeners();
        this.bus0Panel.add(bus0ModXPanel);
      }
      int w = bus0Panel.getPreferredSize().width;
      int h = bus0Panel.getPreferredSize().height;
      w = w * (bus0len + 1);
      this.bus0Panel.setPreferredSize(new Dimension(w, h));
      this.bus0Panel.setMinimumSize(new Dimension(w, h));

      //Bus 1
      for (int i = 0; i < bus1len; i++) {
        FeedbackPanel bus1ModXPanel = new FeedbackPanel((i + 1), deviceId, 1000);
        bus1ModXPanel.setTitle("Node " + deviceId + " Bus 1 Module " + (i + 1));
        feedbackPanels.add(bus1ModXPanel);
        bus1ModXPanel.registerSensorListeners();
        this.bus1Panel.add(bus1ModXPanel);
      }
      w = bus1Panel.getPreferredSize().width;
      h = bus1Panel.getPreferredSize().height;
      w = w * (bus1len + 1);
      this.bus1Panel.setPreferredSize(new Dimension(w, h));
      this.bus1Panel.setMinimumSize(new Dimension(w, h));

      //Bus 2
      for (int i = 0; i < bus2len; i++) {
        FeedbackPanel bus2ModXPanel = new FeedbackPanel((i + 1), deviceId, 2000);
        bus2ModXPanel.setTitle("Node " + deviceId + " Bus 2 Module " + (i + 1));
        bus2ModXPanel.registerSensorListeners();
        this.bus2Panel.add(bus2ModXPanel);
      }
      w = bus2Panel.getPreferredSize().width;
      h = bus2Panel.getPreferredSize().height;
      w = w * (bus2len + 1);
      this.bus2Panel.setPreferredSize(new Dimension(w, h));
      this.bus2Panel.setMinimumSize(new Dimension(w, h));

      //Bus 3
      for (int i = 0; i < bus3len; i++) {
        FeedbackPanel bus3ModXPanel = new FeedbackPanel((i + 1), deviceId, 3000);
        bus3ModXPanel.setTitle("Node " + deviceId + " Bus 3 Module " + (i + 1));
        feedbackPanels.add(bus3ModXPanel);
        bus3ModXPanel.registerSensorListeners();
        this.bus3Panel.add(bus3ModXPanel);
      }
      w = bus3Panel.getPreferredSize().width;
      h = bus3Panel.getPreferredSize().height;
      w = w * (bus3len + 1);
      this.bus3Panel.setPreferredSize(new Dimension(w, h));
      this.bus3Panel.setMinimumSize(new Dimension(w, h));

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
    bus2SP = new JScrollPane();
    bus2Panel = new JPanel();
    bus3SP = new JScrollPane();
    bus3Panel = new JPanel();

    setMinimumSize(new Dimension(975, 150));
    setName("Form"); // NOI18N
    setPreferredSize(new Dimension(975, 150));
    addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent evt) {
        formComponentHidden(evt);
      }
      public void componentShown(ComponentEvent evt) {
        formComponentShown(evt);
      }
    });
    FlowLayout flowLayout4 = new FlowLayout(FlowLayout.LEFT, 0, 0);
    flowLayout4.setAlignOnBaseline(true);
    setLayout(flowLayout4);

    feedbackSensorTP.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    feedbackSensorTP.setTabPlacement(JTabbedPane.LEFT);
    feedbackSensorTP.setToolTipText("");
    feedbackSensorTP.setDoubleBuffered(true);
    feedbackSensorTP.setMinimumSize(new Dimension(975, 135));
    feedbackSensorTP.setName("feedbackSensorTP"); // NOI18N
    feedbackSensorTP.setPreferredSize(new Dimension(975, 150));

    bus0SP.setToolTipText("");
    bus0SP.setMinimumSize(new Dimension(975, 110));
    bus0SP.setName("bus0SP"); // NOI18N
    bus0SP.setPreferredSize(new Dimension(975, 110));
    bus0SP.setViewportView(bus0Panel);

    bus0Panel.setMinimumSize(new Dimension(250, 105));
    bus0Panel.setName("bus0Panel"); // NOI18N
    bus0Panel.setPreferredSize(new Dimension(250, 105));
    FlowLayout flowLayout6 = new FlowLayout(FlowLayout.LEFT, 1, 5);
    flowLayout6.setAlignOnBaseline(true);
    bus0Panel.setLayout(flowLayout6);

    bus0Mod1Panel.setName("bus0Mod1Panel"); // NOI18N
    bus0Mod1Panel.setTitle("Module 1");
    bus0Panel.add(bus0Mod1Panel);

    bus0SP.setViewportView(bus0Panel);

    feedbackSensorTP.addTab("Bus 0", bus0SP);

    bus1SP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    bus1SP.setMinimumSize(new Dimension(975, 110));
    bus1SP.setName("bus1SP"); // NOI18N
    bus1SP.setPreferredSize(new Dimension(975, 110));
    bus1SP.setViewportView(bus1Panel);

    bus1Panel.setMinimumSize(new Dimension(250, 105));
    bus1Panel.setName("bus1Panel"); // NOI18N
    bus1Panel.setPreferredSize(new Dimension(250, 105));
    FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT, 1, 5);
    flowLayout1.setAlignOnBaseline(true);
    bus1Panel.setLayout(flowLayout1);
    bus1SP.setViewportView(bus1Panel);

    feedbackSensorTP.addTab("Bus 1", bus1SP);

    bus2SP.setMinimumSize(new Dimension(975, 110));
    bus2SP.setName("bus2SP"); // NOI18N
    bus2SP.setPreferredSize(new Dimension(975, 110));
    bus2SP.setViewportView(bus2Panel);

    bus2Panel.setMinimumSize(new Dimension(250, 105));
    bus2Panel.setName("bus2Panel"); // NOI18N
    bus2Panel.setPreferredSize(new Dimension(250, 105));
    FlowLayout flowLayout3 = new FlowLayout(FlowLayout.LEFT, 1, 5);
    flowLayout3.setAlignOnBaseline(true);
    bus2Panel.setLayout(flowLayout3);
    bus2SP.setViewportView(bus2Panel);

    feedbackSensorTP.addTab("Bus 2", bus2SP);

    bus3SP.setMinimumSize(new Dimension(975, 110));
    bus3SP.setName("bus3SP"); // NOI18N
    bus3SP.setPreferredSize(new Dimension(975, 110));
    bus3SP.setViewportView(bus3Panel);

    bus3Panel.setMinimumSize(new Dimension(250, 105));
    bus3Panel.setName("bus3Panel"); // NOI18N
    bus3Panel.setPreferredSize(new Dimension(250, 105));
    FlowLayout flowLayout5 = new FlowLayout(FlowLayout.LEFT, 1, 5);
    flowLayout5.setAlignOnBaseline(true);
    bus3Panel.setLayout(flowLayout5);
    bus3SP.setViewportView(bus3Panel);

    feedbackSensorTP.addTab("Bus 3", bus3SP);

    add(feedbackSensorTP);
  }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
      // registerListeners();
    }//GEN-LAST:event_formComponentShown

    private void formComponentHidden(ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
      // removeListeners();
    }//GEN-LAST:event_formComponentHidden

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private FeedbackPanel bus0Mod1Panel;
  private JPanel bus0Panel;
  private JScrollPane bus0SP;
  private JPanel bus1Panel;
  private JScrollPane bus1SP;
  private JPanel bus2Panel;
  private JScrollPane bus2SP;
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
      SensorPanelWide testPanel = new SensorPanelWide();
      JFrame testFrame = new JFrame("ControllerPanel Tester");
      //this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/media/jcs-train-64.png")));
      URL iconUrl = SensorPanelWide.class.getResource("/media/jcs-train-2-512.png");
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
