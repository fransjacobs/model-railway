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
package jcs.ui.options;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jcs.commandStation.ControllerFactory;
import jcs.commandStation.DecoderController;
import jcs.commandStation.FeedbackController;
import jcs.entities.CommandStationBean;
import jcs.entities.CommandStationBean.ConnectionType;
import jcs.entities.CommandStationBean.Protocol;
import jcs.entities.DeviceBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.swing.layout.VerticalFlowLayout;
import jcs.util.Ping;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class CommandStationPanel extends JPanel implements PropertyChangeListener {

  private CommandStationBean selectedCommandStation;
  private ComboBoxModel<CommandStationBean> commandStationComboBoxModel;
  private ComboBoxModel<SerialPort> serialPortComboBoxModel;

  private Task task;

  public CommandStationPanel() {
    initComponents();
    if (PersistenceFactory.getService() != null) {
      initModels();
    }
  }

  private void initModels() {
    selectedCommandStation = PersistenceFactory.getService().getDefaultCommandStation();
    List<CommandStationBean> commandStations = PersistenceFactory.getService().getCommandStations();

    if (selectedCommandStation == null) {
      selectedCommandStation = new CommandStationBean();
      //Add the empty Commandstation
      commandStations.add(selectedCommandStation);
    }

    commandStationComboBoxModel = new DefaultComboBoxModel(commandStations.toArray());
    commandStationComboBoxModel.setSelectedItem(selectedCommandStation);
    commandStationComboBox.setModel(commandStationComboBoxModel);

    SerialPort comPorts[] = SerialPort.getCommPorts();
    serialPortComboBoxModel = new DefaultComboBoxModel(comPorts);
    this.serialPortCB.setModel(serialPortComboBoxModel);

    setFieldValues();
    enableFields(false);

    this.progressBar.setVisible(false);
  }

  private void setFieldValues() {
    if (selectedCommandStation != null) {
      this.networkRB.setSelected(selectedCommandStation.getConnectionType() == ConnectionType.NETWORK);
      this.serialRB.setSelected(selectedCommandStation.getConnectionType() == ConnectionType.SERIAL);

      String id = selectedCommandStation.getId();
      this.idTF.setText("Id: " + id);

      String description = selectedCommandStation.getDescription();
      this.descriptionTF.setText(description);

      String shortName = selectedCommandStation.getShortName();
      this.shortNameLbl.setText(shortName);
      this.shortNameTF.setText(shortName);

      String className = selectedCommandStation.getClassName();
      this.classNameTF.setText(className);

      String portName = selectedCommandStation.getSerialPort();
      if (portName != null) {
        try {
          SerialPort comPort = SerialPort.getCommPort(portName);
          this.serialPortComboBoxModel.setSelectedItem(comPort);
        } catch (SerialPortInvalidPortException ioe) {
          Logger.warn("Can't find com port: " + portName + "; " + ioe.getMessage());
        }
      }

      String ipAddress = selectedCommandStation.getIpAddress();
      if (ipAddress != null) {
        this.ipAddressTF.setText(this.selectedCommandStation.getIpAddress());
      }

      Integer networkPort = selectedCommandStation.getNetworkPort();
      if (networkPort != null) {
        this.portSpinner.setValue(this.selectedCommandStation.getNetworkPort());
      } else {
        this.portSpinner.setValue(0);
      }

      boolean ipAutoConfiguration = selectedCommandStation.isIpAutoConfiguration();
      this.autoConfChkBox.setSelected(ipAutoConfiguration);

      boolean commandAndControlSupport = selectedCommandStation.isDecoderControlSupport();
      this.decoderControlCB.setSelected(commandAndControlSupport);

      boolean accessorySupport = selectedCommandStation.isAccessoryControlSupport();
      this.accessorySupportCB.setSelected(accessorySupport);

      boolean feedbackSupport = selectedCommandStation.isFeedbackSupport();
      this.feedbackSupportCB.setSelected(feedbackSupport);

      boolean locomotiveSynchronizationSupport = selectedCommandStation.isLocomotiveSynchronizationSupport();
      this.locomotiveSynchSupportCB.setSelected(locomotiveSynchronizationSupport);

      boolean accessorySynchronizationSupport = selectedCommandStation.isAccessorySynchronizationSupport();
      this.accessorySynchSupportCB.setSelected(accessorySynchronizationSupport);

      boolean locomotiveImageSynchronizationSupport = selectedCommandStation.isLocomotiveImageSynchronizationSupport();
      this.locomotiveImageSynchSupportCB.setSelected(locomotiveImageSynchronizationSupport);

      boolean locomotiveFunctionSynchronizationSupport = selectedCommandStation.isLocomotiveFunctionSynchronizationSupport();
      this.locomotiveFunctionSynchSupportCB.setSelected(locomotiveFunctionSynchronizationSupport);

      Set<Protocol> protocols = selectedCommandStation.getSupportedProtocols();
      this.mmRB.setSelected(protocols.contains(Protocol.MM));
      this.mfxRB.setSelected(protocols.contains(Protocol.MFX));
      this.dccRB.setSelected(protocols.contains(Protocol.DCC));
      this.sxRB.setSelected(protocols.contains(Protocol.SX));

      boolean defaultCs = selectedCommandStation.isDefault();
      this.defaultCommandStationChkBox.setSelected(defaultCs);
      boolean enabled = selectedCommandStation.isEnabled();
      this.enabledCB.setSelected(enabled);

      String lastUsedSerial = selectedCommandStation.getLastUsedSerial();
      if (lastUsedSerial != null) {
        this.lastUsedSerialLbl.setVisible(true);
        this.lastUsedSerialLbl.setText("Serial number: " + lastUsedSerial);
      } else {
        this.lastUsedSerialLbl.setVisible(false);
      }

      Set<ConnectionType> supportedConnectionTypes = selectedCommandStation.getSupportedConnectionTypes();
      this.supConTypeNetworkRB.setSelected(supportedConnectionTypes.contains(ConnectionType.NETWORK));
      this.supConTypeSerialRB.setSelected(supportedConnectionTypes.contains(ConnectionType.SERIAL));

      if (feedbackSupport) {
        String fbid = selectedCommandStation.getFeedbackModuleIdentifier();
        if (fbid != null) {
          int node = Integer.parseInt(fbid);
          this.nodeSpinner.setValue(node);
        }

        Integer channelCount = selectedCommandStation.getFeedbackChannelCount();
        if (channelCount != null) {
          this.channelCountSpinner.setValue(channelCount);
        }
        Integer bus0Lenght = selectedCommandStation.getFeedbackBus0ModuleCount();
        if (bus0Lenght != null) {
          this.bus0Spinner.setValue(bus0Lenght);
        }

        Integer bus1Lenght = selectedCommandStation.getFeedbackBus1ModuleCount();
        if (bus1Lenght != null) {
          this.bus1Spinner.setValue(bus1Lenght);
        }

        Integer bus2Lenght = selectedCommandStation.getFeedbackBus2ModuleCount();
        if (bus2Lenght != null) {
          this.bus2Spinner.setValue(bus2Lenght);
        }

        Integer bus3Lenght = selectedCommandStation.getFeedbackBus3ModuleCount();
        if (bus3Lenght != null) {
          this.bus3Spinner.setValue(bus3Lenght);
        }

      }
    }
  }

  private void enableFields(boolean enable) {
    Set<ConnectionType> supportedConnectionTypes = selectedCommandStation.getSupportedConnectionTypes();
    if (supportedConnectionTypes.size() > 1) {
      this.networkRB.setEnabled(supportedConnectionTypes.contains(ConnectionType.NETWORK));
      this.serialRB.setEnabled(supportedConnectionTypes.contains(ConnectionType.SERIAL));
    } else {
      this.networkRB.setEnabled(false);
      this.serialRB.setEnabled(false);
    }

    if (this.networkRB.isSelected()) {
      this.serialPortCB.setVisible(false);
      this.serialPortRefreshBtn.setVisible(false);
      this.ipAddressTF.setVisible(true);

      this.connectionPropertiesLbl.setText("IP Address:");

      this.portLbl.setVisible(true);
      this.portSpinner.setVisible(true);
      this.autoConfChkBox.setVisible(true);
      this.portSpinner.setEnabled(enable);
      this.autoConfChkBox.setEnabled(enable);
    }

    if (this.serialRB.isSelected()) {
      this.serialPortCB.setVisible(true);
      this.ipAddressTF.setVisible(false);
      this.serialPortRefreshBtn.setVisible(true);

      this.connectionPropertiesLbl.setText("Serial Port:");

      this.portLbl.setVisible(false);
      this.portSpinner.setVisible(false);
      this.autoConfChkBox.setVisible(false);
    }

    this.decoderControlCB.setEnabled(enable);
    this.accessorySupportCB.setEnabled(enable);
    this.feedbackSupportCB.setEnabled(enable);

    boolean fbEnable = this.feedbackSupportCB.isSelected();
    this.nodeSpinner.setEnabled(fbEnable);

    this.channelCountSpinner.setEnabled(fbEnable);
    this.bus0Spinner.setEnabled(fbEnable);
    this.bus1Spinner.setEnabled(fbEnable);
    this.bus2Spinner.setEnabled(fbEnable);
    this.bus3Spinner.setEnabled(fbEnable);

    this.locomotiveSynchSupportCB.setEnabled(enable);
    this.accessorySynchSupportCB.setEnabled(enable);
    this.locomotiveImageSynchSupportCB.setEnabled(enable);
    this.locomotiveFunctionSynchSupportCB.setEnabled(enable);

    this.mmRB.setEnabled(enable);
    this.mfxRB.setEnabled(enable);
    this.dccRB.setEnabled(enable);
    this.sxRB.setEnabled(enable);

    this.supConTypeNetworkRB.setEnabled(enable);
    this.supConTypeSerialRB.setEnabled(enable);

    this.descriptionTF.setEnabled(enable);
    this.classNameTF.setEnabled(enable);
    this.idTF.setEnabled(enable);

    //Only when editmode is on show the extra fields
    this.nameLbl.setVisible(enable);
    this.classNameLbl.setVisible(enable);
    this.descriptionTF.setVisible(enable);
    this.classNameTF.setVisible(enable);
    this.idLbl.setVisible(enable);

    this.shortNameTFLb.setVisible(enable);
    this.shortNameTF.setEnabled(enable);
    this.shortNameTF.setVisible(enable);

    this.supConTypeNetworkRB.setVisible(enable);
    this.supConTypeSerialRB.setVisible(enable);
    this.supportedConnectionTypesLbl.setVisible(enable);

    this.idTF.setVisible(enable);
    this.newBtn.setEnabled(enable);
    this.newBtn.setVisible(enable);

    this.saveBtn.setVisible(enable);
    this.saveBtn.setEnabled(enable);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("deprecation")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    connectionTypeBG = new ButtonGroup();
    topPanel = new JPanel();
    commandStationSelectionPanel = new JPanel();
    commandStationLbl = new JLabel();
    commandStationComboBox = new JComboBox<>();
    defaultCommandStationChkBox = new JCheckBox();
    filler5 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    shortNameLbl = new JLabel();
    filler6 = new Box.Filler(new Dimension(10, 0), new Dimension(10, 0), new Dimension(10, 32767));
    csPropertiesPanel = new JPanel();
    enabledCB = new JCheckBox();
    filler4 = new Box.Filler(new Dimension(100, 0), new Dimension(100, 0), new Dimension(100, 32767));
    lastUsedSerialLbl = new JLabel();
    centerPanel = new JPanel();
    connectionPanel = new JPanel();
    connectionTypeLbl = new JLabel();
    networkRB = new JRadioButton();
    serialRB = new JRadioButton();
    filler7 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    connectionPropertiesLbl = new JLabel();
    serialPortCB = new JComboBox<>();
    serialPortRefreshBtn = new JButton();
    ipAddressTF = new JTextField();
    portLbl = new JLabel();
    portSpinner = new JSpinner();
    autoConfChkBox = new JCheckBox();
    capabilitiesPanel = new JPanel();
    connectionTestPanel = new JPanel();
    filler3 = new Box.Filler(new Dimension(26, 0), new Dimension(26, 0), new Dimension(20, 32767));
    testConnectionBtn = new JButton();
    filler2 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    connectionTestResultLbl = new JLabel();
    progressBar = new JProgressBar();
    decoderControlSupportPanel = new JPanel();
    decoderControlCB = new JCheckBox();
    accessorySupportPanel = new JPanel();
    accessorySupportCB = new JCheckBox();
    feedbackSupportPanel = new JPanel();
    feedbackSupportCB = new JCheckBox();
    FeedbackPropertiesPanel = new JPanel();
    feedbackIdLbl = new JLabel();
    nodeSpinner = new JSpinner();
    channelCountLbl = new JLabel();
    channelCountSpinner = new JSpinner();
    bus0Lbl = new JLabel();
    bus0Spinner = new JSpinner();
    bus1Lbl = new JLabel();
    bus1Spinner = new JSpinner();
    bus2Lbl = new JLabel();
    bus2Spinner = new JSpinner();
    bus3Lbl = new JLabel();
    bus3Spinner = new JSpinner();
    locomotiveSynchSupportPanel = new JPanel();
    locomotiveSynchSupportCB = new JCheckBox();
    accessorySynchSupportPanel = new JPanel();
    accessorySynchSupportCB = new JCheckBox();
    locomotiveImageSynchSupportPanel = new JPanel();
    locomotiveImageSynchSupportCB = new JCheckBox();
    locomotiveFunctionSynchSupportPanel = new JPanel();
    locomotiveFunctionSynchSupportCB = new JCheckBox();
    protocolPanel = new JPanel();
    supportedProtocolsLbl = new JLabel();
    mmRB = new JRadioButton();
    mfxRB = new JRadioButton();
    dccRB = new JRadioButton();
    sxRB = new JRadioButton();
    filler10 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    supportedConnectionTypesLbl = new JLabel();
    supConTypeNetworkRB = new JRadioButton();
    supConTypeSerialRB = new JRadioButton();
    descPanel = new JPanel();
    nameLbl = new JLabel();
    descriptionTF = new JTextField();
    filler9 = new Box.Filler(new Dimension(15, 0), new Dimension(15, 0), new Dimension(15, 32767));
    classNameLbl = new JLabel();
    classNameTF = new JTextField();
    shortNameTFLb = new JLabel();
    shortNameTF = new JTextField();
    bottomPanel = new JPanel();
    leftBottomPanel = new JPanel();
    enableEditCB = new JCheckBox();
    newBtn = new JButton();
    filler8 = new Box.Filler(new Dimension(30, 0), new Dimension(30, 0), new Dimension(30, 32767));
    idLbl = new JLabel();
    idTF = new JTextField();
    rightBottomPanel = new JPanel();
    filler1 = new Box.Filler(new Dimension(50, 0), new Dimension(200, 0), new Dimension(150, 32767));
    saveBtn = new JButton();

    setMinimumSize(new Dimension(1000, 600));
    setName("Form"); // NOI18N
    setPreferredSize(new Dimension(1000, 600));
    setLayout(new BorderLayout());

    topPanel.setMinimumSize(new Dimension(1000, 50));
    topPanel.setName("topPanel"); // NOI18N
    topPanel.setPreferredSize(new Dimension(1000, 50));
    topPanel.setRequestFocusEnabled(false);
    topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

    commandStationSelectionPanel.setMinimumSize(new Dimension(400, 33));
    commandStationSelectionPanel.setName("commandStationSelectionPanel"); // NOI18N
    commandStationSelectionPanel.setPreferredSize(new Dimension(525, 35));
    FlowLayout flowLayout2 = new FlowLayout(FlowLayout.LEFT, 5, 2);
    flowLayout2.setAlignOnBaseline(true);
    commandStationSelectionPanel.setLayout(flowLayout2);

    commandStationLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    commandStationLbl.setLabelFor(commandStationComboBox);
    commandStationLbl.setText("Command Station:");
    commandStationLbl.setMaximumSize(new Dimension(110, 17));
    commandStationLbl.setMinimumSize(new Dimension(110, 17));
    commandStationLbl.setName("commandStationLbl"); // NOI18N
    commandStationLbl.setPreferredSize(new Dimension(110, 17));
    commandStationSelectionPanel.add(commandStationLbl);

    commandStationComboBox.setName("commandStationComboBox"); // NOI18N
    commandStationComboBox.setPreferredSize(new Dimension(200, 30));
    commandStationComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        commandStationComboBoxActionPerformed(evt);
      }
    });
    commandStationSelectionPanel.add(commandStationComboBox);

    defaultCommandStationChkBox.setText("Default");
    defaultCommandStationChkBox.setHorizontalTextPosition(SwingConstants.LEADING);
    defaultCommandStationChkBox.setName("defaultCommandStationChkBox"); // NOI18N
    defaultCommandStationChkBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        defaultCommandStationChkBoxActionPerformed(evt);
      }
    });
    commandStationSelectionPanel.add(defaultCommandStationChkBox);

    filler5.setName("filler5"); // NOI18N
    commandStationSelectionPanel.add(filler5);

    shortNameLbl.setText("shortName");
    shortNameLbl.setMaximumSize(new Dimension(70, 17));
    shortNameLbl.setMinimumSize(new Dimension(70, 17));
    shortNameLbl.setName("shortNameLbl"); // NOI18N
    shortNameLbl.setPreferredSize(new Dimension(70, 17));
    commandStationSelectionPanel.add(shortNameLbl);

    filler6.setName("filler6"); // NOI18N
    commandStationSelectionPanel.add(filler6);

    topPanel.add(commandStationSelectionPanel);

    csPropertiesPanel.setName("csPropertiesPanel"); // NOI18N
    csPropertiesPanel.setPreferredSize(new Dimension(450, 33));
    FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    csPropertiesPanel.setLayout(flowLayout1);

    enabledCB.setText("Enabled");
    enabledCB.setHorizontalAlignment(SwingConstants.CENTER);
    enabledCB.setHorizontalTextPosition(SwingConstants.LEFT);
    enabledCB.setName("enabledCB"); // NOI18N
    enabledCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        enabledCBActionPerformed(evt);
      }
    });
    csPropertiesPanel.add(enabledCB);

    filler4.setName("filler4"); // NOI18N
    csPropertiesPanel.add(filler4);

    lastUsedSerialLbl.setText("serial");
    lastUsedSerialLbl.setDoubleBuffered(true);
    lastUsedSerialLbl.setName("lastUsedSerialLbl"); // NOI18N
    csPropertiesPanel.add(lastUsedSerialLbl);

    topPanel.add(csPropertiesPanel);

    add(topPanel, BorderLayout.NORTH);

    centerPanel.setMinimumSize(new Dimension(1000, 540));
    centerPanel.setName("centerPanel"); // NOI18N
    centerPanel.setPreferredSize(new Dimension(1000, 500));
    centerPanel.setLayout(new BorderLayout());

    connectionPanel.setName("connectionPanel"); // NOI18N
    connectionPanel.setPreferredSize(new Dimension(1022, 45));
    FlowLayout flowLayout3 = new FlowLayout(FlowLayout.LEFT, 5, 2);
    flowLayout3.setAlignOnBaseline(true);
    connectionPanel.setLayout(flowLayout3);

    connectionTypeLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    connectionTypeLbl.setText("Connection Type(s):");
    connectionTypeLbl.setName("connectionTypeLbl"); // NOI18N
    connectionPanel.add(connectionTypeLbl);

    connectionTypeBG.add(networkRB);
    networkRB.setText("Network");
    networkRB.setName("networkRB"); // NOI18N
    networkRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        networkRBActionPerformed(evt);
      }
    });
    connectionPanel.add(networkRB);

    connectionTypeBG.add(serialRB);
    serialRB.setText("Serial");
    serialRB.setName("serialRB"); // NOI18N
    serialRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        serialRBActionPerformed(evt);
      }
    });
    connectionPanel.add(serialRB);

    filler7.setName("filler7"); // NOI18N
    connectionPanel.add(filler7);

    connectionPropertiesLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    connectionPropertiesLbl.setText("Properties:");
    connectionPropertiesLbl.setName("connectionPropertiesLbl"); // NOI18N
    connectionPropertiesLbl.setPreferredSize(new Dimension(100, 17));
    connectionPanel.add(connectionPropertiesLbl);

    serialPortCB.setName("serialPortCB"); // NOI18N
    serialPortCB.setPreferredSize(new Dimension(150, 30));
    serialPortCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        serialPortCBActionPerformed(evt);
      }
    });
    connectionPanel.add(serialPortCB);

    serialPortRefreshBtn.setIcon(new ImageIcon(getClass().getResource("/media/sync-black-24.png"))); // NOI18N
    serialPortRefreshBtn.setToolTipText("Refresh Serial Ports");
    serialPortRefreshBtn.setMargin(new Insets(2, 2, 2, 2));
    serialPortRefreshBtn.setName("serialPortRefreshBtn"); // NOI18N
    serialPortRefreshBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        serialPortRefreshBtnActionPerformed(evt);
      }
    });
    connectionPanel.add(serialPortRefreshBtn);

    ipAddressTF.setText("0.0.0.0");
    ipAddressTF.setToolTipText("");
    ipAddressTF.setDoubleBuffered(true);
    ipAddressTF.setName("ipAddressTF"); // NOI18N
    ipAddressTF.setPreferredSize(new Dimension(150, 30));
    ipAddressTF.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        ipAddressTFFocusLost(evt);
      }
    });
    connectionPanel.add(ipAddressTF);

    portLbl.setText("Port:");
    portLbl.setName("portLbl"); // NOI18N
    connectionPanel.add(portLbl);

    portSpinner.setModel(new SpinnerNumberModel(0, 0, 65563, 1));
    portSpinner.setName("portSpinner"); // NOI18N
    portSpinner.setPreferredSize(new Dimension(100, 30));
    portSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        portSpinnerStateChanged(evt);
      }
    });
    connectionPanel.add(portSpinner);

    autoConfChkBox.setText("Auto IP Configuration");
    autoConfChkBox.setName("autoConfChkBox"); // NOI18N
    connectionPanel.add(autoConfChkBox);

    centerPanel.add(connectionPanel, BorderLayout.NORTH);

    capabilitiesPanel.setName("capabilitiesPanel"); // NOI18N
    VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
    verticalFlowLayout1.sethAlignment(0);
    verticalFlowLayout1.sethGap(15);
    capabilitiesPanel.setLayout(verticalFlowLayout1);

    connectionTestPanel.setName("connectionTestPanel"); // NOI18N
    FlowLayout flowLayout9 = new FlowLayout(FlowLayout.RIGHT);
    flowLayout9.setAlignOnBaseline(true);
    connectionTestPanel.setLayout(flowLayout9);

    filler3.setName("filler3"); // NOI18N
    connectionTestPanel.add(filler3);

    testConnectionBtn.setIcon(new ImageIcon(getClass().getResource("/media/connect-24.png"))); // NOI18N
    testConnectionBtn.setText("Test");
    testConnectionBtn.setToolTipText("Test Connection");
    testConnectionBtn.setDoubleBuffered(true);
    testConnectionBtn.setFocusable(false);
    testConnectionBtn.setHorizontalTextPosition(SwingConstants.LEADING);
    testConnectionBtn.setIconTextGap(2);
    testConnectionBtn.setMargin(new Insets(2, 2, 2, 2));
    testConnectionBtn.setMaximumSize(new Dimension(60, 40));
    testConnectionBtn.setMinimumSize(new Dimension(60, 40));
    testConnectionBtn.setName("testConnectionBtn"); // NOI18N
    testConnectionBtn.setPreferredSize(new Dimension(60, 40));
    testConnectionBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        testConnectionBtnActionPerformed(evt);
      }
    });
    connectionTestPanel.add(testConnectionBtn);

    filler2.setName("filler2"); // NOI18N
    connectionTestPanel.add(filler2);

    connectionTestResultLbl.setText("Not Connected");
    connectionTestResultLbl.setName("connectionTestResultLbl"); // NOI18N
    connectionTestResultLbl.setPreferredSize(new Dimension(222, 17));
    connectionTestPanel.add(connectionTestResultLbl);
    connectionTestResultLbl.getAccessibleContext().setAccessibleName("");

    progressBar.setName("progressBar"); // NOI18N
    progressBar.setPreferredSize(new Dimension(200, 4));
    connectionTestPanel.add(progressBar);

    capabilitiesPanel.add(connectionTestPanel);

    decoderControlSupportPanel.setName("decoderControlSupportPanel"); // NOI18N
    FlowLayout flowLayout4 = new FlowLayout(FlowLayout.LEFT);
    flowLayout4.setAlignOnBaseline(true);
    decoderControlSupportPanel.setLayout(flowLayout4);

    decoderControlCB.setText("Decoder Control Support");
    decoderControlCB.setName("decoderControlCB"); // NOI18N
    decoderControlCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        decoderControlCBActionPerformed(evt);
      }
    });
    decoderControlSupportPanel.add(decoderControlCB);

    capabilitiesPanel.add(decoderControlSupportPanel);

    accessorySupportPanel.setName("accessorySupportPanel"); // NOI18N
    FlowLayout flowLayout12 = new FlowLayout(FlowLayout.LEFT);
    flowLayout12.setAlignOnBaseline(true);
    accessorySupportPanel.setLayout(flowLayout12);

    accessorySupportCB.setText("Accessory Support");
    accessorySupportCB.setName("accessorySupportCB"); // NOI18N
    accessorySupportCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        accessorySupportCBActionPerformed(evt);
      }
    });
    accessorySupportPanel.add(accessorySupportCB);

    capabilitiesPanel.add(accessorySupportPanel);

    feedbackSupportPanel.setName("feedbackSupportPanel"); // NOI18N
    FlowLayout flowLayout5 = new FlowLayout(FlowLayout.LEFT);
    flowLayout5.setAlignOnBaseline(true);
    feedbackSupportPanel.setLayout(flowLayout5);

    feedbackSupportCB.setText("Feedback Support");
    feedbackSupportCB.setName("feedbackSupportCB"); // NOI18N
    feedbackSupportCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        feedbackSupportCBActionPerformed(evt);
      }
    });
    feedbackSupportPanel.add(feedbackSupportCB);

    capabilitiesPanel.add(feedbackSupportPanel);

    FeedbackPropertiesPanel.setName("FeedbackPropertiesPanel"); // NOI18N
    FlowLayout flowLayout13 = new FlowLayout(FlowLayout.LEFT);
    flowLayout13.setAlignOnBaseline(true);
    FeedbackPropertiesPanel.setLayout(flowLayout13);

    feedbackIdLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    feedbackIdLbl.setLabelFor(nodeSpinner);
    feedbackIdLbl.setText("Feedback Id:");
    feedbackIdLbl.setName("feedbackIdLbl"); // NOI18N
    feedbackIdLbl.setPreferredSize(new Dimension(90, 17));
    FeedbackPropertiesPanel.add(feedbackIdLbl);

    nodeSpinner.setModel(new SpinnerNumberModel(0, 0, 256, 1));
    nodeSpinner.setName("nodeSpinner"); // NOI18N
    nodeSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        nodeSpinnerStateChanged(evt);
      }
    });
    FeedbackPropertiesPanel.add(nodeSpinner);

    channelCountLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    channelCountLbl.setLabelFor(channelCountSpinner);
    channelCountLbl.setText("Channel Count:");
    channelCountLbl.setName("channelCountLbl"); // NOI18N
    channelCountLbl.setPreferredSize(new Dimension(90, 17));
    FeedbackPropertiesPanel.add(channelCountLbl);
    channelCountLbl.getAccessibleContext().setAccessibleName("Channel Count");

    channelCountSpinner.setModel(new SpinnerNumberModel(0, 0, 32, 1));
    channelCountSpinner.setName("channelCountSpinner"); // NOI18N
    channelCountSpinner.setPreferredSize(new Dimension(50, 23));
    channelCountSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        channelCountSpinnerStateChanged(evt);
      }
    });
    FeedbackPropertiesPanel.add(channelCountSpinner);

    bus0Lbl.setHorizontalAlignment(SwingConstants.TRAILING);
    bus0Lbl.setLabelFor(bus0Spinner);
    bus0Lbl.setText("Ch 0 Modules:");
    bus0Lbl.setName("bus0Lbl"); // NOI18N
    bus0Lbl.setPreferredSize(new Dimension(90, 17));
    FeedbackPropertiesPanel.add(bus0Lbl);

    bus0Spinner.setModel(new SpinnerNumberModel(0, 0, 32, 1));
    bus0Spinner.setName("bus0Spinner"); // NOI18N
    bus0Spinner.setPreferredSize(new Dimension(50, 23));
    bus0Spinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        bus0SpinnerStateChanged(evt);
      }
    });
    FeedbackPropertiesPanel.add(bus0Spinner);

    bus1Lbl.setHorizontalAlignment(SwingConstants.TRAILING);
    bus1Lbl.setLabelFor(bus1Spinner);
    bus1Lbl.setText("Ch 1 Modules:");
    bus1Lbl.setName("bus1Lbl"); // NOI18N
    bus1Lbl.setPreferredSize(new Dimension(90, 17));
    FeedbackPropertiesPanel.add(bus1Lbl);

    bus1Spinner.setModel(new SpinnerNumberModel(0, 0, 32, 1));
    bus1Spinner.setName("bus1Spinner"); // NOI18N
    bus1Spinner.setPreferredSize(new Dimension(50, 23));
    bus1Spinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        bus1SpinnerStateChanged(evt);
      }
    });
    FeedbackPropertiesPanel.add(bus1Spinner);

    bus2Lbl.setHorizontalAlignment(SwingConstants.TRAILING);
    bus2Lbl.setLabelFor(bus2Spinner);
    bus2Lbl.setText("Ch 2 Modules:");
    bus2Lbl.setName("bus2Lbl"); // NOI18N
    bus2Lbl.setPreferredSize(new Dimension(90, 17));
    FeedbackPropertiesPanel.add(bus2Lbl);

    bus2Spinner.setModel(new SpinnerNumberModel(0, 0, 32, 1));
    bus2Spinner.setName("bus2Spinner"); // NOI18N
    bus2Spinner.setPreferredSize(new Dimension(50, 23));
    bus2Spinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        bus2SpinnerStateChanged(evt);
      }
    });
    FeedbackPropertiesPanel.add(bus2Spinner);

    bus3Lbl.setHorizontalAlignment(SwingConstants.TRAILING);
    bus3Lbl.setLabelFor(bus3Spinner);
    bus3Lbl.setText("Ch 3 Modules:");
    bus3Lbl.setName("bus3Lbl"); // NOI18N
    bus3Lbl.setPreferredSize(new Dimension(90, 17));
    FeedbackPropertiesPanel.add(bus3Lbl);

    bus3Spinner.setModel(new SpinnerNumberModel(0, 0, 32, 1));
    bus3Spinner.setName("bus3Spinner"); // NOI18N
    bus3Spinner.setPreferredSize(new Dimension(50, 23));
    bus3Spinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        bus3SpinnerStateChanged(evt);
      }
    });
    FeedbackPropertiesPanel.add(bus3Spinner);

    capabilitiesPanel.add(FeedbackPropertiesPanel);

    locomotiveSynchSupportPanel.setName("locomotiveSynchSupportPanel"); // NOI18N
    FlowLayout flowLayout6 = new FlowLayout(FlowLayout.LEFT);
    flowLayout6.setAlignOnBaseline(true);
    locomotiveSynchSupportPanel.setLayout(flowLayout6);

    locomotiveSynchSupportCB.setText("Locomotive Synchronization Support");
    locomotiveSynchSupportCB.setName("locomotiveSynchSupportCB"); // NOI18N
    locomotiveSynchSupportCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        locomotiveSynchSupportCBActionPerformed(evt);
      }
    });
    locomotiveSynchSupportPanel.add(locomotiveSynchSupportCB);

    capabilitiesPanel.add(locomotiveSynchSupportPanel);

    accessorySynchSupportPanel.setName("accessorySynchSupportPanel"); // NOI18N
    FlowLayout flowLayout7 = new FlowLayout(FlowLayout.LEFT);
    flowLayout7.setAlignOnBaseline(true);
    accessorySynchSupportPanel.setLayout(flowLayout7);

    accessorySynchSupportCB.setText("Accessory Synchronization Support");
    accessorySynchSupportCB.setName("accessorySynchSupportCB"); // NOI18N
    accessorySynchSupportCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        accessorySynchSupportCBActionPerformed(evt);
      }
    });
    accessorySynchSupportPanel.add(accessorySynchSupportCB);

    capabilitiesPanel.add(accessorySynchSupportPanel);

    locomotiveImageSynchSupportPanel.setName("locomotiveImageSynchSupportPanel"); // NOI18N

    locomotiveImageSynchSupportCB.setText("Locomotive Image Synchronization Support");
    locomotiveImageSynchSupportCB.setName("locomotiveImageSynchSupportCB"); // NOI18N
    locomotiveImageSynchSupportCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        locomotiveImageSynchSupportCBActionPerformed(evt);
      }
    });
    locomotiveImageSynchSupportPanel.add(locomotiveImageSynchSupportCB);

    capabilitiesPanel.add(locomotiveImageSynchSupportPanel);

    locomotiveFunctionSynchSupportPanel.setName("locomotiveFunctionSynchSupportPanel"); // NOI18N
    FlowLayout flowLayout8 = new FlowLayout(FlowLayout.LEFT);
    flowLayout8.setAlignOnBaseline(true);
    locomotiveFunctionSynchSupportPanel.setLayout(flowLayout8);

    locomotiveFunctionSynchSupportCB.setText("Locomotive Functions Synchronization Support");
    locomotiveFunctionSynchSupportCB.setName("locomotiveFunctionSynchSupportCB"); // NOI18N
    locomotiveFunctionSynchSupportCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        locomotiveFunctionSynchSupportCBActionPerformed(evt);
      }
    });
    locomotiveFunctionSynchSupportPanel.add(locomotiveFunctionSynchSupportCB);

    capabilitiesPanel.add(locomotiveFunctionSynchSupportPanel);

    protocolPanel.setName("protocolPanel"); // NOI18N

    supportedProtocolsLbl.setText("Supported Protocols:");
    supportedProtocolsLbl.setName("supportedProtocolsLbl"); // NOI18N
    protocolPanel.add(supportedProtocolsLbl);

    mmRB.setText("MM");
    mmRB.setToolTipText("Marklin MM");
    mmRB.setName("mmRB"); // NOI18N
    mmRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        mmRBActionPerformed(evt);
      }
    });
    protocolPanel.add(mmRB);

    mfxRB.setText("MFX");
    mfxRB.setToolTipText("Marklin MFX");
    mfxRB.setName("mfxRB"); // NOI18N
    mfxRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        mfxRBActionPerformed(evt);
      }
    });
    protocolPanel.add(mfxRB);

    dccRB.setText("DCC");
    dccRB.setToolTipText("DCC");
    dccRB.setName("dccRB"); // NOI18N
    dccRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dccRBActionPerformed(evt);
      }
    });
    protocolPanel.add(dccRB);

    sxRB.setText("SX");
    sxRB.setToolTipText("Selectrix");
    sxRB.setName("sxRB"); // NOI18N
    sxRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        sxRBActionPerformed(evt);
      }
    });
    protocolPanel.add(sxRB);

    filler10.setName("filler10"); // NOI18N
    protocolPanel.add(filler10);

    supportedConnectionTypesLbl.setText("Supported Connection Types:");
    supportedConnectionTypesLbl.setName("supportedConnectionTypesLbl"); // NOI18N
    protocolPanel.add(supportedConnectionTypesLbl);

    supConTypeNetworkRB.setText("Network");
    supConTypeNetworkRB.setToolTipText("");
    supConTypeNetworkRB.setName("supConTypeNetworkRB"); // NOI18N
    supConTypeNetworkRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        supConTypeNetworkRBActionPerformed(evt);
      }
    });
    protocolPanel.add(supConTypeNetworkRB);

    supConTypeSerialRB.setText("Serial");
    supConTypeSerialRB.setName("supConTypeSerialRB"); // NOI18N
    supConTypeSerialRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        supConTypeSerialRBActionPerformed(evt);
      }
    });
    protocolPanel.add(supConTypeSerialRB);

    capabilitiesPanel.add(protocolPanel);

    descPanel.setName("descPanel"); // NOI18N

    nameLbl.setText("Name:");
    nameLbl.setName("nameLbl"); // NOI18N
    descPanel.add(nameLbl);

    descriptionTF.setText("description");
    descriptionTF.setName("descriptionTF"); // NOI18N
    descriptionTF.setPreferredSize(new Dimension(200, 23));
    descriptionTF.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        descriptionTFFocusLost(evt);
      }
    });
    descPanel.add(descriptionTF);

    filler9.setName("filler9"); // NOI18N
    descPanel.add(filler9);

    classNameLbl.setText("Class name:");
    classNameLbl.setName("classNameLbl"); // NOI18N
    descPanel.add(classNameLbl);

    classNameTF.setText("class name");
    classNameTF.setName("classNameTF"); // NOI18N
    classNameTF.setPreferredSize(new Dimension(350, 23));
    classNameTF.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        classNameTFFocusLost(evt);
      }
    });
    descPanel.add(classNameTF);

    shortNameTFLb.setText("Shot Name:");
    shortNameTFLb.setName("shortNameTFLb"); // NOI18N
    descPanel.add(shortNameTFLb);

    shortNameTF.setName("shortNameTF"); // NOI18N
    shortNameTF.setPreferredSize(new Dimension(100, 23));
    shortNameTF.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        shortNameTFFocusLost(evt);
      }
    });
    descPanel.add(shortNameTF);

    capabilitiesPanel.add(descPanel);

    centerPanel.add(capabilitiesPanel, BorderLayout.CENTER);

    add(centerPanel, BorderLayout.CENTER);

    bottomPanel.setName("bottomPanel"); // NOI18N
    bottomPanel.setPreferredSize(new Dimension(1014, 40));
    bottomPanel.setRequestFocusEnabled(false);
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

    leftBottomPanel.setName("leftBottomPanel"); // NOI18N
    FlowLayout flowLayout11 = new FlowLayout(FlowLayout.LEFT);
    flowLayout11.setAlignOnBaseline(true);
    leftBottomPanel.setLayout(flowLayout11);

    enableEditCB.setText("Enable Edit");
    enableEditCB.setName("enableEditCB"); // NOI18N
    enableEditCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        enableEditCBActionPerformed(evt);
      }
    });
    leftBottomPanel.add(enableEditCB);

    newBtn.setIcon(new ImageIcon(getClass().getResource("/media/add-24.png"))); // NOI18N
    newBtn.setToolTipText("Add a New Command Station");
    newBtn.setEnabled(false);
    newBtn.setName("newBtn"); // NOI18N
    newBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        newBtnActionPerformed(evt);
      }
    });
    leftBottomPanel.add(newBtn);

    filler8.setName("filler8"); // NOI18N
    leftBottomPanel.add(filler8);

    idLbl.setText("id:");
    idLbl.setName("idLbl"); // NOI18N
    leftBottomPanel.add(idLbl);

    idTF.setText("id");
    idTF.setName("idTF"); // NOI18N
    idTF.setPreferredSize(new Dimension(200, 23));
    idTF.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        idTFFocusLost(evt);
      }
    });
    leftBottomPanel.add(idTF);

    bottomPanel.add(leftBottomPanel);

    rightBottomPanel.setName("rightBottomPanel"); // NOI18N
    FlowLayout flowLayout10 = new FlowLayout(FlowLayout.RIGHT);
    flowLayout10.setAlignOnBaseline(true);
    rightBottomPanel.setLayout(flowLayout10);

    filler1.setName("filler1"); // NOI18N
    rightBottomPanel.add(filler1);

    saveBtn.setIcon(new ImageIcon(getClass().getResource("/media/save-24.png"))); // NOI18N
    saveBtn.setToolTipText("Save and Exit");
    saveBtn.setEnabled(false);
    saveBtn.setName("saveBtn"); // NOI18N
    saveBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveBtnActionPerformed(evt);
      }
    });
    rightBottomPanel.add(saveBtn);

    bottomPanel.add(rightBottomPanel);

    add(bottomPanel, BorderLayout.SOUTH);
  }// </editor-fold>//GEN-END:initComponents


  private void testConnectionBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_testConnectionBtnActionPerformed
    Logger.trace(evt.getActionCommand());
    this.progressBar.setVisible(true);
    progressBar.setIndeterminate(true);

    task = new Task();
    task.addPropertyChangeListener(this);
    task.execute();
  }//GEN-LAST:event_testConnectionBtnActionPerformed

  private void commandStationComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_commandStationComboBoxActionPerformed
    selectedCommandStation = (CommandStationBean) commandStationComboBoxModel.getSelectedItem();
    defaultCommandStationChkBox.setSelected(selectedCommandStation.isDefault());
    setFieldValues();
    this.enableFields(this.enableEditCB.isSelected());
    Logger.trace("Selected CS: " + this.selectedCommandStation.getDescription());
  }//GEN-LAST:event_commandStationComboBoxActionPerformed

  private void defaultCommandStationChkBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_defaultCommandStationChkBoxActionPerformed
    Logger.trace("Setting " + selectedCommandStation + " as default");
    PersistenceFactory.getService().changeDefaultCommandStation(selectedCommandStation);
    initModels();
  }//GEN-LAST:event_defaultCommandStationChkBoxActionPerformed

  private void saveBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
    Logger.trace(evt.getActionCommand());
    PersistenceFactory.getService().persist(this.selectedCommandStation);

    initModels();
  }//GEN-LAST:event_saveBtnActionPerformed

  private void ipAddressTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_ipAddressTFFocusLost
    Logger.trace("IP address:" + ipAddressTF.getText());
    this.selectedCommandStation.setIpAddress(this.ipAddressTF.getText());
    PersistenceFactory.getService().persist(this.selectedCommandStation);
  }//GEN-LAST:event_ipAddressTFFocusLost

  private void portSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_portSpinnerStateChanged
    Logger.trace("Port: " + this.portSpinner.getValue());
    this.selectedCommandStation.setNetworkPort((Integer) this.portSpinner.getValue());
  }//GEN-LAST:event_portSpinnerStateChanged

  private void feedbackSupportCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_feedbackSupportCBActionPerformed
    this.selectedCommandStation.setFeedbackSupport(this.feedbackSupportCB.isSelected());
  }//GEN-LAST:event_feedbackSupportCBActionPerformed

  private void enableEditCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_enableEditCBActionPerformed
    Logger.trace(evt.getActionCommand() + " " + this.enableEditCB.isSelected());
    enableFields(this.enableEditCB.isSelected());
  }//GEN-LAST:event_enableEditCBActionPerformed

  private void newBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_newBtnActionPerformed
    this.selectedCommandStation = new CommandStationBean();
    this.selectedCommandStation.setId("new.cs");
    setFieldValues();
  }//GEN-LAST:event_newBtnActionPerformed

  private void enabledCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_enabledCBActionPerformed
    this.selectedCommandStation.setEnabled(this.enabledCB.isSelected());
    PersistenceFactory.getService().persist(this.selectedCommandStation);
  }//GEN-LAST:event_enabledCBActionPerformed

  private void descriptionTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_descriptionTFFocusLost
    this.selectedCommandStation.setDescription(this.descriptionTF.getText());
  }//GEN-LAST:event_descriptionTFFocusLost

  private void classNameTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_classNameTFFocusLost
    this.selectedCommandStation.setClassName(this.classNameTF.getText());
  }//GEN-LAST:event_classNameTFFocusLost

  private void idTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_idTFFocusLost
    this.selectedCommandStation.setId(this.idTF.getText());
  }//GEN-LAST:event_idTFFocusLost

  private void locomotiveFunctionSynchSupportCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_locomotiveFunctionSynchSupportCBActionPerformed
    this.selectedCommandStation.setLocomotiveFunctionSynchronizationSupport(this.locomotiveFunctionSynchSupportCB.isSelected());
  }//GEN-LAST:event_locomotiveFunctionSynchSupportCBActionPerformed

  private void locomotiveImageSynchSupportCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_locomotiveImageSynchSupportCBActionPerformed
    this.selectedCommandStation.setLocomotiveImageSynchronizationSupport(this.locomotiveImageSynchSupportCB.isSelected());
  }//GEN-LAST:event_locomotiveImageSynchSupportCBActionPerformed

  private void accessorySynchSupportCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_accessorySynchSupportCBActionPerformed
    this.selectedCommandStation.setAccessorySynchronizationSupport(this.accessorySynchSupportCB.isSelected());
  }//GEN-LAST:event_accessorySynchSupportCBActionPerformed

  private void locomotiveSynchSupportCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_locomotiveSynchSupportCBActionPerformed
    this.selectedCommandStation.setLocomotiveSynchronizationSupport(this.locomotiveSynchSupportCB.isSelected());
  }//GEN-LAST:event_locomotiveSynchSupportCBActionPerformed

  private void decoderControlCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_decoderControlCBActionPerformed
    this.selectedCommandStation.setDecoderControlSupport(this.decoderControlCB.isSelected());
  }//GEN-LAST:event_decoderControlCBActionPerformed

  private void networkRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_networkRBActionPerformed
    //this.enableFields(this.enableEditCB.isSelected());
    this.selectedCommandStation.setConnectionType(ConnectionType.NETWORK);
    Logger.trace("Connectiontypes set to: " + ConnectionType.NETWORK);
    PersistenceFactory.getService().persist(this.selectedCommandStation);
    this.enableFields(this.enableEditCB.isSelected());
  }//GEN-LAST:event_networkRBActionPerformed

  private void serialRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_serialRBActionPerformed
    //this.enableFields(this.enableEditCB.isSelected());
    this.selectedCommandStation.setConnectionType(ConnectionType.SERIAL);
    Logger.trace("Connectiontypes set to: " + ConnectionType.SERIAL);
    PersistenceFactory.getService().persist(this.selectedCommandStation);
    this.enableFields(this.enableEditCB.isSelected());
  }//GEN-LAST:event_serialRBActionPerformed

  private void shortNameTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_shortNameTFFocusLost
    this.selectedCommandStation.setShortName(this.shortNameTF.getText());
    this.shortNameLbl.setText(this.selectedCommandStation.getShortName());
  }//GEN-LAST:event_shortNameTFFocusLost

  private void mmRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_mmRBActionPerformed
    if (this.mmRB.isSelected()) {
      this.selectedCommandStation.addProtocol(Protocol.MM);
    } else {
      this.selectedCommandStation.removeProtocol(Protocol.MM);
    }
  }//GEN-LAST:event_mmRBActionPerformed

  private void mfxRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_mfxRBActionPerformed
    if (this.mfxRB.isSelected()) {
      this.selectedCommandStation.addProtocol(Protocol.MFX);
    } else {
      this.selectedCommandStation.removeProtocol(Protocol.MFX);
    }
  }//GEN-LAST:event_mfxRBActionPerformed

  private void dccRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_dccRBActionPerformed
    if (this.dccRB.isSelected()) {
      this.selectedCommandStation.addProtocol(Protocol.DCC);
    } else {
      this.selectedCommandStation.removeProtocol(Protocol.DCC);
    }
  }//GEN-LAST:event_dccRBActionPerformed

  private void sxRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sxRBActionPerformed
    if (this.sxRB.isSelected()) {
      this.selectedCommandStation.addProtocol(Protocol.SX);
    } else {
      this.selectedCommandStation.removeProtocol(Protocol.SX);
    }
  }//GEN-LAST:event_sxRBActionPerformed

  private void accessorySupportCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_accessorySupportCBActionPerformed
    this.selectedCommandStation.setAccessoryControlSupport(this.accessorySupportCB.isSelected());
  }//GEN-LAST:event_accessorySupportCBActionPerformed

  private void serialPortRefreshBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_serialPortRefreshBtnActionPerformed
    SerialPort comPorts[] = SerialPort.getCommPorts();
    serialPortComboBoxModel = new DefaultComboBoxModel(comPorts);
    serialPortCB.setModel(serialPortComboBoxModel);

    String portName = selectedCommandStation.getSerialPort();
    Logger.trace("Selected portName: " + portName);

    if (portName != null) {
      try {
        SerialPort comPort = SerialPort.getCommPort(portName);
        this.serialPortComboBoxModel.setSelectedItem(comPort);
        Logger.trace("Selected ComPort: " + comPort);

        if (comPort != null) {
          this.serialPortComboBoxModel.setSelectedItem(comPort);
        }
      } catch (SerialPortInvalidPortException ioe) {
        Logger.warn("Can't find com port: " + portName + "; " + ioe.getMessage());
      }
    }
  }//GEN-LAST:event_serialPortRefreshBtnActionPerformed

  private void serialPortCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_serialPortCBActionPerformed
    SerialPort comPort = (SerialPort) serialPortComboBoxModel.getSelectedItem();
    String portDescription = comPort.getSystemPortName();
    this.selectedCommandStation.setSerialPort(portDescription);
    Logger.trace("Selected Comport: " + portDescription);
    PersistenceFactory.getService().persist(this.selectedCommandStation);
  }//GEN-LAST:event_serialPortCBActionPerformed

  private void supConTypeNetworkRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_supConTypeNetworkRBActionPerformed
    if (this.supConTypeNetworkRB.isSelected()) {
      this.selectedCommandStation.addSupportedConnectionType(ConnectionType.NETWORK);
    } else {
      this.selectedCommandStation.removeSupportedConnectionType(ConnectionType.NETWORK);
    }
  }//GEN-LAST:event_supConTypeNetworkRBActionPerformed

  private void supConTypeSerialRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_supConTypeSerialRBActionPerformed
    if (this.supConTypeSerialRB.isSelected()) {
      this.selectedCommandStation.addSupportedConnectionType(ConnectionType.SERIAL);
    } else {
      this.selectedCommandStation.removeSupportedConnectionType(ConnectionType.SERIAL);
    }
  }//GEN-LAST:event_supConTypeSerialRBActionPerformed

  private void channelCountSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_channelCountSpinnerStateChanged
    this.selectedCommandStation.setFeedbackChannelCount((Integer) this.channelCountSpinner.getValue());
    PersistenceFactory.getService().persist(this.selectedCommandStation);
  }//GEN-LAST:event_channelCountSpinnerStateChanged

  private void bus0SpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_bus0SpinnerStateChanged
    this.selectedCommandStation.setFeedbackBus0ModuleCount((Integer) this.bus0Spinner.getValue());
    PersistenceFactory.getService().persist(this.selectedCommandStation);
  }//GEN-LAST:event_bus0SpinnerStateChanged

  private void bus1SpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_bus1SpinnerStateChanged
    this.selectedCommandStation.setFeedbackBus1ModuleCount((Integer) this.bus1Spinner.getValue());
    PersistenceFactory.getService().persist(this.selectedCommandStation);
  }//GEN-LAST:event_bus1SpinnerStateChanged

  private void bus2SpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_bus2SpinnerStateChanged
    this.selectedCommandStation.setFeedbackBus2ModuleCount((Integer) this.bus2Spinner.getValue());
    PersistenceFactory.getService().persist(this.selectedCommandStation);
  }//GEN-LAST:event_bus2SpinnerStateChanged

  private void bus3SpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_bus3SpinnerStateChanged
    this.selectedCommandStation.setFeedbackBus3ModuleCount((Integer) this.bus3Spinner.getValue());
    PersistenceFactory.getService().persist(this.selectedCommandStation);
  }//GEN-LAST:event_bus3SpinnerStateChanged

  private void nodeSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_nodeSpinnerStateChanged
    this.selectedCommandStation.setFeedbackModuleIdentifier(this.nodeSpinner.getValue().toString());
    PersistenceFactory.getService().persist(this.selectedCommandStation);
  }//GEN-LAST:event_nodeSpinnerStateChanged

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("progress".equals(evt.getPropertyName())) {
      int progress = (Integer) evt.getNewValue();
      progressBar.setValue(progress);
      progressBar.setIndeterminate(progress > 20);

      if (task.isDone()) {
        testConnectionBtn.setEnabled(true);
      }
    }

    if ("ipAddress".equals(evt.getPropertyName())) {
      Logger.trace("Found IP address: " + evt.getNewValue());
      ipAddressTF.setText((String) evt.getNewValue());
      selectedCommandStation.setIpAddress(this.ipAddressTF.getText());
    }

    if ("serial".equals(evt.getPropertyName())) {
      Logger.trace("Found Serial: " + evt.getNewValue());
      lastUsedSerialLbl.setText((String) evt.getNewValue());
      selectedCommandStation.setLastUsedSerial(lastUsedSerialLbl.getText());
      lastUsedSerialLbl.setText("Serial number: " + lastUsedSerialLbl.getText());
      lastUsedSerialLbl.setVisible(true);
    }

    if ("node".equals(evt.getPropertyName())) {
      Logger.trace("Node id: " + evt.getNewValue());
      selectedCommandStation.setFeedbackModuleIdentifier("" + evt.getNewValue());
      this.nodeSpinner.setValue(evt.getNewValue());
    }

    if ("channels".equals(evt.getPropertyName())) {
      Logger.trace("Feedback channel count: " + evt.getNewValue());
      selectedCommandStation.setFeedbackChannelCount((Integer) evt.getNewValue());
      this.channelCountSpinner.setValue(evt.getNewValue());
    }

    if ("bus0".equals(evt.getPropertyName())) {
      Logger.trace("Bus 0 Lenght: " + evt.getNewValue());
      selectedCommandStation.setFeedbackBus0ModuleCount((Integer) evt.getNewValue());
      bus0Spinner.setValue(evt.getNewValue());
    }

    if ("bus1".equals(evt.getPropertyName())) {
      Logger.trace("Bus 1 Lenght: " + evt.getNewValue());
      selectedCommandStation.setFeedbackBus1ModuleCount((Integer) evt.getNewValue());
      bus1Spinner.setValue(evt.getNewValue());
    }

    if ("bus2".equals(evt.getPropertyName())) {
      Logger.trace("Bus 2 Lenght: " + evt.getNewValue());
      selectedCommandStation.setFeedbackBus2ModuleCount((Integer) evt.getNewValue());
      bus2Spinner.setValue(evt.getNewValue());
    }

    if ("bus3".equals(evt.getPropertyName())) {
      Logger.trace("Bus 3 Lenght: " + evt.getNewValue());
      selectedCommandStation.setFeedbackBus3ModuleCount((Integer) evt.getNewValue());
      bus3Spinner.setValue(evt.getNewValue());
    }

    if ("done".equals(evt.getPropertyName())) {
      Logger.trace("Done: " + evt.getNewValue());
      this.connectionTestResultLbl.setText((String) evt.getNewValue());
      this.progressBar.setVisible(false);
      PersistenceFactory.getService().persist(this.selectedCommandStation);
    }
  }

  class Task extends SwingWorker<Void, Void> {

    @Override
    public Void doInBackground() {
      setProgress(0);

      if (null == selectedCommandStation.getConnectionType()) {
        firePropertyChange("done", "", "Can't Connect");
        setProgress(100);
      } else {
        switch (selectedCommandStation.getConnectionType()) {
          case NETWORK -> {
            try {
              String ip = selectedCommandStation.getIpAddress();
              setProgress(10);
              DecoderController commandStation = createCommandStation(selectedCommandStation);
              boolean canConnect = false;
              setProgress(20);
              if (ip == null && selectedCommandStation.isIpAutoConfiguration()) {
                //Try to obtain the ip through auto configuration
                canConnect = commandStation.connect();
                if (canConnect) {
                  firePropertyChange("ipAddress", "", commandStation.getIp());
                }
                setProgress(30);

              } else {
                if (Ping.IsReachable(ip)) {
                  setProgress(10);
                  canConnect = commandStation.connect();
                  setProgress(20);
                }
              }
              if (canConnect) {
                //Let obtain some data fail safe
                try {
                  String sn = commandStation.getDevice().getSerial();
                  firePropertyChange("serial", "", sn);
                  setProgress(50);

                  if (commandStation instanceof FeedbackController) {
                    DeviceBean fbDevice = ((FeedbackController) commandStation).getFeedbackDevice();
                    if (fbDevice != null) {
                      Logger.trace(fbDevice.getName() + " Supports Feedback");
                      String id = fbDevice.getIdentifier();

                      int node = Integer.parseInt(id.replace("0x", ""), 16);
                      firePropertyChange("node", selectedCommandStation.getFeedbackModuleIdentifier(), node);

                      Integer channelCount = fbDevice.getSensorBuses().size();
                      firePropertyChange("channels", selectedCommandStation.getFeedbackChannelCount(), channelCount);

                      Integer bus0 = fbDevice.getBusLength(0);
                      firePropertyChange("bus0", selectedCommandStation.getFeedbackBus0ModuleCount(), bus0);

                      Integer bus1 = fbDevice.getBusLength(1);
                      firePropertyChange("bus1", selectedCommandStation.getFeedbackBus1ModuleCount(), bus1);

                      Integer bus2 = fbDevice.getBusLength(2);
                      firePropertyChange("bus2", selectedCommandStation.getFeedbackBus2ModuleCount(), bus2);

                      Integer bus3 = fbDevice.getBusLength(3);
                      firePropertyChange("bus3", selectedCommandStation.getFeedbackBus3ModuleCount(), bus0);

                      Logger.trace("ID: " + id + " Node: " + node + " Bus 0: " + bus0 + " Bus 1: " + bus1 + " Bus 2: " + bus2 + " Bus 3: " + bus3);
                    }
                  }
                } catch (RuntimeException e) {
                  Logger.warn("Error in data retrieval " + e.getMessage());
                }
                setProgress(90);

                commandStation.disconnect();
                firePropertyChange("done", "", "Connection succeeded");
                setProgress(100);

              } else {
                firePropertyChange("done", "", "Can't Connect");
                setProgress(100);
              }
            } catch (Exception e) {
              Logger.error(e.getMessage());
              firePropertyChange("done", "", "Can't Connect");
              setProgress(100);
            }
          }
          case SERIAL -> {
            String commPort = selectedCommandStation.getSerialPort();
            setProgress(10);
            if (commPort != null) {
              try {
                SerialPort comPort = SerialPort.getCommPort(commPort);
                comPort.openPort();
                if (comPort.isOpen()) {
                  firePropertyChange("done", "", "Connection succeeded");
                  setProgress(100);
                  comPort.closePort();
                } else {
                  firePropertyChange("done", "", "Can't open port " + commPort + " connection failed");
                  setProgress(100);
                }
              } catch (SerialPortInvalidPortException ioe) {
                firePropertyChange("done", "", "Port " + commPort + " does not exist. Connection failed");
                setProgress(100);
              }
            }
          }
          default -> {
            firePropertyChange("done", "", "Can't Connect");
            setProgress(100);
          }
        }
      }
      // } // catch (Exeption e) 
      return null;
    }

    @Override
    public void done() {
      testConnectionBtn.setEnabled(true);
    }
  }

  private DecoderController createCommandStation(CommandStationBean commandStationBean) {
    return ControllerFactory.getDecoderController(commandStationBean, false);
  }

  private boolean checkConnection(final DecoderController commandStation) {
    if (commandStation != null) {
      if (commandStation.isConnected()) {
        return true;
      } else {
        boolean canConnect = commandStation.connect();
        if (canConnect) {
          commandStation.disconnect();
        }
        return canConnect;
      }
    }
    return false;
  }

  public static void main(String args[]) {
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.warn("Can't set the LookAndFeel: " + ex);
    }
    java.awt.EventQueue.invokeLater(() -> {

      CommandStationPanel testPanel = new CommandStationPanel();
      JFrame testFrame = new JFrame();
      JDialog testDialog = new JDialog(testFrame, true);

      testDialog.add(testPanel);

      testDialog.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
          System.exit(0);
        }
      });
      testDialog.pack();
      testDialog.setLocationRelativeTo(null);

      testDialog.setVisible(true);
    });
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  JPanel FeedbackPropertiesPanel;
  JCheckBox accessorySupportCB;
  JPanel accessorySupportPanel;
  JCheckBox accessorySynchSupportCB;
  JPanel accessorySynchSupportPanel;
  JCheckBox autoConfChkBox;
  JPanel bottomPanel;
  JLabel bus0Lbl;
  JSpinner bus0Spinner;
  JLabel bus1Lbl;
  JSpinner bus1Spinner;
  JLabel bus2Lbl;
  JSpinner bus2Spinner;
  JLabel bus3Lbl;
  JSpinner bus3Spinner;
  JPanel capabilitiesPanel;
  JPanel centerPanel;
  JLabel channelCountLbl;
  JSpinner channelCountSpinner;
  JLabel classNameLbl;
  JTextField classNameTF;
  JComboBox<CommandStationBean> commandStationComboBox;
  JLabel commandStationLbl;
  JPanel commandStationSelectionPanel;
  JPanel connectionPanel;
  JLabel connectionPropertiesLbl;
  JPanel connectionTestPanel;
  JLabel connectionTestResultLbl;
  ButtonGroup connectionTypeBG;
  JLabel connectionTypeLbl;
  JPanel csPropertiesPanel;
  JRadioButton dccRB;
  JCheckBox decoderControlCB;
  JPanel decoderControlSupportPanel;
  JCheckBox defaultCommandStationChkBox;
  JPanel descPanel;
  JTextField descriptionTF;
  JCheckBox enableEditCB;
  JCheckBox enabledCB;
  JLabel feedbackIdLbl;
  JCheckBox feedbackSupportCB;
  JPanel feedbackSupportPanel;
  Box.Filler filler1;
  Box.Filler filler10;
  Box.Filler filler2;
  Box.Filler filler3;
  Box.Filler filler4;
  Box.Filler filler5;
  Box.Filler filler6;
  Box.Filler filler7;
  Box.Filler filler8;
  Box.Filler filler9;
  JLabel idLbl;
  JTextField idTF;
  JTextField ipAddressTF;
  JLabel lastUsedSerialLbl;
  JPanel leftBottomPanel;
  JCheckBox locomotiveFunctionSynchSupportCB;
  JPanel locomotiveFunctionSynchSupportPanel;
  JCheckBox locomotiveImageSynchSupportCB;
  JPanel locomotiveImageSynchSupportPanel;
  JCheckBox locomotiveSynchSupportCB;
  JPanel locomotiveSynchSupportPanel;
  JRadioButton mfxRB;
  JRadioButton mmRB;
  JLabel nameLbl;
  JRadioButton networkRB;
  JButton newBtn;
  JSpinner nodeSpinner;
  JLabel portLbl;
  JSpinner portSpinner;
  JProgressBar progressBar;
  JPanel protocolPanel;
  JPanel rightBottomPanel;
  JButton saveBtn;
  JComboBox<SerialPort> serialPortCB;
  JButton serialPortRefreshBtn;
  JRadioButton serialRB;
  JLabel shortNameLbl;
  JTextField shortNameTF;
  JLabel shortNameTFLb;
  JRadioButton supConTypeNetworkRB;
  JRadioButton supConTypeSerialRB;
  JLabel supportedConnectionTypesLbl;
  JLabel supportedProtocolsLbl;
  JRadioButton sxRB;
  JButton testConnectionBtn;
  JPanel topPanel;
  // End of variables declaration//GEN-END:variables
}
