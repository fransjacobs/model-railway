/*
 * Copyright (C) 2020 Frans Jacobs.
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
import jcs.entities.LayoutTileGroup;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Frans Jacobs
 */
public class LayoutTileGroupDAOTest {

  private final List<LayoutTileGroup> layoutTileGroups;

  public LayoutTileGroupDAOTest() {
    DAOTestHelper.setConnectProperties();
    DAOTestHelper.createNewDatabase();
    DAOTestHelper.insertLayoutTileData();
    //DAOTestHelper.insertLayoutTileGroupData();

    layoutTileGroups = new ArrayList<>();

  }

  //@Before
  public void setUp() {
    LayoutTileGroup ltg1 = new LayoutTileGroup(new BigDecimal(1), "Block 1", "YELLOW", "RIGHT", new BigDecimal(1), new BigDecimal(2),1);
    layoutTileGroups.add(ltg1);
    LayoutTileGroup ltg2 = new LayoutTileGroup(new BigDecimal(2), "Block 2", "GREY", "RIGHT", new BigDecimal(2), new BigDecimal(3),2);
    layoutTileGroups.add(ltg2);
  }

  //@After
  public void tearDown() {
  }

  //@Test
  public void testFindAll() {
    System.out.println("findAll");
    LayoutTileGroupDAO instance = new LayoutTileGroupDAO();
    List<LayoutTileGroup> expResult = layoutTileGroups;
    List<LayoutTileGroup> result = instance.findAll();
    assertEquals(expResult, result);
  }

  //@Test
  public void testFind() {
    System.out.println("find");
    Integer id = 1;
    LayoutTileGroupDAO instance = new LayoutTileGroupDAO();
    LayoutTileGroup expResult = layoutTileGroups.get(0);
    LayoutTileGroup result = instance.find(id);
    assertEquals(expResult, result);
  }

  //@Test
  public void testFindById() {
    System.out.println("findById");
    BigDecimal id = new BigDecimal(2);
    LayoutTileGroupDAO instance = new LayoutTileGroupDAO();
    LayoutTileGroup expResult = layoutTileGroups.get(1);
    LayoutTileGroup result = instance.findById(id);
    assertEquals(expResult, result);
  }

  //@Test
  public void testFindByStartLatiId() {
    System.out.println("findByStartLatiId");
    BigDecimal startLatiId = new BigDecimal(1);
    LayoutTileGroupDAO instance = new LayoutTileGroupDAO();
    LayoutTileGroup expResult = layoutTileGroups.get(0);
    LayoutTileGroup result = instance.findByStartLatiId(startLatiId);
    assertEquals(expResult, result);
  }

  //@Test
  public void testFindByEndLatiId() {
    System.out.println("findByEndLatiId");
    BigDecimal endLatiId = new BigDecimal(3);
    LayoutTileGroupDAO instance = new LayoutTileGroupDAO();
    LayoutTileGroup expResult = layoutTileGroups.get(1);
    LayoutTileGroup result = instance.findByEndLatiId(endLatiId);
    assertEquals(expResult, result);
  }

  //@Test
  public void testPersist() {
    System.out.println("persist");
    LayoutTileGroup layoutTileGroup = new LayoutTileGroup(null, "Block 3", "YELLOW", "RIGHT", 3);
    LayoutTileGroupDAO instance = new LayoutTileGroupDAO();
    BigDecimal expResult = new BigDecimal(3);
    BigDecimal result = instance.persist(layoutTileGroup);
    assertEquals(expResult, result);
    
    List<LayoutTileGroup> ltgl = instance.findAll();
    assertEquals(3,ltgl.size());
  }

  //@Test
  public void testRemove() {
    System.out.println("remove");
    LayoutTileGroup layoutTileGroup = new LayoutTileGroup(null, "Block 3", "YELLOW", "RIGHT", 3);
    LayoutTileGroupDAO instance = new LayoutTileGroupDAO();
    BigDecimal expResult = instance.persist(layoutTileGroup);
    BigDecimal result = instance.persist(layoutTileGroup);
    
    assertEquals(expResult, result);
    
    LayoutTileGroup ltg = instance.findById(result);
    
    assertNotNull(ltg);
    layoutTileGroup.setId(result);
    assertEquals(layoutTileGroup, ltg);
    
    instance.remove(layoutTileGroup);

    ltg = instance.findById(result);
    
    assertNull(ltg);
  }

}
