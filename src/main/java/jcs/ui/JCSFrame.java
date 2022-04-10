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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import jcs.JCS;
import jcs.controller.cs3.events.PowerEvent;
import jcs.controller.cs3.events.PowerEventListener;
import jcs.trackservice.TrackServiceFactory;
import jcs.ui.layout.DisplayLayoutPanel;
import jcs.ui.layout.LayoutPanel;
import jcs.ui.monitor.FeedbackMonitor;
import jcs.ui.options.OptionDialog;
import jcs.ui.util.UICallback;
import org.apache.commons.lang3.SystemUtils;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class JCSFrame extends JFrame implements UICallback {

    private final Map<KeyStroke, Action> actionMap;
    private FeedbackMonitor feedbackMonitor;

    /**
     * Creates new form JCSFrame
     */
    public JCSFrame() {
        actionMap = new HashMap<>();
        initComponents();
        if (SystemUtils.IS_OS_MAC_OSX) {
            this.quitMI.setVisible(false);
            this.optionsMI.setVisible(false);
            this.toolsMenu.setVisible(false);
        }

        init();
        initKeyStrokes();
    }

    private void init() {
        if (TrackServiceFactory.getTrackService() != null) {
            this.setTitle(this.getTitleString());
            this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/media/jcs-train-64.png")));

            //Initialize the Touchbar for MacOS
            if (SystemUtils.IS_OS_MAC_OSX) {
                JCS.showTouchbar(this);
            }

            this.locomotivesPanel.loadLocomotives();

            setCS3Properties();

            feedbackMonitor = new FeedbackMonitor();

            this.powerButton.setSelected(TrackServiceFactory.getTrackService().isPowerOn());

            TrackServiceFactory.getTrackService().addPowerEventListener(new Powerlistener(this));

            //Show the default panel
            showOverviewPanel();
        }
    }

    private void initKeyStrokes() {
        KeyStroke key0 = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);

        actionMap.put(key0, new AbstractAction("stopAction") {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });

        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        kfm.addKeyEventDispatcher((KeyEvent e) -> {
            KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
            if (actionMap.containsKey(keyStroke)) {
                final Action a = actionMap.get(keyStroke);
                final ActionEvent ae = new ActionEvent(e.getSource(), e.getID(), null);
                SwingUtilities.invokeLater(() -> {
                    a.actionPerformed(ae);
                });
                return true;
            }
            return false;
        });
    }

    public void showOverviewPanel() {
        CardLayout card = (CardLayout) this.centerPanel.getLayout();
        card.show(this.centerPanel, "overviewPanel");
        this.overviewPanel.loadLayout();
    }

    public void showLocomotives() {
        Logger.debug("Show Locomotives");
    }

    public void showTurnouts() {
        CardLayout card = (CardLayout) this.centerPanel.getLayout();
        card.show(this.centerPanel, "turnoutsPanel");
    }

    public void showSignals() {
        CardLayout card = (CardLayout) this.centerPanel.getLayout();
        card.show(this.centerPanel, "signalsPanel");
    }

    public void showDiagnostics() {
        CardLayout card = (CardLayout) this.centerPanel.getLayout();
        card.show(this.centerPanel, "diagnosticPanel");

    }

    public void showDesignLayoutPanel() {
        CardLayout card = (CardLayout) this.centerPanel.getLayout();
        card.show(this.centerPanel, "designPanel");
        this.layoutPanel.loadLayout();
    }

    public void stop() {
        if (TrackServiceFactory.getTrackService() != null) {
            TrackServiceFactory.getTrackService().switchPower(false);
        }
    }

    private void setCS3Properties() {
        if (TrackServiceFactory.getTrackService() != null) {
            if (TrackServiceFactory.getTrackService().getControllerName() != null) {
                this.connectButton.setSelected(true);
                this.controllerDescriptionLbl.setText(TrackServiceFactory.getTrackService().getControllerName());
                this.controllerCatalogNumberLbl.setText(TrackServiceFactory.getTrackService().getControllerArticleNumber());
                this.controllerSerialNumberLbl.setText(TrackServiceFactory.getTrackService().getControllerSerialNumber());
                this.controllerHostNameLbl.setText("CS3-" + TrackServiceFactory.getTrackService().getControllerSerialNumber());
            } else {
                this.connectButton.setSelected(false);
                this.controllerHostNameLbl.setText("Not Connected");
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("deprecation")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jcsToolBar = new JToolBar();
        connectButton = new JToggleButton();
        filler1 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
        powerButton = new JToggleButton();
        filler2 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
        showOverviewBtn = new JButton();
        showEditDesignBtn = new JButton();
        filler3 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
        showDiagnosticsBtn = new JButton();
        filler5 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
        showFeedbackMonitorBtn = new JButton();
        statusPanel = new JPanel();
        statusPanelLeft = new JPanel();
        filler4 = new Box.Filler(new Dimension(440, 0), new Dimension(140, 0), new Dimension(440, 32767));
        statusPanelMiddle = new JPanel();
        stopBtn = new JButton();
        statusPanelRight = new JPanel();
        mainPanel = new JPanel();
        locoDisplaySP = new JSplitPane();
        centerPanel = new JPanel();
        settingsPanel = new JPanel();
        jLabel1 = new JLabel();
        diagnosticPanel = new ControllerPanel();
        layoutPanel = new LayoutPanel();
        overviewPanel = new DisplayLayoutPanel();
        leftPanel = new JPanel();
        locomotivesPanel = new LocomotivePanel();
        bottomLeftPanel = new JPanel();
        filler7 = new Box.Filler(new Dimension(0, 10), new Dimension(0, 10), new Dimension(32767, 35));
        jPanel1 = new JPanel();
        controllerLbl = new JLabel();
        controllerDescriptionLbl = new JLabel();
        jPanel2 = new JPanel();
        controllerCatalogLbl = new JLabel();
        controllerCatalogNumberLbl = new JLabel();
        jPanel3 = new JPanel();
        controllerSerialLbl = new JLabel();
        controllerSerialNumberLbl = new JLabel();
        jPanel4 = new JPanel();
        controllerHostLbl = new JLabel();
        controllerHostNameLbl = new JLabel();
        filler6 = new Box.Filler(new Dimension(0, 110), new Dimension(0, 110), new Dimension(32767, 35));
        jcsMenuBar = new JMenuBar();
        fileMenu = new JMenu();
        quitMI = new JMenuItem();
        viewMenu = new JMenu();
        showLocosMI = new JMenuItem();
        toolsMenu = new JMenu();
        optionsMI = new JMenuItem();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setBounds(new Rectangle(0, 0, 1400, 900));
        setMinimumSize(new Dimension(1250, 900));
        setName("JCSFrame"); // NOI18N
        setSize(new Dimension(1250, 950));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jcsToolBar.setFloatable(false);
        jcsToolBar.setBorderPainted(false);
        jcsToolBar.setDoubleBuffered(true);
        jcsToolBar.setMargin(new Insets(1, 1, 1, 40));
        jcsToolBar.setMinimumSize(new Dimension(1000, 42));
        jcsToolBar.setName("ToolBar"); // NOI18N
        jcsToolBar.setOpaque(false);
        jcsToolBar.setPreferredSize(new Dimension(1000, 42));

        connectButton.setIcon(new ImageIcon(getClass().getResource("/media/monitor-off-24.png"))); // NOI18N
        connectButton.setToolTipText("Connect/Disconnect with Central Station");
        connectButton.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 204)));
        connectButton.setDoubleBuffered(true);
        connectButton.setFocusable(false);
        connectButton.setHorizontalTextPosition(SwingConstants.CENTER);
        connectButton.setMargin(new Insets(0, 0, 0, 0));
        connectButton.setMaximumSize(new Dimension(40, 40));
        connectButton.setMinimumSize(new Dimension(40, 40));
        connectButton.setName("connectButton"); // NOI18N
        connectButton.setPreferredSize(new Dimension(40, 40));
        connectButton.setSelectedIcon(new ImageIcon(getClass().getResource("/media/monitor-on-24.png"))); // NOI18N
        connectButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });
        jcsToolBar.add(connectButton);

        filler1.setName("filler1"); // NOI18N
        jcsToolBar.add(filler1);

        powerButton.setIcon(new ImageIcon(getClass().getResource("/media/power-red-24.png"))); // NOI18N
        powerButton.setToolTipText("Switch Track power On/Off");
        powerButton.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 204)));
        powerButton.setDoubleBuffered(true);
        powerButton.setFocusable(false);
        powerButton.setHorizontalTextPosition(SwingConstants.CENTER);
        powerButton.setMargin(new Insets(0, 0, 0, 0));
        powerButton.setMaximumSize(new Dimension(40, 40));
        powerButton.setMinimumSize(new Dimension(40, 40));
        powerButton.setName("powerButton"); // NOI18N
        powerButton.setPreferredSize(new Dimension(40, 40));
        powerButton.setSelectedIcon(new ImageIcon(getClass().getResource("/media/power-green-24.png"))); // NOI18N
        powerButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        powerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                powerButtonActionPerformed(evt);
            }
        });
        jcsToolBar.add(powerButton);
        powerButton.getAccessibleContext().setAccessibleName("Power");

        filler2.setName("filler2"); // NOI18N
        jcsToolBar.add(filler2);

        showOverviewBtn.setIcon(new ImageIcon(getClass().getResource("/media/home-24.png"))); // NOI18N
        showOverviewBtn.setToolTipText("Overview");
        showOverviewBtn.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 204)));
        showOverviewBtn.setDoubleBuffered(true);
        showOverviewBtn.setFocusable(false);
        showOverviewBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        showOverviewBtn.setMargin(new Insets(0, 0, 0, 0));
        showOverviewBtn.setMaximumSize(new Dimension(40, 40));
        showOverviewBtn.setMinimumSize(new Dimension(40, 40));
        showOverviewBtn.setName("showOverviewBtn"); // NOI18N
        showOverviewBtn.setPreferredSize(new Dimension(40, 40));
        showOverviewBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showOverviewBtnActionPerformed(evt);
            }
        });
        jcsToolBar.add(showOverviewBtn);
        showOverviewBtn.getAccessibleContext().setAccessibleName("Home");

        showEditDesignBtn.setIcon(new ImageIcon(getClass().getResource("/media/paintbrush-24.png"))); // NOI18N
        showEditDesignBtn.setToolTipText("Design Layout");
        showEditDesignBtn.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 204)));
        showEditDesignBtn.setFocusable(false);
        showEditDesignBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        showEditDesignBtn.setMargin(new Insets(0, 0, 0, 0));
        showEditDesignBtn.setMaximumSize(new Dimension(40, 40));
        showEditDesignBtn.setMinimumSize(new Dimension(40, 40));
        showEditDesignBtn.setName("showEditDesignBtn"); // NOI18N
        showEditDesignBtn.setPreferredSize(new Dimension(40, 40));
        showEditDesignBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        showEditDesignBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showEditDesignBtnActionPerformed(evt);
            }
        });
        jcsToolBar.add(showEditDesignBtn);
        showEditDesignBtn.getAccessibleContext().setAccessibleName("Design");

        filler3.setName("filler3"); // NOI18N
        jcsToolBar.add(filler3);

        showDiagnosticsBtn.setIcon(new ImageIcon(getClass().getResource("/media/controller-24.png"))); // NOI18N
        showDiagnosticsBtn.setToolTipText("Diagnostics");
        showDiagnosticsBtn.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 204)));
        showDiagnosticsBtn.setDoubleBuffered(true);
        showDiagnosticsBtn.setFocusable(false);
        showDiagnosticsBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        showDiagnosticsBtn.setMargin(new Insets(0, 0, 0, 0));
        showDiagnosticsBtn.setMaximumSize(new Dimension(40, 40));
        showDiagnosticsBtn.setMinimumSize(new Dimension(40, 40));
        showDiagnosticsBtn.setName("showDiagnosticsBtn"); // NOI18N
        showDiagnosticsBtn.setPreferredSize(new Dimension(40, 40));
        showDiagnosticsBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        showDiagnosticsBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showDiagnosticsBtnActionPerformed(evt);
            }
        });
        jcsToolBar.add(showDiagnosticsBtn);
        showDiagnosticsBtn.getAccessibleContext().setAccessibleName("Switchboard");

        filler5.setName("filler5"); // NOI18N
        jcsToolBar.add(filler5);

        showFeedbackMonitorBtn.setIcon(new ImageIcon(getClass().getResource("/media/monitor-24.png"))); // NOI18N
        showFeedbackMonitorBtn.setFocusable(false);
        showFeedbackMonitorBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        showFeedbackMonitorBtn.setMaximumSize(new Dimension(40, 40));
        showFeedbackMonitorBtn.setMinimumSize(new Dimension(40, 40));
        showFeedbackMonitorBtn.setName("showFeedbackMonitorBtn"); // NOI18N
        showFeedbackMonitorBtn.setPreferredSize(new Dimension(40, 40));
        showFeedbackMonitorBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        showFeedbackMonitorBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showFeedbackMonitorBtnActionPerformed(evt);
            }
        });
        jcsToolBar.add(showFeedbackMonitorBtn);

        getContentPane().add(jcsToolBar, BorderLayout.NORTH);

        statusPanel.setMinimumSize(new Dimension(574, 45));
        statusPanel.setName("statusPanel"); // NOI18N
        statusPanel.setPreferredSize(new Dimension(1200, 45));
        FlowLayout flowLayout7 = new FlowLayout(FlowLayout.CENTER, 0, 0);
        flowLayout7.setAlignOnBaseline(true);
        statusPanel.setLayout(flowLayout7);

        statusPanelLeft.setName("statusPanelLeft"); // NOI18N
        statusPanelLeft.setPreferredSize(new Dimension(50, 10));
        FlowLayout flowLayout2 = new FlowLayout(FlowLayout.CENTER, 5, 0);
        flowLayout2.setAlignOnBaseline(true);
        statusPanelLeft.setLayout(flowLayout2);

        filler4.setName("filler4"); // NOI18N
        statusPanelLeft.add(filler4);

        statusPanel.add(statusPanelLeft);

        statusPanelMiddle.setMinimumSize(new Dimension(80, 42));
        statusPanelMiddle.setName("statusPanelMiddle"); // NOI18N
        statusPanelMiddle.setPreferredSize(new Dimension(1000, 45));
        statusPanelMiddle.setLayout(new GridLayout(1, 1));

        stopBtn.setIcon(new ImageIcon(getClass().getResource("/media/power-s-red-24.png"))); // NOI18N
        stopBtn.setText("STOP");
        stopBtn.setAlignmentY(0.0F);
        stopBtn.setFocusPainted(false);
        stopBtn.setFocusable(false);
        stopBtn.setMargin(new Insets(0, 0, 0, 0));
        stopBtn.setMaximumSize(new Dimension(80, 40));
        stopBtn.setMinimumSize(new Dimension(80, 40));
        stopBtn.setName("stopBtn"); // NOI18N
        stopBtn.setPreferredSize(new Dimension(120, 40));
        stopBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopBtnActionPerformed(evt);
            }
        });
        statusPanelMiddle.add(stopBtn);

        statusPanel.add(statusPanelMiddle);

        statusPanelRight.setName("statusPanelRight"); // NOI18N
        statusPanelRight.setPreferredSize(new Dimension(50, 40));
        FlowLayout flowLayout1 = new FlowLayout(FlowLayout.RIGHT, 0, 6);
        flowLayout1.setAlignOnBaseline(true);
        statusPanelRight.setLayout(flowLayout1);
        statusPanel.add(statusPanelRight);

        getContentPane().add(statusPanel, BorderLayout.SOUTH);

        mainPanel.setMinimumSize(new Dimension(1050, 900));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new Dimension(1250, 850));
        mainPanel.setLayout(new BorderLayout());

        locoDisplaySP.setDividerLocation(230);
        locoDisplaySP.setMinimumSize(new Dimension(1050, 900));
        locoDisplaySP.setName("locoDisplaySP"); // NOI18N
        locoDisplaySP.setPreferredSize(new Dimension(1050, 850));

        centerPanel.setMinimumSize(new Dimension(1000, 900));
        centerPanel.setName("centerPanel"); // NOI18N
        centerPanel.setPreferredSize(new Dimension(1010, 900));
        centerPanel.setLayout(new CardLayout());

        settingsPanel.setMinimumSize(new Dimension(1000, 750));
        settingsPanel.setName("settingsPanel"); // NOI18N
        settingsPanel.setOpaque(false);
        settingsPanel.setPreferredSize(new Dimension(885, 750));

        jLabel1.setText("Settings");
        jLabel1.setName("jLabel1"); // NOI18N
        settingsPanel.add(jLabel1);

        centerPanel.add(settingsPanel, "settingsPanel");
        settingsPanel.getAccessibleContext().setAccessibleName("settingPanel");

        diagnosticPanel.setMinimumSize(new Dimension(885, 840));
        diagnosticPanel.setName("diagnosticPanel"); // NOI18N
        centerPanel.add(diagnosticPanel, "diagnosticPanel");

        layoutPanel.setMinimumSize(new Dimension(885, 160));
        layoutPanel.setName("layoutPanel"); // NOI18N
        centerPanel.add(layoutPanel, "designPanel");
        layoutPanel.getAccessibleContext().setAccessibleName("designPanel");

        overviewPanel.setName("overviewPanel"); // NOI18N
        centerPanel.add(overviewPanel, "overviewPanel");
        overviewPanel.getAccessibleContext().setAccessibleName("overviewPanel");

        locoDisplaySP.setRightComponent(centerPanel);

        leftPanel.setMinimumSize(new Dimension(220, 850));
        leftPanel.setName("leftPanel"); // NOI18N
        leftPanel.setPreferredSize(new Dimension(220, 845));
        leftPanel.setLayout(new BorderLayout(1, 1));

        locomotivesPanel.setName("locomotivesPanel"); // NOI18N
        locomotivesPanel.setPreferredSize(new Dimension(220, 580));
        leftPanel.add(locomotivesPanel, BorderLayout.PAGE_START);

        bottomLeftPanel.setBorder(BorderFactory.createTitledBorder("Controller Properties"));
        bottomLeftPanel.setMinimumSize(new Dimension(220, 200));
        bottomLeftPanel.setName("bottomLeftPanel"); // NOI18N
        bottomLeftPanel.setPreferredSize(new Dimension(200, 200));
        bottomLeftPanel.setLayout(new BoxLayout(bottomLeftPanel, BoxLayout.Y_AXIS));

        filler7.setName("filler7"); // NOI18N
        bottomLeftPanel.add(filler7);

        jPanel1.setName("jPanel1"); // NOI18N
        FlowLayout flowLayout3 = new FlowLayout(FlowLayout.LEFT);
        flowLayout3.setAlignOnBaseline(true);
        jPanel1.setLayout(flowLayout3);

        controllerLbl.setHorizontalAlignment(SwingConstants.TRAILING);
        controllerLbl.setLabelFor(controllerDescriptionLbl);
        controllerLbl.setText("Controller:");
        controllerLbl.setDoubleBuffered(true);
        controllerLbl.setHorizontalTextPosition(SwingConstants.CENTER);
        controllerLbl.setName("controllerLbl"); // NOI18N
        controllerLbl.setPreferredSize(new Dimension(75, 16));
        jPanel1.add(controllerLbl);

        controllerDescriptionLbl.setText("...");
        controllerDescriptionLbl.setName("controllerDescriptionLbl"); // NOI18N
        controllerDescriptionLbl.setPreferredSize(new Dimension(125, 16));
        jPanel1.add(controllerDescriptionLbl);

        bottomLeftPanel.add(jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N
        FlowLayout flowLayout4 = new FlowLayout(FlowLayout.LEFT);
        flowLayout4.setAlignOnBaseline(true);
        jPanel2.setLayout(flowLayout4);

        controllerCatalogLbl.setHorizontalAlignment(SwingConstants.TRAILING);
        controllerCatalogLbl.setLabelFor(controllerCatalogNumberLbl);
        controllerCatalogLbl.setText("Model:");
        controllerCatalogLbl.setName("controllerCatalogLbl"); // NOI18N
        controllerCatalogLbl.setOpaque(true);
        controllerCatalogLbl.setPreferredSize(new Dimension(75, 16));
        jPanel2.add(controllerCatalogLbl);

        controllerCatalogNumberLbl.setText("...");
        controllerCatalogNumberLbl.setName("controllerCatalogNumberLbl"); // NOI18N
        controllerCatalogNumberLbl.setPreferredSize(new Dimension(125, 16));
        jPanel2.add(controllerCatalogNumberLbl);

        bottomLeftPanel.add(jPanel2);

        jPanel3.setName("jPanel3"); // NOI18N
        FlowLayout flowLayout6 = new FlowLayout(FlowLayout.LEFT);
        flowLayout6.setAlignOnBaseline(true);
        jPanel3.setLayout(flowLayout6);

        controllerSerialLbl.setHorizontalAlignment(SwingConstants.TRAILING);
        controllerSerialLbl.setText("Serial:");
        controllerSerialLbl.setName("controllerSerialLbl"); // NOI18N
        controllerSerialLbl.setPreferredSize(new Dimension(75, 16));
        jPanel3.add(controllerSerialLbl);

        controllerSerialNumberLbl.setText("...");
        controllerSerialNumberLbl.setName("controllerSerialNumberLbl"); // NOI18N
        controllerSerialNumberLbl.setPreferredSize(new Dimension(125, 16));
        jPanel3.add(controllerSerialNumberLbl);

        bottomLeftPanel.add(jPanel3);

        jPanel4.setName("jPanel4"); // NOI18N
        FlowLayout flowLayout5 = new FlowLayout(FlowLayout.LEFT);
        flowLayout5.setAlignOnBaseline(true);
        jPanel4.setLayout(flowLayout5);

        controllerHostLbl.setHorizontalAlignment(SwingConstants.TRAILING);
        controllerHostLbl.setText("Host:");
        controllerHostLbl.setName("controllerHostLbl"); // NOI18N
        controllerHostLbl.setPreferredSize(new Dimension(75, 16));
        jPanel4.add(controllerHostLbl);

        controllerHostNameLbl.setText("...");
        controllerHostNameLbl.setName("controllerHostNameLbl"); // NOI18N
        controllerHostNameLbl.setPreferredSize(new Dimension(125, 16));
        jPanel4.add(controllerHostNameLbl);

        bottomLeftPanel.add(jPanel4);

        filler6.setName("filler6"); // NOI18N
        bottomLeftPanel.add(filler6);

        leftPanel.add(bottomLeftPanel, BorderLayout.PAGE_END);

        locoDisplaySP.setLeftComponent(leftPanel);

        mainPanel.add(locoDisplaySP, BorderLayout.CENTER);

        getContentPane().add(mainPanel, BorderLayout.CENTER);

        jcsMenuBar.setName("jcsMenuBar"); // NOI18N

        fileMenu.setText("File");
        fileMenu.setName("fileMenu"); // NOI18N

        quitMI.setText("Quit JCS");
        quitMI.setName("quitMI"); // NOI18N
        quitMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                quitMIActionPerformed(evt);
            }
        });
        fileMenu.add(quitMI);

        jcsMenuBar.add(fileMenu);

        viewMenu.setText("View");
        viewMenu.setName("viewMenu"); // NOI18N

        showLocosMI.setIcon(new ImageIcon(getClass().getResource("/media/electric-loc-24.png"))); // NOI18N
        showLocosMI.setLabel("Locomotives");
        showLocosMI.setName("showLocosMI"); // NOI18N
        showLocosMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showLocosMIActionPerformed(evt);
            }
        });
        viewMenu.add(showLocosMI);

        jcsMenuBar.add(viewMenu);

        toolsMenu.setText("Tools");
        toolsMenu.setName("toolsMenu"); // NOI18N

        optionsMI.setText("Options");
        optionsMI.setName("optionsMI"); // NOI18N
        optionsMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                optionsMIActionPerformed(evt);
            }
        });
        toolsMenu.add(optionsMI);

        jcsMenuBar.add(toolsMenu);

        setJMenuBar(jcsMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void showLocosMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showLocosMIActionPerformed
      showLocomotives();
  }//GEN-LAST:event_showLocosMIActionPerformed

  private void showDiagnosticsBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showDiagnosticsBtnActionPerformed
      showDiagnostics();
  }//GEN-LAST:event_showDiagnosticsBtnActionPerformed

  private void quitMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_quitMIActionPerformed
      this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
  }//GEN-LAST:event_quitMIActionPerformed

  private void formWindowClosing(WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      boolean closed = this.handleQuitRequest();
      if (closed) {
          this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          setVisible(false);
          dispose();
      }
  }//GEN-LAST:event_formWindowClosing

  private void optionsMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_optionsMIActionPerformed
      handlePreferences();
  }//GEN-LAST:event_optionsMIActionPerformed

  private void stopBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_stopBtnActionPerformed
      stop();
  }//GEN-LAST:event_stopBtnActionPerformed

    private void showEditDesignBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showEditDesignBtnActionPerformed
        showDesignLayoutPanel();
    }//GEN-LAST:event_showEditDesignBtnActionPerformed

    private void showOverviewBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showOverviewBtnActionPerformed
        showOverviewPanel();
        this.overviewPanel.loadLayout();
    }//GEN-LAST:event_showOverviewBtnActionPerformed

    private void powerButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_powerButtonActionPerformed
        boolean on = ((JToggleButton) evt.getSource()).isSelected();
        if (TrackServiceFactory.getTrackService() != null) {
            TrackServiceFactory.getTrackService().switchPower(on);
        }
    }//GEN-LAST:event_powerButtonActionPerformed

    private void showFeedbackMonitorBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showFeedbackMonitorBtnActionPerformed
        this.feedbackMonitor.showMonitor();
    }//GEN-LAST:event_showFeedbackMonitorBtnActionPerformed

    private void connectButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
        boolean connect = ((JToggleButton) evt.getSource()).isSelected();
        if (TrackServiceFactory.getTrackService() != null) {
            if (connect) {
                TrackServiceFactory.getTrackService().connect();
                this.controllerDescriptionLbl.setText(TrackServiceFactory.getTrackService().getControllerName());
                this.controllerCatalogNumberLbl.setText(TrackServiceFactory.getTrackService().getControllerArticleNumber());
                this.controllerSerialNumberLbl.setText(TrackServiceFactory.getTrackService().getControllerSerialNumber());
                this.controllerHostNameLbl.setText("CS3-" + TrackServiceFactory.getTrackService().getControllerSerialNumber());

                TrackServiceFactory.getTrackService().addPowerEventListener(new Powerlistener(this));

            } else {
                TrackServiceFactory.getTrackService().disconnect();
                this.controllerDescriptionLbl.setText("-");
                this.controllerCatalogNumberLbl.setText("-");
                this.controllerSerialNumberLbl.setText("-");
                this.controllerHostNameLbl.setText("Disconnected");
            }
        }
        this.powerButton.setEnabled(connect);
        this.showFeedbackMonitorBtn.setEnabled(connect);
    }//GEN-LAST:event_connectButtonActionPerformed

    private String getTitleString() {
        String jcsVersion = JCS.getVersionInfo().getVersion();
        if (TrackServiceFactory.getTrackService() != null && TrackServiceFactory.getTrackService().getControllerSerialNumber() != null) {
            return "JCS " + "Connected to " + TrackServiceFactory.getTrackService().getControllerName();
        } else {
            return "JCS " + jcsVersion + " - NOT Connected!";
        }
    }

    @Override
    public void openFiles(List<File> files) {
        Logger.trace("Open Files...");
    }

    @Override
    public boolean handleQuitRequest() {
        int result = JOptionPane.showConfirmDialog(JCS.getJCSFrame(), "Are you sure you want to exit JCS?", "Exit JCS", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    @Override
    public void handleAbout() {
        ImageIcon jcsIcon = new ImageIcon(JCSFrame.class.getResource("/media/jcs-train-64.png"));
        JOptionPane.showMessageDialog(JCS.getJCSFrame(), "Java Command Station By Frans Jacobs", "About JCS", JOptionPane.PLAIN_MESSAGE, jcsIcon);
    }

    @Override
    public void handlePreferences() {
        Logger.trace("handlePreferences");

        OptionDialog preferencesDialog = new OptionDialog(this, false);
        preferencesDialog.setVisible(true);

        Logger.debug("refresh data...");
        //this.diagnosticPanel.refreshPanel();
        //this.overviewPanel.refreshPanel();
    }

    private class Powerlistener implements PowerEventListener {

        private final JCSFrame jcsFrame;

        Powerlistener(JCSFrame jcsFrame) {
            this.jcsFrame = jcsFrame;
        }

        @Override
        public void onPowerChange(PowerEvent event) {
            this.jcsFrame.powerButton.setSelected(event.isPower());

        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    JPanel bottomLeftPanel;
    JPanel centerPanel;
    JToggleButton connectButton;
    JLabel controllerCatalogLbl;
    JLabel controllerCatalogNumberLbl;
    JLabel controllerDescriptionLbl;
    JLabel controllerHostLbl;
    JLabel controllerHostNameLbl;
    JLabel controllerLbl;
    JLabel controllerSerialLbl;
    JLabel controllerSerialNumberLbl;
    ControllerPanel diagnosticPanel;
    JMenu fileMenu;
    Box.Filler filler1;
    Box.Filler filler2;
    Box.Filler filler3;
    Box.Filler filler4;
    Box.Filler filler5;
    Box.Filler filler6;
    Box.Filler filler7;
    JLabel jLabel1;
    JPanel jPanel1;
    JPanel jPanel2;
    JPanel jPanel3;
    JPanel jPanel4;
    JMenuBar jcsMenuBar;
    JToolBar jcsToolBar;
    LayoutPanel layoutPanel;
    JPanel leftPanel;
    JSplitPane locoDisplaySP;
    LocomotivePanel locomotivesPanel;
    JPanel mainPanel;
    JMenuItem optionsMI;
    DisplayLayoutPanel overviewPanel;
    JToggleButton powerButton;
    JMenuItem quitMI;
    JPanel settingsPanel;
    JButton showDiagnosticsBtn;
    JButton showEditDesignBtn;
    JButton showFeedbackMonitorBtn;
    JMenuItem showLocosMI;
    JButton showOverviewBtn;
    JPanel statusPanel;
    JPanel statusPanelLeft;
    JPanel statusPanelMiddle;
    JPanel statusPanelRight;
    JButton stopBtn;
    JMenu toolsMenu;
    JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
}
