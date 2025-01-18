/*
 * Copyright 2025 Frans Jacobs.
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
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.entities.AccessoryBean;
import jcs.entities.TileBean;
import org.tinylog.Logger;

public class SignalTileTester extends javax.swing.JFrame {

  private Tile signal2East;
  private Tile signal2South;
  private Tile signal2West;
  private Tile signal2North;

  private Tile signal2MEast;
  private Tile signal2MSouth;
  private Tile signal2MWest;
  private Tile signal2MNorth;

  private Tile signal3East;
  private Tile signal3South;
  private Tile signal3West;
  private Tile signal3North;

  private Tile signal4East;
  private Tile signal4South;
  private Tile signal4West;
  private Tile signal4North;

  /**
   * Creates new form TileTester
   *
   * @param title
   */
  public SignalTileTester(String title) {
    super(title);
    initComponents();

    createTiles();

    this.setVisible(true);
  }

  private void createTiles() {
    signal2East = new Signal(TileBean.Orientation.EAST, 40, 40, AccessoryBean.SignalType.HP01);
    signal2East.setId("east2");
    signal2East.setTrackRouteColor(Color.MAGENTA);
    signal2East.setSignalValue(AccessoryBean.SignalValue.Hp0);

    signal2South = new Signal(TileBean.Orientation.SOUTH, 120, 40, AccessoryBean.SignalType.HP01);
    signal2South.setId("south2");
    signal2South.setTrackRouteColor(Color.YELLOW);
    signal2South.setSignalValue(AccessoryBean.SignalValue.Hp1);

    signal2West = new Signal(TileBean.Orientation.WEST, 200, 40, AccessoryBean.SignalType.HP01);
    signal2West.setId("west2");
    signal2West.setTrackRouteColor(Color.CYAN);
    signal2West.setSignalValue(AccessoryBean.SignalValue.Hp0);

    signal2North = new Signal(TileBean.Orientation.NORTH, 280, 40, AccessoryBean.SignalType.HP01);
    signal2North.setId("north2");
    signal2North.setTrackRouteColor(Color.blue);
    signal2North.setSignalValue(AccessoryBean.SignalValue.Hp1);

    //
    signal2MEast = new Signal(TileBean.Orientation.EAST, 40, 120, AccessoryBean.SignalType.HP0SH1);
    signal2MEast.setId("east2m");
    signal2MEast.setTrackRouteColor(Color.MAGENTA);
    signal2MEast.setSignalValue(AccessoryBean.SignalValue.Hp0);

    signal2MSouth = new Signal(TileBean.Orientation.SOUTH, 120, 120, AccessoryBean.SignalType.HP0SH1);
    signal2MSouth.setId("south2m");
    signal2MSouth.setSignalValue(AccessoryBean.SignalValue.Hp1);

    signal2MWest = new Signal(TileBean.Orientation.WEST, 200, 120, AccessoryBean.SignalType.HP0SH1);
    signal2MWest.setId("west2m");
    signal2MWest.setSignalValue(AccessoryBean.SignalValue.Hp0);

    signal2MNorth = new Signal(TileBean.Orientation.NORTH, 280, 120, AccessoryBean.SignalType.HP0SH1);
    signal2MWest.setId("north2m");
    signal2MNorth.setSignalValue(AccessoryBean.SignalValue.Hp1);

    //
    signal3East = new Signal(TileBean.Orientation.EAST, 40, 200, AccessoryBean.SignalType.HP012);
    signal3East.setId("east3");
    signal3East.setSignalValue(AccessoryBean.SignalValue.Hp0);

    signal3South = new Signal(TileBean.Orientation.SOUTH, 120, 200, AccessoryBean.SignalType.HP012);
    signal3South.setId("south3");
    signal3South.setSignalValue(AccessoryBean.SignalValue.Hp1);

    signal3West = new Signal(TileBean.Orientation.WEST, 200, 200, AccessoryBean.SignalType.HP012);
    signal3West.setId("west3");
    signal3West.setSignalValue(AccessoryBean.SignalValue.Hp2);

    signal3North = new Signal(TileBean.Orientation.NORTH, 280, 200, AccessoryBean.SignalType.HP012);
    signal3North.setId("north3");
    signal3North.setSignalValue(AccessoryBean.SignalValue.Hp0);

    //
    signal4East = new Signal(TileBean.Orientation.EAST, 40, 280, AccessoryBean.SignalType.HP012SH1);
    signal4East.setId("east4");
    signal4East.setSignalValue(AccessoryBean.SignalValue.Hp0);

    signal4South = new Signal(TileBean.Orientation.SOUTH, 120, 280, AccessoryBean.SignalType.HP012SH1);
    signal4South.setId("south4");
    signal4South.setSignalValue(AccessoryBean.SignalValue.Hp1);

    signal4West = new Signal(TileBean.Orientation.WEST, 200, 280, AccessoryBean.SignalType.HP012SH1);
    signal4West.setId("west4");
    signal4West.setSignalValue(AccessoryBean.SignalValue.Hp2);

    signal4North = new Signal(TileBean.Orientation.NORTH, 280, 280, AccessoryBean.SignalType.HP012SH1);
    signal4North.setId("north4");
    signal4North.setSignalValue(AccessoryBean.SignalValue.Hp0Sh1);

    dotGridCanvas.add(signal2East);
    dotGridCanvas.add(signal2South);
    dotGridCanvas.add(signal2West);
    dotGridCanvas.add(signal2North);

    dotGridCanvas.add(signal2MEast);
    dotGridCanvas.add(signal2MSouth);
    dotGridCanvas.add(signal2MWest);
    dotGridCanvas.add(signal2MNorth);

    dotGridCanvas.add(signal3East);
    dotGridCanvas.add(signal3South);
    dotGridCanvas.add(signal3West);
    dotGridCanvas.add(signal3North);

    dotGridCanvas.add(signal4East);
    dotGridCanvas.add(signal4South);
    dotGridCanvas.add(signal4West);
    dotGridCanvas.add(signal4North);

  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    toolbarPanel = new javax.swing.JPanel();
    toolBar = new javax.swing.JToolBar();
    eastTileBtn = new javax.swing.JToggleButton();
    southTileBtn = new javax.swing.JToggleButton();
    westTileBtn = new javax.swing.JToggleButton();
    northTileBtn = new javax.swing.JToggleButton();
    selectSouthTileBtn = new javax.swing.JToggleButton();
    drawCenterBtn = new javax.swing.JToggleButton();
    greenRedBtn = new javax.swing.JToggleButton();
    dotGridCanvas = new jcs.ui.layout.tiles.DotGridCanvas();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    toolbarPanel.setLayout(flowLayout1);

    toolBar.setRollover(true);

    eastTileBtn.setText("East");
    eastTileBtn.setFocusable(false);
    eastTileBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    eastTileBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    eastTileBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        eastTileBtnActionPerformed(evt);
      }
    });
    toolBar.add(eastTileBtn);

    southTileBtn.setText("South");
    southTileBtn.setFocusable(false);
    southTileBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    southTileBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    southTileBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        southTileBtnActionPerformed(evt);
      }
    });
    toolBar.add(southTileBtn);

    westTileBtn.setText("West");
    westTileBtn.setFocusable(false);
    westTileBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    westTileBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    westTileBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        westTileBtnActionPerformed(evt);
      }
    });
    toolBar.add(westTileBtn);

    northTileBtn.setText("North");
    northTileBtn.setFocusable(false);
    northTileBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    northTileBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    northTileBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        northTileBtnActionPerformed(evt);
      }
    });
    toolBar.add(northTileBtn);

    selectSouthTileBtn.setText("Select Tile");
    selectSouthTileBtn.setFocusable(false);
    selectSouthTileBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    selectSouthTileBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    selectSouthTileBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        selectSouthTileBtnActionPerformed(evt);
      }
    });
    toolBar.add(selectSouthTileBtn);

    drawCenterBtn.setText("show Center");
    drawCenterBtn.setFocusable(false);
    drawCenterBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    drawCenterBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    drawCenterBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        drawCenterBtnActionPerformed(evt);
      }
    });
    toolBar.add(drawCenterBtn);

    greenRedBtn.setText("Red");
    greenRedBtn.setFocusable(false);
    greenRedBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    greenRedBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    greenRedBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        greenRedBtnActionPerformed(evt);
      }
    });
    toolBar.add(greenRedBtn);

    toolbarPanel.add(toolBar);

    getContentPane().add(toolbarPanel, java.awt.BorderLayout.NORTH);

    dotGridCanvas.setPreferredSize(new java.awt.Dimension(360, 360));
    getContentPane().add(dotGridCanvas, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void northTileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_northTileBtnActionPerformed
    Logger.trace(signal2North.id + "...");
    this.signal2North.setDrawRoute(this.northTileBtn.isSelected());
  }//GEN-LAST:event_northTileBtnActionPerformed

  private void eastTileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eastTileBtnActionPerformed
    this.signal2East.setDrawRoute(this.eastTileBtn.isSelected());
  }//GEN-LAST:event_eastTileBtnActionPerformed

  private void westTileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_westTileBtnActionPerformed
    this.signal2West.setDrawRoute(this.westTileBtn.isSelected());
  }//GEN-LAST:event_westTileBtnActionPerformed

  private void southTileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_southTileBtnActionPerformed
    this.signal2South.setDrawRoute(this.southTileBtn.isSelected());
  }//GEN-LAST:event_southTileBtnActionPerformed

  private void selectSouthTileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectSouthTileBtnActionPerformed
    this.signal2South.setSelected(this.selectSouthTileBtn.isSelected());
  }//GEN-LAST:event_selectSouthTileBtnActionPerformed

  private void drawCenterBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawCenterBtnActionPerformed
    this.signal2North.setDrawCenterPoint(this.drawCenterBtn.isSelected());
  }//GEN-LAST:event_drawCenterBtnActionPerformed

  private void greenRedBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_greenRedBtnActionPerformed
    boolean red = this.greenRedBtn.isSelected();
    if (red) {
      this.greenRedBtn.setText("Green");
      this.signal2East.setSignalValue(AccessoryBean.SignalValue.Hp0);
      this.signal2MEast.setSignalValue(AccessoryBean.SignalValue.Hp0);
    } else {
      this.greenRedBtn.setText("Red");
      this.signal2East.setSignalValue(AccessoryBean.SignalValue.Hp1);
      this.signal2MEast.setSignalValue(AccessoryBean.SignalValue.Hp1);
    }
  }//GEN-LAST:event_greenRedBtnActionPerformed

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
      SignalTileTester.setDefaultLookAndFeelDecorated(true);

    } catch (ClassNotFoundException
            | InstantiationException
            | IllegalAccessException
            | UnsupportedLookAndFeelException ex) {
      Logger.error(ex);
    }

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(() -> {
      SignalTileTester app = new SignalTileTester("Signal Tile Tester");
      app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      app.setLocation(dim.width / 2 - app.getSize().width / 2, dim.height / 2 - app.getSize().height / 2);
      app.pack();
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private jcs.ui.layout.tiles.DotGridCanvas dotGridCanvas;
  private javax.swing.JToggleButton drawCenterBtn;
  private javax.swing.JToggleButton eastTileBtn;
  private javax.swing.JToggleButton greenRedBtn;
  private javax.swing.JToggleButton northTileBtn;
  private javax.swing.JToggleButton selectSouthTileBtn;
  private javax.swing.JToggleButton southTileBtn;
  private javax.swing.JToolBar toolBar;
  private javax.swing.JPanel toolbarPanel;
  private javax.swing.JToggleButton westTileBtn;
  // End of variables declaration//GEN-END:variables
}
