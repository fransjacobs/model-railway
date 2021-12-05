/*
 * Copyright (C) 2019 frans.
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
package lan.wervel.jcs.trackservice.dao;

import lan.wervel.jcs.trackservice.dao.util.DAOTestHelper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lan.wervel.jcs.entities.SignalBean;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.SignalValue;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author frans
 */
public class SignalDAOTest {

  private final List<SignalBean> signals;

  public SignalDAOTest() {
    DAOTestHelper.setConnectProperties();
    DAOTestHelper.createNewDatabase();
    DAOTestHelper.insertTurnoutData();
    DAOTestHelper.insertSignalData();

    signals = new ArrayList<>();
  }

  @Before
  public void setUp() {
    SignalBean s3 = new SignalBean(3, "S 3", "home made", new BigDecimal(3), AccessoryValue.GREEN, null, 2, null, null, null, null);
    s3.setName("S 3");
    s3.setSwitchTime(200);
    signals.add(s3);

    SignalBean s4 = new SignalBean(4, "S 4", "3942", new BigDecimal(4), AccessoryValue.RED, null, 2, null, null, null, null);
    s4.setName("S 4");
    s4.setSwitchTime(200);
    signals.add(s4);

    SignalBean s5 = new SignalBean(5, "S 5", "3943", new BigDecimal(5), AccessoryValue.GREEN, null, 4, new BigDecimal(6), 6, AccessoryValue.RED, null);
    s5.setName("S 5");
    s5.setSwitchTime(200);
    signals.add(s5);

    SignalBean s6 = new SignalBean(7, "S 7", "dual", new BigDecimal(7), AccessoryValue.GREEN, null, 4, new BigDecimal(8), 8, AccessoryValue.GREEN, SignalValue.Hp2);
    s6.setName("S 7");
    s6.setSwitchTime(200);
    signals.add(s6);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testEquals() {
    System.out.println("equals");

    SignalBean exp = this.signals.get(0);

    SignalBean signal = new SignalBean(3, "S 3", "home made", new BigDecimal(3), AccessoryValue.GREEN, null, 2, null, null, null, SignalValue.Hp1);
    signal.setName("S 3");
    signal.setSwitchTime(200);

    System.out.println("exp.getValue(): " + exp.getValue() + " result.getValue(): " + signal.getValue());
    assertEquals(exp.getValue(), signal.getValue());
    assertEquals(exp.getName(), signal.getName());
    assertEquals(exp.getAddress(), signal.getAddress());
    assertEquals(exp.getSoacId(), signal.getSoacId());
    assertEquals(exp.getAddress2(), signal.getAddress2());
    assertEquals(exp.getId2(), signal.getId2());
    assertEquals(exp.getValue2(), signal.getValue2());
    assertEquals(exp.getSignalValue(), signal.getSignalValue());
    assertEquals(exp.getLightImages(), signal.getLightImages());
    assertEquals(exp.getSwitchTime(), signal.getSwitchTime());

    assertEquals(exp, signal);
  }

  @Test
  public void testEquals2() {
    System.out.println("equals2");

    SignalBean exp = this.signals.get(3);

    SignalBean signal = new SignalBean(7, "S 7", "dual", new BigDecimal(7), AccessoryValue.GREEN, null, 4, new BigDecimal(8), 8, AccessoryValue.GREEN, SignalValue.Hp2);
    signal.setName("S 7");
    signal.setSwitchTime(200);

    System.out.println("exp.getValue(): " + exp.getValue() + " result.getValue(): " + signal.getValue());
    assertEquals(exp.getValue(), signal.getValue());
    assertEquals(exp.getName(), signal.getName());
    assertEquals(exp.getAddress(), signal.getAddress());
    assertEquals(exp.getSoacId(), signal.getSoacId());
    assertEquals(exp.getAddress2(), signal.getAddress2());
    assertEquals(exp.getId2(), signal.getId2());
    assertEquals(exp.getValue2(), signal.getValue2());
    assertEquals(exp.getSignalValue(), signal.getSignalValue());
    assertEquals(exp.getLightImages(), signal.getLightImages());

    assertEquals(exp, signal);
  }

  @Test
  public void testFindAll() {
    System.out.println("findAll");
    SignalDAO instance = new SignalDAO();
    List<SignalBean> expResult = signals;
    List<SignalBean> result = instance.findAll();

    assertEquals(expResult.size(), result.size());
    assertEquals(signals.get(0), result.get(0));
    assertEquals(signals.get(1), result.get(1));
    assertEquals(signals.get(2), result.get(2));
    assertEquals(signals.get(3), result.get(3));

    assertEquals(expResult, result);
  }

  @Test
  public void testSignalValue() {
    System.out.println("signalValue");
    SignalBean s2 = new SignalBean(4, "S 4", "3942", new BigDecimal(4), AccessoryValue.RED, null, 2, null, null, null, null);
    s2.setName("S 4");

    assertEquals(s2.getSignalValue(), SignalValue.Hp0);
    s2.setValue(AccessoryValue.GREEN);
    assertEquals(s2.getSignalValue(), SignalValue.Hp1);
    s2.setValue2(AccessoryValue.GREEN);
    assertEquals(s2.getSignalValue(), SignalValue.Hp2);
    s2.setValue2(AccessoryValue.RED);
    assertEquals(s2.getSignalValue(), SignalValue.Hp0Sh1);
  }

  @Test
  public void testFind() {
    System.out.println("find");

    Integer address = 3;
    SignalDAO instance = new SignalDAO();
    SignalBean expResult = signals.get(0);
    SignalBean result = instance.find(address);

    assertNotNull(result);

    assertEquals(expResult.getId(), result.getId());

    System.out.println("Exp: " + expResult);

    System.out.println("expResult.getValue(): " + expResult.getValue() + " result.getValue(): " + result.getValue());
    assertEquals(expResult.getValue(), result.getValue());
    assertEquals(expResult.getName(), result.getName());
    assertEquals(expResult.getAddress(), result.getAddress());
    assertEquals(expResult.getSoacId(), result.getSoacId());
    assertEquals(expResult.getAddress2(), result.getAddress2());
    assertEquals(expResult.getId2(), result.getId2());
    assertEquals(expResult.getValue2(), result.getValue2());
    assertEquals(expResult.getSignalValue(), result.getSignalValue());
    assertEquals(expResult.getLightImages(), result.getLightImages());

    assertEquals(expResult, result);

    address = 5;
    expResult = signals.get(2);
    result = instance.find(address);
    System.out.println("expResult.getValue(): " + expResult.getValue() + " result.getValue(): " + result.getValue());
    assertEquals(expResult.getValue(), result.getValue());
    assertEquals(expResult.getName(), result.getName());
    assertEquals(expResult.getAddress(), result.getAddress());
    assertEquals(expResult.getSoacId(), result.getSoacId());
    assertEquals(expResult.getAddress2(), result.getAddress2());
    assertEquals(expResult.getId2(), result.getId2());
    assertEquals(expResult.getValue2(), result.getValue2());

    assertEquals(expResult.getSignalValue(), result.getSignalValue());
    assertEquals(expResult.getLightImages(), result.getLightImages());
    assertEquals(expResult, result);
  }

  //@Test
  public void testPersist() {
    System.out.println("persist");

    SignalBean s15 = new SignalBean(15, "S 15", "77777", null, AccessoryValue.GREEN, null, 2, null, null, null, null);
    s15.setName("S 15");

    SignalDAO instance = new SignalDAO();
    BigDecimal result = instance.persist(s15);
    SignalBean sr = instance.find(15);

    BigDecimal expResult = new BigDecimal(9);
    assertEquals(expResult, result);
    assertNotNull(sr);
    assertEquals(AccessoryValue.GREEN, sr.getValue());
    assertEquals(SignalValue.Hp1, sr.getSignalValue());

    //Update
    s15.setValue(AccessoryValue.RED);
    result = instance.persist(s15);
    assertEquals(expResult, result);
    sr = instance.find(15);
    assertNotNull(sr);
    assertEquals(AccessoryValue.RED, sr.getValue());
    assertEquals(SignalValue.Hp0, sr.getSignalValue());

  }

  //@Test
  public void testPersist2() {
    System.out.println("persist2");

    SignalBean s17 = new SignalBean(17, "S 17", "9999", AccessoryValue.GREEN, 4, 18, AccessoryValue.RED);
    s17.setName("S 17");
    s17.setSwitchTime(100);

    assertEquals(SignalValue.Hp1, s17.getSignalValue());

    SignalDAO instance = new SignalDAO();
    BigDecimal result = instance.persist(s17);
    BigDecimal expResult = new BigDecimal(9);

    assertEquals(expResult, result);
    SignalBean sr = instance.find(17);
    assertEquals(s17, sr);
    assertEquals(SignalValue.Hp1, sr.getSignalValue());
    //Update
    s17.setGreen2();
    instance.persist(s17);
    sr = instance.find(17);
    assertEquals(AccessoryValue.GREEN, sr.getValue2());
    assertEquals(SignalValue.Hp2, sr.getSignalValue());
  }

  //@Test
  public void testPersist3() {
    System.out.println("persist3");

    SignalBean s19 = new SignalBean(19, "S 19", "11111111", AccessoryValue.RED, 4, 20, AccessoryValue.RED);
    s19.setName("S 19");
    s19.setSwitchTime(150);
    SignalDAO instance = new SignalDAO();
    BigDecimal result = instance.persist(s19);
    BigDecimal expResult = new BigDecimal(9);

    assertEquals(expResult, result);
    SignalBean sr = instance.find(19);
    assertEquals(s19, sr);
    assertEquals(SignalValue.Hp0, sr.getSignalValue());
    //Update -> Hp1
    s19.setGreen();
    instance.persist(s19);
    sr = instance.find(19);
    assertEquals(AccessoryValue.GREEN, sr.getValue());
    assertEquals(AccessoryValue.RED, sr.getValue2());
    assertEquals(SignalValue.Hp1, sr.getSignalValue());

    //Update -> Hp2
    s19.setGreen2();
    instance.persist(s19);
    sr = instance.find(19);
    assertEquals(AccessoryValue.GREEN, sr.getValue());
    assertEquals(AccessoryValue.GREEN, sr.getValue2());
    assertEquals(SignalValue.Hp2, sr.getSignalValue());
    //Update -> Hp0Sh1
    s19.setRed2();
    instance.persist(s19);
    sr = instance.find(19);
    assertEquals(AccessoryValue.GREEN, sr.getValue());
    assertEquals(AccessoryValue.RED, sr.getValue2());
    assertEquals(SignalValue.Hp0Sh1, sr.getSignalValue());
  }

  //@Test
  public void testRemove() {
    System.out.println("remove");
    SignalBean s11 = new SignalBean(11, "S 11", "11", null, AccessoryValue.GREEN, null, 2, null, null, null, null);
    s11.setName("S 11");
    s11.setSwitchTime(0);

    SignalDAO instance = new SignalDAO();

    instance.persist(s11);
    SignalBean sr = instance.find(11);

    assertEquals(s11, sr);
    instance.remove(sr);

    sr = instance.find(11);
    assertNull(sr);

    SignalBean s13 = new SignalBean(13, "S 13", "13", AccessoryValue.GREEN, 4, 14, AccessoryValue.RED);
    s13.setName("S 13");
    s13.setSwitchTime(0);

    instance.persist(s13);
    sr = instance.find(13);

    assertEquals(s13, sr);
    instance.remove(sr);

    sr = instance.find(13);
    assertNull(sr);
  }
}
