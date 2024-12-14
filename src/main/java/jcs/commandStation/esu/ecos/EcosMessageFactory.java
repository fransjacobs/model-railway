/*
 * Copyright 2024 Frans Jacobs
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
package jcs.commandStation.esu.ecos;

/**
 * Prepare Messages for the ESU ECoS
 */
public class EcosMessageFactory implements Ecos {

  public static EcosMessage getBaseObject() {
    return new EcosMessage("get(1,objectclass,view,listview,control,list,size,minarguments,protocolversion"
            + ",commandstationtype,name,serialnumber,hardwareversion,applicationversion,applicationversionsuffix"
            + ",updateonerror,status,status2,prog-status,m4-status,railcomplus-status,watchdog,railcom,railcomplus"
            + ",railcomplus-range,railcomplus-mode,allowlocotakeover,stoponlastdisconnect)");
  }

  public static EcosMessage subscribeBaseObject() {
    return new EcosMessage("request(1, view)");
  }

  public static EcosMessage unSubscribeBaseObject() {
    return new EcosMessage("release(1, view)");
  }

  public static EcosMessage getPowerStatus() {
    return new EcosMessage("get(" + BASEOBJECT_ID + ", status, status2)");
  }

  public static EcosMessage setPowerStatus(boolean on) {
    return new EcosMessage("set(1, status[" + (on ? "GO" : "STOP") + "])");
  }

  public static EcosMessage getNumberOfFeedbackModules() {
    return new EcosMessage("get(26, size)");
  }

  public static EcosMessage getFeedbackModuleInfo(int moduleId) {
    return new EcosMessage("get(" + moduleId + ", state, ports)");
  }

  public static EcosMessage subscribeFeedbackModule(int moduleId) {
    return new EcosMessage("request(" + moduleId + ", view)");
  }

  public static EcosMessage unSubscribeFeedbackModule(int moduleId) {
    return new EcosMessage("release(" + moduleId + ", view)");
  }

  public static EcosMessage subscribeFeedbackManager() {
    return new EcosMessage("request(" + FEEDBACK_MANAGER_ID + ", view)");
  }

  public static EcosMessage unSubscribeFeedbackManager() {
    return new EcosMessage("release(" + FEEDBACK_MANAGER_ID + ", view)");
  }

  public static EcosMessage getLocomotives() {
    return new EcosMessage("queryObjects(" + LOCOMOTIVES_ID + ", name,addr,protocol)");
  }

  public static EcosMessage subscribeLokManager() {
    return new EcosMessage("request(" + LOCOMOTIVES_ID + ",view)");
  }

  public static EcosMessage unSubscribeLokManager() {
    return new EcosMessage("release(" + LOCOMOTIVES_ID + ",view)");
  }

  public static EcosMessage subscribeLocomotive(long lokId) {
    return new EcosMessage("request(" + lokId + ",view)");
  }

  public static EcosMessage unSubscribeLocomotive(long lokId) {
    return new EcosMessage("release(" + lokId + ",view)");
  }

  public static EcosMessage getLocomotiveDetails(long lokId) {
    return new EcosMessage("get(" + lokId + ",name,addr,protocol,dir,speed,speedstep,active,locodesc,func,funcdesc)");
  }

  public static EcosMessage getRequestLocomotiveControl(long lokId) {
    return new EcosMessage("request(" + lokId + ",control,force)");
  }

  public static EcosMessage getReleaseLocomotiveControl(long lokId) {
    return new EcosMessage("release(" + lokId + ",control)");
  }

  public static EcosMessage setLocomotiveSpeed(long lokId, int speed) {
    return new EcosMessage("set(" + lokId + ",speed[" + speed + "])");
  }

  public static EcosMessage setLocomotiveFunction(long lokId, int function, boolean active) {
    return new EcosMessage("set(" + lokId + ",func[" + function + "," + (active ? "1" : "0") + "])");
  }

  public static EcosMessage setLocomotiveDirection(long lokId, int dir) {
    return new EcosMessage("set(" + lokId + ",dir[" + dir + "])");
  }

//  Ecos commands
//  queryObjects
//  set        
//  get  
//  create  
// delete  
// request
}
