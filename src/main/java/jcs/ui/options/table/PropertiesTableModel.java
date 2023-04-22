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
import jcs.entities.JCSPropertyBean;
import jcs.trackservice.TrackServiceFactory;

/**
 *
 * @author frans
 */
public class PropertiesTableModel extends EntityTableModel<JCSPropertyBean> {

    public PropertiesTableModel() {
        super();
    }

    @Override
    protected List<String> getColumns() {
        if (this.columns == null) {
            List<String> cols = new ArrayList<>(6);
            cols.add("Key");
            cols.add("Value");
            return cols;
        }
        return this.columns;
    }

    @Override
    protected List<JCSPropertyBean> getEntityBeans() {
        if (TrackServiceFactory.getTrackService() != null) {
            return TrackServiceFactory.getTrackService().getProperties();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    Object getColumnValue(JCSPropertyBean device, int column) {
        switch (column) {
            case 0:
                return device.getKey();
            case 1:
                return device.getValue();
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            default:
                return String.class;
        }
    }

    @Override
    void setColumnValue(JCSPropertyBean device, int column, Object value) {
        switch (column) {
            case 0:
                device.setKey((String) value);
                break;
            case 1:
                device.setValue((String) value);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

}
