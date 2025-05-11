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
    int node = identifier;
    int pcnt = portCount;
    int modnr = this.moduleNumber;

    int sensorId = addressOffset + moduleNumber + port;
    int contactId = port;

    //contactid moet doornummer van device is de node of nodg een veld in de sensor table toeveoegen
    SensorBean sb = new SensorBean(sensorId, moduleNumber, contactId, identifier, ports[port]);
    //id of a sensor is deviceid + contactid 
    //device is moduelnr
    //contact --
    //dit lukt bij esu want maar 1 bus..
    //niet bi marklin want 4 bussen of zeggen bus 1 device 0 en conatct 1000
    //                                                     1    conatct 1016 etc
    // bus 0                                               0 en conatct 0 - 15
    // bsu 2                                               0 ense 2000 ---
    //dus voor id de address offset er bij tellen dus
    //modulenr + contactnr
    //this.moduleNumber;

    //Marklin:
    //node in case of Link S88 else 0 when cs self
    //when 0 - 15 then Link s88 self so address offset should be 0 module id 0
    //when 1000 (bus 1) address offset 1000 module nr 0 - 31 sensor is module nr * 16 + port + offset
    //sensor is is offset + module nr * 16 + port
    // module number * 16 + port + address offset
    // address depends on the BUS as ther are 4 busses with differen address offsets        
    //contact id of the module (0- 15) has to be added to the (bus) adressoffset to get the sensor address so bus 1 is 1000
    //ESU 
    // node is 0
    //id is the ojbetc id isstart with 100
    //module number is id - offset (100) set by the esu
    // sensor id offset + module nr + module nr * 16 + port 
    //contact id is the module id * 16 +port ie 0 1st module, 1 2nd mod 16+0 is contact 1 on 2ndmod
    // 
    //second device is 101 so these then id's 16 = 31 addres offset is 100
    // is module number + 16 + address offset
    return sb;
  }

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
