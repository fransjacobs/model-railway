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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import jcs.entities.LocomotiveBean.Direction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frans
 */
public class LocomotiveManagerTest {

  public LocomotiveManagerTest() {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
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

    LocomotiveManager instance = new LocomotiveManager(null, query);
    int expResult = 8;
    int result = instance.getSize();
    assertEquals(expResult, result);
  }

  @Test
  public void testUpdate() {
    System.out.println("update");
    EcosMessage query = new EcosMessage("queryObjects(10, name, addr, protocol)");
    query.addResponse("<REPLY queryObjects(10, name, addr, protocol)>1002 name[\"FS236-002\"] addr[14] protocol[DCC28]1003 name[\"NS 6505\"] addr[8] protocol[DCC28]1000 name[\"193 304-3 DB AG\"] addr[0] protocol[MFX]1001 name[\"SNCB/NMBS HLE 27\"] addr[3] protocol[DCC28]1004 name[\"NS 1205\"] addr[11] protocol[DCC28]1005 name[\"DB-141-015-8\"] addr[12] protocol[MM14]1007 name[\"NS 1139\"] addr[5] protocol[DCC14]1008 name[\"NS 1309\"] addr[68] protocol[DCC28]<END 0 (OK)>");

    LocomotiveManager instance = new LocomotiveManager(null, query);

    EcosMessage detail = new EcosMessage("get(1001, name, addr, protocol,dir,speed,speedstep,active,locodesc,func)");
    detail.addResponse("<REPLY get(1001, name, addr, protocol,dir,speed,speedstep,active,locodesc,func)>1001 name[\"SNCB/NMBS HLE 27\"]1001 addr[3]1001 protocol[DCC28]1001 dir[0]1001 speed[0]1001 speedstep[0]1001 active[0]1001 locodesc[LOCO_TYPE_E,IMAGE_TYPE_USER,2]1001 func[0,1]1001 func[1,0]1001 func[2,1]1001 func[3,0]1001 func[4,0]1001 func[5,0]1001 func[6,0]1001 func[7,0]1001 func[8,0]1001 func[9,0]1001 func[10,0]1001 func[11,0]1001 func[12,0]1001 func[13,0]1001 func[14,0]1001 func[15,0]1001 func[16,0]1001 func[17,0]1001 func[18,0]1001 func[19,0]1001 func[20,0]1001 func[21,0]1001 func[22,0]1001 func[23,0]1001 func[24,0]1001 func[25,0]1001 func[26,0]1001 func[27,0]1001 func[28,0]1001 func[29,0]1001 func[30,0]<END 0 (OK)>");

    instance.update(detail);

    //Check loc 1001
    LocomotiveBean l1001 = instance.getLocomotives().get(1001L);
    assertNotNull(l1001);
    assertEquals((Long) 1001L, l1001.getId());
    assertEquals("SNCB/NMBS HLE 27", l1001.getName());

    assertEquals((Integer) 3, l1001.getAddress());
    assertEquals(DecoderType.DCC, l1001.getDecoderType());
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

    LocomotiveManager instance = new LocomotiveManager(null, query);
    int expResult = 8;
    Map<Long, LocomotiveBean> locomotives = instance.getLocomotives();

    assertEquals(expResult, locomotives.size());

    //Check loc 1001
    LocomotiveBean l1002 = instance.getLocomotives().get(1002L);
    assertNotNull(l1002);
    assertEquals((Long) 1002L, l1002.getId());
    assertEquals("FS236-002", l1002.getName());

    assertEquals((Integer) 14, l1002.getAddress());
    assertEquals(DecoderType.DCC, l1002.getDecoderType());

    LocomotiveBean l1000 = instance.getLocomotives().get(1000L);
    assertNotNull(l1000);
    assertEquals((Long) 1000L, l1000.getId());
    assertEquals("193 304-3 DB AG", l1000.getName());

    assertEquals((Integer) 0, l1000.getAddress());
    assertEquals(DecoderType.MFX, l1000.getDecoderType());

  }

  @Test
  public void testGetLocomotiveFunctions() {
    System.out.println("getLocomotiveFunctions");

    String tx = "get(1001,name,addr,protocol,dir,speed,speedstep,active,locodesc,func,funcdesc)";
    String rx = "<REPLY get(1001,name,addr,protocol,dir,speed,speedstep,active,locodesc,func,funcdesc)>1001 name[\"SNCB/NMBS HLE 27\"]1001 addr[3]1001 protocol[DCC128]1001 dir[0]1001 speed[0]1001 speedstep[0]1001 active[1]1001 locodesc[LOCO_TYPE_E,IMAGE_TYPE_INT,2]1001 func[0,0]1001 func[1,0]1001 func[2,0]1001 func[3,0]1001 func[4,0]1001 func[5,0]1001 func[6,0]1001 func[7,0]1001 func[8,0]1001 func[9,0]1001 func[10,0]1001 func[11,0]1001 func[12,0]1001 func[13,0]1001 func[14,0]1001 func[15,0]1001 func[16,0]1001 func[17,0]1001 func[18,0]1001 func[19,0]1001 func[20,0]1001 func[21,0]1001 func[22,0]1001 func[23,0]1001 func[24,0]1001 func[25,0]1001 func[26,0]1001 func[27,0]1001 func[28,0]1001 func[29,0]1001 func[30,0]1001 funcdesc[0,3]1001 funcdesc[1,7]1001 funcdesc[2,37,moment]1001 funcdesc[3,37,moment]1001 funcdesc[4,34]1001 funcdesc[5,260]1001 funcdesc[6,10]1001 funcdesc[7,4]1001 funcdesc[8,5]1001 funcdesc[9,3]1001 funcdesc[10,7]1001 funcdesc[11,1287]1001 funcdesc[12,12039]1001 funcdesc[13,2055,moment]1001 funcdesc[14,9,moment]1001 funcdesc[15,40]1001 funcdesc[16,39]1001 funcdesc[17,12039]1001 funcdesc[18,9,moment]1001 funcdesc[19,11527,moment]1001 funcdesc[20,11015]1001 funcdesc[21,8,moment]1001 funcdesc[22,9,moment]1001 funcdesc[23,1033,moment]1001 funcdesc[24,809]1001 funcdesc[25,11783,moment]1001 funcdesc[26,300]1001 funcdesc[27,263]1001 funcdesc[28,12039]1001 funcdesc[29,1033,moment]1001 funcdesc[30,1033,moment]<END 0 (OK)>";

    String expFunc = "[0,0],[1,0],[2,0],[3,0],[4,0],[5,0],[6,0],[7,0],[8,0],[9,0],[10,0],[11,0],[12,0],[13,0],[14,0],[15,0],[16,0],[17,0],[18,0],[19,0],[20,0],[21,0],[22,0],[23,0],[24,0],[25,0],[26,0],[27,0],[28,0],[29,0],[30,0]";
    String expFuncDesc = "[0,3],[1,7],[2,37,moment],[3,37,moment],[4,34],[5,260],[6,10],[7,4],[8,5],[9,3],[10,7],[11,1287],[12,12039],[13,2055,moment],[14,9,moment],[15,40],[16,39],[17,12039],[18,9,moment],[19,11527,moment],[20,11015],[21,8,moment],[22,9,moment],[23,1033,moment],[24,809],[25,11783,moment],[26,300],[27,263],[28,12039],[29,1033,moment],[30,1033,moment]";

    EcosMessage get = new EcosMessage(tx);
    get.addResponse(rx);

    LocomotiveManager instance = new LocomotiveManager(null, get);
    int expResult = 1;

    Map<Long, LocomotiveBean> locomotives = instance.getLocomotives();

    assertEquals(expResult, locomotives.size());

    //Check loc 1001
    LocomotiveBean l1001 = instance.getLocomotives().get(1001L);
    assertNotNull(l1001);
    assertEquals((Long) 1001L, l1001.getId());
    assertEquals("SNCB/NMBS HLE 27", l1001.getName());

    assertEquals((Integer) 3, l1001.getAddress());
    assertEquals(DecoderType.DCC, l1001.getDecoderType());
    
    Map<Integer,FunctionBean> functions = l1001.getFunctions();
    
    assertEquals(31, functions.size());
    

  }

}
