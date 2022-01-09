/*
 * Copyright (C) 2022 fransjacobs.
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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.trackservice.dao.util.DAOTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fransjacobs
 */
public class FunctionBeanDAOTest {

    //private final List<LocomotiveBean> locomotives;
    private final List<FunctionBean> functions;

    public FunctionBeanDAOTest() {
        DAOTestHelper.setConnectProperties();
        DAOTestHelper.createNewDatabase();
        DAOTestHelper.insertLocoData();
        DAOTestHelper.insertLocoFuncData();
        functions = new LinkedList<>();
    }

    @Before
    public void setUp() {
        FunctionBean fb_2_0 = new FunctionBean(new BigDecimal(2), 0, 1, 1);
        functions.add(fb_2_0);
        FunctionBean fb_2_4 = new FunctionBean(new BigDecimal(2), 4, 18, 0);
        functions.add(fb_2_4);
        
        FunctionBean fb_11_0 = new FunctionBean(new BigDecimal(11), 0, 1, 1);
        functions.add(fb_11_0);
        FunctionBean fb_11_3 = new FunctionBean(new BigDecimal(11), 3, 8, 0);
        functions.add(fb_11_3);
        FunctionBean fb_11_4 = new FunctionBean(new BigDecimal(11), 4, 18, 0);
        functions.add(fb_11_4);
        
        FunctionBean fb_16389_0 = new FunctionBean(new BigDecimal(16389), 0, 1, 1);
        functions.add(fb_16389_0);
        FunctionBean fb_16389_1 = new FunctionBean(new BigDecimal(16389), 1, 172, 0);
        functions.add(fb_16389_1);
        FunctionBean fb_16389_2 = new FunctionBean(new BigDecimal(16389), 2, 23, 0);
        functions.add(fb_16389_2);
        FunctionBean fb_16389_4 = new FunctionBean(new BigDecimal(16389), 4, 18, 0);
        functions.add(fb_16389_4);
        FunctionBean fb_16389_5 = new FunctionBean(new BigDecimal(16389), 5, 20, 0);
        functions.add(fb_16389_5);
        FunctionBean fb_16389_6 = new FunctionBean(new BigDecimal(16389), 6, 41, 0);
        functions.add(fb_16389_6);
        FunctionBean fb_16389_7 = new FunctionBean(new BigDecimal(16389), 7, 10, 0);
        functions.add(fb_16389_7);
        FunctionBean fb_16389_8 = new FunctionBean(new BigDecimal(16389), 8, 42, 0);
        functions.add(fb_16389_8);
        FunctionBean fb_16389_9 = new FunctionBean(new BigDecimal(16389), 9, 171, 0);
        functions.add(fb_16389_9);
        FunctionBean fb_16389_10 = new FunctionBean(new BigDecimal(16389), 10, 171, 0);
        functions.add(fb_16389_10);
        FunctionBean fb_16389_11 = new FunctionBean(new BigDecimal(16389), 11, 29, 0);
        functions.add(fb_16389_11);
        FunctionBean fb_16389_12 = new FunctionBean(new BigDecimal(16389), 12, 11, 0);
        functions.add(fb_16389_12);
        FunctionBean fb_16389_13 = new FunctionBean(new BigDecimal(16389), 13, 116, 0);
        functions.add(fb_16389_13);
        FunctionBean fb_16389_14 = new FunctionBean(new BigDecimal(16389), 14, 220, 0);
        functions.add(fb_16389_14);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of findAll method, of class FunctionBeanDAO.
     */
    @Test
    public void testFindAll() {
        System.out.println("findAll");
        FunctionBeanDAO instance = new FunctionBeanDAO();
        List<FunctionBean> expResult = this.functions;
        List<FunctionBean> result = instance.findAll();

        assertEquals(expResult, result);
    }

    /**
     * Test of findById method, of class FunctionBeanDAO.
     */
    @Test
    public void testFindById() {
        System.out.println("findById");
        Integer number = null;
        FunctionBeanDAO instance = new FunctionBeanDAO();
        FunctionBean expResult = this.functions.get(0);
        FunctionBean result = instance.findById(new BigDecimal(2), 0);
        assertEquals(expResult, result);
    }

    /**
     * Test of findBy method, of class FunctionBeanDAO.
     */
    @Test
    public void testFindBy() {
        System.out.println("findBy");
        BigDecimal locomotiveId = new BigDecimal(11);
        FunctionBeanDAO instance = new FunctionBeanDAO();
        
        
        List<FunctionBean> expResult = new LinkedList<>();
        expResult.add(this.functions.get(2));
        expResult.add(this.functions.get(3));
        expResult.add(this.functions.get(4));
        
        List<FunctionBean> result = instance.findBy(locomotiveId);
        assertEquals(expResult, result);
    }

    /**
     * Test of persist method, of class FunctionBeanDAO.
     */
    //@Test
    public void testPersist_Collection() {
        System.out.println("persist");
        Collection<FunctionBean> functions = null;
        FunctionBeanDAO instance = new FunctionBeanDAO();
        instance.persist(functions);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of persist method, of class FunctionBeanDAO.
     */
    //@Test
    public void testPersist_FunctionBean() {
        System.out.println("persist");
        FunctionBean function = null;
        FunctionBeanDAO instance = new FunctionBeanDAO();
        BigDecimal expResult = null;
        BigDecimal result = instance.persist(function);
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class FunctionBeanDAO.
     */
    //@Test
    public void testRemove_Collection() {
        System.out.println("remove");
        Collection<FunctionBean> functions = null;
        FunctionBeanDAO instance = new FunctionBeanDAO();
        instance.remove(functions);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of remove method, of class FunctionBeanDAO.
     */
    //@Test
    public void testRemove_FunctionBean() {
        System.out.println("remove");
        FunctionBean function = null;
        FunctionBeanDAO instance = new FunctionBeanDAO();
        instance.remove(function);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
