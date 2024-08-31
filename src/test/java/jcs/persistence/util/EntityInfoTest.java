/*
 * Copyright 2024 Frans Jacobs.
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
package jcs.persistence.util;

import java.awt.Image;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import jcs.entities.LocomotiveBean;
import jcs.ui.util.ImageUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class EntityInfoTest {

  public EntityInfoTest() {
  }

  private LocomotiveBean createBean() {
    LocomotiveBean lb = new LocomotiveBean(8L, "NS DHG 6505", 8L, 8, "", "dcc", 100, 0, 0, 1, true, true);
    String imgPath = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + "dcc-ex" + File.separator + "ns dhg 6505.png";
    lb.setIcon(imgPath);
    Image locImage = ImageUtil.readImage(imgPath);
    //Image is sized by default so
    locImage = ImageUtil.scaleImage(locImage, 100);
    lb.setLocIcon(locImage);

    return lb;
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getValue method, of class EntityInfo.
   */
  @Test
  public void testGetValue() {
    System.out.println("getValue");
    Object bean = createBean();
    String name = "name";
    EntityInfo instance = new EntityInfo(bean.getClass());
    Object expResult = "NS DHG 6505";
    Object result = instance.getValue(bean, name);
    assertEquals(expResult, result);
  }

  /**
   * Test of putValue method, of class EntityInfo.
   */
  //@Test
  public void testPutValue_3args() {
    System.out.println("putValue");
    Object bean = null;
    String name = "";
    Object value = null;
    EntityInfo instance = null;
    instance.putValue(bean, name, value);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of putValue method, of class EntityInfo.
   */
  //@Test
  public void testPutValue_4args() {
    System.out.println("putValue");
    Object bean = null;
    String name = "";
    Object value = null;
    boolean ignoreIfMissing = false;
    EntityInfo instance = null;
    instance.putValue(bean, name, value, ignoreIfMissing);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getPrimaryKeyNames method, of class EntityInfo.
   */
  @Test
  public void testGetPrimaryKeyNames() {
    System.out.println("getPrimaryKeyNames");
    Object bean = createBean();
    EntityInfo instance = new EntityInfo(bean.getClass());

    String[] pkCols = new String[]{"id"};
    List<String> expResult = Arrays.asList(pkCols);

    List<String> result = instance.getPrimaryKeyNames();
    assertEquals(expResult, result);
  }

  /**
   * Test of getAllColumnNames method, of class EntityInfo.
   */
  @Test
  public void testGetAllColumnNames() {
    System.out.println("getAllColumnNames");
    Object bean = createBean();
    EntityInfo instance = new EntityInfo(bean.getClass());

    String[] cols = new String[]{"dispatcher_direction", "id", "name", "address", "tacho_max", "v_min", "velocity", "locomotive_direction", "commuter", "show", "icon", "imported", "command_station_id", "synchronize", "uid", "decoder_type"};

    List<String> expResult = Arrays.asList(cols);

    List<String> result = instance.getAllColumnNames();
    assertEquals(expResult, result);
  }

  /**
   * Test of getAllColumnNames method, of class EntityInfo.
   */
  @Test
  public void testGetAllColumnNamesIgnoreTransientCols() {
    System.out.println("getAllColumnNamesIgnoreTransientCols");
    Object bean = createBean();
    EntityInfo instance = new EntityInfo(bean.getClass(), true);

    String[] cols = new String[]{"commandStationBean", "functions", "functionCount", "dispatcher_direction", "id", "image", "name", "address", "decoder", "tacho_max", "v_min", "velocity", "locomotive_direction", "commuter", "show", "icon", "imported", "command_station_id", "synchronize", "uid", "richtung", "decoder_type"};

    List<String> expResult = Arrays.asList(cols);

    List<String> result = instance.getAllColumnNames();
    assertEquals(expResult, result);
  }

  /**
   * Test of getColumnNames method, of class EntityInfo.
   */
  @Test
  public void testGetColumnNames() {
    System.out.println("getColumnNames");
    Object bean = createBean();

    String[] displayCols = new String[]{"id", "name", "address", "velocity", "icon", "decoder_type"};
    List<String> expResult = Arrays.asList(displayCols);

    EntityInfo instance = new EntityInfo(bean.getClass(), displayCols);

    List<String> result = instance.getColumnNames();
    assertEquals(expResult, result);
  }

  /**
   * Test of getColumnDataType method, of class EntityInfo.
   */
  @Test
  public void testGetColumnDataType() {
    System.out.println("getColumnDataType");
    String columnName = "address";
    Object bean = createBean();
    EntityInfo instance = new EntityInfo(bean.getClass());

    Class expResult = Integer.class;
    Class result = instance.getColumnDataType(columnName);
    assertEquals(expResult, result);
  }

  /**
   * Test of getDisplayColumnList method, of class EntityInfo.
   */
  @Test
  public void testGetDisplayColumnList() {
    System.out.println("getDisplayColumnList");
    Object bean = createBean();
    String[] displayCols = new String[]{"id", "name", "decoder_type", "address", "velocity", "icon"};
    List<String> expResult = Arrays.asList(displayCols);

    EntityInfo instance = new EntityInfo(bean.getClass(), displayCols);
    List<String> result = instance.getDisplayColumnList();
    assertEquals(expResult, result);
  }

}
