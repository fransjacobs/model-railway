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
package lan.wervel.jcs.repository;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import lan.wervel.jcs.common.TrackRepository;
import lan.wervel.jcs.repository.model.FeedbackModule;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class FeedbackTimerTask extends TimerTask {

  private static final long DELAY = 0L;
  private static final long PERIOD = 500L;

  private final Timer timer;
  private final TrackRepository repository;

  public FeedbackTimerTask(TrackRepository repository) {
    this.repository = repository;

    timer = new Timer(true);
    scheduleAtFixedRate(DELAY, PERIOD);
    Logger.info("Feedback Timer task started...");
  }

  private void scheduleAtFixedRate(long delay, long period) {
    timer.scheduleAtFixedRate(this, delay, period);
  }

  @Override
  public void run() {
    Map<Integer, FeedbackModule> fbm = repository.getFeedbackModules();

    for (FeedbackModule fm : fbm.values()) {
      fm.requestFeedback();
      Logger.trace("Feedback requested for module: " + fm.getModuleNumber());
    }
  }

  public void stopFeedbackTimerTask() {
    if (this.timer != null) {
      this.timer.purge();
      this.timer.cancel();
      Logger.info("Feedback Timer task cancelled...");
    }
  }
}
