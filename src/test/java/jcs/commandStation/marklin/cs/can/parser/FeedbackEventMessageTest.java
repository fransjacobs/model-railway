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
package jcs.commandStation.marklin.cs.can.parser;

import java.util.Date;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.commandStation.marklin.cs.can.CanMessageFactory;
import jcs.entities.SensorBean;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 */
public class FeedbackEventMessageTest {

  public FeedbackEventMessageTest() {
  }

  @Test
  public void test65_bus0() {
    System.out.println("65-bus0");
    Date now = new Date();

    CanMessage msg1on = CanMessage.parse("0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x00 0x01 0x00 0x01 0xe5 0x10");
    CanMessage msg1off = CanMessage.parse("0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x00 0x01 0x01 0x00 0x00 0x32");

    CanMessage msg2on = CanMessage.parse("0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x00 0x02 0x00 0x01 0xab 0x68");
    CanMessage msg2off = CanMessage.parse("0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x00 0x02 0x01 0x00 0x00 0x46");

    SensorBean result1on = FeedbackEventMessage.parse(msg1on, now);
    SensorBean result1off = FeedbackEventMessage.parse(msg1off, now);

    SensorBean result2on = FeedbackEventMessage.parse(msg2on, now);
    SensorBean result2off = FeedbackEventMessage.parse(msg2off, now);

    SensorBean expResult1on = new SensorBean(65, 1, 1, 0, 586400, now);
    SensorBean expResult1off = new SensorBean(65, 1, 0, 1, 500, now);

    SensorBean expResult2on = new SensorBean(65, 2, 1, 0, 438800, now);
    SensorBean expResult2off = new SensorBean(65, 2, 0, 1, 700, now);

    assertEquals(expResult1on, result1on);
    assertEquals(expResult1off, result1off);
    assertEquals(expResult2on, result2on);
    assertEquals(expResult2off, result2off);
  }

  @Test
  public void test65_bus1() {
    System.out.println("65-bus1");
    Date now = new Date();

    CanMessage msg1001on = CanMessage.parse("0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x03 0xe9 0x00 0x01 0x00 0x0a");
    CanMessage msg1001off = CanMessage.parse("0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x03 0xe9 0x01 0x00 0x00 0x0a");

    SensorBean result1001on = FeedbackEventMessage.parse(msg1001on, now);
    SensorBean result1001off = FeedbackEventMessage.parse(msg1001off, now);

    SensorBean expResult1001on = new SensorBean(65, 1001, 1, 0, 100, now);
    SensorBean expResult1001off = new SensorBean(65, 1001, 0, 1, 100, now);

    //System.out.println(result1001off.toLogString());
    assertEquals(expResult1001on, result1001on);
    assertEquals(expResult1001off, result1001off);
  }

  @Test
  public void test65_bus2() {
    System.out.println("65-bus2");
    Date now = new Date();

    CanMessage msg2001on = CanMessage.parse("0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x07 0xd1 0x00 0x01 0xdc 0xf0");
    CanMessage msg2001off = CanMessage.parse("0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x07 0xd1 0x01 0x00 0x00 0xb4");

    SensorBean result2001on = FeedbackEventMessage.parse(msg2001on, now);
    SensorBean result2001off = FeedbackEventMessage.parse(msg2001off, now);

    SensorBean expResult2001on = new SensorBean(65, 2001, 1, 0, 565600, now);
    SensorBean expResult2001off = new SensorBean(65, 2001, 0, 1, 1800, now);

    //System.out.println(result2001on.toLogString());
    assertEquals(expResult2001on, result2001on);
    assertEquals(expResult2001off, result2001off);
  }

  @Test
  public void test65_bus3() {
    System.out.println("65-bus3");
    Date now = new Date();

    //53385c41 -> 1396202561 uid link S88
    CanMessage msg3001on = CanMessageFactory.sensorEventMessage(65, 3001, 1, 0, 10000, 1396202561);
    CanMessage msg3001off = CanMessageFactory.sensorEventMessage(65, 3001, 0, 1, 400, 1396202561);

    SensorBean result3001on = FeedbackEventMessage.parse(msg3001on, now);
    SensorBean result3001off = FeedbackEventMessage.parse(msg3001off, now);

    SensorBean expResult3001on = new SensorBean(65, 3001, 1, 0, 10000, now);
    SensorBean expResult3001off = new SensorBean(65, 3001, 0, 1, 400, now);

    //System.out.println(result3001on.toLogString());
    assertEquals(expResult3001on, result3001on);
    assertEquals(expResult3001off, result3001off);
  }

}
