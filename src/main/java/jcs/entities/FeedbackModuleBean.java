/*
 * Copyright 2024 frans.
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
package jcs.entities;

import java.util.ArrayList;
import java.util.List;
import jcs.commandStation.events.SensorEvent;

/**
 * Represents 1 Feedback Module (S88) with a number of ports (usually 16)
 */
public class FeedbackModuleBean {

  private Integer id;
  private Integer moduleNumber;
  private Integer portCount;
  private Integer addressOffset;
  private Integer identifier;

  private int[] ports;
  private int[] prevPorts;

  public static int DEFAULT_PORT_COUNT = 16;
  public static int DEFAULT_ADDRESS_OFFSET = 0;
  public static int DEFAULT_IDENTIFIER = 0;

  public FeedbackModuleBean() {
    this(null, null);
  }

  public FeedbackModuleBean(Integer id, Integer moduleNumber) {
    this(id, moduleNumber, DEFAULT_PORT_COUNT, DEFAULT_ADDRESS_OFFSET, DEFAULT_IDENTIFIER);
  }

  public FeedbackModuleBean(Integer id, Integer moduleNumber, Integer portCount, Integer addressOffset, Integer identifier) {
    this.id = id;
    this.moduleNumber = moduleNumber;
    this.portCount = portCount;
    this.addressOffset = addressOffset;
    this.identifier = identifier;

    ports = new int[portCount];
    prevPorts = new int[portCount];
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getModuleNumber() {
    return moduleNumber;
  }

  public void setModuleNumber(Integer moduleNumber) {
    this.moduleNumber = moduleNumber;
  }

  public Integer getPortCount() {
    return portCount;
  }

  public void setPortCount(Integer portCount) {
    this.portCount = portCount;
    if (portCount != null && portCount != ports.length) {
      ports = new int[portCount];
      prevPorts = new int[portCount];
    }
  }

  public Integer getAddressOffset() {
    return addressOffset;
  }

  public void setAddressOffset(Integer addressOffset) {
    this.addressOffset = addressOffset;
  }

  public Integer getIdentifier() {
    return identifier;
  }

  public void setIdentifier(Integer identifier) {
    this.identifier = identifier;
  }

  public int[] getPorts() {
    return ports;
  }

  public void setPorts(int[] ports) {
    this.ports = ports;
  }

  public void setPortValue(int port, boolean active) {
    //save current values
    System.arraycopy(this.ports, 0, this.prevPorts, 0, this.ports.length);
    this.ports[port] = active ? 1 : 0;
  }

  public boolean isPort(int port) {
    if (ports != null && port < ports.length) {
      return this.ports[port] == 1;
    } else {
      return false;
    }
  }

  public int getAccumulatedPortsValue() {
    int val = 0;
    for (int i = 0; i < ports.length; i++) {
      int portVal = 0;
      if (ports[i] == 1) {
        portVal = (int) Math.pow(2, i);
      }
      val = val + portVal;
    }
    return val;
  }

  public int[] getPrevPorts() {
    return prevPorts;
  }

  public void setPrevPorts(int[] prevPorts) {
    this.prevPorts = prevPorts;
  }

//    public static Integer calculateModuleNumber(int contactId) {
//        int module = (contactId - 1) / 16 + 1;
//        return module;
//    }
//    public static int calculatePortNumber(int contactId) {
//        int module = (contactId - 1) / 16 + 1;
//        int mport = contactId - (module - 1) * 16;
//        return mport;
//    }
//    public static int calculateContactId(int module, int port) {
//        //Bei einer CS2 errechnet sich der richtige Kontakt mit der Formel M - 1 * 16 + N
//        module = module - 1;
//        int contactId = module * 16;
//        return contactId + port;
  //    }  
  public SensorBean getSensor(int port) {
    //Marklin addressOffset is 0, 1000, 2000 or 3000
    //Sensor address is addressOffset + moduleNumber * portCount + port
    //ESU 1st feedbackmodule start a 100 this has portCount sensors. 100 has sensors 0 - 15.
    // 2nd Module is 101 which has sensors 16 - 31 - the node can be setto d the module id address ofset is 0
    //Sensor address is addressOffset(0) + moduleNumber * portCount + port
    
    int sid = addressOffset + moduleNumber * portCount + port;
    int status = ports[port];
    int prevStatus = prevPorts[port];

    SensorBean sb = new SensorBean(sid, moduleNumber, port, identifier, status, prevStatus);
    return sb;
  }

//  here are [FeedbackModuleBean{id=100, moduleNumber=0, portCount=16, addressOffset=100, identifier=0}, FeedbackModuleBean{id=101, moduleNumber=1, portCount=16, addressOffset=100, identifier=0}, FeedbackModuleBean{id=102, moduleNumber=2, portCount=16, addressOffset=100, identifier=0}] Feedback Modules
//TRACE	2025-05-12 21:05:57.782 [main] EsuEcosCommandStationImpl.main(): Module id: 100 nr: 0 ports: 16
//TRACE	2025-05-12 21:05:57.784 [main] EsuEcosCommandStationImpl.main(): Module id: 100 S 1 id:100 cid: 0 did: 0
//TRACE	2025-05-12 21:05:57.785 [main] EsuEcosCommandStationImpl.main(): Module id: 100 S 15 id:115 cid: 15 did: 0
//TRACE	2025-05-12 21:05:57.785 [main] EsuEcosCommandStationImpl.main(): Module id: 101 nr: 1 ports: 16
//TRACE	2025-05-12 21:05:57.785 [main] EsuEcosCommandStationImpl.main(): Module id: 101 S 1 id:116 cid: 0 did: 1
//TRACE	2025-05-12 21:05:57.785 [main] EsuEcosCommandStationImpl.main(): Module id: 101 S 15 id:131 cid: 15 did: 1
//TRACE	2025-05-12 21:05:57.785 [main] EsuEcosCommandStationImpl.main(): Module id: 102 nr: 2 ports: 16
//TRACE	2025-05-12 21:05:57.785 [main] EsuEcosCommandStationImpl.main(): Module id: 102 S 1 id:132 cid: 0 did: 2
//TRACE	2025-05-12 21:05:57.785 [main] EsuEcosCommandStationImpl.main(): Module id: 102 S 15 id:147 cid: 15 did: 2
//  
  
  
  
//TRACE	2025-05-12 21:08:01.425 [main] MarklinCentralStationImpl.getFeedbackModules(): nodeId: 65, bus1Len: 2, bus2Len: 2, bus3Len: 1
//TRACE	2025-05-12 21:08:01.429 [main] MarklinCentralStationImpl.main(): There are [FeedbackModuleBean{id=0, moduleNumber=0, portCount=16, addressOffset=0, identifier=65}, FeedbackModuleBean{id=1000, moduleNumber=0, portCount=16, addressOffset=1000, identifier=65}, FeedbackModuleBean{id=1001, moduleNumber=1, portCount=16, addressOffset=1000, identifier=65}, FeedbackModuleBean{id=2000, moduleNumber=0, portCount=16, addressOffset=2000, identifier=65}, FeedbackModuleBean{id=2001, moduleNumber=1, portCount=16, addressOffset=2000, identifier=65}, FeedbackModuleBean{id=3000, moduleNumber=0, portCount=16, addressOffset=3000, identifier=65}] Feedback Modules
//TRACE	2025-05-12 21:08:01.429 [main] MarklinCentralStationImpl.main(): Module id: 0 nr: 0 ports: 16
//TRACE	2025-05-12 21:08:01.432 [main] MarklinCentralStationImpl.main(): Module id: 0 S 1 id:0 cid: 0 did: 0
//TRACE	2025-05-12 21:08:01.432 [main] MarklinCentralStationImpl.main(): Module id: 0 S 15 id:15 cid: 15 did: 0
//TRACE	2025-05-12 21:08:01.432 [main] MarklinCentralStationImpl.main(): Module id: 1000 nr: 0 ports: 16
//TRACE	2025-05-12 21:08:01.432 [main] MarklinCentralStationImpl.main(): Module id: 1000 S 1 id:1000 cid: 0 did: 0
//TRACE	2025-05-12 21:08:01.432 [main] MarklinCentralStationImpl.main(): Module id: 1000 S 15 id:1015 cid: 15 did: 0
//TRACE	2025-05-12 21:08:01.432 [main] MarklinCentralStationImpl.main(): Module id: 1001 nr: 1 ports: 16
//TRACE	2025-05-12 21:08:01.432 [main] MarklinCentralStationImpl.main(): Module id: 1001 S 1 id:1016 cid: 0 did: 1
//TRACE	2025-05-12 21:08:01.432 [main] MarklinCentralStationImpl.main(): Module id: 1001 S 15 id:1031 cid: 15 did: 1
//TRACE	2025-05-12 21:08:01.432 [main] MarklinCentralStationImpl.main(): Module id: 2000 nr: 0 ports: 16
//TRACE	2025-05-12 21:08:01.432 [main] MarklinCentralStationImpl.main(): Module id: 2000 S 1 id:2000 cid: 0 did: 0
//TRACE	2025-05-12 21:08:01.432 [main] MarklinCentralStationImpl.main(): Module id: 2000 S 15 id:2015 cid: 15 did: 0
//TRACE	2025-05-12 21:08:01.433 [main] MarklinCentralStationImpl.main(): Module id: 2001 nr: 1 ports: 16
//TRACE	2025-05-12 21:08:01.433 [main] MarklinCentralStationImpl.main(): Module id: 2001 S 1 id:2016 cid: 0 did: 1
//TRACE	2025-05-12 21:08:01.433 [main] MarklinCentralStationImpl.main(): Module id: 2001 S 15 id:2031 cid: 15 did: 1
//TRACE	2025-05-12 21:08:01.433 [main] MarklinCentralStationImpl.main(): Module id: 3000 nr: 0 ports: 16
//TRACE	2025-05-12 21:08:01.433 [main] MarklinCentralStationImpl.main(): Module id: 3000 S 1 id:3000 cid: 0 did: 0
//TRACE	2025-05-12 21:08:01.433 [main] MarklinCentralStationImpl.main(): Module id: 3000 S 15 id:3015 cid: 15 did: 0

  
  
  
  
  public List<SensorEvent> getChangedSensors() {
    List<SensorEvent> changedSensors = new ArrayList<>(ports.length);

    for (int i = 0; i < ports.length; i++) {
      if (ports[i] != prevPorts[i]) {
        SensorBean sb = null; //new SensorBean(moduleNumber, i + 1, ports[i]);
        SensorEvent se = new SensorEvent(sb);
        changedSensors.add(se);
      }
    }
    return changedSensors;
  }

  public List<SensorBean> getSensors() {
    List<SensorBean> sensors = new ArrayList<>(ports.length);

    for (int i = 0; i < ports.length; i++) {
      if (ports[i] != prevPorts[i]) {
        SensorBean sb = null; //new SensorBean(moduleNumber, i + 1, ports[i]);
        sensors.add(sb);
      }
    }
    return sensors;
  }

  public String portToString() {
    StringBuilder sb = new StringBuilder();
    sb.append(" {");
    for (int i = 0; i < ports.length; i++) {
      sb.append(i + 1);
      sb.append("[");
      sb.append(ports[i]);
      sb.append("] ");
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public String toString() {
    return "FeedbackModuleBean{" + "id=" + id + ", moduleNumber=" + moduleNumber + ", portCount=" + portCount + ", addressOffset=" + addressOffset + ", identifier=" + identifier + "}";
  }

}
