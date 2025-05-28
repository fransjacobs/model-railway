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
package jcs.commandStation.marklin.parser;

import jcs.commandStation.marklin.cs.can.CanMessage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
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
    byte[] data = new byte[]{5, 6, 7, 8, 9, 10, 11, 12};
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
    CanMessage instance = new CanMessage(0, 0, 0, 0, new byte[CanMessage.DATA_SIZE]);

    int expResult = 13;
    int result = instance.getLength();
    assertEquals(expResult, result);

    byte[] expMessage = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
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

    byte[] expResult = new byte[]{0, 1, 0, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12};

    byte[] result = instance.getMessage();
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of get- setPriority method, of class CanMessage.
   */
  @Test
  public void testGetSetPriority() {
    System.out.println("getSetPriority");
    int priority = 4;
    CanMessage instance = new CanMessage(priority, 0, 0, 0, new byte[CanMessage.DATA_SIZE]);

    assertEquals(4, instance.getPriority());

    byte[] expMessage = new byte[]{(byte) 0x04, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    byte[] msg = instance.getMessage();
    Assert.assertArrayEquals(expMessage, msg);
  }

  /**
   * Test of get- setCommand method, of class CanMessage.
   */
  @Test
  public void testGetSetCommand() {
    System.out.println("getSetCommand");
    int command = 7;
    CanMessage instance = new CanMessage(0, command, 0, 0, new byte[CanMessage.DATA_SIZE]);

    assertEquals(command, instance.getCommand());

    byte[] expMessage = new byte[]{0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    Assert.assertArrayEquals(expMessage, instance.getMessage());
  }

  /**
   * Test of set- getHash method, of class CanMessage.
   */
  @Test
  public void testGetSetHash() {
    System.out.println("getSetHash");
    CanMessage instance = new CanMessage(0, 0, new byte[]{67, 23}, 0, new byte[CanMessage.DATA_SIZE]);
    byte[] expResult = new byte[]{67, 23};

    System.out.println("getSetHash: " + instance);

    byte[] result = instance.getHash();
    assertArrayEquals(expResult, result);

    byte[] expMessage = new byte[]{0, 0, 67, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    Assert.assertArrayEquals(expMessage, instance.getMessage());
  }

  /**
   * Test of set- getDlc method, of class CanMessage.
   */
  @Test
  public void testGetSetDlc() {
    System.out.println("getSetDlc");
    CanMessage instance = new CanMessage(0, 0, 0, 8, new byte[CanMessage.DATA_SIZE]);
    int expResult = 8;

    int result = instance.getDlc();
    assertEquals(expResult, result);

    assertEquals(expResult, instance.getDlc());

    byte[] expMessage = new byte[]{0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0};
    Assert.assertArrayEquals(expMessage, instance.getMessage());
  }

  /**
   * Test of set- getData method, of class CanMessage.
   */
  @Test
  public void testGetSetData() {
    System.out.println("getSetData");
    byte[] expResult = new byte[]{5, 6, 7, 8, 9, 10, 11, 12};
    CanMessage instance = new CanMessage(0, 0, 0, 0, expResult);

    byte[] result = instance.getData();
    assertArrayEquals(expResult, result);

    byte[] expMessage = new byte[]{0, 0, 0, 0, 0, 5, 6, 7, 8, 9, 10, 11, 12};

    Assert.assertArrayEquals(expMessage, instance.getMessage());
  }

  @Test
  public void testgetBytesCreateMessage() {
    System.out.println("getBytesCreateMessage");

    byte[] bytes = new byte[]{0x00, 0x31, 0x47, 0x11, 0x08, 0x4f, 0x59, 0x10, (byte) 0xdf, 0x01, 0x04, (byte) 0xee, (byte) 0xee};

    CanMessage instance = new CanMessage(bytes);

    System.out.println("createMessage: " + instance);

    byte[] expMessage = new byte[]{(byte) 0x00, (byte) 0x31, (byte) 0x47, (byte) 0x11, (byte) 0x08, (byte) 0x4f, (byte) 0x59, (byte) 0x10, (byte) 0xdf, (byte) 0x01, (byte) 0x04, (byte) 0xee, (byte) 0xee};
    Assert.assertArrayEquals(expMessage, instance.getMessage());

    assertArrayEquals(bytes, instance.getMessage());
  }

  @Test
  public void testToString() {
    System.out.println("toString");
    CanMessage instance = new CanMessage(new byte[]{0x00, 0x16, 0x47, 0x11, 0x06, 0x00, 0x00, 0x30, 0x00, 0x01, 0x01, 0x00, 0x00});
    String expResult = "0x00 0x16 0x47 0x11 0x06 0x00 0x00 0x30 0x00 0x01 0x01 0x00 0x00";
    System.out.println("toString: " + instance);

    String result = instance.toString();
    assertEquals(expResult, result);
  }

  @Test
  public void testIsResponse() {
    System.out.println("isResponse");
    CanMessage tx = new CanMessage(new byte[]{0x00, 0x00, 0x03, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
    CanMessage rx = new CanMessage(new byte[]{0x00, 0x01, (byte)0xcb, 0x13, 0x05, 0x43, 0x53, (byte)0x9a, 0x40, 0x01, 0x00, 0x00, 0x00});
    tx.addResponse(rx);

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
    CanMessage instance = new CanMessage(new byte[]{(byte) 0x00, (byte) 0x01, (byte) 0xcb, (byte) 0x05, (byte) 0x05, (byte) 0x43, (byte) 0x53, (byte) 0x9a, (byte) 0x40, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00});
    byte[] expResult = new byte[]{(byte) 0x43, (byte) 0x53, (byte) 0x9a, (byte) 0x40};
    byte[] result = instance.getDeviceUidFromMessage();
    Assert.assertArrayEquals(expResult, result);
  }

  @Test
  public void testHasValidResponse() {
    System.out.println("hasValidResponse");
    byte[] request = new byte[]{0x00, 0x16, (byte)0xcb, 0x13, 0x06, 0x00, 0x00, 0x30, 0x01, 0x00, 0x00, 0x00, 0x00};
    byte[] response = new byte[]{0x00, 0x17, (byte)0xcb, 0x13, 0x06, 0x00, 0x00, 0x30, 0x01, 0x00, 0x00, 0x00, 0x00};

    CanMessage instance = new CanMessage(request);
    assertFalse(instance.isResponseMessage());
    assertFalse(instance.hasValidResponse());

    CanMessage reply = new CanMessage(response);
    assertTrue(reply.isResponseMessage());
    assertFalse(reply.hasValidResponse());

    instance.addResponse(reply);

    assertTrue(instance.hasValidResponse());
  }

  @Test
  public void testIsResponseFor() {
    System.out.println("isResponseFor");

    CanMessage tx = new CanMessage(new byte[]{0x00, 0x3a, (byte)0xcb, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

    CanMessage rx = new CanMessage(new byte[]{0x00, 0x3b, (byte)0xcb, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

    boolean result = rx.isResponseFor(tx);
    assertTrue(result);

    result = tx.isResponseFor(tx);

    assertFalse(result);
  }

  @Test
  public void testIsResponseComplete() {
    System.out.println("issResponseComplete");

    CanMessage tx = new CanMessage(new byte[]{0x00, 0x00, 0x37, 0x7f, 0x04, 0x63, 0x73, 0x45, (byte)0x8c, 0x00, 0x00, 0x00, 0x00});
    CanMessage rx1 = new CanMessage(new byte[]{0x00, 0x01, 0x03, 0x26, 0x05, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00});
    CanMessage rx2 = new CanMessage(new byte[]{0x00, 0x01, 0x03, 0x26, 0x05, 0x63, 0x73, 0x45, (byte)0x8c, 0x01, 0x00, 0x00, 0x00});
    
    tx.addResponse(rx1);
    
    boolean result = tx.isResponseComplete();
    assertTrue(result);
    tx.addResponse(rx2);

    result = tx.isResponseComplete();
    
    assertTrue(result);

    //assertFalse(result);
  }
  
  
  
  //@Test
  public void testGenerateHashInt() {
    System.out.println("generateHashInt");
    byte[] gfpUid = new byte[]{(byte) 0x63, (byte) 0x73, (byte) 0x45, (byte) 0x8c};

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
