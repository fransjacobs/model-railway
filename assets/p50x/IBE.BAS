DECLARE FUNCTION SOGet% (n AS INTEGER)
DEFINT A-Z
DECLARE FUNCTION PrHex$ (n AS INTEGER)
DECLARE SUB ShowHelp ()
DECLARE SUB IBStatus ()
DECLARE SUB TurnoutEvent ()
DECLARE SUB IREvent ()
DECLARE SUB SensorEvent ()
DECLARE SUB LokEvent ()
DECLARE SUB PTCkTask (s AS STRING)
DECLARE SUB PTCkOff ()
DECLARE SUB PTEvent ()
DECLARE SUB ShowSysSts (s AS INTEGER)
DECLARE SUB PrintBin (v AS INTEGER)
DECLARE SUB PrintMAG (n AS INTEGER)
DECLARE FUNCTION IBCommInit (s AS STRING)
DECLARE FUNCTION P50XCmd$ (a AS STRING)
DECLARE SUB Delay (s AS SINGLE)

DIM SHARED P50 AS INTEGER
DIM SHARED P50x AS INTEGER
DIM SHARED c1$
DIM SHARED c2$
DIM SHARED Baud$
DIM SHARED OldBaud$
DIM SHARED ComPort AS INTEGER

DIM SHARED PrtChr AS INTEGER    'Whether P50XCmd should print to the screen or not
PrtChr = -1

DIM SHARED Prompt AS INTEGER    'Whether P50XCmd should wait for IB answer or not
Prompt = -1

CONST Pmt = "]"                 'IB prompt: last char of P50Xa cmd reply


CONST XSensOff = &H99

CONST XStatus = &HA2            'P50Xb commands

CONST XEvent = &HC8
CONST XEvtLok = &HC9
CONST XEvtTrn = &HCA
CONST XEvtSen = &HCB
CONST XEvtIR = &HCC
CONST XEvtLN = &HCD
CONST XEvtPT = &HCE


DIM Loks(20, 2) AS INTEGER

CLS
PRINT
PRINT "*** Intellibox"
PRINT "    Event processing demonstation program (v 1.0 - Jan '99 - by St.Ch-B.)"
PRINT : PRINT

c2$ = ",N,8,2,CS5000,tb16,rb2048"        'Somewhat application dependent
IF NOT (IBCommInit("19200")) THEN SYSTEM: END
OPEN c1$ + Baud$ + c2$ FOR RANDOM AS #1

PRINT
PRINT "Using "; Baud$; " bps"


PRINT
PRINT "*** *** *** Type '?' or 'h' for a list of commands *** *** ***"
PRINT

ON ERROR GOTO SerErr            'Used for "external" power off detection.
                                'A better way would be: using the 'RT' cmd
                                'for setting a very low CTS 'off' time and
                                'then detect a power off only using events


PRINT #1, "X"; CHR$(XSensOff);  'Force initial event for all 'on' sensors
u$ = INPUT$(1, #1)              'Make sure XSensOff reply is received

t! = TIMER
PollTime! = .1
PRINT "Events are polled every"; STR$(PollTime!); " Seconds."

EvtLoop:
u$ = UCASE$(INKEY$)
IF ((t! + PollTime!) > TIMER) AND u$ = "" THEN GOTO EvtLoop

t! = TIMER

IF u$ = "" THEN
    PRINT #1, "x"; CHR$(XEvent);      'Are there any events?
    v% = ASC(INPUT$(1, #1))
    IF v% <> 0 THEN
        PRINT : PRINT "*** Event flags from XEvent cmd = $"; PrHex(v%)
    END IF
  
   
    '*** N.B. this must be the very 1st test performed on v%!!
    IF (v% AND &H80) <> 0 THEN
        PRINT "Reading extended event mask #2: $";
        v2% = ASC(INPUT$(1, #1))
        PRINT PrHex(v2%)
        IF (v2% AND &H80) THEN
            PRINT "Reading extended event mask #3: $";
            v3% = ASC(INPUT$(1, #1))
            PRINT PrHex(v3%)
        ELSE
            v3% = 0
        END IF
    ELSE
        v2% = 0
        v3% = 0
    END IF
   
   
    IF (v% AND 1) <> 0 THEN
        LokEvent
    END IF
   
   
    IF (v% AND 2) <> 0 THEN
        IREvent
    END IF
   
   
    IF (v% AND 4) = 4 THEN
        SensorEvent
    END IF
   
   
    IF (v% AND 8) = 8 THEN
        PRINT "There has been a Power Off."
    END IF
   
    IF (v% AND &H10) = &H10 THEN
        PRINT "Attempt at switching PC-reserved accessories!"
    END IF
   
   
    IF (v% AND &H20) = &H20 THEN
        TurnoutEvent
    END IF

   
    IF (v% AND &H40) <> 0 THEN
        PRINT "Unsupported/undocumented event flag."
    END IF
  
   
    'Process v2%
   
    IF (v2% AND 1) <> 0 THEN
        BEEP
        PRINT "Short on an external Booster!"
    END IF

    IF (v2% AND 2) <> 0 THEN
        BEEP
        PRINT "Short on Lokmaus bus!"
    END IF

    IF (v2% AND 4) <> 0 THEN
        BEEP
        PRINT "Short from IB (internal) booster!"
    END IF

    IF (v2% AND 8) <> 0 THEN
        BEEP
        PRINT "Overload or short on PT, C/D or Rail Sync +/- lines!"
    END IF

    IF (v2% AND &H10) <> 0 THEN
        BEEP
        PRINT "Illegal electrical contact btw PT, C/D or Rail Sync +/- and normal tracks!"
    END IF

    IF (v2% AND &H20) <> 0 THEN
        BEEP
        PRINT "IB overheated!"
    END IF

    IF (v2% AND &H40) <> 0 THEN
        IBStatus
    END IF


    'Process v3%
  
    IF (v3% AND 1) <> 0 THEN
        PTEvent
    END IF
   
    IF (v3% AND 2) <> 0 THEN
        BEEP
        PRINT "RS-232 rx overflow!"
    END IF

END IF


SELECT CASE u$
CASE "H", "?"
    ShowHelp

CASE "S"
    PRINT : PRINT "Reading first 8 s88 modules (using P50 cmd):"
    PRINT #1, CHR$(&H88);
    PRINT "    1......8-9......16  <- contact number"
    FOR i = 1 TO 8
        PRINT USING "##"; i;
        PRINT ": ";
        B$ = INPUT$(1, #1)
        PrintBin ASC(B$)
        PRINT " ";
        B$ = INPUT$(1, #1)
        PrintBin ASC(B$)
        PRINT
    NEXT

CASE "E"
    PTEvent

CASE "P"
    PRINT #1, "x"; CHR$(&HE0);
    p% = ASC(INPUT$(1, #1))
    PRINT "PT status: ";
    IF (p% AND 2) THEN PRINT " on";  ELSE PRINT " off";
    IF (p% AND 1) THEN PRINT ", 'auto'";  ELSE PRINT ", 'PT only'";
    IF (p% AND 128) THEN PRINT " - a PT event is pending!"
    p% = ASC(INPUT$(1, #1))
    IF p% = 0 THEN PRINT " - PT is idle" ELSE PRINT " - PT is busy"

CASE "0"
    PRINT #1, "x"; CHR$(&HE2);
    PTCkOff

CASE "1"
    PRINT #1, "x"; CHR$(&HE1);
    p% = ASC(INPUT$(1, #1))
    IF p% = 0 THEN PRINT "Ok (PT relay is on)" ELSE PRINT "Unexpected error!"

CASE "X"
    PRINT #1, "x"; CHR$(&HE4);
    PTCkTask "PT task started: Selectrix Read"

CASE "R"
    Register = 1
    PRINT #1, "x"; CHR$(&HEC); CHR$(Register); CHR$(0);
    a$ = "PT task started: DCC read of register #" + STR$(Register)
    PTCkTask a$

CASE "D"
    PRINT #1, "x"; CHR$(&HF4);
    PTCkTask "PT task started: does the DCC decoder support direct bit read?"

CASE "B"
    CV% = 1
    PRINT #1, "x"; CHR$(&HF2); CHR$(CV% AND &HFF); CHR$(INT(CV% / 256));
    a$ = "PT task started: DCC direct bit read of CV #" + STR$(CV%)
    PTCkTask a$

CASE "L"
    PRINT #1, "x"; CHR$(&HF5);
    PTCkTask "PT task started: long address read"

CASE "T"
    PRINT #1, "x"; CHR$(&HFE);
    n = ASC(INPUT$(1, #1))
    SELECT CASE n
    CASE 0
        PRINT "PT task terminated."
    CASE &HF3
        PRINT "No PT task is active!"
    CASE &HF2
        PRINT "Could not terminate task!"
    CASE ELSE
        PRINT "Unexpected reply to 'XPT_Term' cmd: "; PrHex$(n); "h"
    END SELECT


'Some P50 commands

CASE "."
    PRINT #1, CHR$(97);

CASE "!"
    PRINT #1, CHR$(96);

CASE "2"
    PRINT #1, CHR$(33); CHR$(1); CHR$(32);

CASE "3"
    PRINT #1, CHR$(34); CHR$(1); CHR$(32);

END SELECT

IF (u$ <> "Q") AND (u$ <> CHR$(27)) THEN GOTO EvtLoop


IF Baud$ <> "2400" THEN
    PrtChr = 0
    a$ = P50XCmd("b2400")
    PRINT : PRINT
    PRINT "Baud rate set to 2400 bps"
END IF
CLOSE
ON ERROR GOTO 0
SYSTEM
END


SerErr:
IF ERR = 24 THEN    'I.e. if a device timeout error
    PRINT : PRINT "Waiting for 'RT' cmd (P50X ASCII) timeout...": PRINT
    RESUME
ELSE
    PRINT : PRINT "Error "; ERR: STOP
END IF


OpenErr:
PRINT
SYSTEM
END


UserEsc:
PRINT
PRINT "*** Terminated by <Esc>"
PRINT
SYSTEM
END


SerOpen:
IF ERR = 24 THEN GOTO CTSErr
PRINT "*** Error opening COM port. Try another COM port number."
RESUME PrgExit
PrgExit:
PRINT
SYSTEM
END


CTSErr:
e = ERR
IF e = 24 THEN    'Device timeout
    PRINT
    PRINT "*** CTS timeout: IB not connected?": PRINT
    PRINT "Press <Return> to retry, <Esc> to abort"
    DO
        SELECT CASE INKEY$
        CASE CHR$(13)
            PRINT
            RESUME
        CASE CHR$(27)
            RESUME UserEsc
        END SELECT
    LOOP
ELSE
    ON ERROR GOTO 0     'Let QBasic show the error
END IF
BEEP
PRINT "Error: "; e
PRINT
SYSTEM
END

DEFSNG A-Z
SUB Delay (s AS SINGLE)

t = TIMER
WHILE t + s > TIMER: WEND

END SUB

DEFINT A-Z
FUNCTION IBCommInit (s AS STRING)

'Use s = "" (empty string) in oder to keep current IB baud rate
'or set s = "2400" .. "19200" to force a specific baud rate

SHARED c1$, c2$, Baud$
SHARED P50 AS INTEGER
SHARED P50x AS INTEGER

GetCOM:
PRINT "Select COM port number 1..2 (default: 1) ";
INPUT p$
IF p$ <> "" THEN
    ComPort = VAL(p$)
    IF ComPort < 1 OR ComPort > 2 THEN
        PRINT
        PRINT "Please enter a valid (QBasic) COM port number!"
        PRINT
        GOTO GetCOM
    END IF
ELSE
    ComPort = 1
END IF
c1$ = "COM" + CHR$(ASC("0") + ComPort) + ":"


'Clear any pending I/O (garbage)
OPEN c1$ + "2400" + c2$ FOR RANDOM AS #1
Delay .2
WHILE LOC(1) > 0
    a$ = INPUT$(1, #1)
WEND
CLOSE #1


PRINT "Generating a break (so as to clear the IB RS-232 input buffer)...";
SELECT CASE ComPort
    CASE 1
        LSR% = &H3F8 + 3
    CASE 2
        LSR% = &H2F8 + 3
END SELECT


Delay .2
OUT LSR%, INP(LSR%) OR &H40     'Set Break bit in the UART
Delay 1
OUT LSR%, INP(LSR%) AND &HBF    'Reset Break bit
Delay .6
PRINT " done."

PRINT
PRINT "Trying to identify the currently selected baud rate..."
Bd& = 2400
Found = 0

P50 = -1        'Default: assume both protocols are enabled
P50x = -1

DO
    Baud$ = STR$(Bd&)
    Baud$ = RIGHT$(Baud$, LEN(Baud$) - 1)

    ON ERROR GOTO SerOpen
    OPEN c1$ + Baud$ + c2$ FOR RANDOM AS #1
    ON ERROR GOTO CTSErr

    PRINT #1, CHR$(&HC4);           '&hC4 = 196 = read s88 Module #4
                                    '(but also XDummy P50Xb cmd)

    Delay .2                        'Allow some time for eventual s88 data

    SELECT CASE LOC(1)
    CASE 2                          'If we did rx the correct amount of data
        Found = -1
        CLOSE #1

    CASE 1                          'The P50 protocol may have been disabled!
        IF (INPUT$(1, #1) = "D") THEN
            BEEP
            PRINT
            PRINT "Intellibox in download mode: do NOT proceed!"
            CLOSE #1
            IBCommInit = 0
            EXIT FUNCTION
        END IF
P50Err:
        Found = -1
        P50 = 0
        PRINT : PRINT
        Prompt = 0
        a$ = P50XCmd("ZzA0")
        Prompt = -1
        PRINT "P50 re-enabled automatically."
        PRINT : PRINT
        CLOSE #1
        P50 = -1

    CASE ELSE
        CLOSE #1
        PRINT "... not "; Baud$; " bps"
        Bd& = Bd& + Bd&
        Delay .1                    'Wait some before retrying

    END SELECT

LOOP UNTIL Found OR (Bd& = 38400)   'Change to 76800 if identif. up to 38400 bps


IF Found THEN
    PRINT "The current IB baud rate should be: "; Baud$; " bps"
  
    OPEN c1$ + Baud$ + c2$ FOR RANDOM AS #1
  
    PRINT
    PRINT "Testing for P50 / P50X / Mixed mode... -> ";
    PRINT #1, "X"           'Which mode is selected? (P50/P50X/Mixed)
    Delay .2

    SELECT CASE LOC(1)
    CASE 2, 3               'I.e. IB accepted the 'X'+CR as an empty P50Xa cmd
        PRINT "Mixed"
        a$ = INPUT$(LOC(1), #1) 'Discard IB answer to 'X' (empty) P50Xa cmd: (CR) + CR + "]"
     
      
        IF s = "" THEN      'I.e. if no baud rate forced by IBCommInit caller
          
            IF (Baud$ = "2400") AND (SOGet(26) <> 0) THEN   'SO #26 set if Break -> 2400 bps
              
                SELECT CASE SOGet(1)    'SO #1 tells IB baud rate
                CASE 0
                    Baud$ = "2400"
                CASE 1
                    Baud$ = "4800"
                CASE 2
                    Baud$ = "9600"
                CASE 3
                    Baud$ = "19200"
                CASE 4
                    Baud$ = "38400"
                    PRINT "Error: 38400 bps not supported by QBasic!"
                    STOP
                END SELECT
          
                PrtChr = 0
                a$ = P50XCmd$("B" + Baud$)
                Delay .2
          
            END IF
          
        ELSE
            IF (s = "2400") OR (s = "4800") OR (s = "9600") OR (s = "19200") THEN
          
                IF s <> Baud$ THEN
                    Baud$ = s
                    PrtChr = 0
                    a$ = P50XCmd$("B" + Baud$)
                    Delay .2
                END IF
          
            ELSE

                PRINT "IBCommInit parameter 's' (='"; s; "') not legal!"
                STOP
          
            END IF
      
        END IF
      
        CLOSE #1
        IBCommInit = -1     'Ok!
 
    CASE IS > 10            'I.e. IB answer was: 'Unknown command'+CR+']'
        PRINT "P50X only"   '(this implies that only the P50X is active)
        PRINT
        GOTO P50Err

    CASE ELSE               'I.e. only the P50 is active
        P50x = 0
        PRINT "P50 only"
        PRINT
        PRINT "### Error: no Intellibox detected!"
        PRINT
        CLOSE #1
        IBCommInit = 0

    END SELECT

ELSE
    PRINT
    PRINT "### Error: unable to identify the baud rate!"
    PRINT "    (could be 38400 bps)"
    IBCommInit = 0

END IF

END FUNCTION

DEFSNG A-Z
SUB IBStatus

PRINT #1, "x"; CHR$(XStatus);
a$ = INPUT$(1, #1)
PRINT : PRINT "Current ICUM status: "; : PrintBin ASC(a$)
PRINT ", i.e.:"
ShowSysSts ASC(a$)
PRINT

END SUB

SUB IREvent

PRINT "Infra-Red event(s):"
PRINT #1, "x"; CHR$(XEvtIR);
a$ = INPUT$(1, #1)
n = ASC(a$)
FOR i = 1 TO n
    a$ = INPUT$(2, #1)
    PRINT PrHex(ASC(a$)); "h, "; PrHex(ASC(RIGHT$(a$, 1))); "h"
NEXT i

END SUB

SUB LokEvent

PRINT "Lok event(s):"
PRINT #1, "x"; CHR$(XEvtLok);
DO
    a$ = INPUT$(1, #1)
    n = ASC(a$)
    SELECT CASE n
    CASE IS > &H80
        PRINT "Unexpected reply to XEvtLok cmd!"
        BEEP
        STOP
    CASE IS < &H80
        a$ = INPUT$(4, #1)      'Get Functions and Lok# (plus Dir and FL)
        r% = ASC(RIGHT$(a$, 1)) 'Real Speed
        i% = ASC(MID$(a$, 3, 1))
        LokNum = ASC(MID$(a$, 2, 1)) + (i% AND &H3F) * 256
        PRINT "Lok #";
        PRINT USING "####"; LokNum;
        Speed = n
        PRINT ", Speed=";
        SELECT CASE Speed
        CASE 0
            PRINT "Stop";
        CASE 1
            PRINT "Em. Stop";
        CASE ELSE
            PRINT USING "###"; Speed;
        END SELECT
        PRINT ", FL="; (i% AND &H40) / &H40;
        PRINT ", Dir=";
        PRINT (i% AND &H80) / &H80; "(";
        IF (i% AND &H80) = 0 THEN PRINT "reverse)";  ELSE PRINT "forward)";
        PRINT ", F1..F8= ";
        PrintBin (ASC(LEFT$(a$, 1)))
        PRINT ", rSp="; r%
    END SELECT
LOOP UNTIL n = &H80

END SUB

FUNCTION P50XCmd$ (a AS STRING)

IF P50 THEN PRINT #1, "x";
PRINT #1, a

IF NOT Prompt THEN
    P50XCmd = " "
    EXIT FUNCTION
END IF

p$ = ""
DO
    WHILE LOC(1) = 0
        IF INKEY$ = CHR$(27) THEN
            P50XCmd = ""        'Use this to tell user abort condition
            EXIT FUNCTION
        END IF
    WEND
    r$ = INPUT$(1, #1)
    IF PrtChr THEN PRINT r$;
    p$ = p$ + r$
LOOP WHILE r$ <> Pmt

P50XCmd = p$

END FUNCTION

FUNCTION PrHex$ (n AS INTEGER)
IF n < 16 THEN
    PrHex = "0" + HEX$(n)
ELSE
    PrHex = HEX$(n)
END IF
END FUNCTION

SUB PrintBin (v AS INTEGER)
DIM m AS INTEGER
m = 128
FOR i = 1 TO 8
   IF v AND m THEN f = 1 ELSE f = 0
   PRINT RIGHT$(STR$(f), 1);
   m = m / 2
NEXT
END SUB

SUB PrintMAG (n AS INTEGER)
IF n = 0 THEN PRINT " red";  ELSE PRINT " green";
END SUB

SUB PTCkOff
p% = ASC(INPUT$(1, #1))
SELECT CASE p%
CASE 0
    PRINT "Ok (PT relay is off, i.e. PT output connected to red/brown output)"
CASE &H80
    PRINT "PT is busy"
CASE &HF1
    PRINT "Cannot turn off: PT in 'PT only' mode"
CASE ELSE
    PRINT "Unexpected error!"
END SELECT
END SUB

SUB PTCkTask (s AS STRING)
p% = ASC(INPUT$(1, #1))
SELECT CASE p%
CASE 0
    PRINT s
CASE &H80
    PRINT "PT is busy!"
CASE ELSE
    PRINT "Command rejected with error: "; HEX$(p%); "h"
END SELECT
END SUB

DEFINT A-Z
SUB PTEvent

PRINT "Programming Track event:"
PRINT #1, "x"; CHR$(XEvtPT);      'Ask for PT event

n = ASC(INPUT$(1, #1))
IF n = &HF5 THEN
    PRINT "<none>"

ELSEIF n < 1 OR n = 4 OR n = 5 OR n > 6 THEN
    PRINT "Unsupported '# of bytes' reply!"
    IF n > 0 THEN a$ = INPUT$(n, #1)
    STOP

ELSE
    a$ = INPUT$(n, #1)
    i = ASC(LEFT$(a$, 1))
    IF i <> 0 THEN
        SELECT CASE i
        CASE &HF7
            PRINT "XPT_DCCQD cmd: direct bit read is (probably) supported"
        CASE &HF6
            PRINT "XPT_DCCQD cmd: direct bit read is (probably) not supported"
        CASE ELSE
            PRINT "PT error #0"; PrHex$(i); "h"
        END SELECT
        
    ELSE
        PRINT "PT result: ";
        SELECT CASE n
        CASE 2      'single byte result
            PRINT ASC(MID$(a$, 2, 1))
       
        CASE 3      '16 bit result (low/high byte)
            PRINT ASC(MID$(a$, 2, 1)) + ASC(MID$(a$, 3, 1)) * 256
       
        CASE 6      'Selectrix read
            PRINT "Selectrix decoder read"
            PRINT "Addr: "; ASC(MID$(a$, 2, 1));
            PRINT "- Max speed: "; ASC(MID$(a$, 3, 1));
            PRINT "- Accel: "; ASC(MID$(a$, 4, 1));
            PRINT "- PWM: "; ASC(MID$(a$, 5, 1));
            PRINT "- Stops: "; ASC(MID$(a$, 6, 1));
       
        CASE ELSE
            PRINT "Unsupported '# of bytes' reply!"
       
        END SELECT
        PRINT
    END IF
END IF

END SUB

DEFSNG A-Z
SUB SensorEvent

PRINT "Sensor event(s):"
PRINT #1, "x"; CHR$(XEvtSen);
DO
    a$ = INPUT$(1, #1)
    n = ASC(a$)
    IF n <> 0 THEN
        PRINT "s88 #"; n; " ->  ";
        a$ = INPUT$(2, #1)
        PrintBin ASC(a$)
        PRINT " ";
        PrintBin ASC(RIGHT$(a$, 1))
        PRINT
    END IF
LOOP UNTIL n = 0

END SUB

SUB ShowHelp
PRINT
PRINT "During program execution you can press:"
PRINT "- 'q' or <Esc> to end this program"
PRINT "- '?' or 'h' to show this help lines"
PRINT "- 's' to read the first 8 s88 Modules explicitly (P50 cmd)"; ""
PRINT "- '0' to turn off the PT relais"
PRINT "- '1' to turn on the PT relais"
PRINT "- 'p' to enquire about PT status"
PRINT "- 'e' explicitly enquire about PT events"
PRINT "- 'r' DCC Read Register #1"
PRINT "- 'd' DCC Query Direct"
PRINT "- 'x' Selectrix Read"
PRINT "- 'b' DCC Read CV #1 in direct bit mode"
PRINT "- 'l' Read long DCC address (also sets bit #5 of CV 29!!)"
PRINT "- 't' to terminate a PT task still executing"
PRINT "- '.' Power Off"
PRINT "- '!' Power On"
PRINT "- '2' Set turnout #1 to 'green'"
PRINT "- '3' Set turnout #1 to 'red'"
PRINT
END SUB

SUB ShowSysSts (s AS INTEGER)

IF (s AND &H40) <> 0 THEN
    PRINT "Voltage: N (regulated)"
ELSE
    PRINT "Voltage: H0 (unregulated)"
END IF

IF (s AND &H20) <> 0 THEN
    PRINT "IB in slave mode (with external I2C CU)"
ELSE
    PRINT "IB in master mode (no external I2C CU)"
END IF

IF (s AND &H10) <> 0 THEN
    PRINT "Halt mode"
ELSE
    PRINT "Normal mode (not halted)"
END IF

IF (s AND &H8) <> 0 THEN
    PRINT "Power On (Go)"
ELSE
    PRINT "Power Off (Stop)"
END IF

IF (s AND &H4) <> 0 THEN
    PRINT "IB overheated!"
ELSE
    PRINT "IB not overheated"
END IF

IF (s AND &H2) <> 0 THEN
    PRINT "A 'Go' key is pressed (on an I2C device)"
ELSE
    PRINT "No 'Go' keys are pressed (on an I2C device)"
END IF

IF (s AND &H1) <> 0 THEN
    PRINT "A 'Stop' key is pressed (on an I2C device)"
ELSE
    PRINT "No 'Stop' keys are pressed (on an I2C device)"
END IF

END SUB

DEFINT A-Z
FUNCTION SOGet (n AS INTEGER)

PRINT #1, "X"; CHR$(&HA4); CHR$(n AND &HFF); CHR$(n \ 256);   'Ask for Special Option #n
IF ASC(INPUT$(1, 1)) <> 0 THEN
    SOGet = -1  'Error: flag to caller via an impossible SO value
ELSE
    SOGet = ASC(INPUT$(1, 1))
END IF

END FUNCTION

DEFSNG A-Z
SUB TurnoutEvent

PRINT "Turnout event(s):"
PRINT #1, "x"; CHR$(XEvtTrn);
n = ASC(INPUT$(1, #1))
FOR i = 1 TO n
    l% = ASC(INPUT$(1, #1))
    h% = ASC(INPUT$(1, #1))
    PRINT "Turnout #";
    PRINT USING "####"; l% + (h% AND &H7) * 256;
    PRINT ", Color = ";
    IF (h% AND &H80) = 0 THEN PRINT "red";  ELSE PRINT "green";
    PRINT ", Status = ";
    IF (h% AND &H40) = 0 THEN PRINT "off" ELSE PRINT "on"
NEXT i

END SUB

