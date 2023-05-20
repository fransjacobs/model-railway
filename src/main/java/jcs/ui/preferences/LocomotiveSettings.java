/*
 * Copyright 2023 frans.
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
package jcs.ui.preferences;

import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.entities.LocomotiveBean;
import jcs.entities.enums.DecoderType;
import jcs.persistence.PersistenceFactory;
import jcs.ui.util.IdNameProperty;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class LocomotiveSettings extends javax.swing.JPanel {

  private LocomotiveBean locomotiveBean;

  /**
   * Creates new form LocomotiveSettings
   */
  public LocomotiveSettings() {
    locomotiveBean = new LocomotiveBean();
    initComponents();
  }

  private static ListModel<IdNameProperty> refreshListModel() {
    DefaultListModel<IdNameProperty> model = new DefaultListModel<>();

    if (PersistenceFactory.getService() != null) {
      List<LocomotiveBean> locomotives = PersistenceFactory.getService().getLocomotives();
      for (LocomotiveBean loc : locomotives) {
        IdNameProperty loco = new IdNameProperty(loc.getId(), loc.getName());
        model.addElement(loco);
      }
    }
    return model;
  }

  private static ComboBoxModel<DecoderType> getDecoderTypeModel() {
    DefaultComboBoxModel<DecoderType> model = new DefaultComboBoxModel<>();
    model.addElement(DecoderType.MM);
    model.addElement(DecoderType.MFX);
    model.addElement(DecoderType.DCC);
    model.addElement(DecoderType.SX1);
    return model;
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
   * method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    mainvBox = new jcs.ui.swing.layout.VBox();
    mainhBox = new jcs.ui.swing.layout.HBox();
    locListSP = new javax.swing.JScrollPane();
    locList = new javax.swing.JList<>();
    detailsPanel = new javax.swing.JPanel();
    refreshSynchPanel = new javax.swing.JPanel();
    synchBtn = new javax.swing.JButton();
    refreshBtn = new javax.swing.JButton();
    comShowIconPanel = new javax.swing.JPanel();
    filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(100, 0), new java.awt.Dimension(100, 0), new java.awt.Dimension(100, 32767));
    commuterCB = new javax.swing.JCheckBox();
    filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(30, 0), new java.awt.Dimension(30, 0), new java.awt.Dimension(15, 32767));
    showCB = new javax.swing.JCheckBox();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(40, 0), new java.awt.Dimension(40, 0), new java.awt.Dimension(40, 32767));
    locIconLbl = new javax.swing.JLabel();
    decoderTypeAddressPanel = new javax.swing.JPanel();
    decoderTypeLbl = new javax.swing.JLabel();
    decoderTypeCB = new javax.swing.JComboBox<>();
    filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(25, 0), new java.awt.Dimension(25, 0), new java.awt.Dimension(20, 32767));
    addressLbl = new javax.swing.JLabel();
    addressSP = new javax.swing.JSpinner();
    namePanel = new javax.swing.JPanel();
    nameLbl = new javax.swing.JLabel();
    nameTF = new javax.swing.JTextField();

    setPreferredSize(new java.awt.Dimension(800, 600));
    setLayout(new jcs.ui.swing.layout.VLayout());

    locListSP.setDoubleBuffered(true);
    locListSP.setPreferredSize(new java.awt.Dimension(150, 250));
    locListSP.setViewportView(locList);

    locList.setModel(refreshListModel());
    locList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    locList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
        locListValueChanged(evt);
      }
    });
    locListSP.setViewportView(locList);

    mainhBox.add(locListSP);

    detailsPanel.setPreferredSize(new java.awt.Dimension(550, 300));
    jcs.ui.swing.layout.VerticalFlowLayout verticalFlowLayout1 = new jcs.ui.swing.layout.VerticalFlowLayout();
    verticalFlowLayout1.sethAlignment(0);
    detailsPanel.setLayout(verticalFlowLayout1);

    refreshSynchPanel.setPreferredSize(new java.awt.Dimension(550, 50));
    java.awt.FlowLayout flowLayout3 = new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT);
    flowLayout3.setAlignOnBaseline(true);
    refreshSynchPanel.setLayout(flowLayout3);

    synchBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/CS2-3-Sync.png"))); // NOI18N
    synchBtn.setToolTipText("Synchronize with Controller");
    synchBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        synchBtnActionPerformed(evt);
      }
    });
    refreshSynchPanel.add(synchBtn);

    refreshBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/refresh-24.png"))); // NOI18N
    refreshBtn.setToolTipText("Refresh from Database");
    refreshBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        refreshBtnActionPerformed(evt);
      }
    });
    refreshSynchPanel.add(refreshBtn);

    detailsPanel.add(refreshSynchPanel);

    comShowIconPanel.setPreferredSize(new java.awt.Dimension(550, 50));
    comShowIconPanel.setRequestFocusEnabled(false);
    java.awt.FlowLayout flowLayout2 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout2.setAlignOnBaseline(true);
    comShowIconPanel.setLayout(flowLayout2);
    comShowIconPanel.add(filler2);

    commuterCB.setText("Commuter Train");
    commuterCB.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        commuterCBPropertyChange(evt);
      }
    });
    comShowIconPanel.add(commuterCB);
    comShowIconPanel.add(filler4);

    showCB.setText("Show");
    showCB.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        showCBPropertyChange(evt);
      }
    });
    comShowIconPanel.add(showCB);
    comShowIconPanel.add(filler1);

    locIconLbl.setText("Icon");
    locIconLbl.setPreferredSize(new java.awt.Dimension(128, 50));
    locIconLbl.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        locIconLblPropertyChange(evt);
      }
    });
    comShowIconPanel.add(locIconLbl);

    detailsPanel.add(comShowIconPanel);

    decoderTypeAddressPanel.setPreferredSize(new java.awt.Dimension(550, 50));
    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    decoderTypeAddressPanel.setLayout(flowLayout1);

    decoderTypeLbl.setText("Decoder Type");
    decoderTypeLbl.setPreferredSize(new java.awt.Dimension(100, 17));
    decoderTypeAddressPanel.add(decoderTypeLbl);

    decoderTypeCB.setModel(getDecoderTypeModel());
    decoderTypeCB.setDoubleBuffered(true);
    decoderTypeCB.setPreferredSize(new java.awt.Dimension(75, 23));
    decoderTypeCB.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        decoderTypeCBPropertyChange(evt);
      }
    });
    decoderTypeAddressPanel.add(decoderTypeCB);
    decoderTypeAddressPanel.add(filler3);

    addressLbl.setText("Address");
    decoderTypeAddressPanel.add(addressLbl);

    addressSP.setModel(new javax.swing.SpinnerNumberModel());
    addressSP.setPreferredSize(new java.awt.Dimension(75, 23));
    addressSP.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        addressSPPropertyChange(evt);
      }
    });
    decoderTypeAddressPanel.add(addressSP);

    detailsPanel.add(decoderTypeAddressPanel);

    namePanel.setPreferredSize(new java.awt.Dimension(550, 50));
    java.awt.FlowLayout flowLayout4 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout4.setAlignOnBaseline(true);
    namePanel.setLayout(flowLayout4);

    nameLbl.setText("Name");
    nameLbl.setPreferredSize(new java.awt.Dimension(100, 17));
    namePanel.add(nameLbl);

    nameTF.setPreferredSize(new java.awt.Dimension(200, 23));
    nameTF.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        nameTFPropertyChange(evt);
      }
    });
    namePanel.add(nameTF);

    detailsPanel.add(namePanel);

    mainhBox.add(detailsPanel);

    mainvBox.add(mainhBox);

    add(mainvBox);
  }// </editor-fold>//GEN-END:initComponents

  private void locListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_locListValueChanged
    if (!evt.getValueIsAdjusting()) {
      if (this.locList.getSelectedValue() != null) {
        locomotiveBean = PersistenceFactory.getService().getLocomotive(this.locList.getSelectedValue().getId());
      } else {
        locomotiveBean = new LocomotiveBean();
      }
      Logger.trace(locomotiveBean);
    }

    PropertyChangeEvent iconPCE;
    if (locomotiveBean.getLocIcon() != null) {
      iconPCE = new PropertyChangeEvent(locomotiveBean, "locIcon", this.locIconLbl.getIcon(), new ImageIcon(locomotiveBean.getLocIcon()));
    } else {
      iconPCE = new PropertyChangeEvent(locomotiveBean, "locIcon", this.locIconLbl.getIcon(), null);
    }

    PropertyChangeEvent commuterPCE = new PropertyChangeEvent(locomotiveBean, "commuter", commuterCB.isSelected(), locomotiveBean.isCommuter());
    PropertyChangeEvent showPCE = new PropertyChangeEvent(locomotiveBean, "show", showCB.isSelected(), locomotiveBean.isShow());

    PropertyChangeEvent decoderTypePCE = new PropertyChangeEvent(locomotiveBean, "decoderType", this.decoderTypeCB.getSelectedItem(), locomotiveBean.getDecoderType());
    PropertyChangeEvent addressPCE = new PropertyChangeEvent(locomotiveBean, "address", this.addressSP.getValue(), locomotiveBean.getAddress());
    PropertyChangeEvent namePCE = new PropertyChangeEvent(locomotiveBean, "name", this.nameTF.getText(), locomotiveBean.getName());

    decoderTypeCBPropertyChange(decoderTypePCE);
    addressSPPropertyChange(addressPCE);
    nameTFPropertyChange(namePCE);

    locIconLblPropertyChange(iconPCE);
    showCBPropertyChange(showPCE);
    commuterCBPropertyChange(commuterPCE);

  }//GEN-LAST:event_locListValueChanged

  private void commuterCBPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_commuterCBPropertyChange
    if ("commuter".equals(evt.getPropertyName())) {
      this.commuterCB.setSelected((Boolean) evt.getNewValue());
    }
  }//GEN-LAST:event_commuterCBPropertyChange

  private void locIconLblPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_locIconLblPropertyChange
    if ("locIcon".equals(evt.getPropertyName())) {
      if (evt.getNewValue() != null) {
        this.locIconLbl.setText("");
        this.locIconLbl.setIcon((ImageIcon) evt.getNewValue());
      } else {
        if (this.locomotiveBean.getIcon() != null) {
          this.locIconLbl.setText(this.locomotiveBean.getIcon());
        } else {
          this.locIconLbl.setText("?");
        }
      }
    }
  }//GEN-LAST:event_locIconLblPropertyChange

  private void showCBPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_showCBPropertyChange
    if ("show".equals(evt.getPropertyName())) {
      this.showCB.setSelected((Boolean) evt.getNewValue());
    }
  }//GEN-LAST:event_showCBPropertyChange

  private void decoderTypeCBPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_decoderTypeCBPropertyChange
    if ("decoderType".equals(evt.getPropertyName())) {
      this.decoderTypeCB.setSelectedItem(evt.getNewValue());
    }
  }//GEN-LAST:event_decoderTypeCBPropertyChange

  private void addressSPPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_addressSPPropertyChange
    if ("address".equals(evt.getPropertyName()) && evt.getNewValue() != null) {
      this.addressSP.setValue(evt.getNewValue());
    } else {
      this.addressSP.setValue(0);
    }
  }//GEN-LAST:event_addressSPPropertyChange

  private void nameTFPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_nameTFPropertyChange
    if ("name".equals(evt.getPropertyName())) {
      this.nameTF.setText((String) evt.getNewValue());
    }
  }//GEN-LAST:event_nameTFPropertyChange

  private void synchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_synchBtnActionPerformed
    synchronizeLocomotives();
    refreshListModel();
  }//GEN-LAST:event_synchBtnActionPerformed

  private void refreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
    refreshListModel();
  }//GEN-LAST:event_refreshBtnActionPerformed

  private void synchronizeLocomotives() {
    Logger.trace("Starting synch...");
    SynchronizeWithControllerDialog wd = new SynchronizeWithControllerDialog(this, true);
    wd.setVisible(true);
  }

  public static void main(String args[]) {
    try {
      String plaf = System.getProperty("jcs.plaf", "com.formdev.flatlaf.FlatLightLaf");
      if (plaf != null) {
        UIManager.setLookAndFeel(plaf);
      } else {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error(ex);
    }

    java.awt.EventQueue.invokeLater(() -> {

      LocomotiveSettings testPanel = new LocomotiveSettings();
      JFrame testFrame = new JFrame("LocomotiveSettings Tester");

      testFrame.add(testPanel);

      testFrame.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
          System.exit(0);
        }
      });
      testFrame.pack();
      testFrame.setLocationRelativeTo(null);
      testFrame.setVisible(true);
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel addressLbl;
  private javax.swing.JSpinner addressSP;
  private javax.swing.JPanel comShowIconPanel;
  private javax.swing.JCheckBox commuterCB;
  private javax.swing.JPanel decoderTypeAddressPanel;
  private javax.swing.JComboBox<DecoderType> decoderTypeCB;
  private javax.swing.JLabel decoderTypeLbl;
  private javax.swing.JPanel detailsPanel;
  private javax.swing.Box.Filler filler1;
  private javax.swing.Box.Filler filler2;
  private javax.swing.Box.Filler filler3;
  private javax.swing.Box.Filler filler4;
  private javax.swing.JLabel locIconLbl;
  private javax.swing.JList<IdNameProperty> locList;
  private javax.swing.JScrollPane locListSP;
  private jcs.ui.swing.layout.HBox mainhBox;
  private jcs.ui.swing.layout.VBox mainvBox;
  private javax.swing.JLabel nameLbl;
  private javax.swing.JPanel namePanel;
  private javax.swing.JTextField nameTF;
  private javax.swing.JButton refreshBtn;
  private javax.swing.JPanel refreshSynchPanel;
  private javax.swing.JCheckBox showCB;
  private javax.swing.JButton synchBtn;
  // End of variables declaration//GEN-END:variables
}