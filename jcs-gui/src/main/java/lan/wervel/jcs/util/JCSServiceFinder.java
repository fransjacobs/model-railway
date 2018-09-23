package lan.wervel.jcs.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.pmw.tinylog.Logger;

public class JCSServiceFinder extends Thread {

  static final String DISCOVER_RESPONSE_MSG = "MARKLIN 6050/6051 Interface SerialPort Client Response";

  private int servicePort;

  private String hostAddress;

  private boolean serviceFound;

  private DatagramSocket socket;
  
  
  public JCSServiceFinder() {
    super();
  }

  @Override
  public void run() {
    Logger.debug("Trying to find a JCS Server...");
    discoverServer();
  }

  private void discoverServer() {
    // Find the server using UDP broadcast
    try {
      // Open a random port to send the package
      socket = new DatagramSocket();
      socket.setBroadcast(true);

      byte[] sendData = ServiceAnnouncer.DISCOVER_REQUEST_MSG.getBytes();

      // Try the 255.255.255.255 first
      try {
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                InetAddress.getByName("255.255.255.255"), ServiceAnnouncer.DEFAULT_ANNOUNCE_PORT);

        socket.send(sendPacket);

        Logger.debug("Request packet sent to: 255.255.255.255 (DEFAULT) on port "
                + ServiceAnnouncer.DEFAULT_ANNOUNCE_PORT);
      } catch (IOException e) {
        Logger.error(e);
      }

      // Check localhost first
      if (RunUtil.getOsType() == RunUtil.OS_MAC_OS_X) {
        try {
          DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                  InetAddress.getLocalHost(), ServiceAnnouncer.DEFAULT_ANNOUNCE_PORT);
          socket.send(sendPacket);

          Logger.debug("Request packet sent to: localhost on port "
                  + ServiceAnnouncer.DEFAULT_ANNOUNCE_PORT);

        } catch (IOException e) {
          // Ignore this
        }
      }

      // Broadcast the message over all the network interfaces
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface networkInterface = interfaces.nextElement();

        if (networkInterface.isLoopback() || !networkInterface.isUp()) {
          continue; // Don't want to broadcast to the loopback interface
        }

        networkInterface.getInterfaceAddresses().stream().map((interfaceAddress) -> interfaceAddress.getBroadcast()).filter((broadcast) -> !(broadcast == null)).map((broadcast) -> {
          Logger.debug("Trying: " + broadcast.getHostAddress());
          return broadcast;
        }).map((broadcast) -> {
          // Send the broadcast package!
          try {
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast,
                    ServiceAnnouncer.DEFAULT_ANNOUNCE_PORT);
            socket.send(sendPacket);
          } catch (IOException e) {
          }
          return broadcast;
        }).forEachOrdered((broadcast) -> {
          Logger.info("Request packet sent to: " + broadcast.getHostAddress() + "; Interface: "
                  + networkInterface.getDisplayName());
        });
      }

      Logger.debug("Done looping over all network interfaces. Now waiting for a reply!");

      // Wait for a response
      byte[] recvBuf = new byte[15000];
      DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
      socket.receive(receivePacket);

      // We have a response
      Logger.info("Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

      // Check if the message is correct
      String message = new String(receivePacket.getData()).trim();

      Logger.debug("Received: " + message);
      // Split the message in the response id and service port.
      if (message.length() > DISCOVER_RESPONSE_MSG.length()) {
        String responseId = message.substring(0, DISCOVER_RESPONSE_MSG.length());
        String srvPort = message.substring(DISCOVER_RESPONSE_MSG.length());
        srvPort = srvPort.replaceAll("__", "");

        if (DISCOVER_RESPONSE_MSG.equals(responseId)) {
          InetAddress ip = receivePacket.getAddress();
          this.hostAddress = ip.getHostAddress();
          this.servicePort = Integer.parseInt(srvPort);
          this.serviceFound = true;

          Logger.info("Service found on IP: " + this.hostAddress + " Service Port: " + this.servicePort);
        }
      }

      // Close the port!
      socket.close();
    } catch (IOException ex) {
      Logger.error(ex);
    }
  }

  public int getServicePort() {
    return servicePort;
  }

  public String getHostAddress() {
    return hostAddress;
  }

  public boolean isServiceFound() {
    return serviceFound;
  }

  public static void main(String[] a) {
    JCSServiceFinder jcsServiceFinder = new JCSServiceFinder();
    jcsServiceFinder.discoverServer();

    String serviceHost = jcsServiceFinder.getHostAddress();
    int servicePort = jcsServiceFinder.getServicePort();
    Logger.info("Found host " + serviceHost + " on port " + servicePort + " as serial port service...");
  }

}
