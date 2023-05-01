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
import jcs.controller.cs3.net.CS3Connection;

/**
 *
 * @author frans
 */
public class TestUDPClient {

  public static void main(String str[]) throws IOException {
    String msg = "Hello";
    //InetAddress group = InetAddress.getByName(str[0]);
    InetAddress group = NetworkUtil.getIPv4InetAddress();

    DatagramSocket s = new DatagramSocket();
    DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(),group, CS3Connection.CS3_TX_PORT);
    s.send(hi);
    // get their responses!
    byte[] buf = new byte[1000];
    DatagramPacket recv = new DatagramPacket(buf, buf.length);
    s.receive(recv);
    System.out.println(new String(buf));
  }
}
