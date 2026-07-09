/*
 * Copyright 2026 Frans Jacobs.
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

import jcs.entities.LocomotiveBean;

/**
 * Commands for the RailwayController
 */
class RailControllerCommand {

  private final String command;
  private final LocomotiveBean locomotiveBean;
  private final String status;

  final static String CMD_START = "start";
  final static String CMD_STOP = "stop";
  final static String CMD_START_LOC = "startLocomotive";
  final static String CMD_STOP_LOC = "stopLocomotive";
  final static String CMD_START_ALL_LOC = "startAllLocomotives";
  final static String CMD_REMOVE_LOC = "removeLocomotive";
  final static String CMD_ADD_LOC = "addLocomotive";
  final static String CMD_RESET = "reset";
  final static String CMD_RESTORE_FUNC = "restoreFunctions";
  final static String CMD_PREP_DISP = "prepareDispatchers";
  final static String CMD_FIRE_STATUS_LST = "fireStatusListeners";

  RailControllerCommand(String command) {
    this(command, null, null);
  }

  RailControllerCommand(String command, String status) {
    this(command, null, status);
  }

  RailControllerCommand(String command, LocomotiveBean locomotiveBean) {
    this(command, locomotiveBean, false);
  }

  RailControllerCommand(String command, LocomotiveBean locomotiveBean, boolean force) {
    this(command, locomotiveBean, (force ? "true" : "false"));
  }

  RailControllerCommand(String command, LocomotiveBean locomotiveBean, String status) {
    this.command = command;
    this.locomotiveBean = locomotiveBean;
    this.status = status;
  }

  String getCommand() {
    return command;
  }

  LocomotiveBean getLocomotiveBean() {
    return locomotiveBean;
  }

  String getStatus() {
    return status;
  }

}
