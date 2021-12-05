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
package lan.wervel.jcs.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import lan.wervel.jcs.JCSGUI;
import lan.wervel.jcs.controller.ControllerEvent;
import lan.wervel.jcs.controller.ControllerEventListener;
import lan.wervel.jcs.controller.cs2.DeviceInfo;
import lan.wervel.jcs.trackservice.TrackServiceFactory;
import lan.wervel.jcs.trackservice.events.HeartBeatListener;
import lan.wervel.jcs.ui.layout.DesignPanel;
import lan.wervel.jcs.ui.options.OptionDialog;
import lan.wervel.jcs.ui.util.UICallback;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class JCSFrame extends JFrame implements UICallback {

    private final Map<KeyStroke, Action> actionMap;
    //private boolean powerOn = true;
    private final ImageIcon blinkOnIcon;
    private final ImageIcon blinkOnIcon2;
    private final ImageIcon blinkOffIcon;
    private final ImageIcon blinkErrorIcon;
    private boolean blinkToggle;

    private long lastUpdatedMillis;

    private Timer checkTimer;

    /**
     * Creates new form JCSFrame
     */
    public JCSFrame() {
        actionMap = new HashMap<>();

        blinkErrorIcon = new ImageIcon(DiagnosticPanel.class.getResource("/media/sync-red-24.png"));
        blinkOnIcon = new ImageIcon(DiagnosticPanel.class.getResource("/media/sync-green-90-24.png"));
        blinkOnIcon2 = new ImageIcon(DiagnosticPanel.class.getResource("/media/sync-dark-green-24.png"));
        blinkOffIcon = new ImageIcon(DiagnosticPanel.class.getResource("/media/sync-black-24.png"));

        initComponents();

        JCSGUI.updateProgress();

        if (JCSGUI.isMacOS()) {
            this.quitMI.setVisible(false);
            this.optionsMI.setVisible(false);
            this.toolsMenu.setVisible(false);
        }

        init();
        JCSGUI.updateProgress();

        initKeyStrokes();
        JCSGUI.updateProgress();
    }

    @SuppressWarnings("Convert2Lambda")
    private void init() {
        if (TrackServiceFactory.getTrackService() != null) {
            this.setTitle(this.getTitleString());
            this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/media/jcs-train-64.png")));

            //Initialize the Touchbar for MacOS
            if (JCSGUI.isMacOS()) {
                JCSGUI.showTouchbar(this);
            }

            TrackServiceFactory.getTrackService().addHeartBeatListener(new HeartBeat(this));
            TrackServiceFactory.getTrackService().addControllerListener(new ControllerListener(this));

            boolean powerOn = TrackServiceFactory.getTrackService().isPowerOn();
            this.setPowerStatus(powerOn, true);

            checkTimer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {

                    long now = System.currentTimeMillis();
                    if (now - lastUpdatedMillis > 2000) {
                        blinkLbl.setIcon(blinkErrorIcon);
                    }
                }
            });

            checkTimer.setRepeats(true);
            checkTimer.start();

            //Show the default panel
            //showDiagnostics();
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
        this.designPanel.loadLayout();
    }

    public void stop() {
        TrackServiceFactory.getTrackService().powerOff();
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
        powerBtn = new JButton();
        filler1 = new Box.Filler(new Dimension(0, 10), new Dimension(0, 10), new Dimension(32767, 10));
        showOverviewBtn = new JButton();
        filler2 = new Box.Filler(new Dimension(0, 20), new Dimension(0, 20), new Dimension(32767, 20));
        showTurnoutsBtn = new JButton();
        showSignalBtn = new JButton();
        showDiagnosticsBtn = new JButton();
        filler5 = new Box.Filler(new Dimension(0, 20), new Dimension(0, 20), new Dimension(32767, 20));
        showEditDesignBtn = new JButton();
        filler3 = new Box.Filler(new Dimension(0, 20), new Dimension(0, 20), new Dimension(32767, 20));
        startOfDayBtn = new JButton();
        synchronizeLocosBtn = new JButton();
        centerPanel = new JPanel();
        settingsPanel = new JPanel();
        jLabel1 = new JLabel();
        signalsPanel = new SignalsPanel();
        turnoutsPanel = new TurnoutsPanel();
        diagnosticPanel = new DiagnosticPanel();
        designPanel = new DesignPanel();
        overviewPanel = new DisplayLayoutPanel();
        statusPanel = new JPanel();
        statusPanelLeft = new JPanel();
        filler4 = new Box.Filler(new Dimension(440, 0), new Dimension(140, 0), new Dimension(440, 32767));
        statusPanelMiddle = new JPanel();
        stopBtn = new JButton();
        statusPanelRight = new JPanel();
        blinkLbl = new JLabel();
        jcsMenuBar = new JMenuBar();
        fileMenu = new JMenu();
        quitMI = new JMenuItem();
        editMenu = new JMenu();
        synchronizeAccessoriesMI = new JMenuItem();
        viewMenu = new JMenu();
        showLocosMI = new JMenuItem();
        showTurnoutsMI = new JMenuItem();
        showSignalsMI = new JMenuItem();
        showDiagnosticsMI = new JMenuItem();
        toolsMenu = new JMenu();
        optionsMI = new JMenuItem();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setBounds(new Rectangle(0, 23, 1600, 900));
        setMinimumSize(new Dimension(1400, 900));
        setName("JCSFrame"); // NOI18N
        setPreferredSize(new Dimension(1600, 900));
        setSize(new Dimension(1600, 900));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jcsToolBar.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jcsToolBar.setFloatable(false);
        jcsToolBar.setOrientation(SwingConstants.VERTICAL);
        jcsToolBar.setMaximumSize(new Dimension(50, 65775));
        jcsToolBar.setMinimumSize(new Dimension(40, 241));
        jcsToolBar.setName("jcsToolBar"); // NOI18N
        jcsToolBar.setPreferredSize(new Dimension(40, 200));

        powerBtn.setIcon(new ImageIcon(getClass().getResource("/media/power-green-24.png"))); // NOI18N
        powerBtn.setToolTipText("Track Power");
        powerBtn.setDoubleBuffered(true);
        powerBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        powerBtn.setMaximumSize(new Dimension(40, 40));
        powerBtn.setMinimumSize(new Dimension(40, 40));
        powerBtn.setName("powerBtn"); // NOI18N
        powerBtn.setPreferredSize(new Dimension(40, 40));
        powerBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        powerBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                powerBtnActionPerformed(evt);
            }
        });
        jcsToolBar.add(powerBtn);

        filler1.setName("filler1"); // NOI18N
        jcsToolBar.add(filler1);

        showOverviewBtn.setIcon(new ImageIcon(getClass().getResource("/media/earth-24.png"))); // NOI18N
        showOverviewBtn.setToolTipText("Overview");
        showOverviewBtn.setFocusable(false);
        showOverviewBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        showOverviewBtn.setMaximumSize(new Dimension(40, 40));
        showOverviewBtn.setMinimumSize(new Dimension(40, 40));
        showOverviewBtn.setName("showOverviewBtn"); // NOI18N
        showOverviewBtn.setPreferredSize(new Dimension(40, 40));
        showOverviewBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        showOverviewBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showOverviewBtnActionPerformed(evt);
            }
        });
        jcsToolBar.add(showOverviewBtn);

        filler2.setName("filler2"); // NOI18N
        jcsToolBar.add(filler2);

        showTurnoutsBtn.setIcon(new ImageIcon(getClass().getResource("/media/turnout-24.png"))); // NOI18N
        showTurnoutsBtn.setToolTipText("Turnouts");
        showTurnoutsBtn.setFocusable(false);
        showTurnoutsBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        showTurnoutsBtn.setMaximumSize(new Dimension(40, 40));
        showTurnoutsBtn.setMinimumSize(new Dimension(40, 40));
        showTurnoutsBtn.setName("showTurnoutsBtn"); // NOI18N
        showTurnoutsBtn.setPreferredSize(new Dimension(40, 40));
        showTurnoutsBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        showTurnoutsBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showTurnoutsBtnActionPerformed(evt);
            }
        });
        jcsToolBar.add(showTurnoutsBtn);

        showSignalBtn.setIcon(new ImageIcon(getClass().getResource("/media/signal-24.png"))); // NOI18N
        showSignalBtn.setToolTipText("Signals");
        showSignalBtn.setFocusable(false);
        showSignalBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        showSignalBtn.setMaximumSize(new Dimension(40, 40));
        showSignalBtn.setMinimumSize(new Dimension(40, 40));
        showSignalBtn.setName("showSignalBtn"); // NOI18N
        showSignalBtn.setPreferredSize(new Dimension(40, 40));
        showSignalBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        showSignalBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showSignalBtnActionPerformed(evt);
            }
        });
        jcsToolBar.add(showSignalBtn);

        showDiagnosticsBtn.setIcon(new ImageIcon(getClass().getResource("/media/stethoscope-24.png"))); // NOI18N
        showDiagnosticsBtn.setToolTipText("Diagnostics");
        showDiagnosticsBtn.setFocusable(false);
        showDiagnosticsBtn.setHorizontalTextPosition(SwingConstants.CENTER);
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

        filler5.setName("filler5"); // NOI18N
        jcsToolBar.add(filler5);

        showEditDesignBtn.setIcon(new ImageIcon(getClass().getResource("/media/layout-24.png"))); // NOI18N
        showEditDesignBtn.setToolTipText("Design Layout");
        showEditDesignBtn.setFocusable(false);
        showEditDesignBtn.setHorizontalTextPosition(SwingConstants.CENTER);
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

        filler3.setName("filler3"); // NOI18N
        jcsToolBar.add(filler3);

        startOfDayBtn.setIcon(new ImageIcon(getClass().getResource("/media/accessory-data-sync.png"))); // NOI18N
        startOfDayBtn.setToolTipText("Synchronize Accessories with the stored settings");
        startOfDayBtn.setFocusable(false);
        startOfDayBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        startOfDayBtn.setMaximumSize(new Dimension(40, 40));
        startOfDayBtn.setMinimumSize(new Dimension(40, 40));
        startOfDayBtn.setName("startOfDayBtn"); // NOI18N
        startOfDayBtn.setPreferredSize(new Dimension(40, 40));
        startOfDayBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        startOfDayBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                startOfDayBtnActionPerformed(evt);
            }
        });
        jcsToolBar.add(startOfDayBtn);

        synchronizeLocosBtn.setIcon(new ImageIcon(getClass().getResource("/media/CS2-3-Sync.png"))); // NOI18N
        synchronizeLocosBtn.setToolTipText("Synchronize with CS2/3");
        synchronizeLocosBtn.setFocusable(false);
        synchronizeLocosBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        synchronizeLocosBtn.setMaximumSize(new Dimension(40, 40));
        synchronizeLocosBtn.setMinimumSize(new Dimension(40, 40));
        synchronizeLocosBtn.setName("synchronizeLocosBtn"); // NOI18N
        synchronizeLocosBtn.setPreferredSize(new Dimension(40, 40));
        synchronizeLocosBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        synchronizeLocosBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                synchronizeLocosBtnActionPerformed(evt);
            }
        });
        jcsToolBar.add(synchronizeLocosBtn);

        getContentPane().add(jcsToolBar, BorderLayout.WEST);

        centerPanel.setMinimumSize(new Dimension(1024, 845));
        centerPanel.setName("centerPanel"); // NOI18N
        centerPanel.setPreferredSize(new Dimension(1324, 845));
        centerPanel.setLayout(new CardLayout());

        settingsPanel.setMinimumSize(new Dimension(1024, 723));
        settingsPanel.setName("settingsPanel"); // NOI18N
        settingsPanel.setOpaque(false);
        settingsPanel.setPreferredSize(new Dimension(1024, 723));

        jLabel1.setText("Settings");
        jLabel1.setName("jLabel1"); // NOI18N
        settingsPanel.add(jLabel1);

        centerPanel.add(settingsPanel, "settingsPanel");
        settingsPanel.getAccessibleContext().setAccessibleName("settingPanel");

        signalsPanel.setName("signalsPanel"); // NOI18N
        signalsPanel.setPreferredSize(new Dimension(1024, 840));
        centerPanel.add(signalsPanel, "signalsPanel");
        signalsPanel.getAccessibleContext().setAccessibleName("signalsPanel");

        turnoutsPanel.setName("turnoutsPanel"); // NOI18N
        turnoutsPanel.setPreferredSize(new Dimension(1024, 840));
        turnoutsPanel.setLayout(new GridLayout(1, 0));
        centerPanel.add(turnoutsPanel, "turnoutsPanel");
        turnoutsPanel.getAccessibleContext().setAccessibleName("turnoutsPanel");

        diagnosticPanel.setName("diagnosticPanel"); // NOI18N
        centerPanel.add(diagnosticPanel, "diagnosticPanel");

        designPanel.setName("designPanel"); // NOI18N
        centerPanel.add(designPanel, "designPanel");

        overviewPanel.setName("overviewPanel"); // NOI18N
        centerPanel.add(overviewPanel, "overviewPanel");

        getContentPane().add(centerPanel, BorderLayout.CENTER);

        statusPanel.setMinimumSize(new Dimension(574, 45));
        statusPanel.setName("statusPanel"); // NOI18N
        statusPanel.setPreferredSize(new Dimension(1324, 45));
        statusPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));

        statusPanelLeft.setName("statusPanelLeft"); // NOI18N
        statusPanelLeft.setPreferredSize(new Dimension(150, 10));
        FlowLayout flowLayout2 = new FlowLayout(FlowLayout.CENTER, 5, 0);
        flowLayout2.setAlignOnBaseline(true);
        statusPanelLeft.setLayout(flowLayout2);

        filler4.setName("filler4"); // NOI18N
        statusPanelLeft.add(filler4);

        statusPanel.add(statusPanelLeft);

        statusPanelMiddle.setName("statusPanelMiddle"); // NOI18N
        statusPanelMiddle.setPreferredSize(new Dimension(1050, 45));
        statusPanelMiddle.setLayout(new GridLayout(1, 1));

        stopBtn.setIcon(new ImageIcon(getClass().getResource("/media/power-s-red-24.png"))); // NOI18N
        stopBtn.setText("STOP");
        stopBtn.setAlignmentY(0.0F);
        stopBtn.setFocusPainted(false);
        stopBtn.setFocusable(false);
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
        statusPanelRight.setPreferredSize(new Dimension(150, 40));
        FlowLayout flowLayout1 = new FlowLayout(FlowLayout.RIGHT, 0, 5);
        flowLayout1.setAlignOnBaseline(true);
        statusPanelRight.setLayout(flowLayout1);

        blinkLbl.setIcon(new ImageIcon(getClass().getResource("/media/sync-black-24.png"))); // NOI18N
        blinkLbl.setName("blinkLbl"); // NOI18N
        statusPanelRight.add(blinkLbl);

        statusPanel.add(statusPanelRight);

        getContentPane().add(statusPanel, BorderLayout.SOUTH);

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

        editMenu.setLabel("Edit");
        editMenu.setName("editMenu"); // NOI18N

        synchronizeAccessoriesMI.setIcon(new ImageIcon(getClass().getResource("/media/sync-black-24.png"))); // NOI18N
        synchronizeAccessoriesMI.setText("Synchronize Accessories");
        synchronizeAccessoriesMI.setToolTipText("Synchronize Accessories with the stored settings");
        synchronizeAccessoriesMI.setName("synchronizeAccessoriesMI"); // NOI18N
        synchronizeAccessoriesMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                synchronizeAccessoriesMIActionPerformed(evt);
            }
        });
        editMenu.add(synchronizeAccessoriesMI);

        jcsMenuBar.add(editMenu);

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

        showTurnoutsMI.setIcon(new ImageIcon(getClass().getResource("/media/turnout-24.png"))); // NOI18N
        showTurnoutsMI.setToolTipText("");
        showTurnoutsMI.setLabel("Turnouts");
        showTurnoutsMI.setName("showTurnoutsMI"); // NOI18N
        showTurnoutsMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showTurnoutsMIActionPerformed(evt);
            }
        });
        viewMenu.add(showTurnoutsMI);

        showSignalsMI.setIcon(new ImageIcon(getClass().getResource("/media/signal-24.png"))); // NOI18N
        showSignalsMI.setLabel("Signals");
        showSignalsMI.setName("showSignalsMI"); // NOI18N
        showSignalsMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showSignalsMIActionPerformed(evt);
            }
        });
        viewMenu.add(showSignalsMI);

        showDiagnosticsMI.setIcon(new ImageIcon(getClass().getResource("/media/stethoscope-24.png"))); // NOI18N
        showDiagnosticsMI.setLabel("Diagnostics");
        showDiagnosticsMI.setName("showDiagnosticsMI"); // NOI18N
        showDiagnosticsMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showDiagnosticsMIActionPerformed(evt);
            }
        });
        viewMenu.add(showDiagnosticsMI);

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

  private void showTurnoutsMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showTurnoutsMIActionPerformed
      showTurnouts();
  }//GEN-LAST:event_showTurnoutsMIActionPerformed

  private void showSignalsMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showSignalsMIActionPerformed
      showSignals();
  }//GEN-LAST:event_showSignalsMIActionPerformed

  private void synchronizeLocosBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_synchronizeLocosBtnActionPerformed
      synchronizeWithController();
  }//GEN-LAST:event_synchronizeLocosBtnActionPerformed

  private void showTurnoutsBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showTurnoutsBtnActionPerformed
      showTurnouts();
  }//GEN-LAST:event_showTurnoutsBtnActionPerformed

  private void showSignalBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showSignalBtnActionPerformed
      showSignals();
  }//GEN-LAST:event_showSignalBtnActionPerformed

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

  private void showDiagnosticsMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showDiagnosticsMIActionPerformed
      showDiagnostics();
  }//GEN-LAST:event_showDiagnosticsMIActionPerformed

  private void stopBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_stopBtnActionPerformed
      stop();
  }//GEN-LAST:event_stopBtnActionPerformed

  private void powerBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_powerBtnActionPerformed
      Logger.trace(evt.getActionCommand());
      TrackServiceFactory.getTrackService().powerOn();
  }//GEN-LAST:event_powerBtnActionPerformed

  private void startOfDayBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_startOfDayBtnActionPerformed
      this.synchronizeAccessories();
  }//GEN-LAST:event_startOfDayBtnActionPerformed

  private void synchronizeAccessoriesMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_synchronizeAccessoriesMIActionPerformed
      this.synchronizeAccessories();
  }//GEN-LAST:event_synchronizeAccessoriesMIActionPerformed

    private void showEditDesignBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showEditDesignBtnActionPerformed
        showDesignLayoutPanel();
    }//GEN-LAST:event_showEditDesignBtnActionPerformed

    private void showOverviewBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showOverviewBtnActionPerformed
        showOverviewPanel();
        this.overviewPanel.loadLayout();
    }//GEN-LAST:event_showOverviewBtnActionPerformed

    private void synchronizeAccessories() {
        TrackServiceFactory.getTrackService().synchronizeAccessories();
    }

    private void synchronizeWithController() {
        TrackServiceFactory.getTrackService().removeAllLocomotiveListeners();
        TrackServiceFactory.getTrackService().removeAllAccessoiryListeners();

        TrackServiceFactory.getTrackService().synchronizeLocomotivesWithController();
        TrackServiceFactory.getTrackService().synchronizeAccessoriesWithController();

        this.signalsPanel.refreshPanel();
        this.turnoutsPanel.refreshPanel();
        this.diagnosticPanel.refreshPanel();
        this.overviewPanel.refreshPanel();
    }

    private String getTitleString() {
        String jcsVersion = JCSGUI.getVersionInfo().getVersion();
        if (TrackServiceFactory.getTrackService() != null && TrackServiceFactory.getTrackService().getControllerInfo() != null) {

            DeviceInfo si = TrackServiceFactory.getTrackService().getControllerInfo();
            //String host = si.getIp();
            String description = si.getDescription();
            String catalogNumber = si.getCatalogNumber();
            String serialNumber = si.getSerialNumber();
            //String cs2IpAddress = si.getIp();
            //return "JCS " + jcsVersion + " - Connected to CS 2/3 @ " + cs2IpAddress + " - " + description + " - " + catalogNumber + " sn: " + serialNumber;
            return "JCS " + jcsVersion + " - Connected to CS 2/3 @ " + description + " - " + catalogNumber + " sn: " + serialNumber;
        } else {
            return "JCS " + jcsVersion + " - NOT Connected!";
        }
    }

    @Override
    public void openFiles(List<File> files) {
        Logger.debug("Open Files...");
    }

    @Override
    public boolean handleQuitRequest() {
        int result = JOptionPane.showConfirmDialog(JCSGUI.getJCSFrame(), "Are you sure you want to exit JCS?", "Exit JCS", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    @Override
    public void handleAbout() {
        ImageIcon jcsIcon = new ImageIcon(JCSFrame.class.getResource("/media/jcs-train-64.png"));
        JOptionPane.showMessageDialog(JCSGUI.getJCSFrame(), "Java Command Station By Frans Jacobs", "About JCS", JOptionPane.PLAIN_MESSAGE, jcsIcon);
    }

    @Override
    public void handlePreferences() {
        Logger.trace("handlePreferences");

        OptionDialog preferencesDialog = new OptionDialog(this, false);
        preferencesDialog.setVisible(true);

        Logger.debug("refresh data...");
        this.signalsPanel.refreshPanel();
        this.turnoutsPanel.refreshPanel();
        this.diagnosticPanel.refreshPanel();
        this.overviewPanel.refreshPanel();
    }

    private void setPowerStatus(boolean powerOn, boolean controllerConnected) {

        if (powerOn) {
            powerBtn.setIcon(new ImageIcon(getClass().getResource("/media/power-green-24.png")));
        } else {
            powerBtn.setIcon(new ImageIcon(getClass().getResource("/media/power-red-24.png")));
        }

        powerBtn.setEnabled(controllerConnected);
        stopBtn.setEnabled(controllerConnected);
    }

    private void toggle() {
        lastUpdatedMillis = System.currentTimeMillis();
        blinkToggle = !blinkToggle;

        if (blinkToggle) {
            this.blinkLbl.setIcon(blinkOnIcon);
        } else {
            this.blinkLbl.setIcon(blinkOnIcon2);
        }
    }

    private class ControllerListener implements ControllerEventListener {

        private final JCSFrame jcsFrame;

        ControllerListener(JCSFrame frame) {
            jcsFrame = frame;
        }

        @Override
        public void notify(ControllerEvent event) {
            jcsFrame.setPowerStatus(event.isPowerOn(), event.isConnected());
            Logger.trace(event);
        }
    }

    private class HeartBeat implements HeartBeatListener {

        private final JCSFrame jcsFrame;

        HeartBeat(JCSFrame frame) {
            jcsFrame = frame;
        }

        @Override
        public void toggle() {
            this.jcsFrame.toggle();
        }
    }
    
//    TBT
//    public void doOSX() {
//		try {
//			/* Issue #744: The file handler must be the first handler to be established! Otherwise the
//			 * event of the double-clicked file that led to launching Structorizer might slip through!
//			 */
//			OSXAdapter.setFileHandler(this, getClass().getDeclaredMethod("loadFile", new Class[]{String.class}));
//			OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[]) null));
//			OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[]) null));
//			OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[]) null));
//			OSXAdapter.setDockIconImage(getIconImage());
//
//			logger.info("OS X handlers established.");
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.log(Level.WARNING, "Failed to establish OS X handlers", e);
//		}
//	}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel blinkLbl;
    private JPanel centerPanel;
    private DesignPanel designPanel;
    private DiagnosticPanel diagnosticPanel;
    private JMenu editMenu;
    private JMenu fileMenu;
    private Box.Filler filler1;
    private Box.Filler filler2;
    private Box.Filler filler3;
    private Box.Filler filler4;
    private Box.Filler filler5;
    private JLabel jLabel1;
    private JMenuBar jcsMenuBar;
    private JToolBar jcsToolBar;
    private JMenuItem optionsMI;
    private DisplayLayoutPanel overviewPanel;
    private JButton powerBtn;
    private JMenuItem quitMI;
    private JPanel settingsPanel;
    private JButton showDiagnosticsBtn;
    private JMenuItem showDiagnosticsMI;
    private JButton showEditDesignBtn;
    private JMenuItem showLocosMI;
    private JButton showOverviewBtn;
    private JButton showSignalBtn;
    private JMenuItem showSignalsMI;
    private JButton showTurnoutsBtn;
    private JMenuItem showTurnoutsMI;
    private SignalsPanel signalsPanel;
    private JButton startOfDayBtn;
    private JPanel statusPanel;
    private JPanel statusPanelLeft;
    private JPanel statusPanelMiddle;
    private JPanel statusPanelRight;
    private JButton stopBtn;
    private JMenuItem synchronizeAccessoriesMI;
    private JButton synchronizeLocosBtn;
    private JMenu toolsMenu;
    private TurnoutsPanel turnoutsPanel;
    private JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
}
