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

// Abstract base state
abstract class State {
    protected final String name;
    protected EventMonitor monitor;
    protected EventCallback eventCallback;
    
    public State(String name) {
        this.name = name;
    }
    
    /**
     * Called when entering this state
     */
    public void onEnter(EventMonitor monitor) {
        this.monitor = monitor;
        System.out.println("Entering state: " + name);
    }
    
    /**
     * Called when exiting this state
     */
    public void onExit() {
        // Unsubscribe from events when leaving state
        if (monitor != null && eventCallback != null) {
            unsubscribeFromEvents();
        }
        System.out.println("Exiting state: " + name);
    }
    
    /**
     * Override in states that need to subscribe to events
     */
    protected void subscribeToEvents() {
        // Default: no subscription
    }
    
    /**
     * Override to unsubscribe from events
     */
    protected void unsubscribeFromEvents() {
        // Default: no action
    }
    
    /**
     * Execute state logic
     */
    public abstract State execute();
}
