/*
 * Copyright (C) 2020 Frans Jacobs <frans.jacobs@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package lan.wervel.jcs.controller.cs2.can;

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
        int[] exp = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        message = new CanMessage(exp);
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
        CanMessage instance = new CanMessage();
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

        int[] expResult = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

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
        CanMessage instance = new CanMessage();
        instance.setPriority(priority);

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
        CanMessage instance = new CanMessage();
        instance.setCommand(command);

        assertEquals(7, instance.getCommand());

        int[] expMessage = new int[]{0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Assert.assertArrayEquals(expMessage, instance.getMessage());
    }

    /**
     * Test of set- getHash method, of class CanMessage.
     */
    @Test
    public void testGetSetHash() {
        System.out.println("getSetHash");
        CanMessage instance = new CanMessage();
        int[] expResult = new int[]{67, 23};

        instance.setHash(new int[]{67, 23});

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
        CanMessage instance = new CanMessage();
        int expResult = 8;

        instance.setDlc(8);
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
        CanMessage instance = new CanMessage();
        int[] expResult = new int[]{5, 6, 7, 8, 9, 10, 11, 12};

        instance.setData(new int[]{5, 6, 7, 8, 9, 10, 11, 12});
        System.out.println("getSetData: " + instance);

        int[] result = instance.getData();
        assertArrayEquals(expResult, result);

        int[] expMessage = new int[]{0, 0, 0, 0, 0, 5, 6, 7, 8, 9, 10, 11, 12};
        Assert.assertArrayEquals(expMessage, instance.getMessage());

        instance.setPriority(0xaa);
        instance.setCommand(0xF8);
        instance.setDlc(7);

        System.out.println("getSetDatx: " + instance);

        byte[] bytes = new byte[]{(byte) 0xaa, (byte) 0xf8, 0x00, 0x00, 0x07, 0x05, 0x6, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c};

        assertArrayEquals(bytes, instance.getBytes());
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
        CanMessage instance = new CanMessage(new int[]{0x00, 0x16, 0x47, 0x11, 0x06, 0x00, 0x00, 0x30, 0x00, 0x01, 0x01, 0x00, 0x00});
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
        CanMessage tx = new CanMessage(new int[]{0x00, 0x00, 0x03, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        CanMessage rx = new CanMessage(new int[]{0x00, 0x01, 0xcb, 0x13, 0x05, 0x43, 0x53, 0x9a, 0x40, 0x01, 0x00, 0x00, 0x00});
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
        CanMessage instance = new CanMessage(new int[]{0x00, 0x01, 0xcb, 0x05, 0x05, 0x43, 0x53, 0x9a, 0x40, 0x01, 0x00, 0x00, 0x00});
        int[] expResult = new int[]{0x43, 0x53, 0x9a, 0x40};
        int[] result = instance.getDeviceUidFromMessage();
        Assert.assertArrayEquals(expResult, result);
    }

    @Test
    public void testHasValidResponse() {
        System.out.println("hasValidResponse");
        int[] request = new int[]{0x00, 0x16, 0xcb, 0x13, 0x06, 0x00, 0x00, 0x30, 0x01, 0x00, 0x00, 0x00, 0x00};
        int[] response = new int[]{0x00, 0x17, 0xcb, 0x13, 0x06, 0x00, 0x00, 0x30, 0x01, 0x00, 0x00, 0x00, 0x00};

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

        CanMessage tx = new CanMessage(new int[]{0x00, 0x3a, 0xcb, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        CanMessage rx = new CanMessage(new int[]{0x00, 0x3b, 0xcb, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

        boolean result = rx.isResponseFor(tx);
        assertTrue(result);

        result = tx.isResponseFor(tx);

        assertFalse(result);
    }
    
    @Test
    public void testIsAcknowlegeFor() {
        System.out.println("isAcknowlegeFor");

        CanMessage tx = new CanMessage(new int[]{0x00, 0x3a, 0xcb, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        CanMessage rx = new CanMessage(new int[]{0x00, 0x3b, 0xcb, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

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
    
    

    @Test
    public void testGenerateHash() {
        System.out.println("generateHash");
        CanMessage instance = new CanMessage(new int[]{0x00, 0x01, 0xcb, 0x13, 0x05, 0x43, 0x53, 0x9a, 0x40, 0x01, 0x00, 0x00, 0x00});

        int hash = instance.getHashInt();
        int generatedHash = instance.generateHashInt();

        int[] h = instance.generateHash();

        String hb = instance.toHexString(h);

        System.out.println("generateHash-> hash: " + hash + " generatedHash: " + generatedHash + " Hash Bytes: " + hb);

        assertEquals(hash, generatedHash);
    }

}
