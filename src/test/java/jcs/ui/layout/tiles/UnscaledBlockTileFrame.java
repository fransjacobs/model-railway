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
import java.awt.Image;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.entities.BlockBean;
import jcs.entities.BlockBean.BlockState;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
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
    //addViewportListener();
    this.orientationCB.setModel(createOrientationComboBoxModel());
    this.incomingSideCB.setSelectedItem("");
    this.stateCB.setModel(createStateComboBoxModel());
    initTile();
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
    blockStateModel.addElement(BlockState.LEAVING);
    blockStateModel.addElement(BlockState.DEPARTING);
    blockStateModel.addElement(BlockState.ARRIVING);
    blockStateModel.addElement(BlockState.LOCKED);
    blockStateModel.addElement(BlockState.FREE);

    return blockStateModel;
  }

  private LocomotiveBean createLocomotiveBean() {
    LocomotiveBean lb = new LocomotiveBean(8L, "NS DHG 6505", 8L, 8, "", "dcc", 100, 0, 0, 1, true, true);
    String imgPath = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + "dcc-ex" + File.separator + "ns dhg 6505.png";
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

//  private void addViewportListener() {
//    this.centerSP.getViewport().addChangeListener((ChangeEvent e) -> {
//      centerSP.revalidate();
//      //offsets....
//      //https://stackoverflow.com/questions/6561246/scroll-event-of-a-jscrollpane
//      repaint();
//    });
//  }
  private void initTile() {
    blockTile = new Block(Orientation.EAST, 750, 250);
    blockTile.setId("bk-1");

    BlockBean bbe = new BlockBean();
    bbe.setId(blockTile.getId());
    bbe.setTileId(blockTile.getId());
    bbe.setDescription("Blok");
    bbe.setArrivalSuffix((String) this.incomingSideCB.getSelectedItem());

    bbe.setBlockState((BlockState) this.stateCB.getSelectedItem());
    bbe.setReverseArrival(this.reverseArrivalCB.isSelected());

    bbe.setArrivalSuffix(getIncomingSuffix());
    
    
    if (this.showLocCB.isSelected()) {
      bbe.setLocomotive(createLocomotiveBean());
    } else {
      bbe.setLocomotive(null);
    }

    ((Block) blockTile).setBlockBean(bbe);
    if (this.scaleCB.isSelected()) {
      ((AbstractTile) blockTile).setScaleImage(false);
    } else {
      ((AbstractTile) blockTile).setScaleImage(true);
    }

    this.blockTileCanvas.setBlock(blockTile);
  }

  private void changeOrientation() {
    Orientation orientation = (Orientation) this.orientationCB.getSelectedItem();
    blockTile.setOrientation(orientation);
    //((Block)tile).setWidthHeightAndOffsets();

    int x = blockTile.getCenterX();
    int y = blockTile.getCenterY();
    int w = blockTile.getWidth() * 10;
    int h = blockTile.getHeight() * 10;

    //calculate a new centerpoint for cross
    switch (blockTile.getOrientation()) {

      case SOUTH -> {
        x = x + w / 2;
        y = y - h / 4;
      }
      case WEST -> {
        x = x + w / 4;
        y = y + h / 2;
      }
      case NORTH -> {
        x = x - w / 2;
        y = y + h / 4;
      }
      default -> {
        x = x - w / 4;
        y = y - h / 2;
      }
    }
    blockTile.setCenter(new Point(x, y));

    if (Orientation.EAST.equals(blockTile.getOrientation()) || Orientation.WEST.equals(blockTile.getOrientation())) {
      ((Block) blockTile).setWidth(Tile.DEFAULT_WIDTH * 3);
      ((Block) blockTile).setHeight(Tile.DEFAULT_HEIGHT);

      ((Block) blockTile).setRenderWidth(Tile.RENDER_WIDTH * 3);
      ((Block) blockTile).setRenderHeight(Tile.RENDER_HEIGHT);
    } else {
      ((Block) blockTile).setWidth(Tile.DEFAULT_WIDTH);
      ((Block) blockTile).setHeight(Tile.DEFAULT_HEIGHT * 3);

      ((Block) blockTile).setRenderWidth(Tile.RENDER_WIDTH);
      ((Block) blockTile).setRenderHeight(Tile.RENDER_HEIGHT * 3);
    }

    this.repaint();
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
    incomingSideCB = new javax.swing.JComboBox<>();
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

    nPanel.setPreferredSize(new java.awt.Dimension(1250, 33));

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

    incomingSuffix.setText("Incoming Suffix");
    nPanel.add(incomingSuffix);

    incomingSideCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "+", "-", "" }));
    incomingSideCB.setPreferredSize(new java.awt.Dimension(50, 22));
    incomingSideCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        incomingSideCBActionPerformed(evt);
      }
    });
    nPanel.add(incomingSideCB);

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

    centerSP.setDoubleBuffered(true);
    centerSP.setMinimumSize(new java.awt.Dimension(1240, 440));
    centerSP.setPreferredSize(new java.awt.Dimension(1240, 440));
    centerSP.setViewportView(blockTileCanvas);
    centerSP.addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsListener() {
      public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
      }
      public void ancestorResized(java.awt.event.HierarchyEvent evt) {
        centerSPAncestorResized(evt);
      }
    });
    centerSP.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(java.awt.event.ComponentEvent evt) {
        centerSPComponentResized(evt);
      }
    });

    blockTileCanvas.setMinimumSize(new java.awt.Dimension(1000, 800));
    blockTileCanvas.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(java.awt.event.ComponentEvent evt) {
        blockTileCanvasComponentResized(evt);
      }
    });

    javax.swing.GroupLayout blockTileCanvasLayout = new javax.swing.GroupLayout(blockTileCanvas);
    blockTileCanvas.setLayout(blockTileCanvasLayout);
    blockTileCanvasLayout.setHorizontalGroup(
      blockTileCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 1240, Short.MAX_VALUE)
    );
    blockTileCanvasLayout.setVerticalGroup(
      blockTileCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 800, Short.MAX_VALUE)
    );

    centerSP.setViewportView(blockTileCanvas);

    getContentPane().add(centerSP, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void rotateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateButtonActionPerformed
    this.blockTile.rotate();

    Orientation orientation = blockTile.getOrientation();
    this.orientationCB.setSelectedItem(orientation);
    this.repaint();
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

  private void incomingSideCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incomingSideCBActionPerformed
    if (blockTile != null && ((Block) blockTile).getBlockBean() != null) {
      ((Block) blockTile).getBlockBean().setArrivalSuffix((String) this.incomingSideCB.getSelectedItem());
      repaint();
    }
  }//GEN-LAST:event_incomingSideCBActionPerformed

  private void stateCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stateCBActionPerformed
    ((Block) blockTile).getBlockBean().setBlockState((BlockState) this.stateCB.getSelectedItem());
    repaint();
  }//GEN-LAST:event_stateCBActionPerformed

  private void showCenterCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCenterCBActionPerformed
    repaint();
  }//GEN-LAST:event_showCenterCBActionPerformed

  private String getIncomingSuffix() {
    if (((Block) blockTile).getBlockBean() != null && ((Block) blockTile).getBlockBean().getArrivalSuffix() != null && !"".equals(((Block) blockTile).getBlockBean().getArrivalSuffix())) {
      return ((Block) blockTile).getBlockBean().getArrivalSuffix();
    } else {
      return "-";
    }
  }
  
  private void reverseArrivalCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reverseArrivalCBActionPerformed
    ((Block) blockTile).getBlockBean().setReverseArrival(this.reverseArrivalCB.isSelected());
    repaint();
  }//GEN-LAST:event_reverseArrivalCBActionPerformed

  private void backwardsRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backwardsRBActionPerformed
    if (((Block) blockTile).getBlockBean().getLocomotive() != null) {
      ((Block) blockTile).getBlockBean().getLocomotive().setDirection(LocomotiveBean.Direction.BACKWARDS);
      repaint();
    }
  }//GEN-LAST:event_backwardsRBActionPerformed

  private void forwardsRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardsRBActionPerformed
    if (((Block) blockTile).getBlockBean().getLocomotive() != null) {
      ((Block) blockTile).getBlockBean().getLocomotive().setDirection(LocomotiveBean.Direction.FORWARDS);
      repaint();
    }
  }//GEN-LAST:event_forwardsRBActionPerformed

  private void scaleCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleCBActionPerformed
    if (this.scaleCB.isSelected()) {
      ((AbstractTile) blockTile).setScaleImage(false);
    } else {
      ((AbstractTile) blockTile).setScaleImage(true);
    }
    repaint();
  }//GEN-LAST:event_scaleCBActionPerformed

  private void centerSPAncestorResized(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_centerSPAncestorResized
    Logger.trace(evt);
  }//GEN-LAST:event_centerSPAncestorResized

  private void centerSPComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_centerSPComponentResized
    Logger.trace(evt);
  }//GEN-LAST:event_centerSPComponentResized

  private void blockTileCanvasComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_blockTileCanvasComponentResized
    Logger.trace(evt);
    
    Dimension d = this.blockTileCanvas.getPreferredSize();
    
    this.centerSP.getViewport().setViewSize(d);
    //JViewport vp = scrollpane.getViewport();
    //vp.setViewSize(newsize);
    revalidate();
    
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
      app.setVisible(true);
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
  private javax.swing.JRadioButton forwardsRB;
  private javax.swing.JComboBox<String> incomingSideCB;
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
