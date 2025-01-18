/*
 * Copyright 2024 FJA.
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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.entities.BlockBean;
import jcs.entities.BlockBean.BlockState;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import jcs.ui.util.ImageUtil;
import org.tinylog.Logger;

/**
 *
 * @author FJA
 */
public class UnscaledBlockTileFrame extends javax.swing.JFrame implements PropertyChangeListener {

  private Tile blockTile;

  /**
   * Creates new form UnscaledBlockTileFrame
   */
  public UnscaledBlockTileFrame() {
    initComponents();
    this.orientationCB.setModel(createOrientationComboBoxModel());
    this.departureSideCB.setSelectedItem("");
    this.stateCB.setModel(createStateComboBoxModel());

    initTile();
    
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

    return blockStateModel;
  }

  private LocomotiveBean createLocomotiveBean() {
    LocomotiveBean lb = new LocomotiveBean(8L, "NS DHG 6505", 8L, 8, "", "dcc", 100, 0, 0, 1, true, true);
    String imgPath = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "images" + File.separator + "DHG 6505.png";
    lb.setIcon(imgPath);
    Image locImage = ImageUtil.readImage(imgPath);
    //Image is sized by default so
    locImage = ImageUtil.scaleImage(locImage, 100);
    lb.setLocIcon(locImage);

    if (this.backwardsRB.isSelected()) {
      lb.setDirection(LocomotiveBean.Direction.BACKWARDS);
    } else {
      lb.setDirection(LocomotiveBean.Direction.FORWARDS);
    }

    return lb;
  }

  private void initTile() {
    Dimension vps = this.blockTileCanvas.getPreferredSize();
    blockTile = new Block(TileBean.Orientation.EAST, vps.width / 2, vps.height / 2);
    blockTile.setId("bk-1");

    BlockBean bbe = new BlockBean();
    bbe.setId(blockTile.getId());
    bbe.setTileId(blockTile.getId());
    bbe.setDescription("Blok");

    //bbe.setArrivalSuffix((String) this.incomingSideCB.getSelectedItem());
    bbe.setDepartureSuffix(null);

    bbe.setBlockState((BlockState) this.stateCB.getSelectedItem());
    bbe.setReverseArrival(this.reverseArrivalCB.isSelected());

    if (this.showLocCB.isSelected()) {
      bbe.setLocomotive(createLocomotiveBean());
    } else {
      bbe.setLocomotive(null);
    }

    ((Block) blockTile).setBlockBean(bbe);

    if (this.scaleCB.isSelected()) {
      blockTile.setScaleImage(false);
    } else {
      blockTile.setScaleImage(true);
    }

    blockTileCanvas.addTile(blockTile);
    centerSP.getViewport().validate();
  }

  private void changeOrientation() {
    Orientation orientation = (Orientation) this.orientationCB.getSelectedItem();
    blockTile.setOrientation(orientation);
//    if (Orientation.EAST.equals(blockTile.getOrientation()) || Orientation.WEST.equals(blockTile.getOrientation())) {
//      ((Block) blockTile).setWidth(Tile.DEFAULT_WIDTH * 3);
//      ((Block) blockTile).setHeight(Tile.DEFAULT_HEIGHT);
//
//      ((Block) blockTile).setRenderWidth(Tile.RENDER_WIDTH * 3);
//      ((Block) blockTile).setRenderHeight(Tile.RENDER_HEIGHT);
//    } else {
//      ((Block) blockTile).setWidth(Tile.DEFAULT_WIDTH);
//      ((Block) blockTile).setHeight(Tile.DEFAULT_HEIGHT * 3);
//
//      ((Block) blockTile).setRenderWidth(Tile.RENDER_WIDTH);
//      ((Block) blockTile).setRenderHeight(Tile.RENDER_HEIGHT * 3);
//    }
//
//    blockTile.drawTile((Graphics2D) getGraphics(), this.showCenterCB.isSelected());
    Dimension vps = this.blockTileCanvas.getPreferredSize();

    Point cc = new Point(Math.abs(vps.width / 2), Math.abs(vps.height / 2));
    blockTile.setCenter(cc);

    this.centerSP.getViewport().revalidate();
    repaint();
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
    orientationLabel = new javax.swing.JLabel();
    orientationCB = new javax.swing.JComboBox<>();
    incomingSuffix = new javax.swing.JLabel();
    departureSideCB = new javax.swing.JComboBox<>();
    stateCB = new javax.swing.JComboBox<>();
    reverseArrivalCB = new javax.swing.JCheckBox();
    rotateButton = new javax.swing.JButton();
    showLocCB = new javax.swing.JCheckBox();
    backwardsRB = new javax.swing.JRadioButton();
    forwardsRB = new javax.swing.JRadioButton();
    showCenterCB = new javax.swing.JCheckBox();
    centerSP = new javax.swing.JScrollPane();
    blockTileCanvas = new jcs.ui.layout.tiles.UnscaledBlockCanvas();

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

    orientationLabel.setText("Orientation");
    nPanel.add(orientationLabel);

    orientationCB.setPreferredSize(new java.awt.Dimension(150, 22));
    orientationCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        orientationCBActionPerformed(evt);
      }
    });
    nPanel.add(orientationCB);

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

    stateCB.setPreferredSize(new java.awt.Dimension(150, 22));
    stateCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        stateCBActionPerformed(evt);
      }
    });
    nPanel.add(stateCB);

    reverseArrivalCB.setText("Reverse Arrival Side");
    reverseArrivalCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        reverseArrivalCBActionPerformed(evt);
      }
    });
    nPanel.add(reverseArrivalCB);

    rotateButton.setText("Rotate");
    rotateButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        rotateButtonActionPerformed(evt);
      }
    });
    nPanel.add(rotateButton);

    showLocCB.setLabel("Show Locomotive");
    showLocCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showLocCBActionPerformed(evt);
      }
    });
    nPanel.add(showLocCB);

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
    centerSP.setViewportView(blockTileCanvas);
    centerSP.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(java.awt.event.ComponentEvent evt) {
        centerSPComponentResized(evt);
      }
    });

    blockTileCanvas.setAutoscrolls(true);
    blockTileCanvas.setPreferredSize(null);
    blockTileCanvas.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(java.awt.event.ComponentEvent evt) {
        blockTileCanvasComponentResized(evt);
      }
    });

    javax.swing.GroupLayout blockTileCanvasLayout = new javax.swing.GroupLayout(blockTileCanvas);
    blockTileCanvas.setLayout(blockTileCanvasLayout);
    blockTileCanvasLayout.setHorizontalGroup(
      blockTileCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 1248, Short.MAX_VALUE)
    );
    blockTileCanvasLayout.setVerticalGroup(
      blockTileCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 498, Short.MAX_VALUE)
    );

    centerSP.setViewportView(blockTileCanvas);

    getContentPane().add(centerSP, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void rotateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateButtonActionPerformed
    this.blockTile.rotate();

    Orientation orientation = blockTile.getOrientation();
    this.orientationCB.setSelectedItem(orientation);
    Logger.trace("Blok is rotated to " + blockTile.getOrientation());
    repaint();
  }//GEN-LAST:event_rotateButtonActionPerformed

  private void showLocCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLocCBActionPerformed
    if (this.showLocCB.isSelected()) {
      ((Block) blockTile).getBlockBean().setLocomotive(this.createLocomotiveBean());
    } else {
      ((Block) blockTile).getBlockBean().setLocomotive(null);
    }
    repaint();
  }//GEN-LAST:event_showLocCBActionPerformed

  private void orientationCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientationCBActionPerformed
    changeOrientation();
  }//GEN-LAST:event_orientationCBActionPerformed

  private void departureSideCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_departureSideCBActionPerformed
    if (blockTile != null && ((Block) blockTile).getBlockBean() != null) {
      if ("".equals(this.departureSideCB.getSelectedItem())) {
        ((Block) blockTile).getBlockBean().setDepartureSuffix(null);
      } else {
        ((Block) blockTile).getBlockBean().setDepartureSuffix((String) this.departureSideCB.getSelectedItem());
      }
      repaint();
    }
  }//GEN-LAST:event_departureSideCBActionPerformed

  private void stateCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stateCBActionPerformed
    ((Block) blockTile).getBlockBean().setBlockState((BlockState) this.stateCB.getSelectedItem());
    repaint();
  }//GEN-LAST:event_stateCBActionPerformed

  private void showCenterCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCenterCBActionPerformed
    //Reset the logical direction
    ((Block) blockTile).getBlockBean().setLogicalDirection(null);
    repaint();
  }//GEN-LAST:event_showCenterCBActionPerformed

  private String getDepartureSuffix() {
    if (((Block) blockTile).getBlockBean() != null && ((Block) blockTile).getBlockBean().getDepartureSuffix() != null && !"".equals(((Block) blockTile).getBlockBean().getDepartureSuffix())) {
      return ((Block) blockTile).getBlockBean().getDepartureSuffix();
    } else {
      return "+";
    }
  }

  private void reverseArrivalCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reverseArrivalCBActionPerformed
    ((Block) blockTile).getBlockBean().setReverseArrival(this.reverseArrivalCB.isSelected());

    //The Suffix is orientation dependent!
//    if ("+".equals(((Block) blockTile).getBlockBean().getArrivalSuffix())) {
//      ((Block) blockTile).getBlockBean().setArrivalSuffix("-");
//    } else {
//      ((Block) blockTile).getBlockBean().setArrivalSuffix("+");
//    }
//    this.departureSideCB.setSelectedItem(((Block) blockTile).getBlockBean().getArrivalSuffix());
    repaint();
  }//GEN-LAST:event_reverseArrivalCBActionPerformed

  private void backwardsRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backwardsRBActionPerformed
    if (((Block) blockTile).getBlockBean().getLocomotive() != null) {
      //((Block) blockTile).getBlockBean().getLocomotive().setDispatcherDirection(LocomotiveBean.Direction.BACKWARDS);
      ((Block) blockTile).getBlockBean().setLogicalDirection(LocomotiveBean.Direction.BACKWARDS.getDirection());
      repaint();
    }
  }//GEN-LAST:event_backwardsRBActionPerformed

  private void forwardsRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardsRBActionPerformed
    if (((Block) blockTile).getBlockBean().getLocomotive() != null) {
      //((Block) blockTile).getBlockBean().getLocomotive().setDispatcherDirection(LocomotiveBean.Direction.FORWARDS);
      ((Block) blockTile).getBlockBean().setLogicalDirection(LocomotiveBean.Direction.FORWARDS.getDirection());
      repaint();
    }
  }//GEN-LAST:event_forwardsRBActionPerformed

  private void scaleCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleCBActionPerformed
    if (this.scaleCB.isSelected()) {
      blockTile.setScaleImage(false);
    } else {
      blockTile.setScaleImage(true);
    }
    repaint();
  }//GEN-LAST:event_scaleCBActionPerformed

  private void centerSPComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_centerSPComponentResized
    //Logger.trace(evt);
    //this.centerSP.validate();
    //wordt bij init aangeroepen
  }//GEN-LAST:event_centerSPComponentResized

  private void blockTileCanvasComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_blockTileCanvasComponentResized
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

  }//GEN-LAST:event_blockTileCanvasComponentResized

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
      UnscaledBlockTileFrame app = new UnscaledBlockTileFrame();
      app.setTitle("Unscaled Tile Tester");
      app.pack();
      app.setLocationRelativeTo(null);
      //app.setVisible(true);
    });
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("repaintTile".equals(evt.getPropertyName())) {
      Tile t = (Tile) evt.getNewValue();
      Logger.trace("Tile: " + t);
      this.repaint();
    }
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JRadioButton backwardsRB;
  private jcs.ui.layout.tiles.UnscaledBlockCanvas blockTileCanvas;
  private javax.swing.JScrollPane centerSP;
  private javax.swing.JComboBox<String> departureSideCB;
  private javax.swing.JRadioButton forwardsRB;
  private javax.swing.JLabel incomingSuffix;
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
