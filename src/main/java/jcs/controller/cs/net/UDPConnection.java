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
package jcs.controller.cs.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.controller.cs.can.CanMessage;
import jcs.controller.cs.events.CanMessageEvent;
import jcs.controller.cs3.events.CanMessageListener;
import org.tinylog.Logger;
import jcs.controller.cs.events.CanPingListener;
import jcs.controller.cs.events.FeedbackEventListener;

/**
 *
 * @author Frans Jacobs
 */
class UDPConnection implements CSConnection {

  private final InetAddress cs2Address;

  private final List<CanMessageListener> listeners;
  private final ExecutorService executor;

  UDPConnection(InetAddress cs2Address) {
    this.cs2Address = cs2Address;
    listeners = new ArrayList<>();
    executor = Executors.newCachedThreadPool();
  }

  @Override
  public CanMessage sendCanMessage(CanMessage message) {
    Logger.trace("Sending: " + message + " from: " + this.cs2Address.getHostAddress() + " port " + CSConnection.CS_RX_PORT);
    CanMessage response = null;
    try {
      InetAddress localAddress = InetAddress.getByName("0.0.0.0");

      try (DatagramSocket requestSocket = new DatagramSocket()) {
        DatagramPacket requestPacket = new DatagramPacket(message.getBytes(), message.getLength(), cs2Address, CSConnection.CS_RX_PORT);
        requestSocket.send(requestPacket);
      }

      try (DatagramSocket responseSocket = new DatagramSocket(CSConnection.CS_TX_PORT, localAddress)) {
        responseSocket.setSoTimeout(5000);
        DatagramPacket responsePacket = new DatagramPacket(new byte[CanMessage.MESSAGE_SIZE], CanMessage.MESSAGE_SIZE, localAddress, CSConnection.CS_TX_PORT);

        Logger.trace("Listen on " + localAddress.getHostAddress() + " port " + CSConnection.CS_TX_PORT);

        responseSocket.receive(responsePacket);

        InetAddress replyAddress = InetAddress.getByName(responsePacket.getAddress().getHostAddress());
        Logger.trace("Received reply from " + replyAddress.getHostName());

        response = new CanMessage(responsePacket.getData());
        Logger.trace("Received: " + response);

        CanMessageEvent event = new CanMessageEvent(response, replyAddress);
        executor.execute(() -> fireMessageListeners(event));
        //fireMessageListeners(event);
      }
    } catch (SocketTimeoutException ste) {
      Logger.debug("No reply. " + ste.getMessage());
    } catch (IOException ex) {
      Logger.error(ex);
    }

    return response;
  }

  @Override
  public void setCanMessageListener(CanMessageListener listener) {
    this.listeners.add(listener);
  }

  @Override
  public void setCanPingRequestListener(CanPingListener listener) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public void setFeedbackEventListener(FeedbackEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  private void fireMessageListeners(final CanMessageEvent event) {
    for (CanMessageListener listener : this.listeners) {
      listener.onCanMessage(event);
    }
  }

  @Override
  public void close() throws Exception {
    this.executor.shutdown();
    this.listeners.clear();
  }

  @Override
  public InetAddress getControllerAddress() {
    return cs2Address;
  }

  @Override
  public boolean isConnected() {
    return true;
  }

}
