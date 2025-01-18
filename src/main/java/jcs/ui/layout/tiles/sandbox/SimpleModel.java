//  SimpleModel.java
// An example of a custom data model that could be used in any MVC
// scenario.
//
package jcs.ui.layout.tiles.sandbox;

import javax.swing.event.*;

public class SimpleModel implements SimpleModelInterface {

  protected transient ChangeEvent changeEvent = null;
  protected EventListenerList listenerList = new EventListenerList();

  private int value = 0;
  private boolean activated = false;

  public SimpleModel() {
  }

  public SimpleModel(int v) {
    value = v;
  }

  public SimpleModel(boolean b) {
    activated = b;
  }

  public SimpleModel(int v, boolean b) {
    value = v;
    activated = b;
  }

  public int getValue() {
    return value;
  }

  public synchronized void setValue(int v) {
    if (v != value) {
      value = v;
      fireChange();
    }
  }

  public boolean isActivated() {
    return activated;
  }

  public synchronized void setActivated(boolean b) {
    if (b != activated) {
      activated = b;
      fireChange();
    }
  }

  public void addChangeListener(ChangeListener l) {
    listenerList.add(ChangeListener.class, l);
  }

  public void removeChangeListener(ChangeListener l) {
    listenerList.remove(ChangeListener.class, l);
  }

  public ChangeListener[] getChangeListeners() {
    return (ChangeListener[]) listenerList.getListeners(ChangeListener.class);
  }

  protected void fireChange() {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ChangeListener.class) {
        if (changeEvent == null) {
          changeEvent = new ChangeEvent(this);
        }
        ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
      }
    }
  }

  public String toString() {
    String modelString = "value=" + getValue() + ", "
            + "activated=" + isActivated();
    return getClass().getName() + "[" + modelString + "]";
  }
}
