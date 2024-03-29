/*
 * Copyright 2023 frans.
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
package jcs.ui.table.model;

import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;

/**
 *
 * @author frans
 */
public class LocomotiveBeanTableModel extends AbstractBeanTableModel<LocomotiveBean> {

  private static final String[] DISPLAY_COLUMNS = new String[]{"id", "decoder_type", "address", "name", "velocity", "richtung"};

  public LocomotiveBeanTableModel() {
    super(LocomotiveBean.class, DISPLAY_COLUMNS);
  }

  @Override
  public void refresh() {
    this.setBeans(PersistenceFactory.getService().getLocomotives());
    this.beans = PersistenceFactory.getService().getLocomotives();
  }

}
