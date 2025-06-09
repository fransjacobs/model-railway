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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import static jcs.persistence.util.H2DatabaseUtil.ADMIN_PWD;
import static jcs.persistence.util.H2DatabaseUtil.ADMIN_USER;
import static jcs.persistence.util.H2DatabaseUtil.jdbcConnect;
import org.tinylog.Logger;

/**
 * Restore the full JCS database
 */
public class Restore extends H2DatabaseUtil {

  public static void restore(String filename) {
    String path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + ((filename != null) ? filename : DEFAULT_BACKUP_FILENAME);

    try {
      try (Connection c = jdbcConnect(ADMIN_USER, ADMIN_PWD, false)) {
        if (c != null) {
          Statement stmt = c.createStatement();
          stmt.executeUpdate("runscript from '" + path + "'");
          Logger.info("Restored JCS database from " + path);
        } else {
          Logger.error("Could not obtain a connection!");
        }
      }
    } catch (SQLException ex) {
      Logger.error(ex);
    }
  }

  public static void main(String[] a) {
    restore(null);
  }

}
