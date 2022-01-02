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
package jcs.trackservice.dao;

import jcs.trackservice.dao.util.DAOTestHelper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jcs.entities.TrackPower;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author frans
 */
public class TrackPowerDAOTest {

  private final List<TrackPower> trackPowerList;

  public TrackPowerDAOTest() {
    DAOTestHelper.setConnectProperties();
    DAOTestHelper.createNewDatabase();
    
    trackPowerList = new ArrayList<>();
  }

  @Before
  public void setUp() {
    TrackPower tp1 = new TrackPower();
    tp1.setStatus(TrackPower.Status.OFF);
    trackPowerList.add(tp1);
  }

  @After
  public void tearDown() {
    TrackPowerDAO instance = new TrackPowerDAO();
    TrackPower tp = instance.find(1);
    if (tp != null) {
      tp.setStatus(TrackPower.Status.UNKNOWN);
      tp.setFeedbackSource(TrackPower.FeedbackSource.OTHER);
      instance.persist(tp);

      tp.setLastUpdated(null);
      instance.persist(tp);
    }
  }

  @Test
  public void testFindAll() {
    System.out.println("findAll");
    TrackPowerDAO instance = new TrackPowerDAO();
    List<TrackPower> expResult = trackPowerList;
    List<TrackPower> result = instance.findAll();
    //Don't know the last updated date time hence just get it
    expResult.get(0).setLastUpdated(result.get(0).getLastUpdated());

    assertEquals(expResult, result);
  }

  @Test
  public void testFind() {
    System.out.println("find");

    Integer address = 1;
    TrackPowerDAO instance = new TrackPowerDAO();
    TrackPower result = instance.find(address);

    assertNotNull(result);
    TrackPower expResult = trackPowerList.get(0);

    assertEquals(expResult.getStatus(), result.getStatus());
    assertEquals(expResult.getFeedbackSource(), result.getFeedbackSource());
    assertEquals(expResult.getId(), result.getId());
    assertNull(result.getLastUpdated());
    //Don't know the last updated date time hence just get it
    expResult.setLastUpdated(result.getLastUpdated());
    assertEquals(expResult, result);
  }

  @Test
  public void testPersist() {
    System.out.println("persist");

    TrackPowerDAO instance = new TrackPowerDAO();
    BigDecimal result = instance.persist(trackPowerList.get(0));

    TrackPower tp = instance.find(1);

    BigDecimal expResult = tp.getId();
    assertEquals(expResult, result);

    tp.On();
    instance.persist(tp);

    TrackPower res = instance.find(1);
    assertTrue(res.isOn());
    assertFalse(res.isOff());
    assertNotNull(res.getLastUpdated());
  }

  @Test
  public void testPersist2() {
    System.out.println("persist");

    TrackPowerDAO instance = new TrackPowerDAO();
    BigDecimal result = instance.persist(trackPowerList.get(0));

    TrackPower tp = instance.find(1);

    BigDecimal expResult = tp.getId();
    assertEquals(expResult, result);

    tp.Off();
    instance.persist(tp);

    TrackPower res = instance.find(1);
    assertTrue(res.isOff());
    assertFalse(res.isOn());
    assertNotNull(res.getLastUpdated());
  }

}
