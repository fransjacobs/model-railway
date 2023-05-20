package jcs.ui.table.cuf;

/**
 * IndexConverter for sorted tables (using NewTableSorter), to synchronize
 * the model index with the view index, see
 * {@link com.sdm.util.model.ui.ListTableMapperBase}.
 */
public interface IndexConverter 
{
    /**
     * This method converts the given view index of a table row to
     * the corresponding model index of this row.
     * @param pViewIndex the view index
     * @return the model index
     */
    public int convert2ModelIndex(int pViewIndex);

    /**
     * This method converts the given model index of a table row to
     * the corresponding view index of this row.
     * @param pModelIndex the model index
     * @return the view index
     */
    public int convert2ViewIndex(int pModelIndex);
}
