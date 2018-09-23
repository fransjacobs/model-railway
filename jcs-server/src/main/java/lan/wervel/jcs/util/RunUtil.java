/*
 * Copyright (C) 2018 Frans Jacobs.
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
package lan.wervel.jcs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.pmw.tinylog.Logger;

import jssc.SerialPortList;

@SuppressWarnings("unused")
public class RunUtil {

  public static final int OS_LINUX = 0;
  public static final int OS_WINDOWS = 1;
  public static final int OS_SOLARIS = 2;
  public static final int OS_MAC_OS_X = 3;

  public static final String MAC_PORT = "/dev/tty.usbserial";
  public static final String LINUX_PORT = "/dev/ttyUSB0";
  // public static final String LINUX_PORT = "/dev/ttyAMA0";
  public static final String WIN_PORT = "COM4";

  private static int osType = -1;

  private static Set<String> serialPorts = new HashSet<String>();

  static {
    String osName = System.getProperty("os.name");
    String architecture = System.getProperty("os.arch");
    String userHome = System.getProperty("user.home");

    String fileSeparator = System.getProperty("file.separator");
    String tmpFolder = System.getProperty("java.io.tmpdir");

    String libRootFolder = new File(userHome).canWrite() ? userHome : tmpFolder;

    String javaLibPath = System.getProperty("java.library.path");

    if (osName.equals("Linux")) {
      osName = "linux";
      osType = OS_LINUX;
    } else if (osName.startsWith("Win")) {
      osName = "windows";
      osType = OS_WINDOWS;
    } else if (osName.equals("SunOS")) {
      osName = "solaris";
      osType = OS_SOLARIS;
    } else if (osName.equals("Mac OS X") || osName.equals("Darwin")) {
      osName = "mac_os_x";
      osType = OS_MAC_OS_X;
    }

    if (architecture.equals("i386") || architecture.equals("i686")) {
      architecture = "x86";
    } else if (architecture.equals("amd64") || architecture.equals("universal")) {
      architecture = "x86_64";
    } else if (architecture.equals("arm")) {
      String floatStr = "sf";
      if (javaLibPath.toLowerCase().contains("gnueabihf") || javaLibPath.toLowerCase().contains("armhf")) {
        floatStr = "hf";
      } else {
        try {
          Process readelfProcess = Runtime.getRuntime().exec("readelf -A /proc/self/exe");
          BufferedReader reader = new BufferedReader(new InputStreamReader(readelfProcess.getInputStream()));
          String buffer = "";
          while ((buffer = reader.readLine()) != null && !buffer.isEmpty()) {
            if (buffer.toLowerCase().contains("Tag_ABI_VFP_args".toLowerCase())) {
              floatStr = "hf";
              break;
            }
          }
          reader.close();
        } catch (Exception ex) {
          // Do nothing
        }
      }
      architecture = "arm" + floatStr;
    }

    listPorts();
    // Preference storage
    // Mac OS ~/Library/Preferences/com.apple.java.util.prefs.plist
    Logger.info("Running on OS: " + osName + " architecture: " + architecture + ".");

  }

  /**
   * Get OS type (OS_LINUX || OS_WINDOWS || OS_SOLARIS || OS_MAC_OS_X) *
   */
  public static int getOsType() {
    return osType;
  }

  public static boolean hasSerialPort() {
    return !serialPorts.isEmpty();
  }

  private static void listPorts() {
    String[] portNames = SerialPortList.getPortNames();
    for (String portName : portNames) {
      // For now ignore the raspberry pi default port as currently we only use an USB
      // serial port
      switch (portName) {
        case "/dev/ttyS0":
          Logger.debug("Skipping " + portName);
          break;
        case "/dev/ttyAMA0":
          Logger.debug("Skipping " + portName);
          break;
        default:
          serialPorts.add(portName);
          break;
      }
    }
  }

  /**
   * Based on the OS try to select a default (existing) port name. This method assumes a USB Serial Port Adapter is used. When a
   * port can't be determined the first in the list of port names is returned
   *
   * @return a string which contains a default existing port
   */
  public static String getDefaultPortname() {
    String os = System.getProperty("os.name");
    if (serialPorts == null || serialPorts.isEmpty()) {
      listPorts();
    }

    String portName = null;
    if (serialPorts.size() == 1) {
      portName = serialPorts.iterator().next();
    } else {
      if (os.contains("Linux")) {
        portName = LINUX_PORT;
      } else if (os.contains("Mac OS X")) {
        portName = MAC_PORT;
      } else if (os.contains("Windows")) {
        portName = WIN_PORT;
      } else {
        Logger.error("No default ports found for OS " + os + "!");
      }
    }
    return portName;
  }

  private static InetAddress getInetAddress(NetworkInterface networkInterface) {
    InetAddress inetAddress = null;
    for (Enumeration<InetAddress> inetAddrs = networkInterface.getInetAddresses(); inetAddrs.hasMoreElements();) {
      inetAddress = (InetAddress) inetAddrs.nextElement();
      if (!inetAddress.isLoopbackAddress()) {
        if (inetAddress.isSiteLocalAddress()) {
          // Found non-loopback site-local address. Return it immediately...
          return inetAddress;
        }
      }
    }
    return inetAddress;
  }

  /**
   * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
   * <p/>
   * This method is intended for use as a replacement of JDK method <code>InetAddress.getLocalHost</code>, because that method is
   * ambiguous on Linux systems. Linux systems enumerate the loopback network interface the same way as regular LAN network
   * interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not specify the algorithm used to select the address
   * returned under such circumstances, and will often return the loopback address, which is not valid for network communication.
   * Details <a href=
   * "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
   * <p/>
   * This method will scan all IP addresses on all network interfaces on the host machine to determine the IP address most likely to
   * be the machine's LAN address. If the machine has multiple IP addresses, this method will prefer a site-local IP address (e.g.
   * 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and will return the first site-local address if the machine has
   * more than one), but if the machine does not hold a site-local address, this method will return simply the first non-loopback
   * address found (IPv4 or IPv6).
   * <p/>
   * If this method cannot find a non-loopback address using this selection algorithm, it will fall back to calling and returning
   * the result of JDK method <code>InetAddress.getLocalHost</code>.
   * <p/>
   *
   * @return @throws UnknownHostException If the LAN address of the machine cannot be found.
   */
  public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
    try {
      InetAddress candidateAddress = null;
      // Iterate all NICs (network interface cards)...
      // In OSX use the first interface if available as that is the wired one
      if (getOsType() == OS_MAC_OS_X) {
        // Try to find the address of en0 which is probably the wired one...
        for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
                .hasMoreElements();) {
          NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
          //Logger.debug("Interface: " + iface.getDisplayName());
          if ("en0".equals(iface.getDisplayName())) {
            // This is probably the wired one...
            InetAddress inetAddress = getInetAddress(iface);
            if (inetAddress != null) {
              return inetAddress;
            }
          }
        }
      }

      // Not OSX or no address found
      for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
              .hasMoreElements();) {
        NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
        //Logger.debug("Iface: " + iface.getDisplayName());
        // Just iterate through all interfaces and iterate all IP addresses assigned to
        // each card...
        for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
          InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
          if (!inetAddr.isLoopbackAddress()) {
            if (inetAddr.isSiteLocalAddress()) {
              // Found non-loopback site-local address. Return it immediately...
              return inetAddr;
            } else if (candidateAddress == null) {
              // Found non-loopback address, but not necessarily site-local.
              // Store it as a candidate to be returned if site-local address is not
              // subsequently found...
              candidateAddress = inetAddr;
              // Note that we don't repeatedly assign non-loopback non-site-local addresses as
              // candidates,
              // only the first. For subsequent iterations, candidate will be non-null.
            }
          }
        }
        if (candidateAddress != null) {
          // We did not find a site-local address, but we found some other non-loopback
          // address.
          // Server might have a non-site-local address assigned to its NIC (or it might
          // be running
          // IPv6 which deprecates the "site-local" concept).
          // Return this non-loopback candidate address...
          return candidateAddress;
        }
        // At this point, we did not find a non-loopback address.
        // Fall back to returning whatever InetAddress.getLocalHost() returns...
        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
        if (jdkSuppliedAddress == null) {
          throw new UnknownHostException(
                  "The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
        }
        return jdkSuppliedAddress;
      }
      return null;
    } catch (SocketException | UnknownHostException e) {
      UnknownHostException unknownHostException = new UnknownHostException(
              "Failed to determine LAN address: " + e);
      unknownHostException.initCause(e);
      throw unknownHostException;
    }
  }

}
