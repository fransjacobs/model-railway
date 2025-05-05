/*
 * Copyright 2025 Frans Jacobs.
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

import java.util.ArrayList;
import java.util.List;
import jcs.commandStation.marklin.cs.can.device.MeasuringChannel;
import jcs.commandStation.marklin.cs.can.device.CanDevice;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.commandStation.marklin.cs.can.device.ConfigChannel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 */
public class CanDeviceParserTest {

  public CanDeviceParserTest() {
  }

  private CanMessage getMemberPing() {
    CanMessage ping = CanMessage.parse("0x00 0x30 0x07 0x69 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00");
    ping.addResponse(CanMessage.parse("0x00 0x31 0x37 0x7e 0x08 0x63 0x73 0x45 0x8d 0x02 0x05 0xff 0xff"));
    ping.addResponse(CanMessage.parse("0x00 0x31 0x7b 0x79 0x08 0x53 0x38 0x5c 0x41 0x01 0x01 0x00 0x40"));
    ping.addResponse(CanMessage.parse("0x00 0x31 0x03 0x26 0x08 0x63 0x73 0x45 0x8c 0x0c 0x71 0x00 0x50"));
    return ping;
  }

  @Test
  public void testParseMemberPingMessage() {
    System.out.println("parseMemberPingMessage");
    CanMessage memberPingmessage = getMemberPing();
    List<CanDevice> expResult = new ArrayList<>();

    CanDevice cs = new CanDevice();
    cs.setUid("0x6373458d");
    cs.setIdentifier("0xffff");
    cs.setVersion("2.5");
    expResult.add(cs);

    CanDevice linkS88 = new CanDevice();
    linkS88.setUid("0x53385c41");
    linkS88.setIdentifier("0x40");
    linkS88.setVersion("1.1");
    expResult.add(linkS88);

    CanDevice gfp = new CanDevice();
    gfp.setUid("0x6373458c");
    gfp.setIdentifier("0x50");
    gfp.setVersion("12.113");
    expResult.add(gfp);

    List<CanDevice> result = CanDeviceParser.parse(memberPingmessage);

    assertEquals(expResult, result);
  }

  private CanMessage getStatusDataConfigGFPIndex0() {
    CanMessage statusDataConfig = CanMessage.parse("0x00 0x3a 0x37 0x7f 0x05 0x63 0x73 0x45 0x8c 0x00 0x00 0x00 0x00");
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x01 0x08 0x04 0x02 0x00 0x00 0x00 0x00 0x09 0x46"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x02 0x08 0x36 0x30 0x32 0x32 0x36 0x00 0x00 0x00"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x03 0x08 0x43 0x65 0x6e 0x74 0x72 0x61 0x6c 0x20"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x04 0x08 0x53 0x74 0x61 0x74 0x69 0x6f 0x6e 0x20"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x05 0x08 0x33 0x00 0x00 0x00 0x00 0x00 0x00 0x00"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x26 0x06 0x63 0x73 0x45 0x8c 0x00 0x05 0x00 0x00"));
    return statusDataConfig;
  }

  @Test
  public void testParseStatusDataConfigGFPIndex0() {
    System.out.println("parseStatusDataConfigGFPIndex0");
    CanDevice canDevice = new CanDevice();
    canDevice.setUid("0x6373458c");
    canDevice.setIdentifier("0x50");
    canDevice.setVersion("12.113");

    CanMessage statusConfigmessage = getStatusDataConfigGFPIndex0();
    CanDevice expResult = new CanDevice();
    expResult.setUid("0x6373458c");
    expResult.setIdentifier("0x50");
    expResult.setVersion("12.113");
    expResult.setName("Central Station 3");
    expResult.setArticleNumber("60226");
    expResult.setMeasureChannelCount(4);
    expResult.setConfigChannelCount(2);
    expResult.setSerial(2374);

    CanDeviceParser.parse(canDevice, statusConfigmessage);
    CanDevice result = canDevice;
    assertEquals(expResult, result);
  }

  private CanMessage getStatusDataConfigGFPIndex1() {
    CanMessage statusDataConfig = CanMessage.parse("0x00 0x3a 0x37 0x7f 0x05 0x63 0x73 0x45 0x8c 0x01 0x00 0x00 0x00");
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x01 0x08 0x01 0xfd 0x30 0xf0 0xe0 0xc0 0x00 0x00"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x02 0x08 0x02 0x28 0x02 0x40 0x02 0x58 0x02 0x94"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x03 0x08 0x4d 0x41 0x49 0x4e 0x00 0x30 0x2e 0x30"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x04 0x08 0x30 0x00 0x35 0x2e 0x35 0x30 0x00 0x41"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x05 0x08 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x26 0x06 0x63 0x73 0x45 0x8c 0x01 0x05 0x00 0x00"));
    return statusDataConfig;
  }

  @Test
  public void testParseStatusDataConfigGFPIndex1() {
    System.out.println("parseStatusDataConfigGFPIndex1");
    CanDevice canDevice = new CanDevice();
    canDevice.setUid("0x6373458c");
    canDevice.setIdentifier("0x50");
    canDevice.setVersion("12.113");
    canDevice.setName("Central Station 3");
    canDevice.setArticleNumber("60226");
    canDevice.setMeasureChannelCount(4);
    canDevice.setConfigChannelCount(2);

    CanMessage statusConfigmessage = getStatusDataConfigGFPIndex1();
    CanDeviceParser.parse(canDevice, statusConfigmessage);

    CanDevice expDeviceResult = new CanDevice();
    expDeviceResult.setUid("0x6373458c");
    expDeviceResult.setIdentifier("0x50");
    expDeviceResult.setVersion("12.113");
    expDeviceResult.setName("Central Station 3");
    expDeviceResult.setArticleNumber("60226");
    expDeviceResult.setMeasureChannelCount(4);
    expDeviceResult.setConfigChannelCount(2);

    MeasuringChannel result = canDevice.getMeasuringChannel(1);
    assertNotNull(result);

    MeasuringChannel expResult = new MeasuringChannel();
    expResult.setNumber(1);
    expResult.setName("MAIN");
    expResult.setScale(-3);
    expResult.setColorGreen(48);
    expResult.setColorYellow(240);
    expResult.setColorRed(224);
    expResult.setColorMax(192);
    expResult.setZeroPoint(0);
    expResult.setRangeGreen(552);
    expResult.setRangeYellow(576);
    expResult.setRangeRed(576);
    expResult.setRangeMax(660);
    expResult.setStartValue(0.0);
    expResult.setEndValue(5.5);
    expResult.setUnit("A");

    expDeviceResult.addMeasuringChannel(expResult);

    assertEquals(expDeviceResult, canDevice);
    assertEquals(expResult, result);
  }

  private CanMessage getStatusDataConfigLinkS88Index0() {
    CanMessage statusDataConfig = CanMessage.parse("0x00 0x3a 0x7b 0x79 0x05 0x53 0x38 0x5c 0x41 0x00 0x00 0x00 0x00");
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x01 0x08 0x00 0x0c 0x00 0x00 0x00 0x00 0x24 0x41"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x02 0x08 0x36 0x30 0x38 0x38 0x33 0x00 0x00 0x00"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x03 0x08 0x4c 0x69 0x6e 0x6b 0x20 0x53 0x38 0x38"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x04 0x08 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x3f 0x3c 0x06 0x53 0x38 0x5c 0x41 0x00 0x04 0x00 0x00"));
    return statusDataConfig;
  }

  @Test
  public void testParseStatusDataConfigLinkS88() {
    System.out.println("parseStatusDataConfigLinkS88");
    CanDevice canDevice = new CanDevice();
    canDevice.setUid("0x53385c41");
    canDevice.setIdentifier("0x40");
    canDevice.setVersion("1.1");

    CanMessage statusConfigmessage = getStatusDataConfigLinkS88Index0();
    CanDevice expResult = new CanDevice();
    expResult.setUid("0x53385c41");
    expResult.setName("Link S88");
    expResult.setIdentifier("0x40");
    expResult.setVersion("1.1");
    expResult.setArticleNumber("60883");
    expResult.setMeasureChannelCount(0);
    expResult.setConfigChannelCount(12);
    expResult.setVersion("1.1");
    expResult.setSerial(9281);

    CanDeviceParser.parse(canDevice, statusConfigmessage);
    CanDevice result = canDevice;
    assertEquals(expResult, result);
  }

  private CanMessage getStatusDataConfigLinkS88Index1() {
    CanMessage statusDataConfig = CanMessage.parse("0x00 0x3a 0x7b 0x79 0x05 0x53 0x38 0x5c 0x41 0x01 0x00 0x00 0x00");
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x01 0x08 0x01 0x01 0x02 0x00 0x00 0x00 0x00 0x00"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x02 0x08 0x41 0x75 0x73 0x77 0x65 0x72 0x74 0x75"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x03 0x08 0x6e 0x67 0x20 0x31 0x20 0x2d 0x20 0x31"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x04 0x08 0x36 0x00 0x45 0x69 0x6e 0x7a 0x65 0x6c"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x05 0x08 0x6e 0x00 0x54 0x61 0x73 0x74 0x61 0x74"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x06 0x08 0x75 0x72 0x6d 0x61 0x74 0x72 0x69 0x78"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x07 0x08 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x3f 0x3c 0x06 0x53 0x38 0x5c 0x41 0x01 0x07 0x00 0x00"));
    return statusDataConfig;
  }

  @Test
  public void testParseStatusDataConfigLinkS88Index1() {
    System.out.println("parseStatusDataConfigLinkS88Index1");
    CanDevice canDevice = new CanDevice();
    canDevice.setUid("0x53385c41");
    canDevice.setIdentifier("0x40");
    canDevice.setVersion("1.1");
    canDevice.setName("Link S88");
    canDevice.setArticleNumber("60883");
    canDevice.setMeasureChannelCount(0);
    canDevice.setConfigChannelCount(12);

    CanMessage statusConfigmessage = getStatusDataConfigLinkS88Index1();
    CanDevice expResultDev = new CanDevice();
    expResultDev.setUid("0x53385c41");
    expResultDev.setName("Link S88");
    expResultDev.setIdentifier("0x40");
    expResultDev.setVersion("1.1");
    expResultDev.setArticleNumber("60883");
    expResultDev.setMeasureChannelCount(0);
    expResultDev.setConfigChannelCount(12);
    expResultDev.setVersion("1.1");

    ConfigChannel expResult = new ConfigChannel();
    expResult.setNumber(1);
    expResult.setChoicesCount(2);
    expResult.setValueId(0);
    expResult.setChoiceDescription("Auswertung 1 - 16");
    expResult.addChoice("Einzeln");
    expResult.addChoice("Tastaturmatrix");

    expResultDev.addConfigChannel(expResult);
    CanDeviceParser.parse(canDevice, statusConfigmessage);

    CanDevice resultDev = canDevice;
    
//expected:<CanDevice{uid=0x53385c41, name=Link S88, identifier=0x40, articleNumber=60883, measureChannelCount=0, configChannelCount=12, version=1.1, configChannels={1=ConfigChannel{number=1, choicesCount=2, valueId=0, choiceDescription=Auswertung 1 - 16, choices=[Einzeln, Tastaturmatrix]}}}> 
// but was:<CanDevice{uid=0x53385c41, name=Link S88, identifier=0x40, articleNumber=60883, measureChannelCount=0, configChannelCount=12, version=1.1, configChannels={1=ConfigChannel{number=1, choicesCount=2, valueId=0, choiceDescription=Auswertung 1 - 16, choices=[Einzeln]}}}>
    
    assertEquals(expResultDev, resultDev);

    ConfigChannel result = canDevice.getConfigChannel(1);
    assertNotNull(result);

    assertEquals(expResult, result);
  }

  private CanMessage getStatusDataConfigLinkS88Index2() {
    CanMessage statusDataConfig = CanMessage.parse("0x00 0x3a 0x7b 0x79 0x05 0x53 0x38 0x5c 0x41 0x02 0x00 0x00 0x00");
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x01 0x08 0x02 0x02 0x00 0x00 0x00 0x1f 0x00 0x02"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x02 0x08 0x4c 0xc3 0xa4 0x6e 0x67 0x65 0x20 0x42"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x03 0x08 0x75 0x73 0x20 0x31 0x20 0x28 0x52 0x4a"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x04 0x08 0x34 0x35 0x2d 0x31 0x29 0x00 0x30 0x00"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x03 0x05 0x08 0x33 0x31 0x00 0x00 0x00 0x00 0x00 0x00"));
    statusDataConfig.addResponse(CanMessage.parse("0x00 0x3b 0x3f 0x3c 0x06 0x53 0x38 0x5c 0x41 0x02 0x05 0x00 0x00"));
    return statusDataConfig;
  }

  @Test
  public void testParseStatusDataConfigLinkS88Index2() {
    System.out.println("parseStatusDataConfigLinkS88Index1");
    CanDevice canDevice = new CanDevice();
    canDevice.setUid("0x53385c41");
    canDevice.setIdentifier("0x40");
    canDevice.setVersion("1.1");
    canDevice.setName("Link S88");
    canDevice.setArticleNumber("60883");
    canDevice.setMeasureChannelCount(0);
    canDevice.setConfigChannelCount(12);

    CanMessage statusConfigmessage = getStatusDataConfigLinkS88Index2();
    CanDevice expResultDev = new CanDevice();
    expResultDev.setUid("0x53385c41");
    expResultDev.setName("Link S88");
    expResultDev.setIdentifier("0x40");
    expResultDev.setVersion("1.1");
    expResultDev.setArticleNumber("60883");
    expResultDev.setMeasureChannelCount(0);
    expResultDev.setConfigChannelCount(12);
    expResultDev.setVersion("1.1");

    ConfigChannel expResult = new ConfigChannel();
    expResult.setNumber(2);
    expResult.setValueId(2);
    expResult.setChoiceDescription("Länge Bus 1 (RJ45-1)");
    expResult.setActualValue(2);
    expResult.setLowValue(0);
    expResult.setHighValue(0);
    expResult.setStartName("0");
    expResult.setEndName("31");

    expResultDev.addConfigChannel(expResult);
    CanDeviceParser.parse(canDevice, statusConfigmessage);

    CanDevice resultDev = canDevice;
    assertEquals(expResultDev, resultDev);

    ConfigChannel result = canDevice.getConfigChannel(2);
    assertNotNull(result);

    assertEquals(expResult, result);
  }

}
