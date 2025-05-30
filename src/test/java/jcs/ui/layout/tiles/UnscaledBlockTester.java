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
package jcs.ui.layout.tiles;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.entities.BlockBean;
import jcs.entities.BlockBean.BlockState;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean.Orientation;
import jcs.ui.util.ImageUtil;
import org.tinylog.Logger;

public class UnscaledBlockTester extends JFrame { //implements PropertyChangeListener {

  private final Tile blockTile;

  /**
   * Creates new form UnscaledBlockTileFrame
   */
  public UnscaledBlockTester() {
    initComponents();

    this.orientationCB.setModel(createOrientationComboBoxModel());
    this.departureSideCB.setSelectedItem("");
    this.stateCB.setModel(createStateComboBoxModel());

    blockTile = createBlock();

    canvas.add(blockTile);
    //String imgPath = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "images" + File.separator + "DHG 6505.png";
    //ImageIcon locIcon = new ImageIcon(getClass().getResource("/images/DHG 6505.png"));
    //Image locImage = new BufferedImage(locIcon.getIconWidth(), locIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);

    //String imgPath = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "images" + File.separator + "DHG 6505.png";
    //Image locImage = ImageUtil.readImage(imgPath);

    //locImage = ImageUtil.scaleImage(locImage, 100);
    //JLabel c = new JLabel(new ImageIcon(locImage));

    //canvas.add(c);

    centerSP.getViewport().validate();
    pack();
    setVisible(true);
  }

  private ComboBoxModel<Orientation> createOrientationComboBoxModel() {
    DefaultComboBoxModel<Orientation> orientationModel = new DefaultComboBoxModel();
    orientationModel.addElement(Orientation.EAST);
    orientationModel.addElement(Orientation.SOUTH);
    orientationModel.addElement(Orientation.WEST);
    orientationModel.addElement(Orientation.NORTH);

    return orientationModel;
  }

  private ComboBoxModel<BlockState> createStateComboBoxModel() {
    DefaultComboBoxModel<BlockState> blockStateModel = new DefaultComboBoxModel();
    blockStateModel.addElement(BlockState.FREE);
    blockStateModel.addElement(BlockState.OCCUPIED);
    blockStateModel.addElement(BlockState.OUTBOUND);
    blockStateModel.addElement(BlockState.INBOUND);
    blockStateModel.addElement(BlockState.LOCKED);
    blockStateModel.addElement(BlockState.OUT_OF_ORDER);

    blockStateModel.setSelectedItem(BlockState.FREE);
    return blockStateModel;
  }

  private LocomotiveBean createLocomotiveBean() {
    LocomotiveBean lb = new LocomotiveBean(8L, "NS DHG 6505", 8L, 8, "", "dcc", 100, 0, 0, 1, true, true);
    String imgPath = getClass().getResource("/images/DHG 6505.png").getFile().replaceAll("%20"," ");
    lb.setIcon(imgPath);
    Image locImage = ImageUtil.readImage(imgPath);
    locImage = ImageUtil.scaleImage(locImage, 100);
    lb.setLocIcon(locImage);

    if (backwardsRB.isSelected()) {
      lb.setDirection(LocomotiveBean.Direction.BACKWARDS);
      this.blockTile.setLogicalDirection(LocomotiveBean.Direction.BACKWARDS);
    } else {
      lb.setDirection(LocomotiveBean.Direction.FORWARDS);
      this.blockTile.setLogicalDirection(LocomotiveBean.Direction.FORWARDS);
    }

    return lb;
  }

  private Block createBlock() {
    Block block = new Block(Orientation.EAST, 640, 280);

    block.setId("bk-1");
    block.setBlockState((BlockState) stateCB.getSelectedItem());
    block.setScaleImage(!scaleCB.isSelected());
    //canvas.setExpanded(scaleCB.isSelected());

    BlockBean blockBean = new BlockBean();
    blockBean.setId(block.getId());
    blockBean.setTileId(block.getId());
    blockBean.setDescription("Blok 1");
    blockBean.setDepartureSuffix(null);
    blockBean.setDepartureSuffix((String) this.departureSideCB.getSelectedItem());

    if (showLocCB.isSelected()) {
      blockBean.setLocomotive(createLocomotiveBean());
    } else {
      blockBean.setLocomotive(null);
    }

    block.setBlockBean(blockBean);
    return block;
  }

  private void changeOrientation() {
    Orientation orientation = (Orientation) this.orientationCB.getSelectedItem();
    blockTile.setOrientation(orientation);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    locDirectionBG = new javax.swing.ButtonGroup();
    nPanel = new javax.swing.JPanel();
    scaleCB = new javax.swing.JCheckBox();
    rotateButton = new javax.swing.JButton();
    orientationLabel = new javax.swing.JLabel();
    orientationCB = new javax.swing.JComboBox<>();
    stateCB = new javax.swing.JComboBox<>();
    incomingSuffix = new javax.swing.JLabel();
    departureSideCB = new javax.swing.JComboBox<>();
    reverseArrivalCB = new javax.swing.JCheckBox();
    showLocCB = new javax.swing.JCheckBox();
    jLabel1 = new javax.swing.JLabel();
    backwardsRB = new javax.swing.JRadioButton();
    forwardsRB = new javax.swing.JRadioButton();
    showCenterCB = new javax.swing.JCheckBox();
    centerSP = new javax.swing.JScrollPane();
    canvas = new jcs.ui.layout.tiles.UnscaledBlockCanvas();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setPreferredSize(new java.awt.Dimension(1250, 500));

    nPanel.setPreferredSize(new java.awt.Dimension(1250, 50));

    scaleCB.setText("Expand");
    scaleCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        scaleCBActionPerformed(evt);
      }
    });
    nPanel.add(scaleCB);

    rotateButton.setText("Rotate");
    rotateButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        rotateButtonActionPerformed(evt);
      }
    });
    nPanel.add(rotateButton);

    orientationLabel.setText("Orientation");
    nPanel.add(orientationLabel);

    orientationCB.setPreferredSize(new java.awt.Dimension(150, 22));
    orientationCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        orientationCBActionPerformed(evt);
      }
    });
    nPanel.add(orientationCB);

    stateCB.setPreferredSize(new java.awt.Dimension(150, 22));
    stateCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        stateCBActionPerformed(evt);
      }
    });
    nPanel.add(stateCB);

    incomingSuffix.setText("Departure Suffix");
    nPanel.add(incomingSuffix);

    departureSideCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "+", "-", "" }));
    departureSideCB.setPreferredSize(new java.awt.Dimension(50, 22));
    departureSideCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        departureSideCBActionPerformed(evt);
      }
    });
    nPanel.add(departureSideCB);

    reverseArrivalCB.setText("Reverse Arrival Side");
    reverseArrivalCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        reverseArrivalCBActionPerformed(evt);
      }
    });
    nPanel.add(reverseArrivalCB);

    showLocCB.setText("");
    showLocCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showLocCBActionPerformed(evt);
      }
    });
    nPanel.add(showLocCB);

    jLabel1.setLabelFor(showLocCB);
    jLabel1.setText("Show Locomotive");
    nPanel.add(jLabel1);

    locDirectionBG.add(backwardsRB);
    backwardsRB.setText("Backwards");
    backwardsRB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        backwardsRBActionPerformed(evt);
      }
    });
    nPanel.add(backwardsRB);

    locDirectionBG.add(forwardsRB);
    forwardsRB.setSelected(true);
    forwardsRB.setText("Forwards");
    forwardsRB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        forwardsRBActionPerformed(evt);
      }
    });
    nPanel.add(forwardsRB);

    showCenterCB.setText("Show Center");
    showCenterCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showCenterCBActionPerformed(evt);
      }
    });
    nPanel.add(showCenterCB);

    getContentPane().add(nPanel, java.awt.BorderLayout.NORTH);

    centerSP.setAutoscrolls(true);
    centerSP.setDoubleBuffered(true);
    centerSP.setMinimumSize(new java.awt.Dimension(1250, 440));
    centerSP.setPreferredSize(new java.awt.Dimension(1250, 440));
    centerSP.setViewportView(canvas);
    centerSP.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(java.awt.event.ComponentEvent evt) {
        centerSPComponentResized(evt);
      }
    });

    canvas.setAutoscrolls(true);
    canvas.setPreferredSize(new java.awt.Dimension(1220, 410));
    canvas.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(java.awt.event.ComponentEvent evt) {
        canvasComponentResized(evt);
      }
    });

    javax.swing.GroupLayout canvasLayout = new javax.swing.GroupLayout(canvas);
    canvas.setLayout(canvasLayout);
    canvasLayout.setHorizontalGroup(
      canvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 1248, Short.MAX_VALUE)
    );
    canvasLayout.setVerticalGroup(
      canvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 445, Short.MAX_VALUE)
    );

    centerSP.setViewportView(canvas);

    getContentPane().add(centerSP, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void rotateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateButtonActionPerformed
    Orientation orientation = blockTile.rotate();
    this.orientationCB.setSelectedItem(orientation);
    Logger.trace("\nBlok is rotated to " + orientation);
  }//GEN-LAST:event_rotateButtonActionPerformed

  private void showLocCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLocCBActionPerformed
    if (showLocCB.isSelected()) {
      blockTile.setLocomotive(createLocomotiveBean());
    } else {
      blockTile.setLocomotive(null);
    }
  }//GEN-LAST:event_showLocCBActionPerformed

  private void orientationCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientationCBActionPerformed
    changeOrientation();
  }//GEN-LAST:event_orientationCBActionPerformed

  private void departureSideCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_departureSideCBActionPerformed
    if (blockTile != null) {
      if ("".equals(departureSideCB.getSelectedItem())) {
        blockTile.setDepartureSuffix(null);
      } else {
        blockTile.setDepartureSuffix(departureSideCB.getSelectedItem().toString());
      }
    }
  }//GEN-LAST:event_departureSideCBActionPerformed

  private void stateCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stateCBActionPerformed
    blockTile.setBlockState((BlockState) stateCB.getSelectedItem());
  }//GEN-LAST:event_stateCBActionPerformed

  private void showCenterCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCenterCBActionPerformed
    blockTile.setDrawCenterPoint(this.showCenterCB.isSelected());
  }//GEN-LAST:event_showCenterCBActionPerformed

  private void reverseArrivalCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reverseArrivalCBActionPerformed
    blockTile.setReverseArrival(reverseArrivalCB.isSelected());
  }//GEN-LAST:event_reverseArrivalCBActionPerformed

  private void backwardsRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backwardsRBActionPerformed
    blockTile.setLogicalDirection(LocomotiveBean.Direction.BACKWARDS);
  }//GEN-LAST:event_backwardsRBActionPerformed

  private void forwardsRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardsRBActionPerformed
    blockTile.setLogicalDirection(LocomotiveBean.Direction.FORWARDS);
  }//GEN-LAST:event_forwardsRBActionPerformed

  private void scaleCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleCBActionPerformed
    Logger.trace("\nBlok is expanded " + this.scaleCB.isSelected());
    blockTile.setScaleImage(!scaleCB.isSelected());
    canvas.setExpanded(scaleCB.isSelected());

    //Shift the tile a bit to fit on the canvas
//    if(!scaleCB.isSelected()) {
//      blockTile.setCenter(new Point(620, 180));
//    } else {
//      blockTile.setCenter(new Point(620, 140));
//    }

  }//GEN-LAST:event_scaleCBActionPerformed

  private void centerSPComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_centerSPComponentResized
    //Logger.trace(evt);
    //this.centerSP.validate();
    //wordt bij init aangeroepen
  }//GEN-LAST:event_centerSPComponentResized

  private void canvasComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_canvasComponentResized
    //Logger.trace(evt);
    //this.centerSP.setLocation(0, 0);
    //this.centerSP.getViewport().revalidate();

//
//    int btcw = this.blockTileCanvas.getSize().width;
//    int btch = this.blockTileCanvas.getSize().height;
//
//    int spw = this.centerSP.getPreferredSize().width;
//    int sph = this.centerSP.getPreferredSize().width;
//
//    int w = spw;
//    if (btcw > spw) {
//      w = btcw;
//    }
//    int h = sph;
//    if (btch > sph) {
//      h = btch;
//    }
//    Logger.trace("Btc W: " + btcw + " Btc H: " + btch + ". SP W: " + spw + " SP H: " + sph);
//    Dimension d = new Dimension(w, h);
//    this.centerSP.getViewport().setViewSize(d);
    //JViewport vp = scrollpane.getViewport();
    //vp.setViewSize(newsize);
//    revalidate();
//jcs.ui.layout.tiles.UnscaledBlockTester.GridCanvas

  }//GEN-LAST:event_canvasComponentResized

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error(ex);
    }

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(() -> {
      UnscaledBlockTester app = new UnscaledBlockTester();
      app.setTitle("Unscaled Tile Tester");
      app.setLocationRelativeTo(null);

      //app.pack();
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JRadioButton backwardsRB;
  private jcs.ui.layout.tiles.UnscaledBlockCanvas canvas;
  private javax.swing.JScrollPane centerSP;
  private javax.swing.JComboBox<String> departureSideCB;
  private javax.swing.JRadioButton forwardsRB;
  private javax.swing.JLabel incomingSuffix;
  private javax.swing.JLabel jLabel1;
  private javax.swing.ButtonGroup locDirectionBG;
  private javax.swing.JPanel nPanel;
  private javax.swing.JComboBox<Orientation> orientationCB;
  private javax.swing.JLabel orientationLabel;
  private javax.swing.JCheckBox reverseArrivalCB;
  private javax.swing.JButton rotateButton;
  private javax.swing.JCheckBox scaleCB;
  private javax.swing.JCheckBox showCenterCB;
  private javax.swing.JCheckBox showLocCB;
  private javax.swing.JComboBox<BlockState> stateCB;
  // End of variables declaration//GEN-END:variables
}
