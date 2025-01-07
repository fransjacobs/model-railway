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

import com.dieselpoint.norm.Database;
import com.dieselpoint.norm.DbException;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import jcs.entities.AccessoryBean;
import jcs.entities.BlockBean;
import jcs.entities.CommandStationBean;
import jcs.entities.FunctionBean;
import jcs.entities.JCSPropertyBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;
import jcs.persistence.sqlmakers.H2SqlMaker;
import jcs.ui.layout.tiles.Tile;
import org.tinylog.Logger;

public class H2PersistenceService implements PersistenceService {

  private Database database;

  private final HashMap<String, Image> imageCache;
  private final HashMap<String, Image> functionImageCache;

  public H2PersistenceService() {
    connect();
    imageCache = new HashMap<>();
    functionImageCache = new HashMap<>();
    setJCSPropertiesAsSystemProperties();
  }

  private void connect() {
    Logger.debug("Connecting to: " + System.getProperty("norm.jdbcUrl") + " with db user: " + System.getProperty("norm.user"));
    database = new Database();
    database.setSqlMaker(new H2SqlMaker());
  }

  @Override
  public List<JCSPropertyBean> getProperties() {
    List<JCSPropertyBean> props = database.results(JCSPropertyBean.class);
    return props;
  }

  @Override
  public JCSPropertyBean getProperty(String key) {
    JCSPropertyBean property = database.where("p_key=?", key).first(JCSPropertyBean.class);
    return property;
  }

  @Override
  public JCSPropertyBean persist(JCSPropertyBean property) {
    JCSPropertyBean prop
            = database.where("p_key=?", property.getKey()).first(JCSPropertyBean.class);
    if (prop != null) {
      int rows = database.update(property).getRowsAffected();
      Logger.trace(rows + " rows updated");
    } else {
      int rows = database.insert(property).getRowsAffected();
      Logger.trace(rows + " rows inserted");
    }
    return property;
  }

  @Override
  public void remove(JCSPropertyBean property) {
    int rows = database.delete(property).getRowsAffected();
    Logger.trace(rows + " rows deleted");
  }

  @Override
  public List<SensorBean> getSensors() {
    List<SensorBean> sensors = database.results(SensorBean.class);
    return sensors;
  }

  @Override
  public List<SensorBean> getAssignedSensors() {
    List<TileBean> sensorTiles = getTileBeansByTileType(TileBean.TileType.SENSOR);
    List<SensorBean> assignedSensors = new ArrayList<>(sensorTiles.size());

    for (TileBean s : sensorTiles) {
      if (s.getSensorBean() != null) {
        assignedSensors.add(s.getSensorBean());
      }
    }
    return assignedSensors;
  }

  @Override
  public SensorBean getSensor(Integer deviceId, Integer contactId) {
    Object[] args = new Object[]{deviceId, contactId};
    SensorBean sensor
            = database.where("device_id=? and contact_id=?", args).first(SensorBean.class);
    return sensor;
  }

  @Override
  public SensorBean getSensor(String id) {
    SensorBean sensor = database.where("id=?", id).first(SensorBean.class);
    return sensor;
  }

  @Override
  public SensorBean persist(SensorBean sensor) {
    SensorBean prev = database.where("id=?", sensor.getId()).first(SensorBean.class);

    if (prev != null) {
      // sensor.setName(prev.getName());
      database.update(sensor);
    } else {
      database.insert(sensor);
    }
    return sensor;
  }

  @Override
  public void remove(SensorBean sensor) {
    // First ensure the linked tile records are decoupled
    database.sql("update tiles set sensor_id = null where sensor_id = ?", sensor.getId()).execute();
    // Also update the blocks
    database.sql("update blocks set min_sensor_id = null where min_sensor_id = ?", sensor.getId()).execute();
    database.sql("update blocks set plus_sensor_id = null where plus_sensor_id = ?", sensor.getId()).execute();

    int rows = database.delete(sensor).getRowsAffected();
    Logger.trace(sensor + " rows + " + rows + " deleted");
  }

  @Override
  public List<SensorBean> generateSensorBeans(Integer deviceId, Integer bus0len, Integer bus1len, Integer bus2len, Integer bus3len) {
    Map<String, SensorBean> sensorBeans = new HashMap<>();

    if (bus0len != null) {
      for (int i = 0; i < (bus0len * 16); i++) {
        SensorBean sb = new SensorBean();
        sb.setDeviceId(deviceId);
        sb.setContactId((i + 1));
        sb.setName("B0-S-" + (i + 1));
        String id = sb.getId();
        sensorBeans.put(id, sb);
      }
    }

    if (bus1len != null) {
      for (int i = 0; i < (bus1len * 16); i++) {
        SensorBean sb = new SensorBean();
        sb.setDeviceId(deviceId);
        sb.setContactId((i + 1001));
        sb.setName("B1-S-" + (i + 1001));
        String id = sb.getId();
        sensorBeans.put(id, sb);
      }
    }

    if (bus2len != null) {
      for (int i = 0; i < (bus2len * 16); i++) {
        SensorBean sb = new SensorBean();
        sb.setDeviceId(deviceId);
        sb.setContactId((i + 2001));
        sb.setName("B2-S-" + (i + 2001));
        String id = sb.getId();
        sensorBeans.put(id, sb);
      }
    }

    if (bus3len != null) {
      for (int i = 0; i < (bus3len * 16); i++) {
        SensorBean sb = new SensorBean();
        sb.setContactId((i + 3001));
        sb.setName("B3-S-" + (i + 3001));
        String id = sb.getId();
        sensorBeans.put(id, sb);
      }
    }

    List<SensorBean> existing = getSensors();
    for (SensorBean sb : existing) {
      if (!sensorBeans.containsKey(sb.getId())) {
        Logger.trace("Removing " + sb);
        remove(sb);
      }
    }

    for (SensorBean sb : sensorBeans.values()) {
      persist(sb);
    }

    return getSensors();
  }

  @Override
  public List<FunctionBean> getLocomotiveFunctions(Long locomotiveId) {
    List<FunctionBean> locFunctions = database.where("locomotive_id=?", locomotiveId).orderBy("f_number").results(FunctionBean.class);

    for (FunctionBean fb : locFunctions) {
      fb.setInActiveIconImage(this.getFunctionImage(fb.getInActiveIcon()));
      fb.setActiveIconImage(this.getFunctionImage(fb.getActiveIcon()));
    }
    return locFunctions;
  }

  @Override
  public FunctionBean getLocomotiveFunction(Long locomotiveId, Integer number) {
    FunctionBean fb = database.where("locomotive_id=? and f_number=?", locomotiveId, number).first(FunctionBean.class);
    if (fb != null) {
      fb.setInActiveIconImage(getFunctionImage(fb.getInActiveIcon()));
      fb.setActiveIconImage(getFunctionImage(fb.getActiveIcon()));
    }
    return fb;
  }

  @Override
  public LocomotiveBean getLocomotive(Integer address, DecoderType decoderType, String commandStionId) {
    DecoderType dt;
    if (decoderType != null) {
      dt = decoderType;
    } else {
      dt = DecoderType.DCC;
    }

    Object[] args = new Object[]{address, dt.getDecoderType(), commandStionId};

    LocomotiveBean loco = database.where("address=? and decoder_type=? and command_station_id=?", args).first(LocomotiveBean.class);
    if (loco != null) {
      if (loco.getIcon() != null) {
        loco.setLocIcon(getLocomotiveImage(loco.getIcon()));
      }
      loco.addAllFunctions(getLocomotiveFunctions(loco.getId()));
    }
    return loco;
  }

  @Override
  public LocomotiveBean getLocomotive(Integer locUid, String commandStionId) {
    Object[] args = new Object[]{locUid, commandStionId};

    LocomotiveBean loco = database.where("uid=? and command_station_id=?", args).first(LocomotiveBean.class);
    if (loco != null) {
      if (loco.getIcon() != null) {
        loco.setLocIcon(getLocomotiveImage(loco.getIcon()));
      }
      loco.addAllFunctions(getLocomotiveFunctions(loco.getId()));
    }
    return loco;
  }

  @Override
  public LocomotiveBean getLocomotive(Long id) {
    LocomotiveBean loco = database.where("id=?", id).first(LocomotiveBean.class);
    if (loco != null) {
      loco.setLocIcon(getLocomotiveImage(loco.getIcon()));
      loco.addAllFunctions(getLocomotiveFunctions(loco.getId()));
    }
    return loco;
  }

  @Override
  public List<LocomotiveBean> getAllLocomotives() {
    List<LocomotiveBean> locos = database.orderBy("id").results(LocomotiveBean.class);

    for (LocomotiveBean loco : locos) {
      loco.setLocIcon(getLocomotiveImage(loco.getIcon()));
      loco.addAllFunctions(getLocomotiveFunctions(loco.getId()));
    }

    return locos;
  }

  @Override
  public List<LocomotiveBean> getLocomotives(boolean show) {
    String commandStationId = getDefaultCommandStation().getId();
    return getLocomotivesByCommandStationId(commandStationId, show);
  }

  @Override
  public List<LocomotiveBean> getLocomotives() {
    String commandStationId = getDefaultCommandStation().getId();
    return getLocomotivesByCommandStationId(commandStationId);
  }

  @Override
  public List<LocomotiveBean> getLocomotivesByCommandStationId(String commandStationId) {
    List<LocomotiveBean> locos = database.where("command_station_id=?", commandStationId).orderBy("id").results(LocomotiveBean.class);

    for (LocomotiveBean loco : locos) {
      loco.setLocIcon(getLocomotiveImage(loco.getIcon()));
      loco.addAllFunctions(getLocomotiveFunctions(loco.getId()));
    }

    return locos;
  }

  @Override
  public List<LocomotiveBean> getLocomotivesByCommandStationId(String commandStationId, Boolean show) {
    Object[] args = new Object[]{commandStationId, (show ? 1 : 0)};

    List<LocomotiveBean> locos = database.where("command_station_id=? and show=?", args).orderBy("id").results(LocomotiveBean.class);

    for (LocomotiveBean loco : locos) {
      loco.setLocIcon(getLocomotiveImage(loco.getIcon()));
      loco.addAllFunctions(getLocomotiveFunctions(loco.getId()));
    }

    return locos;
  }

  @Override
  public FunctionBean persist(FunctionBean functionBean) {
    if (functionBean.getId() == null) {
      // Might be a new refresh of an existing function so let try to find it
      FunctionBean dbFb = this.getLocomotiveFunction(functionBean.getLocomotiveId(), functionBean.getNumber());
      if (dbFb != null) {
        functionBean.setId(dbFb.getId());
      }
    }
    try {
      if (database.where("id=?", functionBean.getId()).first(FunctionBean.class) != null) {
        database.update(functionBean);
      } else {
        database.insert(functionBean);
      }
    } catch (DbException dbe) {
      Logger.error("Error: " + dbe.getMessage());
      Logger.debug("SQL: " + dbe.getSql());
    }
    return functionBean;
  }

  private List<FunctionBean> persistFunctionBeans(List<FunctionBean> functionsBeans, Long locoMotiveId) {
    List<FunctionBean> functions = new LinkedList<>();
    for (FunctionBean fb : functionsBeans) {
      fb.setLocomotiveId(locoMotiveId);
      fb = persist(fb);
      functions.add(fb);
    }
    return functions;
  }

  @Override
  public synchronized LocomotiveBean persist(LocomotiveBean locomotive) {
    try {
      if (database.where("id=?", locomotive.getId()).first(LocomotiveBean.class) != null) {
        database.update(locomotive);
      } else {
        database.sql("delete from locomotive_functions where locomotive_id =?", locomotive.getId()).execute();
        database.insert(locomotive);
      }
    } catch (Exception e) {
      Logger.error(e);
    }

    List<FunctionBean> functions = new LinkedList<>();
    functions.addAll(locomotive.getFunctions().values());

    persistFunctionBeans(functions, locomotive.getId());
    locomotive.setFunctions(functions);

    return locomotive;
  }

  @Override
  public synchronized void remove(LocomotiveBean locomotive) {
    // First femove the functions
    database.sql("delete from locomotive_functions where locomotive_id =?", locomotive.getId()).execute();

    int rows = database.delete(locomotive).getRowsAffected();
    Logger.trace(rows + " rows deleted");
  }

  @Override
  public Image getLocomotiveImage(String imageName) {
    if (!imageCache.containsKey(imageName)) {
      // Try to load the image from the file cache
      Image image = readImage(imageName, false);
      if (image != null) {
        int size = 100;
        float aspect = (float) image.getHeight(null) / (float) image.getWidth(null);
        this.imageCache.put(imageName, image.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH));
      }
    }
    return this.imageCache.get(imageName);
  }

  @Override
  public Image getFunctionImage(String imageName) {
    if (!functionImageCache.containsKey(imageName)) {
      // Try to load the image from the file cache
      Image image = readImage(imageName, true);
      if (image != null) {
        int size = 30;
        float aspect = (float) image.getHeight(null) / (float) image.getWidth(null);
        this.functionImageCache.put(imageName, image.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH));
      }
    }
    return this.functionImageCache.get(imageName);
  }

  @Override
  public Image readImage(String imageName, boolean function) {
    Image image = null;
    if (imageName != null) {
      String path;
      if (imageName.contains(File.separator)) {
        //Contains path seperators so assume it is a manual selected image
        path = imageName;
      } else {
        //no path seperators so assume it is a synchonized command station icon
        if (getDefaultCommandStation() != null) {
          String shortName = getDefaultCommandStation().getShortName().toLowerCase();
          path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + shortName + File.separator;
        } else {
          path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator;
        }
      }

      if (function) {
        path = path + "zfunctions" + File.separator;
      }

      File imgFile;
      if (path.contains(".")) {
        imgFile = new File(path);
      } else {
        imgFile = new File(path + imageName.toLowerCase() + ".png");
      }

      if (imgFile.exists()) {
        try {
          image = ImageIO.read(imgFile);
        } catch (IOException e) {
          Logger.trace("Image file " + imageName + ".png does not exists");
        }
      } else {
        //TODO:
        // should we attempt to obtain it now and cache it when available, but also when it is not
        // available put a marker so that we are not trying over and over again...
        // who should be responsable for the cache... the command controller, this class, or?
      }
    }
    return image;
  }

  @Override
  public List<AccessoryBean> getAccessories() {
    String commandStationId = getDefaultCommandStation().getId();
    return getAccessoriesByCommandStationId(commandStationId);
  }

  @Override
  public List<AccessoryBean> getAccessoriesByCommandStationId(String commandStationId) {
    List<AccessoryBean> accessories = database.where("command_station_id = ?", commandStationId).results(AccessoryBean.class);
    return accessories;
  }

  @Override
  public synchronized boolean isAccessoryLocked(String accessoryId) {
    String commandStationId = getDefaultCommandStation().getId();
    Object[] args = new Object[]{accessoryId, commandStationId};
    Long count = database.sql("select count(*) from accessories a join tiles t on a.id = t.accessory_id join route_elements re on t.id = re.tile_id join routes r on re.route_id = r.id where r.locked = true and a.id = ? and command_station_id = ?", args).first(Long.class);
    return count >= 1;
  }

  @Override
  public List<AccessoryBean> getTurnouts() {
    String typeClause = "%weiche";
    String csid = getDefaultCommandStation().getId();
    Object[] args = new Object[]{typeClause, csid};
    List<AccessoryBean> turnouts = database.where("type like ? and command_station_id = ?", args).results(AccessoryBean.class);
    return turnouts;
  }

  @Override
  public List<AccessoryBean> getSignals() {
    String typeClause = "%signal%";
    String csid = getDefaultCommandStation().getId();
    Object[] args = new Object[]{typeClause, csid};

    List<AccessoryBean> signals = database.where("type like ? and command_station_id = ?", args).results(AccessoryBean.class);
    return signals;
  }

  @Override
  public AccessoryBean getAccessory(String id) {
    AccessoryBean accessoryBean = database.where("id=?", id).first(AccessoryBean.class);
    return accessoryBean;
  }

  @Override
  public AccessoryBean getAccessoryByAddressAndCommandStationId(Integer address, String commandStationId) {
    String cid = commandStationId;
    if (cid == null) {
      cid = getDefaultCommandStation().getId();
    }

    Object[] args = new Object[]{address, cid};
    AccessoryBean accessoryBean = database.where("address=? and command_station_id=?", args).first(AccessoryBean.class);
    return accessoryBean;
  }

  @Override
  public AccessoryBean getAccessoryByAddress(Integer address) {
    return getAccessoryByAddressAndCommandStationId(address, null);
  }

  @Override
  public synchronized AccessoryBean persist(AccessoryBean accessory) {
    if (database.where("id=?", accessory.getId()).first(AccessoryBean.class) != null) {
      database.update(accessory);
    } else {
      database.insert(accessory);
    }
    return accessory;
  }

  @Override
  public synchronized void remove(AccessoryBean accessory) {
    // First ensure the linked tile records are decoupled
    database.sql("update tiles set sensor_id = null where accessory_id =?", accessory.getId()).execute();
    int rows = database.delete(accessory).getRowsAffected();
    Logger.trace(rows + " Accessories deleted");
  }

  private TileBean addReleatedObjects(TileBean tileBean, BlockBean blockBean) {
    if (tileBean != null) {
      if (tileBean.getAccessoryId() != null) {
        tileBean.setAccessoryBean(getAccessory(tileBean.getAccessoryId()));
      }

      if (tileBean.getSensorId() != null) {
        tileBean.setSensorBean(getSensor(tileBean.getSensorId()));
      }

      if (blockBean != null) {
        tileBean.setBlockBean(blockBean);
      } else {
        if (tileBean.getTileType() != null && TileBean.TileType.BLOCK == tileBean.getTileType()) {
          //Logger.trace("Look for blok " + tileBean.getId());
          tileBean.setBlockBean(getBlock(tileBean.getId()));
        }
      }
    }
    return tileBean;
  }

  private List<TileBean> addReleatedObjects(List<TileBean> tileBeans) {
    for (TileBean tileBean : tileBeans) {
      addReleatedObjects(tileBean, null);
    }
    return tileBeans;
  }

  @Override
  public List<TileBean> getTileBeans() {
    List<TileBean> tileBeans = database.results(TileBean.class);
    return addReleatedObjects(tileBeans);
  }

  @Override
  public List<TileBean> getTileBeansByTileType(TileBean.TileType tileType) {
    List<TileBean> tileBeans = database.where("tile_type = ?", tileType.getTileType()).results(TileBean.class);
    return addReleatedObjects(tileBeans);
  }

  @Override
  public TileBean getTileBean(String id) {
    TileBean tileBean = database.where("id=?", id).first(TileBean.class);
    return addReleatedObjects(tileBean, null);
  }

  private TileBean getTileBean(BlockBean blockBean) {
    TileBean tileBean = database.where("id=?", blockBean.getId()).first(TileBean.class);
    return addReleatedObjects(tileBean, blockBean);
  }

  @Override
  public TileBean getTileBean(Integer x, Integer y) {
    Object[] args = new Object[]{x, y};
    TileBean tileBean = database.where("x=? and y=?", args).first(TileBean.class);
    return addReleatedObjects(tileBean, null);
  }

  @Override
  public synchronized TileBean persist(TileBean tileBean) {
    if (tileBean == null) {
      return null;
    }
    TileBean tb;
    if (tileBean instanceof Tile tile) {
      tb = tile.getTileBean();
    } else {
      tb = tileBean;
    }

    if (tb != null && tb.getId() != null) {
      if (database.where("id=?", tb.getId()).first(TileBean.class) != null) {
        database.update(tb).getRowsAffected();
        //Logger.trace("Updated " + tileBean);
      } else {
        database.insert(tb);
      }
    }

    if (tileBean.getBlockBean() != null) {
      this.persist(tileBean.getBlockBean());
    }

    return tileBean;
  }

  @Override
  public synchronized void remove(TileBean tileBean) {
    if (tileBean.getBlockBean() != null) {
      BlockBean bb = tileBean.getBlockBean();
      this.remove(bb);
    }
    int rows = database.delete(tileBean).getRowsAffected();
    Logger.trace(rows + " TileBean(s) deleted");
  }

  @Override
  public synchronized void persist(List<TileBean> tiles) {
    List<TileBean> dbTiles = this.getTileBeans();
    Set<String> newTileIds = new HashSet<>();
    for (TileBean tb : tiles) {
      newTileIds.add(tb.getId());
    }

    //Build a list of tiles which are not in the toPersistTiles
    List<TileBean> tilesToRemove = new ArrayList<>();
    for (TileBean tb : dbTiles) {
      if (!newTileIds.contains(tb.getId())) {
        tilesToRemove.add(tb);
      }
    }
    //Tiles need to be removed so clear all routes
    if (!tilesToRemove.isEmpty()) {
      removeAllRoutes();
    }

    for (TileBean tb : tilesToRemove) {
      this.remove(tb);
    }

    for (TileBean tb : tiles) {
      persist(tb);
    }
  }

  private RouteElementBean addRelatedObjects(RouteElementBean routeElementBean) {
    if (routeElementBean.getTileId() != null) {
      routeElementBean.setTileBean(getTileBean(routeElementBean.getTileId()));
    }

    return routeElementBean;
  }

  private List<RouteElementBean> getRouteElements(String routeId) {
    List<RouteElementBean> routeElements = database.where("route_id=?", routeId).orderBy("order_seq").results(RouteElementBean.class);
    //is the tile needed inside the route element?
    for (RouteElementBean reb : routeElements) {
      addRelatedObjects(reb);
    }

    return routeElements;
  }

  private RouteElementBean persist(RouteElementBean routeElement) {
    if (database.where("id=?", routeElement.getId()).first(RouteElementBean.class) != null) {
      database.update(routeElement);
    } else {
      database.insert(routeElement);
    }
    return routeElement;
  }

  @Override
  public List<RouteBean> getRoutes() {
    List<RouteBean> routes = database.results(RouteBean.class);

    for (RouteBean r : routes) {
      List<RouteElementBean> routeElements = getRouteElements(r.getId());
      r.setRouteElements(routeElements);
    }
    return routes;
  }

  @Override
  public RouteBean getRoute(String id) {
    RouteBean route = database.where("id = ?", id).first(RouteBean.class);
    if (route != null) {
      List<RouteElementBean> routeElements = getRouteElements(route.getId());
      route.setRouteElements(routeElements);
    }
    return route;
  }

  @Override
  public RouteBean getRoute(String fromTileId, String fromSuffix, String toTileId, String toSuffix) {
    Object[] args = new Object[]{fromTileId, fromSuffix, toTileId, toSuffix};
    RouteBean route = database.where("from_tile_id = ? and from_suffix = ? and to_tile_id = ? and to_suffix = ?", args).first(RouteBean.class);

    if (route != null) {
      List<RouteElementBean> routeElements = getRouteElements(route.getId());
      route.setRouteElements(routeElements);
    }
    return route;
  }

  @Override
  public synchronized List<RouteBean> getRoutes(String fromTileId, String fromSuffix) {
    Object[] args = new Object[]{fromTileId, fromSuffix};
    List<RouteBean> routes = database.where("from_tile_id = ? and from_suffix = ? and locked = false", args).results(RouteBean.class);

    List<RouteBean> filtered = new ArrayList<>();

    for (RouteBean r : routes) {
      BlockBean dest = getBlockByTileId(r.getToTileId());
      if (dest != null) {
        if (BlockBean.BlockState.FREE == dest.getBlockState()) {
          List<RouteElementBean> routeElements = getRouteElements(r.getId());
          r.setRouteElements(routeElements);
          filtered.add(r);
        } else {
          Logger.trace("Skip " + r.getId() + " dest status: " + dest.getStatus());
        }
      }
    }
    return filtered;
  }

  @Override
  public synchronized RouteBean persist(RouteBean route) {
    if (database.where("id=?", route.getId()).first(RouteBean.class) != null) {
      database.update(route);
    } else {
      database.insert(route);
    }

    if (route.getRouteElements() != null && !route.getRouteElements().isEmpty()) {
      database.sql("delete from route_elements where route_id =?", route.getId()).execute();

      List<RouteElementBean> rbl = route.getRouteElements();
      List<RouteElementBean> rblr = new LinkedList<>();

      for (RouteElementBean rb : rbl) {
        rb.setRouteId(route.getId());
        // Reset the id
        rb.setId(null);
        rb = persist(rb);
        rblr.add(rb);
      }
      route.setRouteElements(rblr);
    }
    return route;
  }

  @Override
  public synchronized void remove(RouteBean route) {
    if (route.getRouteElements() != null && !route.getRouteElements().isEmpty()) {
      // remove all
      database.sql("delete from route_elements where route_id =?", route.getId()).execute();
    }

    int rows = this.database.delete(route).getRowsAffected();
    Logger.trace(rows + " rows deleted");
  }

  public synchronized void removeAllRoutes() {
    database.sql("delete from route_elements").execute();
    int rows = database.sql("delete from routes").execute().getRowsAffected();
    Logger.trace("Deleted " + rows + " routes");
  }

  private void setJCSPropertiesAsSystemProperties() {
    List<JCSPropertyBean> props = getProperties();
    props.forEach(p -> {
      System.setProperty(p.getKey(), p.getValue());
    });
  }

  @Override
  public BlockBean getBlockByLocomotiveId(Long locomotiveId) {
    Object[] args = new Object[]{locomotiveId};
    BlockBean block = database.where("locomotive_id = ?", args).first(BlockBean.class);
    return addReleatedObjects(block);
  }

  @Override
  public List<BlockBean> getBlocks() {
    List<BlockBean> blocks = database.results(BlockBean.class);
    return addBlockReleatedObjects(blocks);
  }

  private List<BlockBean> addBlockReleatedObjects(List<BlockBean> blockBeans) {
    for (BlockBean bb : blockBeans) {
      addReleatedObjects(bb);
    }
    return blockBeans;
  }

  private BlockBean addReleatedObjects(BlockBean blockBean) {
    if (blockBean != null) {
      if (blockBean.getLocomotiveId() != null) {
        blockBean.setLocomotive(getLocomotive(blockBean.getLocomotiveId()));
      }

      if (blockBean.getPlusSensorId() != null) {
        blockBean.setPlusSensorBean(getSensor(blockBean.getPlusSensorId()));
      }

      if (blockBean.getMinSensorId() != null) {
        blockBean.setMinSensorBean(getSensor(blockBean.getMinSensorId()));
      }

      if (blockBean.getPlusSignalId() != null) {
        blockBean.setPlusSignal(getAccessory(blockBean.getPlusSignalId()));
      }

      if (blockBean.getMinSignalId() != null) {
        blockBean.setMinSignal(getAccessory(blockBean.getMinSignalId()));
      }

      if (blockBean.getTileId() != null && blockBean.getTileBean() == null) {
        blockBean.setTile(this.getTileBean(blockBean));
      }
    }
    return blockBean;
  }

  /**
   * @param id
   * @return
   */
  @Override
  public BlockBean getBlock(String id) {
    BlockBean block = database.where("id = ?", id).first(BlockBean.class);
    return addReleatedObjects(block);
  }

  @Override
  public BlockBean getBlockByTileId(String tileId) {
    BlockBean block = database.where("tile_id = ?", tileId).first(BlockBean.class);

    return addReleatedObjects(block);
  }

  @Override
  public synchronized BlockBean persist(BlockBean block) {
    if (block != null && block.getId() == null && block.getTileId() != null) {
      BlockBean bb = getBlockByTileId(block.getTileId());
      if (bb != null) {
        block.setId(bb.getId());
      }
    }

    if (block != null && block.getId() != null && database.where("id=?", block.getId()).first(BlockBean.class) != null) {
      database.update(block);
    } else {
      database.insert(block);
    }

    return block;
  }

  @Override
  public synchronized void remove(BlockBean block) {
    int rows = this.database.delete(block).getRowsAffected();
    Logger.trace(rows + " rows deleted");
  }

  @Override
  public synchronized void removeAllBlocks() {
    int rows = database.sql("delete from blocks").execute().getRowsAffected();
    Logger.trace("Deleted " + rows + " blocks");
  }

  @Override
  public List<CommandStationBean> getCommandStations() {
    //List<CommandStationBean> commandStationBeans = database.where("enabled=true").results(CommandStationBean.class);
    List<CommandStationBean> commandStationBeans = database.results(CommandStationBean.class);
    return commandStationBeans;
  }

  @Override
  public CommandStationBean getCommandStation(String id) {
    return database.where("id=?", id).first(CommandStationBean.class);
  }

  @Override
  public CommandStationBean getDefaultCommandStation() {
    return database.where("default_cs=true").first(CommandStationBean.class);
  }

  @Override
  public synchronized CommandStationBean persist(CommandStationBean commandStationBean) {
    if (database.where("id=?", commandStationBean.getId()).first(CommandStationBean.class) != null) {
      database.update(commandStationBean);
    } else {
      Logger.warn("Can't Create CommandStation " + commandStationBean);
    }
    return commandStationBean;
  }

  @Override
  public synchronized CommandStationBean changeDefaultCommandStation(CommandStationBean newDefaultCommandStationBean) {
    Object[] args = new Object[]{newDefaultCommandStationBean.getId(), newDefaultCommandStationBean.getId()};
    database.sql("update command_stations set default_cs = case when id = ? then true else false end, enabled = case when id = ? then true else false end", args).execute();
    return newDefaultCommandStationBean;
  }

}
