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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author frans
 */
public class EcosMessageFactory {

  private static final Map<String, String> txRx = new HashMap<>();

  public static EcosMessage getBaseObjectMessage() {
    return new EcosMessage("get(1,objectclass,view,listview,control,list,size,minarguments,protocolversion"
            + ",commandstationtype,name,serialnumber,hardwareversion,applicationversion,applicationversionsuffix"
            + ",updateonerror,status,status2,prog-status,m4-status,railcomplus-status,watchdog,railcom,railcomplus"
            + ",railcomplus-range,railcomplus-mode,allowlocotakeover,stoponlastdisconnect)");
  }

//  Ecos commands
//  queryObjects
//  set        
//  get  
//  create  
// delete  
// request
}
