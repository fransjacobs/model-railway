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
import javax.swing.JDialog;
import javax.swing.JFrame;
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
import jcs.commandStation.FeedbackController;
import jcs.commandStation.entities.Device;
import jcs.commandStation.entities.FeedbackModule;
import jcs.commandStation.entities.InfoBean;
import jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl;
import jcs.commandStation.esu.ecos.net.EcosConnectionFactory;
import jcs.commandStation.marklin.cs.MarklinCentralStationImpl;
import jcs.commandStation.marklin.cs.net.CSConnectionFactory;
import jcs.entities.CommandStationBean;
import static jcs.entities.CommandStationBean.DCC_EX;
import static jcs.entities.CommandStationBean.ESU_ECOS;
import static jcs.entities.CommandStationBean.HSI_S88;
import static jcs.entities.CommandStationBean.MARKLIN_CS;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.JCSFrame;
import jcs.ui.swing.layout.VerticalFlowLayout;
import jcs.util.Ping;
import org.tinylog.Logger;

/**
 *
 * @author frans
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

  private DecoderController controller;

  public CommandStationPanel() {
    initComponents();
    executor = Executors.newSingleThreadExecutor();
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

    if (selectedCommandStation != null && !selectedCommandStation.isFeedbackSupport()) {
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
    if (!selectedCommandStation.isVirtual() && CommandStationBean.ConnectionType.NETWORK == selectedCommandStation.getConnectionType() && selectedCommandStation.getIpAddress() != null && selectedCommandStation.getIpAddress().length() > 8) {
      executor.execute(() -> checkConnection(selectedCommandStation));
    }
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
    jPanel3 = new JPanel();
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
    filler2 = new Box.Filler(new Dimension(100, 0), new Dimension(100, 0), new Dimension(100, 32767));
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

    discoverBtn.setText("Discover");
    discoverBtn.setName("discoverBtn"); // NOI18N
    discoverBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        discoverBtnActionPerformed(evt);
      }
    });
    mainCSPanel.add(discoverBtn);

    checkBtn.setText("Check");
    checkBtn.setToolTipText("Check Connection");
    checkBtn.setName("checkBtn"); // NOI18N
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

    jPanel3.setName("jPanel3"); // NOI18N

    GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGap(0, 1220, Short.MAX_VALUE)
    );
    jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGap(0, 100, Short.MAX_VALUE)
    );

    add(jPanel3, BorderLayout.PAGE_END);

    jPanel4.setName("jPanel4"); // NOI18N

    connectBtn.setText("Connect");
    connectBtn.setName("connectBtn"); // NOI18N
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
    FlowLayout flowLayout16 = new FlowLayout(FlowLayout.LEFT);
    flowLayout16.setAlignOnBaseline(true);
    feedbackModulesPanel.setLayout(flowLayout16);

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

    filler2.setName("filler2"); // NOI18N
    feedbackModulesPanel.add(filler2);

    feedbackPanel.add(feedbackModulesPanel);

    updatePanel.setName("updatePanel"); // NOI18N
    updatePanel.setPreferredSize(new Dimension(350, 57));
    FlowLayout flowLayout17 = new FlowLayout(FlowLayout.RIGHT);
    flowLayout17.setAlignOnBaseline(true);
    updatePanel.setLayout(flowLayout17);

    updateBtn.setText("Update");
    updateBtn.setName("updateBtn"); // NOI18N
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
      .addGap(0, 317, Short.MAX_VALUE)
    );

    jPanel2.add(rightPanel, BorderLayout.EAST);

    devicesPanel.setBorder(BorderFactory.createTitledBorder("Devices"));
    devicesPanel.setName("devicesPanel"); // NOI18N
    devicesPanel.setLayout(new GridLayout(1, 1));

    devicesSP.setName("devicesSP"); // NOI18N

    devicesTree.setName("devicesTree"); // NOI18N
    devicesSP.setViewportView(devicesTree);

    devicesPanel.add(devicesSP);

    jPanel2.add(devicesPanel, BorderLayout.CENTER);

    add(jPanel2, BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void setComponents() {
    controllerLbl.setVisible(selectedCommandStation.isDecoderControlSupport());
    accessoryControllerLbl.setVisible(selectedCommandStation.isAccessoryControlSupport());
    feedbackProviderLbl.setVisible(selectedCommandStation.isFeedbackSupport());

    virtualCB.setSelected(selectedCommandStation.isVirtual());

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

      //feedback settings
      List<FeedbackModule> modules = ((FeedbackController) controller).getFeedbackModules();

      mainLbl.setVisible(true);
      mainSpinner.setVisible(true);

      updateBtn.setVisible(true);

      for (FeedbackModule fbm : modules) {
        Integer busNr = fbm.getBusNumber();
        if (busNr == null) {
          //Assume null is the main
          busNr = 0;
        }
        switch (busNr) {
          case 1 -> {
            bus1Spinner.setValue(fbm.getBusSize());
          }
          case 2 -> {
            bus2Spinner.setValue(fbm.getBusSize());
          }
          case 3 -> {
            bus3Spinner.setValue(fbm.getBusSize());
          }
          default -> {
            mainSpinner.setValue(fbm.getBusSize());
          }
        }
      }

      if (selectedCommandStation.getId().equals(CommandStationBean.MARKLIN_CS)) {
        bus1Lbl.setVisible(true);
        bus1Spinner.setVisible(true);
        bus2Lbl.setVisible(true);
        bus2Spinner.setVisible(true);
        bus3Lbl.setVisible(true);
        bus3Spinner.setVisible(true);
      } else {
        bus1Lbl.setVisible(false);
        bus1Spinner.setVisible(false);
        bus2Lbl.setVisible(false);
        bus2Spinner.setVisible(false);
        bus3Lbl.setVisible(false);
        bus3Spinner.setVisible(false);
      }

      //Command Stations Marklin CS and ESU-ECoS require that the setting 
      //for the number of modules is done on the commandstation it self, hence disable the spinners
      if (selectedCommandStation.getId().equals(MARKLIN_CS) || selectedCommandStation.getId().equals(ESU_ECOS)) {
        mainSpinner.setEnabled(false);
        bus1Spinner.setEnabled(false);
        bus2Spinner.setEnabled(false);
        bus3Spinner.setEnabled(false);
      } else {
        mainSpinner.setEnabled(true);
        bus1Spinner.setEnabled(true);
        bus2Spinner.setEnabled(true);
        bus3Spinner.setEnabled(true);
      }
    } else {
      connectedToLbl.setVisible(false);
      serialLbl.setVisible(false);
      swVersionLbl.setVisible(false);
      hwVersionLbl.setVisible(false);
      connectBtn.setText("Connect");

      //Feedback modules
      mainLbl.setVisible(false);
      mainSpinner.setVisible(false);

      bus1Lbl.setVisible(false);
      bus1Spinner.setVisible(false);
      bus2Lbl.setVisible(false);
      bus2Spinner.setVisible(false);
      bus3Lbl.setVisible(false);
      bus3Spinner.setVisible(false);
      updateBtn.setVisible(false);
    }

    buildTree();
  }

  private void buildTree() {
    Logger.trace("build tree");
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
    Logger.trace("Create feedback nodes");
    if (controller == null) {
      return;
    }

    List<Device> devices = controller.getDevices();

    for (Device d : devices) {
      DefaultMutableTreeNode deviceNode = new DefaultMutableTreeNode(d.getId() + " " + d.getName());

      if (d.isFeedback()) {
        List<FeedbackModule> modules = ((FeedbackController) controller).getFeedbackModules();
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

  //TODO
  public void valueChanged(TreeSelectionEvent e) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) devicesTree.getLastSelectedPathComponent();

    if (node == null) {
      return;
    }

    Object nodeInfo = node.getUserObject();

    if (node.isLeaf()) {

    } else {
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

  private void commandStationCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_commandStationCBActionPerformed
    CommandStationBean newSelectedCommandStation = (CommandStationBean) commandStationCBM.getSelectedItem();
    if (selectedCommandStation != null && selectedCommandStation.getId() != null && !selectedCommandStation.getId().equals(newSelectedCommandStation.getId())) {
      selectedCommandStation.setDefault(false);
      selectedCommandStation.setEnabled(false);
      try {
        if (JCS.getJcsCommandStation() != null && JCS.getJcsCommandStation().isConnected()) {
          JCS.getJcsCommandStation().switchPower(false);
        }
        if (JCS.getParentFrame() != null) {
          JCS.getParentFrame().connect(false);
        }
      } catch (Exception e) {
        Logger.error(e.getMessage());
      }
    } else {
      selectedCommandStation = newSelectedCommandStation;
    }

    selectedCommandStation = (CommandStationBean) commandStationCBM.getSelectedItem();
    selectedCommandStation.setEnabled(true);
    selectedCommandStation.setDefault(true);
    executor.execute(() -> changeDefaultCommandStation(selectedCommandStation));

    Logger.trace("Selected CS: " + selectedCommandStation.getDescription());
  }//GEN-LAST:event_commandStationCBActionPerformed

  private void virtualCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_virtualCBActionPerformed
    selectedCommandStation.setVirtual(virtualCB.isSelected());
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_virtualCBActionPerformed

  private void ipTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_ipTFFocusLost
    Logger.trace("ip Address: " + this.ipTF.getText());
    selectedCommandStation.setIpAddress(ipTF.getText());
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_ipTFFocusLost

  private void ipTFMouseExited(MouseEvent evt) {//GEN-FIRST:event_ipTFMouseExited
    Logger.trace("ip Address: " + this.ipTF.getText());
    selectedCommandStation.setIpAddress(ipTF.getText());
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_ipTFMouseExited

  private void ipTFActionPerformed(ActionEvent evt) {//GEN-FIRST:event_ipTFActionPerformed
    Logger.trace("ip Address: " + this.ipTF.getText());
    selectedCommandStation.setIpAddress(ipTF.getText());
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_ipTFActionPerformed

  private void serialCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_serialCBActionPerformed
    String selectedPort = (String) serialCB.getSelectedItem();
    selectedCommandStation.setSerialPort(selectedPort);
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_serialCBActionPerformed

  private void discoverBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_discoverBtnActionPerformed
    Logger.trace("Try to discover " + selectedCommandStation.getDescription());
    executor.execute(() -> discover(selectedCommandStation));
  }//GEN-LAST:event_discoverBtnActionPerformed

  private void checkBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_checkBtnActionPerformed
    executor.execute(() -> checkConnection(selectedCommandStation));
  }//GEN-LAST:event_checkBtnActionPerformed

  private void networkRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_networkRBActionPerformed
    if (networkRB.isSelected()) {
      selectedCommandStation.setConnectionType(CommandStationBean.ConnectionType.NETWORK);
      selectedCommandStation.setSerialPort(null);
    } else {
      selectedCommandStation.setConnectionType(CommandStationBean.ConnectionType.SERIAL);
    }
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_networkRBActionPerformed

  private void serialRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_serialRBActionPerformed
    if (networkRB.isSelected()) {
      selectedCommandStation.setConnectionType(CommandStationBean.ConnectionType.NETWORK);
    } else {
      selectedCommandStation.setConnectionType(CommandStationBean.ConnectionType.SERIAL);
    }
    persistCommandStation(selectedCommandStation);
  }//GEN-LAST:event_serialRBActionPerformed

  private void feedbackCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_feedbackCBActionPerformed
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

  private void fbpSerialCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_fbpSerialCBActionPerformed
    String selectedPort = (String) fbpSerialCB.getSelectedItem();
    selectedFeedbackProvider.setSerialPort(selectedPort);
    persistCommandStation(selectedFeedbackProvider);
  }//GEN-LAST:event_fbpSerialCBActionPerformed

  private void connectBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_connectBtnActionPerformed
    Logger.trace("Try to connect to " + selectedCommandStation.getDescription());

    if ("Connect".equals(connectBtn.getText())) {
      executor.execute(() -> connect(selectedCommandStation));
    } else {
      executor.execute(() -> disconnect());
    }
  }//GEN-LAST:event_connectBtnActionPerformed

  private void updateBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
    executor.execute(() -> {
      updateBtn.setEnabled(false);
      updateSensors();
    });
  }//GEN-LAST:event_updateBtnActionPerformed

  private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
    Logger.trace("Shown");
  }//GEN-LAST:event_formComponentShown

  private void formComponentHidden(ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
    Logger.trace("Hidden");
  }//GEN-LAST:event_formComponentHidden

  @Override
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
    Logger.trace("Shown " + aFlag);

    if (!aFlag && this.controller != null) {
      if (controller.isConnected()) {
        controller.disconnect();
      }
      controller = null;
      Logger.trace("Disconnected from " + selectedCommandStation.getId());
    }

  }

  private void updateSensors() {
    List<FeedbackModule> modules = ((FeedbackController) controller).getFeedbackModules();

    Logger.trace("There are " + modules.size() + " feedback modules");
    //Catch errors if any...
    try {
      for (FeedbackModule fbm : modules) {
        List<SensorBean> sensors = fbm.getSensors();
        for (SensorBean sb : sensors) {
          Logger.trace("Storing : " + sb);
          PersistenceFactory.getService().persist(sb);
        }
      }
    } catch (Exception e) {
      Logger.error("Error updating sensors! " + e);
    }

    java.awt.EventQueue.invokeLater(() -> {
      setComponents();
      updateBtn.setEnabled(true);
    });
  }

  private List<FeedbackModule> getFeedbackModules(String commandStationId) {
    List<SensorBean> sensors = PersistenceFactory.getService().getSensorsByCommandStationId(commandStationId);

    List<FeedbackModule> modules = new ArrayList<>();
    if (!sensors.isEmpty()) {
      Integer id = -1;
      for (SensorBean sb : sensors) {
        if (!id.equals(sb.getDeviceId())) {
          FeedbackModule fbm = new FeedbackModule();
          fbm.setId(sb.getDeviceId());
          fbm.setIdentifier(sb.getNodeId());
          //The busnumber and address offset depend on the commandstation id 
          fbm.setBusNumber(sb.getBusNr());
        }
      }
    }

    return modules;
  }

  private InetAddress discover(final CommandStationBean commandStation) {
    final JOptionPane optionPane = new JOptionPane("Try to discovering a " + commandStation.getDescription(),
            JOptionPane.INFORMATION_MESSAGE,
            JOptionPane.DEFAULT_OPTION);

    final JDialog discoverDialog = new JDialog((JDialog) null, "Discovering...");
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

  private JFrame getParentFrame() {
    if (SwingUtilities.getRoot(this) instanceof JFrame frame) {
      return frame;
    } else {
      return null;
    }
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

        //Only show the MessageDial when in Dialog
        if (!(getParentFrame() instanceof JCSFrame)) {
          JOptionPane.showMessageDialog(this, "Can't connect with host " + ip, "Can't Connect", JOptionPane.WARNING_MESSAGE);
        } else {
          Logger.trace("Can't connect with host " + ip);
        }
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

    final JDialog connectingDialog = new JDialog((JDialog) null, "Connecting...");
    connectingDialog.setContentPane(optionPane);
    connectingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    connectingDialog.pack();
    connectingDialog.setLocationRelativeTo(null);
    connectingDialog.setVisible(true);

    if (null == commandStation.getId()) {
      Logger.trace("Unknown Controller!");
    } else {
      switch (commandStation.getId()) {
        case MARKLIN_CS ->
          controller = new MarklinCentralStationImpl(commandStation);
        case ESU_ECOS ->
          controller = new EsuEcosCommandStationImpl(commandStation);
        case DCC_EX ->
          Logger.info("TODO: DCC-EX!");
        case HSI_S88 ->
          Logger.info("TODO: HSI-S88!");
        default ->
          Logger.trace("Unknown Controller!");
      }
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
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  JLabel accessoryControllerLbl;
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
  Box.Filler filler2;
  JLabel hwVersionLbl;
  JLabel ipOrPortLbl;
  JTextField ipTF;
  JPanel jPanel2;
  JPanel jPanel3;
  JPanel jPanel4;
  JPanel jPanel5;
  JPanel mainCSPanel;
  JLabel mainLbl;
  JSpinner mainSpinner;
  JRadioButton networkRB;
  JPanel propertiesPanel;
  JPanel rightPanel;
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
