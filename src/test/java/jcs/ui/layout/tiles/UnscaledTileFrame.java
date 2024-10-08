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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Direction;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.entities.TileBean.TileType;
import jcs.ui.layout.events.TileEvent;
import org.tinylog.Logger;

/**
 *
 * @author FJA
 */
public class UnscaledTileFrame extends javax.swing.JFrame implements PropertyChangeListener {

  private Tile tile;

  /**
   * Creates new form UnscaledBlockTileFrame
   */
  public UnscaledTileFrame() {
    initComponents();
    this.tileCB.setModel(createTileTypeComboBoxModel());
    this.orientationCB.setModel(createOrientationComboBoxModel());
    this.incomingSideCB.setModel(createOrientationComboBoxModel());
    this.directionCB.setModel(createDirectionComboBoxModel(true));
    drawTile();
  }

  private ComboBoxModel<TileType> createTileTypeComboBoxModel() {
    DefaultComboBoxModel<TileType> tileTypeModel = new DefaultComboBoxModel();

    tileTypeModel.addElement(TileBean.TileType.STRAIGHT);
    tileTypeModel.addElement(TileBean.TileType.STRAIGHT_DIR);
    tileTypeModel.addElement(TileBean.TileType.SENSOR);
    tileTypeModel.addElement(TileBean.TileType.SIGNAL);
    tileTypeModel.addElement(TileBean.TileType.END);
    tileTypeModel.addElement(TileBean.TileType.CROSSING);
    tileTypeModel.addElement(TileBean.TileType.CURVED);
    tileTypeModel.addElement(TileBean.TileType.SWITCH);
    tileTypeModel.addElement(TileBean.TileType.CROSS);

    return tileTypeModel;
  }

  private ComboBoxModel<Orientation> createOrientationComboBoxModel() {
    DefaultComboBoxModel<Orientation> orientationModel = new DefaultComboBoxModel();

    orientationModel.addElement(Orientation.EAST);
    orientationModel.addElement(Orientation.SOUTH);
    orientationModel.addElement(Orientation.WEST);
    orientationModel.addElement(Orientation.NORTH);

    return orientationModel;
  }

  private ComboBoxModel<Direction> createDirectionComboBoxModel(boolean dontCare) {
    DefaultComboBoxModel<Direction> directionModel = new DefaultComboBoxModel();

    if (dontCare) {
      directionModel.addElement(Direction.CENTER);
    } else {
      directionModel.addElement(Direction.LEFT);
      directionModel.addElement(Direction.RIGHT);
    }

    return directionModel;
  }

  private void drawTile() {
    TileType tileType = (TileType) this.tileCB.getSelectedItem();
    Orientation orientation = (Orientation) this.orientationCB.getSelectedItem();

    if (TileType.SWITCH == tileType || TileType.CROSS == tileType) {
      this.directionCB.setModel(createDirectionComboBoxModel(false));
    } else {
      this.directionCB.setModel(createDirectionComboBoxModel(true));
    }

    Direction direction = (Direction) this.directionCB.getSelectedItem();
    boolean showOutline = this.drawOutlineCB.isSelected();

    int w = this.cPanel.getWidth();
    int h = this.cPanel.getHeight();

    int x;
    int y;
    if (TileType.CROSS == tileType) {
      switch (orientation) {
        case SOUTH -> {
          x = w / 2 + 200;
          y = h / 2 - 150;
        }
        case WEST -> {
          x = w / 2 + 400;
          y = h / 2 + 50;
        }
        case NORTH -> {
          x = w / 2 + 200;
          y = h / 2 + 250;
        }
        default -> {
          x = w / 2;
          y = h / 2 + 50;
        }
      }
    } else {
      x = w / 2;
      y = h / 2 + 50;
    }

    Point center;
    if (TileType.CROSS.equals(tileType)) {
      center = new Point(x - 200, y);
    } else {
      center = new Point(x, y);
    }

    tile = TileFactory.createTile(tileType, orientation, direction, center, showOutline);
    tile.setPropertyChangeListener(this);

    Orientation incomingSide = (Orientation) this.incomingSideCB.getSelectedItem();
    tile.setIncomingSide(incomingSide);

    tile.setDrawRoute(this.displayRouteCB.isSelected());
    tile.setTrackRouteColor(Color.blue);

    ((AbstractTile) tile).setScaleImage(false);

    this.repaint();
  }

  private AccessoryValue getAccessoryState() {
    AccessoryValue value;
    if (greenRB.isSelected()) {
      value = AccessoryValue.GREEN;
    } else if (redRB.isSelected()) {
      value = AccessoryValue.RED;

    } else {
      value = AccessoryValue.OFF;
    }
    return value;
  }

  private void changeAccesoryState() {
    if (tile instanceof Switch aSwitch) {
      if (this.displayRouteCB.isSelected()) {
        aSwitch.setRouteValue(getAccessoryState());
      } else {
        aSwitch.setValue(getAccessoryState());
      }
    }

    if (tile instanceof Signal aSignal) {
      if (this.greenRB.isSelected()) {
        aSignal.setSignalValue(AccessoryBean.SignalValue.Hp1);
      } else if (this.redRB.isSelected()) {
        aSignal.setSignalValue(AccessoryBean.SignalValue.Hp0);
      } else {
        aSignal.setSignalValue(AccessoryBean.SignalValue.OFF);
      }
    }

    repaint();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("repaintTile".equals(evt.getPropertyName())) {
      Tile t = (Tile) evt.getNewValue();
      Logger.trace("Tile: " + t);
      this.repaint();
    }
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    Graphics2D g2d = (Graphics2D) g;
    boolean outline = this.drawOutlineCB.isSelected();
    boolean showRoute = this.displayRouteCB.isSelected();
    tile.setDrawRoute(showRoute);

    tile.drawTile(g2d, outline);

    if (outline) {
      tile.drawBounds(g2d);
      tile.drawCenterPoint(g2d, Color.red);
    }
  }

  private void changeDirection() {
    Direction direction = (Direction) this.directionCB.getSelectedItem();
    this.tile.setDirection(direction);

    if (TileType.CROSS == tile.getTileType()) {
      ((Cross) tile).setWidthHeightAndOffsets();
    }
    this.repaint();
  }

  private void rotateTile() {
    this.tile.rotate();

    if (TileType.CROSS == tile.getTileType()) {
      int x = tile.getCenterX();
      int y = tile.getCenterY();
      int w = tile.getWidth() * 10;
      int h = tile.getHeight() * 10;

      //calculate a new centerpoint for cross
      switch (tile.getOrientation()) {
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
      tile.setCenter(new Point(x, y));
    }

    this.orientationCB.setSelectedItem(tile.getOrientation());

    this.repaint();
  }

  private void changeOrientation() {
    Orientation orientation = (Orientation) this.orientationCB.getSelectedItem();
    tile.setOrientation(orientation);

    if (TileType.CROSS == tile.getTileType()) {
      ((Cross) tile).setWidthHeightAndOffsets();

      int x = tile.getCenterX();
      int y = tile.getCenterY();
      int w = tile.getWidth() * 10;
      int h = tile.getHeight() * 10;

      //calculate a new centerpoint for cross
      switch (tile.getOrientation()) {
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
      tile.setCenter(new Point(x, y));
    }

    this.repaint();
  }

  private void showRoute() {
    String tileId = tile.getId();
    Orientation incomingSide = (Orientation) this.incomingSideCB.getSelectedItem();

    TileEvent tileEvent;
    if (tile.isJunction()) {
      AccessoryValue routeState = getAccessoryState();
      tileEvent = new TileEvent(tileId, true, incomingSide, routeState);
    } else {
      tileEvent = new TileEvent(tileId, true, incomingSide);
    }
    TileFactory.fireTileEventListener(tileEvent);

    //tile.setDrawRoute(displayRouteCB.isSelected());
    //repaint();
  }

  private void showOutline() {
    repaint();
  }

  private void changeIncomingSide() {
    Orientation incomingSide = (Orientation) this.incomingSideCB.getSelectedItem();
    tile.setIncomingSide(incomingSide);
    this.repaint();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    accessoryBG = new javax.swing.ButtonGroup();
    nPanel = new javax.swing.JPanel();
    tileCB = new javax.swing.JComboBox<>();
    orientationCB = new javax.swing.JComboBox<>();
    inComingLbl = new javax.swing.JLabel();
    incomingSideCB = new javax.swing.JComboBox<>();
    directionCB = new javax.swing.JComboBox<>();
    rotateButton = new javax.swing.JButton();
    offRB = new javax.swing.JRadioButton();
    greenRB = new javax.swing.JRadioButton();
    redRB = new javax.swing.JRadioButton();
    displayRouteCB = new javax.swing.JCheckBox();
    drawOutlineCB = new javax.swing.JCheckBox();
    cPanel = new javax.swing.JPanel();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    nPanel.setLayout(flowLayout1);

    tileCB.setPreferredSize(new java.awt.Dimension(150, 23));
    tileCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        tileCBActionPerformed(evt);
      }
    });
    nPanel.add(tileCB);

    orientationCB.setPreferredSize(new java.awt.Dimension(100, 23));
    orientationCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        orientationCBActionPerformed(evt);
      }
    });
    nPanel.add(orientationCB);

    inComingLbl.setText("Incoming Orientation");
    nPanel.add(inComingLbl);

    incomingSideCB.setPreferredSize(new java.awt.Dimension(100, 23));
    incomingSideCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        incomingSideCBActionPerformed(evt);
      }
    });
    nPanel.add(incomingSideCB);

    directionCB.setPreferredSize(new java.awt.Dimension(100, 23));
    directionCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        directionCBActionPerformed(evt);
      }
    });
    nPanel.add(directionCB);

    rotateButton.setText("Rotate");
    rotateButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        rotateButtonActionPerformed(evt);
      }
    });
    nPanel.add(rotateButton);

    accessoryBG.add(offRB);
    offRB.setForeground(new java.awt.Color(153, 153, 153));
    offRB.setSelected(true);
    offRB.setText("Off");
    offRB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        offRBActionPerformed(evt);
      }
    });
    nPanel.add(offRB);

    accessoryBG.add(greenRB);
    greenRB.setForeground(new java.awt.Color(102, 255, 0));
    greenRB.setText("Green");
    greenRB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        greenRBActionPerformed(evt);
      }
    });
    nPanel.add(greenRB);

    accessoryBG.add(redRB);
    redRB.setForeground(new java.awt.Color(255, 0, 51));
    redRB.setText("Red");
    redRB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        redRBActionPerformed(evt);
      }
    });
    nPanel.add(redRB);

    displayRouteCB.setText("Route");
    displayRouteCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        displayRouteCBActionPerformed(evt);
      }
    });
    nPanel.add(displayRouteCB);

    drawOutlineCB.setText("Outline");
    drawOutlineCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        drawOutlineCBActionPerformed(evt);
      }
    });
    nPanel.add(drawOutlineCB);

    getContentPane().add(nPanel, java.awt.BorderLayout.NORTH);

    cPanel.setPreferredSize(new java.awt.Dimension(1000, 1000));
    cPanel.setLayout(null);
    getContentPane().add(cPanel, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void rotateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateButtonActionPerformed
    rotateTile();
  }//GEN-LAST:event_rotateButtonActionPerformed

  private void tileCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tileCBActionPerformed
    drawTile();
  }//GEN-LAST:event_tileCBActionPerformed

  private void orientationCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientationCBActionPerformed
    changeOrientation();
  }//GEN-LAST:event_orientationCBActionPerformed

  private void directionCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directionCBActionPerformed
    changeDirection();
  }//GEN-LAST:event_directionCBActionPerformed

  private void offRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_offRBActionPerformed
    changeAccesoryState();
  }//GEN-LAST:event_offRBActionPerformed

  private void greenRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_greenRBActionPerformed
    changeAccesoryState();
  }//GEN-LAST:event_greenRBActionPerformed

  private void redRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redRBActionPerformed
    changeAccesoryState();
  }//GEN-LAST:event_redRBActionPerformed

  private void displayRouteCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayRouteCBActionPerformed
    showRoute();
  }//GEN-LAST:event_displayRouteCBActionPerformed

  private void drawOutlineCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawOutlineCBActionPerformed
    showOutline();
  }//GEN-LAST:event_drawOutlineCBActionPerformed

  private void incomingSideCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incomingSideCBActionPerformed
    changeIncomingSide();
  }//GEN-LAST:event_incomingSideCBActionPerformed

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
      UnscaledTileFrame app = new UnscaledTileFrame();
      app.setTitle("Unscaled Tile Tester");
      app.pack();
      app.setLocationRelativeTo(null);
      app.setVisible(true);
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup accessoryBG;
  private javax.swing.JPanel cPanel;
  private javax.swing.JComboBox<Direction> directionCB;
  private javax.swing.JCheckBox displayRouteCB;
  private javax.swing.JCheckBox drawOutlineCB;
  private javax.swing.JRadioButton greenRB;
  private javax.swing.JLabel inComingLbl;
  private javax.swing.JComboBox<Orientation> incomingSideCB;
  private javax.swing.JPanel nPanel;
  private javax.swing.JRadioButton offRB;
  private javax.swing.JComboBox<Orientation> orientationCB;
  private javax.swing.JRadioButton redRB;
  private javax.swing.JButton rotateButton;
  private javax.swing.JComboBox<TileBean.TileType> tileCB;
  // End of variables declaration//GEN-END:variables
}
