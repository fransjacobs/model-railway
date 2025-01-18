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
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import jcs.JCS;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.events.PowerEventListener;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Direction;
import jcs.entities.TileBean.TileType;
import jcs.util.RunUtil;
import org.tinylog.Logger;

/**
 * This canvas / Panel is used to draw the layout
 *
 * @author frans
 */
public class LayoutPanel extends JPanel {

  private final boolean readonly;

  public LayoutPanel() {
    this(false);
  }

  public LayoutPanel(boolean readonly) {
    this.readonly = readonly;
    initComponents();
    postInit();
  }

  private void postInit() {
    RunUtil.loadProperties();

    this.straightBtn.setSelected(true);
    this.canvas.setTileType(TileType.STRAIGHT);
    this.setMode(readonly ? LayoutCanvas.Mode.CONTROL : LayoutCanvas.Mode.SELECT);

    if (readonly) {
      this.canvas.setDrawGrid(!readonly);

      this.saveBtn.setEnabled(!readonly);
      this.saveBtn.setVisible(!readonly);
      this.toolBar.remove(this.saveBtn);

      this.loadBtn.setEnabled(!readonly);
      this.loadBtn.setVisible(!readonly);
      this.toolBar.remove(this.loadBtn);

      this.repaintBtn.setEnabled(readonly);
      this.repaintBtn.setVisible(readonly);

      this.routeBtn.setEnabled(readonly);
      this.routeBtn.setVisible(readonly);

      this.selectBtn.setEnabled(!readonly);
      this.selectBtn.setVisible(!readonly);
      this.toolBar.remove(this.selectBtn);

      this.addBtn.setEnabled(!readonly);
      this.addBtn.setVisible(!readonly);

      this.deleteBtn.setEnabled(!readonly);
      this.deleteBtn.setVisible(!readonly);

      this.gridBtn.setEnabled(!readonly);
      this.gridBtn.setVisible(!readonly);

      this.straightBtn.setEnabled(!readonly);
      this.straightBtn.setVisible(!readonly);

      this.curvedBtn.setEnabled(!readonly);
      this.curvedBtn.setVisible(!readonly);

      this.blockBtn.setEnabled(!readonly);
      this.blockBtn.setVisible(!readonly);

      this.sensorBtn.setEnabled(!readonly);
      this.sensorBtn.setVisible(!readonly);

      this.signalBtn.setEnabled(!readonly);
      this.signalBtn.setVisible(!readonly);

      this.leftSwitchBtn.setEnabled(!readonly);
      this.leftSwitchBtn.setVisible(!readonly);

      this.rightSwitchBtn.setEnabled(!readonly);
      this.rightSwitchBtn.setVisible(!readonly);

      this.crossLBtn.setEnabled(!readonly);
      this.crossLBtn.setVisible(!readonly);

      this.crossRBtn.setEnabled(!readonly);
      this.crossRBtn.setVisible(!readonly);

      this.endTrackBtn.setEnabled(!readonly);
      this.endTrackBtn.setVisible(!readonly);

      this.straightDirectionBtn.setEnabled(!readonly);
      this.straightDirectionBtn.setVisible(!readonly);

      this.crossingBtn.setEnabled(!readonly);
      this.crossingBtn.setVisible(!readonly);

      this.moveBtn.setEnabled(!readonly);
      this.moveBtn.setVisible(!readonly);

      this.flipVerticalBtn.setEnabled(!readonly);
      this.flipVerticalBtn.setVisible(!readonly);

      this.flipHorizontalBtn.setEnabled(!readonly);
      this.flipHorizontalBtn.setVisible(!readonly);

      this.rotateBtn.setEnabled(!readonly);
      this.rotateBtn.setVisible(!readonly);

      //Todo Remove the Autopilot related things from this panel
      //this.autoPilotBtn.setEnabled(readonly && JCS.getJcsCommandStation().isPowerOn());
//      this.autoPilotBtn.setEnabled(readonly);
//      this.autoPilotBtn.setVisible(readonly);
//      this.resetAutopilotBtn.setEnabled(readonly);
//      this.resetAutopilotBtn.setVisible(readonly);
//      this.startAllLocomotivesBtn.setEnabled(readonly && this.autoPilotBtn.isSelected());
//      this.startAllLocomotivesBtn.setVisible(readonly);
    } else {
      if ("true".equals(System.getProperty("batch.tile.persist", "true"))) {
        this.saveBtn.setEnabled(true);
        this.saveBtn.setVisible(true);
      } else {
        this.saveBtn.setEnabled(false);
        this.saveBtn.setVisible(false);
        this.toolBar.remove(this.saveBtn);
      }

//      this.toolBar.remove(this.autoPilotBtn);
//      this.autoPilotBtn.setEnabled(readonly);
//      this.autoPilotBtn.setVisible(readonly);
//      this.toolBar.remove(this.resetAutopilotBtn);
//      this.resetAutopilotBtn.setEnabled(readonly);
//      this.resetAutopilotBtn.setVisible(readonly);
//      this.toolBar.remove(this.startAllLocomotivesBtn);
//      this.startAllLocomotivesBtn.setEnabled(readonly);
//      this.startAllLocomotivesBtn.setVisible(readonly);
    }

    this.toolBar.remove(this.autoPilotBtn);
    this.toolBar.remove(this.resetAutopilotBtn);
    this.toolBar.remove(this.startAllLocomotivesBtn);

    if (readonly) {
      loadLayout();

      Powerlistener powerlistener = new Powerlistener(this);
      JCS.getJcsCommandStation().addPowerEventListener(powerlistener);

    }

  }

//  void saveLayout() {
//    this.canvas.saveLayout();
//  }

  public void loadLayout() {
    this.canvas.loadLayoutInBackground();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    straightPopupMenu = new JPopupMenu();
    verticalMI = new JMenuItem();
    horizontalMI = new JMenuItem();
    curvedPopupMenu = new JPopupMenu();
    rightMI = new JMenuItem();
    leftMI = new JMenuItem();
    operationsPM = new JPopupMenu();
    xyMI = new JMenuItem();
    propertiesMI = new JMenuItem();
    rotateMI = new JMenuItem();
    flipHorizontalMI = new JMenuItem();
    flipVerticalMI = new JMenuItem();
    moveMI = new JMenuItem();
    deleteMI = new JMenuItem();
    tileBtnGroup = new ButtonGroup();
    topPanel = new JPanel();
    toolBar = new JToolBar();
    saveBtn = new JButton();
    loadBtn = new JButton();
    repaintBtn = new JButton();
    routeBtn = new JButton();
    autoPilotBtn = new JToggleButton();
    startAllLocomotivesBtn = new JToggleButton();
    resetAutopilotBtn = new JButton();
    filler1 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    selectBtn = new JButton();
    addBtn = new JButton();
    deleteBtn = new JButton();
    filler3 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    gridBtn = new JToggleButton();
    filler2 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    straightBtn = new JToggleButton();
    curvedBtn = new JToggleButton();
    blockBtn = new JToggleButton();
    sensorBtn = new JToggleButton();
    signalBtn = new JToggleButton();
    leftSwitchBtn = new JToggleButton();
    rightSwitchBtn = new JToggleButton();
    crossLBtn = new JToggleButton();
    crossRBtn = new JToggleButton();
    straightDirectionBtn = new JToggleButton();
    endTrackBtn = new JToggleButton();
    crossingBtn = new JToggleButton();
    filler4 = new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 32767));
    moveBtn = new JButton();
    flipVerticalBtn = new JButton();
    flipHorizontalBtn = new JButton();
    rotateBtn = new JButton();
    canvasScrollPane = new JScrollPane();
    canvas = new LayoutCanvas(this.readonly);

    verticalMI.setText("Vertical");
    verticalMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        verticalMIActionPerformed(evt);
      }
    });
    straightPopupMenu.add(verticalMI);

    horizontalMI.setText("Horizontal");
    horizontalMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        horizontalMIActionPerformed(evt);
      }
    });
    straightPopupMenu.add(horizontalMI);

    rightMI.setText("Right");
    rightMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        rightMIActionPerformed(evt);
      }
    });
    curvedPopupMenu.add(rightMI);

    leftMI.setText("Left");
    leftMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        leftMIActionPerformed(evt);
      }
    });
    curvedPopupMenu.add(leftMI);

    xyMI.setText("x: y:");
    operationsPM.add(xyMI);

    propertiesMI.setText("Properties");
    propertiesMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        propertiesMIActionPerformed(evt);
      }
    });
    operationsPM.add(propertiesMI);

    rotateMI.setText("Rotate");
    rotateMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        rotateMIActionPerformed(evt);
      }
    });
    operationsPM.add(rotateMI);

    flipHorizontalMI.setText("Flip Horizontal");
    flipHorizontalMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        flipHorizontalMIActionPerformed(evt);
      }
    });
    operationsPM.add(flipHorizontalMI);

    flipVerticalMI.setText("Flip Vertical");
    flipVerticalMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        flipVerticalMIActionPerformed(evt);
      }
    });
    operationsPM.add(flipVerticalMI);

    moveMI.setText("Move");
    moveMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        moveMIActionPerformed(evt);
      }
    });
    operationsPM.add(moveMI);

    deleteMI.setText("Delete");
    deleteMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        deleteMIActionPerformed(evt);
      }
    });
    operationsPM.add(deleteMI);

    setMinimumSize(new Dimension(1400, 900));
    setOpaque(false);
    setPreferredSize(new Dimension(1400, 900));
    addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent evt) {
        formComponentHidden(evt);
      }
      public void componentResized(ComponentEvent evt) {
        formComponentResized(evt);
      }
      public void componentShown(ComponentEvent evt) {
        formComponentShown(evt);
      }
    });
    setLayout(new BorderLayout());

    topPanel.setMaximumSize(new Dimension(32767, 50));
    topPanel.setMinimumSize(new Dimension(1400, 50));
    topPanel.setPreferredSize(new Dimension(1400, 50));
    FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    topPanel.setLayout(flowLayout1);

    toolBar.setDoubleBuffered(true);
    toolBar.setMargin(new Insets(1, 1, 1, 1));
    toolBar.setMaximumSize(new Dimension(1200, 42));
    toolBar.setMinimumSize(new Dimension(1150, 42));
    toolBar.setName(""); // NOI18N
    toolBar.setPreferredSize(new Dimension(1100, 42));

    saveBtn.setIcon(new ImageIcon(getClass().getResource("/media/save-24.png"))); // NOI18N
    saveBtn.setToolTipText("Save");
    saveBtn.setFocusable(false);
    saveBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    saveBtn.setMaximumSize(new Dimension(40, 40));
    saveBtn.setMinimumSize(new Dimension(40, 40));
    saveBtn.setPreferredSize(new Dimension(40, 40));
    saveBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    saveBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveBtnActionPerformed(evt);
      }
    });
    toolBar.add(saveBtn);

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

    repaintBtn.setIcon(new ImageIcon(getClass().getResource("/media/CS2-3-Sync.png"))); // NOI18N
    repaintBtn.setToolTipText("Repaint");
    repaintBtn.setFocusable(false);
    repaintBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    repaintBtn.setMaximumSize(new Dimension(40, 40));
    repaintBtn.setMinimumSize(new Dimension(38, 38));
    repaintBtn.setPreferredSize(new Dimension(38, 38));
    repaintBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    repaintBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        repaintBtnActionPerformed(evt);
      }
    });
    toolBar.add(repaintBtn);

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

    autoPilotBtn.setIcon(new ImageIcon(getClass().getResource("/media/pilot.png"))); // NOI18N
    autoPilotBtn.setToolTipText("Auto mode");
    autoPilotBtn.setDoubleBuffered(true);
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

    resetAutopilotBtn.setIcon(new ImageIcon(getClass().getResource("/media/director-red.png"))); // NOI18N
    resetAutopilotBtn.setToolTipText("Reset AutoPilot");
    resetAutopilotBtn.setFocusable(false);
    resetAutopilotBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    resetAutopilotBtn.setMaximumSize(new Dimension(38, 38));
    resetAutopilotBtn.setMinimumSize(new Dimension(38, 38));
    resetAutopilotBtn.setPreferredSize(new Dimension(38, 38));
    resetAutopilotBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    resetAutopilotBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        resetAutopilotBtnActionPerformed(evt);
      }
    });
    toolBar.add(resetAutopilotBtn);
    toolBar.add(filler1);

    selectBtn.setIcon(new ImageIcon(getClass().getResource("/media/cursor-24-y.png"))); // NOI18N
    selectBtn.setToolTipText("Select");
    selectBtn.setFocusable(false);
    selectBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    selectBtn.setMaximumSize(new Dimension(40, 40));
    selectBtn.setMinimumSize(new Dimension(38, 38));
    selectBtn.setPreferredSize(new Dimension(38, 38));
    selectBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    selectBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        selectBtnActionPerformed(evt);
      }
    });
    toolBar.add(selectBtn);

    addBtn.setIcon(new ImageIcon(getClass().getResource("/media/add-24.png"))); // NOI18N
    addBtn.setToolTipText("Add");
    addBtn.setFocusable(false);
    addBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    addBtn.setMaximumSize(new Dimension(40, 40));
    addBtn.setMinimumSize(new Dimension(38, 38));
    addBtn.setPreferredSize(new Dimension(38, 38));
    addBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    addBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        addBtnActionPerformed(evt);
      }
    });
    toolBar.add(addBtn);

    deleteBtn.setIcon(new ImageIcon(getClass().getResource("/media/delete-24.png"))); // NOI18N
    deleteBtn.setToolTipText("Delete");
    deleteBtn.setFocusable(false);
    deleteBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    deleteBtn.setMaximumSize(new Dimension(40, 40));
    deleteBtn.setMinimumSize(new Dimension(38, 38));
    deleteBtn.setPreferredSize(new Dimension(38, 38));
    deleteBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    deleteBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        deleteBtnActionPerformed(evt);
      }
    });
    toolBar.add(deleteBtn);
    toolBar.add(filler3);

    gridBtn.setIcon(new ImageIcon(getClass().getResource("/media/grid-2-24.png"))); // NOI18N
    gridBtn.setSelected(true);
    gridBtn.setToolTipText("Grid");
    gridBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    gridBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/grid-dot-24.png"))); // NOI18N
    gridBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    gridBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        gridBtnActionPerformed(evt);
      }
    });
    toolBar.add(gridBtn);
    toolBar.add(filler2);

    tileBtnGroup.add(straightBtn);
    straightBtn.setIcon(new ImageIcon(getClass().getResource("/media/new-straight.png"))); // NOI18N
    straightBtn.setToolTipText("Straight Track");
    straightBtn.setDoubleBuffered(true);
    straightBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    straightBtn.setMaximumSize(new Dimension(38, 38));
    straightBtn.setMinimumSize(new Dimension(38, 38));
    straightBtn.setPreferredSize(new Dimension(38, 38));
    straightBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/new-straight_Y.png"))); // NOI18N
    straightBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    straightBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        straightBtnActionPerformed(evt);
      }
    });
    toolBar.add(straightBtn);

    tileBtnGroup.add(curvedBtn);
    curvedBtn.setIcon(new ImageIcon(getClass().getResource("/media/new-diagonal.png"))); // NOI18N
    curvedBtn.setToolTipText("Curved Track");
    curvedBtn.setDoubleBuffered(true);
    curvedBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    curvedBtn.setMaximumSize(new Dimension(38, 38));
    curvedBtn.setMinimumSize(new Dimension(38, 38));
    curvedBtn.setPreferredSize(new Dimension(38, 38));
    curvedBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/new-diagonal_Y.png"))); // NOI18N
    curvedBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    curvedBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        curvedBtnActionPerformed(evt);
      }
    });
    toolBar.add(curvedBtn);

    tileBtnGroup.add(blockBtn);
    blockBtn.setIcon(new ImageIcon(getClass().getResource("/media/new-block.png"))); // NOI18N
    blockBtn.setToolTipText("Block");
    blockBtn.setDoubleBuffered(true);
    blockBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    blockBtn.setMaximumSize(new Dimension(38, 38));
    blockBtn.setMinimumSize(new Dimension(38, 38));
    blockBtn.setPreferredSize(new Dimension(38, 38));
    blockBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/new-block_Y.png"))); // NOI18N
    blockBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    blockBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        blockBtnActionPerformed(evt);
      }
    });
    toolBar.add(blockBtn);

    tileBtnGroup.add(sensorBtn);
    sensorBtn.setIcon(new ImageIcon(getClass().getResource("/media/new-straight-feedback.png"))); // NOI18N
    sensorBtn.setToolTipText("Sensor");
    sensorBtn.setDoubleBuffered(true);
    sensorBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    sensorBtn.setMaximumSize(new Dimension(38, 38));
    sensorBtn.setMinimumSize(new Dimension(38, 38));
    sensorBtn.setPreferredSize(new Dimension(38, 38));
    sensorBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/new-straight-feedback_Y.png"))); // NOI18N
    sensorBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    sensorBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        sensorBtnActionPerformed(evt);
      }
    });
    toolBar.add(sensorBtn);

    tileBtnGroup.add(signalBtn);
    signalBtn.setIcon(new ImageIcon(getClass().getResource("/media/new-straight-signal.png"))); // NOI18N
    signalBtn.setDoubleBuffered(true);
    signalBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    signalBtn.setMaximumSize(new Dimension(38, 38));
    signalBtn.setMinimumSize(new Dimension(38, 38));
    signalBtn.setPreferredSize(new Dimension(38, 38));
    signalBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/new-straight-signal_Y.png"))); // NOI18N
    signalBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    signalBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        signalBtnActionPerformed(evt);
      }
    });
    toolBar.add(signalBtn);

    tileBtnGroup.add(leftSwitchBtn);
    leftSwitchBtn.setIcon(new ImageIcon(getClass().getResource("/media/new-L-turnout.png"))); // NOI18N
    leftSwitchBtn.setDoubleBuffered(true);
    leftSwitchBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    leftSwitchBtn.setMaximumSize(new Dimension(38, 38));
    leftSwitchBtn.setMinimumSize(new Dimension(38, 38));
    leftSwitchBtn.setPreferredSize(new Dimension(38, 38));
    leftSwitchBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/new-LY-turnout.png"))); // NOI18N
    leftSwitchBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    leftSwitchBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        leftSwitchBtnActionPerformed(evt);
      }
    });
    toolBar.add(leftSwitchBtn);

    tileBtnGroup.add(rightSwitchBtn);
    rightSwitchBtn.setIcon(new ImageIcon(getClass().getResource("/media/new-R-turnout.png"))); // NOI18N
    rightSwitchBtn.setToolTipText("");
    rightSwitchBtn.setDoubleBuffered(true);
    rightSwitchBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    rightSwitchBtn.setMaximumSize(new Dimension(38, 38));
    rightSwitchBtn.setMinimumSize(new Dimension(38, 38));
    rightSwitchBtn.setPreferredSize(new Dimension(38, 38));
    rightSwitchBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/new-RY-turnout.png"))); // NOI18N
    rightSwitchBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    rightSwitchBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        rightSwitchBtnActionPerformed(evt);
      }
    });
    toolBar.add(rightSwitchBtn);

    tileBtnGroup.add(crossLBtn);
    crossLBtn.setIcon(new ImageIcon(getClass().getResource("/media/new-cross-L.png"))); // NOI18N
    crossLBtn.setToolTipText("");
    crossLBtn.setDoubleBuffered(true);
    crossLBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    crossLBtn.setMaximumSize(new Dimension(40, 40));
    crossLBtn.setMinimumSize(new Dimension(38, 38));
    crossLBtn.setPreferredSize(new Dimension(38, 38));
    crossLBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/new-cross-LY.png"))); // NOI18N
    crossLBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    crossLBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        crossLBtnActionPerformed(evt);
      }
    });
    toolBar.add(crossLBtn);

    tileBtnGroup.add(crossRBtn);
    crossRBtn.setIcon(new ImageIcon(getClass().getResource("/media/new-cross-R.png"))); // NOI18N
    crossRBtn.setToolTipText("");
    crossRBtn.setDoubleBuffered(true);
    crossRBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    crossRBtn.setMaximumSize(new Dimension(38, 38));
    crossRBtn.setMinimumSize(new Dimension(38, 38));
    crossRBtn.setPreferredSize(new Dimension(38, 38));
    crossRBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/new-cross-RY.png"))); // NOI18N
    crossRBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    crossRBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        crossRBtnActionPerformed(evt);
      }
    });
    toolBar.add(crossRBtn);

    tileBtnGroup.add(straightDirectionBtn);
    straightDirectionBtn.setIcon(new ImageIcon(getClass().getResource("/media/new-straightDirection.png"))); // NOI18N
    straightDirectionBtn.setDoubleBuffered(true);
    straightDirectionBtn.setFocusable(false);
    straightDirectionBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    straightDirectionBtn.setMaximumSize(new Dimension(38, 38));
    straightDirectionBtn.setMinimumSize(new Dimension(38, 38));
    straightDirectionBtn.setPreferredSize(new Dimension(38, 38));
    straightDirectionBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/new-straightDirection_Y.png"))); // NOI18N
    straightDirectionBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    straightDirectionBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        straightDirectionBtnActionPerformed(evt);
      }
    });
    toolBar.add(straightDirectionBtn);

    tileBtnGroup.add(endTrackBtn);
    endTrackBtn.setIcon(new ImageIcon(getClass().getResource("/media/new-end-track.png"))); // NOI18N
    endTrackBtn.setFocusable(false);
    endTrackBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    endTrackBtn.setMaximumSize(new Dimension(38, 38));
    endTrackBtn.setMinimumSize(new Dimension(38, 38));
    endTrackBtn.setPreferredSize(new Dimension(38, 38));
    endTrackBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/new-end-track_Y.png"))); // NOI18N
    endTrackBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    endTrackBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        endTrackBtnActionPerformed(evt);
      }
    });
    toolBar.add(endTrackBtn);

    tileBtnGroup.add(crossingBtn);
    crossingBtn.setIcon(new ImageIcon(getClass().getResource("/media/new-crossing.png"))); // NOI18N
    crossingBtn.setFocusable(false);
    crossingBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    crossingBtn.setMaximumSize(new Dimension(38, 38));
    crossingBtn.setMinimumSize(new Dimension(38, 38));
    crossingBtn.setPreferredSize(new Dimension(38, 38));
    crossingBtn.setSelectedIcon(new ImageIcon(getClass().getResource("/media/new-crossing_Y.png"))); // NOI18N
    crossingBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    crossingBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        crossingBtnActionPerformed(evt);
      }
    });
    toolBar.add(crossingBtn);
    toolBar.add(filler4);

    moveBtn.setIcon(new ImageIcon(getClass().getResource("/media/drag-24.png"))); // NOI18N
    moveBtn.setToolTipText("Move");
    moveBtn.setFocusable(false);
    moveBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    moveBtn.setMaximumSize(new Dimension(40, 40));
    moveBtn.setMinimumSize(new Dimension(38, 38));
    moveBtn.setPreferredSize(new Dimension(38, 38));
    moveBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    moveBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        moveBtnActionPerformed(evt);
      }
    });
    toolBar.add(moveBtn);

    flipVerticalBtn.setIcon(new ImageIcon(getClass().getResource("/media/flip-vertical-24.png"))); // NOI18N
    flipVerticalBtn.setToolTipText("Flip Vertical");
    flipVerticalBtn.setFocusable(false);
    flipVerticalBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    flipVerticalBtn.setMaximumSize(new Dimension(40, 40));
    flipVerticalBtn.setMinimumSize(new Dimension(38, 38));
    flipVerticalBtn.setPreferredSize(new Dimension(38, 38));
    flipVerticalBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    flipVerticalBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        flipVerticalBtnActionPerformed(evt);
      }
    });
    toolBar.add(flipVerticalBtn);

    flipHorizontalBtn.setIcon(new ImageIcon(getClass().getResource("/media/flip-horizontal-24.png"))); // NOI18N
    flipHorizontalBtn.setToolTipText("Flip Horizontal");
    flipHorizontalBtn.setFocusable(false);
    flipHorizontalBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    flipHorizontalBtn.setMaximumSize(new Dimension(40, 40));
    flipHorizontalBtn.setMinimumSize(new Dimension(38, 38));
    flipHorizontalBtn.setPreferredSize(new Dimension(38, 38));
    flipHorizontalBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    flipHorizontalBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        flipHorizontalBtnActionPerformed(evt);
      }
    });
    toolBar.add(flipHorizontalBtn);

    rotateBtn.setIcon(new ImageIcon(getClass().getResource("/media/rotate2-24.png"))); // NOI18N
    rotateBtn.setToolTipText("Rotate");
    rotateBtn.setFocusable(false);
    rotateBtn.setHorizontalTextPosition(SwingConstants.CENTER);
    rotateBtn.setMaximumSize(new Dimension(40, 40));
    rotateBtn.setMinimumSize(new Dimension(38, 38));
    rotateBtn.setPreferredSize(new Dimension(38, 38));
    rotateBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
    rotateBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        rotateBtnActionPerformed(evt);
      }
    });
    toolBar.add(rotateBtn);

    topPanel.add(toolBar);

    add(topPanel, BorderLayout.NORTH);

    canvasScrollPane.setDoubleBuffered(true);
    canvasScrollPane.setMinimumSize(new Dimension(1024, 850));
    canvasScrollPane.setPreferredSize(new Dimension(1024, 850));
    canvasScrollPane.setViewportView(canvas);

    canvas.setName(""); // NOI18N
    canvas.setLayout(new BorderLayout());
    canvasScrollPane.setViewportView(canvas);

    add(canvasScrollPane, BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void horizontalMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_horizontalMIActionPerformed
//      Logger.trace(this.orientation + ", " + evt.getModifiers() + ", " + evt.paramString());
//
//      if (this.mouseLocation != null && evt.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
//          addTile(this.mouseLocation);
//          this.mouseLocation = null;
//      }
  }//GEN-LAST:event_horizontalMIActionPerformed

  private void verticalMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_verticalMIActionPerformed
//      Logger.trace(this.orientation + ", " + evt.getModifiers() + ", " + evt.paramString());
//
//      if (this.mouseLocation != null && evt.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
//          addTile(this.mouseLocation);
//          this.mouseLocation = null;
//      }
  }//GEN-LAST:event_verticalMIActionPerformed

  private void rightMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rightMIActionPerformed
//      Logger.trace(this.orientation + ", " + evt.getModifiers() + ", " + evt.paramString());
//
//      if (this.mouseLocation != null && evt.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
//          addTile(this.mouseLocation);
//          this.mouseLocation = null;
//      }
  }//GEN-LAST:event_rightMIActionPerformed

  private void leftMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_leftMIActionPerformed
//      Logger.trace(this.orientation + ", " + evt.getModifiers() + ", " + evt.paramString());
//
//      if (this.mouseLocation != null && evt.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
//          addTile(this.mouseLocation);
//
//          //do someting with the rotation/direction...
//          this.mouseLocation = null;
//      }
  }//GEN-LAST:event_leftMIActionPerformed

    private void rotateMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rotateMIActionPerformed
//        rotateSelectedTile();
    }//GEN-LAST:event_rotateMIActionPerformed

    private void flipHorizontalMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_flipHorizontalMIActionPerformed
//        flipSelectedTileHorizontal();
    }//GEN-LAST:event_flipHorizontalMIActionPerformed

    private void flipVerticalMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_flipVerticalMIActionPerformed
//        flipSelectedTileVertical();
    }//GEN-LAST:event_flipVerticalMIActionPerformed

    private void moveMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_moveMIActionPerformed
//        this.mode = Mode.MOVE;
    }//GEN-LAST:event_moveMIActionPerformed

    private void deleteMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_deleteMIActionPerformed
      this.canvas.removeTiles();
    }//GEN-LAST:event_deleteMIActionPerformed

    private void propertiesMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_propertiesMIActionPerformed
//        editSelectedTileProperties();
    }//GEN-LAST:event_propertiesMIActionPerformed

    private void saveBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
      this.canvas.saveLayout();
    }//GEN-LAST:event_saveBtnActionPerformed

    private void loadBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_loadBtnActionPerformed
      this.loadLayout();
    }//GEN-LAST:event_loadBtnActionPerformed

    private void selectBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_selectBtnActionPerformed
      setMode(LayoutCanvas.Mode.SELECT);
    }//GEN-LAST:event_selectBtnActionPerformed

    private void addBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
      setMode(LayoutCanvas.Mode.ADD);
    }//GEN-LAST:event_addBtnActionPerformed

    private void deleteBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
      setMode(LayoutCanvas.Mode.DELETE);
      this.canvas.removeTiles();
    }//GEN-LAST:event_deleteBtnActionPerformed

    private void repaintBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_repaintBtnActionPerformed
      this.canvas.repaint();
    }//GEN-LAST:event_repaintBtnActionPerformed

    private void straightBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_straightBtnActionPerformed
      setTileType(TileBean.TileType.STRAIGHT);
    }//GEN-LAST:event_straightBtnActionPerformed

    private void curvedBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_curvedBtnActionPerformed
      setTileType(TileBean.TileType.CURVED);
    }//GEN-LAST:event_curvedBtnActionPerformed

    private void blockBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_blockBtnActionPerformed
      setTileType(TileBean.TileType.BLOCK);
    }//GEN-LAST:event_blockBtnActionPerformed

    private void rotateBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rotateBtnActionPerformed
      this.canvas.rotateSelectedTile();
    }//GEN-LAST:event_rotateBtnActionPerformed

    private void flipHorizontalBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_flipHorizontalBtnActionPerformed
      this.canvas.flipSelectedTileHorizontal();
    }//GEN-LAST:event_flipHorizontalBtnActionPerformed

    private void flipVerticalBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_flipVerticalBtnActionPerformed
      this.canvas.flipSelectedTileVertical();
    }//GEN-LAST:event_flipVerticalBtnActionPerformed

    private void moveBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_moveBtnActionPerformed
      setMode(LayoutCanvas.Mode.MOVE);
    }//GEN-LAST:event_moveBtnActionPerformed

    private void rightSwitchBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rightSwitchBtnActionPerformed
      this.setTileType(TileBean.TileType.SWITCH);
      this.setDirection(Direction.RIGHT);
    }//GEN-LAST:event_rightSwitchBtnActionPerformed

    private void leftSwitchBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_leftSwitchBtnActionPerformed
      this.setTileType(TileBean.TileType.SWITCH);
      this.setDirection(Direction.LEFT);
    }//GEN-LAST:event_leftSwitchBtnActionPerformed

    private void signalBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_signalBtnActionPerformed
      setTileType(TileBean.TileType.SIGNAL);
    }//GEN-LAST:event_signalBtnActionPerformed

    private void sensorBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sensorBtnActionPerformed
      setTileType(TileBean.TileType.SENSOR);
    }//GEN-LAST:event_sensorBtnActionPerformed

    private void gridBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_gridBtnActionPerformed
      this.canvas.setDrawGrid(this.gridBtn.isSelected());
      this.canvas.repaint();
    }//GEN-LAST:event_gridBtnActionPerformed

    private void formComponentResized(ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
      //TODO!       
      //Logger.debug(evt.getComponent().getSize());// TODO add your handling code here:
    }//GEN-LAST:event_formComponentResized

    private void crossLBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_crossLBtnActionPerformed
      setTileType(TileBean.TileType.CROSS);
      this.setDirection(Direction.LEFT);
    }//GEN-LAST:event_crossLBtnActionPerformed

    private void crossRBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_crossRBtnActionPerformed
      setTileType(TileBean.TileType.CROSS);
      this.setDirection(Direction.RIGHT);
    }//GEN-LAST:event_crossRBtnActionPerformed

    private void routeBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_routeBtnActionPerformed
      Logger.debug("Start Routing...");
      this.canvas.showRoutesDialog();
    }//GEN-LAST:event_routeBtnActionPerformed

    private void formComponentHidden(ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
      ///Logger.trace("HIDDEN");
      if (JCS.getParentFrame() != null) {
        JCS.getParentFrame().hideExtraToolbar(this.toolBar);
      }
    }//GEN-LAST:event_formComponentHidden

    private void formComponentShown(ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
      Logger.trace("SHOWN");
      if (JCS.getParentFrame() != null) {
        topPanel.remove(this.toolBar);
        this.remove(topPanel);
        this.doLayout();
        JCS.getParentFrame().showExtraToolbar(this.toolBar);
      }
    }//GEN-LAST:event_formComponentShown

  private void straightDirectionBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_straightDirectionBtnActionPerformed
    setTileType(TileBean.TileType.STRAIGHT_DIR);
  }//GEN-LAST:event_straightDirectionBtnActionPerformed

  private void endTrackBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_endTrackBtnActionPerformed
    setTileType(TileBean.TileType.END);
  }//GEN-LAST:event_endTrackBtnActionPerformed

  private void autoPilotBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_autoPilotBtnActionPerformed
    Logger.trace(evt.getActionCommand() + (this.autoPilotBtn.isSelected() ? " Enable" : " Disable") + " Auto mode");

    if (this.autoPilotBtn.isSelected()) {
      this.startAllLocomotivesBtn.setEnabled(true);
    } else {
      if (this.startAllLocomotivesBtn.isSelected()) {
        startAllLocomotivesBtn.doClick();
      }
      this.startAllLocomotivesBtn.setEnabled(false);
    }

    AutoPilot.runAutoPilot(this.autoPilotBtn.isSelected());

  }//GEN-LAST:event_autoPilotBtnActionPerformed

  private void startAllLocomotivesBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_startAllLocomotivesBtnActionPerformed
    Logger.trace(evt.getActionCommand() + " Start All Locomotives " + this.startAllLocomotivesBtn.isSelected());
    if (this.startAllLocomotivesBtn.isSelected()) {
      AutoPilot.startAllLocomotives();
    } else {
      AutoPilot.stopAllLocomotives();
    }
  }//GEN-LAST:event_startAllLocomotivesBtnActionPerformed

  private void crossingBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_crossingBtnActionPerformed
    setTileType(TileBean.TileType.CROSSING);
  }//GEN-LAST:event_crossingBtnActionPerformed

  private void resetAutopilotBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_resetAutopilotBtnActionPerformed
    AutoPilot.resetStates();
  }//GEN-LAST:event_resetAutopilotBtnActionPerformed

  private void setTileType(TileBean.TileType tileType) {
    this.canvas.setTileType(tileType);
  }

  private void setDirection(Direction direction) {
    this.canvas.setDirection(direction);
  }

  private void setMode(LayoutCanvas.Mode mode) {
    switch (mode) {
      case SELECT -> {
        selectBtn.setIcon(new ImageIcon(getClass().getResource("/media/cursor-24-y.png")));
        addBtn.setIcon(new ImageIcon(getClass().getResource("/media/add-24.png")));
        deleteBtn.setIcon(new ImageIcon(getClass().getResource("/media/delete-24.png")));
      }
      case ADD -> {
        addBtn.setIcon(new ImageIcon(getClass().getResource("/media/add-24-y.png")));
        selectBtn.setIcon(new ImageIcon(getClass().getResource("/media/cursor-24.png")));
        deleteBtn.setIcon(new ImageIcon(getClass().getResource("/media/delete-24.png")));
      }
      case DELETE -> {
        deleteBtn.setIcon(new ImageIcon(getClass().getResource("/media/delete-24-y.png")));
        selectBtn.setIcon(new ImageIcon(getClass().getResource("/media/cursor-24.png")));
        addBtn.setIcon(new ImageIcon(getClass().getResource("/media/add-24.png")));
      }
      default -> {
        selectBtn.setIcon(new ImageIcon(getClass().getResource("/media/cursor-24.png")));
        addBtn.setIcon(new ImageIcon(getClass().getResource("/media/add-24.png")));
        deleteBtn.setIcon(new ImageIcon(getClass().getResource("/media/delete-24.png")));
      }
    }

    this.canvas.setMode(mode);
  }

  private class Powerlistener implements PowerEventListener {

    private final LayoutPanel layoutPanel;

    Powerlistener(LayoutPanel layoutPanel) {
      this.layoutPanel = layoutPanel;
    }

    @Override
    public void onPowerChange(PowerEvent event) {
      Logger.info("Track Power is " + (event.isPower() ? "on" : "off"));

      if (!event.isPower() && autoPilotBtn.isSelected()) {
        autoPilotBtn.doClick();
      }
      autoPilotBtn.setEnabled(event.isPower());
    }
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JButton addBtn;
  private JToggleButton autoPilotBtn;
  private JToggleButton blockBtn;
  private LayoutCanvas canvas;
  private JScrollPane canvasScrollPane;
  private JToggleButton crossLBtn;
  private JToggleButton crossRBtn;
  private JToggleButton crossingBtn;
  private JToggleButton curvedBtn;
  private JPopupMenu curvedPopupMenu;
  private JButton deleteBtn;
  private JMenuItem deleteMI;
  private JToggleButton endTrackBtn;
  private Box.Filler filler1;
  private Box.Filler filler2;
  private Box.Filler filler3;
  private Box.Filler filler4;
  private JButton flipHorizontalBtn;
  private JMenuItem flipHorizontalMI;
  private JButton flipVerticalBtn;
  private JMenuItem flipVerticalMI;
  private JToggleButton gridBtn;
  private JMenuItem horizontalMI;
  private JMenuItem leftMI;
  private JToggleButton leftSwitchBtn;
  private JButton loadBtn;
  private JButton moveBtn;
  private JMenuItem moveMI;
  private JPopupMenu operationsPM;
  private JMenuItem propertiesMI;
  private JButton repaintBtn;
  private JButton resetAutopilotBtn;
  private JMenuItem rightMI;
  private JToggleButton rightSwitchBtn;
  private JButton rotateBtn;
  private JMenuItem rotateMI;
  private JButton routeBtn;
  private JButton saveBtn;
  private JButton selectBtn;
  private JToggleButton sensorBtn;
  private JToggleButton signalBtn;
  private JToggleButton startAllLocomotivesBtn;
  private JToggleButton straightBtn;
  private JToggleButton straightDirectionBtn;
  private JPopupMenu straightPopupMenu;
  private ButtonGroup tileBtnGroup;
  private JToolBar toolBar;
  private JPanel topPanel;
  private JMenuItem verticalMI;
  private JMenuItem xyMI;
  // End of variables declaration//GEN-END:variables
}
