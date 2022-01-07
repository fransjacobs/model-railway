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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import jcs.controller.ControllerEventListener;
import jcs.controller.cs3.DeviceInfo;
import jcs.controller.cs3.events.CanMessageListener;
import jcs.entities.JCSEntity;
import jcs.entities.JCSProperty;
import jcs.entities.LayoutTile;
import jcs.entities.LayoutTileGroup;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.entities.SignalBean;
import jcs.entities.SolenoidAccessory;
import jcs.entities.SwitchBean;
import jcs.entities.TileBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.DecoderType;
import jcs.entities.enums.Direction;
import jcs.trackservice.events.AccessoryListener;
import jcs.trackservice.events.HeartBeatListener;
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
    List<LocomotiveBean> getLocomotives();

    LocomotiveBean getLocomotive(Integer address, DecoderType decoderType);

    LocomotiveBean getLocomotive(BigDecimal id);

    LocomotiveBean persist(LocomotiveBean locomotive);

    void toggleDirection(Direction direction, LocomotiveBean locomotive);

    void changeSpeed(Integer speed, LocomotiveBean locomotive);

    void setFunction(Boolean value, Integer functionNumber, LocomotiveBean locomotive);

    void toggleFunction(Boolean function, LocomotiveBean locomotive);

    void toggleF1(Boolean f1, LocomotiveBean locomotive);

    void toggleF2(Boolean f2, LocomotiveBean locomotive);

    void toggleF3(Boolean f3, LocomotiveBean locomotive);

    void toggleF4(Boolean f4, LocomotiveBean locomotive);

    //Accessories / Accessory
    List<SwitchBean> getSwitches();

    SwitchBean getSwitchTurnout(Integer address);

    List<SignalBean> getSignals();

    SignalBean getSignal(Integer address);

    SwitchBean persist(SwitchBean turnout);

    SignalBean persist(SignalBean signal);

    void switchAccessory(AccessoryValue value, SolenoidAccessory accessory);

    void switchAccessory(AccessoryValue value, SolenoidAccessory accessory, boolean value2);

    void addAccessoiryListener(AccessoryListener listener);

    void removeAccessoiryListener(AccessoryListener listener);
    
    void removeAllAccessoiryListeners();

    void notifyAllAccessoiryListeners();

    void addHeartBeatListener(HeartBeatListener listener);

    void removeHeartBeatListenerListener(HeartBeatListener listener);

    
    void addSensorListener(SensorListener listener);

    void removeFeedbackPortListener(SensorListener listener);

    void notifyAllSensorListeners();

    //Sensors
    List<SensorBean> getSensors();
    
    SensorBean getSensor(Integer contactId);
    
    SensorBean persist(SensorBean sensor);
    
    void addPersistedEventListener(PersistedEventListener listener);

    void removePersistedEventListener(PersistedEventListener listener);

    void addLocomotiveListener(LocomotiveListener listener);

    void removeLocomotiveListener(LocomotiveListener listener);
    
    void removeAllLocomotiveListeners();
        
    //Generic remove for Loco/accessory/feedback
    void remove(JCSEntity entity);

    DeviceInfo getControllerInfo();

    void addControllerListener(ControllerEventListener listener);

    void removeControllerListener(ControllerEventListener listener);

    List<JCSProperty> getProperties();

    JCSProperty getProperty(String key);

    JCSProperty persist(JCSProperty property);


    void addMessageListener(CanMessageListener listener);

    void removeMessageListener(CanMessageListener listener);

    //Trackplan
    @Deprecated
    Set<LayoutTile> getLayoutTiles();

    @Deprecated
    LayoutTile getLayoutTile(Integer x, Integer y);

    @Deprecated
    LayoutTile persist(LayoutTile layoutTile);

    @Deprecated
    void persistOld(Set<LayoutTile> layoutTiles);

    @Deprecated
    void remove(LayoutTile layoutTile);

    List<LayoutTileGroup> getLayoutTileGroups();

    LayoutTileGroup getLayoutTileGroup(Integer ltgrNr);

    LayoutTileGroup getLayoutTileGroup(BigDecimal ltgrId);

    void persist(LayoutTileGroup layoutTileGroup);

    void remove(LayoutTileGroup layoutTileGroup);
    

    Set<TileBean> getTiles();

    TileBean getTile(Integer x, Integer y);

    TileBean persist(TileBean tile);

    void persist(Set<TileBean> tiles);

    void remove(TileBean tile);

    void synchronizeLocomotivesWithController();

    void synchronizeAccessoriesWithController();

    void synchronizeAccessories();

    void updateGuiStatuses();
}
