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
import java.beans.PropertyChangeListener;
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
 */
public interface PersistenceService {

  /**
   * Adds a PropertyChangeListener to the service. Used for observing changes to persisted data.
   *
   * @param listener The PropertyChangeListener to add.
   */
  void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Removes a PropertyChangeListener from the service.
   *
   * @param listener The PropertyChangeListener to remove.
   */
  void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * Retrieves all JCSPropertyBeans.
   *
   * @return A List of JCSPropertyBeans.
   */
  List<JCSPropertyBean> getProperties();

  /**
   * Retrieves a JCSPropertyBean by its key.
   *
   * @param key The key of the JCSPropertyBean to retrieve.
   * @return The JCSPropertyBean, or null if not found.
   */
  JCSPropertyBean getProperty(String key);

  /**
   * Persists a JCSPropertyBean.
   *
   * @param propertyBean The JCSPropertyBean to persist.
   * @return The persisted JCSPropertyBean.
   */
  JCSPropertyBean persist(JCSPropertyBean propertyBean);

  /**
   * Removes a JCSPropertyBean.
   *
   * @param property The JCSPropertyBean to remove.
   */
  void remove(JCSPropertyBean property);

  // Sensors
  /**
   * Retrieves all SensorBeans.
   *
   * @return A List of SensorBeans.
   */
  List<SensorBean> getSensors();

  /**
   * Retrieves all assigned SensorBeans.
   *
   * @return A List of assigned SensorBeans.
   */
  List<SensorBean> getAssignedSensors();

  /**
   * Retrieves a SensorBean by its ID.
   *
   * @param id The ID of the SensorBean to retrieve.
   * @return The SensorBean, or null if not found.
   */
  SensorBean getSensor(Integer id);

  /**
   * Retrieves a SensorBean by device and contact ID.
   *
   * @param deviceId The device ID.
   * @param contactId The contact ID.
   * @return The SensorBean, or null if not found.
   */
  SensorBean getSensor(Integer deviceId, Integer contactId);

  /**
   * Persists a SensorBean.
   *
   * @param sensor The SensorBean to persist.
   * @return The persisted SensorBean.
   */
  SensorBean persist(SensorBean sensor);

  /**
   * Persists a list of SensorBeans
   *
   * @param sensors a list of SensorBeans top persist
   * @return the persisted list
   */
  List<SensorBean> persistSensorBeans(List<SensorBean> sensors);

  /**
   * Removes a SensorBean.
   *
   * @param sensor The SensorBean to remove.
   */
  void remove(SensorBean sensor);

  /**
   * Removes all SensorBean from de persistent store.<br>
   *
   * Also references to BlockBeans and TileBeans are removed
   */
  void removeAllSensors();

  // Locomotive
  /**
   * Retrieves all LocomotiveBeans.
   *
   * @return A List of all LocomotiveBeans.
   */
  List<LocomotiveBean> getAllLocomotives();

  /**
   * Retrieves all LocomotiveBeans (filter not specified).
   *
   * @return A List of LocomotiveBeans. Implied filter behavior unclear.
   */
  List<LocomotiveBean> getLocomotives();

  /**
   * Retrieves LocomotiveBeans based on a show flag.
   *
   * @param show The show flag.
   * @return A List of LocomotiveBeans.
   */
  List<LocomotiveBean> getLocomotives(boolean show);

  /**
   * Retrieves LocomotiveBeans by command station ID.
   *
   * @param commandStationId The command station ID.
   * @return A List of LocomotiveBeans.
   */
  List<LocomotiveBean> getLocomotivesByCommandStationId(String commandStationId);

  /**
   * Retrieves LocomotiveBeans by command station ID and show flag.
   *
   * @param commandStationId The command station ID.
   * @param show The show flag.
   * @return A List of LocomotiveBeans.
   */
  List<LocomotiveBean> getLocomotivesByCommandStationId(String commandStationId, Boolean show);

  /**
   * Retrieves a LocomotiveBean by address, decoder type, and command station ID.
   *
   * @param address The address.
   * @param decoderType The decoder type.
   * @param commandStionId The command station ID.
   * @return The LocomotiveBean, or null if not found.
   */
  LocomotiveBean getLocomotive(Integer address, DecoderType decoderType, String commandStionId);

  /**
   * Retrieves a LocomotiveBean by UID and command station ID.
   *
   * @param locUid The locomotive UID.
   * @param commandStionId The command station ID.
   * @return The LocomotiveBean, or null if not found.
   */
  LocomotiveBean getLocomotive(Integer locUid, String commandStionId);

  /**
   * Retrieves a LocomotiveBean by ID.
   *
   * @param id The ID of the LocomotiveBean to retrieve.
   * @return The LocomotiveBean, or null if not found.
   */
  LocomotiveBean getLocomotive(Long id);

  /**
   * Persists a LocomotiveBean.
   *
   * @param locomotive The LocomotiveBean to persist.
   * @return The persisted LocomotiveBean.
   */
  LocomotiveBean persist(LocomotiveBean locomotive);

  /**
   * Retrieves a list of FunctionBeans associated with a locomotive.
   *
   * @param locomotiveId The ID of the locomotive.
   * @return A List of FunctionBeans.
   */
  List<FunctionBean> getLocomotiveFunctions(Long locomotiveId);

  /**
   * Retrieves a FunctionBean associated with a locomotive and function number.
   *
   * @param locomotiveId The ID of the locomotive.
   * @param number The function number.
   * @return The FunctionBean, or null if not found.
   */
  FunctionBean getLocomotiveFunction(Long locomotiveId, Integer number);

  /**
   * Persists a FunctionBean.
   *
   * @param functionBean The FunctionBean to persist.
   * @return The persisted FunctionBean.
   */
  FunctionBean persist(FunctionBean functionBean);

  /**
   * Removes a LocomotiveBean.
   *
   * @param locomotiveBean The LocomotiveBean to remove.
   */
  void remove(LocomotiveBean locomotiveBean);

  // Accessories
  /**
   * Retrieves all AccessoryBeans.
   *
   * @return A List of AccessoryBeans.
   */
  List<AccessoryBean> getAccessories();

  /**
   * Retrieves AccessoryBeans by command station ID.
   *
   * @param commandStationId The command station ID.
   * @return A List of AccessoryBeans.
   */
  List<AccessoryBean> getAccessoriesByCommandStationId(String commandStationId);

  /**
   * Checks if an accessory is locked.
   *
   * @param accessoryId The ID of the accessory.
   * @return True if the accessory is locked, false otherwise.
   */
  boolean isAccessoryLocked(String accessoryId);

  /**
   * Retrieves all turnout AccessoryBeans.
   *
   * @return A List of turnout AccessoryBeans.
   */
  List<AccessoryBean> getTurnouts();

  /**
   * Retrieves all signal AccessoryBeans.
   *
   * @return A List of signal AccessoryBeans.
   */
  List<AccessoryBean> getSignals();

  /**
   * Retrieves an AccessoryBean by address and command station ID.
   *
   * @param address The address.
   * @param commandStationId The command station ID.
   * @return The AccessoryBean, or null if not found.
   */
  AccessoryBean getAccessoryByAddressAndCommandStationId(Integer address, String commandStationId);

  /**
   * Retrieves an AccessoryBean by ID.
   *
   * @param id The ID of the AccessoryBean to retrieve.
   * @return The AccessoryBean, or null if not found.
   */
  AccessoryBean getAccessory(String id);

  /**
   * Retrieves an AccessoryBean by address.
   *
   * @param address The address.
   * @return The AccessoryBean, or null if not found.
   */
  AccessoryBean getAccessoryByAddress(Integer address);

  /**
   * Persists an AccessoryBean.
   *
   * @param accessoryBean The AccessoryBean to persist.
   * @return The persisted AccessoryBean.
   */
  AccessoryBean persist(AccessoryBean accessoryBean);

  /**
   * Removes an AccessoryBean.
   *
   * @param accessoryBean The AccessoryBean to remove.
   */
  void remove(AccessoryBean accessoryBean);

  // Tile
  /**
   * Retrieves all TileBeans.
   *
   * @return A List of TileBeans.
   */
  List<TileBean> getTileBeans();

  /**
   * Retrieves TileBeans by tile type.
   *
   * @param tileType The tile type.
   * @return A List of TileBeans.
   */
  List<TileBean> getTileBeansByTileType(TileBean.TileType tileType);

  /**
   * Retrieves a TileBean by ID.
   *
   * @param id The ID of the TileBean to retrieve.
   * @return The TileBean, or null if not found.
   */
  TileBean getTileBean(String id);

  /**
   * Retrieves a TileBean by x and y coordinates.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @return The TileBean, or null if not found.
   */
  TileBean getTileBean(Integer x, Integer y);

  /**
   * Persists a TileBean.
   *
   * @param tileBean The TileBean to persist.
   * @return The persisted TileBean.
   */
  TileBean persist(TileBean tileBean);

  /**
   * Persists a list of TileBeans.
   *
   * @param tiles The list of TileBeans to persist.
   */
  List<TileBean> persist(List<TileBean> tiles);

  /**
   * Removes a TileBean.
   *
   * @param tile The TileBean to remove.
   */
  void remove(TileBean tile);

  /**
   * Retrieves all RouteBeans.
   *
   * @return A List of RouteBeans.
   */
  List<RouteBean> getRoutes();

  /**
   * Retrieves a RouteBean by ID.
   *
   * @param id The ID of the RouteBean to retrieve.
   * @return The RouteBean, or null if not found.
   */
  RouteBean getRoute(String id);

  /**
   * Retrieves RouteBeans by fromTileId and fromSuffix.
   *
   * @param fromTileId The fromTileId.
   * @param fromSuffix The fromSuffix.
   * @return A List of RouteBeans.
   */
  List<RouteBean> getRoutes(String fromTileId, String fromSuffix);

  /**
   * Retrieves a RouteBean by fromTileId, fromSuffix, toTileId, and toSuffix.
   *
   * @param fromTileId The fromTileId.
   * @param fromSuffix The fromSuffix.
   * @param toTileId The toTileId.
   * @param toSuffix The toSuffix.
   * @return The RouteBean, or null if not found.
   */
  RouteBean getRoute(String fromTileId, String fromSuffix, String toTileId, String toSuffix);

  /**
   * Persists a RouteBean.
   *
   * @param routeBean The RouteBean to persist.
   * @return The persisted RouteBean.
   */
  RouteBean persist(RouteBean routeBean);

  /**
   * Removes a RouteBean.
   *
   * @param routeBean The RouteBean to remove.
   */
  void remove(RouteBean routeBean);

  /**
   * Retrieves a BlockBean by locomotive ID.
   *
   * @param locomotiveId The locomotive ID.
   * @return The BlockBean, or null if not found.
   */
  BlockBean getBlockByLocomotiveId(Long locomotiveId);

  /**
   * Retrieves all BlockBeans.
   *
   * @return A List of BlockBeans.
   */
  List<BlockBean> getBlocks();

  /**
   * Retrieves a BlockBean by ID.
   *
   * @param id The ID of the BlockBean to retrieve.
   * @return The BlockBean, or null if not found.
   */
  BlockBean getBlock(String id);

  /**
   * Retrieves a BlockBean by tile ID.
   *
   * @param tileId The tile ID.
   * @return The BlockBean, or null if not found.
   */
  BlockBean getBlockByTileId(String tileId);

  /**
   * Persists a BlockBean.
   *
   * @param block The BlockBean to persist.
   * @return The persisted BlockBean.
   */
  BlockBean persist(BlockBean block);

  /**
   * Removes a BlockBean.
   *
   * @param block The BlockBean to remove.
   */
  void remove(BlockBean block);

  /**
   * Removes all BlockBeans.
   */
  void removeAllBlocks();

  /**
   * Retrieves all CommandStationBeans.
   *
   * @return A List of CommandStationBeans.
   */
  List<CommandStationBean> getCommandStations();

  /**
   * Retrieves a CommandStationBean by ID.
   *
   * @param id The ID of the CommandStationBean to retrieve.
   * @return The CommandStationBean, or null if not found.
   */
  CommandStationBean getCommandStation(String id);

  /**
   * Retrieves the default CommandStationBean.
   *
   * @return The default CommandStationBean.
   */
  CommandStationBean getDefaultCommandStation();

  /**
   *
   * @return the enabled feedback provider
   */
  CommandStationBean getEnabledFeedbackProvider();

  /**
   * Persists a CommandStationBean.
   *
   * @param commandStationBean The CommandStationBean to persist.
   * @return The persisted CommandStationBean.
   */
  CommandStationBean persist(CommandStationBean commandStationBean);

  /**
   * Changes the default CommandStationBean.
   *
   * @param newDefaultCommandStationBean The new default CommandStationBean.
   * @return The new default CommandStationBean.
   */
  CommandStationBean changeDefaultCommandStation(CommandStationBean newDefaultCommandStationBean);

  /**
   * Retrieves a locomotive image.
   *
   * @param imageName The name of the image file.
   * @return The Image, or null if not found.
   */
  Image getLocomotiveImage(String imageName);

  /**
   * Retrieves a function image.
   *
   * @param imageName The name of the image file.
   * @return The Image, or null if not found.
   */
  Image getFunctionImage(String imageName);

  /**
   * Reads an image file.
   *
   * @param imageName The name of the image file.
   * @param function True if the image is a function image, false otherwise.
   * @return The Image, or null if not found.
   */
  Image readImage(String imageName, boolean function);
}
