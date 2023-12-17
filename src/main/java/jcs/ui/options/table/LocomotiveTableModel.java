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
import java.util.Collections;
import java.util.List;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;

/**
 *
 * @author frans
 */
public class LocomotiveTableModel extends BeanTableModel<LocomotiveBean> {

    public LocomotiveTableModel() {
        super();
    }

    @Override
    protected List<LocomotiveBean> getBeans() {
        if (PersistenceFactory.getService() != null) {
            if (this.beans != null) {
                this.beans.clear();
                this.beans.addAll(PersistenceFactory.getService().getLocomotives());
            } else {
                this.beans = PersistenceFactory.getService().getLocomotives();
            }
            this.fireTableRowsInserted(beans.size(), beans.size());
            return beans;
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
            cols.add("Lenght");
            cols.add("Commuter");
            cols.add("Show");
            return cols;
        }
        return this.columns;
    }

    @Override
    Object getColumnValue(LocomotiveBean locomotive, int column) {
        return switch (column) {
            case 0 ->
                locomotive.getName();
            case 1 ->
                locomotive.getAddress();
            case 2 ->
                locomotive.getDecoderTypeString();
            case 3 ->
                locomotive.isSynchronize();
            case 4 ->
                locomotive.isCommuter();
            case 5 ->
                locomotive.isShow();
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
                Integer.class;
            case 2 ->
                String.class;
            case 3 ->
                Boolean.class;
            case 4 ->
                Boolean.class;
            case 5 ->
                Boolean.class;
            default ->
                String.class;
        };
    }

    @Override
    void setColumnValue(LocomotiveBean locomotive, int column, Object value) {
        switch (column) {
            case 0 ->
                locomotive.setName((String) value);
            case 1 ->
                locomotive.setAddress((Integer) value);
            case 2 ->
                locomotive.setDecoderTypeString((String) value);
            case 3 ->
                locomotive.setSynchronize((Boolean) value);
            case 4 ->
                locomotive.setCommuter((Boolean) value);
            case 5 ->
                locomotive.setShow((Boolean) value);
            default -> {
            }
        }
        PersistenceFactory.getService().persist(locomotive);
    }

    @Override
    protected int findRowIndex(LocomotiveBean bean) {
        int row = -1;

        if (bean != null && bean.getId() != null) {
            Long id = bean.getId();
            int rowCount = this.beans.size();

            for (int i = 0; i < rowCount; i++) {
                LocomotiveBean d = beans.get(i);
                if (id.equals(d.getId())) {
                    row = i;
                    break;
                }
            }
        }
        return row;
    }
}
