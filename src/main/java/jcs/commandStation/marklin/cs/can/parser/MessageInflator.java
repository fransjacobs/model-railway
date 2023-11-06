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
package jcs.commandStation.marklin.cs.can.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class MessageInflator {

  public static String inflateConfigDataStream(CanMessage message, String filename) {
    boolean debug = System.getProperty("inflate.debug", "false").equalsIgnoreCase("true");

    try {
      List<CanMessage> responses = message.getResponses();
      if (debug) {
        Logger.trace("There are " + responses.size() + " responses");
      }
      //Create the decompressesed (zlib) datafile from CAN message responses
      //First response is the acknowledges, second response contains the file length and the CRC
      //For now I have chosen not to check the CRC as my assumption is that the zlib inflation will fail
      //when the response is not ok
      int dataLength = 0;

      List<byte[]> dataPackets = new ArrayList<>(responses.size());

      for (int i = 0; i < message.getResponses().size(); i++) {
        //Logger.trace("i: "+i+"; "+responses.get(i));
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
              byte[] crca = new byte[2];
              byte[] dt = lenResp.getData();
              System.arraycopy(dt, 4, crca, 0, crca.length);
              int crc = ByteUtil.toInt(crca);
              //For now the CRC is ignored as the Inflate will probaby fail when the message contains errors
              Logger.error("Data length: " + dataLength + " CRC: " + crc);
            }
          }
          default -> {
            //"Normal" data response
            CanMessage dataResp = responses.get(i);
            if (dataResp.getCommand() != CanMessage.CONFIG_DATA_STREAM && dataResp.getDlc() != CanMessage.DLC_8) {
              Logger.error("Wrong data response: " + dataResp + " skipping");
            } else {
              byte[] dataPart = dataResp.getData();
              dataPackets.add(dataPart);
            }
          }
        }
      }

      //Reconstruct the data parts in one buffer
      byte[] databuf = new byte[dataPackets.size() * 8];
      if (debug) {
        Logger.trace("Data Packets: " + dataPackets.size() + " raw buffer length:" + databuf.length);
      }
      if (databuf.length < dataLength) {
        Logger.warn("Received less data bytes [" + databuf.length + "] then specified [" + dataLength + "]...");
      }

      int bufIdx;
      for (int i = 0; i < dataPackets.size(); i++) {
        bufIdx = i * 8;
        byte[] p = dataPackets.get(i);
        if (bufIdx < databuf.length) {
          System.arraycopy(p, 0, databuf, bufIdx, p.length);
        }
      }

      if (debug) {
        Logger.trace("All Packets in Buffer " + databuf.length);
      }
      //The First 4 byte represent the uncompressed length of the file
      byte[] uclb = new byte[4];
      System.arraycopy(databuf, 0, uclb, 0, uclb.length);
      int uncompressedLength = ByteUtil.toInt(uclb);

      if (debug) {
        Logger.trace("Uncompressed length: " + uncompressedLength + " Compressed length " + dataLength);
      }
      //Create a new buffer skip the first 4 bytes
      //byte[] compressedBuffer = new byte[dataLength];
      byte[] compressedBuffer = new byte[databuf.length - 4];
      if (debug) {
        Logger.trace("Removed first 4 bytes length: " + compressedBuffer.length);
      }
      System.arraycopy(databuf, 4, compressedBuffer, 0, compressedBuffer.length);

      if (debug) {
        Logger.debug("uncompressedLength: " + uncompressedLength + " dataLength: " + dataLength + " databuf.length: " + databuf.length);

        //Write to a file for debug when set in property
        Path path = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + filename + ".bin");
        try {
          Files.write(path, compressedBuffer);
          Logger.debug("Saved raw (zipped) " + filename + " file to: " + path.toString());
        } catch (IOException ex) {
          Logger.error("Can't write raw (zipped) " + filename + " file to " + path.toString());
        }
      }

      //Inflate
      Inflater inflator = new Inflater();
      inflator.setInput(compressedBuffer);
      byte[] inflaterbuf = new byte[uncompressedLength];

      inflator.inflate(inflaterbuf);
      inflator.end();

      String inflatedFile = CanMessage.toString(inflaterbuf);

      if (debug) {
        Logger.trace("Real Uncompressed length " + inflatedFile.length());

        Logger.trace("\n" + inflatedFile);

        //Write to a file for debug when set in property
        Path path = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + filename + ".cs2");
        try {
          Files.writeString(path, inflatedFile);
          Logger.debug("Saved unzipped " + filename + " file to: " + path.toString());
        } catch (IOException ex) {
          Logger.error("Can't write unzipped " + filename + " file to " + path.toString());
        }
      }

      return inflatedFile;
    } catch (DataFormatException ex) {
      Logger.error(ex);
    }
    return null;
  }

}
