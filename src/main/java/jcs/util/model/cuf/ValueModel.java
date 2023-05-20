package jcs.util.model.cuf;

import javax.swing.event.ChangeListener;
import java.util.List;

/**
 * Java version of the Smalltalk/VisualWorks Value Model framework. 
 * See http://c2.com/ppr/vmodels.html for an introduction of the Smalltalk version.
 * 
 * Because i was not sure what is the "right" listener registration method, i included both the smalltalk-like onChangeSend() as
 * well as the Java-like addChangeListener() method.
 * In any case, a change of the value of a ValueModel object will call the dependent method with a ChangeEvent as sole argument, the
 * source of the event is always the ValueModel firing the event.
 *
 * @param <T> the type we contain as our value
 */
public interface ValueModel<T> {

  /*
     * access stuff
   */

  /**
   * Get the current value, during a callback this is the new value.
   *
   * @return null or the value object
   */
  public T getValue();

  /**
   * Set a new value in this model. The method will fire a ChangeEvent to its listeners if the new value is different from the old
   * value. If a ChangeEvent should always be sent, use #setValueForced.
   *
   * @param pValue the new value (null is o.k.)
   * @throws UnsupportedOperationException if this ValueModel is not mutable
   */
  public void setValue(T pValue);

  /**
   * Needed since we generified everything, if we have no type given.
   *
   * @param pValue the value to be casted and set as new value
   */
  public void setObjectValue(Object pValue);

  /**
   * Set a new value in this model. The method will always fire a ChangeEvent to its listeners, even if the new value equals the old
   * value.
   *
   * @param pValue the new value (null is o.k.)
   * @throws UnsupportedOperationException if this ValueModel is not mutable
   */
  public void setValueForced(T pValue);

  /**
   * Set a new value, either forced or without force. The method might fire a ChangeEvent to its listeners.
   *
   * @param pValue the new value (null is o.k.)
   * @param pIsSetForced true if a forced setValue should be done (see #setValueForced for details)
   * @throws UnsupportedOperationException if this ValueModel is not mutable
   */
  public void setValue(T pValue, boolean pIsSetForced);

  /**
   * Returns true if setValueForced() was called, only useful during a callback.
   *
   * @return false if we are not inside a callback or a "normal" setValue() was issued
   */
  public boolean isSetForced();

  /**
   * Returns true if a setValue() is possible, and doesn't throw an UnsupportedOperationException,
   *
   * @return true if setValue() can be called
   */
  public boolean isEditable();

  /*
     * observer stuff
   */
  /**
   * Add an dependent listener to a ValueModel, "Smalltalk style".
   *
   * @param pDependent the object to call
   * @param pMethodName the method name to call back
   * @throws IllegalArgumentException if pDependent or pMethod is null, or no method with the signature "public void
   * pMethodName(ChangeEvent e)" is found for pDependent.
   */
  public void onChangeSend(Object pDependent, String pMethodName);

  /**
   * Add an dependent listener to a ValueModel, "Java style".
   *
   * @param pDependent the object to call
   * @throws IllegalArgumentException if pDependent is null
   */
  public void addChangeListener(ChangeListener pDependent);

  /**
   * Remove a dependent listener, "Smalltalk style". If pDependent is null or not known, nothing happens.
   *
   * @param pDependent object to de-register
   */
  public void retractInterestsFor(Object pDependent);

  /**
   * Remove a dependent listener, "Java style". If pDependent is null or not known, nothing happens.
   *
   * @param pDependent object to de-register
   */
  public void removeChangeListener(ChangeListener pDependent);

  /**
   * Cleanup all resources: disconnect from any input sources (like other ValueModel's ...), and remove all listeners.<br>
   * Any method besides isDisposed, dispose or getName will throw an IllegalStateException after the ValueModel was disposed.
   */
  public void dispose();

  /**
   * Check if the value model is disposed.
   *
   * @return true if the value model is disposed (= no longer valid)
   */
  public boolean isDisposed();

  /**
   * Return a immutable list of all direct dependents of the value model.
   *
   * @return an immutable List of all direct dependents
   */
  public List<Object> getDependents();


  /*
     * naming
   */
  /**
   * Return the name of the state.
   *
   * @return the name of the state (never null)
   */
  public String getName();

  /**
   * Set the name of the state.
   *
   * @param pName the name of the state (must not be null)
   * @throws IllegalArgumentException if pState is null
   */
  public void setName(String pName);


  /*
     * convenience access stuff
   */
  /**
   * Converts our value to a boolean. If the value is not an Boolean, the result of the toString() method is parsed. If the value is
   * null, false is returned.
   *
   * @return the boolean version of our value
   * @throws NumberFormatException if the value can't be converted to a boolean
   */
  public boolean booleanValue();

  /**
   * Converts the given <code>boolean</code> to an object and sets it as new value.
   *
   * @param pValue the value to be converted and set as new value
   */
  public void setValue(boolean pValue);

  /**
   * Converts our value to an int. If the value is not an Integer, the result of the toString() method is parsed. If the value is
   * null, zero is returned.
   *
   * @return the int version of our value
   * @throws NumberFormatException if the value can't be converted to a int
   */
  public int intValue();

  /**
   * Converts the given <code>int</code> to an object and sets it as new value.
   *
   * @param pValue the value to be converted and set as new value
   */
  public void setValue(int pValue);

  /**
   * Converts our value to an long. If the value is not an Long, the result of the toString() method is parsed. If the value is
   * null, zero is returned.
   *
   * @return the long version of our value
   * @throws NumberFormatException if the value can't be converted to a long
   */
  public long longValue();

  /**
   * Converts the given <code>long</code> to an object and sets it as new value.
   *
   * @param pValue the value to be converted and set as new value
   */
  public void setValue(long pValue);

  /**
   * Converts our value to a float. If the value is not a Float, the result of the toString() method is parsed. If the value is
   * null, zero is returned.
   *
   * @return the float version of our value
   * @throws NumberFormatException if the value can't be converted to a float
   */
  public float floatValue();

  /**
   * Converts the given <code>float</code> to an object and sets it as new value.
   *
   * @param pValue the value to be converted and set as new value
   */
  public void setValue(float pValue);

  /**
   * Converts our value to a double. If the value is not a Double, the result of the toString() method is parsed. If the value is
   * null, zero is returned.
   *
   * @return the double version of our value
   * @throws NumberFormatException if the value can't be converted to a double
   */
  public double doubleValue();

  /**
   * Converts the given <code>double</code> to an object and sets it as new value.
   *
   * @param pValue the value to be converted and set as new value
   */
  public void setValue(double pValue);

  /**
   * Converts our value to a String. If the value is not a String, the result of the toString() method is returned. If the value is
   * null, null is returned.
   *
   * @return the String version of our value
   */
  public String stringValue();
}
