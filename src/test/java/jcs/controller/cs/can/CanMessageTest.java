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
package jcs.controller.cs.can;

import jcs.util.ByteUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Frans Jacobs <frans.jacobs@gmail.com>
 */
public class CanMessageTest {

  private CanMessage message;

  public CanMessageTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
    int[] data = new int[]{5, 6, 7, 8, 9, 10, 11, 12};
    message = new CanMessage(0, 1, 2, 4, data);
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getLength method, of class CanMessage.
   */
  @Test
  public void testGetLength() {
    System.out.println("getLength");
    CanMessage instance = new CanMessage(0, 0, 0, 0, CanMessage.getEmptyData());

    int expResult = 13;
    int result = instance.getLength();
    assertEquals(expResult, result);
    int[] expMessage = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    Assert.assertArrayEquals(expMessage, instance.getMessage());
  }

  /**
   * Test of getMessage method, of class CanMessage.
   */
  @Test
  public void testGetMessage() {
    System.out.println("getMessage");
    CanMessage instance = this.message;

    System.out.println("getMessage: " + instance);

    int[] expResult = new int[]{0, 1, 0, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12};

    int[] result = instance.getMessage();
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of get- setPriority method, of class CanMessage.
   */
  @Test
  public void testGetSetPriority() {
    System.out.println("getSetPriority");
    int priority = 4;
    CanMessage instance = new CanMessage(priority, 0, 0, 0, CanMessage.getEmptyData());

    int[] expMessage = new int[]{4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    Assert.assertArrayEquals(expMessage, instance.getMessage());

    assertEquals(4, instance.getPriority());
  }

  /**
   * Test of get- setCommand method, of class CanMessage.
   */
  @Test
  public void testGetSetCommand() {
    System.out.println("getSetCommand");
    int command = 7;
    CanMessage instance = new CanMessage(0, command, 0, 0, CanMessage.getEmptyData());

    assertEquals(command, instance.getCommand());

    int[] expMessage = new int[]{0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    Assert.assertArrayEquals(expMessage, instance.getMessage());
  }

  /**
   * Test of set- getHash method, of class CanMessage.
   */
  @Test
  public void testGetSetHash() {
    System.out.println("getSetHash");
    CanMessage instance = new CanMessage(0, 0, new int[]{67, 23}, 0, CanMessage.getEmptyData());
    int[] expResult = new int[]{67, 23};

    System.out.println("getSetHash: " + instance);

    int[] result = instance.getHash();
    assertArrayEquals(expResult, result);

    int[] expMessage = new int[]{0, 0, 67, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    Assert.assertArrayEquals(expMessage, instance.getMessage());
  }

  /**
   * Test of set- getDlc method, of class CanMessage.
   */
  @Test
  public void testGetSetDlc() {
    System.out.println("getSetDlc");
    CanMessage instance = new CanMessage(0, 0, 0, 8, CanMessage.getEmptyData());
    int expResult = 8;

    int result = instance.getDlc();
    assertEquals(expResult, result);

    assertEquals(expResult, instance.getDlc());

    int[] expMessage = new int[]{0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0};
    Assert.assertArrayEquals(expMessage, instance.getMessage());
  }

  /**
   * Test of set- getData method, of class CanMessage.
   */
  @Test
  public void testGetSetData() {
    System.out.println("getSetData");
    int[] expResult = new int[]{5, 6, 7, 8, 9, 10, 11, 12};
    CanMessage instance = new CanMessage(0, 0, 0, 0, expResult);

    System.out.println("getSetData: " + instance);

    int[] result = instance.getData();
    assertArrayEquals(expResult, result);

    int[] expMessage = new int[]{0, 0, 0, 0, 0, 5, 6, 7, 8, 9, 10, 11, 12};
    Assert.assertArrayEquals(expMessage, instance.getMessage());
  }

  @Test
  public void testgetBytesCreateMessage() {
    System.out.println("getBytesCreateMessage");

    byte[] bytes = new byte[]{0x00, 0x31, 0x47, 0x11, 0x08, 0x4f, 0x59, 0x10, (byte) 0xdf, 0x01, 0x04, (byte) 0xee, (byte) 0xee};

    CanMessage instance = new CanMessage(bytes);

    System.out.println("createMessage: " + instance);

    int[] expMessage = new int[]{0x00, 0x31, 0x47, 0x11, 0x08, 0x4f, 0x59, 0x10, 0xdf, 0x01, 0x04, 0xee, 0xee};
    Assert.assertArrayEquals(expMessage, instance.getMessage());

    assertArrayEquals(bytes, instance.getBytes());
  }

  @Test
  public void testToString() {
    System.out.println("toString");
    CanMessage instance = new CanMessage(ByteUtil.toByteArray(new int[]{0x00, 0x16, 0x47, 0x11, 0x06, 0x00, 0x00, 0x30, 0x00, 0x01, 0x01, 0x00, 0x00}));
    String expResult = "0x00 0x16 0x47 0x11 0x06 0x00 0x00 0x30 0x00 0x01 0x01 0x00 0x00";
    System.out.println("toString: " + instance);

    String result = instance.toString();
    assertEquals(expResult, result);
  }

  //0x00 0x00 0x03 0x00 0x04 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 
  //0x00 0x01 0xcb 0x13 0x05 0x43 0x53 0x9a 0x40 0x01 0x00 0x00 0x00
  @Test
  public void testIsResponse() {
    System.out.println("isResponse");
    CanMessage tx = new CanMessage(ByteUtil.toByteArray(new int[]{0x00, 0x00, 0x03, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
    CanMessage rx = new CanMessage(ByteUtil.toByteArray(new int[]{0x00, 0x01, 0xcb, 0x13, 0x05, 0x43, 0x53, 0x9a, 0x40, 0x01, 0x00, 0x00, 0x00}));
    tx.addResponse(rx);

    boolean expResult = false;
    boolean result = tx.isResponseMessage();
    assertFalse(tx.isResponseMessage());

    CanMessage resp = tx.getResponse();

    assertTrue(resp.isResponseMessage());

    assertTrue(resp.isResponseFor(tx));

    assertTrue(resp.isDeviceUidValid());

  }

  /**
   * Test of getUid method, of class CanMessage.
   */
  @Test
  public void testGetUid() {
    System.out.println("getUid");
    CanMessage instance = new CanMessage(ByteUtil.toByteArray(new int[]{0x00, 0x01, 0xcb, 0x05, 0x05, 0x43, 0x53, 0x9a, 0x40, 0x01, 0x00, 0x00, 0x00}));
    int[] expResult = new int[]{0x43, 0x53, 0x9a, 0x40};
    int[] result = instance.getDeviceUidFromMessage();
    Assert.assertArrayEquals(expResult, result);
  }

  @Test
  public void testHasValidResponse() {
    System.out.println("hasValidResponse");
    int[] request = new int[]{0x00, 0x16, 0xcb, 0x13, 0x06, 0x00, 0x00, 0x30, 0x01, 0x00, 0x00, 0x00, 0x00};
    int[] response = new int[]{0x00, 0x17, 0xcb, 0x13, 0x06, 0x00, 0x00, 0x30, 0x01, 0x00, 0x00, 0x00, 0x00};

    CanMessage instance = new CanMessage(ByteUtil.toByteArray(request));
    assertFalse(instance.isResponseMessage());
    assertFalse(instance.hasValidResponse());

    CanMessage reply = new CanMessage(ByteUtil.toByteArray(response));
    assertTrue(reply.isResponseMessage());
    assertFalse(reply.hasValidResponse());

    instance.addResponse(reply);

    assertTrue(instance.hasValidResponse());
  }

  @Test
  public void testIsResponseFor() {
    System.out.println("isResponseFor");

    CanMessage tx = new CanMessage(ByteUtil.toByteArray(new int[]{0x00, 0x3a, 0xcb, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));

    CanMessage rx = new CanMessage(ByteUtil.toByteArray(new int[]{0x00, 0x3b, 0xcb, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));

    boolean result = rx.isResponseFor(tx);
    assertTrue(result);

    result = tx.isResponseFor(tx);

    assertFalse(result);
  }

  @Test
  public void testIsAcknowlegeFor() {
    System.out.println("isAcknowlegeFor");

    CanMessage tx = new CanMessage(ByteUtil.toByteArray(new int[]{0x00, 0x3a, 0xcb, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
    CanMessage rx = new CanMessage(ByteUtil.toByteArray(new int[]{0x00, 0x3b, 0xcb, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));

    boolean result = rx.isAcknowledgeFor(tx);
    assertTrue(result);

    result = tx.isAcknowledgeFor(tx);

    assertFalse(result);
  }

//    public void testIsAcknowlegeFor() {
//        System.out.println("isAcknowlegeFor");
//
//        CanMessage tx = new CanMessage(new int[]{0x00, 0x3a, 0xcb, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
//        CanMessage rx = new CanMessage(new int[]{0x00, 0x3b, 0xcb, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
//
//        boolean result = rx.isAcknowledgeFor(tx);
//        assertTrue(result);
//
//        result = tx.isAcknowledgeFor(tx);
//
//        assertFalse(result);
//    }
  //TX: 0x00 0x3a 0xcb 0x13 0x05 0x43 0x53 0x9a 0x40 0x00 0x00 0x00 0x00 Expect Ack: true
  //RX: 0x00 0x3b 0x03 0x01 0x08 0x04 0x02 0x00 0x00 0x00 0x00 0x34 0x20 Ack? false
  //RX: 0x00 0x3b 0x03 0x02 0x08 0x36 0x30 0x32 0x31 0x34 0x00 0x00 0x00 Ack? false
  //RX: 0x00 0x3b 0x03 0x03 0x08 0x43 0x65 0x6e 0x74 0x72 0x61 0x6c 0x20 Ack? false
  //RX: 0x00 0x3b 0x03 0x04 0x08 0x53 0x74 0x61 0x74 0x69 0x6f 0x6e 0x20 Ack? false
  //RX: 0x00 0x3b 0x03 0x05 0x08 0x32 0x00 0x00 0x00 0x00 0x00 0x00 0x00 Ack? false
  //RX: 0x00 0x3b 0xcb 0x13 0x05 0x43 0x53 0x9a 0x40 0x00 0x00 0x00 0x00 Ack? false
  //@Test
  public void testGenerateHashInt() {
    System.out.println("generateHashInt");
    int[] gfpUid = new int[]{0x63, 0x73, 0x45, 0x8c};

//        hash: 0x03 0x26
//        gfp: 0x63 0x73 0x45 0x8c
//        
//        of   0x37 0x7e 
//        cs3  0x08 0x63 0x73 0x45 0x8d
//        
    //int hash = instance.getHashInt();
    //int hash = CanMessage.generateHashInt(gfpUid);
    //String hb = ByteUtil.toHexString(hash);
    //System.out.println("hash: " + hb);
    //assertEquals(hash, generatedHash);
  }

}
