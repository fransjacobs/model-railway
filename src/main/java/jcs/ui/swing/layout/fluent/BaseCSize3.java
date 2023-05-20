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
import jcs.ui.swing.layout.CSizeUtils;
import static jcs.ui.swing.layout.CSizeUtils.grow;
import static jcs.ui.swing.layout.CSizeUtils.shrink;

/**
 * Adds functions that set a component size relative to it's current size.
 *
 * @author fred
 */
public class BaseCSize3<CSIZE extends BaseCSize3<CSIZE, CTYPE>, CTYPE extends Component>
        extends BaseCSize2<CSIZE, CTYPE> {

  @SuppressWarnings("unchecked")
  @Override
  protected CSIZE me() {
    return (CSIZE) this;
  }

  /* *** Fixed size options *** */
  /**
   * Sets minimum and maximum size to preferred size. Equal to <code>setFixed(pref())</code>
   */
  public CSIZE fixedSize() {
    return min(pref()).max(pref());
  }

  /**
   * Ensures a component cannot shrink horizontally.
   */
  public CSIZE fixedMinWidth() {
    return min(pref().width, min().height);
  }

  /**
   * Ensures a component cannot shrink vertically.
   */
  public CSIZE fixedMinHeight() {
    return min(min().width, pref().height);
  }

  /**
   * Ensures a component cannot shrink.
   */
  public CSIZE fixedMinSize() {
    return min(pref());
  }

  /**
   * Ensures a component cannot grow horizontally.
   */
  public CSIZE fixedMaxWidth() {
    return max(pref().width, max().height);
  }

  /**
   * Ensures a component cannot grow vertically.
   */
  public CSIZE fixedMaxHeight() {
    return max(max().width, pref().height);
  }

  /**
   * Ensures a component cannot grow.
   */
  public CSIZE fixedMaxSize() {
    return max(pref());
  }

  /**
   * Ensures a component cannot change size horizontally.
   */
  public CSIZE fixedWidth() {
    return fixedMinWidth().fixedMaxWidth();
  }

  /**
   * Ensures a component cannot change size vertical.
   */
  public CSIZE fixedHeight() {
    return fixedMinHeight().fixedMaxHeight();
  }

  /* *** Shrink options *** */
  /**
   * Allow component to shrink to half of the current preferred size.
   */
  public CSIZE shrinkSize() {
    return shrinkSize(0.5f, 0.5f);
  }

  /**
   * Allow component to shrink to given width- and height-factors of the current preferred size.
   */
  public CSIZE shrinkSize(float widthFactor, float heightFactor) {
    return shrinkWidth(widthFactor).shrinkHeight(heightFactor);
  }

  /**
   * Allow component to shrink in width to half of the current preferred width.
   */
  public CSIZE shrinkWidth() {
    return shrinkWidth(0.5f);
  }

  /**
   * Allow component to shrink in width to given factor times the current preferred width.
   */
  public CSIZE shrinkWidth(float factor) {
    return setMinWidth(shrink(pref().width, factor));
  }

  /**
   * Allow component to shrink in height by half of current preferred height.
   */
  public CSIZE shrinkHeight() {
    return shrinkWidth(0.5f);
  }

  /**
   * Allow component to shrink in height to given factor times the current preferred height.
   */
  public CSIZE shrinkHeight(float factor) {
    return setMinHeight(shrink(pref().height, factor));
  }

  /* *** Grow options *** */
  /**
   * Allow component to grow to two times of the current preferred size.
   */
  public CSIZE growSize() {
    return growSize(2.0f, 2.0f);
  }

  /**
   * Allow component to grow to given width- and height-factors of the current preferred size.
   */
  public CSIZE growSize(float widthFactor, float heightFactor) {
    return growWidth(widthFactor).growHeight(heightFactor);
  }

  /**
   * Allow component to grow in width to two times the current preferred width.
   */
  public CSIZE growWidth() {
    return growWidth(2.0f);
  }

  /**
   * Allow component to grow in width to given factor times the current preferred width.
   */
  public CSIZE growWidth(float factor) {
    return setMaxWidth(grow(pref().width, factor));
  }

  /**
   * Allow component to grow in height to two times the current preferred height.
   */
  public CSIZE growHeight() {
    return growHeight(2.0f);
  }

  /**
   * Allow component to grow in height to given factor times the current preferred height.
   */
  public CSIZE growHeight(float factor) {
    return setMaxHeight(grow(pref().height, factor));
  }

  /* *** Scale options *** */
  /**
   * Multiply all sizes times the given factors for width and height.
   */
  public CSIZE scale(float widthFactor, float heightFactor) {

    min(CSizeUtils.scale(min(), widthFactor, heightFactor));
    pref(CSizeUtils.scale(pref(), widthFactor, heightFactor));
    setMax(CSizeUtils.scaleMax(max(), widthFactor, heightFactor));
    return me();
  }

  /**
   * Multiply all width sizes times the given factor.
   */
  public CSIZE scaleWidth(float factor) {
    return scale(factor, 1.0f);
  }

  /**
   * Multiply all height sizes times the given factor.
   */
  public CSIZE scaleHeight(float factor) {
    return scale(1.0f, factor);
  }

}
