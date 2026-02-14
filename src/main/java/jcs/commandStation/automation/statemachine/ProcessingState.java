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

// Example: Processing state (no event subscription needed)
class ProcessingState extends State {
    private int processSteps = 0;
    
    public ProcessingState() {
        super("Processing");
    }
    
    @Override
    public State execute() {
        processSteps++;
        System.out.println("Processing step: " + processSteps);
        
        if (processSteps >= 3) {
            return new IdleState();
        }
        return this;
    }
}
