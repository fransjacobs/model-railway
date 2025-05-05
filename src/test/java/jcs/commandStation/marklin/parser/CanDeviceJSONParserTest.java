/*
 * Copyright 2025 fransjacobs.
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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import jcs.commandStation.marklin.cs.can.device.CanDevice;
import jcs.commandStation.marklin.cs.can.device.ConfigChannel;
import jcs.commandStation.marklin.cs.can.device.MeasuringChannel;
import jcs.commandStation.marklin.cs.net.CSHTTPConnectionVirt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class CanDeviceJSONParserTest {

  String deviceJson;

  public CanDeviceJSONParserTest() {
    try {
      deviceJson = new CSHTTPConnectionVirt().getDevicesJSON();
    } catch (UnknownHostException ex) {
      Logger.error(ex);
    }
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test of parse method, of class CanDeviceJSONParser.
   */
  @Test
  public void testParse() {
    System.out.println("parse");
    String json = deviceJson;
    List<CanDevice> expResult = new ArrayList<>();

    CanDevice expGfp = new CanDevice();
    expGfp.setUid("0x6373458c");
    expGfp.setName("GFP3-1");
    expGfp.setIdentifier("0xffff");
    expGfp.setArticleNumber("60226");
    expGfp.setSerial("0000");
    expGfp.setVersion("12.113");
    expGfp.setMeasureChannelCount(3);
    expGfp.setConfigChannelCount(1);

    MeasuringChannel mc1 = new MeasuringChannel();
    mc1.setNumber(1);
    mc1.setName("MAIN");
    mc1.setScale(-3);
    mc1.setColorGreen(48);
    mc1.setColorYellow(240);
    mc1.setColorRed(224);
    mc1.setColorMax(192);
    mc1.setZeroPoint(0);
    mc1.setRangeGreen(552);
    mc1.setRangeYellow(576);
    mc1.setRangeRed(600);
    mc1.setRangeMax(660);
    mc1.setStartValue(0.0);
    mc1.setEndValue(5.5);
    mc1.setUnit("A");
    expGfp.addMeasuringChannel(mc1);

    MeasuringChannel mc2 = new MeasuringChannel();
    mc2.setNumber(2);
    mc2.setName("PROG");
    mc2.setScale(-3);
    mc2.setColorGreen(48);
    mc2.setColorYellow(240);
    mc2.setColorRed(224);
    mc2.setColorMax(192);
    mc2.setZeroPoint(0);
    mc2.setRangeGreen(330);
    mc2.setRangeYellow(363);
    mc2.setRangeRed(561);
    mc2.setRangeMax(759);
    mc2.setStartValue(0.0);
    mc2.setEndValue(2.3);
    mc2.setUnit("A");
    expGfp.addMeasuringChannel(mc2);

    MeasuringChannel mc4 = new MeasuringChannel();
    mc4.setNumber(4);
    mc4.setName("TEMP");
    mc4.setScale(0);
    mc4.setColorGreen(12);
    mc4.setColorYellow(8);
    mc4.setColorRed(240);
    mc4.setColorMax(192);
    mc4.setZeroPoint(0);
    mc4.setRangeGreen(121);
    mc4.setRangeYellow(145);
    mc4.setRangeRed(169);
    mc4.setRangeMax(193);
    mc4.setStartValue(0.0);
    mc4.setEndValue(80.0);
    mc4.setUnit("C");
    expGfp.addMeasuringChannel(mc4);

    ConfigChannel cc1 = new ConfigChannel();
    cc1.setNumber(1);
    cc1.setChoicesCount(3);
    cc1.setValueId(1);
    cc1.setChoiceDescription("Netzteil:");
    cc1.addChoice("60061");
    cc1.addChoice("60101");
    cc1.addChoice("L51095");
    cc1.setLowValue(0);
    cc1.setHighValue(0);
    cc1.setActualValue(0);
    expGfp.addConfigChannel(cc1);

    List<CanDevice> result = CanDeviceJSONParser.parse(json);
    for (CanDevice cs : result) {
      Logger.trace(cs);
    }
    CanDevice resultGfp = result.get(0);
    assertEquals(expGfp, resultGfp);
  }

}
