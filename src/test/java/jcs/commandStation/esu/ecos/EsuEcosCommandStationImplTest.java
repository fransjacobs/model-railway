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
package jcs.commandStation.esu.ecos;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import jcs.commandStation.entities.InfoBean;
import jcs.entities.AccessoryBean;
import jcs.entities.CommandStationBean;
import jcs.commandStation.entities.FeedbackModule;
import jcs.commandStation.esu.ecos.net.EcosConnectionFactory;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.tinylog.Logger;

/**
 * Use the virtual connection...
 */
public class EsuEcosCommandStationImplTest {

  private boolean skip = false;
  private final PersistenceTestHelper testHelper;

  private CommandStationBean commandStationBean;

  public EsuEcosCommandStationImplTest() {
    System.setProperty("message.debug", "true");
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
    System.setProperty("connection.always.virtual", "true");

    EcosConnectionFactory.getInstance();

    testHelper = PersistenceTestHelper.getInstance();

    testHelper.runTestDataInsertScript("ecos_test_data.sql");
    Logger.info("ECoS Testdata initialized");
    this.commandStationBean = getEcosAsDefaultCommandStationBean();
    commandStationBean.setVirtual(true);

    if (this.commandStationBean == null) {
      //The workflow on GitHup does gives back a Null as command station for reasons yet unknown for me...
      Logger.error("Can't obtain a Command Station! Skipping tests...");
      this.skip = true;
    }
  }

//  @BeforeEach
//  public void setUp() {
//    testHelper.runTestDataInsertScript("ecos_test_data.sql");
//    Logger.info("ECoS Testdata initialized");
//    this.commandStationBean = getEcosAsDefaultCommandStationBean();
//    commandStationBean.setVirtual(true);
//
//    if (this.commandStationBean == null) {
//      //The workflow on GitHup does gives back a Null as command station for reasons yet unknown for me...
//      Logger.error("Can't obtain a Command Station! Skipping tests...");
//      this.skip = true;
//    }
//  }
  @AfterEach
  public void tearDown() {
  }

  private CommandStationBean getEcosAsDefaultCommandStationBean() {

    CommandStationBean ecosCommandStationBean = PersistenceFactory.getService().getDefaultCommandStation();

    if (ecosCommandStationBean == null) {
      Logger.error("ESU ECoS Command Station is NULL!");
    }

    if (ecosCommandStationBean != null && !ecosCommandStationBean.isVirtual()) {
      Logger.error("ESU ECoS Command Station must be virtual for testing!");
    }

    if (ecosCommandStationBean != null) {
      ecosCommandStationBean.setIpAddress(NetworkUtil.getIPv4HostAddress().getHostAddress());
    }

    if (ecosCommandStationBean != null) {
      PersistenceFactory.getService().persist(ecosCommandStationBean);
    }

    return ecosCommandStationBean;
  }

  private void zleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Test of connect method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testConnect() {
    if (!skip) {
      System.out.println("connect");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      boolean expResult = true;

      long now = System.currentTimeMillis();
      long timeout = now + Math.max(1L, 10000);

      boolean result = instance.connect();
      while (now < timeout) {
        zleep(100);
        now = System.currentTimeMillis();
        result = instance.isConnected();
        if (result) {
          break;
        }
      }

      assertTrue(now < timeout);
      assertEquals(expResult, result);
    }
  }

  /**
   * Test of disconnect method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testDisconnect() {
    if (!skip) {
      System.out.println("disconnect");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      boolean connected = instance.connect();
      assertTrue(connected);
      assertTrue(instance.isConnected());
      instance.disconnect();
      assertFalse(instance.isConnected());
      assertFalse(EcosConnectionFactory.getInstance().isConnected());

      instance.connect();
      //TODO review en fix below
//      long now = System.currentTimeMillis();
//      long timeout = now + Math.max(1L, 10000);
//
//      boolean result = instance.connect();
//      while (now < timeout) {
//        zleep(100);
//        now = System.currentTimeMillis();
//        result = instance.isConnected();
//        if (result) {
//          break;
//        }
//      }
//
//      assertTrue(now < timeout);
//      assertTrue(result);
//      assertTrue(instance.isConnected());
//
    }
  }

  /**
   * Test of getCommandStationInfo method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testGetCommandStationInfo() {
    if (!skip) {
      System.out.println("getCommandStationInfo");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      boolean connected = instance.connect();
      assertTrue(connected);
      assertTrue(instance.isConnected());

      InfoBean expResult = new InfoBean(this.commandStationBean);
      expResult.setArticleNumber("Virtual-ECoS");
      expResult.setDescription("ECoS-Virtual");
      expResult.setArticleNumber("Virtual");
      expResult.setSerialNumber("0x00000000");
      expResult.setHardwareVersion("1.3");
      expResult.setSoftwareVersion("4.2.13");
      expResult.setProductName("ECoS-Virtual");
      expResult.setHostname(NetworkUtil.getIPv4HostAddress().getHostAddress());

      InfoBean result = instance.getCommandStationInfo();
      assertEquals(expResult, result);
    }
  }

  /**
   * Test of getIp method, of class EsuEcosCommandStationImpl.
   */
  //@Test
  public void testGetIp() {
    if (!skip) {
      System.out.println("getIp");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      instance.connect();
      String expResult;
      try {
        expResult = InetAddress.getLocalHost().getHostAddress();
        String result = instance.getIp();
        assertEquals(expResult, result);

      } catch (UnknownHostException ex) {
        Logger.error("Can't get the localhost address");
        fail("Can't get the localhost address");
      }

    }
  }

  /**
   * Test of isPower method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testIsPower() {
    if (!skip) {
      System.out.println("isPower");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      instance.connect();
      assertTrue(instance.isPower());
    }
  }

  /**
   * Test of power method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testPower() {
    if (!skip) {
      System.out.println("power");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      instance.connect();
      boolean result = instance.power(true);
      assertTrue(result);
      result = instance.power(false);
      assertFalse(result);
    }
  }

  /**
   * Test of changeDirection method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testChangeDirection() {
    if (!skip) {
      System.out.println("changeDirection");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      instance.connect();

      int locUid = 0;
      LocomotiveBean.Direction direction = LocomotiveBean.Direction.BACKWARDS;
      instance.changeDirection(locUid, direction);
      direction = LocomotiveBean.Direction.FORWARDS;
      instance.changeDirection(locUid, direction);

      //instance.
    }
  }

  /**
   * Test of changeVelocity method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testChangeVelocity() {
    if (!skip) {
      System.out.println("changeVelocity");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      instance.connect();

      int locUid = 0;
      int speed = 0;
      LocomotiveBean.Direction direction = LocomotiveBean.Direction.FORWARDS;;
      instance.changeVelocity(locUid, speed, direction);
    }
  }

  /**
   * Test of changeFunctionValue method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testChangeFunctionValue() {
    if (!skip) {

      System.out.println("changeFunctionValue");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      instance.connect();

      int locUid = 0;
      int functionNumber = 0;
      boolean flag = false;
      instance.changeFunctionValue(locUid, functionNumber, flag);
    }
  }

  /**
   * Test of getLocomotives method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testGetLocomotives() {
    if (!skip) {
      System.out.println("getLocomotives");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      int expResult = 12;
      instance.connect();
      List<LocomotiveBean> result = instance.getLocomotives();
      assertEquals(expResult, result.size());
    }
  }

  /**
   * Test of isSupportTrackMeasurements method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testIsSupportTrackMeasurements() {
    if (!skip) {
      System.out.println("isSupportTrackMeasurements");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      instance.connect();
      assertFalse(instance.isSupportTrackMeasurements());
    }
  }

  /**
   * Test of switchAccessory method, of class EsuEcosCommandStationImpl.
   */
  //@Test
  public void testSwitchAccessory_Integer_AccessoryBeanAccessoryValue() {
    System.out.println("switchAccessory");
    int switchTime = 200;
    String protocol = "mm";
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();

    Integer address = null;
    AccessoryBean.AccessoryValue value = null;
    instance.switchAccessory(address, protocol, value, switchTime);
  }

  /**
   * Test of switchAccessory method, of class EsuEcosCommandStationImpl.
   */
  //@Test
  public void testSwitchAccessory_3args() {
    System.out.println("switchAccessory");
    int switchTime = 200;
    String protocol = "dcc";
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();

    Integer address = null;
    AccessoryBean.AccessoryValue value = null;
    instance.switchAccessory(address, protocol, value, switchTime);
  }

  /**
   * Test of switchAccessory method, of class EsuEcosCommandStationImpl.
   */
  //@Test
  public void testSwitchAccessory_String_AccessoryBeanAccessoryValue() {
    System.out.println("switchAccessory");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();

    String id = "";
    AccessoryBean.AccessoryValue value = null;
    instance.switchAccessory(id, value);
  }

  /**
   * Test of getAccessories method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testGetAccessories() {
    if (!skip) {
      System.out.println("getAccessories");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      instance.connect();

      int expResult = 7;
      List<AccessoryBean> result = instance.getAccessories();
      assertEquals(expResult, result.size());
    }
  }

  /**
   * Test of getFeedbackModules method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testGetFeedbackModules() {
    if (!skip) {
      System.out.println("getFeedbackModules");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      instance.connect();
      int expResult = 1;
      List<FeedbackModule> result = instance.getFeedbackModules();
      assertEquals(expResult, result.size());
    }
  }

}
