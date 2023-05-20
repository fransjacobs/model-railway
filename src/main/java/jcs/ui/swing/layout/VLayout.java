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
import java.util.HashMap;
import java.util.Map;

/**
 * A layout-manager that aligns components vertically and shrinks or grows components vertically as specified by their minimum,
 * preferred and maximum sizes. Use {@code VBox.add(Component)} to put each added component beneath the previous one and let this
 * layout manager do the vertical resizing.
 * <p>
 * The component's sizes must be set properly for this layout manager to work. Use {@code HVProps.setSize()} to accomplish this. Use
 * {@code HVProps} many default values to change to appearance of the screen.
 * <p>
 * Components shrink vertically to their minimum size when their preferred size is larger then their minimum size.<br>
 * Likewise, components will grow vertically when their maximum size is greater then their preferred size.<br>
 * Also, components shrink and grow relative to how much other components want to shrink or grow. For example, if one text area (A)
 * needs to be twice as big as another text area (B), then simply setting the preferred height of B to twice the preferred height of
 * A will do the trick. See for example {@code TestUI} which calls
 * <br> {@code HVProps.getScroller(text, 0, 3, false)} and
 * <br> (@code HVProps.getScroller(text, 0, 6, false)} to show this effect.
 * <p>
 * This class determines the size for components vertically. The {@code HLayout} class determines the size for components
 * horizontally and can be used together with HBox to order components in the reading direction, from the middle or opposite the
 * reading direction.
 * <p>
 * Other layour managers should have no trouble with this layout manager. In other words, {@code VBox and HBox} can be part of a
 * component that uses BorderLayout or Flowlayout, etc.
 * <p>
 * Add instances of {@code VBox} (which uses {@code VLayout}) and {@code HBox} (which uses {@code HLayout}) to the content pane of
 * frames and dialogs (and/or make a {@code VBox} with insets the content pane).
 * <p>
 * Developers note: VLayout and HLayout must be kept in sync to work.
 */
public class VLayout extends HVLayout {

  /**
   * Contructor to layout components from top to bottom.
   */
  public VLayout() {
    this(null);
  }

  /**
   * Contructor to layout components from top to bottom.
   *
   * @param props if null, {@link HVSize#getDefault()} is used.
   */
  public VLayout(HVSize props) {
    super(props);
  }

  /**
   * Set the general alignment to a value that corresponds to the line axis orientation, i.e.
   * <br> left top for left to right and
   * <br> right top for right to left
   */
  @Override
  public float getLayoutAlignmentX(Container parent) {

    final boolean leftToRight = parent.getComponentOrientation().isLeftToRight();
    float f = 0.25f;
    if (!leftToRight) {
      f = 0.75f;
    }
    return f;
  }

  /**
   * defaults to "near the top"
   */
  @Override
  public float getLayoutAlignmentY(Container c) {
    // near the top
    return 0.25f;
  }

  /**
   * Return minimum size (width and height) of this container The minimum size of this layout calculated by summing up all minimum
   * sizes of the components in this layout.
   * <br> The container will refuse to draw smaller.
   * <br> Also sets varMinHeight which is used in the layoutContainer method.
   */
  @Override
  public Dimension minimumLayoutSize(Container target) {

    return minimumLayoutSize(target, true);
  }

  /**
   * The preferred size of this layout calculated by summing up all preferred sizes of the components in this layout.
   */
  @Override
  public Dimension preferredLayoutSize(Container target) {

    return preferredLayoutSize(target, true);
  }

  /**
   * Return the maximum size (width and height) of this container The maximum size of this layout calculated by summing up all
   * maximum sizes of the components in this layout.
   * <br> The container will refuse to draw larger
   */
  @Override
  public Dimension maximumLayoutSize(Container target) {

    if (dmax != null) {
      return new Dimension(dmax.width, dmax.height);
    }

    int height = getInsets(target, true);
    int maxWidth = 0;
    synchronized (target.getTreeLock()) {
      Dimension cmax;
      for (int i = 0; i < parts.size(); i++) {
        cmax = parts.get(i).getMaximumSize();
        height += Math.min(cmax.height, HVSize.MAX_HEIGHT);
        maxWidth = Math.max(maxWidth, cmax.width);
      }
    }
    maxWidth += getInsets(target, false);
    height = height + gapHeight();
    Dimension d = new Dimension(maxWidth, height);
    dmax = new Dimension(d.width, d.height);
    return d;
  }

  /**
   * Determines size and position of all components/lines according to the available size given via the target parameter. Minimum
   * and maximum size of components will be respected so they may be non-visible if the target is too small (component falls outside
   * of Window). The frame.pack() funtion usually prevents this.
   */
  @Override
  public void layoutContainer(Container target) {

    /*
		 * TODO: un-duplicate code with Hlayout#layoutContainer.
		 * A lot of code is similar, but not easy to share.
     */
    // The target sizes to respect:
    final Insets targetInsets = target.getInsets();
    final int insetsWidth = targetInsets.right + targetInsets.left;
    // final int insetsHeight = targetInsets.top + targetInsets.bottom;
    // The maximum width space to divide over the components
    int maxWidth = target.getWidth() - insetsWidth;
    // The maximum height space to divide over the components
    int maxHeight = target.getHeight();

    synchronized (target.getTreeLock()) {
      final Dimension dminimum = minimumLayoutSize(target); // sets varMinHeight 
      final Dimension dpreferred = preferredLayoutSize(target); // sets varMaxHeight
      // respect the minimum sizes
      maxWidth = Math.max(maxWidth, dminimum.width - insetsWidth);
      maxHeight = Math.max(maxHeight, dminimum.height);
      // Check if minimum size is required
      final boolean smallest = maxHeight <= dminimum.height;
      // Check if shrinking is required, varMinHeight > 0 determines if component can shrink
      final boolean shrink = (!smallest) && (maxHeight < dpreferred.height) && (varMinSize > 0);
      // Check if growing is required, dpreferred.height  > staticMaxHeight determines if components can grow 
      boolean grow = !smallest && !shrink && (maxHeight > dpreferred.height) && (varMaxSize > 0);

      // Components that can grow but to a certain height, are static once they reach that max-height.
      // Adjust the calculations for growing components by removing these "reached max height" components.
      int varMaxHeightUsed = varMaxSize;
      int maxHeightStatic = 0;
      final Map<Component, Integer> grownMaxHeight = new HashMap<Component, Integer>();
      if (grow) {
        boolean partMaxGrown = true;
        // Every time a component is removed from the grow-list
        // all growing components need to be re-evaluated with the new grow-height to divide.
        // There can still be a little jump from "almost max height" to "max height".
        while (partMaxGrown) {
          partMaxGrown = false;
          for (int i = 0; i < parts.size(); i++) {
            final Component c = parts.get(i);
            if (grownMaxHeight.containsKey(c)) {
              continue;
            }
            Dimension cpref = c.getPreferredSize();
            Dimension cmax = c.getMaximumSize();
            if (cmax.height > cpref.height) {
              int extra = (int) ((maxHeight - dpreferred.height) * ((float) cpref.height / varMaxHeightUsed));
              if (extra > cmax.height - cpref.height) {
                varMaxHeightUsed -= cpref.height;
                grownMaxHeight.put(c, cmax.height);
                maxHeightStatic += (cmax.height - cpref.height);
                partMaxGrown = true;
              }
            }
          } // for parts
        } // while part grown max.
        grow = (varMaxHeightUsed > 0);
      }
      final int maxGrowHeightToDivide = maxHeight - dpreferred.height - maxHeightStatic;

      // lineHeight determines where the new vertical component position is. 
      int lineHeight = targetInsets.top;
      for (int i = 0; i < parts.size(); i++) {
        final Component c = parts.get(i);
        Dimension cmin = c.getMinimumSize();
        Dimension cpref = c.getPreferredSize();
        Dimension cmax = c.getMaximumSize();
        // Protection against JTextField with column size. 
        ensureSmall(cmin, cpref);
        int cwidth = cmin.width;
        if (maxWidth > cmin.width) {
          if (maxWidth > cpref.width) {
            if (cmax.width > cpref.width) {
              cwidth = Math.min(maxWidth, cmax.width);
            } else {
              cwidth = cpref.width;
            }
          } else {
            cwidth = Math.min(maxWidth, cpref.width);
          }
        }

        // hasStaticHeight determines if the componet can grow or shrink
        // true is default value for smallest = true
        boolean hasStaticHeight = true;
        if (!smallest) {
          if (shrink && cmin.height < cpref.height) {
            hasStaticHeight = false;
          } else if (grow && cmax.height > cpref.height && !grownMaxHeight.containsKey(c)) {
            hasStaticHeight = false;
          }
        }
        // Set default value for component/line height
        int cheight = (smallest ? cmin.height
                : grownMaxHeight.get(c) != null ? grownMaxHeight.get(c) : cpref.height);
        if (!hasStaticHeight) {
          if (shrink) {
            // extra = space to divide * percentage of variable height of this component
            // cast to float is required to get accurate calculation
            // cast to int always rounds down which is also required,
            // if Math.round() is used, components grow too big.
            int extra = (int) ((maxHeight - dminimum.height) * ((cpref.height - cmin.height) / (float) varMinSize));
            cheight = cmin.height + extra;
          }
          if (grow) {
            // extra = space to divide * percentage of variable height of this component
            int extra = (int) (maxGrowHeightToDivide * (cpref.height / (float) varMaxHeightUsed));
            cheight = cpref.height + extra;
          }
        }

        c.setBounds(targetInsets.left, lineHeight, cwidth, cheight);
        lineHeight += cheight + props.getVerticalGap();
      }
    }
  }

}
