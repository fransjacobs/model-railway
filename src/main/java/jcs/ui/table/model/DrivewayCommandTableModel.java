/*
 * Copyright 2025 Frans Jacobs.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import jcs.commandStation.entities.DrivewayCommand;
import jcs.entities.AccessoryBean;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;

/**
 * Table Model to show Properties and command of a Driveway aka Route.
 */
public class DrivewayCommandTableModel extends BeanTableModel<DrivewayCommand> {

  private static final long serialVersionUID = -5743157878988659800L;

  private RouteBean routeBean;

  public DrivewayCommandTableModel() {
    super();
  }

  public RouteBean getRouteBean() {
    return routeBean;
  }

  public void setRouteBean(RouteBean routeBean) {
    this.routeBean = routeBean;
  }

  @Override
  protected List<String> getColumns() {
    if (this.columns == null) {
      List<String> cols = new ArrayList<>(6);
      cols.add("TileID");
      cols.add("AccessoryID");
      cols.add("Name");
      cols.add("Protocol");
      cols.add("Address");
      cols.add("Value");
      return cols;
    }
    return this.columns;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0 ->
        String.class;
      case 1 ->
        String.class;
      case 2 ->
        String.class;
      case 3 ->
        String.class;
      case 4 ->
        Integer.class;
      case 5 ->
        String.class;
      default ->
        null;
    };
  }

  @Override
  public Object getColumnValue(DrivewayCommand drivewayCommand, int column) {
    return switch (column) {
      case 0 ->
        drivewayCommand.getTileId();
      case 1 ->
        drivewayCommand.getAccessoryId();
      case 2 ->
        drivewayCommand.getName();
      case 3 ->
        drivewayCommand.getProtocol();
      case 4 ->
        drivewayCommand.getAddress();
      case 5 ->
        drivewayCommand.getAccessoryValue().getValue();
      default ->
        null;
    };
  }

  @Override
  void setColumnValue(DrivewayCommand drivewayCommand, int column, Object value) {
    switch (column) {
      case 0 ->
        drivewayCommand.setTileId((String) value);
      case 1 ->
        drivewayCommand.setAccessoryId((String) value);
      case 2 ->
        drivewayCommand.setName((String) value);
      case 3 ->
        drivewayCommand.setProtocol((String) value);
      case 4 ->
        drivewayCommand.setAddress((Integer) value);
      case 5 ->
        drivewayCommand.setAccessoryValue(AccessoryBean.AccessoryValue.get((String) value));
      default -> {
      }
    }
  }

  @Override
  protected List<DrivewayCommand> getBeans() {
    if (routeBean != null) {
      this.beans = getTurnoutsInRoute(routeBean);
      return this.beans;
    } else {
      if (this.beans != null) {
        this.beans.clear();
      }

//      if (PersistenceFactory.getService() != null) {
//        List<RouteBean> routes = PersistenceFactory.getService().getRoutes();
//        List<DrivewayCommand> drivewayCommands = new ArrayList<>();
//        for (RouteBean rb : routes) {
//          drivewayCommands.addAll(getTurnoutsInRoute(rb));
//        }
//        return drivewayCommands;
//      } else {
      return Collections.emptyList();
//      }
    }
  }

  protected List<DrivewayCommand> getTurnoutsInRoute(RouteBean routeBean) {
    List<RouteElementBean> rel = routeBean.getRouteElements();
    List<DrivewayCommand> drivewayCommands = new ArrayList<>();
    for (RouteElementBean reb : rel) {
      if (reb.isTurnout()) {

        AccessoryBean.AccessoryValue av = reb.getAccessoryValue();
        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();

        DrivewayCommand dc = new DrivewayCommand(reb.getId(), reb.getRouteId(), reb.getTileId(), turnout.getId(), turnout.getAddress(), turnout.getProtocol().toString(), turnout.getName(), reb.getAccessoryValue(), reb.getElementOrder());
        drivewayCommands.add(dc);
      }
    }
    Collections.sort(drivewayCommands, new DrivewayCommandBySortOrderSorter());
    return drivewayCommands;
  }

  class DrivewayCommandBySortOrderSorter implements Comparator<DrivewayCommand> {

    @Override
    public int compare(DrivewayCommand a, DrivewayCommand b) {
      //Avoid null pointers
      Integer aa = a.getSortOrder();
      if (aa == null) {
        aa = 0;
      }
      Integer bb = b.getSortOrder();
      if (bb == null) {
        bb = 0;
      }
      return aa.compareTo(bb);
    }
  }

  @Override
  protected int findRowIndex(DrivewayCommand bean) {
    int row = -1;

    if (bean != null && bean.getId() != null) {
      Long id = bean.getId();
      int rowCount = beans.size();

      for (int i = 0; i < rowCount; i++) {
        DrivewayCommand d = beans.get(i);
        if (id.equals(d.getId())) {
          row = i;
          break;
        }
      }
    }
    return row;
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }

}
