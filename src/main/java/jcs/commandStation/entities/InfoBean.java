/*
 * Copyright 2024 Frans Jacobs.
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

import jakarta.persistence.Transient;
import java.util.Objects;
import jcs.entities.CommandStationBean;

/**
 * An InfoBean represents the basic information about the connected Command Station.
 */
public class InfoBean extends CommandStationBean {

  private String softwareVersion;
  private String hardwareVersion;
  private String serialNumber;
  private String productName;
  private String articleNumber;
  private String hostname;
  private String gfpUid;
  private String guiUid;
  private boolean supportMeasurements;

  public InfoBean() {

  }

  public InfoBean(CommandStationBean commandStationBean) {
    copyInto(commandStationBean);
  }

  public final void copyInto(CommandStationBean commandStationBean) {
    this.id = commandStationBean.getId();
    this.description = commandStationBean.getDescription();
    this.shortName = commandStationBean.getShortName();
    this.className = commandStationBean.getClassName();
    this.connectVia = commandStationBean.getConnectVia();
    this.serialPort = commandStationBean.getSerialPort();
    this.ipAddress = commandStationBean.getIpAddress();
    this.networkPort = commandStationBean.getNetworkPort();
    this.ipAutoConfiguration = commandStationBean.isIpAutoConfiguration();
    this.decoderControlSupport = commandStationBean.isDecoderControlSupport();
    this.accessoryControlSupport = commandStationBean.isAccessoryControlSupport();
    this.feedbackSupport = commandStationBean.isFeedbackSupport();
    this.locomotiveSynchronizationSupport = commandStationBean.isLocomotiveSynchronizationSupport();
    this.accessorySynchronizationSupport = commandStationBean.isAccessorySynchronizationSupport();
    this.locomotiveImageSynchronizationSupport = commandStationBean.isLocomotiveImageSynchronizationSupport();
    this.locomotiveFunctionSynchronizationSupport = commandStationBean.isLocomotiveFunctionSynchronizationSupport();
    this.protocols = commandStationBean.getProtocols();
    this.defaultCs = commandStationBean.isDefault();
    this.enabled = commandStationBean.isEnabled();
    this.lastUsedSerial = commandStationBean.getLastUsedSerial();
    this.supConnTypesStr = commandStationBean.getSupConnTypesStr();
    this.virtual = commandStationBean.isVirtual();
    this.feedbackModuleIdentifier = commandStationBean.getFeedbackModuleIdentifier();
    this.feedbackChannelCount = commandStationBean.getFeedbackChannelCount();
    this.feedbackBus0ModuleCount = commandStationBean.getFeedbackBus0ModuleCount();
    this.feedbackBus1ModuleCount = commandStationBean.getFeedbackBus1ModuleCount();
    this.feedbackBus2ModuleCount = commandStationBean.getFeedbackBus2ModuleCount();
    this.feedbackBus3ModuleCount = commandStationBean.getFeedbackBus3ModuleCount();
  }

  @Transient
  public String getSoftwareVersion() {
    return softwareVersion;
  }

  public void setSoftwareVersion(String softwareVersion) {
    this.softwareVersion = softwareVersion;
  }

  @Transient
  public String getHardwareVersion() {
    return hardwareVersion;
  }

  public void setHardwareVersion(String hardwareVersion) {
    this.hardwareVersion = hardwareVersion;
  }

  @Transient
  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  @Transient
  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  @Transient
  public String getArticleNumber() {
    return articleNumber;
  }

  public void setArticleNumber(String articleNumber) {
    this.articleNumber = articleNumber;
  }

  @Transient
  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  @Transient
  public String getGfpUid() {
    return gfpUid;
  }

  public void setGfpUid(String gfpUid) {
    this.gfpUid = gfpUid;
  }

  @Transient
  public String getGuiUid() {
    return guiUid;
  }

  public void setGuiUid(String guiUid) {
    this.guiUid = guiUid;
  }

  @Transient
  public boolean isSupportMeasurements() {
    return supportMeasurements;
  }

  public void setSupportMeasurements(boolean supportMeasurements) {
    this.supportMeasurements = supportMeasurements;
  }

  @Override
  public String toString() {
    return "InfoBean{" + "softwareVersion=" + softwareVersion + ", hardwareVersion=" + hardwareVersion + ", serialNumber=" + serialNumber + ", productName=" + productName + ", articleNumber=" + articleNumber + ", hostname=" + hostname + ", gfpUid=" + gfpUid + ", guiUid=" + guiUid + "}";
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + Objects.hashCode(this.softwareVersion);
    hash = 97 * hash + Objects.hashCode(this.hardwareVersion);
    hash = 97 * hash + Objects.hashCode(this.serialNumber);
    hash = 97 * hash + Objects.hashCode(this.productName);
    hash = 97 * hash + Objects.hashCode(this.articleNumber);
    hash = 97 * hash + Objects.hashCode(this.hostname);
    hash = 97 * hash + Objects.hashCode(this.gfpUid);
    hash = 97 * hash + Objects.hashCode(this.guiUid);
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
    final InfoBean other = (InfoBean) obj;
    if (!Objects.equals(this.softwareVersion, other.softwareVersion)) {
      return false;
    }
    if (!Objects.equals(this.hardwareVersion, other.hardwareVersion)) {
      return false;
    }
    if (!Objects.equals(this.serialNumber, other.serialNumber)) {
      return false;
    }
    if (!Objects.equals(this.productName, other.productName)) {
      return false;
    }
    if (!Objects.equals(this.articleNumber, other.articleNumber)) {
      return false;
    }
    if (!Objects.equals(this.hostname, other.hostname)) {
      return false;
    }
    if (!Objects.equals(this.gfpUid, other.gfpUid)) {
      return false;
    }
    return Objects.equals(this.guiUid, other.guiUid);
  }

}
