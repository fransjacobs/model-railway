package jcs.ui.table.cuf;

import jcs.util.model.cuf.ValueModel;
import javax.swing.AbstractAction;
import javax.swing.JTable;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

/**
 * This small helper class is used to provide either a row or a cell copy to the clipboard of the selected row of a JTable.
 */
public class TableRowCopyAction extends AbstractAction {

  /**
   * holds a "true" or "false" marker
   */
  private ValueModel<?> mCopyRow;

  /**
   * Create a new table copy action.
   *
   * @param pCopyRow the value model containing "true" or "false"
   */
  public TableRowCopyAction(final ValueModel<?> pCopyRow) {
    if (pCopyRow == null) {
      throw new IllegalArgumentException("copy row model must not be null");
    }
    mCopyRow = pCopyRow;
  }

  /**
   * Invoked when an action occurs.
   *
   * @param pEvent the event describing the action
   */
  @Override
  public void actionPerformed(final ActionEvent pEvent) {
    if (!(pEvent.getSource() instanceof JTable)) {
      return;
    }

    JTable table = (JTable) pEvent.getSource();
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      return;
    }

    boolean copyRow = mCopyRow.booleanValue();
    StringBuilder sb = new StringBuilder();
    if (copyRow) {
      // copy whole row(s)
      int[] selectedRows = table.getSelectedRows();
      int columns = table.getColumnCount();
      for (final int selectedRow1 : selectedRows) {
        selectedRow = selectedRow1;
        for (int column = 0; column < columns; column++) {
          sb.append(table.getModel().getValueAt(selectedRow, column));
          if (column < columns - 1) {
            sb.append('\t');
          }
        }
        sb.append('\n');
      }
    } else {
      // copy cell only
      int selectedColumn = table.getSelectedColumn();
      if (selectedColumn < 0) {
        return;
      }
      int realColumn = table.convertColumnIndexToModel(selectedColumn);
      sb.append(table.getModel().getValueAt(selectedRow, realColumn));
    }

    // export text to clipboard
    StringSelection selection = new StringSelection(sb.toString());
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(selection, selection);
  }
}
