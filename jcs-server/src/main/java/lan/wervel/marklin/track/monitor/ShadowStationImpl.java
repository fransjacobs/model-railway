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
package lan.wervel.marklin.track.monitor;

import java.util.ArrayList;
import java.util.List;
import lan.wervel.jcs.common.ControllerProvider;
import lan.wervel.jcs.common.TrackRepository;
import lan.wervel.jcs.controller.ControllerFactory;
import lan.wervel.jcs.repository.RepositoryFactory;
import lan.wervel.jcs.repository.model.DriveWay;
import org.pmw.tinylog.Logger;

public class ShadowStationImpl implements TrackMonitor {

  private final TrackRepository repository;

  private final ControllerProvider controller;

  private ShadowStationThread shadowStationThread;
  private TrackTimerTask trackTimer;

  public ShadowStationImpl() {
    repository = RepositoryFactory.getRepository();
    controller = ControllerFactory.getController();
  }

  @Override
  public void startProcess() {
    if (shadowStationThread != null) {
      Logger.debug("Thread already created...");
      return;
    }

    Logger.info("Starting...");
    shadowStationThread = new ShadowStationThread(this.repository);

    Logger.debug("Starting thread...");
    shadowStationThread.start();
    trackTimer = new TrackTimerTask(shadowStationThread);
  }

  @Override
  public void stopProcess() {
    Logger.info("Stopping...");
    trackTimer.stopFeedbackTimerTask();
    //need a stop event...
    TrackEvent stopEvt = new TrackEvent();
    this.shadowStationThread.trackStatusChanged(stopEvt);
    this.shadowStationThread = null;
    trackTimer = null;
  }

  @Override
  public List<DriveWay> getTracks() {
    if (this.shadowStationThread != null) {
      return this.shadowStationThread.getTracks();
    } else {
      List<DriveWay> tracks = new ArrayList<>();
      tracks.addAll(repository.getDriveWayTracks().values());
      return tracks;
    }
  }

  public void setTracks(List<DriveWay> tracks) {

  }

  @Override
  public boolean isRunning() {
    return this.shadowStationThread != null;
  }

  @Override
  public Integer getNextTrack() {
    if (this.shadowStationThread != null) {
      return this.shadowStationThread.getNextTrack();
    } else {
      return 0;
    }
  }

}
