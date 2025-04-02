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
package jcs.ui.settings.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jcs.entities.JCSPropertyBean;
import jcs.persistence.PersistenceFactory;

/**
 *
 * @author frans
 */
public class PropertiesTableModel extends BeanTableModel<JCSPropertyBean> {

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
    protected List<JCSPropertyBean> getBeans() {
        if (PersistenceFactory.getService() != null) {
            return PersistenceFactory.getService().getProperties();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    Object getColumnValue(JCSPropertyBean device, int column) {
        return switch (column) {
            case 0 ->
                device.getKey();
            case 1 ->
                device.getValue();
            default ->
                null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 ->
                String.class;
            case 1 ->
                String.class;
            default ->
                String.class;
        };
    }

    @Override
    void setColumnValue(JCSPropertyBean device, int column, Object value) {
        switch (column) {
            case 0 ->
                device.setKey((String) value);
            case 1 ->
                device.setValue((String) value);
            default -> {
            }
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
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
