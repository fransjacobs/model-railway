package jcs.util.model.cuf;

import jcs.ui.table.model.AbstractValueModel;

/**
 * A ValueHolder object holds a value (any Object). The value can be changed via setValue() and accessed via getValue(). Whenever
 * the value changes, all interested listeners are notified.
 *
 * @param <T> the type of the value we hold
 */
public class ValueHolder<T> extends AbstractValueModel<T> implements ValueModel<T>, DelegateAccess {

  /**
   * our value, may be null
   */
  private T mValue;

  /**
   * Create a new holder with NULL as its value.
   */
  public ValueHolder() {
    this(null);
  }

  /**
   * Create a new holder with the handed object as its value.
   *
   * @param pValue the value we hold, may be null
   */
  public ValueHolder(final T pValue) {
    super();
    mValue = pValue;
  }

  /**
   * Returns always true
   *
   * @return true
   */
  @Override
  public boolean isEditable() {
    return true;
  }

  /**
   * Set a new value, this will fire a ChangeEvent if the new value is different from the old value.
   *
   * @param pValue the new value (null is o.k.)
   * @param pIsSetForced true if a forced setValue should be done
   */
  public void setValue(final T pValue, final boolean pIsSetForced) {
    checkDisposed();
    // optimize only if there is no forced update
    if (!pIsSetForced) {
      if ((mValue != null ? mValue.equals(pValue) : pValue == null || pValue.equals(mValue))) {
        // nothing changed
        return;
      }
    }

    mValue = pValue;
    setInSetValue(true, pIsSetForced);
    try {
      fireStateChanged();
    } finally {
      setInSetValue(false, pIsSetForced);
    }
  }

  /**
   * Get the current value, during a callback this is the new value.
   *
   * @return null or the value object
   */
  public T getValue() {
    checkDisposed();
    return mValue;
  }

  /**
   * Transform the handed value to a new value. This should not change the current value or trigger any updates.
   *
   * @param pValue the value we should assume as our value
   * @return pValue
   */
  public Object getValue(final Object pValue) {
    checkDisposed();
    return pValue;
  }
}
