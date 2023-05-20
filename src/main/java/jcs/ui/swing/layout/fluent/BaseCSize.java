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
import jcs.ui.swing.layout.HVSize;

/**
 * Base class for a fluent API for component-size related functions.Contains basic functions to get or set a component size.<p>
 * Copied from fluent generic design (a.k.a. "Curiously Recurring Template Pattern") described at:
 * <br>http://www.unquietcode.com/blog/2011/programming/using-generics-to-build-fluent-apis-in-java/
 * <br>and adjusted to allow composition using the class hierarchy.
 *
 * @author fred
 * @param <CSIZE>
 * @param <CTYPE>
 */
public class BaseCSize<CSIZE extends BaseCSize<CSIZE, CTYPE>, CTYPE extends Component> {

  private HVSize hvsize;
  private CTYPE c;

  /**
   * Creates component sizer using {@link HVSize#getDefault()}.
   */
  public BaseCSize() {
    super();
    hvsize = HVSize.getDefault();
  }

  /**
   * This method must be overriden by any extending-class to return a CSIZE type of the extended class.
   */
  @SuppressWarnings("unchecked")
  protected CSIZE me() {
    return (CSIZE) this;
  }

  /**
   * @param c The component to perform size-operation upon.
   */
  public CSIZE set(CTYPE c) {
    this.c = c;
    return me();
  }

  /**
   * @return The component on which size operation are currently performed.
   */
  public CTYPE c() {
    return get();
  }

  /**
   * @return The component on which size operation are currently performed.
   */
  public CTYPE get() {
    return c;
  }

  /**
   * @param props The instance from which line-height, button-width, line-width, etc. are used.
   */
  public CSIZE setHvsize(HVSize props) {
    this.hvsize = props;
    return me();
  }

  /**
   * @return The instance from which line-height, button-width, line-width, etc. are used.
   */
  protected HVSize props() {
    return getHvsize();
  }

  /**
   * @return The instance from which line-height, button-width, line-width, etc. are used.
   */
  public HVSize getHvsize() {
    return hvsize;
  }

  /**
   * New dimension with given width and height.
   */
  public Dimension dim(int w, int h) {
    return new Dimension(w, h);
  }

  /**
   * Components current minimum size.
   */
  public Dimension min() {
    return c.getMinimumSize();
  }

  /**
   * Components current preferred size.
   */
  public Dimension pref() {
    return c.getPreferredSize();
  }

  /**
   * Dimension with button-width and line-height.
   */
  public Dimension prefButton() {
    return dim(hvsize.getButtonWidth(), hvsize.getLineHeight());
  }

  /**
   * Dimension with line-width and line-height.
   */
  public Dimension prefLine() {
    return dim(hvsize.getLineWidth(), hvsize.getLineHeight());
  }

  /**
   * Components current maximum size.
   */
  public Dimension max() {
    return c.getMaximumSize();
  }

  /**
   * Dimension with {@link HVSize#MAX_WIDTH} and {@link HVSize#MAX_HEIGHT}.
   */
  public Dimension dimMax() {
    return dim(HVSize.MAX_WIDTH, HVSize.MAX_HEIGHT);
  }

  /**
   * Sets min-size for component directly.
   */
  public CSIZE min(Dimension d) {
    c.setMinimumSize(d);
    return me();
  }

  /**
   * Sets pref-size for component directly.
   */
  public CSIZE pref(Dimension d) {
    c.setPreferredSize(d);
    return me();
  }

  /**
   * Sets max-size for component directly.
   */
  public CSIZE max(Dimension d) {
    c.setMaximumSize(d);
    return me();
  }

  /**
   * Sets min-size for component directly.
   */
  public CSIZE min(int w, int h) {
    return min(dim(w, h));
  }

  /**
   * Sets pref-size for component directly.
   */
  public CSIZE pref(int w, int h) {
    return pref(dim(w, h));
  }

  /**
   * Sets maximum size for component directly but limits width and height to {@link HVSize#MAX_WIDTH} and {@link HVSize#MAX_HEIGHT}
   */
  public CSIZE max(int w, int h) {
    return max(dim(Math.min(w, HVSize.MAX_WIDTH), Math.min(h, HVSize.MAX_HEIGHT)));
  }

  /**
   * Sets min-width for component directly.
   */
  public CSIZE minWidth(int w) {
    return min(w, min().height);
  }

  /**
   * Sets pref-width for component directly.
   */
  public CSIZE prefWidth(int w) {
    return pref(w, pref().height);
  }

  /**
   * Sets max-width for component directly.
   */
  public CSIZE maxWidth(int w) {
    return max(w, max().height);
  }

  /**
   * Sets min-height for component directly.
   */
  public CSIZE minHeight(int h) {
    return min(min().width, h);
  }

  /**
   * Sets pref-height for component directly.
   */
  public CSIZE prefHeight(int h) {
    return pref(pref().width, h);
  }

  /**
   * Sets max-height for component directly.
   */
  public CSIZE maxHeight(int h) {
    return max(max().width, h);
  }

  /**
   * Multiplies button-width with given factor.
   */
  public int getButtonWidth(float factor) {
    return multiply(hvsize.getButtonWidth(), factor);
  }

  /**
   * Multiplies line-width with given factor.
   */
  public int getLineWidth(float factor) {
    return multiply(hvsize.getLineWidth(), factor);
  }

  /**
   * Multiplies line-height with given factor.
   */
  public int getLineHeight(float factor) {
    return multiply(hvsize.getLineHeight(), factor);
  }

  /**
   * Sets size of component to given min/preferred/max dimensions directly.
   */
  public CSIZE setSize(Dimension min, Dimension pref, Dimension max) {
    return min(min).pref(pref).max(max);
  }

  /**
   * Copies min, pref and max-size from source and sets it directly on the current component.
   */
  public CSIZE copySize(Component source) {
    return setSize(source.getMinimumSize(), source.getPreferredSize(), source.getMaximumSize());
  }

}
