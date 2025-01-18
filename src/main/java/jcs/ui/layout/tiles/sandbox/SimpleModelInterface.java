//  SimpleModelInteface.java
// An example of a data model interface that could be used in any MVC
// scenario.  This interface is implemented in the SimpleModel class.
//
package jcs.ui.layout.tiles.sandbox;

import javax.swing.event.*;

public interface SimpleModelInterface {

  public int getValue();

  public void setValue(int v);

  public boolean isActivated();

  public void setActivated(boolean b);

  public void addChangeListener(ChangeListener l);

  public void removeChangeListener(ChangeListener l);

  public ChangeListener[] getChangeListeners();
}
