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
package jcs.commandStation.uhlenbrock.p50x;

/**
 * Intellibox: general purpose and events related P50Xb commands<br>
 * (document version 0.994 - 09/03) - by St.Ch-B. Uhlenbrock Elektronik GmbH
 *
 * (This document uses the word 'turnout' instead of 'accessory'.<br>
 * Therefore, a 'turnout' can also be a signal, etc... not only a 'real<br>
 * turnout'!)<br>
 * <p>
 * *** Introductory note: locomotive direction in the P50 and in the P50X<br>
 * <p>
 * This note is not required reading.<br>
 * In a nutshell it only says: the IB tries to keep track internally of<br>
 * the driving direction even of decoders (e.g. Old Motorola format ones)<br>
 * which do not support such a feature. The idea is to make the different<br>
 * digital formats as 'transparent' as possible to a computer application.<br>
 * Please skip to the next section ('address conflict') if you are not<br>
 * interested in knowing the details of how the driving direction is<br>
 * handled in the IB.<br>
 * <p>
 * Most Motorola-format locomotive decoders produced up to now do NOT<br>
 * support the notion of a "driving direction". In other words, the decoder<br>
 * does NOT "know" what the front side of the locomotive is. There is no way<br>
 * to tell it "go forward" or "go backwards". You can only tell the decoder<br>
 * to invert its driving direction.<br>
 * This fact is reflected in the P50, the protocol of a 6050 Interface:<br>
 * you can only tell a locomotive to change (invert) its driving direction,<br>
 * you CANNOT tell "her" to go in a specific direction.<br>
 * (A 6051 interface uses <strong>the same protocol</strong> as a 6050!)<br>
 * <p>
 * This holds true even if using a 6050 along with a 6021, i.e. even if<br>
 * the Central Unit used is capable (with dip-sw #2 of the 6021 placed in<br>
 * the "on" position) of <em>generating</em> the so-called "new motorola format"<br>
 * digital signal - a signal which, countrary to the original motorola<br>
 * format (known as the "old motorola format"), DOES include information<br>
 * specifying what the locomotive driving direction (usually referred to<br>
 * as the "absolute driving direction") should be.<br>
 * <p>
 * There is a (small) helping factor: if you do a power off which lasts<br>
 * only a few seconds (up to a day or so for newer decoders), then it is<br>
 * guaranteed that the previous driving direction has not been "forgotten"<br>
 * by the decoder.<br>
 * Conversely, after a longer lasting Power Off, the decoder, having<br>
 * forgotten its previous driving direction, defaults to a specific driving<br>
 * direction (determined by the way it has been connected to the locomotive<br>
 * engine: the green and the blue wire).<br>
 * <p>
 * One more factor to consider: some motorola format decoders (those based<br>
 * on the 70113 and 70117 chips) only accept a direction change command,<br>
 * provided that the speed they currently are at is NOT higher than speed 6<br>
 * (expressed in terms of the P50).<br>
 * (However: the 70117 chip, being able to understand the new motorola<br>
 * format, obviously always evaluates the "absolute driving direction"<br>
 * information of that protocol).<br>
 * <p>
 * N.B. By design choice, the IB does NOT take into account this "speed 6"<br>
 * issue. Therefore: a P50 "direction change" command (Speed = 15) is<br>
 * regarded as effective if the current speed of the locomotive was not<br>
 * already "speed" 15.<br>
 * (Please consider that older and newer decoders, along with Uhlenbrock<br>
 * decoders, react to a direction change command even if at speeds higher<br>
 * that P50 speed 6).<br>
 * <p>
 * Therefore, we have the following situation:<br>
 * <p>
 * - P50: no notion of an absolute driving direction.<br>
 * Only a "direction change" can be commanded.<br>
 * <p>
 * - P50X: a bit in the command for locomotives is reserved for specifying<br>
 * the driving direction. There is NO command for telling a Lok to<br>
 * "change" (invert) its driving direction (but this can be easily done<br>
 * by toggling the Dir bit of a locomotive, as shown from its status or<br>
 * as has been specified by the previous command issued to this locomotive).<br>
 * Though it cannot be 100% reliable, this direction bit is also effective<br>
 * with decoders which do not support an absolute driving direction.<br>
 * <p>
 * Furthermore, there is a compatibility issue: since P50 commands can be<br>
 * used in order to drive even non-Motorola format decoders (for locomotive<br>
 * addresses in range 0..255), then a peculiar (but perfectly normal)<br>
 * behaviour of Motorola format decoders must be symulated also for decoders<br>
 * whose digital format does support an absolute driving direction.<br>
 * The "problem" is: if you issue the P50 command for changing locomotive<br>
 * direction (this would be: Speed = 15), then the locomotive shall indeed<br>
 * change its driving direction ONLY IF the "speed" at which it currently is<br>
 * is NOT already "speed" 15 (and, check above note, for some decoders the<br>
 * current speed must be less or equal P50 speed 6).<br>
 * In other words: in order to positively change the speed of a locomotive,<br>
 * the following sequence of P50 commands should be sent:<br>
 * <p>
 * &lt;speed = 0, i.e. Stop&gt;<br>
 * &lt;"speed" 15&gt;<br>
 * <p>
 * In particular, in order to change twice the driving direction of a<br>
 * locomotive, it is MANDATORY to send at least the following sequence:<br>
 * <p>
 * &lt;"speed" 15&gt;<br>
 * &lt;non-"speed" 15 (e.g.: speed 0 = Stop)&gt;<br>
 * &lt;"speed" 15&gt;<br>
 * <p>
 * where it is assumed that, before sending this sequence, the locomotive<br>
 * speed was NOT already "speed" 15 (and was not greater than speed 6) - or<br>
 * else a non-"speed" 15 command (e.g. Stop) would have had to be sent before<br>
 * the above sequence, in order for the very first direction change command<br>
 * to be effective.<br>
 * <p>
 * Let's consider what the possible P50 cases are, but first let us define<br>
 * some terms:<br>
 * <p>
 * P50-CD	P50 command for direction change ("speed" = 15)<br>
 * N.B. The Speed of a Lok commanded at P50-CD would be shown<br>
 * as 1 by locomotive status query P50X commands.<br>
 * Mot-CD	direction change digital signal in the motorola format<br>
 * (this would be "speed" 1 - in the digital signal)<br>
 * Stop	speed = 0 = inertial stop<br>
 * Em. Stop non-inertial (fast) stop<br>
 * Dir	absolute driving direction<br>
 * CrS	current locomotive speed in terms of the P50 protocol<br>
 * .NE.	Not Equal<br>
 * .LE.	Less or Equal<br>
 * Slot	LocoNet slot (please check Digitrax LocoNet documentation)<br>
 * SO	Special Option<br>
 * LSO	Lok Special Option<br>
 * <p>
 * ###### P50<br>
 * <p>
 * a) DCC<br>
 * a P50-CD shall always result in an Em. Stop being sent to the locomotive.<br>
 * Furthermore, if CrS .NE. P50-CD, then the direction of the locomotive<br>
 * shall be inverted. Otherwise, it shall NOT be inverted.<br>
 * N.B. Mixing of P50 and P50X commands: a P50-CD is internally stored as<br>
 * a P50X Speed = 1 (Em. Stop). Therefore, issueing a P50-CD after<br>
 * having commanded the Lok, through a P50X cmd, in Em. Stop, shall NOT<br>
 * result in a direction change.<br>
 * <p>
 * b) FMZ<br>
 * a P50-CD shall always result in an Em. Stop being sent to the locomotive,<br>
 * along with Speed = Stop (Em. Stop is sent through a separate bit in the<br>
 * FMZ protocol).<br>
 * Furthermore, if CrS .NE. P50-CD, then the direction of the locomotive<br>
 * shall be inverted. Otherwise, it shall NOT be inverted.<br>
 * (Also check the "Mixing of P50/P50X commands" note at point "a" above).<br>
 * <p>
 * c) SX<br>
 * a P50-CD shall always result in a Stop being sent to the locomotive.<br>
 * Furthermore, if CrS .NE. P50-CD, then the direction of the locomotive<br>
 * shall be inverted. Otherwise, it shall NOT be inverted.<br>
 * (Also check the "Mixing of P50/P50X commands" note at point "a" above).<br>
 * <p>
 * d) Motorola Old<br>
 * all P50 commands are directly sent to the tracks.<br>
 * If CrS .NE. P50-CD, then the direction of the locomotive, as memorized<br>
 * in the IB, shall be inverted. Otherwise, it shall NOT be inverted.<br>
 * Furthermore, in this case a Stop is automatically sent before the Mot-CD.<br>
 * (Also check the "Mixing of P50/P50X commands" note at point "a" above).<br>
 * <p>
 * e) Motorola New<br>
 * all P50 commands are directly sent to the tracks.<br>
 * If CrS .NE. P50-CD, then the direction of the locomotive shall be inverted.<br>
 * Otherwise, it shall NOT be inverted.<br>
 * (Also check the "Mixing of P50/P50X commands" note at point "a" above).<br>
 * In case of a direction change, the following shall be sent:<br>
 * - if this particular Lok has been configured this way (using the Special<br>
 * Options for this very locomotive (LSO)), then an old format Mot-CD shall<br>
 * be sent. This would be the default configuration;<br>
 * - Mot-CD shall be sent (in the new Motorola format);<br>
 * <p>
 * ###### P50X (or LocoNet)<br>
 * <p>
 * Xa) DCC<br>
 * P50X commands directly map to DCC commands. There is no special case<br>
 * to consider.<br>
 * <p>
 * Xb) FMZ<br>
 * P50X commands directly map to FMZ commands. There is no special case<br>
 * to consider.<br>
 * <p>
 * Xc) SX<br>
 * P50X commands (almost) directly map to SX commands. The only case to<br>
 * consider is Em. Stop. In fact, since the SX protocol does not have<br>
 * this feature, an eventual Speed = 1 command (Em. Stop) shall be<br>
 * translated to a normal Stop (Speed = 0).<br>
 * <p>
 * Xd) Motorola Old<br>
 * Speed = 1 is converted, only as far as the digital signal is concerned,<br>
 * to Speed = 0 (Stop). This is done in order to avoid having a Speed = 1<br>
 * P50X cmd 'implicitly' change the driving direction: since we are in<br>
 * the P50X, the Dir bit should be used for this purpose!<br>
 * The P50X Speed would still be shown as 1 by a Lok status query P50X cmd.<br>
 * <p>
 * If a P50X command specifies a new (different from the current) Dir,<br>
 * then the following shall be automatically sent to the locomotive:<br>
 * - Stop;<br>
 * - Mot-CD;<br>
 * - current Speed.<br>
 * <p>
 * Xe) Motorola New<br>
 * Depending on each Mot New Lok configuration (Lok Special Option #6 for each<br>
 * Mot New Lok), a Speed = 1 P50X cmd may or may not be changed to Speed = 0.<br>
 * The default is to convert it - as usual: only as far as the digital<br>
 * signal is concerned - to Speed = 0 (LSO #6 = 0): this solves a problem with<br>
 * some programmable decoders (e.g. Uhlenbrock and XR1). In fact, these decoders<br>
 * enter programming mode if the Mot-CD signal is sent to them for more than<br>
 * X seconds (usually: X = 8..10 seconds).<br>
 * If a particular Lok is not equipped with such a decoder, then it may<br>
 * be configured so that the mentioned Lok Special Option #6 = 1. This implies<br>
 * that, since the decoder understands the new Motorola protocol, it shall<br>
 * NOT change direction when receiving the Mot-CD signal resulting from the<br>
 * Em. Stop P50X cmd. This results in an Em. Stop like behaviour.<br>
 * <p>
 * If a P50X command specifies a new (different from the current) Dir,<br>
 * then the following shall be automatically sent to the locomotive:<br>
 * - Stop;<br>
 * - if this particular Lok has been configured this way (using the Special<br>
 * Options for this very locomotive), then an old format Mot-CD shall be<br>
 * sent. This would be the default configuration;<br>
 * - finally, the IB shall send the Speed specified by the P50X command, along<br>
 * with the specified direction - obviously using the new Motorola format.<br>
 * <p>
 * The strategy for case "Xd" above has one side effect: using the P50X (or,<br>
 * equivalently, LocoNet) it is not possible to send to the tracks the<br>
 * Mot-CD command for Lok addresses configured for the Motorola Old protocol.<br>
 * Therefore, in the case of some programmable decoders (e.g. Uhlenbrock, XR1,<br>
 * etc...), it would not be possible to have the decoder enter programming mode.<br>
 * This can be easily solved from the PC side by using a P50 cmd.<br>
 * <p>
 * There is at least one more issue to consider: what happens if a locomotive<br>
 * equipped with a Motorola format decoder of the old type is operated<br>
 * through the IB using a locomotive address which has been configured<br>
 * for the new Motorola format (or vice versa)?<br>
 * Given the default IB configuration for Motorola New, decoders <strong>should</strong><br>
 * behave "correctly", i.e. change direction as expected. In fact, the<br>
 * default configuration asks for old protocol Mot-CD commands to be sent<br>
 * to the tracks before the new protocol ones.<br>
 * The exception is: if a P50X Speed = 1 command is sent to an Old Motorola<br>
 * format decoder being operated through an IB locomotive address configured<br>
 * for the New Motorola format (with LSO #6 = 1), then the locomotive would<br>
 * change driving direction, without the IB LCD (and the Lok status) showing<br>
 * it. This "mismatch" would not happen in case of P50 commands.<br>
 * <p>
 * Address conflicts involving the PC (RS-232 interface)<br>
 * -----------------------------------------------------<br>
 * <p>
 * This would mean: the PC is trying to send a command to a Lok already<br>
 * controlled by a non-PC device.<br>
 * The vice versa does not 'exist': since the P50X protocol defines Lok<br>
 * events, when the PC issues a cmd to a Lok, that Lok is NOT declared<br>
 * 'taken', i.e. a non-PC device would still have free access to that Lok.<br>
 * Through Lok events the PC would be informed about eventual usage of<br>
 * that Lok by other devices.<br>
 * Therefore, let's consider only the case of the PC trying to get control<br>
 * of a Lok already controlled by a different device.<br>
 * <p>
 * P50)<br>
 * In this case, Special Option #20 tells whether to discard a Lok Speed+FL<br>
 * command (SO #20 = 0) or to execute it even if the Lok is already<br>
 * controlled by a non-PC device (SO #20 = 1). The IB default for SO #20<br>
 * is 1, therefore the PC can always send commands to any Lok.<br>
 * Differently from P50 Speed+FL commands, P50 Functions commands are<br>
 * always executed (there is no SO to consider).<br>
 * A 6050 would behave this way for function cmds, but would discard any<br>
 * Lok (Speed + FL) cmds sent to a Lok which is already 'taken'.<br>
 * <p>
 * P50X)<br>
 * While functions commands are always executed (just like in the P50 case),<br>
 * Lok commands are executed only if the involved Lok is not already<br>
 * controlled by a non-PC device.<br>
 * However, the P50Xb XLok cmd provides a bit which allows the PC to<br>
 * override this default behaviour: if this 'Force' bit is set, then the cmd<br>
 * would be effective even in case of Loks already controlled by a<br>
 * non-PC device. There is no such 'Force' bit for the P50Xa 'L' cmd.<br>
 * <p>
 * P50Xa (P50X ASCII) locomotive and turnout control commands.<br>
 * -----------------------------------------------------------<br>
 * <p>
 * As usual, the syntax of P50Xa cmds is shown using these 'meta-symbols':<br>
 * []	tell an optional parameter or parameter plus separators, etc<br>
 * {}	tell a non optional part of the command, which may contain<br>
 * optional parts<br>
 * |	tells that two or more possibilities exists: those shown before<br>
 * and after this symbol<br>
 * <p>
 * *** L {Lok#, [Speed], [FL], [Dir], [F1], [F2], [F3], [F4]}<br>
 * Lok#	0..9999 (depending on protocol, not every address is legal!)<br>
 * Speed	0..127 (0 = Stop, 1 = Stop/Em.Stop (protocol dependent))<br>
 * FL	Light status (1 = on, 0 = off)<br>
 * Dir	Direction (1 = forward, 0 = reverse). Direction may also be<br>
 * specified as 'f' = forward or 'r' = reverse<br>
 * F1	F1 status (1 = on, 0 = off)<br>
 * etc...<br>
 * <p>
 * depending on the Lok protocol, not all parameters are internally used<br>
 * or have the same range.<br>
 * For example, the (real) address range of each protocol is:<br>
 * Mrk	1..80 (1..255 for Uhlenbrock decoders)<br>
 * SX	0..111<br>
 * DCC	1..9999 (not every DCC decoder supports addr. &gt; 99 or 127)<br>
 * FMZ	1..119<br>
 * however, virtual addresses can go from 0 to 9999.<br>
 * <p>
 * Depending on Lok protocol, Speed = 1 means (check also initial<br>
 * introductory note):<br>
 * Mrk Old	same as Stop<br>
 * Mrk New	Emergency Stop<br>
 * Selectrix	same as Stop<br>
 * DCC	Emergency Stop<br>
 * FMZ	Emergency Stop<br>
 * <p>
 * Another variable is the number of additional functions (apart from FL,<br>
 * i.e. usually the Lok lights - also known as F0):<br>
 * Mrk	4<br>
 * Selectrix	1<br>
 * DCC	4+ (F5.. to be specified using the P50Xa 'F' cmd)<br>
 * FMZ	1<br>
 * <p>
 * If only the first (mandatory) cmd parameter is specified (i.e. Lok#),<br>
 * then the current status of this Lok shall be displayed, provided, of<br>
 * course, that this Lok has ever been commanded before! (Or else the<br>
 * message 'No Lok data!' would be displayed.)<br>
 * <p>
 * The Speed parameter is <em>internally</em> scaled down in compliance with<br>
 * the Lok protocol. Min speed is always set by Speed = 2.<br>
 * Max Speed is always set by Speed = 127. The actual number of non-Stop<br>
 * speed steps varies depending on the Lok protocol:<br>
 * Mrk	14<br>
 * Selectrix	31<br>
 * DCC	14/27/28/126<br>
 * FMZ	15<br>
 * <p>
 * All parameters excluding the Lok# are optional. If at least one is<br>
 * specified, then Lok status is updated only as far as the specified<br>
 * parameter is concerned. Should no previous status be available, then<br>
 * the default is:<br>
 * Speed = 0<br>
 * Dir = 1 = forward<br>
 * FL (light) = Off<br>
 * F1..F4 = Off<br>
 * This only applies to the case in which at least one status parameter<br>
 * has been specified!<br>
 * In fact, as already written above, if no previous status is known and<br>
 * if the P50Xa 'L' cmd only specifies the Lok# parameter, then an error<br>
 * message is sent ('No Lok data').<br>
 * <p>
 * Address conflict with other devices:<br>
 * if the Lok specified by the 'L' cmd is already controlled by a non-PC<br>
 * device, then the error message 'Lok busy!' shall be displayed.<br>
 * <p>
 * Halt mode:<br>
 * If the Intellibox is in Halt mode, then Lok status (as specified by the<br>
 * 'L' cmd) is updated, but any Speed changes involving a non-Stop speed<br>
 * shall NOT be effective.<br>
 * In this case, the reply shall be: "Halted!".<br>
 * <p>
 * Power Off:<br>
 * If the Intellibox is in Power Off, then Lok status (as specified by the<br>
 * 'L' cmd) is updated. In this case, the reply shall be: "Pwr Off".<br>
 * <p>
 * *** LC {Lok#}<br>
 * Lok#	0..9999 (depending on protocol, not every address is legal!)<br>
 * Reports Lok protocol configuration (e.g. 'Mot Old', etc...) and<br>
 * number of non-Stop speed steps.<br>
 * N.B. There is NO cmd for setting the Lok configuration from the PC!<br>
 * Loks <strong>must</strong> be configured using the IB menus.<br>
 * <p>
 * *** F {Lok#, [F1], [F2], [F3], [F4], [F5], [F6], [F7], [F8]}<br>
 * Lok#	0..9999 (depending on protocol, not every address is legal!)<br>
 * F1	F1 status (1 = on, 0 = off)<br>
 * etc...<br>
 * <p>
 * If only the first (mandatory) cmd parameter is specified (i.e. Lok#),<br>
 * then the current status of this Lok shall be displayed - as far as<br>
 * functions are concerned.<br>
 * <p>
 * The 'F' command is effective even if the specified Lok is already<br>
 * controlled by a non-PC device (there is no address conflict).<br>
 * <p>
 * *** T {Trnt#, [Color], [Status]}<br>
 * Trnt#	1..2040 (depending on protocol, not every address is legal!)<br>
 * Please note: turnout address, NOT turnout <em>decoder</em> address!<br>
 * Legal Motorola format turnout address range: 1..320<br>
 * Legal DCC format turnout address range: 1..2040<br>
 * Color	'r' (red = thrown) or 'g' (green = closed)<br>
 * ('r' may also be spec'd as '0', 'g' also as '1')<br>
 * Status	1 = on, 0 = off (if not specified -&gt; off)<br>
 * <p>
 * If only the first (mandatory) cmd parameter is specified (i.e. Turnout#),<br>
 * then the current status of this Turnout shall be displayed.<br>
 * <p>
 * In addition to the usual 'Syntax Error' reply, this cmd can also<br>
 * reply with:<br>
 * 'Turnout fifo is full!'<br>
 * 'Power is Off!'<br>
 * 'Illegal Turnout address for this protocol!'<br>
 * <p>
 * *** TR {Trnt#, [Res]}<br>
 * Trnt#	1..2040 (depending on protocol, not every address is legal!)<br>
 * Please note: turnout address, NOT turnout <em>decoder</em> address!<br>
 * Res	A flag specifying whether this turnout should be reserved (1)<br>
 * or not (0) for exclusive PC control. If Res = 1, then non-PC<br>
 * turnout commands shall not be executed by the IB - and a<br>
 * corresponding event shall be generated (check XEvent cmd).<br>
 * If only the first (mandatory) cmd parameter is specified (i.e. Turnout#),<br>
 * then the current 'reserved' status of this Turnout shall be displayed.<br>
 * <p>
 * ######## Possible future cmd<br>
 * <p>
 * *** LM {...}<br>
 * Setup, delete and report multi-tractions (consists).<br>
 * <p>
 * *** FX {Lok#, [F9], [F10], [F11], [F12], ...?}<br>
 * Lok#	0..9999 (depending on protocol, not every address is legal!)<br>
 * F9	F9 status (1 = on, 0 = off)<br>
 * etc...<br>
 * <p>
 */
public interface P50x {

  /**
   * Reply code: command OK.
   */
  public final static int X_OK = 0x00;

  /**
   * Reply code: system error.
   */
  public final static int X_SYERR = 0x01;

  /**
   * Reply code: bad parameter.
   */
  public final static int X_BADPRM = 0x02;

  /**
   * Reply code: bad Special Option value.
   */
  public final static int X_BADSOV = 0x0F;

  // -------------------------------------------------------------------------
  // P50 commands issued through P50Xb commands
  //
  // These two commands have been introduced in order to allow issuing P50
  // commands after the 'ZzA1' P50Xa cmd has been sent (which drops the
  // typical leading "x" character of P50X commands). Once 'ZzA1' has been
  // issued, no P50 command is *directly* executable, as the first received
  // byte would always be interpreted as the first byte of a P50X command.
  // -------------------------------------------------------------------------
  /**
   * XP50Len1 (0C6h) - length = 1+1 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: same as a one byte long P50 cmd (e.g. s88, Power On/Off, and also turnout 'off').
   * <p>
   * N.B. There is NO reply!
   */
  public final static int X_P50LEN1 = 0xC6;

  /**
   * XP50Len2 (0C7h) - length = 1+2 bytes
   * <p>
   * Parameters (byte):
   * <br>1st &amp; 2nd: same as a two byte long P50 cmd (e.g. Lok control, function control and turnout control (red on/green on)).
   * <p>
   * N.B. There is NO reply!
   */
  public final static int X_P50LEN2 = 0xC7;

  /**
   * XVer (0A0h) - length = 1 byte
   * <p>
   * Reply: iterated format consisting of one 1st byte telling the amount of bytes which shall follow. If this 1st byte is zero, then this is the last byte of the XVer reply.
   * <p>
   * For example, the IB replies with:
   * <pre>
   * 02h, &lt;SPU version low&gt;, &lt;SPU version high&gt;,
   * 02h, &lt;KPU version low&gt;, &lt;KPU version high&gt;,
   * 01h, &lt;PPU version&gt;,
   * 01h, &lt;LIPU version&gt;,
   * 01h, &lt;DNG version&gt;,
   * 05h, &lt;IB serial number: 5 bytes (digits 98, 76, 54, 32, 10)&gt;,
   * 00h
   * </pre> A single byte version number is to be interpreted as: H.L<br>
   * For example: 10h -&gt; version 1.0<br>
   * A two byte version number (low/high) is to be interpreted as: H.HLL<br>
   * For example: 23h, 10h -&gt; version 1.023<br>
   * (The version numbers and the serial number are sent in BCD notation - Binary Coded Decimal.)
   * <p>
   * The serial number is to be interpreted as: '9876543210' — digit '9' is the most significant digit, etc.
   * <p>
   * SPU = System Processing Unit (the IB 'heart')<br>
   * KPU = Keypad Processing Unit (user interface)<br>
   * PPU = Peripheral Processing Unit (digital signal generator)<br>
   * LIPU = Lokmaus/I2C Processing Unit<br>
   * DNG = Dispositivo di Nostra Gestione (Italian)
   */
  public final static int X_VER = 0xA0;

  /**
   * XSOSet (0A3h) - length = 1+3 bytes
   * <p>
   * N.B. NOT IMPLEMENTED IN THE INTELLIBOX! (Always replies with XSYERR — starting from SPU v1.002.)
   * <p>
   * Parameters (byte):
   * <br>1st: low byte of SO (Special Option) number
   * <br>2nd: high byte of SO number
   * <br>3rd: new SO value
   * <br>Legal SO number is 0..999. Depending on the specific SO, the 3rd byte must lay inside a given range (i.e., for each SO there is a minimum and a maximum value). Special Options are user-only
   * configurable parameters which control many aspects of the Intellibox. Please check SO_DOC.TXT for a list of the main SOs and their meaning.
   * <p>
   * Reply: either 00h (cmd Ok) or error code:
   * <br>XSYERR (01h): cmd not implemented
   * <br>XBADPRM (02h): bad SO number (bad parameter value)
   * <br>XBADSOV (0Fh): bad SO value
   * <p>
   * N.B. By design choice there is NO command for writing (changing) the value of an SO in the Intellibox. This can only be done manually using the IB menus. The XSOSet cmd has only been defined in
   * view of possible future devices (e.g. CompuBox) and/or software implementations.
   */
  public final static int X_SOSET = 0xA3;

  /**
   * XSOGet (0A4h) - length = 1+2 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: low byte of SO (Special Option) number
   * <br>2nd: high byte of SO number
   * <br>Legal SO number is 0..999. Special Options are user-only configurable parameters which control many aspects of the Intellibox. Please check SO_DOC.TXT for a list of the main SOs and their
   * meaning.
   * <p>
   * Reply: either 00h (cmd Ok, one more byte shall follow) or error code (XBADPRM). The eventual 2nd byte holds the SO value.
   */
  public final static int X_SOGET = 0xA4;

  /**
   * XPwrOff (0A6h) - length = 1 byte
   * <p>
   * Reply:
   * <br>1st: 00h (cmd Ok)
   */
  public final static int X_PWR_OFF = 0xA6;

  /**
   * XPwrOn (0A7h) - length = 1 byte
   * <p>
   * Reply:
   * <br>1st: 00h (cmd Ok) or error code XPWOFF (06h): the Power is Off!
   * <br>XPWOFF is reported if an error condition (e.g. 'overheating') prevents the Intellibox from turning on the power to the layout.
   */
  public final static int X_PWR_ON = 0xA7;

  /**
   * XHalt (0A5h) - length = 1 byte
   * <p>
   * Reply:
   * <br>1st: 00h (cmd Ok) or error code.
   * <p>
   * While in halt mode, all Loks are stopped. The light and the driving direction are still controllable. Turnouts can be controlled. Halt mode is terminated by a Power On command (manual or PC).
   */
  public final static int X_HALT = 0xA5;

  /**
   * XP50XCh (0A1h) - length = 1+1 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: new P50X lead-in char (range: 0 or 50h..5Fh).
   * <br>If 00h is specified (as parameter value), then the P50X protocol shall be disabled. Re-enable is possible only by PC break or Intellibox reset. The latter would only work provided that the
   * Intellibox has been configured that way using its menus (i.e. configured for 'P50X enabled').
   * <p>
   * Reply:
   * <br>1st: 00h (cmd Ok) or error code.
   */
  public final static int X_P50XCH = 0xA1;

  /**
   * XStatus (0A2h) - length = 1 byte
   * <p>
   * Reply:
   * <br>1st: current system status:
   * <pre>
   *  bit#   7     6     5     4     3     2     1     0
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   *      |Sts2 |VReg |ExtCU|Halt | Pwr | Hot | Go  |Stop |
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   * </pre> where:
   * <br>Sts2: currently always reported as 0. If set, then one more byte is to be expected as part of the XStatus reply (this would be an eventual expansion for a future software release, similar to
   * what happens with the XEvent cmd).
   * <br>VReg: set if voltage regulation (N scale) is enabled.
   * <br>ExtCU: set if an external I2C Central Unit is present.
   * <br>Halt: set if in Halt mode (Lok's stopped, Power On).
   * <br>Pwr: set if we are in Power On.
   * <br>Hot: overheating condition detected.
   * <br>Go: set if a [Go] key on an external I2C device is currently pressed.
   * <br>Stop: set if a [Stop] key on an external I2C device is currently pressed.
   */
  public final static int X_STATUS = 0xA2;

  /**
   * XNOP (0C4h) - length = 1 byte
   * <p>
   * This cmd can be used in order to automatically identify the currently selected IB baud rate and protocol. This cmd only replies with the typical P50Xb 'Ok' (00h) answer. This, along with the fact
   * that the 0C4h P50 cmd replies with two bytes (s88 data from the 4th s88 module), enables automatic baud rate and protocol identification.
   * <p>
   * A second possible usage: checking that the communication channel with the IB still works.
   * <p>
   * N.B. The IB discards any byte which has been received with 'wrong' stop bits. This fact is 'used' as part of the automatic baud rate identification algorithm.
   */
  public final static int X_NOP = 0xC4;

  /**
   * XSensor (098h) - length = 1+1 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: s88 module number (1..128). Module numbers 1..31 may correspond to real s88 modules connected to the IB. Module numbers starting from 32 only correspond to LocoNet sensors.
   * <p>
   * Reply:
   * <br>1st: 00h (cmd Ok, 2 bytes shall follow) or error code (XBADPRM). Only in case of no error are more bytes sent:
   * <br>2nd: contacts #1..8 (bits #7..0)
   * <br>3rd: contacts #9..16 (bits #7..0)
   * <p>
   * N.B. If less than 31 'real' s88 modules are used, then the corresponding addresses can be used for LocoNet sensors. (Check also XEvtSen cmd.) This would be the mapping of LocoNet sensors to s88
   * modules:
   * <pre>
   * LocoNet sensor #0    s88 module #1, contact #1
   * LocoNet sensor #1    s88 module #1, contact #2
   * ...
   * LocoNet sensor #15   s88 module #1, contact #16
   * LocoNet sensor #16   s88 module #2, contact #1
   * ...
   * LocoNet sensor #2047 s88 module #128, contact #16
   * </pre> The current version of the IB software "only" supports up to 2048 LocoNet sensors (0..2047). Eventual LocoNet messages related to higher sensor addresses are currently discarded (ignored).
   * <p>
   * N.B. In agreement with the Digitrax LocoNet documentation, bit 'I' of byte 'IN2' of the OPC_INPUT_REP LocoNet message is regarded as the least significant bit of the LocoNet sensor address.
   * <p>
   * N.B. The data read by the XSensor cmd shows the *current* status, not the accumulated OR status (as would be the case for P50 s88 commands), for the s88 module or LocoNet sensor being read.
   * Reading an s88 module with the XSensor cmd removes any eventually pending sensor event for that module.
   */
  public final static int X_SENSOR = 0x98;

  /**
   * XSensOff (099h) - length = 1 byte
   * <p>
   * Reply:
   * <br>1st: always 00h.
   * <p>
   * This cmd tells the IB to report as new 'sensor events' the status of all sensors which are not 'off'. It can be useful, e.g., at the start of a PC program, in order to read — using events — the
   * current status of all sensors. Sensors for which no events are reported (after an XSensOff cmd) can be assumed to be in the 'off' condition.
   */
  public final static int X_SENS_OFF = 0x99;

  /**
   * X88PGet (09Ch) - length = 1+1 bytes
   * <p>
   * Parameter (byte):
   * <br>1st: s88 parameter number.
   * <p>
   * Allows reading the current value of an s88 related parameter. Currently, these are the accessible parameters:
   * <br>00h: number of automatically read s88 'half modules' (check also P50Xa 'SE' cmd).
   * <br>01h: s88 module 'half number' which is source for the s88 timers.
   * <br>02h: s88 module 'half number' which is source for the s88 counters.
   * <p>
   * Reply: either 00h (cmd Ok, one byte shall follow) or error code. The eventual 2nd byte holds the s88 parameter value.
   */
  public final static int X_88P_GET = 0x9c;

  /**
   * X88PSet (09Dh) - length = 1+2 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: s88 parameter number (check X88PGet for details).
   * <br>2nd: s88 parameter value.
   * <p>
   * Allows setting the current value of an s88 related parameter.
   * <p>
   * N.B. s88 parameters shall only be modified up to the next IB reset. Upon an IB reset, all s88 parameters are reset to the values specified by the user (per IB menus) and stored in the
   * corresponding Special Option.
   * <p>
   * Reply: either 00h (cmd Ok) or error code.
   */
  public final static int X_88P_SET = 0x9d;

  /**
   * Xs88Tim (09Eh) - length = 1+1 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: Bit #0..3: s88 Timer number (1..8). Bit #7: set if Timer to be reset after reading (i.e. after this cmd). (Bit #4..6 are reserved: must always be 0.)
   * <p>
   * Reply:
   * <br>1st: 00h (cmd Ok, 2 bytes shall follow) or error code. Only in case of no error are more bytes sent:
   * <br>2nd: s88 Timer value (low byte).
   * <br>3rd: s88 Timer value (high byte).
   * <p>
   * The timer value is to be interpreted in 200 ms units. Provided the corresponding s88 input is 'on' (closed), each s88 Timer is incremented (Timer = Timer + 1) approximately every 200 ms. Upon
   * eventually reaching the maximum value (0FFFFh), there is no 'wrap around': the timer stays at this maximum value.
   */
  public final static int X_S88_TIM = 0x9e;

  /**
   * Xs88Cnt (09Fh) - length = 1+1 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: Bit #0..3: s88 Counter number (1..8). Bit #7: set if Counter to be reset after reading (i.e. after this cmd). (Bit #4..6 are reserved: must always be 0.)
   * <p>
   * Reply:
   * <br>1st: 00h (cmd Ok, 2 bytes shall follow) or error code. Only in case of no error are more bytes sent:
   * <br>2nd: s88 Counter value (low byte).
   * <br>3rd: s88 Counter value (high byte).
   * <p>
   * The time resolution of the Counters depends on the number of s88 modules being automatically read by the IB: the lower this number, the higher the resolution of s88 counters (i.e. they are able
   * to keep track also of very fast on/off cycles). A reasonable estimate, while automatically reading the first 8 s88 modules, would be about 50 ms resolution for the Counters — or better. Upon
   * eventually reaching the maximum value (0FFFFh), there is no 'wrap around': the counter stays at this maximum value.
   */
  public final static int X_S88_CNT = 0x9f;

  // -------------------------------------------------------------------------
  // P50Xb events related commands
  // -------------------------------------------------------------------------
  /**
   * XEvent (0C8h) - length = 1 byte
   * <p>
   * Reply: the length of the reply varies from one to three bytes, determined from bit #7 of bytes #1 and #2. If bit #7 is set, then one more byte shall be sent as part of the reply. Bit #7 of the
   * 3rd byte is currently always reported as 0 (reserved for future extensions). In case of 'no event to report', the reply consists of a single byte: 00h.
   * <p>
   * Pseudo-code algorithm:
   * <pre>
   * if (bit #7 of 1st byte of reply to XEvent cmd is 1) then
   *     receive also 2nd byte
   *     if (bit #7 of 2nd byte is 1) then
   *         receive also 3rd byte
   *     else
   *         consider 3rd byte as if it were 00h
   *     endif
   * else
   *     consider 2nd and 3rd bytes as if both were 00h
   * endif
   * </pre> N.B. Only *after* having eventually received also the 2nd and the 3rd byte should one proceed and process the 'normal' (i.e. not bit #7) event flags of the XEvent reply. This prevents
   * mixing up a reply to a further cmd with the 2nd and 3rd byte.
   * <p>
   * 1st byte — event flags:
   * <pre>
   *  bit#   7     6     5     4     3     2     1     0
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   *      | Ev2 |  x  |Trnt |TRes |PwOff| Sen | IR  | Lok |
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   * </pre> where:
   * <br>Ev2: set if also the 2nd byte of the XEvent reply shall be sent.
   * <br>x: reserved for future use.
   * <br>Trnt: there has been at least one non-PC Turnout cmd.
   * <br>TRes: there has been at least one non-PC attempt at changing the status of a 'reserved' Turnout.
   * <br>PwOff: there *has been* (not: is!) a Power Off.
   * <br>Sen: there has been at least one sensor event (s88 or LocoNet).
   * <br>IR: there has been at least one infra-red event.
   * <br>Lok: there has been at least one non-PC Lok cmd.
   * <p>
   * 2nd byte — event flags:
   * <pre>
   *  bit#   7     6     5     4     3     2     1     0
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   *      | Ev3 | Sts | Hot |PTSh |RSSh |IntSh|LMSh |ExtSh|
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   * </pre> where:
   * <br>Ev3: set if also the 3rd byte of the XEvent reply shall be sent.
   * <br>Sts: an XStatus cmd should be issued.
   * <br>Hot: overheating condition detected.
   * <br>PTSh: while the PT relay was on (e.g., PT in 'PT only' mode), a non-allowed electrical connection between the Programming Track and the rest of the layout has been detected.
   * <br>RSSh: overload (short) on the DCC Booster C/D lines or on the LocoNet (B connector) Rail Sync +/- lines (or on the PT, if the PT relay was on).
   * <br>IntSh: short reported by the internal Booster.
   * <br>LMSh: overload (short) on the Lokmaus bus.
   * <br>ExtSh: short reported by an external Booster.
   * <p>
   * 3rd byte — event flags:
   * <pre>
   *  bit#   7     6     5     4     3     2     1     0
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   *      | Ev4 |  x  |  x  |ExVlt|TkRel| Mem |RSOF |  PT |
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   * </pre> where:
   * <br>Ev4: not currently used, reported as 0.
   * <br>x: not currently used.
   * <br>ExVlt: an external voltage source is present (prior to turning on the layout), e.g. an external transformer is in contact with the rails.
   * <br>TkRel: report Lok 'take' and 'release' events from non-PC Lok controllers (to be documented).
   * <br>Mem: set if there has been at least one 'memory' event (to be documented — related to the future IB 'memory' software expansion).
   * <br>RSOF: set if an RS-232 rx overflow has been detected (the PC probably does not correctly handle the CTS line).
   * <br>PT: a PT event is available. N.B. This bit must be 'cleared' by sending the XPT_Event cmd!
   * <p>
   * Possible future events (and eventual associated commands): events from s88 Counters and/or Timers; events from 6043 (Mrk Memory).
   * <p>
   * Note: though it is possible to only issue XEvtSen, XEvtLok, etc. cmds while not issuing any XEvent cmd, since XEvent also reports error events (e.g. Power off or Short), it would be best to use
   * XEvent and, depending on its reply, eventually issue XEvtSen, XEvtLok, etc.
   */
  public final static int X_EVENT = 0xc8;

  /**
   * XEvtSen (0CBh) - length = 1 byte
   * <p>
   * Report eventual sensor events (s88 modules and/or LocoNet sensors).
   * <p>
   * Reply: iterated format. The 1st byte tells:
   * <br>1st byte = 00h: 'no further sensor event to report'.
   * <br>1st byte &gt; 00h: s88 module number whose status is reported with the next 2 bytes.
   * <br>The 1st byte holds the current status of this s88 module inputs #1..8 (bit #7..0). The 2nd byte holds the current status of inputs #9..16 (bit #7..0).
   * <p>
   * The s88 module number is 'real' for modules 1..31 (31 is the maximum number of physical s88 modules which can be connected to the IB). For s88 module numbers &gt;= 32, a 'virtual' module is
   * assumed: the reported data really relates to LocoNet sensors starting from sensor #497. If only 'Events style' reading of sensor data is performed (i.e. if only XEvtSen is used for reading sensor
   * status), then physical s88 modules and LocoNet sensors can coexist in the same address range.
   * <p>
   * N.B. The current version of the IB software does not process nor store any OPC_SW_REP LocoNet messages (turnout feedbacks). On the contrary, OPC_INPUT_REP messages are internally stored and
   * processed (these are the sensor messages of LocoNet).
   * <p>
   * N.B. It is possible that the XEvent cmd reports a sensor event, but that a subsequent XEvtSen does not report any sensor event. This happens if the sensor status has changed back to its previous
   * ('on') status in the time elapsed between the XEvent and the XEvtSen cmds. In other words: it is possible to miss a super-fast 'off' status for a sensor which is normally 'on'. On the contrary, a
   * no matter how brief 'on' status would always be reported at least once.
   * <p>
   * It is guaranteed that an answer to an XEvtSen event does NOT include the SAME s88 module twice. The status of up to 128 s88 modules may be reported by the XEvtSen cmd.
   * <p>
   * N.B. NO SENSOR EVENT IS REPORTED, as far as physical s88 modules are concerned, if the automatic reading (check P50Xa 'SE' cmd in P50XAGEN.TXT) has been either disabled ('SE 0') or set too low.
   * In the latter case, no event would be reported for s88 modules laying "outside" the automatic read range. While the user can set per IB menus the amount of s88 modules connected to the IB, it
   * would be best to check this setting by PC and/or let the user configure this also per PC — thus eventually sending an appropriate 'SE xx' cmd. The 'SE' cmd obviously does not affect LocoNet
   * sensors.
   */
  public final static int X_EVT_SEN = 0xCB;

  /**
   * XEvtLok (0C9h) - length = 1 byte
   * <p>
   * Report locomotives whose status may have changed due to non-PC commands. Up to 119 locomotives may be reported by XEvtLok.
   * <p>
   * Reply: iterated format. The 1st byte tells:
   * <br>1st byte = 80h: 'no further Lok event to report'.
   * <br>1st byte &lt; 80h: Lok status in this (Speed) and next 4 bytes.
   * <br>1st byte &gt; 80h: reserved for future use.
   * <p>
   * Complete Lok status is given by:
   * <br>1st: Speed: 0..127 (0 = Stop, 1 = Stop/Em.Stop)
   * <br>2nd: F1..F8 (bit #0..7)
   * <br>3rd: low byte of Lok# (A7..A0)
   * <br>4th: high byte of Lok#, plus Dir and Light status:
   * <pre>
   *  bit#   7     6     5     4     3     2     1     0
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   *      | Dir |  FL | A13 | A12 | A11 | A10 | A9  | A8  |
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   * </pre> where:
   * <br>Dir: Lok direction (1 = forward).
   * <br>FL: Light status.
   * <br>A13..8: high bits of Lok#.
   * <br>5th: 'real' Lok speed (in terms of the Lok type/configuration) (please check XLokSts in P50X_LT.TXT for doc on 'real' speed).
   * <p>
   * The maximum number of events which can be reported by one XEvtLok cmd is 119: the amount of LocoNet 'normal' slots supported by the IB. ('Special' slots being those used for the Programming Track
   * and for the FAST clock feature (not supported in the current IB software version).)
   */
  public final static int X_EVT_LOK = 0xc9;

  /**
   * XEvtTrn (0CAh) - length = 1 byte
   * <p>
   * Report eventual turnout events (turnout commands <b>not</b> from the PC).
   * <p>
   * Reply: a first byte which tells:
   * <br>1st byte = 00h: 'no turnout event to report'.
   * <br>1st byte &gt; 00h: this many turnout events are going to be reported.
   * <p>
   * For each reported turnout event, two bytes are sent. The format is the same used with the XTrnt P50Xb cmd for <b>sending</b> a turnout cmd to the IB, with the exception that bit #5..3 are always
   * reported as 0 (reserved for future use). Up to 64 turnout events may be reported by one XEvtTrn cmd (the size of the turnout events buffer in the IB). Should more such events occur before an
   * XEvtTrn cmd is issued, then newer events shall overwrite older ones.
   */
  public final static int X_EVT_TRN = 0xca;

  /**
   * XEvtIR (0CCh) - length = 1 byte
   * <p>
   * Report eventual events due to Infra-Red (remote controller) commands.
   * <p>
   * Reply: a first byte which tells:
   * <br>1st byte = 00h: 'no IR event to report'.
   * <br>1st byte &gt; 00h: this many IR events (not bytes!) are going to be reported. For each reported IR event, two bytes are sent.
   * <p>
   * Format of the two bytes:
   * <br>1st byte:
   * <pre>
   *  bit#   7     6     5     4     3     2     1     0
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   *      |  1  |  0  |  T  | Ch4 | Ch3 | Ch2 | Ch1 | Ch0 |
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   * </pre> where:
   * <br>Ch0..4: channel number (IRIS: 24, 25, 26, 27).
   * <br>T: this bit toggles at each new keypress, and can be used to distinguish between a new cmd and the repetition of a previous cmd.
   * <br>0: this bit always has the value 0.
   * <br>1: this bit always has the value 1.
   * <p>
   * 2nd byte:
   * <pre>
   *  bit#   7     6     5     4     3     2     1     0
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   *      |  0  | PC  | K5  | K4  | K3  | K2  |  K1 | K0  |
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   * </pre> where:
   * <br>K5..0: Cmd (key) code.
   * <br>PC: this bit is set for IR events resulting from the usage of IR_PCx or IR_PCLNx IRIS tokens.
   * <br>0: this bit always has the value 0.
   * <p>
   * In this context, a 'token' is a command/action which has been assigned to a particular IRIS key. Tokens include: digits (e.g., during loco, turnout, route selection), loco/trnt/route selection
   * start, speed control, direction control, function control, turnout control, route activation, send an IR event to the PC, send a LocoNet msg. Tokens are assigned using IB SOs (documented in a
   * separate file).
   * <p>
   * With regard to IR events, SO #768 is used to tell:
   * <br>bit #0: set if the IB is to report to the PC any received IR command
   * <em>not</em> coming from an IRIS channel.
   * <br>bit #1: set if the IB is to report to the PC any received IR command which translated to an IR_PCx or IR_PCLNx token.
   * <br>bit #2: set if the IB is to report to the PC any received IR command coming from an IRIS channel.
   * <br>bit #3: set if the IB is to report on LocoNet any received IR command which translated to an IR_LNx or IR_PCLNx token.
   * <p>
   * If both bits #1 and #2 are set and an IR command is received which translates to an IR_PCx or IR_PCLNx token, then 2 IR events shall be reported by the IB.
   * <p>
   * LocoNet messages resulting from IR_LNx or IR_PCLNx tokens have this format (hex):
   * <pre>
   * E5 07 00 00 00 5x Cks  (for new keypresses)
   * E5 07 00 00 00 6x Cks  (for repetitions)
   * </pre> (where x equals the x in the IR_LNx or IR_PCLNx token). These messages are only sent if SO #768.3 has the value 1.
   */
  public final static int X_EVT_IR = 0xcc;

  /**
   * XEvtPT (0CEh) - length = 1 byte
   * <p>
   * Report Programming Track events — please check the P50X_PT.TXT document.
   */
  public final static int X_EVT_PT = 0xce;

  /**
   * XEvtTkR (0CFh) - length = 1 byte
   * <p>
   * Report Lok 'take' and 'release' events.
   */
  public final static int X_EVT_TKR = 0xcf;

  /**
   * XEvtMem (0D0h) - length = 1 byte
   * <p>
   * Report 'Memory' events.
   */
  public final static int X_EVT_MEM = 0xd0;

  //------------------------------------------------------------
  //
  // P50Xb (P50X binary) locomotive & turnout control commands.
  //
  //------------------------------------------------------------
  /**
   * XLok (080h) - length = 1+4 bytes
   * <p>
   * Parameters (byte) — please check also P50Xa 'L' cmd:
   * <br>1st: low byte of Lok address.
   * <br>2nd: high byte of Lok address.
   * <br>3rd: speed (0..127: 0 = Stop, 1 = Stop/Em.Stop). N.B. bit #7 is reserved for future use!
   * <br>4th: this byte has the following format:
   * <pre>
   *  bit#   7     6     5     4     3     2     1     0
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   *      |ChgF |Force| Dir | FL  | F4  | F3  | F2  | F1  |
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   * </pre> where:
   * <br>ChgF: set if F1..F4 to be used for setting F1..F4 of Lok (otherwise F1..F4 are ignored).
   * <br>Force: if set (1), then the XLok command is 'forced' even in case of a Lok already controlled by a non-PC device.
   * <br>Dir: Lok direction: 1 = forward, 0 = reverse.
   * <br>FL: Lok light status: 1 = on, 0 = off.
   * <br>F4..F1: Lok F4..F1 status (if ChgF is set).
   * <p>
   * N.B. Address must be in range 0..9999 (depending on protocol, not every address is legal!)
   * <p>
   * Reply:
   * <br>1st: either 00h (cmd Ok) or error code.
   * <p>
   * Error/warning codes:
   * <br>XBADPRM (02h): illegal parameter value.
   * <br>XNOLSPC (08h): there is no space in the Lok cmd buffer, please try later!
   * <br>XNOSLOT (0Bh): there is no slot available.
   * <br>XBADLNP (0Ch): Lok# is illegal for this protocol.
   * <br>XLKBUSY (0Dh): Lok already controlled by another device.
   * <br>XLKHALT (41h): Command accepted (Lok status updated), but IB in 'Halt' mode!
   * <br>XLkPOFF (42h): Command accepted (Lok status updated), but IB in Power Off!
   */
  public final static int X_LOK = 0x80;

  /**
   * XLokSts (084h) - length = 1+2 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: low byte of Lok address.
   * <br>2nd: high byte of Lok address.
   * <p>
   * Reply:
   * <br>1st: either 00h (cmd Ok, 3 bytes shall follow) or error code.
   * <br>2nd: Speed (0..127: 0 = Stop, 1 = Stop/Em.Stop).
   * <br>3rd: same format as 4th parameter of XLok cmd (bit #6 &amp; 7 = 0).
   * <br>4th: 'real' Lok speed (in terms of the Lok type/configuration).
   * <p>
   * Error codes:
   * <br>XBADPRM (02h): illegal parameter value.
   * <br>XNODATA (0Ah): no Lok status available (Lok is not in a slot).
   * <br>XBADLNP (0Ch): Lok# is illegal for this protocol.
   * <p>
   * N.B. The 'real' speed is NOT expressed in the P50 format! According to each protocol, the 'real' speed value tells:
   * <pre>
   * Motorola:  0 (Stop), 1 (Change Dir), 2 (min Speed), .., 15 (max Speed)
   * Selectrix: 0 (Stop), 1 (min Speed), .., 31 (max Speed)
   * DCC:       0 (Stop), 1 (Em.Stop), 2 (min Speed), .., 15/28/29/127 (max Speed)
   * FMZ:       0 (Stop), 1 (min Speed), .., 15 (max Speed)
   *            (no information is returned about an eventual FMZ Em. Stop
   *            status; however this can be inferred from the Speed status of
   *            the Lok, i.e. from the 2nd byte of the XLokSts reply)
   * </pre> If you need to compute the P50 speed starting from the P50X Speed, the following BASIC routine shows how to proceed (this very algorithm is internally used by the IB):
   * <pre>
   * DEFINT A-Z
   * FUNCTION P50_Speed% (P50X_Speed AS INTEGER)
   * SELECT CASE P50X_Speed
   * CASE 0, 1
   *    P50_Speed = 0
   * CASE ELSE
   *    P50_Speed = INT((P50X_Speed * 2) / 19) + 1
   * END SELECT
   * END FUNCTION
   * </pre> (Obviously, it cannot ever happen that P50_Speed() = 15.)
   * <p>
   * The opposite conversion is performed using a table, indexed by the P50 speed:
   * <pre>
   *    0,  2, 10, 19, 29, 38, 48, 57, 67, 76, 86, 95, 105, 114, 127, 1
   * </pre> The speed conversion routines for the other protocols/speed steps are:
   * <pre>
   * Speed_15: (FMZ protocol: 0 = Stop, 1..15 = non-stop speed steps)
   *    IF (P50X_Speed = 0) OR (P50X_Speed = 1) THEN
   *       Speed_15 = 0
   *    ELSE
   *       Speed_15 = INT((P50X_Speed + 1) / 9) + 1
   *    ENDIF
   *
   * Speed_27: (DCC 27 protocol: 0 = Stop, 1 = Em. Stop, 2..28 = non-stop speed steps)
   *    IF (P50X_Speed = 0) OR (P50X_Speed = 1) THEN
   *       Speed_27 = P50X_Speed
   *    ELSE
   *       IF (P50X_Speed &lt; 8) THEN
   *          Speed_27 = 2
   *       ELSE
   *          nTemp  = P50X_Speed * 2
   *          nTemp1 = INT(nTemp / 9)
   *          nTemp2 = nTemp - (nTemp1 * 9)
   *          IF (nTemp2 &gt;= 7) THEN
   *             nTemp1 = nTemp1 + 2
   *          ELSE
   *             nTemp1 = nTemp1 + 1
   *          ENDIF
   *          IF nTemp1 &gt; 28 THEN
   *             Speed_27 = 28
   *          ELSE
   *             Speed_27 = nTemp1
   *          ENDIF
   *       ENDIF
   *    ENDIF
   *
   * Speed_28: (DCC 28 protocol: 0 = Stop, 1 = Em. Stop, 2..29 = non-stop speed steps)
   *    IF (P50X_Speed = 0) OR (P50X_Speed = 1) THEN
   *       Speed_28 = P50X_Speed
   *    ELSE
   *       Speed_28 = INT(((P50X_Speed - 2) * 2) / 9) + 2
   *    ENDIF
   *
   * Speed_31: (Selectrix protocol: 0 = Stop, 1..31 = non-stop speed steps)
   *    IF (P50X_Speed = 0) OR (P50X_Speed = 1) THEN
   *       Speed_31 = 0
   *    ELSE
   *       IF (P50X_Speed &lt; 4) THEN
   *          Speed_31 = 1
   *       ELSE
   *          Speed_31 = INT(P50X_Speed / 4)
   *       ENDIF
   *    ENDIF
   * </pre>
   */
  public final static int X_LOK_STS = 0x84;

  /**
   * XLokCfg (085h) - length = 1+2 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: low byte of Lok address.
   * <br>2nd: high byte of Lok address.
   * <p>
   * Reply:
   * <br>1st: either 00h (cmd Ok, 4 bytes shall follow) or error code.
   * <br>2nd: Lok protocol: 0..3 = Mrk, SX, DCC, FMZ.
   * <br>3rd: number of (non-Stop) speed steps supported by this Lok type. Currently this would be: 14, 15, 27, 28, 31 or 126.
   * <p>
   * The rest of the reply depends on whether this Lok is a virtual one or a real one:
   * <br><em>Virtual:</em>
   * <br>4th: Real Lok# (low) corresponding to this virtual one.
   * <br>5th: Real Lok# (high) (Bit #6 is 0 — this tells a virtual Lok from a real one).
   * <br><em>Real:</em>
   * <br>4th: 0FFh
   * <br>5th: 0FFh (bit #6 is 1 — this tells a real Lok from a virtual one).
   * <p>
   * N.B. Bit #7 in the 5th byte of the reply is reserved for future use.
   * <p>
   * Error codes:
   * <br>XBADPRM (02h): illegal parameter value.
   */
  public final static int X_LOK_CFG = 0x85;

  /**
   * XLkDisp (083h) - length = 1+2 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: low byte of Lok address.
   * <br>2nd: high byte of Lok address (or 0FFh if asking for dispatch status).
   * <p>
   * Reply (if Lok address high byte not equal 0FFh — i.e. 'dispatch put'):
   * <br>1st: either 00h (cmd Ok) or error code (e.g. XBADLNP, XNODATA).
   * <p>
   * Reply (if Lok address high byte equal 0FFh — i.e. 'dispatch get'):
   * <br>1st: either 00h (there is no dispatched slot to 'get') or slot# (1..119) of the slot waiting to be 'got'.
   */
  public final static int X_LK_DISP = 0x83;

  /**
   * XFunc (088h) - length = 1+3 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: low byte of Lok address.
   * <br>2nd: high byte of Lok address.
   * <br>3rd: status of F1 (bit #0) .. F8 (bit #7).
   * <p>
   * Reply:
   * <br>1st: either 00h (cmd Ok) or error code.
   * <p>
   * Error codes:
   * <br>XBADPRM (02h): illegal parameter value.
   * <br>XNOSLOT (0Bh): there is no slot available.
   * <br>XBADLNP (0Ch): Lok# is illegal for this protocol.
   */
  public final static int X_FUNC = 0x88;

  /**
   * XFuncSts (08Ch) - length = 1+2 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: low byte of Lok address.
   * <br>2nd: high byte of Lok address.
   * <p>
   * Reply:
   * <br>1st: either 00h (cmd Ok, 1 byte shall follow) or error code.
   * <br>2nd: status of F1 (bit #0) .. F8 (bit #7).
   * <p>
   * Error codes:
   * <br>XBADPRM (02h): illegal parameter value.
   * <br>XNODATA (0Ah): no Lok status available (Lok is not in a slot).
   * <br>XBADLNP (0Ch): Lok# is illegal for this protocol.
   */
  public final static int X_FUNC_STS = 0x8c;

  /**
   * XTrnt (090h) - length = 1+2 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: low byte of Turnout address (A7..A0). N.B. turnout address, NOT turnout <em>decoder</em> address!
   * <br>2nd: high byte of Turnout address plus 'color' and status bits:
   * <pre>
   *  bit#   7     6     5     4     3     2     1     0
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   *      |Color| Sts | Res |NoCmd| n.u.| A10 |  A9 |  A8 |
   *      +-----+-----+-----+-----+-----+-----+-----+-----+
   * </pre> where:
   * <br>Color: 1 = closed (green), 0 = thrown (red).
   * <br>Sts: turnout status (1 = on, 0 = off).
   * <br>Res: set if this turnout is to be reserved for exclusive PC control: this implies that non-PC commands to this turnout would be discarded by the IB. An event would also be generated (please
   * check the XEvent cmd).
   * <br>NoCmd: if set then no turnout cmd is actually sent to the tracks. Setting this bit allows, e.g., to only set/reset the 'Res' bit of a turnout. Besides, if NoCmd is set, not even the internal
   * IB status of this turnout is modified.
   * <br>n.u.: not currently used (0).
   * <br>A10..A8: top address bits of turnout address.
   * <p>
   * Reply:
   * <br>1st: either 00h (cmd Ok) or error/warning code.
   * <p>
   * Error/warning codes:
   * <br>XBADPRM (02h): illegal parameter value.
   * <br>XPWOFF (06h): Error: the Power is Off!
   * <br>XNOTSPC (09h): Error: the fifo for turnout cmds is full, please try later. N.B. XNOTSPC was erroneously described as being equal to 07h in P50X_LT.TXT versions up to 0.93. Please also note
   * the new XNOISPC reply.
   * <br>XBADTNP (0Eh): Error: illegal Turnout address for this protocol.
   * <br>XNOISPC (10h): Error: the I2C fifo is full, please try later.
   * <br>XLOWTSP (40h): Warning: the fifo for turnout cmds is 75% full.
   * <p>
   * The IB fifo for turnout cmds can hold up to 16 turnout cmds. This allows 'parallel' turnout and Lok commands execution, and also plays a role in avoiding blocking the RS-232 channel in case of a
   * Power Off.
   * <p>
   * Side note: if a P50 turnout cmd is sent to the IB while in Power Off, the command is inserted in the turnout cmd buffer as usual, provided that Special Option (SO) #21 is 0 (the IB default).
   * However, since while in Power Off the turnout buffer is not processed 'from the other side' (no turnout cmds are sent to the tracks), sooner or later this buffer shall get full — thus eventually
   * blocking any further PC command to the IB. If SO #21 is 1, then P50 turnout cmds sent by the PC while in Power Off are automatically discarded, preventing RS-232 blocking. The default SO #21
   * value (0) corresponds to what a 6050+Mrk CU would do.
   */
  public final static int X_TRNT = 0x90;

  /**
   * XTrntFree (093h) - length = 1 byte
   * <p>
   * Allows resetting the 'Res' (reserved) bit of all turnouts with only one command.
   * <p>
   * Reply: always 00h.
   * <p>
   * N.B. Use the XTrnt cmd, with 'NoCmd' = 1, for individually setting the 'reserved' status of each turnout.
   * <p>
   * N.B. It would be best to issue an XTrntFree cmd before exiting your PC program! Otherwise some turnouts may be left in the 'reserved' status, not allowing a user to issue manual commands to them.
   */
  public final static int X_TRNT_FREE = 0x93;

  /**
   * XTrntSts (094h) - length = 1+2 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: low byte of Turnout address.
   * <br>2nd: high byte of Turnout address.
   * <p>
   * Reply:
   * <br>1st: either 00h (cmd Ok, 1 byte shall follow) or error code.
   * <br>2nd: bit field with the following meaning:
   * <br>bit #0: turnout configuration (check Bit #3 below).
   * <br>bit #1: turnout 'reserved' status: 1 = reserved, 0 = free.
   * <br>bit #2: turnout color: 1 = green (closed), 0 = red (thrown).
   * <br>bit #3: turnout extended configuration:
   * <pre>
   *   Bit #0 / #3   Turnout type
   *      0 0          Motorola
   *      1 0          DCC
   *      0 1          SX
   *      1 1          FMZ
   * </pre> Other bits are reserved for future use.
   * <p>
   * Error codes:
   * <br>XBADPRM (02h): illegal parameter value.
   * <br>XBADTNP (0Eh): Error: illegal Turnout address for this protocol.
   */
  public final static int X_TRNT_STS = 0x94;

  /**
   * XTrntGrp (095h) - length = 1+1 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: Turnout 'group' address (legal value: 1..255).
   * <br>The turnout 'group' address is 1 for turnouts #1..8, 2 for turnouts #9..16, etc. Formula:
   * <br>{@code turnout 'group' address = ((turnout address - 1) \ 8) + 1}
   * <p>
   * Reply:
   * <br>1st: either 00h (cmd Ok, 2 bytes shall follow) or error code.
   * <br>2nd: Turnout 'group' color: each bit tells the 'green' (1) or 'red' (0) status of the corresponding turnout. The 1st turnout of this group is in bit #0, etc.
   * <br>3rd: Turnout 'reserved' status: each bit corresponds to one turnout in the group.
   * <p>
   * Error codes:
   * <br>XBADPRM (02h): illegal parameter value.
   */
  public final static int X_TRNT_GRP = 0x95;

  // -------------------------------------------------------------------------
  // Possible future commands
  // -------------------------------------------------------------------------
  /**
   * XFuncX (089h) - length = 1+3 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: low byte of Lok address.
   * <br>2nd: high byte of Lok address.
   * <br>3rd: status of F9 (bit #0) .. F16 (bit #7).
   * <p>
   * Reply:
   * <br>1st: either 00h (cmd Ok) or error code.
   */
  public final static int X_FUNC_X = 0x89;

  /**
   * XFuncXSts (08Dh) - length = 1+2 bytes
   * <p>
   * Parameters (byte):
   * <br>1st: low byte of Lok address.
   * <br>2nd: high byte of Lok address.
   * <p>
   * Reply:
   * <br>1st: either 00h (cmd Ok, 1 byte shall follow) or error code.
   * <br>2nd: status of F9 (bit #0) .. F16 (bit #7).
   */
  public final static int X_FUNC_X_STS = 0x8d;

}
