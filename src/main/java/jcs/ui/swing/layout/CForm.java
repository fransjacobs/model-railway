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
import java.awt.Insets;
import jcs.ui.swing.layout.fluent.BaseCForm;
import jcs.ui.swing.layout.fluent.BaseCSize3;
import jcs.ui.swing.layout.fluent.BaseCSize4;

/**
 * Helper class for building a form.
 * With this class a leaf of the container tree is active
 * and can be changed by adding a child or going up the tree.
 * 
 * This class is an end-point for the fluent class {@link BaseCForm}.
 * Try not to extend this class (that will break the fluent API hierarchy), instead extend {@link BaseCForm}
 * in the same manner that {@link BaseCSize4} extends {@link BaseCSize3}.
 * <br>Then create a 'concrete' class similar to this class.
 * @author fred
 *
 */
public class CForm extends BaseCForm<CForm, CSize, Component> {

	/**
	 * The root-level box in a content-pane needs insets to keep text from sticking
	 * to the edges. The insets here give components a little distance from the window edges/borders.
	 * <br>Example code: {@code CForm form = new CForm(new VBox(CForm.MAIN_BOX_INSETS));}
	 *  
	 */
	public static Insets MAIN_BOX_INSETS = new Insets(2, 4, 2, 4);
	
	public CForm() {
		this(null, null);
	}

	public CForm(Container root) {
		this(root, null);
	}

	public CForm(Container root, CSize cs) {
		super();
		setRoot(root);
		setCSize(cs == null ? CSize.getDefault() : cs);
	}
	
}
