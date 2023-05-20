package jcs.util.model.cuf;

import jcs.ui.table.model.AbstractValueModel;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A SelectionInList holds both the data as well as the selection as a ValueModel. A SelectionInList differs from an IndexedAdapter
 * in the following points:<ul>
 * <li> the users of an IndexedAdapter doesn't know that a list is used at all, they are usally interrested in the aspect of one
 * entry in the list
 * <li> the SelectionInList holds both the data as well as the selection
 * </ul>
 * Typical users of a SelectionInList are adapters for comboboxes, radiobuttons, ... . This users are interested in the list as a
 * whole, as well as in the selection index.
 *
 * @param <T> the Type inside the list we monitor the selection
 */
public class SelectionInList<T> extends AbstractValueModel<List<T>>
        implements ValueModel<List<T>>, ChangeListener, ExternalUpdate, IndexProvider {

  /*
     * TODO changes that should be made here corresponding to the new MultiSelectionInList:
     * - Introduce a value model that holds the selected object of the list
     *   and that is modifiable within the values of the base list and null.
     * - Change the IndexedAdapter to reflect that change.
     * - If the List is given explicitly in a constructor instead of a ValueModel,
     *   internally create a value model and put the value in it (simplifies the code).
   */

  /**
   * null or value model holding our our list
   */
  private ValueModel<List<T>> mListHolder;
  /**
   * null or mListHolder if the value model provides an initial index
   */
  private IndexProvider mIndexProvider;
  /**
   * null or mListHolder if the value model supports and external update
   */
  private ExternalUpdate mExternalUpdate;
  /**
   * our list, may be null
   */
  private List<T> mList;
  /**
   * value model holding either NO_SELECTION or the selection in our list as an Integer, never null, value never null
   */
  private ValueModel<Integer> mSelectionIndexHolder;
  /**
   * checks if the index is not NO_SELECTION, that means that the index is inside our list, never null, value either Boolean.TRUE or
   * Boolean.FALSE
   */
  private IndexValidHolder mIndexInList;
  /**
   * If true, we try to keep the selected object stable when the list changes, if false, a change in the list will always result in
   * clearing the selection. Default value is false.
   * <P>
   * A value of true means that we search in the new list for an object that is equal to the previously selected object using the
   * {@link #mSelectionComparator}. If we find one, we adjust the selection index to match the new position. If we don't find one,
   * we set the selection index to {@link #NO_SELECTION}.
   * <P>
   * Note that in case of value true and unless there is no previous selection, a change in the list will result in a notification
   * on the {@link #mSelectionIndexHolder}, even if the actual index doesn't change. Furthermore, if the object is found, a
   * notification will be fired with the new index which causes in an {@link IndexedAdapter} to fire a notification event even
   * though its object may have stayed the same.
   */
  private boolean mKeepSelection;

  /**
   * the comparator used to find the previously selected object in a new list - see {@link #mKeepSelection}. This value may be null,
   * which means that {@link Object#equals(Object)} should be used.
   */
  private Comparator<T> mSelectionComparator;

  /**
   * no selection as an Integer object
   */
  public static final Integer NO_SELECTION = IndexProvider.NO_SELECTION;

  /**
   * Creates a new SelectionInList that does not select an entry in the list.
   *
   * @param pListHolder the list we monitor the selection is the value of pListHolder
   * @throws IllegalArgumentException if pListHolder is null or pListHolder.getValue() is not null and not a List
   */
  public SelectionInList(final ValueModel<List<T>> pListHolder) {
    this(pListHolder, IndexProvider.NO_SELECTION);
  }

  /**
   * Creates a new SelectionInList that does not select an entry in the list.
   *
   * @param pListHolder the list we monitor the selection is the value of pListHolder
   * @param pIndex NO_SELECTION or the initial index in the list
   * @throws IllegalArgumentException if pListHolder is null or the value if pListHolder is neither null nor a List or pIndex is out
   * of range
   */
  public SelectionInList(final ValueModel<List<T>> pListHolder, final int pIndex) {
    super();
    if (pListHolder == null) {
      throw new IllegalArgumentException("list holder must not be null");
    }
    init(pListHolder.getValue(), pListHolder, pIndex);
  }

  /**
   * Creates a new SelectionInList that does not select an entry in the list.
   *
   * @param pList the list we are indexing into, may be null
   */
  public SelectionInList(final List<T> pList) {
    this(pList, IndexProvider.NO_SELECTION);
  }

  /**
   * Creates a new SelectionInList.
   *
   * @param pList the list we are indexing into, may be null
   * @param pIndex NO_SELECTION or the initial index in the list
   * @throws IllegalArgumentException if pIndex is out of range
   */
  public SelectionInList(final List<T> pList, final int pIndex) {
    super();
    init(pList, null, pIndex);
  }

  /**
   * Handle common constructor stuff.
   *
   * @param pList list we are indexing into, may be null
   * @param pListHolder ValueModel of pList, may be null
   * @param pIndex NO_SELECTION or the initial index in pList
   * @throws IllegalArgumentException if pIndex is out of range
   */
  private void init(final List<T> pList, final ValueModel<List<T>> pListHolder, int pIndex) {
    mList = pList;
    mListHolder = pListHolder;
    setInSetValue(false, false);
    if (mListHolder instanceof IndexProvider) {
      mIndexProvider = (IndexProvider) mListHolder;
      pIndex = mIndexProvider.getIndex();
    } else {
      mIndexProvider = null;
    }
    if (mListHolder instanceof ExternalUpdate) {
      mExternalUpdate = (ExternalUpdate) mListHolder;
    }

    mIndexInList = new IndexValidHolder(pIndex);
    mSelectionIndexHolder = new SelectionIndexHolder(pIndex);

    if (mListHolder != null) {
      mListHolder.addChangeListener(this);
    }
  }

  /**
   * Returns always true
   *
   * @return true
   */
  public boolean isEditable() {
    // TODO: this is a bug. 
    // When a list holder is used, the value should
    // depend on the mListHolder.isEditable()
    return true;
  }

  /**
   * Cleanup all resources: disconnect from any input sources (like other ValueModel's ...), and remove all listeners.
   */
  public void dispose() {
    super.dispose();
    if (mListHolder != null && !mListHolder.isDisposed()) {
      mListHolder.removeChangeListener(this);
    }
    mSelectionIndexHolder.dispose();
    mIndexInList.dispose();
  }

  /**
   * Small helper to check the index.
   *
   * @param pIndex index to check
   * @param pList List for the index, may be null
   */
  private static void checkIndex(final int pIndex, final List<?> pList) {
    int maxSize = IndexProvider.NO_SELECTION;

    if (pList != null) {
      maxSize = pList.size() - 1;
    }
    if ((pIndex < IndexProvider.NO_SELECTION) || (pIndex > maxSize)) {
      throw new IllegalArgumentException("index out of range, got "
              + pIndex + ", but list size is "
              + (maxSize + 1));
    }
  }

  /**
   * defines the behavior of the selection when the list changes.
   *
   * @param pKeepSelection if true, we try to keep the selected object, if false, the selection is cleared.
   * @see #mKeepSelection
   */
  public void setKeepSelection(final boolean pKeepSelection) {
    mKeepSelection = pKeepSelection;
  }

  /**
   * @return true if we try to keep the selected object when the list changes, false if the selection will be cleared
   * @see #mKeepSelection
   */
  public boolean isKeepSelection() {
    return mKeepSelection;
  }

  /**
   * @return the comparator used to find the object if {@link #mKeepSelection} is true
   * @see #mKeepSelection
   */
  public Comparator<?> getSelectionComparator() {
    return mSelectionComparator;
  }

  /**
   * sets the comparator to use to find the object if {@link #mKeepSelection} is true
   *
   * @param pSelectionComparator the new comparator, may be null. If null, {@link Object#equals(Object)} will be used.
   * @see #mKeepSelection
   */
  public void setSelectionComparator(final Comparator<T> pSelectionComparator) {
    mSelectionComparator = pSelectionComparator;
  }

  /**
   * Set a new value, this will fire a ChangeEvent if the new value is different from the old value. The new value must be a List or
   * null, if we have a list holder we set the list to the value holder.
   *
   * @param pValue the new list (null is o.k.)
   * @param pIsSetForced true if a forced setValue should be done
   * @throws IllegalArgumentException if pValue is neither a List nor null
   */
  public void setValue(final List<T> pValue, final boolean pIsSetForced) {
    checkDisposed();
    if (isInSetValue()) {
      return;
    }

    if ((pValue != null) && !(pValue instanceof List)) {
      throw new IllegalArgumentException("value must be a List or null, not "
              + "a " + pValue.getClass().getName());
    }

    int oldSelectionIndex = mSelectionIndexHolder.intValue();
    T oldSelectedObject = null;
    if (oldSelectionIndex >= 0 && mList != null && oldSelectionIndex < mList.size()) {
      oldSelectedObject = mList.get(oldSelectionIndex);
    }

    // optimize only if there is no forced update
    if (mListHolder == null && !pIsSetForced) {
      if (mList == null ? pValue == null : mList.equals(pValue)) {
        return;
      }
    }

    setInSetValue(true, pIsSetForced);
    try {
      Integer newIndex = NO_SELECTION;
      boolean forced = false;
      if (mListHolder != null) {
        mListHolder.setValue(pValue);
      }
      mList = pValue;
      if (mKeepSelection && oldSelectionIndex >= 0) {
        newIndex = findIndexInList(mList, oldSelectedObject);
        forced = true;
      }
      mSelectionIndexHolder.setValue(newIndex, forced);
      // this must happen after the index has been updated, otherwise
      // we have a index that might not be inside the table
      fireStateChanged();
    } finally {
      setInSetValue(false, false);
    }
  }

  /**
   * Finds the first index in the list that matches the given object. To compare the object, the {@link #mSelectionComparator} is
   * used if it is given with the object as the first parameter and the list entry as the second parameter, otherwise
   * {@link Object#equals(Object)} is used. <code>null</code> is only matched by <code>null</code>. If the object is not found in
   * the list, -1 will be returned.
   *
   * @param pList the list to search through, may be null
   * @param pObject the object to look for, may be null
   * @return the index of the first matching object in the list or -1 if none could not be found
   */
  private int findIndexInList(final List<T> pList, final T pObject) {
    if (pList == null) {
      return -1;
    } else if (mSelectionComparator == null) {
      // we can use default method
      return pList.indexOf(pObject);
    } else {
      for (int i = 0; i < pList.size(); i++) {
        T objInList = pList.get(i);
        if (pObject == null) {
          if (objInList == null) {
            return i;
          }
        } else if (objInList != null) {
          if (mSelectionComparator.compare(pObject, objInList) == 0) {
            return i;
          }
        }
      }
      return -1;
    }
  }

  /**
   * Get the current list.
   *
   * @return null or our list
   */
  public List<T> getValue() {
    try {
      checkDisposed();
    } catch (IllegalStateException e) {
      // we consider this as an valid action, otherwise we would force
      // to disconnect all cascading value models, returning null in a
      // "shutdown" scenaria is slightly more gracefull than throwing an
      // exception
      return null;
    }
    return mList;
  }

  /**
   * Return the index of the currently selected item in our list.
   *
   * @return NO_SELECTION or our index
   */
  public int getIndex() {
    checkDisposed();
    return mSelectionIndexHolder.intValue();
  }

  /**
   * Add an item to the underlying list at the current position or at the end of the list if there is currently no selection.
   *
   * @param pItem the item to add
   */
  public void addItem(final T pItem) {
    checkDisposed();
    int index = mSelectionIndexHolder.intValue();
    if (index == NO_SELECTION) {
      addItem(mList.size(), pItem);
    } else {
      addItem(index, pItem);
    }
  }

  /**
   * Add an item to the underlying list at the given position. If the List is inside a ValueModel, re-set the list to that value
   * model. The current index is updated to match pIndex before firing a state-change.
   *
   * @param pIndex the index to add the item
   * @param pItem the item to add
   */
  public void addItem(final int pIndex, final T pItem) {
    checkDisposed();
    if (isInSetValue()) {
      throw new IllegalStateException("addItem called during setValue, index= "
              + pIndex + ", new item= " + pItem);
    }

    setInSetValue(true, false);
    try {
      mList.add(pIndex, pItem);
      if (mExternalUpdate != null) {
        mExternalUpdate.signalExternalUpdate();
      }
      // do not change the order of the following lines, otherwise
      // we have a index that might not be inside the table
      mSelectionIndexHolder.setValueForced(pIndex);
      fireStateChanged();
    } finally {
      setInSetValue(false, false);
    }
  }

  /**
   * Remove the item at the handed position. If the current index would no longer be valid (when removing the last item or a list),
   * the current index is set first to the list size - 1 or to NO_SELECTION before firing a state-change.
   *
   * @param pIndex NO_SELECTION or 0..list size-1
   * @return null or the object at that list position
   */
  public Object removeItem(final int pIndex) {
    checkDisposed();
    if (isInSetValue()) {
      throw new IllegalStateException("removeItem called during setValue, index= " + pIndex);
    }
    if (pIndex == NO_SELECTION) {
      return null;
    }

    Object back = null;
    setInSetValue(true, false);
    try {
      back = mList.remove(pIndex);
      if (mExternalUpdate != null) {
        mExternalUpdate.signalExternalUpdate();
      }
      int index = pIndex;
      if (pIndex == mList.size()) {
        index = (mList.size() - 1);
      }
      // do not change the order of the following lines, otherwise
      // we have a index that might not be inside the table
      mSelectionIndexHolder.setValue(new Integer(index));
      fireStateChanged();
    } finally {
      setInSetValue(false, false);
    }
    return back;
  }

  /**
   * Removes the currently selecte item, or does nothing if no item is selected.
   *
   * @return null or the object at the removed position
   */
  public Object removeItem() {
    return removeItem(selectionHolder().intValue());
  }

  /**
   * Invoked when the value model holding our list changed its state.
   *
   * @param pEvent a ChangeEvent object, not used
   */
  public void stateChanged(final ChangeEvent pEvent) {
    checkDisposed();

    // we ignore state changes when we just changed the list
    if (isInSetValue()) {
      return;
    }

    // remember the current selection
    int oldSelectionIndex = mSelectionIndexHolder.intValue();
    T oldSelectedObject = null;
    if (oldSelectionIndex >= 0 && mList != null && oldSelectionIndex < mList.size()) {
      oldSelectedObject = mList.get(oldSelectionIndex);
    }

    // check the index
    List<T> value = mListHolder.getValue();
    int index = IndexProvider.NO_SELECTION;
    if (mIndexProvider != null) {
      index = mIndexProvider.getIndex();
    }
    checkIndex(index, value);

    // first remember the list (so that it is available during the callbacks),
    // then update the index, then notify our listeners
    mList = value;
    if (mKeepSelection && oldSelectionIndex >= 0) {
      mSelectionIndexHolder.setValueForced(findIndexInList(mList, oldSelectedObject));
    } else {
      mSelectionIndexHolder.setValue(new Integer(index));
    }
    fireStateChanged();
  }

  /**
   * The provided ValueModel can be used to monitor if this IndexedAdapter holds a "valid" value (index is not NO_SELECTION). The
   * ValueModel is read-only, and contains either Boolean.TRUE or Boolean.FALSE.
   *
   * @return a ValueModel with a Boolean value
   */
  public ValueModel<Boolean> isIndexInList() {
    return mIndexInList;
  }

  /**
   * The provided ValueModel can be used to monitor and change the selection in our list. The value of the ValueModel is and an
   * Integer (never null) holding the index or NO_SELECTION if there is no valid index.
   *
   * @return a ValueModel with a Integer value
   */
  public ValueModel<Integer> selectionHolder() {
    checkDisposed();
    return mSelectionIndexHolder;
  }

  /**
   * Provides the ValueModel of the ListHolder for our List.
   *
   * @return ValueModel holding our list, may be null
   */
  public ValueModel<List<T>> listHolder() {
    return mListHolder;
  }

  /**
   * helper class that holds the index of the selection and checks if the value in setValue() is valid.
   */
  private class SelectionIndexHolder extends ValueHolder<Integer> {

    /**
     * Creates a new SelectionIndexHolder with the given index.
     *
     * @param pIndex the initial index.
     */
    SelectionIndexHolder(final int pIndex) {
      setValue(new Integer(pIndex));
    }

    /**
     * Set a new value, this will fire a ChangeEvent if the new value is different from the old value.
     *
     * @param pValue the new Integer value (null is not o.k.)
     * @param pIsSetForced true if a forced setValue should be done
     * @throws IllegalArgumentException if pValue is not an Integer or the integer is not inside the list
     */
    public void setValue(final Integer pValue, final boolean pIsSetForced) {
      checkDisposed();
      if (pValue == null) {
        throw new IllegalArgumentException("selection must not be null");
      }

      int index = pValue;
      checkIndex(index, mList);

      super.setValue(pValue, pIsSetForced);
      mIndexInList.setIndex(index);
    }
  }
}
