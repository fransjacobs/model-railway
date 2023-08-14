/*
 * Copyright 2023 fransjacobs.
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
package jcs.controller.cs;

import java.awt.Image;
import java.util.List;
import jcs.entities.AccessoryBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.DecoderType;
import jcs.entities.enums.Direction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class MarklinCSTest {

  private static MarklinCS instance;
  private static boolean csAvailable = false;

  public MarklinCSTest() {
    try {
      if (instance == null) {
        instance = new MarklinCS();
        pause(500);
        csAvailable = instance.connect();

        if (csAvailable) {
          instance.disconnect();
        }
      }
    } catch (Exception e) {
      Logger.warn("CS 3 not available skipping tests");
    }
  }

  @Before
  public void setUp() {
    if (csAvailable) {
      pause(500);
    } else {
      Logger.warn("Skipping tests CS 3 not available");
    }
  }

  @After
  public void tearDown() {
    //instance.disconnect();
  }

  /**
   * Test of connect method, of class MarklinCS.
   */
  @Test
  public void testConnect() {
    if (csAvailable) {
      System.out.println("connect");
      boolean expResult = true;
      boolean result = instance.connect();
      assertEquals(expResult, result);
      result = instance.isConnected();
      assertEquals(expResult, result);

      // When really connected the UID of devices should not be 0
      assertTrue(instance.getCsUid() != 0);
      //assertTrue(instance.getGfpUid() != 0);
      //Be sure it is connected and powered on
      //assertTrue(instance.getLinkSxxUid() != 0);
    }
  }

  private void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      //ignore
    }
  }

  /**
   * Test of getSystemStatus method, of class MarklinCS.
   */
  @Test
  public void testPower() {
    if (csAvailable) {

      if (!instance.isConnected()) {
        return;
      }
      System.out.println("isPower");
      //The gfpUid should not be 0 when connected to a real CS..
      int gfpuid = instance.getGfpUid();
      assertTrue(gfpuid != 0);

      boolean expResult = instance.power(true);
      pause(500);
      boolean result = instance.isPower();
      assertEquals(expResult, result);

      pause(500);
      expResult = instance.power(false);
      pause(500);
      result = instance.isPower();
      assertEquals(expResult, result);
    }
  }

  /**
   * Test of getDeviceIp method, of class MarklinCS.
   */
  @Test
  public void testGetDeviceIp() {
    if (csAvailable) {
      if (!instance.isConnected()) {
        return;
      }
      System.out.println("getDeviceIp");
      String expResult = "192.168.1.180";
      String result = instance.getIp();
      assertEquals(expResult, result);
    }
  }

}
