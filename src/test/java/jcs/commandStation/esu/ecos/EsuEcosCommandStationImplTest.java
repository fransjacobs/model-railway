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

import java.util.List;
import jcs.commandStation.entities.InfoBean;
import jcs.entities.AccessoryBean;
import jcs.entities.CommandStationBean;
import jcs.commandStation.entities.FeedbackModule;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.util.NetworkUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class EsuEcosCommandStationImplTest {

  private boolean skip = false;
  private final PersistenceTestHelper testHelper;

  private CommandStationBean commandStationBean;

  public EsuEcosCommandStationImplTest() {
    System.setProperty("message.debug", "true");
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
    System.setProperty("connection.always.virtual", "true");
    testHelper = PersistenceTestHelper.getInstance();

  }

  @BeforeEach
  public void setUp() {
    testHelper.runTestDataInsertScript("ecos_test_data.sql");
    Logger.info("ECoS Testdate initialized");

    this.commandStationBean = getEcosAsDefaultCommandStationBean();

    if (this.commandStationBean == null) {
      //The workflow on GitHup does gives back a Null as command station for reasons yet unknown for me...
      Logger.error("Can't obtain a Command Station! Skipping tests...");
      this.skip = true;
    }
  }

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

  /**
   * Test of connect method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testConnect() {
    if (!skip) {
      System.out.println("connect");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      boolean expResult = true;
      boolean result = instance.connect();
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
      instance.connect();
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
      
//expected: <InfoBean{softwareVersion=4.2.13, hardwareVersion=1.3, serialNumber=0x00000000, productName=null, articleNumber=Virtual, hostname=192.168.1.231, gfpUid=null, guiUid=null}> 
// but was: <InfoBean{softwareVersion=4.2.13, hardwareVersion=1.3, serialNumber=0x00000000, productName=ECoS-Virtual, articleNumber=Virtual, hostname=192.168.1.231, gfpUid=null, guiUid=null}>
      
      
      assertEquals(expResult, result);
    }
  }

  /**
   * Test of getIp method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testGetIp() {
    if (!skip) {
      System.out.println("getIp");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      assertNull(instance.getIp());
      instance.connect();
      String expResult = NetworkUtil.getIPv4HostAddress().getHostAddress();
      String result = instance.getIp();
      assertEquals(expResult, result);
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
      assertFalse(instance.isPower());
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
  //@Test
  public void testChangeDirection() {
    if (!skip) {
      System.out.println("changeDirection");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      instance.connect();

      int locUid = 0;
      LocomotiveBean.Direction direction = null;
      instance.changeDirection(locUid, direction);
    }
  }

  /**
   * Test of changeVelocity method, of class EsuEcosCommandStationImpl.
   */
  //@Test
  public void testChangeVelocity() {
    if (!skip) {

      System.out.println("changeVelocity");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      instance.connect();

      int locUid = 0;
      int speed = 0;
      LocomotiveBean.Direction direction = null;
      instance.changeVelocity(locUid, speed, direction);
    }
  }

  /**
   * Test of changeFunctionValue method, of class EsuEcosCommandStationImpl.
   */
  //@Test
  public void testChangeFunctionValue() {
    if (!skip) {

      System.out.println("changeFunctionValue");
      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
      instance.connect();

      int locUid = 0;
      int functionNumber = 0;
      boolean flag = false;
      instance.changeFunctionValue(locUid, functionNumber, flag);
      // TODO review the generated test code and remove the default call to fail.
      fail("The test case is a prototype.");
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
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
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
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
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
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
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
   * Test of getFeedbackDevice method, of class EsuEcosCommandStationImpl.
   */
//  @Test
//  public void testGetFeedbackDevice() {
//    if (!skip) {
//      System.out.println("getFeedbackDevice");
//      EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
//      instance.connect();
//
//      DeviceBean expResult = new DeviceBean();
//      expResult.setArticleNumber("ECoS-Virtual");
//      expResult.setIdentifier("0x0");
//      expResult.getBusLength(1);
//      expResult.setVersion("4.2.13");
//      expResult.setSerial("0x00000000");
//      expResult.setTypeName("Link S88");
//
//      ChannelBean cb = new ChannelBean();
//      cb.setName(DeviceBean.BUS0);
//      cb.setNumber(0);
//
//      expResult.addSensorBus(0, cb);
//
//      DeviceBean result = instance.getFeedbackDevice();
//      assertEquals(expResult, result);
//    }
//  }
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
