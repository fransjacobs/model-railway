/*
 * Copyright 2026 Frans Jacobs.
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
import jcs.entities.StationBlockBean;

/**
 * Table Model to show Properties and command of a Driveway aka Route.
 */
public class StationBlockTableModel extends BeanTableModel<StationBlockBean> {

  //private static final long serialVersionUID = -5743157878988659800L;
  private StationBlockBean stationBlockBean;

  public StationBlockTableModel() {
    super();
  }

  public StationBlockBean getStationBlockBean() {
    return stationBlockBean;
  }

  public void setStationBlockBean(StationBlockBean stationBlockBean) {
    this.stationBlockBean = stationBlockBean;
  }

//  private String id;
//  private String stationId;
//  private String blockId;
//  private Date lastUpdated;
//
//  private StationBean station;
//  private BlockBean block;
  @Override
  protected List<String> getColumns() {
    if (this.columns == null) {
      List<String> cols = new ArrayList<>(6);
      cols.add("Id");
      cols.add("StationId");
      cols.add("BlockId");
      cols.add("LastUpdated");
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
  public Object getColumnValue(StationBlockBean stationBlockBean, int column) {
    return switch (column) {
//      case 0 ->
//        stationBlockBean.getTileId();
//      case 1 ->
//        stationBlockBean.getAccessoryId();
//      case 2 ->
//        stationBlockBean.getName();
//      case 3 ->
//        stationBlockBean.getProtocol();
//      case 4 ->
//        stationBlockBean.getAddress();
//      case 5 ->
//        stationBlockBean.getAccessoryValue().getValue();
      default ->
        null;
    };
  }

  @Override
  void setColumnValue(StationBlockBean stationBlockBean, int column, Object value) {
    switch (column) {
//      case 0 ->
//        stationBlockBean.setTileId((String) value);
//      case 1 ->
//        stationBlockBean.setAccessoryId((String) value);
//      case 2 ->
//        stationBlockBean.setName((String) value);
//      case 3 ->
//        stationBlockBean.setProtocol((String) value);
//      case 4 ->
//        stationBlockBean.setAddress((Integer) value);
//      case 5 ->
//        stationBlockBean.setAccessoryValue(AccessoryBean.AccessoryValue.get((String) value));
      default -> {
      }
    }
  }

  @Override
  protected List<StationBlockBean> getBeans() {
    if (stationBlockBean != null) {
      //this.beans = getTurnoutsInRoute(routeBean);
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

  class StationBlockBeanByStationBlockIdSorter implements Comparator<StationBlockBean> {

    @Override
    public int compare(StationBlockBean a, StationBlockBean b) {
      //Avoid null pointers
//      Integer aa = a.getSortOrder();
//      if (aa == null) {
//        aa = 0;
//      }
//      Integer bb = b.getSortOrder();
//      if (bb == null) {
//        bb = 0;
//      }
//      return aa.compareTo(bb);
      return 0;
    }
  }

  @Override
  protected int findRowIndex(StationBlockBean bean) {
    int row = -1;

//    if (bean != null && bean.getId() != null) {
//      Long id = bean.getId();
//      int rowCount = beans.size();
//
//      for (int i = 0; i < rowCount; i++) {
//        StationBlockBean d = beans.get(i);
//        if (id.equals(d.getId())) {
//          row = i;
//          break;
//        }
//      }
//    }
    return row;
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }

}
