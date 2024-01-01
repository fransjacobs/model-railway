/*
 * Copyright 2023 frans.
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
package jcs.entities;

import jcs.commandStation.marklin.cs.can.CanMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author frans
 */
public class DeviceTest {

  private CanMessage updateMessage;
  private CanMessage updateMessage1;
  private CanMessage message;
  private CanMessage response;

  public DeviceTest() {
  }

  @Before
  public void setUp() {
    updateMessage = new CanMessage(new byte[]{0x00, 0x3a, (byte) 0xcb, 0x13, 0x05, 0x43, 0x53, (byte) 0x9a, 0x40, 0x00, 0x00, 0x00, 0x00});
    updateMessage.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x01, 0x08, 0x04, 0x02, 0x00, 0x00, 0x00, 0x00, 0x34, 0x20}));
    updateMessage.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x02, 0x08, 0x36, 0x30, 0x32, 0x31, 0x34, 0x00, 0x00, 0x00}));
    updateMessage.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x03, 0x08, 0x43, 0x65, 0x6e, 0x74, 0x72, 0x61, 0x6c, 0x20}));
    updateMessage.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x04, 0x08, 0x53, 0x74, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x20}));
    updateMessage.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x05, 0x08, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
    updateMessage.addResponse(new CanMessage(new byte[]{0x00, 0x3b, (byte) 0xcb, 0x13, 0x05, 0x43, 0x53, (byte) 0x9a, 0x40, 0x00, 0x00, 0x00, 0x00}));

    updateMessage1 = new CanMessage(new byte[]{0x00, 0x3a, (byte) 0xcb, 0x13, 0x05, 0x43, 0x53, (byte) 0x9a, 0x40, 0x00, 0x00, 0x00, 0x00});
    updateMessage1.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x01, 0x08, 0x04, 0x02, 0x00, 0x00, 0x00, 0x00, 0x34, 0x20}));
    updateMessage1.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x02, 0x08, 0x36, 0x30, 0x32, 0x31, 0x34, 0x00, 0x00, 0x00}));
    updateMessage1.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x03, 0x08, 0x43, 0x65, 0x6e, 0x74, 0x72, 0x61, 0x6c, 0x20}));
    updateMessage1.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x04, 0x08, 0x53, 0x74, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x20}));
    updateMessage1.addResponse(new CanMessage(new byte[]{0x00, 0x3b, 0x03, 0x05, 0x08, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
    updateMessage1.addResponse(new CanMessage(new byte[]{0x00, 0x3b, (byte) 0xcb, 0x13, 0x05, 0x43, 0x53, (byte) 0x9a, 0x40, 0x00, 0x00, 0x00, 0x00}));

    message = new CanMessage(new byte[]{0x00, 0x30, 0x07, 0x69, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
    message.addResponse(new CanMessage(new byte[]{0x00, 0x31, (byte) 0xcb, 0x13, 0x08, 0x43, 0x53, (byte) 0x9a, 0x40, 0x03, 0x55, 0x00, 0x00}));

    response = new CanMessage(new byte[]{0x00, 0x31, 0x07, 0x69, 0x08, 0x43, 0x53, (byte) 0x9a, 0x41, 0x04, 0x03, (byte) 0xff, (byte) 0xff});

  }

  @After
  public void tearDown() {
  }

  @Test
  public void testUpdateFromMessage() {
    System.out.println("updateFromMessage");
    DeviceBean instance = new DeviceBean();
    instance.updateFromMessage(updateMessage);

    assertEquals("60214", instance.getArticleNumber());
    assertEquals("Central Station 2", instance.getName());
    assertEquals("13344", instance.getSerial());
    //assertEquals(4, instance.getMeasureChannels());
    //assertEquals(2, instance.getConfigChannels());
  }

  @Test
  public void testBuildFromMessage() {
    System.out.println("buildFromMessage");
    DeviceBean instance = new DeviceBean(message);
    System.out.println(instance);
    assertEquals("0x43539a40", instance.getUid());
    assertEquals((Integer) 1129552448, instance.getUidAsInt());
    assertEquals("853", instance.getVersion());
    assertEquals((Integer) 0, instance.getIdentifierAsInt());
  }

  @Test
  public void testBuildFromResponse() {
    System.out.println("buildFromResponse");
    DeviceBean instance = new DeviceBean(response);
  
    System.out.println(instance);

    assertEquals((Integer)1129552449, instance.getUidAsInt());
    assertEquals("1027", instance.getVersion());
    assertEquals((Integer) 65535, instance.getIdentifierAsInt());
  }

}
