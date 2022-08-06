/*
 * Copyright (C) 2018 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.trackservice;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;
import jcs.JCS;
import jcs.controller.cs3.events.SensorMessageEvent;
import jcs.controller.cs3.events.CanMessageListener;
import jcs.entities.JCSProperty;
import jcs.entities.SensorBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.DecoderType;
import jcs.entities.enums.Direction;
import jcs.trackservice.dao.JCSPropertiesDAO;
import jcs.trackservice.events.AccessoryListener;
import jcs.controller.cs3.events.SensorMessageListener;
import jcs.entities.AccessoryBean;
import jcs.entities.FunctionBean;
import jcs.entities.JCSEntity;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean;
import jcs.trackservice.dao.AccessoryBeanDAO;
import jcs.trackservice.dao.SensorDAO;
import jcs.trackservice.events.SensorListener;
import jcs.trackservice.dao.FunctionBeanDAO;
import jcs.trackservice.dao.LocomotiveBeanDAO;
import jcs.trackservice.dao.TileBeanDAO;
import org.tinylog.Logger;
import jcs.controller.MarklinController;
import jcs.controller.cs3.devices.LinkSxx;
import jcs.controller.cs3.events.AccessoryMessageEvent;
import jcs.controller.cs3.events.AccessoryMessageEventListener;
import jcs.controller.cs3.events.DirectionMessageEvent;
import jcs.controller.cs3.events.DirectionMessageEventListener;
import jcs.controller.cs3.events.FunctionMessageEvent;
import jcs.controller.cs3.events.PowerEventListener;
import jcs.entities.enums.TileType;
import static jcs.entities.enums.TileType.BLOCK;
import static jcs.entities.enums.TileType.CROSS;
import static jcs.entities.enums.TileType.CURVED;
import static jcs.entities.enums.TileType.SENSOR;
import static jcs.entities.enums.TileType.SIGNAL;
import static jcs.entities.enums.TileType.STRAIGHT;
import jcs.controller.cs3.events.FunctionMessageEventListener;
import jcs.controller.cs3.events.VelocityMessageEvent;
import jcs.controller.cs3.events.VelocityMessageEventListener;
import jcs.entities.Route;
import jcs.entities.RouteElement;
import jcs.trackservice.dao.RouteDAO;
import jcs.trackservice.dao.RouteElementDAO;
import jcs.trackservice.events.DirectionListener;
import jcs.trackservice.events.FunctionListener;
import jcs.trackservice.events.VelocityListener;

public class H2TrackService implements TrackService {

    private final JCSPropertiesDAO propDao;
    private final LocomotiveBeanDAO locoDAO;
    private final FunctionBeanDAO funcDAO;
    private final AccessoryBeanDAO acceDAO;
    private final SensorDAO sensDAO;
    private final TileBeanDAO tileDAO;

    private final RouteDAO routeDAO;
    private final RouteElementDAO routeElementDAO;

    private MarklinController controllerService;

    private final List<SensorListener> sensorListeners;
    private final List<AccessoryListener> accessoryListeners;
    private final List<FunctionListener> functionListeners;

    private final List<DirectionListener> directionListeners;
    private final List<VelocityListener> velocityListeners;

    private final Properties jcsProperties;

    //private StatusDataConfigParser controllerInfo;
    private HashMap<String, Image> imageCache;
    private HashMap<String, Image> functionImageCache;

    public H2TrackService() {
        this(true);
    }

    private H2TrackService(boolean aquireControllerService) {
        propDao = new JCSPropertiesDAO();
        jcsProperties = new Properties();

        imageCache = new HashMap<>();
        functionImageCache = new HashMap<>();

        sensDAO = new SensorDAO();
        locoDAO = new LocomotiveBeanDAO();
        funcDAO = new FunctionBeanDAO();
        acceDAO = new AccessoryBeanDAO();
        tileDAO = new TileBeanDAO();

        routeDAO = new RouteDAO();
        routeElementDAO = new RouteElementDAO();

        sensorListeners = new LinkedList<>();
        accessoryListeners = new LinkedList<>();
        functionListeners = new LinkedList<>();
        directionListeners = new LinkedList<>();
        velocityListeners = new LinkedList<>();

        retrieveJCSProperties();

        if (aquireControllerService) {
            if (System.getProperty("trackServiceSkipControllerInit", "false").equals("true")) {
                Logger.info("Skipping controller initialization...");
            } else {
                connect();
                Logger.trace(controllerService != null ? "Aquired " + controllerService.getClass().getSimpleName() : "Could not aquire a Controller Service!");
            }
        }
    }

    private void retrieveJCSProperties() {
        JCS.logProgress("Connect to Database");
        List<JCSProperty> props = this.propDao.findAll();

        props.forEach(p -> {
            jcsProperties.setProperty(p.getKey(), p.getValue());
            System.setProperty(p.getKey(), p.getValue());
        });
    }

    @Override
    public final boolean connect() {
        JCS.logProgress("Connecting to Central Station");
        String controllerImpl = System.getProperty("CS3");

        if (controllerService == null) {
            try {
                this.controllerService = (MarklinController) Class.forName(controllerImpl).getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
                Logger.error("Can't instantiate a '" + controllerImpl + "' " + ex.getMessage());
            }
        }

        if (controllerService != null) {
            this.controllerService.addSensorMessageListener(new SensorMessageEventListener(this));
            this.controllerService.addAccessoryEventListener(new AccessoryMessageListener(this));
            this.controllerService.addFunctionMessageEventListener(new FunctionMessageListener(this));
            this.controllerService.addDirectionMessageEventListener(new DirectionMessageListener(this));
            this.controllerService.addVelocityMessageEventListener(new VelocityMessageListener(this));
        }

        JCS.logProgress("Obtaining the last state of all items...");

        return this.controllerService != null && this.controllerService.isConnected();
    }

    @Override
    public boolean isConnected() {
        return this.controllerService.isConnected();
    }

    @Override
    public void disconnect() {
        this.controllerService.disconnect();
        this.controllerService = null;
    }

    @Override
    public List<SensorBean> getSensors() {
        return sensDAO.findAll();
    }

    @Override
    public SensorBean getSensor(Integer deviceId, Integer contactId) {
        return sensDAO.find(deviceId, contactId);
    }

    @Override
    public SensorBean getSensor(BigDecimal id) {
        return sensDAO.findById(id);
    }

    @Override
    public SensorBean persist(SensorBean sensor) {
        SensorBean prev = sensDAO.find(sensor.getDeviceId(), sensor.getContactId());
        if (prev != null) {
            sensor.setId(prev.getId());
            sensor.setName(prev.getName());
            sensDAO.persist(sensor);
        }
        return sensor;
    }

    @Override
    public void remove(JCSEntity entity) {
        if (entity instanceof SensorBean) {
            sensDAO.remove((SensorBean) entity);
        } else if (entity instanceof LocomotiveBean) {
            Collection<FunctionBean> functions = ((LocomotiveBean) entity).getFunctions().values();
            funcDAO.remove(functions);
            locoDAO.remove((LocomotiveBean) entity);
        } else if (entity instanceof AccessoryBean) {
            //Check whether the turnout is linked to a layout tile
            this.acceDAO.remove((AccessoryBean) entity);
//        } else if (entity instanceof SignalBean) {
//            //Check whether the signal is linked to a layout tile
//            signalDAO.remove((SignalBean) entity);
        } else if (entity instanceof JCSProperty) {
            this.propDao.remove((JCSProperty) entity);
        }
    }

    @Override
    public LocomotiveBean getLocomotive(Integer address, DecoderType decoderType) {
        LocomotiveBean loco = locoDAO.find(address, decoderType.getDecoderType());
        if (loco != null) {
            List<FunctionBean> functions = this.funcDAO.findBy(loco.getId());
            loco.addAllFunctions(functions);
            loco.setLocIcon(getLocomotiveImage(loco.getIcon()));
        }
        return loco;
    }

    @Override
    public LocomotiveBean getLocomotive(BigDecimal id) {
        LocomotiveBean loco = locoDAO.findById(id);
        if (loco != null) {
            List<FunctionBean> functions = this.funcDAO.findBy(loco.getId());
            loco.addAllFunctions(functions);
            loco.setLocIcon(getLocomotiveImage(loco.getIcon()));
        }
        return loco;
    }

    @Override
    public List<LocomotiveBean> getLocomotives() {
        List<LocomotiveBean> locos = locoDAO.findAll();

        for (LocomotiveBean loco : locos) {
            List<FunctionBean> functions = this.funcDAO.findBy(loco.getId());
            loco.addAllFunctions(functions);
            loco.setLocIcon(getLocomotiveImage(loco.getIcon()));
        }

        return locos;
    }

    public Image getLocomotiveImage(String imageName) {
        if (!imageCache.containsKey(imageName)) {
            //Try to load the image from the file cache
            boolean fromCS3 = false;
            Image image = readImage(imageName);
            if (image == null) {
                image = controllerService.getLocomotiveImage(imageName);
                fromCS3 = (image != null);
            }
            if (image != null) {
                int size = 100;
                if (fromCS3) {
                    storeImage(image, imageName);
                }
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
            Image image = readFunctionImage(imageName);
            if (image != null) {
                int size = 30;
                float aspect = (float) image.getHeight(null) / (float) image.getWidth(null);
                this.functionImageCache.put(imageName, image.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH));
            }
        }
        return this.functionImageCache.get(imageName);
    }

    private void storeImage(Image image, String imageName) {
        String path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache";
        File cachePath = new File(path);
        if (cachePath.mkdir()) {
            Logger.trace("Created new directory " + cachePath);
        }
        try {
            ImageIO.write((BufferedImage) image, "png", new File(path + File.separator + imageName + ".png"));
        } catch (IOException ex) {
            Logger.error("Can't store image " + cachePath.getName() + "! ", ex.getMessage());
        }
        Logger.trace("Stored image " + imageName + ".png in the cache");
    }

    private Image readImage(String imageName) {
        String path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator;
        Image image = null;

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

    private Image readFunctionImage(String imageName) {
        String path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + "functions" + File.separator;
        Image image = null;

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

    @Override
    public LocomotiveBean persist(LocomotiveBean locomotive) {
        locoDAO.persist(locomotive);
        funcDAO.persist(locomotive.getFunctions().values());

        return locomotive;
    }

    @Override
    public List<AccessoryBean> getTurnouts() {
        return this.acceDAO.findBy("%weiche");
    }

    @Override
    public List<AccessoryBean> getSignals() {
        return this.acceDAO.findBy("%signal%");
    }

    @Override
    public AccessoryBean getAccessory(BigDecimal id) {
        return this.acceDAO.findById(id);
    }

    @Override
    public AccessoryBean getAccessory(Integer address) {
        return this.acceDAO.find(address);
    }

    @Override
    public AccessoryBean persist(AccessoryBean accessory) {
        this.acceDAO.persist(accessory);
        return accessory;
    }

    @Override
    public List<JCSProperty> getProperties() {
        return this.propDao.findAll();
    }

    @Override
    public JCSProperty getProperty(String key) {
        return this.propDao.find(key);
    }

    @Override
    public JCSProperty persist(JCSProperty property) {
        this.propDao.persist(property);
        return property;
    }

    @Override
    public Set<TileBean> getTiles() {
        Set<TileBean> beans = new HashSet<>();
        beans.addAll(this.tileDAO.findAll());

        return beans;
    }

    @Override
    public TileBean getTile(Integer x, Integer y) {
        return this.tileDAO.findByXY(x, y);
    }

    @Override
    public TileBean persist(TileBean tile) {

        if (tile.getEntityBean() != null) {
            TileType tileType = tile.getTileType();

            switch (tileType) {
                case STRAIGHT:
                    break;
                case CURVED:
                    break;
                case SWITCH:
                    AccessoryBean turnout = (AccessoryBean) tile.getAccessoryBean();
                    tile.setAccessoryBeanId(this.acceDAO.persist(turnout));
                    break;
                case CROSS:
                    AccessoryBean cross = (AccessoryBean) tile.getAccessoryBean();
                    tile.setAccessoryBeanId(this.acceDAO.persist(cross));
                    break;
                case SIGNAL:
                    AccessoryBean signal = (AccessoryBean) tile.getAccessoryBean();
                    tile.setAccessoryBeanId(this.acceDAO.persist(signal));
                    break;
                case SENSOR:
                    SensorBean sensor = (SensorBean) tile.getSensorBean();
                    tile.setSensorBeanId(this.sensDAO.persist(sensor));
                    break;
                case BLOCK:
                    break;
                default:
                    Logger.warn("Unknown Tile Type " + tileType);
            }
        }
        this.tileDAO.persist(tile);

        return tile;
    }

    @Override
    public void persist(Set<TileBean> tiles) {
        //get all existing tiles from database
        List<TileBean> existing = tileDAO.findAll();

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
                tileDAO.remove(p.x, p.y);
            }
        }

        for (TileBean tb : tiles) {
            if (tb.getId() == null) {
                //store the layouttile but incase check if it exist based on x and y
                TileBean tbxy = this.tileDAO.findByXY(tb.getX(), tb.getY());
                if (tbxy != null) {
                    tb.setId(tbxy.getId());
                }
            }
            persist(tb);
        }
    }

    @Override
    public List<Route> getRoutes() {
        List<Route> routes = this.routeDAO.findAll();

        for (Route r : routes) {
            List<RouteElement> elements = this.routeElementDAO.findByRouteId(r.getId());
            r.setRouteElements(elements);
        }
        return routes;
    }

    @Override
    public void persist(Route route) {
        this.routeDAO.persist(route);

        //first remove all elements if any
        this.routeElementDAO.removeByRouteId(route.getId());
        for (RouteElement re : route.getRouteElements()) {
            this.routeElementDAO.persist(re);
        }
    }

    @Override
    public void remove(Route route) {
        this.routeElementDAO.removeByRouteId(route.getId());
        this.routeDAO.remove(route);
    }

    @Override
    public void remove(TileBean tile) {
        this.tileDAO.remove(tile);
    }

    @Override
    public String getControllerName() {
        if (this.controllerService != null) {
            return this.controllerService.getName();
        } else {
            return null;
        }
    }

    @Override
    public String getControllerSerialNumber() {
        if (this.controllerService != null) {
            return this.controllerService.getSerialNumber();
        } else {
            return null;
        }
    }

    @Override
    public String getControllerArticleNumber() {
        if (this.controllerService != null) {
            return this.controllerService.getArticleNumber();
        } else {
            return null;
        }
    }

    @Override
    public LinkSxx getLinkSxx() {
        if (this.controllerService != null) {
            return this.controllerService.getLinkSxx();
        } else {
            return null;
        }
    }

    @Override
    public void addPowerEventListener(PowerEventListener listener) {
        if (this.controllerService != null) {
            this.controllerService.addPowerEventListener(listener);
        }
    }

    @Override
    public void removePowerEventListener(PowerEventListener listener) {
        if (this.controllerService != null) {
            this.controllerService.removePowerEventListener(listener);
        }
    }

    @Override
    public void switchPower(boolean on) {
        Logger.trace("Switch Power " + (on ? "On" : "Off"));
        if (this.controllerService != null) {
            this.controllerService.power(on);
        }
    }

    @Override
    public boolean isPowerOn() {
        boolean power = false;
        if (this.controllerService != null) {
            power = controllerService.isPower();
        }

        return power;
    }

    @Override
    public void synchronizeLocomotivesWithController() {
        List<LocomotiveBean> fromCs2 = this.controllerService.getLocomotives();

        for (LocomotiveBean loc : fromCs2) {
            Integer addr = loc.getAddress();
            String dt = loc.getDecoderTypeString();
            LocomotiveBean dbLoc = this.locoDAO.find(addr, dt);
            if (dbLoc != null) {
                loc.setId(dbLoc.getId());
                Logger.trace("Update " + loc.toLogString());
            } else {
                Logger.trace("Add " + loc.toLogString());
            }

            persist(loc);
        }
        //Also cache the function Icons
        this.controllerService.cacheAllFunctionIcons();
    }

    @Override
    public void synchronizeTurnouts() {
        List<AccessoryBean> ma = this.controllerService.getSwitches();

        for (AccessoryBean ab : ma) {
            Logger.trace(ab.toLogString());
            this.acceDAO.persist(ab);
        }
    }

    @Override
    public void synchronizeSignals() {
        List<AccessoryBean> ma = this.controllerService.getSignals();

        for (AccessoryBean ab : ma) {
            Logger.trace(ab.toLogString());
            this.acceDAO.persist(ab);
        }
    }

    @Override
    public void updateGuiStatuses() {
        //updateAccessoryStatuses();
    }

    @Override
    public void changeDirection(Direction newDirection, LocomotiveBean locomotive) {
        Logger.debug("Changing direction to " + newDirection + " for: " + locomotive.toLogString());
        Integer address = locomotive.getAddress();
        DecoderType decoderType = locomotive.getDecoderType();

        //Issue a halt or stop for the loc
        controllerService.changeVelocity(locomotive.getAddress(), DecoderType.get(locomotive.getDecoderTypeString()), 0);
        controllerService.changeDirection(address, decoderType, newDirection);
    }

    @Override
    public void changeVelocity(Integer newVelocity, LocomotiveBean locomotive) {
        Logger.trace("Changing velocity to " + newVelocity + " for " + locomotive.getName());
        controllerService.changeVelocity(locomotive.getAddress(), DecoderType.get(locomotive.getDecoderTypeString()), newVelocity);
    }

    @Override
    public void changeFunction(Boolean newValue, Integer functionNumber, LocomotiveBean locomotive) {
        Logger.trace("Changing Function " + functionNumber + " to " + (newValue ? "on" : "off") + " on " + locomotive.getName());
        controllerService.changeFunctionValue(locomotive.getAddress(), DecoderType.get(locomotive.getDecoderTypeString()), functionNumber, newValue);
    }

    @Override
    public void switchAccessory(AccessoryValue value, AccessoryBean accessory) {
        int address = accessory.getAddress();
        AccessoryValue val = value;
        if (accessory.isSignal() && accessory.getStates() > 2) {
            if (accessory.getPosition() > 1) {
                address = address + 1;
                val = AccessoryValue.cs3Get(accessory.getPosition() - 2);
            }
        }

        Logger.trace("Change accessory with address: " + address + ", " + accessory.getName() + " to " + val.getValue());
        controllerService.switchAccessory(address, val);
    }

    @Override
    public void addMessageListener(CanMessageListener listener) {
        if (this.controllerService != null) {
            //this.controllerService.addCanMessageListener(listener);
        }
    }

    @Override
    public void removeMessageListener(CanMessageListener listener) {
        if (this.controllerService != null) {
            //this.controllerService.removeCanMessageListener(listener);
        }
    }

    @Override
    public void addSensorListener(SensorListener listener) {
        this.sensorListeners.add(listener);
    }

    @Override
    public void removeSensorListener(SensorListener listener) {
        this.sensorListeners.remove(listener);
    }

    @Override
    public void addAccessoryListener(AccessoryListener listener) {
        this.accessoryListeners.add(listener);
    }

    @Override
    public void removeAccessoryListener(AccessoryListener listener) {
        this.accessoryListeners.remove(listener);
    }

    @Override
    public void addFunctionListener(FunctionListener listener) {
        this.functionListeners.add(listener);
    }

    @Override
    public void removeFunctionListener(FunctionListener listener) {
        this.functionListeners.remove(listener);
    }

    @Override
    public void addDirectionListener(DirectionListener listener) {
        this.directionListeners.add(listener);
    }

    @Override
    public void removeDirectionListener(DirectionListener listener) {
        this.directionListeners.remove(listener);
    }

    @Override
    public void addVelocityListener(VelocityListener listener) {
        this.velocityListeners.add(listener);
    }

    @Override
    public void removeVelocityListener(VelocityListener listener) {
        this.velocityListeners.remove(listener);
    }

    private class SensorMessageEventListener implements SensorMessageListener {

        private final H2TrackService trackService;

        SensorMessageEventListener(H2TrackService trackService) {
            this.trackService = trackService;
        }

        @Override
        public void onSensorMessage(SensorMessageEvent event) {
            SensorBean sb = event.getSensorBean();
            SensorBean dbsb = this.trackService.getSensor(sb.getDeviceId(), sb.getContactId());
            if (dbsb != null) {
                sb.setId(dbsb.getId());
                sb.setName(dbsb.getName());
                this.trackService.persist(sb);
            }

            for (SensorListener sl : this.trackService.sensorListeners) {
                sl.onChange(sb);
            }
        }
    }

    private class AccessoryMessageListener implements AccessoryMessageEventListener {

        private final H2TrackService trackService;

        AccessoryMessageListener(H2TrackService trackService) {
            this.trackService = trackService;
        }

        @Override
        public void onAccessoryMessage(AccessoryMessageEvent event) {
            AccessoryBean ab = event.getAccessoryBean();

            int address = ab.getAddress();
            AccessoryBean dbab = this.trackService.getAccessory(ab.getAddress());
            if (dbab == null) {
                //check if address is even, might be the second address of a signal
                if (address % 2 == 0) {
                    address = address - 1;
                    dbab = this.trackService.getAccessory(address);
                    if (dbab != null && dbab.isSignal() && dbab.getStates() > 2) {
                        ab.setAddress(address);
                        int p = ab.getPosition() + 2;
                        ab.setPosition(p);
                    } else {
                        dbab = null;
                    }
                }
            }

            if (dbab != null) {
                //set all properties
                ab.setId(dbab.getId());
                ab.setDecoder(dbab.getDecoder());
                ab.setDecoderType(dbab.getDecoderType());
                ab.setName(dbab.getName());
                ab.setType(dbab.getType());
                ab.setGroup(dbab.getGroup());
                ab.setIcon(dbab.getIcon());
                ab.setIconFile(dbab.getIconFile());
                ab.setStates(dbab.getStates());
                //might be set by the event
                if (ab.getSwitchTime() == null) {
                    ab.setSwitchTime(dbab.getSwitchTime());
                }
                this.trackService.persist(ab);

                for (AccessoryListener al : this.trackService.accessoryListeners) {
                    al.onChange(event);
                }
            }
        }
    }

    private class FunctionMessageListener implements FunctionMessageEventListener {

        private final H2TrackService trackService;

        FunctionMessageListener(H2TrackService trackService) {
            this.trackService = trackService;
        }

        @Override
        public void onFunctionMessage(FunctionMessageEvent functionEvent) {
            LocomotiveBean lb = functionEvent.getLocomotiveBean();

            BigDecimal id = lb.getId();
            LocomotiveBean dblb = this.trackService.getLocomotive(id);

            if (dblb != null) {
                FunctionBean fb = lb.getFunctionBean(functionEvent.getUpdatedFunctionNumber());
                if (fb != null) {
                    dblb.setFunctionValue(fb.getNumber(), fb.getValue());
                    this.trackService.persist(dblb);
                    functionEvent.setLocomotiveBean(dblb);
                }
                for (FunctionListener fl : this.trackService.functionListeners) {
                    fl.onFunctionChange(functionEvent);
                }
            }
        }
    }

    private class DirectionMessageListener implements DirectionMessageEventListener {

        private final H2TrackService trackService;

        DirectionMessageListener(H2TrackService trackService) {
            this.trackService = trackService;
        }

        @Override
        public void onDirectionMessage(DirectionMessageEvent directionEvent) {
            LocomotiveBean lb = directionEvent.getLocomotiveBean();

            BigDecimal id = lb.getId();
            LocomotiveBean dblb = this.trackService.getLocomotive(id);

            if (dblb != null) {
                Integer richtung = lb.getRichtung();
                dblb.setRichtung(richtung);
                this.trackService.persist(dblb);
                directionEvent.setLocomotiveBean(dblb);

                for (DirectionListener dl : this.trackService.directionListeners) {
                    dl.onDirectionChange(directionEvent);
                }
            }
        }

    }

    private class VelocityMessageListener implements VelocityMessageEventListener {

        private final H2TrackService trackService;

        VelocityMessageListener(H2TrackService trackService) {
            this.trackService = trackService;
        }

        @Override
        public void onVelocityMessage(VelocityMessageEvent velocityEvent) {
            LocomotiveBean lb = velocityEvent.getLocomotiveBean();
            if (lb != null && lb.getId() != null) {
                BigDecimal id = lb.getId();
                LocomotiveBean dblb = this.trackService.getLocomotive(id);

                if (dblb != null) {
                    Integer velocity = lb.getVelocity();
                    dblb.setVelocity(velocity);
                    this.trackService.persist(dblb);
                    velocityEvent.setLocomotiveBean(dblb);

                    for (VelocityListener dl : this.trackService.velocityListeners) {
                        dl.onVelocityChange(velocityEvent);
                    }
                }
            }
        }
    }

}

//TODO clean this up!
//    private boolean connectController() {
//
//        JCS.logProgress("Connecting to Central Station");
//        String controllerImpl = System.getProperty("CS3");
//        if (controllerService == null) {
//            try {
//                this.controllerService = (MarklinController) Class.forName(controllerImpl).getDeclaredConstructor().newInstance();
//            } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
//                Logger.error("Can't instantiate a '" + controllerImpl + "' " + ex.getMessage());
//            }
//        }
//
//        JCS.logProgress("Obtaining the last state of all items...");
//
//        //Configure the sensors
//        int sensorCount = 0;//controllerService.getControllerInfo().getLinkSxx().getTotalSensors();
//        List<SensorBean> allSensors = sensDAO.findAll();
//        if (sensorCount != allSensors.size()) {
//            Logger.debug("The Sensor count has changed since last run from " + allSensors.size() + " to " + sensorCount + "...");
//            //remove sensors which are not in the system
//            if (allSensors.size() > sensorCount) {
//                for (int contactId = sensorCount; contactId <= allSensors.size(); contactId++) {
//                    SensorBean s = this.sensDAO.find(contactId);
//                    if (s == null) {
//                        //remove the sensor
//                        sensDAO.remove(s);
//                    }
//                }
//            }
//            for (int contactId = 1; contactId <= sensorCount; contactId++) {
//                //is there a sensor in the database?
//                SensorBean s = this.sensDAO.find(contactId);
//                if (s == null) {
//                    String name = "m" + SensorBean.calculateModuleNumber(contactId) + "p" + SensorBean.calculatePortNumber(contactId);
//                    String description = name;
//                    //create the sensor
//                    s = new SensorBean(contactId, name, description, 0, 0, 0, 0);
//                    if (s.getId() != null) {
//                        sensDAO.persist(s);
//                    }
//                }
//            }
//        } else {
//            Logger.trace("The Sensor count has not changed since last run...");
//        }
//        if (controllerService != null) {
//            this.controllerService.addSensorMessageListener(new SensorMessageEventListener(this));
//            this.controllerService.addAccessoryEventListener(new AccessoryMessageListener(this));
//            this.controllerService.addFunctionMessageEventListener(new FunctionMessageListener(this));
//            this.controllerService.addDirectionMessageEventListener(new DirectionMessageListener(this));
//            this.controllerService.addVelocityMessageEventListener(new VelocityMessageListener(this));
//        }
//
//        return this.controllerService != null && this.controllerService.isConnected();
//
//    }
