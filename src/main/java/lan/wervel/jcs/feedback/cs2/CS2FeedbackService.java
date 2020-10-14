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
package lan.wervel.jcs.feedback.cs2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import lan.wervel.jcs.controller.cs2.CanFeedbackEvent;
import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.controller.cs2.can.MarklinCan;
import lan.wervel.jcs.controller.cs2.events.CanMessageEvent;
import lan.wervel.jcs.controller.cs2.events.CanMessageListener;
import lan.wervel.jcs.controller.cs2.net.CS2ConnectionFactory;
import lan.wervel.jcs.feedback.FeedbackEvent;
import lan.wervel.jcs.feedback.FeedbackEventListener;
import lan.wervel.jcs.feedback.FeedbackService;
import org.pmw.tinylog.Logger;
import lan.wervel.jcs.feedback.FeedbackSampleListener;

public class CS2FeedbackService extends TimerTask implements FeedbackService {

    private static final long DELAY = 0L;

    private final Timer timer;

    private final List<FeedbackEventListener> feedbackEventListeners;
    private final List<FeedbackSampleListener> feedbackSampleEventListeners;

    private boolean running = false;

    @SuppressWarnings("LeakingThisInConstructor")
    public CS2FeedbackService() {
        CS2ConnectionFactory.getInstance().addCanMessageListener(new CanMessageListener() {
            @Override
            public void onCanMessage(CanMessageEvent canEvent) {
                parseMessage(canEvent);
            }
        });

        this.feedbackEventListeners = new ArrayList<>();
        this.feedbackSampleEventListeners = new ArrayList<>();

        timer = new Timer(true);
        this.running = true;
        scheduleAtFixedRate(DELAY, FeedbackService.DEFAULT_POLL_MILLIS);
    }

    private void parseMessage(CanMessageEvent canEvent) {
        CanMessage msg = canEvent.getCanMessage();

        switch (msg.getCommand()) {
            case MarklinCan.S88_EVENT_RESPONSE:
                CanFeedbackEvent cfe = new CanFeedbackEvent(canEvent.getCanMessage());
                handleFeedbackEvent(cfe);
                break;
            default:
            // Ignore Message...
            //Logger.trace(msg);
        }
    }

    private void handleFeedbackEvent(CanFeedbackEvent canFeedbackEvent) {
        Logger.trace(canFeedbackEvent);
        int contactId = canFeedbackEvent.getContactId();
        boolean value = canFeedbackEvent.isValue();
        boolean prevValue = canFeedbackEvent.isPreviousValue();
        FeedbackEvent fe = new FeedbackEvent(contactId, value, prevValue);
        
        Logger.debug(fe);

        for (FeedbackEventListener listener : feedbackEventListeners) {
            listener.notify(fe);
        }
    }

    private void scheduleAtFixedRate(long delay, long period) {
        timer.scheduleAtFixedRate(this, delay, period);
    }

    @Override
    public void run() {
        try {
            Set<FeedbackSampleListener> snapshot;
            synchronized (feedbackSampleEventListeners) {
                snapshot = new HashSet<>(feedbackSampleEventListeners);
            }

            for (FeedbackSampleListener listener : snapshot) {
                listener.sample();
            }
        } catch (Exception e) {
            Logger.error("Error in CS2FeedbackService poll task! " + e.getMessage());
            Logger.error("Quit polling!");

            this.stopFeedbackTimerTask();
        }
    }

    @Override
    public void addFeedbackSampleListener(FeedbackSampleListener listener) {
        synchronized (feedbackSampleEventListeners) {
            this.feedbackSampleEventListeners.add(listener);
        }
    }

    @Override
    public void removeFeedbackSampleListener(FeedbackSampleListener listener) {
        synchronized (feedbackSampleEventListeners) {
            this.feedbackSampleEventListeners.remove(listener);
        }
    }

    @Override
    public void removeAllFeedbackSampleListeners() {
        synchronized (feedbackSampleEventListeners) {
            this.feedbackSampleEventListeners.clear();
        }
    }

    public void stopFeedbackTimerTask() {
        if (this.timer != null) {
            this.timer.purge();
            this.timer.cancel();
            this.running = false;
            this.feedbackSampleEventListeners.clear();
            Logger.info("CS2FeedbackService poll task cancelled...");
        }
    }

    @Override
    public long getPollIntervalMillis() {
        return FeedbackService.DEFAULT_POLL_MILLIS;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void addFeedbackEventListener(FeedbackEventListener listener) {
        synchronized (feedbackEventListeners) {
            this.feedbackEventListeners.add(listener);
        }
    }

    @Override
    public void removeFeedbackEventListener(FeedbackEventListener listener) {
        synchronized (feedbackEventListeners) {
            this.feedbackEventListeners.remove(listener);
        }
    }

    @Override
    public void removeAllFeedbackEventListeners() {
        synchronized (feedbackEventListeners) {
            this.feedbackEventListeners.clear();
        }
    }

}
