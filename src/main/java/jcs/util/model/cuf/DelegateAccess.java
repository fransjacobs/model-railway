package jcs.util.model.cuf;

/**
 * This interface should be implemented by ValueModels that map or transform their value.
 * This is mainly usefull for users of AspectAdapter's and TypeConverter's that monitor a value inside a List to transform the
 * values of the same list, but do not to want to change the selection of the list. The major use case is the mapping of a List to a
 * Java Swing TableModel, see com.sdm.util.model.ui.ListTableMapper for details.
 */
public interface DelegateAccess {

  /**
   * Transform the handed value to a new value according to the current transformation rules. This should not change the current
   * value or trigger any updates if the object is implements also the ValueModel interface.
   *
   * @param pValue the value we should assume as our value
   * @return null or the transformed/mapped value object
   */
  public Object getValue(Object pValue);
}
