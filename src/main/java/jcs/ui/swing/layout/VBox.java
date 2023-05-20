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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;

/**
 * A container that lays out components vertically (top to bottom). This container can be placed inside a scroller.
 */
public class VBox extends JComponent implements Scrollable {

  private static final long serialVersionUID = -5778892883397662105L;

  /**
   * Constructs a container that that lays out components from top to bottom, has no border and uses the
   * {@link HVSize#getDefault()}.
   */
  public VBox() {
    this(null, null);
  }

  /**
   * Constructs a container that that lays out components from top to bottom, has no border.
   */
  public VBox(HVSize props) {
    this(props, null);
  }

  public VBox(Insets borderInsets) {
    this(null, borderInsets);
  }

  /**
   * Constructs a container that that lays out components from top to bottom.
   *
   * @param borderInsets if not null then an empty border with the given insets is set.
   */
  public VBox(HVSize props, Insets borderInsets) {
    super();
    super.setLayout(new VLayout(props));
    if (borderInsets != null) {
      setBorder(new EmptyBorder(borderInsets));
    }
  }

  @Override
  public VLayout getLayout() {
    return (VLayout) super.getLayout();
  }

  public void setHvprops(HVSize props) {
    getLayout().setHvprops(props);
  }

  public HVSize getHvprops() {
    return getLayout().getHvprops();
  }

  /**
   * To accomodate a scroller
   */
  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return this.getPreferredSize();
  }

  /**
   * To accomodate a scroller
   */
  @Override
  public int getScrollableUnitIncrement(final Rectangle arg0, final int arg1,
          final int arg2) {
    return getHvprops().getLineHeightNoDepth();
  }

  /**
   * To accomodate a scroller
   */
  @Override
  public int getScrollableBlockIncrement(final Rectangle arg0, final int arg1,
          final int arg2) {
    return getHvprops().getLineHeightNoDepth();
  }

  /**
   * To accomodate a scroller
   */
  @Override
  public boolean getScrollableTracksViewportWidth() {
    if (this.getParent().getWidth() > this.getMinimumSize().width) {
      return true;
    }
    return false;
  }

  /**
   * To accomodate a scroller
   */
  @Override
  public boolean getScrollableTracksViewportHeight() {
    if (this.getParent().getHeight() > this.getMinimumSize().height) {
      return true;
    }
    return false;
  }

  /**
   * This (overloaded) convenience method allows adding components in the middle of a list of components (at the place indicated by
   * index).
   *
   * @param index if -1 component is added at he end of the components-list.
   * @see java.awt.Container#add(java.awt.Component, Object, int)
   */
  @Override
  public Component add(final Component comp, final int index) {
    super.add(comp, Integer.valueOf(index), index);
    return comp;
  }

}
