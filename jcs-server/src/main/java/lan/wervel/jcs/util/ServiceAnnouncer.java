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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.pmw.tinylog.Logger;

public class ServiceAnnouncer extends Thread {

  static final String DISCOVER_REQUEST_MSG = "MARKLIN 6050/6051 Interface SerialPort Server Request";
  private static final String DISCOVER_RESPONSE_MSG = "MARKLIN 6050/6051 Interface SerialPort Client Response";

  public static final int DEFAULT_ANNOUNCE_PORT = 8188;

  private DatagramSocket socket;

  private static ServiceAnnouncer i6050ServiceAnnouncer;

  private final int announcePort;
  private final int servicePort;

  private ServiceAnnouncer(int announcePort, int servicePort) {
    this.announcePort = announcePort;
    this.servicePort = servicePort;
  }

  public static ServiceAnnouncer getInstance(int announcePort, int servicePort) {
    if (i6050ServiceAnnouncer == null) {
      i6050ServiceAnnouncer = new ServiceAnnouncer(announcePort, servicePort);
    }

    return i6050ServiceAnnouncer;
  }

  @Override
  public void run() {
    try {
      // Keep a socket open to listen to all the UDP traffic that is destined for this port
      InetAddress inetAddress = InetAddress.getByName("0.0.0.0");

      Logger.debug("Announce host: " + inetAddress.getHostName() + " Announce port: " + announcePort);
      socket = new DatagramSocket(announcePort, inetAddress);
      socket.setBroadcast(true);

      while (true) {
        Logger.info("Listening on port " + announcePort + ". Ready to receive broadcast packets!...");

        // Receive a packet
        byte[] recvBuf = new byte[15000];
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
        socket.receive(packet);

        // Packet received
        Logger.info("Discovery packet received from: " + packet.getAddress().getHostAddress());

        //int len = packet.getLength();
        String rxd = new String(packet.getData());

        Logger.info("Packet received; data: [" + rxd.trim() + "]");

        // See if the packet holds the right command (message)
        String message = rxd.trim(); //new String(packet.getData()).trim();
        if (DISCOVER_REQUEST_MSG.equals(message)) {

          String response = DISCOVER_RESPONSE_MSG + "__" + servicePort;

          byte[] sendData = response.getBytes();

          // Send a response
          DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(),
                  packet.getPort());
          socket.send(sendPacket);

          Logger.info("Replied [" + response + "] to: " + sendPacket.getAddress().getHostAddress());
        }
      }
    } catch (IOException ex) {
      Logger.error(ex);
    }
  }
}
