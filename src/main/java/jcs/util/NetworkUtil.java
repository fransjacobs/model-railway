/*
 * Copyright (C) 2020 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class NetworkUtil {

  public static InetAddress getIPv4HostAddress() {
    InetAddress wlan = null;
    InetAddress eth = null;
    InetAddress lo = null;

    try {
      Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
      for (NetworkInterface netint : Collections.list(nets)) {
        //Get all networks
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress ia : Collections.list(inetAddresses)) {
          if (ia instanceof Inet4Address) {
            //Got an ip4 address, which kind?
            if (netint.getDisplayName().contains("wlan")) {
              wlan = ia;
            } else if (netint.getDisplayName().contains("eth")) {
              eth = ia;
            } else if (netint.getDisplayName().contains("lo")) {
              lo = ia;
            }
          }
        }
      }
      if (eth != null) {
        Logger.trace("Interface eth: " + eth.getHostAddress());
        return eth;
      } else if (wlan != null) {
        Logger.trace("Interface wlan: " + wlan.getHostAddress());
        return wlan;
      } else {
        Logger.trace("Interface lo: " + (lo != null ? lo.getHostAddress() : ""));
        return lo;
      }
    } catch (SocketException se) {
      Logger.error(se);
    }
    return null;
  }
}
