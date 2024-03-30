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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jcs.commandStation.AccessoryController;
import jcs.commandStation.ControllerFactory;
import jcs.entities.AccessoryBean;
import static jcs.entities.AccessoryBean.AccessoryValue.GREEN;
import static jcs.entities.AccessoryBean.AccessoryValue.RED;
import jcs.entities.AccessoryBean.Protocol;
import jcs.entities.CommandStationBean;
import jcs.persistence.PersistenceFactory;
import jcs.util.KeyValuePair;
import org.tinylog.Logger;

/**
 * Dialog panel for importing and editing locomotive settings
 *
 * @author Frans Jacobs
 */
public class AccessoryPreferencesPanel extends JPanel implements PropertyChangeListener {

  private final AccessoryBeanListModel accessoryListModel;
  //TODO: support for multiple AccessoryControllers 
  private CommandStationBean commandStationBean;
  private AccessoryBean selectedAccessory;
  private final DefaultComboBoxModel<Protocol> protocolCBModel;
  private final DefaultComboBoxModel<KeyValuePair> typeCBModel;

  private final Map<String, KeyValuePair> typeMap = new HashMap<>();

  private SynchronizationTask task;

  public AccessoryPreferencesPanel() {
    accessoryListModel = new AccessoryBeanListModel();
    protocolCBModel = new DefaultComboBoxModel(Protocol.values());

    List<KeyValuePair> kvl = createTypeKeyValuePairs();
    typeCBModel = new DefaultComboBoxModel(kvl.toArray());
    //Lookup for types
    for (KeyValuePair kv : kvl) {
      typeMap.put(kv.getKey(), kv);
    }

    initComponents();
    initModels();
  }

  private void initModels() {
    if (PersistenceFactory.getService() != null) {
      commandStationBean = PersistenceFactory.getService().getDefaultCommandStation();
      commandStationLbl.setText(commandStationBean.getDescription());

      accessoryListModel.clear();
      List<AccessoryBean> accessories = PersistenceFactory.getService().getAccessoriesByCommandStationId(commandStationBean.getId());
      accessoryListModel.addAll(accessories);
      this.accessoryList.setModel(accessoryListModel);

      setFieldValues();
    }
  }

  private void setFieldValues() {
    if (selectedAccessory != null) {
      String id = selectedAccessory.getId();
      if (id != null) {
        this.idLabel.setText(id);
      }

      Integer address = selectedAccessory.getAddress();
      if (address == null) {
        address = 0;
      }
      this.addressSpinner.setValue(address);

      String name = selectedAccessory.getName();
      this.nameTF.setText(name);

      String type = selectedAccessory.getType();
      if (type != null) {
        KeyValuePair kv = typeMap.get(type);
        typeCBModel.setSelectedItem(kv);
      }

      Integer switchTime = selectedAccessory.getSwitchTime();
      if (switchTime == null) {
        switchTime = 200;
        selectedAccessory.setSwitchTime(switchTime);
      }
      this.switchTimeSpinner.setValue(switchTime);

      Protocol protocol = selectedAccessory.getProtocol();
      this.protocolCBModel.setSelectedItem(protocol);

      String decoder = selectedAccessory.getDecoder();
      this.decoderTF.setText(decoder);

      Integer states = selectedAccessory.getStates();
      if (states == null) {
        states = 2;
        selectedAccessory.setStates(states);
      }
      this.statesSpinner.setValue(states);

      //Integer state = selectedAccessory.getState();
      if (null == selectedAccessory.getAccessoryValue()) {
        this.currentGreenStateLabel.setVisible(false);
        this.currentRedStateLabel.setVisible(false);
      } else {
        switch (selectedAccessory.getAccessoryValue()) {
          case GREEN -> {
            this.currentGreenStateLabel.setVisible(true);
            this.currentRedStateLabel.setVisible(false);
          }
          case RED -> {
            this.currentGreenStateLabel.setVisible(false);
            this.currentRedStateLabel.setVisible(true);
          }
          default -> {
            this.currentGreenStateLabel.setVisible(false);
            this.currentRedStateLabel.setVisible(false);
          }
        }
      }
      String group = selectedAccessory.getGroup();
      if (group == null) {
        if (selectedAccessory.isSignal()) {
          groupLabel.setText("lichtsignale");
        } else if (selectedAccessory.isTurnout()) {
          groupLabel.setText("weichen");
        } else {
          groupLabel.setText("other");
        }
        selectedAccessory.setGroup(groupLabel.getText());
      } else {
        groupLabel.setText(group);
      }
      //String icon = selectedAccessory.getIcon();
      String iconFile = selectedAccessory.getIconFile();
      this.iconTF.setText(iconFile);

      String source = selectedAccessory.getSource();
      if (source == null) {
        selectedAccessory.setSource("Manual Inserted");
      }

      //String commandStationId = selectedAccessory.getCommandStationId();
      boolean synch = selectedAccessory.isSynchronize();
      synchronizeCB.setSelected(synch);
    } else {
      this.idLabel.setText(null);
      this.addressSpinner.setValue(0);
      this.nameTF.setText(null);
      this.typeCBModel.setSelectedItem(typeMap.get("null"));
      this.switchTimeSpinner.setValue(200);
      this.protocolCBModel.setSelectedItem(Protocol.MM);
      this.decoderTF.setText(null);
      this.statesSpinner.setValue(2);
      this.currentGreenStateLabel.setVisible(false);
      this.currentRedStateLabel.setVisible(false);
      this.groupLabel.setText(null);
      this.iconTF.setText(null);
      this.synchronizeCB.setSelected(false);
    }
    enableFields(selectedAccessory != null);
  }

  private void enableFields(boolean enable) {
    //ProgressBar
    this.synchPB.setVisible(false);

    this.synchronizeBtn.setEnabled(this.commandStationBean.isAccessoryControlSupport());
    this.synchronizeBtn.setVisible(this.commandStationBean.isAccessorySynchronizationSupport());

    this.synchronizeCB.setEnabled(this.commandStationBean.isAccessorySynchronizationSupport() && enable);

    boolean showId = selectedAccessory != null && selectedAccessory.getId() != null;
    this.idNameLbl.setVisible(showId);
    this.idLabel.setVisible(showId);

    boolean showGrp = selectedAccessory != null && selectedAccessory.getGroup() != null;
    this.groupLbl.setVisible(showGrp);
    this.groupLabel.setVisible(showGrp);

    boolean enableFields = enable && !this.synchronizeCB.isSelected();
    this.nameTF.setEnabled(enableFields);
    this.addressSpinner.setEnabled(enableFields);
    this.protocolCB.setEnabled(enableFields);
    this.typeCB.setEnabled(enableFields);
    this.decoderTF.setEnabled(enableFields);

    boolean acsup = this.commandStationBean.isAccessorySynchronizationSupport();
    this.iconTF.setEnabled(enableFields && acsup);
    this.iconFileDialogBtn.setEnabled(enableFields && acsup);

    this.switchTimeSpinner.setEnabled(enableFields);
    this.statesSpinner.setEnabled(enableFields);

    this.saveBtn.setEnabled(!this.synchronizeCB.isSelected() && enable);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings({"unchecked", "deprecation"})
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    filterButtonGroup = new ButtonGroup();
    topPanel = new JPanel();
    leftPanel = new JPanel();
    commandStationNameLbl = new JLabel();
    commandStationLbl = new JLabel();
    rightPanel = new JPanel();
    synchPB = new JProgressBar();
    synchronizeBtn = new JButton();
    newBtn = new JButton();
    deleteBtn = new JButton();
    refreshBtn = new JButton();
    detailPanel = new JPanel();
    row0Panel = new JPanel();
    showAllRB = new JRadioButton();
    showTurnoutsRB = new JRadioButton();
    showSignalsRB = new JRadioButton();
    row1Panel = new JPanel();
    synchronizeCB = new JCheckBox();
    addressLbl = new JLabel();
    addressSpinner = new JSpinner();
    protocolLabel = new JLabel();
    protocolCB = new JComboBox<>();
    typeLbl = new JLabel();
    typeCB = new JComboBox<>();
    row2Panel = new JPanel();
    nameLbl = new JLabel();
    nameTF = new JTextField();
    idNameLbl = new JLabel();
    idLabel = new JLabel();
    row3Panel = new JPanel();
    decoderLbl = new JLabel();
    decoderTF = new JTextField();
    groupLbl = new JLabel();
    groupLabel = new JLabel();
    row4Panel = new JPanel();
    statesLbl = new JLabel();
    statesSpinner = new JSpinner();
    currentStateLbl = new JLabel();
    currentGreenStateLabel = new JLabel();
    currentRedStateLabel = new JLabel();
    row5Panel = new JPanel();
    switchTimeLbl = new JLabel();
    switchTimeSpinner = new JSpinner();
    row6Panel = new JPanel();
    iconNameLbl = new JLabel();
    iconTF = new JTextField();
    iconFileDialogBtn = new JButton();
    imageLabel = new JLabel();
    row7Panel = new JPanel();
    row9Panel = new JPanel();
    filler2 = new Box.Filler(new Dimension(0, 50), new Dimension(0, 50), new Dimension(32767, 300));
    buttonPanel = new JPanel();
    westPanel = new JPanel();
    accessoriesSP = new JScrollPane();
    accessoryList = new JList<>();
    bottomPanel = new JPanel();
    filler1 = new Box.Filler(new Dimension(100, 0), new Dimension(200, 0), new Dimension(150, 32767));
    saveBtn = new JButton();

    setMinimumSize(new Dimension(1000, 600));
    setPreferredSize(new Dimension(1000, 600));
    setLayout(new BorderLayout());

    topPanel.setMinimumSize(new Dimension(1000, 50));
    topPanel.setPreferredSize(new Dimension(1000, 50));
    topPanel.setRequestFocusEnabled(false);
    topPanel.setLayout(new GridLayout(1, 2));

    FlowLayout flowLayout11 = new FlowLayout(FlowLayout.LEFT);
    flowLayout11.setAlignOnBaseline(true);
    leftPanel.setLayout(flowLayout11);

    commandStationNameLbl.setText("Command Station:");
    leftPanel.add(commandStationNameLbl);

    commandStationLbl.setFont(new Font("sansserif", 1, 13)); // NOI18N
    commandStationLbl.setText("The Command Station");
    leftPanel.add(commandStationLbl);

    topPanel.add(leftPanel);

    FlowLayout flowLayout2 = new FlowLayout(FlowLayout.RIGHT);
    flowLayout2.setAlignOnBaseline(true);
    rightPanel.setLayout(flowLayout2);
    rightPanel.add(synchPB);

    synchronizeBtn.setIcon(new ImageIcon(getClass().getResource("/media/CS2-3-Sync.png"))); // NOI18N
    synchronizeBtn.setToolTipText("Synchronize All Locomotives with Central Station");
    synchronizeBtn.setDoubleBuffered(true);
    synchronizeBtn.setMaximumSize(new Dimension(40, 40));
    synchronizeBtn.setMinimumSize(new Dimension(40, 40));
    synchronizeBtn.setPreferredSize(new Dimension(40, 40));
    synchronizeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        synchronizeBtnActionPerformed(evt);
      }
    });
    rightPanel.add(synchronizeBtn);

    newBtn.setIcon(new ImageIcon(getClass().getResource("/media/add-24.png"))); // NOI18N
    newBtn.setToolTipText("Create new Accessory");
    newBtn.setMaximumSize(new Dimension(40, 40));
    newBtn.setMinimumSize(new Dimension(40, 40));
    newBtn.setPreferredSize(new Dimension(40, 40));
    newBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        newBtnActionPerformed(evt);
      }
    });
    rightPanel.add(newBtn);

    deleteBtn.setIcon(new ImageIcon(getClass().getResource("/media/delete-24.png"))); // NOI18N
    deleteBtn.setMaximumSize(new Dimension(40, 40));
    deleteBtn.setMinimumSize(new Dimension(40, 40));
    deleteBtn.setPreferredSize(new Dimension(40, 40));
    deleteBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        deleteBtnActionPerformed(evt);
      }
    });
    rightPanel.add(deleteBtn);

    refreshBtn.setIcon(new ImageIcon(getClass().getResource("/media/refresh-24.png"))); // NOI18N
    refreshBtn.setToolTipText("Refresh Locomotives");
    refreshBtn.setMaximumSize(new Dimension(40, 40));
    refreshBtn.setMinimumSize(new Dimension(40, 40));
    refreshBtn.setPreferredSize(new Dimension(40, 40));
    refreshBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        refreshBtnActionPerformed(evt);
      }
    });
    rightPanel.add(refreshBtn);

    topPanel.add(rightPanel);

    add(topPanel, BorderLayout.NORTH);

    detailPanel.setMinimumSize(new Dimension(390, 540));
    detailPanel.setPreferredSize(new Dimension(480, 500));
    detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));

    row0Panel.setMinimumSize(new Dimension(380, 30));
    row0Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout8 = new FlowLayout(FlowLayout.LEFT);
    flowLayout8.setAlignOnBaseline(true);
    row0Panel.setLayout(flowLayout8);

    filterButtonGroup.add(showAllRB);
    showAllRB.setSelected(true);
    showAllRB.setText("All");
    showAllRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showAllRBActionPerformed(evt);
      }
    });
    row0Panel.add(showAllRB);

    filterButtonGroup.add(showTurnoutsRB);
    showTurnoutsRB.setText("Turnouts");
    showTurnoutsRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showTurnoutsRBActionPerformed(evt);
      }
    });
    row0Panel.add(showTurnoutsRB);

    filterButtonGroup.add(showSignalsRB);
    showSignalsRB.setText("Signals");
    showSignalsRB.setActionCommand("");
    showSignalsRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showSignalsRBActionPerformed(evt);
      }
    });
    row0Panel.add(showSignalsRB);

    detailPanel.add(row0Panel);

    row1Panel.setMinimumSize(new Dimension(380, 30));
    row1Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    row1Panel.setLayout(flowLayout1);

    synchronizeCB.setText("Synchronize:");
    synchronizeCB.setToolTipText("Automatically imported from Command Station");
    synchronizeCB.setDoubleBuffered(true);
    synchronizeCB.setHorizontalAlignment(SwingConstants.TRAILING);
    synchronizeCB.setHorizontalTextPosition(SwingConstants.LEADING);
    synchronizeCB.setPreferredSize(new Dimension(110, 21));
    synchronizeCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        synchronizeCBActionPerformed(evt);
      }
    });
    row1Panel.add(synchronizeCB);

    addressLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    addressLbl.setLabelFor(addressSpinner);
    addressLbl.setText("Address:");
    addressLbl.setPreferredSize(new Dimension(80, 16));
    row1Panel.add(addressLbl);

    addressSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
    addressSpinner.setToolTipText("The accessorry address");
    addressSpinner.setDoubleBuffered(true);
    addressSpinner.setEditor(new JSpinner.NumberEditor(addressSpinner, ""));
    addressSpinner.setMinimumSize(new Dimension(50, 26));
    addressSpinner.setName(""); // NOI18N
    addressSpinner.setNextFocusableComponent(nameTF);
    addressSpinner.setPreferredSize(new Dimension(85, 26));
    addressSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        addressSpinnerStateChanged(evt);
      }
    });
    row1Panel.add(addressSpinner);

    protocolLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    protocolLabel.setLabelFor(protocolCB);
    protocolLabel.setText("Protocol:");
    protocolLabel.setPreferredSize(new Dimension(80, 16));
    row1Panel.add(protocolLabel);

    protocolCB.setModel(protocolCBModel);
    protocolCB.setToolTipText("The Accessory protocol");
    protocolCB.setDoubleBuffered(true);
    protocolCB.setPreferredSize(new Dimension(85, 26));
    protocolCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        protocolCBActionPerformed(evt);
      }
    });
    row1Panel.add(protocolCB);

    typeLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    typeLbl.setText("Type:");
    typeLbl.setPreferredSize(new Dimension(60, 17));
    row1Panel.add(typeLbl);

    typeCB.setModel(typeCBModel);
    typeCB.setToolTipText("Accessory Type");
    typeCB.setDoubleBuffered(true);
    typeCB.setPreferredSize(new Dimension(200, 26));
    typeCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        typeCBActionPerformed(evt);
      }
    });
    row1Panel.add(typeCB);

    detailPanel.add(row1Panel);

    row2Panel.setMinimumSize(new Dimension(380, 30));
    row2Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout3 = new FlowLayout(FlowLayout.LEFT);
    flowLayout3.setAlignOnBaseline(true);
    row2Panel.setLayout(flowLayout3);

    nameLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    nameLbl.setLabelFor(nameTF);
    nameLbl.setText("Name:");
    nameLbl.setPreferredSize(new Dimension(100, 16));
    row2Panel.add(nameLbl);

    nameTF.setToolTipText("Name of the Accessory");
    nameTF.setDoubleBuffered(true);
    nameTF.setMinimumSize(new Dimension(175, 26));
    nameTF.setPreferredSize(new Dimension(150, 26));
    nameTF.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        nameTFFocusLost(evt);
      }
    });
    row2Panel.add(nameTF);

    idNameLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    idNameLbl.setLabelFor(idNameLbl);
    idNameLbl.setText("Id:");
    idNameLbl.setToolTipText("Accessory ID");
    idNameLbl.setPreferredSize(new Dimension(100, 17));
    row2Panel.add(idNameLbl);

    idLabel.setPreferredSize(new Dimension(85, 17));
    row2Panel.add(idLabel);

    detailPanel.add(row2Panel);

    row3Panel.setMinimumSize(new Dimension(380, 30));
    row3Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout4 = new FlowLayout(FlowLayout.LEFT);
    flowLayout4.setAlignOnBaseline(true);
    row3Panel.setLayout(flowLayout4);

    decoderLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    decoderLbl.setText("Decoder:");
    decoderLbl.setPreferredSize(new Dimension(100, 17));
    row3Panel.add(decoderLbl);

    decoderTF.setToolTipText("Decoder Type");
    decoderTF.setDoubleBuffered(true);
    decoderTF.setPreferredSize(new Dimension(150, 26));
    decoderTF.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        decoderTFFocusLost(evt);
      }
    });
    row3Panel.add(decoderTF);

    groupLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    groupLbl.setLabelFor(groupLabel);
    groupLbl.setText("Group:");
    groupLbl.setToolTipText("Accessory Group");
    groupLbl.setPreferredSize(new Dimension(100, 17));
    row3Panel.add(groupLbl);

    groupLabel.setPreferredSize(new Dimension(85, 17));
    row3Panel.add(groupLabel);

    detailPanel.add(row3Panel);

    row4Panel.setMinimumSize(new Dimension(380, 30));
    row4Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout5 = new FlowLayout(FlowLayout.LEFT);
    flowLayout5.setAlignOnBaseline(true);
    row4Panel.setLayout(flowLayout5);

    statesLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    statesLbl.setLabelFor(statesSpinner);
    statesLbl.setText("States:");
    statesLbl.setToolTipText("");
    statesLbl.setPreferredSize(new Dimension(100, 17));
    row4Panel.add(statesLbl);

    statesSpinner.setModel(new SpinnerNumberModel(0, 0, 300, 1));
    statesSpinner.setToolTipText("Nr of States the accessory can perform/show");
    statesSpinner.setDoubleBuffered(true);
    statesSpinner.setPreferredSize(new Dimension(85, 26));
    statesSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        statesSpinnerStateChanged(evt);
      }
    });
    row4Panel.add(statesSpinner);

    currentStateLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    currentStateLbl.setText("Current State:");
    currentStateLbl.setPreferredSize(new Dimension(100, 17));
    row4Panel.add(currentStateLbl);

    currentGreenStateLabel.setIcon(new ImageIcon(getClass().getResource("/media/Button-Green-14px.png"))); // NOI18N
    currentGreenStateLabel.setPreferredSize(new Dimension(17, 17));
    row4Panel.add(currentGreenStateLabel);

    currentRedStateLabel.setIcon(new ImageIcon(getClass().getResource("/media/Button-Red-14px.png"))); // NOI18N
    currentRedStateLabel.setPreferredSize(new Dimension(17, 17));
    row4Panel.add(currentRedStateLabel);

    detailPanel.add(row4Panel);

    row5Panel.setMinimumSize(new Dimension(380, 30));
    row5Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout6 = new FlowLayout(FlowLayout.LEFT);
    flowLayout6.setAlignOnBaseline(true);
    row5Panel.setLayout(flowLayout6);

    switchTimeLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    switchTimeLbl.setLabelFor(switchTimeSpinner);
    switchTimeLbl.setText("Switchtime (ms):");
    switchTimeLbl.setPreferredSize(new Dimension(100, 17));
    row5Panel.add(switchTimeLbl);

    switchTimeSpinner.setModel(new SpinnerNumberModel(0, 0, null, 10));
    switchTimeSpinner.setDoubleBuffered(true);
    switchTimeSpinner.setPreferredSize(new Dimension(85, 26));
    switchTimeSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        switchTimeSpinnerStateChanged(evt);
      }
    });
    row5Panel.add(switchTimeSpinner);

    detailPanel.add(row5Panel);

    row6Panel.setMinimumSize(new Dimension(380, 30));
    row6Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout7 = new FlowLayout(FlowLayout.LEFT);
    flowLayout7.setAlignOnBaseline(true);
    row6Panel.setLayout(flowLayout7);

    iconNameLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    iconNameLbl.setText("Icon:");
    iconNameLbl.setPreferredSize(new Dimension(100, 17));
    row6Panel.add(iconNameLbl);

    iconTF.setPreferredSize(new Dimension(375, 26));
    iconTF.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        iconTFFocusLost(evt);
      }
    });
    row6Panel.add(iconTF);

    iconFileDialogBtn.setText("...");
    iconFileDialogBtn.setPreferredSize(new Dimension(26, 26));
    iconFileDialogBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        iconFileDialogBtnActionPerformed(evt);
      }
    });
    row6Panel.add(iconFileDialogBtn);

    imageLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    imageLabel.setToolTipText("The locomotive image");
    imageLabel.setPreferredSize(new Dimension(128, 48));
    row6Panel.add(imageLabel);

    detailPanel.add(row6Panel);

    row7Panel.setMinimumSize(new Dimension(380, 30));
    row7Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout9 = new FlowLayout(FlowLayout.LEFT);
    flowLayout9.setAlignOnBaseline(true);
    row7Panel.setLayout(flowLayout9);
    detailPanel.add(row7Panel);

    row9Panel.setMinimumSize(new Dimension(380, 30));
    row9Panel.setPreferredSize(new Dimension(380, 30));
    row9Panel.setLayout(new FlowLayout(FlowLayout.LEFT));
    detailPanel.add(row9Panel);
    detailPanel.add(filler2);

    buttonPanel.setPreferredSize(new Dimension(380, 40));
    FlowLayout flowLayout10 = new FlowLayout();
    flowLayout10.setAlignOnBaseline(true);
    buttonPanel.setLayout(flowLayout10);
    detailPanel.add(buttonPanel);

    add(detailPanel, BorderLayout.CENTER);

    westPanel.setMinimumSize(new Dimension(175, 500));
    westPanel.setPreferredSize(new Dimension(175, 500));
    westPanel.setLayout(new BorderLayout());

    accessoriesSP.setPreferredSize(new Dimension(175, 130));

    accessoryList.setModel(accessoryListModel);
    accessoryList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        accessoryListValueChanged(evt);
      }
    });
    accessoriesSP.setViewportView(accessoryList);

    westPanel.add(accessoriesSP, BorderLayout.CENTER);

    add(westPanel, BorderLayout.WEST);

    bottomPanel.setPreferredSize(new Dimension(1000, 50));
    bottomPanel.setRequestFocusEnabled(false);
    FlowLayout flowLayout12 = new FlowLayout(FlowLayout.RIGHT);
    flowLayout12.setAlignOnBaseline(true);
    bottomPanel.setLayout(flowLayout12);
    bottomPanel.add(filler1);

    saveBtn.setIcon(new ImageIcon(getClass().getResource("/media/save-24.png"))); // NOI18N
    saveBtn.setMaximumSize(new Dimension(40, 40));
    saveBtn.setMinimumSize(new Dimension(40, 40));
    saveBtn.setPreferredSize(new Dimension(40, 40));
    saveBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveBtnActionPerformed(evt);
      }
    });
    bottomPanel.add(saveBtn);

    add(bottomPanel, BorderLayout.SOUTH);
  }// </editor-fold>//GEN-END:initComponents

  private void newBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_newBtnActionPerformed
    Logger.trace("Create new Accessory...");
    AccessoryBean newAccessory = new AccessoryBean();
    newAccessory.setCommandStationId(commandStationBean.getId());
    List<AccessoryBean> accessories = PersistenceFactory.getService().getAccessoriesByCommandStationId(commandStationBean.getId());
    int last = accessories.size();
    String id = String.format("%03d", (last + 1));
    newAccessory.setId(id);

    newAccessory.setSource("Manual Inserted");

    newAccessory.setProtocol(Protocol.DCC);
    newAccessory.setSynchronize(false);

    this.accessoryListModel.add(newAccessory);
    this.accessoryList.setSelectedValue(newAccessory, true);
    this.selectedAccessory = newAccessory;

    setFieldValues();
  }//GEN-LAST:event_newBtnActionPerformed

  private void saveBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
    if (selectedAccessory != null) {
      if (!selectedAccessory.isSynchronize() && selectedAccessory.getId() == null) {
        String id = String.format("%03d", selectedAccessory.getAddress());
        selectedAccessory.setId(id);
      }
      Logger.trace("Saving: " + selectedAccessory.toLogString());

      selectedAccessory = PersistenceFactory.getService().persist(selectedAccessory);
      initModels();
      accessoryList.setSelectedValue(selectedAccessory, true);
    }
  }//GEN-LAST:event_saveBtnActionPerformed

  private void deleteBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
    Logger.trace("Delete: " + selectedAccessory.toLogString());
    PersistenceFactory.getService().remove(selectedAccessory);
    selectedAccessory = null;
    initModels();
  }//GEN-LAST:event_deleteBtnActionPerformed

  private void refreshBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
    this.initModels();
  }//GEN-LAST:event_refreshBtnActionPerformed

    private void synchronizeBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_synchronizeBtnActionPerformed
      Logger.trace(evt.getActionCommand());
      this.synchronizeBtn.setEnabled(false);
      this.accessoryList.setEnabled(false);
      this.showAllRB.setSelected(true);

      this.synchPB.setValue(0);
      this.synchPB.setIndeterminate(true);
      this.synchPB.setVisible(true);

      task = new SynchronizationTask();
      task.addPropertyChangeListener(this);
      task.execute();
    }//GEN-LAST:event_synchronizeBtnActionPerformed

  private void accessoryListValueChanged(ListSelectionEvent evt) {//GEN-FIRST:event_accessoryListValueChanged
    if (!evt.getValueIsAdjusting()) {
      Logger.trace(this.accessoryList.getSelectedValue());
      selectedAccessory = accessoryList.getSelectedValue();
      setFieldValues();
    }
  }//GEN-LAST:event_accessoryListValueChanged

  private void iconFileDialogBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_iconFileDialogBtnActionPerformed
    Logger.trace(evt.getActionCommand());
    JFrame parentFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
    IconFileChooser fileDialog = new IconFileChooser(parentFrame, true);
    fileDialog.setVisible(true);

    File iconFile = fileDialog.getSelectedIconFile();
    if (iconFile != null) {
      //iconTF.setText(iconFile.getPath());
      //this.selectedAccessory.setIcon(iconFile.getPath());

      //try to show the image also
      Image img = PersistenceFactory.getService().readImage(iconFile.getPath(), false);
      //if (img != null) {
      //img = PersistenceFactory.getService().getLocomotiveImage(iconFile.getPath());
      //this.selectedAccessory.setLocIcon(img);
      //this.imageLabel.setIcon(new ImageIcon(img));
      //}
    }
  }//GEN-LAST:event_iconFileDialogBtnActionPerformed

  private void nameTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_nameTFFocusLost
    this.selectedAccessory.setName(this.nameTF.getText());
  }//GEN-LAST:event_nameTFFocusLost

  private void synchronizeCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_synchronizeCBActionPerformed
    enableFields(this.selectedAccessory != null);
    if (this.selectedAccessory != null) {
      this.selectedAccessory.setSynchronize(this.synchronizeCB.isSelected());
    }
  }//GEN-LAST:event_synchronizeCBActionPerformed

  private void addressSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_addressSpinnerStateChanged
    if (selectedAccessory != null) {
      selectedAccessory.setAddress((Integer) addressSpinner.getValue());
      long uid = this.selectedAccessory.getAddress();
      //this.selectedAccessory.setUid(uid);
    }
  }//GEN-LAST:event_addressSpinnerStateChanged

  private void protocolCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_protocolCBActionPerformed
    if (this.selectedAccessory != null) {
      this.selectedAccessory.setProtocol((Protocol) protocolCB.getSelectedItem());
    }
  }//GEN-LAST:event_protocolCBActionPerformed

  private void iconTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_iconTFFocusLost
    this.selectedAccessory.setIcon(this.iconTF.getText());
    if (this.selectedAccessory.getIcon() != null) {
      String icon = this.selectedAccessory.getIcon();
      Image locIcon = PersistenceFactory.getService().getLocomotiveImage(icon);
      if (locIcon != null) {
        //this.selectedAccessory.setLocIcon(locIcon);
        this.imageLabel.setIcon(new ImageIcon(locIcon));
        this.imageLabel.setText("");
      } else {
        this.imageLabel.setIcon(null);
        this.imageLabel.setText("");
      }
    }
  }//GEN-LAST:event_iconTFFocusLost

  private void switchTimeSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_switchTimeSpinnerStateChanged
    Integer st = (Integer) switchTimeSpinner.getValue();
    if (selectedAccessory != null) {
      this.selectedAccessory.setSwitchTime(st);
    }
  }//GEN-LAST:event_switchTimeSpinnerStateChanged

  private void statesSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_statesSpinnerStateChanged
    Integer states = (Integer) statesSpinner.getValue();
    if (selectedAccessory != null) {
      this.selectedAccessory.setState(states);
    }
  }//GEN-LAST:event_statesSpinnerStateChanged

  private void showAllRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showAllRBActionPerformed
    accessoryListModel.resetFilters();
    if (selectedAccessory != null) {
      accessoryList.setSelectedValue(this.selectedAccessory, true);
    }
  }//GEN-LAST:event_showAllRBActionPerformed

  private void showTurnoutsRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showTurnoutsRBActionPerformed
    accessoryListModel.showTurnoutsOnly(true);
    if (selectedAccessory != null && selectedAccessory.isTurnout()) {
      accessoryList.setSelectedValue(this.selectedAccessory, true);
    } else {
      accessoryList.setSelectedValue(null, true);
      selectedAccessory = null;
    }
  }//GEN-LAST:event_showTurnoutsRBActionPerformed

  private void showSignalsRBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showSignalsRBActionPerformed
    accessoryListModel.showSignalsOnly(true);
    if (selectedAccessory != null && selectedAccessory.isSignal()) {
      accessoryList.setSelectedValue(this.selectedAccessory, true);
    } else {
      accessoryList.setSelectedValue(null, true);
      selectedAccessory = null;
    }
  }//GEN-LAST:event_showSignalsRBActionPerformed

  private void typeCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_typeCBActionPerformed
    KeyValuePair kv = (KeyValuePair) typeCB.getSelectedItem();
    if (selectedAccessory != null) {
      this.selectedAccessory.setType(kv.getKey());
    }
  }//GEN-LAST:event_typeCBActionPerformed

  private void decoderTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_decoderTFFocusLost
    // TODO add your handling code here:
  }//GEN-LAST:event_decoderTFFocusLost

  private List<KeyValuePair> createTypeKeyValuePairs() {
    List<KeyValuePair> kvl = new ArrayList<>();

    kvl.add(new KeyValuePair("null", ""));
    kvl.add(new KeyValuePair("std_rot_gruen", "Red/Green"));
    kvl.add(new KeyValuePair("std_rot", "Red only"));
    kvl.add(new KeyValuePair("std_gruen", "Green only"));
    kvl.add(new KeyValuePair("rechtsweiche", "Turnout Right"));
    kvl.add(new KeyValuePair("linksweiche", "Turnout Left"));
    kvl.add(new KeyValuePair("y_weiche", "Cross"));
    kvl.add(new KeyValuePair("dreiwegweiche", "3 way Turnout"));
    kvl.add(new KeyValuePair("entkupplungsgleis", "Decoupler"));
    kvl.add(new KeyValuePair("entkupplungsgleis_1", "Decoupler 1"));

    kvl.add(new KeyValuePair("lichtsignal_HP01", "Signal Hp0/1"));
    kvl.add(new KeyValuePair("lichtsignal_HP02", "Signal Hp0/2"));
    kvl.add(new KeyValuePair("lichtsignal_HP012", "Signal Hp0/1/2"));
    kvl.add(new KeyValuePair("lichtsignal_HP012_SH01", "Signal Hp0/1/2 Sh0/1"));
    kvl.add(new KeyValuePair("lichtsignal_SH01", "Signal Sh0/1"));

    kvl.add(new KeyValuePair("formsignal_HP01", "Semaphore Hp0/1"));
    kvl.add(new KeyValuePair("formsignal_HP02", "Semaphore Hp0/2"));
    kvl.add(new KeyValuePair("formsignal_HP012", "Semaphore Hp0/1/2"));
    kvl.add(new KeyValuePair("formsignal_HP012_SH01", "Semaphore Hp0/1/2 Sh0/1"));
    kvl.add(new KeyValuePair("formsignal_SH01", "Semaphore Sh0/1"));
    //URC with Under Rail Controller
    kvl.add(new KeyValuePair("urc_lichtsignal_HP01", "Signal URC Hp0/1"));
    kvl.add(new KeyValuePair("urc_lichtsignal_HP012", "Signal URC Hp0/1/2"));
    kvl.add(new KeyValuePair("urc_lichtsignal_HP012_SH01", "Signal URC Hp0/1/2"));
    kvl.add(new KeyValuePair("urc_lichtsignal_SH01", "Signal URC Sh0/1"));

    return kvl;
  }

  class AccessoryBeanByNameSorter implements Comparator<AccessoryBean> {

    @Override
    public int compare(AccessoryBean a, AccessoryBean b) {
      //Avoid null pointers
      String aa = a.getName();
      if (aa == null) {
        aa = "000";
      }
      String bb = b.getName();
      if (bb == null) {
        bb = "000";
      }

      return aa.compareTo(bb);
    }
  }

  class AccessoryBeanListModel extends AbstractListModel<AccessoryBean> {

    private final List<AccessoryBean> all;
    private final List<AccessoryBean> filtered;

    private boolean turnoutsOnly;
    private boolean signalsOnly;

    public AccessoryBeanListModel() {
      all = new ArrayList<>();
      filtered = new ArrayList<>();
    }

    public void showTurnoutsOnly(boolean flag) {
      turnoutsOnly = flag;
      signalsOnly = !flag;
      filterList();
      fireContentsChanged(this, 0, getSize());
    }

    public void showSignalsOnly(boolean flag) {
      turnoutsOnly = !flag;
      signalsOnly = flag;
      filterList();
      fireContentsChanged(this, 0, getSize());
    }

    public void resetFilters() {
      turnoutsOnly = false;
      signalsOnly = false;
      filterList();
      fireContentsChanged(this, 0, getSize());
    }

    @Override
    public int getSize() {
      return filtered.size();
    }

    @Override
    public AccessoryBean getElementAt(int index) {
      return (AccessoryBean) filtered.toArray()[index];
    }

    public void add(AccessoryBean element) {
      if (all.add(element)) {
        filterList();
        Collections.sort(filtered, new AccessoryBeanByNameSorter());
        fireContentsChanged(this, 0, getSize());
      }
    }

    public void addAll(AccessoryBean elements[]) {
      Collection<AccessoryBean> c = Arrays.asList(elements);
      all.addAll(c);
      filterList();
      Collections.sort(filtered, new AccessoryBeanByNameSorter());
      fireContentsChanged(this, 0, getSize());
    }

    public void addAll(Collection<AccessoryBean> elements) {
      all.addAll(elements);
      filterList();
      Collections.sort(filtered, new AccessoryBeanByNameSorter());
      fireContentsChanged(this, 0, getSize());
    }

    public void clear() {
      all.clear();
      filterList();
      fireContentsChanged(this, 0, getSize());
    }

    public boolean contains(AccessoryBean element) {
      return filtered.contains(element);
    }

    public AccessoryBean firstElement() {
      if (!filtered.isEmpty()) {
        return filtered.get(0);
      } else {
        return null;
      }
    }

    public Iterator<AccessoryBean> iterator() {
      return filtered.iterator();
    }

    public AccessoryBean lastElement() {
      if (!filtered.isEmpty()) {
        return filtered.get(filtered.size() - 1);
      } else {
        return null;
      }
    }

    public boolean removeElement(AccessoryBean element) {
      boolean removed = all.remove(element);
      if (removed) {
        filterList();
        Collections.sort(filtered, new AccessoryBeanByNameSorter());
        fireContentsChanged(this, 0, getSize());
      }
      return removed;
    }

    private void filterList() {
      filtered.clear();
      for (AccessoryBean ab : all) {
        if (turnoutsOnly && ab.isTurnout()) {
          filtered.add(ab);
        } else if (signalsOnly && ab.isSignal()) {
          filtered.add(ab);
        } else if (!turnoutsOnly && !signalsOnly) {
          filtered.add(ab);
        }
      }
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("progress".equals(evt.getPropertyName())) {
      int progress = (Integer) evt.getNewValue();
      synchPB.setIndeterminate(progress < 20);
      synchPB.setValue(progress);

      if (task.isDone()) {
        synchronizeBtn.setEnabled(commandStationBean.isLocomotiveSynchronizationSupport());
      }
    }
    if ("updated".equals(evt.getPropertyName())) {
      Logger.trace("Done: " + evt.getNewValue());
      //if (!accessoryListModel.contains((LocomotiveBean) evt.getNewValue())) {
      //  accessoryListModel.add((LocomotiveBean) evt.getNewValue());
      //}
      this.accessoryList.setSelectedValue(evt.getNewValue(), true);
    }

    if ("done".equals(evt.getPropertyName())) {
      Logger.trace("Done: " + evt.getNewValue());
      //this.connectionTestResultLbl.setText((String) evt.getNewValue());
      this.synchPB.setVisible(false);
      this.accessoryList.setEnabled(true);
      accessoryList.clearSelection();
    }
  }

  class SynchronizationTask extends SwingWorker<Void, Void> {

    @Override
    public Void doInBackground() {
      setProgress(0);

      AccessoryController accessoryController = ControllerFactory.getAccessoryController(commandStationBean);
      if (accessoryController != null && !accessoryController.isConnected()) {
        accessoryController.connect();
      }

      if (accessoryController == null || !accessoryController.isConnected()) {

        Logger.warn("No Controller or No connection!");
        firePropertyChange("done", "", "Can't Connect with Controller!");
        setProgress(0);
        return null;
      }

      List<AccessoryBean> fromController = accessoryController.getAccessories();
      String importedFrom = commandStationBean.getShortName();

      int accCount = fromController.size();
      int processedCount = 0;

      for (AccessoryBean accessory : fromController) {
        String id = accessory.getId();
        AccessoryBean dbAccessory = PersistenceFactory.getService().getAccessory(id);
        boolean store = true;
        if (dbAccessory != null && accessory.getId().equals(dbAccessory.getId())) {
          if (dbAccessory.isSynchronize()) {
            Logger.trace("Accessory id: " + accessory.getId() + ", " + accessory.getName() + " Addres: " + accessory.getAddress() + " Exists");
            accessory.setSource(importedFrom);
            accessory.setSynchronize(true);
          } else {
            Logger.trace("Skip Accessory id: " + accessory.getId() + ", " + accessory.getName() + " Addres: " + accessory.getAddress() + " Exists");
            store = false;
          }
        } else {
          Logger.trace("New Accessory, id:" + accessory.getId() + ", " + accessory.getName() + " Addres: " + accessory.getAddress());
          accessory.setSynchronize(true);
          accessory.setSource(importedFrom);
        }

        if (store) {
          PersistenceFactory.getService().persist(accessory);
          firePropertyChange("updated", null, accessory);
        }
        processedCount++;

        double progress = (double) processedCount / accCount * 100;
        setProgress((int) progress);
      }

      firePropertyChange("done", "", "Accessories Synchronized");

      return null;
    }

    @Override
    public void done() {
      initModels();
      synchronizeBtn.setEnabled(commandStationBean.isAccessorySynchronizationSupport());
    }
  }

  //Testing
  public static void main(String args[]) {
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error("Can't set the LookAndFeel: " + ex);
    }
    java.awt.EventQueue.invokeLater(() -> {

      AccessoryPreferencesPanel testPanel = new AccessoryPreferencesPanel();
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
  JScrollPane accessoriesSP;
  JList<AccessoryBean> accessoryList;
  JLabel addressLbl;
  JSpinner addressSpinner;
  JPanel bottomPanel;
  JPanel buttonPanel;
  JLabel commandStationLbl;
  JLabel commandStationNameLbl;
  JLabel currentGreenStateLabel;
  JLabel currentRedStateLabel;
  JLabel currentStateLbl;
  JLabel decoderLbl;
  JTextField decoderTF;
  JButton deleteBtn;
  JPanel detailPanel;
  Box.Filler filler1;
  Box.Filler filler2;
  ButtonGroup filterButtonGroup;
  JLabel groupLabel;
  JLabel groupLbl;
  JButton iconFileDialogBtn;
  JLabel iconNameLbl;
  JTextField iconTF;
  JLabel idLabel;
  JLabel idNameLbl;
  JLabel imageLabel;
  JPanel leftPanel;
  JLabel nameLbl;
  JTextField nameTF;
  JButton newBtn;
  JComboBox<Protocol> protocolCB;
  JLabel protocolLabel;
  JButton refreshBtn;
  JPanel rightPanel;
  JPanel row0Panel;
  JPanel row1Panel;
  JPanel row2Panel;
  JPanel row3Panel;
  JPanel row4Panel;
  JPanel row5Panel;
  JPanel row6Panel;
  JPanel row7Panel;
  JPanel row9Panel;
  JButton saveBtn;
  JRadioButton showAllRB;
  JRadioButton showSignalsRB;
  JRadioButton showTurnoutsRB;
  JLabel statesLbl;
  JSpinner statesSpinner;
  JLabel switchTimeLbl;
  JSpinner switchTimeSpinner;
  JProgressBar synchPB;
  JButton synchronizeBtn;
  JCheckBox synchronizeCB;
  JPanel topPanel;
  JComboBox<KeyValuePair> typeCB;
  JLabel typeLbl;
  JPanel westPanel;
  // End of variables declaration//GEN-END:variables
}
