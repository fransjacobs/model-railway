/*
 * Copyright 2023 Frans Jacobs.
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

import com.dieselpoint.norm.Database;
import java.net.URL;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class PersistenceTestHelper extends H2DatabaseUtil {

  private static PersistenceTestHelper instance;

  private PersistenceTestHelper() {
    super(true);
  }

  public static PersistenceTestHelper getInstance() {
    if (instance == null) {
      PersistenceTestHelper.test = true;
      setProperties();

      createDatabaseUsers();
      createDatabase();
      instance = new PersistenceTestHelper();
    }
    return instance;
  }

  public final Database getDatabase() {
    return this.db;
  }

  public void insertTestData() {
    executeSQLScript("jcs-test-data-h2.sql");
    Logger.debug("Inserted Test data...");
  }

  public void insertSimpleLayoutTestData() {
    executeSQLScript("simple_layout_tiles.sql");
    Logger.debug("Inserted Simple Layout Test data...");
  }

  public void insertSimpleLayoutDirectionTestData() {
    executeSQLScript("simple_layout_tiles_with_direction.sql");
    Logger.debug("Inserted Simple Layout Direction Test data...");
  }

  public void runTestDataInsertScript(String scriptName) {
    URL url = PersistenceTestHelper.class.getClassLoader().getResource(scriptName);
    String f = url.getFile();
    executeSQLScript(f);
    Logger.debug("Executed script: " + scriptName);
  }

  public static void main(String[] a) {
    PersistenceTestHelper pth = getInstance();

    pth.insertTestData();

    pth.insertSimpleLayoutTestData();

    pth.insertSimpleLayoutDirectionTestData();
  }
}
