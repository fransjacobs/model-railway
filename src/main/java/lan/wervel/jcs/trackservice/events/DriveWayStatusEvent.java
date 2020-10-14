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
package lan.wervel.jcs.trackservice.events;

import java.io.Serializable;
import lan.wervel.jcs.entities.ControllableDevice;

/**
 *
 * @author frans
 */
public class DriveWayStatusEvent implements Serializable {

  private final EventType eventType;
  private final ControllableDevice source;

  public enum EventType {
    STOP, FEEDBACK, STATUS, DRIVEWAY, TIMER;
  }

  public DriveWayStatusEvent() {
    this(EventType.STOP, null);
  }

  public DriveWayStatusEvent(EventType eventType) {
    this(eventType, null);
  }

  public DriveWayStatusEvent(EventType eventType, ControllableDevice source) {
    this.eventType = eventType;
    this.source = source;
  }

  public ControllableDevice getSource() {
    return source;
  }

  public EventType getEventType() {
    return eventType;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    switch (this.eventType) {
      case STOP:
        sb.append(this.eventType);
        break;
      case FEEDBACK:
        sb.append("Module {");
        sb.append(this.source);
        sb.append("}");
        break;
      case STATUS:
        sb.append("Accessoiry {");
        sb.append(this.source);
        sb.append("}");
        break;
      case TIMER:
        sb.append(this.eventType);
        break;
      default:
        sb.append(this.eventType);
        break;
    }
    return sb.toString();
  }

  boolean isStopEvent() {
    return EventType.STOP.equals(this.eventType);
  }

  boolean isFeedbackEvent() {
    return EventType.FEEDBACK.equals(this.eventType);
  }

  boolean isStatusEvent() {
    return EventType.STATUS.equals(this.eventType);
  }

  boolean isDriveWayEvent() {
    return EventType.DRIVEWAY.equals(this.eventType);
  }

  boolean isTimerEvent() {
    return EventType.TIMER.equals(this.eventType);
  }

}
