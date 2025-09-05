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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;
import jcs.commandStation.events.AllSensorEventsListener;

/**
 * @author frans
 */
public class SensorTableModel extends BeanTableModel<SensorBean> implements AllSensorEventsListener {

  private static final long serialVersionUID = -5544147259543750662L;

  private final Map<Integer, SensorBean> sensorBeanCache;

  public SensorTableModel() {
    super();
    sensorBeanCache = new HashMap<>();
    beans = new LinkedList<>();
  }

  @Override
  protected List<SensorBean> getBeans() {
    if (sensorBeanCache == null) {
      return Collections.EMPTY_LIST;
    }
    sensorBeanCache.clear();

    if (PersistenceFactory.getService() != null) {
      List<SensorBean> sbl = PersistenceFactory.getService().getSensors();
      for (SensorBean sb : sbl) {
        Integer key = sb.getDeviceId() + sb.getContactId();
        sensorBeanCache.put(key, sb);
      }
      return sbl;
    } else {
      List<SensorBean> sensorList = new LinkedList<>();
      return sensorList;
    }
  }

  @Override
  public void onSensorChange(SensorEvent event) {
    SensorBean sensor = event.getSensorBean();
    Integer key = null;
    if (sensor.getId() != null) {
      key = sensor.getId();
    } else if (sensor.getDeviceId() != null && sensor.getContactId() != null) {
      key = sensor.getDeviceId() + sensor.getContactId();
    }

    if (key != null) {
      if (sensorBeanCache.containsKey(key)) {
        SensorBean csb = sensorBeanCache.get(key);
        csb.setStatus(sensor.getStatus());
        csb.setPreviousStatus(sensor.getPreviousStatus());
        csb.setMillis(sensor.getMillis());
        csb.setLastUpdated(sensor.getLastUpdated());
      } else {
        sensorBeanCache.put(key, sensor);
        beans.add(sensor);
      }

      fireTableDataChanged();
    } else {
      Logger.warn("Sensor event without a identifiable key " + sensor);
    }
  }

  @Override
  public List<String> getColumns() {
    if (columns == null) {
      List<String> cols = new ArrayList<>(5);
      cols.add("ID");
      cols.add("Name");
      cols.add("DeviceId");
      cols.add("ContactId");
      cols.add("Status");
      cols.add("PreviousStatus");
      cols.add("Millis");
      return cols;
    }
    return columns;
  }

  @Override
  public Object getColumnValue(SensorBean sensor, int column) {
    return switch (column) {
      case 0 ->
        sensor.getId();
      case 1 ->
        sensor.getName();
      case 2 ->
        sensor.getDeviceId();
      case 3 ->
        sensor.getContactId();
      case 4 ->
        sensor.getStatus();
      case 5 ->
        sensor.getPreviousStatus();
      case 6 ->
        sensor.getMillis();
      default ->
        null;
    };
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0 ->
        Integer.class;
      case 1 ->
        String.class;
      case 2 ->
        Integer.class;
      case 3 ->
        Integer.class;
      case 4 ->
        Integer.class;
      case 5 ->
        Integer.class;
      case 6 ->
        Integer.class;
      default ->
        String.class;
    };
  }

  @Override
  void setColumnValue(SensorBean sensor, int column, Object value) {
    switch (column) {
      case 0 ->
        sensor.setId((Integer) value);
      case 1 ->
        sensor.setName((String) value);
      case 2 ->
        sensor.setDeviceId((Integer) value);
      case 3 ->
        sensor.setContactId((Integer) value);
      case 4 ->
        sensor.setStatus((Integer) value);
      case 5 ->
        sensor.setPreviousStatus((Integer) value);
      case 6 ->
        sensor.setMillis((Integer) value);
      default -> {
      }
    }
    PersistenceFactory.getService().persist(sensor);
  }

  public void clear() {
    if (beans != null) {
      beans.clear();
    }
    sensorBeanCache.clear();
    fireTableDataChanged();
  }

  @Override
  protected int findRowIndex(SensorBean bean) {
    int row = -1;

    if (bean != null && bean.getId() != null) {
      Integer id = bean.getId();
      int rowCount = beans.size();

      for (int i = 0; i < rowCount; i++) {
        SensorBean d = beans.get(i);
        if (id.equals(d.getId())) {
          row = i;
          break;
        }
      }
    }
    return row;
  }
}
