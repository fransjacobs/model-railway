package jcs.util.model.cuf;

/**
 * If the ValueModel holding a List implements that interface, special adapters/ValueModel's like IndexedAdapter or SelectionInList
 * use that interface, to get the selection in that List.
 */
public interface IndexProvider {

  /**
   * Index that marks that no index is choosen.
   */
  public static final int NO_SELECTION = -1;

  /**
   * Return the index for a List.
   *
   * @return NO_SELECTION or the index
   */
  public int getIndex();
}
