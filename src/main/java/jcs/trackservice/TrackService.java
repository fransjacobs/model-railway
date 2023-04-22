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
import jcs.controller.cs3.devices.LinkSxx;
import jcs.controller.cs3.events.CanMessageListener;
import jcs.controller.cs3.events.PowerEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.JCSEntity;
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
public interface TrackService {

    void switchPower(boolean on);

    boolean isPowerOn();

    boolean connect();

    boolean isConnected();

    void disconnect();

    void addPowerEventListener(PowerEventListener listener);

    void removePowerEventListener(PowerEventListener listener);

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

    AccessoryBean getAccessory(Integer address);

    AccessoryBean persist(AccessoryBean accessory);

    void switchAccessory(AccessoryValue value, AccessoryBean accessory);

    void addAccessoryListener(AccessoryListener listener);

    void removeAccessoryListener(AccessoryListener listener);

    void addSensorListener(SensorListener listener);

    void removeSensorListener(SensorListener listener);

    //Sensors
    List<SensorBean> getSensors();

    SensorBean getSensor(BigDecimal id);

    SensorBean getSensor(Integer deviceId, Integer contactId);

    SensorBean persist(SensorBean sensor);

    void addFunctionListener(FunctionListener listener);

    void removeFunctionListener(FunctionListener listener);

    void addDirectionListener(DirectionListener listener);

    void removeDirectionListener(DirectionListener listener);

    void addVelocityListener(VelocityListener listener);

    public void removeVelocityListener(VelocityListener listener);

    //Generic remove for Loco/accessory/feedback
    void remove(JCSEntity entity);

    String getControllerName();

    String getControllerSerialNumber();

    String getControllerArticleNumber();

    LinkSxx getLinkSxx();

    List<JCSPropertyBean> getProperties();

    JCSPropertyBean getProperty(String key);

    JCSPropertyBean persist(JCSPropertyBean property);

    void addMessageListener(CanMessageListener listener);

    void removeMessageListener(CanMessageListener listener);

    Set<TileBean> getTiles();

    TileBean getTile(Integer x, Integer y);

    TileBean persist(TileBean tile);

    void persist(Set<TileBean> tiles);

    void remove(TileBean tile);

    void synchronizeLocomotivesWithController();

    List<RouteBean> getRoutes();

    void persist(RouteBean route);

    void remove(RouteBean route);

    void synchronizeTurnouts();

    void synchronizeSignals();

    void updateGuiStatuses();
}
