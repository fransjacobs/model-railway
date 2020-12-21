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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import lan.wervel.jcs.entities.LayoutTile;
import lan.wervel.jcs.entities.Signal;
import lan.wervel.jcs.entities.Turnout;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.SignalValue;
import lan.wervel.jcs.trackservice.TrackServiceFactory;
import lan.wervel.jcs.trackservice.events.AccessoryListener;
import lan.wervel.jcs.ui.layout.tiles.AbstractTile;
import lan.wervel.jcs.ui.layout.tiles.FeedbackPort;
import lan.wervel.jcs.ui.layout.tiles.OccupancyDetector;
import lan.wervel.jcs.ui.layout.tiles.SignalTile;
import lan.wervel.jcs.ui.layout.tiles.TurnoutTile;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;
import lan.wervel.jcs.trackservice.events.SensorListener;

/**
 * This panel is used to show the layout and be able use the layout as switch
 * board and monitor the tracks
 *
 * @author frans
 */
public class DisplayCanvas extends JPanel implements ReDrawListener {

    private final Set<AbstractTile> tiles;
    private AbstractTile selectedTile;
    private final ExecutorService executor;

    /**
     * Creates new form GridsCanvas
     */
    public DisplayCanvas() {
        this.tiles = new HashSet<>();
        this.executor = Executors.newSingleThreadExecutor();
        initComponents();
        loadLayout();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, this.getWidth(), this.getHeight());

        Set<AbstractTile> snapshot;
        synchronized (tiles) {
            snapshot = new HashSet<>(tiles);
        }

        for (AbstractTile tile : snapshot) {
            tile.drawTile(g2d);

            if(tile instanceof FeedbackPort || tile instanceof OccupancyDetector) {
                tile.setDrawName(false);
            } 
            
            if (!tile.equals(this.selectedTile)) {
                tile.setDrawName(true);
                if (tile instanceof SignalTile | tile instanceof TurnoutTile) {
                    tile.drawCenterPoint(g2d);
                }
            }
            
            if(tile instanceof FeedbackPort || tile instanceof OccupancyDetector) {
                tile.setDrawName(false);
            } 
            
        }

        for (AbstractTile tile : snapshot) {
            tile.drawName(g2d);
        }

        if (this.selectedTile != null) {
            selectedTile.drawCenterPoint(g2d);
            selectedTile.drawBounds(g2d);
        }
        g2d.dispose();
    }

    public final void loadLayout() {
        if (TrackServiceFactory.getTrackService() == null) {
            return;
        }

        Set<AbstractTile> snapshot;
        synchronized (this.tiles) {
            snapshot = new HashSet<>(tiles);
        }

        //Logger.debug("Removing listeners from " + snapshot.size() + " Tiles...");
        //Unregister listeners...
        for (AbstractTile t : snapshot) {
            if (t instanceof SensorListener) {
                SensorListener fbpl = (SensorListener) t;
                if (fbpl.getContactId() != null && fbpl.getContactId() != null) {
                    TrackServiceFactory.getTrackService().removeFeedbackPortListener(fbpl);
                }
            }

            if (t instanceof AccessoryListener) {
                AccessoryListener al = (AccessoryListener) t;
                TrackServiceFactory.getTrackService().removeAccessoiryListener(al);
            }
        }

        Set<LayoutTile> lts = new HashSet<>(TrackServiceFactory.getTrackService().getLayoutTiles());

        snapshot.clear();
        Logger.debug("Start loading " + lts.size() + " layoutTiles...");

        //Determine the size of the drawing during load
        //start with the min X, max X then min Y and max Y
        int minX = Integer.MAX_VALUE, maxX = 0, minY = Integer.MAX_VALUE, maxY = 0;

        for (LayoutTile lt : lts) {
            AbstractTile t = AbstractTile.createTile(lt);
            if (t != null) {
                snapshot.add(t);

                int w = t.getWidth();
                int h = t.getHeight();

                int tx = t.getCenterX() - w / 2;
                int ly = t.getCenterY() - h / 2;
                int bx = t.getCenterX() + w / 2;
                int ry = t.getCenterY() + h / 2;

                if (minX > tx) {
                    minX = tx;
                    //Logger.trace("New minX: " + minX);
                }
                if (maxX < bx) {
                    maxX = bx;
                    //Logger.trace("New maxX: " + maxX);
                }
                if (minY > ly) {
                    minY = ly;
                    //Logger.trace("New minY: " + minY);
                }
                if (maxY < ry) {
                    maxY = ry;
                    //Logger.trace("New maxY: " + maxY);
                }
            }
        }

        int width = maxX - minX + minX + AbstractTile.MIN_GRID;
        int height = maxY - minY + minY + AbstractTile.MIN_GRID;

        Dimension d = new Dimension(width, height);
        this.setMinimumSize(d);
        this.setPreferredSize(d);

        Logger.debug("Loaded " + snapshot.size() + " Tiles from " + lts.size() + " LayoutTiles...");

        //Register listeners
        for (AbstractTile t : snapshot) {
            if (t instanceof SensorListener) {
                SensorListener fbpl = (SensorListener) t;
                if (fbpl.getContactId() != null && fbpl.getContactId() != null) {
                    TrackServiceFactory.getTrackService().addSensorListener(fbpl);
                    t.setReDrawListener(this);
                }
            }

            if (t instanceof AccessoryListener) {
                AccessoryListener al = (AccessoryListener) t;
                TrackServiceFactory.getTrackService().addAccessoiryListener(al);
                t.setReDrawListener(this);
            }
        }

        synchronized (this.tiles) {
            this.tiles.clear();
            this.tiles.addAll(snapshot);
        }

        //first initialization, the listeners need to be triggered once..
        TrackServiceFactory.getTrackService().notifyAllAccessoiryListeners();
        TrackServiceFactory.getTrackService().notifyAllSensorListeners();

        this.selectedTile = null;
        this.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    signal4PopupMenu = new JPopupMenu();
    s4hp0 = new JMenuItem();
    s4hp1 = new JMenuItem();
    s4hp2 = new JMenuItem();
    s4hp0Sh1 = new JMenuItem();
    signal2PopupMenu = new JPopupMenu();
    s2hp0 = new JMenuItem();
    s2hp1 = new JMenuItem();
    turnoutRightPopupMenu = new JPopupMenu();
    turnoutRStraight = new JMenuItem();
    turnoutRCurved = new JMenuItem();
    turnoutLeftPopupMenu = new JPopupMenu();
    turnoutLStraight = new JMenuItem();
    turnoutLCurved = new JMenuItem();

    s4hp0.setIcon(new ImageIcon(getClass().getResource("/media/signal4-Hp0.png"))); // NOI18N
    s4hp0.setToolTipText("Hp0");
    s4hp0.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        s4hp0ActionPerformed(evt);
      }
    });
    signal4PopupMenu.add(s4hp0);

    s4hp1.setIcon(new ImageIcon(getClass().getResource("/media/signal4-Hp1.png"))); // NOI18N
    s4hp1.setToolTipText("Hp1");
    s4hp1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        s4hp1ActionPerformed(evt);
      }
    });
    signal4PopupMenu.add(s4hp1);

    s4hp2.setIcon(new ImageIcon(getClass().getResource("/media/signal4-Hp2.png"))); // NOI18N
    s4hp2.setToolTipText("Hp2");
    s4hp2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        s4hp2ActionPerformed(evt);
      }
    });
    signal4PopupMenu.add(s4hp2);

    s4hp0Sh1.setIcon(new ImageIcon(getClass().getResource("/media/signal4-Hp0Sh1.png"))); // NOI18N
    s4hp0Sh1.setToolTipText("Hp0Sh1");
    s4hp0Sh1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        s4hp0Sh1ActionPerformed(evt);
      }
    });
    signal4PopupMenu.add(s4hp0Sh1);

    s2hp0.setIcon(new ImageIcon(getClass().getResource("/media/signal2-Hp0.png"))); // NOI18N
    s2hp0.setToolTipText("Hp0");
    s2hp0.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        s2hp0ActionPerformed(evt);
      }
    });
    signal2PopupMenu.add(s2hp0);

    s2hp1.setIcon(new ImageIcon(getClass().getResource("/media/signal2-Hp1.png"))); // NOI18N
    s2hp1.setToolTipText("Hp1");
    s2hp1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        s2hp1ActionPerformed(evt);
      }
    });
    signal2PopupMenu.add(s2hp1);

    turnoutRStraight.setIcon(new ImageIcon(getClass().getResource("/media/turnout-r-s.png"))); // NOI18N
    turnoutRStraight.setToolTipText("Hp0");
    turnoutRStraight.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        turnoutRStraightActionPerformed(evt);
      }
    });
    turnoutRightPopupMenu.add(turnoutRStraight);

    turnoutRCurved.setIcon(new ImageIcon(getClass().getResource("/media/turnout-r-c.png"))); // NOI18N
    turnoutRCurved.setToolTipText("Hp1");
    turnoutRCurved.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        turnoutRCurvedActionPerformed(evt);
      }
    });
    turnoutRightPopupMenu.add(turnoutRCurved);

    turnoutLStraight.setIcon(new ImageIcon(getClass().getResource("/media/turnout-l-s.png"))); // NOI18N
    turnoutLStraight.setToolTipText("Hp0");
    turnoutLStraight.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        turnoutLStraightActionPerformed(evt);
      }
    });
    turnoutLeftPopupMenu.add(turnoutLStraight);

    turnoutLCurved.setIcon(new ImageIcon(getClass().getResource("/media/turnout-l-c.png"))); // NOI18N
    turnoutLCurved.setToolTipText("Hp1");
    turnoutLCurved.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        turnoutLCurvedActionPerformed(evt);
      }
    });
    turnoutLeftPopupMenu.add(turnoutLCurved);

    setToolTipText("");
    setMinimumSize(new Dimension(520, 1080));
    setOpaque(false);
    setPreferredSize(new Dimension(520, 1080));
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        formMouseClicked(evt);
      }
    });
    setLayout(new GridBagLayout());
  }// </editor-fold>//GEN-END:initComponents

    private void showSignalPopup(SignalTile st, MouseEvent evt) {
        Signal signal = (Signal) st.getLayoutTile().getSolenoidAccessoiry();
        if (signal != null) {
            if (signal.getLightImages() > 2) {
                this.signal4PopupMenu.show(this, evt.getX(), evt.getY());
            } else {
                this.signal2PopupMenu.show(this, evt.getX(), evt.getY());
            }
        }
    }

    private void setTurnoutValue(AccessoryValue value) {
        Turnout turnout = (Turnout) this.selectedTile.getLayoutTile().getSolenoidAccessoiry();
        turnout.setValue(value);

        this.executor.execute(() -> TrackServiceFactory.getTrackService().switchAccessory(turnout.getValue(), turnout));
    }

    private void setSignalValue(SignalValue signalValue) {
        Signal signal = (Signal) this.selectedTile.getLayoutTile().getSolenoidAccessoiry();
        signal.setSignalValue(signalValue);

        this.executor.execute(() -> TrackServiceFactory.getTrackService().switchAccessory(signal.getValue(), signal));
    }

  private void formMouseClicked(MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
      Logger.trace("X: " + evt.getX() + " Y:" + evt.getY() + " button " + evt.getButton() + " " + evt.paramString());

      Point p = AbstractTile.snapToGrid(evt.getPoint());

      Logger.trace("Selecting point at X: " + evt.getX() + ", Y:" + evt.getY() + " -> " + p + "...");
      selectedTile = selectTile(p);

      if (this.selectedTile instanceof SignalTile) {
          showSignalPopup((SignalTile) selectedTile, evt);
      }

      if (this.selectedTile instanceof TurnoutTile) {
          if (Direction.LEFT.equals(selectedTile.getDirection())) {
              this.turnoutLeftPopupMenu.show(this, evt.getX(), evt.getY());
          } else {
              this.turnoutRightPopupMenu.show(this, evt.getX(), evt.getY());
          }
      }
  }//GEN-LAST:event_formMouseClicked

  private void s4hp0ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_s4hp0ActionPerformed
      setSignalValue(SignalValue.Hp0);
  }//GEN-LAST:event_s4hp0ActionPerformed

  private void s4hp1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_s4hp1ActionPerformed
      setSignalValue(SignalValue.Hp1);
  }//GEN-LAST:event_s4hp1ActionPerformed

  private void s4hp2ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_s4hp2ActionPerformed
      setSignalValue(SignalValue.Hp2);
  }//GEN-LAST:event_s4hp2ActionPerformed

  private void s4hp0Sh1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_s4hp0Sh1ActionPerformed
      setSignalValue(SignalValue.Hp0Sh1);
  }//GEN-LAST:event_s4hp0Sh1ActionPerformed

  private void s2hp0ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_s2hp0ActionPerformed
      setSignalValue(SignalValue.Hp0);
  }//GEN-LAST:event_s2hp0ActionPerformed

  private void s2hp1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_s2hp1ActionPerformed
      setSignalValue(SignalValue.Hp1);
  }//GEN-LAST:event_s2hp1ActionPerformed

  private void turnoutRStraightActionPerformed(ActionEvent evt) {//GEN-FIRST:event_turnoutRStraightActionPerformed
      setTurnoutValue(AccessoryValue.GREEN);
  }//GEN-LAST:event_turnoutRStraightActionPerformed

  private void turnoutRCurvedActionPerformed(ActionEvent evt) {//GEN-FIRST:event_turnoutRCurvedActionPerformed
      setTurnoutValue(AccessoryValue.RED);
  }//GEN-LAST:event_turnoutRCurvedActionPerformed

  private void turnoutLStraightActionPerformed(ActionEvent evt) {//GEN-FIRST:event_turnoutLStraightActionPerformed
      setTurnoutValue(AccessoryValue.GREEN);
  }//GEN-LAST:event_turnoutLStraightActionPerformed

  private void turnoutLCurvedActionPerformed(ActionEvent evt) {//GEN-FIRST:event_turnoutLCurvedActionPerformed
      setTurnoutValue(AccessoryValue.RED);
  }//GEN-LAST:event_turnoutLCurvedActionPerformed

    private AbstractTile selectTile(Point p) {
        Logger.trace("Selecting tile @ (" + p.x + "," + p.y + ")...");
        AbstractTile toSelect = null;

        Set<AbstractTile> snapshot;
        synchronized (tiles) {
            snapshot = new HashSet<>(tiles);
        }

        for (AbstractTile e : snapshot) {
            if (e.getCenter().equals(p) && ((e instanceof SignalTile) || (e instanceof TurnoutTile))) {
                toSelect = e;
                Logger.trace("Direct search found Tile " + toSelect + "...");
                break;
            }
        }

        if (toSelect != null) {
            Logger.debug("Found tile " + toSelect.getClass().getSimpleName() + " @ " + toSelect.getCenter() + "...");
        } else {
            Logger.trace("No tile Found @ " + p + "...");
        }
        return toSelect;
    }

    @Override
    public void reDraw() {
        this.repaint();
    }

    public static void main(String args[]) {
        Configurator.defaultConfig().level(org.pmw.tinylog.Level.DEBUG).activate();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.error(ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            JFrame f = new JFrame("DisplayCanvas Tester");
            DisplayCanvas displayCanvas = new DisplayCanvas();
            f.add(displayCanvas);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setVisible(true);
        });
    }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JMenuItem s2hp0;
  private JMenuItem s2hp1;
  private JMenuItem s4hp0;
  private JMenuItem s4hp0Sh1;
  private JMenuItem s4hp1;
  private JMenuItem s4hp2;
  private JPopupMenu signal2PopupMenu;
  private JPopupMenu signal4PopupMenu;
  private JMenuItem turnoutLCurved;
  private JMenuItem turnoutLStraight;
  private JPopupMenu turnoutLeftPopupMenu;
  private JMenuItem turnoutRCurved;
  private JMenuItem turnoutRStraight;
  private JPopupMenu turnoutRightPopupMenu;
  // End of variables declaration//GEN-END:variables
}
