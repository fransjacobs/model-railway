/*
 * Copyright (C) 2020 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package lan.wervel.jcs.ui.options.table;

import java.io.Serializable;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import lan.wervel.jcs.entities.ControllableDevice;
import org.pmw.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 * @param <T>
 */
public abstract class DeviceTableModel<T extends ControllableDevice> extends AbstractTableModel implements Serializable {

  protected List<T> devices;

  protected List<String> columns;

  /**
   * Creates an empty table with zero rows and zero columns.
   */
  public DeviceTableModel() {
    init();
  }

  private void init() {
    columns = getColumns();
    devices = getDevices();
  }

  protected abstract List<String> getColumns();

  protected abstract List<T> getDevices();

  public void addRow(T row) {
    this.devices.add(row);
    int rowNumAdded = this.devices.size() - 1;
    fireTableRowsInserted(rowNumAdded, rowNumAdded);
  }

  public void removeRow(T device) {
    int row = this.findRowIndex(device);
    devices.remove(device);
    fireTableRowsDeleted(row, row);
  }

  /**
   * Removes a row from the table and sends a {@link TableModelEvent} to all registered listeners.
   *
   * @param row the row index.
   */
  public void removeRow(int row) {
    this.devices.remove(row);
    fireTableRowsDeleted(row, row);
  }

  /**
   * Returns the number of rows in the model.
   *
   * @return The row count.
   */
  @Override
  public int getRowCount() {
    return this.devices.size();
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

  abstract Object getColumnValue(T device, int column);

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
    if (devices != null && row < devices.size()) {
      Logger.trace("row: " + row + " column: " + column);
      T device = this.devices.get(row);
      return getColumnValue(device, column);
    }
    Logger.warn("row: " + row + " rows: " + devices.size());
    return null;
  }

  abstract void setColumnValue(T device, int column, Object value);

  /**
   * Sets the value for the specified cell in the table and sends a {@link TableModelEvent} to all registered listeners.
   *
   * @param value the value (<code>Object</code>, <code>null</code> permitted).
   * @param row the row index.
   * @param column the column index.
   */
  @Override
  public void setValueAt(Object value, int row, int column) {
    if (this.devices != null && row < this.devices.size()) {
      T device = this.devices.get(row);
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
    if (devices != null && row >= 0 && row < devices.size()) {
      Logger.trace("Row: " + row);
      return this.devices.get(row);
    }
    Logger.warn("No row " + row + " rowsize is " + (devices == null ? "null" : "" + devices.size()));
    return null;
  }

  protected int findRowIndex(T device) {
    int row = -1;

    if (device != null && device.getAddress() != null) {
      Integer address = device.getAddress();
      int rowCount = this.devices.size();

      for (int i = 0; i < rowCount; i++) {
        T d = devices.get(i);
        if (address.equals(d.getAddress())) {
          row = i;
          break;
        }
      }
    }

    return row;
  }

  public void refresh() {
    this.devices = this.getDevices();
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
