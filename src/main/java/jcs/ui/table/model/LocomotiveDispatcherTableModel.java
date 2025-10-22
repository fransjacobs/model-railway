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

import javax.swing.ImageIcon;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.autopilot.state.Dispatcher;
import jcs.commandStation.autopilot.state.StateEventListener;
import jcs.entities.LocomotiveBean.Direction;
import org.tinylog.Logger;

/**
 * Table Model for the dispatcher
 */
public class LocomotiveDispatcherTableModel extends AbstractBeanTableModel<Dispatcher> implements StateEventListener {

  private static final String[] DISPLAY_COLUMNS = new String[]{"image", "name", "state", "speed"};
  private static final long serialVersionUID = 5321472215655025458L;

  public LocomotiveDispatcherTableModel() {
    super(Dispatcher.class, DISPLAY_COLUMNS);
  }

  @Override
  public void refresh() {
    if (AutoPilot.isAutoModeActive()) {
      setBeans(AutoPilot.getLocomotiveDispatchers());
      Logger.trace("There are " + beans.size() + " dispatchers");

      for (Dispatcher ld : beans) {
        ld.addStateEventListener(this);
        Logger.trace("Listen to dispatcher " + ld.getName());
      }
    } else {
      if (beans != null) {
        for (Dispatcher ld : beans) {
          ld.removeStateEventListener(this);
          Logger.trace("Remove Listen to dispatcher " + ld.getName());
        }
      }
      setBeans(AutoPilot.getLocomotiveDispatchers());
    }
  }

  @Override
  public void onStateChange(Dispatcher dispatcher) {
    if (beans.contains(dispatcher)) {
      //replace
      int idx = beans.indexOf(dispatcher);
      beans.set(idx, dispatcher);
      //Logger.trace("idx: "+idx+" "+dispatcher.getName()+" "+dispatcher.getDispatcherStateString());
      //table data changed is too much?
      fireTableDataChanged();
    }
  }

  @Override
  public int findRowIndex(Dispatcher bean) {
    int row = -1;
    if (bean == null) {
      return row;
    }

    Object idValue = bean.getName();

    if (idValue != null) {
      for (int i = 0; i < beans.size(); i++) {
        Dispatcher b = beans.get(i);
        Object id = b.getName();

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
      Dispatcher b = beans.get(row);

      if (b != null) {
        return switch (column) {
          case 0 ->
            b.getLocomotiveBean().getLocIcon();
          case 1 ->
            b.getName();
          case 2 ->
            b.getStateName();
          case 3 ->
            (Direction.FORWARDS == b.getLocomotiveBean().getDirection() ? ">>" : "<<") + " " + ((Long) Math.round((b.getLocomotiveBean().getVelocity() / 1000.0) * 100)).intValue();
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
        ImageIcon.class;
      case 1 ->
        String.class;
      case 2 ->
        String.class;
      case 3 ->
        //Integer.class;
        String.class;
      default ->
        String.class;
    };
  }

}
