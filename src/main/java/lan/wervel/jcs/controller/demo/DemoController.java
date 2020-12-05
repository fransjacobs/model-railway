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
package lan.wervel.jcs.controller.demo;

import lan.wervel.jcs.controller.cs2.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lan.wervel.jcs.controller.ControllerEvent;
import lan.wervel.jcs.controller.ControllerEventListener;
import lan.wervel.jcs.controller.ControllerService;
import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.controller.cs2.can.MarklinCan;
import lan.wervel.jcs.controller.cs2.events.CanMessageEvent;
import lan.wervel.jcs.controller.cs2.events.CanMessageListener;
import lan.wervel.jcs.controller.cs2.net.Connection;
import lan.wervel.jcs.entities.Locomotive;
import lan.wervel.jcs.entities.SolenoidAccessory;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.Direction;
import lan.wervel.jcs.entities.enums.DecoderType;
import lan.wervel.jcs.feedback.FeedbackEvent;
import lan.wervel.jcs.feedback.FeedbackEventListener;
import lan.wervel.jcs.feedback.FeedbackService;
import org.pmw.tinylog.Logger;
import lan.wervel.jcs.feedback.HeartbeatListener;
import lan.wervel.jcs.util.NetworkUtil;
import org.pmw.tinylog.Configurator;

/**
 *
 * @author Frans Jacobs
 */
public class DemoController implements ControllerService, FeedbackService {

    private Connection connection;
    private boolean connected = false;
    private boolean running = false;

    private final List<ControllerEventListener> controllerEventListeners;
    private final List<FeedbackEventListener> feedbackEventListeners;

    private final List<HeartbeatListener> heartbeatListeners;

    private final Timer timer;
    private boolean startTimer;

    private final ExecutorService executor;

    private int[] deviceUid;
    private int deviceUidNumber;

    private DeviceInfo deviceInfo;

    private static final long DELAY = 0L;

    ///
    private PowerStatus powerStatus;

    private Map<Integer, DirectionInfo> locDirections;

    public DemoController() {
        this(true);
    }

    DemoController(boolean useTimer) {
        controllerEventListeners = new ArrayList<>();
        feedbackEventListeners = new ArrayList<>();
        heartbeatListeners = new ArrayList<>();
        startTimer = useTimer;
        timer = new Timer("Heartbeat", true);
        executor = Executors.newCachedThreadPool();

        locDirections = new HashMap<>();

        connect();
    }

    @Override
    public PowerStatus powerOff() {
        Logger.debug("PowerOff");
        powerStatus = new PowerStatus(false, deviceUid, deviceUidNumber);
        return powerStatus;
    }

    @Override
    public PowerStatus powerOn() {
        Logger.debug("PowerOn");
        powerStatus = new PowerStatus(true, deviceUid, deviceUidNumber);
        return powerStatus;
    }

    public PowerStatus getPowerStatus() {
        return powerStatus;
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
            Logger.debug("Connecting to Demo Controller...");
            this.connected = true;
        }

        if (connected) {
            deviceUid = new int[]{0x01, 0x02, 0x03, 0x04};
            deviceUidNumber = 16909060;

            powerStatus = new PowerStatus(false, deviceUid, deviceUidNumber);

            Logger.trace("Track Power is " + (powerStatus.isPowerOn() ? "On" : "Off") + " DeviceId: " + deviceUidNumber);
            deviceInfo = getControllerInfo();
            Logger.info("Connected with " + deviceInfo.getDescription() + " " + deviceInfo.getCatalogNumber() + " Serial# " + deviceInfo.getSerialNumber() + ". Track Power is " + (powerStatus.isPowerOn() ? "On" : "Off") + ". DeviceId: " + deviceUidNumber);

            addCanMessageListener(new ExtraMessageListener(this));

            //Send powerstatus
            executor.execute(() -> notifyControllerEventListeners(new ControllerEvent(powerStatus.isPowerOn(), connected)));
        }

        // Finally start the harbeat timer which will takes care of the feedback 
        if (startTimer && connected) {
            //Start the timer
            //timer.scheduleAtFixedRate(new HeartbeatTask(this), DELAY, FeedbackService.DEFAULT_POLL_MILLIS);
            timer.scheduleAtFixedRate(new HeartbeatTask(this), DELAY, 1000);
            this.running = true;
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
            connected = false;

            stopHeartbeatTask();

            this.deviceUid = null;
            this.deviceUidNumber = 0;
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
            case MM2:
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

        if (!this.locDirections.containsKey(la)) {
            DirectionInfo di = new DirectionInfo(Direction.FORWARDS);
            this.locDirections.put(la, di);
        }

        DirectionInfo di = this.locDirections.get(la);
        Direction direction = di.getDirection();
        direction = direction.toggle();
        setDirection(address, decoderType, direction);
    }

    @Override
    public void setDirection(int address, DecoderType decoderType, Direction direction) {
        int la = getLocoAddres(address, decoderType);
        Logger.trace("Setting direction to: " + direction + " for loc address: " + la + " Decoder: " + decoderType);
        this.locDirections.put(la, new DirectionInfo(direction));
    }

    public DirectionInfo getDirection(int address, DecoderType decoderType) {
        int la = getLocoAddres(address, decoderType);
        if (!this.locDirections.containsKey(la)) {
            DirectionInfo di = new DirectionInfo(Direction.FORWARDS);
            this.locDirections.put(la, di);
        }

        DirectionInfo di = this.locDirections.get(la);
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
    }

    @Override
    public void setFunction(int address, DecoderType decoderType, int functionNumber, boolean flag) {
        //int value = flag ? FUNCTION_ON : FUNCTION_OFF;
        //int la = getLocoAddres(address, decoderType);
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

    private void switchAccessoiryOnOff(int address, AccessoryValue value) {
    }

    @Override
    public int[] getFeedback(int moduleNumber) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    public List<PingResponse> membersPing() {
//        CanMessage msg = connection.sendCanMessage(CanMessageFactory.getMemberPing());
//        List<CanMessage> rl = msg.getResponses();
//        List<PingResponse> prl = new ArrayList<>();
//        for (CanMessage resp : rl) {
//            PingResponse pr = new PingResponse(resp);
//            prl.add(pr);
//        }
//
//        return prl;
//    }
    @Override
    public DeviceInfo getControllerInfo() {
        if (deviceInfo == null) {
            deviceInfo = new DeviceInfo("123456", "JCS Demo", "Demo Controller", 32, true, true, true, true);
            String deviceHostName = NetworkUtil.getMyAddress().getHostName();
            deviceInfo.setDeviceHostName(deviceHostName);
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
    public long getPollIntervalMillis() {
        return FeedbackService.DEFAULT_POLL_MILLIS;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void addFeedbackEventListener(FeedbackEventListener listener) {
        synchronized (feedbackEventListeners) {
            this.feedbackEventListeners.add(listener);
        }
    }

    @Override
    public void removeFeedbackEventListener(FeedbackEventListener listener) {
        synchronized (feedbackEventListeners) {
            this.feedbackEventListeners.remove(listener);
        }
    }

    @Override
    public void removeAllFeedbackEventListeners() {
        synchronized (feedbackEventListeners) {
            this.feedbackEventListeners.clear();
        }
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

    public String getDeviceIp() {
        return "0.0.0.0";
    }

    @Override
    public List<Locomotive> getLocomotives() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SolenoidAccessory> getAccessories() {
        return Collections.EMPTY_LIST;
    }

//    public void getLocomotiveConfigData() {
//        CanMessage msg = this.connection.sendCanMessage(CanMessageFactory.requestConfig("loks"));
//    }
//    public void requestFeedbackEvents(int contactId) {
//        CanMessage msg = connection.sendCanMessage(CanMessageFactory.feedbackEvent(contactId));
//        Logger.trace(msg.getResponse());
//    }
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

    private void notifyFeedbackEventListeners(FeedbackEvent event) {
        Set<FeedbackEventListener> snapshot;
        synchronized (feedbackEventListeners) {
            if (feedbackEventListeners.isEmpty()) {
                snapshot = new HashSet<>();
            } else {
                snapshot = new HashSet<>(feedbackEventListeners);
            }
        }

        for (FeedbackEventListener listener : snapshot) {
            listener.notify(event);
        }
    }

//    private void wait200ms() {
//        try {
//            Thread.sleep(200L);
//        } catch (InterruptedException ex) {
//            Logger.error(ex);
//        }
//    }
    public void stopHeartbeatTask() {
        if (timer != null) {
            timer.purge();
            timer.cancel();
            running = false;
            this.heartbeatListeners.clear();
            Logger.trace("JCS Demo Heartbeat Task cancelled...");
        }
    }

    private class HeartbeatTask extends TimerTask {

        private final DemoController controller;
        private boolean powerOn;
        private boolean toggle = false;

        HeartbeatTask(DemoController demoController) {
            controller = demoController;
        }

        @Override
        public void run() {
            try {
                if (toggle) {
                    boolean power = this.controller.getPowerStatus().isPowerOn();
                    if (power != powerOn) {
                        powerOn = power;
                        //Logger.debug("Power Status changed to " + (powerOn ? "On" : "Off"));
                        ControllerEvent ce = new ControllerEvent(powerOn, this.controller.isConnected());
                        controller.notifyControllerEventListeners(ce);
                    }
                }
//                else {
//                    List<PingResponse> prl = membersPing();
//                }

                Set<HeartbeatListener> snapshot;
                synchronized (controller.heartbeatListeners) {
                    snapshot = new HashSet<>(controller.heartbeatListeners);
                }

                for (HeartbeatListener listener : snapshot) {
                    listener.sample();
                }
                toggle = !toggle;
            } catch (Exception e) {
                Logger.error(e.getMessage());
                Logger.trace(e);
            }
        }
    }

    private class ExtraMessageListener implements CanMessageListener {

        private final DemoController controller;
        private final ExecutorService executor;

        ExtraMessageListener(DemoController cs2Controller) {
            controller = cs2Controller;
            executor = Executors.newCachedThreadPool();
        }

        @Override
        public void onCanMessage(CanMessageEvent canEvent) {
            CanMessage msg = canEvent.getCanMessage();
            int cmd = msg.getCommand();

            switch (cmd) {
                case MarklinCan.S88_EVENT_RESPONSE:
                    FeedbackEventStatus fs = new FeedbackEventStatus(msg);
                    FeedbackEvent fe = new FeedbackEvent(fs);

                    //Logger.trace(fs);
                    executor.execute(() -> controller.notifyFeedbackEventListeners(fe));
                    break;
                default:
                    //Logger.trace("Message: " + msg);
                    break;
            }
        }
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.error(ex);
        }
    }

    public static void main(String[] a) {
        Configurator.defaultConfig().level(org.pmw.tinylog.Level.TRACE).activate();
        DemoController cs2 = new DemoController(true);

        if (cs2.isConnected()) {
            //cs2.powerOn();

//            Logger.debug("Sending  member ping\n");
//            List<PingResponse> prl = cs2.membersPing();
//            //Logger.info("Query direction of loc 12");
//            //DirectionInfo info = cs2.getDirection(12, DecoderType.MM2);
//            Logger.debug("got " + prl.size() + " responses");
//            for (PingResponse pr : prl) {
//                Logger.debug(pr);
//            }
        }

        //PingResponse pr2 = cs2.memberPing();
        //Logger.info("Query direction of loc 12");
        //DirectionInfo info = cs2.getDirection(12, DecoderType.MM2);
        // Logger.debug(pr2);
        pause(5);
    }
    //for (int i = 0; i < 16; i++) {
    //    cs2.requestFeedbackEvents(i + 1);
    //}

}
