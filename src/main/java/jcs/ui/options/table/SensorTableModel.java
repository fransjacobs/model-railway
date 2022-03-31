/*
 * Copyright (C) 2019 frans.
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
package jcs.ui.options.table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jcs.entities.SensorBean;
import jcs.trackservice.TrackServiceFactory;
import jcs.trackservice.events.SensorListener;

/**
 *
 * @author frans
 */
public class SensorTableModel extends EntityTableModel<SensorBean> implements SensorListener {

    private Map<Integer, SensorBean> sensorBeanCache;

    public SensorTableModel() {
        super();
        this.sensorBeanCache = new HashMap<>();
        this.devices = new LinkedList<>();
    }

    @Override
    protected List<SensorBean> getEntityBeans() {
        if (sensorBeanCache == null) {
            this.sensorBeanCache = new HashMap<>();
        }
        this.sensorBeanCache.clear();

        if (TrackServiceFactory.getTrackService() != null) {
            List<SensorBean> beans = TrackServiceFactory.getTrackService().getSensors();
            for (SensorBean sb : beans) {
                Integer key = sb.getDeviceId() + sb.getContactId();
                this.sensorBeanCache.put(key, sb);
            }

            return beans;
        } else {
            List<SensorBean> sensorList = new LinkedList<>();
            return sensorList;
        }
    }

    @Override
    public void onChange(SensorBean sensor) {
        Integer key = sensor.getDeviceId() + sensor.getContactId();
        if (this.sensorBeanCache.containsKey(key)) {
            SensorBean csb = this.sensorBeanCache.get(key);
            csb.setStatus(sensor.getStatus());
            csb.setPreviousStatus(sensor.getPreviousStatus());
            csb.setMillis(sensor.getMillis());
            csb.setLastUpdated(sensor.getLastUpdated());
        } else {
            sensorBeanCache.put(key, sensor);
            devices.add(sensor);
        }

        this.fireTableDataChanged();
    }

    @Override
    public List<String> getColumns() {
        if (this.columns == null) {
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
        return this.columns;
    }

    @Override
    public Object getColumnValue(SensorBean sensor, int column) {
        switch (column) {
            case 0:
                return sensor.getId();
            case 1:
                return sensor.getName();
            case 2:
                return sensor.getDeviceId();
            case 3:
                return sensor.getContactId();
            case 4:
                return sensor.getStatus();
            case 5:
                return sensor.getPreviousStatus();
            case 6:
                return sensor.getMillis();
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return BigDecimal.class;
            case 1:
                return String.class;
            case 2:
                return Integer.class;
            case 3:
                return Integer.class;
            case 4:
                return Integer.class;
            case 5:
                return Integer.class;
            case 6:
                return Integer.class;
            default:
                return String.class;
        }
    }

    @Override
    void setColumnValue(SensorBean sensor, int column, Object value) {
        switch (column) {
            case 0:
                sensor.setId((BigDecimal) value);
                break;
            case 1:
                sensor.setName((String) value);
                break;
            case 2:
                sensor.setDeviceId((Integer) value);
                break;
            case 3:
                sensor.setContactId((Integer) value);
                break;
            case 4:
                sensor.setStatus((Integer) value);
                break;
            case 5:
                sensor.setPreviousStatus((Integer) value);
                break;
            case 6:
                sensor.setMillis((Integer) value);
                break;
            default:
                break;
        }

        TrackServiceFactory.getTrackService().persist(sensor);
    }

    public void clear() {
        if (this.devices != null) {
            this.devices.clear();
            this.sensorBeanCache.clear();
        }
        this.fireTableDataChanged();

    }

}
