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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jcs.entities.AccessoryBean;
import jcs.persistence.PersistenceFactory;

/**
 *
 * @author frans
 */
public class TurnoutTableModel extends BeanTableModel<AccessoryBean> {

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
    protected List<AccessoryBean> getBeans() {
        if (PersistenceFactory.getService() != null) {
            return PersistenceFactory.getService().getTurnouts();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public Object getColumnValue(AccessoryBean device, int column) {
        return switch (column) {
            case 0 ->
                device.getId();
            case 1 ->
                device.getName();
            case 2 ->
                device.getType();
            case 3 ->
                device.getPosition();
            default ->
                null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 ->
                BigDecimal.class;
            case 1 ->
                String.class;
            case 2 ->
                String.class;
            case 3 ->
                Integer.class;
            default ->
                null;
        };
    }

    @Override
    void setColumnValue(AccessoryBean device, int column, Object value) {
        switch (column) {
            case 0 ->
                device.setId((Long) value);
            case 1 ->
                device.setName((String) value);
            case 2 ->
                device.setType((String) value);
            case 3 ->
                device.setPosition((Integer) value);
            default -> {
            }
        }
    }

    @Override
    protected int findRowIndex(AccessoryBean bean) {
        int row = -1;

        if (bean != null && bean.getId() != null) {
            Long id = bean.getId();
            int rowCount = this.beans.size();

            for (int i = 0; i < rowCount; i++) {
                AccessoryBean d = beans.get(i);
                if (id.equals(d.getId())) {
                    row = i;
                    break;
                }
            }
        }
        return row;
    }

}
