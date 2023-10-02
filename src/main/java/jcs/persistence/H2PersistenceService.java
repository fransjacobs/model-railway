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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import jcs.entities.AccessoryBean;
import jcs.entities.BlockBean;
import jcs.entities.FunctionBean;
import jcs.entities.JCSPropertyBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;
import jcs.entities.enums.DecoderType;
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
    Logger.debug(
        "Connecting to: "
            + System.getProperty("norm.jdbcUrl")
            + " with db user: "
            + System.getProperty("norm.user"));
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
    JCSPropertyBean prop =
        database.where("p_key=?", property.getKey()).first(JCSPropertyBean.class);
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
  public SensorBean getSensor(Integer deviceId, Integer contactId) {
    Object[] args = new Object[] {deviceId, contactId};
    SensorBean sensor =
        database.where("device_id=? and contact_id=?", args).first(SensorBean.class);
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

    int rows = database.delete(sensor).getRowsAffected();
    Logger.trace(sensor + " rows + " + rows + " deleted");
  }

  @Override
  public List<FunctionBean> getLocomotiveFunctions(Long locomotiveId) {
    List<FunctionBean> locFunctions =
        database
            .where("locomotive_id=?", locomotiveId)
            .orderBy("f_number")
            .results(FunctionBean.class);

    for (FunctionBean fb : locFunctions) {
      fb.setInActiveIconImage(this.getFunctionImage(fb.getInActiveIcon()));
      fb.setActiveIconImage(this.getFunctionImage(fb.getActiveIcon()));
    }
    return locFunctions;
  }

  @Override
  public FunctionBean getLocomotiveFunction(Long locomotiveId, Integer number) {
    FunctionBean fb =
        database
            .where("locomotive_id=? and f_number=?", locomotiveId, number)
            .first(FunctionBean.class);
    if (fb != null) {
      fb.setInActiveIconImage(this.getFunctionImage(fb.getInActiveIcon()));
      fb.setActiveIconImage(this.getFunctionImage(fb.getActiveIcon()));
    }
    return fb;
  }

  @Override
  public LocomotiveBean getLocomotive(Integer address, DecoderType decoderType) {
    Object[] args = new Object[] {address, decoderType.getDecoderType()};

    LocomotiveBean loco =
        database.where("address=? and decoder_type=?", args).first(LocomotiveBean.class);
    loco.setLocIcon(getLocomotiveImage(loco.getIcon()));
    loco.addAllFunctions(getLocomotiveFunctions(loco.getId()));
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
  public List<LocomotiveBean> getLocomotives() {
    List<LocomotiveBean> locos = database.orderBy("id").results(LocomotiveBean.class);

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
      FunctionBean dbFb =
          this.getLocomotiveFunction(functionBean.getLocomotiveId(), functionBean.getNumber());
      if (dbFb != null) {
        functionBean.setId(dbFb.getId());
      }
    }
    try {
      if (database.where("id=?", functionBean.getId()).first(FunctionBean.class) != null) {
        int rows = database.update(functionBean).getRowsAffected();
        // Logger.trace(rows + " rows updated; " + functionBean);
      } else {
        int rows = database.insert(functionBean).getRowsAffected();
        // Logger.trace(rows + " rows inserted; " + functionBean);
      }
    } catch (DbException dbe) {
      Logger.error("Error: " + dbe.getMessage());
      Logger.debug("SQL: " + dbe.getSql());
    }
    return functionBean;
  }

  private List<FunctionBean> persistFunctionBeans(List<FunctionBean> functionsBeans) {
    List<FunctionBean> functions = new LinkedList<>();
    for (FunctionBean fb : functionsBeans) {
      fb = persist(fb);
      functions.add(fb);
    }
    return functions;
  }

  @Override
  public LocomotiveBean persist(LocomotiveBean locomotive) {
    if (database.where("id=?", locomotive.getId()).first(LocomotiveBean.class) != null) {
      int rows = database.update(locomotive).getRowsAffected();
      // Logger.trace(rows + " rows updated; " + locomotive);
    } else {
      int rows = database.insert(locomotive).getRowsAffected();
      // Logger.trace(rows + " rows inserted; " + locomotive);
    }
    List<FunctionBean> functions = new LinkedList<>();
    functions.addAll(locomotive.getFunctions().values());

    // remove the current functions
    // database.sql("delete from locomotive_functions where locomotive_id =?",
    // locomotive.getId()).execute();
    persistFunctionBeans(functions);
    locomotive.setFunctions(functions);

    return locomotive;
  }

  @Override
  public void remove(LocomotiveBean locomotive) {
    // First femove the functions
    database
        .sql("delete from locomotive_functions where locomotive_id =?", locomotive.getId())
        .execute();

    int rows = database.delete(locomotive).getRowsAffected();
    Logger.trace(rows + " rows deleted");
  }

  public Image getLocomotiveImage(String imageName) {
    if (!imageCache.containsKey(imageName)) {
      // Try to load the image from the file cache
      Image image = readImage(imageName, false);
      if (image != null) {
        int size = 100;
        float aspect = (float) image.getHeight(null) / (float) image.getWidth(null);
        this.imageCache.put(
            imageName, image.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH));
      }
    }
    return this.imageCache.get(imageName);
  }

  public Image getFunctionImage(String imageName) {
    if (!functionImageCache.containsKey(imageName)) {
      // Try to load the image from the file cache
      Image image = readImage(imageName, true);
      if (image != null) {
        int size = 30;
        float aspect = (float) image.getHeight(null) / (float) image.getWidth(null);
        this.functionImageCache.put(
            imageName, image.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH));
      }
    }
    return this.functionImageCache.get(imageName);
  }

  private Image readImage(String imageName, boolean function) {
    Image image = null;

    String serial = System.getProperty("cs.serial", "");
    String path =
        System.getProperty("user.home")
            + File.separator
            + "jcs"
            + File.separator
            + "cache"
            + File.separator
            + serial
            + File.separator;

    if (function) {
      path = path + "functions" + File.separator;
    }

    File imgFile = new File(path + imageName.toLowerCase() + ".png");
    if (imgFile.exists()) {
      try {
        image = ImageIO.read(imgFile);
      } catch (IOException e) {
        Logger.trace("Image file " + imageName + ".png does not exists");
      }
    } else {
      // should we attempt to obatian it now and cache it when available, but also when it is not
      // avaliable put a marker so tha we are not trying over and over again...
      // who should be responsable for the cache.. the vendor controller this class or?
    }
    return image;
  }

  @Override
  public List<AccessoryBean> getTurnouts() {
    String typeClause = "%weiche";
    List<AccessoryBean> turnouts =
        database.where("type like ?", typeClause).results(AccessoryBean.class);
    return turnouts;
  }

  @Override
  public List<AccessoryBean> getSignals() {
    String typeClause = "%signal%";
    List<AccessoryBean> signals =
        database.where("type like ?", typeClause).results(AccessoryBean.class);
    return signals;
  }

  @Override
  public AccessoryBean getAccessory(String id) {
    AccessoryBean accessoryBean = database.where("id=?", id).first(AccessoryBean.class);

    return accessoryBean;
  }

  @Override
  public AccessoryBean getAccessoryByAddress(Integer address) {
    AccessoryBean accessoryBean = database.where("address=?", address).first(AccessoryBean.class);

    return accessoryBean;
  }

  @Override
  public AccessoryBean persist(AccessoryBean accessory) {
    if (database.where("id=?", accessory.getId()).first(AccessoryBean.class) != null) {
      database.update(accessory);
    } else {
      database.insert(accessory);
    }
    return accessory;
  }

  @Override
  public void remove(AccessoryBean accessory) {
    // First ensure the linked tile records are decoupled
    database
        .sql("update tiles set sensor_id = null where accessory_id =?", accessory.getId())
        .execute();
    int rows = database.delete(accessory).getRowsAffected();
    Logger.trace(rows + " Accessories deleted");
  }

  private TileBean addReleatedObjects(TileBean tileBean) {
    if (tileBean != null) {
      if (tileBean.getAccessoryId() != null) {
        tileBean.setAccessoryBean(this.getAccessory(tileBean.getAccessoryId()));
      }

      if (tileBean.getSensorId() != null) {
        tileBean.setSensorBean(this.getSensor(tileBean.getSensorId()));
      }

      if (tileBean.getTileType() != null && TileBean.TileType.BLOCK == tileBean.getTileType()) {
        tileBean.setBlockBean(this.getBlockByTileId(tileBean.getId()));
      }
    }
    return tileBean;
  }

  private List<TileBean> addReleatedObjects(List<TileBean> tileBeans) {
    for (TileBean tileBean : tileBeans) {
      addReleatedObjects(tileBean);
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
    List<TileBean> tileBeans =
        database.where("tile_type = ?", tileType.getTileType()).results(TileBean.class);
    return addReleatedObjects(tileBeans);
  }

  @Override
  public TileBean getTileBean(String id) {
    TileBean tileBean = database.where("id=?", id).first(TileBean.class);
    return addReleatedObjects(tileBean);
  }

  @Override
  public TileBean getTileBean(Integer x, Integer y) {
    Object[] args = new Object[] {x, y};
    TileBean tileBean = database.where("x=? and y=?", args).first(TileBean.class);
    return addReleatedObjects(tileBean);
  }

  @Override
  public TileBean persist(TileBean tileBean) {
    TileBean tb;
    if (tileBean instanceof Tile) {
      tb = ((Tile) tileBean).getTileBean();
    } else {
      tb = tileBean;
    }

    if (tb != null && tb.getId() != null) {
      if (database.where("id=?", tb.getId()).first(TileBean.class) != null) {
        database.update(tb).getRowsAffected();
        Logger.trace("Updated " + tileBean);
      } else {
        database.insert(tb);
      }
    }
    return tileBean;
  }

  @Override
  public void remove(TileBean tileBean) {
    int rows = database.delete(tileBean).getRowsAffected();
    Logger.trace(rows + " TileBeans deleted");
  }

  @Override
  public void persist(List<TileBean> tiles) {
    removeAllBlocks();
    removeAllRoutes();

    database.sql("delete from tiles").execute();
    for (TileBean tb : tiles) {
      persist(tb);
    }
  }

  private List<RouteElementBean> getRouteElements(String routeId) {
    List<RouteElementBean> routeElements =
        database.where("route_id=?", routeId).orderBy("order_seq").results(RouteElementBean.class);
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
  public RouteBean getRoute(
      String fromTileId, String fromSuffix, String toTileId, String toSuffix) {
    Object[] args = new Object[] {fromTileId, fromSuffix, toTileId, toSuffix};
    RouteBean route =
        database
            .where(
                "from_tile_id = ? and from_suffix = ? and to_tile_id = ? and to_suffix = ?", args)
            .first(RouteBean.class);
    if (route != null) {
      List<RouteElementBean> routeElements = getRouteElements(route.getId());
      route.setRouteElements(routeElements);
    }
    return route;
  }

  @Override
  public RouteBean persist(RouteBean route) {
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
  public void remove(RouteBean route) {
    if (route.getRouteElements() != null && !route.getRouteElements().isEmpty()) {
      // remove all
      database.sql("delete from route_elements where route_id =?", route.getId()).execute();
    }

    int rows = this.database.delete(route).getRowsAffected();
    Logger.trace(rows + " rows deleted");
  }

  public void removeAllRoutes() {
    database.sql("delete from route_elements").execute();
    int rows = database.sql("delete from routes").execute().getRowsAffected();
    Logger.trace("Deleted " + rows + " routes");
  }

  private void setJCSPropertiesAsSystemProperties() {
    List<JCSPropertyBean> props = getProperties();
    props.forEach(
        p -> {
          System.setProperty(p.getKey(), p.getValue());
        });
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
        blockBean.setLocomotive(this.getLocomotive(blockBean.getLocomotiveId()));
      }

      if (blockBean.getPlusSensorBean() != null) {
        blockBean.setPlusSensorBean(this.getSensor(blockBean.getPlusSensorId()));
      }

      if (blockBean.getMinSensorBean() != null) {
        blockBean.setMinSensorBean(this.getSensor(blockBean.getMinSensorId()));
      }

      if (blockBean.getPlusSignalId() != null) {
        blockBean.setPlusSignal(this.getAccessory(blockBean.getPlusSignalId()));
      }

      if (blockBean.getMinSignalId() != null) {
        blockBean.setMinSignal(this.getAccessory(blockBean.getMinSignalId()));
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
  public BlockBean persist(BlockBean block) {
    if (block != null && block.getId() == null && block.getTileId() != null) {
      BlockBean bb = getBlockByTileId(block.getTileId());
      if (bb != null) {
        block.setId(bb.getId());
      }
    }

    if (block != null
        && block.getId() != null
        && database.where("id=?", block.getId()).first(BlockBean.class) != null) {
      database.update(block).getRowsAffected();
    } else {
      database.insert(block).getRowsAffected();
    }

    return block;
  }

  @Override
  public void remove(BlockBean block) {
    int rows = this.database.delete(block).getRowsAffected();
    Logger.trace(rows + " rows deleted");
  }

  @Override
  public void removeAllBlocks() {
    int rows = database.sql("delete from blocks").execute().getRowsAffected();
    Logger.trace("Deleted " + rows + " blocks");
  }
}
