DECLARE FUNCTION SOGet% (n AS INTEGER)
DECLARE SUB PTHelp ()
DECLARE SUB Delay (s AS SINGLE)
DECLARE FUNCTION IBCommInit! (s AS STRING)
DECLARE FUNCTION P50XCmd$ (a AS STRING)

'Intellibox P50Xa (P50X ASCII) commands utility

DIM SHARED P50 AS INTEGER       'True if P50 enabled
DIM SHARED P50x AS INTEGER      'True if P50X enabled

DIM SHARED c1$
DIM SHARED c2$
DIM SHARED Baud$
DIM SHARED ComPort AS INTEGER

DIM SHARED PrtChr AS INTEGER
PrtChr = -1

DIM SHARED Prompt AS INTEGER
Prompt = -1

CONST Pmt = "]"


CLS
PRINT "*** Intellibox"
PRINT "    P50Xa (P50X ASCII) command test utility (v 1.0 - Jan '98 - by St.Ch-B.)"
PRINT

c2$ = ",N,8,2,CS3000,tb16,rb2048"       'Somewhat application dependent
IF NOT (IBCommInit("19200")) THEN GOTO OpenErr   'If unable to communicate with the IB
OPEN c1$ + Baud$ + c2$ FOR RANDOM AS #1

PRINT
PRINT "Using "; Baud$; " bps"

PRINT
PRINT "*** Type a P50Xa command or:"
PRINT "- 'hp' for help on Programming Track commands;"
PRINT "- 'quit'<Return> to end this program;"
PRINT "- <Space><Return> to repeat the previous command;"
PRINT "- <Esc> to stop waiting for cmd answer."
PRINT
PRINT "N.B. The P50Xa 'PX' cmd is not supported by this program."
PRINT

ol$ = ""                'No previous command
PRINT Pmt;
Quit = 0
WHILE NOT Quit          'Main program loop
   
    LINE INPUT "", l$
    IF l$ = " " THEN
        l$ = ol$        'Repeat previous command
        LOCATE CSRLIN - 1, POS(0) + 1
        PRINT l$
    END IF
   
    IF LEN(l$) >= 62 THEN
        BEEP: PRINT
        PRINT "*** Command too long! (Max = 61 characters (including leading 'X'))": PRINT
        PRINT Pmt;
   
    ELSE
        ol$ = l$
        IF (UCASE$(l$) = "QUIT") THEN
            Quit = -1
       
        ELSEIF LEN(l$) > 1 AND UCASE$(LEFT$(l$, 1)) = "B" THEN
            a$ = P50XCmd(l$)
            IF a$ = "" THEN GOTO UserEsc
            IF NOT PrtChr THEN PRINT a$;
            IF a$ = "Baud rate changed!" + CHR$(13) + "]" THEN
                Baud$ = STR$(VAL(RIGHT$(l$, LEN(l$) - 1)))
                CLOSE #1
                OPEN c1$ + Baud$ + c2$ FOR RANDOM AS #1     'Open withnew baud rate
            END IF
       
        ELSE
            IF l$ = "@" OR l$ = "@@" THEN
                PRINT : PRINT "*** Intellibox reset, please wait ";
                Prompt = 0
                a$ = P50XCmd(l$)
                IF a$ = "" THEN GOTO UserEsc
                CLOSE #1
                FOR i = 1 TO 12
                    t = TIMER
                    WHILE t > TIMER - 1: WEND
                    PRINT ".";
                NEXT i
                IF NOT (IBCommInit(Baud$)) THEN GOTO OpenErr
                PRINT "]";
                Prompt = -1
                OPEN c1$ + Baud$ + c2$ FOR RANDOM AS #1
           
            ELSE
                IF UCASE$(l$) = "HP" THEN
                    PTHelp
                ELSE
                    a$ = P50XCmd(l$)
                    IF a$ = "" THEN GOTO UserEsc
                    IF NOT PrtChr THEN PRINT a$;
                    IF l$ = "ZzA1" OR l$ = "ZzA1" + CHR$(13) THEN
                        P50 = 0
                    ELSEIF l$ = "ZzA0" OR l$ = "ZzA0" + CHR$(13) THEN
                        P50 = -1
                    END IF
                END IF
            END IF
        END IF
    END IF
WEND

IF Baud$ <> "2400" THEN                'Reset to 2400 bps if not already so
    PrtChr = 0
    a$ = P50XCmd("b2400")
    CLOSE #1
    PRINT
    PRINT "Baud rate set to 2400 bps"
END IF

PRINT : PRINT "That's all, folks!": PRINT

SYSTEM
END

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
    ON ERROR GOTO 0         'Let QBasic show the error
END IF
BEEP
PRINT "Error: "; e
PRINT
SYSTEM
END

SUB Delay (s AS SINGLE)

t = TIMER
WHILE t + s > TIMER: WEND

END SUB

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

SUB PTHelp

PRINT "Programming Track commands:"
PRINT
PRINT "PT = show PT/relais status"
PRINT "PT1/0 = PT relais On/Off"
PRINT "PTSR = SX Read"
PRINT "PTSW = SX Write {Address, max Speed, Acceleration, PWM, # of stop zones}"
PRINT "PTFS = FMZ Search"
PRINT "PTFW = FMZ Write {Addr}"
PRINT "PTMS = Motorola Search"
PRINT "PTUA/B/C = Uhlenbrock 750/755/760 decoders {Old Address[, [New Address], ...]}"
PRINT "PTDS = DCC Search"
PRINT "PTRR/P/D = DCC Read Reg/Pag/Dir {Register/CV}"
PRINT "PTWR/P/D = DCC Write Reg/Pag/Dir {Register/CV, value}"
PRINT "PTRB = DCC Read CV in bit mode {CV}"
PRINT "PTWB = DCC Write CV bit {CV, Bit, 0|1}"
PRINT "PTQA = DCC Query Addr"
PRINT "PTQD = DCC Query Direct"
PRINT "PTRL = DCC Read Long addr"
PRINT "PTWL = DCC Write Long {Address}"
PRINT
PRINT "parms: use leading '$' for hex and '%' for binary numbers"
PRINT
PRINT "]";

END SUB

FUNCTION SOGet% (n AS INTEGER)

PRINT #1, "X"; CHR$(&HA4); CHR$(n AND &HFF); CHR$(n \ 256);   'Ask for Special Option #n
IF ASC(INPUT$(1, 1)) <> 0 THEN
    SOGet% = -1     'Error: flag to caller via an impossible SO value
ELSE
    SOGet% = ASC(INPUT$(1, 1))
END IF

END FUNCTION

