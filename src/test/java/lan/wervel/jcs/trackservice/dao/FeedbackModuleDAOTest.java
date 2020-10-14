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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lan.wervel.jcs.entities.FeedbackModule;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author frans
 */
public class FeedbackModuleDAOTest {

  private final List<FeedbackModule> feedbackModules;

  public FeedbackModuleDAOTest() {
    DAOTestHelper.setConnectProperties();
    DAOTestHelper.createNewDatabase();
    DAOTestHelper.insertFeedbackData();

    feedbackModules = new ArrayList<>();
  }

  @Before
  public void setUp() {
    FeedbackModule femo1 = new FeedbackModule(1, "S88", 16);
    femo1.setName("S88_1");
    femo1.setDescription("S88 1-16");
    femo1.setResponse(new Integer[]{0, 0});
    femo1.setId(new BigDecimal(1));
    feedbackModules.add(femo1);

    FeedbackModule femo2 = new FeedbackModule(2, "S88", 16);
    femo2.setName("S88_2");
    femo2.setDescription("S88 17-32");
    femo2.setResponse(new Integer[]{0, 0});
    femo2.setId(new BigDecimal(2));
    feedbackModules.add(femo2);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testFindAll() {
    System.out.println("findAll");
    FeedbackModuleDAO instance = new FeedbackModuleDAO();
    List<FeedbackModule> expResult = feedbackModules;
    List<FeedbackModule> result = instance.findAll();
    assertEquals(expResult, result);
  }

  @Test
  public void testFind() {
    System.out.println("find");
    Integer address = 1;
    FeedbackModuleDAO instance = new FeedbackModuleDAO();
    FeedbackModule expResult = feedbackModules.get(0);
    FeedbackModule result = instance.find(address);
    assertEquals(expResult, result);

    address = 2;
    expResult = feedbackModules.get(1);
    result = instance.find(address);
    assertEquals(expResult, result);
  }

  @Test
  public void testPersist() {
    System.out.println("persist");
    Date now = new Date();

    FeedbackModule feedbackModule = new FeedbackModule(3, "S88-Test", 16);
    feedbackModule.setName("S88_8");
    feedbackModule.setDescription("S88 33-48");
    feedbackModule.setResponse(new Integer[]{128, 128});
    feedbackModule.setLastUpdated(now);

    FeedbackModuleDAO instance = new FeedbackModuleDAO();
    BigDecimal result = instance.persist(feedbackModule);

    FeedbackModule femo = instance.find(3);
    assertEquals(result, femo.getId());

    feedbackModule.setId(new BigDecimal(3));

    assertEquals(feedbackModule, femo);

    assertTrue(femo.isPort1());
    assertTrue(femo.isPort9());

    feedbackModule.setResponse(new Integer[]{128, 0});
    result = instance.persist(feedbackModule);

    FeedbackModule femo1 = instance.find(3);
    assertEquals(result, femo1.getId());
    assertTrue(femo1.isPort1());
    assertFalse(femo1.isPort9());
  }

  @Test
  public void testRemove() {
    System.out.println("remove");
    Date now = new Date();

    FeedbackModule feedbackModule = new FeedbackModule(12, "S88-Test", 16);
    feedbackModule.setName("S88_12");
    feedbackModule.setResponse(new Integer[]{32, 32});
    feedbackModule.setLastUpdated(now);

    FeedbackModuleDAO instance = new FeedbackModuleDAO();
    BigDecimal result = instance.persist(feedbackModule);

    assertEquals(result, feedbackModule.getId());

    FeedbackModule femo = instance.find(12);
    assertEquals(feedbackModule, femo);

    instance.remove(feedbackModule);
    femo = instance.find(12);

    assertNull(femo);
  }

}
