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
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import jcs.controller.cs3.DeviceInfo;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.events.CanMessageEvent;
import jcs.controller.cs3.events.CanMessageListener;
import jcs.trackservice.TrackServiceFactory;
import jcs.trackservice.events.HeartBeatListener;
import jcs.ui.widgets.FeedbackPanel;
import jcs.ui.widgets.LocoPanel;
import jcs.ui.widgets.SwitchPanel;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class DiagnosticPanel extends JPanel {

    private final ImageIcon blinkOnIcon;
    private final ImageIcon blinkOffIcon;
    private boolean blinkToggle;

    /**
     * Creates new form FeedbackMonitorPanel
     */
    public DiagnosticPanel() {
        blinkOnIcon = new ImageIcon(DiagnosticPanel.class.getResource("/media/monitor-off-24.png"));
        blinkOffIcon = new ImageIcon(DiagnosticPanel.class.getResource("/media/monitor-on-24.png"));

        initComponents();

        postInit();
    }

    private void postInit() {
        if (TrackServiceFactory.getTrackService() != null) {
            DeviceInfo di = TrackServiceFactory.getTrackService().getControllerInfo();

            if (di != null) {
                this.controllerDescriptionLbl.setText(di.getDescription());
                this.controllerCatalogNumberLbl.setText(di.getCatalogNumber());
                this.controllerSerialNumberLbl.setText(di.getSerialNumber());
                this.controllerHostNameLbl.setText(di.getDeviceHostName());
            }

            TrackServiceFactory.getTrackService().addHeartBeatListener(new HeartBeat(this));
            TrackServiceFactory.getTrackService().addMessageListener(new LogTextAreaHandler(this.logArea));
        }
    }

    public void refreshPanel() {
        this.locPanel1.refreshPanel();
        this.locPanel2.refreshPanel();
        this.locPanel3.refreshPanel();
        this.locPanel4.refreshPanel();
        this.locPanel5.refreshPanel();
        this.locPanel6.refreshPanel();
    }

    private void toggle() {
        blinkToggle = !blinkToggle;
        if (blinkToggle) {
            this.blinkLbl.setIcon(blinkOnIcon);
        } else {
            this.blinkLbl.setIcon(blinkOffIcon);
        }
    }

    private class MessageListener implements CanMessageListener {

        private final DiagnosticPanel diagnosticPanel;

        MessageListener(DiagnosticPanel panel) {
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

//    public static void main(String args[]) {
//        Configurator.defaultConfig().level(org.pmw.tinylog.Level.DEBUG).activate();
//
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//            Logger.error(ex);
//        }
//
//        java.awt.EventQueue.invokeLater(() -> {
//            JFrame f = new JFrame("Diagnostics Panel Tester");
//            DiagnosticPanel diagnosticPanel = new DiagnosticPanel();
//            f.add(diagnosticPanel);
//
//            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            f.pack();
//            f.setVisible(true);
//        });
//    }
    private class HeartBeat implements HeartBeatListener {

        private final DiagnosticPanel diagnosticPanel;

        HeartBeat(DiagnosticPanel panel) {
            diagnosticPanel = panel;
        }

        @Override
        public void toggle() {
            this.diagnosticPanel.toggle();
        }
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
        topLeftPanel = new JPanel();
        blinkLbl = new JLabel();
        topCenterPanel = new JPanel();
        feedbackTPjTabbedPane2 = new JTabbedPane();
        feedback1to3Panel = new JPanel();
        feedbackPanel1 = new FeedbackPanel(1);
        feedbackPanel2 = new FeedbackPanel(2);
        feedbackPanel3 = new FeedbackPanel(3);
        feedback4to6Panel = new JPanel();
        feedbackPanel4 = new FeedbackPanel(4);
        feedbackPanel5 = new FeedbackPanel(5);
        feedbackPanel6 = new FeedbackPanel(6);
        topRightPanel = new JPanel();
        filler2 = new Box.Filler(new Dimension(0, 35), new Dimension(0, 35), new Dimension(32767, 35));
        filler3 = new Box.Filler(new Dimension(0, 35), new Dimension(0, 35), new Dimension(32767, 35));
        controllerLbl = new JLabel();
        controllerDescriptionLbl = new JLabel();
        controllerCatalogLbl = new JLabel();
        controllerCatalogNumberLbl = new JLabel();
        controllerSerialLbl = new JLabel();
        controllerSerialNumberLbl = new JLabel();
        controllerHostLbl = new JLabel();
        controllerHostNameLbl = new JLabel();
        leftPanel = new JPanel();
        centerPanel = new JPanel();
        centerCenterPanel = new JPanel();
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
        centerRightPanel = new JPanel();
        locoTP = new JTabbedPane();
        locoPanels1to3 = new JPanel();
        locPanel1 = new LocoPanel(1);
        locPanel2 = new LocoPanel(2);
        locPanel3 = new LocoPanel(3);
        locoPanels4to6 = new JPanel();
        locPanel4 = new LocoPanel(4);
        locPanel5 = new LocoPanel(5);
        locPanel6 = new LocoPanel(6);

        setMinimumSize(new Dimension(1200, 840));
        setName("Form"); // NOI18N
        setPreferredSize(new Dimension(1200, 840));
        setLayout(new BorderLayout());

        topPanel.setMinimumSize(new Dimension(1200, 140));
        topPanel.setName("topPanel"); // NOI18N
        topPanel.setPreferredSize(new Dimension(1200, 140));
        topPanel.setLayout(new BorderLayout());

        topLeftPanel.setName("topLeftPanel"); // NOI18N
        topLeftPanel.setPreferredSize(new Dimension(50, 140));
        FlowLayout flowLayout1 = new FlowLayout(FlowLayout.CENTER, 10, 10);
        flowLayout1.setAlignOnBaseline(true);
        topLeftPanel.setLayout(flowLayout1);

        blinkLbl.setIcon(new ImageIcon(getClass().getResource("/media/monitor-off-24.png"))); // NOI18N
        blinkLbl.setName("blinkLbl"); // NOI18N
        topLeftPanel.add(blinkLbl);

        topPanel.add(topLeftPanel, BorderLayout.WEST);

        topCenterPanel.setName("topCenterPanel"); // NOI18N
        topCenterPanel.setPreferredSize(new Dimension(845, 145));
        topCenterPanel.setLayout(new GridLayout(1, 1));

        feedbackTPjTabbedPane2.setToolTipText("");
        feedbackTPjTabbedPane2.setName("feedbackTPjTabbedPane2"); // NOI18N
        feedbackTPjTabbedPane2.setPreferredSize(new Dimension(900, 141));

        feedback1to3Panel.setName("feedback1to3Panel"); // NOI18N
        feedback1to3Panel.setPreferredSize(new Dimension(845, 600));
        feedback1to3Panel.setLayout(new GridLayout(1, 3));

        feedbackPanel1.setName("feedbackPanel1"); // NOI18N
        feedbackPanel1.setTitle("Module - 1");
        feedback1to3Panel.add(feedbackPanel1);

        feedbackPanel2.setName("feedbackPanel2"); // NOI18N
        feedbackPanel2.setTitle("Module - 2");
        feedback1to3Panel.add(feedbackPanel2);

        feedbackPanel3.setName("feedbackPanel3"); // NOI18N
        feedbackPanel3.setTitle("Module - 3");
        feedback1to3Panel.add(feedbackPanel3);

        feedbackTPjTabbedPane2.addTab("1 - 3", feedback1to3Panel);

        feedback4to6Panel.setName("feedback4to6Panel"); // NOI18N
        feedback4to6Panel.setLayout(new GridLayout(1, 3));

        feedbackPanel4.setName("feedbackPanel4"); // NOI18N
        feedbackPanel4.setTitle("Module - 4");
        feedback4to6Panel.add(feedbackPanel4);

        feedbackPanel5.setName("feedbackPanel5"); // NOI18N
        feedbackPanel5.setTitle("Module - 5");
        feedback4to6Panel.add(feedbackPanel5);

        feedbackPanel6.setName("feedbackPanel6"); // NOI18N
        feedbackPanel6.setTitle("Module - 6");
        feedback4to6Panel.add(feedbackPanel6);

        feedbackTPjTabbedPane2.addTab("4 - 5", feedback4to6Panel);

        topCenterPanel.add(feedbackTPjTabbedPane2);

        topPanel.add(topCenterPanel, BorderLayout.CENTER);

        topRightPanel.setMinimumSize(new Dimension(260, 10));
        topRightPanel.setName("topRightPanel"); // NOI18N
        topRightPanel.setPreferredSize(new Dimension(350, 10));
        topRightPanel.setLayout(new GridLayout(5, 2, 2, 0));

        filler2.setName("filler2"); // NOI18N
        topRightPanel.add(filler2);

        filler3.setName("filler3"); // NOI18N
        topRightPanel.add(filler3);

        controllerLbl.setHorizontalAlignment(SwingConstants.TRAILING);
        controllerLbl.setText("Controller:");
        controllerLbl.setName("controllerLbl"); // NOI18N
        topRightPanel.add(controllerLbl);

        controllerDescriptionLbl.setText("...");
        controllerDescriptionLbl.setName("controllerDescriptionLbl"); // NOI18N
        topRightPanel.add(controllerDescriptionLbl);

        controllerCatalogLbl.setHorizontalAlignment(SwingConstants.TRAILING);
        controllerCatalogLbl.setLabelFor(controllerCatalogNumberLbl);
        controllerCatalogLbl.setText("Controller Model:");
        controllerCatalogLbl.setName("controllerCatalogLbl"); // NOI18N
        topRightPanel.add(controllerCatalogLbl);

        controllerCatalogNumberLbl.setText("...");
        controllerCatalogNumberLbl.setName("controllerCatalogNumberLbl"); // NOI18N
        topRightPanel.add(controllerCatalogNumberLbl);

        controllerSerialLbl.setHorizontalAlignment(SwingConstants.TRAILING);
        controllerSerialLbl.setText("Controller Serial:");
        controllerSerialLbl.setName("controllerSerialLbl"); // NOI18N
        topRightPanel.add(controllerSerialLbl);

        controllerSerialNumberLbl.setText("...");
        controllerSerialNumberLbl.setName("controllerSerialNumberLbl"); // NOI18N
        topRightPanel.add(controllerSerialNumberLbl);

        controllerHostLbl.setHorizontalAlignment(SwingConstants.TRAILING);
        controllerHostLbl.setText("Controller Host:");
        controllerHostLbl.setName("controllerHostLbl"); // NOI18N
        topRightPanel.add(controllerHostLbl);

        controllerHostNameLbl.setText("...");
        controllerHostNameLbl.setName("controllerHostNameLbl"); // NOI18N
        topRightPanel.add(controllerHostNameLbl);

        topPanel.add(topRightPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        leftPanel.setMinimumSize(new Dimension(275, 700));
        leftPanel.setName("leftPanel"); // NOI18N
        leftPanel.setPreferredSize(new Dimension(50, 700));
        add(leftPanel, BorderLayout.WEST);

        centerPanel.setMinimumSize(new Dimension(1200, 700));
        centerPanel.setName("centerPanel"); // NOI18N
        centerPanel.setPreferredSize(new Dimension(1150, 700));
        centerPanel.setLayout(new BorderLayout());

        centerCenterPanel.setMinimumSize(new Dimension(850, 700));
        centerCenterPanel.setName("centerCenterPanel"); // NOI18N
        centerCenterPanel.setPreferredSize(new Dimension(850, 700));
        centerCenterPanel.setLayout(new GridLayout(2, 1, 1, 2));

        keyboardTP.setMinimumSize(new Dimension(840, 290));
        keyboardTP.setName("keyboardTP"); // NOI18N
        keyboardTP.setPreferredSize(new Dimension(840, 290));
        keyboardTP.setRequestFocusEnabled(false);

        keyboardsPanel.setMinimumSize(new Dimension(845, 610));
        keyboardsPanel.setName("keyboardsPanel"); // NOI18N
        keyboardsPanel.setPreferredSize(new Dimension(600, 75));
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

        keyboardsPanel1.setName("keyboardsPanel1"); // NOI18N
        keyboardsPanel1.setPreferredSize(new Dimension(600, 300));
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

        keyboardsPanel2.setName("keyboardsPanel2"); // NOI18N
        keyboardsPanel2.setPreferredSize(new Dimension(600, 300));
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

        keyboardsPanel3.setName("keyboardsPanel3"); // NOI18N
        keyboardsPanel3.setPreferredSize(new Dimension(600, 300));
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

        centerCenterPanel.add(keyboardTP);

        logPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)), "Controller Messages"));
        logPanel.setMinimumSize(new Dimension(850, 300));
        logPanel.setName("logPanel"); // NOI18N
        logPanel.setPreferredSize(new Dimension(850, 300));
        FlowLayout flowLayout2 = new FlowLayout(FlowLayout.LEFT);
        flowLayout2.setAlignOnBaseline(true);
        logPanel.setLayout(flowLayout2);

        logSP.setMinimumSize(new Dimension(850, 300));
        logSP.setName("logSP"); // NOI18N
        logSP.setPreferredSize(new Dimension(860, 300));
        logSP.setRequestFocusEnabled(false);
        logSP.setViewportView(logArea);

        logArea.setColumns(10);
        logArea.setRows(10);
        logArea.setDoubleBuffered(true);
        logArea.setEnabled(false);
        logArea.setMaximumSize(new Dimension(2147483647, 290));
        logArea.setMinimumSize(new Dimension(900, 290));
        logArea.setName("logArea"); // NOI18N
        logArea.setPreferredSize(new Dimension(800, 20));
        logSP.setViewportView(logArea);

        logPanel.add(logSP);

        centerCenterPanel.add(logPanel);

        centerPanel.add(centerCenterPanel, BorderLayout.CENTER);

        centerRightPanel.setMinimumSize(new Dimension(300, 700));
        centerRightPanel.setName("centerRightPanel"); // NOI18N
        centerRightPanel.setPreferredSize(new Dimension(310, 700));
        centerRightPanel.setLayout(new BorderLayout());

        locoTP.setMinimumSize(new Dimension(240, 706));
        locoTP.setName("locoTP"); // NOI18N

        locoPanels1to3.setMinimumSize(new Dimension(230, 660));
        locoPanels1to3.setName("locoPanels1to3"); // NOI18N
        locoPanels1to3.setLayout(new BoxLayout(locoPanels1to3, BoxLayout.PAGE_AXIS));
        locoPanels1to3.add(locPanel1);
        locoPanels1to3.add(locPanel2);
        locoPanels1to3.add(locPanel3);

        locoTP.addTab("1 - 3", locoPanels1to3);

        locoPanels4to6.setMinimumSize(new Dimension(230, 660));
        locoPanels4to6.setName("locoPanels4to6"); // NOI18N
        locoPanels4to6.setLayout(new BoxLayout(locoPanels4to6, BoxLayout.PAGE_AXIS));
        locoPanels4to6.add(locPanel4);
        locoPanels4to6.add(locPanel5);
        locoPanels4to6.add(locPanel6);

        locoTP.addTab("4 - 6", locoPanels4to6);

        centerRightPanel.add(locoTP, BorderLayout.CENTER);

        centerPanel.add(centerRightPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel blinkLbl;
    private JPanel centerCenterPanel;
    private JPanel centerPanel;
    private JPanel centerRightPanel;
    private JLabel controllerCatalogLbl;
    private JLabel controllerCatalogNumberLbl;
    private JLabel controllerDescriptionLbl;
    private JLabel controllerHostLbl;
    private JLabel controllerHostNameLbl;
    private JLabel controllerLbl;
    private JLabel controllerSerialLbl;
    private JLabel controllerSerialNumberLbl;
    private JPanel feedback1to3Panel;
    private JPanel feedback4to6Panel;
    private FeedbackPanel feedbackPanel1;
    private FeedbackPanel feedbackPanel2;
    private FeedbackPanel feedbackPanel3;
    private FeedbackPanel feedbackPanel4;
    private FeedbackPanel feedbackPanel5;
    private FeedbackPanel feedbackPanel6;
    private JTabbedPane feedbackTPjTabbedPane2;
    private Box.Filler filler2;
    private Box.Filler filler3;
    private JTabbedPane keyboardTP;
    private JPanel keyboardsPanel;
    private JPanel keyboardsPanel1;
    private JPanel keyboardsPanel2;
    private JPanel keyboardsPanel3;
    private JPanel leftPanel;
    private LocoPanel locPanel1;
    private LocoPanel locPanel2;
    private LocoPanel locPanel3;
    private LocoPanel locPanel4;
    private LocoPanel locPanel5;
    private LocoPanel locPanel6;
    private JPanel locoPanels1to3;
    private JPanel locoPanels4to6;
    private JTabbedPane locoTP;
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
    private JPanel topCenterPanel;
    private JPanel topLeftPanel;
    private JPanel topPanel;
    private JPanel topRightPanel;
    // End of variables declaration//GEN-END:variables
}
