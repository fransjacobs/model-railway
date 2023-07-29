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
package jcs.persistence;

import java.awt.Image;
import java.util.List;
import jcs.entities.AccessoryBean;
import jcs.entities.JCSPropertyBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;
import jcs.entities.enums.DecoderType;

/**
 * The Persistence Service takes care of all persistence functionality which is needed within the JCS Application
 *
 * @author frans
 */
public interface PersistenceService {

  List<JCSPropertyBean> getProperties();

  JCSPropertyBean getProperty(String key);

  JCSPropertyBean persist(JCSPropertyBean propertyBean);

  void remove(JCSPropertyBean property);

  //Sensors
  List<SensorBean> getSensors();

  SensorBean getSensor(Long id);

  SensorBean getSensor(Integer deviceId, Integer contactId);

  SensorBean persist(SensorBean sensor);

  void remove(SensorBean sensor);

  //Locomotive 
  List<LocomotiveBean> getLocomotives();

  LocomotiveBean getLocomotive(Integer address, DecoderType decoderType);

  LocomotiveBean getLocomotive(Long id);

  LocomotiveBean persist(LocomotiveBean locomotive);

  void remove(LocomotiveBean locomotiveBean);

  Image getFunctionImage(String imageName);

  //Accessories
  List<AccessoryBean> getTurnouts();

  List<AccessoryBean> getSignals();

  AccessoryBean getAccessoryById(Long id);

  AccessoryBean getAccessory(Integer address);

  AccessoryBean persist(AccessoryBean accessoryBean);

  void remove(AccessoryBean accessoryBean);

  //Tile
  List<TileBean> getTiles();

  TileBean getTile(String id);

  TileBean getTile(Integer x, Integer y);

  TileBean persist(TileBean tileBean);

  void persist(List<TileBean> tiles);

  void remove(TileBean tile);

  List<RouteBean> getRoutes();

  RouteBean getRoute(String id);

  RouteBean getRoute(String fromTileId, String fromSuffix, String toTileId, String toSuffix);

  RouteBean persist(RouteBean routeBean);

  void remove(RouteBean routeBean);

}
