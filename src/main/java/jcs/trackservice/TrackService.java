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
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import jcs.controller.ControllerEventListener;
import jcs.controller.cs3.events.CanMessageListener;
import jcs.entities.AccessoryBean;
import jcs.entities.JCSEntity;
import jcs.entities.JCSProperty;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.DecoderType;
import jcs.entities.enums.Direction;
import jcs.trackservice.events.AccessoryListener;
import jcs.trackservice.events.LocomotiveListener;
import jcs.trackservice.events.PersistedEventListener;
import jcs.trackservice.events.SensorListener;

/**
 * The Track repository contain all track item which are used on the Track This
 * can be Locomotives, Turnouts, Signals, etc There For future use the
 * implementation of the Repository could be changed to an other storage
 * provider
 *
 * @author frans
 */
public interface TrackService {

    //public static final String SERVICE_TYPE = "TrackService";
    //Track power  
    void switchPower(boolean on);

    boolean isPowerOn();

    //Controller
    boolean connect();

    boolean isConnected();

    void disconnect();

    //Locomotive 
    List<LocomotiveBean> getLocomotives();

    LocomotiveBean getLocomotive(Integer address, DecoderType decoderType);

    LocomotiveBean getLocomotive(BigDecimal id);

    LocomotiveBean persist(LocomotiveBean locomotive);

    Image getFunctionImage(String imageName);

    void changeDirection(Direction direction, LocomotiveBean locomotive);

    void changeVelocity(Integer speed, LocomotiveBean locomotive);

    void changeFunction(Boolean value, Integer functionNumber, LocomotiveBean locomotive);

    //Accessories
    List<AccessoryBean> getTurnouts();

    List<AccessoryBean> getSignals();

    AccessoryBean getAccessory(BigDecimal id);

    AccessoryBean persist(AccessoryBean accessory);

    void switchAccessory(AccessoryValue value, AccessoryBean accessory);

    void addAccessoiryListener(AccessoryListener listener);

    void removeAccessoiryListener(AccessoryListener listener);

    void removeAllAccessoiryListeners();

    void notifyAllAccessoiryListeners();

    //void addHeartBeatListener(HeartBeatListener listener);
    //void removeHeartBeatListenerListener(HeartBeatListener listener);
    void addSensorListener(SensorListener listener);

    void removeSensorListener(SensorListener listener);

    void notifyAllSensorListeners();

    //Sensors
    List<SensorBean> getSensors();

    SensorBean getSensor(BigDecimal id);

    SensorBean getSensor(Integer deviceId, Integer contactId);

    SensorBean persist(SensorBean sensor);

    void addPersistedEventListener(PersistedEventListener listener);

    void removePersistedEventListener(PersistedEventListener listener);

    void addLocomotiveListener(LocomotiveListener listener);

    void removeLocomotiveListener(LocomotiveListener listener);

    void removeAllLocomotiveListeners();

    //Generic remove for Loco/accessory/feedback
    void remove(JCSEntity entity);

    //StatusDataConfigParser getControllerInfo();
    String getControllerName();

    String getControllerSerialNumber();

    String getControllerArticleNumber();

    void addControllerListener(ControllerEventListener listener);

    void removeControllerListener(ControllerEventListener listener);

    List<JCSProperty> getProperties();

    JCSProperty getProperty(String key);

    JCSProperty persist(JCSProperty property);

    void addMessageListener(CanMessageListener listener);

    void removeMessageListener(CanMessageListener listener);

    Set<TileBean> getTiles();

    TileBean getTile(Integer x, Integer y);

    TileBean persist(TileBean tile);

    void persist(Set<TileBean> tiles);

    void remove(TileBean tile);

    void synchronizeLocomotivesWithController();

    void synchronizeAccessoriesWithController();

    void updateGuiStatuses();
}
