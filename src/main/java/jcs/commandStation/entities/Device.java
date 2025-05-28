/*
 * Copyright 2025 fransjacobs.
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
package jcs.commandStation.entities;

import java.util.Objects;

/**
 * Command Station Device is an Object to be able to show the Devices that "live"<br>
 * inside a Command Station. Examples <br>
 * Marklin CS: <br>
 * - GFP internal booster device - Link S88 external feedback device ESU-ECoS:<br>
 * - Ecos device self / booster - Locomotive Manager - Accessories Manager - Feedback Manager
 */
public class Device {

  private String id;
  private String name;
  private String hardwareVersion;
  private String softwareVersion;
  private String serialNumber;

  private Integer size;
  private Integer channels;
  private boolean feedback;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHardwareVersion() {
    return hardwareVersion;
  }

  public void setHardwareVersion(String hardwareVersion) {
    this.hardwareVersion = hardwareVersion;
  }

  public String getSoftwareVersion() {
    return softwareVersion;
  }

  public void setSoftwareVersion(String softwareVersion) {
    this.softwareVersion = softwareVersion;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public Integer getChannels() {
    return channels;
  }

  public void setChannels(Integer channels) {
    this.channels = channels;
  }

  public boolean isFeedback() {
    return feedback;
  }

  public void setFeedback(boolean feedback) {
    this.feedback = feedback;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 67 * hash + Objects.hashCode(this.id);
    hash = 67 * hash + Objects.hashCode(this.name);
    hash = 67 * hash + Objects.hashCode(this.hardwareVersion);
    hash = 67 * hash + Objects.hashCode(this.softwareVersion);
    hash = 67 * hash + Objects.hashCode(this.serialNumber);
    hash = 67 * hash + Objects.hashCode(this.size);
    hash = 67 * hash + Objects.hashCode(this.channels);
    hash = 67 * hash + Objects.hashCode(this.feedback);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Device other = (Device) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.hardwareVersion, other.hardwareVersion)) {
      return false;
    }
    if (!Objects.equals(this.softwareVersion, other.softwareVersion)) {
      return false;
    }
    if (!Objects.equals(this.serialNumber, other.serialNumber)) {
      return false;
    }
    if (!Objects.equals(this.size, other.size)) {
      return false;
    }
    if (!Objects.equals(this.feedback, other.feedback)) {
      return false;
    }
    return Objects.equals(this.channels, other.channels);
  }

}
