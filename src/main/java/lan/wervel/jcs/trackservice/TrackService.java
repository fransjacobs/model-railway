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

import java.math.BigDecimal;
import java.util.List;
import lan.wervel.jcs.controller.ControllerInfo;
import lan.wervel.jcs.entities.ControllableDevice;
import lan.wervel.jcs.entities.FeedbackModule;
import lan.wervel.jcs.entities.JCSProperty;
import lan.wervel.jcs.entities.Locomotive;
import lan.wervel.jcs.entities.Signal;
import lan.wervel.jcs.entities.SolenoidAccessory;
import lan.wervel.jcs.entities.Turnout;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.Direction;
import lan.wervel.jcs.feedback.FeedbackPortListener;
import lan.wervel.jcs.trackservice.events.AccessoryListener;
import lan.wervel.jcs.trackservice.events.HeartBeatListener;
import lan.wervel.jcs.trackservice.events.LocomotiveListener;
import lan.wervel.jcs.trackservice.events.PersistedEventListener;

/**
 * The Track repository contain all track item which are used on the Track This
 * can be Locomotives, Turnouts, Signals, etc There For future use the
 * implementation of the Repository could be changed to an other storage
 * provider
 *
 * @author frans
 */
public interface TrackService {

    public static final String SERVICE_TYPE = "TrackService";

    //Track power  
    void powerOff();

    void powerOn();

    boolean isPowerOn();

    //Controller
    boolean connect();

    boolean isConnected();

    void disconnect();

    //Locomotive 
    List<Locomotive> getLocomotives();

    Locomotive getLocomotive(Integer address);

    Locomotive getLocomotive(BigDecimal id);

    Locomotive persist(Locomotive locomotive);

    void toggleDirection(Direction direction, Locomotive locomotive);

    void changeSpeed(Integer speed, Locomotive locomotive);

    void setFunction(Boolean value, Integer functionNumber, Locomotive locomotive);

    void toggleFunction(Boolean function, Locomotive locomotive);

    void toggleF1(Boolean f1, Locomotive locomotive);

    void toggleF2(Boolean f2, Locomotive locomotive);

    void toggleF3(Boolean f3, Locomotive locomotive);

    void toggleF4(Boolean f4, Locomotive locomotive);

    //Accessories / Accessory
    List<Turnout> getTurnouts();

    Turnout getTurnout(Integer address);

    List<Signal> getSignals();

    Signal getSignal(Integer address);

    Turnout persist(Turnout turnout);

    Signal persist(Signal signal);

    void switchAccessory(AccessoryValue value, SolenoidAccessory accessory);

    void switchAccessory(AccessoryValue value, SolenoidAccessory accessory, boolean value2);

    void addAccessoiryListener(AccessoryListener listener);

    void removeAccessoiryListener(AccessoryListener listener);

    void notifyAllAccessoiryListeners();

    //Feedback Modules
    List<FeedbackModule> getFeedbackModules();

    FeedbackModule getFeedbackModule(Integer moduleNumber);

    void addHeartBeatListener(HeartBeatListener listener);

    void removeHeartBeatListenerListener(HeartBeatListener listener);

    FeedbackModule persist(FeedbackModule feedbackModule);

    void addFeedbackPortListener(FeedbackPortListener listener);

    void removeFeedbackPortListener(FeedbackPortListener listener);

    void notifyAllFeedbackListeners();

    void addPersistedEventListener(PersistedEventListener listener);

    void removePersistedEventListener(PersistedEventListener listener);

    void addLocomotiveListener(LocomotiveListener listener);

    void removeLocomotiveListener(LocomotiveListener listener);

    //Generic remove for Loco/accessory/feedback
    void remove(ControllableDevice entity);

    ControllerInfo getControllerInfo();

    List<JCSProperty> getProperties();

    JCSProperty getProperty(String key);

    JCSProperty persist(JCSProperty property);

    void synchronizeAccessories();

}
