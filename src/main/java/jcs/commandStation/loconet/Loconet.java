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

/**
 * LocoNet® is a registered trademark of Digitrax, Inc.
 */
public interface Loconet {

  public static final int OPC_BUSY = 0x81;
  public static final int OPC_GPOFF = 0x82;
  public static final int OPC_GPON = 0x83;
  public static final int OPC_IDLE = 0x85;

  public static final int OPC_LOCO_SPD = 0xA0;
  public static final int OPC_LOCO_DIRF = 0xA1;
  public static final int OPC_LOCO_SND = 0xA2;
  public static final int OPC_LOCO_F9F12 = 0xA3; //!
  public static final int OPC_SW_REQ = 0xB0;
  public static final int OPC_SW_REP = 0xB1;
  public static final int OPC_INPUT_REP = 0xB2;
  public static final int OPC_LONG_ACK = 0xB4;
  public static final int OPC_SLOT_STAT1 = 0xB5;
  public static final int OPC_CONSIST_FUNC = 0xB6;
  public static final int OPC_UNLINK_SLOTS = 0xB8;
  public static final int OPC_LINK_SLOTS = 0xB9;
  public static final int OPC_MOVE_SLOTS = 0xBA;
  public static final int OPC_RQ_SL_DATA = 0xBB;
  public static final int OPC_SW_STATE = 0xBC;
  public static final int OPC_SW_ACK = 0xBD;
  public static final int OPC_LOCO_ADR = 0xBF;

  public static final int OPC_MULTI_SENSE = 0xD0; //!
  public static final int OPC_D4 = 0xD4; //!

  // n byte message opcodes:
  public static final int OPC_MULTI_SENSE_LONG = 0XE0; // !
  public static final int OPC_E4 = 0xE4; // !
  public static final int OPC_PEER_XFER = 0xE5;
  public static final int OPC_SL_RD_DATA = 0xE7;
  public static final int OPC_IMM_PACKET = 0xED;
  public static final int OPC_WR_SL_DATA = 0xEF;

}
