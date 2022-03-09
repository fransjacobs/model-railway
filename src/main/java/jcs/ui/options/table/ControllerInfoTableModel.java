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
import java.util.List;
import jcs.controller.cs3.can.parser.StatusDataConfigParser;
import jcs.entities.JCSProperty;
import jcs.trackservice.TrackServiceFactory;

/**
 *
 * @author frans
 */
public class ControllerInfoTableModel extends EntityTableModel<JCSProperty> {

    public ControllerInfoTableModel() {
        super();
    }

    @Override
    protected List<String> getColumns() {
        if (this.columns == null) {
            List<String> cols = new ArrayList<>(6);
            cols.add("Item");
            cols.add("Value");
            return cols;
        }
        return this.columns;
    }

    @Override
    protected List<JCSProperty> getEntityBeans() {
        List<JCSProperty> props = new ArrayList<>();
        if (TrackServiceFactory.getTrackService() != null) {
            //Build a list with Controller properties
            StatusDataConfigParser cs3 = TrackServiceFactory.getTrackService().getControllerInfo();

//            props.add(new JCSProperty("Controller", cs3.getCs3().getName()));
//            props.add(new JCSProperty("Article", cs3.getGfp().getArticleNumber()));
//            props.add(new JCSProperty("CS 3 UID", cs3.getCs3().getUid()));
//            props.add(new JCSProperty("GFP UID", cs3.getGfp().getUid()));
//            props.add(new JCSProperty("GFP Serial", cs3.getGfp().getSerial()));
//            props.add(new JCSProperty("GFP Queryinterval", cs3.getGfp().getQueryInterval() + " S"));
//            props.add(new JCSProperty("GFP Version", cs3.getGfp().getVersion()));
//            props.add(new JCSProperty("LinkSxx UID", cs3.getLinkSxx().getUid()));
//            props.add(new JCSProperty("LinkSxx ID", cs3.getLinkSxx().getIdentifier()));
//            props.add(new JCSProperty("LinkSxx Serial", cs3.getLinkSxx().getSerialNumber()));
//            props.add(new JCSProperty("LinkSxx Name", cs3.getLinkSxx().getName()));
//            props.add(new JCSProperty("LinkSxx Version", cs3.getLinkSxx().getVersion()));
//            props.add(new JCSProperty("LinkSxx Total Sensors", cs3.getLinkSxx().getTotalSensors() + ""));
//
//            props.add(new JCSProperty("Track Current", cs3.getGfp().getTrackCurrent() + " A"));
//            props.add(new JCSProperty("Prog Track Current", cs3.getGfp().getProgrammingTrackCurrent() + " A"));
//            props.add(new JCSProperty("Track Voltage", cs3.getGfp().getTrackVoltage() + " V"));
//            props.add(new JCSProperty("CS 3 Temperature", cs3.getGfp().getCS3Temperature() + " C"));

            return props;
        } else {
            return props;
        }
    }

    @Override
    Object getColumnValue(JCSProperty device, int column) {
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
    void setColumnValue(JCSProperty device, int column, Object value) {
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
        return false;
    }

}
