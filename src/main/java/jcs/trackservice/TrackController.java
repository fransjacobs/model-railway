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
package jcs.trackservice;

import java.util.List;
import java.util.Set;
import jcs.controller.cs3.devices.LinkSxx;
import jcs.controller.cs3.events.CanMessageListener;
import jcs.controller.cs3.events.PowerEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.JCSPropertyBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.DecoderType;
import jcs.entities.enums.Direction;
import jcs.trackservice.events.AccessoryListener;
import jcs.trackservice.events.DirectionListener;
import jcs.trackservice.events.FunctionListener;
import jcs.trackservice.events.SensorListener;
import jcs.trackservice.events.VelocityListener;

/**
 * The Track repository contain all track item which are used on the Track This
 * can be Locomotives, Turnouts, Signals, etc There For future use the
 * implementation of the Repository could be changed to an other storage
 * provider
 *
 * @author frans
 */
public interface TrackController {

    void switchPower(boolean on);

    boolean isPowerOn();

    boolean connect();

    boolean isConnected();

    void disconnect();

    void addPowerEventListener(PowerEventListener listener);

    void removePowerEventListener(PowerEventListener listener);

    //Locomotive 
    @Deprecated //use the PersistenService
    List<LocomotiveBean> getLocomotives();

    @Deprecated //use the PersistenService
    LocomotiveBean getLocomotive(Integer address, DecoderType decoderType);

    @Deprecated //use the PersistenService
    LocomotiveBean getLocomotive(Long id);

    @Deprecated //use the PersistenService
    LocomotiveBean persist(LocomotiveBean locomotive);

    //Image getFunctionImage(String imageName);

    void changeDirection(Direction direction, LocomotiveBean locomotive);

    void changeVelocity(Integer speed, LocomotiveBean locomotive);

    void changeFunction(Boolean value, Integer functionNumber, LocomotiveBean locomotive);

    //Accessories
    @Deprecated //use the PersistenService
    List<AccessoryBean> getTurnouts();

    @Deprecated //use the PersistenService
    List<AccessoryBean> getSignals();

    @Deprecated //use the PersistenService
    AccessoryBean getAccessory(Long id);

    @Deprecated //use the PersistenService
    AccessoryBean getAccessory(Integer address, String decoderTypee);

    @Deprecated //use the PersistenService
    AccessoryBean persist(AccessoryBean accessory);

    void switchAccessory(AccessoryValue value, AccessoryBean accessory);

    void addAccessoryListener(AccessoryListener listener);

    void removeAccessoryListener(AccessoryListener listener);

    void addSensorListener(SensorListener listener);

    void removeSensorListener(SensorListener listener);

    //Sensors
    @Deprecated //use the PersistenService
    List<SensorBean> getSensors();

    @Deprecated //use the PersistenService
    SensorBean getSensor(Long id);

    @Deprecated //use the PersistenService
    SensorBean getSensor(Integer deviceId, Integer contactId);

    @Deprecated //use the PersistenService
    SensorBean persist(SensorBean sensor);

    void addFunctionListener(FunctionListener listener);

    void removeFunctionListener(FunctionListener listener);

    void addDirectionListener(DirectionListener listener);

    void removeDirectionListener(DirectionListener listener);

    void addVelocityListener(VelocityListener listener);

    public void removeVelocityListener(VelocityListener listener);

    @Deprecated //use the PersistenService
    void remove(SensorBean sensor);

    @Deprecated //use the PersistenService
    void remove(LocomotiveBean locomotive);

    @Deprecated //use the PersistenService
    void remove(AccessoryBean accessory);

    @Deprecated //use the PersistenService
    void remove(JCSPropertyBean property);

    String getControllerName();

    String getControllerSerialNumber();

    String getControllerArticleNumber();

    LinkSxx getLinkSxx();

    @Deprecated //use the PersistenService
    List<JCSPropertyBean> getProperties();

    @Deprecated //use the PersistenService
    JCSPropertyBean getProperty(String key);

    @Deprecated //use the PersistenService
    JCSPropertyBean persist(JCSPropertyBean property);

    void addMessageListener(CanMessageListener listener);

    void removeMessageListener(CanMessageListener listener);

    @Deprecated //use the PersistenService
    Set<TileBean> getTiles();

    @Deprecated //use the PersistenService
    TileBean getTile(Integer x, Integer y);

    @Deprecated //use the PersistenService
    TileBean persist(TileBean tile);

    @Deprecated //use the PersistenService
    void persist(Set<TileBean> tiles);

    @Deprecated //use the PersistenService
    void remove(TileBean tile);

    void synchronizeLocomotivesWithController();

    @Deprecated //use the PersistenService
    List<RouteBean> getRoutes();

    @Deprecated //use the PersistenService
    void persist(RouteBean route);

    @Deprecated //use the PersistenService
    void remove(RouteBean route);

    void synchronizeTurnouts();

    void synchronizeSignals();

    void updateGuiStatuses();
}
