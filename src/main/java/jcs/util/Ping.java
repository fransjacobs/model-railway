/*
 * Copyright 2023 Frans Jacobs.
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
package jcs.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.tinylog.Logger;

/**
 * Network ping test to check the availability of a Command Station
 */
public class Ping {

  public static int DEFAULT_TIMEOUT = 2000;

  public static Boolean isReachable(String ipAddress) {
    return isReachable(ipAddress, DEFAULT_TIMEOUT);
  }

  public static Boolean isReachable(String ipAddress, int timeout) {
    Boolean reachable = false;
    try {
      InetAddress inet = InetAddress.getByName(ipAddress);
      reachable = inet.isReachable(timeout);
    } catch (UnknownHostException e) {
      Logger.trace(e.getMessage() + " Can't Reach Host: " + ipAddress);
    } catch (IOException e) {
      Logger.trace(e.getMessage() + " Error in reaching Host: " + ipAddress);
    }
    Logger.trace("Host: " + ipAddress + " is " + (reachable ? "" : "not ") + "reachable");
    return reachable;
  }
}
