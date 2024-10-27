/*
 * Copyright 2023 Frans Jacobs.
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Deprecated
public class TrackPowerBean extends ControllableDevice {

  public enum Status {
    ON, OFF, UNKNOWN
  }

  public enum FeedbackSource {
    OTHER, CONTROLLER
  }

  private FeedbackSource feedbackSource;
  private Status status;
  private Date lastUpdated;

  public TrackPowerBean() {
    this(Status.UNKNOWN, FeedbackSource.OTHER);
  }

  public TrackPowerBean(Status status, FeedbackSource feedbackSource) {
    this(status, feedbackSource, null);
  }

  private TrackPowerBean(Status status, FeedbackSource feedbackSource, Date lastUpdated) {
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
//  public TrackPowerBean copy() {
//    return new TrackPowerBean(this.status, this.feedbackSource, this.lastUpdated);
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
    final TrackPowerBean other = (TrackPowerBean) obj;
    if (this.feedbackSource != other.feedbackSource) {
      return false;
    }
    if (this.status != other.status) {
      return false;
    }
    return Objects.equals(this.lastUpdated, other.lastUpdated);
  }

}
