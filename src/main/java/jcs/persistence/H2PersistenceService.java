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
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import jcs.entities.JCSPropertyBean;
import jcs.entities.SensorBean;
import jcs.entities.enums.DecoderType;
import jcs.entities.AccessoryBean;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean;
import org.tinylog.Logger;

import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.persistence.sqlmakers.H2SqlMaker;

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
    JCSPropertyBean prop = database.where("p_key=?", property.getKey()).first(JCSPropertyBean.class);
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
    Object[] args = new Object[]{deviceId, contactId};
    SensorBean sensor = database.where("device_id=? and contact_id=?", args).first(SensorBean.class);
    return sensor;
  }

  @Override
  public SensorBean getSensor(Long id) {
    SensorBean sensor = database.where("id=?", id).first(SensorBean.class);
    return sensor;
  }

  @Override
  public SensorBean persist(SensorBean sensor) {
    SensorBean prev = database.where("id=?", sensor.getId()).first(SensorBean.class);
    if (prev != null) {
      sensor.setId(prev.getId());
      sensor.setName(prev.getName());
      int rows = database.update(sensor).getRowsAffected();
      Logger.trace(rows + " rows updated");
    } else {
      int rows = database.insert(sensor).getRowsAffected();
      Logger.trace(rows + " rows inserted");
    }
    return sensor;
  }

  @Override
  public void remove(SensorBean sensor) {
    //First ensure the linked tile records are decoupled
    database.sql("update tiles set sensor_id = null where sensor_id =?", sensor.getId()).execute();

    int rows = database.delete(sensor).getRowsAffected();
    Logger.trace(rows + " rows deleted");
  }

  private LocomotiveBean getLocomotiveFunctionsAndImage(LocomotiveBean locomotive) {
    if (locomotive != null) {
      Long locomotiveId = locomotive.getId();
      List<FunctionBean> locFunctions = database.where("locomotive_id=?", locomotiveId).orderBy("f_number").results(FunctionBean.class);
      locomotive.replaceAllFunctions(locFunctions);
      locomotive.setLocIcon(getLocomotiveImage(locomotive.getIcon()));
    }
    return locomotive;
  }

  private List<LocomotiveBean> getLocomotiveFunctions(List<LocomotiveBean> locomotives) {
    List<LocomotiveBean> locs = new LinkedList<>();
    for (LocomotiveBean lb : locomotives) {
      lb = getLocomotiveFunctionsAndImage(lb);
      locs.add(lb);
    }
    return locs;
  }

  @Override
  public LocomotiveBean getLocomotive(Integer address, DecoderType decoderType) {
    Object[] args = new Object[]{address, decoderType.getDecoderType()};
    LocomotiveBean loco = getLocomotiveFunctionsAndImage(database.where("address=? and decoder_type=?", args).first(LocomotiveBean.class));
    return loco;
  }

  @Override
  public LocomotiveBean getLocomotive(Long id) {
    LocomotiveBean loco = getLocomotiveFunctionsAndImage(database.where("id=?", id).first(LocomotiveBean.class));
    return loco;
  }

  @Override
  public List<LocomotiveBean> getLocomotives() {
    List<LocomotiveBean> locos = H2PersistenceService.this.getLocomotiveFunctions(database.orderBy("id").results(LocomotiveBean.class));
    return locos;
  }

  private FunctionBean persist(FunctionBean functionBean) {
    if (database.where("id=?", functionBean.getId()).first(FunctionBean.class) != null) {
      int rows = database.update(functionBean).getRowsAffected();
      Logger.trace(rows + " rows updated");
    } else {
      int rows = database.insert(functionBean).getRowsAffected();
      Logger.trace(rows + " rows inserted");
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
      Logger.trace(rows + " rows updated");
    } else {
      int rows = database.insert(locomotive).getRowsAffected();
      Logger.trace(rows + " rows inserted");
    }
    List<FunctionBean> functions = new LinkedList<>();
    functions.addAll(locomotive.getFunctions().values());

    //remove the current functions
    database.sql("delete from locomotive_functions where locomotive_id =?", locomotive.getId()).execute();
    persistFunctionBeans(functions);
    locomotive.replaceAllFunctions(functions);

    return locomotive;
  }

  @Override
  public void remove(LocomotiveBean locomotive) {
    //First femove the functions
    database.sql("delete from locomotive_functions where locomotive_id =?", locomotive.getId()).execute();

    int rows = database.delete(locomotive).getRowsAffected();
    Logger.trace(rows + " rows deleted");
  }

  public Image getLocomotiveImage(String imageName) {
    if (!imageCache.containsKey(imageName)) {
      //Try to load the image from the file cache
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
      //Try to load the image from the file cache
      Image image = readImage(imageName, true);
      if (image != null) {
        int size = 30;
        float aspect = (float) image.getHeight(null) / (float) image.getWidth(null);
        this.functionImageCache.put(imageName, image.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH));
      }
    }
    return this.functionImageCache.get(imageName);
  }

  private Image readImage(String imageName, boolean function) {
    Image image = null;
    String path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator;

    if (function) {
      path = path + "functions" + File.separator;
    }

    File imgFile = new File(path + imageName + ".png");
    if (imgFile.exists()) {
      try {
        image = ImageIO.read(imgFile);

      } catch (IOException e) {
        Logger.trace("Image file " + imageName + ".png does not exists");
      }
    }
    return image;
  }

//  private Image readLocomotiveImage(String imageName) {
//    String path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator;
//    Image image = null;
//
//    File imgFile = new File(path + imageName + ".png");
//    if (imgFile.exists()) {
//      try {
//        image = ImageIO.read(imgFile);
//
//      } catch (IOException e) {
//        Logger.trace("Image file " + imageName + ".png does not exists");
//      }
//    }
//    return image;
//  }
//  private Image readFunctionImage(String imageName) {
//    String path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + "functions" + File.separator;
//    Image image = null;
//
//    File imgFile = new File(path + imageName + ".png");
//    if (imgFile.exists()) {
//      try {
//        image = ImageIO.read(imgFile);
//
//      } catch (IOException e) {
//        Logger.trace("Image file " + imageName + ".png does not exists");
//      }
//    }
//    return image;
//  }
  @Override
  public List<AccessoryBean> getTurnouts() {
    String typeClause = "%weiche";
    List<AccessoryBean> turnouts = database.where("type like ?", typeClause).results(AccessoryBean.class);
    return turnouts;
  }

  @Override
  public List<AccessoryBean> getSignals() {
    String typeClause = "%signal%";
    List<AccessoryBean> signals = database.where("type like ?", typeClause).results(AccessoryBean.class);
    return signals;
  }

  @Override
  public AccessoryBean getAccessoryById(Long id) {
    AccessoryBean accessoryBean = database.where("id=?", id).first(AccessoryBean.class);

    return accessoryBean;
  }

  @Override
  public AccessoryBean getAccessory(Integer address) {
    AccessoryBean accessoryBean = database.where("address=?", address).first(AccessoryBean.class);

    return accessoryBean;
  }

  @Override
  public AccessoryBean persist(AccessoryBean accessory) {
    if (database.where("id=?", accessory.getId()).first(AccessoryBean.class) != null) {
      int rows = database.update(accessory).getRowsAffected();
      Logger.trace(rows + " rows updated");
    } else {
      int rows = database.insert(accessory).getRowsAffected();
      Logger.trace(rows + " rows inserted");
    }
    return accessory;
  }

  @Override
  public void remove(AccessoryBean accessory) {
    //First ensure the linked tile records are decoupled
    database.sql("update tiles set sensor_id = null where accessory_id =?", accessory.getId()).execute();

    int rows = database.delete(accessory).getRowsAffected();
    Logger.trace(rows + " rows deleted");
  }

  @Override
  public List<TileBean> getTiles() {
    List<TileBean> tileBeans = database.results(TileBean.class);
    return tileBeans;
  }

  @Override
  public TileBean getTile(String id) {
    TileBean tileBean = database.where("id=?", id).first(TileBean.class);
    return tileBean;
  }

  @Override
  public TileBean getTile(Integer x, Integer y) {
    Object[] args = new Object[]{x, y};
    TileBean tileBean = database.where("x=? and y=?", args).first(TileBean.class);
    return tileBean;
  }

  @Override
  public TileBean persist(TileBean tileBean) {
    if (database.where("id=?", tileBean.getId()).first(TileBean.class) != null) {
      database.update(tileBean).getRowsAffected();
    } else {
      database.insert(tileBean).getRowsAffected();
    }
    return tileBean;
  }

  @Override
  public void remove(TileBean tileBean) {
    int rows = database.delete(tileBean).getRowsAffected();
    Logger.trace(rows + " rows deleted");
  }

  @Override
  public void removeTile(Integer x, Integer y) {
    Object[] args = new Object[]{x, y};
    database.sql("delete from tiles where x= ? and y = ?", args);
  }

  @Override
  public void persist(List<TileBean> tiles) {
    //When persisting a whole list of tiles,
    //check for tiles which have changed position
    //and check for tiles which have to be removed
    List<TileBean> existing = database.results(TileBean.class);

    Map<Point, TileBean> currentTP = new HashMap<>();
    Map<Point, TileBean> updatedTP = new HashMap<>();

    for (TileBean tb : existing) {
      currentTP.put(tb.getCenter(), tb);
    }

    for (TileBean tb : tiles) {
      updatedTP.put(tb.getCenter(), tb);
    }

    //remove the ones which do no longer exists
    Set<Point> currentPoints = currentTP.keySet();
    Set<Point> updatedPoints = updatedTP.keySet();

    for (Point p : currentPoints) {
      if (!updatedPoints.contains(p)) {
        TileBean tb = this.getTile(p.x, p.y);
        this.remove(tb);
      }
    }

    for (TileBean tb : tiles) {
      if (tb.getId() == null) {
        Integer x = tb.getX();
        Integer y = tb.getY();
        TileBean tbxy = this.getTile(x, y);

        if (tbxy != null) {
          tb.setId(tbxy.getId());
        }
      }
      persist(tb);
    }
  }

  private List<RouteElementBean> getRouteElements(Long routeId) {
    List<RouteElementBean> routeElements = database.where("route_id=?", routeId).orderBy("id").results(RouteElementBean.class);
    return routeElements;
  }

  private RouteElementBean persist(RouteElementBean routeElement) {
    if (database.where("id=?", routeElement.getId()).first(RouteElementBean.class) != null) {
      int rows = database.update(routeElement).getRowsAffected();
      Logger.trace(rows + " rows updated");
    } else {
      int rows = database.insert(routeElement).getRowsAffected();
      Logger.trace(rows + " rows inserted");
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
  public RouteBean getRoute(Integer id) {
    RouteBean route = database.where("id = ?", id).first(RouteBean.class);
    if (route != null) {
      List<RouteElementBean> routeElements = getRouteElements(route.getId());
      route.setRouteElements(routeElements);
    }
    return route;
  }

  @Override
  public RouteBean getRoute(String fromTileId, String fromTileSite, String toTileId, String toTileSite) {
    Object[] args = new Object[]{fromTileId, fromTileSite, toTileId, toTileSite};
    RouteBean route = database.where("from_tile_id = ? and from_tile_site = ? and to_tile_id = ? and to_tile_site = ?", args).first(RouteBean.class);
    if (route != null) {
      List<RouteElementBean> routeElements = getRouteElements(route.getId());
      route.setRouteElements(routeElements);
    }
    return route;
  }

  @Override
  public RouteBean persist(RouteBean route) {
    if (database.where("id=?", route.getId()).first(RouteBean.class) != null) {
      int rows = database.update(route).getRowsAffected();
      Logger.trace(rows + " rows updated");
    } else {
      int rows = database.insert(route).getRowsAffected();
      Logger.trace(rows + " rows inserted");
    }

    if (route.getRouteElements() != null && !route.getRouteElements().isEmpty()) {
      //remove all
      database.sql("delete from route_elements where route_id =?", route.getId()).execute();

      List<RouteElementBean> rbl = route.getRouteElements();
      List<RouteElementBean> rblr = new LinkedList<>();

      for (RouteElementBean rb : rbl) {
        rb.setRouteId(route.getId());
        //Reset the id
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
      //remove all
      database.sql("delete from route_elements where route_d =?", route.getId()).execute();
    }

    int rows = this.database.delete(route).getRowsAffected();
    Logger.trace(rows + " rows deleted");
  }

  private void setJCSPropertiesAsSystemProperties() {
    List<JCSPropertyBean> props = getProperties();
    props.forEach(p -> {
      System.setProperty(p.getKey(), p.getValue());
    });
  }

}
