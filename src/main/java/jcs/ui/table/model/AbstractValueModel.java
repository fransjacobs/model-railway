package jcs.ui.table.model;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import jcs.util.model.cuf.ExternalUpdate;
import jcs.util.model.cuf.ValueModel;

/**
 * Abstract base class for a ValueModel, does the listener handling. The methods of this class are not threadsave, but in an UI
 * environment that's o.k.. A derived class needs only to implement the methods <ul>
 * <li> void setValue(Object pValue, boolean pIsSetForced); and
 * <li> Object getValue();
 * </ul>
 * Inside the derived class' setValue() method the code may check if the value changed at all if the pIsSetForced flag is false. If
 * there was no change (and pIsSetForced is false), the method may return immediately.<br>
 * If pIsSetForced if true, or the new and old value differ, the setValue() method should do the following tasks: <ol>
 * <li> set the value
 * <li> call the setInSetValue(true, pIsSetForced) method
 * <li> call the fireStateChanged() method in a try/finally block
 * <li> call the setInSetValue(false, pIsSetForced) method in the finally block
 * </ol>
 * In the getValue() method, a derived class has not to do any special handling.
 *
 * @param <T> the type of the value we contain
 */
public abstract class AbstractValueModel<T> implements ValueModel<T>, ExternalUpdate {

  /**
   * our "Java style" listeners
   */
  private ChangeListener[] mJListeners;

  /**
   * our "Smalltalk style" listeners
   */
  private Object[] mSListeners;
  /**
   * our "Smalltalk style" listener methods
   */
  private Method[] mSMethods;

  // to avoid re-creation of (immutable) objects, each AbstractValueModel
  // uses only one ChangeEvent and one args array, they are created when
  // they are the required for the first time
  /**
   * null or the change event
   */
  private ChangeEvent mChangeEvent;
  /**
   * null or the change args
   */
  private Object[] mChangeArgs;

  /**
   * marker if we are in setValue()
   */
  private boolean mInSetValue;
  /**
   * marker if we are in a forced setValue()
   */
  private boolean mIsSetForced;
  /**
   * marker if we are disposed (= no longer valid)
   */
  private boolean mIsDisposed;
  /**
   * our name (never null)
   */
  private String mName;

  /**
   * shared empty change listener array
   */
  private static final ChangeListener[] EMPTY_CL_LIST = new ChangeListener[0];
  /**
   * shared empty object array
   */
  private static final Object[] EMPTY_OBJECT_LIST = new Object[0];
  /**
   * shared empty method array
   */
  private static final Method[] EMPTY_METHOD_LIST = new Method[0];
  /**
   * shared empty class array
   */
  private static final Class<?>[] PARAMS = {ChangeEvent.class};

  /**
   * Creates internal listener lists.
   */
  protected AbstractValueModel() {
    mJListeners = EMPTY_CL_LIST;
    mSListeners = EMPTY_OBJECT_LIST;
    mSMethods = EMPTY_METHOD_LIST;
    mInSetValue = false;
    mIsSetForced = false;
    mIsDisposed = false;
    mName = "";
  }

  /**
   * Add an dependent listener to a ValueModel, "Smalltalk style".
   *
   * @param pDependent the object to call
   * @param pMethodName the method name to call back
   * @throws IllegalArgumentException if pDependent or pMethod is null, or no method with the signature "public void
   * pMethodName(ChangeEvent e)" is found for pDependent.
   */
  public void onChangeSend(final Object pDependent, final String pMethodName) {
    checkDisposed();
    if (pDependent == null) {
      throw new IllegalArgumentException("dependent object must not be null");
    }
    if (pMethodName == null) {
      throw new IllegalArgumentException("method name must not be null");
    }
    if (!Modifier.isPublic(pDependent.getClass().getModifiers())) {
      throw new IllegalArgumentException("The class " + pDependent.getClass().getName()
              + " of the dependent object is not public");
    }

    /*
         * create Method Object, the signature is always
         * void <pMethodName>(ChangeEvent e)
     */
    Method method;
    try {
      method = pDependent.getClass().getMethod(pMethodName, PARAMS);
    } catch (NoSuchMethodException e) {
      IllegalArgumentException iae = new IllegalArgumentException(
              "could not extract method " + pMethodName + " of class "
              + pDependent.getClass().getName() + ": " + e.getMessage());
      iae.initCause(e);
      throw iae;
    }

    // check if we know pDependent, we only change the method in that case
    int oldLength = mSListeners.length;
    for (int i = 0; i < oldLength; i++) {
      //noinspection ObjectEquality
      if (mSListeners[i] == pDependent) {
        mSMethods[i] = method;
        return;
      }
    }

    // add entry at the end of each array
    Object[] sListeners = new Object[oldLength + 1];
    Method[] sMethods = new Method[oldLength + 1];
    System.arraycopy(mSListeners, 0, sListeners, 0, oldLength);
    System.arraycopy(mSMethods, 0, sMethods, 0, oldLength);
    sListeners[oldLength] = pDependent;
    sMethods[oldLength] = method;
    mSListeners = sListeners;
    mSMethods = sMethods;
  }

  /**
   * Add an dependent listener to a ValueModel, "Java style".
   *
   * @param pDependent the object to call
   * @throws IllegalArgumentException if pDependent is null
   */
  public void addChangeListener(final ChangeListener pDependent) {
    checkDisposed();
    if (pDependent == null) {
      throw new IllegalArgumentException("dependent must not be null");
    }

    // check if we know pDependent
    int oldLength = mJListeners.length;
    for (int i = 0; i < oldLength; i++) {
      //noinspection ObjectEquality
      if (mJListeners[i] == pDependent) {
        return;
      }
    }

    // add entry at the end of the array
    ChangeListener[] jListeners = new ChangeListener[oldLength + 1];
    System.arraycopy(mJListeners, 0, jListeners, 0, oldLength);
    jListeners[oldLength] = pDependent;
    mJListeners = jListeners;
  }

  /**
   * Remove a dependent listener, "Smalltalk style". If pDependent is null or not known, nothing happens.
   *
   * @param pDependent object to de-register
   */
  public void retractInterestsFor(final Object pDependent) {
    checkDisposed();
    // check if we know pDependent
    int i;
    int oldLength = mSListeners.length;
    for (i = 0; i < oldLength; i++) {
      Object sListener = mSListeners[i];
      //noinspection ObjectEquality
      if (sListener == pDependent) {
        break;
      }
    }
    if (i == oldLength) {
      return;
    }

    // check if we get empty
    int newLength = oldLength - 1;
    if (newLength < 1) {
      mSListeners = EMPTY_OBJECT_LIST;
      mSMethods = EMPTY_METHOD_LIST;
    } else {
      Object[] sListeners = new Object[newLength];
      Method[] sMethods = new Method[newLength];
      System.arraycopy(mSListeners, 0, sListeners, 0, i);
      System.arraycopy(mSListeners, i + 1, sListeners, i, newLength - i);
      System.arraycopy(mSMethods, 0, sMethods, 0, i);
      System.arraycopy(mSMethods, i + 1, sMethods, i, newLength - i);
      mSListeners = sListeners;
      mSMethods = sMethods;
    }
  }

  /**
   * Remove a dependent listener, "Java style". If pDependent is null or not known, nothing happens.
   *
   * @param pDependent object to de-register
   */
  public void removeChangeListener(final ChangeListener pDependent) {
    checkDisposed();
    // check if we know pDependent
    int i;
    int oldLength = mJListeners.length;
    for (i = 0; i < oldLength; i++) {
      Object jListener = mJListeners[i];
      //noinspection ObjectEquality
      if (jListener == pDependent) {
        break;
      }
    }
    if (i == oldLength) {
      return;
    }

    // check if we get empty
    int newLength = oldLength - 1;
    if (newLength < 1) {
      mJListeners = EMPTY_CL_LIST;
    } else {
      ChangeListener[] jListeners = new ChangeListener[newLength];
      System.arraycopy(mJListeners, 0, jListeners, 0, i);
      System.arraycopy(mJListeners, i + 1, jListeners, i, newLength - i);
      mJListeners = jListeners;
    }
  }

  /**
   * Cleanup all resources: disconnect from any input sources (like other ValueModel's ...), and remove all listeners. If any method
   * is called after the ValueModel was exposed, a IllegalStateException may be thrown.
   */
  public void dispose() {
    mIsDisposed = true;
    mJListeners = EMPTY_CL_LIST;
    mSListeners = EMPTY_OBJECT_LIST;
    mSMethods = EMPTY_METHOD_LIST;
  }

  /**
   * Check if the value model is disposed.
   *
   * @return true if the value model is disposed (= no longer valid)
   */
  public boolean isDisposed() {
    return mIsDisposed;
  }

  /**
   * Helper method to throw a IllegalStateException if we are disposed.
   *
   * @throws IllegalStateException if the value model is disposed
   */
  protected void checkDisposed() throws IllegalStateException {
    if (mIsDisposed) {
      throw new IllegalStateException("value model " + mName + " was already disposed");
    }
  }

  /**
   * Return a immutable list of all direct dependents of the value model.
   *
   * @return an immutablelList of all direct dependents
   */
  public List<Object> getDependents() {
    List<Object> dependents = new ArrayList<Object>(mJListeners.length + mSListeners.length);
    dependents.addAll(Arrays.asList(mJListeners));
    dependents.addAll(Arrays.asList(mSListeners));
    return Collections.unmodifiableList(dependents);
  }

  /**
   * Return the name of the state.
   *
   * @return the name of the state (never null)
   */
  public String getName() {
    return mName;
  }

  /**
   * Set the name of the state.
   *
   * @param pName the name of the state (must not be null)
   * @throws IllegalArgumentException if pState is null
   */
  public void setName(final String pName) {
    checkDisposed();
    if (pName == null) {
      throw new IllegalArgumentException("the name of a value model must not be null");
    }
    mName = pName;
  }

  /**
   * Set a new value, this will call setValue(pValue, false) for the real work.
   *
   * @param pValue the new value (null is o.k.)
   * @throws UnsupportedOperationException if this ValueModel is not mutable
   */
  public void setValue(final T pValue) {
    setValue(pValue, false);
  }

  /**
   * Needed since we generified everything, if we have no type given.
   *
   * @param pValue the value to be casted and set as new value
   */
  public void setObjectValue(final Object pValue) {
    setValue((T) pValue);
  }

  /**
   * Set a new value, this will call setValue(pValue, true) for the real work.
   *
   * @param pValue the new value (null is o.k.)
   * @throws UnsupportedOperationException if this ValueModel is not mutable
   */
  public void setValueForced(final T pValue) {
    setValue(pValue, true);
  }

  /**
   * Signal this object that portions of its data changed. If the ValueModel is currently firing a ChangeEvent to its listeners, the
   * call is ignored.
   */
  public void signalExternalUpdate() {
    checkDisposed();
    if (isInSetValue()) {
      return;
    }
    setInSetValue(false, true);
    try {
      fireStateChanged();
    } finally {
      setInSetValue(false, false);
    }
  }

  /**
   * Helper method for our subclasses to fire ChangeEvents.
   */
  protected void fireStateChanged() {
    // copy the listener array references to local variables, so when a listener
    // array changes during callback, we still iterate over the old array
    ChangeListener[] jListeners = mJListeners;
    Object[] sListeners = mSListeners;
    Method[] sMethods = mSMethods;

    // notify Java style listeners last to first
    for (int i = jListeners.length - 1; i >= 0; i--) {
      // lazy initialisation
      if (mChangeEvent == null) {
        mChangeEvent = new ChangeEvent(this);
      }
      ChangeListener jListener = jListeners[i];
      jListener.stateChanged(mChangeEvent);
    }

    // notify Smalltalk style listeners last to first
    for (int i = sListeners.length - 1; i >= 0; i--) {
      // lazy initialisation
      if (mChangeEvent == null) {
        mChangeEvent = new ChangeEvent(this);
      }
      if (mChangeArgs == null) {
        mChangeArgs = new Object[]{mChangeEvent};
      }
      Object sListener = sListeners[i];
      Method sMethod = sMethods[i];

      try {
        // doit: call the method with property change event
        sMethod.invoke(sListener, mChangeArgs);
      } catch (IllegalAccessException iae) {
        // map iae to a IllegalArgumentException
        throw new IllegalArgumentException(iae.getMessage());
      } catch (InvocationTargetException ite) {
        // map the cause of e to a IllegalArgumentException, if it is
        // not a RuntimeException or an Error
        Throwable cause = ite.getTargetException();

        if (cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        }
        if (cause instanceof Error) {
          throw (Error) cause;
        }

        throw new IllegalArgumentException(cause != null ? cause.getMessage() : ite.getMessage());
      }
    }
  }

  /**
   * Marker method that indicates if the object is just firing a state change due to a call of setValue(). This is useful for
   * objects that are listeners to the ValueModel and are not interested in state changes triggered by themselfs.
   *
   * @return true if we during a state change due to setValue(), false otherwise.
   */
  protected boolean isInSetValue() {
    return mInSetValue;
  }

  /**
   * Pendant to isInSetValue() including the "forced" flag.
   *
   * @param pInSetValue true or false
   * @param pIsSetForced true or false
   */
  protected void setInSetValue(final boolean pInSetValue, final boolean pIsSetForced) {
    mInSetValue = pInSetValue;
    mIsSetForced = pIsSetForced;
  }

  /**
   * Returns true if setValueForced() was called, only useful during a callback.
   *
   * @return false if we are not inside a callback or a "normal" setValue() was issued
   */
  public boolean isSetForced() {
    checkDisposed();
    return mIsSetForced;
  }


  /*
     * type conversion methods
   */
  /**
   * {@inheritDoc}
   */
  public boolean booleanValue() {
    boolean back;
    Object value = getValue();
    if (value == null) {
      back = false;
    } else if (value instanceof Boolean) {
      back = (Boolean) value;
    } else {
      back = Boolean.valueOf(value.toString());
    }
    return back;
  }

  /**
   * {@inheritDoc}
   */
  public void setValue(final boolean pValue) {
    // needed to be backwards compatible
    T value = (T) Boolean.valueOf(pValue);
    setValue(value);
  }

  /**
   * {@inheritDoc}
   */
  public int intValue() {
    int back;
    Object value = getValue();
    if (value == null) {
      back = 0;
    } else if (value instanceof Integer) {
      back = (Integer) value;
    } else {
      back = Integer.parseInt(value.toString());
    }
    return back;
  }

  /**
   * {@inheritDoc}
   */
  public void setValue(final int pValue) {
    // needed to be backwards compatible
    T value = (T) Integer.valueOf(pValue);
    setValue(value);
  }

  /**
   * {@inheritDoc}
   */
  public long longValue() {
    long back;
    Object value = getValue();
    if (value == null) {
      back = 0;
    } else if (value instanceof Long) {
      back = (Long) value;
    } else {
      back = Long.parseLong(value.toString());
    }
    return back;
  }

  /**
   * {@inheritDoc}
   */
  public void setValue(final long pValue) {
    // needed to be backwards compatible
    T value = (T) Long.valueOf(pValue);
    setValue(value);
  }

  /**
   * {@inheritDoc}
   */
  public float floatValue() {
    float back;
    Object value = getValue();
    if (value == null) {
      back = 0;
    } else if (value instanceof Float) {
      back = (Float) value;
    } else {
      back = Float.valueOf(value.toString());
    }
    return back;
  }

  /**
   * {@inheritDoc}
   */
  public void setValue(final float pValue) {
    // needed to be backwards compatible
    T value = (T) Float.valueOf(pValue);
    setValue(value);
  }

  /**
   * {@inheritDoc}
   */
  public double doubleValue() {
    double back;
    Object value = getValue();
    if (value == null) {
      back = 0;
    } else if (value instanceof Double) {
      back = (Double) value;
    } else {
      back = Double.valueOf(value.toString());
    }
    return back;
  }

  /**
   * {@inheritDoc}
   */
  public void setValue(final double pValue) {
    // needed to be backwards compatible
    T value = (T) Double.valueOf(pValue);
    setValue(value);
  }

  /**
   * {@inheritDoc}
   */
  public String stringValue() {
    Object value = getValue();
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }
}
