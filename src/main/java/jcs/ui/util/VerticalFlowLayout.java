/*
 * Copyright (C) 2001 Vassili Dzuba.
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
package jcs.ui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.Serializable;

/**
 * A vertical flow layout is similar to a flow layout but it layouts the components vertically instead of horizontally.
 *
 * @author Vassili Dzuba
 * @since March 24, 2001
 */
public class VerticalFlowLayout implements LayoutManager, Serializable {

  private static final long serialVersionUID = -2610593156590860481L;

  int hAlign;
  int vAlign;
  int hGap;
  int vGap;

  /**
   * Description of the Field
   */
  public final static int TOP = 0;
  /**
   * Description of the Field
   */
  public final static int CENTER = 1;
  /**
   * Description of the Field
   */
  public final static int BOTTOM = 2;
  /**
   * Description of the Field
   */
  public final static int LEFT = 3;
  /**
   * Description of the Field
   */
  public final static int RIGHT = 4;

  /**
   * Constructor for the VerticalFlowLayout object
   */
  public VerticalFlowLayout() {
    this(CENTER, CENTER, 5, 5);
  }

  /**
   * Constructor for the VerticalFlowLayout object
   *
   * @param halign Description of Parameter
   * @param valign Description of Parameter
   */
  public VerticalFlowLayout(int halign, int valign) {
    this(halign, valign, 5, 5);
  }

  /**
   * Constructor for the VerticalFlowLayout object
   *
   * @param halign Description of Parameter
   * @param valign Description of Parameter
   * @param hgap Description of Parameter
   * @param vgap Description of Parameter
   */
  public VerticalFlowLayout(int halign, int valign, int hgap, int vgap) {
    hGap = hgap;
    vGap = vgap;
    hAlign = halign;
    vAlign = valign;
  }

  /**
   * Sets the Alignment attribute of the VerticalFlowLayout object
   *
   * @param halign The new Alignment value
   * @param valign The new Alignment value
   */
  public void setAlignment(int halign, int valign) {
    hAlign = halign;
    vAlign = valign;
  }

  /**
   * Sets the Hgap attribute of the VerticalFlowLayout object
   *
   * @param hgap The new Hgap value
   */
  public void sethGap(int hgap) {
    hGap = hgap;
  }

  /**
   * Sets the Vgap attribute of the VerticalFlowLayout object
   *
   * @param vgap The new Vgap value
   */
  public void setvGap(int vgap) {
    vGap = vgap;
  }

  /**
   * Gets the Halignment attribute of the VerticalFlowLayout object
   *
   * @return The Halignment value
   */
  public int getHalignment() {
    return hAlign;
  }

  /**
   * Gets the Valignment attribute of the VerticalFlowLayout object
   *
   * @return The Valignment value
   */
  public int getValignment() {
    return vAlign;
  }

  /**
   * Gets the Hgap attribute of the VerticalFlowLayout object
   *
   * @return The Hgap value
   */
  public int gethGap() {
    return hGap;
  }

  /**
   * Gets the Vgap attribute of the VerticalFlowLayout object
   *
   * @return The Vgap value
   */
  public int getvGap() {
    return vGap;
  }

  /**
   * Adds a feature to the LayoutComponent attribute of the VerticalFlowLayout object
   *
   * @param name The feature to be added to the LayoutComponent attribute
   * @param comp The feature to be added to the LayoutComponent attribute
   */
  @Override
  public void addLayoutComponent(String name, Component comp) {
  }

  /**
   * Description of the Method
   *
   * @param comp Description of Parameter
   */
  @Override
  public void removeLayoutComponent(Component comp) {
  }

  /**
   * Description of the Method
   *
   * @param target Description of Parameter
   * @return Description of the Returned Value
   */
  @Override
  public Dimension preferredLayoutSize(Container target) {
    synchronized (target.getTreeLock()) {
      Dimension dim = new Dimension(0, 0);
      int nmembers = target.getComponentCount();
      boolean firstVisibleComponent = true;

      for (int ii = 0; ii < nmembers; ii++) {
        Component m = target.getComponent(ii);
        if (m.isVisible()) {
          Dimension d = m.getPreferredSize();
          dim.width = Math.max(dim.width, d.width);
          if (firstVisibleComponent) {
            firstVisibleComponent = false;
          } else {
            dim.height += vGap;
          }
          dim.height += d.height;
        }
      }
      Insets insets = target.getInsets();
      dim.width += insets.left + insets.right + hGap * 2;
      dim.height += insets.top + insets.bottom + vGap * 2;
      return dim;
    }
  }

  /**
   * Description of the Method
   *
   * @param target Description of Parameter
   * @return Description of the Returned Value
   */
  @Override
  public Dimension minimumLayoutSize(Container target) {
    synchronized (target.getTreeLock()) {
      Dimension dim = new Dimension(0, 0);
      int nmembers = target.getComponentCount();
      boolean firstVisibleComponent = true;

      for (int ii = 0; ii < nmembers; ii++) {
        Component m = target.getComponent(ii);
        if (m.isVisible()) {
          Dimension d = m.getPreferredSize();
          dim.width = Math.max(dim.width, d.width);
          if (firstVisibleComponent) {
            firstVisibleComponent = false;
          } else {
            dim.height += vGap;
          }
          dim.height += d.height;
        }
      }
      Insets insets = target.getInsets();
      dim.width += insets.left + insets.right + hGap * 2;
      dim.height += insets.top + insets.bottom + vGap * 2;
      return dim;
    }
  }

  /**
   * Description of the Method
   *
   * @param target Description of Parameter
   */
  @Override
  public void layoutContainer(Container target) {
    synchronized (target.getTreeLock()) {
      Insets insets = target.getInsets();
      int maxheight = target.getHeight() - (insets.top + insets.bottom + vGap * 2);
      int nmembers = target.getComponentCount();
      int y = 0;

      Dimension preferredSize = preferredLayoutSize(target);
      Dimension targetSize = target.getSize();

      switch (vAlign) {
        case TOP:
          y = insets.top;
          break;
        case CENTER:
          y = (targetSize.height - preferredSize.height) / 2;
          break;
        case BOTTOM:
          y = targetSize.height - preferredSize.height - insets.bottom;
          break;
      }

      for (int i = 0; i < nmembers; i++) {
        Component m = target.getComponent(i);
        if (m.isVisible()) {
          Dimension d = m.getPreferredSize();
          m.setSize(d.width, d.height);

          if ((y + d.height) <= maxheight) {
            if (y > 0) {
              y += vGap;
            }

            int x = 0;
            switch (hAlign) {
              case LEFT:
                x = insets.left;
                break;
              case CENTER:
                x = (targetSize.width - d.width) / 2;
                break;
              case RIGHT:
                x = targetSize.width - d.width - insets.right;
                break;
            }

            m.setLocation(x, y);

            y += d.getHeight();

          } else {
            break;
          }
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    String halign = "";
    switch (hAlign) {
      case TOP:
        halign = "top";
        break;
      case CENTER:
        halign = "center";
        break;
      case BOTTOM:
        halign = "bottom";
        break;
    }
    String valign = "";
    switch (vAlign) {
      case TOP:
        valign = "top";
        break;
      case CENTER:
        valign = "center";
        break;
      case BOTTOM:
        valign = "bottom";
        break;
    }
    return getClass().getName() + "[hgap=" + hGap + ",vgap=" + vGap + ",halign=" + halign + ",valign=" + valign + "]";
  }

}
