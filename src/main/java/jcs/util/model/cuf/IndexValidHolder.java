package jcs.util.model.cuf;

/**
 * Helper class to monitor if the index points inside a the list or not. This class is used by IndexedAdapter and SelectionInList.
 */
class IndexValidHolder extends ValueHolder<Boolean> {

  /**
   * Creates a new IndexValidHolder with the given index.
   *
   * @param pIndex the initial index.
   */
  IndexValidHolder(final int pIndex) {
    setIndex(pIndex);
  }

  /**
   * Set the index.
   *
   * @param pIndex index, we assume that the boundary condition (the index is inside the list) is checked by the users of this
   * class.
   */
  void setIndex(final int pIndex) {
    checkDisposed();
    if (pIndex == IndexProvider.NO_SELECTION) {
      super.setValue(Boolean.FALSE);
    } else {
      super.setValue(Boolean.TRUE);
    }
  }

  /**
   * This value model can't be changed by setValue().
   *
   * @param pValue not used
   * @throws UnsupportedOperationException always thrown
   */
  public void setValue(final Boolean pValue) {
    throw new UnsupportedOperationException("a IndexValidHolder is not mutable"
            + " from outside, can't set value " + pValue);
  }
}
