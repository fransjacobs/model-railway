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

  public FeedbackModuleBean() {
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
    if (portCount != null) {
      if (this.ports == null) {
        ports = new int[portCount];
        prevPorts = new int[portCount];
      } else {
        if (ports.length != portCount) {
          ports = new int[portCount];
          prevPorts = new int[portCount];
        }
      }
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

  public int[] getPrevPorts() {
    return prevPorts;
  }

  public void setPrevPorts(int[] prevPorts) {
    this.prevPorts = prevPorts;
  }

  public SensorBean getSensor(int port) {
    SensorBean sb = new SensorBean(id, port, ports[port]);
    return sb;
  }

  public boolean isPort(int port) {
    if (ports != null && port < ports.length) {
      return this.ports[port] == 1;
    } else {
      return false;
    }
  }

  public List<SensorEvent> getChangedSensors() {
    List<SensorEvent> changedSensors = new ArrayList<>(ports.length);

    for (int i = 0; i < ports.length; i++) {
      if (ports[i] != prevPorts[i]) {
        SensorBean sb = new SensorBean(moduleNumber, i+1, ports[i]);
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
        SensorBean sb = new SensorBean(moduleNumber, i+1, ports[i]);
        sensors.add(sb);
      }
    }
    return sensors;
  }

  @Override
  public String toString() {
    return "FeedbackModuleBean{" + "id=" + id + ", moduleNumber=" + moduleNumber + ", portCount=" + portCount + ", addressOffset=" + addressOffset + ", identifier=" + identifier + "}";
  }

}
