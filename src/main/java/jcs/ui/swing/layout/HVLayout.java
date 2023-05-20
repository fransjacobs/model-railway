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
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import jcs.ui.swing.layout.fluent.BaseCSize4;

/**
 * Base class for {@link VLayout} and {@link HLayout}. A lot of code between these classes is similar and where possible, this class
 * shares code for both classes. However, there are subtle differences between the methods and sharing to much might make debugging
 * difficult. One example of a method that is difficult to share is <code>maximumLayoutSize</code>
 *
 * @author fred
 *
 */
public abstract class HVLayout implements LayoutManager2 {

  /**
   * Orientation following the line-axis. See {@link javax.swing.SwingConstants#LEADING}
   */
  public static final int LEADING = javax.swing.SwingConstants.LEADING; // 10
  /**
   * Orientation opposite the line-axis. See {@link javax.swing.SwingConstants#TRAILING}
   */
  public static final int TRAILING = javax.swing.SwingConstants.TRAILING; // 11
  /**
   * The central position in an area. See {@link javax.swing.SwingConstants#CENTER}
   */
  public static final int CENTER = javax.swing.SwingConstants.CENTER; // 0

  // The components to align vertically
  protected final transient ArrayList<Component> parts = new ArrayList<>();
  // MaximumSize of this form
  protected transient Dimension dmax;
  // MinimumSize of this form
  protected transient Dimension dmin;
  // PreferredSize of this form
  protected transient Dimension dpref;
  // Used in calculation to shrink component, set by minimumLayoutSize
  protected transient int varMinSize;
  protected transient int varMaxSize;
  protected transient HVSize props;

  public HVLayout() {
    this(null);
  }

  /**
   * @param props if null, {@link HVSize#getDefault()} is used.
   */
  public HVLayout(HVSize props) {
    super();
    if (props == null) {
      props = HVSize.getDefault();
    }
    setHvprops(props);
  }

  public final void setHvprops(HVSize props) {
    this.props = props;
  }

  public HVSize getHvprops() {
    return props;
  }

  /**
   * Adds a component at the end of the list or if place is a valid index (Integer class required) adds the component at the
   * specified index-place (0 is the first place).
   * <br> LineBox uses this method for a call to add(component, index)
   *
   * @param c The component to add
   * @param place The place in the list of components for this layout.
   */
  @Override
  public void addLayoutComponent(Component c, Object place) {

    if (place instanceof Integer) {
      final int index = ((Integer) place).intValue();
      if (index > -1 && (index < parts.size())) {
        parts.add(index, c);
      } else {
        parts.add(c);
      }
    } else {
      parts.add(c);
    }
  }

  /**
   * Adds component c to the end of the list of components for this layout.
   */
  @Override
  public void addLayoutComponent(String s, Component c) {
    parts.add(c);
  }

  /**
   * Removes component c from the list of components for this layout.
   */
  @Override
  public void removeLayoutComponent(Component c) {

    boolean found = false;
    int i = 0;
    while (!found && i < parts.size()) {
      found = (parts.get(i) == c);
      i++;
    }
    if (found) {
      parts.remove(--i);
    }
    // and NOT lines.remove(i--)
  }

  /**
   * This method ensures that temporary values can be used. Only this function can give the signal that a re-calculation has to be
   * done
   * <br> According to BoxLayout this should be synchronized
   */
  @Override
  public synchronized void invalidateLayout(Container c) {

    dmax = null;
    dmin = null;
    dpref = null;
//System.out.println("Invalidate HVLayout");		
  }

  /**
   * The total gap height, used in resizing calculations.
   */
  protected int gapHeight() {
    return (parts.isEmpty() ? 0 : (props.getVerticalGap() * (parts.size() - 1)));
  }

  /**
   * The total gap width, used in resizing calculations. Note that this is a slightly different calculation then gapHeight() in
   * VLayout.
   */
  protected int gapWidth() {
    return (parts.isEmpty() ? 0 : (props.getHorizontalGap() * (parts.size() - 1)));
  }

  protected int gapSize(boolean vert) {
    return (vert ? gapHeight() : gapWidth());
  }

  protected int getInsets(Container target, boolean vert) {

    Insets targetInsets = target.getInsets();
    return (vert ? targetInsets.top + targetInsets.bottom : targetInsets.left + targetInsets.right);
  }

  protected int getSize(Dimension d, boolean vert) {
    return (vert ? d.height : d.width);
  }

  protected int getSizeDiff(Dimension big, Dimension small, boolean vert) {
    return getSize(big, vert) - getSize(small, vert);
  }

  protected void ensureSmall(Dimension small, Dimension big) {

    if (small.width > big.width) {
      small.width = big.width;
    }
    if (small.height > big.height) {
      small.height = big.height;
    }
  }

  protected boolean isBigger(Dimension big, Dimension small, boolean vert) {
    return getSize(big, vert) > getSize(small, vert);
  }

  protected int getSmallest(Dimension d1, Dimension d2, boolean vert) {
    return (getSize(d1, vert) > getSize(d2, vert) ? getSize(d2, vert) : getSize(d1, vert));
  }

  /**
   * Return minimum size (width and height) of this container The minimum size of this layout calculated by summing up all minimum
   * sizes of the components in this layout.
   * <br> The container will refuse to draw smaller.
   * <br> Also sets {@link #varMinSize} which is used in the layoutContainer method.
   * <p>
   * A check is done to confirm min-size is smaller than pref-size. This is needed in case a preferred size is calculated and cannot
   * be set on a component. An example of this is a {@link javax.swing.JTextField#JTextField(int)} which sets a "column size" which
   * in turn sets a calculated fixed width of a text-field. This fixed width cannot be changed, i.e. setting a preferred size has no
   * effect. See also {@link BaseCSize4#setColumnWidth(int)}.
   */
  public Dimension minimumLayoutSize(Container target, boolean vert) {

    if (dmin != null) // By not returning dmin itself we ensure that dmin values cannot be changed from the outside
    {
      return new Dimension(dmin.width, dmin.height);
    }

    // height will determine the reuired height for this form
    int size = getInsets(target, vert);
    // maxWidth will determine the required width for this form
    int maxSizeOpposite = 0;
    // varMinHeight determines the amount of space that can be 
    // resized during shrinking from components
    varMinSize = 0;
    // According to other layout managers, this is how you ensure that
    // access to component/line sizes is synchronized 
    synchronized (target.getTreeLock()) {
      Dimension cmin, cpref;
      for (int i = 0; i < parts.size(); i++) {
        Component c = parts.get(i);
        cmin = c.getMinimumSize();
        cpref = c.getPreferredSize();
        // Protection against JTextField with column size.
        ensureSmall(cmin, cpref);
        size += getSize(cmin, vert);
        maxSizeOpposite = Math.max(maxSizeOpposite, getSize(cmin, !vert));
        if (isBigger(cpref, cmin, vert)) {
          varMinSize += getSizeDiff(cpref, cmin, vert);
        }
      }
    }
    maxSizeOpposite += getInsets(target, !vert);
    Dimension d;
    if (vert) {
      d = new Dimension(maxSizeOpposite, size + gapHeight());
    } else {
      d = new Dimension(size + gapWidth(), maxSizeOpposite);
    }
    dmin = new Dimension(d.width, d.height);
    return d;
  }

  /**
   * The preferred size of this layout calculated by summing up all preferred sizes of the components in this layout.
   */
  public Dimension preferredLayoutSize(Container target, boolean vert) {

    if (dpref != null) {
      return new Dimension(dpref.width, dpref.height);
    }

    int size = getInsets(target, vert);
    int maxSizeOpposite = 0;
    varMaxSize = 0;
    synchronized (target.getTreeLock()) {
      Dimension cpref, cmax;
      for (int i = 0; i < parts.size(); i++) {
        Component c = parts.get(i);
        cpref = c.getPreferredSize();
        cmax = c.getMaximumSize();
        // there is no use-case for ensuring pref is smaller than max
        // but it could prevent errors
        // ensureSmall(cpref, cmax);
        size += getSize(cpref, vert);
        maxSizeOpposite = Math.max(maxSizeOpposite, getSize(cpref, !vert));
        if (isBigger(cmax, cpref, vert)) {
          varMaxSize += getSize(cpref, vert);
        }
      }
    }
    maxSizeOpposite += getInsets(target, !vert);
    Dimension d;
    if (vert) {
      d = new Dimension(maxSizeOpposite, size + gapHeight());
    } else {
      d = new Dimension(size + gapWidth(), maxSizeOpposite);
    }
    dpref = new Dimension(d.width, d.height);
    return d;
  }

}
