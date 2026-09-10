# Interfaces for JCS
***

## HSI-S88
Littfinski DatenTechnik (LDT)
Translated from https://mobatron.4lima.de/wp-content/uploads/2020/05/hsi88_command-codes_de.pdf
Translated with DeepL.com (free version)

### High Speed Interface-88 (HSI-88) (command set / version 1.3)
(Software version from 0.40 from 06.10.2000)

#### Brief description
The HSI-88 is an interface from the s88 feedback bus to the RS 232 interface.
The interface has three s88 bus connectors. This offers the advantage of faster
bus processing and the possibility of forming three bus lines on the system.
The three plugs are referred to as left, middle and right bus plugs.
A maximum of 31*16 feedback contacts can be monitored.
A maximum of 31*16 per bus line, but no more than 31*16 contacts can be read in total.
In each case, 16 feedback inputs are combined into one module.
The module with the number 1 is the first module on the left bus line.
It is counted up to the last registered module on the left bus line.
Then continue with the first module on the middle line.
The module with the highest module number is the last module on the right-hand line.

#### RS-232
- Baud rate: 9600 baud
- Format: 8 bit data, 1 start and 1 stop bit, no parity
- Handshake: Hardware handshake via RTS and CTS
- Interface: Galvanically isolated. DTR must be activated by the PC (high level).

#### Command structure
Commands and data are transmitted.
The last character of each command is Carriage Return.
If TerminalMode is switched off, unsigned hex bytes are transmitted (one value equals one byte).
If TerminalMode is switched on, ASCII characte##### Command form   : "t" \<CR>
- Command length    : 2 bytes 
- Response          : "t" <on ("1") or off ("0")>  \<CR>      
- Response length   : 3 bytes

After the start, TerminalMode is switched off.
It can be switched on with "t" in order to monitor the data stream using ASCII characters with the help of a terminal program.

#### Initialization / register feedback modules:
##### Command form: "s" \<number of Modules left> \<number of Modules middle> \<number of Modules right> \<CR>

If the maximum number of modules of 31 is exceeded, the default value (2 modules per line) is set.

- Command length:\
-- TerminalMode off: 5 bytes\
-- TerminalMode on:  8 bytes\
 
- 1st response: "s" \<total number of registered modules> \<CR>\
-- The input statuses of the registered modules are read in between the 1st and 2nd response.
- 2nd response: "i" \<Number of modules that are reported> \<Module number> \<HighByte> \<LowByte> \<Module number> \<HighByte> \<LowByte> \<Module number> \<HighByte> \<LowByte> \<CR>

Response length:\
- TerminalMode off: (6 + (number of modules) * 3) bytes\ 
- TerminalMode on: (8 + (number of modules) * 6) bytes\

With the 2nd response, the contents of all registered modules are transferred.
The number of modules can be changed dynamically during the program run using the "s" command.
After the interface is switched on, changes to the inputs of the feedback modules (via "i") are only reported from the first "s" command.

#### HSI-88 report s change(s)
- Response: "i" \<number of modules reported> \<module number> \<high byte> \<low byte> \<module number> \<high byte> \<low byte> \<module number> \<HighByte> \<LowByte> \<CR>

Response length:
- TerminalMode off: (3 + (number of modules) * 3) bytes\ 
- TerminalMode on: (4 + (number of modules) * 6) bytes\

Only the contents of the modules for which the input statuses have changed are transferred.

### PC queries input statuses
#### Command form: "m" \<CR>
Command length: 2 bytes\
Response: "m" \<Number of modules that are reported> \<Module number> \<HighByte> \<LowByte> \<Module number> \<HighByte> \<LowByte> \<module number> \<HighByte> \<LowByte> \<CR>
Response length:\ 
- TerminalMode off: (3 + (number of modules) * 3) bytes\ 
- TerminalMode on: (4 + (number of modules) * 6) bytes\

The contents of all registered modules are transferred.

### Version query 
#### Command form: "v" \<CR>
Command length: 2 bytes\
Response: "Ver. x.xx / dd.mm.yy / HSI-88 / (c) LDT" \<CR> 

Response length: 41 bytes

## Loconet

LocoNet® Personal Use Edition 1.0
Digitrax Inc., Norcross GA 30071
October 16, 1997
©Copyrighted material, all rights reserved.

Specification: [https://www.digitrax.com/support/loconet/loconetpersonaledition.pdf](loconetpersonaledition.pdf) 

### Table of Contents

  - [Checksum](#checksum)
  - [Message Length](#message_length)
  - [Refresh Slots](#refresh_slots)
  - [Standard Address Selection](#standard_address_selection)
  - [Dispatching](#dispatching)
  - [Future Expansion Codes](#future_expansion_codes)
  - [2 Byte Message Opcodes](#byte_message_opcodes)
  - [4 Byte Message Opcodes](#byte_message_opcodes1)
  - [6 Byte Message Opcodes](#byte_message_opcodes2)
  - [Variable Byte Message Opcodes](#variable_byte_message_opcodes)
  - [LocoNet Parameter Summary](#loconet_parameter_summary)

## LocoNet Parameter Summary

## Checksum

The data bytes on LocoNet® are defined as 8 bit data with the most significant bit (transmitted last in the 8 bit octet) as an OPCODE flag bit.
If the MS bit, D7, is 1 the 7 least significant bits are interpreted as a network OPCODE.
The opcode byte may only occur once in a valid message and is the FIRST byte of a message.
All the remaining bytes in the message must have a most significant bit of 0, including the last CHECKSUM byte.

The CHECKSUM is the 1st COMPLEMENT of the byte wise Exclusive Or of all the bytes in the message, except the CHECKSUM itself.
To validate data accuracy, all bytes in a correctly formatted message are Exclusive Or'ed.
If this resulting byte value is 0xFF hexadecimal, the message data is accepted as good.

## Message Length

The OPCODES may be examined to determine message length and if subsequent response message is required.
Data bits D6 and D5 encode the message length.

**D3=1 implies Follow-on message/reply**:

| D7 | D6 | D5 | D4 | D3 | D2 | D1 | D0 |  |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 0 | F | D | C | B | A | Message is 2 bytes, including Checksum |
| 1 | 0 | 1 | F | D | C | B | A | Message is 4 bytes, inc. checksum |
| 1 | 1 | 0 | F | D | C | B | A | Message is 6 bytes, inc checksum |
| 1 | 1 | 1 | F | D | C | B | A | Message in N bytes, where next byte in message is a 7 bit BYTE COUNT. |

The A, B, C, D, F are bits available to encode 32 __OPCODES__ per message length.

## Refresh Slots

The model of the MASTER refresh stack is an array of up to 120 read/write refresh SLOTS.
The slot address is a principal component and is generally the second byte or 1st argument of a message to the master.
The refresh SLOT contains up to 10 data bytes relating to a Locomotive and also controls a task in the Track DCC refresh stack.
Most mobile decoder or Locomotive operations process the SLOT associated with the Locomotive to be controlled.
The SLOT number is a similar shorthand ID# to a “file handle” used to mark and process files in a DOS PC environment.

Slot addresses 120-127 are reserved for System and Master control.
Slot #124 (0x7C) is allocated for read/write access to the DCS100 programming track, and the format of the 10 data bytes is not the same as a “normal” slot. See later.

## Standard Address Selection

To request a MOBILE or LOCOMOTIVE decoder task in the refresh stack, a Throttle device requests a LOCOMOTIVE address for use, (opcode <0xBF>, <loco adr hi>, <loco adr lo>, <chk>).
The Master ( or PC in a Limited Master environment) responds with a SLOT DATA READ for the SLOT, (opcode <0xE7>), which **contains** the Locomotive address and all of its state information.
If the address is currently not in any SLOT, the master will load this NEW locomotive address into a new SLOT, {speed=0, FWD, Lite/Functions OFF and 128 step mode} and return this as a SLOT DATA READ.
If no inactive slots are free to load the NEW locomotive address, the response will be the Long Acknowledgment, (opcode <0xB4>), with a “fail” code, 0x00.
  
Note that regular “SHORT” 7 bit NMRA addresses are denoted by <loco-adr hi>=0. The Analog, Zero stretched, loco is selected when both <loco adr hi> == <loco adr lo>=0.
<Loco adr lo> is always a 7 bit value. When <loco adr hi> is non-zero then the Master will generate NMRA type 14 bit or “LONG” address packets using all 14 bits from <loco adr hi> and <loco adr lo> with Loco adr Hi being the MOST significant address bits.
Note that a DT200 Master does NOT process 14 bit adr requests and will consider the <loco adr hi> to always zero. You can check the <TRK> return bits to see if the Master is a DT200.
  
  
**The throttle must then examine the SLOT READ DATA bytes to work out how to process the Master response.**
If the STATUS1 byte shows the SLOT to be COMMON, IDLE or NEW the throttle may change the SLOT to IN_USE by performing a *NULL MOVE* instruction, (opcode <0xBA>, <slotX>,<slotX>,<chk>) on this SLOT.
**This activation mechanism is used to guarantee proper SLOT usage interlocking in a multi-user asynchronous environment.**
    
If the SLOT return information shows the Locomotive requested is IN_USE or UP-CONSISTED (i.e. the SL_CONUP, bit 6 of STATUS1 =1 ) the user should NOT use the SLOT.
Any UP_CONSISTED locos must be UNLINKED before usage! 
Always process the result from the LINK and UNLINK commands, since the Master reserves the right to change the reply slot number and can reject the linking tasks under several circumstances.
Verify the reply slot # and the Link UP/DN bits in STAT1 are as you expected.
  
The throttle will then be able to update Speed/Direction and Function information. 
Whenever SLOT information is changed in an active slot, the SLOT is flagged to be updated as the next DCC packet sent to the track.
If the SLOT is part of linked CONSIST SLOTS the whole CONSIST chain is updated consecutively.
  
If a throttle is disconnected from the LocoNet®, upon reconnection (if the throttle retains the SLOT state from before disconnection) it will request the full status of the SLOT it was previously using.
If the reported STATUS and Speed/Function data etc., from the master exactly matches the remembered SLOT state the throttle will continue using the SLOT.
If the SLOT data does not match, the throttle will assume the SLOT was purged free by the system and will go through the setup “log on” procedure again.
  
With this procedure the throttle does not need to have a unique “ID number”.
SLOT addresses DO NOT imply they contain any particular LOCOMOTIVE address.
The system can be mapped such that the SLOT address matches the LOCOMOTIVE address within, if the user directly Reads and Writes to SLOTs without using the Master to allocate Locomotive addresses.

## Dispatching

Active Locomotives (including Consist TOP) SLOTS may be released for assignment to BT2 throttles in the “DISPATCH” mode.
In this case a BT2 operating in its normal mode will request a DISPATCH SLOT that has been prepared by a supervisor type device.
This is included for Club type operations where simpler throttles with limited capabilities are given to Engineers (Operators) by the Hostler or Dispatcher.
    
To **DISPATCH PUT** a slot, perform a SLOT MOVE to Slot 0.
In this case the Destination Slot 0 is not copied to, but the source SLOT number is marked by the system as the DISPATCH slot.
This is only a “one deep stack”.
 
To **DISPATCH GET** perform a SLOT MOVE from Slot 0 (no destination needed).
If there is a DISPATCH marked slot in the system, a SLOT DATA READ (<0xE7>) with the SLOT information will
be the response. If there is NO DISPATCH slot, the response will be a LONG ACK ( opc <0xB4>) with the
Fail code 0x00.

## Future Expansion Codes

Immediate codes may be sent to the Master by a device.
These are converted to DCC packets and sent as the next packet to the rails.
They are not entered into any refresh stack.
These are available in a system based on the DCS100/“Chief”.
  
Opcodes for access to an auxiliary Service mode Programming Track are included.
These requests are not entered in the main DCC packet stream.
    
Note that several confusing expansions and opcode sequences have been stripped from this LocoNet® version.
An experimenter who implements this protocol correctly should have no problems running on a LocoNet® that has other expanded features.
Again, we recommend resisting the temptation to “optimise” or take shortcuts with this protocol since it will lead to guaranteed future problems with your hardware and software.

## 2 Byte Message Opcodes

FORMAT

```
<OPC>,<CKSUM>
```

| Symbol | Code | Description | Success | Fail |
| --- | --- | --- | --- | --- |
| **OPC_IDLE** | 0x85 | FORCE IDLE state, B'cast emerg. STOP | - | - |
| **OPC_GPON** | 0x83 | GLOBAL power ON request | - | - |
| **OPC_GPOFF** | 0x82 | GLOBAL power OFF req | - | - |
| **OPC_BUSY** | 0x81 | MASTER busy code, NUL | - | - |

## 4 Byte Message Opcodes

FORMAT

```
<OPC>,<ARG1>,<ARG2>,<CKSUM>
```

| Symbol | Code | Description | Arg1 | Arg2 | Success | Fail |
| --- | --- | --- | --- | --- | --- | --- |
| **OPC_LOCO_ADR** | 0xBF | Request loco address, if not found master puts address in a free slot. | 0 | ADR | OPC_SL_RD_DATA | OPC_LONG_ACK   No free slot available. ACK1=0 |
| **OPC_SW_ACK** | 0xBD | Request switch with acknowledge function. | SW1 | SW2 | OPC_LONG_ACK, ACK1=0x7F | OPC_LONG_ACK, ACK1=0x00 |
| **OPC_SW_STATE** | 0xBC | Request state of switch. | SW1 | SW2 | OPC_LONG_ACK, ACK1=0x7F | OPC_LONG_ACK, ACK1=0x00 |
| **OPC_RQ_SL_DATA** | 0xBB | Request slot data/status block | SLOT | 0 | OPC_SL_RD_DATA |  |
| **OPC_MOVE_SLOTS** | 0xBA | Move slot SRC to DST | SRC | DST | OPC_SL_RD_DATA |  |
| **OPC_LINK_SLOTS** | 0xB9 | Link slot ARG1 to slot ARG2 | SL1 | SL2 | OPC_SL_RD_DATA |  |
| **OPC_UNLINK_SLOTS** | 0xB8 | Unlink slot ARG1 from slot ARG2 | SL1 | SL2 | OPC_SL_RD_DATA |  |
| **OPC_CONSIST_FUNC** | 0xB6 | Set FUNC bits in a CONSIST uplink element | SLOT | DIRF |  |  |
| **OPC_SLOT_STAT1** | 0xB5 | Write slot stat1 | SLOT | STAT1 |  |  |
| **OPC_LONG_ACK** | 0xB4 | Long acknowledge | LOPC | ACK1 |  |  |
| **OPC_INPUT_REP** | 0xB2 | General sensor input codes | IN1 | IN2 |  |  |
| **OPC_SW_REP** | 0xB1 | Turnout sensor state report | SN1 | SN2 |  |  |
| **OPC_SW_REQ** | 0xB0 | Request switch function | SW1 | SW2 |  | OPC_LONG_ACK, LOPC=0x30, ACK1=0x00 |
| **OPC_LOCO_SND** | 0xA2 | Set slot sound function | SLOT | SND |  |  |
| **OPC_LOCO_DIRF** | 0xA1 | Set slot direction, function 0-4 state | SLOT | DIRF |  |  |
| **OPC_LOCO_SPD** | 0xA0 | Set slot speed | SLOT | SPD |  |  |

## 6 Byte Message Opcodes

FORMAT

```
<OPC>,<ARG1>,<ARG2>,<ARG3>,<ARG4>,<CKSUM>
```

| Symbol | Code | Description | Arg1 | Arg2 | Arg3 | Arg4 | Success | Fail |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **OPC_MULTI_SENSE** | 0xD0 | power management and transponding | type | zone and section | addr | addr |  |  |
| **OPC_UHLI_FUN** | 0xD4 | Function 9-28 by Uhlenbrock | 0x20 | slot | function group | function |  |  |

## Variable Byte Message Opcodes

FORMAT

```
<OPC>,<COUNT>,<ARG1>,<ARG2>,<ARG3>,...,<ARG(COUNT-3)>,<CKSUM>
```

| Symbol | Code | Description | Count | Arg1 | Arg2 | Arg3 | Arg4 | Arg5 | Arg6 | Arg7 | Arg8 | Arg9 | Arg10 | Arg11 | Response |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **OPC_WR_SL_DATA** | 0xEF | Write slot data. | 0x0E | SLOT# | STAT1 | ADR | SPD | DIRF | TRK | SS2 | ADR2 | SND | ID1 | ID2 | OPC_LONG_ACK |
| **OPC_WR_SL_DATA** | 0xEF | Write PT slot data. | 0x0E | 0x7C | [PCMD| 0 | HOPSA | LOPSA | TRK | CVH | CVL | DATA7 | 0 | 0 | OPC_LONG_ACK |
| **OPC_WR_SL_DATA** | 0xEF | Write Fast Clock slot data. | 0x0E | 0x7B | CLK_RATE | FRAC_MINSL | FRAC_MINSH | 256-MINS_60 | TRK | 256-HRS_24 | DAYS | CLK_CNTR | ID1 | [ID2 | OPC_LONG_ACK |
| **OPC_SL_RD_DATA** | 0xE7 | Slot data response. | 0x0E | SLOT#| STAT1| ADR | SPD | DIRF | TRK| SS2 | ADR2 | SND | ID1 | ID2 | - |
| - | 0xE6 | Programming service mode aborted. | 0x10/0x15 |  |  |  |  |  |  |  |  |  |  |  |  |

| Symbol | Code | Description | Count | Arg1 | Arg2 | Arg3 | Arg4 | Arg5 | Arg6 | Arg7 | Arg8 | Arg9 | Arg10 | Arg11 | Arg12 | Arg13 | Response |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **OPC_PEER_XFER** | 0xE5 | Move 8 bytes peer to peer, SRC→DST. | 0x10 | | DSTL | DSTH | PXCT1 |D1 | D2 | D3 | D4 | PXCT2 | D5 | D6 | D7 | D8 | - |

| Symbol | Code | Description | Count | Arg1 | Arg2 | Arg3 | Arg4 | Arg5 | Arg6 | Response |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **OPC_LISSY_REP** | 0xE4 | Lissy IR report | 0x08 | 0x00 | high unit, direction | low unit | high addr | low addr | - | - |
| **OPC_WHEELCNT_REP** | 0xE4 | Wheel counter report | 0x08 | 0x40 | high unit, direction | low unit | high count | low count | - | - |

| Symbol | Code | Description | Count | Arg1 | Arg2 | Arg3 | Arg4 | Arg5 | Arg6 | Arg7 | Arg8 | Arg9 | Response |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **OPC_LISSY_REP** | 0xE4 | RFID-5 report | 0x0C | 0x41 | addr. high | addr. low | RFID-0 | RFID-1 | RFID-2 | RFID-3 | RFID-4 | RFID-HI | - |

| Symbol | Code | Description | Count | Arg1 | Arg2 | Arg3 | Arg4 | Arg5 | Arg6 | Arg7 | Arg8 | Arg9 | Arg10 | Arg11 | Response |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **OPC_LISSY_REP** | 0xE4 | RFID-7 report | 0x0E | 0x41 | addr. high | addr. low | RFID-0 | RFID-1 | RFID-2 | RFID-3 | RFID-4 | RFID-5 | RFID-6 | RFID-HI | - |

| Symbol | Code | Description | Count | Arg1 | Arg2 | Arg3 | Arg4 | Arg5 | Arg6 | Arg7 | Arg8 | Response |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **OPC_IMM_PACKET¹** | 0xED | Send n-byte packet immediate. | 0x0B | 0x7F | REPS | DH1 | IM1 | IM2 | IM3 | IM4| IM5 | OPC_LONG_ACK   ACK1=0x7F if not limited   ACK1=lim if limited   ACK1=0x00 If busy |

¹)
The SLOT DATA bytes are, in order of TRANSMISSION for <E7> READ or <EF> WRITE. 
NOTE: SLOT 0 <E7> read will return MASTER config information bytes.


## LocoNet Parameter Summaryrs (one value = two bytes) are transmitted as hexadecimal values.

#### TerminalMode


### Parameters

| Name | Description | Bits/Range |
| --- | --- | --- |
| **ADR** | 7 bit loco address | short address, CV18 if ADR2 > 0 |
| **ADR2** | 7 bit high loco address | 0=short address, CV17 |
| **SW1** | 7 ls switch address bits | 0,A6,A5,A4,A3,A2,A1,A0 |
| **SW2** | 4 ms switch address and control bits | 0,0,DIR,ON,A10,A9,A8,A7 |
|  | DIR, switch direction | 1=closed(green), 0=thrown(red) |
|  | ON, switch activation | output 1=ON, 0=OFF |
| **SLOT** | 7 bit slot number | 0..127 |
| **SRC** | 7 bit source slot number | 0..127 |
| **DEST** | 7 bit destination slot number | 0..127 |
| **SL1** | 7 bit slave slot number | 0..127 |
| **SL2** | 7 bit master slot number | 0..127 |
| **SPD** | speed | 0x00=SPEED 0 ,STOP<br><br>0x01=SPEED 0 EMERGENCY stop<br><br>0x02-0x7F increasing SPEED,0x7F=MAX speed |
| **DIRF** | loco direction and functions(4) | 0,0,DIR,F0,F4,F3,F2,F1 |
| **SND** | slot sound | 0,0,0,0,F8,F7,F6,F5 |
| **STAT1** | slot status containing speed steps | See below |
| **SS2** | slot status 2 | 0,0,0,0,D3,D2,0,D0 |
|  | D0 | 1=this slot has suppressed ADV consist |
|  | D2 | 1=expansion ID1/2 is not ID usage |
|  | D3 | 1=expansion IN ID1/2, 0=encoded alias |
| **LOPC** | copy of opcode | bit 7 is set to zero |
| **ACK1** | response code | 0=failed |
| **IN1** | sensor address | 0,A6,A5,A4,A3,A2,A1,A0 |
| **IN2** | sensor address and status | 0,X,I,L,A10,A9,A8,A7 |
|  | X, control bit | 0=reserved for future |
|  | I, input source | 0=DS54, 1=switch |
|  | L, sensor level | 0=low, 1=high |
| **SN1** | turnout sensor address | 0,A6,A5,A4,A3,A2,A1,A0 |
| **SN2** | turnout sensor address and status | 0,1,I,L,A10,A9,A8,A7 |
|  | I, input source | 0=aux, 1=switch |
|  | L, sensor level | 0=low, 1=high |
| **SN2** | alternately turnout sensor address and status | 0,0,C,T,A10,A9,A8,A7 |
|  | C, closed output | 0=OFF, 1=ON |
|  | T, thrown output | 0=OFF, 1=ON |
| **ID1** | throttle/PC ID if SS2.4=1 | 7 bit ls |
| **ID2** | throttle/PC ID if SS2.4=1 | 7 bit ms |

### Slot Status 1

- **D7-SL_SPURGE**
  - 1=SLOT purge en,ALSO adrSEL (INTERNAL use only, not seen on NET!)

CONDN/CONUP: bit encoding-Control double linked Consist List

#### 2 BITS for Consist

- **D6-SL_CONUP**
- **D3-SL_CONDN**
  - 11=LOGICAL MID CONSIST , Linked up AND down
  - 10=LOGICAL CONSIST TOP, Only linked downwards
  - 01=LOGICAL CONSIST SUB-MEMBER, Only linked upwards
  - 00=FREE locomotive, no CONSIST indirection/linking

ALLOWS “CONSISTS of CONSISTS”. Uplinked means that Slot SPD number is now SLOT adr of SPD/DIR and STATUS of consist. i.e. is an Indirect pointer. This Slot has same BUSY/ACTIVE bits as TOP of Consist. TOP is loco with SPD/DIR for whole consist. (top of list). BUSY/ACTIVE: bit encoding for SLOT activity

#### 2 BITS for BUSY/ACTIVE

- **D5-SL_BUSY**
- **D4-SL_ACTIVE**
  - 11=IN_USE loco adr in SLOT -REFRESHED
  - 10=IDLE loco adr in SLOT, not refreshed
  - 01=COMMON loco adr IN SLOT, refreshed
  - 00=FREE SLOT, no valid DATA, not refreshed

#### 3 BITS for Decoder TYPE encoding for this SLOT

- **D2-SL_SPDEX**
- **D1-SL_SPD14**
- **D0-SL_SPD28**
  - 010=14 step MODE
  - 001=28 step. Generate Trinary packets for this Mobile ADR
  - 000=28 step/ 3 BYTE PKT regular mode
  - 011=128 speed mode packets
  - 111=128 Step decoder, Allow Advanced DCC consisting
  - 100=28 Step decoder ,Allow Advanced DCC consisting

## Slots

| Nr. | Description |
| --- | --- |
| 0 | dispatch |
| 1 - 119 | active locos |
| 120 - 127 | reserved for System and Master control |
| 123 | Fast Clock |
| 124 | Programming Track |
| 127 | Command Station Options |

## Programmer track

The programmer track is accessed as Special slot #124 ( $7C, 0x7C). It is a full asynchronous shared
system resource.

To start Programmer task, write to slot 124. There will be an immediate LACK acknowledge that
indicates what programming will be allowed. If a valid programming task is started, then at the final
(asynchronous) programming completion, a Slot read <E7> from slot 124 will be sent. This is the final
task status reply.

### Parameters

| Name | Description | Bits/Range |
| --- | --- | --- |
| **ACK1¹** | response code | 0=busy/aborted, 1=accepted(OPC_SL_RD_DATA), 0x40=accepted blind(OPC_SL_RD_DATA), 0x7F=not implemented |
| **PCMD** | programmer command (0 will abort current operation) | 0,WR,BM,TY1,TY0,OPS,0,0 |
|  | WR, Write/Read | 1=Write, 0=Read |
|  | MD, Byte mode | 1=byte operation, 0=bit operation (if possible) |
|  | OPS, Ops Mode | 1=Mainline, 0=PT |
| **PSTAT²** | programmer error flags | 0,0,0,0,D3,D2,D1,D0 |
|  | D3 | 1=User aborted |
|  | D2 | 1=No read ack |
|  | D1 | 1=No write ack |
|  | D0 | 1=Programming track empty |
| **HOPSA** | operations mode programming | 7 bit high addres, 0 if service mode (POM) |
| **LOPSA** | operations mode programming | 7 bit low addres, 0 if service mode (POM) |
| **CVH** | CV# | 0,0,CV9,CV8,0,0D7,CV7 |
| **CVL** | CV# | 0,CV6,CV5,CV4,CV3,CV2,CV1,CV0 |
| **DATA7** | data | 0,D6,D5,D4,D3,D2,D1,D0 |

¹)
Note that the <7F> code will occur in Operations Mode Read requests if the System is not configured for
and has no Advanced Acknowlegement detection installed.. Operations Mode requests can be made and
executed whilst a current Service Mode programming task is keeping the Programming track BUSY. If a
Programming request is rejected, delay and resend the complete request later. Some readback operations
can keep the Programming track busy for up to a minute. Multiple devices, throttles/PC's etc, can share
and sequentially use the Programming track as long as they correctly interpret the response messages .
Any Slot RD from the master will also contain the Programmer Busy status in bit 3 of the <TRK> byte.

²)
This <E7> response is issued whenever a Programming task is completed. It echos most of the request
information and returns the PSTAT status code to indicate how the task completed. If a READ was
requested <DATA7> and <CVH> contain the returned data, if the PSTAT indicates a successful readback
(typically =0). Note that if a Paged Read fails to detect a successful Page write acknowledge when first
setting the Page register, the read will be aborted, showing no Write acknowledge flag D1=1

### Type codes

| Byte Mode | Ops Mode | TY1 | TY0 | Meaning |
| --- | --- | --- | --- | --- |
| 1 | 0 | 0 | 0 | Paged mode byte Read/Write on Service Track |
| 1 | 0 | 0 | 0 | Paged mode byte Read/Write on Service Track |
| 1 | 0 | 0 | 1 | Direct mode byteRead/Write on Service Track |
| 0 | 0 | 0 | 1 | Direct mode bit Read/Write on Service Track |
| x | 0 | 1 | 0 | Physical Register byte Read/Write on Service Track |
| x | 0 | 1 | 1 | Service Track- reserved function |
| 1 | 1 | 0 | 0 | Ops mode Byte program, no feedback |
| 1 | 1 | 0 | 1 | Ops mode Byte program, feedback |
| 0 | 1 | 0 | 0 | Ops mode Bit program, no feedback |
| 0 | 1 | 0 | 1 | Ops mode Bit program, feedback |

## Multi Sense

Power and transponding information is brought with the
**`OPC_MULTI_SENSE`**
opcode.

#### Power

| Variable | Value |
| --- | --- |
| ARG1 | ? |
| ARG2 | ? |
| ARG3 | ? |
| ARG4 | ? |

#### Transponding

| Variable | Value |
| --- | --- |
| ARG1 | type, present/absent, and high part board address |
| ARG2 | low part board address and Zone A to H in the lower 4 bits |
| ARG3 | high part of transponding mobile decoder address |
| ARG4 | low part of transponding mobile decoder address |

## Functions 9-12

_Digitrax / Uhlenbrock_

| Variable | Value | Variable | Value |
| --- | --- | --- | --- |
| OPC | OPC_IMM_PACKET | OPC | OPC_UHLI_FUN |
| REPS | 0x24 for short and 0x34 for long address | Arg1 | 0x20 |
| DHI | 0x02 or 0x04 + high bits of address and functions | Arg2 | Slot# |
| IM1 | long address Lo | Arg3 | 0x07 |
| IM2 | long address Hi | Arg4 | f9=0x10, f10=0x20, f11=0x40 |
| IM3 | 0x20 + function bits & 0x7f |  |  |
| IM4 | 0x00 |  |  |
| IM5 | 0x00 |  |  |
| IM1 | short address |  |  |
| IM2 | 0x20 + function bits & 0x7f |  |  |
| IM3 | 0x00 |  |  |
| IM4 | 0x00 |  |  |
| IM5 | 0x00 |  |  |

## Functions 13-20

_Digitrax / Uhlenbrock_

| Variable | Value | Variable | Value |
| --- | --- | --- | --- |
| OPC | OPC_IMM_PACKET | OPC | OPC_UHLI_FUN |
| REPS | 0x34 for short and 0x44 for long address | Arg1 | 0x20 |
| DHI | 0x02 or 0x04 + high bits of address and functions | Arg2 | Slot# |
| IM1 | long address Lo | Arg3 | 0x05=f12+f20+f28, 0x08=f13-f19 |
| IM2 | long address Hi | Arg4 | f12=0x10, f20=0x20, f28=0x40, f13=0x01…f19=0x40 |
| IM3 | 0x5E |  |  |
| IM4 | function bits & 0x7f |  |  |
| IM5 | 0x00 |  |  |
| IM1 | short address |  |  |
| IM2 | 0x5E |  |  |
| IM3 | function bits & 0x7f |  |  |
| IM4 | 0x00 |  |  |
| IM5 | 0x00 |  |  |

## Functions 21-28

_Digitrax / Uhlenbrock_

| Variable | Value | Variable | Value |
| --- | --- | --- | --- |
| OPC | OPC_IMM_PACKET | OPC | OPC_UHLI_FUN |
| REPS | 0x34 for short and 0x44 for long address | Arg1 | 0x20 |
| DHI | 0x06 + high bits of address and functions | Arg2 | Slot# |
| IM1 | long address Lo | Arg3 | 0x09 |
| IM2 | long address Hi | Arg4 | f21=0x01…f27=0x40 |
| IM3 | 0x5F |  |  |
| IM4 | function bits & 0x7f |  |  |
| IM5 | 0x00 |  |  |
| IM1 | short address |  |  |
| IM2 | 0x5F |  |  |
| IM3 | function bits & 0x7f |  |  |
| IM4 | 0x00 |  |  |
| IM5 | 0x00 |  |  |

## Fast Clock

The system FAST clock and parameters are implemented in Slot#123 <7B>.

Use <EF> to write new clock information, Slot read of 0x7B,<BB><7B>.., will return current System
clock information, and other throttles will update to this SYNC. Note that all attached display devices
keep a current clock calculation based on this SYNC read value, i.e. devices MUST not continuously poll
the clock SLOT to generate time, but use this merely to restore SYNC and follow current RATE etc. This
clock slot is typically “pinged” or read SYNC'd every 70 to 100 seconds , by a single user, so all attached
devices can synchronise any phase drifts. Upon seeing a SYNC read, all devices should reset their local
sub-minute phase counter and invalidate the SYNC update ping generator.

### Parameters

| Name | Description | Bits/Range |
| --- | --- | --- |
| **CLK_RATE** | Clock rate | 0=Freeze clock, 1=normal 1:1 rate, 10=10:1 etc, max VALUE is 7F/128 to 1 |
| **FRAC_MINSH/L** | FRAC mins hi/lo are a sub-minute counter , depending on the CLOCK generator. Not for ext. usage. | This counter is reset when valid <E6><7B> SYNC msg seen. |
| **256-MINS_60** | This is FAST clock MINUTES subtracted from 256. | Modulo 0-59 |
| **256-HRS_24** | This is FAST clock HOURS subtracted from 256. | Modulo 0-23 |
| **DAYS** | number of 24 Hr clock rolls, positive count |  |
| **CLK_CNTRL** | Clock Control Byte | D6- 1=This is valid Clock information, 0=ignore this <E6><7B>, SYNC reply |
| **ID1/2** | This is device ID last setting the clock. | <00><00> shows no set has happened, <7F><7x> are reserved for PC access |

## Stationary Broadcast Command

Note that a 3 byte DCC track packet configured as:

```text
<sync> ,<1011-1111>,<1000-D c b a > <ecb>
```

is a DCC Broadcast Address to Stationary decoders.
Broadcast LocoNet Switch adr is then

```text
<SW2>=<0,0,a,D-1,1,1,1>, <SW1>=<0,1,1,1-1,0,c,b>
```

## Stationary Interrogate Command

The DCC packet

```text
<sync>,<1011-1111>,<1100-D c b a> <ecb>
```

is an Interrogation for all DS54's. This
causes a 2 LocoNet <B1> messages encoding both Output state and Input state, for each sensor adr a/b/c
encodes.
Interrogate LocoNet Switch adr is

```text
<SW2>=<0,0,a,1-0,1,1,1>, <SW1>= <0,1,1,1-1,0,c,b>
```

This is generated by DCS100 at power ON, and scans all 8 inputs of all DS54's.



