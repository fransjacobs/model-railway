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
import jcs.entities.BlockBean;
import jcs.entities.CommandStationBean;
import jcs.entities.FunctionBean;
import jcs.entities.JCSPropertyBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;

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

  // Sensors
  List<SensorBean> getSensors();

  SensorBean getSensor(String id);

  SensorBean getSensor(Integer deviceId, Integer contactId);

  SensorBean persist(SensorBean sensor);

  void remove(SensorBean sensor);

  List<SensorBean> generateSensorBeans(Integer deviceId, Integer bus0len, Integer bus1len, Integer bus2len, Integer bus3len);

  // Locomotive
  List<LocomotiveBean> getAllLocomotives();

  List<LocomotiveBean> getLocomotives();

  List<LocomotiveBean> getLocomotivesByCommandStationId(String commandStationId);

  List<LocomotiveBean> getLocomotivesByCommandStationId(String commandStationId, Boolean show);

  LocomotiveBean getLocomotive(Integer address, DecoderType decoderType, String commandStionId);

  LocomotiveBean getLocomotive(Long id);

  LocomotiveBean persist(LocomotiveBean locomotive);

  List<FunctionBean> getLocomotiveFunctions(Long locomotiveId);

  FunctionBean getLocomotiveFunction(Long locomotiveId, Integer number);

  FunctionBean persist(FunctionBean functionBean);

  void remove(LocomotiveBean locomotiveBean);

  // Accessories
  List<AccessoryBean> getAccessoriesByCommandStationId(String commandStationId);

  boolean isAccessoryLocked(String accessoryId);

  List<AccessoryBean> getTurnouts();

  List<AccessoryBean> getSignals();

  AccessoryBean getAccessoryByAddressAndCommandStationId(Integer address, String commandStationId);

  AccessoryBean getAccessory(String id);

  AccessoryBean getAccessoryByAddress(Integer address);

  AccessoryBean persist(AccessoryBean accessoryBean);

  void remove(AccessoryBean accessoryBean);

  // Tile
  List<TileBean> getTileBeans();

  List<TileBean> getTileBeansByTileType(TileBean.TileType tileType);

  TileBean getTileBean(String id);

  TileBean getTileBean(Integer x, Integer y);

  TileBean persist(TileBean tileBean);

  void persist(List<TileBean> tiles);

  void remove(TileBean tile);

  List<RouteBean> getRoutes();

  RouteBean getRoute(String id);

  List<RouteBean> getRoutes(String fromTileId, String fromSuffix);

  RouteBean getRoute(String fromTileId, String fromSuffix, String toTileId, String toSuffix);

  RouteBean persist(RouteBean routeBean);

  void remove(RouteBean routeBean);

  BlockBean getBlockByLocomotiveId(Long locomotiveId);

  List<BlockBean> getBlocks();

  BlockBean getBlock(String id);

  BlockBean getBlockByTileId(String tileId);

  BlockBean persist(BlockBean block);

  void remove(BlockBean block);

  void removeAllBlocks();

  List<CommandStationBean> getCommandStations();

  CommandStationBean getCommandStation(String id);

  CommandStationBean getDefaultCommandStation();

  CommandStationBean persist(CommandStationBean commandStationBean);

  CommandStationBean changeDefaultCommandStation(CommandStationBean newDefaultCommandStationBean);

  Image getLocomotiveImage(String imageName);

  Image getFunctionImage(String imageName);

  Image readImage(String imageName, boolean function);

}
