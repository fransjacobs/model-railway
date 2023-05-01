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
package jcs.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import jcs.controller.cs3.net.CS3Connection;

/**
 *
 * @author frans
 */
public class TestUDPServer {

  public static void main(String s[]) throws IOException {
    try {
      //DatagramSocket socket = new DatagramSocket(Integer.valueOf(s[0]).intValue());
      DatagramSocket socket = new DatagramSocket(CS3Connection.CS3_TX_PORT,NetworkUtil.getIPv4InetAddress());
      byte[] buf = new byte[256];
      DatagramPacket packet = new DatagramPacket(buf, buf.length);
      System.out.println("Waiting package");
      socket.receive(packet);
      System.out.println("Package received. Send information back");
      InetAddress address = packet.getAddress();
      int port = packet.getPort();
      System.out.println("Reply information:" + address.toString() + "port:" + packet.getPort());
      packet = new DatagramPacket(buf, buf.length, address, port);
      socket.send(packet);
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }
}
