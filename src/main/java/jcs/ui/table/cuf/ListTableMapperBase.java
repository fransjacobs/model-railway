package jcs.ui.table.cuf;

import jcs.util.model.cuf.SelectionInList;

import javax.swing.table.TableModel;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import java.util.List;

/**
 * ListTableMapperBase contains the common code of TableSelectionModel and ListTableMapper. The main difference between those is
 * that the TableSelectionModel supports editing but requires a row data object that contains all displayed column values, and the
 * ListTableMapper is read only but supports navigation through multiple getters and conversions.
 *
 * @param <T> The Type we have for a table row entry
 */
public abstract class ListTableMapperBase<T> implements TableModel, ListSelectionListener {

  /**
   * our JTable, never null
   */
  private JTable mTable;
  /**
   * value model holding the list, never null
   */
  protected SelectionInList<T> mSelectionInList;
  /**
   * helper for the TableModel callback handling
   */
  private EventListenerList mListenerList;
  /**
   * marker if we are inside a selection change, we then ignore selection changes from the "other" model
   */
  private boolean mInSelectionChange;
  /**
   * a IndexConverter maps the view index to the model index and vice versa, never null
   */
  private IndexConverter mIndexConverter;

  /**
   * the shared identity converter
   */
  private static final IndexConverter IDENTITY_CONVERTER = new IdentityIndexConverter();

  /**
   * Common construction stuff, it creates a new adaption between a JTable and a SelectionInList value model. This object is also
   * set as the JTable's TableModel and ListSelectionModel.
   *
   * @param pTable the table for which we provide TableModel and ListSelectionModel behaviour
   * @param pValueModel the value model that drives the table (data, selection) and gets updated by the table (selection only)
   * @throws IllegalArgumentException if a parameter is bogus
   */
  protected void init(final JTable pTable, final SelectionInList<T> pValueModel) {
    if (pTable == null) {
      throw new IllegalArgumentException("the table must not be null");
    }
    if (pValueModel == null) {
      throw new IllegalArgumentException("the value model must not be null");
    }

    mTable = pTable;
    mSelectionInList = pValueModel;
    mListenerList = new EventListenerList();
    mInSelectionChange = false;
    mIndexConverter = IDENTITY_CONVERTER;

    // we support only single-selection
    pTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // this must stay at the end so that everything is setup first
    mSelectionInList.onChangeSend(this, "vmDataChanged");
    mSelectionInList.selectionHolder().onChangeSend(this, "vmSelectionChanged");
    pTable.setModel(this);
    pTable.getSelectionModel().addListSelectionListener(this);
  }

  /**
   * Setter for IndexConverter, see ListTableMapper for details.
   *
   * @param pIndexConverter the new IndexConverter, must not be null
   */
  protected void setIndexConverter(final IndexConverter pIndexConverter) {
    if (pIndexConverter == null) {
      throw new IllegalArgumentException("IndexConverter must not be null");
    }
    mIndexConverter = pIndexConverter;
    vmSelectionChanged(new ChangeEvent(this));
  }

  /**
   * Called whenever the data of the SelectionInList changes.
   *
   * @param pEvent not used
   */
  @SuppressWarnings({"UnusedDeclaration"})
  public void vmDataChanged(final ChangeEvent pEvent) {
    // ignore the changes we triggered
    if (mInSelectionChange) {
      return;
    }

    // notify the table to re-read the data, this will clear the selection,
    // so we re-set the selection afterwards
    int modelIndex = mSelectionInList.selectionHolder().intValue();

    mInSelectionChange = true;
    try {
      TableModelEvent e = new TableModelEvent(this);
      fireTableChanged(e);
      setTableSelection(modelIndex);
    } finally {
      mInSelectionChange = false;
    }
  }

  /**
   * Called whenever the selection of the SelectionInList changes.
   *
   * @param pEvent not used
   */
  @SuppressWarnings({"UnusedDeclaration"})
  public void vmSelectionChanged(final ChangeEvent pEvent) {
    // ignore the changes we triggered
    if (mInSelectionChange) {
      return;
    }

    mInSelectionChange = true;
    try {
      // re-adjust the table selection in our JTable
      int modelIndex = mSelectionInList.selectionHolder().intValue();
      setTableSelection(modelIndex);
    } finally {
      mInSelectionChange = false;
    }
  }

  /**
   * Helper method to set (or un-set) the table selection
   *
   * @param pModelIndex the model index, may be -1
   */
  private void setTableSelection(final int pModelIndex) {
    int viewIndex = mIndexConverter.convert2ViewIndex(pModelIndex);
    if (viewIndex == -1) {
      mTable.getSelectionModel().clearSelection();
    } else {
      mTable.getSelectionModel().setSelectionInterval(viewIndex, viewIndex);
    }
  }

  /**
   * Called whenever the selection of the JTable changes, we update our SelectionInList index.
   *
   * @param pEvent event describing the new selection
   */
  public void valueChanged(final ListSelectionEvent pEvent) {
    // ignore the changes we triggered
    if (mInSelectionChange) {
      return;
    }
    // ignore intermediate changes
    if (pEvent.getValueIsAdjusting()) {
      return;
    }

    mInSelectionChange = true;
    try {
      int viewIndex = mTable.getSelectedRow();
      // due to Bug 4905083, up to JDK 1.5, we must ignore changes that 
      // happen with invalid rows
      if (mTable.getRowCount() <= viewIndex) {
        return;
      }
      Integer modelIndex;
      if (viewIndex < 0) {
        modelIndex = SelectionInList.NO_SELECTION;
      } else {
        modelIndex = mIndexConverter.convert2ModelIndex(viewIndex);
      }
      mSelectionInList.selectionHolder().setValueForced(modelIndex);
    } finally {
      mInSelectionChange = false;
    }
  }

  /*
     * Swing TableModel methods
   */
  public int getRowCount() {
    List<T> list = mSelectionInList.getValue();
    return list == null ? 0 : list.size();
  }

  public boolean isCellEditable(final int pRowIndex, final int pColumnIndex) {
    return false;
  }

  public void setValueAt(final Object pValue, final int pRowIndex, final int pColumnIndex) {
    throw new UnsupportedOperationException("this is a read-only table model");
  }

  public void addTableModelListener(final TableModelListener pTableModelListener) {
    mListenerList.add(TableModelListener.class, pTableModelListener);
  }

  public void removeTableModelListener(final TableModelListener pTableModelListener) {
    mListenerList.remove(TableModelListener.class, pTableModelListener);
  }

  /**
   * Helper to fire a table event to all our table model listeners.
   *
   * @param pEvent the table event
   */
  protected void fireTableChanged(final TableModelEvent pEvent) {
    Object[] listeners = mListenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TableModelListener.class) {
        ((TableModelListener) listeners[i + 1]).tableChanged(pEvent);
      }
    }
  }

  /**
   * Small helper class that provides a model index = view index mapping.
   */
  public static class IdentityIndexConverter implements IndexConverter {

    public int convert2ModelIndex(final int pViewIndex) {
      return pViewIndex;
    }

    public int convert2ViewIndex(final int pModelIndex) {
      return pModelIndex;
    }
  }
}
