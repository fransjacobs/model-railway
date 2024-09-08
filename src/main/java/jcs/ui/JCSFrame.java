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

import com.formdev.flatlaf.util.SystemInfo;
import java.awt.CardLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import jcs.JCS;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.events.DisconnectionEvent;
import jcs.commandStation.events.DisconnectionEventListener;
import jcs.commandStation.events.PowerEvent;
import jcs.entities.InfoBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.LayoutPanel;
import jcs.ui.monitor.FeedbackMonitor;
import jcs.ui.options.CommandStationDialog;
import jcs.ui.options.OptionDialog;
import jcs.ui.util.FrameMonitor;
import jcs.ui.util.UICallback;
import jcs.util.RunUtil;
import jcs.util.SerialPortUtil;
import jcs.util.VersionInfo;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class JCSFrame extends JFrame implements UICallback, DisconnectionEventListener {

  private final Map<KeyStroke, Action> actionMap;
  private FeedbackMonitor feedbackMonitor;

  /**
   * Creates new form JCSFrame
   */
  public JCSFrame() {
    actionMap = new HashMap<>();
    initComponents();

    if (RunUtil.isMacOSX()) {
      this.quitMI.setVisible(false);
      this.optionsMI.setVisible(false);
      this.toolsMenu.setVisible(false);

      if (SystemInfo.isMacFullWindowContentSupported) {
        this.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
        this.getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
        this.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
        //this.getRootPane().putClientProperty( "apple.awt.windowTitleVisible", false );
      }

      initJCS();

      if (SystemInfo.isMacFullWindowContentSupported) {
        //avoid overlap of the red/orange/green buttons and the window title
        this.jcsToolBar.add(Box.createHorizontalStrut(70), 0);
      }

      initKeyStrokes();
    }
  }

  private void initJCS() {
    if (PersistenceFactory.getService() != null) {
      this.setTitle(this.getTitleString());

      //Initialize the Touchbar for MacOS
      if (RunUtil.isMacOSX()) {
        this.setTitle("");
      }

      if (JCS.getJcsCommandStation().isConnected()) {
        setControllerProperties();
      }

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

  public void showExtraToolbar(JToolBar toolbar) {
    this.jcsToolBar.add(toolbar);
    jcsToolBar.doLayout();
    this.repaint();
  }

  public void hideExtraToolbar(JToolBar toolbar) {
    this.jcsToolBar.remove(toolbar);
    jcsToolBar.doLayout();
    this.repaint();
  }

  public void showOverviewPanel() {
    CardLayout card = (CardLayout) this.centerPanel.getLayout();
    card.show(this.centerPanel, "overviewPanel");
    this.overviewPanel.loadLayout();
  }

  public void showLocomotives() {
    Logger.debug("Show Locomotives");
    handlePreferences();
  }

  public void showTurnouts() {
    CardLayout card = (CardLayout) this.centerPanel.getLayout();
    card.show(this.centerPanel, "turnoutsPanel");
  }

  public void showSignals() {
    CardLayout card = (CardLayout) this.centerPanel.getLayout();
    card.show(this.centerPanel, "signalsPanel");
  }

  public void showKeyboards() {
    CardLayout card = (CardLayout) this.centerPanel.getLayout();
    card.show(this.centerPanel, "diagnosticPanel");
  }

  public void showSettings() {
    CardLayout card = (CardLayout) this.centerPanel.getLayout();
    card.show(this.centerPanel, "settingsPanel");
  }

  public void showDesignLayoutPanel() {
    if (!AutoPilot.isAutoModeActive()) {
      CardLayout card = (CardLayout) this.centerPanel.getLayout();
      card.show(this.centerPanel, "designPanel");
      this.layoutPanel.loadLayout();
    }
  }

  public void stop() {
    if (JCS.getJcsCommandStation() != null) {
      JCS.getJcsCommandStation().switchPower(false);
    }
  }

  private void setControllerProperties() {
    if (JCS.getJcsCommandStation() != null) {
      InfoBean info = JCS.getJcsCommandStation().getCommandStationInfo();
      if (info != null) {
        this.connectButton.setSelected(true);
        this.controllerDescriptionLbl.setText(info.getProductName());
        this.controllerCatalogNumberLbl.setText(info.getArticleNumber());
        this.controllerSerialNumberLbl.setText(info.getSerialNumber());
        this.controllerHostNameLbl.setText(info.getHostname());
        this.powerButton.setSelected(JCS.getJcsCommandStation().isPowerOn());
      } else {
        this.connectButton.setSelected(false);
        this.controllerHostNameLbl.setText("Not Connected");
        this.powerButton.setSelected(false);
      }
    }
  }

  private void showSensorMonitor() {
    if (this.feedbackMonitor == null) {
      Logger.trace("Creating a Monitor UI");
      feedbackMonitor = new FeedbackMonitor();
      FrameMonitor.registerFrame(feedbackMonitor, FeedbackMonitor.class.getName());
    }
    this.feedbackMonitor.showMonitor();
  }

  /**
   * This method is called from within the constructor to initialize the form.<br>
   * WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("deprecation")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    toolbarPanel = new javax.swing.JPanel();
    jcsToolBar = new javax.swing.JToolBar();
    connectButton = new javax.swing.JToggleButton();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
    powerButton = new javax.swing.JToggleButton();
    filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
    showOverviewBtn = new javax.swing.JButton();
    showEditDesignBtn = new javax.swing.JButton();
    filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
    showKeyboardBtn = new javax.swing.JButton();
    filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
    showSettingsBtn = new javax.swing.JButton();
    filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
    showFeedbackMonitorBtn = new javax.swing.JButton();
    filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
    statusPanel = new jcs.ui.StatusPanel();
    mainPanel = new javax.swing.JPanel();
    locoDisplaySP = new javax.swing.JSplitPane();
    centerPanel = new javax.swing.JPanel();
    keyboardSensorMessagePanel = new jcs.ui.KeyboardSensorPanel();
    layoutPanel = new jcs.ui.layout.LayoutPanel();
    overviewPanel = new LayoutPanel(true);
    settingsPanel = new javax.swing.JPanel();
    commandStationPanel = new jcs.ui.options.CommandStationPanel();
    leftPanel = new javax.swing.JPanel();
    bottomLeftPanel = new javax.swing.JPanel();
    filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 35));
    jPanel1 = new javax.swing.JPanel();
    controllerLbl = new javax.swing.JLabel();
    controllerDescriptionLbl = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    controllerCatalogLbl = new javax.swing.JLabel();
    controllerCatalogNumberLbl = new javax.swing.JLabel();
    jPanel3 = new javax.swing.JPanel();
    controllerSerialLbl = new javax.swing.JLabel();
    controllerSerialNumberLbl = new javax.swing.JLabel();
    jPanel4 = new javax.swing.JPanel();
    controllerHostLbl = new javax.swing.JLabel();
    controllerHostNameLbl = new javax.swing.JLabel();
    filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 110), new java.awt.Dimension(0, 110), new java.awt.Dimension(32767, 35));
    dispatcherStatusPanel1 = new jcs.ui.DispatcherStatusPanel();
    jcsMenuBar = new javax.swing.JMenuBar();
    fileMenu = new javax.swing.JMenu();
    quitMI = new javax.swing.JMenuItem();
    connectMI = new javax.swing.JMenuItem();
    viewMenu = new javax.swing.JMenu();
    showHome = new javax.swing.JMenuItem();
    showLocosMI = new javax.swing.JMenuItem();
    editLayout = new javax.swing.JMenuItem();
    showKeyboard = new javax.swing.JMenuItem();
    showSensorMonitor = new javax.swing.JMenuItem();
    toolsMenu = new javax.swing.JMenu();
    optionsMI = new javax.swing.JMenuItem();
    commandStationsMI = new javax.swing.JMenuItem();
    helpMenu = new javax.swing.JMenu();
    aboutMI = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    setTitle("Java Central Station");
    setBounds(new java.awt.Rectangle(0, 0, 1400, 900));
    setMinimumSize(new java.awt.Dimension(1250, 900));
    setName("JCSFrame"); // NOI18N
    setSize(new java.awt.Dimension(1250, 950));
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    toolbarPanel.setName("toolbarPanel"); // NOI18N
    java.awt.FlowLayout flowLayout8 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout8.setAlignOnBaseline(true);
    toolbarPanel.setLayout(flowLayout8);

    jcsToolBar.setBorderPainted(false);
    jcsToolBar.setDoubleBuffered(true);
    jcsToolBar.setMargin(new java.awt.Insets(1, 1, 1, 1));
    jcsToolBar.setMaximumSize(new java.awt.Dimension(1050, 42));
    jcsToolBar.setMinimumSize(new java.awt.Dimension(1000, 42));
    jcsToolBar.setName("ToolBar"); // NOI18N
    jcsToolBar.setOpaque(false);
    jcsToolBar.setPreferredSize(new java.awt.Dimension(1300, 42));

    connectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/monitor-off-24.png"))); // NOI18N
    connectButton.setToolTipText("Connect/Disconnect with Central Station");
    connectButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
    connectButton.setDoubleBuffered(true);
    connectButton.setFocusable(false);
    connectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    connectButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    connectButton.setMaximumSize(new java.awt.Dimension(40, 40));
    connectButton.setMinimumSize(new java.awt.Dimension(40, 40));
    connectButton.setName("connectButton"); // NOI18N
    connectButton.setPreferredSize(new java.awt.Dimension(40, 40));
    connectButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/media/monitor-on-24.png"))); // NOI18N
    connectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    connectButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        connectButtonActionPerformed(evt);
      }
    });
    jcsToolBar.add(connectButton);

    filler1.setName("filler1"); // NOI18N
    jcsToolBar.add(filler1);

    powerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/power-red-24.png"))); // NOI18N
    powerButton.setToolTipText("Switch Track power On/Off");
    powerButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
    powerButton.setDoubleBuffered(true);
    powerButton.setFocusable(false);
    powerButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    powerButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    powerButton.setMaximumSize(new java.awt.Dimension(40, 40));
    powerButton.setMinimumSize(new java.awt.Dimension(40, 40));
    powerButton.setName("powerButton"); // NOI18N
    powerButton.setPreferredSize(new java.awt.Dimension(40, 40));
    powerButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/media/power-green-24.png"))); // NOI18N
    powerButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    powerButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        powerButtonActionPerformed(evt);
      }
    });
    jcsToolBar.add(powerButton);
    powerButton.getAccessibleContext().setAccessibleName("Power");

    filler2.setName("filler2"); // NOI18N
    jcsToolBar.add(filler2);

    showOverviewBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/home-24.png"))); // NOI18N
    showOverviewBtn.setToolTipText("Overview");
    showOverviewBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
    showOverviewBtn.setDoubleBuffered(true);
    showOverviewBtn.setFocusable(false);
    showOverviewBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    showOverviewBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
    showOverviewBtn.setMaximumSize(new java.awt.Dimension(40, 40));
    showOverviewBtn.setMinimumSize(new java.awt.Dimension(40, 40));
    showOverviewBtn.setName("showOverviewBtn"); // NOI18N
    showOverviewBtn.setPreferredSize(new java.awt.Dimension(40, 40));
    showOverviewBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showOverviewBtnActionPerformed(evt);
      }
    });
    jcsToolBar.add(showOverviewBtn);
    showOverviewBtn.getAccessibleContext().setAccessibleName("Home");

    showEditDesignBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/paintbrush-24.png"))); // NOI18N
    showEditDesignBtn.setToolTipText("Design Layout");
    showEditDesignBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
    showEditDesignBtn.setFocusable(false);
    showEditDesignBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    showEditDesignBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
    showEditDesignBtn.setMaximumSize(new java.awt.Dimension(40, 40));
    showEditDesignBtn.setMinimumSize(new java.awt.Dimension(40, 40));
    showEditDesignBtn.setName("showEditDesignBtn"); // NOI18N
    showEditDesignBtn.setPreferredSize(new java.awt.Dimension(40, 40));
    showEditDesignBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    showEditDesignBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showEditDesignBtnActionPerformed(evt);
      }
    });
    jcsToolBar.add(showEditDesignBtn);
    showEditDesignBtn.getAccessibleContext().setAccessibleName("Design");

    filler3.setName("filler3"); // NOI18N
    jcsToolBar.add(filler3);

    showKeyboardBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/controller-24.png"))); // NOI18N
    showKeyboardBtn.setToolTipText("Diagnostics");
    showKeyboardBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
    showKeyboardBtn.setDoubleBuffered(true);
    showKeyboardBtn.setFocusable(false);
    showKeyboardBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    showKeyboardBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
    showKeyboardBtn.setMaximumSize(new java.awt.Dimension(40, 40));
    showKeyboardBtn.setMinimumSize(new java.awt.Dimension(40, 40));
    showKeyboardBtn.setName("showKeyboardBtn"); // NOI18N
    showKeyboardBtn.setPreferredSize(new java.awt.Dimension(40, 40));
    showKeyboardBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    showKeyboardBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showKeyboardBtnActionPerformed(evt);
      }
    });
    jcsToolBar.add(showKeyboardBtn);
    showKeyboardBtn.getAccessibleContext().setAccessibleName("Switchboard");

    filler5.setName("filler5"); // NOI18N
    jcsToolBar.add(filler5);

    showSettingsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/load-24.png"))); // NOI18N
    showSettingsBtn.setFocusable(false);
    showSettingsBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    showSettingsBtn.setMaximumSize(new java.awt.Dimension(40, 40));
    showSettingsBtn.setMinimumSize(new java.awt.Dimension(40, 40));
    showSettingsBtn.setName("showSettingsBtn"); // NOI18N
    showSettingsBtn.setPreferredSize(new java.awt.Dimension(40, 40));
    showSettingsBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    showSettingsBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showSettingsBtnActionPerformed(evt);
      }
    });
    jcsToolBar.add(showSettingsBtn);

    filler9.setName("filler9"); // NOI18N
    jcsToolBar.add(filler9);

    showFeedbackMonitorBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/monitor-24.png"))); // NOI18N
    showFeedbackMonitorBtn.setFocusable(false);
    showFeedbackMonitorBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    showFeedbackMonitorBtn.setMaximumSize(new java.awt.Dimension(40, 40));
    showFeedbackMonitorBtn.setMinimumSize(new java.awt.Dimension(40, 40));
    showFeedbackMonitorBtn.setName("showFeedbackMonitorBtn"); // NOI18N
    showFeedbackMonitorBtn.setPreferredSize(new java.awt.Dimension(40, 40));
    showFeedbackMonitorBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    showFeedbackMonitorBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showFeedbackMonitorBtnActionPerformed(evt);
      }
    });
    jcsToolBar.add(showFeedbackMonitorBtn);

    filler8.setName("filler8"); // NOI18N
    jcsToolBar.add(filler8);

    toolbarPanel.add(jcsToolBar);

    getContentPane().add(toolbarPanel, java.awt.BorderLayout.NORTH);

    statusPanel.setName("statusPanel"); // NOI18N
    getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);

    mainPanel.setMinimumSize(new java.awt.Dimension(1050, 900));
    mainPanel.setName("mainPanel"); // NOI18N
    mainPanel.setPreferredSize(new java.awt.Dimension(1315, 850));
    mainPanel.setLayout(new java.awt.BorderLayout());

    locoDisplaySP.setDividerLocation(300);
    locoDisplaySP.setMinimumSize(new java.awt.Dimension(1050, 900));
    locoDisplaySP.setName("locoDisplaySP"); // NOI18N
    locoDisplaySP.setPreferredSize(new java.awt.Dimension(1050, 850));

    centerPanel.setMinimumSize(new java.awt.Dimension(1000, 900));
    centerPanel.setName("centerPanel"); // NOI18N
    centerPanel.setPreferredSize(new java.awt.Dimension(1010, 900));
    centerPanel.setLayout(new java.awt.CardLayout());

    keyboardSensorMessagePanel.setMinimumSize(new java.awt.Dimension(885, 840));
    keyboardSensorMessagePanel.setName("keyboardSensorMessagePanel"); // NOI18N
    centerPanel.add(keyboardSensorMessagePanel, "diagnosticPanel");

    layoutPanel.setMinimumSize(new java.awt.Dimension(885, 160));
    layoutPanel.setName("layoutPanel"); // NOI18N
    centerPanel.add(layoutPanel, "designPanel");
    layoutPanel.getAccessibleContext().setAccessibleName("designPanel");

    overviewPanel.setName("overviewPanel"); // NOI18N
    centerPanel.add(overviewPanel, "overviewPanel");
    overviewPanel.getAccessibleContext().setAccessibleName("overviewPanel");

    settingsPanel.setName("settingsPanel"); // NOI18N
    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    settingsPanel.setLayout(flowLayout1);

    commandStationPanel.setName("commandStationPanel"); // NOI18N
    settingsPanel.add(commandStationPanel);

    centerPanel.add(settingsPanel, "settingsPanel");

    locoDisplaySP.setRightComponent(centerPanel);

    leftPanel.setMinimumSize(new java.awt.Dimension(220, 850));
    leftPanel.setName("leftPanel"); // NOI18N
    leftPanel.setPreferredSize(new java.awt.Dimension(225, 845));
    leftPanel.setLayout(new java.awt.BorderLayout(1, 1));

    bottomLeftPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Controller Properties"));
    bottomLeftPanel.setMinimumSize(new java.awt.Dimension(220, 200));
    bottomLeftPanel.setName("bottomLeftPanel"); // NOI18N
    bottomLeftPanel.setPreferredSize(new java.awt.Dimension(200, 200));
    bottomLeftPanel.setLayout(new javax.swing.BoxLayout(bottomLeftPanel, javax.swing.BoxLayout.Y_AXIS));

    filler7.setName("filler7"); // NOI18N
    bottomLeftPanel.add(filler7);

    jPanel1.setName("jPanel1"); // NOI18N
    java.awt.FlowLayout flowLayout3 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout3.setAlignOnBaseline(true);
    jPanel1.setLayout(flowLayout3);

    controllerLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    controllerLbl.setLabelFor(controllerDescriptionLbl);
    controllerLbl.setText("Controller:");
    controllerLbl.setDoubleBuffered(true);
    controllerLbl.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    controllerLbl.setName("controllerLbl"); // NOI18N
    controllerLbl.setPreferredSize(new java.awt.Dimension(75, 16));
    jPanel1.add(controllerLbl);

    controllerDescriptionLbl.setText("...");
    controllerDescriptionLbl.setName("controllerDescriptionLbl"); // NOI18N
    controllerDescriptionLbl.setPreferredSize(new java.awt.Dimension(125, 16));
    jPanel1.add(controllerDescriptionLbl);

    bottomLeftPanel.add(jPanel1);

    jPanel2.setName("jPanel2"); // NOI18N
    java.awt.FlowLayout flowLayout4 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout4.setAlignOnBaseline(true);
    jPanel2.setLayout(flowLayout4);

    controllerCatalogLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    controllerCatalogLbl.setLabelFor(controllerCatalogNumberLbl);
    controllerCatalogLbl.setText("Model:");
    controllerCatalogLbl.setName("controllerCatalogLbl"); // NOI18N
    controllerCatalogLbl.setOpaque(true);
    controllerCatalogLbl.setPreferredSize(new java.awt.Dimension(75, 16));
    jPanel2.add(controllerCatalogLbl);

    controllerCatalogNumberLbl.setText("...");
    controllerCatalogNumberLbl.setName("controllerCatalogNumberLbl"); // NOI18N
    controllerCatalogNumberLbl.setPreferredSize(new java.awt.Dimension(125, 16));
    jPanel2.add(controllerCatalogNumberLbl);

    bottomLeftPanel.add(jPanel2);

    jPanel3.setName("jPanel3"); // NOI18N
    java.awt.FlowLayout flowLayout6 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout6.setAlignOnBaseline(true);
    jPanel3.setLayout(flowLayout6);

    controllerSerialLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    controllerSerialLbl.setText("Serial:");
    controllerSerialLbl.setName("controllerSerialLbl"); // NOI18N
    controllerSerialLbl.setPreferredSize(new java.awt.Dimension(75, 16));
    jPanel3.add(controllerSerialLbl);

    controllerSerialNumberLbl.setText("...");
    controllerSerialNumberLbl.setName("controllerSerialNumberLbl"); // NOI18N
    controllerSerialNumberLbl.setPreferredSize(new java.awt.Dimension(125, 16));
    jPanel3.add(controllerSerialNumberLbl);

    bottomLeftPanel.add(jPanel3);

    jPanel4.setName("jPanel4"); // NOI18N
    java.awt.FlowLayout flowLayout5 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout5.setAlignOnBaseline(true);
    jPanel4.setLayout(flowLayout5);

    controllerHostLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    controllerHostLbl.setText("Host:");
    controllerHostLbl.setName("controllerHostLbl"); // NOI18N
    controllerHostLbl.setPreferredSize(new java.awt.Dimension(75, 16));
    jPanel4.add(controllerHostLbl);

    controllerHostNameLbl.setText("...");
    controllerHostNameLbl.setName("controllerHostNameLbl"); // NOI18N
    controllerHostNameLbl.setPreferredSize(new java.awt.Dimension(125, 16));
    jPanel4.add(controllerHostNameLbl);

    bottomLeftPanel.add(jPanel4);

    filler6.setName("filler6"); // NOI18N
    bottomLeftPanel.add(filler6);

    leftPanel.add(bottomLeftPanel, java.awt.BorderLayout.SOUTH);

    dispatcherStatusPanel1.setName("dispatcherStatusPanel1"); // NOI18N
    leftPanel.add(dispatcherStatusPanel1, java.awt.BorderLayout.CENTER);

    locoDisplaySP.setLeftComponent(leftPanel);

    mainPanel.add(locoDisplaySP, java.awt.BorderLayout.CENTER);

    getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

    jcsMenuBar.setName("jcsMenuBar"); // NOI18N

    fileMenu.setText("File");
    fileMenu.setName("fileMenu"); // NOI18N

    quitMI.setText("Quit");
    quitMI.setName("quitMI"); // NOI18N
    quitMI.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        quitMIActionPerformed(evt);
      }
    });
    fileMenu.add(quitMI);

    connectMI.setText("Connect");
    connectMI.setName("connectMI"); // NOI18N
    connectMI.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        connectMIActionPerformed(evt);
      }
    });
    fileMenu.add(connectMI);

    jcsMenuBar.add(fileMenu);

    viewMenu.setText("View");
    viewMenu.setName("viewMenu"); // NOI18N

    showHome.setText("Home");
    showHome.setName("showHome"); // NOI18N
    showHome.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showHomeActionPerformed(evt);
      }
    });
    viewMenu.add(showHome);

    showLocosMI.setLabel("Locomotives");
    showLocosMI.setName("showLocosMI"); // NOI18N
    showLocosMI.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showLocosMIActionPerformed(evt);
      }
    });
    viewMenu.add(showLocosMI);

    editLayout.setText("Edit Layout");
    editLayout.setName("editLayout"); // NOI18N
    editLayout.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        editLayoutActionPerformed(evt);
      }
    });
    viewMenu.add(editLayout);

    showKeyboard.setText("Keyboard");
    showKeyboard.setName("showKeyboard"); // NOI18N
    showKeyboard.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showKeyboardActionPerformed(evt);
      }
    });
    viewMenu.add(showKeyboard);

    showSensorMonitor.setText("Sensor Monitor");
    showSensorMonitor.setName("showSensorMonitor"); // NOI18N
    showSensorMonitor.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showSensorMonitorActionPerformed(evt);
      }
    });
    viewMenu.add(showSensorMonitor);

    jcsMenuBar.add(viewMenu);

    toolsMenu.setText("Tools");
    toolsMenu.setName("toolsMenu"); // NOI18N

    optionsMI.setText("Options");
    optionsMI.setName("optionsMI"); // NOI18N
    optionsMI.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        optionsMIActionPerformed(evt);
      }
    });
    toolsMenu.add(optionsMI);

    commandStationsMI.setText("Command Stations");
    commandStationsMI.setName("commandStationsMI"); // NOI18N
    commandStationsMI.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        commandStationsMIActionPerformed(evt);
      }
    });
    toolsMenu.add(commandStationsMI);

    jcsMenuBar.add(toolsMenu);

    helpMenu.setText("Help");
    helpMenu.setName("helpMenu"); // NOI18N

    aboutMI.setText("About");
    aboutMI.setName("aboutMI"); // NOI18N
    aboutMI.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        aboutMIActionPerformed(evt);
      }
    });
    helpMenu.add(aboutMI);

    jcsMenuBar.add(helpMenu);

    setJMenuBar(jcsMenuBar);

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void showLocosMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showLocosMIActionPerformed
      showLocomotives();
    }//GEN-LAST:event_showLocosMIActionPerformed

    private void showKeyboardBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showKeyboardBtnActionPerformed
      showKeyboards();
    }//GEN-LAST:event_showKeyboardBtnActionPerformed

    private void quitMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_quitMIActionPerformed
      this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }//GEN-LAST:event_quitMIActionPerformed

    private void formWindowClosing(WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      boolean closed = this.handleQuitRequest();
      if (closed) {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(false);
        dispose();

        //Force close ports
        SerialPortUtil.closeAllPorts();
        Logger.debug("Shutting down");
      }
    }//GEN-LAST:event_formWindowClosing

    private void optionsMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_optionsMIActionPerformed
      handlePreferences();
    }//GEN-LAST:event_optionsMIActionPerformed

    private void showEditDesignBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showEditDesignBtnActionPerformed
      showDesignLayoutPanel();
    }//GEN-LAST:event_showEditDesignBtnActionPerformed

    private void showOverviewBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showOverviewBtnActionPerformed
      showOverviewPanel();
      this.overviewPanel.loadLayout();
    }//GEN-LAST:event_showOverviewBtnActionPerformed

    private void powerButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_powerButtonActionPerformed
      boolean on = ((JToggleButton) evt.getSource()).isSelected();
      if (JCS.getJcsCommandStation() != null) {
        JCS.getJcsCommandStation().switchPower(on);
      }
    }//GEN-LAST:event_powerButtonActionPerformed

    private void showFeedbackMonitorBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showFeedbackMonitorBtnActionPerformed
      showSensorMonitor();
    }//GEN-LAST:event_showFeedbackMonitorBtnActionPerformed

  public void connect(boolean connect) {
    if (JCS.getJcsCommandStation() != null) {
      if (connect) {
        JCS.getJcsCommandStation().connect();

        InfoBean info = JCS.getJcsCommandStation().getCommandStationInfo();
        if (info != null) {
          this.connectButton.setSelected(true);
          this.controllerDescriptionLbl.setText(info.getProductName());
          this.controllerCatalogNumberLbl.setText(info.getArticleNumber());
          this.controllerSerialNumberLbl.setText(info.getSerialNumber());
          this.controllerHostNameLbl.setText(info.getHostname());
          this.powerButton.setSelected(JCS.getJcsCommandStation().isPowerOn());
          this.connectMI.setText("Disconnect");

        }
      } else {
        JCS.getJcsCommandStation().disconnect();
        this.controllerDescriptionLbl.setText("-");
        this.controllerCatalogNumberLbl.setText("-");
        this.controllerSerialNumberLbl.setText("-");
        this.controllerHostNameLbl.setText("Disconnected");

        this.connectMI.setText("Connect");
      }
    }
    JCS.getJcsCommandStation().addDisconnectionEventListener(this);

    this.powerButton.setEnabled(connect);
    this.showFeedbackMonitorBtn.setEnabled(connect);
    this.setControllerProperties();
  }

    private void connectButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
      boolean connect = ((JToggleButton) evt.getSource()).isSelected();
      connect(connect);
    }//GEN-LAST:event_connectButtonActionPerformed

    private void showHomeActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showHomeActionPerformed
      showOverviewPanel();
      this.overviewPanel.loadLayout();
    }//GEN-LAST:event_showHomeActionPerformed

    private void editLayoutActionPerformed(ActionEvent evt) {//GEN-FIRST:event_editLayoutActionPerformed
      showDesignLayoutPanel();
    }//GEN-LAST:event_editLayoutActionPerformed

    private void showKeyboardActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showKeyboardActionPerformed
      showKeyboards();
    }//GEN-LAST:event_showKeyboardActionPerformed

    private void showSensorMonitorActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showSensorMonitorActionPerformed
      showSensorMonitor();
    }//GEN-LAST:event_showSensorMonitorActionPerformed

    private void connectMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_connectMIActionPerformed
      boolean connect = "Connect".equals(((JMenuItem) evt.getSource()).getText());
      connect(connect);
      this.connectButton.setSelected(connect);
    }//GEN-LAST:event_connectMIActionPerformed

  private void commandStationsMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_commandStationsMIActionPerformed
    CommandStationDialog csd = new CommandStationDialog(this, true);
    csd.setLocationRelativeTo(null);
    csd.setVisible(true);
  }//GEN-LAST:event_commandStationsMIActionPerformed

  private void showSettingsBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showSettingsBtnActionPerformed
    showSettings();
  }//GEN-LAST:event_showSettingsBtnActionPerformed

  private void aboutMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_aboutMIActionPerformed
    Logger.trace(evt.getActionCommand());
    AboutDialog dialog = new AboutDialog(this, true);
    dialog.pack();
    dialog.setLocationRelativeTo(null);

    dialog.setVisible(true);
  }//GEN-LAST:event_aboutMIActionPerformed

  private String getTitleString() {
    String jcsVersion = VersionInfo.getVersion();

    if (JCS.getJcsCommandStation() != null && JCS.getJcsCommandStation().getCommandStationInfo() != null) {
      InfoBean info = JCS.getJcsCommandStation().getCommandStationInfo();
      return "JCS " + "Connected to " + info.getProductName();
    } else {
      return "JCS " + jcsVersion + " - NOT Connected!";
    }
  }

  @Override
  public void openFiles(List<File> files) {
    Logger.trace("Open Files...");
  }

  @Override
  public void onDisconnect(DisconnectionEvent event) {
    JOptionPane.showMessageDialog(this, "CommandStation " + event.getSource() + " is disconnected.", "Disconnection error", JOptionPane.ERROR_MESSAGE);

    this.controllerHostNameLbl.setText("Disconnected");
    this.connectMI.setText("Connect");
    this.connectButton.setSelected(false);
  }

  @Override
  public boolean handleQuitRequest() {
    int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit JCS?", "Exit JCS", JOptionPane.YES_NO_OPTION);
    return result == JOptionPane.YES_OPTION;
  }

  @Override
  public void handleAbout() {
    //ImageIcon jcsIcon = new ImageIcon(JCSFrame.class.getResource("/media/jcs-train-64.png"));
    //JOptionPane.showMessageDialog(this, "Java Command Station By Frans Jacobs", "About JCS", JOptionPane.PLAIN_MESSAGE, jcsIcon);
    //TODO make it more generic
    AboutDialog dialog = new AboutDialog(this, true);
    dialog.pack();
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
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

  public void powerChanged(PowerEvent event) {
    this.powerButton.setSelected(event.isPower());
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem aboutMI;
  private javax.swing.JPanel bottomLeftPanel;
  private javax.swing.JPanel centerPanel;
  private jcs.ui.options.CommandStationPanel commandStationPanel;
  private javax.swing.JMenuItem commandStationsMI;
  private javax.swing.JToggleButton connectButton;
  private javax.swing.JMenuItem connectMI;
  private javax.swing.JLabel controllerCatalogLbl;
  private javax.swing.JLabel controllerCatalogNumberLbl;
  private javax.swing.JLabel controllerDescriptionLbl;
  private javax.swing.JLabel controllerHostLbl;
  private javax.swing.JLabel controllerHostNameLbl;
  private javax.swing.JLabel controllerLbl;
  private javax.swing.JLabel controllerSerialLbl;
  private javax.swing.JLabel controllerSerialNumberLbl;
  private jcs.ui.DispatcherStatusPanel dispatcherStatusPanel1;
  private javax.swing.JMenuItem editLayout;
  private javax.swing.JMenu fileMenu;
  private javax.swing.Box.Filler filler1;
  private javax.swing.Box.Filler filler2;
  private javax.swing.Box.Filler filler3;
  private javax.swing.Box.Filler filler5;
  private javax.swing.Box.Filler filler6;
  private javax.swing.Box.Filler filler7;
  private javax.swing.Box.Filler filler8;
  private javax.swing.Box.Filler filler9;
  private javax.swing.JMenu helpMenu;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JMenuBar jcsMenuBar;
  private javax.swing.JToolBar jcsToolBar;
  private jcs.ui.KeyboardSensorPanel keyboardSensorMessagePanel;
  private jcs.ui.layout.LayoutPanel layoutPanel;
  private javax.swing.JPanel leftPanel;
  private javax.swing.JSplitPane locoDisplaySP;
  private javax.swing.JPanel mainPanel;
  private javax.swing.JMenuItem optionsMI;
  private jcs.ui.layout.LayoutPanel overviewPanel;
  private javax.swing.JToggleButton powerButton;
  private javax.swing.JMenuItem quitMI;
  private javax.swing.JPanel settingsPanel;
  private javax.swing.JButton showEditDesignBtn;
  private javax.swing.JButton showFeedbackMonitorBtn;
  private javax.swing.JMenuItem showHome;
  private javax.swing.JMenuItem showKeyboard;
  private javax.swing.JButton showKeyboardBtn;
  private javax.swing.JMenuItem showLocosMI;
  private javax.swing.JButton showOverviewBtn;
  private javax.swing.JMenuItem showSensorMonitor;
  private javax.swing.JButton showSettingsBtn;
  private jcs.ui.StatusPanel statusPanel;
  private javax.swing.JPanel toolbarPanel;
  private javax.swing.JMenu toolsMenu;
  private javax.swing.JMenu viewMenu;
  // End of variables declaration//GEN-END:variables
}
