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

// Demo application
public class StateMachineDemo {
    public static void main(String[] args) throws InterruptedException {
        EventMonitor monitor = new EventMonitor();
        Thread monitorThread = new Thread(monitor, "EventMonitor");
        monitorThread.start();
        
        StateMachine stateMachine = new StateMachine(monitor, new WaitingForLocomotiveState());
        Thread smThread = new Thread(stateMachine, "StateMachine");
        smThread.start();
        
        // Simulate external event after 2 seconds
        Thread.sleep(2000);
        System.out.println("\n>>> Posting LOCOMOTIVE_ARRIVED event <<<\n");
        monitor.postEvent(new Event(EventType.LOCOMOTIVE_ARRIVED, "Loco-123"));
        
        // Let it run for a bit
        Thread.sleep(3000);
        
        // Shutdown
        stateMachine.shutdown();
        monitor.shutdown();
        smThread.join();
        monitorThread.join();
        
        System.out.println("Demo completed");
    }
}
