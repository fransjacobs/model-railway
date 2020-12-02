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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lan.wervel.jcs.controller.ControllerEvent;
import lan.wervel.jcs.controller.ControllerEventListener;
import lan.wervel.jcs.controller.cs2.DeviceInfo;
import lan.wervel.jcs.controller.ControllerService;
import lan.wervel.jcs.controller.cs2.events.CanMessageListener;
import lan.wervel.jcs.entities.ControllableDevice;
import lan.wervel.jcs.entities.FeedbackModule;
import lan.wervel.jcs.entities.JCSProperty;
import lan.wervel.jcs.entities.LayoutTile;
import lan.wervel.jcs.entities.LayoutTileGroup;
import lan.wervel.jcs.entities.Locomotive;
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
import lan.wervel.jcs.feedback.FeedbackEvent;
import lan.wervel.jcs.feedback.FeedbackEventListener;
import lan.wervel.jcs.feedback.FeedbackPortListener;
import lan.wervel.jcs.feedback.FeedbackService;
import lan.wervel.jcs.trackservice.dao.FeedbackModuleDAO;
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
import lan.wervel.jcs.feedback.HeartbeatListener;
import lan.wervel.jcs.trackservice.dao.LayoutTileDAO;
import lan.wervel.jcs.trackservice.dao.LayoutTileGroupDAO;

public class H2TrackService implements TrackService {

    private final FeedbackModuleDAO femoDAO;
    private final LocomotiveDAO locoDAO;
    private final TurnoutDAO turnoutDAO;
    private final SignalDAO signalDAO;
    private final TrackPowerDAO trpoDAO;

    //private final DriveWayDAO drwaDAO;
    // private final AccessorySettingDAO acseDao;
    private final LayoutTileDAO latiDao;
    private final LayoutTileGroupDAO ltgtDao;
    private final JCSPropertiesDAO propDao;

    //private final Map<String, ClientInfo> clients;
    private FeedbackService feedbackService;
    private ControllerService controllerService;

    private final ExecutorService executor;

    private final List<FeedbackPortListener> feedbackPortListeners;
    private final List<AccessoryListener> accessoiryListeners;
    private final List<LocomotiveListener> locomotiveListeners;
    private final List<HeartBeatListener> heartBeatListeners;
    private final List<PersistedEventListener> persistListeners;

    private boolean fbToggle = false;

    private TrackPower trpo;

    private final Properties jcsProperties;

    private DeviceInfo controllerInfo;

    public H2TrackService() {
        this(true, true);
    }

    private H2TrackService(boolean aquireFeedbackService, boolean aquireControllerService) {
        propDao = new JCSPropertiesDAO();
        jcsProperties = new Properties();

        femoDAO = new FeedbackModuleDAO();
        locoDAO = new LocomotiveDAO();
        turnoutDAO = new TurnoutDAO();
        signalDAO = new SignalDAO();
        trpoDAO = new TrackPowerDAO();
        //drwaDAO = new DriveWayDAO();
        //acseDao = new AccessorySettingDAO();
        latiDao = new LayoutTileDAO();
        ltgtDao = new LayoutTileGroupDAO();
        executor = Executors.newCachedThreadPool();

        feedbackPortListeners = new LinkedList<>();
        accessoiryListeners = new ArrayList<>();
        locomotiveListeners = new ArrayList<>();
        heartBeatListeners = new ArrayList<>();
        persistListeners = new ArrayList<>();

        //clients = new HashMap<>();
        retrieveJCSProperties();
        trpo = trpoDAO.find(1);

        if (aquireControllerService) {
            aquireControllerService();
        }

        Logger.debug(controllerService != null ? "Aquired " + controllerService.getClass().getSimpleName() : "Could not aquire a Controller Service!");

        if (aquireFeedbackService) {
            aquireFeedbackService();
        }
        Logger.debug(feedbackService != null ? "Aquired " + feedbackService.getClass().getSimpleName() : "Could not aquire a Feedback Service!");
    }

    private void retrieveJCSProperties() {
        List<JCSProperty> props = this.propDao.findAll();

        props.forEach(p -> {
            this.jcsProperties.setProperty(p.getKey(), p.getValue());
        });
    }

    private void aquireFeedbackService() {
        String S88Demo = jcsProperties.getProperty("S88-demo");
        String S88Remote = jcsProperties.getProperty("S88-remote");
        String S88CS2 = jcsProperties.getProperty("S88-CS2");
        String activeFeedbackService = jcsProperties.getProperty("activeFeedbackService");

        //Check if the Controller is also a feedback service
        if (this.controllerService instanceof FeedbackService) {
            Logger.trace("Using Controller as Feedback Service...");
            this.feedbackService = (FeedbackService) this.controllerService;
        }

        String feedbackServiceImpl;

        if (feedbackService == null) {
            switch (activeFeedbackService) {
                case "S88-remote":
                    feedbackServiceImpl = S88Remote;
                    break;
                case "S88-CS2":
                    feedbackServiceImpl = S88CS2;
                    break;
                default:
                    feedbackServiceImpl = S88Demo;
                    break;
            }

            Logger.trace("ActiveFeedbackService: " + activeFeedbackService);

            Logger.trace("Obtaining an " + feedbackServiceImpl + " instance...");
            try {
                FeedbackService fs = (FeedbackService) Class.forName(feedbackServiceImpl).getDeclaredConstructor().newInstance();
                this.feedbackService = fs;
            } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex2) {
                Logger.error("Can't instantiate a '" + feedbackServiceImpl + "' " + ex2.getMessage());
            }
        }

        feedbackService.addFeedbackEventListener(new FeedbackServiceEventListener());
        feedbackService.addHeartbeatListener(new FeedbackServiceSampleListener());
    }

    private void aquireControllerService() {
        String m6050Local = jcsProperties.getProperty("M6050-local");
        String m6050Remote = jcsProperties.getProperty("M6050-remote");
        String m6050Demo = jcsProperties.getProperty("M6050-demo");
        String cs2 = jcsProperties.getProperty("CS2");
        String activeControllerService = jcsProperties.getProperty("activeControllerService");

        Logger.debug("ActiveControllerService: " + activeControllerService);

        String controllerImpl;

        switch (activeControllerService) {
            case "M6050-remote":
                controllerImpl = m6050Remote;
                break;
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

        controllerService.addControllerEventListener(new ControllerServiceEventListener());
        controllerInfo = controllerService.getControllerInfo();
    }

    @Override
    public List<FeedbackModule> getFeedbackModules() {
        return femoDAO.findAll();
    }

    @Override
    public FeedbackModule getFeedbackModule(Integer moduleNumber
    ) {
        return femoDAO.find(moduleNumber);
    }

    @Override
    public FeedbackModule persist(FeedbackModule feedbackModule
    ) {
        FeedbackModule prev = femoDAO.find(feedbackModule.getModuleNumber());
        femoDAO.persist(feedbackModule);

        fireFeedbackModuleChanged(feedbackModule, prev);

        firePersistEvent(feedbackModule, prev);
        return feedbackModule;
    }

    private void firePersistEvent(ControllableDevice current, ControllableDevice previous) {
        if (!current.equals(previous)) {
            for (PersistedEventListener listener : persistListeners) {
                listener.persisted(current, previous);
            }
        }
    }

    private void fireFeedbackModuleChanged(FeedbackModule curr, FeedbackModule prev) {
        executor.execute(() -> broadcastFeedbackModuleChanged(curr, prev));
    }

    private void broadcastFeedbackModuleChanged(FeedbackModule curr, FeedbackModule prev) {
        if (!curr.equals(prev)) {
            Set<FeedbackPortListener> snapshot;
            synchronized (this.feedbackPortListeners) {
                snapshot = new HashSet<>(feedbackPortListeners);
            }

            for (FeedbackPortListener listener : snapshot) {
                boolean prevValue = prev.getPortValue(listener.getPort());
                boolean currValue = curr.getPortValue((listener.getPort()));
                if (curr.getModuleNumber() == listener.getModuleNumber() && prevValue != currValue) {
                    listener.setValue(currValue);
                }
            }
        }
    }

    @Override
    public void notifyAllFeedbackListeners() {
//        if (feedbackService != null) {
//            List<FeedbackModule> currFbml = feedbackService.getFeedbackModules();
//            if (currFbml != null) {
//                for (FeedbackModule fbm : currFbml) {
//                    FeedbackModule cFbm = this.getFeedbackModule(fbm.getModuleNumber());
//                    if (cFbm != null) {
//                        cFbm.setResponse(fbm.getResponse());
//                        this.persist(cFbm);
//                    }
//                }
//
//                List<FeedbackModule> fbml = getFeedbackModules();
//
//                Set<FeedbackPortListener> snapshot;
//                synchronized (feedbackPortListeners) {
//                    snapshot = new HashSet<>(feedbackPortListeners);
//                }
//
//                for (FeedbackModule fm : fbml) {
//                    for (FeedbackPortListener fbpl : snapshot) {
//                        if (fm.getModuleNumber() == fbpl.getModuleNumber()) {
//                            fbpl.setValue(fm.getPortValue(fbpl.getPort()));
//                        }
//                    }
//                }
//                Logger.trace("Refreshed " + feedbackPortListeners.size() + " FeedbackPortListeners...");
//            }
//        }
    }

    private void notifyAccessoiryListeners(SolenoidAccessory accessoiry) {
        AccessoryEvent ae = new AccessoryEvent(accessoiry);
        for (AccessoryListener listener : accessoiryListeners) {
            listener.switched(ae);
        }
    }

    @Override
    public void remove(ControllableDevice entity) {
        if (entity instanceof FeedbackModule) {
            femoDAO.remove((FeedbackModule) entity);
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
    public Locomotive getLocomotive(Integer address) {
        return locoDAO.find(address);
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
        if (lt.getFemoId() != null) {
            lt.setFeedbackModule(this.femoDAO.findById(lt.getFemoId()));
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
        this.controllerService.disconnect();
    }

    @Override
    public void synchronizeLocomotives() {
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
    public void addFeedbackPortListener(FeedbackPortListener listener) {
        this.feedbackPortListeners.add(listener);
    }

    @Override
    public void removeFeedbackPortListener(FeedbackPortListener listener) {
        this.feedbackPortListeners.remove(listener);
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

        Logger.debug("#Listeners: " + locomotiveListeners.size());
    }

    @Override
    public void removeLocomotiveListener(LocomotiveListener listener) {
        this.locomotiveListeners.remove(listener);

        Logger.debug("#Listeners: " + locomotiveListeners.size());
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

    private class FeedbackServiceEventListener implements FeedbackEventListener {

        @Override
        public void notify(FeedbackEvent event) {
            try {
                TrackService trackService = TrackServiceFactory.getTrackService();
                if (trackService != null) {
                    FeedbackModule fmc;
                    if (event.getContactId() > 0) {
                        Integer mn = FeedbackModule.contactIdToModuleNr(event.getContactId());
                        int port = FeedbackModule.contactIdToPort(event.getContactId());
                        fmc = trackService.getFeedbackModule(mn);
                        if (fmc != null) {
                            fmc.setPortValue(event.isNewValue(), port);
                            trackService.persist(fmc);
                            Logger.trace(fmc.toLogString());
                        }
                    } else {
                        Integer[] response = event.getResponse();
                        fmc = trackService.getFeedbackModule(event.getModuleNumber());
                        if (fmc != null) {
                            Integer[] rresponse = fmc.getResponse();
                            if (response.length == rresponse.length) {
                                if (response[0].equals(rresponse[0]) && response[1].equals(rresponse[1])) {
                                } else {
                                    fmc.setResponse(response);
                                    trackService.persist(fmc);
                                    Logger.trace(fmc.toLogString());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error(e.getMessage());
                Logger.trace(e);
            }
        }
    }

    private class FeedbackServiceSampleListener implements HeartbeatListener {

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

}
