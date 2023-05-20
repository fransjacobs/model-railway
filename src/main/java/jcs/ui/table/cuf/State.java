package jcs.ui.table.cuf;

import javax.swing.event.ChangeListener;
import java.util.List;

/**
 * The State interface describes a (more-or-less) binary state.<br>
 * Whenever the state changes from not initialized to true/false, from false to true or from true to false, all attached change
 * listeners are notified.<br>
 * If a state changes from either true or false to uninitialized, its listeners are <b>not</b> notified.<br>
 * The source in the ChangeEvent can always be casted to State.
 */
public interface State {

  /**
   * Return the name of the state.
   *
   * @return the name of the state (never null)
   */
  String getName();

  /**
   * Set the name of the state.
   *
   * @param pName the name of the state (must not be null)
   * @throws IllegalArgumentException if pState is null
   */
  void setName(String pName);

  /**
   * Add a listener. If we already know the listener, we do nothing.
   *
   * @param pStateChangeListener the listener to be added
   */
  void addChangeListener(ChangeListener pStateChangeListener);

  /**
   * Remove a listener. If we don't know the listener, we do nothing.
   *
   * @param pStateChangeListener the listener to be removed
   */
  void removeChangeListener(ChangeListener pStateChangeListener);

  /**
   * Checks if we are in a defined state (either enabled or disabled).
   *
   * @return true if we have a defined state, false otherwise
   */
  boolean isInitialized();

  /**
   * Checks the state.
   *
   * @return true if we are enabled, false if we are disabled or not initialized
   */
  boolean isEnabled();

  /**
   * During a callback, a listener can call this method to get a reason object for the change. If we are not in the stateChanged()
   * callback method or we don't know the reason for the change, null is returned.
   *
   * @return null or the reason of a change
   */
  Object getChangeReason();

  /**
   * Cleanup all resources: disconnect from any input sources and remove all listeners.<br>
   * Any method besides isDisposed, dispose or getName will throw an IllegalStateException after the State was disposed.
   */
  void dispose();

  /**
   * Check if the state is disposed.
   *
   * @return true if the state is disposed (= no longer valid)
   */
  boolean isDisposed();

  /**
   * Return a immutable list of all direct dependents of the state.
   *
   * @return an immutable List of all direct dependents
   */
  List<ChangeListener> getDependents();

}
