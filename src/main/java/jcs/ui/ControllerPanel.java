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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.SimpleDateFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.devices.LinkSxx;
import jcs.controller.cs3.events.CanMessageEvent;
import jcs.controller.cs3.events.CanMessageListener;
import jcs.trackservice.TrackServiceFactory;
import jcs.ui.widgets.FeedbackPanel;
import jcs.ui.widgets.SwitchPanel;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class ControllerPanel extends JPanel {

    /**
     * Creates new form FeedbackMonitorPanel
     */
    public ControllerPanel() {
        initComponents();
        postInit();
    }

    void registerListeners() {
        this.csModule1Panel.registerSensorListeners();
        this.csModule2Panel.registerSensorListeners();
        this.csModule3Panel.registerSensorListeners();
        this.csModule4Panel.registerSensorListeners();

        this.bus1Module1Panel.registerSensorListeners();
        this.bus1Module2Panel.registerSensorListeners();
        this.bus1Module3Panel.registerSensorListeners();
        this.bus1Module4Panel.registerSensorListeners();

        this.bus2Module1Panel.registerSensorListeners();
        this.bus2Module2Panel.registerSensorListeners();
        this.bus2Module3Panel.registerSensorListeners();
        this.bus2Module4Panel.registerSensorListeners();

        this.bus3Module1Panel.registerSensorListeners();
        this.bus3Module2Panel.registerSensorListeners();
        this.bus3Module3Panel.registerSensorListeners();
        this.bus3Module4Panel.registerSensorListeners();
        Logger.trace("Added Sensor Listeners...");
    }

    void removeListeners() {
        this.csModule1Panel.removeSensorListeners();
        this.csModule2Panel.removeSensorListeners();
        this.csModule3Panel.removeSensorListeners();
        this.csModule4Panel.removeSensorListeners();

        this.bus1Module1Panel.removeSensorListeners();
        this.bus1Module2Panel.removeSensorListeners();
        this.bus1Module3Panel.removeSensorListeners();
        this.bus1Module4Panel.removeSensorListeners();

        this.bus2Module1Panel.removeSensorListeners();
        this.bus2Module2Panel.removeSensorListeners();
        this.bus2Module3Panel.removeSensorListeners();
        this.bus2Module4Panel.removeSensorListeners();

        this.bus3Module1Panel.removeSensorListeners();
        this.bus3Module2Panel.removeSensorListeners();
        this.bus3Module3Panel.removeSensorListeners();
        this.bus3Module4Panel.removeSensorListeners();
        Logger.trace("Removed Sensor Listeners...");
    }

    private void postInit() {
        //Find the number of feedback modules from the Controller LinkS88 busses
        if (TrackServiceFactory.getTrackService() != null && TrackServiceFactory.getTrackService().getLinkSxx() != null) {

            LinkSxx linkSxx = TrackServiceFactory.getTrackService().getLinkSxx();
            int deviceId = linkSxx.getDeviceId();
            //For now support only a max of 4 modules per bus, which
            //should be sufficient for most tracks ;)
            this.csModule1Panel.setContactIdOffset(0);
            this.csModule1Panel.setDeviceId(deviceId);

            this.csModule2Panel.setContactIdOffset(0);
            this.csModule2Panel.setDeviceId(deviceId);

            this.csModule3Panel.setContactIdOffset(0);
            this.csModule3Panel.setDeviceId(deviceId);

            this.csModule4Panel.setContactIdOffset(0);
            this.csModule4Panel.setDeviceId(deviceId);

            int bus1Offset = linkSxx.getContactIdOffset(1);
            this.bus1Module1Panel.setContactIdOffset(bus1Offset);
            this.bus1Module1Panel.setDeviceId(deviceId);

            this.bus1Module2Panel.setContactIdOffset(bus1Offset);
            this.bus1Module2Panel.setDeviceId(deviceId);

            this.bus1Module3Panel.setContactIdOffset(bus1Offset);
            this.bus1Module3Panel.setDeviceId(deviceId);

            this.bus1Module4Panel.setContactIdOffset(bus1Offset);
            this.bus1Module4Panel.setDeviceId(deviceId);

            int bus2Offset = linkSxx.getContactIdOffset(2);
            this.bus2Module1Panel.setContactIdOffset(bus2Offset);
            this.bus2Module1Panel.setDeviceId(deviceId);

            this.bus2Module2Panel.setContactIdOffset(bus2Offset);
            this.bus2Module2Panel.setDeviceId(deviceId);
            this.bus2Module2Panel.registerSensorListeners();

            this.bus2Module3Panel.setContactIdOffset(bus2Offset);
            this.bus2Module3Panel.setDeviceId(deviceId);

            this.bus2Module4Panel.setContactIdOffset(bus2Offset);
            this.bus2Module4Panel.setDeviceId(deviceId);

            int bus3Offset = linkSxx.getContactIdOffset(3);
            this.bus3Module1Panel.setContactIdOffset(bus3Offset);
            this.bus3Module1Panel.setDeviceId(deviceId);

            this.bus3Module2Panel.setContactIdOffset(bus3Offset);
            this.bus3Module2Panel.setDeviceId(deviceId);

            this.bus3Module3Panel.setContactIdOffset(bus3Offset);
            this.bus3Module3Panel.setDeviceId(deviceId);

            this.bus3Module4Panel.setContactIdOffset(bus3Offset);
            this.bus3Module4Panel.setDeviceId(deviceId);
        }
    }

    private class MessageListener implements CanMessageListener {

        private final ControllerPanel diagnosticPanel;

        MessageListener(ControllerPanel panel) {
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
            sb.append(canEvent.getCanMessage().getMessageName());
            sb.append(" ");
            sb.append(canEvent.getCanMessage());
            sb.append("\n");

            diagnosticPanel.logArea.append(sb.toString());

        }

    }

    private class LogTextAreaHandler implements CanMessageListener {

        private final JTextArea textArea;
        private int lines = 0;
        int lineHeight;

        LogTextAreaHandler(JTextArea textArea) {
            this.textArea = textArea;
            lineHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
        }

        @Override
        public void onCanMessage(CanMessageEvent canEvent) {
            CanMessage msg = canEvent.getCanMessage();

            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("HH24:mm:ss.S");
            sb.append(sdf.format(canEvent.getEventDate()));
            //sb.append(" [");
            //sb.append(canEvent.getSourceAddress().getHostName());
            //sb.append("] ");
            sb.append(": ");
            sb.append(canEvent.getCanMessage());
            sb.append(", ");
            sb.append(canEvent.getCanMessage().getMessageName());
            sb.append("\n");

            //if (EventQueue.isDispatchThread()) {
            //textArea.insert(sb.toString(), 1);
            //textArea.append(sb.toString());
            //textArea.setCaretPosition(textArea.getText().length());
            try {
                textArea.getDocument().insertString(0, sb.toString(), null);
                lines += 1;

                //int height = this.lineHeight * lines;
                int height = 30 * lines;
                //textArea.setSize(this.textArea.getWidth(), height);
                textArea.setPreferredSize(new Dimension(this.textArea.getWidth(), height));
            } catch (BadLocationException e1) {
                Logger.trace(e1);
            }
        }
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.error("Can't set the LookAndFeel: " + ex);
        }
        java.awt.EventQueue.invokeLater(() -> {

            ControllerPanel testPanel = new ControllerPanel();
            JFrame testFrame = new JFrame("ControllerPanel Tester");

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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new JPanel();
        feedbackSensorTP = new JTabbedPane();
        csSP = new JScrollPane();
        csPanel = new JPanel();
        csModule1Panel = new FeedbackPanel(1);
        csModule2Panel = new FeedbackPanel(2);
        csModule3Panel = new FeedbackPanel(3);
        csModule4Panel = new FeedbackPanel(4);
        bus1SP = new JScrollPane();
        bus1Panel = new JPanel();
        bus1Module1Panel = new FeedbackPanel(5);
        bus1Module2Panel = new FeedbackPanel(6);
        bus1Module3Panel = new FeedbackPanel(7);
        bus1Module4Panel = new FeedbackPanel(8);
        bus2SP = new JScrollPane();
        bus2Panel = new JPanel();
        bus2Module1Panel = new FeedbackPanel(5);
        bus2Module2Panel = new FeedbackPanel(6);
        bus2Module3Panel = new FeedbackPanel(7);
        bus2Module4Panel = new FeedbackPanel(8);
        bus3SP = new JScrollPane();
        bus3Panel = new JPanel();
        bus3Module1Panel = new FeedbackPanel(5);
        bus3Module2Panel = new FeedbackPanel(6);
        bus3Module3Panel = new FeedbackPanel(7);
        bus3Module4Panel = new FeedbackPanel(8);
        centerPanel = new JPanel();
        keyboardTP = new JTabbedPane();
        keyboardsPanel = new JPanel();
        switchPanel1 = new SwitchPanel(1);
        switchPanel2 = new SwitchPanel(2);
        switchPanel3 = new SwitchPanel(3);
        switchPanel4 = new SwitchPanel(4);
        keyboardsPanel1 = new JPanel();
        switchPanel5 = new SwitchPanel(5);
        switchPanel6 = new SwitchPanel(6);
        switchPanel7 = new SwitchPanel(7);
        switchPanel8 = new SwitchPanel(8);
        keyboardsPanel2 = new JPanel();
        switchPanel9 = new SwitchPanel(9);
        switchPanel10 = new SwitchPanel(10);
        switchPanel11 = new SwitchPanel(11);
        switchPanel12 = new SwitchPanel(12);
        keyboardsPanel3 = new JPanel();
        switchPanel13 = new SwitchPanel(13);
        switchPanel14 = new SwitchPanel(14);
        switchPanel15 = new SwitchPanel(15);
        switchPanel16 = new SwitchPanel(16);
        logPanel = new JPanel();
        logSP = new JScrollPane();
        logArea = new JTextArea();

        setMinimumSize(new Dimension(1010, 850));
        setName("Form"); // NOI18N
        setPreferredSize(new Dimension(1010, 850));
        addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent evt) {
                formComponentHidden(evt);
            }
            public void componentShown(ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        setLayout(new BorderLayout());

        topPanel.setMinimumSize(new Dimension(1000, 160));
        topPanel.setName("topPanel"); // NOI18N
        topPanel.setPreferredSize(new Dimension(1010, 160));
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        feedbackSensorTP.setToolTipText("");
        feedbackSensorTP.setDoubleBuffered(true);
        feedbackSensorTP.setMinimumSize(new Dimension(1010, 160));
        feedbackSensorTP.setName("feedbackSensorTP"); // NOI18N
        feedbackSensorTP.setPreferredSize(new Dimension(1030, 160));

        csSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        csSP.setMinimumSize(new Dimension(1000, 100));
        csSP.setName("csSP"); // NOI18N
        csSP.setPreferredSize(new Dimension(1000, 120));
        csSP.setViewportView(csPanel);

        csPanel.setMinimumSize(new Dimension(1000, 100));
        csPanel.setName("csPanel"); // NOI18N
        csPanel.setPreferredSize(new Dimension(1000, 100));
        FlowLayout flowLayout1 = new FlowLayout(FlowLayout.CENTER, 0, 0);
        flowLayout1.setAlignOnBaseline(true);
        csPanel.setLayout(flowLayout1);

        csModule1Panel.setName("csModule1Panel"); // NOI18N
        csModule1Panel.setTitle("Module 1");
        csPanel.add(csModule1Panel);

        csModule2Panel.setModuleNumber(2);
        csModule2Panel.setName("csModule2Panel"); // NOI18N
        csModule2Panel.setTitle("Module 2");
        csPanel.add(csModule2Panel);

        csModule3Panel.setModuleNumber(3);
        csModule3Panel.setName("csModule3Panel"); // NOI18N
        csModule3Panel.setTitle("Module 3");
        csPanel.add(csModule3Panel);

        csModule4Panel.setModuleNumber(4);
        csModule4Panel.setName("csModule4Panel"); // NOI18N
        csModule4Panel.setTitle("Module 4");
        csPanel.add(csModule4Panel);

        csSP.setViewportView(csPanel);

        feedbackSensorTP.addTab("Bus 0", csSP);

        bus1SP.setMinimumSize(new Dimension(1000, 100));
        bus1SP.setName("bus1SP"); // NOI18N
        bus1SP.setPreferredSize(new Dimension(1000, 120));

        bus1Panel.setMinimumSize(new Dimension(885, 140));
        bus1Panel.setName("bus1Panel"); // NOI18N
        bus1Panel.setPreferredSize(new Dimension(1000, 100));
        FlowLayout flowLayout2 = new FlowLayout(FlowLayout.CENTER, 0, 0);
        flowLayout2.setAlignOnBaseline(true);
        bus1Panel.setLayout(flowLayout2);

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

        bus1Module4Panel.setModuleNumber(4);
        bus1Module4Panel.setName("bus1Module4Panel"); // NOI18N
        bus1Module4Panel.setTitle("Module 4");
        bus1Panel.add(bus1Module4Panel);

        bus1SP.setViewportView(bus1Panel);

        feedbackSensorTP.addTab("Bus 1", bus1SP);

        bus2SP.setName("bus2SP"); // NOI18N

        bus2Panel.setMinimumSize(new Dimension(885, 140));
        bus2Panel.setName("bus2Panel"); // NOI18N
        bus2Panel.setPreferredSize(new Dimension(1000, 100));
        FlowLayout flowLayout3 = new FlowLayout(FlowLayout.CENTER, 0, 0);
        flowLayout3.setAlignOnBaseline(true);
        bus2Panel.setLayout(flowLayout3);

        bus2Module1Panel.setName("bus2Module1Panel"); // NOI18N
        bus2Module1Panel.setTitle("Module 1");
        bus2Panel.add(bus2Module1Panel);

        bus2Module2Panel.setModuleNumber(2);
        bus2Module2Panel.setName("bus2Module2Panel"); // NOI18N
        bus2Module2Panel.setTitle("Module 2");
        bus2Panel.add(bus2Module2Panel);

        bus2Module3Panel.setModuleNumber(3);
        bus2Module3Panel.setName("bus2Module3Panel"); // NOI18N
        bus2Module3Panel.setTitle("Module 3");
        bus2Panel.add(bus2Module3Panel);

        bus2Module4Panel.setModuleNumber(4);
        bus2Module4Panel.setName("bus2Module4Panel"); // NOI18N
        bus2Module4Panel.setTitle("Module 4");
        bus2Panel.add(bus2Module4Panel);

        bus2SP.setViewportView(bus2Panel);

        feedbackSensorTP.addTab("Bus 2", bus2SP);

        bus3SP.setName("bus3SP"); // NOI18N

        bus3Panel.setMinimumSize(new Dimension(885, 140));
        bus3Panel.setName("bus3Panel"); // NOI18N
        bus3Panel.setPreferredSize(new Dimension(1000, 100));
        FlowLayout flowLayout5 = new FlowLayout(FlowLayout.CENTER, 0, 0);
        flowLayout5.setAlignOnBaseline(true);
        bus3Panel.setLayout(flowLayout5);

        bus3Module1Panel.setName("bus3Module1Panel"); // NOI18N
        bus3Module1Panel.setTitle("Module 1");
        bus3Panel.add(bus3Module1Panel);

        bus3Module2Panel.setModuleNumber(2);
        bus3Module2Panel.setName("bus3Module2Panel"); // NOI18N
        bus3Module2Panel.setTitle("Module 2");
        bus3Panel.add(bus3Module2Panel);

        bus3Module3Panel.setModuleNumber(3);
        bus3Module3Panel.setName("bus3Module3Panel"); // NOI18N
        bus3Module3Panel.setTitle("Module 3");
        bus3Panel.add(bus3Module3Panel);

        bus3Module4Panel.setModuleNumber(4);
        bus3Module4Panel.setName("bus3Module4Panel"); // NOI18N
        bus3Module4Panel.setTitle("Module 4");
        bus3Panel.add(bus3Module4Panel);

        bus3SP.setViewportView(bus3Panel);

        feedbackSensorTP.addTab("Bus 3", bus3SP);

        topPanel.add(feedbackSensorTP);

        add(topPanel, BorderLayout.NORTH);

        centerPanel.setMinimumSize(new Dimension(885, 400));
        centerPanel.setName("centerPanel"); // NOI18N
        centerPanel.setPreferredSize(new Dimension(885, 400));
        centerPanel.setLayout(new BorderLayout());

        keyboardTP.setMinimumSize(new Dimension(885, 400));
        keyboardTP.setName("keyboardTP"); // NOI18N
        keyboardTP.setPreferredSize(new Dimension(885, 400));
        keyboardTP.setRequestFocusEnabled(false);

        keyboardsPanel.setMinimumSize(new Dimension(1200, 400));
        keyboardsPanel.setName("keyboardsPanel"); // NOI18N
        keyboardsPanel.setPreferredSize(new Dimension(885, 390));
        keyboardsPanel.setLayout(new BoxLayout(keyboardsPanel, BoxLayout.PAGE_AXIS));

        switchPanel1.setName("switchPanel1"); // NOI18N
        keyboardsPanel.add(switchPanel1);

        switchPanel2.setName("switchPanel2"); // NOI18N
        keyboardsPanel.add(switchPanel2);

        switchPanel3.setName("switchPanel3"); // NOI18N
        keyboardsPanel.add(switchPanel3);

        switchPanel4.setName("switchPanel4"); // NOI18N
        keyboardsPanel.add(switchPanel4);

        keyboardTP.addTab("1 - 64", keyboardsPanel);

        keyboardsPanel1.setMinimumSize(new Dimension(1200, 400));
        keyboardsPanel1.setName("keyboardsPanel1"); // NOI18N
        keyboardsPanel1.setPreferredSize(new Dimension(885, 390));
        keyboardsPanel1.setRequestFocusEnabled(false);
        keyboardsPanel1.setLayout(new BoxLayout(keyboardsPanel1, BoxLayout.PAGE_AXIS));

        switchPanel5.setName("switchPanel5"); // NOI18N
        keyboardsPanel1.add(switchPanel5);

        switchPanel6.setName("switchPanel6"); // NOI18N
        keyboardsPanel1.add(switchPanel6);

        switchPanel7.setName("switchPanel7"); // NOI18N
        keyboardsPanel1.add(switchPanel7);

        switchPanel8.setName("switchPanel8"); // NOI18N
        keyboardsPanel1.add(switchPanel8);

        keyboardTP.addTab("65 - 128", keyboardsPanel1);

        keyboardsPanel2.setMinimumSize(new Dimension(1200, 400));
        keyboardsPanel2.setName("keyboardsPanel2"); // NOI18N
        keyboardsPanel2.setPreferredSize(new Dimension(885, 390));
        keyboardsPanel2.setLayout(new BoxLayout(keyboardsPanel2, BoxLayout.PAGE_AXIS));

        switchPanel9.setName("switchPanel9"); // NOI18N
        keyboardsPanel2.add(switchPanel9);

        switchPanel10.setName("switchPanel10"); // NOI18N
        keyboardsPanel2.add(switchPanel10);

        switchPanel11.setName("switchPanel11"); // NOI18N
        keyboardsPanel2.add(switchPanel11);

        switchPanel12.setName("switchPanel12"); // NOI18N
        keyboardsPanel2.add(switchPanel12);

        keyboardTP.addTab("129 - 192", keyboardsPanel2);

        keyboardsPanel3.setMinimumSize(new Dimension(1200, 400));
        keyboardsPanel3.setName("keyboardsPanel3"); // NOI18N
        keyboardsPanel3.setPreferredSize(new Dimension(885, 390));
        keyboardsPanel3.setLayout(new BoxLayout(keyboardsPanel3, BoxLayout.PAGE_AXIS));

        switchPanel13.setName("switchPanel13"); // NOI18N
        keyboardsPanel3.add(switchPanel13);

        switchPanel14.setName("switchPanel14"); // NOI18N
        keyboardsPanel3.add(switchPanel14);

        switchPanel15.setName("switchPanel15"); // NOI18N
        keyboardsPanel3.add(switchPanel15);

        switchPanel16.setName("switchPanel16"); // NOI18N
        keyboardsPanel3.add(switchPanel16);

        keyboardTP.addTab("193 - 256", keyboardsPanel3);

        centerPanel.add(keyboardTP, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        logPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)), "Controller Messages"));
        logPanel.setMinimumSize(new Dimension(885, 280));
        logPanel.setName("logPanel"); // NOI18N
        logPanel.setPreferredSize(new Dimension(885, 280));
        logPanel.setLayout(new BorderLayout());

        logSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        logSP.setDoubleBuffered(true);
        logSP.setMinimumSize(new Dimension(870, 260));
        logSP.setName("logSP"); // NOI18N
        logSP.setPreferredSize(new Dimension(870, 260));
        logSP.setRequestFocusEnabled(false);
        logSP.setViewportView(logArea);

        logArea.setColumns(8);
        logArea.setRows(9);
        logArea.setDoubleBuffered(true);
        logArea.setEnabled(false);
        logArea.setMaximumSize(new Dimension(2147483647, 250));
        logArea.setMinimumSize(new Dimension(500, 260));
        logArea.setName("logArea"); // NOI18N
        logArea.setPreferredSize(new Dimension(500, 250));
        logSP.setViewportView(logArea);

        logPanel.add(logSP, BorderLayout.CENTER);

        add(logPanel, BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        registerListeners();
    }//GEN-LAST:event_formComponentShown

    private void formComponentHidden(ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
        removeListeners();
    }//GEN-LAST:event_formComponentHidden

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private FeedbackPanel bus1Module1Panel;
    private FeedbackPanel bus1Module2Panel;
    private FeedbackPanel bus1Module3Panel;
    private FeedbackPanel bus1Module4Panel;
    private JPanel bus1Panel;
    private JScrollPane bus1SP;
    private FeedbackPanel bus2Module1Panel;
    private FeedbackPanel bus2Module2Panel;
    private FeedbackPanel bus2Module3Panel;
    private FeedbackPanel bus2Module4Panel;
    private JPanel bus2Panel;
    private JScrollPane bus2SP;
    private FeedbackPanel bus3Module1Panel;
    private FeedbackPanel bus3Module2Panel;
    private FeedbackPanel bus3Module3Panel;
    private FeedbackPanel bus3Module4Panel;
    private JPanel bus3Panel;
    private JScrollPane bus3SP;
    private JPanel centerPanel;
    private FeedbackPanel csModule1Panel;
    private FeedbackPanel csModule2Panel;
    private FeedbackPanel csModule3Panel;
    private FeedbackPanel csModule4Panel;
    private JPanel csPanel;
    private JScrollPane csSP;
    private JTabbedPane feedbackSensorTP;
    private JTabbedPane keyboardTP;
    private JPanel keyboardsPanel;
    private JPanel keyboardsPanel1;
    private JPanel keyboardsPanel2;
    private JPanel keyboardsPanel3;
    private JTextArea logArea;
    private JPanel logPanel;
    private JScrollPane logSP;
    private SwitchPanel switchPanel1;
    private SwitchPanel switchPanel10;
    private SwitchPanel switchPanel11;
    private SwitchPanel switchPanel12;
    private SwitchPanel switchPanel13;
    private SwitchPanel switchPanel14;
    private SwitchPanel switchPanel15;
    private SwitchPanel switchPanel16;
    private SwitchPanel switchPanel2;
    private SwitchPanel switchPanel3;
    private SwitchPanel switchPanel4;
    private SwitchPanel switchPanel5;
    private SwitchPanel switchPanel6;
    private SwitchPanel switchPanel7;
    private SwitchPanel switchPanel8;
    private SwitchPanel switchPanel9;
    private JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
