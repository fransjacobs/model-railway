/*
* Copyright 2018 Frederik Wiers
*
* Permission is hereby granted, free of charge, to any person obtaining a 
* copy of this software and associated documentation files (the 
* "Software"), to deal in the Software without restriction, including 
* without limitation the rights to use, copy, modify, merge, publish, 
* distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to 
* the following conditions:
*
* The above copyright notice and this permission notice shall be included 
* in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package jcs.ui.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JScrollPane;

/**
 * Static functions describing or changing a components size.
 *
 * @author fred
 *
 */
public class CSizeUtils {

  private CSizeUtils() {
  }

  /* *** Size characteristics *** */
  public static boolean canShrinkWidth(Component c) {
    return (c.getPreferredSize().width > c.getMinimumSize().width);
  }

  public static boolean canShrinkHeight(Component c) {
    return (c.getPreferredSize().height > c.getMinimumSize().height);
  }

  public static boolean canShrink(Component c) {
    return (canShrinkWidth(c) || canShrinkHeight(c));
  }

  public static boolean canGrowWidth(Component c) {
    return (c.getPreferredSize().width < c.getMaximumSize().width);
  }

  public static boolean canGrowHeight(Component c) {
    return (c.getPreferredSize().height < c.getMaximumSize().height);
  }

  public static boolean canGrow(Component c) {
    return (canGrowWidth(c) || canGrowHeight(c));
  }

  public static boolean isFixedSize(Component c) {
    return !(canShrink(c) || canGrow(c));
  }

  /**
   * Multiplies given number with factor and rounds result.
   */
  public static int multiply(int l, float factor) {
    // using Math.round() does not appear to give too small or big components.
    // cast to int does also work.
    return Math.round(l * factor);
  }

  /**
   * Divides given number with factor and rounds result
   */
  public static int divide(int l, float factor) {
    return Math.round(l / factor);
  }

  /**
   * Returns a smaller number by dividing or multiplying with the factor.
   */
  public static int shrink(int l, float factor) {
    return (factor > 1.0f ? divide(l, factor) : multiply(l, factor));
  }

  /**
   * Returns a larger number by dividing or multiplying with the factor.
   */
  public static int grow(int l, float factor) {
    return (factor < 1.0f ? divide(l, factor) : multiply(l, factor));
  }

  /**
   * Sets minimum size to preferred size so that the component cannot shrink.
   */
  public static void fixedMinSize(Component c) {
    c.setMinimumSize(c.getPreferredSize());
  }

  /**
   * Sets maximum size to preferred size so that the component cannot grow.
   */
  public static void fixedMaxSize(Component c) {
    c.setMaximumSize(c.getPreferredSize());
  }

  /**
   * Sets minimum and mximum size to preferred size so that the component cannot shrink or grow.
   */
  public static void fixedSize(Component c) {

    Dimension d = c.getPreferredSize();
    c.setMinimumSize(d);
    c.setMaximumSize(d);
  }

  /**
   * Ensures all components in the container cannot shrink or grow.
   */
  public static void fixedSizeAll(Container c) {

    synchronized (c.getTreeLock()) {
      fixedSizeAllRecursive(c, true, true);
    }
  }

  /**
   * Ensures all components in the container cannot shrink.
   */
  public static void fixedMinSizeAll(Container c) {

    synchronized (c.getTreeLock()) {
      fixedSizeAllRecursive(c, true, false);
    }
  }

  /**
   * Ensures all components in the container cannot grow.
   */
  public static void fixedMaxSizeAll(final Container c) {

    synchronized (c.getTreeLock()) {
      fixedSizeAllRecursive(c, false, true);
    }
  }

  private static void fixedSizeAllRecursive(Container c, final boolean min, final boolean max) {

    // TODO: container size is derived from child-components?
    // If so, container size should not be set.
    fixedSize(c, min, max);
    for (int i = 0; i < c.getComponentCount(); i++) {
      Component comp = c.getComponent(i);
      if (comp instanceof Container) {
        fixedSizeAllRecursive((Container) comp, min, max);
      } else {
        fixedSize(c, min, max);
      }
    }
  }

  private static void fixedSize(Component c, boolean min, boolean max) {

    if (min && max) {
      fixedSize(c);
    } else {
      if (min) {
        fixedMinSize(c);
      }
      if (max) {
        fixedMaxSize(c);
      }
    }
  }

  /**
   * Returns given dimension adjusted with given factors.
   */
  public static Dimension scale(Dimension d, float widthFactor, float heightFactor) {

    d.width = multiply(d.width, widthFactor);
    d.height = multiply(d.height, heightFactor);
    return d;
  }

  /**
   * Returns given dimension adjusted with given factors. But given dimension will never be larger than {@link HVSize#MAX_WIDTH} and
   * {@link HVSize#MAX_HEIGHT}.
   */
  public static Dimension scaleMax(Dimension d, float widthFactor, float heightFactor) {

    d.width = Math.min(multiply(d.width, widthFactor), HVSize.MAX_WIDTH);
    d.height = Math.min(multiply(d.height, heightFactor), HVSize.MAX_HEIGHT);
    return d;
  }

  public static void scaleComponent(Component c, float widthFactor, float heightFactor) {

    c.setMinimumSize(scale(c.getMinimumSize(), widthFactor, heightFactor));
    c.setPreferredSize(scale(c.getPreferredSize(), widthFactor, heightFactor));
    c.setMaximumSize(scaleMax(c.getMaximumSize(), widthFactor, heightFactor));
  }

  public static void scaleAll(Container c, float widthFactor, float heightFactor) {

    synchronized (c.getTreeLock()) {
      scaleAllRecursive(c, widthFactor, heightFactor);
    }
  }

  private static void scaleAllRecursive(Container c, final float widthFactor, final float heightFactor) {

    // TODO: container size is derived from child-components?
    // If so, container size should not be set.
    scaleComponent(c, widthFactor, heightFactor);
    for (int i = 0; i < c.getComponentCount(); i++) {
      Component comp = c.getComponent(i);
      if (comp instanceof Container) {
        scaleAllRecursive((Container) comp, widthFactor, heightFactor);
      } else {
        scaleComponent(comp, widthFactor, heightFactor);
      }
    }
  }

  /**
   * Returns the height of the horizontal scroll-bar. Add this to the preferred height of the scroller in case a horizontal
   * scroll-bar is always shown.
   */
  public static int getScrollbarHeight(final JScrollPane scroller) {
    return scroller.getHorizontalScrollBar().getMaximumSize().height;
  }

  /**
   * Returns the width of the vertical scroll-bar. Add this to the preferred width of the scroller in case a vertical scroll-bar is
   * always shown.
   */
  public static int getScrollbarWidth(final JScrollPane scroller) {
    return scroller.getVerticalScrollBar().getMaximumSize().width;
  }

  public static void copySize(Component source, Component target) {

    target.setMinimumSize(source.getMinimumSize());
    target.setPreferredSize(source.getPreferredSize());
    target.setMaximumSize(source.getMaximumSize());
  }

  public static Dimension clone(Dimension d) {
    return new Dimension(d.width, d.height);
  }

  public static String sizesToString(Component c) {

    StringBuilder sb = new StringBuilder();
    sb.append("Component sizes of ").append(c.getClass().toString()).append(':').append(c.hashCode());
    Dimension d = c.getMinimumSize();
    sb.append("\nMin : " + d.width + " / " + d.height);
    d = c.getPreferredSize();
    sb.append("\nPref: " + d.width + " / " + d.height);
    d = c.getMaximumSize();
    sb.append("\nMax : " + d.width + " / " + d.height);
    return sb.toString();
  }

}
