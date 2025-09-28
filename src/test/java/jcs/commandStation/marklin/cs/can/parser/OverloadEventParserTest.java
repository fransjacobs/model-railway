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
package jcs.commandStation.marklin.cs.can.parser;

import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.marklin.cs.can.CanMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author fransjacobs
 */
public class OverloadEventParserTest {

  public OverloadEventParserTest() {
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test of parse method, of class OverloadEventParser.
   */
  @Test
  public void testParse() {
    System.out.println("parse");
    CanMessage message = CanMessage.parse("0x00 0x01 0x03 0x26 0x06 0x63 0x73 0x45 0x8c 0x0a 0x01 0x00 0x00");
    PowerEvent expResult = new PowerEvent(false, true);
    PowerEvent result = OverloadEventParser.parse(message);
    assertEquals(expResult, result);
  }

}
