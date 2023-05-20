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
import jcs.ui.swing.layout.fluent.BaseCSize3;
import jcs.ui.swing.layout.fluent.BaseCSize4;
import jcs.ui.swing.layout.fluent.BaseCSize5;

/**
 * Adjust Component Size based on {@link HVSize} defaults so that HVlayout can layout components properly.
 * <p>
 * This class is an end-point for the fluent class {@link BaseCSize4}. Try not to extend this class (that will break the fluent API
 * hierarchy), instead extend {@link BaseCSize4} in the same manner that {@link BaseCSize4} extends {@link BaseCSize3}.
 * <br>Then create a 'concrete' class similar to this class.
 *
 * @author fred
 *
 */
public class CSize extends BaseCSize5<CSize, Component> {

  private static CSize defaultInstance = new CSize();

  public static CSize getDefault() {
    return defaultInstance;
  }

  public static void setDefault(CSize defaultInstance) {
    if (defaultInstance != null) {
      CSize.defaultInstance = defaultInstance;
    }
  }

  public CSize() {
    this(null, null);
  }

  public CSize(Component c) {
    this(null, c);
  }

  public CSize(HVSize props) {
    this(props, null);
  }

  public CSize(HVSize props, Component c) {
    super();
    if (props != null) {
      setHvsize(props);
    }
    set(c);
  }

}
