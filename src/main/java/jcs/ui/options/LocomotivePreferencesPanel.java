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
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import jcs.commandStation.ControllerFactory;
import jcs.commandStation.DecoderController;
import jcs.entities.CommandStationBean;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Dialog panel for importing and editing locomotive settings
 *
 * @author Frans Jacobs
 */
public class LocomotivePreferencesPanel extends JPanel implements PropertyChangeListener {

  private final LocomotiveBeanListModel locoListModel;
  private CommandStationBean commandStationBean;
  private LocomotiveBean selectedLocomotive;
  private final DefaultComboBoxModel<DecoderType> decoderTypes;

  private SynchronizationTask task;

  public LocomotivePreferencesPanel() {
    locoListModel = new LocomotiveBeanListModel();
    decoderTypes = new DefaultComboBoxModel(DecoderType.values());

    initComponents();
    initModels();
  }

  private void initModels() {
    if (PersistenceFactory.getService() != null) {
      commandStationBean = PersistenceFactory.getService().getDefaultCommandStation();
      commandStationLbl.setText(commandStationBean.getDescription());

      locoListModel.clear();
      List<LocomotiveBean> locos = PersistenceFactory.getService().getLocomotivesByCommandStationId(commandStationBean.getId());
      this.locoListModel.addAll(locos);

      setFieldValues();
    }
  }

  private void setFieldValues() {
    if (selectedLocomotive != null) {
      Long id = selectedLocomotive.getId();
      if (id != null) {
        this.idLbl.setText(id.toString());
      }

      String name = selectedLocomotive.getName();
      this.nameTF.setText(name);

      Long uid = selectedLocomotive.getUid();
      if (uid != null) {
        this.uidLbl.setText(uid.toString());
      }

      Integer address = selectedLocomotive.getAddress();
      if (address == null) {
        address = 0;
      }
      this.addressSpinner.setValue(address);

      String icon = selectedLocomotive.getIcon();
      this.iconTF.setText(icon);

      Image locIcon = selectedLocomotive.getLocIcon();
      if (locIcon == null) {
        if (commandStationBean.isDecoderControlSupport() && commandStationBean.isLocomotiveImageSynchronizationSupport() && icon != null) {
          locIcon = PersistenceFactory.getService().getLocomotiveImage(icon);
          if (locIcon != null) {
            this.selectedLocomotive.setLocIcon(locIcon);
          }
        }
      }

      if (locIcon != null) {
        this.imageLbl.setIcon(new ImageIcon(locIcon));
        this.imageLbl.setText("");
      } else {
        this.imageLbl.setIcon(null);
        this.imageLbl.setText("");
      }

      DecoderType decoderType = selectedLocomotive.getDecoderType();
      this.decoderTypes.setSelectedItem(decoderType);

      Integer tachoMax = selectedLocomotive.getTachoMax();
      if (tachoMax == null) {
        tachoMax = 100;
      }
      this.tachoMaxSpinner.setValue(tachoMax);

      Integer vMin = selectedLocomotive.getvMin();
      if (vMin == null) {
        vMin = 0;
      }
      this.vMinSpinner.setValue(vMin);

      int functionCount = this.selectedLocomotive.getFunctionCount();
      this.functionCountSpinner.setValue(functionCount);

      Integer velocity = selectedLocomotive.getVelocity();
      Integer richtung = selectedLocomotive.getRichtung();

      boolean commuter = selectedLocomotive.isCommuter();
      this.commuterCB.setSelected(commuter);

      boolean show = selectedLocomotive.isShow();
      this.showCB.setSelected(show);

      boolean synch = selectedLocomotive.isSynchronize();
      this.synchronizeCB.setSelected(synch);

      String commandStationId = selectedLocomotive.getCommandStationId();
    } else {
      //this.imageLbl.setText("ICON");
      this.addressSpinner.setValue(0);
      this.functionCountSpinner.setValue(0);
      this.decoderCB.setSelectedItem("mm_prg");
      this.nameTF.setText("");
      this.commuterCB.setSelected(false);
      this.showCB.setSelected(true);
      this.synchronizeCB.setSelected(false);
    }
    enableFields(selectedLocomotive != null);
  }

  private void enableFields(boolean enable) {
    this.synchPB.setVisible(false);

    this.synchronizeBtn.setEnabled(this.commandStationBean.isLocomotiveSynchronizationSupport());
    this.synchronizeBtn.setVisible(this.commandStationBean.isLocomotiveSynchronizationSupport());

    this.synchronizeCB.setEnabled(this.commandStationBean.isLocomotiveSynchronizationSupport() && enable);

    boolean showUid = selectedLocomotive != null && (DecoderType.MFX == selectedLocomotive.getDecoderType() || DecoderType.MFXP == selectedLocomotive.getDecoderType());
    this.uidNameLbl.setVisible(showUid);
    this.uidLbl.setVisible(showUid);

    boolean enableFields = enable && !this.synchronizeCB.isSelected();
    this.nameTF.setEnabled(enableFields);
    this.addressSpinner.setEnabled(enableFields);
    this.decoderCB.setEnabled(enableFields);
    this.functionCountSpinner.setEnabled(enableFields);
    this.iconTF.setEnabled(enableFields);
    this.iconFileDialogBtn.setEnabled(enableFields);
    this.vMinSpinner.setEnabled(enableFields);
    this.tachoMaxSpinner.setEnabled(enableFields);

    this.saveBtn.setEnabled(!this.synchronizeCB.isSelected() && enable);

    this.showCB.setEnabled(enable);
    this.commuterCB.setEnabled(enable);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings({"unchecked", "deprecation"})
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

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
    locoDetailPanel = new JPanel();
    row0Panel = new JPanel();
    imageLbl = new JLabel();
    row1Panel = new JPanel();
    synchronizeCB = new JCheckBox();
    idNameLbl = new JLabel();
    idLbl = new JLabel();
    uidNameLbl = new JLabel();
    uidLbl = new JLabel();
    row2Panel = new JPanel();
    addressLbl = new JLabel();
    addressSpinner = new JSpinner();
    decoderLabel = new JLabel();
    decoderCB = new JComboBox<>();
    nameLbl = new JLabel();
    nameTF = new JTextField();
    row3Panel = new JPanel();
    iconNameLbl = new JLabel();
    iconTF = new JTextField();
    iconFileDialogBtn = new JButton();
    row4Panel = new JPanel();
    vMinLbl = new JLabel();
    vMinSpinner = new JSpinner();
    tachoMaxLbl = new JLabel();
    tachoMaxSpinner = new JSpinner();
    functionCountLbl = new JLabel();
    functionCountSpinner = new JSpinner();
    row5Panel = new JPanel();
    showCB = new JCheckBox();
    commuterCB = new JCheckBox();
    row6Panel = new JPanel();
    row7Panel = new JPanel();
    row9Panel = new JPanel();
    filler2 = new Box.Filler(new Dimension(0, 50), new Dimension(0, 50), new Dimension(32767, 300));
    buttonPanel = new JPanel();
    westPanel = new JPanel();
    locomotivesSP = new JScrollPane();
    locomotiveList = new JList<>();
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
    newBtn.setToolTipText("Create new Locomotive");
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

    locoDetailPanel.setMinimumSize(new Dimension(390, 540));
    locoDetailPanel.setPreferredSize(new Dimension(480, 500));
    locoDetailPanel.setLayout(new BoxLayout(locoDetailPanel, BoxLayout.Y_AXIS));

    row0Panel.setMinimumSize(new Dimension(380, 30));
    row0Panel.setPreferredSize(new Dimension(380, 50));
    FlowLayout flowLayout8 = new FlowLayout();
    flowLayout8.setAlignOnBaseline(true);
    row0Panel.setLayout(flowLayout8);

    imageLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    imageLbl.setToolTipText("The locomotive image");
    imageLbl.setPreferredSize(new Dimension(128, 48));
    row0Panel.add(imageLbl);

    locoDetailPanel.add(row0Panel);

    row1Panel.setMinimumSize(new Dimension(380, 30));
    row1Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    row1Panel.setLayout(flowLayout1);

    synchronizeCB.setText("Synchronize:");
    synchronizeCB.setToolTipText("Automatically imported from Command Station");
    synchronizeCB.setHorizontalAlignment(SwingConstants.TRAILING);
    synchronizeCB.setHorizontalTextPosition(SwingConstants.LEADING);
    synchronizeCB.setPreferredSize(new Dimension(110, 21));
    synchronizeCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        synchronizeCBActionPerformed(evt);
      }
    });
    row1Panel.add(synchronizeCB);

    idNameLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    idNameLbl.setLabelFor(idNameLbl);
    idNameLbl.setText("Id:");
    idNameLbl.setToolTipText("");
    idNameLbl.setPreferredSize(new Dimension(100, 17));
    row1Panel.add(idNameLbl);

    idLbl.setPreferredSize(new Dimension(85, 17));
    row1Panel.add(idLbl);

    uidNameLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    uidNameLbl.setLabelFor(uidLbl);
    uidNameLbl.setText("UID:");
    uidNameLbl.setToolTipText("MFX UID:");
    uidNameLbl.setPreferredSize(new Dimension(100, 17));
    row1Panel.add(uidNameLbl);

    uidLbl.setPreferredSize(new Dimension(85, 17));
    row1Panel.add(uidLbl);

    locoDetailPanel.add(row1Panel);

    row2Panel.setMinimumSize(new Dimension(380, 30));
    row2Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout3 = new FlowLayout(FlowLayout.LEFT);
    flowLayout3.setAlignOnBaseline(true);
    row2Panel.setLayout(flowLayout3);

    addressLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    addressLbl.setLabelFor(addressSpinner);
    addressLbl.setText("Address:");
    addressLbl.setPreferredSize(new Dimension(100, 16));
    row2Panel.add(addressLbl);

    addressSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
    addressSpinner.setToolTipText("The locomotive address");
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
    row2Panel.add(addressSpinner);

    decoderLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    decoderLabel.setLabelFor(decoderCB);
    decoderLabel.setText("Decoder:");
    decoderLabel.setPreferredSize(new Dimension(100, 16));
    row2Panel.add(decoderLabel);

    decoderCB.setModel(decoderTypes);
    decoderCB.setToolTipText("The Locomotive decoder");
    decoderCB.setDoubleBuffered(true);
    decoderCB.setPreferredSize(new Dimension(85, 26));
    decoderCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        decoderCBActionPerformed(evt);
      }
    });
    row2Panel.add(decoderCB);

    nameLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    nameLbl.setLabelFor(nameTF);
    nameLbl.setText("Name:");
    nameLbl.setPreferredSize(new Dimension(100, 16));
    row2Panel.add(nameLbl);

    nameTF.setToolTipText("Name of the locomotive");
    nameTF.setMinimumSize(new Dimension(175, 26));
    nameTF.setPreferredSize(new Dimension(200, 26));
    nameTF.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        nameTFFocusLost(evt);
      }
    });
    row2Panel.add(nameTF);

    locoDetailPanel.add(row2Panel);

    row3Panel.setMinimumSize(new Dimension(380, 30));
    row3Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout4 = new FlowLayout(FlowLayout.LEFT);
    flowLayout4.setAlignOnBaseline(true);
    row3Panel.setLayout(flowLayout4);

    iconNameLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    iconNameLbl.setText("Icon:");
    iconNameLbl.setPreferredSize(new Dimension(100, 17));
    row3Panel.add(iconNameLbl);

    iconTF.setPreferredSize(new Dimension(375, 26));
    iconTF.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        iconTFFocusLost(evt);
      }
    });
    row3Panel.add(iconTF);

    iconFileDialogBtn.setText("...");
    iconFileDialogBtn.setPreferredSize(new Dimension(26, 26));
    iconFileDialogBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        iconFileDialogBtnActionPerformed(evt);
      }
    });
    row3Panel.add(iconFileDialogBtn);

    locoDetailPanel.add(row3Panel);

    row4Panel.setMinimumSize(new Dimension(380, 30));
    row4Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout5 = new FlowLayout(FlowLayout.LEFT);
    flowLayout5.setAlignOnBaseline(true);
    row4Panel.setLayout(flowLayout5);

    vMinLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    vMinLbl.setLabelFor(vMinSpinner);
    vMinLbl.setText("V min:");
    vMinLbl.setPreferredSize(new Dimension(100, 17));
    row4Panel.add(vMinLbl);

    vMinSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
    vMinSpinner.setPreferredSize(new Dimension(85, 26));
    vMinSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        vMinSpinnerStateChanged(evt);
      }
    });
    row4Panel.add(vMinSpinner);

    tachoMaxLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    tachoMaxLbl.setLabelFor(tachoMaxSpinner);
    tachoMaxLbl.setText("Tacho Max:");
    tachoMaxLbl.setPreferredSize(new Dimension(100, 17));
    row4Panel.add(tachoMaxLbl);

    tachoMaxSpinner.setModel(new SpinnerNumberModel(0, 0, 300, 1));
    tachoMaxSpinner.setPreferredSize(new Dimension(85, 26));
    tachoMaxSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        tachoMaxSpinnerStateChanged(evt);
      }
    });
    row4Panel.add(tachoMaxSpinner);

    functionCountLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    functionCountLbl.setLabelFor(functionCountSpinner);
    functionCountLbl.setText("Nr. of Functions:");
    functionCountLbl.setPreferredSize(new Dimension(100, 17));
    row4Panel.add(functionCountLbl);

    functionCountSpinner.setModel(new SpinnerNumberModel(0, 0, 32, 1));
    functionCountSpinner.setPreferredSize(new Dimension(85, 23));
    functionCountSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        functionCountSpinnerStateChanged(evt);
      }
    });
    row4Panel.add(functionCountSpinner);

    locoDetailPanel.add(row4Panel);

    row5Panel.setMinimumSize(new Dimension(380, 30));
    row5Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout6 = new FlowLayout(FlowLayout.LEFT);
    flowLayout6.setAlignOnBaseline(true);
    row5Panel.setLayout(flowLayout6);

    showCB.setSelected(true);
    showCB.setText("Show:");
    showCB.setToolTipText("Show the Locomotive");
    showCB.setDoubleBuffered(true);
    showCB.setHorizontalAlignment(SwingConstants.TRAILING);
    showCB.setHorizontalTextPosition(SwingConstants.LEADING);
    showCB.setPreferredSize(new Dimension(110, 26));
    showCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showCBActionPerformed(evt);
      }
    });
    row5Panel.add(showCB);

    commuterCB.setText("Commuter:");
    commuterCB.setToolTipText("A commuter Train");
    commuterCB.setDoubleBuffered(true);
    commuterCB.setHorizontalAlignment(SwingConstants.TRAILING);
    commuterCB.setHorizontalTextPosition(SwingConstants.LEADING);
    commuterCB.setPreferredSize(new Dimension(110, 26));
    commuterCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        commuterCBActionPerformed(evt);
      }
    });
    row5Panel.add(commuterCB);

    locoDetailPanel.add(row5Panel);

    row6Panel.setMinimumSize(new Dimension(380, 30));
    row6Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout7 = new FlowLayout(FlowLayout.LEFT);
    flowLayout7.setAlignOnBaseline(true);
    row6Panel.setLayout(flowLayout7);
    locoDetailPanel.add(row6Panel);

    row7Panel.setMinimumSize(new Dimension(380, 30));
    row7Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout9 = new FlowLayout(FlowLayout.LEFT);
    flowLayout9.setAlignOnBaseline(true);
    row7Panel.setLayout(flowLayout9);
    locoDetailPanel.add(row7Panel);

    row9Panel.setMinimumSize(new Dimension(380, 30));
    row9Panel.setPreferredSize(new Dimension(380, 30));
    row9Panel.setLayout(new FlowLayout(FlowLayout.LEFT));
    locoDetailPanel.add(row9Panel);
    locoDetailPanel.add(filler2);

    buttonPanel.setPreferredSize(new Dimension(380, 40));
    FlowLayout flowLayout10 = new FlowLayout();
    flowLayout10.setAlignOnBaseline(true);
    buttonPanel.setLayout(flowLayout10);
    locoDetailPanel.add(buttonPanel);

    add(locoDetailPanel, BorderLayout.CENTER);

    westPanel.setMinimumSize(new Dimension(175, 500));
    westPanel.setPreferredSize(new Dimension(175, 500));
    westPanel.setLayout(new BorderLayout());

    locomotivesSP.setPreferredSize(new Dimension(175, 130));

    locomotiveList.setModel(locoListModel);
    locomotiveList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        locomotiveListValueChanged(evt);
      }
    });
    locomotivesSP.setViewportView(locomotiveList);

    westPanel.add(locomotivesSP, BorderLayout.CENTER);

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
    Logger.trace("Create new Loco...");
    LocomotiveBean newLoco = new LocomotiveBean();
    newLoco.setCommandStationId(commandStationBean.getId());
    newLoco.setRichtung(0);
    newLoco.setVelocity(0);
    newLoco.setImported("Manual Inserted");

    if (this.commandStationBean.getSupportedProtocols().size() == 1) {
      newLoco.setDecoderTypeString(this.commandStationBean.getProtocols().toLowerCase());
    }

    newLoco.setShow(true);
    this.locoListModel.add(newLoco);
    this.locomotiveList.setSelectedValue(newLoco, true);
    this.selectedLocomotive = newLoco;
    enableFields(this.selectedLocomotive != null);

    setFieldValues();
  }//GEN-LAST:event_newBtnActionPerformed

  private LocomotiveBean setLocomotiveValues(LocomotiveBean locomotiveBean) {
    if (locomotiveBean.getId() == null) {
      long id = (long) locomotiveBean.getAddress();
      locomotiveBean.setId(id);
      locomotiveBean.setUid(id);
    }

    if (locomotiveBean.getvMin() == null) {
      locomotiveBean.setvMin((Integer) vMinSpinner.getValue());
    }

    if (locomotiveBean.getTachoMax() == null) {
      locomotiveBean.setTachoMax((Integer) tachoMaxSpinner.getValue());
    }

    locomotiveBean.setImported("Manual Updated");
    locomotiveBean = crudFunctionBeans(locomotiveBean);

    return locomotiveBean;
  }

  private void saveBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
    if (this.selectedLocomotive != null) {
      setLocomotiveValues(selectedLocomotive);

      Logger.trace("Saving Loco: " + selectedLocomotive);

      selectedLocomotive = PersistenceFactory.getService().persist(selectedLocomotive);
      initModels();
      locomotiveList.setSelectedValue(selectedLocomotive, true);
    }
  }//GEN-LAST:event_saveBtnActionPerformed

  private void deleteBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
    Logger.trace("Delete Loco: " + this.selectedLocomotive);
    PersistenceFactory.getService().remove(selectedLocomotive);
    this.selectedLocomotive = null;
    this.initModels();
  }//GEN-LAST:event_deleteBtnActionPerformed

  private String storeImage(String imageName, Image image, boolean locomotive) {
    String csp = commandStationBean.getShortName().toLowerCase();
    String basePath = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + csp;

    Path path;
    if (locomotive) {
      path = Paths.get(basePath);
    } else {
      path = Paths.get(basePath + File.separator + "zfunctions");
    }
    File imageFile = new File(path + File.separator + imageName.toLowerCase() + ".png");

    try {
      if (!Files.exists(path)) {
        Files.createDirectories(path);
        Logger.trace("Created new directory " + path);
      }
      ImageIO.write((BufferedImage) image, "png", imageFile);

      return imageFile.getAbsolutePath();
    } catch (IOException ex) {
      Logger.error("Can't store image " + imageFile + "! ", ex.getMessage());
      return null;
    }
  }

  private void refreshBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
    this.initModels();
  }//GEN-LAST:event_refreshBtnActionPerformed

    private void synchronizeBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_synchronizeBtnActionPerformed
      Logger.trace(evt.getActionCommand());
      this.synchronizeBtn.setEnabled(false);
      this.locomotiveList.setEnabled(true);

      this.synchPB.setValue(0);
      this.synchPB.setIndeterminate(true);
      this.synchPB.setVisible(true);

      task = new SynchronizationTask();
      task.addPropertyChangeListener(this);
      task.execute();
    }//GEN-LAST:event_synchronizeBtnActionPerformed

  private void locomotiveListValueChanged(ListSelectionEvent evt) {//GEN-FIRST:event_locomotiveListValueChanged
    if (!evt.getValueIsAdjusting()) {
      Logger.trace(this.locomotiveList.getSelectedValue());

      this.selectedLocomotive = this.locomotiveList.getSelectedValue();
      this.setFieldValues();
    }
  }//GEN-LAST:event_locomotiveListValueChanged

  private void iconFileDialogBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_iconFileDialogBtnActionPerformed
    Logger.trace(evt.getActionCommand());
    JFrame parentFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
    IconFileChooser fileDialog = new IconFileChooser(parentFrame, true);
    fileDialog.setVisible(true);

    File iconFile = fileDialog.getSelectedIconFile();
    if (iconFile != null) {
      iconTF.setText(iconFile.getPath());
      this.selectedLocomotive.setIcon(iconFile.getPath());

      //try to show the image also
      Image img = PersistenceFactory.getService().readImage(iconFile.getPath(), false);
      if (img != null) {
        img = PersistenceFactory.getService().getLocomotiveImage(iconFile.getPath());
        this.selectedLocomotive.setLocIcon(img);
        this.imageLbl.setIcon(new ImageIcon(img));
      }
    }
  }//GEN-LAST:event_iconFileDialogBtnActionPerformed

  private void nameTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_nameTFFocusLost
    this.selectedLocomotive.setName(this.nameTF.getText());
  }//GEN-LAST:event_nameTFFocusLost

  private void commuterCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_commuterCBActionPerformed
    this.selectedLocomotive.setCommuter(this.commuterCB.isSelected());
    if (this.selectedLocomotive.getId() != null) {
      PersistenceFactory.getService().persist(this.selectedLocomotive);
    }
  }//GEN-LAST:event_commuterCBActionPerformed

  private void showCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showCBActionPerformed
    this.selectedLocomotive.setShow(this.showCB.isSelected());
    if (this.selectedLocomotive.getId() != null) {
      PersistenceFactory.getService().persist(this.selectedLocomotive);
    }
  }//GEN-LAST:event_showCBActionPerformed

  private void synchronizeCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_synchronizeCBActionPerformed
    enableFields(this.selectedLocomotive != null);
  }//GEN-LAST:event_synchronizeCBActionPerformed

  private void addressSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_addressSpinnerStateChanged
    if(this.selectedLocomotive != null) {
      this.selectedLocomotive.setAddress((Integer) this.addressSpinner.getValue());
      long uid = this.selectedLocomotive.getAddress();
      this.selectedLocomotive.setUid(uid);
    }
  }//GEN-LAST:event_addressSpinnerStateChanged

  private void decoderCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_decoderCBActionPerformed
    this.selectedLocomotive.setDecoderTypeString(this.decoderCB.getSelectedItem().toString().toLowerCase());
    Logger.trace("Protocol is " + this.decoderCB.getSelectedItem().toString());
  }//GEN-LAST:event_decoderCBActionPerformed

  private void iconTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_iconTFFocusLost
    this.selectedLocomotive.setIcon(this.iconTF.getText());
    if (this.selectedLocomotive.getIcon() != null) {
      String icon = this.selectedLocomotive.getIcon();
      Image locIcon = PersistenceFactory.getService().getLocomotiveImage(icon);
      if (locIcon != null) {
        this.selectedLocomotive.setLocIcon(locIcon);
        this.imageLbl.setIcon(new ImageIcon(locIcon));
        this.imageLbl.setText("");
      } else {
        this.imageLbl.setIcon(null);
        this.imageLbl.setText("");
      }
    }
  }//GEN-LAST:event_iconTFFocusLost

  private void vMinSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_vMinSpinnerStateChanged
    Integer vMin = (Integer) vMinSpinner.getValue();
    this.selectedLocomotive.setvMin(vMin);
  }//GEN-LAST:event_vMinSpinnerStateChanged

  private void tachoMaxSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_tachoMaxSpinnerStateChanged
    Integer tachoMax = (Integer) tachoMaxSpinner.getValue();
    this.selectedLocomotive.setTachoMax(tachoMax);
  }//GEN-LAST:event_tachoMaxSpinnerStateChanged

  private void functionCountSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_functionCountSpinnerStateChanged

  }//GEN-LAST:event_functionCountSpinnerStateChanged

  private LocomotiveBean crudFunctionBeans(LocomotiveBean locomotiveBean) {
    int functionCount = (int) this.functionCountSpinner.getValue();

    long locomotiveId;
    if (locomotiveBean.getId() != null) {
      locomotiveId = locomotiveBean.getId();
    } else {
      locomotiveId = locomotiveBean.getAddress();
    }

    Map<Integer, FunctionBean> functions = locomotiveBean.getFunctions();
    if (functionCount < functions.size()) {
      //Remove the function at the end so
      int difFn = functions.size() - functionCount;
      int startIdx = functions.size() - difFn;
      int max = functions.size();
      for (int i = startIdx; i < max; i++) {
        Logger.trace("Removing F" + i);
        functions.remove(i);
      }
    }

    if (functionCount > functions.size()) {
      for (int i = 0; i < functionCount; i++) {
        //Check if the function exists
        if (functions.containsKey(i)) {
          //Logger.trace("Function F" + i + " exists");
        } else {
          //Logger.trace("Function F" + i + " created");
          int functionType = 50+ i;
          FunctionBean fb = new FunctionBean(locomotiveId, i,functionType, 0);
          functions.put(i, fb);
        }
      }
    }
    return locomotiveBean;
  }

  class LocomotiveBeanByNameSorter implements Comparator<LocomotiveBean> {

    @Override
    public int compare(LocomotiveBean a, LocomotiveBean b) {
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

  class LocomotiveBeanListModel extends AbstractListModel<LocomotiveBean> {

    private final List<LocomotiveBean> model;

    public LocomotiveBeanListModel() {
      model = new ArrayList<>();
    }

    @Override
    public int getSize() {
      return model.size();
    }

    @Override
    public LocomotiveBean getElementAt(int index) {
      return (LocomotiveBean) model.toArray()[index];
    }

    public void add(LocomotiveBean element) {
      if (model.add(element)) {
        Collections.sort(model, new LocomotiveBeanByNameSorter());

        fireContentsChanged(this, 0, getSize());
      }
    }

    public void addAll(LocomotiveBean elements[]) {
      Collection<LocomotiveBean> c = Arrays.asList(elements);
      model.addAll(c);
      Collections.sort(model, new LocomotiveBeanByNameSorter());

      fireContentsChanged(this, 0, getSize());
    }

    public void addAll(Collection<LocomotiveBean> elements) {
      model.addAll(elements);
      Collections.sort(model, new LocomotiveBeanByNameSorter());

      fireContentsChanged(this, 0, getSize());
    }

    public void clear() {
      model.clear();
      fireContentsChanged(this, 0, getSize());
    }

    public boolean contains(LocomotiveBean element) {
      return model.contains(element);
    }

    public LocomotiveBean firstElement() {
      if (!model.isEmpty()) {
        return model.get(0);
      } else {
        return null;
      }
    }

    public Iterator<LocomotiveBean> iterator() {
      return model.iterator();
    }

    public LocomotiveBean lastElement() {
      if (!model.isEmpty()) {
        return model.get(model.size() - 1);
      } else {
        return null;
      }
    }

    public boolean removeElement(LocomotiveBean element) {
      boolean removed = model.remove(element);
      if (removed) {
        fireContentsChanged(this, 0, getSize());
      }
      return removed;
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
      if (!locoListModel.contains((LocomotiveBean) evt.getNewValue())) {
        locoListModel.add((LocomotiveBean) evt.getNewValue());
      }
      this.locomotiveList.setSelectedValue(evt.getNewValue(), true);
    }

    if ("done".equals(evt.getPropertyName())) {
      Logger.trace("Done: " + evt.getNewValue());
      //this.connectionTestResultLbl.setText((String) evt.getNewValue());
      this.synchPB.setVisible(false);
      this.locomotiveList.setEnabled(true);
      locomotiveList.clearSelection();
    }
  }

  class SynchronizationTask extends SwingWorker<Void, Void> {

    @Override
    public Void doInBackground() {
      setProgress(0);

      DecoderController decoderController = ControllerFactory.getDecoderController(commandStationBean, true);
      if (!decoderController.isConnected()) {
        decoderController.connect();
      }

      List<LocomotiveBean> fromController = decoderController.getLocomotives();
      String importedFrom = commandStationBean.getShortName();
      Set<String> functionImageNames = new HashSet<>();

      int locCount = fromController.size();
      int processedCount = 0;

      for (LocomotiveBean loco : fromController) {
        Long id = loco.getId();
        LocomotiveBean dbLoco = PersistenceFactory.getService().getLocomotive(id);
        boolean store = true;
        if (dbLoco != null && loco.getId().equals(dbLoco.getId())) {
          if (dbLoco.isSynchronize()) {
            Logger.trace("Loco id: " + loco.getId() + ", " + loco.getName() + " Addres: " + loco.getAddress() + " Decoder: " + loco.getDecoderTypeString() + " Exists");
            loco.setCommuter(dbLoco.isCommuter());
            loco.setShow(dbLoco.isShow());
            loco.setImported(importedFrom);
            loco.setSynchronize(true);
          } else {
            Logger.trace("Skip Loco id: " + loco.getId() + ", " + loco.getName() + " Addres: " + loco.getAddress() + " Decoder: " + loco.getDecoderTypeString() + " Exists");
            store = false;
          }
        } else {
          Logger.trace("New Loco, id:" + loco.getId() + ", " + loco.getName() + " Addres: " + loco.getAddress() + " Decoder: " + loco.getDecoderTypeString());
          loco.setSynchronize(true);
          loco.setImported(importedFrom);
          loco.setShow(true);
        }

        if (store && commandStationBean.isLocomotiveImageSynchronizationSupport()) {
          try {
            String icon = loco.getIcon();
            Image locImage = decoderController.getLocomotiveImage(icon);
            String iconPath = storeImage(icon, locImage, true);
            loco.setIcon(iconPath);
            Logger.trace("Loc Image path: " + iconPath);
          } catch (Exception e) {
            Logger.error(e);
          }
        }

        if (store && commandStationBean.isLocomotiveFunctionSynchronizationSupport()) {
          try {
            //Function icons...
            Set<FunctionBean> functions = loco.getFunctions().values().stream().collect(Collectors.toSet());
            for (FunctionBean fb : functions) {
              String aIcon = fb.getActiveIcon();
              String iIcon = fb.getInActiveIcon();
              functionImageNames.add(aIcon);
              functionImageNames.add(iIcon);
            }
          } catch (Exception e) {
            Logger.error(e);
          }

          for (String functionImage : functionImageNames) {
            Image functionIcon = decoderController.getLocomotiveFunctionImage(functionImage);
            if (functionIcon != null) {
              storeImage(functionImage, functionIcon, false);
            } else {
              Logger.trace("Function Image " + functionImage + " is NOT available");
            }
          }
        }

        if (store) {
          PersistenceFactory.getService().persist(loco);
          firePropertyChange("updated", null, loco);
        }
        processedCount++;

        double progress = (double) processedCount / locCount * 100;
        setProgress((int) progress);
      }

      firePropertyChange("done", "", "Locomotives Synchronized");

      return null;
    }

    @Override
    public void done() {
      initModels();
      synchronizeBtn.setEnabled(commandStationBean.isLocomotiveSynchronizationSupport());

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

      LocomotivePreferencesPanel testPanel = new LocomotivePreferencesPanel();
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
  JLabel addressLbl;
  JSpinner addressSpinner;
  JPanel bottomPanel;
  JPanel buttonPanel;
  JLabel commandStationLbl;
  JLabel commandStationNameLbl;
  JCheckBox commuterCB;
  JComboBox<DecoderType> decoderCB;
  JLabel decoderLabel;
  JButton deleteBtn;
  Box.Filler filler1;
  Box.Filler filler2;
  JLabel functionCountLbl;
  JSpinner functionCountSpinner;
  JButton iconFileDialogBtn;
  JLabel iconNameLbl;
  JTextField iconTF;
  JLabel idLbl;
  JLabel idNameLbl;
  JLabel imageLbl;
  JPanel leftPanel;
  JPanel locoDetailPanel;
  JList<LocomotiveBean> locomotiveList;
  JScrollPane locomotivesSP;
  JLabel nameLbl;
  JTextField nameTF;
  JButton newBtn;
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
  JCheckBox showCB;
  JProgressBar synchPB;
  JButton synchronizeBtn;
  JCheckBox synchronizeCB;
  JLabel tachoMaxLbl;
  JSpinner tachoMaxSpinner;
  JPanel topPanel;
  JLabel uidLbl;
  JLabel uidNameLbl;
  JLabel vMinLbl;
  JSpinner vMinSpinner;
  JPanel westPanel;
  // End of variables declaration//GEN-END:variables
}
