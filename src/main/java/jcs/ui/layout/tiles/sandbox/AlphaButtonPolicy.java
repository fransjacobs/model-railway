//  AlphaButtonPolicy.java
// A custom focus traversal policy that uses alphabetical ordering of button
// labels to determine "next" and "previous" buttons for keyboard traversal.
//
package jcs.ui.layout.tiles.sandbox;

import java.awt.*;
import java.util.*;
import javax.swing.*;

public class AlphaButtonPolicy extends FocusTraversalPolicy {

  private SortedMap getSortedButtons(Container focusCycleRoot) {
    if (focusCycleRoot == null) {
      throw new IllegalArgumentException("focusCycleRoot can't be null");
    }
    SortedMap result = new TreeMap();  // Will sort all buttons by text.
    sortRecursive(result, focusCycleRoot);
    return result;
  }

  private void sortRecursive(Map buttons, Container container) {
    for (int i = 0; i < container.getComponentCount(); i++) {
      Component c = container.getComponent(i);
      if (c instanceof JButton) {  // Found another button to sort.
        buttons.put(((JButton) c).getText(), c);
      }
      if (c instanceof Container) {  // Found a container to search.
        sortRecursive(buttons, (Container) c);
      }
    }
  }

  // The rest of the code implements the FocusTraversalPolicy interface.
  public Component getFirstComponent(Container focusCycleRoot) {
    SortedMap buttons = getSortedButtons(focusCycleRoot);
    if (buttons.isEmpty()) {
      return null;
    }
    return (Component) buttons.get(buttons.firstKey());
  }

  public Component getLastComponent(Container focusCycleRoot) {
    SortedMap buttons = getSortedButtons(focusCycleRoot);
    if (buttons.isEmpty()) {
      return null;
    }
    return (Component) buttons.get(buttons.lastKey());
  }

  public Component getDefaultComponent(Container focusCycleRoot) {
    return getFirstComponent(focusCycleRoot);
  }

  public Component getComponentAfter(Container focusCycleRoot,
          Component aComponent) {
    if (!(aComponent instanceof JButton)) {
      return null;
    }
    SortedMap buttons = getSortedButtons(focusCycleRoot);
    // Find all buttons after the current one.
    String nextName = ((JButton) aComponent).getText() + "\0";
    SortedMap nextButtons = buttons.tailMap(nextName);
    if (nextButtons.isEmpty()) {  // Wrapped back to beginning
      if (!buttons.isEmpty()) {
        return (Component) buttons.get(buttons.firstKey());
      }
      return null;  // Degenerate case of no buttons.
    }
    return (Component) nextButtons.get(nextButtons.firstKey());
  }

  public Component getComponentBefore(Container focusCycleRoot,
          Component aComponent) {
    if (!(aComponent instanceof JButton)) {
      return null;
    }

    SortedMap buttons = getSortedButtons(focusCycleRoot);
    SortedMap prevButtons
            = // Find all buttons before this one.
            buttons.headMap(((JButton) aComponent).getText());
    if (prevButtons.isEmpty()) {  // Wrapped back to end.
      if (!buttons.isEmpty()) {
        return (Component) buttons.get(buttons.lastKey());
      }
      return null;  // Degenerate case of no buttons.
    }
    return (Component) prevButtons.get(prevButtons.lastKey());
  }
}
