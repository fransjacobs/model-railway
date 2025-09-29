/*
 * Copyright 2025 fransjacobs.
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author fransjacobs
 */
public class LocomotiveBeanTest {
  
  public LocomotiveBeanTest() {
  }
  
  @BeforeEach
  public void setUp() {
  }
  
  @AfterEach
  public void tearDown() {
  }

  /**
   * Test of getDecoderType method, of class LocomotiveBean.
   */
  @Test
  public void testGetDecoderType() {
    System.out.println("getDecoderType");
    LocomotiveBean instance = new LocomotiveBean();

    LocomotiveBean.DecoderType result = instance.getDecoderType();
    assertNull(result);

    instance.setDecoderTypeString("mm");
    
    LocomotiveBean.DecoderType expResult = LocomotiveBean.DecoderType.MM;
    result = instance.getDecoderType();
    assertEquals(expResult, result);

    instance.setDecoderTypeString("MM");
    result = instance.getDecoderType();
    assertEquals(expResult, result);

    instance.setDecoderTypeString("mm_prg");
    result = instance.getDecoderType();
    assertEquals(expResult, result);

    instance.setDecoderTypeString("mm28");
    result = instance.getDecoderType();
    assertEquals(expResult, result);
    
    
    expResult = LocomotiveBean.DecoderType.MFX;
    instance.setDecoderTypeString("mfx");
    result = instance.getDecoderType();
    assertEquals(expResult, result);
    
    instance.setDecoderTypeString("mfx+");
    result = instance.getDecoderType();
    assertEquals(expResult, result);

    instance.setDecoderTypeString("mfxp");
    result = instance.getDecoderType();
    assertEquals(expResult, result);

    expResult = LocomotiveBean.DecoderType.DCC;
    instance.setDecoderTypeString("dcc");
    result = instance.getDecoderType();
    assertEquals(expResult, result);

    instance.setDecoderTypeString("dcc14");
    result = instance.getDecoderType();
    assertEquals(expResult, result);

    instance.setDecoderTypeString("dcc128");
    result = instance.getDecoderType();
    assertEquals(expResult, result);
    
    expResult = LocomotiveBean.DecoderType.SX1;
    instance.setDecoderTypeString("sx1");
    result = instance.getDecoderType();
    assertEquals(expResult, result);
    
    instance.setDecoderTypeString("sx");
    result = instance.getDecoderType();
    assertEquals(expResult, result);
    
  }


  /**
   * Test of getDirection method, of class LocomotiveBean.
   */
  //@Test
  public void testGetDirection() {
    System.out.println("getDirection");
    LocomotiveBean instance = new LocomotiveBean();
    LocomotiveBean.Direction expResult = null;
    LocomotiveBean.Direction result = instance.getDirection();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  
}
