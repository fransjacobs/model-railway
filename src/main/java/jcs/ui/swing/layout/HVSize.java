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
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.JTextField;

/**
 * Default sizes used by HVLayout when no (preferred) sizes are specified. All sizes are based on {@link #getLineHeight()}. Use
 * {@link #alignLineHeight(HVSize)} to update the line-size according to used font.
 *
 * @author fred
 *
 */
public class HVSize {

  public static Dimension SCREEN_SIZE_PRIMARY = Toolkit.getDefaultToolkit().getScreenSize();
  public static Dimension SCREEN_SIZE_TOTAL = calculateTotalScreenSize();

  public static Dimension calculateTotalScreenSize() {

    Rectangle virtualBounds = new Rectangle();
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();

    for (GraphicsDevice gd : gs) {
      GraphicsConfiguration[] gc = gd.getConfigurations();
      for (GraphicsConfiguration gc1 : gc) {
        virtualBounds = virtualBounds.union(gc1.getBounds());
      }
    }
    Dimension d = new Dimension();
    d.width = virtualBounds.width;
    d.height = virtualBounds.height;
    return d;
  }

  /**
   * Maximum width calculated from total width of all screens.
   */
  public static int MAX_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;

  /**
   * Maximum height calculated from height of all screens.
   */
  public static int MAX_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;

  private static HVSize defaultInstance = new HVSize();

  static {
    alignLineHeight(new HVSize());
  }

  /**
   * Default instance is used by HVLayout when no instance of this class is specified.
   *
   * @return
   */
  public static HVSize getDefault() {
    return defaultInstance;
  }

  public static void setDefault(HVSize defaultInstance) {
    if (defaultInstance != null) {
      HVSize.defaultInstance = defaultInstance;
    }
  }

  /**
   * Aligns {@link #getDefault()} to the preferred height of a {@link JTextField}.
   */
  public static void alignLineHeight() {
    alignLineHeight(getDefault());
  }

  /**
   * Aligns given {@link HVSize} to the preferred height of a {@link JTextField}.
   *
   * @param p
   */
  public static void alignLineHeight(HVSize p) {
    alignLineHeight(p, new JTextField());
  }

  /**
   * Aligns given {@link HVSize} to the preferred height of the given {@link Component}.Also updates gaps.
   *
   * @param p
   * @param c
   */
  public static void alignLineHeight(HVSize p, Component c) {
    p.setLineHeight(getComponentHeight(c));
    // add 0.5f so int-value is always round-up.
    int i = (int) (p.getLineHeight() * p.getVerticalGapFactor() + 0.5f);
    p.setVerticalGap(i);
    i = (int) (p.getLineHeight() * p.getHorizontalGapFactor() + 0.5f);
    p.setHorizontalGap(i);
  }

  /**
   * @param c
   * @return The components preferred height, tweaked to accomodate small font sizes.
   */
  public static int getComponentHeight(Component c) {

    // One extra is needed for small font sizes like 11 and 8.
    int tweakHeight = (c.getFont().getSize() % 3 == 0 ? 0 : 1);
    return c.getPreferredSize().height + tweakHeight;
  }

  private int lineHeight = 20;
  private int horizontalGap = 4;
  private int verticalGap = 3;
  private float horizontalGapFactor = 0.20f;
  private float verticalGapFactor = 0.15f;
  private int lineHeightDepthDelta = 4;
  private float buttonWidthFactor = 4f;
  private float lineWidthFactor = 8f;

  public HVSize() {
    super();
  }

  /**
   * @return true if this is {@link #getDefault()}.
   */
  public boolean isDefault() {
    return (getDefault() == this);
  }

  /* *** getters and setters *** */
  public int getLineHeight() {
    return lineHeight;
  }

  public void setLineHeight(int lineHeight) {
    this.lineHeight = lineHeight;
  }

  public int getLineHeightNoDepth() {
    return getLineHeight() - getLineHeightDepthDelta();
  }

  public int getHorizontalGap() {
    return horizontalGap;
  }

  public void setHorizontalGap(int horizontalGap) {
    this.horizontalGap = horizontalGap;
  }

  public int getVerticalGap() {
    return verticalGap;
  }

  public void setVerticalGap(int verticalGap) {
    this.verticalGap = verticalGap;
  }

  public int getLineHeightDepthDelta() {
    return lineHeightDepthDelta;
  }

  /**
   * Text displayed without depth-effects needs less room.This is the case for table-rows and multi-line labels. These depth-effects
   * usually require 4 pixels. The {@link #getLineHeightNoDepth()} is the same as {@link #getLineHeight()} minus this value. This
   * value is not final to accomodate layouts that use different depth-effects.
   *
   * @param lineHeightDepthDelta
   */
  public void setLineHeightDepthDelta(int lineHeightDepthDelta) {
    this.lineHeightDepthDelta = lineHeightDepthDelta;
  }

  public void setButtonWidthFactor(int buttonWidthFactor) {
    this.buttonWidthFactor = buttonWidthFactor;
  }

  public int getButtonWidth() {
    return (int) (getLineHeight() * getButtonWidthFactor());
  }

  public int getLineWidth() {
    return (int) (getLineHeight() * getLineWidthFactor());
  }

  public float getHorizontalGapFactor() {
    return horizontalGapFactor;
  }

  public void setHorizontalGapFactor(float horizontalGapFactor) {
    this.horizontalGapFactor = horizontalGapFactor;
  }

  public float getVerticalGapFactor() {
    return verticalGapFactor;
  }

  public void setVerticalGapFactor(float verticalGapFactor) {
    this.verticalGapFactor = verticalGapFactor;
  }

  public float getButtonWidthFactor() {
    return buttonWidthFactor;
  }

  public void setButtonWidthFactor(float buttonWidthFactor) {
    this.buttonWidthFactor = buttonWidthFactor;
  }

  public float getLineWidthFactor() {
    return lineWidthFactor;
  }

  public void setLineWidthFactor(float lineWidthFactor) {
    this.lineWidthFactor = lineWidthFactor;
  }

}
