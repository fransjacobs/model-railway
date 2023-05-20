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
 * Adds functions that set a component size and update related sizes so that
 * after a size change, the following holds true: <code>min <= pref <= max </code>  
 * @author fred
 */
public class BaseCSize2<CSIZE extends BaseCSize2<CSIZE, CTYPE>, CTYPE extends Component> 
	extends BaseCSize<CSIZE, CTYPE> {

	@SuppressWarnings("unchecked")
	@Override
	protected CSIZE me() { 
		return (CSIZE) this;	
	}

	/**
	 * Sets minimum width to button width, updates other sizes if needed.
	 */
	public CSIZE setMinWidthButton() { 
		return setMinWidth(props().getButtonWidth()); 
	}
	
	/**
	 * Sets minimum width scaled by button-width with given factor, updates other sizes if needed.
	 */
	public CSIZE setMinWidthButton(float factor) { 
		return setMinWidth(getButtonWidth(factor)); 
	}
	
	/** 
	 * Scales current minimum width, updates other sizes if needed. 
	 */
	public CSIZE setMinWidth(float factor) { 
		return setMinWidth(multiply(min().width, factor));	
	}
	
	/** 
	 * Sets minimum width, updates other sizes if needed. 
	 */
	public CSIZE setMinWidth(int l) {
		
		Dimension d = min();
		min(l, d.height);
		if ((d = pref()).width < l) {
			pref(l, d.height);
		}
		if ((d = max()).width < l) {
			max(l, d.height);
		}
		return me();
	}

	/**
	 * Sets minimum height to line-height, updates other sizes if needed.
	 */
	public CSIZE setMinHeightLine() { 
		return setMinHeight(props().getLineHeight()); 
	}
	
	/**
	 * Sets minimum height scaled by line-height with given factor, updates other sizes if needed.
	 */
	public CSIZE setMinHeightLine(float factor) { 
		return setMinHeight(getLineHeight(factor)); 
	}

	/** 
	 * Scales current minimum height, updates other sizes if needed. 
	 */
	public CSIZE setMinHeight(float factor) { 
		return setMinHeight(multiply(min().height, factor)); 
	}

	/** 
	 * Scales current minimum height, updates other sizes if needed. 
	 */
	public CSIZE setMinHeight(int l) {
		
		Dimension d = min();
		min(d.width, l);
		if ((d = pref()).height < l) {
			pref(d.width, l);
		}
		if ((d = max()).height < l) {
			max(d.width, l);
		}
		return me();
	}

	/**
	 * Sets min-size to button width and line height, updates other sizes if needed.
	 */
	public CSIZE setMinButton() { 
		return setMinWidthButton().setMinHeightLine(); 
	}
		
	/**
	 * Sets min-size to button width and line height, scaled by given factors. Updates other sizes if needed.
	 */
	public CSIZE setMinButton(float widthFactor, float heightFactor) {
		return setMinWidthButton(widthFactor).setMinHeightLine(heightFactor);
	}

	/**
	 * Scales current min-size by given factors, updates other sizes if needed.
	 */
	public CSIZE setMin(float widthFactor, float heightFactor) {
		return setMinWidth(widthFactor).setMinHeight(heightFactor);
	}

	/**
	 * Sets min-size, updates other sizes if needed. 
	 */
	public CSIZE setMin(Dimension d) { 
		return setMin(d.width, d.height); 
	}

	/**
	 * Sets min-size, updates other sizes if needed. 
	 */
	public CSIZE setMin(int w, int h) {
		return setMinWidth(w).setMinHeight(h);
	}

	/**
	 * Sets preferred width to button width, updates other sizes if needed.
	 */
	public CSIZE setPrefWidthButton() { 
		return setPrefWidth(props().getButtonWidth()); 
	}

	/**
	 * Sets preferred width scaled by button-width with given factor, updates other sizes if needed.
	 */
	public CSIZE setPrefWidthButton(float factor) {
		return setPrefWidth(getButtonWidth(factor));
	}
	
	/** 
	 * Scales current preferred width, updates other sizes if needed. 
	 */
	public CSIZE setPrefWidth(float factor) { 
		return setPrefWidth(multiply(pref().width, factor));	
	}

	/** 
	 * Sets preferred width, updates other sizes if needed. 
	 */
	public CSIZE setPrefWidth(int l) {
		
		Dimension d = pref();
		pref(l, d.height);
		if ((d = max()).width < l) {
			max(l, d.height);
		}
		if ((d = min()).width > l) {
			min(l, d.height);
		}
		return me();
	}
	
	/**
	 * Sets preferred height to line-height, updates other sizes if needed.
	 */
	public CSIZE setPrefHeightLine() { 
		return setPrefHeight(props().getLineHeight()); 
	}

	/**
	 * Sets preferred height scaled by line-height with given factor, updates other sizes if needed.
	 */
	public CSIZE setPrefHeightLine(float factor) {
		return setPrefHeight(getLineHeight(factor));
	}

	/** 
	 * Scales current preferred height, updates other sizes if needed. 
	 */
	public CSIZE setPrefHeight(float factor) { 
		return setPrefHeight(multiply(pref().height, factor));	
	}

	/** 
	 * Sets preferred height, updates other sizes if needed. 
	 */
	public CSIZE setPrefHeight(int l) {
		
		Dimension d = pref();
		pref(d.width, l);
		if ((d = max()).height < l) {
			max(d.width, l);
		}
		if ((d = min()).height > l) {
			min(d.width, l);
		}
		return me();
	}
	
	/**
	 * Sets pref-size to button width and line height, updates other sizes if needed.
	 */
	public CSIZE setPrefButton() {
		return setPrefWidthButton().setPrefHeightLine();
	}
	
	/**
	 * Sets pref-size to button width and line height, scaled by given factors. Updates other sizes if needed.
	 */
	public CSIZE setPrefButton(float widthFactor, float heightFactor) {
		return setPrefWidthButton(widthFactor).setPrefHeightLine(heightFactor);
	}

	/**
	 * Updates current pref-size scaled by given factors, updates other sizes if needed.
	 */
	public CSIZE setPref(float widthFactor, float heightFactor) {
		return setPrefWidth(widthFactor).setPrefHeight(heightFactor);
	}

	/**
	 * Sets pref-size, updates other sizes if needed.
	 */
	public CSIZE setPref(Dimension d) {
		return setPref(d.width, d.height);
	}

	/**
	 * Sets pref-size, updates other sizes if needed.
	 */
	public CSIZE setPref(int w, int h) {
		return setPrefWidth(w).setPrefHeight(h);
	}

	/**
	 * Sets maximum width to button width, updates other sizes if needed.
	 */
	public CSIZE setMaxWidthButton() { 
		return setMaxWidth(props().getButtonWidth()); 
	}

	/**
	 * Sets maximum width scaled by button-width with given factor. Updates other sizes if needed.
	 */
	public CSIZE setMaxWidthButton(float factor) {
		return setMaxWidth(getButtonWidth(factor));
	}

	/** 
	 * Multiplies current PREFERRED width with given factor and sets it as maximum size.
	 * Updates other sizes if needed. 
	 */
	public CSIZE setMaxWidth(float factor) { 
		return setMaxWidth(multiply(pref().width, factor));	
	}

	/**
	 * Sets maximum width, updates other sizes if needed.
	 */
	public CSIZE setMaxWidth(int l) {
		
		Dimension d = max();
		max(l, d.height);
		if ((d = pref()).width > l) {
			pref(l, d.height);
		}
		if ((d = min()).width > l) {
			min(l, d.height);
		}
		return me();
	}
	
	/**
	 * Sets maximum height to line-height, updates other sizes if needed.
	 */
	public CSIZE setMaxHeightLine() { 
		return setMaxHeight(props().getLineHeight()); 
	}

	/**
	 * Sets maximum height scaled by line-height with given factor, updates other sizes if needed.
	 */
	public CSIZE setMaxHeightLine(float factor) {
		return setMaxHeight(getLineHeight(factor));
	}

	/** 
	 * Multiplies current PREFERRED height with given factor and sets it as maximum size.
	 * Updates other sizes if needed. 
	 */
	public CSIZE setMaxHeight(float factor) { 
		return setMaxHeight(multiply(pref().height, factor));	
	}

	/**
	 * Sets maximum height, updates other sizes if needed.
	 */
	public CSIZE setMaxHeight(int l) {
		
		Dimension d = max();
		max(d.width, l);
		if ((d = pref()).height > l) {
			pref(d.width, l);
		}
		if ((d = min()).height > l) {
			min(d.width, l);
		}
		return me();
	}
	
	/**
	 * Sets maximum height to button width and line heigth, updates other sizes if needed.
	 */
	public CSIZE setMaxButton() {
		return setMaxWidthButton().setMaxHeightLine();
	}
	
	/**
	 * Sets maximum height to button width and line heigth scaled by given factors, c
	 */
	public CSIZE setMaxButton(float widthFactor, float heightFactor) {
		return setMaxWidthButton(widthFactor).setMaxHeightLine(heightFactor);
	}

	/** 
	 * Multiplies current PREFERRED size with given factors and sets it as maximum size.
	 * Updates other sizes if needed. 
	 */
	public CSIZE setMax(float widthFactor, float heightFactor) {
		return setMaxWidth(widthFactor).setMaxHeight(heightFactor);
	}

	/**
	 * Sets maximum size, updates other sizes if needed.
	 */
	public CSIZE setMax(Dimension d) {
		return setMax(d.width, d.height);
	}

	/**
	 * Sets maximum size, updates other sizes if needed.
	 */
	public CSIZE setMax(int w, int h) {
		return setMaxWidth(w).setMaxHeight(h);
	}

}
