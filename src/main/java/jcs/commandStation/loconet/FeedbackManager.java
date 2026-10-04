/*
 * Copyright 2026 Frans Jacobs.
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
package jcs.commandStation.loconet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import jcs.commandStation.entities.FeedbackModule;
import jcs.commandStation.events.SensorEvent;
import static jcs.commandStation.loconet.Intellibox2Impl.COMMAND_STATION_ID;
import static jcs.commandStation.loconet.Opcodes.OPC_INPUT_REP;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.SensorBean;
import org.tinylog.Logger;

/**
 *
 * Loconet Sensor Management
 */
class FeedbackManager {

  private final Intellibox2Impl intelliboxImpl;
  private final Map<Integer, FeedbackModule> modules;
  private final Map<Integer, SensorBean> sensors;

  static final int DEFAULT_ARTICLE = 6510;
  static final int DEFAULT_MODULE = 8491;
  static final int LNCV_MODULE_COUNT = 71;

  private static final int DEFAULT_REPORT_ADDRESS = 1017;

  private volatile int numberOfFeedbackModules;
//  private int expectedFeedbackStates;
//  private final BitSet receivedFeedbackStates = new BitSet();

  FeedbackManager(Intellibox2Impl intelliboxImpl) {
    this.intelliboxImpl = intelliboxImpl;
    modules = new HashMap<>();
    sensors = new HashMap<>();
  }

//  void beginSnapshot(int moduleCount) {
//    expectedFeedbackStates = moduleCount * 16;
//    receivedFeedbackStates.clear();
//  }
//  void updateFromSnapshot(int address, boolean active) {
//    if (address < 0 || address >= expectedFeedbackStates) {
//      Logger.warn("Feedback address {} outside expected range 0..{}", address, expectedFeedbackStates - 1);
//      return;
//    }
//
//    boolean alreadyReceived = receivedFeedbackStates.get(address);
//    receivedFeedbackStates.set(address);
//
//    if (!alreadyReceived && receivedFeedbackStates.cardinality() == expectedFeedbackStates) {
//      Logger.debug("Feedback snapshot complete: {} states", expectedFeedbackStates);
//    }
//  }
  private boolean isLNCVReply(LoconetMessage message, int expectedArticle, int expectedLncv) {
    try {
      parseLNCVReadReply(message, expectedArticle, expectedLncv);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }

  void readFeedbackConfigurations() {
    Logger.trace("Start reading Intellibox 2 feedback configurations...");
    LoconetMessage start = LoconetMessageFactory.startLNCVProgramming(DEFAULT_ARTICLE, DEFAULT_MODULE);
    Predicate<LoconetMessage> startReplyMatcher = message -> isLNCVReply(message, DEFAULT_ARTICLE, 0);
    LoconetMessage startReply = intelliboxImpl.loconet.sendMessageAwaitEchoAndReply(start, startReplyMatcher, 500);

    if (startReply == null) {
      Logger.warn("No LNCV programming-start reply received");
      return;
    }

    LoconetMessage request = LoconetMessageFactory.readLNCV(DEFAULT_ARTICLE, LNCV_MODULE_COUNT);
    Predicate<LoconetMessage> readReplyMatcher = message -> isLNCVReply(message, DEFAULT_ARTICLE, LNCV_MODULE_COUNT);
    LoconetMessage readReply = intelliboxImpl.loconet.sendMessageAwaitEchoAndReply(request, readReplyMatcher, 500);

    if (readReply == null) {
      Logger.warn("No reply received for LNCV {}", LNCV_MODULE_COUNT);
      return;
    }

    int moduleCount = parseLNCVReadReply(readReply, DEFAULT_ARTICLE, LNCV_MODULE_COUNT);
    setNumberOfFeedbackModules(moduleCount);
    LoconetMessage end = LoconetMessageFactory.endLNCVProgramming(DEFAULT_ARTICLE, DEFAULT_MODULE);
    LoconetMessage endEcho = intelliboxImpl.loconet.sendMessage(end);

    if (endEcho == null) {
      Logger.warn("No echo received for LNCV programming end");
      return;
    }

    requestCurrentSensorStates();
  }

  private int parseLNCVReadReply(LoconetMessage message, int expectedArticle, int expectedLncv) {
    if (message == null) {
      throw new IllegalArgumentException("LNCV reply may not be null");
    }

    if (!message.isExpectedsOpcode(LoconetMessage.OPC_PEER_XFER)) {
      throw new IllegalArgumentException("Expected OPC_PEER_XFER, got: " + message);
    }

    if (message.getLength() != 15) {
      throw new IllegalArgumentException("Expected 15 byte LNCV reply, got: " + message.getLength());
    }

    int[] frame = message.getMessage();

    /*
     * Expected LNCV read reply:
     *
     *  0  E5   OPC_PEER_XFER
     *  1  0F   length
     *  2  05
     *  3  49
     *  4  4B
     *  5  1F   LNCV reply
     *  6  PXCT
     *  7  article low
     *  8  article high
     *  9  LNCV low
     * 10  LNCV high
     * 11  value low
     * 12  value high
     * 13  command data
     * 14  checksum
     */
    if (frame[2] != 0x05
            || frame[3] != 0x49
            || frame[4] != 0x4B
            || frame[5] != 0x1F) {
      throw new IllegalArgumentException(
              "Not an LNCV read reply: " + message);
    }

    int pxct = frame[6] & 0x7F;

    int articleLow = decodePeerByte(frame[7], pxct, 0);
    int articleHigh = decodePeerByte(frame[8], pxct, 1);
    int article = articleLow | (articleHigh << 8);

    int lncvLow = decodePeerByte(frame[9], pxct, 2);
    int lncvHigh = decodePeerByte(frame[10], pxct, 3);
    int lncv = lncvLow | (lncvHigh << 8);

    if (article != expectedArticle || lncv != expectedLncv) {
      throw new IllegalArgumentException(
              "Unexpected LNCV reply: article=" + article
              + ", lncv=" + lncv
              + ", expected article=" + expectedArticle
              + ", lncv=" + expectedLncv);
    }

    int valueLow = decodePeerByte(frame[11], pxct, 4);
    int valueHigh = decodePeerByte(frame[12], pxct, 5);

    return valueLow | (valueHigh << 8);
  }

  private int decodePeerByte(int value, int pxct, int dataIndex) {
    value &= 0x7F;

    if ((pxct & (1 << dataIndex)) != 0) {
      value |= 0x80;
    }

    return value;
  }

  Map<Integer, FeedbackModule> getModules() {
    return modules;
  }

  FeedbackModule getFeedbackModule(int id) {
    return modules.get(id);
  }

  SensorBean getSensor(Integer id) {
    return sensors.get(id);
  }

  void refresh() {
    //refreshSensors(PersistenceFactory.getService().getSensorsByCommandStationId(COMMAND_STATION_ID));
    sensors.clear();
    modules.clear();

    readFeedbackConfigurations();

  }

//  synchronized void refreshSensors(List<SensorBean> sensorList) {
//    sensors.clear();
//    modules.clear();
//
//    for (SensorBean sb : sensorList) {
//      this.sensors.put(sb.getId(), sb);
//      //this.modules
//    }
//
//    Logger.trace("There are {} sensors.", sensors.size());
//  }
  List<FeedbackModule> getFeedbackModules() {
//      for (int i = 0; i < bus1Len; i++) {
//      FeedbackModule b1 = new FeedbackModule();
//        //Use the offset plus module nr as the id
//        b1.setId(1000 + i);
//        b1.setAddressOffset(1000);
//        b1.setModuleNumber(i + 1);
//        b1.setPortCount(16);
//        b1.setIdentifier(nodeId);
//        b1.setBusNumber(1);
//        b1.setCommandStationId(commandStationBean.getId());
//        b1.setBusSize(bus1Len);
//        feedbackModules.add(b1);
//      }

    return null;
  }

  SensorBean getSensorStatus(SensorBean sensorBean) {
    if (sensorBean != null && sensorBean.getId() != null) {
      Integer sensorId = sensorBean.getId();
      return this.sensors.get(sensorId);
    } else {
      return null;
    }
  }

  void setNumberOfFeedbackModules(int moduleCount) {
    if (moduleCount < 1 || moduleCount > 31) {
      throw new IllegalArgumentException("Invalid number of feedback modules: " + moduleCount);
    }
    numberOfFeedbackModules = moduleCount;

    int numberOfContacts = numberOfFeedbackModules * 16;
    if (this.sensors.isEmpty()) {
      //Create Sensorbeans
      for (int id = 0; id < numberOfContacts; id++) {
        //  public SensorBean(Integer id, Integer deviceId, Integer contactId, Integer nodeId, Integer status, Integer previousStatus, String commandStationId, Integer busNr) {
        int deviceId = calculateDeviceId(id);
        int contactId = calculateContactId(id);
        int nodeId = 0;
        int status = 0;
        int previousStatus = 1;
        String commandStationId = Intellibox2Impl.COMMAND_STATION_ID;
        int busNr = 0;

        SensorBean sb = new SensorBean(id, deviceId, contactId, nodeId, status, previousStatus, commandStationId, busNr);
        this.sensors.put(id, sb);
      }
      Logger.debug("Intellibox S88 has {} feedback modules and {} sensors", numberOfFeedbackModules, sensors.size());
    }
  }

  private static Integer calculateDeviceId(int address) {
    int deviceId = address / 16 + 1;
    return deviceId;
  }

  private static int calculateContactId(int address) {
    int contact = address % 16 + 1;
    return contact;
  }

  private static int calculateAddress(int module, int contact) {
    module = (module - 1) * 16 + (contact - 1);
    return module;
  }

  void requestCurrentSensorStates() {
    LoconetMessage request = LoconetMessageFactory.switchAccessory(DEFAULT_REPORT_ADDRESS, AccessoryValue.GREEN, true);
    Logger.trace("Requesting current feedback states using request {}", request);
    LoconetMessage echo = intelliboxImpl.loconet.sendMessage(request);
    if (echo == null) {
      Logger.warn("No echo received for feedback-state request");
    }
  }

  void update(LoconetMessage message) {
    SensorBean sb = parseSensorEvent(message);

    int address = sb.getId();
    //updateFromSnapshot(address, sb.isActive());

    if (this.sensors.containsKey(sb.getId())) {
      SensorBean csb = this.sensors.get(sb.getId());
      csb.setActive(sb.isActive());
      csb.setLastUpdatedMillis(System.currentTimeMillis());
    } else {
      Logger.trace("Adding new Sensor {}", sb.getId());
      sb.setLastUpdatedMillis(System.currentTimeMillis());
      this.sensors.put(sb.getId(), sb);
    }

    SensorBean csb = sensors.get(sb.getId());
    if (csb != null) {
      Logger.trace("Sensor: {} Value: {} ", sb.getId(), sb.getStatus());
      SensorEvent sme = new SensorEvent(sb);
      intelliboxImpl.fireAllSensorEventsListeners(sme);
    }
  }

  /**
   * Parses a raw 4-byte LocoNet sensor message.
   *
   * @param opcode expected 0xB2
   * @param in1 address low byte (a6..a0)
   * @param in2 address high nibble + flags (X, I, L, a10..a7)
   * @param chk checksum byte
   * @return the decoded sensor event
   * @throws IllegalArgumentException if opcode or checksum is invalid
   */
  SensorBean parseSensorEvent(LoconetMessage message) {
    if (!message.isChecksumValid()) {
      throw new IllegalArgumentException(String.format("Checksum mismatch for message {}", message.toString()));
    }
    if (!message.isExpectedsOpcode(OPC_INPUT_REP)) {
      throw new IllegalArgumentException(String.format("Not a sensor message, opcode={}", message.getHexOpcode()));
    }

    int in1 = message.getArgument(1);
    int in2 = message.getArgument(2);

    int addrLow = in1 & 0x7F;   // a6..a0
    int addrHigh = in2 & 0x0F;  // a10..a7
    int rawAddress = (addrHigh << 7) | addrLow; // 11-bit pair address

    boolean x = (in2 & 0x40) != 0;
    boolean i = (in2 & 0x20) != 0; // selects sub-address within the pair
    boolean value = (in2 & 0x10) != 0; // L bit

    // I bit distinguishes the two sensors sharing this raw pair address
    int address = (rawAddress << 1) | (i ? 1 : 0);

    Integer id = address; // + 1;
    Integer deviceId = calculateDeviceId(address);
    Integer contactId = calculateContactId(address);
    return new SensorBean(id, deviceId, contactId, 0, (value ? 1 : 0), (value ? 0 : 1), COMMAND_STATION_ID, 0);
  }

}
