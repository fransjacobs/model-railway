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
package lan.wervel.jcs.trackservice;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lan.wervel.jcs.controller.ControllerEvent;
import lan.wervel.jcs.controller.ControllerEventListener;
import lan.wervel.jcs.controller.cs2.DeviceInfo;
import lan.wervel.jcs.controller.ControllerService;
import lan.wervel.jcs.controller.cs2.events.SensorMessageEvent;
import lan.wervel.jcs.controller.cs2.events.CanMessageListener;
import lan.wervel.jcs.entities.ControllableDevice;
import lan.wervel.jcs.entities.JCSProperty;
import lan.wervel.jcs.entities.LayoutTile;
import lan.wervel.jcs.entities.LayoutTileGroup;
import lan.wervel.jcs.entities.Locomotive;
import lan.wervel.jcs.entities.Sensor;
import lan.wervel.jcs.entities.Signal;
import lan.wervel.jcs.entities.SolenoidAccessory;
import lan.wervel.jcs.entities.TrackPower;
import lan.wervel.jcs.entities.Turnout;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.DecoderType;
import lan.wervel.jcs.entities.enums.Direction;
import static lan.wervel.jcs.entities.enums.SignalValue.Hp0;
import static lan.wervel.jcs.entities.enums.SignalValue.Hp0Sh1;
import static lan.wervel.jcs.entities.enums.SignalValue.Hp1;
import static lan.wervel.jcs.entities.enums.SignalValue.Hp2;
import lan.wervel.jcs.trackservice.dao.JCSPropertiesDAO;
import lan.wervel.jcs.trackservice.dao.LocomotiveDAO;
import lan.wervel.jcs.trackservice.dao.SignalDAO;
import lan.wervel.jcs.trackservice.dao.TrackPowerDAO;
import lan.wervel.jcs.trackservice.dao.TurnoutDAO;
import lan.wervel.jcs.trackservice.events.AccessoryListener;
import lan.wervel.jcs.trackservice.events.HeartBeatListener;
import lan.wervel.jcs.trackservice.events.LocomotiveListener;
import lan.wervel.jcs.trackservice.events.PersistedEventListener;
import org.pmw.tinylog.Logger;
import lan.wervel.jcs.controller.HeartbeatListener;
import lan.wervel.jcs.controller.cs2.AccessoryStatus;
import lan.wervel.jcs.controller.cs2.events.SensorMessageListener;
import lan.wervel.jcs.entities.enums.SignalValue;
import lan.wervel.jcs.trackservice.dao.LayoutTileDAO;
import lan.wervel.jcs.trackservice.dao.LayoutTileGroupDAO;
import lan.wervel.jcs.trackservice.dao.SensorDAO;
import lan.wervel.jcs.trackservice.events.SensorListener;

public class H2TrackService implements TrackService {

    private final SensorDAO sensDAO;
    private final LocomotiveDAO locoDAO;
    private final TurnoutDAO turnoutDAO;
    private final SignalDAO signalDAO;
    private final TrackPowerDAO trpoDAO;

    private final LayoutTileDAO latiDao;
    private final LayoutTileGroupDAO ltgtDao;
    private final JCSPropertiesDAO propDao;

    private ControllerService controllerService;
    int feedbackModules;

    private final ExecutorService executor;

    private final List<AccessoryListener> accessoiryListeners;
    private final List<LocomotiveListener> locomotiveListeners;
    private final List<HeartBeatListener> heartBeatListeners;
    private final List<PersistedEventListener> persistListeners;

    private Map<Integer, List<SensorListener>> sensorListeners;

    private boolean fbToggle = false;

    private TrackPower trpo;

    private final Properties jcsProperties;

    private DeviceInfo controllerInfo;
    private final Timer timer;

    public H2TrackService() {
        this(true);
    }

    private H2TrackService(boolean aquireControllerService) {
        propDao = new JCSPropertiesDAO();
        jcsProperties = new Properties();

        sensDAO = new SensorDAO();
        locoDAO = new LocomotiveDAO();
        turnoutDAO = new TurnoutDAO();
        signalDAO = new SignalDAO();
        trpoDAO = new TrackPowerDAO();
        latiDao = new LayoutTileDAO();
        ltgtDao = new LayoutTileGroupDAO();
        executor = Executors.newCachedThreadPool();

        accessoiryListeners = new ArrayList<>();
        locomotiveListeners = new ArrayList<>();
        heartBeatListeners = new ArrayList<>();

        sensorListeners = new HashMap<>();

        persistListeners = new ArrayList<>();
        timer = new Timer("UpdateGui", true);

        retrieveJCSProperties();
        trpo = trpoDAO.find(1);

        if (aquireControllerService) {
            aquireControllerService();
        }

        Logger.debug(controllerService != null ? "Aquired " + controllerService.getClass().getSimpleName() : "Could not aquire a Controller Service!");
    }

    private void retrieveJCSProperties() {
        List<JCSProperty> props = this.propDao.findAll();

        props.forEach(p -> {
            jcsProperties.setProperty(p.getKey(), p.getValue());
            System.setProperty(p.getKey(), p.getValue());
        });
    }

    private void aquireControllerService() {
        //String m6050Local = jcsProperties.getProperty("M6050-local");
        //String m6050Remote = jcsProperties.getProperty("M6050-remote");
        String m6050Demo = System.getProperty("M6050-demo");
        String cs2 = System.getProperty("CS2");
        String activeControllerService = System.getProperty("activeControllerService");

        String moduleCount = System.getProperty("S88-module-count", "1");
        this.feedbackModules = Integer.decode(moduleCount);

        Logger.debug("There are " + feedbackModules + " FeedbackModules");
        Logger.debug("ActiveControllerService: " + activeControllerService);

        String controllerImpl;

        switch (activeControllerService) {
//            case "M6050-remote":
//                controllerImpl = m6050Remote;
//                break;
            case "CS2":
                controllerImpl = cs2;
                break;
            default:
                controllerImpl = m6050Demo;
                break;
        }

        if (controllerService == null) {
            try {
                this.controllerService = (ControllerService) Class.forName(controllerImpl).getDeclaredConstructor().newInstance();

                if (!this.controllerService.isConnected()) {
                    Logger.info("Not connected to Real CS2/3. Switch to demo...");
                    this.controllerService.disconnect();
                    this.controllerService = null;
                }
            } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
                Logger.error("Can't instantiate a '" + controllerImpl + "' " + ex.getMessage());
            }
        }

        if (controllerService == null) {
            //Use a demo...
            try {
                controllerImpl = "lan.wervel.jcs.controller.demo.DemoController";
                this.controllerService = (ControllerService) Class.forName(controllerImpl).getDeclaredConstructor().newInstance();
                Logger.info("Using a DEMO controller");
            } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException exd) {
                Logger.error("Can't instantiate a '" + controllerImpl + "' " + exd.getMessage());
            }
        }

        //Configure the sensors
        int sensorCount = feedbackModules * 16;
        List<Sensor> allSensors = sensDAO.findAll();

        System.setProperty("sensorCount", "" + sensorCount);

        if (sensorCount != allSensors.size()) {
            Logger.debug("The Sensor count has changed since last run...");
            //remove sensors whic are not in the system
            if (allSensors.size() > sensorCount) {
                for (int contactId = sensorCount; contactId <= allSensors.size(); contactId++) {
                    Sensor s = this.sensDAO.find(contactId);
                    if (s == null) {
                        //remove the sensor
                        sensDAO.remove(s);
                    }
                }
            }
            for (int contactId = 1; contactId <= sensorCount; contactId++) {
                //is there a sensor in the database?
                Sensor s = this.sensDAO.find(contactId);
                if (s == null) {
                    String name = "m" + Sensor.calculateModuleNumber(contactId) + "p" + Sensor.calculatePortNumber(contactId);
                    String description = name;
                    //create the sensor
                    s = new Sensor(contactId, name, description, 0, 0, 0, 0);
                    sensDAO.persist(s);
                }
            }
        } else {
            Logger.trace("The Sensor count has not changed since last run...");
        }
        //Update the sensors with the status od the Controller
        synchronizeSensors();

        controllerInfo = controllerService.getControllerInfo();

        controllerService.addSensorMessageListener(new SensorMessageEventHandler(this));
        controllerService.addHeartbeatListener(new ControllerWatchDogListener());
        controllerService.addControllerEventListener(new ControllerServiceEventListener());

        timer.scheduleAtFixedRate(new UpdateGuiStatusTask(this), 0, 1000);
    }

    @Override
    public List<Sensor> getSensors() {
        return sensDAO.findAll();
    }

    @Override
    public Sensor getSensor(Integer contactId) {
        return sensDAO.find(contactId);
    }

    @Override
    public Sensor persist(Sensor sensor) {
        Sensor prev = sensDAO.find(sensor.getContactId());
        //make shure the name etc is kept
        if (prev != null) {
            sensor.setId(prev.getId());
            sensor.setName(prev.getName());
            sensor.setDescription(prev.getDescription());
            sensDAO.persist(sensor);
            firePersistEvent(sensor, prev);

            //Logger.trace("Updated Sensor: " + sensor.toLogString());
            executor.execute(() -> broadcastSensorChanged(sensor));
        } else {
            //Sensor does not exit is database
            Logger.warn("Skip persisting ghost sensor " + sensor.toLogString());
        }

        return sensor;
    }

    private void firePersistEvent(ControllableDevice current, ControllableDevice previous) {
        if (!current.equals(previous)) {
            for (PersistedEventListener listener : persistListeners) {
                listener.persisted(current, previous);
            }
        }
    }

    private void broadcastSensorChanged(Sensor sensor) {
        List<SensorListener> sll = sensorListeners.get(sensor.getContactId());

        if (sll != null) {
            for (SensorListener listener : sll) {
                listener.setActive(sensor.isActive());
                //Logger.trace("Listener CID: " + listener.getContactId() + " Active: " + sensor.isActive());
            }
        }
    }

    public void synchronizeSensors() {
        int sensorCount = Integer.decode(System.getProperty("sensorCount", "16"));
        //obtain the current sensorstatus
        List<SensorMessageEvent> sml = controllerService.querySensors(sensorCount);
        for (SensorMessageEvent sme : sml) {
            Sensor s = new Sensor(sme.getContactId(), sme.isNewValue() ? 1 : 0, sme.isOldValue() ? 1 : 0, sme.getDeviceId(), sme.getMillis(), new Date());
            persist(s);
        }
        Logger.debug("Updated " + sml.size() + " Sensor statuses...");
    }

    @Override
    public void notifyAllSensorListeners() {
        List<Sensor> sl = this.sensDAO.findAll();
        //Query all sensors
//        List<SensorMessageEvent> sel = this.controllerService.querySensors(sl.size());
//        for (SensorMessageEvent se : sel) {
//            
//            Sensor s = sensDAO.find(se.getContactId());
//            s.setDeviceId(se.getDeviceId());
//            s.setActive(se.isNewValue());
//            s.setPreviousActive(se.isOldValue());
//            s.setMillis(se.getMillis());
//            s.setLastUpdated(new Date());
//            sensDAO.persist(s);
//        }

//        sl = this.sensDAO.findAll();
        for (Sensor s : sl) {
            broadcastSensorChanged(s);
        }
//        Logger.trace("Refreshed " + sl.size() + " Sensors...");
    }

    private void notifyAccessoiryListeners(SolenoidAccessory accessoiry) {
        AccessoryEvent ae = new AccessoryEvent(accessoiry);
        for (AccessoryListener listener : accessoiryListeners) {
            listener.switched(ae);
        }
    }

    @Override
    public void remove(ControllableDevice entity) {
        if (entity instanceof Sensor) {
            sensDAO.remove((Sensor) entity);
        } else if (entity instanceof Locomotive) {
            locoDAO.remove((Locomotive) entity);
        } else if (entity instanceof Turnout) {
            //Check whether the turnout is linked to a layout tile
            turnoutDAO.remove((Turnout) entity);
        } else if (entity instanceof Signal) {
            //Check whether the signal is linked to a layout tile
            signalDAO.remove((Signal) entity);
        } else if (entity instanceof JCSProperty) {
            this.propDao.remove((JCSProperty) entity);
        }
    }

    @Override
    public List<Signal> getSignals() {
        return this.signalDAO.findAll();
    }

    @Override
    public List<Turnout> getTurnouts() {
        return this.turnoutDAO.findAll();
    }

    @Override
    public Signal getSignal(Integer address) {
        return this.signalDAO.find(address);
    }

    @Override
    public Turnout getTurnout(Integer address) {
        return this.turnoutDAO.find(address);
    }

    @Override
    public Locomotive getLocomotive(Integer address, DecoderType decoderType) {
        return locoDAO.find(address, decoderType);
    }

    @Override
    public Locomotive getLocomotive(BigDecimal id) {
        return locoDAO.findById(id);
    }

    @Override
    public List<Locomotive> getLocomotives() {
        return locoDAO.findAll();
    }

    @Override
    public Locomotive persist(Locomotive locomotive) {
        Locomotive cl = locoDAO.findById(locomotive.getId());
        locoDAO.persist(locomotive);

        if (cl != null && cl.isChanged(locomotive)) {
            LocomotiveEvent event = new LocomotiveEvent(locomotive);
            Logger.debug("Changed loco-> " + event);
            broadcastLocomotiveEvent(event);
        }

        return locomotive;
    }

    private void broadcastLocomotiveEvent(final LocomotiveEvent event) {
        Set<LocomotiveListener> snapshot;
        synchronized (this.locomotiveListeners) {
            snapshot = new HashSet<>(locomotiveListeners);
        }

        for (LocomotiveListener listener : snapshot) {
            listener.changed(event);
        }
    }

    @Override
    public Turnout persist(Turnout turnout) {
        this.turnoutDAO.persist(turnout);
        return turnout;
    }

    @Override
    public Signal persist(Signal signal) {
        this.signalDAO.persist(signal);
        return signal;
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

    private LayoutTile addDependencies(LayoutTile lt) {
        if (lt == null) {
            return null;
        }
        if (lt.getSoacId() != null) {
            if ("TurnoutTile".equals(lt.getTiletype())) {
                Turnout t = turnoutDAO.findById(lt.getSoacId());
                lt.setSolenoidAccessoiry(t);
            }
            if ("SignalTile".equals(lt.getTiletype())) {
                lt.setSolenoidAccessoiry(signalDAO.findById(lt.getSoacId()));
            }
        }
        if (lt.getSensId() != null) {
            lt.setSensor(this.sensDAO.findById(lt.getSensId()));
        }

        if (lt.getLtgrId() != null) {
            lt.setLayoutTileGroup(this.ltgtDao.findById(lt.getLtgrId()));
        }

        return lt;
    }

    @Override
    public Set<LayoutTile> getLayoutTiles() {
        List<LayoutTile> ltl = latiDao.findAll();
        Set<LayoutTile> layoutTiles = new HashSet<>(ltl);

        layoutTiles.forEach((lt) -> {
            addDependencies(lt);
        });
        return layoutTiles;
    }

    @Override
    public LayoutTile getLayoutTile(Integer x, Integer y) {
        LayoutTile layoutTile = this.latiDao.findByXY(x, y);
        return addDependencies(layoutTile);
    }

    @Override
    public LayoutTile persist(LayoutTile layoutTile) {
        if (layoutTile.getLayoutTileGroup() != null) {
            LayoutTileGroup layoutTileGroup = layoutTile.getLayoutTileGroup();
            if (layoutTileGroup.getAddress() != null && !layoutTileGroup.getAddress().equals(0)) {
                ltgtDao.persist(layoutTileGroup);
                layoutTile.setLayoutTileGroup(layoutTileGroup);
            } else {
                layoutTile.setLayoutTileGroup(null);
            }
        }

        latiDao.persist(layoutTile);

        return layoutTile;
    }

    @Override
    public void remove(LayoutTile layoutTile) {
        latiDao.remove(layoutTile);
    }

    @Override
    public void persist(Set<LayoutTile> layoutTiles) {
        //Remove the ones not in the current list...
        List<LayoutTile> cltl = latiDao.findAll();

        for (LayoutTile lt : cltl) {
            if (!layoutTiles.contains(lt)) {
                latiDao.remove(lt);
            }
        }

        for (LayoutTile lt : layoutTiles) {
            if (lt.getId() == null) {
                //store the layouttile but incase check if it exist based on x and y
                LayoutTile ltxy = this.latiDao.findByXY(lt.getX(), lt.getY());
                if (ltxy != null) {
                    lt.setId(ltxy.getId());
                }
            }
            persist(lt);
        }
    }

    @Override
    public List<LayoutTileGroup> getLayoutTileGroups() {
        return this.ltgtDao.findAll();
    }

    @Override
    public LayoutTileGroup getLayoutTileGroup(Integer ltgrNr) {
        return this.ltgtDao.find(ltgrNr);
    }

    @Override
    public LayoutTileGroup getLayoutTileGroup(BigDecimal ltgrId) {
        return this.ltgtDao.findById(ltgrId);
    }

    @Override
    public void persist(LayoutTileGroup layoutTileGroup) {
        this.ltgtDao.persist(layoutTileGroup);
    }

    @Override
    public void remove(LayoutTileGroup layoutTileGroup) {
        this.ltgtDao.remove(layoutTileGroup);
    }

    @Override
    public DeviceInfo getControllerInfo() {
        if (this.controllerInfo == null) {
            controllerInfo = controllerService.getControllerInfo();
        }
        return controllerInfo;
    }

    @Override
    public void addControllerListener(ControllerEventListener listener) {
        if (controllerService != null) {
            controllerService.addControllerEventListener(listener);
        }
    }

    @Override
    public void removeControllerListener(ControllerEventListener listener) {
        if (controllerService != null) {
            controllerService.removeControllerEventListener(listener);
        }
    }

    private void broadcastHeartBeatToggle() {
        Set<HeartBeatListener> snapshot;
        synchronized (heartBeatListeners) {
            snapshot = new HashSet<>(heartBeatListeners);
        }

        for (HeartBeatListener listener : snapshot) {
            listener.toggle();
        }
    }

    @Override
    public void addHeartBeatListener(HeartBeatListener listener) {
        this.heartBeatListeners.add(listener);
    }

    @Override
    public void removeHeartBeatListenerListener(HeartBeatListener listener) {
        this.heartBeatListeners.remove(listener);
    }

    public void feedbackModuleSampleToggle() {
        this.fbToggle = !this.fbToggle;
        executor.execute(() -> broadcastHeartBeatToggle());
    }

    @Override
    public void powerOff() {
        if (this.controllerService != null) {
            this.controllerService.powerOff();
        }
    }

    @Override
    public void powerOn() {
        if (this.controllerService != null) {
            this.controllerService.powerOn();
        }
    }

    @Override
    public boolean isPowerOn() {
        if (trpo == null) {
            trpo = this.trpoDAO.find(1);
        }
        boolean power = false;
        if (this.controllerService != null) {
            power = trpo.isOn();
        }

        return power;
    }

    void handleControllerEvent(ControllerEvent event) {
        Logger.debug(event);
        if (event.isPowerOn()) {
            this.trpo.On();
        } else {
            this.trpo.Off();
        }

        this.trpoDAO.persist(trpo);
        notifyControllerListeners(event);
    }

    private void notifyControllerListeners(ControllerEvent event) {
//    ControllerEvent ce = new ControllerEvent((controllerService != null), this.controllerHost, this.name, isPowerOn(), isFeedbackSourceAvailable(), feedbackSourceName);
//    for (ControllerListener listener : this.controllerListeners) {
//      listener.notify(ce);
//    }
    }

    @Override
    public boolean connect() {
        if (!this.controllerService.isConnected()) {
            return this.controllerService.connect();
        }
        return this.controllerService.isConnected();
    }

    @Override
    public boolean isConnected() {
        return this.controllerService.isConnected();
    }

    @Override
    public void disconnect() {
        this.timer.cancel();
        this.controllerService.disconnect();
    }

    @Override
    public void synchronizeLocomotivesWithController() {
        List<Locomotive> fromCs2 = this.controllerService.getLocomotives();

        for (Locomotive loc : fromCs2) {
            Integer addr = loc.getAddress();
            DecoderType dt = loc.getDecoderType();
            Locomotive dbLoc = this.locoDAO.find(addr, dt);
            if (dbLoc != null) {
                loc.setId(dbLoc.getId());
                Logger.trace("Update " + loc);
            } else {
                Logger.trace("Add " + loc);
            }
            this.locoDAO.persist(loc);
        }
    }

    @Override
    public void synchronizeAccessoriesWithController() {
        List<SolenoidAccessory> sal = this.controllerService.getAccessories();

        for (SolenoidAccessory sa : sal) {
            if (sa.isTurnout()) {
                Turnout t = (Turnout) sa;
                Integer addr = t.getAddress();
                Turnout dbt = this.turnoutDAO.find(addr);
                if (dbt != null) {
                    t.setId(dbt.getId());
                    Logger.trace("Update " + t);
                } else {
                    Logger.trace("Add " + t);
                }
                this.turnoutDAO.persist(t);
            } else if (sa.isSignal()) {
                Signal s = (Signal) sa;
                Integer addr = s.getAddress();
                Signal dbs = this.signalDAO.find(addr);
                if (dbs != null) {
                    s.setId(dbs.getId());
                    if (dbs.getLightImages() > 2) {
                        s.setId2(dbs.getId2());
                        s.setAddress2(dbs.getAddress2());
                    }
                    Logger.trace("Update " + s);
                } else {
                    Logger.trace("Add " + s);
                }
                this.signalDAO.persist(s);
            }
        }
    }

    @Override
    public void synchronizeAccessories() {
        List<Turnout> tl = this.getTurnouts();

        for (Turnout t : tl) {
            this.switchAccessory(t.getValue(), t);
        }

        List<Signal> sl = this.getSignals();
        for (Signal s : sl) {
            this.switchAccessory(s.getValue(), s, true);
        }
    }

    @Override
    public void updateGuiStatuses() {
        updateAccessoryStatuses();

    }

    private void updateAccessoryStatuses() {
        List<AccessoryStatus> asl = controllerService.getAccessoryStatuses();
        List<SolenoidAccessory> accessoiries = new ArrayList<>();

        for (AccessoryStatus as : asl) {
            Integer a = as.getAddress();
            Turnout t = turnoutDAO.find(a);
            if (t != null) {
                AccessoryValue av = as.getAccessoryValue();
                if (!av.equals(t.getValue())) {
                    t.setValue(av);
                    turnoutDAO.persist(t);
                    accessoiries.add(t);
                }
            } else {
                Signal s = signalDAO.find(a);
                if (s != null) {
                    SignalValue sv = as.getSignalValue();
                    if (!sv.equals(s.getSignalValue())) {
                        s.setSignalValue(sv);
                        signalDAO.persist(s);
                        accessoiries.add(s);
                    }
                }
            }
        }
        //notify the changed listeners
        accessoiries.forEach((accessoiry) -> {
            notifyAccessoiryListeners(accessoiry);
        });
    }

    @Override
    public void toggleDirection(Direction direction, Locomotive locomotive) {
        String cs = jcsProperties.getProperty("activeControllerService");
        Logger.debug("New: " + direction + " for: " + locomotive.toLogString() + " Current: " + locomotive.getDirection() + " Decoder: " + locomotive.getDecoderType());

        locomotive.setDirection(direction);

        if ("CS2".equals(cs)) {
            controllerService.toggleDirection(locomotive.getAddress(), locomotive.getDecoderType());
        } else {
            controllerService.toggleDirection(locomotive.getAddress(), locomotive.getDecoderType(), locomotive.isF0());
            if (locomotive.getFunctionCount() > 1) {
                controllerService.setFunctions(locomotive.getAddress(), locomotive.getDecoderType(), locomotive.isF1(), locomotive.isF2(), locomotive.isF3(), locomotive.isF4());
            }
        }

        if (locoDAO.findById(locomotive.getId()) != null) {
            persist(locomotive);
        }
    }

    @Override
    public void changeSpeed(Integer speed, Locomotive locomotive) {
        String cs = jcsProperties.getProperty("activeControllerService");
        Logger.trace("Changing speed to " + speed + " for " + locomotive.toLogString() + " Decoder: " + locomotive.getDecoderType());
        locomotive.setSpeed(speed);

        if ("CS2".equals(cs)) {
            controllerService.setSpeed(locomotive.getAddress(), locomotive.getDecoderType(), locomotive.getSpeed());
        } else {
            controllerService.setSpeedAndFunction(locomotive.getAddress(), locomotive.getDecoderType(), locomotive.isF0(), locomotive.getSpeed());
        }
        if (locoDAO.findById(locomotive.getId()) != null) {
            persist(locomotive);
        }
    }

    @Override
    public void setFunction(Boolean value, Integer functionNumber, Locomotive locomotive) {
        String cs = jcsProperties.getProperty("activeControllerService");
        Logger.trace("Changing Function nr. " + functionNumber + " to " + (value ? "on" : "off") + " for: " + locomotive.toLogString() + " Decoder: " + locomotive.getDecoderType());

        locomotive.setFunctionValue(functionNumber, value);

        if ("CS2".equals(cs)) {
            controllerService.setFunction(locomotive.getAddress(), locomotive.getDecoderType(), functionNumber, locomotive.getFunctionValue(functionNumber));
        } else {
            if (functionNumber == 0) {
                controllerService.setSpeedAndFunction(locomotive.getAddress(), locomotive.getDecoderType(), locomotive.isF0(), locomotive.getSpeed());
            } else {
                controllerService.setFunctions(locomotive.getAddress(), locomotive.getDecoderType(), locomotive.isF1(), locomotive.isF2(), locomotive.isF3(), locomotive.isF4());
            }
        }
        if (locoDAO.findById(locomotive.getId()) != null) {
            persist(locomotive);
        }
    }

    @Override
    public void toggleFunction(Boolean function, Locomotive locomotive) {
        this.setFunction(function, 0, locomotive);
    }

    @Override
    public void toggleF1(Boolean f1, Locomotive locomotive) {
        this.setFunction(f1, 1, locomotive);
    }

    @Override
    public void toggleF2(Boolean f2, Locomotive locomotive) {
        this.setFunction(f2, 2, locomotive);
    }

    @Override
    public void toggleF3(Boolean f3, Locomotive locomotive) {
        this.setFunction(f3, 4, locomotive);
    }

    @Override
    public void toggleF4(Boolean f4, Locomotive locomotive) {
        this.setFunction(f4, 4, locomotive);
    }

    @Override
    public void switchAccessory(AccessoryValue value, SolenoidAccessory accessoiry) {
        switchAccessory(value, accessoiry, false);
    }

    @Override
    public void switchAccessory(AccessoryValue value, SolenoidAccessory accessoiry, boolean useValue2) {
        Logger.trace("Value: " + value + " Accessoiry: " + accessoiry.toLogString() + " useValue2: " + useValue2);

        if (accessoiry.isSignal()) {
            //incase of a signal the SignalValue is set in the signal
            Signal s = (Signal) accessoiry;

            switch (s.getSignalValue()) {
                case Hp0:
                    sendSignalCommand(s.getAddress(), s.getValue(), s.getLightImages() > 2);
                    break;
                case Hp1:
                    sendSignalCommand(s.getAddress(), s.getValue(), s.getLightImages() > 2);
                    break;
                case Hp2:
                    sendSignalCommand(s.getAddress2(), s.getValue2(), s.getLightImages() > 2);
                    break;
                case Hp0Sh1:
                    sendSignalCommand(s.getAddress2(), s.getValue2(), s.getLightImages() > 2);
                    break;
                default:
                    Logger.warn("No command send for " + s.toLogString());
                    break;
            }

            persist(s);

            this.notifyAccessoiryListeners(s);
        } else {
            //turnout or else...
            //ensure the values is also set...
            accessoiry.setValue(value);
            controllerService.switchAccessoiry(accessoiry.getAddress(), accessoiry.getValue());

            if (accessoiry.isTurnout()) {
                persist((Turnout) accessoiry);
                this.notifyAccessoiryListeners((Turnout) accessoiry);
            }
        }
    }

    private void sendSignalCommand(Integer address, AccessoryValue value, boolean repeat) {
        if (value == null || address == null) {
            return;
        }
        if (repeat) {
            for (int i = 0; i < 3; i++) {
                controllerService.switchAccessoiry(address, value);
            }
        } else {
            controllerService.switchAccessoiry(address, value);
        }
    }

    @Override
    public void addMessageListener(CanMessageListener listener) {
        if (this.controllerService != null) {
            this.controllerService.addCanMessageListener(listener);
        }
    }

    @Override
    public void removeMessageListener(CanMessageListener listener) {
        if (this.controllerService != null) {
            this.controllerService.removeCanMessageListener(listener);
        }
    }

    @Override
    public void addSensorListener(SensorListener listener) {
        List<SensorListener> sll = this.sensorListeners.get(listener.getContactId());
        if (sll == null) {
            sll = new ArrayList<>();
        }
        sll.add(listener);
        this.sensorListeners.put(listener.getContactId(), sll);
    }

    @Override
    public void removeFeedbackPortListener(SensorListener listener) {
        List<SensorListener> sll = this.sensorListeners.get(listener.getContactId());
        if (sll != null) {
            sll.remove(listener);
            if (sll.isEmpty()) {
                sensorListeners.remove(listener.getContactId());
            } else {
                sensorListeners.put(listener.getContactId(), sll);
            }
        }
    }

    @Override
    public void addAccessoiryListener(AccessoryListener listener) {
        this.accessoiryListeners.add(listener);
    }

    @Override
    public void removeAccessoiryListener(AccessoryListener listener) {
        this.accessoiryListeners.remove(listener);
    }

    @Override
    public void removeAllAccessoiryListeners() {
        this.accessoiryListeners.clear();
    }

    @Override
    public void addPersistedEventListener(PersistedEventListener listener) {
        this.persistListeners.add(listener);
    }

    @Override
    public void removePersistedEventListener(PersistedEventListener listener) {
        this.persistListeners.remove(listener);
    }

    @Override
    public void addLocomotiveListener(LocomotiveListener listener) {
        this.locomotiveListeners.add(listener);
    }

    @Override
    public void removeLocomotiveListener(LocomotiveListener listener) {
        this.locomotiveListeners.remove(listener);
    }

    @Override
    public void removeAllLocomotiveListeners() {
        this.locomotiveListeners.clear();
    }

    @Override
    public void notifyAllAccessoiryListeners() {

        List<Signal> signals = this.getSignals();
        List<Turnout> turnouts = this.getTurnouts();

        List<SolenoidAccessory> accessoiries = new ArrayList<>();
        accessoiries.addAll(signals);
        accessoiries.addAll(turnouts);

        accessoiries.forEach((accessoiry) -> {
            this.notifyAccessoiryListeners(accessoiry);
        });

    }

    private class SensorMessageEventHandler implements SensorMessageListener {

        private final TrackService trackService;

        SensorMessageEventHandler(TrackService trackService) {
            this.trackService = trackService;
        }

        @Override
        public void onSensorMessage(SensorMessageEvent event) {
            Sensor s = new Sensor(event.getContactId(), event.isNewValue() ? 1 : 0, event.isOldValue() ? 1 : 0, event.getDeviceId(), event.getMillis(), new Date());
            trackService.persist(s);
        }
    }

    private class ControllerWatchDogListener implements HeartbeatListener {

        @Override
        public void sample() {
            try {
                TrackService trackService = TrackServiceFactory.getTrackService();
                if (trackService instanceof H2TrackService) {
                    ((H2TrackService) trackService).feedbackModuleSampleToggle();
                }
            } catch (Exception e) {
                Logger.error(e.getMessage());
            }
        }
    }

    private class ControllerServiceEventListener implements ControllerEventListener {

        @Override
        public void notify(ControllerEvent event) {
            try {
                TrackService trackService = TrackServiceFactory.getTrackService();
                if (trackService != null) {
                    if (trackService instanceof H2TrackService) {
                        Logger.info(event);
                        ((H2TrackService) trackService).handleControllerEvent(event);
                    }
                }
            } catch (Exception e) {
                Logger.error(e.getMessage());
                Logger.trace(e);
            }
        }
    }

    private class UpdateGuiStatusTask extends TimerTask {

        private final TrackService trackService;

        UpdateGuiStatusTask(TrackService trackService) {
            this.trackService = trackService;
        }

        @Override
        public void run() {
            try {
                trackService.updateGuiStatuses();
//                trackService.notifyAllSensorListeners();
            } catch (Exception e) {
                Logger.error(e.getMessage());
                Logger.trace(e);
            }
        }
    }
}
