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

import java.awt.BasicStroke;
import jcs.entities.enums.TileType;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import jcs.entities.AccessoryBean;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.TileBean;
import jcs.entities.enums.AccessoryValue;
import jcs.trackservice.TrackControllerFactory;
import jcs.ui.layout.tiles.enums.Direction;
import jcs.entities.enums.Orientation;
import jcs.ui.layout.dialogs.SensorDialog;
import jcs.ui.layout.dialogs.SignalDialog;
import jcs.ui.layout.dialogs.SwitchDialog;
import jcs.ui.layout.enums.Mode;
import jcs.ui.layout.pathfinding.BreathFirst;
import jcs.ui.layout.tiles.Sensor;
import jcs.ui.layout.tiles.Signal;
import jcs.ui.layout.tiles.Switch;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 * This canvas / Panel is used to draw the layout
 *
 * @author frans
 */
public class LayoutCanvas extends JPanel implements RepaintListener {

    private boolean readonly;
    private Mode mode;
    private boolean drawGrid = true;

    private Orientation orientation;
    private Direction direction;
    private TileType tileType;

    private Point mouseLocation = new Point(0, 0);

    private BufferedImage grid;

    private final ExecutorService executor;

    private final Map<Point, Tile> tiles;
    private final Map<Point, Tile> altTiles;
    private final Set<Point> selectedTiles;
    private final Set<Point> movingTiles;

    private Tile movingTile;

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
        this.movingTiles = new HashSet<>();

        this.selectedRouteElements = new HashMap<>();

        this.executor = Executors.newSingleThreadExecutor();

        this.mode = Mode.SELECT;
        this.orientation = Orientation.EAST;
        this.direction = Direction.CENTER;

        initComponents();
        postInit();
    }

    private void postInit() {
        routesDialog = new RoutesDialog(getParentFrame(), false, this, this.readonly);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        Set<Tile> snapshot;
        Map<String, RouteElementBean> routeSnapshot;

        synchronized (tiles) {
            snapshot = new HashSet<>(tiles.values());
            routeSnapshot = new HashMap<>(this.selectedRouteElements);
        }

        if (this.drawGrid) {
            //paintDotGrid(g);
            paintLineGrid(g);
        } else {
            paintNullGrid(g);
        }

        for (Tile tile : snapshot) {
            if (tile != null) {
                tile.setDrawOutline(drawGrid);

                if (selectedTiles.contains(tile.getCenter())) {
                    tile.setBackgroundColor(Color.yellow);
                } else {
                    tile.setBackgroundColor(Color.white);
                }

                if (routeSnapshot.containsKey(tile.getId())) {
                    if (TileType.CROSS.equals(tile.getTileType()) || TileType.SWITCH.equals(tile.getTileType())) {
                        RouteElementBean re = routeSnapshot.get(tile.getId());
                        AccessoryValue av = re.getAccessoryValue();
                        ((Switch) tile).setRouteValue(av, Color.darkGray);
                        Logger.trace("Tile: " + tile.getId() + " Value: " + av + "; " + re);
                    } else {
                        tile.setTrackColor(Color.darkGray);
                    }
                } else {
                    if (TileType.CROSS.equals(tile.getTileType()) || TileType.SWITCH.equals(tile.getTileType())) {
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
        }

        g2.dispose();
    }

    @Override
    public void repaintTile(Tile tile) {
        this.repaint(tile.getBounds());
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
            int pw = this.getWidth(); // getSize().width;
            int ph = this.getHeight(); // getSize().height;

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

    void setMode(Mode mode) {
        this.mode = mode;
        Logger.trace("Mode: " + mode);
    }

    void setDrawGrid(boolean flag) {
        this.drawGrid = flag;
    }

    void setTileType(TileType tileType) {
        this.tileType = tileType;
        Logger.trace("TileType: " + this.tileType);
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

    public void loadLayout() {
        this.executor.execute(() -> loadTiles());
    }

    private void loadTiles() {
        boolean showValues = Mode.CONTROL.equals(this.mode);

        Map<Point, Tile> tm = LayoutUtil.loadLayout(drawGrid, this, showValues);
        Set<Point> ps = tm.keySet();
        synchronized (tiles) {
            selectedTiles.clear();
            altTiles.clear();
            tiles.clear();

            for (Point p : ps) {
                Tile t = tm.get(p);
                tiles.put(t.getCenter(), t);
                //Alternative point(s) to be able to find all points
                if (!t.getAltPoints().isEmpty()) {
                    Set<Point> alt = t.getAltPoints();
                    for (Point ap : alt) {
                        altTiles.put(ap, t);
                    }
                }
            }
        }

        Logger.debug("Loaded " + this.tiles.size() + " tiles...");
        this.repaint();
    }

    public void saveLayout() {
        this.executor.execute(() -> saveTiles());
    }

    private void saveTiles() {
        Logger.debug("Saving " + this.tiles.size() + " tiles...");

        Set<Tile> snapshot;
        synchronized (tiles) {
            snapshot = new HashSet<>(tiles.values());
        }

        Set<TileBean> beans = new HashSet<>();

        for (Tile tile : snapshot) {
            if (tile != null) {
                TileBean tb = tile.getTileBean();
                Logger.trace("Saving " + tile + " -> " + tb);
                beans.add(tb);
            } else {
                Logger.warn("Tile is null?");
            }
        }
        TrackControllerFactory.getTrackController().persist(beans);
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
            public void mouseMoved(MouseEvent evt) {
                formMouseMoved(evt);
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                formMouseClicked(evt);
            }
            public void mousePressed(MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents


  private void formMouseClicked(MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
      Logger.trace("Click Mouse @ X: " + evt.getX() + " Y:" + evt.getY() + " button " + evt.getButton() + " " + evt.paramString());
      Point p = LayoutUtil.snapToGrid(evt.getPoint());
      Logger.trace("Snap Tile X: " + p.getX() + " Y:" + p.getY() + " Mode: " + this.mode);

      Tile tile = this.findTile(p);

      //Always make a new selection
      this.selectedTiles.clear();
      if (MouseEvent.BUTTON1 == evt.getButton() || MouseEvent.BUTTON3 == evt.getButton()) {
          if (tile != null) {
              Logger.trace("selecting " + tile + " @ " + p);
              this.selectedTiles.addAll(tile.getAllPoints());
          }
      }

      switch (this.mode) {
          case ADD:
              if (MouseEvent.BUTTON1 == evt.getButton()) {
                  this.selectedTiles.clear();

                  Logger.trace("Adding tile: " + this.tileType + " @ (" + p.x + ", " + p.y + ")");
                  addTile(p);
                  this.selectedTiles.add(p);
              }
              if (MouseEvent.BUTTON3 == evt.getButton() && tile != null) {
                  showOperationsPopupMenu(tile, p);
              }
              break;
          case DELETE:
              Set<Point> toRemove = new HashSet<>(selectedTiles);
              this.removeTiles(toRemove);
              break;
          case CONTROL:
              if (tile != null) {
                  executeControlActionForTile(tile, p);
              }
              break;
          default:
              if (MouseEvent.BUTTON3 == evt.getButton() && tile != null) {
                  showOperationsPopupMenu(tile, p);
              }
              break;
      }
      this.repaint();

  }//GEN-LAST:event_formMouseClicked

    private void executeControlActionForTile(Tile tile, Point p) {
        TileType tt = tile.getTileType();
        switch (tt) {
            case STRAIGHT:
                break;
            case CURVED:
                break;
            case SENSOR:
                break;
            case BLOCK:
                break;
            case SIGNAL:
                toggleSignal((Signal) tile);
                break;
            case SWITCH:
                toggleSwitch((Switch) tile);
                break;
            case CROSS:
                break;
            default:
                break;
        }
    }

    private void toggleSwitch(Switch turnout) {
        if (turnout.getAccessoryBean() != null) {
            AccessoryBean ab = turnout.getAccessoryBean();
            ab.toggle();
            TrackControllerFactory.getTrackController().switchAccessory(ab.getAccessoryValue(), ab);
        } else {
            Logger.trace("No AccessoryBean configured for Turnout: " + turnout.getId());
        }
    }

    private void toggleSignal(Signal signal) {
        if (signal.getAccessoryBean() != null) {
            AccessoryBean ab = signal.getAccessoryBean();
            ab.toggle();
            Logger.trace("A: " + ab.getAddress() + " S: " + ab.getStates() + " P: " + ab.getPosition());

            TrackControllerFactory.getTrackController().switchAccessory(ab.getAccessoryValue(), ab);
        } else {
            Logger.trace("No AccessoryBean configured for Signal: " + signal.getId());
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
            TileType tt = tile.getTileType();
            switch (tt) {
                case STRAIGHT:
                    showRotate = true;
                    showDelete = true;
                    break;
                case CURVED:
                    showRotate = true;
                    showDelete = true;
                    break;
                case SENSOR:
                    SensorDialog fbd = new SensorDialog(getParentFrame(), (Sensor) tile);
                    fbd.setVisible(true);
                    break;
//                case BLOCK:
//                    OccupancySensorDialog osd = new OccupancySensorDialog(getParentFrame(), true, (BlockTile) tile);
//                    osd.setVisible(true);
//                    break;
                case SIGNAL:
                    SignalDialog sd = new SignalDialog(getParentFrame(), (Signal) tile);
                    sd.setVisible(true);
                    break;
                case SWITCH:
                    SwitchDialog td = new SwitchDialog(getParentFrame(), (Switch) tile);
                    td.setVisible(true);
                    break;
                default:
                    break;
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

        TileType tt = tile.getTileType();
        switch (tt) {
//            case "StraightTrack":
//                showRotate = true;
//                showDelete = true;
//                break;
//            case "DiagonalTrack":
//                showRotate = true;
//                showDelete = true;
//                break;
            case SENSOR:
                showProperties = true;
                showRotate = true;
                showDelete = true;
                break;
            case BLOCK:
                showProperties = true;
                showRotate = true;
                showDelete = true;
                break;
            case SIGNAL:
                showProperties = true;
                showRotate = true;
                showDelete = true;
                break;
            case SWITCH:
                showProperties = true;
                showFlip = true;
                showRotate = true;
                showDelete = true;
                break;
            case CROSS:
                showProperties = true;
                showFlip = true;
                showRotate = true;
                showDelete = true;
                break;
            default:
                showRotate = true;
                showDelete = true;
                break;
        }
        this.xyMI.setVisible(true);
        this.xyMI.setText(tile.getId() + " (" + p.x + "," + p.y + ") O: " + tile.getOrientation().getOrientation() + " D: " + tile.getDirection());
        this.propertiesMI.setVisible(showProperties);
        this.flipHorizontalMI.setVisible(showFlip);
        this.flipVerticalMI.setVisible(showFlip);
        this.rotateMI.setVisible(showRotate);
        this.moveMI.setVisible(showMove);
        this.deleteMI.setVisible(showDelete);
        this.operationsPM.show(this, p.x, p.y);
    }


  private void formMousePressed(MouseEvent evt) {//GEN-FIRST:event_formMousePressed
      Logger.trace("Press X: " + evt.getX() + " Y:" + evt.getY() + " button " + evt.getButton() + " " + evt.paramString());
      Point p = LayoutUtil.snapToGrid(evt.getPoint());
      Tile tile = this.findTile(p);

      if (MouseEvent.BUTTON1 == evt.getButton() && tile != null) {
          this.movingTiles.clear();
          this.selectedTiles.clear();
          this.movingTiles.add(tile.getCenter());

          this.movingTile = tile;
          this.selectedTiles.add(p);
          Logger.trace("Setting moving tile: " + tile);
      } else {
          this.movingTiles.clear();
          this.movingTile = null;
      }
  }//GEN-LAST:event_formMousePressed

  private void formMouseReleased(MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
      Logger.trace("X: " + evt.getX() + " Y:" + evt.getY() + " button " + evt.getButton() + " " + evt.paramString());

      Point p = LayoutUtil.snapToGrid(evt.getPoint());

      if (this.movingTile != null) {
          Point tp = this.movingTile.getCenter();
          if (!tp.equals(p)) {
              Logger.tag("Moving Tile from " + tp + " to " + p + " Tile to move: " + this.movingTile);
              //Check if new position is free
              if (this.tiles.containsKey(p)) {
                  Logger.debug("Point " + p + " is occupied by tile: " + tiles.get(p));
                  this.movingTile = null;
              } else {
                  Tile tile = this.tiles.remove(tp);
                  if (tile != null) {
                      Set<Point> rps = tile.getAltPoints();
                      //Also remove alt points
                      for (Point ap : rps) {
                          this.tiles.remove(ap);
                      }
                      Logger.trace("Moved Tile from " + tp + " to " + p + " Tile: " + tile + "...");

                      tile.setCenter(p);
                      this.tiles.put(p, tile);
                      for (Point ep : tile.getAltPoints()) {
                          this.altTiles.put(ep, tile);
                      }

                      this.selectedTiles.clear();
                      this.selectedTiles.add(p);

                      this.movingTile = null;
                  }
                  this.repaint();
              }
          } else {
              this.movingTile = null;
          }
      }
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

    private Tile findTile(Point cp) {
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
      //Only repaint when the mouse is snapped to the next grid
      Point sp = LayoutUtil.snapToGrid(evt.getPoint());

      Tile tile = findTile(sp);
      if (tile != null) {
          setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      } else {
          setCursor(Cursor.getDefaultCursor());
      }
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
            switch (this.orientation) {
                case EAST:
                    np = new Point(ecp.x + w, ecp.y);
                    break;
                case WEST:
                    np = new Point(newPoint.x - w, ecp.y);
                    break;
                case SOUTH:
                    np = new Point(ecp.x, newPoint.y + h);
                    break;
                default:
                    np = new Point(ecp.x, newPoint.y - h);
                    break;
            }

            Logger.trace("Alternative CP: " + np);
            // recursive check
            return getCheckAvailable(np);
        } else {
            Logger.debug("@ " + newPoint + " is not yet used...");

            return newPoint;
        }
    }

    private void addTile(Point p) {
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
    }

    void removeTiles() {
        removeTiles(this.selectedTiles);
    }

    private void removeTiles(Set<Point> pointsToRemove) {
        for (Point p : pointsToRemove) {
            synchronized (tiles) {
                Tile removed = this.tiles.remove(p);
                if (removed != null && removed.getAllPoints() != null) {
                    Set<Point> rps = removed.getAltPoints();
                    //Also remove alt points
                    for (Point ap : rps) {
                        this.tiles.remove(ap);
                    }
                    Logger.trace("Removed: " + removed);
                }
            }
        }
        this.selectedTiles.clear();
        this.repaint();

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

    void routeLayoutDirect() {
        routeLayoutWithBreathFirst();
    }

    private void routeLayoutWithBreathFirst() {
        //Make sure the layout is saved
        this.saveTiles();

        BreathFirst bf = new BreathFirst();
        bf.buildGraph(this.tiles);
        bf.routeAll();
        bf.persistRoutes();
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
                String nodeId = re.getNodeId();
                if (id.startsWith("sw-") || id.startsWith("cs-")) {
                    //Switches are 2 times in there, only the one with a value is needed
                    if (!id.equals(nodeId)) {
                        selectedRouteElements.put(id, re);
                    }
                } else {
                    selectedRouteElements.put(id, re);
                }
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
