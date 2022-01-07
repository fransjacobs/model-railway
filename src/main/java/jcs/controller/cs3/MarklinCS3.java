/*
 * Copyright (C) 2020 Frans Jacobs.
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
package jcs.controller.cs3;

import java.awt.Image;
import jcs.controller.cs3.events.SensorMessageEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.controller.ControllerEvent;
import jcs.controller.ControllerEventListener;
import jcs.controller.ControllerService;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.can.CanMessageFactory;
import jcs.controller.cs3.can.MarklinCan;
import static jcs.controller.cs3.can.MarklinCan.FUNCTION_OFF;
import static jcs.controller.cs3.can.MarklinCan.FUNCTION_ON;
import jcs.controller.cs3.events.CanMessageEvent;
import jcs.controller.cs3.events.CanMessageListener;
import jcs.controller.cs3.http.AccessoryParser;
import jcs.controller.cs3.http.LocomotiveBeanParser;
import jcs.controller.cs3.net.Connection;
import jcs.controller.cs3.net.CS3ConnectionFactory;
import jcs.controller.cs3.net.HTTPConnection;
import jcs.entities.SolenoidAccessory;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Direction;
import jcs.entities.enums.DecoderType;
import jcs.controller.HeartbeatListener;
import jcs.controller.cs3.events.SensorMessageListener;
import jcs.controller.cs3.http.DeviceParser;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class MarklinCS3 implements ControllerService {

    private Connection connection;
    private boolean connected = false;

    private final List<ControllerEventListener> controllerEventListeners;

    private final List<HeartbeatListener> heartbeatListeners;
    private final List<SensorMessageListener> sensorMessageEventListeners;

    private final Timer timer;
    private boolean startTimer;

    private final ExecutorService executor;
    private DeviceInfo deviceInfo;

    private static final long DELAY = 0L;

    public MarklinCS3() {
        this(true);
    }

    MarklinCS3(boolean useTimer) {
        controllerEventListeners = new ArrayList<>();
        sensorMessageEventListeners = new ArrayList<>();
        heartbeatListeners = new ArrayList<>();
        startTimer = useTimer;
        timer = new Timer("Idle", true);
        executor = Executors.newCachedThreadPool();
        connect();
    }

    @Override
    public PowerStatus powerOff() {
        PowerStatus ps = new PowerStatus(connection.sendCanMessage(CanMessageFactory.stop()));
        return ps;
    }

    @Override
    public PowerStatus powerOn() {
        PowerStatus ps = new PowerStatus(connection.sendCanMessage(CanMessageFactory.go()));
        return ps;
    }

    public PowerStatus getPowerStatus() {
        CanMessage m = connection.sendCanMessage(CanMessageFactory.powerStatus());
        PowerStatus ps = new PowerStatus(m);
        return ps;
    }

    @Override
    public boolean isPowerOn() {
        if (connected) {
            return getPowerStatus().isPowerOn();
        } else {
            return false;
        }
    }

    @Override
    public final boolean connect() {
        if (!connected) {
            Logger.debug("Connecting to CS3...");
            this.connection = CS3ConnectionFactory.getConnection();
            this.connected = this.connection != null;
        }

        if (connected) {
            Logger.debug("Obtaining controller device information...");
            getControllerInfo();
            //int gfpUid = Integer.getInteger(deviceInfo.getGfpUid());
            long gfpUid = Long.parseLong(deviceInfo.getGfpUid(), 16);

            int gfpUidInt = (int) gfpUid;

            //int guiUid = Integer.getInteger(deviceInfo.getGuiUid());
            long guiUid = Integer.getInteger(deviceInfo.getGuiUid(), 16);

            CanMessageFactory.setGFPUid((int) gfpUid);
            CanMessageFactory.setGUIUid((int) guiUid);
            PowerStatus ps = getPowerStatus();

            Logger.info("Connected with " + deviceInfo.getDescription() + " " + deviceInfo.getCatalogNumber() + " Serial# " + deviceInfo.getSerialNumber() + ". Track Power is " + (ps.isPowerOn() ? "On" : "Off") + ". GFPUID : " + deviceInfo.getGfpUid() + ". GUIUID : " + deviceInfo.getGuiUid());
            Logger.trace("Track Power is " + (ps.isPowerOn() ? "On" : "Off"));

            addCanMessageListener(new ExtraMessageListener(this));

            executor.execute(() -> notifyControllerEventListeners(new ControllerEvent(ps.isPowerOn(), connected)));
        }

        // Finally start the heartbeat timer which will takes care of the feedback 
        if (startTimer && connected) {
            //Start the idle task to listen
            int interval = Integer.decode(System.getProperty("idle-interval", "100"));
            timer.scheduleAtFixedRate(new IdleTask(this), DELAY, interval);
            Logger.trace("Started hearbeat task with an interval of " + interval + " ms...");
        } else {
            Logger.info("Hearbeat time is NOT started! " + (!connected ? "NOT Connected" : (!startTimer ? "Timer is off" : "")));
        }
        return connected;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void disconnect() {
        try {
            stopIdleTask();
            final Connection conn = this.connection;
            connected = false;
            this.connection = null;

            if (conn != null) {
                synchronized (conn) {
                    conn.sendCanMessage(CanMessageFactory.stop());
                    wait200ms();
                    conn.close();
                    CanMessageFactory.setGFPUid(0);
                    CanMessageFactory.setGUIUid(0);
                }
            }
            executor.shutdown();
        } catch (Exception ex) {
            Logger.error(ex);
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    private int getLocoAddres(int address, DecoderType decoderType) {
        int locoAddress;
        switch (decoderType) {
            case MFX:
                locoAddress = 0x4000 + address;
                break;
            case DCC:
                locoAddress = 0xC000 + address;
                break;
            case SX1:
                locoAddress = 0x0800 + address;
                break;
            case MM:
                locoAddress = address;
                break;
            default:
                locoAddress = address;
                break;
        }

        return locoAddress;
    }

    /**
     * Compatibility with 6050
     *
     * @param address the locomotive address
     * @param protocol
     * @param function the value of the function (F0)
     */
    @Override
    public void toggleDirection(int address, DecoderType protocol, boolean function) {
        toggleDirection(address, protocol);
        setFunction(address, protocol, 0, function);
    }

    @Override
    public void toggleDirection(int address, DecoderType decoderType) {
        int la = getLocoAddres(address, decoderType);
        CanMessage msg = this.connection.sendCanMessage(CanMessageFactory.queryDirection(la));
        DirectionInfo di = new DirectionInfo(msg);
        Logger.trace(di);
        Direction direction = di.getDirection();
        direction = direction.toggle();

        setDirection(address, decoderType, direction);
    }

    @Override
    public void setDirection(int address, DecoderType decoderType, Direction direction) {
        int la = getLocoAddres(address, decoderType);
        Logger.trace("Setting direction to: " + direction + " for loc address: " + la + " Decoder: " + decoderType);
        this.connection.sendCanMessage(CanMessageFactory.setDirection(la, direction.getCS2Value()));
    }

    public DirectionInfo getDirection(int address, DecoderType decoderType) {
        int la = getLocoAddres(address, decoderType);
        DirectionInfo di = new DirectionInfo(connection.sendCanMessage(CanMessageFactory.queryDirection(la)));
        Logger.trace(di);
        return di;
    }

    /**
     * Compatibility 6050
     *
     * @param address the locomotive address
     * @param decoderType
     * @param function the function 0 value
     * @param speed the speed
     */
    @Override
    public void setSpeedAndFunction(int address, DecoderType decoderType, boolean function, int speed) {
        setSpeed(address, decoderType, speed);
        setFunction(address, decoderType, 0, function);
    }

    @Override
    public void setSpeed(int address, DecoderType decoderType, int speed) {
        int la = getLocoAddres(address, decoderType);
        Logger.trace("Setting speed to: " + speed + " for loc address: " + la + " Decoder: " + decoderType);

        //Calculate the speed??
        this.connection.sendCanMessage(CanMessageFactory.setLocSpeed(la, speed));
    }

    @Override
    public void setFunction(int address, DecoderType decoderType, int functionNumber, boolean flag) {
        int value = flag ? FUNCTION_ON : FUNCTION_OFF;
        int la = getLocoAddres(address, decoderType);
        this.connection.sendCanMessage(CanMessageFactory.setFunction(la, functionNumber, value));
    }

    /**
     * Compatibility with 6050
     *
     * @param address address of the locomotive
     * @param decoderType the locomotive decoder protocol
     * @param f1 value function 1
     * @param f2 value function 2
     * @param f3 value function 3
     * @param f4 value function 4
     */
    @Override
    public void setFunctions(int address, DecoderType decoderType, boolean f1, boolean f2, boolean f3, boolean f4) {
        setFunction(address, decoderType, 1, f1);
        setFunction(address, decoderType, 2, f2);
        setFunction(address, decoderType, 3, f3);
        setFunction(address, decoderType, 4, f4);
    }

    @Override
    public void switchAccessoiry(int address, AccessoryValue value) {
        executor.execute(() -> switchAccessoiryOnOff(address, value));
    }

    private void wait200ms() {
        pause(200L);
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.error(ex);
        }
    }

    private void switchAccessoiryOnOff(int address, AccessoryValue value) {
        this.connection.sendCanMessage(CanMessageFactory.switchAccessory(address, value, true));
        //TODO dynamic setting of time or queue it
        wait200ms();
        this.connection.sendCanMessage(CanMessageFactory.switchAccessory(address, value, false));
    }

    public List<PingResponse> membersPing() {
        CanMessage msg = connection.sendCanMessage(CanMessageFactory.getMemberPing());
        List<CanMessage> rl = msg.getResponses();
        List<PingResponse> prl = new ArrayList<>();
        for (CanMessage resp : rl) {
            PingResponse pr = new PingResponse(resp);
            prl.add(pr);
        }

        return prl;
    }

    @Override
    public List<LocomotiveBean> getLocomotives() {
        HTTPConnection httpCon = CS3ConnectionFactory.getHTTPConnection();
        String lokomotiveCs2 = httpCon.getLocomotivesFile();
        LocomotiveBeanParser lp = new LocomotiveBeanParser();
        return lp.parseLocomotivesFile(lokomotiveCs2);
    }

    @Override
    public List<SolenoidAccessory> getAccessories() {
        HTTPConnection httpCon = CS3ConnectionFactory.getHTTPConnection();
        String magnetartikelCs2 = httpCon.getAccessoriesFile();
        AccessoryParser ap = new AccessoryParser();
        return ap.parseAccessoryFile(magnetartikelCs2);
    }

    @Override
    public Image getLocomotiveImage(String icon) {
        HTTPConnection httpCon = CS3ConnectionFactory.getHTTPConnection();
        Image locIcon = httpCon.getLocomotiveImage(icon);
        return locIcon;
    }

    @Override
    public List<AccessoryStatus> getAccessoryStatuses() {
        //This piece does not seem to work with a CS3
        //TO sort out how to get the statuses

        //Update the file by sending a configRequest first
//        CanMessage msg = connection.sendCanMessage(CanMessageFactory.requestConfig("magstat"));
//        //give it some time to process
//        pause(100L);
//        HTTPConnection httpCon = CS3ConnectionFactory.getHTTPConnection();
//        String accessoryStatuses = httpCon.getAccessoryStatusesFile();
//        AccessoryParser ap = new AccessoryParser();
//        return ap.parseAccessoryStatusFile(accessoryStatuses);
        //STUB
        return Collections.EMPTY_LIST;
    }

    @Override
    public DeviceInfo getControllerInfo() {
        if (deviceInfo == null) {
            HTTPConnection httpCon = CS3ConnectionFactory.getHTTPConnection();
            String deviceFile = httpCon.getDeviceFile();
            DeviceParser dp = new DeviceParser();
            deviceInfo = dp.parseAccessoryFile(deviceFile);
            long gfpUid = Long.parseLong(deviceInfo.getGfpUid(), 16);

            deviceInfo.updateFromStatusMessageResponse(connection.sendCanMessage(CanMessageFactory.statusConfig((int) gfpUid)));
            String deviceHostName = connection.getCs2Address().getHostName();
            deviceInfo.setDeviceHostName(deviceHostName);
            deviceInfo.setMaxFunctions(32);
            deviceInfo.setSupportMM(true);
            deviceInfo.setSupportMFX(true);
            deviceInfo.setSupportDCC(true);
            deviceInfo.setSupportSX1(true);
        }
        return deviceInfo;
    }

    @Override
    public void addControllerEventListener(ControllerEventListener listener) {
        this.controllerEventListeners.add(listener);
    }

    @Override
    public void removeControllerEventListener(ControllerEventListener listener) {
        this.controllerEventListeners.remove(listener);
    }

    @Override
    public void notifyAllControllerEventListeners() {
        Logger.info("Current Controller Power Status: " + (isPowerOn() ? "On" : "Off") + "...");
        executor.execute(() -> notifyControllerEventListeners(new ControllerEvent(isPowerOn(), isConnected())));
    }

    @Override
    public void addHeartbeatListener(HeartbeatListener listener) {
        synchronized (heartbeatListeners) {
            this.heartbeatListeners.add(listener);
        }
    }

    @Override
    public void removeHeartbeatListener(HeartbeatListener listener) {
        synchronized (heartbeatListeners) {
            this.heartbeatListeners.remove(listener);
        }
    }

    @Override
    public void removeAllHeartbeatListeners() {
        synchronized (heartbeatListeners) {
            this.heartbeatListeners.clear();
        }
    }

    @Override
    public void addCanMessageListener(CanMessageListener listener) {
        if (this.connection != null) {
            this.connection.addCanMessageListener(listener);
        }
    }

    @Override
    public void removeCanMessageListener(CanMessageListener listener) {
        if (this.connection != null) {
            this.connection.addCanMessageListener(listener);
        }
    }

    @Override
    public void addSensorMessageListener(SensorMessageListener listener) {
        this.sensorMessageEventListeners.add(listener);
    }

    @Override
    public void removeSensorMessageListener(SensorMessageListener listener) {
        this.sensorMessageEventListeners.remove(listener);
    }

    public String getDeviceIp() {
        return CS3ConnectionFactory.getInstance().getDeviceIp();
    }

    public SensorMessageEvent querySensor(int contactId) {
        CanMessage msg = connection.sendCanMessage(CanMessageFactory.querySensor(contactId));
        CanMessage resp = msg.getResponse();
        if (resp.isResponseFor(msg)) {
            return new SensorMessageEvent(msg);
        } else {
            return null;
        }
    }

    //To listen on the the TCP port for data
    private void sendIdle() {
        connection.sendCanMessage(null);
    }

    @Override
    public List<SensorMessageEvent> querySensors(int sensorCount) {
        Logger.trace("Query Contacts from 1 until: " + sensorCount);

        List<SensorMessageEvent> sel = new ArrayList<>(sensorCount);

        int fromContactId = 1;
        int toContactId = sensorCount;
        CanMessage msg = connection.sendCanMessage(CanMessageFactory.querySensors(fromContactId, toContactId));

        List<CanMessage> responses = msg.getResponses();
        Logger.trace("Got " + responses.size() + " responses...");

        for (CanMessage rm : responses) {
            SensorMessageEvent se = new SensorMessageEvent(rm);
            sel.add(se);
        }

        return sel;
    }

    private void notifyControllerEventListeners(ControllerEvent event) {
        Set<ControllerEventListener> snapshot;
        synchronized (controllerEventListeners) {
            if (controllerEventListeners.isEmpty()) {
                snapshot = new HashSet<>();
            } else {
                snapshot = new HashSet<>(controllerEventListeners);
            }
        }

        for (ControllerEventListener listener : snapshot) {
            listener.notify(event);
        }
    }

    private void notifySensorMessageEventListeners(SensorMessageEvent event) {
        for (SensorMessageListener listener : sensorMessageEventListeners) {
            listener.onSensorMessage(event);
        }
    }

    public void stopIdleTask() {
        if (timer != null) {
            timer.purge();
            timer.cancel();
            //running = false;
            this.heartbeatListeners.clear();
            Logger.trace("Connection Idle Task cancelled...");
        }
    }

    private class IdleTask extends TimerTask {

        private final MarklinCS3 controller;
        private boolean powerOn;
        private boolean toggle = false;

        private int cnt = 0;

        IdleTask(MarklinCS3 cs2Controller) {
            controller = cs2Controller;
        }

        @Override
        public void run() {
            try {
                controller.sendIdle();

//                if (cnt % 4 == 0) {
//                    boolean power = controller.getPowerStatus().isPowerOn();
//                    if (power != powerOn) {
//                        powerOn = power;
//                        //Logger.trace("Power Status changed to " + (powerOn ? "On" : "Off"));
//                        ControllerEvent ce = new ControllerEvent(powerOn, this.controller.isConnected());
//                        controller.notifyControllerEventListeners(ce);
//                    }
//                    cnt = 0;
//                } else {
                //List<PingResponse> prl = membersPing();
//                }
//                cnt++;
                if (cnt % 5 == 0) {
                    Set<HeartbeatListener> snapshot;
                    synchronized (controller.heartbeatListeners) {
                        snapshot = new HashSet<>(controller.heartbeatListeners);
                    }

                    for (HeartbeatListener listener : snapshot) {
                        listener.sample();
                    }
                    toggle = !toggle;
                }

                cnt++;
            } catch (Exception e) {
                Logger.error(e.getMessage());
                Logger.trace(e);
            }
        }
    }

    private class ExtraMessageListener implements CanMessageListener {

        private final MarklinCS3 controller;

        ExtraMessageListener(MarklinCS3 cs2Controller) {
            controller = cs2Controller;
        }

        @Override
        public void onCanMessage(CanMessageEvent canEvent) {
            CanMessage msg = canEvent.getCanMessage();
            int cmd = msg.getCommand();

            switch (cmd) {
                case MarklinCan.S88_EVENT_RESPONSE:
                    SensorMessageEvent sme = new SensorMessageEvent(msg);
                    Logger.trace(sme);
                    controller.notifySensorMessageEventListeners(sme);
                    break;
                default:
                    //Logger.trace("Message: " + msg);
                    break;
            }
        }
    }

    public static void main(String[] a) {
        //Configurator.
        //        currentConfig().formatPattern("{date:yyyy-MM_DIL-dd HH:mm:ss.SSS} [{thread}] {class_name}.{method}() {level}: {message}").
        //        activate();

        MarklinCS3 cs2 = new MarklinCS3(false);

        if (cs2.isConnected()) {
            //cs2.powerOn();

//            List<SensorMessageEvent> sml = cs2.querySensors(48);
//            for (SensorMessageEvent sme : sml) {
//                Sensor s = new Sensor(sme.getContactId(), sme.isNewValue() ? 1 : 0, sme.isOldValue() ? 1 : 0, sme.getDeviceId(), sme.getMillis(), new Date());
//                Logger.debug(s.toLogString());
//            }
            List<AccessoryStatus> asl = cs2.getAccessoryStatuses();
            for (AccessoryStatus as : asl) {
                Logger.debug(as.toString());
            }

//            for (int i = 0; i < 30; i++) {
//                cs2.sendIdle();
//                pause(500);
//            }
//            Logger.debug("Sending  member ping\n");
//            List<PingResponse> prl = cs2.membersPing();
//            //Logger.info("Query direction of loc 12");
//            //DirectionInfo info = cs2.getDirection(12, DecoderType.MM);
//            Logger.debug("got " + prl.size() + " responses");
//            for (PingResponse pr : prl) {
//                Logger.debug(pr);
//            }
//            List<SensorMessageEvent> sel = cs2.querySensors(48);
//
//            for (SensorMessageEvent se : sel) {
//                Logger.debug(se.toString());
//            }
//            FeedbackModule fm2 = new FeedbackModule(2);
//            cs2.queryAllPorts(fm2);
//            Logger.debug(fm2.toLogString());
            //cs2.querySensor(1);
        }

        //PingResponse pr2 = cs2.memberPing();
        //Logger.info("Query direction of loc 12");
        //DirectionInfo info = cs2.getDirection(12, DecoderType.MM);
        Logger.debug("DONE");
        cs2.pause(5L);
    }
    //for (int i = 0; i < 16; i++) {
    //    cs2.requestFeedbackEvents(i + 1);
    //}

}
