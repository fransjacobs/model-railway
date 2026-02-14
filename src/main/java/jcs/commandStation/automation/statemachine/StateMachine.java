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

// State machine
class StateMachine implements Runnable {
    private State currentState;
    private final EventMonitor monitor;
    private volatile boolean running = true;
    
    public StateMachine(EventMonitor monitor, State initialState) {
        this.monitor = monitor;
        this.currentState = initialState;
        this.currentState.onEnter(monitor);
    }
    
    @Override
    public void run() {
        while (running) {
            State nextState = currentState.execute();
            
            if (nextState != currentState) {
                currentState.onExit();
                currentState = nextState;
                currentState.onEnter(monitor);
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public void shutdown() {
        running = false;
        if (currentState != null) {
            currentState.onExit();
        }
    }
}
