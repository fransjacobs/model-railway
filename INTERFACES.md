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
If TerminalMode is switched on, ASCII characters (one value = two bytes) are transmitted as hexadecimal values.

#### TerminalMode
##### Command form   : "t" \<CR>
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

### Checksum

The data bytes on LocoNet® are defined as 8 bit data with the most significant bit (transmitted last in the 8 bit octet) as an OPCODE flag bit. If the MS bit , D7, is 1 the 7 least significant bits are interpreted as a network OPCODE . The opcode byte may only occur once in a valid message and is the FIRST byte of a message. All the remaining bytes in the message must have a most significant bit of 0 , including the last CHECKSUM byte. The CHECKSUM is the 1's COMPLEMENT of the byte wise Exclusive Or of all the bytes in the message, except the CHECKSUM itself. To validate data accuracy, all the bytes in a correctly formatted message are Exclusive Or'ed. If this resulting byte value is “FF” hexadecimal, the message data is accepted as good.


### Message Length

The OPCODES may be examined to determine message length and if subsequent response message is required. Data bits D6 and D5 encode the message length. D3=1 implies Follow-on message/reply:


| D7 | D6 | D5 | D4 | D3 | D2 | D1 | D0 |    |
|----|----|----|----|----|----|----|----|----|	
| 1  |  0 |  0 |  F |  D |  C |  B | A  | Message is 2 bytes, including Checksum |
| 1  |  0 |  1 |  F |  D |  C |  B | A  | Message is 4 bytes, including checksum |
| 1  |  1 |  0 |  F |  D |  C |  B | A 	| Message is 6 bytes, including checksum |
| 1  |  1 |  1 |  F |  D |  C |	 B | A  | Message in N bytes, next byte in message contains a 7 bit Byte count |

The A, B, C, D, F are bits available to encode 32 OPCODES per message length.

### Refresh Slots

The model of the MASTER refresh stack is an array of up to 120 read/write refresh SLOTS. The slot address is a principal component and is generally the second byte or 1st argument of a message to the master. The refresh SLOT contains up to 10 data bytes relating to a Locomotive and also controls a task in the Track DCC refresh stack. Most mobile decoder or Locomotive operations process the SLOT associated with the Locomotive to be controlled. The SLOT number is a similar shorthand ID# to a “file handle” used to mark and process files in a DOS PC environment. Slot addresses 120-127 ARE reserved for System and Master control. Slot #124 ($7C) is allocated for read/write access to the DCS100 programming track, and the format of the 10 data bytes is not the same as a “normal” slot. See later.
 
### Standard Address Selection

To request a MOBILE or LOCOMOTIVE decoder task in the refresh stack, a Throttle device requests a LOCOMOTIVE address for use,( opcode <BF>,<loco adr hi>,<loco adr lo>, <chk> ). The Master ( or PC in a Limited Master environment) responds with a SLOT DATA READ for the SLOT ,( opcode <E7>,,) ,that contains this Locomotive address and all of its state information. If the address is currently not in any SLOT, the master will load this NEW locomotive address into a new SLOT ,[speed=0, FWD, Lite/Functions OFF and 128 step mode]and return this as a SLOT DATA READ. If no inactive slots are free to load the NEW locomotive address, the response will be the Long Acknowledgment ,(opcode <B4>,) , with a “fail” code, 0.
Note that regular “SHORT” 7 bit NMRA addresses are denoted by <loco-adr hi>=0. The Analog , Zero stretched, loco is selected when both <loco adr hi>=<loco adr lo>=0. <Loco adr lo> is always a 7 bit value. If <loco adr hi> is non-zero then the Master will generate NMRA type 14 bit or “LONG” address packets using all 14 bits from <loco adr hi> and <loco adr lo> with Loco adr Hi being the MOST significant address bits. Note that a DT200 Master does NOT process 14 bit adr requests and will consider the <loco adr hi> to always zero. You can check the <TRK> return bits to see if the Master is a DT200.

The throttle must then examine the SLOT READ DATA bytes to work out how to process the Master response. If the STATUS1 byte shows the SLOT to be COMMON, IDLE or NEW the throttle may change the SLOT to IN_USE by performing a NULL MOVE instruction ,(opcode <BA>,<slotX>,<slotX>,<chk> ) on this SLOT. This activation mechanism is used to guarantee proper SLOT usage interlocking in a multi-user asynchronous environment.

If the SLOT return information shows the Locomotive requested is IN_USE or UP-CONSISTED (i.e. the SL_CONUP, bit 6 of STATUS1 =1 ) the user should NOT use the SLOT. Any UP_CONSISTED locos must be UNLINKED before usage! Always process the result from the LINK and UNLINK commands, since the Master reserves the right to change the reply slot number and can reject the linking tasks under several circumstances. Verify the reply slot # and the Link UP/DN bits in STAT1 are as you expected.

The throttle will then be able to update Speed./Direction and Function information . Whenever SLOT information is changed in an active slot , the SLOT is flagged to be updated as the next DCC packet sent to the track. If the SLOT is part of linked CONSIST SLOTS the whole CONSIST chain is updated consecutively.

If a throttle is disconnected from the LocoNet®, upon reconnection (if the throttle retains the SLOT state from before disconnection) it will request the full status of the SLOT it was previously using. If the reported STATUS and Speed/Function data etc., from the master exactly matches the remembered SLOT state the throttle will continue using the SLOT. If the SLOT data does not match, the throttle will assume the SLOT was purged free by the system and will go through the setup “log on” procedure again.

With this procedure the throttle does not need to have a unique “ID number”. SLOT addresses DO NOT imply they contain any particular LOCOMOTIVE address. The system can be mapped such that the SLOT address matches the LOCOMOTIVE address within, if the user directly Reads and Writes to SLOTs without using the Master to allocate Locomotive addresses


### Dispatching

Active Locomotives (including Consist TOP) SLOTS may be released for assignment to BT2 throttles in the “DISPATCH” mode. In this case a BT2 operating in its normal mode will request a DISPATCH SLOT that has been prepared by a supervisor type device. This is included for Club type operations where simpler throttles with limited capabilities are given to Engineers (Operators) by the Hostler or Dispatcher.

To DISPATCH PUT a slot , perform a SLOT MOVE to Slot 0. In this case the Destination Slot 0 is not copied to, but the source SLOT number is marked by the system as the DISPATCH slot. This is only a “one deep stack”.

To DISPATCH GET, perform a SLOT MOVE from Slot 0 (no destination needed). If there is a DISPATCH marked slot in the system, a SLOT DATA READ ( <E7>,,,) with the SLOT information will be the response. If there is NO DISPATCH slot, the response will be a LONG ACK ( opc <B4>,,) with the Fail code,00.


###Future Expansion Codes

Immediate codes may be sent to the Master by a device. These are converted to DCC packets and sent as the next packet to the rails. They are not entered into any refresh stack. These are available in a system based on the DCS100/“Chief”.

Opcodes for access to an auxiliary Service mode Programming Track are included. These requests are not entered in the main DCC packet stream .

Note that several confusing expansions and opcode sequences have been stripped from this LocoNet® version. An experimenter who implements this protocol correctly should have no problems running on a LocoNet® that has other expanded features. Again, we recommend resisting the temptation to “optimise” or take shortcuts with this protocol since it will lead to guaranteed future problems with your hardware and software.


### 2 Byte Message Opcodes

FORMAT:

| ** < OPC >** , __< CKSUM >__ |
|---------------| 

