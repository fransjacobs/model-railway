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
package jcs.controller.cs.can.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import jcs.controller.cs.can.CanMessage;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class MessageInflator {

  public static String inflateConfigDataStream(CanMessage message) {
    try {
      List<CanMessage> responses = message.getResponses();
      Logger.trace("There are " + responses.size() + " response messages");

      //Ccreate the compressesed (zlib) datafile
      //First response is the acknowledges, second response contains the file length and the CRC
      int dataLength = 0;

      List<byte[]> dataPackets = new ArrayList<>(responses.size());

      for (int i = 0; i < message.getResponses().size(); i++) {
        switch (i) {
          case 0 -> {
            CanMessage ack = responses.get(i);
            if (ack.getCommand() != CanMessage.REQUEST_CONFIG_DATA_RESP) {
              Logger.error("Wrong acknowledge response: " + ack);
              break;
            }
          }
          case 1 -> {
            CanMessage lenResp = responses.get(i);
            if (lenResp.getCommand() != CanMessage.CONFIG_DATA_STREAM && lenResp.getDlc() != CanMessage.DLC_6) {
              Logger.error("Found wrong data length response: " + lenResp);
              break;
            } else {
              dataLength = lenResp.getDeviceUidNumberFromMessage();
              int[] crca = new int[2];
              int[] dt = lenResp.getData();
              System.arraycopy(dt, 4, crca, 0, crca.length);
              int crc = ByteUtil.toInt(crca);
              //For now the CRC is ignored as the Inflate will fail anyway whet the message contains errors
              Logger.error("Data length: " + dataLength + " CRC: " + crc);
            }
          }
          default -> {
            //"Normal" data response
            CanMessage dataResp = responses.get(i);
            if (dataResp.getCommand() != CanMessage.CONFIG_DATA_STREAM && dataResp.getDlc() != CanMessage.DLC_8) {
              Logger.error("Wrong data response: " + dataResp + " skipping");
            } else {
              byte[] dataPart = dataResp.getDataBytes();
              dataPackets.add(dataPart);
            }
          }
        }
      }

      //Reconstruct the data parts in one buffer
      byte[] databuf = new byte[dataPackets.size() * 8];
      Logger.trace("Packets: " + dataPackets.size() + " Buffer " + databuf.length);

      for (int i = 0; i < dataPackets.size(); i++) {
        byte[] p = dataPackets.get(i);
        if ((i * 8) < databuf.length) {
          System.arraycopy(p, 0, databuf, (i * 8), p.length);
        }
      }

      //The First 4 byte represent the uncompressed length of the file
      byte[] uclb = new byte[4];
      System.arraycopy(databuf, 0, uclb, 0, uclb.length);
      int uncompressedLength = ByteUtil.toInt(uclb);

      Logger.trace("uncompressedLength: " + uncompressedLength + " dataLength: " + dataLength + " databuf.length: " + databuf.length);

      //Create a new buffer skip the first 4 bytes
      byte[] compressedBuffer = new byte[dataLength];
      System.arraycopy(databuf, 4, compressedBuffer, 0, dataLength - 4);

      //Inflate
      Inflater inflator = new Inflater();
      inflator.setInput(compressedBuffer);
      byte[] inflaterbuf = new byte[uncompressedLength];

      Logger.trace("Compressed length: " + compressedBuffer.length + " Temp inflate buf: " + inflaterbuf.length);
      inflator.inflate(inflaterbuf);
      inflator.end();

      Logger.trace(ByteUtil.bytesToString(inflaterbuf));
      return ByteUtil.bytesToString(inflaterbuf);
    } catch (DataFormatException ex) {
      Logger.error(ex);
    }
    return null;
  }

}
