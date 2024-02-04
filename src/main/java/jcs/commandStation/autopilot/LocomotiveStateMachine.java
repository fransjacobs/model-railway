/*
 * Copyright 2023 fransjacobs.
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
package jcs.commandStation.autopilot;

/**
 * @author fransjacobs
 */
public class LocomotiveStateMachine extends Thread { 


//what are the states
  
  // a loc can be in a block just stayng there: IDLE
  // When The auto drive in initiated a loc in a blok can become active so INIT
  // A lok can set a route PREPARING
  // A loc can start when the route is found and locked PREPARED
  // A lok can start driveing DRIVING
  // A lok has hit the enter block sensor BRAKING
  // A lok has hit the in or occupance sensor STOP
  // A lok has stopped in a blok and is waiting for a new adventure WAIT
  // A lok is decativated will not start again -> IDLE
  // A lok has wiated and can start again -> PREPARING
  // A lok is preparing but cant find a valid route -> WAIT



  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public void run() {
    super.run();
  }

  @Override
  public void start() {
    super.start();
  }


}
