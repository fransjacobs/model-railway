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
package jcs.commandStation.marklin.cs;

import jcs.JCS;
import jcs.entities.CommandStationBean;
import jcs.persistence.PersistenceFactory;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class MarklinCSTest {

  private static MarklinCentralStationImpl instance;
  private static boolean csAvailable = false;

  //controller.skip.init
  public MarklinCSTest() {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
    //JCS.getJcsCommandStation().disconnect();
    //When running in a batch the default command station could be different..
    //CommandStationBean marklinCs = PersistenceFactory.getService().getCommandStation("marklin.cs");
    //PersistenceFactory.getService().changeDefaultCommandStation(marklinCs);
    try {
      if (instance == null) {
        CommandStationBean csb = new CommandStationBean();
        csb.setId("marklin.cs");
        csb.setDescription("Marklin Central Station 2/3");
        csb.setShortName("CS");
        csb.setClassName("jcs.commandStation.marklin.cs.MarklinCentralStationImpl");
        csb.setConnectVia("NETWORK");
        csb.setDecoderControlSupport(true);
        csb.setAccessorySynchronizationSupport(true);
        csb.setFeedbackSupport(true);
        csb.setLocomotiveFunctionSynchronizationSupport(true);
        csb.setLocomotiveImageSynchronizationSupport(true);
        csb.setLocomotiveSynchronizationSupport(true);
        csb.setNetworkPort(15731);
        csb.setProtocols("DCC,MFX,MM");
        csb.setDefault(true);
        csb.setEnabled(true);
        csb.setVirtual(true);

        instance = new MarklinCentralStationImpl(csb, false);
        pause(200);
        csAvailable = false; //instance.connect();

        if (csAvailable) {
          instance.disconnect();
        } else {
          JCS.getJcsCommandStation().disconnect();
        }
      }
    } catch (Exception e) {
      Logger.warn("CS not available skipping tests");
    }
  }

  @Before
  public void setUp() {
    if (csAvailable) {
      pause(200);
    } else {
      Logger.warn("Skipping tests CS not available");
    }
  }

  @After
  public void tearDown() {
    //instance.disconnect();
  }

  /**
   * Test of connect method, of class MarklinCentralStationImpl.
   */
  //@Test
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
   * Test of getSystemStatus method, of class MarklinCentralStationImpl.
   */
  //@Test
  public void testPower() {
    if (csAvailable) {

      if (!instance.isConnected()) {
        return;
      }
      System.out.println("isPower");
      //The gfpUid should not be 0 when connected to a real CS..
      int csUid = instance.getCsUid();
      assertTrue(csUid != 0);

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
   * Test of getDeviceIp method, of class MarklinCentralStationImpl.
   */
  //@Test
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

  @AfterAll
  public static void setDefaultCommandStation() {
    JCS.getJcsCommandStation().disconnect();

    CommandStationBean virt = PersistenceFactory.getService().getCommandStation("virtual");
    //PersistenceFactory.getService().changeDefaultCommandStation(virt);
  }

}
