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
import java.util.concurrent.LinkedBlockingQueue;
import lan.wervel.jcs.repository.model.DriveWay;
import lan.wervel.jcs.common.TrackRepository;
import lan.wervel.jcs.controller.ControllerFactory;
import lan.wervel.jcs.repository.XmlRepository;
import lan.wervel.jcs.repository.model.DriveWay.TrackStatus;
import lan.wervel.jcs.repository.model.FeedbackModule;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry;
import lan.wervel.marklin.track.monitor.TrackEvent.EventType;
import org.pmw.tinylog.Logger;

public class ShadowStationThread extends Thread implements TrackEventListener {

  private final LinkedBlockingQueue<TrackEvent> eventQueue;

  private final List<DriveWay> tracks;

  private final DriveWay initialPath;
  private final DriveWay routeToTrack1;
  private final DriveWay routeToTrack2;
  private final DriveWay routeToTrack3;

  private final DriveWay track1;
  private final DriveWay track2;
  private final DriveWay track3;
  private final DriveWay entryTrack;

  private final TrackRepository repository;

  private boolean running = false;

  private int nextTrack = 1;
  private int TrackLeft = 0;
  private int allTrackOccupiedCount = 0;

  public ShadowStationThread(TrackRepository repository) {
    this.repository = repository;
    eventQueue = new LinkedBlockingQueue<>();
    tracks = new ArrayList<>();

    initialPath = repository.getDriveWay(1);
    routeToTrack1 = repository.getDriveWay(2);
    track1 = repository.getDriveWay(3);
    routeToTrack2 = repository.getDriveWay(4);
    track2 = repository.getDriveWay(5);
    routeToTrack3 = repository.getDriveWay(6);
    track3 = repository.getDriveWay(7);
    entryTrack = repository.getDriveWay(8);

    tracks.add(track1);
    tracks.add(track2);
    tracks.add(track3);
    tracks.add(entryTrack);

    nextTrack = 1;

  }

  @Override
  public void trackStatusChanged(TrackEvent evt) {
    if (!running) {
      return;
    }
    //Logger.debug("Queue size: "+this.eventQueue.size()+" Enqueing event: "+evt.getSource());
    this.eventQueue.offer(evt);
  }

  public List<DriveWay> getTracks() {
    return this.tracks;
  }

  public int getNextTrack() {
    return nextTrack;
  }

  private void handleStopEvent(TrackEvent evt) {
    running = false;
    eventQueue.clear();
    Logger.debug("Ending...");
  }

  private void handleStatusEvent(TrackEvent evt) {
    SolenoidAccessoiry sa = (SolenoidAccessoiry) evt.getSource();

    for (DriveWay track : tracks) {
      if (track.containsSolenoidAccessoiry(sa)) {
        track.getAccessoiries().get(sa.getAddress()).setStatus(sa.getStatus());
      }
    }
  }

  private void handleTimerEvent(TrackEvent evt) {
    //Check whether all tracks are occupied then activate a track
    if (track1.isOccupied() && track2.isOccupied() && track3.isOccupied()) {
      if (this.TrackLeft != this.nextTrack) {
        allTrackOccupiedCount++;
      }
      if (allTrackOccupiedCount > 2) {
        Logger.debug("All tracks occupied. For " + allTrackOccupiedCount + " counts. Activating track " + this.nextTrack);

        switch (nextTrack) {
          case 1:
            if (track1.isOccupied()) {
              track1.activate();
            }
            break;
          case 2:
            if (track2.isOccupied()) {
              track2.activate();
            }
            break;
          case 3:
            if (track3.isOccupied()) {
              track3.activate();
            }
            break;
          default:
            Logger.warn("Unknown track " + nextTrack);
            break;
        }
        this.TrackLeft = nextTrack;

        allTrackOccupiedCount = 0;
      } else {
        Logger.debug("All tracks occupied. For " + allTrackOccupiedCount + " counts. Next Track will be: " + this.nextTrack);
      }
    } else {
      allTrackOccupiedCount = 0;

      if (this.entryTrack.isOccupied() && !this.entryTrack.isActive()) {
        if (setRouteToFirstFreeTrack()) {
          this.entryTrack.activate();
          Logger.debug("Activated entry Track to " + this.findRouteToFirstFreeTrack());
        }
      } else {
        //Logger.debug("Track 1: " + track1.getTrackStatus() + " Track 2: " + track2.getTrackStatus() + " Track 3: " + track3.getTrackStatus());
        //Logger.trace("Not all tracks occupied. waiting");
      }
    }
  }

  private void handleDriveWayEvent(TrackEvent evt) {
    DriveWay dw = (DriveWay) evt.getSource();
  }

  private boolean setRouteToFirstFreeTrack() {
    if (track1.isFree()) {
      routeToTrack1.activate();
      Logger.debug("Route 2 Track 1 Activated");
      return true;
    } else if (track2.isFree()) {
      routeToTrack2.activate();
      Logger.debug("Route 2 Track 2 Activated");
      return true;
    } else if (track3.isFree()) {
      routeToTrack3.activate();
      Logger.debug("Route 2 Track 3 Activated");
      return true;
    } else {
      Logger.debug("No Free Track");
      return false;
    }
  }

  private int findRouteToFirstFreeTrack() {
    if (track1.isFree()) {
      return 1;
    } else if (track2.isFree()) {
      return 2;
    } else if (track3.isFree()) {
      return 3;
    } else {
      return 0;
    }
  }

  private void handleFeedbackEvent(TrackEvent evt) {
    FeedbackModule s88 = (FeedbackModule) evt.getSource();

    if (s88.isPort1() && !entryTrack.isActive()) {
      Logger.debug("Port 1; Train on Entry Track");
      entryTrack.setTrackStatus(TrackStatus.OCCUPIED);

      boolean trackFree = setRouteToFirstFreeTrack();

      if (trackFree) {
        entryTrack.activate();
      } else {
        entryTrack.deActivate();
        Logger.warn("There are no free tracks...");
      }
    }
    if (s88.isPort2() && track1.isActive()) {
      Logger.debug("Port 2; Train has left Track 1");
      track1.deActivate();
      track1.setTrackStatus(TrackStatus.FREE);
      nextTrack = 2;
    }
    if (s88.isPort3() && track2.isActive()) {
      Logger.debug("Port 3; Train has left Track 2");
      track2.deActivate();
      track2.setTrackStatus(TrackStatus.FREE);
      nextTrack = 3;
    }
    if (s88.isPort4() && track3.isActive()) {
      Logger.debug("Port 4; Train has left Track 3");
      track3.deActivate();
      track3.setTrackStatus(TrackStatus.FREE);
      nextTrack = 1;
    }
    if (s88.isPort5()) {
      Logger.debug("Port 5; Train has entered Track 1");
      track1.setTrackStatus(TrackStatus.OCCUPIED);
    } else {
      Logger.debug("Track 1 no longer occupied");
      track1.setTrackStatus(TrackStatus.FREE);
    }
    if (s88.isPort6()) {
      Logger.debug("Port 6; Train has entered Track 2");
      track2.setTrackStatus(TrackStatus.OCCUPIED);
    } else {
      Logger.debug("Track 2 no longer occupied");
      track2.setTrackStatus(TrackStatus.FREE);
    }
    if (s88.isPort7()) {
      Logger.debug("Port 7; Train has entered Track 3");
      track3.setTrackStatus(TrackStatus.OCCUPIED);
    } else {
      Logger.debug("Track 3 no longer occupied");
      track3.setTrackStatus(TrackStatus.FREE);
    }
    if (s88.isPort8()) {
      Logger.debug("Port 8; Train in entering Track 1");
      //Train came trough S12
      entryTrack.deActivate();
      track1.setTrackStatus(TrackStatus.ENTERING);
      entryTrack.setTrackStatus(TrackStatus.FREE);
    }
    if (s88.isPort9()) {
      Logger.debug("Port 9; Train in entering Track 2");
      //Train came trough S12
      entryTrack.deActivate();
      track2.setTrackStatus(TrackStatus.ENTERING);
      entryTrack.setTrackStatus(TrackStatus.FREE);
    }
    if (s88.isPort10()) {
      Logger.debug("Port; Train in entering Track 3");
      //Train came trough S12
      entryTrack.deActivate();
      track3.setTrackStatus(TrackStatus.ENTERING);
      entryTrack.setTrackStatus(TrackStatus.FREE);
    }
    if (s88.isPort11() && !entryTrack.isEntering()) {
      Logger.debug("Port 11; Train is approaching Entry Track");

      //Train is coming....
      entryTrack.setTrackStatus(TrackStatus.ENTERING);
      //Try to find a free track
      boolean trackFree = setRouteToFirstFreeTrack();

      if (trackFree) {
        entryTrack.activate();
      } else {
        entryTrack.deActivate();
        Logger.debug("Train is approaching, there are no free tracks...");
      }
    }
    if (s88.isPort12()) {
      Logger.debug("Port 12 is set");
      TrackLeft = 0;
      //Train left from track 1,2 or 3
      if (track1.isOccupied() && track2.isOccupied() && track3.isOccupied()) {
        Logger.warn("Train Left Tracks still occupied!");
        if (entryTrack.isOccupied()) {
          //possible collision!
          ControllerFactory.getController().powerOff();
          Logger.warn("Shutdown due to possible collission!");
        } else {
          //Try to resolve the issue
          if (track1.isActive() && track1.isOccupied()) {
            //track 1 active and occupied missed port 2
            track1.deActivate();
            track1.setTrackStatus(TrackStatus.FREE);
            nextTrack = 2;
            Logger.warn("Recovered Track 1!");
          }
          if (track2.isActive() && track2.isOccupied()) {
            //track 2 active and occupied missed port 3
            track2.deActivate();
            track2.setTrackStatus(TrackStatus.FREE);
            nextTrack = 3;
            Logger.warn("Recovered Track 2!");
          }
          if (track3.isActive() && track3.isOccupied()) {
            //track 3 active and occupied missed port 4
            track3.deActivate();
            track3.setTrackStatus(TrackStatus.FREE);
            nextTrack = 1;
            Logger.warn("Recovered Track 3!");
          }
        }
      } else {
        Logger.debug("Route anticipated to " + findRouteToFirstFreeTrack());
      }
    }
    if (s88.isPort13()) {
      Logger.debug("Port 13 is set");
    }
    if (s88.isPort14()) {
      Logger.debug("Port 13 is set");
    }
    if (s88.isPort15()) {
      Logger.debug("Port 15 is set");
    }
    if (s88.isPort16()) {
      Logger.debug("Port 16 is set");
    }
  }

  @Override
  public void run() {
    Logger.info("Handle thread initializing...");

    //Thy to get the status of the tracks...
    FeedbackModule fm = repository.getFeedbackModule(1);

    //register for events...
    if (repository instanceof XmlRepository) {
      ((XmlRepository) repository).addTrackEventListener(this);
    }

    initialPath.activate();
    Logger.debug("Initial route activated...");

    track1.setTrackStatus((fm.isPort5() ? TrackStatus.OCCUPIED : TrackStatus.FREE));
    track2.setTrackStatus((fm.isPort6() ? TrackStatus.OCCUPIED : TrackStatus.FREE));
    track3.setTrackStatus((fm.isPort7() ? TrackStatus.OCCUPIED : TrackStatus.FREE));
    entryTrack.setTrackStatus((fm.isPort1() ? TrackStatus.OCCUPIED : TrackStatus.FREE));

    Logger.debug("Track 1: " + track1.getTrackStatus() + " Track 2: " + track2.getTrackStatus() + " Track 3: " + track3.getTrackStatus());

    this.running = true;

    while (running) {
      try {
        //This will block until there is a event
        TrackEvent evt = eventQueue.take();

        EventType et = evt.getEventType();
        switch (et) {
          case FEEDBACK:
            Logger.trace("Dequeued FB Event: " + evt.getSource());
            this.handleFeedbackEvent(evt);
            break;
          case STATUS:
            Logger.trace("Dequeued SA Event: " + evt.getSource());
            this.handleStatusEvent(evt);
            break;
          case DRIVEWAY:
            Logger.trace("Dequeued DW Event: " + evt.getSource());
            this.handleDriveWayEvent(evt);
            break;
          case STOP:
            Logger.trace("Dequeued STOP Event");
            handleStopEvent(evt);
            break;
          case TIMER:
            Logger.trace("Dequeued TIMER Event");
            handleTimerEvent(evt);
            break;
          default:
            Logger.warn("Dequeued unknown Event, stop...");
            handleStopEvent(evt);
            break;
        }
      } catch (InterruptedException ex) {
        Logger.error(ex);
      }
    }

    if (repository instanceof XmlRepository) {
      ((XmlRepository) repository).removeTrackEventListener(this);
    }

    Logger.info("Thread stopped...");
  }
}
