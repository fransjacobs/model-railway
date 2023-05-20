package jcs.ui.table.cuf;

import javax.swing.table.TableModel;

/**
 * Base interface of all the TableModels we use in our application. It adds the filtering and first-row-is-header capabilities to
 * the basic table model.
 */
public interface FilteredTableModel extends TableModel {

  /**
   * Filter the data with the handed regular expression.
   *
   * @param pFilterExpression the regular expression we should filter our rows with
   * @return true if the filter expression was valid, false if not
   */
  boolean filter(String pFilterExpression);

  /**
   * Reset any filtering. Can be called also when no filter was set before.
   */
  void filterReset();

  /**
   * Return the number of all rows, including those that are currently filtered.
   *
   * @return the number of total (not filtered) rows
   */
  int getAllRowsCount();

  /**
   * Sets if the first data row is treated as header information.
   *
   * @param pFirstRowIsHeader true if we should handle the first data row as table header
   */
  void setFirstRowIsHeader(boolean pFirstRowIsHeader);
}
