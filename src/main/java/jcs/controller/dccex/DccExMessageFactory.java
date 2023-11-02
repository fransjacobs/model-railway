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
package jcs.controller.dccex;

/**
 *
 * @author frans
 */
public class DccExMessageFactory {

  /**
   * Request Current on the Track(s)
   *
   * @return the dcc-ex message
   */
  static String trackCurrentRequest() {
    return "<c>";
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

}
