/*
 * Copyright (C) 2019 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.ui.layout;

import java.awt.BorderLayout;
import jcs.entities.enums.TileType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.ui.layout.tiles.enums.Direction;
import jcs.ui.layout.enums.Mode;
import org.tinylog.Logger;

/**
 * This canvas / Panel is used to draw the layout
 *
 * @author frans
 */
public class LayoutPanel extends JPanel {

    public static final int GRID_SIZE = Tile.GRID;

    private Mode mode;

    private TileType tileType;

    //private final ExecutorService executor;
    /**
     * Creates new form GridsCanvas
     */
    public LayoutPanel() {
        //this.executor = Executors.newSingleThreadExecutor();
        this.mode = Mode.SELECT;
        this.tileType = TileType.STRAIGHT;
        initComponents();
        postInit();
    }

    private void postInit() {
        this.straightBtn.setSelected(true);
        this.canvas.setTileType(tileType);
        this.canvas.setMode(mode);
    }

    public void saveLayout() {
        this.canvas.saveLayout();
    }

    public void loadLayout() {
        this.canvas.loadLayout();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
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
        routeBtn = new JButton();
        filler1 = new Box.Filler(new Dimension(76, 0), new Dimension(76, 0), new Dimension(76, 32767));
        selectBtn = new JButton();
        addBtn = new JButton();
        deleteBtn = new JButton();
        repaintBtn = new JButton();
        filler3 = new Box.Filler(new Dimension(19, 0), new Dimension(19, 0), new Dimension(19, 32767));
        gridBtn = new JToggleButton();
        filler2 = new Box.Filler(new Dimension(38, 0), new Dimension(38, 0), new Dimension(38, 32767));
        straightBtn = new JToggleButton();
        curvedBtn = new JToggleButton();
        blockBtn = new JToggleButton();
        sensorBtn = new JToggleButton();
        signalBtn = new JToggleButton();
        leftSwitchBtn = new JToggleButton();
        rightSwitchBtn = new JToggleButton();
        crossLBtn = new JToggleButton();
        crossRBtn = new JToggleButton();
        filler4 = new Box.Filler(new Dimension(38, 0), new Dimension(38, 0), new Dimension(38, 32767));
        moveBtn = new JButton();
        flipVerticalBtn = new JButton();
        flipHorizontalBtn = new JButton();
        rotateBtn = new JButton();
        canvasScrollPane = new JScrollPane();
        canvas = new LayoutCanvas();

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

        setMinimumSize(new Dimension(1000, 160));
        setOpaque(false);
        setPreferredSize(new Dimension(1000, 775));
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        setLayout(new BorderLayout());

        topPanel.setMaximumSize(new Dimension(32767, 50));
        topPanel.setMinimumSize(new Dimension(1000, 50));
        topPanel.setPreferredSize(new Dimension(1000, 50));
        FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT);
        flowLayout1.setAlignOnBaseline(true);
        topPanel.setLayout(flowLayout1);

        toolBar.setDoubleBuffered(true);
        toolBar.setMargin(new Insets(1, 1, 1, 1));
        toolBar.setMaximumSize(new Dimension(1050, 42));
        toolBar.setMinimumSize(new Dimension(1000, 42));
        toolBar.setPreferredSize(new Dimension(1000, 42));

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

        routeBtn.setIcon(new ImageIcon(getClass().getResource("/media/route-24.png"))); // NOI18N
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
        toolBar.add(filler1);

        selectBtn.setIcon(new ImageIcon(getClass().getResource("/media/cursor-24.png"))); // NOI18N
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
        canvasScrollPane.setMinimumSize(new Dimension(110, 110));
        canvasScrollPane.setPreferredSize(new Dimension(1000, 700));
        canvasScrollPane.setViewportView(canvas);

        canvas.setMinimumSize(new Dimension(100, 100));
        canvas.setPreferredSize(new Dimension(895, 695));
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
        this.saveLayout();
    }//GEN-LAST:event_saveBtnActionPerformed

    private void loadBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_loadBtnActionPerformed
        this.loadLayout();
    }//GEN-LAST:event_loadBtnActionPerformed

    private void selectBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_selectBtnActionPerformed
        setMode(Mode.SELECT);
    }//GEN-LAST:event_selectBtnActionPerformed

    private void addBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        setMode(Mode.ADD);
    }//GEN-LAST:event_addBtnActionPerformed

    private void deleteBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
        setMode(Mode.DELETE);
        this.canvas.removeTiles();
    }//GEN-LAST:event_deleteBtnActionPerformed

    private void repaintBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_repaintBtnActionPerformed
        this.canvas.repaint();
    }//GEN-LAST:event_repaintBtnActionPerformed

    private void straightBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_straightBtnActionPerformed
        setTileType(TileType.STRAIGHT);
    }//GEN-LAST:event_straightBtnActionPerformed

    private void curvedBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_curvedBtnActionPerformed
        setTileType(TileType.CURVED);
    }//GEN-LAST:event_curvedBtnActionPerformed

    private void blockBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_blockBtnActionPerformed
        setTileType(TileType.BLOCK);
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
        setMode(Mode.MOVE);
    }//GEN-LAST:event_moveBtnActionPerformed

    private void rightSwitchBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rightSwitchBtnActionPerformed
        this.setTileType(TileType.SWITCH);
        this.setDirection(Direction.RIGHT);
    }//GEN-LAST:event_rightSwitchBtnActionPerformed

    private void leftSwitchBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_leftSwitchBtnActionPerformed
        this.setTileType(TileType.SWITCH);
        this.setDirection(Direction.LEFT);
    }//GEN-LAST:event_leftSwitchBtnActionPerformed

    private void signalBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_signalBtnActionPerformed
        setTileType(TileType.SIGNAL);
    }//GEN-LAST:event_signalBtnActionPerformed

    private void sensorBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sensorBtnActionPerformed
        setTileType(TileType.SENSOR);
    }//GEN-LAST:event_sensorBtnActionPerformed

    private void gridBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_gridBtnActionPerformed
        this.canvas.setDrawGrid(this.gridBtn.isSelected());
        this.canvas.repaint();
    }//GEN-LAST:event_gridBtnActionPerformed

    private void formComponentResized(ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        Logger.debug(evt.getComponent().getSize());// TODO add your handling code here:
    }//GEN-LAST:event_formComponentResized

    private void crossLBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_crossLBtnActionPerformed
        setTileType(TileType.CROSS);
        this.setDirection(Direction.LEFT);
    }//GEN-LAST:event_crossLBtnActionPerformed

    private void crossRBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_crossRBtnActionPerformed
        setTileType(TileType.CROSS);
        this.setDirection(Direction.RIGHT);
    }//GEN-LAST:event_crossRBtnActionPerformed

    private void routeBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_routeBtnActionPerformed
        Logger.debug("Start Routing...");

        this.canvas.routeLayout();

    }//GEN-LAST:event_routeBtnActionPerformed

    private void setTileType(TileType tileType) {
        this.tileType = tileType;
        this.canvas.setTileType(tileType);
    }

    private void setDirection(Direction direction) {
        //this.direction = direction;
        this.canvas.setDirection(direction);
    }

    private void setMode(Mode mode) {
        this.mode = mode;
        this.canvas.setMode(mode);

//        switch (this.mode) {
//            case ADD:
//                straightBtn.setBorder(new LineBorder(new Color(153, 153, 153), 1, true));
//                this.curvedBtn.setBorder(new LineBorder(new Color(153, 153, 153), 1, true));
//                this.blockBtn.setBorder(new LineBorder(new Color(153, 153, 153), 1, true));
//                this.sensorBtn.setBorder(new LineBorder(new Color(153, 153, 153), 1, true));
//                this.leftSwitchBtn.setBorder(new LineBorder(new Color(153, 153, 153), 1, true));
//                this.rightSwitchBtn.setBorder(new LineBorder(new Color(153, 153, 153), 1, true));
//                this.signalBtn.setBorder(new LineBorder(new Color(153, 153, 153), 1, true));
//                this.crossLBtn.setBorder(new LineBorder(new Color(153, 153, 153), 1, true));
//                this.crossRBtn.setBorder(new LineBorder(new Color(153, 153, 153), 1, true));
//                break;
//            default:
//                this.straightBtn.setBorder(null);
//                this.curvedBtn.setBorder(null);
//                this.blockBtn.setBorder(null);
//                this.sensorBtn.setBorder(null);
//                this.leftSwitchBtn.setBorder(null);
//                this.rightSwitchBtn.setBorder(null);
//                this.signalBtn.setBorder(null);
//                this.crossLBtn.setBorder(null);
//                this.crossRBtn.setBorder(null);
//                break;
//        }
    }

    public static void main(String args[]) {
        System.setProperty("trackServiceAlwaysUseDemo", "true");
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");

            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.error(ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            JFrame f = new JFrame("LayoutCanvas Tester");
            LayoutPanel layoutPanel = new LayoutPanel();
            f.add(layoutPanel);

            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            f.pack();

            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            f.setLocation(dim.width / 2 - f.getSize().width / 2, dim.height / 2 - f.getSize().height / 2);

            f.setVisible(true);
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton addBtn;
    private JToggleButton blockBtn;
    private LayoutCanvas canvas;
    private JScrollPane canvasScrollPane;
    private JToggleButton crossLBtn;
    private JToggleButton crossRBtn;
    private JToggleButton curvedBtn;
    private JPopupMenu curvedPopupMenu;
    private JButton deleteBtn;
    private JMenuItem deleteMI;
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
    private JMenuItem rightMI;
    private JToggleButton rightSwitchBtn;
    private JButton rotateBtn;
    private JMenuItem rotateMI;
    private JButton routeBtn;
    private JButton saveBtn;
    private JButton selectBtn;
    private JToggleButton sensorBtn;
    private JToggleButton signalBtn;
    private JToggleButton straightBtn;
    private JPopupMenu straightPopupMenu;
    private ButtonGroup tileBtnGroup;
    private JToolBar toolBar;
    private JPanel topPanel;
    private JMenuItem verticalMI;
    private JMenuItem xyMI;
    // End of variables declaration//GEN-END:variables
}