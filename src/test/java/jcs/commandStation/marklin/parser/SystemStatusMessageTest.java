/*
 * Copyright 2025 frans.
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
package jcs.commandStation.marklin.parser;

import jcs.commandStation.entities.MeasurementBean;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.commandStation.marklin.cs.can.device.MeasuringChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author frans
 */
public class SystemStatusMessageTest {

  public SystemStatusMessageTest() {
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  private CanMessage getSystemStatusMessageChannel1() {
    CanMessage sysStat = CanMessage.parse("0x00 0x00 0x37 0x7f 0x06 0x63 0x73 0x45 0x8c 0x0b 0x01 0x00 0x00");
    sysStat.addResponse(CanMessage.parse("0x00 0x01 0x03 0x26 0x08 0x63 0x73 0x45 0x8c 0x0b 0x01 0x00 0x00"));
    return sysStat;
  }

  private CanMessage getSystemStatusMessageChannel2() {
    CanMessage sysStat = CanMessage.parse("0x00 0x00 0x37 0x7f 0x06 0x63 0x73 0x45 0x8c 0x0b 0x02 0x00 0x00");
    sysStat.addResponse(CanMessage.parse("0x00 0x01 0x03 0x26 0x08 0x63 0x73 0x45 0x8c 0x0b 0x02 0x00 0x03"));
    return sysStat;
  }

  private CanMessage getSystemStatusMessageChannel3() {
    CanMessage sysStat = CanMessage.parse("0x00 0x00 0x37 0x7f 0x06 0x63 0x73 0x45 0x8c 0x0b 0x03 0x00 0x00");
    sysStat.addResponse(CanMessage.parse("0x00 0x01 0x03 0x26 0x08 0x63 0x73 0x45 0x8c 0x0b 0x03 0x01 0x5e"));
    return sysStat;
  }

  private CanMessage getSystemStatusMessageChannel4() {
    CanMessage sysStat = CanMessage.parse("0x00 0x00 0x37 0x7f 0x06 0x63 0x73 0x45 0x8c 0x0b 0x04 0x00 0x00");
    sysStat.addResponse(CanMessage.parse("0x00 0x01 0x03 0x26 0x08 0x63 0x73 0x45 0x8c 0x0b 0x04 0x00 0x74"));
    return sysStat;
  }

  @Test
  public void testParseChannel1() {
    System.out.println("parseChannel1");
    MeasuringChannel channel = new MeasuringChannel();
    channel.setNumber(1);
    channel.setName("MAIN");
    channel.setScale(-3);
    channel.setColorGreen(48);
    channel.setColorYellow(240);
    channel.setColorRed(224);
    channel.setColorMax(192);
    channel.setZeroPoint(0);
    channel.setRangeGreen(552);
    channel.setRangeRed(576);
    channel.setRangeMax(660);
    channel.setStartValue(0.0);
    channel.setEndValue(5.5);
    channel.setUnit("A");

    CanMessage systemStatusmessage = getSystemStatusMessageChannel1();
    long now = System.currentTimeMillis();
    MeasurementBean expResult = new MeasurementBean(channel.getNumber(), channel.getName(), now, 0, channel.getUnit(), 0.0);

    MeasurementBean result = SystemStatusMessage.parse(channel, systemStatusmessage, now);
    assertEquals(expResult, result);
  }

  @Test
  public void testParseChannel2() {
    System.out.println("parseChannel2");
    MeasuringChannel channel = new MeasuringChannel();
    channel.setNumber(2);
    channel.setName("PROG");
    channel.setScale(-3);
    channel.setColorGreen(48);
    channel.setColorYellow(240);
    channel.setColorRed(224);
    channel.setColorMax(192);
    channel.setZeroPoint(0);
    channel.setRangeGreen(330);
    channel.setRangeRed(363);
    channel.setRangeMax(759);
    channel.setStartValue(0.0);
    channel.setEndValue(2.3);
    channel.setUnit("A");

    CanMessage systemStatusmessage = getSystemStatusMessageChannel2();
    long now = System.currentTimeMillis();
    MeasurementBean expResult = new MeasurementBean(channel.getNumber(), channel.getName(), now, 3, channel.getUnit(), 0.009);

    MeasurementBean result = SystemStatusMessage.parse(channel, systemStatusmessage, now);
    assertEquals(expResult, result);
  }

  @Test
  public void testParseChannel3() {
    System.out.println("parseChannel3");
    MeasuringChannel channel = new MeasuringChannel();
    channel.setNumber(3);
    channel.setName("VOLT");
    channel.setScale(-3);
    channel.setColorGreen(192);
    channel.setColorYellow(12);
    channel.setColorRed(48);
    channel.setColorMax(192);
    channel.setZeroPoint(0);
    channel.setRangeGreen(194);
    channel.setRangeYellow(252);
    channel.setRangeRed(252);
    channel.setRangeMax(659);
    channel.setStartValue(10.0);
    channel.setEndValue(27.0);
    channel.setUnit("V");

    CanMessage systemStatusmessage = getSystemStatusMessageChannel3();
    long now = System.currentTimeMillis();
    MeasurementBean expResult = new MeasurementBean(channel.getNumber(), channel.getName(), now, 350, channel.getUnit(), 19.0);

    MeasurementBean result = SystemStatusMessage.parse(channel, systemStatusmessage, now);
    assertEquals(expResult, result);
  }

  @Test
  public void testParseChannel4() {
    System.out.println("parseChannel4");
    MeasuringChannel channel = new MeasuringChannel();
    channel.setNumber(4);
    channel.setName("TEMP");
    channel.setScale(-3);
    channel.setColorGreen(12);
    channel.setColorYellow(8);
    channel.setColorRed(240);
    channel.setColorMax(192);
    channel.setZeroPoint(0);
    channel.setRangeGreen(121);
    channel.setRangeYellow(145);
    channel.setRangeRed(145);
    channel.setRangeMax(193);
    channel.setStartValue(0.0);
    channel.setEndValue(80.0);
    channel.setUnit("C");

    CanMessage systemStatusmessage = getSystemStatusMessageChannel4();
    long now = System.currentTimeMillis();
    MeasurementBean expResult = new MeasurementBean(channel.getNumber(), channel.getName(), now, 116, channel.getUnit(), 48.1);

    MeasurementBean result = SystemStatusMessage.parse(channel, systemStatusmessage, now);
    assertEquals(expResult, result);
  }

}
