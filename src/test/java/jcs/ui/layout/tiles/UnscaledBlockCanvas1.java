/*
 * Copyright 2024 Frans Jacobs
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
package jcs.ui.layout.tiles;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import org.tinylog.Logger;

public class UnscaledBlockCanvas1 extends JComponent implements PropertyChangeListener, Scrollable, SwingConstants {

  private Tile block;
  private boolean showCenter;

  /**
   * Creates new form BlockCanvas
   */
  public UnscaledBlockCanvas1() {
    this.setSize(1240, 440);
  }

  public Tile getBlock() {
    return block;
  }

  public void setBlock(Tile block) {
    this.block = block;
  }

  public boolean isShowCenter() {
    return showCenter;
  }

  public void setShowCenter(boolean showCenter) {
    this.showCenter = showCenter;
  }

  @Override
  public Dimension getPreferredSize() {
    int w, h;
    if (getParent() instanceof JViewport jViewport) {
      w = jViewport.getWidth();
      h = jViewport.getHeight();
      Logger.trace("Viewport Size w: " + w + " h: " + h);
    } else {
      w = this.getSize().width;
      h = this.getSize().height;
      Logger.trace("Size w: " + w + " h: " + h);
    }

    if (this.block != null) {
      boolean expand = !((AbstractTile) block).isScaleImage();
      int bw = block.getWidth() * (expand ? 10 : 1);
      int bh = block.getHeight() * (expand ? 10 : 1);
      Logger.trace("Min Canvas Size w: " + w + " h: " + h + " Block Size w: " + bw + " h: " + bh);

      if (bw > w || bh > h) {
        if (bw > w) {
          w = bw;
        }
        if (bh > h) {
          h = bh;
        }
        Dimension newDimension = new Dimension(w, h);
        Logger.trace("Resize needed to w: " + newDimension.width + " h: " + newDimension.height);
        //revalidate();
        return newDimension;
      } else {
        return new Dimension(w, h);
      }
    } else {
      return new Dimension(w, h);
    }
  }

  @Override
  public void paint(Graphics g) {
    int w, h;
    if (getParent() instanceof JViewport jViewport) {
      w = jViewport.getWidth();
      h = jViewport.getHeight();
      Logger.trace("Viewport Size w: " + w + " h: " + h);
    } else {
      w = this.getSize().width;
      h = this.getSize().height;
      Logger.trace("Size w: " + w + " h: " + h);
    }

    int x = w / 2;
    int y = h / 2;
    Point canvasCenter = new Point(x, y);
    Logger.trace("Canvas Center: (" + x + "," + y + ")");

    if (this.block != null) {
      boolean expand = !((AbstractTile) block).isScaleImage();

      int bw = block.getWidth() * (expand ? 10 : 1);
      int bh = block.getHeight() * (expand ? 10 : 1);
      
      if (bw > w || bh > h) {
        if (bw > w) {
          w = bw;
        }
        if (bh > h) {
          h = bh;
        }
        Dimension newDimension = new Dimension(w, h);
        Logger.trace("Resize needed to w: " + newDimension.width + " h: " + newDimension.height);
        this.setSize(newDimension);
        x = w / 2;
        y = h / 2;
        canvasCenter = new Point(x, y);
        Logger.trace("Canvas Center: (" + x + "," + y + ")");
        block.setCenter(canvasCenter);
        revalidate();
      } else {
        block.setCenter(canvasCenter);
      }  

      scrollRectToVisible(block.getBounds());
      Graphics2D g2d = (Graphics2D) g;
      block.drawTile(g2d, true);

      if (this.showCenter) {
        block.drawBounds(g2d);
        block.drawCenterPoint(g2d, Color.red);
      }
    }
  }

  
  
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("repaintTile".equals(evt.getPropertyName())) {
      Tile tile = (Tile) evt.getNewValue();
      this.repaint(tile.getBounds());
    }
  }

  @Override
  public Dimension getPreferredScrollableViewportSize() {
    Logger.trace(this.getPreferredSize());
    return getPreferredSize();
  }

  public int getScrollableUnitIncrement() {
    Logger.trace(Tile.GRID);
    return Tile.GRID;
  }

  public int getScrollableBlockIncrement() {
    Logger.trace(Tile.GRID);
    return Tile.GRID;
  }

  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    Logger.trace("visibleRect: "+visibleRect.toString()+" orientation: "+orientation+" direction"+direction);
    return Tile.GRID;
  }

  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    Logger.trace("visibleRect: "+visibleRect.toString()+" orientation: "+orientation+" direction"+direction);
    return Tile.GRID;
  }

  @Override
  public boolean getScrollableTracksViewportWidth() {
    int w;
    if (getParent() instanceof JViewport jViewport) {
      w = jViewport.getWidth();
      Logger.trace("Viewport w: " + w);
    } else {
      w = this.getSize().width;
      Logger.trace("Size w: " + w);
    }

    if (this.block != null) {
      boolean expand = !((AbstractTile) block).isScaleImage();
      int bw = block.getWidth() * (expand ? 10 : 1);
      Logger.trace("Min Canvas w: " + w + " Block w: " + bw+ "; "+(bw > w));
      return bw > w;
    } else {
      return false;
    }
  }

  @Override
  public boolean getScrollableTracksViewportHeight() {
    int h;
    if (getParent() instanceof JViewport jViewport) {
      h = jViewport.getHeight();
      Logger.trace("Viewport h: " + h);
    } else {
      h = this.getSize().height;
      Logger.trace("Size h: " + h);
    }

    if (this.block != null) {
      boolean expand = !((AbstractTile) block).isScaleImage();
      int bh = block.getHeight() * (expand ? 10 : 1);
      Logger.trace("Min Canvas h: " + h + " Block h: " + bh+"; "+(bh > h));

      return bh > h;
    } else {
      return false;
    }
  }

}
