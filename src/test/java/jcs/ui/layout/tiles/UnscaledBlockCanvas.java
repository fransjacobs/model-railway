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
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import org.tinylog.Logger;

/**
 * Inspired from https://stackoverflow.com/questions/6561246/scroll-event-of-a-jscrollpane
 */
public class UnscaledBlockCanvas extends javax.swing.JPanel implements PropertyChangeListener, Scrollable {

  private Tile block;

  private boolean showCenter;

  /**
   * Creates new form BlockCanvas
   */
  public UnscaledBlockCanvas() {
    initComponents();
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
  public Dimension getPreferredScrollableViewportSize() {
    Dimension pdim = this.getPreferredSize();
    return pdim;
  }

  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    //return 2;
    return (orientation == SwingConstants.VERTICAL) ? visibleRect.height / 10 : visibleRect.width / 10;
  }

  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    //return 2;
    return (orientation == SwingConstants.VERTICAL) ? visibleRect.height / 10 : visibleRect.width / 10;
  }

  @Override
  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  @Override
  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  @Override
  public Dimension getPreferredSize() {

    if (this.block != null) {
      int w = this.getWidth();
      int h = this.getHeight();
      boolean expand = !((AbstractTile) block).isScaleImage();

      int bw = block.getWidth() * (expand ? 10 : 1);
      int bh = block.getHeight() * (expand ? 10 : 1);

      Logger.trace("Canvas Size w: " + w + " h: " + h + " Block Size w: " + bw + " h: " + bh);

      return new Dimension(bw, bh);

    } else {
      int w = this.getWidth();
      int h = this.getHeight();

      return new Dimension(this.getSize().width, this.getSize().height);
      //return new Dimension(w, h);

    }

  }

//  public void paint(Graphics g) {
  //https://docs.oracle.com/javase/tutorial/uiswing/components/scrollpane.html#api
  //https://github.com/tips4java/tips4java/blob/main/source/ScrollablePanel.java
//  https://forums.codeguru.com/showthread.php?33008-Canvas-and-JScrollPane
//    int iWidth = this.getSize().width;
//    int iHeight = this.getSize().height;
//    _drawSpace = createImage(this.getSize().width, this.getSize().height);
//    _drawSpaceG = _drawSpace.getGraphics();
//    _drawSpaceG.setColor(Color.black);
//
//    _drawSpaceG.fillRect(0, 0, iWidth, iHeight);
//
//    _drawSpaceG.setColor(Color.white);
//
//    _drawSpaceG.drawLine(0, 0, 1000, 1000);
//    _drawSpaceG.drawLine(0, 1000, 500, 0);
//
//    g.drawImage(_drawSpace, 0, 0, Color.black, this);
//  }

  @Override
  protected void paintComponent(Graphics g) {
//    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//    int sw = (int) dim.getWidth();
//    int sh = (int) dim.getHeight();
//
//    Dimension pdim = this.getSize();

    //Logger.trace("Canvas w: " + w + " h: " + h + " Block w: " + bw + " h: " + bh + "," + expand);
    //Logger.trace("Screen: W: " + sw + " H: " + sh + " Panel w: " + w + " h: " + h + " Size W: " + pdim.width + " H: " + pdim.height);
    int w = this.getWidth();
    int h = this.getHeight();

    if (this.block != null) {
      boolean expand = !((AbstractTile) block).isScaleImage();

      int bw = block.getWidth() * (expand ? 10 : 1);
      int bh = block.getHeight() * (expand ? 10 : 1);

      //Logger.trace("Canvas Size w: " + w + " h: " + h + " Block Size w: " + bw + " h: " + bh);
      ///Dimension d = new Dimension(w, h);
      if (w < bw) {
        Logger.trace("W too small " + w + " -> " + bw);
        w = bw + 40;
        Dimension d = new Dimension(w, h);
        //this.setPreferredSize(d);
      }
      if (h < bh) {
        Logger.trace("H too small " + h + " -> " + bh);
        h = bh + 40;
        Dimension d = new Dimension(w, h);
        //this.setPreferredSize(d);
      }

      //Logger.trace("Updated Canvas Size w: " + w + " h: " + h + " Block Size w: " + bw + " h: " + bh);
      int x = w / 2;
      int y = h / 2;
      Point center = new Point(x, y);
      block.setCenter(center);

      this.scrollRectToVisible(block.getBounds());
      Logger.trace("Center: (" + x + "," + y + ")");

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

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    setMinimumSize(new java.awt.Dimension(1240, 450));
    setPreferredSize(new java.awt.Dimension(1240, 450));
    setLayout(null);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
}
