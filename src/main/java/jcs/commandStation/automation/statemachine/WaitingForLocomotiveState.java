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

// Example: Waiting for locomotive arrival
class WaitingForLocomotiveState extends State {
    private volatile boolean locomotiveArrived = false;
    
    public WaitingForLocomotiveState() {
        super("WaitingForLocomotive");
    }
    
    @Override
    public void onEnter(EventMonitor monitor) {
        super.onEnter(monitor);
        locomotiveArrived = false;
        subscribeToEvents();
    }
    
    @Override
    protected void subscribeToEvents() {
        // Create callback and subscribe to event
        eventCallback = (event) -> {
            System.out.println("Locomotive arrived event received!");
            locomotiveArrived = true;
        };
        monitor.subscribe(EventType.LOCOMOTIVE_ARRIVED, eventCallback);
    }
    
    @Override
    protected void unsubscribeFromEvents() {
        monitor.unsubscribe(EventType.LOCOMOTIVE_ARRIVED, eventCallback);
    }
    
    @Override
    public State execute() {
        if (locomotiveArrived) {
            return new ProcessingState();
        }
        return this; // Stay in current state
    }
}

