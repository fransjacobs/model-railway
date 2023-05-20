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
import java.awt.Dimension;
import static jcs.ui.swing.layout.CSizeUtils.multiply;

/**
 * Adds fixed-size functions and aliases. Also adds align-functions, the basis for calculated sizes for forms based on button-width
 * and line-height.
 *
 * @author fred
 */
public class BaseCSize4<CSIZE extends BaseCSize4<CSIZE, CTYPE>, CTYPE extends Component>
        extends BaseCSize3<CSIZE, CTYPE> {

  @SuppressWarnings("unchecked")
  @Override
  protected CSIZE me() {
    return (CSIZE) this;
  }

  /* *** Fixed size layout *** */
  /**
   * Sets all width sizes to current preferred width.
   */
  public CSIZE setFixedWidth() {
    int w = pref().width;
    return setMinWidth(w).setMaxWidth(w);
  }

  /**
   * Sets all width sizes to button-width.
   */
  public CSIZE setFixedWidthButton() {
    return setMinWidthButton().setMaxWidthButton();
  }

  /**
   * Sets all width sizes to current preferred width multiplied by given factor.
   */
  public CSIZE setFixedWidth(float factor) {
    return setFixedWidth(multiply(pref().width, factor));
  }

  /**
   * Sets all width sizes to button-width times given factor.
   */
  public CSIZE setFixedWidthButton(float factor) {
    return setFixedWidth(getButtonWidth(factor));
  }

  /**
   * Sets all width sizes to given width.
   */
  public CSIZE setFixedWidth(int w) {
    return setMinWidth(w).setMaxWidth(w);
  }

  /**
   * Sets all height sizes to current preferred height.
   */
  public CSIZE setFixedHeight() {
    int h = pref().height;
    return setMinHeight(h).setMaxHeight(h);
  }

  /**
   * Sets all height sizes to line-height.
   */
  public CSIZE setFixedHeightLine() {
    return setMinHeightLine().setMaxHeightLine();
  }

  /**
   * Sets all height sizes to line-height multiplied by given factor.
   */
  public CSIZE setFixedHeightLine(float factor) {
    return setFixedHeight(getLineHeight(factor));
  }

  /**
   * Sets all height sizes to current preferred height multiplied by given factor.
   */
  public CSIZE setFixedHeight(float factor) {
    return setFixedHeight(multiply(pref().height, factor));
  }

  /**
   * Sets all height sizes to given height.
   */
  public CSIZE setFixedHeight(int h) {
    return setMinHeight(h).setMaxHeight(h);
  }

  /**
   * Sets a fixed size for a square (width equal to line-height).
   */
  public CSIZE setFixedSquare() {
    return setFixed(props().getLineHeight(), props().getLineHeight());
  }

  /**
   * Sets a fixed size for a component to button-width and line-height.
   */
  public CSIZE setFixedButton() {
    return setFixed(props().getButtonWidth(), props().getLineHeight());
  }

  /**
   * Sets a fixed size for a component scaled to button-width and line-height.
   */
  public CSIZE setFixedButton(float widthFactor, float heightFactor) {
    return setFixed(getButtonWidth(widthFactor), getLineHeight(heightFactor));
  }

  /**
   * Sets a fixed size for a component scaled to button-width.
   *
   * @param widthFactor scaled to button-width
   * @param height if 0, components preferred height is used.
   */
  public CSIZE setFixedButton(float widthFactor, int height) {
    return setFixed(getButtonWidth(widthFactor), (height == 0 ? pref().height : height));
  }

  /**
   * Sets a fixed size for a component scaled to line-height.
   *
   * @param width if 0, components preferred width is used.
   * @param heightFactor scaled to line-height.
   */
  public CSIZE setFixedLine(int width, float heightFactor) {
    return setFixed((width == 0 ? pref().width : width), getLineHeight(heightFactor));
  }

  /**
   * Sets all sizes to current preferred size.
   */
  public CSIZE setFixed() {
    Dimension p = pref();
    return setFixed(p.width, p.height);
  }

  /**
   * Sets all sizes scaled to current preferred size.
   */
  public CSIZE setFixed(float widthFactor, float heightFactor) {
    Dimension p = pref();
    return setFixed(multiply(p.width, widthFactor), multiply(p.height, heightFactor));
  }

  /**
   * Sets all sizes to given width and height.
   */
  public CSIZE setFixed(int w, int h) {
    return setFixed(dim(w, h));
  }

  /**
   * Sets all sizes to given dimension.
   */
  public CSIZE setFixed(Dimension d) {
    return min(d).max(d).pref(d);
  }

  /**
   * Sets a fixed size for a component
   *
   * @param width if 0, components preferred width is used.
   * @param heightFactor scaled to current preferred height.
   */
  public CSIZE setFixed(int width, float heightFactor) {
    Dimension p = pref();
    return setFixed((width == 0 ? p.width : width), multiply(p.height, heightFactor));
  }

  /**
   * Sets a fixed size for a component
   *
   * @param widthFactor scaled to current preferred width
   * @param height if 0, components preferred height is used.
   */
  public CSIZE setFixed(float widthFactor, int height) {
    Dimension p = pref();
    return setFixed(multiply(p.width, widthFactor), (height == 0 ? p.height : height));
  }

  /* *** Aliases for fixed sizes *** */
  /**
   * Fixed size set to a square, see also {@link #setFixedSquare()}
   */
  public CSIZE setSquareSize() {
    return setFixedSquare();
  }

  /**
   * Fixed size set to a button, see also {@link #setFixedButton()},
   * {@link #setFixedButton(float, int)} and {@link #setFixedButton(float, float)}
   */
  public CSIZE setButtonSize() {
    return setFixedButton();
  }

  /* *** Align width/height to defaults *** */
  /**
   * Aligns all sizes to a button-width factor and line-height factor.
   */
  public CSIZE alignSize() {
    return min(align(min())).pref(align(pref())).max(align(max()));
  }

  /**
   * Aligns dimension to a button-width factor and line-height factor.
   */
  public Dimension align(Dimension d) {

    d.width = alignWidth(d.width);
    d.height = alignHeight(d.height);
    return d;
  }

  /**
   * Aligns width to a button-width factor.
   */
  public int alignWidth(int width) {
    return (width + (props().getButtonWidth() - (width % props().getButtonWidth())));
  }

  /**
   * Aligns height to a line-height factor.
   */
  public int alignHeight(int height) {
    return (height + (props().getLineHeight() - (height % props().getLineHeight())));
  }

}
