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

  /*  Master busy*/
  public static final int OPC_BUSY = 0x81;
  /* Global power OFF */
  public static final int OPC_GPOFF = 0x82;
  /* Global power ON  */
  public static final int OPC_GPON = 0x83;
  /* Emergency stop */
  public static final int OPC_IDLE = 0x85;
  
  
  /* Set locomotive speed */
  public static final int OPC_LOCO_SPD = 0xA0;
  /* Set direction/F0-F4 */
  public static final int OPC_LOCO_DIRF = 0xA1;
  /* Set functions F5-F8 */
  public static final int OPC_LOCO_SND = 0xA2;
  
  
  
  /* Switch request */
  public static final int OPC_SW_REQ = 0xB0;
  /* Switch output status / report */
  public static final int OPC_SW_REP = 0xB1;
  /* Sensor input */
  public static final int OPC_INPUT_REP = 0xB2;
  /* Long acknowledge */
  public static final int OPC_LONG_ACK = 0xB4;
  /* Write slot status */
  public static final int OPC_SLOT_STAT1 = 0xB5;
  /* Consist function */
  public static final int OPC_CONSIST_FUNC = 0xB6;
  /* Unlink consist */
  public static final int OPC_UNLINK_SLOTS = 0xB8;
  /* Link consist */
  public static final int OPC_LINK_SLOTS = 0xB9;
  /* Move/activate slot */
  public static final int OPC_MOVE_SLOTS = 0xBA;
  /* Request slot data */
  public static final int OPC_RQ_SL_DATA = 0xBB;
  /* Request switch state */
  public static final int OPC_SW_STATE = 0xBC;
  /* Switch with acknowledge */
  public static final int OPC_SW_ACK = 0xBD;
  /* Request loco address */
  public static final int OPC_LOCO_ADR = 0xBF;
  /* Transponding present/absent, power management */
  public static final int OPC_MULTI_SENSE = 0xD0;
  /* LISSY/RailCom loco identification */
  public static final int OPC_LISSY_UPDATE = 0xE4;
  
  
  /* LNCV reply (15 bytes) */
  public static final int OPC_PEER_XFER = 0xE5;
  /* Slot data (14 bytes) */
  public static final int OPC_SL_RD_DATA = 0xE7;
  /* LNCV command (15 bytes) */
  public static final int OPC_IMM_PACKET = 0xED;
  
  /* Write slot data (14 bytes) */
  public static final int OPC_WR_SL_DATA = 0xEF;

}

