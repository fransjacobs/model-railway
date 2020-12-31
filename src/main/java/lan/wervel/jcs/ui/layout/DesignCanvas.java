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
package lan.wervel.jcs.ui.layout;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import lan.wervel.jcs.entities.LayoutTile;
import lan.wervel.jcs.entities.LayoutTileGroup;
import lan.wervel.jcs.trackservice.TrackServiceFactory;
import lan.wervel.jcs.ui.layout.tiles.AbstractTile;
import lan.wervel.jcs.ui.layout.tiles.SensorTile;
import lan.wervel.jcs.ui.layout.tiles.BlockTile;
import lan.wervel.jcs.ui.layout.tiles.SignalTile;
import lan.wervel.jcs.ui.layout.tiles.TileFactory;
import lan.wervel.jcs.ui.layout.tiles.TurnoutTile;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import lan.wervel.jcs.ui.layout.tiles.enums.Orientation;
import lan.wervel.jcs.ui.layout.tiles.enums.Rotation;
import org.pmw.tinylog.Logger;

/**
 * This canvas / Panel is used to draw the layout
 *
 * @author frans
 */
public class DesignCanvas extends JPanel {

    public static final int GRID_SIZE = AbstractTile.MIN_GRID;

    private Mode mode;
    private Orientation orientation;
    private Direction direction;
    private TileType tileType;

    private boolean mouseInCanvas = false;

    private final Set<AbstractTile> tiles;

    private final List<SelectionListener> selectionListeners;

    private SelectionModeChangedListener selectionModeChangedListener;

    private final Set<AbstractTile> selectedTiles;
    private AbstractTile movingTile;

    private Point mouseLocation = new Point(0, 0);

    private BufferedImage grid;

    private final ExecutorService executor;

    /**
     * Creates new form GridsCanvas
     */
    public DesignCanvas() {
        this.tiles = new HashSet<>();
        this.selectedTiles = new HashSet();
        this.executor = Executors.newSingleThreadExecutor();
        this.selectionListeners = new ArrayList<>();
        //Default 
        this.mode = Mode.SELECT;
        this.orientation = Orientation.EAST;
        this.direction = Direction.CENTER;

        initComponents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        paintDotGrid(g);

        Set<AbstractTile> snapshot;
        synchronized (tiles) {
            snapshot = new HashSet<>(tiles);
        }

        Set<AbstractTile> selectedSnapshot;
        synchronized (selectedTiles) {
            selectedSnapshot = new HashSet<>(selectedTiles);
        }

        Set<LayoutTileGroup> selectedGroups = new HashSet<>();
        for (AbstractTile selectedTile : selectedSnapshot) {
            if (selectedTile != null && selectedTile.getLayoutTile() != null && selectedTile.getLayoutTile().getLayoutTileGroup() != null) {
                LayoutTileGroup ltg = selectedTile.getLayoutTile().getLayoutTileGroup();
                selectedGroups.add(ltg);
            }
        }

        Graphics2D g2 = (Graphics2D) g.create();

        //Logger.trace("Selected Tiles: " + selectedSnapshot.size() + " Selected Groups: " + selectedGroups.size());
        for (AbstractTile tile : snapshot) {
            if (tile != null) {
                if (tile.getLayoutTile() != null && tile.getLayoutTile().getLayoutTileGroup() != null) {
                    LayoutTileGroup ltg = tile.getLayoutTile().getLayoutTileGroup();
                    if (ltg.getAddress() != null && !ltg.getAddress().equals(0) && selectedGroups.contains(ltg)) {
                        //also need reset if the imgae todo...
                        tile.setTrackColor(Color.pink);
                    } else {
                        tile.setTrackColor(AbstractTile.DEFAULT_TRACK_COLOR);
                    }
                } else {
                    tile.setTrackColor(AbstractTile.DEFAULT_TRACK_COLOR);
                }

                tile.drawTile(g2);

                if (tile.getLayoutTile() != null && tile.getLayoutTile().getLayoutTileGroup() != null) {
                    LayoutTileGroup ltg = tile.getLayoutTile().getLayoutTileGroup();
                    if (selectedGroups.contains(ltg)) {
                        tile.drawCenterPoint(g2, Color.magenta, 6);
                    } else {
                        tile.drawCenterPoint(g2, Color.yellow, 3);
                    }
                } else {
                    tile.drawCenterPoint(g2, Color.yellow, 3);
                }
            }
        }

        for (AbstractTile tile : snapshot) {
            if (tile != null) {
                tile.drawName(g2);
            }
        }

        for (AbstractTile selectedTile : selectedSnapshot) {
            if (selectedTile != null) {
                selectedTile.drawBounds(g2);
                selectedTile.drawCenterPoint(g2, Color.red, 4);
            }
        }

        if (this.movingTile != null) {
            g2.setColor(Color.CYAN);
            g2.draw(this.movingTile.getBounds());

            g.setColor(Color.BLUE);
            movingTile.drawCenterPoint(g2, Color.blue);
        }
        g2.dispose();
    }

    private void paintDotGrid(Graphics g) {
        if (this.grid != null) {
            int pw = getSize().width;
            int ph = getSize().height;

            int gw = grid.getWidth();
            int gh = grid.getHeight();

            if (pw != gw || ph != gh) {
                Logger.trace("Changed Panel: " + pw + " x " + ph + " Grid: " + gw + " x " + gh);
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

    void resetDotGrid() {
        Logger.debug("reset dot grid");
        this.grid = null;
    }

    void addSelectionListener(SelectionListener listener) {
        this.selectionListeners.add(listener);
    }

    void setSelectionModeChangedListener(SelectionModeChangedListener listener) {
        selectionModeChangedListener = listener;
        if (selectionModeChangedListener != null) {
            this.selectionModeChangedListener.selectionModeChanged(mode, orientation, direction, tileType);
        }
    }

    void selectionModeChanged(Mode mode, Orientation orientation, Direction direction, TileType tileType) {
        Logger.debug("Mode: " + mode + " Orientation: " + orientation + " Direction: " + direction + " Tile: " + tileType);
        this.mode = mode;
        this.orientation = orientation;
        this.direction = direction;
        this.tileType = tileType;
        notifySelectionModeChange();
    }

    private void notifySelectionModeChange() {
        if (selectionModeChangedListener != null) {
            this.selectionModeChangedListener.selectionModeChanged(mode, orientation, direction, tileType);
        }
    }

    Mode getMode() {
        return mode;
    }

    void setMode(Mode mode) {
        this.mode = mode;
        notifySelectionModeChange();
    }

    TileType getTileType() {
        return tileType;
    }

    void setTileType(TileType elementType) {
        this.tileType = elementType;
        Logger.trace(elementType);
        notifySelectionModeChange();
    }

    Orientation getOrientation() {
        return orientation;
    }

    void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        notifySelectionModeChange();
    }

    void setDirection(Direction direction) {
        this.direction = direction;
        notifySelectionModeChange();
    }

    Direction getDirection() {
        return direction;
    }

    public void loadLayout() {
        if (TrackServiceFactory.getTrackService() == null) {
            return;
        }

        Set<LayoutTile> layoutTiles = TrackServiceFactory.getTrackService().getLayoutTiles();
        Logger.trace("Start loading " + layoutTiles.size() + " layoutTiles. Currently there are " + this.tiles.size() + " tiles...");

        Set<AbstractTile> snapshot = new HashSet<>();

        for (LayoutTile lt : layoutTiles) {
            AbstractTile t = TileFactory.createTile(lt);
            snapshot.add(t);
        }

        this.selectedTiles.clear();

        synchronized (tiles) {
            tiles.clear();
            tiles.addAll(snapshot);
        }

        Logger.debug("Loaded " + this.tiles.size() + " from " + layoutTiles.size() + " tiles...");
        //this.repaint();
        this.executor.execute(() -> repaint());

    }

    public void saveLayout() {
        this.executor.execute(() -> saveTiles());
    }

    private void saveTiles() {
        Logger.debug("Saving " + this.tiles.size() + " tiles...");

        Set<AbstractTile> snapshot;
        synchronized (tiles) {
            snapshot = new HashSet<>(tiles);
        }

        Set<LayoutTile> layoutTiles = new HashSet<>();

        for (AbstractTile tile : snapshot) {
            if (tile != null) {
                if (tile.getLayoutTile() == null) {
                    Logger.warn("Tile " + tile + " has no layout tile...");
                } else {
                    LayoutTile lt = tile.getLayoutTile();
                    Logger.trace("Saving " + tile + " -> " + lt);
                    layoutTiles.add(lt);
                }
            } else {
                Logger.warn("Tile is null?");
            }
        }

        TrackServiceFactory.getTrackService().persist(layoutTiles);
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
      Point p = AbstractTile.snapToGrid(evt.getPoint());

      AbstractTile tile = this.findTile(p);

      switch (this.mode) {
          case ADD:
              if (MouseEvent.BUTTON1 == evt.getButton()) {
                  this.selectedTiles.clear();
                  Logger.trace("Adding tile: " + this.tileType + " at X: " + evt.getX() + ", Y:" + evt.getY() + "...");
                  addTile(p);
              }
              if (MouseEvent.BUTTON3 == evt.getButton() && tile != null) {
                  showOperationsPopupMenu(tile, p.x, p.y);
              }

              break;
          default:
              if (tile != null) {
                  Logger.trace("selecting " + tile);
                  this.selectedTiles.clear();
                  this.selectedTiles.add(tile);
              } else {
                  this.selectedTiles.clear();
              }

              if (MouseEvent.BUTTON3 == evt.getButton() && tile != null) {
                  showOperationsPopupMenu(tile, p.x, p.y);
              }

              break;
      }

      this.executor.execute(() -> repaint());
      //this.executor.execute(() -> setDependentComponents());
      //notifySelectionListeners();
      this.executor.execute(() -> notifySelectionListeners());
      /////////////////////
      //this.repaint();

  }//GEN-LAST:event_formMouseClicked


  private void formMouseDragged(MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
      //Logger.trace("X: " + evt.getX() + " Y:" + evt.getY() + " button " + evt.getButton() + " " + evt.paramString());
      boolean positionChanged = false;
      Point p = AbstractTile.snapToGrid(evt.getX(), evt.getY());
      for (AbstractTile tile : selectedTiles) {
          Point tc = tile.getCenter();
          if (!tc.equals(p)) {
              tile.setCenter(p);
              positionChanged = true;
              synchronized (tiles) {
                  this.tiles.add(tile);
              }
              Logger.trace("Mode: " + mode + " Changed " + tile + " from: " + tc);

          }
      }

      if (positionChanged) {
          this.executor.execute(() -> repaint());
      }


  }//GEN-LAST:event_formMouseDragged

    private void showOperationsPopupMenu(AbstractTile tile, int x, int y) {
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
            case "BlockTile":
                showProperties = true;
                showRotate = true;
                showDelete = true;
                break;
            case "SignalTile":
                showProperties = true;
                showRotate = true;
                showDelete = true;
                break;
            case "TurnoutTile":
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
        this.propertiesMI.setVisible(showProperties);
        this.flipHorizontalMI.setVisible(showFlip);
        this.flipVerticalMI.setVisible(showFlip);
        this.rotateMI.setVisible(showRotate);
        this.moveMI.setVisible(showMove);
        this.deleteMI.setVisible(showDelete);
        this.operationsPM.show(this, x, y);
    }


  private void formMousePressed(MouseEvent evt) {//GEN-FIRST:event_formMousePressed
      Logger.trace("Press X: " + evt.getX() + " Y:" + evt.getY() + " button " + evt.getButton() + " " + evt.paramString());

      Point p = AbstractTile.snapToGrid(evt.getPoint());
      AbstractTile tile = this.findTile(p);

      if (MouseEvent.BUTTON3 == evt.getButton() && tile != null) {
          this.selectedTiles.clear();
          this.selectedTiles.add(tile);

          showOperationsPopupMenu(tile, p.x, p.y);
      }
  }//GEN-LAST:event_formMousePressed

  private void formMouseReleased(MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
      //Logger.trace("X: " + evt.getX() + " Y:" + evt.getY() + " button " + evt.getButton() + " " + evt.paramString());
  }//GEN-LAST:event_formMouseReleased

  private void formMouseExited(MouseEvent evt) {//GEN-FIRST:event_formMouseExited
      //Logger.trace("X: " + evt.getX() + " Y:" + evt.getY());
      this.mouseInCanvas = false;
      this.repaint();
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
      this.mouseInCanvas = true;
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

    private void notifySelectionListeners() {
        Set<AbstractTile> snapshot;
        if (selectedTiles.isEmpty()) {
            snapshot = Collections.emptySet();
        } else {
            synchronized (this.selectedTiles) {
                snapshot = new HashSet(selectedTiles);
            }
        }

        for (SelectionListener listener : this.selectionListeners) {
            listener.setSelectedLayoutTiles(snapshot);
        }
    }

    private AbstractTile findTile(Point cp) {
        AbstractTile result = null;
        for (AbstractTile tile : this.tiles) {
            if (tile.contains(cp)) {
                result = tile;
                break;
            }
        }
        return result;
    }

  private void formMouseMoved(MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
      //Only repaint when the mouse is snapped to the next grid
      Point sp = AbstractTile.snapToGrid(evt.getPoint());

      AbstractTile tile = findTile(sp);
      if (tile != null) {
          setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      } else {
          setCursor(Cursor.getDefaultCursor());
      }

      int x = evt.getX();
      int y = evt.getY();
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
        removeSelectedTile();
    }//GEN-LAST:event_deleteMIActionPerformed

    private void propertiesMIActionPerformed(ActionEvent evt) {//GEN-FIRST:event_propertiesMIActionPerformed
        editSelectedTileProperties();
    }//GEN-LAST:event_propertiesMIActionPerformed

    private void addTile(Point p) {
        if (this.orientation == null) {
            this.orientation = Orientation.EAST;
        }

        if (this.direction == null) {
            this.direction = Direction.RIGHT;
        }

        Logger.trace("Adding: " + tileType);

        AbstractTile tile = TileFactory.createTile(tileType, orientation, direction, p);

        if (tile != null) {
            AbstractTile t = tile;
            //this.executor.execute(() -> addTile(t));
            if (this.tiles.contains(tile)) {
                Logger.trace("A Tile " + tile.getClass().getSimpleName() + " exists; replacing it...");
            }

            tiles.add(tile);
            Logger.trace("Added Element " + tile.getClass().getSimpleName() + " " + tile.getOrientation() + " At " + tile.getCenter());

            selectedTiles.clear();
            selectedTiles.add(tile);

            Logger.trace("Added " + tile + " There are now " + this.tiles.size() + " tiles...");
        }
    }

    private void deleteTiles(Set<AbstractTile> toRemove) {
        synchronized (tiles) {
            for (AbstractTile tile : toRemove) {
                tiles.remove(tile);
                Logger.trace("Removed: " + tile);
                LayoutTile lt = TrackServiceFactory.getTrackService().getLayoutTile(tile.getCenterX(), tile.getCenterY());
                if (lt != null) {
                    TrackServiceFactory.getTrackService().remove(lt);
                    Logger.trace("Removed: " + lt);
                } else {
                    Logger.trace("Tile " + tile + " not found in repository...");
                }
            }
            this.selectedTiles.clear();
        }

        this.repaint();
    }

    void removeSelectedTile() {
        Set<AbstractTile> toRemove = new HashSet<>();
        for (AbstractTile tile : this.selectedTiles) {
            toRemove.add(tile);
            Logger.trace("Removing: " + tile);
        }

        this.executor.execute(() -> deleteTiles(toRemove));
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
        if (this.selectedTiles.size() > 0) {
            AbstractTile tile = this.selectedTiles.iterator().next();
            Logger.trace("Selected Tile: " + tile);
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
                case "FeedbackPort":
                    SensorDialog fbd = new SensorDialog(getParentFrame(), true, (SensorTile) tile);
                    fbd.setVisible(true);
                    break;
                case "OccupancyDetector":
                    OccupancySensorDialog osd = new OccupancySensorDialog(getParentFrame(), true, (BlockTile) tile);
                    osd.setVisible(true);
                    break;
                case "SignalTile":
                    SignalDialog sd = new SignalDialog(getParentFrame(), true, (SignalTile) tile);
                    sd.setVisible(true);
                    break;
                case "TurnoutTile":
                    TurnoutDialog td = new TurnoutDialog(getParentFrame(), true, (TurnoutTile) tile);
                    td.setVisible(true);
                    break;
                default:
                    break;
            }
        }
        this.executor.execute(() -> repaint());
        Logger.trace("Edit done");

    }

    public void rotateSelectedTile() {
        for (AbstractTile tile : selectedTiles) {
            tile.rotate();
            Logger.trace("Rotated " + tile);
            this.orientation = tile.getOrientation();
            this.direction = tile.getDirection();

            synchronized (tiles) {
                this.tiles.add(tile);
            }
        }
        //this.executor.execute(() -> notifySelectionModeChange());
        notifySelectionModeChange();
        this.executor.execute(() -> repaint());
    }

    public void flipSelectedTileHorizontal() {
        for (AbstractTile tile : selectedTiles) {
            Logger.trace("Flip H " + tile + " Orientation: " + tile.getOrientation() + " Direction: " + tile.getDirection());
            tile.flipHorizontal();
            Logger.trace("Flipped H " + tile + " Orientation: " + tile.getOrientation() + " Direction: " + tile.getDirection());

            this.orientation = tile.getOrientation();
            this.direction = tile.getDirection();
            synchronized (tiles) {
                this.tiles.add(tile);
            }
        }

        notifySelectionModeChange();
        this.executor.execute(() -> repaint());
    }

    public void flipSelectedTileVertical() {
        for (AbstractTile tile : selectedTiles) {
            Logger.trace("Flip H " + tile + " Orientation: " + tile.getOrientation() + " Direction: " + tile.getDirection());
            tile.flipVertical();
            Logger.trace("Flipped H " + tile + " Orientation: " + tile.getOrientation() + " Direction: " + tile.getDirection());

            this.orientation = tile.getOrientation();
            this.direction = tile.getDirection();
            synchronized (tiles) {
                this.tiles.add(tile);
            }
        }

        notifySelectionModeChange();
        this.executor.execute(() -> repaint());
    }

    public void moveSelectedTile() {
        if (!this.selectedTiles.isEmpty()) {
            AbstractTile selectedTile = selectedTiles.iterator().next();
            this.movingTile = selectedTile;
            selectedTiles.clear();
            this.repaint();
        }
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
    // End of variables declaration//GEN-END:variables
}
