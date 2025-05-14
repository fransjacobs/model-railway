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
import org.tinylog.Logger;

/**
 * Represents 1 Feedback Module (S88) with a number of ports (usually 16)
 */
public class FeedbackModuleBean {

  private Integer id;
  private Integer moduleNumber;
  private Integer portCount;
  private Integer addressOffset;
  private Integer identifier;
  private Integer busNumber;

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

  public Integer getBusNumber() {
    return busNumber;
  }

  public void setBusNumber(Integer busNumber) {
    this.busNumber = busNumber;
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

  public SensorBean getSensor(int port) {
    int sid;
    int offset = 0;
    String name;
    if (busNumber == null || busNumber < 0) {
      //Not part of a Bus. Check the Address offset if it need an offset
      if (addressOffset != null) {
        offset = addressOffset;
      }
      sid = offset + moduleNumber * portCount + port;
      name = "M" + String.format("%02d", moduleNumber) + "-C" + String.format("%02d", port);
    } else {
      //Part of a bus, there should be an offset...  
      if (addressOffset != null) {
        offset = addressOffset;
      } else {
        Logger.warn("Module connected to bus " + busNumber + " but bus address offset is not specified!");
      }
      sid = offset + moduleNumber * portCount + port;
      name = "B" + busNumber.toString() + "-M" + String.format("%02d", moduleNumber) + "-C" + String.format("%02d", port);
    }

    int status = ports[port];
    int prevStatus = prevPorts[port];

    SensorBean sb = new SensorBean(sid, moduleNumber, port, identifier, status, prevStatus);
    sb.setName(name);
    return sb;
  }

  public List<SensorBean> getSensors() {
    List<SensorBean> sensors = new ArrayList<>(ports.length);

    for (int i = 0; i < ports.length; i++) {
      SensorBean sb = getSensor(i);
      sensors.add(sb);
    }

    return sensors;
  }

  public List<SensorBean> getChangedSensors() {
    List<SensorBean> changedSensors = new ArrayList<>(ports.length);

    for (int i = 0; i < ports.length; i++) {
      if (ports[i] != prevPorts[i]) {
        SensorBean sb = getSensor(i);
        changedSensors.add(sb);
      }
    }
    return changedSensors;
  }

  public List<SensorEvent> getChangedSensorEvents() {
    List<SensorEvent> changedSensorEvents = new ArrayList<>(ports.length);

    for (int i = 0; i < ports.length; i++) {
      if (ports[i] != prevPorts[i]) {
        SensorBean sb = getSensor(i);
        SensorEvent se = new SensorEvent(sb);
        changedSensorEvents.add(se);
      }
    }
    return changedSensorEvents;
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
//
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
