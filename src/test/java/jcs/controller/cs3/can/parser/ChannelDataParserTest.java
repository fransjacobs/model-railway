/*
 * Copyright 2023 Frans Jacobs.
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
package jcs.controller.cs3.can.parser;

import jcs.controller.cs2.ChannelDataParser;
import jcs.controller.cs.MeasurementChannel;
import jcs.controller.cs.can.CanMessage;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class ChannelDataParserTest {

  private CanMessage chan1;
  private CanMessage chan2;
  private CanMessage chan3;
  private CanMessage chan4;
  private MeasurementChannel channel1;
  private MeasurementChannel channel2;
  private MeasurementChannel channel3;
  private MeasurementChannel channel4;

  public ChannelDataParserTest() {
  }

  @Before
  public void setUp() {
    chan1 = new CanMessage(new byte[]{0x00, 0x3a, 0x37, 0x7f, 0x05, 0x63, 0x73, 0x45, (byte) 0x8c, 0x01, 0x00, 0x00, 0x00});
    chan1.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x01, 0x08, 0x01, (byte) 0xfd, 0x30, (byte) 0xf0, (byte) 0xe0, (byte) 0xc0, 0x00, 0x00}));
    chan1.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x02, 0x08, 0x02, 0x28, 0x02, 0x40, 0x02, 0x58, 0x02, (byte) 0x94}));
    chan1.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x03, 0x08, 0x4d, 0x41, 0x49, 0x4e, 0x00, 0x30, 0x2e, 0x30}));
    chan1.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x04, 0x08, 0x30, 0x00, 0x35, 0x2e, 0x35, 0x30, 0x00, 0x41}));
    chan1.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x05, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
    chan1.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x26, 0x06, 0x63, 0x73, 0x45, (byte) 0x8c, 0x01, 0x05, 0x00, 0x00}));

    this.channel1 = new MeasurementChannel();
    this.channel1.setNumber(1);
    this.channel1.setScale(-3);
    this.channel1.setColorMax(48);
    this.channel1.setColorGreen(240);
    this.channel1.setColorYellow(224);
    this.channel1.setColorRed(192);
    this.channel1.setStartValue(0.0);
    this.channel1.setRangeMax(552);
    this.channel1.setRangeGreen(576);
    this.channel1.setRangeYellow(600);
    this.channel1.setRangeRed(660);
    this.channel1.setName("MAIN");
    this.channel1.setEndValue(5.5);
    this.channel1.setUnit("A");

    chan2 = new CanMessage(new byte[]{0x00, 0x3a, 0x37, 0x7f, 0x05, 0x63, 0x73, 0x45, (byte) 0x8c, 0x02, 0x00, 0x00, 0x00});
    chan2.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x01, 0x08, 0x02, (byte) 0xfd, 0x30, (byte) 0xf0, (byte) 0xe0, (byte) 0xc0, 0x00, 0x00}));
    chan2.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x02, 0x08, 0x01, 0x4a, 0x01, 0x6b, 0x02, 0x31, 0x02, (byte) 0xf7}));
    chan2.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x03, 0x08, 0x50, 0x52, 0x4f, 0x47, 0x00, 0x30, 0x2e, 0x30}));
    chan2.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x04, 0x08, 0x30, 0x00, 0x32, 0x2e, 0x33, 0x30, 0x00, 0x41}));
    chan2.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x05, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
    chan2.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x26, 0x06, 0x63, 0x73, 0x45, (byte) 0x8c, 0x02, 0x05, 0x00, 0x00}));

    this.channel2 = new MeasurementChannel();
    this.channel2.setNumber(2);
    this.channel2.setScale(-3);
    this.channel2.setColorMax(48);
    this.channel2.setColorGreen(240);
    this.channel2.setColorYellow(224);
    this.channel2.setColorRed(192);
    this.channel2.setStartValue(0.0);
    this.channel2.setRangeMax(330);
    this.channel2.setRangeGreen(363);
    this.channel2.setRangeYellow(561);
    this.channel2.setRangeRed(759);
    this.channel2.setName("PROG");
    this.channel2.setEndValue(2.3);
    this.channel2.setUnit("A");

    chan3 = new CanMessage(new byte[]{0x00, 0x3a, 0x37, 0x7f, 0x05, 0x63, 0x73, 0x45, (byte) 0x8c, 0x03, 0x00, 0x00, 0x00});
    chan3.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x01, 0x08, 0x03, (byte) 0xfd, (byte) 0xc0, 0x0c, 0x30, (byte) 0xc0, 0x00, 0x00}));
    chan3.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x02, 0x08, 0x00, (byte) 0xc2, 0x00, (byte) 0xfc, 0x02, 0x1f, 0x02, (byte) 0x93}));
    chan3.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x03, 0x08, 0x56, 0x4f, 0x4c, 0x54, 0x00, 0x31, 0x30, 0x2e}));
    chan3.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x04, 0x08, 0x30, 0x30, 0x00, 0x32, 0x37, 0x2e, 0x30, 0x30}));
    chan3.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x05, 0x08, 0x00, 0x56, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
    chan3.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x26, 0x06, 0x63, 0x73, 0x45, (byte) 0x8c, 0x03, 0x05, 0x00, 0x00}));

    this.channel3 = new MeasurementChannel();
    this.channel3.setNumber(3);
    this.channel3.setScale(-3);
    this.channel3.setColorMax(192);
    this.channel3.setColorGreen(12);
    this.channel3.setColorYellow(48);
    this.channel3.setColorRed(192);
    this.channel3.setStartValue(10.0);
    this.channel3.setRangeMax(194);
    this.channel3.setRangeGreen(252);
    this.channel3.setRangeYellow(543);
    this.channel3.setRangeRed(659);
    this.channel3.setName("VOLT");
    this.channel3.setEndValue(27.0);
    this.channel3.setUnit("V");

    chan4 = new CanMessage(new byte[]{0x00, 0x3a, 0x37, 0x7f, 0x05, 0x63, 0x73, 0x45, (byte) 0x8c, 0x04, 0x00, 0x00, 0x00});
    chan4.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x01, 0x08, 0x04, 0x00, 0x0c, 0x08, (byte) 0xf0, (byte) 0xc0, 0x00, 0x00}));
    chan4.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x02, 0x08, 0x00, 0x79, 0x00, (byte) 0x91, 0x00, (byte) 0xa9, 0x00, (byte) 0xc1}));
    chan4.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x03, 0x08, 0x54, 0x45, 0x4d, 0x50, 0x00, 0x30, 0x2e, 0x30}));
    chan4.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x04, 0x08, 0x00, 0x38, 0x30, 0x2e, 0x30, 0x00, 0x43, 0x00}));
    chan4.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x26, 0x06, 0x63, 0x73, 0x45, (byte) 0x8c, 0x04, 0x04, 0x00, 0x00}));

    this.channel4 = new MeasurementChannel();
    this.channel4.setNumber(4);
    this.channel4.setScale(0);
    this.channel4.setColorMax(12);
    this.channel4.setColorGreen(8);
    this.channel4.setColorYellow(240);
    this.channel4.setColorRed(192);
    this.channel4.setStartValue(0.0);
    this.channel4.setRangeMax(121);
    this.channel4.setRangeGreen(145);
    this.channel4.setRangeYellow(169);
    this.channel4.setRangeRed(193);
    this.channel4.setName("TEMP");
    this.channel4.setEndValue(80.0);
    this.channel4.setUnit("C");
  }

  @Test
  public void testGetChannel1() {
    System.out.println("getChannel1");
    ChannelDataParser instance = new ChannelDataParser(chan1);
    Logger.debug(instance);

    MeasurementChannel expResult = channel1;
    MeasurementChannel result = instance.getChannel();
    assertEquals(expResult, result);

//expected:<GFPChannel{unit=A, endValue=5.5, colorYellow=224, colorGreen=240, colorMax=48, colorRed=192, name=MAIN, number=1, scale=-3, rangeYellow=600, rangeGreen=576, rangeMax=552, rangeRed=660, startValue=0.0, value=null, humanValue=null}> 
// but was:<GFPChannel{unit=A, endValue=5.5, colorYellow=224, colorGreen=240, colorMax=48, colorRed=192, name=MAIN, number=1, scale=253, rangeYellow=600, rangeGreen=576, rangeMax=552, rangeRed=660, startValue=0.0, value=null, humanValue=null}>
  }

  @Test
  public void testGetChannel2() {
    System.out.println("getChannel2");
    ChannelDataParser instance = new ChannelDataParser(chan2);
    Logger.debug(instance);

    MeasurementChannel expResult = channel2;
    MeasurementChannel result = instance.getChannel();
    assertEquals(expResult, result);

//expected:<GFPChannel{unit=A, endValue=2.3, colorYellow=224, colorGreen=240, colorMax=48, colorRed=192, name=PROG, number=2, scale=-3, rangeYellow=561, rangeGreen=363, rangeMax=330, rangeRed=759, startValue=0.0, value=null, humanValue=null}>
// but was:<GFPChannel{unit=null, endValue=null, colorYellow=224, colorGreen=240, colorMax=48, colorRed=192, name=PROG, number=2, scale=-3, rangeYellow=561, rangeGreen=363, rangeMax=330, rangeRed=759, startValue=0.0, value=null, humanValue=null}>
  }

  @Test
  public void testGetChannel3() {
    System.out.println("getChannel3");
    ChannelDataParser instance = new ChannelDataParser(chan3);
    Logger.debug(instance);

    MeasurementChannel expResult = channel3;
    MeasurementChannel result = instance.getChannel();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetChannel4() {
    System.out.println("getChannel4");
    ChannelDataParser instance = new ChannelDataParser(chan4);
    Logger.debug(instance);

    MeasurementChannel expResult = channel4;
    MeasurementChannel result = instance.getChannel();
    assertEquals(expResult, result);
  }

}
