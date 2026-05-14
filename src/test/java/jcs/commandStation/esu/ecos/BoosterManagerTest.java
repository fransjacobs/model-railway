/*
 * Copyright 2026 Frans Jacobs.
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

import java.util.ArrayList;
import java.util.List;
import jcs.commandStation.esu.ecos.entities.EcosBooster;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.tinylog.Logger;

/**
 * ECoS Booster manager is created by reverse engineering the latest ESU ECoS firmware as there is NO documentation on the protocol for the Booster related calls
 */
public class BoosterManagerTest {

  public BoosterManagerTest() {
  }

  @BeforeAll
  public static void setUpClass() {
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testCreate() {
    Logger.info("Create a Booster Manager");

    String tx = "queryObjects(27,name)";
    String rx = "<REPLY queryObjects(27,name)>\n65000 name[\"Interne booster\"]\n65001 name[\"Ext. Booster Ctl\"]\n<END 0 (OK)>\n";

    EcosMessage boosterQuery = new EcosMessage(tx);
    boosterQuery.addResponse(rx);

    BoosterManager bm = new BoosterManager(null, boosterQuery);

    assertEquals(2, bm.getSize());

    EcosBooster b1 = new EcosBooster();
    b1.setId("65000");
    b1.setName("Interne booster");

    EcosBooster b2 = new EcosBooster();
    b2.setId("65001");
    b2.setName("Ext. Booster Ctl");
    List<EcosBooster> expBoosters = new ArrayList<>();
    expBoosters.add(b1);
    expBoosters.add(b2);

    assertEquals(expBoosters, bm.getBoosters());
  }

  @Test
  public void testUpdate() {
    Logger.info("Update Booster Manager");

    String tx = "queryObjects(27,name)";
    String rx = "<REPLY queryObjects(27,name)>\n65000 name[\"Interne booster\"]\n65001 name[\"Ext. Booster Ctl\"]\n<END 0 (OK)>\n";
    EcosMessage boosterQuery = new EcosMessage(tx);
    boosterQuery.addResponse(rx);
    BoosterManager bm = new BoosterManager(null, boosterQuery);
    assertEquals(2, bm.getSize());

    String tx2 = "get(65000,name,status,voltage,current,temperature,limit))";
    String rx2 = "<REPLY get(65000,name,status,voltage,current,temperature,limit)>\n65000 name[\"Interne booster\"]\n65000 status[GO]\n65000 voltage[17434]\n65000 current[46,55]\n65000 temperature[39]\n65000 limit[4000]\n<END 0 (OK)>\n";

    EcosMessage boosterDetails = new EcosMessage(tx2);
    boosterDetails.addResponse(rx2);

    String tx3 = "get(65001,name,status,voltage,current,temperature,limit))";
    String rx3 = "<REPLY get(65001,name,status,voltage,current,temperature,limit)>\n65001 name[\"Ext. Booster Ctl\"]\n65001 status[GO]\n65001 voltage[0]\n65001 current[0,0]\n65001 temperature[0]\n65001 limit[10000]\n<END 0 (OK)>\n";
    EcosMessage boosterDetails2 = new EcosMessage(tx3);
    boosterDetails2.addResponse(rx3);

    bm.update(boosterDetails);

    EcosBooster eb1 = new EcosBooster();
    eb1.setId("65000");
    eb1.setName("Interne booster");
    eb1.setStatus("GO");
    eb1.setTemperature(39.0);
    eb1.setCurrent(46.0);
    eb1.setPeakCurrent(55.0);
    eb1.setLimit(4000);
    eb1.setVoltage(17.434);
    eb1.setCurrentUnit("mA");

    EcosBooster booster1 = bm.getBooster("65000");

    assertEquals(eb1.getId(), booster1.getId());
    assertEquals(eb1.getName(), booster1.getName());
    assertEquals(eb1.getStatus(), booster1.getStatus());
    assertEquals(eb1.getTemperature(), booster1.getTemperature());
    assertEquals(eb1.getCurrent(), booster1.getCurrent());
    assertEquals(eb1.getPeakCurrent(), booster1.getPeakCurrent());
    assertEquals(eb1.getLimit(), booster1.getLimit());
    assertEquals(eb1.getVoltage(), booster1.getVoltage());
    assertEquals(eb1.getCurrentUnit(), booster1.getCurrentUnit());

    assertEquals(eb1.hashCode(), booster1.hashCode());
  }

}

//
//<REPLY queryObjects(27,name)>65000 name["Interne booster"]65001 name["Ext. Booster Ctl"]<END 0 (OK)>
//<REPLY get(65000,name,status,voltage,current,temperature)>\r\n65000 name["Interne booster"]\r\n65000 status[GO]\r\n65000 voltage[17347]\r\n65000 current[130,183]\r\n65000 temperature[37]\r\n<END 0 (OK)>\r\n
//<REPLY get(65000,name,status,voltage,current,temperature,limit)>\r\n65000 name["Interne booster"]\r\n65000 status[GO]\r\n65000 voltage[17438]\r\n65000 current[44,55]\r\n65000 temperature[40]\r\n65000 limit[4000]\r\n<END 0 (OK)>\r\n
