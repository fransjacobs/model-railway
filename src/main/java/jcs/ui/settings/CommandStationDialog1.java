/*
 * Copyright 2025 fransjacobs.
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.JCS;
import jcs.commandStation.esu.ecos.net.EcosConnectionFactory;
import jcs.commandStation.marklin.cs.net.CSConnectionFactory;
import jcs.entities.CommandStationBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;
import java.net.InetAddress;
import javax.swing.JDialog;
import jcs.commandStation.DecoderController;
import jcs.commandStation.entities.InfoBean;
import jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl;
import jcs.commandStation.marklin.cs.MarklinCentralStationImpl;
import jcs.util.Ping;

/**
 *
 * @author fransjacobs
 */
public class CommandStationDialog1 extends javax.swing.JDialog {

  private ComboBoxModel<CommandStationBean> commandStationCBM;
  private ComboBoxModel<CommandStationBean> feedbackCBM;
  private ComboBoxModel<String> serialPortCBM;
  private ComboBoxModel<String> fbpSerialPortCBM;
  private CommandStationBean selectedCommandStation;
  private CommandStationBean selectedFeedbackProvider;

  private final ExecutorService executor;

  private CommandStationBean emptyCS;
  private CommandStationBean emptyFB;

  private static final String MARKLIN_CS = "marklin.cs";
  private static final String ESU_ECOS = "esu-ecos";
  private static final String DCC_EX = "dcc-ex";
  private static final String HSI_S88 = "hsi-s88";

  private DecoderController controller;

  /**
   * Creates new form CommandStationDialog1
   */
  public CommandStationDialog1(java.awt.Frame parent, boolean modal) {
    super(parent, modal);
    initComponents();
    this.executor = Executors.newSingleThreadExecutor();
    if (PersistenceFactory.getService() != null) {
      initModels(null);
    }

  }

  private void initModels(CommandStationBean selected) {
    if (selected == null) {
      selectedCommandStation = PersistenceFactory.getService().getDefaultCommandStation();
    } else {
      selectedCommandStation = selected;
    }

    if (!selectedCommandStation.isFeedbackSupport()) {
      selectedFeedbackProvider = PersistenceFactory.getService().getEnabledFeedbackProvider();
    }

    List<CommandStationBean> allCommandStations = PersistenceFactory.getService().getCommandStations();

    List<CommandStationBean> commandStations = new ArrayList<>();
    List<CommandStationBean> feedbackProviders = new ArrayList<>();

    for (CommandStationBean csb : allCommandStations) {
      if (csb.isDecoderControlSupport()) {
        commandStations.add(csb);
      } else {
        feedbackProviders.add(csb);
      }
    }

    emptyCS = new CommandStationBean();
    emptyCS.setDecoderControlSupport(true);
    emptyFB = new CommandStationBean();
    emptyFB.setFeedbackSupport(true);

    commandStations.add(emptyCS);
    feedbackProviders.add(emptyFB);

    if (selectedCommandStation == null) {
      selectedCommandStation = emptyCS;
    }

    if (selectedFeedbackProvider == null) {
      selectedFeedbackProvider = emptyFB;
    }

    CommandStationBean[] csba = new CommandStationBean[commandStations.size()];
    CommandStationBean[] fbpa = new CommandStationBean[feedbackProviders.size()];
    commandStations.toArray(csba);
    feedbackProviders.toArray(fbpa);

    commandStationCBM = new DefaultComboBoxModel<>(csba);
    commandStationCBM.setSelectedItem(selectedCommandStation);
    commandStationCB.setModel(commandStationCBM);

    feedbackCBM = new DefaultComboBoxModel<>(fbpa);
    feedbackCBM.setSelectedItem(selectedFeedbackProvider);
    feedbackCB.setModel(feedbackCBM);

    SerialPort comPorts[] = SerialPort.getCommPorts();
    String[] ports = new String[comPorts.length];
    for (int i = 0; i < comPorts.length; i++) {
      ports[i] = comPorts[i].getSystemPortName();
    }

    serialPortCBM = new DefaultComboBoxModel<>(ports);
    fbpSerialPortCBM = new DefaultComboBoxModel<>(ports);
    serialCB.setModel(serialPortCBM);
    fbpSerialCB.setModel(fbpSerialPortCBM);

    if (CommandStationBean.ConnectionType.SERIAL == selectedCommandStation.getConnectionType()) {
      String port = selectedCommandStation.getSerialPort();
      serialCB.setSelectedItem(port);
      serialRB.setSelected(true);
    } else {
      networkRB.setSelected(true);
    }

    if (selectedFeedbackProvider.getConnectionType() != null && CommandStationBean.ConnectionType.SERIAL == selectedFeedbackProvider.getConnectionType()) {
      String port = selectedFeedbackProvider.getSerialPort();
      fbpSerialCB.setSelectedItem(port);
    }

    setComponents();
    if (CommandStationBean.ConnectionType.NETWORK == selectedCommandStation.getConnectionType() && selectedCommandStation.getIpAddress() != null && selectedCommandStation.getIpAddress().length() > 8) {
      executor.execute(() -> checkConnection(selectedCommandStation));
    }

    //enableFields(false);
    //this.progressBar.setVisible(false);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    connectionTypeBG = new javax.swing.ButtonGroup();
    topPanel = new javax.swing.JPanel();
    mainCSPanel = new javax.swing.JPanel();
    commandStationLbl = new javax.swing.JLabel();
    commandStationCB = new javax.swing.JComboBox<>();
    ipOrPortLbl = new javax.swing.JLabel();
    ipTF = new javax.swing.JTextField();
    serialCB = new javax.swing.JComboBox<>();
    controllerLbl = new javax.swing.JLabel();
    accessoryControllerLbl = new javax.swing.JLabel();
    feedbackProviderLbl = new javax.swing.JLabel();
    discoverBtn = new javax.swing.JButton();
    checkBtn = new javax.swing.JButton();
    networkRB = new javax.swing.JRadioButton();
    serialRB = new javax.swing.JRadioButton();
    feedbackCSPanel = new javax.swing.JPanel();
    feedbackLbl = new javax.swing.JLabel();
    feedbackCB = new javax.swing.JComboBox<>();
    secondfbpLbl = new javax.swing.JLabel();
    fbpSerialLbl = new javax.swing.JLabel();
    fbpSerialCB = new javax.swing.JComboBox<>();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 23), new java.awt.Dimension(0, 23), new java.awt.Dimension(32767, 23));
    jPanel3 = new javax.swing.JPanel();
    jPanel4 = new javax.swing.JPanel();
    connectBtn = new javax.swing.JButton();
    jPanel5 = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    jTree1 = new javax.swing.JTree();
    jPanel2 = new javax.swing.JPanel();
    jPanel1 = new javax.swing.JPanel();
    controllerPanel = new javax.swing.JPanel();
    connectedToLbl = new javax.swing.JLabel();
    serialLbl = new javax.swing.JLabel();
    swVersionLbl = new javax.swing.JLabel();
    hwVersionLbl = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

    jcs.ui.swing.layout.VerticalFlowLayout verticalFlowLayout1 = new jcs.ui.swing.layout.VerticalFlowLayout();
    verticalFlowLayout1.sethAlignment(0);
    topPanel.setLayout(verticalFlowLayout1);

    mainCSPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    commandStationLbl.setLabelFor(commandStationCB);
    commandStationLbl.setText("Command Station");
    commandStationLbl.setPreferredSize(new java.awt.Dimension(110, 17));
    mainCSPanel.add(commandStationLbl);

    commandStationCB.setPreferredSize(new java.awt.Dimension(200, 23));
    commandStationCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        commandStationCBActionPerformed(evt);
      }
    });
    mainCSPanel.add(commandStationCB);

    ipOrPortLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    ipOrPortLbl.setLabelFor(ipTF);
    ipOrPortLbl.setText("ip Address:");
    ipOrPortLbl.setPreferredSize(new java.awt.Dimension(105, 17));
    mainCSPanel.add(ipOrPortLbl);

    ipTF.setPreferredSize(new java.awt.Dimension(120, 23));
    ipTF.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusLost(java.awt.event.FocusEvent evt) {
        ipTFFocusLost(evt);
      }
    });
    ipTF.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseExited(java.awt.event.MouseEvent evt) {
        ipTFMouseExited(evt);
      }
    });
    ipTF.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        ipTFActionPerformed(evt);
      }
    });
    mainCSPanel.add(ipTF);

    serialCB.setPreferredSize(new java.awt.Dimension(120, 23));
    serialCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        serialCBActionPerformed(evt);
      }
    });
    mainCSPanel.add(serialCB);

    controllerLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/controller-console-24.png"))); // NOI18N
    controllerLbl.setToolTipText("Decoder Controller");
    mainCSPanel.add(controllerLbl);

    accessoryControllerLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/branch-24.png"))); // NOI18N
    accessoryControllerLbl.setToolTipText("Accessory Controller");
    mainCSPanel.add(accessoryControllerLbl);

    feedbackProviderLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/chat-24.png"))); // NOI18N
    feedbackProviderLbl.setToolTipText("Feedback Provider");
    mainCSPanel.add(feedbackProviderLbl);

    discoverBtn.setText("Discover");
    discoverBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        discoverBtnActionPerformed(evt);
      }
    });
    mainCSPanel.add(discoverBtn);

    checkBtn.setText("Check");
    checkBtn.setToolTipText("Check Connection");
    checkBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkBtnActionPerformed(evt);
      }
    });
    mainCSPanel.add(checkBtn);

    connectionTypeBG.add(networkRB);
    networkRB.setText("Network");
    networkRB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        networkRBActionPerformed(evt);
      }
    });
    mainCSPanel.add(networkRB);

    connectionTypeBG.add(serialRB);
    serialRB.setText("Serialport");
    serialRB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        serialRBActionPerformed(evt);
      }
    });
    mainCSPanel.add(serialRB);

    topPanel.add(mainCSPanel);

    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    feedbackCSPanel.setLayout(flowLayout1);

    feedbackLbl.setLabelFor(feedbackCB);
    feedbackLbl.setText("Feedback Device");
    feedbackLbl.setPreferredSize(new java.awt.Dimension(110, 17));
    feedbackCSPanel.add(feedbackLbl);

    feedbackCB.setPreferredSize(new java.awt.Dimension(200, 23));
    feedbackCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        feedbackCBActionPerformed(evt);
      }
    });
    feedbackCSPanel.add(feedbackCB);

    secondfbpLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/chat-24.png"))); // NOI18N
    secondfbpLbl.setToolTipText("Feedback Provicer");
    secondfbpLbl.setPreferredSize(new java.awt.Dimension(25, 24));
    feedbackCSPanel.add(secondfbpLbl);

    fbpSerialLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    fbpSerialLbl.setText("Serial Port:");
    fbpSerialLbl.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    fbpSerialLbl.setPreferredSize(new java.awt.Dimension(75, 17));
    feedbackCSPanel.add(fbpSerialLbl);

    fbpSerialCB.setPreferredSize(new java.awt.Dimension(120, 23));
    fbpSerialCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        fbpSerialCBActionPerformed(evt);
      }
    });
    feedbackCSPanel.add(fbpSerialCB);
    feedbackCSPanel.add(filler1);

    topPanel.add(feedbackCSPanel);

    getContentPane().add(topPanel, java.awt.BorderLayout.PAGE_START);

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 1113, Short.MAX_VALUE)
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 100, Short.MAX_VALUE)
    );

    getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_END);

    connectBtn.setText("Connect");
    connectBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        connectBtnActionPerformed(evt);
      }
    });
    jPanel4.add(connectBtn);

    getContentPane().add(jPanel4, java.awt.BorderLayout.LINE_END);

    jPanel5.setPreferredSize(new java.awt.Dimension(200, 400));
    jPanel5.setLayout(new java.awt.BorderLayout());

    jScrollPane1.setViewportView(jTree1);

    jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    getContentPane().add(jPanel5, java.awt.BorderLayout.LINE_START);

    jPanel2.setLayout(new java.awt.BorderLayout());

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 825, Short.MAX_VALUE)
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 390, Short.MAX_VALUE)
    );

    jPanel2.add(jPanel1, java.awt.BorderLayout.CENTER);

    java.awt.FlowLayout flowLayout2 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout2.setAlignOnBaseline(true);
    controllerPanel.setLayout(flowLayout2);

    connectedToLbl.setText("Connected to: command station");
    connectedToLbl.setPreferredSize(new java.awt.Dimension(200, 17));
    controllerPanel.add(connectedToLbl);

    serialLbl.setText("Serial: xxxxxx");
    serialLbl.setPreferredSize(new java.awt.Dimension(150, 17));
    controllerPanel.add(serialLbl);

    swVersionLbl.setText("Software version: xxxxxxx");
    swVersionLbl.setPreferredSize(new java.awt.Dimension(160, 17));
    controllerPanel.add(swVersionLbl);

    hwVersionLbl.setText("Hardware version: xxxxxx");
    hwVersionLbl.setPreferredSize(new java.awt.Dimension(160, 17));
    controllerPanel.add(hwVersionLbl);

    jPanel2.add(controllerPanel, java.awt.BorderLayout.PAGE_START);

    getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void commandStationCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commandStationCBActionPerformed
    CommandStationBean newSelectedCommandStation = (CommandStationBean) commandStationCBM.getSelectedItem();

    if (!selectedCommandStation.getId().equals(newSelectedCommandStation.getId())) {
      try {
        if (JCS.getJcsCommandStation() != null) {
          JCS.getJcsCommandStation().switchPower(false);
        }
        if (JCS.getParentFrame() != null) {
          JCS.getParentFrame().connect(false);
        }
      } catch (Exception e) {
        Logger.error(e.getMessage());
      }
    }

    selectedCommandStation = (CommandStationBean) commandStationCBM.getSelectedItem();
    executor.execute(() -> changeDefaultCommandStation(selectedCommandStation));
    selectedCommandStation.setEnabled(true);

    setComponents();
    //this.enableFields(this.enableEditCB.isSelected());
    Logger.trace("Selected CS: " + this.selectedCommandStation.getDescription());
  }//GEN-LAST:event_commandStationCBActionPerformed

  private void setComponents() {
    controllerLbl.setVisible(selectedCommandStation.isDecoderControlSupport());
    accessoryControllerLbl.setVisible(selectedCommandStation.isAccessoryControlSupport());
    feedbackProviderLbl.setVisible(selectedCommandStation.isFeedbackSupport());

    discoverBtn.setVisible(selectedCommandStation.isIpAutoConfiguration());
    ipTF.setText(selectedCommandStation.getIpAddress());

    serialCB.setVisible(CommandStationBean.ConnectionType.SERIAL == selectedCommandStation.getConnectionType());
    ipTF.setVisible(CommandStationBean.ConnectionType.NETWORK == selectedCommandStation.getConnectionType());

    networkRB.setVisible(selectedCommandStation.getSupportedConnectionTypes().size() > 1);
    serialRB.setVisible(selectedCommandStation.getSupportedConnectionTypes().size() > 1);
    networkRB.setSelected(CommandStationBean.ConnectionType.NETWORK == selectedCommandStation.getConnectionType());

    if (serialCB.isVisible()) {
      ipOrPortLbl.setText("Serial Port:");
    } else {
      ipOrPortLbl.setText("ip Address:");
    }

    checkBtn.setVisible(CommandStationBean.ConnectionType.NETWORK == selectedCommandStation.getConnectionType() && selectedCommandStation.getIpAddress() != null && selectedCommandStation.getIpAddress().length() > 8);

    if (selectedCommandStation.getIpAddress() == null || selectedCommandStation.getIpAddress().length() > 8) {
      ipTF.setBackground(new java.awt.Color(255, 255, 255));
      connectBtn.setEnabled(true);
    } else {
      connectBtn.setEnabled(false);
    }

    //no main controller feedback support, enable the secondary
    feedbackCB.setVisible(!selectedCommandStation.isFeedbackSupport());
    feedbackCB.setEnabled(!selectedCommandStation.isFeedbackSupport());
    feedbackLbl.setVisible(!selectedCommandStation.isFeedbackSupport());

    secondfbpLbl.setVisible(!selectedCommandStation.isFeedbackSupport() && selectedFeedbackProvider.isFeedbackSupport() && selectedFeedbackProvider.getId() != null);

    fbpSerialCB.setVisible(!selectedCommandStation.isFeedbackSupport());
    fbpSerialCB.setEnabled(!selectedCommandStation.isFeedbackSupport());
    fbpSerialLbl.setVisible(!selectedCommandStation.isFeedbackSupport());

    if (controller != null && controller.isConnected()) {
      InfoBean ib = controller.getCommandStationInfo();
      connectedToLbl.setText("Connected to : " + ib.getProductName());
      connectedToLbl.setVisible(true);

      serialLbl.setText("Serial: " + ib.getSerialNumber());
      serialLbl.setVisible(true);

      if (ib.getSoftwareVersion() != null) {
        swVersionLbl.setText("Software version: " + ib.getSoftwareVersion());
        swVersionLbl.setVisible(true);
      } else {
        swVersionLbl.setVisible(false);
      }

      if (ib.getHardwareVersion() != null) {
        hwVersionLbl.setText(("Hardware version: " + ib.getHardwareVersion()));
        hwVersionLbl.setVisible(true);
      } else {
        hwVersionLbl.setVisible(false);
      }

      connectBtn.setText("Disconnect");
    } else {
      connectedToLbl.setVisible(false);
      serialLbl.setVisible(false);
      swVersionLbl.setVisible(false);
      hwVersionLbl.setVisible(false);
      connectBtn.setText("Connect");
    }
  }

  private void changeDefaultCommandStation(final CommandStationBean newDefault) {
    PersistenceFactory.getService().changeDefaultCommandStation(newDefault);
    java.awt.EventQueue.invokeLater(() -> {
      setComponents();
    });
  }

  private void persistCommandStation(final CommandStationBean commandStation) {
    PersistenceFactory.getService().persist(commandStation);
    java.awt.EventQueue.invokeLater(() -> {
      setComponents();
    });
  }

  private void discoverBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discoverBtnActionPerformed
    Logger.trace("Try to discover " + selectedCommandStation.getDescription());
    executor.execute(() -> discover(selectedCommandStation));
  }//GEN-LAST:event_discoverBtnActionPerformed

  private void feedbackCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_feedbackCBActionPerformed
    CommandStationBean newSelectedFeedbackProvider = (CommandStationBean) feedbackCBM.getSelectedItem();
    //Check if it is not the empty one
    if (feedbackCB.isEnabled()) {
      if (selectedFeedbackProvider != null && selectedFeedbackProvider.getId() != null && newSelectedFeedbackProvider.getId() == null) {
        //Disable the curren selected provider
        selectedFeedbackProvider.setEnabled(false);
        PersistenceFactory.getService().persist(selectedFeedbackProvider);
        selectedFeedbackProvider = newSelectedFeedbackProvider;
      } else {
        selectedFeedbackProvider = newSelectedFeedbackProvider;
        selectedFeedbackProvider.setEnabled(newSelectedFeedbackProvider.getId() != null);
        //Persist the change
        PersistenceFactory.getService().persist(selectedFeedbackProvider);
      }
      secondfbpLbl.setVisible(selectedFeedbackProvider.isFeedbackSupport() && selectedFeedbackProvider.getId() != null);
    }
  }//GEN-LAST:event_feedbackCBActionPerformed

  private void serialCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serialCBActionPerformed
    String selectedPort = (String) serialCB.getSelectedItem();
    selectedCommandStation.setSerialPort(selectedPort);
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_serialCBActionPerformed

  private void fbpSerialCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fbpSerialCBActionPerformed
    String selectedPort = (String) fbpSerialCB.getSelectedItem();
    selectedFeedbackProvider.setSerialPort(selectedPort);
    persistCommandStation(selectedFeedbackProvider);
  }//GEN-LAST:event_fbpSerialCBActionPerformed

  private void networkRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_networkRBActionPerformed
    if (networkRB.isSelected()) {
      selectedCommandStation.setConnectionType(CommandStationBean.ConnectionType.NETWORK);
      selectedCommandStation.setSerialPort(null);
    } else {
      selectedCommandStation.setConnectionType(CommandStationBean.ConnectionType.SERIAL);
    }
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_networkRBActionPerformed

  private void serialRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serialRBActionPerformed
    if (networkRB.isSelected()) {
      selectedCommandStation.setConnectionType(CommandStationBean.ConnectionType.NETWORK);
    } else {
      selectedCommandStation.setConnectionType(CommandStationBean.ConnectionType.SERIAL);
    }
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_serialRBActionPerformed

  private void ipTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipTFActionPerformed
    Logger.trace("ip Address: " + this.ipTF.getText());
    selectedCommandStation.setIpAddress(ipTF.getText());
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_ipTFActionPerformed

  private void ipTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ipTFFocusLost
    Logger.trace("ip Address: " + this.ipTF.getText());
    selectedCommandStation.setIpAddress(ipTF.getText());
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_ipTFFocusLost

  private void ipTFMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ipTFMouseExited
    Logger.trace("ip Address: " + this.ipTF.getText());
    selectedCommandStation.setIpAddress(ipTF.getText());
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_ipTFMouseExited

  private void connectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectBtnActionPerformed
    Logger.trace("Try to connect to " + selectedCommandStation.getDescription());

    if ("Connect".equals(connectBtn.getText())) {
      executor.execute(() -> connect(selectedCommandStation));
    } else {
      executor.execute(() -> disconnect());
    }
  }//GEN-LAST:event_connectBtnActionPerformed

  private void checkBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBtnActionPerformed
    executor.execute(() -> checkConnection(selectedCommandStation));
  }//GEN-LAST:event_checkBtnActionPerformed

  private InetAddress discover(final CommandStationBean commandStation) {
    final JOptionPane optionPane = new JOptionPane("Try to discovering a " + commandStation.getDescription(),
            JOptionPane.INFORMATION_MESSAGE,
            JOptionPane.DEFAULT_OPTION);

    final JDialog discoverDialog = new JDialog(this, "Discovering...");
    discoverDialog.setContentPane(optionPane);
    discoverDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    discoverDialog.pack();
    discoverDialog.setLocationRelativeTo(null);
    discoverDialog.setVisible(true);

    InetAddress inetAddress = null;
    if (MARKLIN_CS.equals(commandStation.getId())) {
      inetAddress = CSConnectionFactory.discoverCs();
    } else if (ESU_ECOS.equals(commandStation.getId())) {
      inetAddress = EcosConnectionFactory.discoverEcos();
    }

    if (inetAddress != null) {
      Logger.trace("Discovered host " + inetAddress.getHostAddress() + " for " + commandStation.getDescription());
      commandStation.setIpAddress(inetAddress.getHostAddress());
      persistCommandStation(commandStation);
    }

    java.awt.EventQueue.invokeLater(() -> {
      discoverDialog.setVisible(false);
      discoverDialog.dispose();
    });

    return inetAddress;
  }

  private void checkConnection(final CommandStationBean commandStation) {
    String ip = commandStation.getIpAddress();
    boolean canConnect = Ping.IsReachable(ip);

    java.awt.EventQueue.invokeLater(() -> {
      if (canConnect) {
        ipTF.setBackground(new java.awt.Color(204, 255, 204));
        connectBtn.setEnabled(true);
      } else {
        ipTF.setBackground(new java.awt.Color(255, 255, 255));
        connectBtn.setEnabled(false);
        JOptionPane.showMessageDialog(this, "Can't connect with host " + ip, "Can't Connect", JOptionPane.WARNING_MESSAGE);
      }
    });
  }

  private void disconnect() {
    if (controller != null) {
      controller.disconnect();
      controller = null;

      java.awt.EventQueue.invokeLater(() -> {
        setComponents();
      });

    }
  }

  private void connect(final CommandStationBean commandStation) {
    final JOptionPane optionPane = new JOptionPane("Try to connect to " + commandStation.getDescription(),
            JOptionPane.INFORMATION_MESSAGE,
            JOptionPane.DEFAULT_OPTION);

    final JDialog connectingDialog = new JDialog(this, "Connecting...");
    connectingDialog.setContentPane(optionPane);
    connectingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    connectingDialog.pack();
    connectingDialog.setLocationRelativeTo(null);
    connectingDialog.setVisible(true);

    if (MARKLIN_CS.equals(commandStation.getId())) {
      controller = new MarklinCentralStationImpl(commandStation);
    } else if (ESU_ECOS.equals(commandStation.getId())) {
      controller = new EsuEcosCommandStationImpl(commandStation);
    } else if (DCC_EX.equals(commandStation.getId())) {
      Logger.info("TODO: DCC-EX!");
    } else if (HSI_S88.equals(commandStation.getId())) {
      Logger.info("TODO: HSI-S88!");
    } else {
      Logger.trace("Unknown Controller!");
    }

    if (controller == null) {
      return;
    }

    controller.connect();
    if (controller.isConnected()) {
      //Obtain some info from the controller
      Logger.trace("Connected to " + controller.getCommandStationInfo());

      java.awt.EventQueue.invokeLater(() -> {
        setComponents();
      });
    }

    java.awt.EventQueue.invokeLater(() -> {
      connectingDialog.setVisible(false);
      connectingDialog.dispose();
    });

    //} catch (UnknownHostException ex) {
    //  Logger.error("Unknown host " + commandStation.getIpAddress());
    //  return false;
    //}
  }

  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);

    if (!b && this.controller != null) {
      if (controller.isConnected()) {
        controller.disconnect();
      }
      controller = null;
      Logger.trace("Disconnected from " + selectedCommandStation.getId());
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    /* Set the FlatLightLaf look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.warn("Can't set the LookAndFeel: " + ex);
    }
    //</editor-fold>

    /* Create and display the dialog */
    java.awt.EventQueue.invokeLater(() -> {
      CommandStationDialog1 dialog = new CommandStationDialog1(new javax.swing.JFrame(), true);
      dialog.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
          dialog.setVisible(false);
          System.exit(0);
        }
      });

      dialog.pack();
      dialog.setLocationRelativeTo(null);
      dialog.setVisible(true);

    });
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel accessoryControllerLbl;
  private javax.swing.JButton checkBtn;
  private javax.swing.JComboBox<CommandStationBean> commandStationCB;
  private javax.swing.JLabel commandStationLbl;
  private javax.swing.JButton connectBtn;
  private javax.swing.JLabel connectedToLbl;
  private javax.swing.ButtonGroup connectionTypeBG;
  private javax.swing.JLabel controllerLbl;
  private javax.swing.JPanel controllerPanel;
  private javax.swing.JButton discoverBtn;
  private javax.swing.JComboBox<String> fbpSerialCB;
  private javax.swing.JLabel fbpSerialLbl;
  private javax.swing.JComboBox<CommandStationBean> feedbackCB;
  private javax.swing.JPanel feedbackCSPanel;
  private javax.swing.JLabel feedbackLbl;
  private javax.swing.JLabel feedbackProviderLbl;
  private javax.swing.Box.Filler filler1;
  private javax.swing.JLabel hwVersionLbl;
  private javax.swing.JLabel ipOrPortLbl;
  private javax.swing.JTextField ipTF;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTree jTree1;
  private javax.swing.JPanel mainCSPanel;
  private javax.swing.JRadioButton networkRB;
  private javax.swing.JLabel secondfbpLbl;
  private javax.swing.JComboBox<String> serialCB;
  private javax.swing.JLabel serialLbl;
  private javax.swing.JRadioButton serialRB;
  private javax.swing.JLabel swVersionLbl;
  private javax.swing.JPanel topPanel;
  // End of variables declaration//GEN-END:variables

}
