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
package jcs.ui.options.table;

import java.util.ArrayList;
import java.util.List;
import jcs.entities.JCSPropertyBean;
import jcs.persistence.PersistenceFactory;

/**
 *
 * @author frans
 */
public class ControllerInfoTableModel extends BeanTableModel<JCSPropertyBean> {

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
    protected List<JCSPropertyBean> getBeans() {
        List<JCSPropertyBean> props = new ArrayList<>();
        if (PersistenceFactory.getService() != null) {
            //Build a list with Controller properties
            //StatusDataConfigParser cs3 = TrackServiceFactory.getTrackService().getControllerInfo();

//            props.add(new JCSPropertyBean("Controller", cs3.getCs3().getName()));
//            props.add(new JCSPropertyBean("Article", cs3.getGfp().getArticleNumber()));
//            props.add(new JCSPropertyBean("CS 3 UID", cs3.getCs3().getUid()));
//            props.add(new JCSPropertyBean("GFP UID", cs3.getGfp().getUid()));
//            props.add(new JCSPropertyBean("GFP Serial", cs3.getGfp().getSerial()));
//            props.add(new JCSPropertyBean("GFP Queryinterval", cs3.getGfp().getQueryInterval() + " S"));
//            props.add(new JCSPropertyBean("GFP Version", cs3.getGfp().getVersion()));
//            props.add(new JCSPropertyBean("LinkSxx UID", cs3.getLinkSxx().getUid()));
//            props.add(new JCSPropertyBean("LinkSxx ID", cs3.getLinkSxx().getIdentifier()));
//            props.add(new JCSPropertyBean("LinkSxx Serial", cs3.getLinkSxx().getSerialNumber()));
//            props.add(new JCSPropertyBean("LinkSxx Name", cs3.getLinkSxx().getName()));
//            props.add(new JCSPropertyBean("LinkSxx Version", cs3.getLinkSxx().getVersion()));
//            props.add(new JCSPropertyBean("LinkSxx Total Sensors", cs3.getLinkSxx().getTotalSensors() + ""));
//
//            props.add(new JCSPropertyBean("Track Current", cs3.getGfp().getTrackCurrent() + " A"));
//            props.add(new JCSPropertyBean("Prog Track Current", cs3.getGfp().getProgrammingTrackCurrent() + " A"));
//            props.add(new JCSPropertyBean("Track Voltage", cs3.getGfp().getTrackVoltage() + " V"));
//            props.add(new JCSPropertyBean("CS 3 Temperature", cs3.getGfp().getCS3Temperature() + " C"));
            return props;
        } else {
            return props;
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
        return false;
    }

    @Override
    protected int findRowIndex(JCSPropertyBean bean) {
        int row = -1;

        if (bean != null && bean.getKey() != null) {
            String key = bean.getKey();
            int rowCount = this.beans.size();

            for (int i = 0; i < rowCount; i++) {
                JCSPropertyBean d = beans.get(i);
                if (key.equals(d.getKey())) {
                    row = i;
                    break;
                }
            }
        }
        return row;
    }
}
