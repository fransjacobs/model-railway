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
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
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
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import jcs.JCS;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.entities.InfoBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.LayoutCanvas;
import jcs.ui.layout.LayoutPanel;
import jcs.ui.panel.SmallDriverCabPanel;
import jcs.ui.settings.AccessoryDialog;
import jcs.ui.settings.CommandStationDialog;
import jcs.ui.settings.CommandStationPanel;
import jcs.ui.settings.LocomotiveDialog;
import jcs.ui.settings.SettingsDialog;
import jcs.ui.settings.PropertiesDialog;
import jcs.ui.util.FrameMonitor;
import jcs.ui.util.UICallback;
import jcs.util.RunUtil;
import jcs.util.SerialPortUtil;
import jcs.util.VersionInfo;
import org.tinylog.Logger;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.util.Ping;

/**
 *
 * @author frans
 */
public class JCSFrame extends JFrame implements UICallback, ConnectionEventListener {

  private static final long serialVersionUID = -5800900684173242844L;

  private final Map<KeyStroke, Action> actionMap;
  private FeedbackSensorDialog feedbackMonitor;

  private boolean editMode = false;

  private LocomotiveDialog locomotiveDialog;
  private AccessoryDialog accessoryDialog;
  private CommandStationDialog commandStationDialog;
  private PropertiesDialog propertiesDialog;

  private SettingsDialog settingsDialog;

  /**
   * Creates new form JCSFrame
   */
  public JCSFrame() {
    actionMap = new HashMap<>();
    initComponents();

    if (RunUtil.isMacOSX()) {
      //quitMI.setVisible(false);
      //optionsMI.setVisible(false);
      //settingsMenu.setVisible(false);

      //see https://www.formdev.com/flatlaf/macos/
      if (SystemInfo.isMacFullWindowContentSupported) {
        getRootPane().putClientProperty("apple.awt.transparentTitleBar", false);
        getRootPane().putClientProperty("apple.awt.fullWindowContent", false);

        //getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);
        //avoid overlap of the red/orange/green buttons and the window title
        //jcsToolBar.add(Box.createHorizontalStrut(70), 0);
      }
    }
    initJCS();
    initKeyStrokes();
  }

  private void initJCS() {
    if (PersistenceFactory.getService() != null) {
      setTitle(getTitleString());

      if (JCS.getJcsCommandStation().isConnected()) {
        setControllerProperties();
      }

      //Connect the panels so that they are notified with the locomotive selection
      dispatcherStatusPanel.addLocomotiveSelectionChangeListener(smallDriverCabPanel);

      //Show the default panel
      showOverviewPanel();
      editMode = false;
      //JCS.addRefreshListener(dispatcherStatusPanel);
    }
  }

  private void initKeyStrokes() {
    KeyStroke keySpace = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
    KeyStroke keyQuit = KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK);

    KeyStroke keySensorMonitor = KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.ALT_DOWN_MASK);
    KeyStroke keyHome = KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK);
    KeyStroke keyEdit = KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);

    //Edit screen
    KeyStroke keyModeSelect = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK);
    KeyStroke keyModeAdd = KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK);
    KeyStroke keyModeDelete = KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.ALT_DOWN_MASK);

    KeyStroke keyRotate = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK);
    KeyStroke keyFlipHorizontal = KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.ALT_DOWN_MASK);
    KeyStroke keyFlipVertical = KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK);

    actionMap.put(keySpace, new PowerAction());
    actionMap.put(keyQuit, new QuitAction());
    actionMap.put(keySensorMonitor, new ShowMonitorAction());
    actionMap.put(keyHome, new HomeAction());
    actionMap.put(keyEdit, new EditAction());

    actionMap.put(keyHome, new HomeAction());

    actionMap.put(keyModeSelect, new SelectModeKeyAction());
    actionMap.put(keyModeAdd, new AddModeKeyAction());
    actionMap.put(keyModeDelete, new DeleteModeKeyAction());

    actionMap.put(keyRotate, new RotateKeyAction());
    actionMap.put(keyFlipHorizontal, new FlipHorizontalKeyAction());
    actionMap.put(keyFlipVertical, new FlipVerticalKeyAction());

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
    jcsToolBar.add(toolbar);
    jcsToolBar.doLayout();
    toolbarPanel.repaint();
  }

  public void hideExtraToolbar(JToolBar toolbar) {
    jcsToolBar.remove(toolbar);
    jcsToolBar.doLayout();
    toolbarPanel.repaint();
  }

  public void showOverviewPanel() {
    CardLayout card = (CardLayout) centerPanel.getLayout();
    card.show(centerPanel, "overviewPanel");
    editMode = false;
    overviewPanel.loadLayout();
  }

  private void showLocomotives() {
    Logger.debug("Show Locomotives");

    if (locomotiveDialog == null) {
      locomotiveDialog = new LocomotiveDialog(this, true);
      locomotiveDialog.pack();
      locomotiveDialog.setLocationRelativeTo(null);
    }
    locomotiveDialog.setVisible(true);

    //Should add a listener here?
  }

  private void showAccessories() {
    if (accessoryDialog == null) {
      accessoryDialog = new AccessoryDialog(this, true);
      accessoryDialog.pack();
      accessoryDialog.setLocationRelativeTo(null);
    }
    accessoryDialog.setVisible(true);
  }

  private void showProperties() {
    if (propertiesDialog == null) {
      propertiesDialog = new PropertiesDialog(this, true);
      propertiesDialog.pack();
      propertiesDialog.setLocationRelativeTo(null);
    }
    propertiesDialog.setVisible(true);
  }

  private void showCommandStations() {
    if (commandStationDialog == null) {
      commandStationDialog = new CommandStationDialog(this, true);
      commandStationDialog.pack();
      commandStationDialog.setLocationRelativeTo(null);
    }
    commandStationDialog.setVisible(true);
  }

  private void showRoutes() {
    if (editMode) {
      layoutPanel.showRoutes();
    }
  }

  private void showKeyboards() {
    CardLayout card = (CardLayout) this.centerPanel.getLayout();
    card.show(centerPanel, "diagnosticPanel");
    editMode = false;
  }

  private void showVNCConsole() {
    CardLayout card = (CardLayout) this.centerPanel.getLayout();
    card.show(centerPanel, "vncPanel");
    editMode = false;
  }

  private void showEditLayoutPanel() {
    if (!AutoPilot.isAutoModeActive()) {
      CardLayout card = (CardLayout) centerPanel.getLayout();
      card.show(centerPanel, "designPanel");
      layoutPanel.loadLayout();
      editMode = true;
    }
  }

  private void setControllerProperties() {
    if (JCS.getJcsCommandStation() != null) {
      InfoBean info = JCS.getJcsCommandStation().getCommandStationInfo();
      boolean connected = JCS.getJcsCommandStation().isConnected();
      if (info != null) {
        this.connectButton.setSelected(connected);
        this.powerButton.setSelected(JCS.getJcsCommandStation().isPowerOn());
      } else {
        this.connectButton.setSelected(connected);
        this.powerButton.setSelected(connected);
      }
      boolean virt = JCS.getJcsCommandStation().isVirtual();
      virtualCBMI.setSelected(virt);
    }
  }

  private void showSensorMonitor() {
    if (feedbackMonitor == null) {
      Logger.trace("Creating a Monitor UI");
      feedbackMonitor = new FeedbackSensorDialog(this, false);
      FrameMonitor.registerFrame(feedbackMonitor, FeedbackSensorDialog.class.getName());
    }
    feedbackMonitor.showMonitor();
  }

  /**
   * This method is called from within the constructor to initialize the form.<br>
   * WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("deprecation")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    toolbarPanel = new JPanel();
    jcsToolBar = new JToolBar();
    powerButton = new JToggleButton();
    filler1 = new Box.Filler(new Dimension(5, 0), new Dimension(5, 0), new Dimension(5, 32767));
    connectButton = new JToggleButton();
    filler2 = new Box.Filler(new Dimension(10, 0), new Dimension(10, 0), new Dimension(10, 32767));
    showOverviewBtn = new JButton();
    showEditDesignBtn = new JButton();
    showVNCBtn = new JButton();
    showKeyboardBtn = new JButton();
    filler3 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    showFeedbackMonitorBtn = new JButton();
    filler4 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    autoPilotBtn = new JToggleButton();
    filler5 = new Box.Filler(new Dimension(5, 0), new Dimension(5, 0), new Dimension(5, 32767));
    startAllLocsBtn = new JButton();
    filler8 = new Box.Filler(new Dimension(25, 0), new Dimension(25, 0), new Dimension(25, 32767));
    statusPanel = new StatusPanel();
    mainPanel = new JPanel();
    locoDisplaySP = new JSplitPane();
    centerPanel = new JPanel();
    keyboardSensorMessagePanel = new KeyboardSensorPanel();
    layoutPanel = new LayoutPanel();
    overviewPanel = new LayoutPanel(true);
    settingsPanel = new JPanel();
    commandStationPanel = new CommandStationPanel();
    vncPanel = new VNCPanel();
    leftPanel = new JPanel();
    locoSplitPane = new JSplitPane();
    dispatcherStatusPanel = new DispatcherStatusPanel();
    smallDriverCabPanel = new SmallDriverCabPanel();
    jcsMenuBar = new JMenuBar();
    fileMenu = new JMenu();
    quitMI = new JMenuItem();
    connectMI = new JMenuItem();
    virtualCBMI = new JCheckBoxMenuItem();
    autoPilotMI = new JMenuItem();
    startAllLocsMI = new JMenuItem();
    resetAutopilotMI = new JMenuItem();
    editMenu = new JMenu();
    rotateTileMI = new JMenuItem();
    flipTileHorizontallyMI = new JMenuItem();
    flipTileVerticallyMI = new JMenuItem();
    deleteTileMI = new JMenuItem();
    windowMenu = new JMenu();
    showHome = new JMenuItem();
    editLayout = new JMenuItem();
    vncMI = new JMenuItem();
    showKeyboard = new JMenuItem();
    showSensorMonitor = new JMenuItem();
    showRoutesMI = new JMenuItem();
    settingsMenu = new JMenu();
    showLocosMI = new JMenuItem();
    showAccessoryMI = new JMenuItem();
    showCommandStationsMI = new JMenuItem();
    showPropertiesMI = new JMenuItem();
    helpMenu = new JMenu();
    aboutMI = new JMenuItem();

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setTitle("Java Central Station");
    setBounds(new Rectangle(0, 0, 1400, 900));
    setMinimumSize(new Dimension(1350, 850));
    setName("JCSFrame"); // NOI18N
    setSize(new Dimension(1350, 870));
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    toolbarPanel.setName("toolbarPanel"); // NOI18N
    toolbarPanel.setPreferredSize(new Dimension(1350, 52));
    FlowLayout flowLayout2 = new FlowLayout(FlowLayout.LEFT);
    flowLayout2.setAlignOnBaseline(true);
    toolbarPanel.setLayout(flowLayout2);

    jcsToolBar.setMaximumSize(new Dimension(1050, 42));
    jcsToolBar.setMinimumSize(new Dimension(1000, 42));
    jcsToolBar.setName("ToolBar"); // NOI18N
    jcsToolBar.setOpaque(false);
    jcsToolBar.setPreferredSize(new Dimension(1380, 42));

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

    filler1.setName("filler1"); // NOI18N
    jcsToolBar.add(filler1);

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
    showEditDesignBtn.setToolTipText("Edit Layout");
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

    showVNCBtn.setIcon(new ImageIcon(getClass().getResource("/media/vnc-icon-bk-24.png"))); // NOI18N
    showVNCBtn.setToolTipText("Open VNC Console");
    showVNCBtn.setBorder(new LineBorder(new Color(204, 204, 204), 1, true));
    showVNCBtn.setFocusable(false);
    showVNCBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    showVNCBtn.setMargin(new Insets(2, 2, 2, 2));
    showVNCBtn.setMaximumSize(new Dimension(40, 40));
    showVNCBtn.setMinimumSize(new Dimension(40, 40));
    showVNCBtn.setName("showVNCBtn"); // NOI18N
    showVNCBtn.setPreferredSize(new Dimension(40, 40));
    showVNCBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    showVNCBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showVNCBtnActionPerformed(evt);
      }
    });
    jcsToolBar.add(showVNCBtn);

    showKeyboardBtn.setIcon(new ImageIcon(getClass().getResource("/media/controller-24.png"))); // NOI18N
    showKeyboardBtn.setToolTipText("Show Accessory Keyboard");
    showKeyboardBtn.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 204)));
    showKeyboardBtn.setDoubleBuffered(true);
    showKeyboardBtn.setFocusable(false);
    showKeyboardBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    showKeyboardBtn.setMargin(new Insets(0, 0, 0, 0));
    showKeyboardBtn.setMaximumSize(new Dimension(40, 40));
    showKeyboardBtn.setMinimumSize(new Dimension(40, 40));
    showKeyboardBtn.setName("showKeyboardBtn"); // NOI18N
    showKeyboardBtn.setPreferredSize(new Dimension(40, 40));
    showKeyboardBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    showKeyboardBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showKeyboardBtnActionPerformed(evt);
      }
    });
    jcsToolBar.add(showKeyboardBtn);
    showKeyboardBtn.getAccessibleContext().setAccessibleName("Switchboard");

    filler3.setName("filler3"); // NOI18N
    jcsToolBar.add(filler3);

    showFeedbackMonitorBtn.setIcon(new ImageIcon(getClass().getResource("/media/monitor-24.png"))); // NOI18N
    showFeedbackMonitorBtn.setToolTipText("Show Sensor Monitor");
    showFeedbackMonitorBtn.setBorder(new LineBorder(new Color(204, 204, 204), 1, true));
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

    filler4.setName("filler4"); // NOI18N
    jcsToolBar.add(filler4);

    autoPilotBtn.setIcon(new ImageIcon(getClass().getResource("/media/pilot.png"))); // NOI18N
    autoPilotBtn.setToolTipText("En- or Disable automatic driving");
    autoPilotBtn.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 204)));
    autoPilotBtn.setDoubleBuffered(true);
    autoPilotBtn.setFocusable(false);
    autoPilotBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    autoPilotBtn.setMargin(new Insets(0, 0, 0, 0));
    autoPilotBtn.setMaximumSize(new Dimension(40, 40));
    autoPilotBtn.setMinimumSize(new Dimension(40, 40));
    autoPilotBtn.setName("autoPilotBtn"); // NOI18N
    autoPilotBtn.setPreferredSize(new Dimension(40, 40));
    autoPilotBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/pilot-green.png"))); // NOI18N
    autoPilotBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    autoPilotBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        autoPilotBtnActionPerformed(evt);
      }
    });
    jcsToolBar.add(autoPilotBtn);

    filler5.setName("filler5"); // NOI18N
    jcsToolBar.add(filler5);

    startAllLocsBtn.setIcon(new ImageIcon(getClass().getResource("/media/arrowhead-right-gn.png"))); // NOI18N
    startAllLocsBtn.setToolTipText("Start all Locomotives");
    startAllLocsBtn.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 204)));
    startAllLocsBtn.setDisabledIcon(new ImageIcon(getClass().getResource("/media/pause-gr.png"))); // NOI18N
    startAllLocsBtn.setEnabled(false);
    startAllLocsBtn.setFocusable(false);
    startAllLocsBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    startAllLocsBtn.setMargin(new Insets(0, 0, 0, 0));
    startAllLocsBtn.setMaximumSize(new Dimension(40, 40));
    startAllLocsBtn.setMinimumSize(new Dimension(40, 40));
    startAllLocsBtn.setName("startAllLocsBtn"); // NOI18N
    startAllLocsBtn.setPreferredSize(new Dimension(40, 40));
    startAllLocsBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    startAllLocsBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        startAllLocsBtnActionPerformed(evt);
      }
    });
    jcsToolBar.add(startAllLocsBtn);

    filler8.setName("filler8"); // NOI18N
    jcsToolBar.add(filler8);

    toolbarPanel.add(jcsToolBar);

    getContentPane().add(toolbarPanel, BorderLayout.NORTH);

    statusPanel.setMinimumSize(new Dimension(1227, 45));
    statusPanel.setName("statusPanel"); // NOI18N
    statusPanel.setPreferredSize(new Dimension(1227, 48));
    getContentPane().add(statusPanel, BorderLayout.SOUTH);

    mainPanel.setMinimumSize(new Dimension(1232, 770));
    mainPanel.setName("mainPanel"); // NOI18N
    mainPanel.setPreferredSize(new Dimension(1232, 770));
    mainPanel.setLayout(new BorderLayout());

    locoDisplaySP.setDividerLocation(302);
    locoDisplaySP.setMinimumSize(new Dimension(1050, 900));
    locoDisplaySP.setName("locoDisplaySP"); // NOI18N
    locoDisplaySP.setPreferredSize(new Dimension(1050, 850));

    centerPanel.setMinimumSize(new Dimension(1002, 772));
    centerPanel.setName("centerPanel"); // NOI18N
    centerPanel.setPreferredSize(new Dimension(1002, 772));
    centerPanel.setLayout(new CardLayout());

    keyboardSensorMessagePanel.setMinimumSize(new Dimension(1002, 772));
    keyboardSensorMessagePanel.setName("keyboardSensorMessagePanel"); // NOI18N
    keyboardSensorMessagePanel.setPreferredSize(new Dimension(1002, 772));
    centerPanel.add(keyboardSensorMessagePanel, "diagnosticPanel");

    layoutPanel.setMinimumSize(new Dimension(885, 160));
    layoutPanel.setName("layoutPanel"); // NOI18N
    centerPanel.add(layoutPanel, "designPanel");
    layoutPanel.getAccessibleContext().setAccessibleName("designPanel");

    overviewPanel.setName("overviewPanel"); // NOI18N
    centerPanel.add(overviewPanel, "overviewPanel");
    overviewPanel.getAccessibleContext().setAccessibleName("overviewPanel");

    settingsPanel.setName("settingsPanel"); // NOI18N
    FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    settingsPanel.setLayout(flowLayout1);

    commandStationPanel.setName("commandStationPanel"); // NOI18N
    settingsPanel.add(commandStationPanel);

    centerPanel.add(settingsPanel, "settingsPanel");

    vncPanel.setMinimumSize(new Dimension(1002, 772));
    vncPanel.setName("vncPanel"); // NOI18N
    vncPanel.setPreferredSize(new Dimension(1002, 772));
    centerPanel.add(vncPanel, "vncPanel");

    locoDisplaySP.setRightComponent(centerPanel);

    leftPanel.setMinimumSize(new Dimension(225, 772));
    leftPanel.setName("leftPanel"); // NOI18N
    leftPanel.setPreferredSize(new Dimension(225, 772));
    leftPanel.setLayout(new BorderLayout(1, 1));

    locoSplitPane.setDividerLocation(500);
    locoSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    locoSplitPane.setToolTipText("");
    locoSplitPane.setName("locoSplitPane"); // NOI18N

    dispatcherStatusPanel.setName("dispatcherStatusPanel"); // NOI18N
    locoSplitPane.setLeftComponent(dispatcherStatusPanel);

    smallDriverCabPanel.setName("smallDriverCabPanel"); // NOI18N
    locoSplitPane.setRightComponent(smallDriverCabPanel);

    leftPanel.add(locoSplitPane, BorderLayout.CENTER);

    locoDisplaySP.setLeftComponent(leftPanel);

    mainPanel.add(locoDisplaySP, BorderLayout.CENTER);

    getContentPane().add(mainPanel, BorderLayout.CENTER);

    jcsMenuBar.setName("jcsMenuBar"); // NOI18N

    fileMenu.setText("File");
    fileMenu.setName("fileMenu"); // NOI18N

    quitMI.setText("Quit");
    quitMI.setName("quitMI"); // NOI18N
    quitMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        quitMIActionPerformed(evt);
      }
    });
    fileMenu.add(quitMI);

    connectMI.setText("Connect");
    connectMI.setName("connectMI"); // NOI18N
    connectMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        connectMIActionPerformed(evt);
      }
    });
    fileMenu.add(connectMI);

    virtualCBMI.setText("Virtual Connection");
    virtualCBMI.setName("virtualCBMI"); // NOI18N
    virtualCBMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        virtualCBMIActionPerformed(evt);
      }
    });
    fileMenu.add(virtualCBMI);

    autoPilotMI.setText("Autopilot");
    autoPilotMI.setToolTipText("Switch Autopilot on");
    autoPilotMI.setName("autoPilotMI"); // NOI18N
    autoPilotMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        autoPilotMIActionPerformed(evt);
      }
    });
    fileMenu.add(autoPilotMI);

    startAllLocsMI.setText("Start All Locomotives");
    startAllLocsMI.setToolTipText("Start All Locomotives");
    startAllLocsMI.setName("startAllLocsMI"); // NOI18N
    startAllLocsMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        startAllLocsMIActionPerformed(evt);
      }
    });
    fileMenu.add(startAllLocsMI);

    resetAutopilotMI.setText("Reset Autopilot");
    resetAutopilotMI.setToolTipText("Reset Autopilot");
    resetAutopilotMI.setName("resetAutopilotMI"); // NOI18N
    resetAutopilotMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        resetAutopilotMIActionPerformed(evt);
      }
    });
    fileMenu.add(resetAutopilotMI);

    jcsMenuBar.add(fileMenu);

    editMenu.setText("Edit");
    editMenu.setName("editMenu"); // NOI18N

    rotateTileMI.setMnemonic('R');
    rotateTileMI.setText("Rotate Tile");
    rotateTileMI.setToolTipText("Rotate the selected Tile");
    rotateTileMI.setName("rotateTileMI"); // NOI18N
    rotateTileMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        rotateTileMIActionPerformed(evt);
      }
    });
    editMenu.add(rotateTileMI);

    flipTileHorizontallyMI.setText("Flip Horizontally");
    flipTileHorizontallyMI.setToolTipText("Flip selected Tile Horizontally");
    flipTileHorizontallyMI.setName("flipTileHorizontallyMI"); // NOI18N
    flipTileHorizontallyMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        flipTileHorizontallyMIActionPerformed(evt);
      }
    });
    editMenu.add(flipTileHorizontallyMI);

    flipTileVerticallyMI.setText("Flip Tile Vertically");
    flipTileVerticallyMI.setToolTipText("Flip the selected Tile Vertically");
    flipTileVerticallyMI.setName("flipTileVerticallyMI"); // NOI18N
    flipTileVerticallyMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        flipTileVerticallyMIActionPerformed(evt);
      }
    });
    editMenu.add(flipTileVerticallyMI);

    deleteTileMI.setText("Delete Tile");
    deleteTileMI.setToolTipText("Delete the selected Tile");
    deleteTileMI.setName("deleteTileMI"); // NOI18N
    deleteTileMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        deleteTileMIActionPerformed(evt);
      }
    });
    editMenu.add(deleteTileMI);

    jcsMenuBar.add(editMenu);

    windowMenu.setText("Window");
    windowMenu.setName("windowMenu"); // NOI18N

    showHome.setMnemonic('H');
    showHome.setText("Home");
    showHome.setToolTipText("Home screen");
    showHome.setName("showHome"); // NOI18N
    showHome.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showHomeActionPerformed(evt);
      }
    });
    windowMenu.add(showHome);

    editLayout.setMnemonic('E');
    editLayout.setText("Edit Layout");
    editLayout.setToolTipText("Edit the track layout");
    editLayout.setName("editLayout"); // NOI18N
    editLayout.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        editLayoutActionPerformed(evt);
      }
    });
    windowMenu.add(editLayout);

    vncMI.setMnemonic('V');
    vncMI.setText("VNC");
    vncMI.setToolTipText("Show VNC screen");
    vncMI.setName("vncMI"); // NOI18N
    vncMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        vncMIActionPerformed(evt);
      }
    });
    windowMenu.add(vncMI);

    showKeyboard.setMnemonic('K');
    showKeyboard.setText("Keyboard");
    showKeyboard.setToolTipText("Accessory keyboard");
    showKeyboard.setName("showKeyboard"); // NOI18N
    showKeyboard.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showKeyboardActionPerformed(evt);
      }
    });
    windowMenu.add(showKeyboard);

    showSensorMonitor.setMnemonic('M');
    showSensorMonitor.setText("Sensor Monitor");
    showSensorMonitor.setName("showSensorMonitor"); // NOI18N
    showSensorMonitor.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showSensorMonitorActionPerformed(evt);
      }
    });
    windowMenu.add(showSensorMonitor);

    showRoutesMI.setText("Show Routes");
    showRoutesMI.setToolTipText("Show Routes");
    showRoutesMI.setName("showRoutesMI"); // NOI18N
    showRoutesMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showRoutesMIActionPerformed(evt);
      }
    });
    windowMenu.add(showRoutesMI);

    jcsMenuBar.add(windowMenu);

    settingsMenu.setText("Settings");
    settingsMenu.setName("settingsMenu"); // NOI18N

    showLocosMI.setToolTipText("Locomotive settings");
    showLocosMI.setLabel("Locomotives");
    showLocosMI.setName("showLocosMI"); // NOI18N
    showLocosMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showLocosMIActionPerformed(evt);
      }
    });
    settingsMenu.add(showLocosMI);

    showAccessoryMI.setText("Accessories");
    showAccessoryMI.setToolTipText("Accessory Settings");
    showAccessoryMI.setName("showAccessoryMI"); // NOI18N
    showAccessoryMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showAccessoryMIActionPerformed(evt);
      }
    });
    settingsMenu.add(showAccessoryMI);

    showCommandStationsMI.setText("Command Stations");
    showCommandStationsMI.setToolTipText("Commans Station Settings");
    showCommandStationsMI.setName("showCommandStationsMI"); // NOI18N
    showCommandStationsMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showCommandStationsMIActionPerformed(evt);
      }
    });
    settingsMenu.add(showCommandStationsMI);

    showPropertiesMI.setText("Properties");
    showPropertiesMI.setToolTipText("Property Settings");
    showPropertiesMI.setName("showPropertiesMI"); // NOI18N
    showPropertiesMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showPropertiesMIActionPerformed(evt);
      }
    });
    settingsMenu.add(showPropertiesMI);

    jcsMenuBar.add(settingsMenu);

    helpMenu.setText("Help");
    helpMenu.setName("helpMenu"); // NOI18N

    aboutMI.setText("About");
    aboutMI.setName("aboutMI"); // NOI18N
    aboutMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        aboutMIActionPerformed(evt);
      }
    });
    helpMenu.add(aboutMI);

    jcsMenuBar.add(helpMenu);

    setJMenuBar(jcsMenuBar);

    pack();
    setLocationRelativeTo(null);
  }// </editor-fold>//GEN-END:initComponents

    private void showLocosMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showLocosMIActionPerformed
      showLocomotives();
    }//GEN-LAST:event_showLocosMIActionPerformed

    private void showKeyboardBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showKeyboardBtnActionPerformed
      showKeyboards();
    }//GEN-LAST:event_showKeyboardBtnActionPerformed

    private void quitMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_quitMIActionPerformed
      dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }//GEN-LAST:event_quitMIActionPerformed

    private void formWindowClosing(WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      QuitApp();
    }//GEN-LAST:event_formWindowClosing

    private void showEditDesignBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showEditDesignBtnActionPerformed
      showEditLayoutPanel();
    }//GEN-LAST:event_showEditDesignBtnActionPerformed

    private void showOverviewBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showOverviewBtnActionPerformed
      showOverviewPanel();
    }//GEN-LAST:event_showOverviewBtnActionPerformed

    private void powerButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_powerButtonActionPerformed
      boolean on = ((JToggleButton) evt.getSource()).isSelected();
      Logger.trace("Switch Power " + (on ? "On" : "Off"));
      if (JCS.getJcsCommandStation() != null) {
        JCS.getJcsCommandStation().switchPower(on);
      }
    }//GEN-LAST:event_powerButtonActionPerformed

    private void showFeedbackMonitorBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showFeedbackMonitorBtnActionPerformed
      showSensorMonitor();
    }//GEN-LAST:event_showFeedbackMonitorBtnActionPerformed

  private boolean QuitApp() {
    int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit JCS?", "Exit JCS", JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION) {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      //Disconnect Command stations
      JCS.getJcsCommandStation().switchPower(false);
      JCS.getJcsCommandStation().disconnect();

      setVisible(false);
      dispose();

      //Force close ports
      SerialPortUtil.closeAllPorts();
      Logger.debug("Shutting down");
      //Force!
      System.exit(0);
    }
    return false;
  }

  public void connect(boolean connect) {
    boolean connected = false;
    if (JCS.getJcsCommandStation() != null) {
      if (connect) {
        String ip = JCS.getJcsCommandStation().getCommandStationBean().getIpAddress();
        String name = JCS.getJcsCommandStation().getCommandStationBean().getDescription();
        if (Ping.IsReachable(ip)) {
          if ("AWT-EventQueue-0".equals(Thread.currentThread().getName())) {
            JCS.getJcsCommandStation().connectInBackground();
          } else {
            JCS.getJcsCommandStation().connect();
          }
        } else {
          Logger.debug("Can't reach ip " + ip + "...");
          JOptionPane.showMessageDialog(this, "Can't connect to " + name + ", " + ip + " is not reachable.", "Can't Connect", JOptionPane.ERROR_MESSAGE, null);
        }

        InfoBean info = JCS.getJcsCommandStation().getCommandStationInfo();
        connected = JCS.getJcsCommandStation().isConnected();

        if (info != null && connected) {
          connectButton.setSelected(true);
          powerButton.setSelected(JCS.getJcsCommandStation().isPowerOn());
          connectMI.setText("Disconnect");
        } else {
          connectButton.setSelected(false);
          powerButton.setSelected(JCS.getJcsCommandStation().isPowerOn());
        }
      } else {
        if (JCS.getJcsCommandStation().isConnected()) {
          JCS.getJcsCommandStation().disconnect();
        }
        connectMI.setText("Connect");
      }
    }
    JCS.getJcsCommandStation().addDisconnectionEventListener(this);

    powerButton.setEnabled(connect && connected);
    showFeedbackMonitorBtn.setEnabled(connect && connected);

    showVNCBtn.setEnabled(connect && connected);
    setControllerProperties();
  }

    private void connectButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
      boolean connect = ((JToggleButton) evt.getSource()).isSelected();
      connect(connect);
    }//GEN-LAST:event_connectButtonActionPerformed

    private void showHomeActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showHomeActionPerformed
      showOverviewPanel();
    }//GEN-LAST:event_showHomeActionPerformed

    private void editLayoutActionPerformed(ActionEvent evt) {//GEN-FIRST:event_editLayoutActionPerformed
      showEditLayoutPanel();
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
      connectButton.setSelected(connect);
    }//GEN-LAST:event_connectMIActionPerformed

  private void showCommandStationsMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showCommandStationsMIActionPerformed
    showCommandStations();
  }//GEN-LAST:event_showCommandStationsMIActionPerformed

  private void aboutMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_aboutMIActionPerformed
    Logger.trace(evt.getActionCommand());
    AboutDialog dialog = new AboutDialog(this, true);
    dialog.pack();
    dialog.setLocationRelativeTo(null);

    dialog.setVisible(true);
  }//GEN-LAST:event_aboutMIActionPerformed

  private void showVNCBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showVNCBtnActionPerformed
    showVNCConsole();
  }//GEN-LAST:event_showVNCBtnActionPerformed

  private void autoPilotBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_autoPilotBtnActionPerformed
    //Logger.trace(evt.getActionCommand() + (autoPilotBtn.isSelected() ? " Enable" : " Disable") + " Auto mode");
    startAutopilot();
  }//GEN-LAST:event_autoPilotBtnActionPerformed

  private void startAutopilot() {
    if (autoPilotBtn.isSelected()) {
      startAllLocsBtn.setEnabled(true);
      dispatcherStatusPanel.showDispatcherTab();
      // startAllLocsBtn.setIcon(new ImageIcon(getClass().getResource("/media/arrowhead-right-gn.png")));
    } else {
      startAllLocsBtn.setEnabled(false);
      dispatcherStatusPanel.showLocomotiveTab();
    }

    AutoPilot.runAutoPilot(autoPilotBtn.isSelected());
  }

  private void startAllLocsBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_startAllLocsBtnActionPerformed
    startAllLocomotives();
  }//GEN-LAST:event_startAllLocsBtnActionPerformed

  private void showAccessoryMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showAccessoryMIActionPerformed
    showAccessories();
  }//GEN-LAST:event_showAccessoryMIActionPerformed

  private void showPropertiesMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showPropertiesMIActionPerformed
    showProperties();
  }//GEN-LAST:event_showPropertiesMIActionPerformed

  private void rotateTileMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rotateTileMIActionPerformed
    if (editMode) {
      layoutPanel.rotateSelectedTile();
    }
  }//GEN-LAST:event_rotateTileMIActionPerformed

  private void flipTileHorizontallyMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_flipTileHorizontallyMIActionPerformed
    if (editMode) {
      layoutPanel.flipSelectedTileHorizontal();
    }
  }//GEN-LAST:event_flipTileHorizontallyMIActionPerformed

  private void flipTileVerticallyMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_flipTileVerticallyMIActionPerformed
    if (editMode) {
      layoutPanel.flipSelectedTileVerical();
    }
  }//GEN-LAST:event_flipTileVerticallyMIActionPerformed

  private void deleteTileMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_deleteTileMIActionPerformed
    if (editMode) {
      layoutPanel.deleteSelectedTile();
    }
  }//GEN-LAST:event_deleteTileMIActionPerformed

  private void virtualCBMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_virtualCBMIActionPerformed
    Logger.trace(evt.getActionCommand() + " Switch Virtual Connection " + (virtualCBMI.isSelected() ? "On" : "Off"));
    if (JCS.getJcsCommandStation() != null) {
      JCS.getJcsCommandStation().setVirtual(virtualCBMI.isSelected());
    }
  }//GEN-LAST:event_virtualCBMIActionPerformed

  private void vncMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_vncMIActionPerformed
    showVNCConsole();
  }//GEN-LAST:event_vncMIActionPerformed

  private void autoPilotMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_autoPilotMIActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_autoPilotMIActionPerformed

  private void resetAutopilotMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_resetAutopilotMIActionPerformed
    AutoPilot.reset();
  }//GEN-LAST:event_resetAutopilotMIActionPerformed

  private void startAllLocsMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_startAllLocsMIActionPerformed
    startAllLocomotives();
  }//GEN-LAST:event_startAllLocsMIActionPerformed

  private void showRoutesMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showRoutesMIActionPerformed
    showRoutes();
  }//GEN-LAST:event_showRoutesMIActionPerformed

  private void startAllLocomotives() {
    int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to start All Locomotives?", "Start ALL Locomotives", JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION) {
      AutoPilot.startAllLocomotives();
    }
  }

  private String getTitleString() {
    String jcsVersion = VersionInfo.getVersion();

    if (JCS.getJcsCommandStation() != null && JCS.getJcsCommandStation().getCommandStationInfo() != null && JCS.getJcsCommandStation().getCommandStationInfo().getProductName() != null) {
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
  public void onConnectionChange(ConnectionEvent event) {
    if (event.isConnected()) {
      connectMI.setText("DisConnect");
      connectButton.setSelected(true);
      showVNCBtn.setEnabled(true);
    } else {
      JOptionPane.showMessageDialog(this, "CommandStation " + event.getSource() + " is disconnected.", "Disconnection error", JOptionPane.ERROR_MESSAGE);
      connectMI.setText("Connect");
      connectButton.setSelected(false);
      showVNCBtn.setEnabled(false);
    }
  }

  @Override
  public boolean handleQuitRequest() {
    return QuitApp();
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

  /**
   * MacOs Settings, Show a Tabbed Dialog where each tab is one of the setting Dialog
   */
  @Override
  public void handlePreferences() {
    if (settingsDialog == null) {
      settingsDialog = new SettingsDialog(this, true);
      settingsDialog.pack();
      settingsDialog.setLocationRelativeTo(null);
    }
    settingsDialog.setVisible(true);
  }

  public void powerChanged(PowerEvent event) {
    powerButton.setSelected(event.isPower());
  }

  public void refreshData() {
    Logger.trace("Refresh data due to settings change...");
  }

  public void refreshLocomotives() {
    this.dispatcherStatusPanel.refresh();
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JMenuItem aboutMI;
  private JToggleButton autoPilotBtn;
  private JMenuItem autoPilotMI;
  private JPanel centerPanel;
  private CommandStationPanel commandStationPanel;
  private JToggleButton connectButton;
  private JMenuItem connectMI;
  private JMenuItem deleteTileMI;
  private DispatcherStatusPanel dispatcherStatusPanel;
  private JMenuItem editLayout;
  private JMenu editMenu;
  private JMenu fileMenu;
  private Box.Filler filler1;
  private Box.Filler filler2;
  private Box.Filler filler3;
  private Box.Filler filler4;
  private Box.Filler filler5;
  private Box.Filler filler8;
  private JMenuItem flipTileHorizontallyMI;
  private JMenuItem flipTileVerticallyMI;
  private JMenu helpMenu;
  private JMenuBar jcsMenuBar;
  private JToolBar jcsToolBar;
  private KeyboardSensorPanel keyboardSensorMessagePanel;
  private LayoutPanel layoutPanel;
  private JPanel leftPanel;
  private JSplitPane locoDisplaySP;
  private JSplitPane locoSplitPane;
  private JPanel mainPanel;
  private LayoutPanel overviewPanel;
  private JToggleButton powerButton;
  private JMenuItem quitMI;
  private JMenuItem resetAutopilotMI;
  private JMenuItem rotateTileMI;
  private JMenu settingsMenu;
  private JPanel settingsPanel;
  private JMenuItem showAccessoryMI;
  private JMenuItem showCommandStationsMI;
  private JButton showEditDesignBtn;
  private JButton showFeedbackMonitorBtn;
  private JMenuItem showHome;
  private JMenuItem showKeyboard;
  private JButton showKeyboardBtn;
  private JMenuItem showLocosMI;
  private JButton showOverviewBtn;
  private JMenuItem showPropertiesMI;
  private JMenuItem showRoutesMI;
  private JMenuItem showSensorMonitor;
  private JButton showVNCBtn;
  private SmallDriverCabPanel smallDriverCabPanel;
  private JButton startAllLocsBtn;
  private JMenuItem startAllLocsMI;
  private StatusPanel statusPanel;
  private JPanel toolbarPanel;
  private JCheckBoxMenuItem virtualCBMI;
  private JMenuItem vncMI;
  private VNCPanel vncPanel;
  private JMenu windowMenu;
  // End of variables declaration//GEN-END:variables

  private class PowerAction extends AbstractAction {

    private static final long serialVersionUID = 4263882874269440066L;

    @Override
    public void actionPerformed(ActionEvent e) {
      powerButton.doClick(50);
    }
  }

  private class QuitAction extends AbstractAction {

    private static final long serialVersionUID = 106411709893099942L;

    @Override
    public void actionPerformed(ActionEvent e) {
      QuitApp();
    }
  }

  private class ShowMonitorAction extends AbstractAction {

    private static final long serialVersionUID = -3352181383049583600L;

    @Override
    public void actionPerformed(ActionEvent e) {
      showSensorMonitor();
    }
  }

  private class HomeAction extends AbstractAction {

    private static final long serialVersionUID = 6369350924548859534L;

    @Override
    public void actionPerformed(ActionEvent e) {
      showOverviewPanel();
    }
  }

  private class EditAction extends AbstractAction {

    private static final long serialVersionUID = -4725560671766567186L;

    @Override
    public void actionPerformed(ActionEvent e) {
      showEditLayoutPanel();
    }
  }

  private class SelectModeKeyAction extends AbstractAction {

    private static final long serialVersionUID = -5543240676519086334L;

    @Override
    public void actionPerformed(ActionEvent e) {
      if (editMode) {
        layoutPanel.setMode(LayoutCanvas.Mode.SELECT);
      }
    }
  }

  private class AddModeKeyAction extends AbstractAction {

    private static final long serialVersionUID = -429465825958791906L;

    @Override
    public void actionPerformed(ActionEvent e) {
      if (editMode) {
        layoutPanel.setMode(LayoutCanvas.Mode.ADD);
      }
    }
  }

  private class DeleteModeKeyAction extends AbstractAction {

    private static final long serialVersionUID = 569113006687591145L;

    @Override
    public void actionPerformed(ActionEvent e) {
      if (editMode) {
        layoutPanel.setMode(LayoutCanvas.Mode.DELETE);
      }
    }
  }

  private class RotateKeyAction extends AbstractAction {

    private static final long serialVersionUID = -292237743142583719L;

    @Override
    public void actionPerformed(ActionEvent e) {
      if (editMode) {
        layoutPanel.rotateSelectedTile();
      }
    }
  }

  private class FlipHorizontalKeyAction extends AbstractAction {

    private static final long serialVersionUID = 7657976620206362097L;

    @Override
    public void actionPerformed(ActionEvent e) {
      layoutPanel.flipSelectedTileHorizontal();
    }
  }

  private class FlipVerticalKeyAction extends AbstractAction {

    private static final long serialVersionUID = -4269202419142803636L;

    @Override
    public void actionPerformed(ActionEvent e) {
      layoutPanel.flipSelectedTileVerical();
    }
  }

}
