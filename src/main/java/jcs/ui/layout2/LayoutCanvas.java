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
package jcs.ui.layout2;

import java.awt.BasicStroke;
import jcs.entities.enums.TileType;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import jcs.entities.TileBean;
import jcs.trackservice.TrackServiceFactory;
import lan.wervel.jcs.ui.layout.tiles.AbstractTile;
import jcs.ui.layout.tiles.enums.Direction;
import jcs.entities.enums.Orientation;
import jcs.ui.layout2.enums.Mode;
import jcs.ui.layout2.pathfinding.BreathFirst;
import jcs.ui.layout2.tiles2.Block;
import jcs.ui.layout2.tiles2.TileFactory2;
import org.tinylog.Logger;

/**
 * This canvas / Panel is used to draw the layout
 *
 * @author frans
 */
public class LayoutCanvas extends JPanel {

    public static final int GRID_SIZE = AbstractTile.MIN_GRID;

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

    /**
     * Creates new form GridsCanvas
     */
    public LayoutCanvas() {
        this.tiles = new HashMap<>();
        this.altTiles = new HashMap<>();

        this.selectedTiles = new HashSet<>();
        this.movingTiles = new HashSet<>();
        this.executor = Executors.newSingleThreadExecutor();
        //Default 
        this.mode = Mode.SELECT;
        this.orientation = Orientation.EAST;
        this.direction = Direction.CENTER;

        initComponents();

        postInit();
    }

    private void postInit() {

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        Set<Tile> snapshot;
        synchronized (tiles) {
            snapshot = new HashSet<>(tiles.values());
        }

        if (this.drawGrid) {
            //paintDotGrid(g);
            paintLineGrid(g);
        }

        for (Tile tile : snapshot) {
            if (tile != null) {
                tile.setDrawOutline(drawGrid);

                if (selectedTiles.contains(tile.getCenter())) {
                    tile.setBackgroundColor(Color.yellow);
                } else {
                    tile.setBackgroundColor(Color.white);
                }

                tile.drawTile(g2, drawGrid);

                tile.drawCenterPoint(g2, Color.magenta, 3);
            }
        }

//        for (AbstractTile tile : snapshot) {
//            if (tile != null) {
//                tile.drawName(g2);
//            }
//        }
//        for (AbstractTile2 selectedTile : selectedSnapshot) {
//            selectedTile.drawBounds(g2);
//            selectedTile.drawCenterPoint(g2, Color.red, 4);
//        }
//        if (this.movingTile != null) {
//            g2.setColor(Color.CYAN);
//            g2.draw(this.movingTile.getBounds());
//
//            g.setColor(Color.BLUE);
//            movingTile.drawCenterPoint(g2, Color.blue);
//        }
        g2.dispose();
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
                    gc.drawOval(r * GRID_SIZE * 2, c * GRID_SIZE * 2, 1, 1);
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
        Map<Point, Tile> tm = LayoutUtil.loadLayout(drawGrid);
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

        Logger.debug("Loaded " + this.tiles.size()+" tiles...");
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
        TrackServiceFactory.getTrackService().persist(beans);
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

        setBounds(new Rectangle(0, 0, 800, 700));
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
            public void mouseClicked(MouseEvent evt) {
                formMouseClicked(evt);
            }
            public void mouseEntered(MouseEvent evt) {
                formMouseEntered(evt);
            }
            public void mouseExited(MouseEvent evt) {
                formMouseExited(evt);
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
      Logger.trace("Snap Tile X: " + p.getX() + " Y:" + p.getY());

      //cross tile selectie gaat fout
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

          default:

              if (MouseEvent.BUTTON3 == evt.getButton() && tile != null) {
                  showOperationsPopupMenu(tile, p);
              }

              break;
      }

//      this.executor.execute(() -> repaint());
      //this.executor.execute(() -> setDependentComponents());
      //notifySelectionListeners();
//      this.executor.execute(() -> notifySelectionListeners());
      /////////////////////
      //this.repaint();
      this.repaint();

  }//GEN-LAST:event_formMouseClicked


  private void formMouseDragged(MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
//      this.movedPoint = AbstractTile2.snapToGrid(evt.getX(), evt.getY());
//      Logger.trace("Moved point: " + this.movedPoint + " button " + evt.getButton() + " " + evt.paramString());
//      if (!this.selectedTiles.isEmpty()) {
//
//      }


  }//GEN-LAST:event_formMouseDragged

    private void showOperationsPopupMenu(Tile tile, Point p) {
        //which items should be shown
        boolean showProperties = false;
        boolean showFlip = false;
        boolean showRotate = false;
        boolean showMove = false;
        boolean showDelete = false;

        String tt = tile.getClass().getSimpleName();
        switch (tt) {
//            case "StraightTrack":
//                showRotate = true;
//                showDelete = true;
//                break;
//            case "DiagonalTrack":
//                showRotate = true;
//                showDelete = true;
//                break;
            case "SensorTile":
                showProperties = true;
                showRotate = true;
                showDelete = true;
                break;
            case "Block":
                showProperties = true;
                showRotate = true;
                showDelete = true;
                break;
            case "SignalTile":
                showProperties = true;
                showRotate = true;
                showDelete = true;
                break;
            case "SwitchTile":
                showProperties = true;
                showFlip = true;
                showRotate = true;
                showDelete = true;
                break;
            case "CrossTile":
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

  private void formMouseExited(MouseEvent evt) {//GEN-FIRST:event_formMouseExited
      //Logger.trace("X: " + evt.getX() + " Y:" + evt.getY());
      //this.mouseInCanvas = false;
      //this.repaint();
  }//GEN-LAST:event_formMouseExited

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

  private void formMouseEntered(MouseEvent evt) {//GEN-FIRST:event_formMouseEntered
      //Logger.trace("X: " + evt.getX() + " Y:" + evt.getY());
      //this.mouseInCanvas = true;
  }//GEN-LAST:event_formMouseEntered

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

      //int x = evt.getX();
      //int y = evt.getY();
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

        Tile tile = TileFactory2.createTile(tileType, orientation, direction, chkp, drawGrid);

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
        java.awt.Frame frame = null;
        java.awt.Container container;
        int compCount = 10;
        while (compCount > 0) {
            container = this.getParent();
            if (container != null && container instanceof java.awt.Frame) {
                frame = (java.awt.Frame) container;
                compCount = 0;
            } else {
                compCount--;
            }
        }
        return frame;
    }

    private void editSelectedTileProperties() {
        //the first tile should be the selected one
//        if (this.selectedTiles.size() > 0) {
//            AbstractTile2 tile = this.selectedTiles.iterator().next();
//            Logger.trace("Selected Tile: " + tile);
//            String tt = tile.getClass().getSimpleName();
//            switch (tt) {
//            case "StraightTrack":
//                showRotate = true;
//                showDelete = true;
//                break;
//            case "DiagonalTrack":
//                showRotate = true;
//                showDelete = true;
//                break;
//                case "FeedbackPort":
//                    SensorDialog fbd = new SensorDialog(getParentFrame(), true, (SensorTile) tile);
//                    fbd.setVisible(true);
//                    break;
//                case "OccupancyDetector":
//                    OccupancySensorDialog osd = new OccupancySensorDialog(getParentFrame(), true, (BlockTile) tile);
//                    osd.setVisible(true);
//                    break;
//                case "SignalTile":
//                    SignalDialog sd = new SignalDialog(getParentFrame(), true, (SignalTile) tile);
//                    sd.setVisible(true);
//                    break;
//                case "TurnoutTile":
//                    TurnoutDialog td = new TurnoutDialog(getParentFrame(), true, (SwitchTile) tile);
//                    td.setVisible(true);
//                    break;
//                default:
//                    break;
//            }
//        }
        this.executor.execute(() -> repaint());
        Logger.trace("Edit done");

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
        //this.executor.execute(() -> notifySelectionModeChange());
        //notifySelectionModeChange();
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

        //this.executor.execute(() -> notifySelectionModeChange());
        //notifySelectionModeChange();
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

        //this.executor.execute(() -> notifySelectionModeChange());
        //notifySelectionModeChange();
        this.executor.execute(() -> repaint());
    }

    public void moveSelectedTile() {
//        if (!this.selectedTiles.isEmpty()) {
//            AbstractTile selectedTile = selectedTiles.iterator().next();
//            this.movingTile = selectedTile;
//            selectedTiles.clear();
//            this.repaint();
//        }
    }

    void routeLayout() {
        //only routes from block to block so
        List<Block> blocks = new ArrayList<>();
        //Find blocks..
        Set<Tile> snapshot = new HashSet<>(tiles.values());
        Logger.trace("Layout has " + snapshot.size() + " tiles...");
        for (Tile b : snapshot) {
            if (b instanceof Block) {
                blocks.add((Block) b);
            }
        }
        Logger.trace("Layout has " + blocks.size() + " blocks...");

        //Router2 r = new Router2();
        //r.createGraph(snapshot);
        BreathFirst bfr = new BreathFirst();
        //bfr.createGraph(snapshot);

//        Color[] colors = {Color.blue, Color.cyan, Color.magenta, Color.orange, Color.pink, Color.red, Color.yellow, Color.green, Color.black, Color.darkGray};
//        List<List<Tile>> routes = new ArrayList<>();
//
//        int col = 0;
//        for (Block from : blocks) {
//            for (Block to : blocks) {
//                if (!from.getId().equals(to.getId())) {
//                    Logger.trace("Route from: " + from.getId() + " (" + from.getCenterX() + "," + from.getCenterY() + ") to: " + to.getId() + " (" + to.getCenterX() + "," + to.getCenterY() + ")...");
//                    //List<Tile> route = r.route(from, to);
//
//                    List<Tile> route = bfr.search(from, to);
//                    if (route != null && !route.isEmpty()) {
//                        routes.add(route);
//
//                        for (Tile at : route) {
//                            at.setTrackColor(colors[col]);
//                            //?
//                            //this.tiles.put(at.getCenter(), at);
//                        }
//                        if (col < colors.length) {
//                            col++;
//                        } else {
//                            col = 0;
//                        }
//                    }
//                }
//            }
//        }

//        Logger.debug("Found " + routes.size() + " routes...");

        this.repaint();

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