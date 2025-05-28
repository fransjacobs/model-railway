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
package jcs.ui.settings.table;

import java.io.Serializable;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 * @param <T>
 */
public abstract class BeanTableModel<T> extends AbstractTableModel implements Serializable {

    protected List<T> beans;

    protected List<String> columns;

    /**
     * Creates an empty table with zero rows and zero columns.
     */
    public BeanTableModel() {
        init();
    }

    private void init() {
        columns = getColumns();
        beans = getBeans();
    }

    protected abstract List<String> getColumns();

    protected abstract List<T> getBeans();

    protected abstract int findRowIndex(T bean);

    public void addRow(T row) {
        this.beans.add(row);
        int rowNumAdded = this.beans.size() - 1;
        fireTableRowsInserted(rowNumAdded, rowNumAdded);
    }

    public void removeRow(T entity) {
        int row = this.findRowIndex(entity);
        beans.remove(entity);
        fireTableRowsDeleted(row, row);
    }

    /**
     * Removes a row from the table and sends a {@link TableModelEvent} to all
     * registered listeners.
     *
     * @param row the row index.
     */
    public void removeRow(int row) {
        this.beans.remove(row);
        fireTableRowsDeleted(row, row);
    }

    /**
     * Returns the number of rows in the model.
     *
     * @return The row count.
     */
    @Override
    public int getRowCount() {
        return this.beans.size();
    }

    /**
     * Returns the number of columns in the model.
     *
     * @return The column count.
     */
    @Override
    public int getColumnCount() {
        return columns == null ? 0 : columns.size();
    }

    /**
     * @param column the column index.
     *
     * @return The column name.
     */
    @Override
    public String getColumnName(int column) {
        if (this.columns != null && column < this.columns.size()) {
            return columns.get(column);
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the specified cell can be modified, and
     * <code>false</code> otherwise. For this implementation, the method always
     * returns <code>true</code>.
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

    abstract Object getColumnValue(T device, int column);

    /**
     * Returns the value at the specified cell in the table.
     *
     * @param row the row index.
     * @param column the column index.
     *
     * @return The value (<code>Object</code>, possibly <code>null</code>) at
     * the specified cell in the table.
     */
    @Override
    public Object getValueAt(int row, int column) {
        if (beans != null && row < beans.size()) {
            //Logger.trace("row: " + row + " column: " + column);
            T device = this.beans.get(row);
            return getColumnValue(device, column);
        }
        Logger.warn("row: " + row + " rows: " + beans.size());
        return null;
    }

    abstract void setColumnValue(T entity, int column, Object value);

    /**
     * Sets the value for the specified cell in the table and sends a
     * {@link TableModelEvent} to all registered listeners.
     *
     * @param value the value (<code>Object</code>, <code>null</code>
     * permitted).
     * @param row the row index.
     * @param column the column index.
     */
    @Override
    public void setValueAt(Object value, int row, int column) {
        if (this.beans != null && row < this.beans.size()) {
            T device = this.beans.get(row);
            setColumnValue(device, column, value);
            fireTableCellUpdated(row, column);
        }
    }

    /**
     *
     * @param row the row number to
     * @return The controllable device on row
     */
    public T getControllableDeviceAt(int row) {
        if (beans != null && row >= 0 && row < beans.size()) {
            //Logger.trace("Row: " + row);
            return this.beans.get(row);
        }
        Logger.warn("No row " + row + " rowsize is " + (beans == null ? "null" : "" + beans.size()));
        return null;
    }

//    protected int findRowIndex(T entity) {
//        int row = -1;
//
//        if (entity != null && entity.getId() != null) {
//            Object id = entity.getId();
//            int rowCount = this.beans.size();
//
//            for (int i = 0; i < rowCount; i++) {
//                T d = beans.get(i);
//                if (id.equals(d.getId())) {
//                    row = i;
//                    break;
//                }
//            }
//        }
//
//        return row;
//    }
    public void refresh() {
        this.beans = this.getBeans();
        this.fireTableDataChanged();
    }

    @Override
    public int findColumn(String columnName) {
        int col = -1;

        if (this.columns != null) {
            for (int i = 0; this.columns.size() < i; i++) {
                if (columnName.equals(this.columns.get(i))) {
                    col = i;
                    break;
                }
            }
        }
        return col;
    }

}
