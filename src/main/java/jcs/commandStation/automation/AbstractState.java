/*
 * Copyright 2026 Frans Jacobs
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
package jcs.commandStation.automation;

import jcs.commandStation.automation.SensorMonitor;
import java.util.ArrayList;
import java.util.List;
import jcs.commandStation.automation.Dispatcher;
import jcs.commandStation.automation.state.SensorEventCallback;
import jcs.entities.AccessoryBean;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Base State
 */
abstract class AbstractState {
  protected final String name;
  protected SensorMonitor monitor;
  protected SensorEventCallback eventCallback;

  protected Dispatcher dispatcher;

  //protected final long waitInterval;
  //protected final long threadWaitMillis;

  protected boolean resetRequested;

  protected AbstractState(String name) {
    this.name = name;
    //waitInterval = Long.parseUnsignedLong(System.getProperty("autopilot.thread.wait.interval", "1000"));
    //threadWaitMillis = Long.parseUnsignedLong(System.getProperty("autopilot.thread.wait.millis", "10000"));
  }

  void onEnter(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;    
    this.monitor = dispatcher.getSensorMonitor();
    Logger.trace("Entering " + name + " state...");
  }

  abstract AbstractState execute();

  void onExit() {

  }

  /**
   * Override in states that need to subscribe to events
   */
//  protected void subscribeToEvents() {
//    // Default: no subscription
//  }

  /**
   * Override to unsubscribe from events
   */
//  protected void unsubscribeFromEvents() {
//    // Default: no action
//  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  public String getName() {
    return this.name;
  }

  public void setResetRequested(boolean resetRequested) {
    this.resetRequested = resetRequested;
  }

  void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

//  protected boolean isAllowed(boolean allowCommuter, boolean allowNonCommuter, boolean commuter) {
//    //both flags are the same → all trains allowed
//    if (allowCommuter == allowNonCommuter) {
//      return true;
//    }
//
//    // only non-commuter allowed
//    if (!allowCommuter && allowNonCommuter) {
//      return !commuter;
//    }
//
//    // only commuter allowed
//    if (allowCommuter && !allowNonCommuter) {
//      return commuter;
//    }
//
//    // Should never happen, but default deny
//    return false;
//  }

//  protected boolean turnoutsNotLocked(RouteBean route) {
//    List<RouteElementBean> turnouts = getTurnouts(route);
//
//    boolean switchesNotLocked = true;
//    for (RouteElementBean reb : turnouts) {
//      //AccessoryBean.AccessoryValue av = reb.getAccessoryValue();
//      AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
//      //check if the accessory is not set by an other reserved nextRoute
//      boolean locked = PersistenceFactory.getService().isAccessoryLocked(turnout.getId());
//      if (locked) {
//        Logger.debug("Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] is locked!");
//        return false;
//      }
//    }
//    Logger.trace("There are " + turnouts.size() + " free turnouts in this route");
//    return switchesNotLocked;
//  }

//  protected List<RouteElementBean> getTurnouts(RouteBean routeBean) {
//    List<RouteElementBean> rel = routeBean.getRouteElements();
//    List<RouteElementBean> turnouts = new ArrayList<>();
//    for (RouteElementBean reb : rel) {
//      if (reb.isTurnout()) {
//        turnouts.add(reb);
//      }
//    }
//    return turnouts;
//  }

}
