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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jcs.entities.LocomotiveBean;
import jcs.trackservice.TrackServiceFactory;

/**
 *
 * @author frans
 */
public class LocomotiveTableModel extends EntityTableModel<LocomotiveBean> {

    public LocomotiveTableModel() {
        super();
    }

    @Override
    protected List<LocomotiveBean> getEntityBeans() {
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
            cols.add("Name");
            cols.add("Address");
            cols.add("Decoder");
            cols.add("Icon");
            cols.add("Show");
            return cols;
        }
        return this.columns;
    }

    @Override
    Object getColumnValue(LocomotiveBean device, int column) {
        switch (column) {
            case 0:
                return device.getName();
            case 1:
                return device.getAddress();
            case 2:
                return device.getDecoderTypeString();
            case 3:
                return device.getIcon();
            case 4:
                return device.isShow();
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> String.class;
            case 1 -> Integer.class;
            case 2 -> String.class;
            case 3 -> String.class;
            case 4 -> Boolean.class;
            default -> String.class;
        };
    }

    @Override
    void setColumnValue(LocomotiveBean device, int column, Object value) {
        switch (column) {
            case 0:
                device.setName((String) value);
                break;
            case 1:
                device.setAddress((Integer) value);
                break;
            case 2:
                device.setDecoderTypeString((String) value);
                break;
            case 3:
                device.setIcon((String) value);
                break;
            case 4:
                device.setShow((Boolean) value);
                break;
            default:
                break;
        }

        TrackServiceFactory.getTrackService().persist(device);
    }

}
