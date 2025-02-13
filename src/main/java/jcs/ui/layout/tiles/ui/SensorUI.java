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
package jcs.ui.layout.tiles.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.SensorBean;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import jcs.ui.layout.tiles.TileModel;

public class SensorUI extends StraightUI implements MouseListener, MouseMotionListener {

  public SensorUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new SensorUI();
  }

  @Override
  public void installUI(JComponent c) {
    Tile tile = (Tile) c;
    tile.addMouseListener(this);
    tile.addMouseMotionListener(this);
  }

  @Override
  public void uninstallUI(JComponent c) {
    Tile tile = (Tile) c;
    tile.removeMouseListener(this);
    tile.removeMouseMotionListener(this);
  }

  private void renderSensor(Graphics2D g2, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();

    int xx = RENDER_GRID - 75;
    int yy = RENDER_GRID - 75;

    Point cp = new Point(xx, yy);
    float radius = 300;
    float[] dist = {0.0f, 0.6f};

    if (model.isSensorActive()) {
      Color[] colors = {Color.red.brighter(), Color.red.darker()};
      RadialGradientPaint foreground = new RadialGradientPaint(cp, radius, dist, colors, MultipleGradientPaint.CycleMethod.REFLECT);
      g2.setPaint(foreground);
    } else {
      Color[] colors = {Color.green.darker(), Color.green.brighter()};
      RadialGradientPaint foreground = new RadialGradientPaint(cp, radius, dist, colors, MultipleGradientPaint.CycleMethod.REFLECT);
      g2.setPaint(foreground);
    }

    g2.fill(new Ellipse2D.Double(xx, yy, 0.5f * radius, 0.5f * radius));
  }

  @Override
  public void renderTile(Graphics2D g2, JComponent c) {
    renderStraight(g2, c);
    renderSensor(g2, c);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    //Logger.trace("Mouse button " + e.getButton() + " @ (" + e.getXOnScreen() + "," + e.getYOnScreen() + ")");
    //Only JCS is in CONTROL mode (readonly) activate sensor action events. 
    if (isControlMode((Component) e.getSource()) && e.getButton() == MouseEvent.BUTTON1) {
      Tile tile = (Tile) e.getSource();
      tile.setActive(!tile.isActive());
      if (tile.getSensorBean() != null) {
        SensorBean sb = tile.getSensorBean();
        sb.setLastUpdated(new Date());
        SensorEvent sae = new SensorEvent(sb);
        TileCache.enqueTileAction(sae);
        //Logger.trace("Changing Tile "+tile.getId()+" Sensor "+sb.getId()+" to "+sb.isActive()+"...");
      }
    } else {
      redispatchToParent(e);
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    //Logger.trace("Mouse button " + e.getButton() + " @ (" + e.getXOnScreen() + "," + e.getYOnScreen() + ")");
    redispatchToParent(e);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    //Logger.trace("Mouse button " + e.getButton() + " @ (" + e.getXOnScreen() + "," + e.getYOnScreen());
    redispatchToParent(e);
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    //Logger.trace("Mouse button " + e.getButton() + " @ (" + e.getXOnScreen() + ",");
    Tile tile = (Tile) e.getSource();
    String toolTipText = tile.getId();
    if (tile.getSensorBean() != null) {
      toolTipText = toolTipText + "; Id: " + tile.getSensorBean().getId();
    }
    tile.setToolTipText(toolTipText);

    redispatchToParent(e);
  }

  @Override
  public void mouseExited(MouseEvent e) {
    //Logger.trace("Mouse button " + e.getButton() + " @ (" + e.getXOnScreen() + ",");
    Tile tile = (Tile) e.getSource();
    tile.setToolTipText(null);
    redispatchToParent(e);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    //Logger.trace("Mouse button " + e.getButton() + " @ (" + e.getXOnScreen() + "," + e.getYOnScreen());
    redispatchToParent(e);
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    //Logger.trace("Mouse button " + e.getButton() + " @ (" + e.getXOnScreen() + "," + e.getYOnScreen());
    redispatchToParent(e);
  }

}
