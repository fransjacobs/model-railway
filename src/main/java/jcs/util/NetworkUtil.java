/*
 * Copyright (C) 2024 Frans Jacobs.
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

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
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
          //Logger.trace("Evaluate "+netint.getName()+" Displayname: "+netint.getDisplayName());
          if (ia instanceof Inet4Address) {
            //Got an ip4 address, which kind?
            if (netint.getDisplayName().contains("wlan") || netint.getName().contains("wlan")) {
              wlan = ia;
            } else if (netint.getDisplayName().contains("eth") || netint.getName().contains("eth")) {
              eth = ia;
            } else if (netint.getDisplayName().contains("lo") || netint.getName().contains("lo")) {
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
        //Only localhost, usually on Mac OS Wifi.
        //Perform an ultimate attempt to get the host ip address by calling something...
        InetAddress la = null;
        try (final DatagramSocket socket = new DatagramSocket()) {
          socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
          la = socket.getLocalAddress();
          Logger.trace("Local address : " + socket.getLocalAddress().getHostAddress());
          socket.close();
        } catch (UnknownHostException ex) {
          Logger.error(ex.getMessage());
        }
        if (la != null) {
          return la;
        } else {
          Logger.trace("Interface lo: " + (lo != null ? lo.getHostAddress() : ""));
          return lo;
        }
      }
    } catch (SocketException se) {
      Logger.error(se);
    }
    return null;
  }
}
