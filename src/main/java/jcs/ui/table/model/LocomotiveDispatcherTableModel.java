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

import java.awt.Image;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.autopilot.state.LocomotiveDispatcher;
import jcs.commandStation.autopilot.state.StateEventListener;
import org.tinylog.Logger;

/**
 * Table Model for the dispatcher
 */
public class LocomotiveDispatcherTableModel extends AbstractBeanTableModel<LocomotiveDispatcher> implements StateEventListener {

  private static final String[] DISPLAY_COLUMNS = new String[]{"image", "name", "state", "speed"};

  public LocomotiveDispatcherTableModel() {
    super(LocomotiveDispatcher.class, DISPLAY_COLUMNS);
  }

  @Override
  public void refresh() {
    if (AutoPilot.getInstance().isRunning()) {
      setBeans(AutoPilot.getInstance().getLocomotiveDispatchers());
      Logger.trace("There are " + this.beans.size() + " dispatchers");

      for (LocomotiveDispatcher ld : this.beans) {
        ld.addStateEventListener(this);
        Logger.trace("Listen to dispatcher " + ld.getName());
      }
    } else {
      if (this.beans != null) {
        for (LocomotiveDispatcher ld : this.beans) {
          ld.removeStateEventListener(this);
          Logger.trace("Remove Listen to dispatcher " + ld.getName());
        }
      }
      setBeans(AutoPilot.getInstance().getLocomotiveDispatchers());
    }
  }

  @Override
  public void onStateChange(LocomotiveDispatcher dispatcher) {
    if (this.beans.contains(dispatcher)) {
      //replace
      int idx = this.beans.indexOf(dispatcher);
      this.beans.set(idx, dispatcher);
      //Logger.trace("idx: "+idx+" "+dispatcher.getName()+" "+dispatcher.getDispatcherState());
      //table data changed is too much?
      this.fireTableDataChanged();
    }
  }

  @Override
  public int findRowIndex(LocomotiveDispatcher bean) {
    int row = -1;
    if (bean == null) {
      return row;
    }

    Object idValue = bean.getName();

    if (idValue != null) {
      for (int i = 0; i < this.beans.size(); i++) {
        LocomotiveDispatcher b = beans.get(i);
        Object id = bean.getName();

        if (idValue.equals(id)) {
          row = i;
          break;
        }
      }
    }
    return row;
  }

  @Override
  public int getColumnCount() {
    return DISPLAY_COLUMNS.length;
  }

  @Override
  public String getColumnName(int column) {
    if (column >= 0 && column < DISPLAY_COLUMNS.length) {
      return DISPLAY_COLUMNS[column];
    } else {
      return null;
    }
  }

  @Override
  public Object getValueAt(int row, int column) {
    if (beans != null && row < beans.size()) {
      String columnName = getColumnName(column);
      LocomotiveDispatcher b = beans.get(row);

      if (b != null) {
        return switch (column) {
          case 0 ->
            b.getLocomotiveBean().getLocIcon();
          case 1 ->
            b.getName();
          case 2 ->
            b.getDispatcherState();
          case 3 ->
            ((Long) Math.round((b.getLocomotiveBean().getVelocity() / 1024.0) * 100)).intValue();
          default ->
            null;
        };
      }
    }
    return null;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0 ->
        Image.class;
      case 1 ->
        String.class;
      case 2 ->
        String.class;
      case 3 ->
        Integer.class;
      default ->
        String.class;
    };
  }

}
