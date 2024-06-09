/*
 * Copyright 2023 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.ui.table.model;

import java.util.Collections;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import jcs.persistence.util.EntityInfo;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 * @param <T>
 */
public abstract class AbstractBeanTableModel<T> extends AbstractTableModel {

  protected List<T> beans;
  protected EntityInfo beanInfo;

  public AbstractBeanTableModel(Class T) {
    this(T, null);
  }

  public AbstractBeanTableModel(Class T, String[] displayColumnNames) {
    beanInfo = new EntityInfo(T, displayColumnNames, true);
  }

  public List<T> getBeans() {
    return beans;
  }

  public void setBeans(List<T> beans) {
    if (beans != null && !beans.isEmpty()) {
      this.beans = beans;
    } else {
      this.beans = Collections.EMPTY_LIST;
    }
    this.fireTableDataChanged();
  }

  protected abstract void refresh();

  public void setDisplayColumns(String[] displayColumns) {
    this.beanInfo.setDisplayColumns(displayColumns);
  }

  public void setDisplayColumns(List<String> displayColumnList) {
    this.beanInfo.setDisplayColumnList(displayColumnList);
    this.fireTableDataChanged();
  }

  public int findRowIndex(T bean) {
    int row = -1;
    if (bean == null) {
      return row;
    }

    String pkColumn = (String) this.beanInfo.getPrimaryKeyNames().get(0);
    Object idValue = this.beanInfo.getValue(bean, pkColumn);

    if (idValue != null) {
      for (int i = 0; i < this.beans.size(); i++) {
        T b = beans.get(i);
        Object id = this.beanInfo.getValue(b, pkColumn);

        if (idValue.equals(id)) {
          row = i;
          break;
        }
      }
    }
    return row;
  }

  public void addRow(T row) {
    this.beans.add(row);
    int rowNumAdded = beans.size() - 1;
    fireTableRowsInserted(rowNumAdded, rowNumAdded);
  }

  public void removeRow(T entity) {
    int row = findRowIndex(entity);
    beans.remove(entity);
    fireTableRowsDeleted(row, row);
  }

  /**
   * Removes a row from the table and sends a {@link TableModelEvent} to all registered listeners.
   *
   * @param row the row index.
   */
  public void removeRow(int row) {
    beans.remove(row);
    fireTableRowsDeleted(row, row);
  }

  /**
   * Returns the number of rows in the model.
   *
   * @return The row count.
   */
  @Override
  public int getRowCount() {
    if (beans == null) {
      return 0;
    }
    return beans.size();
  }

  /**
   * Returns the number of columns in the model.
   *
   * @return The column count.
   */
  @Override
  public int getColumnCount() {
    if (beanInfo != null) {

      List<String> columns = beanInfo.getColumnNames();
      return columns.size();
    } else {
      return 0;
    }
  }

  /**
   * @param column the column index.
   *
   * @return The column name.
   */
  @Override
  public String getColumnName(int column) {
    if (this.beanInfo != null) {
      List<String> columns = this.beanInfo.getColumnNames();

      if (column < columns.size()) {
        return columns.get(column);
      }
    }
    return null;
  }

  /**
   * Returns <code>true</code> if the specified cell can be modified, and <code>false</code> otherwise. For this implementation, the
   * method always returns <code>true</code>.
   *
   * @param row the row index.
   * @param column the column index.
   *
   * @return <code>true</code> in all cases.
   */
  @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }

  /**
   * Returns the value at the specified cell in the table.
   *
   * @param row the row index.
   * @param column the column index.
   *
   * @return The value (<code>Object</code>, possibly <code>null</code>) at the specified cell in the table.
   */
  @Override
  public Object getValueAt(int row, int column) {
    if (beans != null && row < beans.size()) {
      String columnName = getColumnName(column);
      T b = beans.get(row);

      if (b != null && columnName != null) {
        return beanInfo.getValue(b, columnName);
      }
    }
    return null;
  }

  /**
   * Sets the value for the specified cell in the table and sends a {@link TableModelEvent} to all registered listeners.
   *
   * @param value the value (<code>Object</code>, <code>null</code> permitted).
   * @param row the row index.
   * @param column the column index.
   */
  @Override
  public void setValueAt(Object value, int row, int column) {
    if (beans != null && row < beans.size()) {
      String columnName = this.getColumnName(column);
      T b = this.beans.get(row);

      if (b != null && columnName != null) {
        this.beanInfo.putValue(b, columnName, value);
        fireTableCellUpdated(row, column);
      }
    }
  }

  /**
   *
   * @param row the row number to
   * @return The controllable device on row
   */
  public T getBeanAt(int row) {
    if (beans != null && row >= 0 && row < beans.size()) {
      //Logger.trace("Row: " + row);
      return beans.get(row);
    }
    Logger.warn("No row " + row + " rowsize is " + (beans == null ? "null" : "" + beans.size()));
    return null;
  }

  @Override
  public int findColumn(String columnName) {
    int col = -1;
    if (beanInfo != null) {
      List<String> columns = beanInfo.getColumnNames();

      for (int i = 0; columns.size() < i; i++) {
        if (columnName.equals(columns.get(i))) {
          col = i;
          break;
        }
      }
    }
    return col;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    String columnName = getColumnName(columnIndex);
    return beanInfo.getColumnDataType(columnName);
  }

}
