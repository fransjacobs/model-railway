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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
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
import jcs.commandStation.GenericController;
import jcs.entities.CommandStationBean;
import jcs.entities.CommandStationBean.ConnectionType;
import jcs.entities.CommandStationBean.Protocol;
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
      //String connectVia = selectedCommandStation.getConnectVia();
      this.networkRB.setSelected(selectedCommandStation.getConnectionTypes().contains(ConnectionType.NETWORK));
      this.serialRB.setSelected(selectedCommandStation.getConnectionTypes().contains(ConnectionType.SERIAL));

      String id = selectedCommandStation.getId();
      this.idTF.setText("Id: " + id);

      String description = selectedCommandStation.getDescription();
      this.descriptionTF.setText(description);

      String shortName = selectedCommandStation.getShortName();
      this.shortNameLbl.setText(shortName);
      this.shortNameTF.setText(shortName);

      String className = selectedCommandStation.getClassName();
      this.classNameTF.setText(className);

      String serialPort = selectedCommandStation.getSerialPort();
      if (serialPort != null) {
        SerialPort comPort = SerialPort.getCommPort(serialPort);
        this.serialPortComboBoxModel.setSelectedItem(comPort);
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

      //String protocols = selectedCommandStation.getProtocols();
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
        this.lastUsedSerialLbl.setText("CS Serial number: " + lastUsedSerial);
      } else {
        this.lastUsedSerialLbl.setVisible(false);
      }
    }
  }

  private void enableFields(boolean enable) {
    if (this.networkRB.isSelected()) {
      this.serialPortCB.setVisible(false);
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

      this.connectionPropertiesLbl.setText("Serial Port:");

      this.portLbl.setVisible(false);
      this.portSpinner.setVisible(false);
      this.autoConfChkBox.setVisible(false);
    }

    this.networkRB.setEnabled(enable);
    this.serialRB.setEnabled(enable);

    this.decoderControlCB.setEnabled(enable);
    this.accessorySupportCB.setEnabled(enable);
    this.feedbackSupportCB.setEnabled(enable);
    this.locomotiveSynchSupportCB.setEnabled(enable);
    this.accessorySynchSupportCB.setEnabled(enable);
    this.locomotiveImageSynchSupportCB.setEnabled(enable);
    this.locomotiveFunctionSynchSupportCB.setEnabled(enable);

    this.mmRB.setEnabled(enable);
    this.mfxRB.setEnabled(enable);
    this.dccRB.setEnabled(enable);
    this.sxRB.setEnabled(enable);

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
    filler5 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    shortNameLbl = new JLabel();
    filler6 = new Box.Filler(new Dimension(10, 0), new Dimension(10, 0), new Dimension(10, 32767));
    defaultCommandStationChkBox = new JCheckBox();
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
    locomotiveSynchSupportPanel = new JPanel();
    locomotiveSynchSupportCB = new JCheckBox();
    accessorySynchSupportPanel = new JPanel();
    accessorySynchSupportCB = new JCheckBox();
    locomotiveImageSynchSupportPanel = new JPanel();
    locomotiveImageSynchSupportCB = new JCheckBox();
    locomotiveFunctionSynchSupportPanel = new JPanel();
    locomotiveFunctionSynchSupportCB = new JCheckBox();
    protocolPanel = new JPanel();
    jLabel1 = new JLabel();
    mmRB = new JRadioButton();
    mfxRB = new JRadioButton();
    dccRB = new JRadioButton();
    sxRB = new JRadioButton();
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
    commandStationSelectionPanel.setPreferredSize(new Dimension(525, 33));
    FlowLayout flowLayout2 = new FlowLayout(FlowLayout.LEFT);
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
    commandStationComboBox.setPreferredSize(new Dimension(200, 23));
    commandStationComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        commandStationComboBoxActionPerformed(evt);
      }
    });
    commandStationSelectionPanel.add(commandStationComboBox);

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

    defaultCommandStationChkBox.setText("Set default");
    defaultCommandStationChkBox.setHorizontalTextPosition(SwingConstants.LEADING);
    defaultCommandStationChkBox.setName("defaultCommandStationChkBox"); // NOI18N
    defaultCommandStationChkBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        defaultCommandStationChkBoxActionPerformed(evt);
      }
    });
    commandStationSelectionPanel.add(defaultCommandStationChkBox);

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
    lastUsedSerialLbl.setName("lastUsedSerialLbl"); // NOI18N
    csPropertiesPanel.add(lastUsedSerialLbl);

    topPanel.add(csPropertiesPanel);

    add(topPanel, BorderLayout.NORTH);

    centerPanel.setMinimumSize(new Dimension(1000, 540));
    centerPanel.setName("centerPanel"); // NOI18N
    centerPanel.setPreferredSize(new Dimension(1000, 500));
    centerPanel.setLayout(new BorderLayout());

    connectionPanel.setName("connectionPanel"); // NOI18N
    FlowLayout flowLayout3 = new FlowLayout(FlowLayout.LEFT);
    flowLayout3.setAlignOnBaseline(true);
    connectionPanel.setLayout(flowLayout3);

    connectionTypeLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    connectionTypeLbl.setText("Connection Type(s):");
    connectionTypeLbl.setName("connectionTypeLbl"); // NOI18N
    connectionTypeLbl.setPreferredSize(new Dimension(115, 17));
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
    serialPortCB.setPreferredSize(new Dimension(150, 23));
    connectionPanel.add(serialPortCB);

    ipAddressTF.setText("0.0.0.0");
    ipAddressTF.setToolTipText("");
    ipAddressTF.setDoubleBuffered(true);
    ipAddressTF.setName("ipAddressTF"); // NOI18N
    ipAddressTF.setPreferredSize(new Dimension(150, 23));
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

    jLabel1.setText("Supported Protocols");
    jLabel1.setName("jLabel1"); // NOI18N
    protocolPanel.add(jLabel1);

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
    Logger.trace("Setting " + this.selectedCommandStation + " as default");
    PersistenceFactory.getService().changeDefaultCommandStation(selectedCommandStation);
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
    this.enableFields(this.enableEditCB.isSelected());
  }//GEN-LAST:event_networkRBActionPerformed

  private void serialRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_serialRBActionPerformed
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
      this.ipAddressTF.setText((String) evt.getNewValue());
      PersistenceFactory.getService().persist(this.selectedCommandStation);
    }

    if ("done".equals(evt.getPropertyName())) {
      Logger.trace("Done: " + evt.getNewValue());

      this.connectionTestResultLbl.setText((String) evt.getNewValue());
      this.progressBar.setVisible(false);
    }
  }

  class Task extends SwingWorker<Void, Void> {

    @Override
    public Void doInBackground() {
      setProgress(0);

      if (selectedCommandStation.getConnectionTypes().contains(ConnectionType.NETWORK)) {
        String ip = selectedCommandStation.getIpAddress();

        if (Ping.IsReachable(ip)) {
          setProgress(10);
          GenericController commandStation = createCommandStation(selectedCommandStation);
          setProgress(30);
          boolean canConnect = checkConnection(commandStation);

          setProgress(100);

          Logger.trace("canConnect: " + canConnect);
          if (canConnect) {
            if (selectedCommandStation.isIpAutoConfiguration()) {
              selectedCommandStation.setIpAddress(commandStation.getIp());

              this.firePropertyChange("ipAddress", "", commandStation.getIp());
            }
            this.firePropertyChange("done", "", "Connection succeeded");
          } else {
            this.firePropertyChange("done", "", "Can't Connect");
          }
        } else {
          setProgress(100);

          this.firePropertyChange("done", "", "Can't Connect");
        }
      }
      return null;
    }

    @Override
    public void done() {
      Toolkit.getDefaultToolkit().beep();
      testConnectionBtn.setEnabled(true);

      //connectionTestResultLbl.setText("");
      //progressMonitor.setProgress(0);
    }
  }

  private GenericController createCommandStation(CommandStationBean commandStationBean) {
    GenericController commandStation = null;

    String commandStationImplClassName = commandStationBean.getClassName();
    try {
      commandStation = (GenericController) Class.forName(commandStationImplClassName).getDeclaredConstructor(Boolean.class, CommandStationBean.class).newInstance(false, commandStationBean);
    } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
      Logger.trace("Can't instantiate a '" + commandStationImplClassName + "' " + ex.getMessage());
      Logger.trace(ex);
    }
    return commandStation;
  }

  private boolean checkConnection(final GenericController commandStation) {
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
  JCheckBox accessorySupportCB;
  JPanel accessorySupportPanel;
  JCheckBox accessorySynchSupportCB;
  JPanel accessorySynchSupportPanel;
  JCheckBox autoConfChkBox;
  JPanel bottomPanel;
  JPanel capabilitiesPanel;
  JPanel centerPanel;
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
  JCheckBox feedbackSupportCB;
  JPanel feedbackSupportPanel;
  Box.Filler filler1;
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
  JLabel jLabel1;
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
  JLabel portLbl;
  JSpinner portSpinner;
  JProgressBar progressBar;
  JPanel protocolPanel;
  JPanel rightBottomPanel;
  JButton saveBtn;
  JComboBox<SerialPort> serialPortCB;
  JRadioButton serialRB;
  JLabel shortNameLbl;
  JTextField shortNameTF;
  JLabel shortNameTFLb;
  JRadioButton sxRB;
  JButton testConnectionBtn;
  JPanel topPanel;
  // End of variables declaration//GEN-END:variables
}
