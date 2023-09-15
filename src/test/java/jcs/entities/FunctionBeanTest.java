/*
 * Copyright 2023 frans.
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
package jcs.entities;

import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 *
 * @author frans
 */
public class FunctionBeanTest {

  public FunctionBeanTest() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

//  
// public static void main(String[] z) throws IOException {
//    FunctionSvgToPngConverter svgCache = new FunctionSvgToPngConverter();
//    Path fcticons = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "fcticons.json");
//    String json = Files.readString(fcticons);
//    svgCache.loadSvgCache(json);
//
//    FunctionBean fba = new FunctionBean();
//    fba.setIcon("fkticon_a_001");
//
//    FunctionBean fbi = new FunctionBean();
//    fbi.setIcon("fkticon_i_092");
//
//    Logger.trace(fba.getIcon() + " a: " + fba.getActiveIcon() + " i: " + fba.getInActiveIcon());
//    Logger.trace(fbi.getIcon() + " a: " + fbi.getActiveIcon() + " i: " + fbi.getInActiveIcon());
//
//    Logger.trace("fba a:" + svgCache.getFunctionImageCS3(fba.getActiveIcon()));
////    Logger.trace("fba i:" + svgCache.getFunctionImageCS3(fba.getInActiveIcon()));
////
////    Logger.trace("fbi a:" + svgCache.getFunctionImageCS3(fbi.getActiveIcon()));
////    Logger.trace("fbi i:" + svgCache.getFunctionImageCS3(fbi.getInActiveIcon()));
//  }  
//  
  /**
   * Test of getIconName method, of class FunctionBean.
   */
  //@Test
  public void testGetIconName_0args() {
    System.out.println("getIconName");
    FunctionBean instance = new FunctionBean();
    String expResult = "";
    String result = instance.getIconName();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getIconName method, of class FunctionBean.
   */
  //@Test
  public void testGetIconName_boolean() {
    System.out.println("getIconName");
    boolean active = false;
    FunctionBean instance = new FunctionBean();
    String expResult = "";
    String result = instance.getIconName(active);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getActiveIcon method, of class FunctionBean.
   */
  //@Test
  public void testGetActiveIcon() {
    System.out.println("getActiveIcon");
    FunctionBean instance = new FunctionBean();
    String expResult = "";
    String result = instance.getActiveIcon();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getInActiveIcon method, of class FunctionBean.
   */
  //@Test
  public void testGetInActiveIcon() {
    System.out.println("getInActiveIcon");
    FunctionBean instance = new FunctionBean();
    String expResult = "";
    String result = instance.getInActiveIcon();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
