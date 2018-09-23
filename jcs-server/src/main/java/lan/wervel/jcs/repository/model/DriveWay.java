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
package lan.wervel.jcs.repository.model;

import java.util.LinkedHashMap;
import java.util.Map;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry.StatusType;

/**
 *
 * @author frans
 */
public class DriveWay extends ControllableItem {

  private static final long serialVersionUID = -6567765870175512499L;

  private final Map<Integer, StatusType> accessoryConfig;
  private final Map<Integer, SolenoidAccessoiry> accessoiries;

  private String type;
  private TrackStatus trackStatus;

  public enum TrackStatus {
    FREE, OCCUPIED, ENTERING, ROUTING, INIT;
  }

  public DriveWay() {
    this(-1, null, null, null, null);
  }

  public DriveWay(Integer address, String name, String description, String type, Map<SolenoidAccessoiry, StatusType> accessoirySettings, TrackStatus trackStatus) {
    this(address, name, description, type, trackStatus);

    if (accessoirySettings != null && !accessoirySettings.isEmpty()) {
      for (SolenoidAccessoiry sa : accessoirySettings.keySet()) {
        accessoryConfig.put(sa.getAddress(), accessoirySettings.get(sa));
        accessoiries.put(sa.getAddress(), sa);
      }
    }
  }

  private DriveWay(Integer address, String name, String description, String type, TrackStatus trackStatus) {
    super(address, name, description, null);
    this.accessoryConfig = new LinkedHashMap<>();
    this.accessoiries = new LinkedHashMap<>();
    this.type = type;
    this.trackStatus = trackStatus;
  }

  public void addAccessoiry(SolenoidAccessoiry solenoidAccessoiry, StatusType preferedStatus) {
    accessoryConfig.put(solenoidAccessoiry.getAddress(), preferedStatus);
    accessoiries.put(solenoidAccessoiry.getAddress(), solenoidAccessoiry);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getSimpleName());
    sb.append(": ");
    sb.append(this.name);
    sb.append(":[");
    sb.append(this.address);
    sb.append("] ");
    sb.append(this.description);
    sb.append("; {");
    for (SolenoidAccessoiry sa : accessoiries.values()) {
      if (sa != null) {
        sb.append("(");
        sb.append((sa.isSignal() ? "S" : "T"));
        sb.append(";[");
        sb.append(sa.address);
        sb.append("]:");
        sb.append(accessoryConfig.get(sa.getAddress()));
        sb.append(")");
      }
    }
    sb.append("}");
    return sb.toString();
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isTrack() {
    return "track".equals(this.type);
  }

  public boolean isRoute() {
    return "route".equals(this.type);
  }

  public TrackStatus getTrackStatus() {
    return trackStatus;
  }

  public boolean isFree() {
    return TrackStatus.FREE.equals(this.trackStatus);
  }

  public boolean isEntering() {
    return TrackStatus.ENTERING.equals(this.trackStatus);
  }

  public boolean isOccupied() {
    return TrackStatus.OCCUPIED.equals(this.trackStatus);
  }

  public void setTrackStatus(TrackStatus trackStatus) {
    this.trackStatus = trackStatus;
  }

  public boolean isActive() {
    boolean active = true;
    for (SolenoidAccessoiry sa : accessoiries.values()) {
      StatusType status = accessoryConfig.get(sa.getAddress());
      if (!status.equals(sa.getStatus())) {
        active = false;
      }
    }
    return active;
  }

  public void activate() {
    for (SolenoidAccessoiry sa : this.accessoiries.values()) {
      if (sa != null) {
        StatusType status = this.accessoryConfig.get(sa.getAddress());
        sa.setStatus(status);
      }
    }
  }

  public void deActivate() {
    if ("track".equals(this.type)) {
      for (SolenoidAccessoiry sa : accessoiries.values()) {
        StatusType status = accessoryConfig.get(sa.getAddress());
        //revert the status of the driveway      
        switch (status) {
          case RED:
            sa.setStatus(StatusType.GREEN);
            break;
          case GREEN:
            sa.setStatus(StatusType.RED);
            break;
          default:
            sa.setStatus(StatusType.OFF);
            break;
        }
      }
    }
  }

  @Override
  public void addAttributeChangeListener(AttributeChangeListener listener) {
    for (SolenoidAccessoiry sa : accessoiries.values()) {
      sa.addAttributeChangeListener(listener);
    }
    super.addAttributeChangeListener(listener);
  }

  @Override
  public void removeAttributeChangeListener(AttributeChangeListener listener) {
    super.removeAttributeChangeListener(listener);
    for (SolenoidAccessoiry sa : accessoiries.values()) {
      sa.removeAttributeChangeListener(listener);
    }
  }

  @Override
  public DriveWay copy() {
    DriveWay dwc = new DriveWay(address, name, description, type, trackStatus);

    for (SolenoidAccessoiry sa : this.accessoiries.values()) {
      StatusType status = this.accessoryConfig.get(sa.getAddress());

      dwc.accessoiries.put(sa.getAddress(), sa.copy());
      dwc.accessoryConfig.put(sa.getAddress(), status);
    }

    return dwc;
  }

  public Map<Integer, SolenoidAccessoiry> getAccessoiries() {
    return accessoiries;
  }

  public boolean containsSolenoidAccessoiry(SolenoidAccessoiry solenoidAccessoiry) {
    return this.accessoiries.containsKey(solenoidAccessoiry.getAddress());
  }
}
