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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jcs.JCS;
import jcs.entities.CommandStationBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * Welke properties zijn nodig voor een locomotief?<br>
 * command station id<br>
 * adress, decoder, naam, image, min speed, tacho max<br>
 * uid (cs 3) is die geimporteerd of met de hand gemaakt<br>
 * moet die in de throttle worden getoond is het een commuter
 *
 *
 * @author frans
 */
public class LocomotivePreferencesPanel extends JPanel {

  private final LocomotiveBeanListModel locoListModel;
  private CommandStationBean commandStationBean;
  private LocomotiveBean selectedLocomotive;

  /**
   * Creates new form LocomotivePanel
   */
  public LocomotivePreferencesPanel() {
    locoListModel = new LocomotiveBeanListModel();
    initComponents();
    initModels();
  }

  private void initModels() {
    if (PersistenceFactory.getService() != null) {
      commandStationBean = PersistenceFactory.getService().getDefaultCommandStation();
      this.commandStationLbl.setText(commandStationBean.getDescription());

      List<LocomotiveBean> locos = PersistenceFactory.getService().getLocomotivesByCommandStation(commandStationBean.getId());
      this.locoListModel.addAll(locos);
      this.synchronizeBtn.setVisible(commandStationBean.isLocomotiveSynchronizationSupport());

      setFieldValues();
      enableFields(selectedLocomotive != null);
    }
  }

  private void setFieldValues() {
    if (selectedLocomotive != null) {
      Long id = selectedLocomotive.getId();
      String name = selectedLocomotive.getName();
      
      //String previousName;
      Long uid = selectedLocomotive.getUid();
      
      //Long mfxUid = selectedLocomotive.getMfxUid();
      Integer address = selectedLocomotive.getAddress();
      String icon = selectedLocomotive.getIcon();
      String decoderTypeString = selectedLocomotive.getDecoderTypeString();
      
      //String mfxSid = selectedLocomotive.getMfxSid();
      
      Integer tachoMax = selectedLocomotive.getTachoMax();
      Integer vMin = selectedLocomotive.getvMin();
      
      //Integer accelerationDelay = selectedLocomotive.getAccelerationDelay();
      //Integer brakeDelay = selectedLocomotive.getBrakeDelay();
      //Integer volume = selectedLocomotive.getVolume();
      //String spm = selectedLocomotive.getSpm();
      
      Integer velocity = selectedLocomotive.getVelocity();
      Integer richtung = selectedLocomotive.getRichtung();
      
      //String mfxType = selectedLocomotive.getMfxType();
      boolean commuter = selectedLocomotive.isCommuter();
      
      Integer length = selectedLocomotive.getLength();
      //String block = selectedLocomotive.getBlock();
      boolean show = selectedLocomotive.isShow();
      //String source= selectedLocomotive;
      String commandStationId = selectedLocomotive.getCommandStationId();
      //boolean manual= selectedLocomotive;

      if (selectedLocomotive.getLocIcon() != null) {
        //this.imageLbl.setIcon(new ImageIcon(selectedLocomotive.getIcon()));
        this.imageLbl.setText("");
      } else {
        //this.iconTF.setText(selectedLocomotive.getIcon());
      }
      this.nameTF.setText(selectedLocomotive.getName());

      this.addressSpinner.setValue(selectedLocomotive.getAddress());
      this.decoderCB.setSelectedItem(selectedLocomotive.getDecoderTypeString());

      if (selectedLocomotive.getvMin() != null) {
        this.vMinSpinner.setValue(selectedLocomotive.getvMin());
      }
      if (selectedLocomotive.getTachoMax() != null) {
        this.tachoMaxSpinner.setValue(selectedLocomotive.getTachoMax());
      }

      if (selectedLocomotive.getLength() != null) {
        this.lengthSpinner.setValue(selectedLocomotive.getLength());
      } else {
        this.lengthSpinner.setValue(0);
      }
      this.commuterCB.setSelected(selectedLocomotive.isCommuter());
      this.showCB.setSelected(selectedLocomotive.isShow());

    } else {
      this.imageLbl.setText("ICON");

      this.addressSpinner.setValue(0);
      this.decoderCB.setSelectedItem("mm_prg");
      this.nameTF.setText("");

      this.lengthSpinner.setValue(0);
      this.commuterCB.setSelected(false);
      this.showCB.setSelected(true);

    }

  }

  private void enableFields(boolean enable) {

  }

//  protected void setComponentValues(LocomotiveBean loco) {
//    if (loco != null) {
//      if (loco.getLocIcon() != null) {
//        this.imageLbl.setIcon(new ImageIcon(loco.getLocIcon()));
//        this.imageLbl.setText("");
//      } else {
//        this.iconTF.setText(loco.getIcon());
//      }
//      this.nameTF.setText(loco.getName());
//
//      this.addressSpinner.setValue(loco.getAddress());
//      this.decoderCB.setSelectedItem(loco.getDecoderTypeString());
//
//      if (loco.getvMin() != null) {
//        this.vMinSpinner.setValue(loco.getvMin());
//      }
//      if (loco.getTachoMax() != null) {
//        this.tachoMaxSpinner.setValue(loco.getTachoMax());
//      }
//
//      if (loco.getLength() != null) {
//        this.lengthSpinner.setValue(loco.getLength());
//      } else {
//        this.lengthSpinner.setValue(0);
//      }
//      this.commuterCB.setSelected(loco.isCommuter());
//      this.showCB.setSelected(loco.isShow());
//
//    } else {
//      this.imageLbl.setText("ICON");
//
//      this.addressSpinner.setValue(0);
//      this.decoderCB.setSelectedItem("mm_prg");
//      this.nameTF.setText("");
//
//      this.lengthSpinner.setValue(0);
//      this.commuterCB.setSelected(false);
//      this.showCB.setSelected(true);
//
//    }
//  }
  private LocomotiveBean getLocomotiveFromPersistentStore(LocomotiveBean locomotive) {
    LocomotiveBean loco;
    if (locomotive.getId() != null) {
      loco = PersistenceFactory.getService().getLocomotive(locomotive.getId());
    } else {
      loco = PersistenceFactory.getService().getLocomotive(locomotive.getAddress(), locomotive.getDecoderType());
    }
    return loco;
  }

  protected LocomotiveBean setLocomotiveValues(LocomotiveBean loco) {
    boolean show = this.showCB.isSelected();
    boolean commuter = this.commuterCB.isSelected();
    String name = this.nameTF.getText();

    Integer length = (Integer) this.lengthSpinner.getValue();

    LocomotiveBean loc;
    if (loco != null) {
      loc = loco;
    } else {
      loc = new LocomotiveBean();
    }

    //loc.setName(name);
    loc.setLength(length);
    loc.setShow(show);
    loc.setCommuter(commuter);

    return loc;
  }

//  public void refresh() {
//    this.selectedLocomotive = null;
//  }
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
    synchronizeBtn = new JButton();
    newBtn = new JButton();
    refreshBtn = new JButton();
    locoDetailPanel = new JPanel();
    row1Panel = new JPanel();
    synchronizedCB = new JCheckBox();
    imageLbl = new JLabel();
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
    row5Panel = new JPanel();
    filler3 = new Box.Filler(new Dimension(100, 0), new Dimension(100, 0), new Dimension(100, 32767));
    showCB = new JCheckBox();
    filler4 = new Box.Filler(new Dimension(40, 0), new Dimension(95, 0), new Dimension(40, 32767));
    commuterCB = new JCheckBox();
    row6Panel = new JPanel();
    lengthLbl = new JLabel();
    lengthSpinner = new JSpinner();
    row7Panel = new JPanel();
    row8Panel = new JPanel();
    row9Panel = new JPanel();
    idNameLbl = new JLabel();
    idLbl = new JLabel();
    uidNameLbl = new JLabel();
    uidLbl = new JLabel();
    filler2 = new Box.Filler(new Dimension(0, 50), new Dimension(0, 50), new Dimension(32767, 300));
    buttonPanel = new JPanel();
    westPanel = new JPanel();
    locomotivesSP = new JScrollPane();
    locomotiveList = new JList<>();
    bottomPanel = new JPanel();
    deleteBtn = new JButton();
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

    synchronizeBtn.setIcon(new ImageIcon(getClass().getResource("/media/CS2-3-Sync.png"))); // NOI18N
    synchronizeBtn.setToolTipText("Synchronize Locomotives met CS 2/3");
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

    row1Panel.setMinimumSize(new Dimension(380, 30));
    row1Panel.setPreferredSize(new Dimension(380, 50));
    FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    row1Panel.setLayout(flowLayout1);

    synchronizedCB.setText("Synchronized");
    synchronizedCB.setToolTipText("Automatically imported from Command Station");
    synchronizedCB.setHorizontalTextPosition(SwingConstants.LEADING);
    synchronizedCB.setPreferredSize(new Dimension(100, 21));
    synchronizedCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        synchronizedCBActionPerformed(evt);
      }
    });
    row1Panel.add(synchronizedCB);

    imageLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    imageLbl.setToolTipText("The locomotive image");
    imageLbl.setPreferredSize(new Dimension(128, 48));
    row1Panel.add(imageLbl);

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

    addressSpinner.setModel(new SpinnerNumberModel());
    addressSpinner.setToolTipText("The locomotive address");
    addressSpinner.setDoubleBuffered(true);
    addressSpinner.setEditor(new JSpinner.NumberEditor(addressSpinner, ""));
    addressSpinner.setMinimumSize(new Dimension(50, 26));
    addressSpinner.setName(""); // NOI18N
    addressSpinner.setNextFocusableComponent(nameTF);
    addressSpinner.setPreferredSize(new Dimension(85, 26));
    row2Panel.add(addressSpinner);

    decoderLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    decoderLabel.setLabelFor(decoderCB);
    decoderLabel.setText("Decoder:");
    decoderLabel.setPreferredSize(new Dimension(100, 16));
    row2Panel.add(decoderLabel);

    decoderCB.setModel(new DefaultComboBoxModel<>(new String[] { "mm_prg", "mfx", "dcc", "sx1" }));
    decoderCB.setToolTipText("The Locomotive decoder");
    decoderCB.setDoubleBuffered(true);
    decoderCB.setPreferredSize(new Dimension(85, 26));
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

    iconTF.setPreferredSize(new Dimension(300, 26));
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
    vMinLbl.setText("V min:");
    vMinLbl.setPreferredSize(new Dimension(100, 17));
    row4Panel.add(vMinLbl);

    vMinSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
    vMinSpinner.setPreferredSize(new Dimension(85, 26));
    row4Panel.add(vMinSpinner);

    tachoMaxLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    tachoMaxLbl.setLabelFor(tachoMaxSpinner);
    tachoMaxLbl.setText("Tacho Max:");
    tachoMaxLbl.setPreferredSize(new Dimension(100, 17));
    row4Panel.add(tachoMaxLbl);

    tachoMaxSpinner.setModel(new SpinnerNumberModel(0, 0, 300, 1));
    tachoMaxSpinner.setPreferredSize(new Dimension(85, 26));
    row4Panel.add(tachoMaxSpinner);

    locoDetailPanel.add(row4Panel);

    row5Panel.setMinimumSize(new Dimension(380, 30));
    row5Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout6 = new FlowLayout(FlowLayout.LEFT);
    flowLayout6.setAlignOnBaseline(true);
    row5Panel.setLayout(flowLayout6);
    row5Panel.add(filler3);

    showCB.setSelected(true);
    showCB.setText("Show");
    showCB.setToolTipText("Show the Locomotive");
    showCB.setDoubleBuffered(true);
    showCB.setPreferredSize(new Dimension(85, 26));
    showCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showCBActionPerformed(evt);
      }
    });
    row5Panel.add(showCB);
    row5Panel.add(filler4);

    commuterCB.setText("Commuter");
    commuterCB.setToolTipText("A commuter Train");
    commuterCB.setDoubleBuffered(true);
    commuterCB.setPreferredSize(new Dimension(85, 26));
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

    lengthLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    lengthLbl.setLabelFor(lengthSpinner);
    lengthLbl.setText("Length");
    lengthLbl.setPreferredSize(new Dimension(100, 16));
    row6Panel.add(lengthLbl);

    lengthSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
    lengthSpinner.setDoubleBuffered(true);
    lengthSpinner.setPreferredSize(new Dimension(85, 26));
    row6Panel.add(lengthSpinner);

    locoDetailPanel.add(row6Panel);

    row7Panel.setMinimumSize(new Dimension(380, 30));
    row7Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout9 = new FlowLayout(FlowLayout.LEFT);
    flowLayout9.setAlignOnBaseline(true);
    row7Panel.setLayout(flowLayout9);
    locoDetailPanel.add(row7Panel);

    row8Panel.setMinimumSize(new Dimension(380, 30));
    row8Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout8 = new FlowLayout(FlowLayout.LEFT);
    flowLayout8.setAlignOnBaseline(true);
    row8Panel.setLayout(flowLayout8);
    locoDetailPanel.add(row8Panel);

    row9Panel.setMinimumSize(new Dimension(380, 30));
    row9Panel.setPreferredSize(new Dimension(380, 30));
    row9Panel.setLayout(new FlowLayout(FlowLayout.LEFT));

    idNameLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    idNameLbl.setText("Id:");
    idNameLbl.setPreferredSize(new Dimension(100, 17));
    row9Panel.add(idNameLbl);

    idLbl.setPreferredSize(new Dimension(85, 17));
    row9Panel.add(idLbl);

    uidNameLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    uidNameLbl.setText("UID:");
    uidNameLbl.setToolTipText("MFX UID:");
    uidNameLbl.setPreferredSize(new Dimension(100, 17));
    row9Panel.add(uidNameLbl);

    uidLbl.setPreferredSize(new Dimension(85, 17));
    row9Panel.add(uidLbl);

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
    bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    deleteBtn.setIcon(new ImageIcon(getClass().getResource("/media/delete-24.png"))); // NOI18N
    deleteBtn.setText("Delete");
    deleteBtn.setEnabled(false);
    deleteBtn.setMaximumSize(new Dimension(100, 36));
    deleteBtn.setMinimumSize(new Dimension(100, 36));
    deleteBtn.setPreferredSize(new Dimension(100, 36));
    deleteBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        deleteBtnActionPerformed(evt);
      }
    });
    bottomPanel.add(deleteBtn);
    bottomPanel.add(filler1);

    saveBtn.setIcon(new ImageIcon(getClass().getResource("/media/save-24.png"))); // NOI18N
    saveBtn.setText("Save");
    saveBtn.setMaximumSize(new Dimension(100, 36));
    saveBtn.setMinimumSize(new Dimension(100, 36));
    saveBtn.setPreferredSize(new Dimension(100, 36));
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

    //this.setComponentValues(selectedLocomotive);
  }//GEN-LAST:event_newBtnActionPerformed


  private void saveBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
    if (this.selectedLocomotive != null) {
      LocomotiveBean loco = getLocomotiveFromPersistentStore(selectedLocomotive);
      loco = setLocomotiveValues(loco);
      Logger.trace("Save the Loco: " + this.selectedLocomotive);

      selectedLocomotive = this.selectedLocomotive = PersistenceFactory.getService().persist(loco);
      //setComponentValues(selectedLocomotive);
    }
  }//GEN-LAST:event_saveBtnActionPerformed

  private void deleteBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
    Logger.trace("Delete Loco: " + this.selectedLocomotive);
    PersistenceFactory.getService().remove(selectedLocomotive);
    this.selectedLocomotive = null;
    //this.setComponentValues(selectedLocomotive);
  }//GEN-LAST:event_deleteBtnActionPerformed

  private void synchronize() {
    JCS.getJcsCommandStation().synchronizeLocomotivesWithCommandStation(null);

    //refresh();
  }

  private void refreshBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
    //refresh();
  }//GEN-LAST:event_refreshBtnActionPerformed

    private void synchronizeBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_synchronizeBtnActionPerformed
      synchronize();
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
    LocomotiveIconFileChooser fileDialog = new LocomotiveIconFileChooser(parentFrame, true);
    fileDialog.setVisible(true);

    File iconFile = fileDialog.getSelectedIconFile();
    if (iconFile != null) {
      iconTF.setText(iconFile.getPath());

      //try to show the image also
      Image img = PersistenceFactory.getService().readImage(iconFile.getPath(), false);
      if (img != null) {
        img = PersistenceFactory.getService().getLocomotiveImage(iconFile.getPath());

        this.imageLbl.setIcon(new ImageIcon(img));
      }
    }
  }//GEN-LAST:event_iconFileDialogBtnActionPerformed

  private void nameTFFocusLost(FocusEvent evt) {//GEN-FIRST:event_nameTFFocusLost
    // TODO add your handling code here:
  }//GEN-LAST:event_nameTFFocusLost

  private void commuterCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_commuterCBActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_commuterCBActionPerformed

  private void showCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showCBActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_showCBActionPerformed

  private void synchronizedCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_synchronizedCBActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_synchronizedCBActionPerformed

  class LocomotiveBeanByNameSorter implements Comparator<LocomotiveBean> {

    @Override
    public int compare(LocomotiveBean a, LocomotiveBean b) {
      return a.getName().compareTo(b.getName());
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
  JComboBox<String> decoderCB;
  JLabel decoderLabel;
  JButton deleteBtn;
  Box.Filler filler1;
  Box.Filler filler2;
  Box.Filler filler3;
  Box.Filler filler4;
  JButton iconFileDialogBtn;
  JLabel iconNameLbl;
  JTextField iconTF;
  JLabel idLbl;
  JLabel idNameLbl;
  JLabel imageLbl;
  JPanel leftPanel;
  JLabel lengthLbl;
  JSpinner lengthSpinner;
  JPanel locoDetailPanel;
  JList<LocomotiveBean> locomotiveList;
  JScrollPane locomotivesSP;
  JLabel nameLbl;
  JTextField nameTF;
  JButton newBtn;
  JButton refreshBtn;
  JPanel rightPanel;
  JPanel row1Panel;
  JPanel row2Panel;
  JPanel row3Panel;
  JPanel row4Panel;
  JPanel row5Panel;
  JPanel row6Panel;
  JPanel row7Panel;
  JPanel row8Panel;
  JPanel row9Panel;
  JButton saveBtn;
  JCheckBox showCB;
  JButton synchronizeBtn;
  JCheckBox synchronizedCB;
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
