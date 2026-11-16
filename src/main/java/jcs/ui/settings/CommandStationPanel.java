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
package jcs.ui.settings;

import com.fazecast.jSerialComm.SerialPort;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import jcs.JCS;
import jcs.commandStation.DecoderController;
import jcs.commandStation.JCSCommandStation;
import jcs.commandStation.automation.RailController;
import jcs.commandStation.events.PowerEventListener;
import jcs.commandStation.lifecycle.ConnectionState;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.entities.Device;
import jcs.commandStation.entities.FeedbackModule;
import jcs.commandStation.entities.InfoBean;
import jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl;
import jcs.commandStation.esu.ecos.net.EcosConnectionFactory;
import jcs.commandStation.marklin.cs.MarklinCentralStationImpl;
import jcs.commandStation.marklin.cs.net.CSConnectionFactory;
import jcs.commandStation.virtual.VirtualCommandStationImpl;
import jcs.entities.CommandStationBean;
import static jcs.entities.CommandStationBean.ESU_ECOS;
import static jcs.entities.CommandStationBean.MARKLIN_CS;
import static jcs.entities.CommandStationBean.VIRTUAL;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.swing.layout.VerticalFlowLayout;
import jcs.util.Ping;
import org.tinylog.Logger;

/**
 * Command Station Panel is the Settings Panel to configure a Command Station in JCS
 *
 */
public class CommandStationPanel extends JPanel implements TreeSelectionListener {

  private static final long serialVersionUID = -6257688549267578845L;

  private ComboBoxModel<CommandStationBean> commandStationCBM;
  private ComboBoxModel<CommandStationBean> feedbackCBM;
  private ComboBoxModel<String> serialPortCBM;
  private ComboBoxModel<String> fbpSerialPortCBM;
  private CommandStationBean selectedCommandStation;
  private CommandStationBean selectedFeedbackProvider;

  private final ExecutorService executor;

  private CommandStationBean emptyCS;
  private CommandStationBean emptyFB;

  // controller is for display only; it may be borrowed from the active JCS
  // session and MUST NOT be disconnected by this panel.
  private DecoderController controller;
  // The only transport owned by the panel. Never point this at the active one.
  private DecoderController provisionalController;
  private CommandStationBean selectedCatalogEntry;
  private volatile boolean workflowBusy;
  private boolean updatingUi;
  private String verifiedConfigurationKey;

  private final PowerEventListener powerListener = event -> SwingUtilities.invokeLater(this::refreshSafetyState);
  private final jcs.commandStation.automation.RailControllerStatusListener railListener = status -> SwingUtilities.invokeLater(this::refreshSafetyState);

  public CommandStationPanel() {
    initComponents();
    executor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "JCS-CS-PANEL");
      t.setDaemon(true);
      return t;
    });
    if (PersistenceFactory.getService() != null) {
      initModels(null);
    }
  }

  @Override
  public void addNotify() {
    super.addNotify();
    JCS.getJcsCommandStation().addPowerEventListener(powerListener);
    RailController.getInstance().addStatusListener(railListener);
    refreshSafetyState();
  }

  @Override
  public void removeNotify() {
    RailController.getInstance().removeStatusListener(railListener);
    JCS.getJcsCommandStation().removePowerEventListener(powerListener);
    super.removeNotify();
  }

  private void initModels(CommandStationBean selected) {
    updatingUi = true;
    try {
      CommandStationBean saved = selected != null ? selected : PersistenceFactory.getService().getDefaultCommandStation();
      List<CommandStationBean> commandStations = new ArrayList<>();
      List<CommandStationBean> feedbackProviders = new ArrayList<>();

      for (CommandStationBean candidate : PersistenceFactory.getService().getCommandStations()) {
        if (candidate.isDecoderControlSupport()) {
          commandStations.add(candidate);
        } else {
          feedbackProviders.add(candidate);
        }
      }

      emptyCS = new CommandStationBean();
      emptyCS.setDecoderControlSupport(true);
      emptyFB = new CommandStationBean();
      emptyFB.setFeedbackSupport(true);
      commandStations.add(emptyCS);
      feedbackProviders.add(emptyFB);

      CommandStationBean currentActive = JCS.getJcsCommandStation().getCommandStationBean();
      if (currentActive != null && JCS.getJcsCommandStation().isConnected() && JCS.getJcsCommandStation().isPowerOn()) {
        saved = currentActive;
      }
      selectedCatalogEntry = findById(commandStations, saved == null ? null : saved.getId());
      if (selectedCatalogEntry == null) {
        selectedCatalogEntry = emptyCS;
      }
      selectedCommandStation = copyBean(selectedCatalogEntry);
      commandStationCBM = new DefaultComboBoxModel<>(commandStations.toArray(CommandStationBean[]::new));
      commandStationCB.setModel(commandStationCBM);
      commandStationCB.setSelectedItem(selectedCatalogEntry);

      CommandStationBean savedFB = PersistenceFactory.getService().getEnabledFeedbackProvider();
      CommandStationBean fbEntry = findById(feedbackProviders, savedFB == null ? null : savedFB.getId());
      if (fbEntry == null) {
        fbEntry = emptyFB;
      }
      selectedFeedbackProvider = copyBean(fbEntry);
      feedbackCBM = new DefaultComboBoxModel<>(feedbackProviders.toArray(CommandStationBean[]::new));
      feedbackCB.setModel(feedbackCBM);
      feedbackCB.setSelectedItem(fbEntry);

      SerialPort[] ports = SerialPort.getCommPorts();
      String[] names = new String[ports.length];
      for (int i = 0; i < ports.length; i++) {
        names[i] = ports[i].getSystemPortName();
      }
      serialPortCBM = new DefaultComboBoxModel<>(names);
      fbpSerialPortCBM = new DefaultComboBoxModel<>(names);
      serialCB.setModel(serialPortCBM);
      fbpSerialCB.setModel(fbpSerialPortCBM);

      serialCB.setSelectedItem(selectedCommandStation.getSerialPort());
      fbpSerialCB.setSelectedItem(selectedFeedbackProvider.getSerialPort());
      controller = activeControllerForSelection();
    } finally {
      updatingUi = false;
    }
    setComponents();
  }

  private static CommandStationBean findById(List<CommandStationBean> beans, String id) {
    for (CommandStationBean bean : beans) {
      if (Objects.equals(bean.getId(), id)) {
        return bean;
      }
    }
    return null;
  }

  // Staging is mandatory: ComboBox entries may be attached to persistence or
  // the operational driver. Edits before activation must never mutate them.
  private static CommandStationBean copyBean(CommandStationBean original) {
    CommandStationBean bean = new CommandStationBean();
    bean.setId(original.getId());
    bean.setDescription(original.getDescription());
    bean.setShortName(original.getShortName());
    bean.setClassName(original.getClassName());
    bean.setConnectVia(original.getConnectVia());
    bean.setSerialPort(original.getSerialPort());
    bean.setIpAddress(original.getIpAddress());
    bean.setNetworkPort(original.getNetworkPort());
    bean.setIpAutoConfiguration(original.isIpAutoConfiguration());
    bean.setDecoderControlSupport(original.isDecoderControlSupport());
    bean.setAccessoryControlSupport(original.isAccessoryControlSupport());
    bean.setFeedbackSupport(original.isFeedbackSupport());
    bean.setLocomotiveSynchronizationSupport(original.isLocomotiveSynchronizationSupport());
    bean.setAccessorySynchronizationSupport(original.isAccessorySynchronizationSupport());
    bean.setLocomotiveImageSynchronizationSupport(original.isLocomotiveImageSynchronizationSupport());
    bean.setLocomotiveFunctionSynchronizationSupport(original.isLocomotiveFunctionSynchronizationSupport());
    bean.setProtocols(original.getProtocols());
    bean.setDefault(original.isDefault());
    bean.setEnabled(original.isEnabled());
    bean.setLastUsedSerial(original.getLastUsedSerial());
    bean.setSupConnTypesStr(original.getSupConnTypesStr());
    bean.setVirtual(original.isVirtual());
    bean.setFeedbackModuleIdentifier(original.getFeedbackModuleIdentifier());
    bean.setFeedbackChannelCount(original.getFeedbackChannelCount());
    bean.setFeedbackBus0ModuleCount(original.getFeedbackBus0ModuleCount());
    bean.setFeedbackBus1ModuleCount(original.getFeedbackBus1ModuleCount());
    bean.setFeedbackBus2ModuleCount(original.getFeedbackBus2ModuleCount());
    bean.setFeedbackBus3ModuleCount(original.getFeedbackBus3ModuleCount());
    return bean;
  }

  private static String configKey(CommandStationBean bean) {
    return bean == null ? "" : String.join("|",
            String.valueOf(bean.getId()), String.valueOf(bean.getConnectionType()),
            String.valueOf(bean.isVirtual()), String.valueOf(bean.getIpAddress()),
            String.valueOf(bean.getNetworkPort()), String.valueOf(bean.getSerialPort()));
  }

  private boolean automationBusy() {
    RailController rc = RailController.getInstance();
    String status = rc.getStatus();
    return rc.isAutoModeActive()
            || RailController.STARTED.equals(status)
            || RailController.STOPPING.equals(status)
            //|| RailController.PENDING.equals(status)
            || rc.getRunningDispatcherCount() > 0;
  }

  private boolean activePowerOn() {
    JCSCommandStation jcs = JCS.getJcsCommandStation();
    return jcs.isConnected() && jcs.isPowerOn();
  }

  private boolean configurationAllowed() {
    if (automationBusy()) {
      return false;
    }
    if (activePowerOn()) {
      return false;
    }

    ConnectionState state = JCS.getJcsCommandStation().getConnectionState();
    return state != ConnectionState.CONNECTING
            && state != ConnectionState.RECONNECTING
            && state != ConnectionState.STOPPING;
  }

  private boolean requireConfigurationAllowed() {
    if (!configurationAllowed() || workflowBusy) {
      showMessage("The command station cannot be changed while track power is ON, "
              + "Automode is active/stopping, or the active connection is changing.",
              "Configuration locked", JOptionPane.WARNING_MESSAGE);

      refreshSafetyState();
      return false;
    }
    return true;
  }

  private void showMessage(String message, String title, int messageType) {
    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, title, messageType));
  }

  private void setBusy(boolean busy) {
    workflowBusy = busy;
    setComponents();
  }

  private DecoderController activeControllerForSelection() {
    JCSCommandStation station = JCS.getJcsCommandStation();
    DecoderController active = station.getDecoderController();
    return active != null && active.isConnected()
            && selectedCommandStation != null
            && Objects.equals(active.getCommandStationBean().getId(), selectedCommandStation.getId())
            ? active : null;
  }

  private void refreshSafetyState() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(this::refreshSafetyState);
      return;
    }

    if (activePowerOn() || automationBusy()) {
      // No provisional transport remains open during active operation.
      closeProvisionalAsync();
      // Even if another draft was selected earlier, show the currently active
      // station when configuration becomes locked.
      DecoderController active = JCS.getJcsCommandStation().getDecoderController();
      if (active != null && active.isConnected()
              && (selectedCommandStation == null || !Objects.equals(selectedCommandStation.getId(), active.getCommandStationBean().getId()))) {

        CommandStationBean current = active.getCommandStationBean();
        updatingUi = true;

        try {
          for (int i = 0; i < commandStationCB.getItemCount(); i++) {
            CommandStationBean entry = commandStationCB.getItemAt(i);
            if (Objects.equals(entry.getId(), current.getId())) {
              selectedCatalogEntry = entry;
              commandStationCB.setSelectedItem(entry);
              break;
            }
          }

          selectedCommandStation = copyBean(current);
          controller = active;
        } finally {
          updatingUi = false;
        }
      }
    }
    setComponents();
  }

  private void closeProvisionalAsync() {
    DecoderController preview = provisionalController;
    provisionalController = null;
    verifiedConfigurationKey = null;
    if (preview != null) {
      if (controller == preview) {
        controller = activeControllerForSelection();
      }
      executor.execute(() -> {
        try {
          preview.disconnect();
        } catch (RuntimeException ex) {
          Logger.warn("Could not close provisional command station: {}", ex.getMessage());
        }
      });
    }
  }

  private void invalidateVerification() {
    verifiedConfigurationKey = null;
    closeProvisionalAsync();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("deprecation")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    connectionTypeBG = new ButtonGroup();
    topPanel = new JPanel();
    mainCSPanel = new JPanel();
    commandStationLbl = new JLabel();
    commandStationCB = new JComboBox<>();
    virtualCB = new JCheckBox();
    ipOrPortLbl = new JLabel();
    ipTF = new JTextField();
    serialCB = new JComboBox<>();
    controllerLbl = new JLabel();
    accessoryControllerLbl = new JLabel();
    feedbackProviderLbl = new JLabel();
    discoverBtn = new JButton();
    checkBtn = new JButton();
    networkRB = new JRadioButton();
    serialRB = new JRadioButton();
    feedbackCSPanel = new JPanel();
    feedbackLbl = new JLabel();
    feedbackCB = new JComboBox<>();
    secondfbpLbl = new JLabel();
    fbpSerialLbl = new JLabel();
    fbpSerialCB = new JComboBox<>();
    filler1 = new Box.Filler(new Dimension(0, 23), new Dimension(0, 23), new Dimension(32767, 23));
    bottomPanel = new JPanel();
    saveActivateBtn = new JButton();
    jPanel4 = new JPanel();
    connectBtn = new JButton();
    jPanel5 = new JPanel();
    jPanel2 = new JPanel();
    propertiesPanel = new JPanel();
    controllerPanel = new JPanel();
    connectedToLbl = new JLabel();
    serialLbl = new JLabel();
    swVersionLbl = new JLabel();
    hwVersionLbl = new JLabel();
    feedbackPanel = new JPanel();
    feedbackModulesPanel = new JPanel();
    mainLbl = new JLabel();
    mainSpinner = new JSpinner();
    bus1Lbl = new JLabel();
    bus1Spinner = new JSpinner();
    bus2Lbl = new JLabel();
    bus2Spinner = new JSpinner();
    bus3Lbl = new JLabel();
    bus3Spinner = new JSpinner();
    updatePanel = new JPanel();
    updateBtn = new JButton();
    rightPanel = new JPanel();
    devicesPanel = new JPanel();
    devicesSP = new JScrollPane();
    devicesTree = new JTree();

    setMinimumSize(new Dimension(1080, 600));
    setName("Form"); // NOI18N
    setPreferredSize(new Dimension(1200, 600));
    addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent evt) {
        formComponentHidden(evt);
      }
      public void componentShown(ComponentEvent evt) {
        formComponentShown(evt);
      }
    });
    setLayout(new BorderLayout());

    topPanel.setName("topPanel"); // NOI18N
    VerticalFlowLayout verticalFlowLayout2 = new VerticalFlowLayout();
    verticalFlowLayout2.sethAlignment(0);
    topPanel.setLayout(verticalFlowLayout2);

    mainCSPanel.setName("mainCSPanel"); // NOI18N
    mainCSPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

    commandStationLbl.setText("Command Station");
    commandStationLbl.setName("commandStationLbl"); // NOI18N
    commandStationLbl.setPreferredSize(new Dimension(110, 17));
    mainCSPanel.add(commandStationLbl);

    commandStationCB.setName("commandStationCB"); // NOI18N
    commandStationCB.setPreferredSize(new Dimension(200, 23));
    commandStationCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        commandStationCBActionPerformed(evt);
      }
    });
    mainCSPanel.add(commandStationCB);

    virtualCB.setText("Virtual");
    virtualCB.setToolTipText("Use Virtual Connection");
    virtualCB.setName("virtualCB"); // NOI18N
    virtualCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        virtualCBActionPerformed(evt);
      }
    });
    mainCSPanel.add(virtualCB);

    ipOrPortLbl.setHorizontalAlignment(SwingConstants.RIGHT);
    ipOrPortLbl.setText("ip Address:");
    ipOrPortLbl.setName("ipOrPortLbl"); // NOI18N
    ipOrPortLbl.setPreferredSize(new Dimension(105, 17));
    mainCSPanel.add(ipOrPortLbl);

    ipTF.setName("ipTF"); // NOI18N
    ipTF.setPreferredSize(new Dimension(120, 23));
    ipTF.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        ipTFFocusLost(evt);
      }
    });
    ipTF.addMouseListener(new MouseAdapter() {
      public void mouseExited(MouseEvent evt) {
        ipTFMouseExited(evt);
      }
    });
    ipTF.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ipTFActionPerformed(evt);
      }
    });
    mainCSPanel.add(ipTF);

    serialCB.setName("serialCB"); // NOI18N
    serialCB.setPreferredSize(new Dimension(120, 23));
    serialCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        serialCBActionPerformed(evt);
      }
    });
    mainCSPanel.add(serialCB);

    controllerLbl.setIcon(new ImageIcon(getClass().getResource("/media/controller-console-24.png"))); // NOI18N
    controllerLbl.setToolTipText("Decoder Controller");
    controllerLbl.setName("controllerLbl"); // NOI18N
    mainCSPanel.add(controllerLbl);

    accessoryControllerLbl.setIcon(new ImageIcon(getClass().getResource("/media/branch-24.png"))); // NOI18N
    accessoryControllerLbl.setToolTipText("Accessory Controller");
    accessoryControllerLbl.setName("accessoryControllerLbl"); // NOI18N
    mainCSPanel.add(accessoryControllerLbl);

    feedbackProviderLbl.setIcon(new ImageIcon(getClass().getResource("/media/chat-24.png"))); // NOI18N
    feedbackProviderLbl.setToolTipText("Feedback Provider");
    feedbackProviderLbl.setName("feedbackProviderLbl"); // NOI18N
    mainCSPanel.add(feedbackProviderLbl);

    discoverBtn.setIcon(new ImageIcon(getClass().getResource("/media/discover-24.png"))); // NOI18N
    discoverBtn.setText("Discover");
    discoverBtn.setToolTipText("Discover Command Station");
    discoverBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
    discoverBtn.setMargin(new Insets(2, 7, 2, 7));
    discoverBtn.setName("discoverBtn"); // NOI18N
    discoverBtn.setPreferredSize(new Dimension(100, 30));
    discoverBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        discoverBtnActionPerformed(evt);
      }
    });
    mainCSPanel.add(discoverBtn);

    checkBtn.setIcon(new ImageIcon(getClass().getResource("/media/check-24-g.png"))); // NOI18N
    checkBtn.setText("Check");
    checkBtn.setToolTipText("Check Connection");
    checkBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
    checkBtn.setMargin(new Insets(2, 7, 2, 7));
    checkBtn.setName("checkBtn"); // NOI18N
    checkBtn.setPreferredSize(new Dimension(100, 30));
    checkBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        checkBtnActionPerformed(evt);
      }
    });
    mainCSPanel.add(checkBtn);

    networkRB.setText("Network");
    networkRB.setName("networkRB"); // NOI18N
    networkRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        networkRBActionPerformed(evt);
      }
    });
    mainCSPanel.add(networkRB);

    serialRB.setText("Serialport");
    serialRB.setName("serialRB"); // NOI18N
    serialRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        serialRBActionPerformed(evt);
      }
    });
    mainCSPanel.add(serialRB);

    topPanel.add(mainCSPanel);

    feedbackCSPanel.setName("feedbackCSPanel"); // NOI18N
    FlowLayout flowLayout14 = new FlowLayout(FlowLayout.LEFT);
    flowLayout14.setAlignOnBaseline(true);
    feedbackCSPanel.setLayout(flowLayout14);

    feedbackLbl.setText("Feedback Device");
    feedbackLbl.setName("feedbackLbl"); // NOI18N
    feedbackLbl.setPreferredSize(new Dimension(110, 17));
    feedbackCSPanel.add(feedbackLbl);

    feedbackCB.setName("feedbackCB"); // NOI18N
    feedbackCB.setPreferredSize(new Dimension(200, 23));
    feedbackCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        feedbackCBActionPerformed(evt);
      }
    });
    feedbackCSPanel.add(feedbackCB);

    secondfbpLbl.setIcon(new ImageIcon(getClass().getResource("/media/chat-24.png"))); // NOI18N
    secondfbpLbl.setToolTipText("Feedback Provicer");
    secondfbpLbl.setName("secondfbpLbl"); // NOI18N
    secondfbpLbl.setPreferredSize(new Dimension(25, 24));
    feedbackCSPanel.add(secondfbpLbl);

    fbpSerialLbl.setHorizontalAlignment(SwingConstants.RIGHT);
    fbpSerialLbl.setText("Serial Port:");
    fbpSerialLbl.setHorizontalTextPosition(SwingConstants.LEADING);
    fbpSerialLbl.setName("fbpSerialLbl"); // NOI18N
    fbpSerialLbl.setPreferredSize(new Dimension(75, 17));
    feedbackCSPanel.add(fbpSerialLbl);

    fbpSerialCB.setName("fbpSerialCB"); // NOI18N
    fbpSerialCB.setPreferredSize(new Dimension(120, 23));
    fbpSerialCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        fbpSerialCBActionPerformed(evt);
      }
    });
    feedbackCSPanel.add(fbpSerialCB);

    filler1.setName("filler1"); // NOI18N
    feedbackCSPanel.add(filler1);

    topPanel.add(feedbackCSPanel);

    add(topPanel, BorderLayout.PAGE_START);

    bottomPanel.setName("bottomPanel"); // NOI18N
    FlowLayout flowLayout1 = new FlowLayout(FlowLayout.RIGHT);
    flowLayout1.setAlignOnBaseline(true);
    bottomPanel.setLayout(flowLayout1);

    saveActivateBtn.setIcon(new ImageIcon(getClass().getResource("/media/save-24.png"))); // NOI18N
    saveActivateBtn.setText("Activate");
    saveActivateBtn.setToolTipText("Save and Active the newly chosen Command Station");
    saveActivateBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
    saveActivateBtn.setMargin(new Insets(2, 7, 2, 7));
    saveActivateBtn.setName("saveActivateBtn"); // NOI18N
    saveActivateBtn.setPreferredSize(new Dimension(100, 30));
    saveActivateBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveActivateBtnActionPerformed(evt);
      }
    });
    bottomPanel.add(saveActivateBtn);

    add(bottomPanel, BorderLayout.PAGE_END);

    jPanel4.setName("jPanel4"); // NOI18N

    connectBtn.setIcon(new ImageIcon(getClass().getResource("/media/connecting.png"))); // NOI18N
    connectBtn.setText("Connect");
    connectBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
    connectBtn.setMargin(new Insets(2, 7, 2, 7));
    connectBtn.setName("connectBtn"); // NOI18N
    connectBtn.setPreferredSize(new Dimension(100, 30));
    connectBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        connectBtnActionPerformed(evt);
      }
    });
    jPanel4.add(connectBtn);

    add(jPanel4, BorderLayout.LINE_END);

    jPanel5.setName("jPanel5"); // NOI18N
    jPanel5.setPreferredSize(new Dimension(100, 400));
    jPanel5.setLayout(new BorderLayout());
    add(jPanel5, BorderLayout.LINE_START);

    jPanel2.setName("jPanel2"); // NOI18N
    jPanel2.setLayout(new BorderLayout());

    propertiesPanel.setName("propertiesPanel"); // NOI18N
    propertiesPanel.setLayout(new BorderLayout());

    controllerPanel.setName("controllerPanel"); // NOI18N
    controllerPanel.setPreferredSize(new Dimension(695, 40));
    FlowLayout flowLayout15 = new FlowLayout(FlowLayout.LEFT);
    flowLayout15.setAlignOnBaseline(true);
    controllerPanel.setLayout(flowLayout15);

    connectedToLbl.setText("Connected to: command station");
    connectedToLbl.setName("connectedToLbl"); // NOI18N
    connectedToLbl.setPreferredSize(new Dimension(200, 17));
    controllerPanel.add(connectedToLbl);

    serialLbl.setText("Serial: xxxxxx");
    serialLbl.setName("serialLbl"); // NOI18N
    serialLbl.setPreferredSize(new Dimension(150, 17));
    controllerPanel.add(serialLbl);

    swVersionLbl.setText("Software version: xxxxxxx");
    swVersionLbl.setName("swVersionLbl"); // NOI18N
    swVersionLbl.setPreferredSize(new Dimension(160, 17));
    controllerPanel.add(swVersionLbl);

    hwVersionLbl.setText("Hardware version: xxxxxx");
    hwVersionLbl.setName("hwVersionLbl"); // NOI18N
    hwVersionLbl.setPreferredSize(new Dimension(160, 17));
    controllerPanel.add(hwVersionLbl);

    propertiesPanel.add(controllerPanel, BorderLayout.NORTH);

    feedbackPanel.setBorder(BorderFactory.createTitledBorder("Feedback Modules"));
    feedbackPanel.setName("feedbackPanel"); // NOI18N
    feedbackPanel.setPreferredSize(new Dimension(654, 60));
    feedbackPanel.setLayout(new GridLayout(1, 2));

    feedbackModulesPanel.setName("feedbackModulesPanel"); // NOI18N
    FlowLayout flowLayout2 = new FlowLayout(FlowLayout.LEFT);
    flowLayout2.setAlignOnBaseline(true);
    feedbackModulesPanel.setLayout(flowLayout2);

    mainLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    mainLbl.setText("Main");
    mainLbl.setName("mainLbl"); // NOI18N
    mainLbl.setPreferredSize(new Dimension(40, 17));
    feedbackModulesPanel.add(mainLbl);

    mainSpinner.setModel(new SpinnerNumberModel(0, null, 31, 1));
    mainSpinner.setName("mainSpinner"); // NOI18N
    feedbackModulesPanel.add(mainSpinner);

    bus1Lbl.setHorizontalAlignment(SwingConstants.TRAILING);
    bus1Lbl.setText("Bus 1");
    bus1Lbl.setName("bus1Lbl"); // NOI18N
    bus1Lbl.setPreferredSize(new Dimension(40, 17));
    feedbackModulesPanel.add(bus1Lbl);

    bus1Spinner.setModel(new SpinnerNumberModel(0, null, 31, 1));
    bus1Spinner.setName("bus1Spinner"); // NOI18N
    feedbackModulesPanel.add(bus1Spinner);

    bus2Lbl.setHorizontalAlignment(SwingConstants.TRAILING);
    bus2Lbl.setText("Bus 2");
    bus2Lbl.setName("bus2Lbl"); // NOI18N
    bus2Lbl.setPreferredSize(new Dimension(40, 17));
    feedbackModulesPanel.add(bus2Lbl);

    bus2Spinner.setModel(new SpinnerNumberModel(0, null, 31, 1));
    bus2Spinner.setName("bus2Spinner"); // NOI18N
    feedbackModulesPanel.add(bus2Spinner);

    bus3Lbl.setHorizontalAlignment(SwingConstants.TRAILING);
    bus3Lbl.setText("Bus 3");
    bus3Lbl.setName("bus3Lbl"); // NOI18N
    bus3Lbl.setPreferredSize(new Dimension(40, 17));
    feedbackModulesPanel.add(bus3Lbl);

    bus3Spinner.setModel(new SpinnerNumberModel(0, null, 31, 1));
    bus3Spinner.setName("bus3Spinner"); // NOI18N
    feedbackModulesPanel.add(bus3Spinner);

    feedbackPanel.add(feedbackModulesPanel);

    updatePanel.setName("updatePanel"); // NOI18N
    updatePanel.setPreferredSize(new Dimension(350, 57));
    FlowLayout flowLayout3 = new FlowLayout(FlowLayout.RIGHT, 5, 0);
    flowLayout3.setAlignOnBaseline(true);
    updatePanel.setLayout(flowLayout3);

    updateBtn.setIcon(new ImageIcon(getClass().getResource("/media/sync-black-24.png"))); // NOI18N
    updateBtn.setText("Update");
    updateBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
    updateBtn.setMargin(new Insets(2, 7, 2, 7));
    updateBtn.setName("updateBtn"); // NOI18N
    updateBtn.setPreferredSize(new Dimension(100, 30));
    updateBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        updateBtnActionPerformed(evt);
      }
    });
    updatePanel.add(updateBtn);

    feedbackPanel.add(updatePanel);

    propertiesPanel.add(feedbackPanel, BorderLayout.SOUTH);

    jPanel2.add(propertiesPanel, BorderLayout.PAGE_START);

    rightPanel.setName("rightPanel"); // NOI18N
    rightPanel.setPreferredSize(new Dimension(750, 318));

    GroupLayout rightPanelLayout = new GroupLayout(rightPanel);
    rightPanel.setLayout(rightPanelLayout);
    rightPanelLayout.setHorizontalGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGap(0, 750, Short.MAX_VALUE)
    );
    rightPanelLayout.setVerticalGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGap(0, 371, Short.MAX_VALUE)
    );

    jPanel2.add(rightPanel, BorderLayout.EAST);

    devicesPanel.setBorder(BorderFactory.createTitledBorder("Devices"));
    devicesPanel.setName("devicesPanel"); // NOI18N
    devicesPanel.setLayout(new GridLayout(1, 1));

    devicesSP.setName("devicesSP"); // NOI18N

    DefaultMutableTreeNode treeNode1 = new DefaultMutableTreeNode("Properties");
    DefaultMutableTreeNode treeNode2 = new DefaultMutableTreeNode("booster");
    DefaultMutableTreeNode treeNode3 = new DefaultMutableTreeNode("b1");
    treeNode2.add(treeNode3);
    treeNode1.add(treeNode2);
    treeNode2 = new DefaultMutableTreeNode("Feedback Modules");
    treeNode3 = new DefaultMutableTreeNode("Module 1");
    treeNode2.add(treeNode3);
    treeNode3 = new DefaultMutableTreeNode("Module 2");
    treeNode2.add(treeNode3);
    treeNode1.add(treeNode2);
    devicesTree.setModel(new DefaultTreeModel(treeNode1));
    devicesTree.setName("devicesTree"); // NOI18N
    devicesSP.setViewportView(devicesTree);

    devicesPanel.add(devicesSP);

    jPanel2.add(devicesPanel, BorderLayout.CENTER);

    add(jPanel2, BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void setComponents() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(this::setComponents);
      return;
    }
    if (selectedCommandStation == null) {
      return;
    }

    DecoderController active = activeControllerForSelection();
    if (provisionalController != null && provisionalController.isConnected()) {
      controller = provisionalController;
    } else {
      controller = active;
    }

    boolean editable = configurationAllowed() && !workflowBusy;
    boolean hasId = selectedCommandStation.getId() != null;
    boolean isNetwork = CommandStationBean.ConnectionType.NETWORK == selectedCommandStation.getConnectionType();
    boolean isSerial = CommandStationBean.ConnectionType.SERIAL == selectedCommandStation.getConnectionType();
    boolean borrowedActive = controller != null && controller == active;
    boolean hasPreview = controller != null && controller == provisionalController;

    updatingUi = true;
    try {
      commandStationCB.setEnabled(editable);
      virtualCB.setSelected(selectedCommandStation.isVirtual());
      virtualCB.setEnabled(editable && hasId && !VIRTUAL.equals(selectedCommandStation.getId()));
      controllerLbl.setVisible(selectedCommandStation.isDecoderControlSupport());
      accessoryControllerLbl.setVisible(selectedCommandStation.isAccessoryControlSupport());
      feedbackProviderLbl.setVisible(selectedCommandStation.isFeedbackSupport());

      discoverBtn.setVisible(selectedCommandStation.isIpAutoConfiguration() && isNetwork);
      discoverBtn.setEnabled(editable && hasId);
      ipTF.setVisible(isNetwork);
      ipTF.setText(selectedCommandStation.getIpAddress());
      ipTF.setEditable(editable && hasId);
      ipTF.setEnabled(true); // Readable even when activation is locked.
      serialCB.setVisible(isSerial);
      serialCB.setSelectedItem(selectedCommandStation.getSerialPort());
      serialCB.setEnabled(editable && hasId);

      ipOrPortLbl.setText(isSerial ? "Serial Port:" : "ip Address:");
      boolean dualConnection = selectedCommandStation.getSupportedConnectionTypes().size() > 1;
      networkRB.setVisible(dualConnection);
      serialRB.setVisible(dualConnection);
      networkRB.setSelected(isNetwork);
      serialRB.setSelected(isSerial);
      networkRB.setEnabled(editable && hasId);
      serialRB.setEnabled(editable && hasId);

      boolean hasHost = selectedCommandStation.getIpAddress() != null && !selectedCommandStation.getIpAddress().isBlank();
      checkBtn.setVisible(isNetwork && hasId);
      checkBtn.setEnabled(editable && hasHost);
      ipTF.setBackground(java.awt.Color.WHITE);

      boolean externalFeedback = !selectedCommandStation.isFeedbackSupport();
      feedbackCB.setVisible(externalFeedback);
      feedbackLbl.setVisible(externalFeedback);
      feedbackCB.setEnabled(editable && externalFeedback);
      secondfbpLbl.setVisible(externalFeedback && selectedFeedbackProvider != null
              && selectedFeedbackProvider.getId() != null);
      fbpSerialCB.setVisible(externalFeedback);
      fbpSerialLbl.setVisible(externalFeedback);
      fbpSerialCB.setSelectedItem(selectedFeedbackProvider == null ? null
              : selectedFeedbackProvider.getSerialPort());
      fbpSerialCB.setEnabled(editable && externalFeedback && selectedFeedbackProvider != null);

      if (borrowedActive) {
        connectBtn.setText("Active");
        connectBtn.setEnabled(false);
        connectBtn.setToolTipText("Viewing the operational connection; it cannot be disconnected here");
      } else if (hasPreview) {
        connectBtn.setText("Disconnect");
        connectBtn.setEnabled(editable);
        connectBtn.setToolTipText("Close this temporary inspection connection");
      } else {
        connectBtn.setText("Connect");
        connectBtn.setEnabled(editable && hasId && (selectedCommandStation.isVirtual() || (isNetwork && hasHost)
                || (isSerial && selectedCommandStation.getSerialPort() != null)));
        connectBtn.setToolTipText("Temporarily connect to inspect properties; does not activate!");
      }

      boolean validated = VIRTUAL.equals(selectedCommandStation.getId())
              || selectedCommandStation.isVirtual()
              || (hasPreview && Objects.equals(verifiedConfigurationKey, configKey(selectedCommandStation)))
              || (borrowedActive && Objects.equals(configKey(selectedCommandStation), configKey(active.getCommandStationBean())))
              || (borrowedActive && selectedCommandStation.getId() != null);

      saveActivateBtn.setEnabled(editable && hasId && validated);
      saveActivateBtn.setToolTipText(validated ? "Save as default and activate; track power will remain OFF" : "Connect and verify the selected station before activating");

      boolean connected = controller != null && controller.isConnected();
      connectedToLbl.setVisible(connected);
      serialLbl.setVisible(connected);
      swVersionLbl.setVisible(connected);
      hwVersionLbl.setVisible(connected);

      if (connected) {
        InfoBean info = controller.getCommandStationInfo();
        if (info != null) {
          connectedToLbl.setText("Connected to: " + info.getProductName() + (borrowedActive ? " (active)" : " (temporary)"));
          serialLbl.setText("Serial: " + info.getSerialNumber());
          swVersionLbl.setVisible(info.getSoftwareVersion() != null);
          swVersionLbl.setText("Software version: " + info.getSoftwareVersion());
          hwVersionLbl.setVisible(info.getHardwareVersion() != null);
          hwVersionLbl.setText("Hardware version: " + info.getHardwareVersion());
        }
      }

      // Feedback sync is allowed for the ACTIVE station even with power ON.
      boolean canSync = connected && controller instanceof FeedbackController && !automationBusy();
      updateBtn.setVisible(canSync);
      updateBtn.setEnabled(canSync && !workflowBusy);
      mainLbl.setVisible(canSync);
      mainSpinner.setVisible(canSync);
      boolean marklin = MARKLIN_CS.equals(selectedCommandStation.getId());
      bus1Lbl.setVisible(canSync && marklin);
      bus1Spinner.setVisible(canSync && marklin);
      bus2Lbl.setVisible(canSync && marklin);
      bus2Spinner.setVisible(canSync && marklin);
      bus3Lbl.setVisible(canSync && marklin);
      bus3Spinner.setVisible(canSync && marklin);
      boolean spinnerEditable = canSync && editable
              && !MARKLIN_CS.equals(selectedCommandStation.getId())
              && !ESU_ECOS.equals(selectedCommandStation.getId());
      mainSpinner.setEnabled(spinnerEditable);
      bus1Spinner.setEnabled(spinnerEditable);
      bus2Spinner.setEnabled(spinnerEditable);
      bus3Spinner.setEnabled(spinnerEditable);

      if (canSync) {
        List<FeedbackModule> modules = ((FeedbackController) controller).getFeedbackModules();
        if (modules != null) {
          for (FeedbackModule module : modules) {
            int bus = module.getBusNumber() == null ? 0 : module.getBusNumber();
            int count = module.getBusSize() == null ? 0 : module.getBusSize();
            switch (bus) {
              case 1 ->
                bus1Spinner.setValue(count);
              case 2 ->
                bus2Spinner.setValue(count);
              case 3 ->
                bus3Spinner.setValue(count);
              default ->
                mainSpinner.setValue(count > 0 ? count : modules.size());
            }
          }
        }
      }
    } finally {
      updatingUi = false;
    }
    buildTree();
  }

  private void buildTree() {
    Logger.trace("building properties tree");
    String rootDesc;
    if (selectedCommandStation != null) {
      rootDesc = selectedCommandStation.getDescription();
    } else {
      rootDesc = "";
    }

    DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootDesc);
    createNodes(root);

    DefaultTreeModel model = new DefaultTreeModel(root);

    devicesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    devicesTree.addTreeSelectionListener(this);

    devicesTree.setModel(model);
  }

  private void createNodes(DefaultMutableTreeNode root) {
    Logger.trace("Creating feedback nodes");
    if (controller == null) {
      return;
    }

    List<Device> devices = controller.getDevices();

    for (Device d : devices) {
      DefaultMutableTreeNode deviceNode = new DefaultMutableTreeNode(d.getId() + " " + d.getName());

      if (d.isFeedback() && controller instanceof FeedbackController fc) {
        List<FeedbackModule> modules = fc.getFeedbackModules();
        if (modules == null) {
          continue;
        }
        modules = new ArrayList<>(modules);
        Collections.sort(modules);

        //Marklin show per bus
        for (FeedbackModule fm : modules) {
          StringBuilder sb = new StringBuilder();
          sb.append("id: ");
          sb.append(fm.getId());
          if (fm.getIdentifier() != null) {
            sb.append(" node: ");
            sb.append(fm.getIdentifier());
          }
          if (fm.getBusNumber() != null) {
            sb.append(" bus: ");
            sb.append(fm.getBusNumber());
          }
          sb.append(" module: ");
          sb.append(fm.getModuleNumber());

          DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode(sb);
          Logger.trace("M " + sb.toString());

          deviceNode.add(moduleNode);
        }
      }

      root.add(deviceNode);
    }
  }

  @Override
  public void valueChanged(TreeSelectionEvent e) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) devicesTree.getLastSelectedPathComponent();
    //TODO !
    if (node == null) {
      return;
    }

    Object nodeInfo = node.getUserObject();

    if (node.isLeaf()) {

    } else {
    }
  }

  private void applyIpEdit() {
    if (updatingUi || !configurationAllowed() || workflowBusy
            || selectedCommandStation == null) {
      return;
    }
    String address = ipTF.getText().trim();
    if (!Objects.equals(address, selectedCommandStation.getIpAddress())) {
      selectedCommandStation.setIpAddress(address);
      invalidateVerification();
      setComponents();
    }
  }

  private void saveFeedbackSelection(CommandStationBean choice) {
    CommandStationBean previous = PersistenceFactory.getService().getEnabledFeedbackProvider();
    if (previous != null && (choice == null || !Objects.equals(previous.getId(), choice.getId()))) {
      previous.setEnabled(false);
      PersistenceFactory.getService().persist(previous);
    }
    if (choice != null && choice.getId() != null) {
      choice.setEnabled(true);
      PersistenceFactory.getService().persist(choice);
    }
  }

  private void saveAndActivate() {
    if (selectedCommandStation == null || selectedCommandStation.getId() == null) {
      showMessage("Select a command station first.", "Activation",
              JOptionPane.WARNING_MESSAGE);
      return;
    }

    DecoderController active = activeControllerForSelection();
    
    boolean sameActive = active != null;
    boolean verified = selectedCommandStation.isVirtual()
            || VIRTUAL.equals(selectedCommandStation.getId())
            || (provisionalController != null && provisionalController.isConnected()
            && Objects.equals(verifiedConfigurationKey, configKey(selectedCommandStation)))
            || sameActive;

    if (!verified) {
      showMessage("Connect to and verify this command station before activating it.",
              "Not verified", JOptionPane.WARNING_MESSAGE);
      return;
    }
    
    if (sameActive && !Objects.equals(configKey(selectedCommandStation),configKey(active.getCommandStationBean()))) {

      int answer = JOptionPane.showConfirmDialog(this, """
                                                       You have edited the settings of the active station. Testing another address is not possible while its transport is in use.
                                                       Save and reconnect using the new settings?""",
              "Reconnect command station", JOptionPane.YES_NO_OPTION);
      if (answer != JOptionPane.YES_OPTION) {
        return;
      }
    }

    CommandStationBean chosen = copyBean(selectedCommandStation);
    chosen.setEnabled(true);
    CommandStationBean feedback = selectedFeedbackProvider == null
            ? null : copyBean(selectedFeedbackProvider);

    setBusy(true);

    // Provisional connection must be closed BEFORE the operational session
    // is selected and started. The executor serializes these actions.
    closeProvisionalAsync();
    executor.execute(() -> {
      try {
        JCS.getJcsCommandStation().activateCommandStation(chosen);
        if (!chosen.isFeedbackSupport()) {
          saveFeedbackSelection(feedback);
        }
        SwingUtilities.invokeLater(() -> {
          initModels(chosen);
          setBusy(false);
          showMessage("Saved " + chosen.getDescription()
                  + " as the default command station. Connection startup was requested. "
                  + "Track power remains OFF until the operator enables it.",
                  "Activation requested", JOptionPane.INFORMATION_MESSAGE);
        });
      } catch (RuntimeException ex) {
        Logger.error(ex, "Command station activation failed");
        SwingUtilities.invokeLater(() -> {
          setBusy(false);
          showMessage("Activation failed: " + ex.getMessage(),
                  "Activation failed", JOptionPane.ERROR_MESSAGE);
        });
      }
    });
  }

  private void commandStationCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_commandStationCBActionPerformed
    if (updatingUi) {
      return;
    }

    if (!requireConfigurationAllowed()) {
      updatingUi = true;
      commandStationCB.setSelectedItem(selectedCatalogEntry);
      updatingUi = false;
      return;
    }

    CommandStationBean choice = (CommandStationBean) commandStationCB.getSelectedItem();
    if (choice == null) {
      return;
    }

    closeProvisionalAsync();
    selectedCatalogEntry = choice;
    selectedCommandStation = copyBean(choice);
    controller = activeControllerForSelection();
    Logger.info("Previewing command station configuration: {}", selectedCommandStation.getId());
    setComponents();
  }//GEN-LAST:event_commandStationCBActionPerformed

  private void virtualCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_virtualCBActionPerformed

    if (updatingUi || !requireConfigurationAllowed()) {
      return;
    }

    selectedCommandStation.setVirtual(virtualCB.isSelected());
    invalidateVerification();
    setComponents();
  }//GEN-LAST:event_virtualCBActionPerformed

  private void ipTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_ipTFFocusLost
    applyIpEdit();
  }//GEN-LAST:event_ipTFFocusLost

  private void ipTFMouseExited(MouseEvent evt) {//GEN-FIRST:event_ipTFMouseExited
    applyIpEdit();
  }//GEN-LAST:event_ipTFMouseExited

  private void ipTFActionPerformed(ActionEvent evt) {//GEN-FIRST:event_ipTFActionPerformed
    applyIpEdit();
  }//GEN-LAST:event_ipTFActionPerformed

  private void serialCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_serialCBActionPerformed
    if (updatingUi || !requireConfigurationAllowed()) {
      return;
    }

    selectedCommandStation.setSerialPort((String) serialCB.getSelectedItem());
    invalidateVerification();
    setComponents();
  }//GEN-LAST:event_serialCBActionPerformed

  private void discoverBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_discoverBtnActionPerformed
    if (!requireConfigurationAllowed()) {
      return;
    }

    CommandStationBean draft = copyBean(selectedCommandStation);
    setBusy(true);
    executor.execute(() -> discover(draft));
  }//GEN-LAST:event_discoverBtnActionPerformed

  private void checkBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_checkBtnActionPerformed
    if (!requireConfigurationAllowed()) {
      return;
    }

    CommandStationBean draft = copyBean(selectedCommandStation);
    setBusy(true);
    executor.execute(() -> checkConnection(draft));
  }//GEN-LAST:event_checkBtnActionPerformed

  private void networkRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_networkRBActionPerformed
    if (updatingUi || !requireConfigurationAllowed()) {
      return;
    }

    selectedCommandStation.setConnectionType(CommandStationBean.ConnectionType.NETWORK);
    invalidateVerification();
    setComponents();
  }//GEN-LAST:event_networkRBActionPerformed

  private void serialRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_serialRBActionPerformed
    if (updatingUi || !requireConfigurationAllowed()) {
      return;
    }

    selectedCommandStation.setConnectionType(CommandStationBean.ConnectionType.SERIAL);
    invalidateVerification();
    setComponents();
  }//GEN-LAST:event_serialRBActionPerformed

  private void feedbackCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_feedbackCBActionPerformed
    if (updatingUi || !requireConfigurationAllowed()) {
      return;
    }

    CommandStationBean fb = (CommandStationBean) feedbackCB.getSelectedItem();
    if (fb != null) {
      selectedFeedbackProvider = copyBean(fb);
    }
    setComponents();
  }//GEN-LAST:event_feedbackCBActionPerformed

  private void fbpSerialCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_fbpSerialCBActionPerformed
    if (updatingUi || !requireConfigurationAllowed()) {
      return;
    }

    if (selectedFeedbackProvider != null) {
      selectedFeedbackProvider.setSerialPort((String) fbpSerialCB.getSelectedItem());
    }
  }//GEN-LAST:event_fbpSerialCBActionPerformed

  private void connectBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_connectBtnActionPerformed
    if (!requireConfigurationAllowed()) {
      return;
    }

    if (provisionalController != null) {
      closeProvisionalAsync();
      setComponents();
      return;
    }

    // The active connection is borrowed exclusively for viewing; NEVER
    // acquire a second transport through its static connection factory.
    if (activeControllerForSelection() != null) {
      showMessage("The current command station is already connected. "
              + "Its properties are shown without opening a second connection.",
              "Active command station", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    CommandStationBean draft = copyBean(selectedCommandStation);
    setBusy(true);
    executor.execute(() -> connect(draft));
  }//GEN-LAST:event_connectBtnActionPerformed

  private void updateBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
    if (automationBusy() || workflowBusy || !(controller instanceof FeedbackController fc)) {
      return;
    }

    setBusy(true);
    executor.execute(() -> updateSensors(fc));
  }//GEN-LAST:event_updateBtnActionPerformed

  private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
    refreshSafetyState();
  }//GEN-LAST:event_formComponentShown

  private void formComponentHidden(ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
    closeProvisionalAsync();
  }//GEN-LAST:event_formComponentHidden

  private void saveActivateBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveActivateBtnActionPerformed
    if (!requireConfigurationAllowed()) {
      return;
    }

    saveAndActivate();
  }//GEN-LAST:event_saveActivateBtnActionPerformed

  @Override
  public void setVisible(boolean visible) {
    if (!visible) {
      // Only close the temporary inspection connection. The real controller
      // belongs to JCSCommandStation and must survive dialog closure.
      closeProvisionalAsync();
    }
    super.setVisible(visible);
    if (visible) {
      refreshSafetyState();
    }
  }

  private void updateSensors(FeedbackController feedback) {
    try {
      List<FeedbackModule> modules = feedback.getFeedbackModules();
      if (modules == null) {
        throw new IllegalStateException("No feedback modules returned by the station");
      }
      int updated = 0;
      for (FeedbackModule module : modules) {
        List<SensorBean> sensors = module.getSensors();
        if (sensors == null) {
          continue;
        }
        for (SensorBean sensor : sensors) {
          PersistenceFactory.getService().persist(sensor);
          updated++;
        }
      }
      Logger.info("Updated {} feedback sensors from the command station", updated);
    } catch (RuntimeException ex) {
      Logger.error(ex, "Error updating feedback sensors");
      showMessage("Updating feedback sensors failed: " + ex.getMessage(),
              "Sensor update", JOptionPane.ERROR_MESSAGE);
    } finally {
      SwingUtilities.invokeLater(() -> setBusy(false));
    }
  }

  private void discover(CommandStationBean draft) {
    InetAddress found = null;
    try {
      if (MARKLIN_CS.equals(draft.getId())) {
        found = CSConnectionFactory.discoverCs();
      } else if (ESU_ECOS.equals(draft.getId())) {
        found = EcosConnectionFactory.discoverEcos();
      }
      final InetAddress address = found;
      SwingUtilities.invokeLater(() -> {
        if (address != null && configurationAllowed()
                && Objects.equals(draft.getId(), selectedCommandStation.getId())) {

          selectedCommandStation.setIpAddress(address.getHostAddress());
          invalidateVerification();
          Logger.info("Discovered {} at {}", draft.getId(), address.getHostAddress());

          showMessage("Discovered " + draft.getDescription() + " at "
                  + address.getHostAddress(), "Discovery successful",
                  JOptionPane.INFORMATION_MESSAGE);
        } else if (address == null) {
          showMessage("No " + draft.getDescription()
                  + " was found on the network. Enter its address manually.",
                  "Discovery unsuccessful", JOptionPane.INFORMATION_MESSAGE);
        }
        setBusy(false);
      });
    } catch (RuntimeException ex) {
      Logger.error(ex, "Discovery failed");
      SwingUtilities.invokeLater(() -> {
        setBusy(false);
        showMessage("Discovery failed: " + ex.getMessage(),
                "Discovery error", JOptionPane.ERROR_MESSAGE);
      });
    }
  }

//  private JFrame getParentFrame() {
//    if (SwingUtilities.getRoot(this) instanceof JFrame frame) {
//      return frame;
//    }
//    return null;
//  }
  private void checkConnection(CommandStationBean draft) {
    boolean reachable;
    try {
      reachable = Ping.isReachable(draft.getIpAddress());
    } catch (RuntimeException ex) {
      Logger.warn("Ping check failed for {}: {}", draft.getIpAddress(), ex.getMessage());
      reachable = false;
    }

    final boolean pingResult = reachable;

    SwingUtilities.invokeLater(() -> {
      setBusy(false);
      if (Objects.equals(configKey(draft), configKey(selectedCommandStation))) {
        ipTF.setBackground(pingResult ? new java.awt.Color(204, 255, 204)
                : new java.awt.Color(255, 226, 195));
      }
      if (!pingResult) {
        showMessage("No ICMP ping reply from " + draft.getIpAddress()
                + ". This does not necessarily mean that the station protocol is unreachable. "
                + "You can still try Connect.", "Reachability check",
                JOptionPane.WARNING_MESSAGE);
      }
    });
  }

  private void connect(CommandStationBean draft) {
    DecoderController preview = null;
    try {
      // Ping is diagnostic only; a valid protocol connection is authoritative.
      if (!draft.isVirtual()
              && CommandStationBean.ConnectionType.NETWORK == draft.getConnectionType()
              && !Ping.isReachable(draft.getIpAddress())) {
        Logger.warn("No ping response from {}; attempting protocol connection",
                draft.getIpAddress());
      }

      if (MARKLIN_CS.equals(draft.getId())) {
        preview = new MarklinCentralStationImpl(draft);
      } else if (ESU_ECOS.equals(draft.getId())) {
        preview = new EsuEcosCommandStationImpl(draft);
      } else if (VIRTUAL.equals(draft.getId())) {
        preview = new VirtualCommandStationImpl(draft);
      } else {
        throw new UnsupportedOperationException(
                "Temporary property connection is not implemented for "
                + draft.getDescription() + ". Intellibox lifecycle migration is still pending.");
      }

      if (!preview.connect() || !preview.isConnected()) {
        throw new IllegalStateException("The station did not complete protocol initialization");
      }

      DecoderController connectedPreview = preview;
      SwingUtilities.invokeLater(() -> {
        // Recheck safety after a potentially long connection attempt.
        if (!isShowing() || !configurationAllowed()
                || !Objects.equals(configKey(draft), configKey(selectedCommandStation))) {
          executor.execute(connectedPreview::disconnect);
        } else {
          provisionalController = connectedPreview;
          verifiedConfigurationKey = configKey(draft);
          controller = connectedPreview;
          Logger.info("Provisional connection established to {}", draft.getDescription());
        }
        setBusy(false);
      });
    } catch (RuntimeException ex) {
      if (preview != null) {
        try {
          preview.disconnect();
        } catch (RuntimeException closeEx) {
          Logger.warn("Provisional disconnect failed: {}", closeEx.getMessage());
        }
      }
      Logger.warn("Provisional connection failed: {}", ex.getMessage());
      SwingUtilities.invokeLater(() -> {
        setBusy(false);
        showMessage("Could not verify " + draft.getDescription() + ": " + ex.getMessage(),
                "Connection failed", JOptionPane.ERROR_MESSAGE);
      });
    }
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  JLabel accessoryControllerLbl;
  JPanel bottomPanel;
  JLabel bus1Lbl;
  JSpinner bus1Spinner;
  JLabel bus2Lbl;
  JSpinner bus2Spinner;
  JLabel bus3Lbl;
  JSpinner bus3Spinner;
  JButton checkBtn;
  JComboBox<CommandStationBean> commandStationCB;
  JLabel commandStationLbl;
  JButton connectBtn;
  JLabel connectedToLbl;
  ButtonGroup connectionTypeBG;
  JLabel controllerLbl;
  JPanel controllerPanel;
  JPanel devicesPanel;
  JScrollPane devicesSP;
  JTree devicesTree;
  JButton discoverBtn;
  JComboBox<String> fbpSerialCB;
  JLabel fbpSerialLbl;
  JComboBox<CommandStationBean> feedbackCB;
  JPanel feedbackCSPanel;
  JLabel feedbackLbl;
  JPanel feedbackModulesPanel;
  JPanel feedbackPanel;
  JLabel feedbackProviderLbl;
  Box.Filler filler1;
  JLabel hwVersionLbl;
  JLabel ipOrPortLbl;
  JTextField ipTF;
  JPanel jPanel2;
  JPanel jPanel4;
  JPanel jPanel5;
  JPanel mainCSPanel;
  JLabel mainLbl;
  JSpinner mainSpinner;
  JRadioButton networkRB;
  JPanel propertiesPanel;
  JPanel rightPanel;
  JButton saveActivateBtn;
  JLabel secondfbpLbl;
  JComboBox<String> serialCB;
  JLabel serialLbl;
  JRadioButton serialRB;
  JLabel swVersionLbl;
  JPanel topPanel;
  JButton updateBtn;
  JPanel updatePanel;
  JCheckBox virtualCB;
  // End of variables declaration//GEN-END:variables
}
