/*
 * Copyright (C) 2020 fransjacobs.
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
import jcs.entities.SensorBean;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fransjacobs
 */
public class SensorDAOTest {

    private final List<SensorBean> sensors;

    public SensorDAOTest() {
        DAOTestHelper.setConnectProperties();
        DAOTestHelper.createNewDatabase();
        DAOTestHelper.insertSensorData();
        sensors = new ArrayList<>();
    }

    @Before
    public void setUp() {
        SensorBean s1 = new SensorBean(new BigDecimal(1), "M1P1", 0x41, 1, 0, 0, 0, null);
        sensors.add(s1);
        SensorBean s2 = new SensorBean(new BigDecimal(2), "M1P2", 65, 2, 1, 0, 0, null);
        sensors.add(s2);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFindAll() {
        System.out.println("findAll");
        SensorDAO instance = new SensorDAO();
        List<SensorBean> expResult = this.sensors;
        List<SensorBean> result = instance.findAll();
        assertEquals(expResult, result);
    }

    @Test
    public void testFind() {
        System.out.println("find");
        Integer deviceId = 65;
        Integer contactId = 2;
        SensorDAO instance = new SensorDAO();
        SensorBean expResult = sensors.get(1);
        SensorBean result = instance.find(deviceId, contactId);
        assertEquals(expResult, result);
    }

    @Test
    public void testFindById() {
        System.out.println("findById");
        BigDecimal id = new BigDecimal(1);
        SensorDAO instance = new SensorDAO();
        SensorBean expResult = sensors.get(0);
        SensorBean result = instance.findById(id);
        assertEquals(expResult, result);
    }

    @Test
    public void testPersist() {
        System.out.println("persist");
        SensorBean sensor = new SensorBean("M1P3", 65, 3, 0, 1, 0, null);

        SensorDAO instance = new SensorDAO();
        BigDecimal expResult = new BigDecimal(3);

        BigDecimal result = instance.persist(sensor);
        assertEquals(expResult, result);

        SensorBean s3 = instance.find(65, 3);
        sensor.setId(result);
        assertEquals(sensor, s3);

        sensor.setStatus(1);
        result = instance.persist(sensor);
        assertEquals(expResult, result);

        s3 = instance.findById(result);

        assertEquals(sensor, s3);
    }

    @Test
    public void testRemove() {
        System.out.println("remove");
        SensorBean sensor = new SensorBean("M1P4", 65, 4, 0, 1, 0, null);
        SensorDAO instance = new SensorDAO();
        BigDecimal expResult = new BigDecimal(3);
        BigDecimal result = instance.persist(sensor);
        assertEquals(expResult, result);

        SensorBean s4 = instance.find(65, 4);
        sensor.setId(result);
        assertEquals(sensor, s4);

        instance.remove(sensor);
        s4 = instance.find(65, 4);
        assertNull(s4);
    }
}
