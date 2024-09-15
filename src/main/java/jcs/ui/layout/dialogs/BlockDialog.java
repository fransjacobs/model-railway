/*
 * Copyright 2024 Frans Jacobs.
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
package jcs.ui.layout.dialogs;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import jcs.entities.BlockBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.LayoutCanvas;
import jcs.ui.layout.tiles.Block;
import jcs.ui.layout.tiles.Sensor;
import jcs.ui.layout.tiles.Tile;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class BlockDialog extends javax.swing.JDialog {

  private final Block block;
  private final LayoutCanvas layoutCanvas;

  private ComboBoxModel<SensorBean> plusSensorComboBoxModel;
  private ComboBoxModel<SensorBean> minSensorComboBoxModel;

  /**
   * Creates new form SensorDialog
   *
   * @param parent
   * @param block
   * @param layoutCanvas
   */
  public BlockDialog(java.awt.Frame parent, Block block, LayoutCanvas layoutCanvas) {
    super(parent, true);
    this.block = block;
    this.layoutCanvas = layoutCanvas;
    initComponents();

    postInit();
  }

  private Set<String> getLinkedSensorIds() {
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    Set<String> linkedSensorIds = new HashSet<>();
    for (BlockBean bb : blocks) {
      if (bb.getPlusSensorId() != null) {
        linkedSensorIds.add(bb.getPlusSensorId());
      }
      if (bb.getMinSensorId() != null) {
        linkedSensorIds.add(bb.getMinSensorId());
      }
    }
    return linkedSensorIds;
  }

  private void postInit() {
    setLocationRelativeTo(null);
    String text = this.headingLbl.getText() + " " + this.block.getId();
    this.headingLbl.setText(text);

    if (this.block != null) {
      //Get a list of all sensors
      List<SensorBean> allSensors = PersistenceFactory.getService().getSensors();
      List<SensorBean> freeSensors = new ArrayList<>();
      // filter the list, remove sensors which are already linked to other blocks.
      Set<String> linkedSensorIds = getLinkedSensorIds();

      for (SensorBean sb : allSensors) {
        if (!linkedSensorIds.contains(sb.getId())) {
          freeSensors.add(sb);
        }
      }

      //Expand with an empty one for display
      SensorBean emptyBean = new SensorBean();
      freeSensors.add(emptyBean);

      BlockBean bb = this.block.getBlockBean();
      if (bb == null) {
        Logger.tags("bb is null for " + this.block.getId());
        bb = new BlockBean();
        bb.setTile(block);
        bb.setTileId(this.block.getId());
        bb.setBlockState(BlockBean.BlockState.FREE);
        this.block.setBlockBean(bb);
      }

      SensorBean plusSb, minSb;
      if (bb.getPlusSensorId() != null && bb.getPlusSensorBean() == null) {
        plusSb = PersistenceFactory.getService().getSensor(bb.getPlusSensorId());
        bb.setPlusSensorBean(plusSb);
      } else {
        plusSb = bb.getPlusSensorBean();
      }

      if (bb.getMinSensorId() != null && bb.getMinSensorBean() == null) {
        minSb = PersistenceFactory.getService().getSensor(bb.getMinSensorId());
        bb.setPlusSensorBean(plusSb);
      } else {
        minSb = bb.getMinSensorBean();
      }

      if (bb.getPlusSensorBean() != null) {
        //Add the used sensor also the the filtered list
        freeSensors.add(bb.getPlusSensorBean());
      }
      if (bb.getMinSensorBean() != null) {
        //Add the used sensor also the the filtered list
        freeSensors.add(bb.getMinSensorBean());
      }

      plusSensorComboBoxModel = new DefaultComboBoxModel(freeSensors.toArray());
      minSensorComboBoxModel = new DefaultComboBoxModel(freeSensors.toArray());

      this.plusSensorCB.setModel(plusSensorComboBoxModel);
      this.minSensorCB.setModel(minSensorComboBoxModel);

      this.blockIdTF.setText(this.block.getId());
      this.blockNameTF.setText(bb.getDescription());
      String desc = this.block.getBlockBean().getDescription();
      this.saveExitBtn.setEnabled((desc != null && desc.length() > 1));

      if (plusSb != null) {
        this.plusSensorComboBoxModel.setSelectedItem(plusSb);
      } else {
        this.plusSensorComboBoxModel.setSelectedItem(emptyBean);
      }
      if (minSb != null) {
        this.minSensorComboBoxModel.setSelectedItem(minSb);
      } else {
        this.minSensorComboBoxModel.setSelectedItem(emptyBean);
      }
    }
  }

  private void autoLink() {
    //Try to link the sensors automatically
    Point pnp = this.block.getNeighborPoint("+");
    Point mnp = this.block.getNeighborPoint("-");

    Logger.trace("Neighbor point +: " + pnp + " -: " + mnp);

    Tile neighborPlus = this.layoutCanvas.findTile(pnp);
    Tile neighborMin = this.layoutCanvas.findTile(mnp);

    if (neighborPlus != null && neighborPlus instanceof Sensor) {
      Sensor ps = (Sensor) neighborPlus;
      SensorBean plusSb = ps.getSensorBean();
      if (plusSb != null) {
        this.block.getBlockBean().setPlusSensorBean(plusSb);
        this.plusSensorComboBoxModel.setSelectedItem(plusSb);
        Logger.trace("Auto linked Plus Sensor: " + ps + " " + plusSb.getName());
      }
    }

    if (neighborMin != null && neighborMin instanceof Sensor) {
      Sensor ms = (Sensor) neighborMin;
      SensorBean minSb = ms.getSensorBean();
      if (minSb != null) {
        this.block.getBlockBean().setMinSensorBean(minSb);
        this.minSensorComboBoxModel.setSelectedItem(minSb);
        Logger.trace("Auto linked Min Sensor: " + ms + " " + minSb.getName());
      }
    }

  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    headingPanel = new javax.swing.JPanel();
    headingLbl = new javax.swing.JLabel();
    deviceIdPanel = new javax.swing.JPanel();
    blockIdLbl = new javax.swing.JLabel();
    blockIdTF = new javax.swing.JTextField();
    namePanel = new javax.swing.JPanel();
    blockDescLbl = new javax.swing.JLabel();
    blockNameTF = new javax.swing.JTextField();
    autoLinkPanel = new javax.swing.JPanel();
    autoLinkLbl = new javax.swing.JLabel();
    autoLinkButton = new javax.swing.JButton();
    sensorPanel = new javax.swing.JPanel();
    plusSensorLbl = new javax.swing.JLabel();
    plusSensorCB = new javax.swing.JComboBox<>();
    minSensorLbl = new javax.swing.JLabel();
    minSensorCB = new javax.swing.JComboBox<>();
    saveExitPanel = new javax.swing.JPanel();
    saveExitBtn = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Block Properties");
    setBounds(new java.awt.Rectangle(0, 25, 340, 250));
    setMinimumSize(new java.awt.Dimension(340, 250));
    setPreferredSize(new java.awt.Dimension(340, 250));
    getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

    headingPanel.setMinimumSize(new java.awt.Dimension(290, 40));
    headingPanel.setPreferredSize(new java.awt.Dimension(290, 40));
    headingPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    headingLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/new-block.png"))); // NOI18N
    headingLbl.setText("Block");
    headingPanel.add(headingLbl);

    getContentPane().add(headingPanel);

    deviceIdPanel.setPreferredSize(new java.awt.Dimension(290, 40));
    java.awt.FlowLayout flowLayout2 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout2.setAlignOnBaseline(true);
    deviceIdPanel.setLayout(flowLayout2);

    blockIdLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    blockIdLbl.setLabelFor(blockIdTF);
    blockIdLbl.setText("Id:");
    blockIdLbl.setPreferredSize(new java.awt.Dimension(100, 16));
    deviceIdPanel.add(blockIdLbl);

    blockIdTF.setEnabled(false);
    blockIdTF.setPreferredSize(new java.awt.Dimension(150, 23));
    blockIdTF.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusLost(java.awt.event.FocusEvent evt) {
        blockIdTFFocusLost(evt);
      }
    });
    blockIdTF.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        blockIdTFActionPerformed(evt);
      }
    });
    deviceIdPanel.add(blockIdTF);

    getContentPane().add(deviceIdPanel);

    namePanel.setMinimumSize(new java.awt.Dimension(290, 40));
    namePanel.setPreferredSize(new java.awt.Dimension(290, 40));
    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    namePanel.setLayout(flowLayout1);

    blockDescLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    blockDescLbl.setLabelFor(blockNameTF);
    blockDescLbl.setText("Name:");
    blockDescLbl.setToolTipText("");
    blockDescLbl.setPreferredSize(new java.awt.Dimension(100, 16));
    namePanel.add(blockDescLbl);

    blockNameTF.setPreferredSize(new java.awt.Dimension(150, 23));
    blockNameTF.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusLost(java.awt.event.FocusEvent evt) {
        blockNameTFFocusLost(evt);
      }
    });
    blockNameTF.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        blockNameTFActionPerformed(evt);
      }
    });
    namePanel.add(blockNameTF);

    getContentPane().add(namePanel);

    autoLinkPanel.setPreferredSize(new java.awt.Dimension(290, 40));
    java.awt.FlowLayout flowLayout7 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout7.setAlignOnBaseline(true);
    autoLinkPanel.setLayout(flowLayout7);

    autoLinkLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    autoLinkLbl.setLabelFor(autoLinkButton);
    autoLinkLbl.setText("Auto link");
    autoLinkLbl.setPreferredSize(new java.awt.Dimension(100, 17));
    autoLinkPanel.add(autoLinkLbl);

    autoLinkButton.setText("Link");
    autoLinkButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        autoLinkButtonActionPerformed(evt);
      }
    });
    autoLinkPanel.add(autoLinkButton);

    getContentPane().add(autoLinkPanel);

    sensorPanel.setPreferredSize(new java.awt.Dimension(290, 40));
    java.awt.FlowLayout flowLayout3 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout3.setAlignOnBaseline(true);
    sensorPanel.setLayout(flowLayout3);

    plusSensorLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    plusSensorLbl.setLabelFor(plusSensorCB);
    plusSensorLbl.setText("Sensors +:");
    plusSensorLbl.setDoubleBuffered(true);
    plusSensorLbl.setPreferredSize(new java.awt.Dimension(100, 17));
    sensorPanel.add(plusSensorLbl);

    plusSensorCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        plusSensorCBActionPerformed(evt);
      }
    });
    sensorPanel.add(plusSensorCB);

    minSensorLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    minSensorLbl.setLabelFor(minSensorCB);
    minSensorLbl.setText("-:");
    minSensorLbl.setPreferredSize(new java.awt.Dimension(10, 17));
    sensorPanel.add(minSensorLbl);

    minSensorCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        minSensorCBActionPerformed(evt);
      }
    });
    sensorPanel.add(minSensorCB);

    getContentPane().add(sensorPanel);

    saveExitPanel.setPreferredSize(new java.awt.Dimension(290, 50));
    java.awt.FlowLayout flowLayout4 = new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT);
    flowLayout4.setAlignOnBaseline(true);
    saveExitPanel.setLayout(flowLayout4);

    saveExitBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/save-24.png"))); // NOI18N
    saveExitBtn.setToolTipText("Save and Exit");
    saveExitBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveExitBtnActionPerformed(evt);
      }
    });
    saveExitPanel.add(saveExitBtn);

    getContentPane().add(saveExitPanel);

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void saveExitBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveExitBtnActionPerformed
      if (this.block != null && this.block.getBlockBean() != null) {
        BlockBean bb = this.block.getBlockBean();
        //TODO, depends on future changes but for now
        if (bb.getId() == null) {
          //Can't be null and should be the same as the block
          bb.setId(this.block.getId());
        }

        if (bb.getStatus() == null) {
          bb.setBlockState(BlockBean.BlockState.FREE);
        }

        if (bb.getMinWaitTime() == null) {
          //The wait time cannot be null, put a default there
          Integer minWait = Integer.getInteger("default.min.waittime", 10);
          bb.setMinWaitTime(minWait);
        }

        PersistenceFactory.getService().persist(bb);
      }

      this.setVisible(false);
      this.dispose();
      Logger.trace(evt.getActionCommand() + "Block " + block.getId() + " Name: " + this.block.getBlockBean().getDescription());
    }//GEN-LAST:event_saveExitBtnActionPerformed

  private void plusSensorCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusSensorCBActionPerformed
    this.block.getBlockBean().setPlusSensorBean((SensorBean) this.plusSensorCB.getSelectedItem());
  }//GEN-LAST:event_plusSensorCBActionPerformed

  private void minSensorCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minSensorCBActionPerformed
    this.block.getBlockBean().setMinSensorBean((SensorBean) this.minSensorCB.getSelectedItem());
  }//GEN-LAST:event_minSensorCBActionPerformed

  private void blockNameTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blockNameTFActionPerformed
    this.block.getBlockBean().setDescription(((JTextField) evt.getSource()).getText());
    String desc = this.block.getBlockBean().getDescription();
    this.saveExitBtn.setEnabled((desc != null && desc.length() > 1));
  }//GEN-LAST:event_blockNameTFActionPerformed

  private void blockNameTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_blockNameTFFocusLost
    this.block.getBlockBean().setDescription(((JTextField) evt.getSource()).getText());
    String desc = this.block.getBlockBean().getDescription();
    this.saveExitBtn.setEnabled((desc != null && desc.length() > 1));
  }//GEN-LAST:event_blockNameTFFocusLost

  private void blockIdTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blockIdTFActionPerformed
    this.block.getBlockBean().setTileId(((JTextField) evt.getSource()).getText());
  }//GEN-LAST:event_blockIdTFActionPerformed

  private void blockIdTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_blockIdTFFocusLost
    this.block.getBlockBean().setTileId(((JTextField) evt.getSource()).getText());
  }//GEN-LAST:event_blockIdTFFocusLost

  private void autoLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoLinkButtonActionPerformed
    autoLink();
  }//GEN-LAST:event_autoLinkButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton autoLinkButton;
  private javax.swing.JLabel autoLinkLbl;
  private javax.swing.JPanel autoLinkPanel;
  private javax.swing.JLabel blockDescLbl;
  private javax.swing.JLabel blockIdLbl;
  private javax.swing.JTextField blockIdTF;
  private javax.swing.JTextField blockNameTF;
  private javax.swing.JPanel deviceIdPanel;
  private javax.swing.JLabel headingLbl;
  private javax.swing.JPanel headingPanel;
  private javax.swing.JComboBox<SensorBean> minSensorCB;
  private javax.swing.JLabel minSensorLbl;
  private javax.swing.JPanel namePanel;
  private javax.swing.JComboBox<SensorBean> plusSensorCB;
  private javax.swing.JLabel plusSensorLbl;
  private javax.swing.JButton saveExitBtn;
  private javax.swing.JPanel saveExitPanel;
  private javax.swing.JPanel sensorPanel;
  // End of variables declaration//GEN-END:variables
}
