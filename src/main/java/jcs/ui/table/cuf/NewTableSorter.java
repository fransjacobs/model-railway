package jcs.ui.table.cuf;

import jcs.ui.table.cuf.IndexConverter;
import jcs.ui.table.cuf.ListTableMapper;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NewTableSorter is a decorator for TableModels; adding sorting functionality to a supplied TableModel. NewTableSorter does not
 * store or copy the data in its TableModel; instead it maintains a map from the row indexes of the view to the row indexes of the
 * model. As requests are made of the sorter (like getValueAt(row, col)) they are passed to the underlying model after the row
 * numbers have been translated via the internal mapping array. This way, the NewTableSorter appears to hold another copy of the
 * table with the rows in a different order.
 * 
 * NewTableSorter registers itself as a listener to the underlying model, just as the JTable itself would. Events recieved from the
 * model are examined, sometimes manipulated (typically widened), and then passed on to the TableSorter's listeners (typically the
 * JTable). If a change to the model has invalidated the order of NewTableSorter's rows, a note of this is made and the sorter will
 * resort the rows the next time a value is requested.
 * 
 * The NewTableSorter implements an IndexConverter {@link IndexConverter}, so the ListTableMapperBase {@link ListTableMapperBase}
 * has access to the modelIndex of the sorted table for using them in the ListSelectionModel callback methods.
 * 
 * When the tableHeader property is set, either by using the setTableHeader() method or the two argument constructor, the table
 * header may be used as a complete UI for NewTableSorter. The default renderer of the tableHeader is decorated with a renderer that
 * indicates the sorting status of a column. In addition, a mouse listener is installed with the following behavior:
 * <ul>
 * <li>
 * Mouse-click: Advances the sorting status of that column through three values: {NOT_SORTED, ASCENDING, DESCENDING} (then back to
 * NOT_SORTED again). The column last clicked has the highest sorting priority, but the previous sortings are stable.
 * <li>
 * SHIFT-mouse-click: Cycles the sorting status of the column through the same three values, in the opposite order: {NOT_SORTED,
 * DESCENDING, ASCENDING}.
 * <li>
 * CONTROL-mouse-click and CONTROL-SHIFT-mouse-click: as above except that the changes to the column do cancel the statuses of
 * columns that are already sorting, so the table is sorted by only one column.
 * </ul>
 * 
 * <P>
 * Any sorting will clear the selection in the table because the table reacts that way to the change event. If the sorting is
 * initiated by the user (see the private inner class MouseHandler), we attempt to restore the selection for the new order
 * afterwards. If that behaviour causes problem, it can be switched off by <CODE>setRestoreSelectionAfterSort(false)</CODE>.
 * </P>
 *
 * @author Philip Milne
 * @author Brendon McLean
 * @author Dan van Enckevort
 * @author Parwinder Sekhon
 * @version 2.0 02/27/04
 * 
 * Taken originally from "The Java Tutorial" by Sun Microsystems. Modified by Andreas Schober, 10.05.2004, sd&amp;m AG. Modified by
 * Michael Brade, 08.05.2009, capgemini sd&m. Modified by Marco Berroth, 22.11.2011, msg-systems.
 */
public class NewTableSorter extends AbstractTableModel implements IndexConverter {

  /**
   * original TableModel
   */
  protected TableModel mTableModel;

  /**
   * sorting-status of a column
   */
  public static final int DESCENDING = -1;
  public static final int NOT_SORTED = 0;
  public static final int ASCENDING = 1;

  private static final Directive EMPTY_DIRECTIVE = new Directive(-1, NOT_SORTED);

  /**
   * static Comparable-Comparator
   */
  public static final Comparator COMPARABLE_COMAPRATOR = new Comparator() {
    @Override
    public int compare(final Object pO1, final Object pO2) {
      return ((Comparable) pO1).compareTo(pO2);
    }
  };

  /**
   * Static Lexical-Comparator with a special handling of String[]'s.
   */
  public static final Comparator LEXICAL_COMPARATOR = new Comparator() {
    @Override
    public int compare(final Object pO1, final Object pO2) {
      // compare the first element of each array
      if (pO1 instanceof String[] && pO2 instanceof String[]) {
        String[] s1 = (String[]) pO1;
        String[] s2 = (String[]) pO2;
        if (s1 != null && s1.length > 0 && s2 != null && s2.length > 0) {
          return s1[0].compareTo(s2[0]);
        }
      }
      String s1 = pO1.toString();
      String s2 = pO2.toString();
      if (s1 == s2) {
        return 0;
      }
      if (s1 == null) {
        return -1;
      }
      if (s2 == null) {
        return 1;
      }
      return s1.compareTo(s2);
    }
  };

  private Row[] mViewToModel;
  private int[] mModelToView;

  private JTableHeader mTableHeader;
  private MouseListener mMouseListener;
  private TableModelListener mTableModelListener;
  private Map mColumnComparators = new HashMap();
  private List mSortingColumns = new ArrayList();
  private List mSortingColumnsReverse = new ArrayList();
  /**
   * if true we attempt to restore the selection after the user has initiated a sort. This won't work when the sorting is initiated
   * by the program.
   */
  private boolean mAttemptRestoreSelectionAfterSort;

  private NewTableSorter() {
    mMouseListener = new MouseHandler();
    mTableModelListener = new TableModelHandler();
    mColumnComparators = new HashMap();
    mSortingColumns = new ArrayList();
    mSortingColumnsReverse = new ArrayList();
    mAttemptRestoreSelectionAfterSort = true;
  }

  public NewTableSorter(final TableModel pTableModel) {
    this();
    setTableModel(pTableModel);
  }

  public NewTableSorter(final TableModel pTableModel, final JTableHeader pTableHeader) {
    this();
    setTableHeader(pTableHeader);
    setTableModel(pTableModel);
  }

  public boolean isAttemptRestoreSelectionAfterSort() {
    return mAttemptRestoreSelectionAfterSort;
  }

  public void setAttemptRestoreSelectionAfterSort(
          final boolean pAttemptRestoreSelectionAfterSort) {
    mAttemptRestoreSelectionAfterSort = pAttemptRestoreSelectionAfterSort;
  }

  private void clearSortingState() {
    mViewToModel = null;
    mModelToView = null;
  }

  public TableModel getTableModel() {
    return mTableModel;
  }

  public void setTableModel(final TableModel pTableModel) {
    if (mTableModel != null) {
      mTableModel.removeTableModelListener(mTableModelListener);
    }

    mTableModel = pTableModel;
    if (mTableModel != null) {
      mTableModel.addTableModelListener(mTableModelListener);
    }

    clearSortingState();
    fireTableStructureChanged();
  }

  public JTableHeader getTableHeader() {
    return mTableHeader;
  }

  public void setTableHeader(final JTableHeader pTableHeader) {
    if (mTableHeader != null) {
      mTableHeader.removeMouseListener(mMouseListener);
      TableCellRenderer defaultRenderer = mTableHeader.getDefaultRenderer();
      if (defaultRenderer instanceof SortableHeaderRenderer) {
        mTableHeader.setDefaultRenderer(((SortableHeaderRenderer) defaultRenderer).mTableCellRenderer);
      }
    }

    mTableHeader = pTableHeader;
    if (mTableHeader != null) {
      mTableHeader.addMouseListener(mMouseListener);
      mTableHeader.setDefaultRenderer(
              new SortableHeaderRenderer(mTableHeader.getDefaultRenderer()));
    }
  }

  public boolean isSorting() {
    return !mSortingColumns.isEmpty();
  }

  private Directive getDirective(final int pColumn) {
    for (final Object mSortingColumn : mSortingColumns) {
      Directive directive = (Directive) mSortingColumn;
      if (directive.mColumn == pColumn) {
        return directive;
      }
    }
    return EMPTY_DIRECTIVE;
  }

  public int getSortingStatus(final int pColumn) {
    return getDirective(pColumn).mDirection;
  }

  private void sortingStatusChanged() {
    clearSortingState();
    fireTableDataChanged(); // Note: this will clear the selection
    if (mTableHeader != null) {
      mTableHeader.repaint();
    }
  }

  public void setSortingStatus(final int pColumn, final int pStatus) {
    Directive directive = getDirective(pColumn);
    //noinspection ObjectEquality
    if (directive != EMPTY_DIRECTIVE) {
      mSortingColumns.remove(directive);
    }
    if (pStatus != NOT_SORTED) {
      mSortingColumns.add(new Directive(pColumn, pStatus));
    }

    // reverse the priority of the column sorting:
    // last clicked column -> highest sorting priority
    mSortingColumnsReverse.clear();
    mSortingColumnsReverse.addAll(mSortingColumns);
    Collections.reverse(mSortingColumnsReverse);

    sortingStatusChanged();
  }

  protected Icon getHeaderRendererIcon(final int pColumn, final int pSize) {
    Directive directive = getDirective(pColumn);
    int priority = mSortingColumnsReverse.indexOf(directive);

    //noinspection ObjectEquality
    if (directive == EMPTY_DIRECTIVE) {
      return null;
    }
    return new Arrow(directive.mDirection == DESCENDING, pSize, priority);
  }

  private void cancelSorting() {
    mSortingColumns.clear();
    sortingStatusChanged();
  }

  public void setColumnComparator(final Class pType, final Comparator pComparator) {
    if (pComparator == null) {
      mColumnComparators.remove(pType);
    } else {
      mColumnComparators.put(pType, pComparator);
    }
  }

  protected Comparator getComparator(final int pColumn) {
    Class columnType = mTableModel.getColumnClass(pColumn);
    Comparator comparator = (Comparator) mColumnComparators.get(columnType);
    if (comparator != null) {
      return comparator;
    }
    if (Comparable.class.isAssignableFrom(columnType)) {
      return COMPARABLE_COMAPRATOR;
    }
    return LEXICAL_COMPARATOR;
  }

  private Row[] getViewToModel() {
    if (mViewToModel == null) {
      int tableModelRowCount = mTableModel.getRowCount();
      mViewToModel = new Row[tableModelRowCount];
      for (int row = 0; row < tableModelRowCount; row++) {
        mViewToModel[row] = new Row(row);
      }

      if (isSorting()) {
        Arrays.sort(mViewToModel);
      }
    }
    return mViewToModel;
  }

  public int modelIndex(final int pViewIndex) {
    if (pViewIndex == -1) {
      return -1;
    } else if (pViewIndex >= getViewToModel().length) {
      return -1; // map bad indices to -1
    } else {
      return getViewToModel()[pViewIndex].mModelIndex;
    }
  }

  private int[] getModelToView() {
    if (mModelToView == null) {
      int n = getViewToModel().length;
      mModelToView = new int[n];
      for (int i = 0; i < n; i++) {
        mModelToView[modelIndex(i)] = i;
      }
    }
    return mModelToView;
  }

  public int viewIndex(final int pModelIndex) {
    if (pModelIndex == -1) {
      return -1;
    } else if (pModelIndex >= getModelToView().length) {
      return -1; // map bad indices to -1
    } else {
      return getModelToView()[pModelIndex];
    }
  }

  // Helper classes
  @SuppressWarnings({"ClassNamingConvention"})
  private class Row implements Comparable {

    private int mModelIndex;
    private Object[] mValueCache;

    public Row(final int pIndex) {
      mModelIndex = pIndex;
      mValueCache = new Object[mTableModel.getColumnCount()];
    }

    @Override
    public int compareTo(final Object pOther) {
      Row rowOther = (Row) pOther;

      int row1 = mModelIndex;
      int row2 = rowOther.mModelIndex;

      for (final Object aMSortingColumnsReverse : mSortingColumnsReverse) {
        Directive directive = (Directive) aMSortingColumnsReverse;
        int column = directive.mColumn;

        Object o1;
        Object o2;

        // MiB: use caching, getValueAt is *very* slow!!
        if (mValueCache[column] != null) {
          o1 = mValueCache[column];
        } else {
          if ((mTableModel instanceof ListTableMapper)
                  && ((ListTableMapper) mTableModel).isColumnSortable(column)) {
            o1 = ((ListTableMapper) mTableModel).getValueForSortingAt(row1, column);
          } else {
            o1 = mTableModel.getValueAt(row1, column);
          }

          mValueCache[column] = o1;
        }

        if (rowOther.mValueCache[column] != null) {
          o2 = rowOther.mValueCache[column];
        } else {
          if ((mTableModel instanceof ListTableMapper)
                  && ((ListTableMapper) mTableModel).isColumnSortable(column)) {
            o2 = ((ListTableMapper) mTableModel).getValueForSortingAt(row2, column);
          } else {
            o2 = mTableModel.getValueAt(row2, column);
          }

          rowOther.mValueCache[column] = o2;
        }

        int comparison;
        // Define null less than everything, except null.
        if (o1 == null && o2 == null) {
          comparison = 0;
        } else if (o1 == null) {
          comparison = -1;
        } else if (o2 == null) {
          comparison = 1;
        } else {
          if (Comparable.class.isAssignableFrom(o1.getClass())
                  && Comparable.class.isAssignableFrom(o2.getClass())) {
            comparison = ((Comparable) o1).compareTo(o2);
          } else {
            comparison = getComparator(column).compare(o1, o2);
          }
        }
        if (comparison != 0) {
          return directive.mDirection == DESCENDING ? -comparison : comparison;
        }
      }
      return 0;
    }
  }

  private class TableModelHandler implements TableModelListener {

    @Override
    public void tableChanged(final TableModelEvent pEvent) {
      // If we're not sorting by anything, just pass the event along.             
      if (!isSorting()) {
        clearSortingState();
        fireTableChanged(pEvent);
        return;
      }

      // If the table structure has changed, cancel the sorting; the             
      // sorting columns may have been either moved or deleted from             
      // the model. 
      if (pEvent.getFirstRow() == TableModelEvent.HEADER_ROW) {
        cancelSorting();
        fireTableChanged(pEvent);
        return;
      }

      // We can map a cell event through to the view without widening             
      // when the following conditions apply: 
      // 
      // a) all the changes are on one row (pEvent.getFirstRow() == pEvent.getLastRow()) and,
      // b) all the changes are in one column (column != TableModelEvent.ALL_COLUMNS) and,
      // c) we are not sorting on that column (getSortingStatus(column) == NOT_SORTED) and, 
      // d) a reverse lookup will not trigger a sort (mModelToView != null)
      //
      // Note: INSERT and DELETE events fail this test as they have column == ALL_COLUMNS.
      // 
      // The last check, for (mModelToView != null) is to see if mModelToView
      // is already allocated. If we don't do this check; sorting can become 
      // a performance bottleneck for applications where cells  
      // change rapidly in different parts of the table. If cells 
      // change alternately in the sorting column and then outside of             
      // it this class can end up re-sorting on alternate cell updates - 
      // which can be a performance problem for large tables. The last 
      // clause avoids this problem. 
      int column = pEvent.getColumn();
      if (pEvent.getFirstRow() == pEvent.getLastRow()
              && column != TableModelEvent.ALL_COLUMNS
              && getSortingStatus(column) == NOT_SORTED
              && mModelToView != null) {
        int viewIndex = getModelToView()[pEvent.getFirstRow()];
        fireTableChanged(new TableModelEvent(NewTableSorter.this,
                viewIndex, viewIndex,
                column, pEvent.getType()));
        return;
      }

      // Something has happened to the data that may have invalidated the row order. 
      clearSortingState();
      fireTableDataChanged();
    }
  }

  private class MouseHandler extends MouseAdapter {

    @Override
    public void mouseClicked(final MouseEvent pEvent) {
      // ignore mouse clicks when in column resizing mode
      if (getTableHeader().getCursor().getType() != Cursor.DEFAULT_CURSOR) {
        return;
      }

      JTableHeader h = (JTableHeader) pEvent.getSource();
      TableColumnModel columnModel = h.getColumnModel();
      int viewColumn = columnModel.getColumnIndexAtX(pEvent.getX());
      if (viewColumn < 0) {
        // the mouse position was inside the table at an area with no column
        return;
      }
      int column = columnModel.getColumn(viewColumn).getModelIndex();

      // Note: the sorting will clear the selection
      int[] selectedRows = null;
      if (mAttemptRestoreSelectionAfterSort) {
        selectedRows = h.getTable().getSelectedRows();
        for (int i = 0; i < selectedRows.length; i++) {
          selectedRows[i] = modelIndex(selectedRows[i]);
        }
      }
      triggerSorting(column, pEvent.isControlDown(), pEvent.isShiftDown());
      if (mAttemptRestoreSelectionAfterSort) {
        //noinspection ConstantConditions
        for (final int selectedRow : selectedRows) {
          int viewIndex = viewIndex(selectedRow);
          h.getTable().getSelectionModel().addSelectionInterval(viewIndex, viewIndex);
        }
      }
    }
  }

  private static class Arrow implements Icon {

    private boolean mDescending;
    private int mSize;
    private int mPriority;

    public Arrow(final boolean pDescending, final int pSize, final int pPriority) {
      mDescending = pDescending;
      mSize = pSize;
      mPriority = pPriority;
    }

    @Override
    public void paintIcon(final Component pComponent, final Graphics pGraphics, final int pX, int pY) {
      Color color = pComponent == null ? Color.GRAY : pComponent.getBackground();
      // In a compound sort, make each succesive triangle 20% 
      // smaller than the previous one. 
      int dx = (int) (mSize / 2 * Math.pow(0.8, mPriority));
      int dy = mDescending ? dx : -dx;
      // Align icon (roughly) with font baseline. 
      pY = pY + 5 * mSize / 6 + (mDescending ? -dy : 0);
      int shift = mDescending ? 1 : -1;
      pGraphics.translate(pX, pY);

      // Right diagonal. 
      pGraphics.setColor(color.darker());
      pGraphics.drawLine(dx / 2, dy, 0, 0);
      pGraphics.drawLine(dx / 2, dy + shift, 0, shift);

      // Left diagonal. 
      pGraphics.setColor(color.brighter());
      pGraphics.drawLine(dx / 2, dy, dx, 0);
      pGraphics.drawLine(dx / 2, dy + shift, dx, shift);

      // Horizontal line. 
      if (mDescending) {
        pGraphics.setColor(color.darker().darker());
      } else {
        pGraphics.setColor(color.brighter().brighter());
      }
      pGraphics.drawLine(dx, 0, 0, 0);

      pGraphics.setColor(color);
      pGraphics.translate(-pX, -pY);
    }

    @Override
    public int getIconWidth() {
      return mSize;
    }

    @Override
    public int getIconHeight() {
      return mSize;
    }
  }

  private class SortableHeaderRenderer implements TableCellRenderer {

    private TableCellRenderer mTableCellRenderer;

    public SortableHeaderRenderer(final TableCellRenderer pTableCellRenderer) {
      mTableCellRenderer = pTableCellRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(final JTable pTable,
            final Object pValue,
            final boolean pIsSelected,
            final boolean pHasFocus,
            final int pRow,
            final int pColumn) {
      Component c = mTableCellRenderer.getTableCellRendererComponent(pTable,
              pValue, pIsSelected, pHasFocus, pRow, pColumn);
      if (c instanceof JLabel) {
        JLabel l = (JLabel) c;
        l.setHorizontalTextPosition(JLabel.LEFT);
        int modelColumn = pTable.convertColumnIndexToModel(pColumn);
        l.setIcon(getHeaderRendererIcon(modelColumn, l.getFont().getSize()));
      }
      return c;
    }
  }

  private static class Directive {

    private int mColumn;
    private int mDirection;

    public Directive(final int pColumn, final int pDirection) {
      mColumn = pColumn;
      mDirection = pDirection;
    }
  }

  /*
    * sorting stuff
   */
  /**
   * Provides programmatic acces to our sorting.
   *
   * @param pColumn the column that should be sorted
   */
  public void forceInitialSorting(final int pColumn) {
    triggerSorting(pColumn, false, false);
  }

  private void triggerSorting(final int pColumn, final boolean pControlDown, final boolean pShiftDown) {
    if (pColumn != -1) {
      int status = getSortingStatus(pColumn);
      if (pControlDown) {
        cancelSorting();
      }
      // Cycle the sorting states through {NOT_SORTED, ASCENDING, DESCENDING} or
      // {NOT_SORTED, DESCENDING, ASCENDING} depending on whether shift is pressed.
      status = status + (pShiftDown ? -1 : 1);
      status = (status + 4) % 3 - 1; // signed mod, returning {-1, 0, 1}
      setSortingStatus(pColumn, status);
    }
  }

  /*
     * IndexConverter interface methods
   */
  @Override
  public int convert2ModelIndex(final int pViewIndex) {
    return modelIndex(pViewIndex);
  }

  @Override
  public int convert2ViewIndex(final int pModelIndex) {
    return viewIndex(pModelIndex);
  }

  /*
     * TableModel interface methods
   */
  @Override
  public int getRowCount() {
    return (mTableModel == null) ? 0 : mTableModel.getRowCount();
  }

  @Override
  public int getColumnCount() {
    return (mTableModel == null) ? 0 : mTableModel.getColumnCount();
  }

  @Override
  public String getColumnName(final int pColumn) {
    return mTableModel.getColumnName(pColumn);
  }

  @Override
  public Class getColumnClass(final int pColumn) {
    return mTableModel.getColumnClass(pColumn);
  }

  @Override
  public boolean isCellEditable(final int pRow, final int pColumn) {
    return mTableModel.isCellEditable(modelIndex(pRow), pColumn);
  }

  @Override
  public Object getValueAt(final int pRow, final int pColumn) {
    return mTableModel.getValueAt(modelIndex(pRow), pColumn);
  }

  @Override
  public void setValueAt(final Object pValue, final int pRow, final int pColumn) {
    mTableModel.setValueAt(pValue, modelIndex(pRow), pColumn);
  }
}
