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

import java.util.ArrayList;
import java.util.List;
import jcs.commandStation.entities.DeviceBean;
import jcs.commandStation.entities.InfoBean;
import jcs.entities.AccessoryBean;
import jcs.entities.ChannelBean;
import jcs.entities.CommandStationBean;
import jcs.entities.FeedbackModuleBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.util.NetworkUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author fransjacobs
 */
public class EsuEcosCommandStationImplTest {

  private final PersistenceTestHelper testHelper;

  private CommandStationBean commandStationBean;

  public EsuEcosCommandStationImplTest() {
    System.setProperty("message.debug", "true");
    System.setProperty("persistenceService", "jcs.persistence.H2PersistenceService");

    testHelper = PersistenceTestHelper.getInstance();
  }

  private CommandStationBean getEcosAsDefaultCommandStationBean() {
    CommandStationBean ecosCommandStationBean = new CommandStationBean();
    ecosCommandStationBean.setId("esu-ecos");
    PersistenceFactory.getService().changeDefaultCommandStation(ecosCommandStationBean);
    ecosCommandStationBean = PersistenceFactory.getService().getDefaultCommandStation();

    ecosCommandStationBean.setIpAddress(NetworkUtil.getIPv4HostAddress().getHostAddress());
    PersistenceFactory.getService().persist(ecosCommandStationBean);
    return ecosCommandStationBean;
  }

  @BeforeEach
  public void setUp() {
    testHelper.runTestDataInsertScript("ecos_test_data.sql");
    this.commandStationBean = getEcosAsDefaultCommandStationBean();
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test of connect method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testConnect() {
    System.out.println("connect");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    boolean expResult = true;
    boolean result = instance.connect();
    assertEquals(expResult, result);
  }

  /**
   * Test of disconnect method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testDisconnect() {
    System.out.println("disconnect");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    boolean connected = instance.connect();
    assertTrue(connected);
    assertTrue(instance.isConnected());
    instance.disconnect();
    assertFalse(instance.isConnected());
  }

  /**
   * Test of getCommandStationInfo method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testGetCommandStationInfo() {
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
    expResult.setHostname(NetworkUtil.getIPv4HostAddress().getHostAddress());

    InfoBean result = instance.getCommandStationInfo();
    assertEquals(expResult, result);
  }

  /**
   * Test of getDevice method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testGetDevice() {
    System.out.println("getDevice");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();
    DeviceBean expResult = new DeviceBean();
    expResult.setName("ECoS-Virtual");
    expResult.setVersion("1.3");
    expResult.setTypeName("ECoS");
    expResult.setSerial("0x00000000");

    DeviceBean result = instance.getDevice();
    assertEquals(expResult, result);
  }

  /**
   * Test of getDevices method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testGetDevices() {
    System.out.println("getDevices");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();
    DeviceBean db = new DeviceBean();
    db.setName("ECoS-Virtual");
    db.setVersion("1.3");
    db.setTypeName("ECoS");
    db.setSerial("0x00000000");

    List<DeviceBean> expResult = new ArrayList<>();
    expResult.add(db);

    List<DeviceBean> result = instance.getDevices();
    assertEquals(expResult, result);
  }

  /**
   * Test of getIp method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testGetIp() {
    System.out.println("getIp");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    assertNull(instance.getIp());
    instance.connect();
    String expResult = NetworkUtil.getIPv4HostAddress().getHostAddress();
    String result = instance.getIp();
    assertEquals(expResult, result);
  }

  /**
   * Test of isPower method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testIsPower() {
    System.out.println("isPower");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    assertFalse(instance.isPower());
    instance.connect();
    assertTrue(instance.isPower());
  }

  /**
   * Test of power method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testPower() {
    System.out.println("power");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();
    boolean result = instance.power(true);
    assertTrue(result);
    result = instance.power(false);
    assertFalse(result);
  }

  /**
   * Test of changeDirection method, of class EsuEcosCommandStationImpl.
   */
  //@Test
  public void testChangeDirection() {
    System.out.println("changeDirection");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();

    int locUid = 0;
    LocomotiveBean.Direction direction = null;
    instance.changeDirection(locUid, direction);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of changeVelocity method, of class EsuEcosCommandStationImpl.
   */
  //@Test
  public void testChangeVelocity() {
    System.out.println("changeVelocity");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();

    int locUid = 0;
    int speed = 0;
    LocomotiveBean.Direction direction = null;
    instance.changeVelocity(locUid, speed, direction);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of changeFunctionValue method, of class EsuEcosCommandStationImpl.
   */
  //@Test
  public void testChangeFunctionValue() {
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

  /**
   * Test of getLocomotives method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testGetLocomotives() {
    System.out.println("getLocomotives");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    int expResult = 12;
    instance.connect();
    List<LocomotiveBean> result = instance.getLocomotives();
    assertEquals(expResult, result.size());
  }

  /**
   * Test of isSupportTrackMeasurements method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testIsSupportTrackMeasurements() {
    System.out.println("isSupportTrackMeasurements");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();
    assertFalse(instance.isSupportTrackMeasurements());
  }

  /**
   * Test of switchAccessory method, of class EsuEcosCommandStationImpl.
   */
  //@Test
  public void testSwitchAccessory_Integer_AccessoryBeanAccessoryValue() {
    System.out.println("switchAccessory");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();

    Integer address = null;
    AccessoryBean.AccessoryValue value = null;
    instance.switchAccessory(address, value);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of switchAccessory method, of class EsuEcosCommandStationImpl.
   */
  //@Test
  public void testSwitchAccessory_3args() {
    System.out.println("switchAccessory");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();

    Integer address = null;
    AccessoryBean.AccessoryValue value = null;
    Integer switchTime = null;
    instance.switchAccessory(address, value, switchTime);
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
    System.out.println("getAccessories");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();

    int expResult = 7;
    List<AccessoryBean> result = instance.getAccessories();
    assertEquals(expResult, result.size());
  }

  /**
   * Test of getFeedbackDevice method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testGetFeedbackDevice() {
    System.out.println("getFeedbackDevice");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();

    DeviceBean expResult = new DeviceBean();
    expResult.setArticleNumber("ECoS-Virtual");
    expResult.setIdentifier("0x0");
    expResult.getBusLength(1);
    expResult.setVersion("4.2.13");
    expResult.setSerial("0x00000000");
    expResult.setTypeName("Link S88");

    ChannelBean cb = new ChannelBean();
    cb.setName(DeviceBean.BUS0);
    cb.setNumber(0);

    expResult.addSensorBus(0, cb);

    DeviceBean result = instance.getFeedbackDevice();
    assertEquals(expResult, result);
  }

  /**
   * Test of getFeedbackModules method, of class EsuEcosCommandStationImpl.
   */
  @Test
  public void testGetFeedbackModules() {
    System.out.println("getFeedbackModules");
    EsuEcosCommandStationImpl instance = new EsuEcosCommandStationImpl(commandStationBean);
    instance.connect();
    int expResult = 1;
    List<FeedbackModuleBean> result = instance.getFeedbackModules();
    assertEquals(expResult, result.size());
  }

}
