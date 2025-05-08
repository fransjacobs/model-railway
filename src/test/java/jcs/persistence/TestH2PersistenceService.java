/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.persistence;

import com.dieselpoint.norm.Database;
import java.io.File;
import jcs.persistence.sqlmakers.H2SqlMaker;
import jcs.persistence.util.H2DatabaseUtil;
import org.tinylog.Logger;

/**
 * Persistence service to use during unit testing
 */
public class TestH2PersistenceService extends H2PersistenceService {

  public TestH2PersistenceService() {
    super();
  }

  /**
   * Overridden to make sure it connects to the test database
   */
  @Override
  protected void connect() {
    String jdbcUrl = H2DatabaseUtil.JDBC_PRE + System.getProperty("user.home") + File.separator + "jcs" + File.separator + "test-" + H2DatabaseUtil.JCS_DB_NAME + H2DatabaseUtil.DB_MODE + H2DatabaseUtil.SCHEMA;
    System.setProperty("norm.jdbcUrl", jdbcUrl);

    Logger.info("TESTMODE Connecting to: " + System.getProperty("norm.jdbcUrl") + " with db user: " + System.getProperty("norm.user"));
    database = new Database();
    database.setSqlMaker(new H2SqlMaker());
  }

//  protected void setJCSPropertiesAsSystemProperties() {
//    List<JCSPropertyBean> props = getProperties();
//    props.forEach(p -> {
//      System.setProperty(p.getKey(), p.getValue());
//    });
//  }

}
