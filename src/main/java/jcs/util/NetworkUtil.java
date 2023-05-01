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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class NetworkUtil {

  private static final String IP4_PATTERN = "\\d+(\\.\\d+){3}";

  /**
   * Return this server's InetAddress
   *
   * @return InetAddress of this server or null address cannot be obtained.
   */
  public static InetAddress getMyAddress() {
    InetAddress inetAddr = null;
    List<InetAddress> addrs = getAllAddresses();
    // try to choose a non-local IPv4 address
    for (InetAddress addr : addrs) {
      if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
        continue;
      }
      if (addr.getHostAddress().matches(IP4_PATTERN)) {
        return addr;
      }
    }
    // didn't find a match. Try LocalHost address.
    try {
      inetAddr = InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      Logger.error(e.getMessage());
    }
    return inetAddr;
  }

  /**
   * Return all active addresses of this server, except loopback address.
   *
   * @return
   */
  public static List<InetAddress> getAllAddresses() {
    List<InetAddress> addrlist = new ArrayList<>();
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface iface = interfaces.nextElement();
        // filters out 127.0.0.1 and inactive interfaces
        if (iface.isLoopback() || !iface.isUp()) {
          continue;
        }

        Enumeration<InetAddress> addresses = iface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          InetAddress ipaddr = addresses.nextElement();
          addrlist.add(ipaddr);
          //Logger.trace(ipaddr);
        }
      }
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
    return addrlist;
  }

  public static InetAddress getIPv4InetAddress() throws SocketException, UnknownHostException {
    InetAddress wlan = null;
    InetAddress eth = null;
    InetAddress lo = null;

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
      Logger.trace("Inteface eth: " + eth.getHostAddress());
      return eth;
    } else if (wlan != null) {
      Logger.trace("Inteface wlan: " + wlan.getHostAddress());
      return wlan;
    } else {
      Logger.trace("Inteface lo: " + (lo != null ? lo.getHostAddress() : ""));
      return lo;
    }
  }

  /**
   * For testing.
   *
   * @param args
   */
  public static void main(String[] args) {
    InetAddress myAddress = getMyAddress();

    System.out.println("My IP Address: " + myAddress.getHostAddress());

    List<InetAddress> addrs = getAllAddresses();
    System.out.println("IP Addresses: ");
    for (InetAddress addr : addrs) {
      System.out.println(" " + addr.getHostAddress());
      if (addr.isLinkLocalAddress()) {
        System.out.println(" (LinkLocal)");
      }
      if (addr.isLoopbackAddress()) {
        System.out.println(" (Loopback)");
      }
    }
    System.out.println();
  }
}
