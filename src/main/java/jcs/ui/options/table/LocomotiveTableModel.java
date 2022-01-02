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
import jcs.entities.Locomotive;
import jcs.entities.enums.DecoderType;
import jcs.trackservice.TrackServiceFactory;

/**
 *
 * @author frans
 */
public class LocomotiveTableModel extends DeviceTableModel<Locomotive> {

    public LocomotiveTableModel() {
        super();
    }

    @Override
    protected List<Locomotive> getDevices() {
        if (TrackServiceFactory.getTrackService() != null) {
            return TrackServiceFactory.getTrackService().getLocomotives();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<String> getColumns() {
        if (this.columns == null) {
            List<String> cols = new ArrayList<>(5);
            cols.add("Id");
            cols.add("Address");
            cols.add("Decoder");
            cols.add("Name");
            cols.add("Description");
            return cols;
        }
        return this.columns;
    }

    @Override
    Object getColumnValue(Locomotive device, int column) {
        switch (column) {
            case 0:
                return device.getId();
            case 1:
                return device.getAddress();
            case 2:
                return device.getDecoderType().getDecoderType();
            case 3:
                return device.getName();
            case 4:
                return device.getDescription();
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
                return Integer.class;
            case 2:
                return String.class;
            case 3:
                return String.class;
            case 4:
                return String.class;
            default:
                return String.class;
        }
    }

    @Override
    void setColumnValue(Locomotive device, int column, Object value) {
        switch (column) {
            case 0:
                device.setId((BigDecimal) value);
                break;
            case 1:
                device.setAddress((Integer) value);
                break;
            case 2:
                device.setDecoderType(DecoderType.get((String) value));
                break;
            case 3:
                device.setName((String) value);
                break;
            case 4:
                device.setDescription((String) value);
                break;
            default:
                break;
        }

        TrackServiceFactory.getTrackService().persist(device);
    }

}
