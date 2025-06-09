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
package jcs.persistence.util;

import java.io.File;
import java.sql.SQLException;
import org.h2.tools.Script;
import org.tinylog.Logger;

/**
 * Back the JCS data int a script sql file
 */
public class Backup extends H2DatabaseUtil {

  public static void backup(String filename) {
    String jdbcUrlAdmin = JDBC_PRE + System.getProperty("user.home") + File.separator + "jcs" + File.separator + JCS_DB_NAME + DB_MODE;
    String path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + ((filename != null) ? filename : DEFAULT_BACKUP_FILENAME);

    try {
      Script.process(jdbcUrlAdmin, ADMIN_USER, ADMIN_PWD, path, "DROP", "");
    } catch (SQLException ex) {
      Logger.error(ex);
    }
  }

  public static void main(String[] a) throws SQLException {
    backup(null);
  }

}
