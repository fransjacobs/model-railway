/*
 * Copyright 2024 frans.
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

import java.util.Map;
import jcs.entities.AccessoryBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author frans
 */
public class AccessoryManagerTest {

  public AccessoryManagerTest() {
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testGetSize() {
    System.out.println("getSize");

    String tx = "queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)";
    String rx = "<REPLY queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)>20000 name1[\"W1\"] name2[\"artikel\"] name3[\">0001<\"] addr[1] protocol[DCC] mode[SWITCH] symbol[1]20001 name1[\"W2\"] name2[\"artikel\"] name3[\">0001<\"] addr[2] protocol[DCC] mode[SWITCH] symbol[0]20002 name1[\"Sein\"] name2[\"2 aspect\"] name3[\">0001<\"] addr[10] protocol[MOT] mode[SWITCH] symbol[9]20003 name1[\"Sein 3\"] name2[\"3 aspect\"] name3[\">0012<\"] addr[12] protocol[MOT] mode[SWITCH] symbol[11]20004 name1[\"Sein 4\"] name2[\"4 aspect\"] name3[\">0001<\"] addr[14] protocol[MOT] mode[SWITCH] symbol[12]20005 name1[\"Sein mini\"] name2[\"artikel\"] name3[\">0001<\"] addr[16] protocol[MOT] mode[SWITCH] symbol[13]<END 0 (OK)>";

    EcosMessage query = new EcosMessage(tx);
    query.addResponse(rx);

    AccessoryManager instance = new AccessoryManager(null, query);
    int expResult = 6;
    int result = instance.getSize();
    assertEquals(expResult, result);
  }

  //@Test
  public void testUpdate() {
    System.out.println("update");
    EcosMessage message = null;
    AccessoryManager instance = null;
    instance.update(message);
    fail("The test case is a prototype.");
  }

//  @Test
  public void testGetAccessories() {
    System.out.println("getAccessories");
    AccessoryManager instance = null;
    Map<String, AccessoryBean> expResult = null;
    Map<String, AccessoryBean> result = instance.getAccessories();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }
}
