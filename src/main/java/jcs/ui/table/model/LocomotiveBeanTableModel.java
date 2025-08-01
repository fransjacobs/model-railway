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
package jcs.ui.table.model;

import java.util.ArrayList;
import java.util.List;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 */
public class LocomotiveBeanTableModel extends AbstractBeanTableModel<LocomotiveBean> {

  private static final String[] DISPLAY_COLUMNS = new String[]{"image", "name", "address"};
  private static final long serialVersionUID = 5525083196000156106L;

  public LocomotiveBeanTableModel() {
    super(LocomotiveBean.class, DISPLAY_COLUMNS);
  }

  @Override
  public void refresh() {
    if (PersistenceFactory.getService() != null) {
      List<LocomotiveBean> activeLocos = new ArrayList<>();
      List<LocomotiveBean> allLocos = PersistenceFactory.getService().getLocomotives();
      for (LocomotiveBean loco : allLocos) {
        if (loco.isShow()) {
          activeLocos.add(loco);
        }
      }

      Logger.trace("In total there are " + allLocos.size() + " Locomotives of which there are " + activeLocos.size() + " shown");

      this.setBeans(activeLocos);
    }
  }

}
