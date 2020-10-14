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
package lan.wervel.jcs.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class TrackPower extends ControllableDevice {

  public enum Status {
    ON, OFF, UNKNOWN
  }

  public enum FeedbackSource {
    OTHER, CONTROLLER
  }

  private FeedbackSource feedbackSource;
  private Status status;
  private Date lastUpdated;

  public TrackPower() {
    this(Status.UNKNOWN, FeedbackSource.OTHER);
  }

  public TrackPower(Status status, FeedbackSource feedbackSource) {
    this(status, feedbackSource, null);
  }

  private TrackPower(Status status, FeedbackSource feedbackSource, Date lastUpdated) {
    super(1, null);
    this.feedbackSource = feedbackSource;
    this.status = status;
    this.lastUpdated = lastUpdated;
    init();
  }

  private void init() {
    this.setId(new BigDecimal(this.address));
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.status);
    return sb.toString();
  }

  @Override
  public String toLogString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.status);
    return sb.toString();
  }

  public boolean isOn() {
    return Status.ON.equals(this.status);
  }

  public void On() {
    this.setStatus(Status.ON);
  }

  public void Off() {
    this.setStatus(Status.OFF);
  }

  public boolean isOff() {
    return Status.OFF.equals(this.status);
  }

  public boolean isFeedbackSourceOther() {
    return FeedbackSource.OTHER.equals(this.feedbackSource);
  }

  public boolean isFeedbackSourceController() {
    return FeedbackSource.CONTROLLER.equals(this.feedbackSource);
  }

  public void setFeedbackSource(String feedbackSource) {
    switch (feedbackSource) {
      case "CONTROLLER":
        setFeedbackSource(FeedbackSource.CONTROLLER);
        break;
      default:
        setFeedbackSource(FeedbackSource.OTHER);
        break;
    }
  }

  public void setStatus(String status) {
    switch (status) {
      case "ON":
        setStatus(Status.ON);
        break;
      case "OFF":
        setStatus(Status.OFF);
        break;
      default:
        setStatus(Status.UNKNOWN);
        break;
    }
  }

  public FeedbackSource getFeedbackSource() {
    return feedbackSource;
  }

  public void setFeedbackSource(FeedbackSource feedbackSource) {
    FeedbackSource oldFeedbackSource = this.feedbackSource;
    this.feedbackSource = feedbackSource;
    if (!this.feedbackSource.equals(oldFeedbackSource)) {
      this.lastUpdated = new Date();
    }
    this.feedbackSource = feedbackSource;
  }

  public void setStatus(Status status) {
    this.status = status;
    this.lastUpdated = new Date();
  }

  public Status getStatus() {
    return status;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

//  @Override
//  public TrackPower copy() {
//    return new TrackPower(this.status, this.feedbackSource, this.lastUpdated);
//  }

  public static FeedbackSource getFeedbackSource(String feedbackSource) {
    switch (feedbackSource) {
      case "CONTROLLER":
        return FeedbackSource.CONTROLLER;
      default:
        return FeedbackSource.OTHER;
    }
  }

  public static Status getStatusType(String status) {
    switch (status) {
      case "ON":
        return Status.ON;
      case "OFF":
        return Status.OFF;
      default:
        return Status.UNKNOWN;
    }
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 31 * hash + Objects.hashCode(this.feedbackSource);
    hash = 31 * hash + Objects.hashCode(this.status);
    hash = 31 * hash + Objects.hashCode(this.lastUpdated);
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
    final TrackPower other = (TrackPower) obj;
    if (this.feedbackSource != other.feedbackSource) {
      return false;
    }
    if (this.status != other.status) {
      return false;
    }
    return Objects.equals(this.lastUpdated, other.lastUpdated);
  }

}
