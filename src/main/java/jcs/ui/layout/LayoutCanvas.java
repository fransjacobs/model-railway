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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import jcs.JCS;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Direction;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.persistence.PersistenceFactory;
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
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 * This canvas / Panel is used to draw the layout
 *
 * @author frans
 */
public class LayoutCanvas extends JPanel implements PropertyChangeListener {

  public enum Mode {
    SELECT,
    ADD,
    EDIT,
    MOVE,
    DELETE,
    CONTROL
  }

  private boolean readonly;
  private Mode mode;
  private boolean drawGrid = true;
  private boolean lineGrid = true;

  private Orientation orientation;
  private Direction direction;
  private TileBean.TileType tileType;

  private Point mouseLocation = new Point(0, 0);

  private BufferedImage grid;

  private final ExecutorService executor;

  private final Map<Point, Tile> tiles;
  private final Map<Point, Tile> altTiles;
  private final Set<Point> selectedTiles;

  private RoutesDialog routesDialog;
  private final Map<String, RouteElementBean> selectedRouteElements;

  public LayoutCanvas() {
    this(false);
  }

  public LayoutCanvas(boolean readonly) {
    this.readonly = readonly;
    this.tiles = new HashMap<>();
    this.altTiles = new HashMap<>();

    this.selectedTiles = new HashSet<>();
    this.selectedRouteElements = new HashMap<>();

    this.executor = Executors.newSingleThreadExecutor();
    //this.executor = Executors.newCachedThreadPool();

    this.mode = Mode.SELECT;
    this.orientation = Orientation.EAST;
    this.direction = Direction.CENTER;

    initComponents();
    postInit();
  }

  private void postInit() {
    routesDialog = new RoutesDialog(getParentFrame(), false, this, this.readonly);
    lineGrid = "true".equals(System.getProperty("draw.linegrid", "true"));
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();

    Set<Tile> snapshot = new HashSet<>(tiles.values());
    //ensure there is no tile which is null... 
    Map<String, RouteElementBean> routeSnapshot;
    routeSnapshot = new HashMap<>(this.selectedRouteElements);

    if (this.drawGrid) {
      if (lineGrid) {
        paintLineGrid(g);
      } else {
        paintDotGrid(g);
      }
    } else {
      paintNullGrid(g);
    }

    for (Tile tile : snapshot) {
      tile.setDrawOutline(drawGrid);
      tile.setTrackRouteColor(null, null);

      if (selectedTiles.contains(tile.getCenter())) {
        //tile.setBackgroundColor(Color.yellow);
        tile.setBackgroundColor(Color.orange);
      } else {
        tile.setBackgroundColor(Color.white);
      }

      if (routeSnapshot.containsKey(tile.getId())) {
        RouteElementBean re = routeSnapshot.get(tile.getId());
        tile.setTrackRouteColor(Color.black, re.getIncomingOrientation());

        if (tile.isJunction()) {
          AccessoryValue av = re.getAccessoryValue();
          ((Switch) tile).setRouteValue(av, Color.darkGray);
          Logger.trace("Tile: " + tile.getId() + " Value: " + av + "; " + re);
        } else {
          tile.setTrackColor(Color.darkGray);
        }
      } else {
        tile.setTrackRouteColor(null, null);

        if (tile.isJunction()) {
          ((Switch) tile).setRouteValue(AccessoryValue.OFF, Tile.DEFAULT_TRACK_COLOR);
        } else {
          tile.setTrackColor(Tile.DEFAULT_TRACK_COLOR);
        }
      }

      tile.drawTile(g2, drawGrid);
      //debug
      if (!this.readonly) {
        tile.drawCenterPoint(g2, Color.magenta, 3);
      }
    }
    g2.dispose();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("repaintTile".equals(evt.getPropertyName())) {
      Tile tile = (Tile) evt.getNewValue();
      this.repaint(tile.getBounds());
    }
  }

  private void paintNullGrid(Graphics g) {
    if (this.grid != null) {
      int pw = this.getWidth();
      int ph = this.getHeight();
      int gw = grid.getWidth();
      int gh = grid.getHeight();

      if (pw != gw || ph != gh) {
        Logger.trace("Changed Canvas: " + pw + " x " + ph + " Grid: " + gw + " x " + gh);
        this.grid = null;
      } else {
      }
    }

    if (this.grid == null) {
      int width = getSize().width;
      int height = getSize().height;

      Logger.trace("Width: " + width + " Height: " + height);

      grid = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D gc = grid.createGraphics();

      gc.setBackground(Color.white);
      gc.clearRect(0, 0, width, height);

      gc.setPaint(Color.black);

      gc.dispose();
    }
    Graphics2D g2 = (Graphics2D) g;
    //Draw grid from pre computed image
    g2.drawImage(grid, null, 0, 0);
  }

  private void paintDotGrid(Graphics g) {
    if (this.grid != null) {
      int pw = this.getWidth();
      int ph = this.getHeight();

      int gw = grid.getWidth();
      int gh = grid.getHeight();

      if (pw != gw || ph != gh) {
        Logger.trace("Changed Canvas: " + pw + " x " + ph + " Grid: " + gw + " x " + gh);
        this.grid = null;
      } else {
      }
    }

    if (this.grid == null) {
      int width = getSize().width;
      int height = getSize().height;

      Logger.trace("Width: " + width + " Height: " + height);

      grid = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D gc = grid.createGraphics();

      gc.setBackground(Color.white);
      gc.clearRect(0, 0, width, height);

      gc.setPaint(Color.black);

      for (int r = 0; r < width; r++) {
        for (int c = 0; c < height; c++) {
          gc.drawOval(r * Tile.GRID * 2, c * Tile.GRID * 2, 1, 1);
        }
      }
      gc.dispose();
    }
    Graphics2D g2 = (Graphics2D) g;
    //Draw grid from pre computed image
    g2.drawImage(grid, null, 0, 0);
  }

  private void paintLineGrid(Graphics g) {
    if (this.grid != null) {
      int pw = this.getWidth();
      int ph = this.getHeight();

      int gw = grid.getWidth();
      int gh = grid.getHeight();

      if (pw != gw || ph != gh) {
        Logger.trace("Changed Canvas: " + pw + " x " + ph + " Grid: " + gw + " x " + gh);
        this.grid = null;
      }
    }

    if (this.grid == null) {
      int width = getSize().width;
      int height = getSize().height;
      Logger.trace("Width: " + width + " Height: " + height);

      grid = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D gc = grid.createGraphics();

      gc.setBackground(Color.white);
      gc.clearRect(0, 0, width, height);

      gc.setPaint(Color.lightGray);

      gc.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      for (int x = 0; x < width; x += 40) {
        gc.drawLine(x, 0, x, height);
      }
      for (int y = 0; y < height; y += 40) {
        gc.drawLine(0, y, width, y);
      }
      gc.dispose();
    }
    Graphics2D g2 = (Graphics2D) g;
    g2.drawImage(grid, null, 0, 0);
  }

  void resetDotGrid() {
    Logger.debug("reset dot grid");
    this.grid = null;
  }

  void setMode(LayoutCanvas.Mode mode) {
    this.mode = mode;
    Logger.trace("Mode: " + mode);
  }

  LayoutCanvas.Mode getMode() {
    return this.mode;
  }

  void setDrawGrid(boolean flag) {
    this.drawGrid = flag;
    this.repaint();
  }

  void setTileType(TileBean.TileType tileType) {
    this.tileType = tileType;
    Logger.trace("TileType: " + tileType + " Current mode: " + mode);
  }

  Orientation getOrientation() {
    return orientation;
  }

  void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }

  void setDirection(Direction direction) {
    this.direction = direction;
  }

  Direction getDirection() {
    return direction;
  }

  public void loadLayoutInBackground() {
    this.executor.execute(() -> loadTiles());
  }

  public void loadTiles() {
    boolean showValues = Mode.CONTROL.equals(mode);

    List<TileBean> tileBeans = PersistenceFactory.getService().getTileBeans();

    selectedTiles.clear();
    altTiles.clear();
    tiles.clear();
    selectedRouteElements.clear();

    for (TileBean tb : tileBeans) {
      Tile tile = TileFactory.createTile(tb, drawGrid, showValues);
      tile.setPropertyChangeListener(this);

      switch (tile.getTileType()) {
        case SENSOR ->
          JCS.getJcsCommandStation().addSensorEventListener((SensorEventListener) tile);
        case SWITCH ->
          JCS.getJcsCommandStation().addAccessoryEventListener((AccessoryEventListener) tile);
        case SIGNAL ->
          JCS.getJcsCommandStation().addAccessoryEventListener((AccessoryEventListener) tile);

        default -> {
          //Do nothing
        }
      }
      tiles.put(tile.getCenter(), tile);

      //Alternative point(s) to be able to find all points
      if (!tile.getAltPoints().isEmpty()) {
        Set<Point> alt = tile.getAltPoints();
        for (Point ap : alt) {
          altTiles.put(ap, tile);
        }
      }
    }
    Logger.debug("Loaded " + tiles.size() + " Tiles...");

    this.repaint();
  }

  public void saveLayout() {
    Set<Tile> snapshot = new HashSet<>(tiles.values());
    this.selectedTiles.clear();
    this.executor.execute(() -> saveTiles(snapshot));
  }

  private void saveTiles(Set<Tile> snapshot) {
    Logger.debug("Saving " + snapshot.size() + " tiles...");
    List<TileBean> beans = new LinkedList<>();

    for (Tile tile : snapshot) {
      if (tile != null) {
        TileBean tb = tile.getTileBean();
        Logger.trace("Saving " + tile + " -> " + tb);
        beans.add(tb);
      } else {
        Logger.warn("Tile is null?");
      }
    }
    PersistenceFactory.getService().persist(beans);
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

    setBackground(new Color(250, 250, 250));
    setOpaque(false);
    setPreferredSize(new Dimension(800, 700));
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
  }// </editor-fold>//GEN-END:initComponents

  private void mouseMoveAction(MouseEvent evt) {
    //Only repaint when the mouse is snapped to the next grid
    Point sp = LayoutUtil.snapToGrid(evt.getPoint());

    Tile tile = findTile(sp);
    if (tile != null) {
      setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    } else {
      setCursor(Cursor.getDefaultCursor());
    }
  }

  private Tile getSelectedTile() {
    if (!selectedTiles.isEmpty()) {
      Set<Tile> sts = new HashSet<>();
      for (Point p : this.selectedTiles) {
        if (this.tiles.containsKey(p)) {
          Tile t = this.tiles.get(p);
          sts.add(t);
        }
      }
      Logger.trace("There are " + sts.size() + " selected tiles.");
      return sts.iterator().next();
    }
    return null;
  }

  private void mousePressedAction(MouseEvent evt) {
    Point snapPoint = LayoutUtil.snapToGrid(evt.getPoint());
    Tile tile = findTile(snapPoint);

    //Clear any previous selection
    selectedTiles.clear();
    if (tile != null) {
      selectedTiles.addAll(tile.getAllPoints());
    }

    switch (mode) {
      case CONTROL -> {
        if (tile != null) {
          executeControlActionForTile(tile, snapPoint);
        }
      }
      case ADD -> {
        if (MouseEvent.BUTTON1 == evt.getButton() && tile == null) {
          //Only add a new tile when there is no tile on the selected snapPoint
          Logger.debug("Adding a new tile: " + tileType + " @ (" + snapPoint.x + ", " + snapPoint.y + ")");
          Tile addedTile = addTile(snapPoint);
          selectedTiles.addAll(addedTile.getAllPoints());
        } else {
          Logger.debug("A tile exists at the selected position: " + tile.getTileType() + " @ (" + snapPoint.x + ", " + snapPoint.y + ")");
        }
        if (MouseEvent.BUTTON3 == evt.getButton() && tile != null) {
          showOperationsPopupMenu(tile, snapPoint);
        }
      }
      case DELETE -> {
        removeTiles(selectedTiles);
      }
      default -> {
        Logger.trace((tile != null ? "Selected tile: " + tile.getId() + ", " + tile.xyToString() : "No tile selected"));
        if (MouseEvent.BUTTON3 == evt.getButton()) {
          showOperationsPopupMenu(tile, snapPoint);
        }
      }
    }

    //in theory only the surrouding of the snapPoint should be repainted...
    this.repaint();
  }

  private void mouseDragAction(MouseEvent evt) {
    //Use this for showing the tile moving in the future
  }

  private boolean checkTileOccupation(Tile tile) {
    Set<Point> tilePoints = tile.getAllPoints();
    return checkTileOccupation(tilePoints);
  }

  private boolean checkTileOccupation(Set<Point> tilePoints) {
    for (Point p : tilePoints) {
      if (tiles.containsKey(p) || altTiles.containsKey(p)) {
        return true;
      }
    }
    return false;
  }

  private void mouseReleasedAction(MouseEvent evt) {
    Tile selTile = getSelectedTile();
    if (selTile != null) {
      Logger.trace("Selected tile: " + selTile.getId() + ", " + selTile.xyToString());
    }

    Point snapPoint = LayoutUtil.snapToGrid(evt.getPoint());

    if (!LayoutCanvas.Mode.CONTROL.equals(mode) && MouseEvent.BUTTON1 == evt.getButton() && selTile != null) {
      Point tp = selTile.getCenter();
      if (!tp.equals(snapPoint)) {
        Logger.tag("Moving Tile from " + tp + " to " + snapPoint + " Tile to move: " + selTile);
        //Check if new position is free

        if (tiles.containsKey(snapPoint) || this.altTiles.containsKey(snapPoint)) {
          Tile tile = findTile(snapPoint);
          Logger.debug("Position " + snapPoint + " is occupied with tile: " + tile + ", can't move tile");
        } else {
          //Remove the original tile center from the tiles
          Tile movingTile = tiles.remove(tp);
          if (movingTile != null) {
            //Also remove from the alt points
            Point oldCenter = movingTile.getCenter();
            Set<Point> oldAltPoints = movingTile.getAltPoints();

            //Set the new center position
            movingTile.setCenter(snapPoint);
            //Check again
            if (!checkTileOccupation(movingTile)) {
              //Also remove previous alt points
              for (Point ap : oldAltPoints) {
                tiles.remove(tp);
                altTiles.remove(tp);
              }
              Logger.trace("Moved Tile " + movingTile.getId() + " from " + tp + " to " + snapPoint + "...");
              tiles.put(snapPoint, movingTile);
              for (Point ep : movingTile.getAltPoints()) {
                altTiles.put(ep, movingTile);
              }

              selectedTiles.clear();
              selectedTiles.addAll(movingTile.getAllPoints());

              repaint();
            } else {
              //Do not move Tile, put back where it was
              movingTile.setCenter(oldCenter);
              tiles.put(oldCenter, movingTile);
            }
          }
        }
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
        this.executor.execute(() -> toggleSensor((Sensor) tile));
        //toggleSensor((Sensor) tile);
      }
      case BLOCK -> {
        Logger.trace("Show BlockDialog for " + tile.getId());
        //show the Block control dialog so tha a locomotive can be assigned to the block
        Block block = (Block) tile;
        BlockControlDialog bcd = new BlockControlDialog(getParentFrame(), block);
        bcd.setVisible(true);

        this.repaint(block.getX(), block.getY(), block.getWidth(), block.getHeight());
      }
      case SIGNAL ->
        this.executor.execute(() -> toggleSignal((Signal) tile));
      case SWITCH ->
        this.executor.execute(() -> toggleSwitch((Switch) tile));
      case CROSS -> {
      }
      default -> {
      }
    }
  }

  private void toggleSwitch(Switch turnout) {
    if (turnout.getAccessoryBean() != null) {
      AccessoryBean ab = turnout.getAccessoryBean();
      ab.toggle();
      turnout.setValue(ab.getAccessoryValue());
      
      JCS.getJcsCommandStation().switchAccessory(ab, ab.getAccessoryValue());
      repaint(turnout.getX(), turnout.getY(), turnout.getWidth(), turnout.getHeight());
    } else {
      Logger.trace("No AccessoryBean configured for Turnout: " + turnout.getId());
    }
  }

  private void toggleSignal(Signal signal) {
    if (signal.getAccessoryBean() != null) {
      AccessoryBean ab = signal.getAccessoryBean();
      ab.toggle();
      Logger.trace("A: " + ab.getAddress() + " S: " + ab.getStates() + " P: " + ab.getState());

      JCS.getJcsCommandStation().switchAccessory(ab, ab.getAccessoryValue());
      repaint(signal.getX(), signal.getY(), signal.getWidth(), signal.getHeight());
    } else {
      Logger.trace("No AccessoryBean configured for Signal: " + signal.getId());
    }
  }

  private void toggleSensor(Sensor sensor) {
    SensorBean sb = sensor.getSensorBean();
    if (sb != null) {
      sb.toggle();
      sensor.setActive((sb.getStatus()==1));
      Logger.trace("id: " + sb.getId() + " state " + sb.getStatus());

      sensor.repaintTile();
      
      SensorEvent sensorEvent = new SensorEvent(sb);

      List<FeedbackController> acl = JCS.getJcsCommandStation().getFeedbackControllers();
      for (FeedbackController fbc : acl) {
        fbc.fireSensorEventListeners(sensorEvent);
      }
    }
  }

  private void notifyFeedbackListeners(SensorEvent sensorEvent) {
    List<FeedbackController> acl = JCS.getJcsCommandStation().getFeedbackControllers();

    for (FeedbackController fbc : acl) {
      fbc.fireSensorEventListeners(sensorEvent);
    }
  }

  private void editSelectedTileProperties() {
    //the first tile should be the selected one
    boolean showProperties = false;
    boolean showFlip = false;
    boolean showRotate = false;
    boolean showMove = false;
    boolean showDelete = false;

    if (!this.selectedTiles.isEmpty()) {
      Point tcp = this.selectedTiles.iterator().next();
      Tile tile = findTile(tcp);
      TileBean.TileType tt = tile.getTileType();

      Logger.trace("Seleted tile " + tile.getId() + " TileType " + tt);

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
          SensorDialog fbd = new SensorDialog(getParentFrame(), (Sensor) tile);
          fbd.setVisible(true);
        }
        case SIGNAL -> {
          SignalDialog sd = new SignalDialog(getParentFrame(), (Signal) tile);
          sd.setVisible(true);
        }
        case SWITCH -> {
          SwitchDialog td = new SwitchDialog(getParentFrame(), (Switch) tile);
          td.setVisible(true);
        }
        case BLOCK -> {
          Logger.trace("Show BlockDialog for " + tile.getId());
          BlockDialog bd = new BlockDialog(getParentFrame(), (Block) tile, this);
          bd.setVisible(true);
        }
        default -> {
        }
      }
    }
    this.executor.execute(() -> repaint());
    Logger.trace("Edit done");
  }

  private void showOperationsPopupMenu(Tile tile, Point p) {
    //which items should be shown
    boolean showProperties = false;
    boolean showFlip = false;
    boolean showRotate = false;
    boolean showMove = false;
    boolean showDelete = false;

    TileBean.TileType tt = tile.getTileType();
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

  private void formMousePressed(MouseEvent evt) {//GEN-FIRST:event_formMousePressed
    mousePressedAction(evt);
  }//GEN-LAST:event_formMousePressed

  private void formMouseReleased(MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
    mouseReleasedAction(evt);
  }//GEN-LAST:event_formMouseReleased

  private void horizontalMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_horizontalMIActionPerformed
    Logger.trace(this.orientation + ", " + evt.getModifiers() + ", " + evt.paramString());

    if (this.mouseLocation != null && evt.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
      addTile(this.mouseLocation);
      this.mouseLocation = null;
    }
  }//GEN-LAST:event_horizontalMIActionPerformed

  private void verticalMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_verticalMIActionPerformed
    Logger.trace(this.orientation + ", " + evt.getModifiers() + ", " + evt.paramString());

    if (this.mouseLocation != null && evt.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
      addTile(this.mouseLocation);
      this.mouseLocation = null;
    }
  }//GEN-LAST:event_verticalMIActionPerformed

  private void rightMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rightMIActionPerformed
    Logger.trace(this.orientation + ", " + evt.getModifiers() + ", " + evt.paramString());

    if (this.mouseLocation != null && evt.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
      addTile(this.mouseLocation);
      this.mouseLocation = null;
    }
  }//GEN-LAST:event_rightMIActionPerformed

  private void leftMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_leftMIActionPerformed
    Logger.trace(this.orientation + ", " + evt.getModifiers() + ", " + evt.paramString());

    if (this.mouseLocation != null && evt.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
      addTile(this.mouseLocation);

      //do someting with the rotation/direction...
      this.mouseLocation = null;
    }
  }//GEN-LAST:event_leftMIActionPerformed

  public Tile findTile(Point cp) {
    Tile result = this.tiles.get(cp);
    if (result == null) {
      //Logger.trace("Using alternative points...");
      result = this.altTiles.get(cp);
      if (result != null) {
        //Logger.trace("Found " + result + " in alt tiles");
      }
    }

    return result;
  }

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
      this.removeTiles(selectedTiles);
    }//GEN-LAST:event_deleteMIActionPerformed

    private void propertiesMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_propertiesMIActionPerformed
      editSelectedTileProperties();
    }//GEN-LAST:event_propertiesMIActionPerformed

  private void formMouseDragged(MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
    mouseDragAction(evt);
  }//GEN-LAST:event_formMouseDragged

  private Point getCheckAvailable(Point newPoint) {
    if (this.tiles.containsKey(newPoint)) {
      Tile et = this.tiles.get(newPoint);

      Logger.debug("@ " + newPoint + " is allready occcupied by: " + et + "...");
      //Search for the nearest avalaible free point 
      //first get the Center point of the tile which is occuping this slot

      // show warning!
      Point ecp = et.getCenter();

      int w = et.getWidth();
      int h = et.getHeight();

      Point np;
      np = switch (this.orientation) {
        case EAST ->
          new Point(ecp.x + w, ecp.y);
        case WEST ->
          new Point(newPoint.x - w, ecp.y);
        case SOUTH ->
          new Point(ecp.x, newPoint.y + h);
        default ->
          new Point(ecp.x, newPoint.y - h);
      };

      Logger.trace("Alternative CP: " + np);
      // recursive check
      return getCheckAvailable(np);
    } else {
      Logger.debug("@ " + newPoint + " is not yet used...");

      return newPoint;
    }
  }

  private Tile addTile(Point p) {
    if (this.orientation == null) {
      this.orientation = Orientation.EAST;
    }

    if (this.direction == null) {
      this.direction = Direction.RIGHT;
    }

    Logger.trace("Adding: " + tileType + " @ " + p);

    Point chkp = getCheckAvailable(p);

    boolean fullRepaint = !chkp.equals(p);

    Tile tile = TileFactory.createTile(tileType, orientation, direction, chkp, drawGrid);

    tiles.put(chkp, tile);
    //Alternative point(s) to be able to find all points
    if (!tile.getAltPoints().isEmpty()) {
      Set<Point> alt = tile.getAltPoints();
      for (Point ap : alt) {
        this.altTiles.put(ap, tile);
      }
    }
    Logger.trace("Added Tile " + tile.getClass().getSimpleName() + " " + tile.getOrientation() + " @ " + tile.getCenter() + " Full repaint: " + fullRepaint);
    Logger.trace("Added " + tile + " There are now " + this.tiles.size() + " tiles...");

    if (fullRepaint) {
      this.repaint();
    }
    return tile;
  }

  void removeTiles() {

    removeTiles(selectedTiles);
  }

  private void removeTiles(Set<Point> pointsToRemove) {
    for (Point p : pointsToRemove) {
      Tile removed = this.tiles.remove(p);

      if (removed != null && removed.getAllPoints() != null) {
        Set<Point> rps = removed.getAltPoints();
        //Also remove alt points
        for (Point ap : rps) {
          tiles.remove(ap);
        }

        Logger.trace("Removed: " + removed);
      }
    }
    selectedTiles.clear();
    repaint();
  }

  private java.awt.Frame getParentFrame() {
    JFrame frame = (JFrame) SwingUtilities.getRoot(this);
    return frame;
  }

  public void rotateSelectedTile() {
    Logger.trace("Selected Tiles " + selectedTiles.size());

    //make copy as the map could be cleared
    Set<Point> snapshot = new HashSet<>(selectedTiles);
    for (Point p : snapshot) {
      Logger.trace("Selected Tile @ " + p);
      if (this.tiles.containsKey(p)) {
        Tile t = this.tiles.get(p);
        //Remove the alternative or extra points...
        for (Point ep : t.getAltPoints()) {
          this.altTiles.remove(ep);
        }

        t.rotate();
        Logger.trace("Rotated " + t);
        this.orientation = t.getOrientation();
        this.direction = t.getDirection();

        //override
        this.tiles.put(p, t);
        for (Point ep : t.getAltPoints()) {
          this.altTiles.put(ep, t);
        }

        this.selectedTiles.clear();
        this.selectedTiles.addAll(t.getAllPoints());
      }
    }
    this.executor.execute(() -> repaint());
  }

  public void flipSelectedTileHorizontal() {
    Set<Point> snapshot = new HashSet<>(selectedTiles);
    for (Point p : snapshot) {
      Logger.trace("Selected Tile @ " + p);
      if (this.tiles.containsKey(p)) {
        Tile t = this.tiles.get(p);
        //Remove the alternative or extra points...
        for (Point ep : t.getAltPoints()) {
          this.altTiles.remove(ep);
        }

        t.flipHorizontal();
        Logger.trace("Flipped " + t);
        this.orientation = t.getOrientation();
        this.direction = t.getDirection();

        //override
        this.tiles.put(p, t);
        for (Point ep : t.getAltPoints()) {
          this.altTiles.put(ep, t);
        }

        this.selectedTiles.clear();
        this.selectedTiles.addAll(t.getAllPoints());
      }
    }
    this.executor.execute(() -> repaint());
  }

  public void flipSelectedTileVertical() {
    Set<Point> snapshot = new HashSet<>(selectedTiles);
    for (Point p : snapshot) {
      Logger.trace("Selected Tile @ " + p);
      if (this.tiles.containsKey(p)) {
        Tile t = this.tiles.get(p);
        //Remove the alternative or extra points...
        for (Point ep : t.getAltPoints()) {
          this.altTiles.remove(ep);
        }

        t.flipVertical();
        Logger.trace("Flipped " + t);
        this.orientation = t.getOrientation();
        this.direction = t.getDirection();

        //override
        this.tiles.put(p, t);
        for (Point ep : t.getAltPoints()) {
          this.altTiles.put(ep, t);
        }

        this.selectedTiles.clear();
        this.selectedTiles.addAll(t.getAllPoints());
      }
    }
    this.executor.execute(() -> repaint());
  }

  void routeLayout() {
    //routeLayoutWithAStar();
    this.executor.execute(() -> routeLayoutWithAStar());
  }

  private void routeLayoutWithAStar() {
    //Make sure the layout is saved
    Set<Tile> snapshot = new HashSet<>(tiles.values());
    this.saveTiles(snapshot);

    AStar astar = new AStar();
    astar.buildGraph(this.tiles.values().stream().collect(Collectors.toList()));
    astar.routeAll();
    astar.persistRoutes();
    if (this.routesDialog.isVisible()) {
      this.routesDialog.loadRoutes();
    }
  }

  void showRoutesDialog() {
    this.routesDialog.setVisible(true);
  }

  void setSelectRoute(RouteBean route) {
    selectedRouteElements.clear();
    if (route != null) {
      List<RouteElementBean> rel = route.getRouteElements();
      for (RouteElementBean re : rel) {
        String id = re.getTileId();
        Orientation incomingSide = re.getIncomingOrientation();

        String nodeId = re.getNodeId();

        //if (id.startsWith("sw-") || id.startsWith("cs-")) {
        //Switches are 2 times in there, only the one with a value is needed
        //if (!id.equals(nodeId)) {
        //  selectedRouteElements.put(id, re);
        //}
        //} else {
        selectedRouteElements.put(id, re);
        //}
      }
    }

    this.executor.execute(() -> repaint());
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JPopupMenu curvedPopupMenu;
  private JMenuItem deleteMI;
  private JMenuItem flipHorizontalMI;
  private JMenuItem flipVerticalMI;
  private JMenuItem horizontalMI;
  private JMenuItem leftMI;
  private JMenuItem moveMI;
  private JPopupMenu operationsPM;
  private JMenuItem propertiesMI;
  private JMenuItem rightMI;
  private JMenuItem rotateMI;
  private JPopupMenu straightPopupMenu;
  private JMenuItem verticalMI;
  private JMenuItem xyMI;
  // End of variables declaration//GEN-END:variables
}
