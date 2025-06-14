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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.entities.BlockBean;
import jcs.entities.BlockBean.BlockState;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Direction;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.SOUTH;
import jcs.entities.TileBean.TileType;
import static jcs.entities.TileBean.TileType.BLOCK;
import static jcs.entities.TileBean.TileType.CROSS;
import static jcs.entities.TileBean.TileType.CURVED;
import static jcs.entities.TileBean.TileType.END;
import static jcs.entities.TileBean.TileType.SENSOR;
import static jcs.entities.TileBean.TileType.SIGNAL;
import static jcs.entities.TileBean.TileType.STRAIGHT;
import static jcs.entities.TileBean.TileType.STRAIGHT_DIR;
import static jcs.entities.TileBean.TileType.SWITCH;
import jcs.persistence.PersistenceFactory;
import static jcs.ui.layout.LayoutCanvas.Mode.CONTROL;
import jcs.ui.layout.dialogs.BlockControlDialog;
import jcs.ui.layout.dialogs.BlockDialog;
import jcs.ui.layout.dialogs.SensorDialog;
import jcs.ui.layout.dialogs.SignalDialog;
import jcs.ui.layout.dialogs.SwitchDialog;
import jcs.ui.layout.pathfinding.astar.AStar;
import jcs.ui.layout.tiles.Block;
import jcs.ui.layout.tiles.Sensor;
import jcs.ui.layout.tiles.Signal;
import jcs.ui.layout.tiles.Switch;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import org.tinylog.Logger;

/**
 * This canvas / Panel is used to draw the layout
 *
 */
public class LayoutCanvas extends JPanel {

  private static final long serialVersionUID = 9075914241802892566L;

  public enum Mode {
    SELECT,
    ADD,
    EDIT,
    MOVE,
    DELETE,
    CONTROL
  }

  static final int LINE_GRID = 1;

  private int gridType = LINE_GRID;

  private boolean readonly;
  private Mode mode;
  private boolean drawGrid = true;

  private Orientation orientation;
  private Direction direction;
  private TileType tileType;

  private Point mouseLocation = new Point();

  private final ExecutorService executor;

  private Tile selectedTile;

  private RoutesDialog routesDialog;

  private boolean showCenter;

  public LayoutCanvas() {
    this(false);
  }

  public LayoutCanvas(boolean readonly) {
    super();
    setLayout(null);
    setOpaque(true);
    setDoubleBuffered(true);

    showCenter = "true".equalsIgnoreCase(System.getProperty("tile.show.center", "false"));

    this.readonly = readonly;
    drawGrid = !readonly;
    this.executor = Executors.newCachedThreadPool();

    this.mode = Mode.SELECT;
    this.orientation = Orientation.EAST;
    this.direction = Direction.CENTER;

    initComponents();
    postInit();
  }

  private void postInit() {
    routesDialog = new RoutesDialog(getParentFrame(), false, this, this.readonly);
  }

  public boolean isReadonly() {
    return readonly;
  }

  @Override
  public void paint(Graphics g) {
    //long started = System.currentTimeMillis();
    super.paint(g);

    if (drawGrid) {
      switch (gridType) {
        case 1 ->
          paintLineGrid(g);
        case 2 ->
          paintDotGrid(g);
        //default -> no grid
      }
    }

    //long now = System.currentTimeMillis();
    //Logger.trace("Duration: " + (now - started) + " ms.");
  }

  @Override
  public Component add(Component component) {
    super.add(component);
    if (component instanceof Tile tile) {
      tile.setBounds(tile.getTileBounds());
    }
    return component;
  }

  @Override
  public Component add(String name, Component component) {
    if (component instanceof Tile tile) {
      super.add(tile.getId(), tile);
      tile.setBounds(tile.getTileBounds());
    } else {
      super.add(component);
    }
    return component;
  }

  private void paintDotGrid(Graphics g) {
    int width = getWidth();
    int height = getHeight();
    Graphics2D gc = (Graphics2D) g;
    Paint p = gc.getPaint();
    gc.setPaint(Color.black);

    int xOffset = 0;
    int yOffset = 0;
    for (int r = 0; r < width; r++) {
      for (int c = 0; c < height; c++) {
        gc.drawOval((r * 20 * 2) + xOffset - 2, (c * 20 * 2) + yOffset - 2, 4, 4);
      }
    }
    gc.setPaint(p);
  }

  private void paintLineGrid(Graphics g) {
    int width = getWidth();
    int height = getHeight();
    Graphics2D gc = (Graphics2D) g;
    Paint p = gc.getPaint();
    gc.setPaint(Color.black);
    gc.setPaint(Color.lightGray);

    gc.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    for (int x = 0; x < width; x += 40) {
      gc.drawLine(x, 0, x, height);
    }
    for (int y = 0; y < height; y += 40) {
      gc.drawLine(0, y, width, y);
    }
    gc.setPaint(p);
  }

  void setMode(LayoutCanvas.Mode mode) {
    this.mode = mode;
    Logger.trace("Mode: " + mode);
  }

  void setGridType(int gridType) {
    this.gridType = gridType;
    executor.execute((() -> repaint()));
  }

  void setTileType(TileBean.TileType tileType) {
    this.tileType = tileType;
    Logger.trace("TileType: " + tileType + " Current mode: " + mode);
  }

  void setDirection(Direction direction) {
    this.direction = direction;
  }

  void loadLayoutInBackground() {
    executor.execute(() -> {
      List<Tile> tiles = TileCache.loadTiles(readonly);

      java.awt.EventQueue.invokeLater(() -> {
        loadTiles(tiles);
      });
    });
  }

  private void loadTiles(List<Tile> tiles) {
    removeAll();
    selectedTile = null;

    Dimension minSize = TileCache.getMinCanvasSize();
    setMinimumSize(minSize);

    //Check if we must enlarge the canvas
    int w = getPreferredSize().width;
    int h = getPreferredSize().height;
    boolean changeSize = false;
    if (w < minSize.width) {
      w = minSize.width;
      changeSize = true;
    }
    if (h < minSize.height) {
      h = minSize.height;
      changeSize = true;
    }

    if (changeSize) {
      setPreferredSize(new Dimension(w, h));
      setSize(new Dimension(w, h));
      Logger.trace("Changed size to w: " + w + " h: " + h);
    }

    for (Tile tile : tiles) {
      add(tile);
      if (showCenter) {
        tile.setDrawCenterPoint(showCenter);
      }
    }
  }

  private void mouseMoveAction(MouseEvent evt) {
    //Point sp = LayoutUtil.snapToGrid(evt.getPoint());
    if (selectedTile != null) {
      setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    } else {
      setCursor(Cursor.getDefaultCursor());
    }
  }

  private void mousePressedAction(MouseEvent evt) {
    Logger.trace("@ (" + evt.getX() + "," + evt.getY() + ")");
    Point snapPoint = LayoutUtil.snapToGrid(evt.getPoint());
    //Clear any previous selection
    Tile previousSelected = selectedTile;
    selectedTile = TileCache.findTile(snapPoint);
    //Only show selected tile in edit mode
    if (selectedTile != null && CONTROL != mode) {
      selectedTile.setSelected(true);
    }

    if (previousSelected != null && selectedTile != null && previousSelected.getId().equals(selectedTile.getId())) {
      Logger.trace("Same tile " + selectedTile.getId() + " selected");
    } else if (previousSelected != null) {
      previousSelected.setSelected(false);
    }

    switch (mode) {
      case CONTROL -> {
        if (selectedTile != null) {
          if (evt.getButton() == MouseEvent.BUTTON1) {
            executeControlActionForTile(selectedTile, snapPoint);
          } else {
            if (selectedTile.isBlock()) {
              showBlockPopupMenu(selectedTile, snapPoint);
            }
          }
        }
      }
      case ADD -> {
        if (MouseEvent.BUTTON1 == evt.getButton() && selectedTile == null) {
          //Only add a new tile when there is no tile on the selected snapPoint
          Logger.trace("Adding a new tile: " + tileType + " @ (" + snapPoint.x + ", " + snapPoint.y + ")");
          selectedTile = addTile(snapPoint, tileType, orientation, direction, true, showCenter);
          if (selectedTile != null) {
            selectedTile.setSelected(true);
            repaint(selectedTile.getTileBounds());
          }
        } else {
          if (selectedTile != null) {
            Logger.debug("A tile exists at the selected position: " + selectedTile.getTileType() + " @ (" + snapPoint.x + ", " + snapPoint.y + ") id: " + selectedTile.getId());
          } else {
            Logger.warn("Found something (" + snapPoint.x + ", " + snapPoint.y + ")");
          }
        }
        if (MouseEvent.BUTTON3 == evt.getButton() && selectedTile != null) {
          showOperationsPopupMenu(selectedTile, snapPoint);
        }
      }
      case DELETE -> {
        Component c = getComponentAt(snapPoint);
        if (c != null && c instanceof Tile) {
          Tile toBeDeleted = (Tile) c;
          removeTile(toBeDeleted);
          repaint(toBeDeleted.getTileBounds());
          selectedTile = null;
        }
      }
      default -> {
        Logger.trace((selectedTile != null ? "Selected tile: " + selectedTile.getId() + ", " + selectedTile.xyToString() : "No tile @ (" + snapPoint.x + "," + snapPoint.y + ")"));
        if (MouseEvent.BUTTON3 == evt.getButton()) {
          showOperationsPopupMenu(selectedTile, snapPoint);
        }
      }
    }
  }

  private Tile addTile(Point p, TileType tileType, Orientation orientation, Direction direction, boolean selected, boolean showCenter) {
    Logger.trace("Adding: " + tileType + " @ " + p + " O: " + orientation + " D: " + direction);
    Tile tile = TileCache.createTile(tileType, orientation, direction, p);

    if (TileCache.canMoveTo(tile, p)) {
      tile.setSelected(selected);
      tile.setDrawCenterPoint(showCenter);
      add(tile);
      TileCache.addAndSaveTile(tile);
      return tile;
    } else {
      Tile occ = TileCache.findTile(p);
      Logger.trace("Can't add tile " + tile.getId() + " on " + tile.xyToString() + " Is occupied by " + occ.getId());
      TileCache.rollback(tile);
      return null;
    }
  }

  void deleteSelectedTile() {
    Logger.trace("Selected Tile " + selectedTile.getId());
    removeTile(selectedTile);
    selectedTile = null;
  }

  void removeTile(Tile tile) {
    Tile toBeDeleted = (Tile) getComponentAt(tile.getCenter());
    if (toBeDeleted != null) {
      Logger.trace("Deleting Tile " + tile.getId());
      remove(toBeDeleted);
      TileCache.deleteTile(tile);
    }
  }

  private void mouseDragAction(MouseEvent evt) {
    //Logger.trace("@ (" + evt.getX() + "," + evt.getY() + ")");
    Point snapPoint = LayoutUtil.snapToGrid(evt.getPoint());
    if (selectedTile != null) {
      int z = getComponentZOrder(selectedTile);
      setComponentZOrder(selectedTile, 0);
      Logger.trace("Moving: " + selectedTile.getId() + " @ " + selectedTile.xyToString() + " P: " + snapPoint.x + "," + snapPoint.y + ")");

      if (TileCache.canMoveTo(selectedTile, snapPoint)) {
        selectedTile.setSelectedColor(Tile.DEFAULT_SELECTED_COLOR);
      } else {
        selectedTile.setSelectedColor(Tile.DEFAULT_WARN_COLOR);
      }

      int curX, curY;
      switch (selectedTile.getTileType()) {
        case BLOCK -> {
          if (selectedTile.isHorizontal()) {
            curX = snapPoint.x - Tile.GRID - Tile.GRID * 2;
            curY = snapPoint.y - Tile.GRID;
          } else {
            curX = snapPoint.x - Tile.GRID;
            curY = snapPoint.y - Tile.GRID - Tile.GRID * 2;
          }
        }
        case CROSS -> {
          switch (selectedTile.getOrientation()) {
            case SOUTH -> {
              curX = snapPoint.x - Tile.GRID;
              curY = snapPoint.y - Tile.GRID;
            }
            case WEST -> {
              curX = snapPoint.x - Tile.GRID - Tile.GRID * 2;
              curY = snapPoint.y - Tile.GRID;
            }
            case NORTH -> {
              curX = snapPoint.x - Tile.GRID;
              curY = snapPoint.y - Tile.GRID - Tile.GRID * 2;
            }
            default -> {
              //East
              curX = snapPoint.x - Tile.GRID;
              curY = snapPoint.y - Tile.GRID;
            }
          }
        }
        default -> {
          curX = snapPoint.x - Tile.GRID;
          curY = snapPoint.y - Tile.GRID;
        }
      }
      selectedTile.setBounds(curX, curY, selectedTile.getWidth(), selectedTile.getHeight());
    }
  }

  private void mouseReleasedAction(MouseEvent evt) {
    Point snapPoint = LayoutUtil.snapToGrid(evt.getPoint());
    if (!Mode.CONTROL.equals(mode) && MouseEvent.BUTTON1 == evt.getButton() && selectedTile != null) {
      if (TileCache.canMoveTo(selectedTile, snapPoint)) {
        TileCache.moveTo(selectedTile, snapPoint);
      } else {
        selectedTile.setSelectedColor(Tile.DEFAULT_SELECTED_COLOR);
        selectedTile.setBounds(selectedTile.getTileBounds());
      }
    }
  }

  private void executeControlActionForTile(Tile tile, Point p) {
    TileBean.TileType tt = tile.getTileType();
    switch (tt) {
      case STRAIGHT -> {
      }
      case CURVED -> {
      }
      case SENSOR -> {
        //this.executor.execute(() -> toggleSensor((Sensor) tile));
      }
      case BLOCK -> {
        Logger.trace("Show BlockDialog for " + tile.getId());
        //show the Block control dialog so tha a locomotive can be assigned to the block
        Block block = (Block) tile;
        BlockControlDialog bcd = new BlockControlDialog(getParentFrame(), block);
        bcd.setVisible(true);

        Logger.trace("Block properties closed");
        repaint(block.getTileBounds());
      }
      case SIGNAL -> {
        //this.executor.execute(() -> toggleSignal((Signal) tile));
      }
      case SWITCH -> {
        //this.executor.execute(() -> toggleSwitch((Switch) tile));
      }
      case CROSS -> {
        //this.executor.execute(() -> toggleSwitch((Switch) tile));
      }
      default -> {
      }
    }
  }

  private void editSelectedTileProperties() {
    //the first tile should be the selected one
    boolean showProperties = false;
    boolean showFlip = false;
    boolean showRotate = false;
    boolean showMove = false;
    boolean showDelete = false;

    if (selectedTile != null) {
      TileBean.TileType tt = selectedTile.getTileType();
      Logger.trace("Selected tile " + selectedTile.getId() + " TileType " + tt);

      switch (tt) {
        case END -> {
          showRotate = true;
          showDelete = true;
        }
        case STRAIGHT_DIR -> {
          showRotate = true;
          showDelete = true;
        }
        case STRAIGHT -> {
          showRotate = true;
          showDelete = true;
        }
        case CURVED -> {
          showRotate = true;
          showDelete = true;
        }
        case SENSOR -> {
          SensorDialog fbd = new SensorDialog(getParentFrame(), (Sensor) selectedTile);
          fbd.setVisible(true);
        }
        case SIGNAL -> {
          SignalDialog sd = new SignalDialog(getParentFrame(), (Signal) selectedTile);
          sd.setVisible(true);
        }
        case SWITCH -> {
          SwitchDialog td = new SwitchDialog(getParentFrame(), (Switch) selectedTile);
          td.setVisible(true);
        }
        case CROSS -> {
          SwitchDialog td = new SwitchDialog(getParentFrame(), (Switch) selectedTile);
          td.setVisible(true);
        }
        case BLOCK -> {
          Logger.trace("Show BlockDialog for " + selectedTile.getId());
          BlockDialog bd = new BlockDialog(getParentFrame(), (Block) selectedTile, this);
          bd.setVisible(true);
        }
        default -> {
        }
      }
      //TODO: only repaint the edited tile?
      repaint();
    }
  }

  private void showBlockPopupMenu(Tile tile, Point p) {
    if (tile == null || p == null) {
      return;
    }
    //Check if automode is on etc
    boolean autoPilotEnabled = AutoPilot.isAutoModeActive();
    boolean hasLoco = ((Block) tile).getBlockBean().getLocomotive() != null;
    boolean isGhost = ((Block) tile).getBlockBean().getBlockState() == BlockState.GHOST;
    this.startLocomotiveMI.setEnabled(autoPilotEnabled && hasLoco);
    this.stopLocomotiveMI.setEnabled(autoPilotEnabled && hasLoco);
    this.resetDispatcherMI.setEnabled(autoPilotEnabled && hasLoco);
    this.removeLocMI.setEnabled(hasLoco);
    this.toggleLocomotiveDirectionMI.setEnabled(hasLoco);
    this.reverseArrivalSideMI.setEnabled(hasLoco);
    this.resetGhostMI.setEnabled(isGhost);

    this.toggleOutOfOrderMI.setEnabled(!hasLoco);

    if (BlockBean.BlockState.OUT_OF_ORDER == ((Block) tile).getBlockState()) {
      this.toggleOutOfOrderMI.setText("Enable Block");
    } else {
      this.toggleOutOfOrderMI.setText("Set Out of Order");
    }

    this.blockPopupMenu.show(this, p.x, p.y);
  }

  private void showOperationsPopupMenu(Tile tile, Point p) {
    if (tile == null || p == null) {
      return;
    }

    //which items should be shown
    boolean showProperties = false;
    boolean showFlip = false;
    @SuppressWarnings("UnusedAssignment")
    boolean showRotate = false;
    boolean showMove = false;
    @SuppressWarnings("UnusedAssignment")
    boolean showDelete = false;

    TileType tt = tile.getTileType();
    switch (tt) {
      case SENSOR -> {
        showProperties = true;
        showRotate = true;
        showDelete = true;
      }
      case BLOCK -> {
        showProperties = true;
        showRotate = true;
        showDelete = true;
      }
      case SIGNAL -> {
        showProperties = true;
        showRotate = true;
        showDelete = true;
      }
      case SWITCH -> {
        showProperties = true;
        showFlip = true;
        showRotate = true;
        showDelete = true;
      }
      case CROSS -> {
        showProperties = true;
        showFlip = true;
        showRotate = true;
        showDelete = true;
      }
      default -> {
        showRotate = true;
        showDelete = true;
      }
    }
    this.xyMI.setVisible(true);

    String extra = "";
    if (tile instanceof Sensor s) {
      if (s.getSensorBean() != null) {
        extra = " " + s.getSensorBean().getName();
      }
    }

    this.xyMI.setText(tile.getId() + extra + " (" + p.x + "," + p.y + ") O: " + tile.getOrientation().getOrientation() + " D: " + tile.getDirection());

    this.propertiesMI.setVisible(showProperties);
    this.flipHorizontalMI.setVisible(showFlip);
    this.flipVerticalMI.setVisible(showFlip);
    this.rotateMI.setVisible(showRotate);
    this.moveMI.setVisible(showMove);
    this.deleteMI.setVisible(showDelete);
    this.operationsPM.show(this, p.x, p.y);
  }

  private JFrame getParentFrame() {
    JFrame frame = (JFrame) SwingUtilities.getRoot(this);
    return frame;
  }

  public void rotateSelectedTile() {
    Logger.trace("Selected Tile " + selectedTile.getId());
    selectedTile = TileCache.rotateTile(selectedTile);
    selectedTile.setBounds(selectedTile.getTileBounds());
  }

  public void flipSelectedTileHorizontal() {
    selectedTile = TileCache.flipHorizontal(selectedTile);
    selectedTile.setBounds(selectedTile.getTileBounds());
  }

  public void flipSelectedTileVertical() {
    selectedTile = TileCache.flipVertical(selectedTile);
    selectedTile.setBounds(selectedTile.getTileBounds());
  }

  void routeLayout() {
    this.executor.execute(() -> routeLayoutWithAStar());
  }

  private void routeLayoutWithAStar() {
    //Make sure the layout is saved
    TileCache.persistAllTiles();

    AStar astar = new AStar();
    astar.buildGraph(TileCache.getTiles());
    astar.routeAll();
    astar.persistRoutes();
    if (routesDialog.isVisible()) {
      routesDialog.loadRoutes();
    }
  }

  void showRoutesDialog() {
    routesDialog.setVisible(true);
  }

  /**
   * This method is called from within the constructor to initialize the form.<br>
   * WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
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
    blockPopupMenu = new JPopupMenu();
    toggleOutOfOrderMI = new JMenuItem();
    resetGhostMI = new JMenuItem();
    startLocomotiveMI = new JMenuItem();
    stopLocomotiveMI = new JMenuItem();
    resetDispatcherMI = new JMenuItem();
    reverseArrivalSideMI = new JMenuItem();
    toggleLocomotiveDirectionMI = new JMenuItem();
    removeLocMI = new JMenuItem();
    blockPropertiesMI = new JMenuItem();

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

    toggleOutOfOrderMI.setText("Set Out of Order");
    toggleOutOfOrderMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        toggleOutOfOrderMIActionPerformed(evt);
      }
    });
    blockPopupMenu.add(toggleOutOfOrderMI);

    resetGhostMI.setText("Reset Ghost");
    resetGhostMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        resetGhostMIActionPerformed(evt);
      }
    });
    blockPopupMenu.add(resetGhostMI);

    startLocomotiveMI.setText("Start Locomotive");
    startLocomotiveMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        startLocomotiveMIActionPerformed(evt);
      }
    });
    blockPopupMenu.add(startLocomotiveMI);

    stopLocomotiveMI.setText("Stop Locomotive");
    stopLocomotiveMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        stopLocomotiveMIActionPerformed(evt);
      }
    });
    blockPopupMenu.add(stopLocomotiveMI);

    resetDispatcherMI.setText("Reset Dispatcher");
    resetDispatcherMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        resetDispatcherMIActionPerformed(evt);
      }
    });
    blockPopupMenu.add(resetDispatcherMI);

    reverseArrivalSideMI.setText("Reverse Arrival Side");
    reverseArrivalSideMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        reverseArrivalSideMIActionPerformed(evt);
      }
    });
    blockPopupMenu.add(reverseArrivalSideMI);

    toggleLocomotiveDirectionMI.setText("Toggle Direction");
    toggleLocomotiveDirectionMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        toggleLocomotiveDirectionMIActionPerformed(evt);
      }
    });
    blockPopupMenu.add(toggleLocomotiveDirectionMI);

    removeLocMI.setText("Remove Locomotive");
    removeLocMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        removeLocMIActionPerformed(evt);
      }
    });
    blockPopupMenu.add(removeLocMI);

    blockPropertiesMI.setText("Properties");
    blockPropertiesMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        blockPropertiesMIActionPerformed(evt);
      }
    });
    blockPopupMenu.add(blockPropertiesMI);

    setBackground(new Color(255, 255, 255));
    setMinimumSize(new Dimension(1000, 760));
    setPreferredSize(new Dimension(1000, 760));
    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent evt) {
        formMouseDragged(evt);
      }
      public void mouseMoved(MouseEvent evt) {
        formMouseMoved(evt);
      }
    });
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent evt) {
        formMousePressed(evt);
      }
      public void mouseReleased(MouseEvent evt) {
        formMouseReleased(evt);
      }
    });
    setLayout(null);
    getAccessibleContext().setAccessibleParent(this);
  }// </editor-fold>//GEN-END:initComponents

  private void formMousePressed(MouseEvent evt) {//GEN-FIRST:event_formMousePressed
    mousePressedAction(evt);
  }//GEN-LAST:event_formMousePressed

  private void formMouseReleased(MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
    mouseReleasedAction(evt);
  }//GEN-LAST:event_formMouseReleased

  private void horizontalMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_horizontalMIActionPerformed
    Logger.trace(this.orientation + ", " + evt.getModifiers() + ", " + evt.paramString());

    if (this.mouseLocation != null && evt.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
      this.mouseLocation = null;
    }
  }//GEN-LAST:event_horizontalMIActionPerformed

  private void verticalMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_verticalMIActionPerformed
    Logger.trace(this.orientation + ", " + evt.getModifiers() + ", " + evt.paramString());

    if (this.mouseLocation != null && evt.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
      this.mouseLocation = null;
    }
  }//GEN-LAST:event_verticalMIActionPerformed

  private void rightMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rightMIActionPerformed
    Logger.trace(this.orientation + ", " + evt.getModifiers() + ", " + evt.paramString());

    if (this.mouseLocation != null && evt.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
      this.mouseLocation = null;
    }
  }//GEN-LAST:event_rightMIActionPerformed

  private void leftMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_leftMIActionPerformed
    Logger.trace(this.orientation + ", " + evt.getModifiers() + ", " + evt.paramString());

    if (this.mouseLocation != null && evt.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
      this.mouseLocation = null;
    }
  }//GEN-LAST:event_leftMIActionPerformed

  private void formMouseMoved(MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
    mouseMoveAction(evt);
  }//GEN-LAST:event_formMouseMoved

  private void rotateMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rotateMIActionPerformed
    rotateSelectedTile();
  }//GEN-LAST:event_rotateMIActionPerformed

  private void flipHorizontalMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_flipHorizontalMIActionPerformed
    flipSelectedTileHorizontal();
  }//GEN-LAST:event_flipHorizontalMIActionPerformed

  private void flipVerticalMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_flipVerticalMIActionPerformed
    flipSelectedTileVertical();
  }//GEN-LAST:event_flipVerticalMIActionPerformed

  private void moveMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_moveMIActionPerformed
    this.mode = Mode.MOVE;
  }//GEN-LAST:event_moveMIActionPerformed

  private void deleteMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_deleteMIActionPerformed
    if (selectedTile != null) {
      removeTile(selectedTile);
      repaint(selectedTile.getTileBounds());
      selectedTile = null;
    }
  }//GEN-LAST:event_deleteMIActionPerformed

  private void propertiesMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_propertiesMIActionPerformed
    editSelectedTileProperties();
  }//GEN-LAST:event_propertiesMIActionPerformed

  private void formMouseDragged(MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
    mouseDragAction(evt);
  }//GEN-LAST:event_formMouseDragged

  private void startLocomotiveMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_startLocomotiveMIActionPerformed
    if (selectedTile != null && selectedTile.isBlock() && selectedTile.getLocomotive() != null) {
      LocomotiveBean locomotive = selectedTile.getLocomotive();
      //executor.execute(() -> AutoPilot.startStopLocomotive(locomotive, true));
      AutoPilot.startLocomotive(locomotive);
    }
  }//GEN-LAST:event_startLocomotiveMIActionPerformed

  private void stopLocomotiveMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_stopLocomotiveMIActionPerformed
    if (selectedTile != null && selectedTile.isBlock() && selectedTile.getLocomotive() != null) {
      LocomotiveBean locomotive = selectedTile.getLocomotive();
      //executor.execute(() -> AutoPilot.startStopLocomotive(locomotive, false));
      AutoPilot.stopLocomotive(locomotive);
    }
  }//GEN-LAST:event_stopLocomotiveMIActionPerformed

  private void resetDispatcherMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_resetDispatcherMIActionPerformed
    if (this.selectedTile != null) {
      Block block = (Block) selectedTile;
      LocomotiveBean locomotive = block.getBlockBean().getLocomotive();

      executor.execute(() -> {
        AutoPilot.resetDispatcher(locomotive);

        repaint();
      });
    }
  }//GEN-LAST:event_resetDispatcherMIActionPerformed

  private void removeLocMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_removeLocMIActionPerformed
    if (selectedTile != null && selectedTile.isBlock()) {
      LocomotiveBean locomotive = selectedTile.getLocomotive();
      locomotive.setDispatcherDirection(null);

      selectedTile.setLocomotive(null);

      executor.execute(() -> {
        PersistenceFactory.getService().persist(selectedTile.getBlockBean());
        PersistenceFactory.getService().persist(locomotive);

        AutoPilot.removeLocomotive(locomotive);
      });
    }
  }//GEN-LAST:event_removeLocMIActionPerformed

  private void blockPropertiesMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_blockPropertiesMIActionPerformed
    if (selectedTile != null && selectedTile.isBlock()) {
      //show the Block control dialog so tha a locomotive can be assigned to the block
      BlockControlDialog bcd = new BlockControlDialog(getParentFrame(), (Block) selectedTile);
      bcd.setVisible(true);

      repaint(selectedTile.getTileBounds());
    }
  }//GEN-LAST:event_blockPropertiesMIActionPerformed

  private void reverseArrivalSideMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_reverseArrivalSideMIActionPerformed
    if (this.selectedTile != null) {
      Block block = (Block) selectedTile;

      String suffix = block.getArrivalSuffix();
      if ("+".equals(suffix)) {
        block.setArrivalSuffix("-");
      } else {
        block.setArrivalSuffix("+");
      }
      block.setReverseArrival(!block.isReverseArrival());
      this.executor.execute(() -> {
        PersistenceFactory.getService().persist(block.getBlockBean());
      });
    }
  }//GEN-LAST:event_reverseArrivalSideMIActionPerformed

  private void toggleLocomotiveDirectionMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_toggleLocomotiveDirectionMIActionPerformed
    if (selectedTile != null) {
      Block block = (Block) selectedTile;
      LocomotiveBean locomotive = block.getLocomotive();

      LocomotiveBean.Direction curDir;
      if (block.getLogicalDirection() != null) {
        curDir = block.getLogicalDirection();
      } else {
        curDir = locomotive.getDirection();
      }
      LocomotiveBean.Direction newDir = LocomotiveBean.toggle(curDir);
      block.setLogicalDirection(newDir);
      Logger.trace(block.getId() + " LogicalDir changed from " + curDir + " to " + newDir + " for " + locomotive.getName());

      this.executor.execute(() -> {
        PersistenceFactory.getService().persist(block.getTileBean());
      });
    }
  }//GEN-LAST:event_toggleLocomotiveDirectionMIActionPerformed

  private void toggleOutOfOrderMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_toggleOutOfOrderMIActionPerformed
    if (this.selectedTile != null) {
      Block block = (Block) selectedTile;
      BlockState currentState = block.getBlockState();
      if (BlockState.FREE == currentState) {
        block.setBlockState(BlockState.OUT_OF_ORDER);
      } else if (BlockState.OUT_OF_ORDER == currentState) {
        block.setBlockState(BlockState.FREE);
      }

      if (currentState != block.getBlockState()) {
        this.executor.execute(() -> {
          PersistenceFactory.getService().persist(block.getBlockBean());
        });
      }
    }
  }//GEN-LAST:event_toggleOutOfOrderMIActionPerformed

  private void resetGhostMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_resetGhostMIActionPerformed
    if (this.selectedTile != null) {
      Block block = (Block) selectedTile;
      BlockBean.BlockState currentState = block.getBlockState();

      if (BlockBean.BlockState.GHOST == currentState) {
        if (block.getLocomotive() != null) {
          block.setBlockState(BlockBean.BlockState.OCCUPIED);
        } else {
          block.setBlockState(BlockBean.BlockState.FREE);
        }
        this.executor.execute(() -> {
          PersistenceFactory.getService().persist(block.getBlockBean());
        });
      }
    }
  }//GEN-LAST:event_resetGhostMIActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JPopupMenu blockPopupMenu;
  private JMenuItem blockPropertiesMI;
  private JPopupMenu curvedPopupMenu;
  private JMenuItem deleteMI;
  private JMenuItem flipHorizontalMI;
  private JMenuItem flipVerticalMI;
  private JMenuItem horizontalMI;
  private JMenuItem leftMI;
  private JMenuItem moveMI;
  private JPopupMenu operationsPM;
  private JMenuItem propertiesMI;
  private JMenuItem removeLocMI;
  private JMenuItem resetDispatcherMI;
  private JMenuItem resetGhostMI;
  private JMenuItem reverseArrivalSideMI;
  private JMenuItem rightMI;
  private JMenuItem rotateMI;
  private JMenuItem startLocomotiveMI;
  private JMenuItem stopLocomotiveMI;
  private JPopupMenu straightPopupMenu;
  private JMenuItem toggleLocomotiveDirectionMI;
  private JMenuItem toggleOutOfOrderMI;
  private JMenuItem verticalMI;
  private JMenuItem xyMI;
  // End of variables declaration//GEN-END:variables
}
