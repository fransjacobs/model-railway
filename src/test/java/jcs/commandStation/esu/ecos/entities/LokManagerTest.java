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
package jcs.commandStation.esu.ecos.entities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;
import jcs.commandStation.esu.ecos.EcosMessage;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import jcs.entities.LocomotiveBean.Direction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author frans
 */
public class LokManagerTest {

  public LokManagerTest() {
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
    EcosMessage query = new EcosMessage("queryObjects(10, name, addr, protocol)");
    query.addResponse("<REPLY queryObjects(10, name, addr, protocol)>1002 name[\"FS236-002\"] addr[14] protocol[DCC28]1003 name[\"NS 6505\"] addr[8] protocol[DCC28]1000 name[\"193 304-3 DB AG\"] addr[0] protocol[MFX]1001 name[\"SNCB/NMBS HLE 27\"] addr[3] protocol[DCC28]1004 name[\"NS 1205\"] addr[11] protocol[DCC28]1005 name[\"DB-141-015-8\"] addr[12] protocol[MM14]1007 name[\"NS 1139\"] addr[5] protocol[DCC14]1008 name[\"NS 1309\"] addr[68] protocol[DCC28]<END 0 (OK)>");

    LokManager instance = new LokManager(query);
    int expResult = 8;
    int result = instance.getSize();
    assertEquals(expResult, result);
  }

  @Test
  public void testUpdate() {
    System.out.println("update");
    EcosMessage query = new EcosMessage("queryObjects(10, name, addr, protocol)");
    query.addResponse("<REPLY queryObjects(10, name, addr, protocol)>1002 name[\"FS236-002\"] addr[14] protocol[DCC28]1003 name[\"NS 6505\"] addr[8] protocol[DCC28]1000 name[\"193 304-3 DB AG\"] addr[0] protocol[MFX]1001 name[\"SNCB/NMBS HLE 27\"] addr[3] protocol[DCC28]1004 name[\"NS 1205\"] addr[11] protocol[DCC28]1005 name[\"DB-141-015-8\"] addr[12] protocol[MM14]1007 name[\"NS 1139\"] addr[5] protocol[DCC14]1008 name[\"NS 1309\"] addr[68] protocol[DCC28]<END 0 (OK)>");

    LokManager instance = new LokManager(query);

    EcosMessage detail = new EcosMessage("get(1001, name, addr, protocol,dir,speed,speedstep,active,locodesc,func)");
    detail.addResponse("<REPLY get(1001, name, addr, protocol,dir,speed,speedstep,active,locodesc,func)>1001 name[\"SNCB/NMBS HLE 27\"]1001 addr[3]1001 protocol[DCC28]1001 dir[0]1001 speed[0]1001 speedstep[0]1001 active[0]1001 locodesc[LOCO_TYPE_E,IMAGE_TYPE_USER,2]1001 func[0,1]1001 func[1,0]1001 func[2,1]1001 func[3,0]1001 func[4,0]1001 func[5,0]1001 func[6,0]1001 func[7,0]1001 func[8,0]1001 func[9,0]1001 func[10,0]1001 func[11,0]1001 func[12,0]1001 func[13,0]1001 func[14,0]1001 func[15,0]1001 func[16,0]1001 func[17,0]1001 func[18,0]1001 func[19,0]1001 func[20,0]1001 func[21,0]1001 func[22,0]1001 func[23,0]1001 func[24,0]1001 func[25,0]1001 func[26,0]1001 func[27,0]1001 func[28,0]1001 func[29,0]1001 func[30,0]<END 0 (OK)>");

    instance.update(detail);

    //Check loc 1001
    LocomotiveBean l1001 = instance.getLocomotives().get(1001L);
    assertNotNull(l1001);
    assertEquals((Long) 1001L, l1001.getId());
    assertEquals("SNCB/NMBS HLE 27", l1001.getName());

    assertEquals((Integer) 3, l1001.getAddress());
    assertEquals(DecoderType.DCC28, l1001.getDecoderType());
    assertEquals(Direction.FORWARDS, l1001.getDirection());
    assertEquals((Integer) 0, l1001.getVelocity());

    Map<Integer, FunctionBean> functions = l1001.getFunctions();
    assertEquals(31, functions.size());

    assertTrue(functions.get(0).isOn());
    assertFalse(functions.get(1).isOn());
    assertTrue(functions.get(2).isOn());

  }

  @Test
  public void testGetLocomotives() {
    System.out.println("getLocomotives");
    EcosMessage query = new EcosMessage("queryObjects(10, name, addr, protocol)");
    query.addResponse("<REPLY queryObjects(10, name, addr, protocol)>1002 name[\"FS236-002\"] addr[14] protocol[DCC28]1003 name[\"NS 6505\"] addr[8] protocol[DCC28]1000 name[\"193 304-3 DB AG\"] addr[0] protocol[MFX]1001 name[\"SNCB/NMBS HLE 27\"] addr[3] protocol[DCC28]1004 name[\"NS 1205\"] addr[11] protocol[DCC28]1005 name[\"DB-141-015-8\"] addr[12] protocol[MM14]1007 name[\"NS 1139\"] addr[5] protocol[DCC14]1008 name[\"NS 1309\"] addr[68] protocol[DCC28]<END 0 (OK)>");

    LokManager instance = new LokManager(query);
    int expResult = 8;
    Map<Long, LocomotiveBean> locomotives = instance.getLocomotives();

    assertEquals(expResult, locomotives.size());

    //Check loc 1001
    LocomotiveBean l1002 = instance.getLocomotives().get(1002L);
    assertNotNull(l1002);
    assertEquals((Long) 1002L, l1002.getId());
    assertEquals("FS236-002", l1002.getName());

    assertEquals((Integer) 14, l1002.getAddress());
    assertEquals(DecoderType.DCC28, l1002.getDecoderType());

    LocomotiveBean l1000 = instance.getLocomotives().get(1000L);
    assertNotNull(l1000);
    assertEquals((Long) 1000L, l1000.getId());
    assertEquals("193 304-3 DB AG", l1000.getName());

    assertEquals((Integer) 0, l1000.getAddress());
    assertEquals(DecoderType.MFX, l1000.getDecoderType());

  }
}
