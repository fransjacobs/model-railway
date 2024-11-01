/*
 * Copyright 2025 Frans Jacobs.
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

public interface Ecos {

  public static final int BASEOBJECT_ID = 1;
  public static final int LOCOMOTIVES_ID = 10;
  public static final int ACCESSORIES_ID = 11;
  public static final int COMMUTER_ID = 12;
  public static final int DEVICE_MANAGER_ID = 20;
  public static final int SNIFFER_ID = 25;
  public static final int FEEDBACK_MANAGER_ID = 26;
  public static final int BOOSTER_ID = 26;
  public static final int CONTROL_PANEL_ID = 31;

  public static final String CMD_QUERY = "queryObjects";
  public static final String CMD_SET = "set";
  public static final String CMD_GET = "get";
  public static final String CMD_CREATE = "create";
  public static final String CMD_DELETE = "delete";
  public static final String CMD_REQUEST = "request";
  public static final String CMD_RELEASE = "release";

  public static final String OBJECTCLASS = "objectclass";
  public static final String VIEW = "view";
  public static final String LISTVIEW = "listview";
  public static final String CONTROL = "control";
  public static final String LIST = "list";
  public static final String SIZE = "size";
  public static final String MINARGUMENTS = "minarguments";
  public static final String PROTOCOLVERSION = "protocolversion";
  public static final String COMMANDSTATIONTYPE = "commandstationtype";
  public static final String NAME = "name";
  public static final String SERIALNUMBER = "serialnumber";
  public static final String HARDWAREVERSION = "hardwareversion";
  public static final String APPLICATIONVERSION = "applicationversion";
  public static final String APPLICATIONVERSIONSUFFIX = "applicationversionsuffix";
  public static final String UPDATEONERROR = "updateonerror";
  public static final String STATUS = "status";
  public static final String STATUS2 = "status2";
  public static final String PROG_STATUS = "prog-status";
  public static final String M4_STATUS = "m4-status";
  public static final String RAILCOMPLUS_STATUS = "railcomplus-status";
  public static final String WATCHDOG = "watchdog";
  public static final String RAILCOM = "railcom";
  public static final String RAILCOMPLUS = "railcomplus";
  public static final String RAILCOMPLUS_RANGE = "railcomplus-range";
  public static final String RAILCOMPLUS_MODE = "railcomplus-mode";
  public static final String ALLOWLOCOTAKEOVER = "allowlocotakeover";
  public static final String STOPONLASTCONNECT = "stoponlastdisconnect";
  
  public static final String STATE = "state";
  public static final String PORTS = "ports";
  
  

}
