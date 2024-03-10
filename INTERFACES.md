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