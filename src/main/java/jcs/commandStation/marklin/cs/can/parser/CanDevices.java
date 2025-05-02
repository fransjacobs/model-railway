/*
 * Copyright 2025 Frans Jacobs.
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import jcs.commandStation.marklin.cs.can.device.MeasuringChannel;
import jcs.commandStation.marklin.cs.can.device.CanDevice;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.commandStation.marklin.cs.can.device.ConfigChannel;
import org.tinylog.Logger;

/**
 * Parse the CS CAN Bus devices from the <br>
 * "Softwarestand Anfrage / Teilnehmer Ping" and "Statusdaten Konfiguration" messages
 */
public class CanDevices {

  public static List<CanDevice> parse(CanMessage memberPingmessage) {
    List<CanDevice> devices = new ArrayList<>();
    for (CanMessage response : memberPingmessage.getResponses()) {
      CanDevice device = parseResponse(response);
      if (device != null) {
        devices.add(device);
      }
    }
    Logger.trace("Found " + devices.size() + " CANDevices");
    return devices;
  }

  private static CanDevice parseResponse(CanMessage response) {
    if (CanMessage.PING_RESP == response.getCommand() && CanMessage.DLC_8 == response.getDlc()) {
      byte[] data = response.getData();

      byte[] uida = new byte[4];
      System.arraycopy(data, 0, uida, 0, uida.length);

      byte[] vera = new byte[2];
      System.arraycopy(data, 4, vera, 0, vera.length);

      byte[] deva = new byte[2];
      System.arraycopy(data, 6, deva, 0, deva.length);

      int uidAsInt = response.getDeviceUidNumberFromMessage();
      String uid = "0x" + Integer.toHexString(uidAsInt);
      int major = Byte.toUnsignedInt(vera[0]);
      int minor = Byte.toUnsignedInt(vera[1]);
      String version = major + "." + minor;

      int identifierAsInt = CanMessage.toInt(deva);
      String identifier = "0x" + Integer.toHexString(identifierAsInt);

      CanDevice device = new CanDevice();

      device.setUid(uid);
      device.setVersion(version);
      device.setIdentifier(identifier);
      return device;
    }
    return null;
  }

  public static void parse(CanDevice canDevice, CanMessage statusConfigmessage) {
    //Filter the responses
    List<CanMessage> responses = new ArrayList<>(statusConfigmessage.getResponses().size());
    for (CanMessage resp : statusConfigmessage.getResponses()) {
      if (CanMessage.STATUS_CONFIG_RESP == resp.getCommand()) {
        responses.add(resp);
      }
    }
    if (responses.isEmpty()) {
      return;
    }

    //The last response has the total response messages, aka the package number
    CanMessage last = responses.get(responses.size() - 1);
    int packets = 0;
    int index = 0;

    if (last.getDlc() == CanMessage.DLC_6) {
      index = last.getDataByte(4);
      packets = last.getDataByte(5);
    } else if (last.getDlc() == CanMessage.DLC_5) {
      //CS-2 lets assume the number packets to be the size
      packets = responses.size() - 1;
    }
    if (responses.size() - 1 != packets) {
      Logger.warn("Config Data might be invalid. Packages expected: " + packets + " received: " + (responses.size() - 1));
      Logger.trace(statusConfigmessage);
      for (CanMessage m : responses) {
        Logger.trace(m);
      }
    }

    if (index == 0) {
      parseDeviceDescription(canDevice, responses);
    } else {
      int measurementChannels;
      if (canDevice.getMeasureChannelCount() == null) {
        measurementChannels = 0;
      } else {
        measurementChannels = canDevice.getMeasureChannelCount();
      }
      if (index <= measurementChannels) {
        parseMeasurementChannel(canDevice, responses);
      } else {
        parseConfigurationChannel(canDevice, responses);
      }
    }
  }

  /**
   * In case the index equals zero (0) the responses contain a CAN Device Description.
   *
   * @param responses
   * @return
   */
  private static void parseDeviceDescription(CanDevice canDevice, List<CanMessage> responses) {
    List<Byte> stringDataList = new ArrayList<>();
    for (int i = 0; i < responses.size(); i++) {
      CanMessage msg = responses.get(i);
      byte[] data = msg.getData();
      int packageNr = msg.getPackageNumber();

      switch (packageNr) {
        case 1 -> {
          if (CanMessage.DLC_8 == msg.getDlc()) {
            int measureChannels = Byte.toUnsignedInt(data[0]);
            int configChannels = Byte.toUnsignedInt(data[1]);
            canDevice.setMeasureChannelCount(measureChannels);
            canDevice.setConfigChannelCount(configChannels);
          } else {
            Logger.trace("Invalid DLC " + msg.getDlc() + " Package " + packageNr + " " + msg);
          }
        }
        case 2 -> {
          if (CanMessage.DLC_8 == msg.getDlc()) {
            //Article is defined in the full 8 bytes of the 2nd package
            String articleNumber = CanMessage.toString(data);
            canDevice.setArticleNumber(articleNumber.trim());
          } else {
            Logger.trace("Invalid DLC " + msg.getDlc() + " Package " + packageNr + " " + msg);
          }
        }
        default -> {
          switch (msg.getDlc()) {
            case CanMessage.DLC_8 -> {
              for (int j = 0; j < data.length; j++) {
                stringDataList.add(data[j]);
              }
            }
            case CanMessage.DLC_6 -> {
              //Got the last response
              List<String> strings = splitIntoStrings(stringDataList);
              if (!strings.isEmpty()) {
                canDevice.setName(strings.get(0));
              }
              if (strings.size() > 1) {
                Logger.warn("There are more name strings than expected " + strings);
              }
            }
            default ->
              Logger.trace("Invalid DLC " + msg.getDlc() + " Package " + packageNr + " " + msg);
          }
        }
      }
    }
  }

  private static void parseMeasurementChannel(CanDevice canDevice, List<CanMessage> responses) {
    List<Byte> stringDataList = new ArrayList<>();
    MeasuringChannel channel = new MeasuringChannel();

    for (int i = 0; i < responses.size(); i++) {
      CanMessage msg = responses.get(i);
      byte[] data = msg.getData();
      int packageNr = msg.getPackageNumber();

      switch (packageNr) {
        case 1 -> {
          if (CanMessage.DLC_8 == msg.getDlc()) {
            int channelNumber = Byte.toUnsignedInt(data[0]);
            int measurementScale = (int) data[1];
            int colorRange1 = Byte.toUnsignedInt(data[2]);
            int colorRange2 = Byte.toUnsignedInt(data[3]);
            int colorRange3 = Byte.toUnsignedInt(data[4]);
            int colorRange4 = Byte.toUnsignedInt(data[5]);

            byte[] zeroPoint = new byte[2];
            System.arraycopy(data, 6, zeroPoint, 0, zeroPoint.length);
            int zero = CanMessage.toInt(zeroPoint);

            //channel.setIndex(index);
            channel.setNumber(channelNumber);
            channel.setScale(measurementScale);
            channel.setColorGreen(colorRange1);
            channel.setColorYellow(colorRange2);
            channel.setColorRed(colorRange3);
            channel.setColorMax(colorRange4);

            channel.setZeroPoint(zero);
          } else {
            Logger.trace("Invalid DLC " + msg.getDlc() + " Package " + packageNr + " " + msg);
          }
        }
        case 2 -> {
          if (CanMessage.DLC_8 == msg.getDlc()) {
            byte[] brange1 = new byte[2];
            System.arraycopy(data, 0, brange1, 0, brange1.length);
            byte[] brange2 = new byte[2];
            System.arraycopy(data, 2, brange2, 0, brange2.length);
            byte[] brange3 = new byte[2];
            System.arraycopy(data, 4, brange3, 0, brange3.length);
            byte[] brange4 = new byte[2];
            System.arraycopy(data, 6, brange4, 0, brange4.length);

            int range1 = CanMessage.toInt(brange1);
            int range2 = CanMessage.toInt(brange2);
            int range3 = CanMessage.toInt(brange2);
            int range4 = CanMessage.toInt(brange4);

            channel.setRangeGreen(range1);
            channel.setRangeYellow(range2);
            channel.setRangeRed(range3);
            channel.setRangeMax(range4);
          } else {
            Logger.trace("Invalid DLC " + msg.getDlc() + " Package " + packageNr + " " + msg);
          }
        }
        default -> {
          switch (msg.getDlc()) {
            case CanMessage.DLC_8 -> {
              //The last part of the measurement data are strings, so first concat all data packets
              for (int ii = 0; ii < data.length; ii++) {
                stringDataList.add(data[ii]);
              }
            }
            case CanMessage.DLC_6 -> {
              //Last message in this response
              if (!stringDataList.isEmpty()) {
                List<String> strings = splitIntoStrings(stringDataList);

                if (!strings.isEmpty()) {
                  for (int j = 0; j < strings.size(); j++) {
                    switch (j) {
                      case 0 ->
                        channel.setName(strings.get(0));
                      case 1 ->
                        channel.setStartValue(Double.valueOf(strings.get(1)));
                      case 2 ->
                        channel.setEndValue(Double.valueOf(strings.get(2)));
                      case 3 ->
                        channel.setUnit(strings.get(3));
                      default ->
                        Logger.trace("Remaining: " + strings.get(j));
                    }
                  }
                }
              }
            }
            default ->
              Logger.trace("Invalid DLC " + msg.getDlc() + " Package " + packageNr + " " + msg);
          }
        }
      }
    }
    canDevice.addMeasuringChannel(channel);
  }

  private static void parseConfigurationChannel(CanDevice canDevice, List<CanMessage> responses) {
    List<Byte> stringDataList = new ArrayList<>();
    ConfigChannel channel = new ConfigChannel();

    for (int i = 0; i < responses.size(); i++) {
      CanMessage msg = responses.get(i);
      byte[] data = msg.getData();
      int packageNr = msg.getPackageNumber();

      //There are 2 possible formats; one with choice lists and one with values.
      //Not clear how the recognice the one or the other...
      switch (packageNr) {
        case 1 -> {
          if (CanMessage.DLC_8 == msg.getDlc()) {
            int channelConfigNumber = Byte.toUnsignedInt(data[0]);
            int valueId = Byte.toUnsignedInt(data[1]);
            channel.setNumber(channelConfigNumber);
            channel.setValueId(valueId);
            //Here it is a bit unclear. For choice list
            int choicesCount = Byte.toUnsignedInt(data[2]);
            int defaultValueId = Byte.toUnsignedInt(data[3]);

            //next 4 byte are reserved aka 0
            int res1 = Byte.toUnsignedInt(data[4]);
            int res2 = Byte.toUnsignedInt(data[5]);
            int res3 = Byte.toUnsignedInt(data[6]);
            int res4 = Byte.toUnsignedInt(data[7]);

            if (res1 == 0 & res2 == 0 && res3 == 0 && res4 == 0) {
              channel.setChoicesCount(choicesCount);
              channel.setValueId(defaultValueId);
            } else {
              //for the other format:
              byte[] lowval = new byte[2];
              System.arraycopy(data, 2, lowval, 0, lowval.length);
              int lowValue = CanMessage.toInt(lowval);
              channel.setLowValue(lowValue);
              byte[] upperval = new byte[2];
              System.arraycopy(data, 4, upperval, 0, upperval.length);
              int upperValue = CanMessage.toInt(lowval);
              channel.setHighValue(upperValue);
              byte[] setval = new byte[2];
              System.arraycopy(data, 6, setval, 0, setval.length);
              int actualValue = CanMessage.toInt(setval);
              channel.setActualValue(actualValue);
            }
          } else {
            Logger.trace("Invalid DLC " + msg.getDlc() + " Package " + packageNr + " " + msg);
          }
        }
        default -> {
          int dlc = msg.getDlc();
          switch (dlc) {
            case CanMessage.DLC_8 -> {
              //The last part of the config channel data are strings, so first concat all data packets
              for (int ii = 0; ii < data.length; ii++) {
                stringDataList.add(data[ii]);
              }
            }
            case CanMessage.DLC_6 -> {
              //Last message in this response
              if (!stringDataList.isEmpty()) {
                List<String> strings = splitIntoStrings(stringDataList);
                for (String s : strings) {
                  Logger.trace(s);
                }
                int choicesCount = 0;
                if (channel.getChoicesCount() != null) {
                  choicesCount = channel.getChoicesCount();
                }

                //first string the the description
                channel.setChoiceDescription(strings.get(0));

                if (choicesCount > 0) {
                  //next are the choices
                  for (int j = 1; j <= choicesCount; j++) {
                    if (strings.size() >= j) {
                      channel.addChoice(strings.get(j));
                    }
                  }
                } else {
                  //No choice list must be single values
                  for (int j = 1; j < strings.size(); j++) {
                    switch (j) {
                      case 1 ->
                        channel.setStartName(strings.get(j));
                      case 2 ->
                        channel.setEndName(strings.get(j));
                      case 3 ->
                        channel.setUnit(strings.get(j));
                      default ->
                        Logger.trace("Remaining: " + strings.get(j));
                    }
                  }
                }
                canDevice.addConfigChannel(channel);
              }
            }
            default ->
              Logger.trace("Invalid DLC " + msg.getDlc() + " Package " + packageNr + " " + msg);
          }
        }
      }
    }
  }

  private static List<String> splitIntoStrings(List<Byte> byteList) {
    List<String> strings = new ArrayList<>();
    int index = 0;
    byte[] d = new byte[byteList.size()];
    for (int j = 0; j < d.length; j++) {
      d[j] = byteList.get(j);

      if (d[j] == 0) {
        //Terminator
        int len = j - index;

        byte[] tmp = new byte[len];
        System.arraycopy(d, index, tmp, 0, tmp.length);
        String s;
        try {
          s = new String(tmp, "UTF-8");

          if (s.length() >= 1) {
            strings.add(s);
          }
        } catch (UnsupportedEncodingException ex) {
          Logger.error(ex);
        }
        index = j + 1;
      }
    }
    return strings;
  }

}
