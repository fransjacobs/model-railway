package jcs.ui.table.cuf;

import jcs.util.model.cuf.SelectionInList;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import jcs.util.model.cuf.DelegateAccess;
import jcs.util.model.cuf.ValueHolder;

/**
 * This class maps the content of List of a SelectionInList ValueModel to a JTable TableModel and ListSelectionModel.<br/>
 * Each entry in the list of the SelectionInList ValueModel describes a row, all entries are assumed to be objects from the same
 * class/interface. Whenever the either the list or the selection changes, the table is adjusted accordingly. The table data is
 * read-only, when the user changes the selection in the table, the selection holder of the SelectionInList is changed accordingly.
 * Only single-selection is therefore supported.<br/>
 * The initial selection is taken from the SelectionInList value model. FIXME: currently we never unregister from the
 * SelectionInList value model, this might lead to memory leaks.
 */
public class ListTableMapper extends ListTableMapperBase {

  /**
   * List of Mapping objects, contains a DelegateAccess object for each column, never null
   */
  private List<Mapping> mColumnMapping;

  /**
   * shared empty array
   */
  private static final Class[] NO_CLASSES = {};
  /**
   * shared empty array
   */
  private static final Object[] NO_OBJECTS = {};

  /**
   * Create a new adaption object between a JTable and a SelectionInList value model. This object is also set as the JTable's
   * TableModel and ListSelectionModel.
   *
   * @param pTable the table for which we provide TableModel and ListSelectionModel behaviour
   * @param pValueModel the value model that drives the table (data, selection) and gets updated by the table (selection only)
   * @param pMapping non-null List containing Mapping objects
   * @param pSortable true if table is sortable
   * @throws IllegalArgumentException if a parameter is bogus
   */
  public ListTableMapper(final JTable pTable, final SelectionInList pValueModel, final List<Mapping> pMapping, final boolean pSortable) {
    this(pTable, pValueModel, pMapping, pSortable, -1);
  }

  /**
   * Create a new adaption object between a JTable and a SelectionInList value model. This object is also set as the JTable's
   * TableModel and ListSelectionModel.
   *
   * @param pTable the table for which we provide TableModel and ListSelectionModel behaviour
   * @param pValueModel the value model that drives the table (data, selection) and gets updated by the table (selection only)
   * @param pMapping non-null List containing Mapping objects
   * @param pSortable true if table is sortable
   * @param pColumnForInitialSorting -1 or the column index for initial sorting
   * @throws IllegalArgumentException if a parameter is bogus
   */
  public ListTableMapper(final JTable pTable, final SelectionInList pValueModel, final List<Mapping> pMapping,
          final boolean pSortable, final int pColumnForInitialSorting) {
    mColumnMapping = Collections.emptyList();
    init(pTable, pValueModel);
    setColumnMapping(pTable, pMapping, pSortable, pColumnForInitialSorting);
  }

  /*
     * "our" handling code (=no Swing or ValueModel callback/handling stuff)
   */
  /**
   * The method maps the attributes of our List entry class to column names in the table and the alignments of the columns.
   *
   * @param pTable the table for which we set the column alingnments
   * @param pMapping non-null List containing Mapping objects
   * @param pSortable true if table is sortable
   * @param pColumnForInitialSorting -1 or the column index for initial sorting
   * @throws IllegalArgumentException if pMapping is null or contains invalid mappings
   */
  private void setColumnMapping(final JTable pTable, final List<Mapping> pMapping, final boolean pSortable, final int pColumnForInitialSorting) {
    if (pMapping == null) {
      throw new IllegalArgumentException("mapping must not be null");
    }

    List<Mapping> columnMapping = new ArrayList<Mapping>(pMapping.size());
    for (int i = 0, n = pMapping.size(); i < n; i++) {
      Mapping mapping = pMapping.get(i);
      Mapping myMapping = new Mapping(mapping);
      columnMapping.add(myMapping);
    }
    mColumnMapping = columnMapping;

    // notify the table to re-create the columns
    TableModelEvent e = new TableModelEvent(this, TableModelEvent.HEADER_ROW);
    fireTableChanged(e);

    // make table sortable with a NewTabelSorter-TableModel
    if (pSortable) {
      NewTableSorter sorter = new NewTableSorter(pTable.getModel());
      sorter.setTableHeader(pTable.getTableHeader());
      pTable.setModel(sorter);

      setIndexConverter(sorter);

      // add Comparator for sorting, if necessary
      for (int i = 0, n = pMapping.size(); i < n; i++) {
        Mapping mapping = pMapping.get(i);

        if (mapping.getComparatorClass() != null) {
          try {
            Comparator myComparator
                    = (Comparator) mapping
                            .getComparatorClass()
                            .getConstructor(NO_CLASSES)
                            .newInstance(NO_OBJECTS);

            sorter.setColumnComparator(mapping.getColumnClass(), myComparator);
          } catch (Exception e1) {
            //throw SwingXMLBuilder.createException("could not instantiate ComparatorClass "
            //        + mapping.getComparatorClass() + " for "
            //        + mapping.getColumnClass(), e1);
            throw new RuntimeException("could not instantiate ComparatorClass ");
          }
        }
      }

      // force initial sorting
      if (pColumnForInitialSorting >= 0 && pColumnForInitialSorting < sorter.getColumnCount()) {
        sorter.forceInitialSorting(pColumnForInitialSorting);
      }
    }

    for (int i = 0, n = pMapping.size(); i < n; i++) {
      Mapping mapping = pMapping.get(i);

      // set defined column alignment
      if (mapping.getColumnAlignment() != null) {
        pTable.getColumnModel().getColumn(i).setCellRenderer(
                new ColumnAlignmentRenderer(mapping.getColumnAlignment()));
      } else {
        // if no alignment defined: set default-alignment per column-class
        if (mapping.getColumnClass() != null
                && Boolean.class.isAssignableFrom(mapping.getColumnClass())) {
          pTable.getColumnModel().getColumn(i).setCellRenderer(
                  new ColumnAlignmentRenderer(ColumnAlignmentRenderer.ALIGN_CENTER));
        } else if (mapping.getColumnClass() != null
                && (Number.class.isAssignableFrom(mapping.getColumnClass())
                || Date.class.isAssignableFrom(mapping.getColumnClass()))) {
          pTable.getColumnModel().getColumn(i).setCellRenderer(
                  new ColumnAlignmentRenderer(ColumnAlignmentRenderer.ALIGN_RIGHT));
        } else {
          pTable.getColumnModel().getColumn(i).setCellRenderer(
                  new ColumnAlignmentRenderer(ColumnAlignmentRenderer.ALIGN_LEFT));
        }
      }

      // set prefered width for table columns
      if (mapping.getColumnPrefWidthIntValue() > 0) {
        // if we don't disable the auto resize, preferred with has no consequences
        pTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTextField dummyTextField = new JTextField(mapping.getColumnPrefWidthIntValue());
        pTable.getColumnModel().getColumn(i).setPreferredWidth(dummyTextField.getPreferredSize().width);

      }
    }
  }

  /**
   * ***************************************************************
   */

  /*
     * TableModel callbacks
   */
  public int getColumnCount() {
    return mColumnMapping.size();
  }

  public String getColumnName(final int pColumnIndex) {
    return mColumnMapping.get(pColumnIndex).getColumnTitle();
  }

  public Class getColumnClass(final int pColumnIndex) {
    Class clazz = mColumnMapping.get(pColumnIndex).getColumnClass();
    if (clazz == null) {
      clazz = Object.class;
    }
    return clazz;
  }

  public Object getValueAt(final int pRowIndex, final int pColumnIndex) {
    Mapping mapping = mColumnMapping.get(pColumnIndex);
    DelegateAccess valueModel = mapping.getValueModel();
    List list = mSelectionInList.getValue();
    Object value = list.get(pRowIndex);
    Object back = valueModel.getValue(value);
    return back;
  }

  public Object getRawValueAt(final int pRowIndex, final int pColumnIndex) {
    List list = mSelectionInList.getValue();
    Object value = list.get(pRowIndex);
    return value;
  }

  public Object getValueForSortingAt(final int pRowIndex, final int pColumnIndex) {
    Mapping mapping = mColumnMapping.get(pColumnIndex);
    DelegateAccess valueModelForSorting = mapping.getValueModelForSorting();
    List list = mSelectionInList.getValue();
    Object value = list.get(pRowIndex);
    Object back = valueModelForSorting.getValue(value);
    return back;
  }

  public boolean isColumnSortable(final int pColumnIndex) {
    Mapping mapping = mColumnMapping.get(pColumnIndex);
    return mapping.isSortable();
  }

  /**
   * ***************************************************************
   */
  /**
   * ***************************************************************
   */
  /**
   * Small helper class to describe the attribut/column mapping.
   */
  public static class Mapping {

    /**
     * name of the table column, never null
     */
    private String mColumnTitle;
    /**
     * alignment of the table column, may be null
     */
    private String mColumnAlignment;
    /**
     * prefWidth of the table column or 0
     */
    private int mColumnPrefWidth;
    /**
     * class of the table column, may be null
     */
    private Class mColumnClass;
    /**
     * ValueModel to get the attribute, never null
     */
    private DelegateAccess mValueModel;
    /**
     * ValueModel to get the attribute in the sorting case, may be null
     */
    private DelegateAccess mValueModelForSorting;
    /**
     * flag if this column should be sortable
     */
    private boolean mSortable;
    /**
     * comparator-class for sorting, may be null
     */
    private Class mComparatorClass;

    public Mapping() {
      mColumnTitle = "";
      mColumnAlignment = null;
      mColumnPrefWidth = 0;
      mColumnClass = null;
      mValueModel = new ValueHolder();
      mValueModelForSorting = null;
      mSortable = false;
      mComparatorClass = null;
    }

    public Mapping(final Mapping pCopyFrom) {
      this(pCopyFrom.getColumnTitle(), pCopyFrom.getColumnAlignment(), pCopyFrom.getColumnPrefWidth(),
              pCopyFrom.getColumnClass(), pCopyFrom.getValueModel(),
              pCopyFrom.getValueModelForSorting(), pCopyFrom.isSortable(), pCopyFrom.getComparatorClassName());
    }

    public Mapping(final String pColumnTitle) {
      this();
      setColumnTitle(pColumnTitle);
    }

    public Mapping(final String pColumnTitle, final Class pColumnClass) {
      this();
      setColumnTitle(pColumnTitle);
      setColumnClass(pColumnClass);
    }

    public Mapping(final String pColumnTitle, final Class pColumnClass, final DelegateAccess pValueModel) {
      this();
      setColumnTitle(pColumnTitle);
      setColumnClass(pColumnClass);
      setValueModel(pValueModel);
    }

    public Mapping(final String pColumnTitle, final String pColumnAlignment, final Class pColumnClass, final DelegateAccess pValueModel) {
      this();
      setColumnTitle(pColumnTitle);
      setColumnAlignment(pColumnAlignment);
      setColumnClass(pColumnClass);
      setValueModel(pValueModel);
    }

    public Mapping(final String pColumnTitle, final String pColumnAlignment, final String pColumnPrefWidth, final Class pColumnClass,
            final DelegateAccess pValueModel, final DelegateAccess pValueModelForSorting, final boolean pSortable,
            final String pComparatorClassName) {
      this();
      setColumnTitle(pColumnTitle);
      setColumnAlignment(pColumnAlignment);
      setColumnPrefWidth(pColumnPrefWidth);
      setColumnClass(pColumnClass);
      setValueModel(pValueModel);
      setValueModelForSorting(pValueModelForSorting);
      setSortable(pSortable);
      setComparatorClassName(pComparatorClassName);
    }

    public String getColumnTitle() {
      return mColumnTitle;
    }

    private void setColumnTitle(final String pColumnTitle) {
      if (pColumnTitle == null) {
        throw new IllegalArgumentException("column title must not be null");
      }
      mColumnTitle = pColumnTitle;
    }

    public String getColumnAlignment() {
      return mColumnAlignment;
    }

    private void setColumnAlignment(final String pColumnAlignment) {
      mColumnAlignment = pColumnAlignment;
    }

    public String getColumnPrefWidth() {
      return Integer.toString(mColumnPrefWidth);
    }

    public int getColumnPrefWidthIntValue() {
      return mColumnPrefWidth;
    }

    private void setColumnPrefWidth(final String pColumnPrefWidth) {
      if (pColumnPrefWidth == null) {
        mColumnPrefWidth = 0;
      } else {
        try {
          mColumnPrefWidth = Integer.parseInt(pColumnPrefWidth);
        } catch (NumberFormatException e) {
          //throw SwingXMLBuilder.createException("could not parse prefWidth "
          //        + pColumnPrefWidth + " of column " + getColumnTitle(), e);
          throw new RuntimeException("could not parse prefWidth ");
        }
      }
    }

    public Class getComparatorClass() {
      return mComparatorClass;
    }

    public String getComparatorClassName() {
      return mComparatorClass == null ? null : mComparatorClass.getName();
    }

    private void setComparatorClassName(final String pComparatorClass) {
      if (pComparatorClass == null) {
        return;
      }

      try {
        mComparatorClass = Class.forName(pComparatorClass);
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException("class " + pComparatorClass + " not found");
      }
    }

    public Class getColumnClass() {
      return mColumnClass;
    }

    private void setColumnClass(final Class pColumnClass) {
      mColumnClass = pColumnClass;
    }

    public DelegateAccess getValueModel() {
      return mValueModel;
    }

    private void setValueModel(final DelegateAccess pValueModel) {
      if (pValueModel == null) {
        throw new IllegalArgumentException("value model must not be null");
      }
      mValueModel = pValueModel;
    }

    public DelegateAccess getValueModelForSorting() {
      return mValueModelForSorting;
    }

    public void setValueModelForSorting(final DelegateAccess pValueModelForSorting) {
      mValueModelForSorting = pValueModelForSorting;
    }

    public boolean isSortable() {
      return mSortable;
    }

    private void setSortable(final boolean pSortable) {
      mSortable = pSortable;
    }

    public boolean equals(final Object pOther) {
      if (this == pOther) {
        return true;
      }
      if (!(pOther instanceof Mapping)) {
        return false;
      }

      final Mapping mapping = (Mapping) pOther;

      if (mColumnPrefWidth != mapping.mColumnPrefWidth) {
        return false;
      }
      if (mSortable != mapping.mSortable) {
        return false;
      }
      if (mColumnAlignment != null ? !mColumnAlignment.equals(mapping.mColumnAlignment) : mapping.mColumnAlignment != null) {
        return false;
      }
      if (mColumnClass != null ? !mColumnClass.equals(mapping.mColumnClass) : mapping.mColumnClass != null) {
        return false;
      }
      if (mColumnTitle != null ? !mColumnTitle.equals(mapping.mColumnTitle) : mapping.mColumnTitle != null) {
        return false;
      }
      if (mComparatorClass != null ? !mComparatorClass.equals(mapping.mComparatorClass) : mapping.mComparatorClass != null) {
        return false;
      }
      if (mValueModel != null ? !mValueModel.equals(mapping.mValueModel) : mapping.mValueModel != null) {
        return false;
      }
      //noinspection RedundantIfStatement
      if (mValueModelForSorting != null ? !mValueModelForSorting.equals(mapping.mValueModelForSorting) : mapping.mValueModelForSorting != null) {
        return false;
      }

      return true;
    }

    public int hashCode() {
      int result;
      result = (mColumnTitle != null ? mColumnTitle.hashCode() : 0);
      result = 29 * result + (mColumnAlignment != null ? mColumnAlignment.hashCode() : 0);
      result = 29 * result + mColumnPrefWidth;
      result = 29 * result + (mColumnClass != null ? mColumnClass.hashCode() : 0);
      result = 29 * result + (mValueModel != null ? mValueModel.hashCode() : 0);
      result = 29 * result + (mValueModelForSorting != null ? mValueModelForSorting.hashCode() : 0);
      result = 29 * result + (mSortable ? 1 : 0);
      result = 29 * result + (mComparatorClass != null ? mComparatorClass.hashCode() : 0);
      return result;
    }

    public String toString() {
      return "ListTableMapper.Mapping{"
              + "ColumnTitle='" + mColumnTitle + '\''
              + ", ColumnAlignment='" + mColumnAlignment + '\''
              + ", ColumnPrefWidth=" + mColumnPrefWidth
              + ", ColumnClass=" + mColumnClass
              + ", ValueModel=" + mValueModel
              + ", ValueModelForSorting=" + mValueModelForSorting
              + ", Sortable=" + mSortable
              + ", ComparatorClass=" + mComparatorClass
              + '}';
    }
  }
}
