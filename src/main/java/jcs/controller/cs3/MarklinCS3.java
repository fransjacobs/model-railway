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

import jcs.controller.cs3.devices.CS3Device;
import java.awt.Image;
import jcs.controller.cs3.events.SensorMessageEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.controller.ControllerEvent;
import jcs.controller.ControllerEventListener;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.can.CanMessageFactory;
import jcs.controller.cs3.can.MarklinCan;
import static jcs.controller.cs3.can.MarklinCan.FUNCTION_OFF;
import static jcs.controller.cs3.can.MarklinCan.FUNCTION_ON;
import jcs.controller.cs3.events.CanMessageEvent;
import jcs.controller.cs3.events.CanMessageListener;
import jcs.controller.cs3.http.AccessoryBeanParser;
import jcs.controller.cs3.http.LocomotiveBeanParser;
import jcs.controller.cs3.net.Connection;
import jcs.controller.cs3.net.ControllerConnectionFactory;
import jcs.controller.cs3.net.HTTPConnection;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Direction;
import jcs.entities.enums.DecoderType;
import jcs.controller.HeartbeatListener;
import jcs.controller.cs3.events.SensorMessageListener;
import jcs.controller.cs3.http.CS3DeviceParser;
import jcs.controller.cs3.http.SvgIconToPngIconConverter;
import jcs.entities.AccessoryBean;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;
import jcs.controller.MarklinController;
import jcs.controller.cs3.http.DevicesParser;

/**
 *
 * @author Frans Jacobs
 */
public class MarklinCS3 implements MarklinController {

    private Connection connection;
    private boolean connected = false;

    private final List<ControllerEventListener> controllerEventListeners;

    private final List<HeartbeatListener> heartbeatListeners;
    private final List<SensorMessageListener> sensorMessageEventListeners;

    private final Timer timer;
    private boolean startTimer;

    private final ExecutorService executor;
    private CS3Device deviceInfo;

    private static final long DELAY = 0L;

    public MarklinCS3() {
        this(true);
    }

    //For testing
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
    public ControllerStatus power(boolean on) {
        ControllerStatus ps;
        if (this.connected) {
            CanMessage cm;
            if (on) {
                cm = CanMessageFactory.go();
            } else {
                cm = CanMessageFactory.stop();
            }
            ps = new ControllerStatus(connection.sendCanMessage(cm));
        } else {
            ps = new ControllerStatus(false, false, new int[0], -1);
        }
        return ps;
    }

    @Override
    public ControllerStatus getControllerStatus() {
        ControllerStatus ps;
        if (this.connected) {
            CanMessage m = connection.sendCanMessage(CanMessageFactory.powerStatus());
            ps = new ControllerStatus(m);
        } else {
            ps = new ControllerStatus(false, false, new int[0], -1);

        }
        return ps;
    }

    @Override
    public boolean isPower() {
        return getControllerStatus().isPower();
    }

    @Override
    public final boolean connect() {
        if (!connected) {
            Logger.debug("Connecting to CS3...");
            this.connection = ControllerConnectionFactory.getConnection();
            this.connected = this.connection != null;
        }

        if (connected) {
            Logger.trace("Obtaining device information...");
            getControllerInfo();

            getDeviceInfo();

            Logger.trace("CS3 uid: "+ this.deviceInfo.getCs3().getUid());
            Logger.trace("Article: "+ this.deviceInfo.getGfp().getArticleNumber());
            Logger.trace("CS3: " + this.deviceInfo.getCs3().getName());
            Logger.trace("GFP uid: "  + this.deviceInfo.getGfp().getUid());
            Logger.trace("GFP version: "  + this.deviceInfo.getGfp().getVersion());
            Logger.trace("GFP Serial: " + this.deviceInfo.getGfp().getSerial());
            Logger.trace("LinkSxx uid: " + this.deviceInfo.getLinkSxx().getUid());
            Logger.trace("LinkSxx id: " + this.deviceInfo.getLinkSxx().getIdentifier());
            Logger.trace("LinkSxx serial: " + this.deviceInfo.getLinkSxx().getSerialNumber());
            Logger.trace("LinkSxx version: " + this.deviceInfo.getLinkSxx().getVersion());
            Logger.trace("LinkSxx sensors: "+this.deviceInfo.getLinkSxx().getTotalSensors());
            
            Logger.trace("Track Current "+ this.deviceInfo.getGfp().getTrackCurrent()+" A. Prog Track Current: "+this.deviceInfo.getGfp().getProgrammingTrackCurrent()+" A. Track Voltage: "+this.deviceInfo.getGfp().getTrackVoltage()+" V. Temperature: "+this.deviceInfo.getGfp().getCS3Temperature()+" C.");
            
            long gfpUid = Long.parseLong(deviceInfo.getGfpUid(), 16);
            long guiUid = Integer.getInteger(deviceInfo.getCs3Uid(), 16);

            CanMessageFactory.setGFPUid((int) gfpUid);
            CanMessageFactory.setGUIUid((int) guiUid);
            ControllerStatus ps = getControllerStatus();

            Logger.info("Connected with " + deviceInfo.getProduct() + " " + deviceInfo.getArticleNumber() + " Serial# " + deviceInfo.getSerialNumber() + ". Track Power is " + (ps.isPower() ? "On" : "Off") + ". GFPUID : " + deviceInfo.getGfpUid() + ". GUIUID : " + deviceInfo.getCs3Uid());
            Logger.trace("Track Power is " + (ps.isPower() ? "On" : "Off"));

            addCanMessageListener(new ExtraMessageListener(this));

            executor.execute(() -> notifyControllerEventListeners(new ControllerEvent(ps.isPower(), connected)));
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
        HTTPConnection httpCon = ControllerConnectionFactory.getHTTPConnection();
        String lokomotiveCs2 = httpCon.getLocomotivesFile();
        LocomotiveBeanParser lp = new LocomotiveBeanParser();
        return lp.parseLocomotivesFile(lokomotiveCs2);
    }

    @Override
    public void getAllFunctionIcons() {
        HTTPConnection httpCon = ControllerConnectionFactory.getHTTPConnection();
        String json = httpCon.getAllFunctionsSvgJSON();
        SvgIconToPngIconConverter svgp = new SvgIconToPngIconConverter();
        svgp.convertAndCacheAllFunctionsSvgIcons(json);
    }

    @Override
    public List<AccessoryBean> getAccessories() {
        HTTPConnection httpCon = ControllerConnectionFactory.getHTTPConnection();
        String magnetartikelCs2 = httpCon.getAccessoriesFile();
        AccessoryBeanParser ap = new AccessoryBeanParser();
        return ap.parseAccessoryFile(magnetartikelCs2);
    }

    @Override
    public Image getLocomotiveImage(String icon) {
        HTTPConnection httpCon = ControllerConnectionFactory.getHTTPConnection();
        Image locIcon = httpCon.getLocomotiveImage(icon);
        return locIcon;
    }

    public CS3Device getDeviceInfo() {
        HTTPConnection httpCon = ControllerConnectionFactory.getHTTPConnection();
        String deviceJSON = httpCon.getDevicesJSON();
        DevicesParser dp = new DevicesParser();
        dp.parseDevices(deviceJSON);
        if (deviceInfo == null) {
            deviceInfo = new CS3Device(dp.getCs3(), dp.getGfp(), dp.getLinkSxx());
        } else {
            deviceInfo.setCs3(dp.getCs3());
            deviceInfo.setGfp(dp.getGfp());
            deviceInfo.setLinkSxx(dp.getLinkSxx());
        }
        return deviceInfo;
    }

    @Override
    public CS3Device getControllerInfo() {
        if (deviceInfo == null) {
            HTTPConnection httpCon = ControllerConnectionFactory.getHTTPConnection();
            String deviceFile = httpCon.getDeviceFile();
            CS3DeviceParser dp = new CS3DeviceParser();
            deviceInfo = dp.parseAccessoryFile(deviceFile);
            long gfpUid = Long.parseLong(deviceInfo.getGfpUid(), 16);

            deviceInfo.updateFromStatusMessageResponse(connection.sendCanMessage(CanMessageFactory.statusConfig((int) gfpUid)));
            //String deviceHostName = connection.getControllerAddress().getHostName();
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
        Logger.info("Current Controller Power Status: " + (isPower() ? "On" : "Off") + "...");
        executor.execute(() -> notifyControllerEventListeners(new ControllerEvent(isPower(), isConnected())));
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
        return ControllerConnectionFactory.getControllerIp();
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
//                    boolean power = controller.getControllerStatus().isPower();
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

    //Test
    public static void main(String[] a) {

        MarklinCS3 cs3 = new MarklinCS3(false);

        if (cs3.isConnected()) {
            cs3.power(false);

//            List<SensorMessageEvent> sml = cs3.querySensors(48);
//            for (SensorMessageEvent sme : sml) {
//                Sensor s = new Sensor(sme.getContactId(), sme.isNewValue() ? 1 : 0, sme.isOldValue() ? 1 : 0, sme.getDeviceId(), sme.getMillis(), new Date());
//                Logger.debug(s.toLogString());
//            }
            //List<AccessoryBean> asl = cs3.getAccessoryStatuses();
            //for (AccessoryStatus as : asl) {
            //    Logger.debug(as.toString());
            //}
//            for (int i = 0; i < 30; i++) {
//                cs3.sendIdle();
//                pause(500);
//            }
//            Logger.debug("Sending  member ping\n");
//            List<PingResponse> prl = cs3.membersPing();
//            //Logger.info("Query direction of loc 12");
//            //DirectionInfo info = cs3.getDirection(12, DecoderType.MM);
//            Logger.debug("got " + prl.size() + " responses");
//            for (PingResponse pr : prl) {
//                Logger.debug(pr);
//            }
//            List<SensorMessageEvent> sel = cs3.querySensors(48);
//
//            for (SensorMessageEvent se : sel) {
//                Logger.debug(se.toString());
//            }
//            FeedbackModule fm2 = new FeedbackModule(2);
//            cs3.queryAllPorts(fm2);
//            Logger.debug(fm2.toLogString());
            //cs2.querySensor(1);
        }

        //PingResponse pr2 = cs3.memberPing();
        //Logger.info("Query direction of loc 12");
        //DirectionInfo info = cs3.getDirection(12, DecoderType.MM);
        Logger.debug("DONE");
        cs3.pause(5L);
        System.exit(0);
    }
    //for (int i = 0; i < 16; i++) {
    //    cs3.requestFeedbackEvents(i + 1);
    //}

}
//    @Override
//    public List<AccessoryStatus> getAccessoryStatuses() {
//This piece does not seem to work with a CS3
//TO sort out how to get the statuses
//Update the file by sending a configRequest first
//        CanMessage msg = connection.sendCanMessage(CanMessageFactory.requestConfig("magstat"));
//        //give it some time to process
//        pause(100L);
//        HTTPConnection httpCon = ControllerConnectionFactory.getHTTPConnection();
//        String accessoryStatuses = httpCon.getAccessoryStatusesFile();
//        AccessoryBeanParser ap = new AccessoryBeanParser();
//        return ap.parseAccessoryStatusFile(accessoryStatuses);
//STUB
//        return Collections.EMPTY_LIST;
//    }
