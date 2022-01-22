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
import java.util.Collections;
import java.util.List;
import jcs.entities.AccessoryBean;
import jcs.trackservice.TrackServiceFactory;

/**
 *
 * @author frans
 */
public class TurnoutTableModel extends DeviceTableModel<AccessoryBean> {

    public TurnoutTableModel() {
        super();
    }

    @Override
    protected List<String> getColumns() {
        if (this.columns == null) {
            List<String> cols = new ArrayList<>(6);
            cols.add("ID");
            cols.add("Name");
            cols.add("Type");
            cols.add("Position");
            return cols;
        }
        return this.columns;
    }

    @Override
    protected List<AccessoryBean> getDevices() {
        if (TrackServiceFactory.getTrackService() != null) {
            return TrackServiceFactory.getTrackService().getTurnouts();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public Object getColumnValue(AccessoryBean device, int column) {
        switch (column) {
            case 0:
                return device.getId();
            case 1:
                return device.getName();
            case 2:
                return device.getType();
            case 3:
                return device.getPosition();
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
                return String.class;
            case 3:
                return Integer.class;
            default:
                return null;
        }
    }

    @Override
    void setColumnValue(AccessoryBean device, int column, Object value) {
        switch (column) {
            case 0:
                device.setId((BigDecimal) value);
                break;
            case 1:
                device.setName((String) value);
                break;
            case 2:
                device.setType((String) value);
                break;
            case 3:
                device.setPosition((Integer) value);
                break;
            default:
                break;
        }
    }

}
