/*
 * Copyright 2025 Frans Jacobs.
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

import java.util.List;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 */
public class RouteBeanTableModel extends AbstractBeanTableModel<RouteBean> {

  private static final String[] DISPLAY_COLUMNS = new String[]{"id", "from_tile_id", "from_suffix", "to_tile_id", "to_suffix", "locked", "routeState"};
  private static final long serialVersionUID = 4950988987258692944L;

  public RouteBeanTableModel() {
    super(RouteBean.class,DISPLAY_COLUMNS);
  }

  @Override
  public void refresh() {
    if (PersistenceFactory.getService() != null) {
      List<RouteBean> routes = PersistenceFactory.getService().getRoutes();
      Logger.trace("There are " + routes.size() + " Routes");
      setBeans(routes);
    }
  }

}
