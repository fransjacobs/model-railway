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
package jcs.ui.layout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import jcs.JCS;
import jcs.commandStation.automation.RailController;
import jcs.entities.TileBean.TileType;
import jcs.ui.layout.tiles.LayoutScale;
import jcs.util.RunUtil;
import org.tinylog.Logger;

/**
 * The `LayoutPanel` class is a custom `JPanel` used to visually represent and manipulate a layout.<br>
 * It provides a graphical canvas (`LayoutCanvas`) for drawing and editing elements,<br>
 * along with a toolbar offering various tools for adding, deleting, manipulating, and loading/saving layouts.<br>
 * The panel can operate in read-only mode, disabling editing functionalities.
 *
 * It uses a tile-based system to represent tracks and other elements, allowing for easy manipulation and modification of the layout.<br>
 * The class handles events related to adding, deleting, rotating, and flipping tiles, as well as loading and saving layout configurations.<br>
 * It supports different tile types (straight tracks, curved tracks, blocks, sensors, signals, switches, and crossings),<br>
 * each with different properties and behaviors. external resource (e.g., a database or configuration file) to load and save layout data.<br>
 * It also incorporates an undo/redo mechanism for easy recovery from mistakes.<br>
 * It provides options for showing/hiding the grid and offers different modes of operation (adding, deleting, selecting, moving).
 *
 */
public class LayoutPanel extends JPanel {

  private static final long serialVersionUID = 2275543202224445302L;

  private final boolean readonly = true;

  private int gridType;

  public static final String GRID_0 = "/media/square-grid-24.png";
  public static final String GRID_1 = "/media/grid-2-24.png";
  public static final String GRID_2 = "/media/grid-dot-24.png";

  public LayoutPanel() {
    initComponents();
    postInit();
  }

  private void postInit() {
    canvas.setTileType(TileType.NONE);
    if (readonly) {
      canvas.setGridType(0);

      loadBtn.setEnabled(!readonly);
      loadBtn.setVisible(!readonly);
      toolBar.remove(loadBtn);

      routeBtn.setEnabled(readonly);
      routeBtn.setVisible(readonly);

      gridBtn.setEnabled(!readonly);
      gridBtn.setVisible(!readonly);

      autoPilotBtn.setEnabled(false);
      startAllLocomotivesBtn.setEnabled(false);
      toolBar.remove(autoPilotBtn);
      toolBar.remove(startAllLocomotivesBtn);
    } else {
      gridType = 1;
      gridBtn.setIcon(new ImageIcon(getClass().getResource(GRID_1)));
      canvas.setGridType(gridType);

      toolBar.remove(autoPilotBtn);
      toolBar.remove(startAllLocomotivesBtn);
    }
  }

  public void setReadOnly(boolean readonly) {
    if (readonly) {
      canvas.setGridType(0);

      loadBtn.setEnabled(!readonly);
      loadBtn.setVisible(!readonly);
      toolBar.remove(loadBtn);

      routeBtn.setEnabled(readonly);
      routeBtn.setVisible(readonly);

      gridBtn.setEnabled(!readonly);
      gridBtn.setVisible(!readonly);

      autoPilotBtn.setEnabled(false);
      startAllLocomotivesBtn.setEnabled(false);
      toolBar.remove(autoPilotBtn);
      toolBar.remove(startAllLocomotivesBtn);

      if (JCS.getParentFrame() != null) {
        JCS.getParentFrame().hideExtraToolbar(this.toolBar);
      }

    } else {
      gridType = 1;
      gridBtn.setIcon(new ImageIcon(getClass().getResource(GRID_1)));
      canvas.setGridType(gridType);

      toolBar.remove(autoPilotBtn);
      toolBar.remove(startAllLocomotivesBtn);
      toolBar.remove(zoomMinBtn);
      toolBar.remove(zoomPercLbl);
      toolBar.remove(zoomPlusBtn);

      if (JCS.getParentFrame() != null) {
        toolBar.remove(autoPilotBtn);
        toolBar.remove(startAllLocomotivesBtn);
        toolBar.remove(zoomMinBtn);
        toolBar.remove(zoomPercLbl);
        toolBar.remove(zoomPlusBtn);

        topPanel.remove(this.toolBar);
        remove(topPanel);
        doLayout();
        JCS.getParentFrame().showExtraToolbar(this.toolBar);
      }
    }

    canvas.setReadonly(readonly);

  }

  public int getGridType() {
    return gridType;
  }

  public void setGridType(int gridType) {
    this.gridType = gridType;
  }

  public void loadLayoutInBackground() {
    canvas.loadLayoutInBackground();
  }

  public void loadLayout() {
    canvas.loadLayoutInBackground();
  }

  public void rotateSelectedTile() {
    canvas.rotateSelectedTile();
  }

  public void flipSelectedTileHorizontal() {
    canvas.flipSelectedTileHorizontal();
  }

  public void flipSelectedTileVertical() {
    canvas.flipSelectedTileVertical();
  }

  public void deleteSelectedTile() {
    canvas.deleteSelectedTile();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    topPanel = new JPanel();
    toolBar = new JToolBar();
    selectBtn = new JButton();
    filler3 = new Box.Filler(new Dimension(10, 0), new Dimension(10, 0), new Dimension(10, 32767));
    routeBtn = new JButton();
    filler4 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    editBtn = new JToggleButton();
    filler1 = new Box.Filler(new Dimension(10, 0), new Dimension(10, 0), new Dimension(10, 32767));
    zoomMinBtn = new JButton();
    zoomPercLbl = new JLabel();
    zoomPlusBtn = new JButton();
    gridBtn = new JButton();
    autoPilotBtn = new JToggleButton();
    startAllLocomotivesBtn = new JToggleButton();
    filler2 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    loadBtn = new JButton();
    canvasScrollPane = new JScrollPane();
    canvas = new LayoutCanvas(this.readonly);

    setOpaque(false);
    addAncestorListener(new AncestorListener() {
      public void ancestorAdded(AncestorEvent evt) {
        formAncestorAdded(evt);
      }
      public void ancestorMoved(AncestorEvent evt) {
      }
      public void ancestorRemoved(AncestorEvent evt) {
      }
    });
    setLayout(new BorderLayout());

    topPanel.setMaximumSize(new Dimension(32767, 50));
    FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    topPanel.setLayout(flowLayout1);

    toolBar.setMaximumSize(new Dimension(1200, 42));
    toolBar.setName(""); // NOI18N
    toolBar.setPreferredSize(new Dimension(980, 42));

    selectBtn.setIcon(new ImageIcon(getClass().getResource("/media/cursor-24.png"))); // NOI18N
    selectBtn.setFocusable(false);
    selectBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    selectBtn.setMargin(new Insets(2, 2, 2, 2));
    selectBtn.setMaximumSize(new Dimension(38, 38));
    selectBtn.setMinimumSize(new Dimension(38, 38));
    selectBtn.setPreferredSize(new Dimension(38, 38));
    selectBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    selectBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        selectBtnActionPerformed(evt);
      }
    });
    toolBar.add(selectBtn);
    toolBar.add(filler3);

    routeBtn.setIcon(new ImageIcon(getClass().getResource("/media/river-black.png"))); // NOI18N
    routeBtn.setToolTipText("Route");
    routeBtn.setFocusable(false);
    routeBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    routeBtn.setMaximumSize(new Dimension(38, 38));
    routeBtn.setMinimumSize(new Dimension(38, 38));
    routeBtn.setPreferredSize(new Dimension(38, 38));
    routeBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    routeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        routeBtnActionPerformed(evt);
      }
    });
    toolBar.add(routeBtn);
    toolBar.add(filler4);

    editBtn.setIcon(new ImageIcon(getClass().getResource("/media/edit-24.png"))); // NOI18N
    editBtn.setDoubleBuffered(true);
    editBtn.setFocusable(false);
    editBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    editBtn.setMargin(new Insets(2, 2, 2, 2));
    editBtn.setMaximumSize(new Dimension(38, 38));
    editBtn.setMinimumSize(new Dimension(38, 38));
    editBtn.setPreferredSize(new Dimension(38, 38));
    editBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/controller-console-24.png"))); // NOI18N
    editBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    editBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        editBtnActionPerformed(evt);
      }
    });
    toolBar.add(editBtn);
    toolBar.add(filler1);

    zoomMinBtn.setIcon(new ImageIcon(getClass().getResource("/media/zoom-out.png"))); // NOI18N
    zoomMinBtn.setFocusable(false);
    zoomMinBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    zoomMinBtn.setMargin(new Insets(2, 2, 2, 2));
    zoomMinBtn.setMaximumSize(new Dimension(38, 38));
    zoomMinBtn.setMinimumSize(new Dimension(38, 38));
    zoomMinBtn.setPreferredSize(new Dimension(38, 38));
    zoomMinBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    zoomMinBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        zoomMinBtnActionPerformed(evt);
      }
    });
    toolBar.add(zoomMinBtn);

    zoomPercLbl.setHorizontalAlignment(SwingConstants.CENTER);
    zoomPercLbl.setText("100 %");
    zoomPercLbl.setMinimumSize(new Dimension(42, 17));
    zoomPercLbl.setPreferredSize(new Dimension(42, 17));
    toolBar.add(zoomPercLbl);

    zoomPlusBtn.setIcon(new ImageIcon(getClass().getResource("/media/zoom-in.png"))); // NOI18N
    zoomPlusBtn.setFocusable(false);
    zoomPlusBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    zoomPlusBtn.setMargin(new Insets(2, 2, 2, 2));
    zoomPlusBtn.setMaximumSize(new Dimension(38, 38));
    zoomPlusBtn.setMinimumSize(new Dimension(38, 38));
    zoomPlusBtn.setPreferredSize(new Dimension(38, 38));
    zoomPlusBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    zoomPlusBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        zoomPlusBtnActionPerformed(evt);
      }
    });
    toolBar.add(zoomPlusBtn);

    gridBtn.setIcon(new ImageIcon(getClass().getResource("/media/square-grid-24.png"))); // NOI18N
    gridBtn.setFocusable(false);
    gridBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    gridBtn.setMargin(new Insets(2, 2, 2, 2));
    gridBtn.setMaximumSize(new Dimension(38, 38));
    gridBtn.setMinimumSize(new Dimension(38, 38));
    gridBtn.setPreferredSize(new Dimension(38, 38));
    gridBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    gridBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        gridBtnActionPerformed(evt);
      }
    });
    toolBar.add(gridBtn);

    autoPilotBtn.setIcon(new ImageIcon(getClass().getResource("/media/pilot.png"))); // NOI18N
    autoPilotBtn.setToolTipText("Auto mode");
    autoPilotBtn.setDoubleBuffered(true);
    autoPilotBtn.setEnabled(false);
    autoPilotBtn.setFocusable(false);
    autoPilotBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    autoPilotBtn.setMaximumSize(new Dimension(38, 38));
    autoPilotBtn.setMinimumSize(new Dimension(38, 38));
    autoPilotBtn.setPreferredSize(new Dimension(38, 38));
    autoPilotBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/pilot-green.png"))); // NOI18N
    autoPilotBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    autoPilotBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        autoPilotBtnActionPerformed(evt);
      }
    });
    toolBar.add(autoPilotBtn);

    startAllLocomotivesBtn.setIcon(new ImageIcon(getClass().getResource("/media/cruise-control-on-black.png"))); // NOI18N
    startAllLocomotivesBtn.setToolTipText("Start all Locomotives");
    startAllLocomotivesBtn.setDoubleBuffered(true);
    startAllLocomotivesBtn.setEnabled(false);
    startAllLocomotivesBtn.setFocusable(false);
    startAllLocomotivesBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    startAllLocomotivesBtn.setMaximumSize(new Dimension(38, 38));
    startAllLocomotivesBtn.setMinimumSize(new Dimension(38, 38));
    startAllLocomotivesBtn.setPreferredSize(new Dimension(38, 38));
    startAllLocomotivesBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/cruise-control-on-green.png"))); // NOI18N
    startAllLocomotivesBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    startAllLocomotivesBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        startAllLocomotivesBtnActionPerformed(evt);
      }
    });
    toolBar.add(startAllLocomotivesBtn);
    toolBar.add(filler2);

    loadBtn.setIcon(new ImageIcon(getClass().getResource("/media/load-24.png"))); // NOI18N
    loadBtn.setToolTipText("Load");
    loadBtn.setFocusable(false);
    loadBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    loadBtn.setMaximumSize(new Dimension(38, 38));
    loadBtn.setMinimumSize(new Dimension(38, 38));
    loadBtn.setPreferredSize(new Dimension(38, 38));
    loadBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    loadBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        loadBtnActionPerformed(evt);
      }
    });
    toolBar.add(loadBtn);

    topPanel.add(toolBar);

    add(topPanel, BorderLayout.NORTH);

    canvasScrollPane.setViewportView(canvas);

    canvas.setName(""); // NOI18N
    canvasScrollPane.setViewportView(canvas);

    add(canvasScrollPane, BorderLayout.CENTER);
    canvasScrollPane.getAccessibleContext().setAccessibleDescription("");
  }// </editor-fold>//GEN-END:initComponents

    private void loadBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_loadBtnActionPerformed
      loadLayoutInBackground();
    }//GEN-LAST:event_loadBtnActionPerformed

    private void routeBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_routeBtnActionPerformed
      showRoutes();
    }//GEN-LAST:event_routeBtnActionPerformed

  private void autoPilotBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_autoPilotBtnActionPerformed
    Logger.trace(evt.getActionCommand() + (autoPilotBtn.isSelected() ? " Enable" : " Disable") + " Auto mode");

    if (autoPilotBtn.isSelected()) {
      startAllLocomotivesBtn.setEnabled(true);
    } else {
      startAllLocomotivesBtn.setEnabled(false);
    }
    RailController.getInstance().enableAutomode(autoPilotBtn.isSelected());
  }//GEN-LAST:event_autoPilotBtnActionPerformed

  private void startAllLocomotivesBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_startAllLocomotivesBtnActionPerformed
    Logger.trace(evt.getActionCommand() + " Start All Locomotives " + this.startAllLocomotivesBtn.isSelected());
    if (startAllLocomotivesBtn.isSelected()) {
      RailController.getInstance().startAllLocomotives();
    }
  }//GEN-LAST:event_startAllLocomotivesBtnActionPerformed

  public void showGrid() {
    gridType++;
    if (gridType > 2) {
      gridType = 0;
    }
    switch (gridType) {
      case 0 -> {
        gridBtn.setIcon(new ImageIcon(getClass().getResource(GRID_0)));
      }
      case 1 -> {
        gridBtn.setIcon(new ImageIcon(getClass().getResource(GRID_1)));
      }
      case 2 -> {
        gridBtn.setIcon(new ImageIcon(getClass().getResource(GRID_2)));
      }
    }
    canvas.setGridType(gridType);
  }

  public void zoomIn() {
    LayoutScale.getInstance().zoomIn();
    zoomPercLbl.setText(LayoutScale.getInstance().getScalePercent() + " %");
    canvas.setScale(LayoutScale.getInstance().getScalePercent());
  }

  public void zoomOut() {
    LayoutScale.getInstance().zoomOut();
    zoomPercLbl.setText(LayoutScale.getInstance().getScalePercent() + " %");
    canvas.setScale(LayoutScale.getInstance().getScalePercent());
  }

  private void gridBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_gridBtnActionPerformed
    showGrid();
  }//GEN-LAST:event_gridBtnActionPerformed

  private void zoomMinBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_zoomMinBtnActionPerformed
    zoomIn();
  }//GEN-LAST:event_zoomMinBtnActionPerformed

  private void zoomPlusBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_zoomPlusBtnActionPerformed
    zoomOut();
  }//GEN-LAST:event_zoomPlusBtnActionPerformed

  private void editBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_editBtnActionPerformed
    setReadOnly(!editBtn.isSelected());
  }//GEN-LAST:event_editBtnActionPerformed

  private void selectBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_selectBtnActionPerformed
    this.canvas.resetSelection();
  }//GEN-LAST:event_selectBtnActionPerformed

  /**
   * When added to the JCSFrame remove the toolbar and panel and some buttons which are taken over by the JCS Frame
   *
   * @param evt
   */
  private void formAncestorAdded(AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    if (JCS.getParentFrame() != null) {
      toolBar.remove(autoPilotBtn);
      toolBar.remove(startAllLocomotivesBtn);
      toolBar.remove(zoomMinBtn);
      toolBar.remove(zoomPercLbl);
      toolBar.remove(zoomPlusBtn);
      toolBar.remove(editBtn);

      topPanel.remove(toolBar);
      remove(topPanel);
      doLayout();
    }
  }//GEN-LAST:event_formAncestorAdded

  public void showRoutes() {
    canvas.showRoutesDialog();
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JToggleButton autoPilotBtn;
  private LayoutCanvas canvas;
  private JScrollPane canvasScrollPane;
  private JToggleButton editBtn;
  private Box.Filler filler1;
  private Box.Filler filler2;
  private Box.Filler filler3;
  private Box.Filler filler4;
  private JButton gridBtn;
  private JButton loadBtn;
  private JButton routeBtn;
  private JButton selectBtn;
  private JToggleButton startAllLocomotivesBtn;
  private JToolBar toolBar;
  private JPanel topPanel;
  private JButton zoomMinBtn;
  private JLabel zoomPercLbl;
  private JButton zoomPlusBtn;
  // End of variables declaration//GEN-END:variables
}
