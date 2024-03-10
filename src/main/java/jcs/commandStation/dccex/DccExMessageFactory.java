/*
 * Copyright 2023 frans.
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
package jcs.commandStation.dccex;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author frans
 */
public class DccExMessageFactory {

  private static final Map<String, String> txRx = new HashMap<>();

  static {
    txRx.put("0", "p");
    txRx.put("1", "p");
    txRx.put("J", "j");
    txRx.put("=", "=");
    txRx.put("D", "S");
    txRx.put("s", "i");
  }

  public static String getResponseOpcode(String txOpcode) {
    return txRx.get(txOpcode);
  }

  public static String getResponseOpcodeFor(String message) {
    String opcode = message.substring(1, 2);
    return txRx.get(opcode);
  }

  /**
   * Request Current on the Track(s)
   *
   * @return the dcc-ex message
   */
  static String trackCurrentRequest() {
    return "<c>";
  }

  /**
   * Request Current Status on the Track(s)
   *
   * @return the dcc-ex message
   */
  static String currentStatusRequest() {
    return "<J I>";
  }

  /**
   * Request max Current on the Track(s)
   *
   * @return the dcc-ex message
   */
  static String maxCurrentRequest() {
    return "<J G>";
  }

  /**
   * Request Track Manager Configuration
   *
   * @return the dcc-ex message
   */
  static String trackManagerConfigRequest() {
    return "<=>";
  }

  /**
   * Request the DCC-EX version and hardware info, along with listing defined turnouts
   *
   * @return the dcc-ex message
   */
  static String versionHarwareInfoRequest() {
    return "<s>";
  }

  /**
   * Request the number of supported cabs(locos)
   *
   * @return the dcc-ex message
   */
  static String supportedNumberOfCabsRequest() {
    return "<#>";
  }

  /**
   *
   * @param power true: On, false: Off
   * @return the dcc-ex message
   */
  static String changePowerRequest(boolean power) {
    StringBuilder sb = new StringBuilder();
    sb.append("<");
    sb.append((power ? "1" : "0"));
    sb.append(">");
    return sb.toString();
  }

  /**
   * Request a deliberate update on the cab (loco) speed/functions
   *
   * @param cabAddress the loco address
   * @return the dcc-ex message
   */
  static String cabUpdateRequest(int cabAddress) {
    StringBuilder sb = new StringBuilder();
    sb.append("<t ");
    sb.append(cabAddress);
    sb.append(">");
    return sb.toString();
  }

  /**
   * Set Cab (Loco) Speed
   *
   * @param cabAddress the loco address
   * @param newSpeed the new loco speed
   * @param direction the loco direction (1 forwards 0 backwards)
   * @return the dcc-ex message
   */
  static String cabChangeSpeedRequest(int cabAddress, int newSpeed, int direction) {
    StringBuilder sb = new StringBuilder();
    sb.append("<t ");
    sb.append(cabAddress);
    sb.append(" ");
    sb.append(newSpeed);
    sb.append(" ");
    sb.append(direction);
    sb.append(">");
    return sb.toString();
  }

  /**
   * Emergency Stop
   *
   * @return the dcc-ex message
   */
  static String emergencyStopRequest() {
    return "<!>";
  }

  /**
   * Turn loco decoder functions ON or OFF
   *
   * @param cabAddress
   * @param function
   * @param state
   * @return the dcc-ex message
   */
  static String cabChangeFunctionsRequest(int cabAddress, int function, boolean state) {
    StringBuilder sb = new StringBuilder();
    sb.append("<F ");
    sb.append(cabAddress);
    sb.append(" ");
    sb.append(function);
    sb.append(" ");
    sb.append((state ? "1" : "0"));
    sb.append(">");
    return sb.toString();
  }

  /**
   * Remove all locos from reminders
   *
   * @return the dcc-ex message
   */
  static String removeAllLocosRequest() {
    return "<->";
  }

  /**
   * Remove one loco from reminders
   *
   * @param cabAddress
   * @return the dcc-ex message
   */
  static String removeLocoRequest(int cabAddress) {
    StringBuilder sb = new StringBuilder();
    sb.append("<- ");
    sb.append(cabAddress);
    sb.append(">");
    return sb.toString();
  }

  /**
   * Switch to 28 speed steps
   *
   * @return the dcc-ex message
   */
  static String use28SpeedStepsRequest() {
    return "<D SPEED28>";
  }

  /**
   * Switch to 128 speed steps
   *
   * @return the dcc-ex message
   */
  static String use128SpeedStepsRequest() {
    return "<D SPEED128>";
  }

  /**
   * Store (save) this definition to EEPROM
   *
   * @return the dcc-ex message
   */
  static String store() {
    return "<E>";
  }

  /**
   * Deletes a turnout by Id
   *
   * @param id identifier of the Turnout/Point
   *
   * @return the dcc-ex message
   */
  static String delete(String id) {
    return "<T " + id + ">";
  }

  /**
   * Define turnout/point on a DCC Accessory Decoder with the specified address and subaddress
   *
   * @param id identifier of the Turnout/Point
   * @param address ranges from 0 to 511
   * @param subAddress ranges from 0 to 3
   * @return dcc-ex message
   */
  static String defineAccessory(String id, int address, int subAddress) {
    StringBuilder sb = new StringBuilder();
    sb.append("<T ");
    sb.append(id);
    sb.append(" DCC ");
    sb.append(address);
    sb.append(" ");
    sb.append(subAddress);
    sb.append(">");
    return sb.toString();
  }

  /**
   * Define turnout/point on a DCC Accessory Decoder with the specified linear address
   *
   * @param id identifier of the Turnout/Point
   * @param address ranges from 1 (address 1/subaddress 0) to 2044 (address 511/subaddress 3)
   * @return dcc-ex message
   */
  static String defineAccessory(String id, int address) {
    StringBuilder sb = new StringBuilder();
    sb.append("<T ");
    sb.append(id);
    sb.append(" DCC ");
    sb.append(address);
    sb.append(">");
    return sb.toString();
  }

  /**
   * Request a list all defined turnouts/Points
   *
   * @return dcc-ex message
   */
  static String requestDefinedAccessoryList() {
    return "<T>";
  }

  /**
   * Throw or Close a defined turnout/point
   *
   * @param id identifier of the Turnout/Point
   * @param state one of: 1 = Throw, T = Throw, 0 = Close, C = Close, X = eXamine
   * @return dcc-ex message
   */
  static String setAccessoryState(String id, String state) {
    return "<T " + id + " " + state + ">";
  }

  /**
   * Request details of a specific Turnout/Point
   *
   * @param id unique id of the Turnout/Point
   * @return dcc-ex message
   */
  static String requestDefinedAccessoryState(String id) {
    return "J T " + id + ">";
  }

  /**
   * Request the list of defined turnout/Point IDs
   *
   * @return dcc-ex message
   */
  static String requestDefinedAccessoryIdList() {
    return "<J T>";
  }

  /**
   * Control an Accessory Decoder with Address and Subaddress
   *
   * @param address the primary address of the decoder controlling the turnout (0-511)
   * @param subAddress the subaddress of the decoder controlling this turnout (0-3)
   * @param activate one of 0=off, deactivate, straight or Closed; 1=on, activate, turn or thrown
   * @return dcc-ex message
   */
  static String activateAccessory(int address, int subAddress, String activate) {
    StringBuilder sb = new StringBuilder();
    sb.append("<a ");
    sb.append(address);
    sb.append(" ");
    sb.append(subAddress);
    sb.append(" ");
    sb.append(activate);
    sb.append(">");
    return sb.toString();
  }

  /**
   * Control an Accessory Decoder with linear address
   *
   * @param linear address of the decoder controlling this turnout (1-2044)
   * @param activate one of 0=off, deactivate, straight or Closed; 1=on, activate, turn or thrown
   * @return dcc-ex message
   */
  static String activateAccessory(int address, String activate) {
    StringBuilder sb = new StringBuilder();
    sb.append("<a ");
    sb.append(address);
    sb.append(" ");
    sb.append(activate);
    sb.append(">");
    return sb.toString();
  }

}
