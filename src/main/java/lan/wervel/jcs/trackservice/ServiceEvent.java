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
package lan.wervel.jcs.trackservice;

import java.io.Serializable;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.SignalValue;

/**
 *
 * @author Frans Jacobs
 */
public class ServiceEvent implements Serializable {

  private TrackServiceEventType trackEventType;
  
  //Controller
  private boolean powerOn;
  private boolean connected;

  //FeedbackPort  
  private Integer moduleNumber;
  private Integer port;
  private boolean portValue;
  
  //Accessory / Turnout
  private Integer address;
  private AccessoryValue accessoryValue;
    
  //Signal  
  private Integer address2;
  private SignalValue signalValue;
 
  //HeartBeat
  private boolean beat;
  
  //Layout 
  private boolean layoutChanged;
    
  //Locomotive
  //speedChanged
  //directionChanged
  //functionChanged
  
  //Service
  //entityPersited
  
  //Driveway
 
 

}
