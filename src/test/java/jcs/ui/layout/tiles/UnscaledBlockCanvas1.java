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

/**
 * jcs.ui.layout.tiles.UnscaledBlockCanvas1 Inspired on https://stackoverflow.com/questions/6561246/scroll-event-of-a-jscrollpane and
 * https://forums.codeguru.com/showthread.php?33008-Canvas-and-JScrollPane
 */
public class UnscaledBlockCanvas1 extends JComponent implements PropertyChangeListener, Scrollable, SwingConstants {

  private Tile block;

  private boolean showCenter;

  public enum ScrollableSizeHint {
    NONE,
    FIT,
    STRETCH;
  }

  public enum IncrementType {
    PERCENT,
    PIXELS;
  }

  private ScrollableSizeHint scrollableHeight = ScrollableSizeHint.NONE;
  private ScrollableSizeHint scrollableWidth = ScrollableSizeHint.NONE;

  private IncrementInfo horizontalBlock;
  private IncrementInfo horizontalUnit;
  private IncrementInfo verticalBlock;
  private IncrementInfo verticalUnit;

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
  public void paint(Graphics g) {
    if (this.block != null) {
      boolean expand = !((AbstractTile) block).isScaleImage();

      int bw = block.getWidth() * (expand ? 10 : 1);
      int bh = block.getHeight() * (expand ? 10 : 1);

      int w = this.getSize().width;
      int h = this.getSize().height;

      Logger.trace("Canvas Size w: " + w + " h: " + h + " Block Size w: " + bw + " h: " + bh);
      ///Dimension d = new Dimension(w, h);

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
   * Get the height ScrollableSizeHint enum
   *
   * @return the ScrollableSizeHint enum for the height
   */
  public ScrollableSizeHint getScrollableHeight() {
    return scrollableHeight;
  }

  /**
   * Set the ScrollableSizeHint enum for the height. The enum is used to determine the boolean value that is returned by the getScrollableTracksViewportHeight() method. The valid values are:
   *
   * ScrollableSizeHint.NONE - return "false", which causes the height of the panel to be used when laying out the children ScrollableSizeHint.FIT - return "true", which causes the height of the
   * viewport to be used when laying out the children ScrollableSizeHint.STRETCH - return "true" when the viewport height is greater than the height of the panel, "false" otherwise.
   *
   * @param scrollableHeight as represented by the ScrollableSizeHint enum.
   */
  public void setScrollableHeight(ScrollableSizeHint scrollableHeight) {
    this.scrollableHeight = scrollableHeight;
    revalidate();
  }

  /**
   * Get the width ScrollableSizeHint enum
   *
   * @return the ScrollableSizeHint enum for the width
   */
  public ScrollableSizeHint getScrollableWidth() {
    return scrollableWidth;
  }

  /**
   * Set the ScrollableSizeHint enum for the width. The enum is used to determine the boolean value that is returned by the getScrollableTracksViewportWidth() method. The valid values are:
   *
   * ScrollableSizeHint.NONE - return "false", which causes the width of the panel to be used when laying out the children ScrollableSizeHint.FIT - return "true", which causes the width of the
   * viewport to be used when laying out the children ScrollableSizeHint.STRETCH - return "true" when the viewport width is greater than the width of the panel, "false" otherwise.
   *
   * @param scrollableWidth as represented by the ScrollableSizeHint enum.
   */
  public void setScrollableWidth(ScrollableSizeHint scrollableWidth) {
    this.scrollableWidth = scrollableWidth;
    revalidate();
  }

  /**
   * Get the block IncrementInfo for the specified orientation
   *
   * @return the block IncrementInfo for the specified orientation
   */
  public IncrementInfo getScrollableBlockIncrement(int orientation) {
    return orientation == SwingConstants.HORIZONTAL ? horizontalBlock : verticalBlock;
  }

  /**
   * Specify the information needed to do block scrolling.
   *
   * @param orientation specify the scrolling orientation. Must be either: SwingContants.HORIZONTAL or SwingContants.VERTICAL.
   * @paran type specify how the amount parameter in the calculation of the scrollable amount. Valid values are: IncrementType.PERCENT - treat the amount as a % of the viewport size
   * IncrementType.PIXEL - treat the amount as the scrollable amount
   * @param amount a value used with the IncrementType to determine the scrollable amount
   */
  public void setScrollableBlockIncrement(int orientation, IncrementType type, int amount) {
    IncrementInfo info = new IncrementInfo(type, amount);
    setScrollableBlockIncrement(orientation, info);
  }

  /**
   * Specify the information needed to do block scrolling.
   *
   * @param orientation specify the scrolling orientation. Must be either: SwingContants.HORIZONTAL or SwingContants.VERTICAL.
   * @param info An IncrementInfo object containing information of how to calculate the scrollable amount.
   */
  public void setScrollableBlockIncrement(int orientation, IncrementInfo info) {
    switch (orientation) {
      case SwingConstants.HORIZONTAL:
        horizontalBlock = info;
        break;
      case SwingConstants.VERTICAL:
        verticalBlock = info;
        break;
      default:
        throw new IllegalArgumentException("Invalid orientation: " + orientation);
    }
  }

  /**
   * Get the unit IncrementInfo for the specified orientation
   *
   * @return the unit IncrementInfo for the specified orientation
   */
  public IncrementInfo getScrollableUnitIncrement(int orientation) {
    return orientation == SwingConstants.HORIZONTAL ? horizontalUnit : verticalUnit;
  }

  /**
   * Specify the information needed to do unit scrolling.
   *
   * @param orientation specify the scrolling orientation. Must be either: SwingContants.HORIZONTAL or SwingContants.VERTICAL.
   * @paran type specify how the amount parameter in the calculation of the scrollable amount. Valid values are: IncrementType.PERCENT - treat the amount as a % of the viewport size
   * IncrementType.PIXEL - treat the amount as the scrollable amount
   * @param amount a value used with the IncrementType to determine the scrollable amount
   */
  public void setScrollableUnitIncrement(int orientation, IncrementType type, int amount) {
    IncrementInfo info = new IncrementInfo(type, amount);
    setScrollableUnitIncrement(orientation, info);
  }

  /**
   * Specify the information needed to do unit scrolling.
   *
   * @param orientation specify the scrolling orientation. Must be either: SwingContants.HORIZONTAL or SwingContants.VERTICAL.
   * @param info An IncrementInfo object containing information of how to calculate the scrollable amount.
   */
  public void setScrollableUnitIncrement(int orientation, IncrementInfo info) {
    switch (orientation) {
      case SwingConstants.HORIZONTAL:
        horizontalUnit = info;
        break;
      case SwingConstants.VERTICAL:
        verticalUnit = info;
        break;
      default:
        throw new IllegalArgumentException("Invalid orientation: " + orientation);
    }
  }

//  Implement Scrollable interface
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  public int getScrollableUnitIncrement(
          Rectangle visible, int orientation, int direction) {
    switch (orientation) {
      case SwingConstants.HORIZONTAL:
        return getScrollableIncrement(horizontalUnit, visible.width);
      case SwingConstants.VERTICAL:
        return getScrollableIncrement(verticalUnit, visible.height);
      default:
        throw new IllegalArgumentException("Invalid orientation: " + orientation);
    }
  }

  public int getScrollableBlockIncrement(
          Rectangle visible, int orientation, int direction) {
    switch (orientation) {
      case SwingConstants.HORIZONTAL:
        return getScrollableIncrement(horizontalBlock, visible.width);
      case SwingConstants.VERTICAL:
        return getScrollableIncrement(verticalBlock, visible.height);
      default:
        throw new IllegalArgumentException("Invalid orientation: " + orientation);
    }
  }

  protected int getScrollableIncrement(IncrementInfo info, int distance) {
    if (info.getIncrement() == IncrementType.PIXELS) {
      return info.getAmount();
    } else {
      return distance * info.getAmount() / 100;
    }
  }

  public boolean getScrollableTracksViewportWidth() {
    if (scrollableWidth == ScrollableSizeHint.NONE) {
      return false;
    }

    if (scrollableWidth == ScrollableSizeHint.FIT) {
      return true;
    }

    //  STRETCH sizing, use the greater of the panel or viewport width
    if (getParent() instanceof JViewport) {
      return (((JViewport) getParent()).getWidth() > getPreferredSize().width);
    }

    return false;
  }

  public boolean getScrollableTracksViewportHeight() {
    if (scrollableHeight == ScrollableSizeHint.NONE) {
      return false;
    }

    if (scrollableHeight == ScrollableSizeHint.FIT) {
      return true;
    }

    //  STRETCH sizing, use the greater of the panel or viewport height
    if (getParent() instanceof JViewport) {
      return (((JViewport) getParent()).getHeight() > getPreferredSize().height);
    }

    return false;
  }

  /**
   * Helper class to hold the information required to calculate the scroll amount.
   */
  static class IncrementInfo {

    private IncrementType type;
    private int amount;

    public IncrementInfo(IncrementType type, int amount) {
      this.type = type;
      this.amount = amount;
    }

    public IncrementType getIncrement() {
      return type;
    }

    public int getAmount() {
      return amount;
    }

    public String toString() {
      return "ScrollablePanel["
              + type + ", "
              + amount + "]";
    }
  }

}
