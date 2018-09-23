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

import java.util.Timer;
import java.util.TimerTask;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class TrackTimerTask extends TimerTask {

  private static final long DELAY = 0L;
  private static final long PERIOD = 3000L;

  private final Timer timer;
  private final TrackEventListener listener;

  public TrackTimerTask(TrackEventListener listener) {
    this.listener = listener;
    timer = new Timer(true);
    scheduleAtFixedRate(DELAY, PERIOD);
    Logger.info("Track Timer task started...");
  }

  private void scheduleAtFixedRate(long delay, long period) {
    timer.scheduleAtFixedRate(this, delay, period);
  }

  @Override
  public void run() {
    TrackEvent te = new TrackEvent(TrackEvent.EventType.TIMER);
    this.listener.trackStatusChanged(te);

  }

  public void stopFeedbackTimerTask() {
    if (this.timer != null) {
      this.timer.purge();
      this.timer.cancel();
      Logger.info("Track Timer task cancelled...");
    }
  }
}
