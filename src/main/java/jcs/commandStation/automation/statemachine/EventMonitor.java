/*
 * Copyright 2026 frans.
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
package jcs.commandStation.automation.statemachine;

// Monitor thread - central event dispatcher

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class EventMonitor implements Runnable {
    private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private final Map<EventType, List<EventCallback>> subscribers = new ConcurrentHashMap<>();
    private volatile boolean running = true;
    
    /**
     * Subscribe to a specific event type
     */
    public void subscribe(EventType eventType, EventCallback callback) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                   .add(callback);
    }
    
    /**
     * Unsubscribe from an event type
     */
    public void unsubscribe(EventType eventType, EventCallback callback) {
        List<EventCallback> callbacks = subscribers.get(eventType);
        if (callbacks != null) {
            callbacks.remove(callback);
        }
    }
    
    /**
     * Post an event to the monitor
     */
    public void postEvent(Event event) {
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Monitor thread main loop
     */
    @Override
    public void run() {
        while (running) {
            try {
                Event event = eventQueue.poll(100, TimeUnit.MILLISECONDS);
                if (event != null) {
                    notifySubscribers(event);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Notify all subscribers of an event type
     */
    private void notifySubscribers(Event event) {
        List<EventCallback> callbacks = subscribers.get(event.getType());
        if (callbacks != null && !callbacks.isEmpty()) {
            for (EventCallback callback : callbacks) {
                try {
                    callback.onEvent(event);
                } catch (Exception e) {
                    System.err.println("Error in event callback: " + e.getMessage());
                }
            }
        }
    }
    
    public void shutdown() {
        running = false;
    }
}
