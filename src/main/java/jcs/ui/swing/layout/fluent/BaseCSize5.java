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
package jcs.ui.swing.layout.fluent;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Insets;

import javax.swing.JComponent;
import static jcs.ui.swing.layout.CSizeUtils.grow;
import static jcs.ui.swing.layout.CSizeUtils.shrink;
import jcs.ui.swing.layout.HVSize;

/**
 * Adds functions that set a component size relative to {@link HVSize#getLineHeight()}.
 *
 * @author fred
 */
public class BaseCSize5<CSIZE extends BaseCSize5<CSIZE, CTYPE>, CTYPE extends Component>
        extends BaseCSize4<CSIZE, CTYPE> {

  @SuppressWarnings("unchecked")
  @Override
  protected CSIZE me() {
    return (CSIZE) this;
  }

  /* *** Variable line-width layout *** */
  /**
   * Sets fixed height to line-height and sets variable width to line-width.
   */
  public CSIZE setLineSize() {
    return setLineSize(props().getLineWidth());
  }

  /**
   * Sets fixed height to component's preferred height and sets variable width to line-width.
   */
  public CSIZE setLineSizeWithPrefHeight() {
    return setLineSize(props().getLineWidth(), pref().height);
  }

  /**
   * Sets fixed height to line-height and sets variable width to given width.
   */
  public CSIZE setLineSize(int width) {
    return setLineSize(width, 0.5f, 0.0f);
  }

  /**
   * Sets fixed height to given height and sets variable width to given width.
   */
  public CSIZE setLineSize(int width, int height) {
    return setLineSize(width, 0.5f, 0.0f, height);
  }

  /**
   * Calls {@link #setLineSize(int, float, float, int)} with line-width and line-height.
   */
  public CSIZE setLineSize(float minWidthFactor, float maxWidthFactor) {
    return setLineSize(props().getLineWidth(), minWidthFactor, maxWidthFactor);
  }

  /**
   * Calls {@link #setLineSize(int, float, float, int)} with line-height.
   */
  public CSIZE setLineSize(int width, float minWidthFactor, float maxWidthFactor) {
    return setLineSize(width, minWidthFactor, maxWidthFactor, props().getLineHeight());
  }

  /**
   * Sets a fixed height and a variable width for a component.
   *
   * @param width preferred width
   * @param minWidthFactor factor by which component width can shrink.
   * @param maxWidthFactor factor by which a component width can grow. If 0.0f or less, maximum width is set to
   * {@link HVSize#MAX_WIDTH}.
   * @param height the fixed height of the component
   */
  public CSIZE setLineSize(int width, float minWidthFactor, float maxWidthFactor, int height) {

    int minWidth = shrink(width, minWidthFactor);
    int maxWidth = (maxWidthFactor <= 0.0f ? HVSize.MAX_WIDTH : grow(width, maxWidthFactor));
    return setLineSize(minWidth, width, maxWidth, height);
  }

  /**
   * Sets a fixed height for a component with a variable width.
   *
   * @param minWidth minimum width
   * @param width preferred width
   * @param maxWidth maximum width
   * @param height fixed height
   */
  public CSIZE setLineSize(int minWidth, int width, int maxWidth, int height) {
    return min(minWidth, height).pref(width, height).max(maxWidth, height);
  }

  /* *** Variable area layout *** */
  /**
   * Sets an area of variable size with preferred size set to line-width and 4 times line-height.
   */
  public CSIZE setAreaSize() {
    return setAreaSize(1.0f, 4.0f);
  }

  /**
   * Sets an area of variable size with preferred size set to line-width times given factor and line-height times given factor. See
   * also {@link #setAreaSize(int, int, float, float)}.
   */
  public CSIZE setAreaSize(float widthFactor, float heightFactor) {
    return setAreaSize(getLineWidth(widthFactor), getLineHeight(heightFactor));
  }

  /**
   * Sets an area size of given width and height that can grow to screen-size and can shrink to a quarter of given sizes. See also
   * {@link #setAreaSize(int, int, float, float)}.
   */
  public CSIZE setAreaSize(int width, int height) {
    return setAreaSize(width, height, 4.0f, 0.0f);
  }

  /**
   * Sets a variable width and height for a component.
   *
   * @param width preferred width
   * @param height preferred height
   * @param minFactor factor by which component can shrink.
   * <br>A minimum width less than button-width is ignored when preferred width is more than button-width.
   * <br>A minimum height less than line-height is ignored.
   * @param maxFactor factor by which component can grow. If 0.0f or less, maximum width and height is set to
   * {@link HVSize#MAX_WIDTH} and {@link HVSize#MAX_HEIGHT}
   */
  public CSIZE setAreaSize(int width, int height, float minFactor, float maxFactor) {

    int minWidth = shrink(width, minFactor);
    if (width > props().getButtonWidth() && minWidth < props().getButtonWidth()) {
      minWidth = props().getButtonWidth();
    }
    if (width <= minWidth) {
      width = minWidth + 1;
    }

    int minHeight = shrink(height, minFactor);
    if (minHeight < props().getLineHeight()) {
      minHeight = props().getLineHeight();
    }
    if (height < minHeight) {
      height = minHeight + 1;
    }
    int maxWidth = (maxFactor <= 0.0f ? HVSize.MAX_WIDTH : grow(width, maxFactor));
    int maxHeight = (maxFactor <= 0.0f ? HVSize.MAX_HEIGHT : grow(height, maxFactor));
    return min(minWidth, minHeight).pref(width, height).max(maxWidth, maxHeight);
  }

  /* *** JTextField column width support *** */
  /**
   * Sets the preferred size to the width of the character <em>m</em> times the given columns and adds the insets width.
   */
  public CSIZE setColumnWidth(int columns) {

    // Copied from JTextField.getPreferredSize()
    int insetsWidth = 0;
    if (c() instanceof JComponent) {
      Insets insets = ((JComponent) c()).getInsets();
      insetsWidth = insets.left + insets.right;
    }
    FontMetrics metrics = c().getFontMetrics(c().getFont());
    setPrefWidth(insetsWidth + metrics.charWidth('m') * columns);
    return me();
  }

}
