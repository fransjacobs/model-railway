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
import java.io.BufferedReader;
import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;
import jcs.persistence.sqlmakers.H2SqlMaker;
import jcs.util.RunUtil;
import org.tinylog.Logger;

public class H2DatabaseUtil {

  protected static final String JCS_DB_NAME = "jcs-db";

  protected static final String SCHEMA = ";SCHEMA=jcs";
  protected static final String DB_MODE = ";AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE";

  protected static final String ADMIN_USER = "SA";
  protected static final String ADMIN_PWD = "jcs";
  protected static final String JCS_USER = "jcs";
  protected static final String JCS_PWD = "repo";
  protected static final String JDBC_PRE = "jdbc:h2:";

  protected static final String DB_CREATE_DDL = "jcs-db.sql";

  protected Database db;

  protected static boolean test = false;

  public H2DatabaseUtil() {
    this(false);
  }

  public H2DatabaseUtil(boolean test) {
    H2DatabaseUtil.test = test;
  }

  public static void setProperties(boolean test) {
    String jdbcUrl = JDBC_PRE + RunUtil.DEFAULT_PATH + (test ? "test-" : "") + JCS_DB_NAME + DB_MODE + SCHEMA;
    System.setProperty("norm.jdbcUrl", jdbcUrl);
    System.setProperty("norm.user", JCS_USER);
    System.setProperty("norm.password", JCS_PWD);
  }

  protected void connect() {
    if (System.getProperty("norm.jdbcUrl") == null) {
      RunUtil.loadProperties();
      setProperties(H2DatabaseUtil.test);
    }
    Logger.trace("Connecting to: " + System.getProperty("norm.jdbcUrl") + " with db user: " + System.getProperty("norm.user"));

    this.db = new Database();
    this.db.setSqlMaker(new H2SqlMaker());
    Logger.trace("Connected to: " + System.getProperty("norm.jdbcUrl"));
  }

  protected static Connection obtainJdbcConnection(String jdbcUrl, String user, String password) throws SQLException {
    Logger.trace("URL: " + jdbcUrl);
    Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
    String cat = conn.getCatalog();

    Logger.trace("User: " + user + " Connected to database: " + cat);
    return conn;
  }

  protected static Connection jdbcConnect(String user, String password, boolean defaultSchema, boolean test) {
    String jdbcUrl = JDBC_PRE + RunUtil.DEFAULT_PATH + (test ? "test-" : "") + JCS_DB_NAME + DB_MODE + (defaultSchema ? SCHEMA : "");

    Connection conn = null;
    Logger.trace("URL: " + jdbcUrl);
    try {
      conn = DriverManager.getConnection(jdbcUrl, user, password);
      String cat = conn.getCatalog();

      Logger.trace("User: " + user + " Connected to " + cat);
    } catch (SQLException sqle) {
      Logger.error("Can't connect to: " + jdbcUrl + " as " + user + "...", sqle);
    }
    return conn;
  }

  public static void createDatabase() {
    createDatabase(null);
  }

  protected static String readFromJARFile(String filename) throws IOException {
    InputStream is = H2DatabaseUtil.class.getClassLoader().getResourceAsStream(filename);
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    br.close();
    isr.close();
    is.close();
    return sb.toString();
  }

  protected static void createDatabase(Connection connection) {
//    try {
    executeSQLScript(DB_CREATE_DDL, connection);

//      URL url = H2DatabaseUtil.class.getClassLoader().getResource(DB_CREATE_DDL);
//      String f = url.getFile();
//      File file = new File(f);
//      String script;
//      if (file.exists()) {
//        Logger.trace("Reading from path...");
//        script = Files.readString(file.toPath());
//      } else {
//        Logger.trace("Reading from jar file...");
//        script = readFromJARFile(DB_CREATE_DDL);
//      }
//
//      Logger.trace("Script length: " + script.length());
//      if (script.length() > 10) {
//        Logger.info("Loading ddl script: " + DB_CREATE_DDL);
//
//        executeSQLScript(script, connection, DB_CREATE_DDL);
//        Logger.info("Created JCS Database schema.");
//      } else {
//        Logger.error("Could not create JCS Database schema!");
//      }
//    } catch (IOException ex) {
//      Logger.error("Error: " + ex.getMessage());
//    }
  }

  public static void createDatabaseUsers(boolean test) {
    Logger.info("Creating new " + (test ? "TEST " : "") + "JCS Database...");
    H2DatabaseUtil.test = test;
    deleteDatebaseFile(test);

    try {
      try (Connection c = jdbcConnect(ADMIN_USER, ADMIN_PWD, false, test)) {
        if (c != null) {
          Statement stmt = c.createStatement();

          stmt.executeUpdate("DROP SCHEMA IF EXISTS JCS CASCADE");
          stmt.executeUpdate("DROP USER IF EXISTS JCS CASCADE");

          //Create the JCS user and schema
          stmt.executeUpdate("CREATE USER JCS PASSWORD 'repo' ADMIN");
          stmt.executeUpdate("CREATE SCHEMA JCS AUTHORIZATION JCS");

          Logger.debug("Created JCS User.");
        } else {
          Logger.error("Could not obtain a connection!");
        }
      }
    } catch (SQLException ex) {
      Logger.error(ex);
    }
  }

  protected static void executeSQLScript(String filename) {
    executeSQLScript(filename,null);
  }

  protected static void executeSQLScript(String filename, Connection connection) {
    try {
      File file = new File(filename);
      String script;
      if (file.exists()) {
        script = Files.readString(file.toPath());
      } else {
        script = readFromJARFile(filename);
      }
      executeSQLScript(script, connection, filename);
    } catch (IOException ex) {
      Logger.error("Error: " + ex.getMessage());
    }
  }

  protected static void executeSQLScript(String script, Connection connection, String scriptName) {
    Connection conn = connection;
    try {
      if (conn == null) {
        if (System.getProperty("norm.jdbcUrl") == null) {
          RunUtil.loadProperties();
        }
        if (System.getProperty("norm.jdbcUrl") == null) {
          Logger.warn("Using DatabaseUtil defaults!");
          setProperties(H2DatabaseUtil.test);
        }

        String jdbcUrl = System.getProperty("norm.jdbcUrl");
        String user = System.getProperty("norm.user");
        String password = System.getProperty("norm.password");
        conn = obtainJdbcConnection(jdbcUrl, user, password);
      }
      if (conn == null) {
        Logger.error("Connection is null! Quit!");
        return;
      }

      var buffer = new StringBuilder();
      var scanner = new Scanner(script);
      while (scanner.hasNextLine()) {
        var line = scanner.nextLine();
        buffer.append(line);
        // When a semicolon is found, assume a complete statement, so execute it.
        if (line.endsWith(";")) {
          String command = buffer.toString();

          //Logger.trace(command);
          conn.createStatement().execute(command);
          buffer = new StringBuilder();
        } else {
          buffer.append("\n");
        }

        Logger.trace("Executed script: " + scriptName);
      }
    } catch (SQLException e) {
      Logger.error("Can't execute ddl script!");
      Logger.error("Cause: " + e.getMessage(), e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException ex) {
          Logger.trace("Can't close connection");
        }
      }
    }
  }

  private static void deleteDatebaseFile(boolean test) {
    String pathString = System.getProperty("user.home") + File.separator + "jcs";
    String fileName = (test ? "test-" : "") + JCS_DB_NAME;

    Logger.trace("Check for file(s) " + fileName);

    Path jcsPath = Paths.get(pathString);
    Stream<Path> list;
    try {
      list = Files.list(jcsPath);
      List<Path> files = list.toList();
      Logger.trace("Found " + files.size() + " files");
      for (Path p : files) {
        try {
          Path fnp = p.getFileName();
          String fn = fnp.toString();
          if (fn.contains(".")) {
            fn = fn.substring(0, fn.indexOf("."));
          }
          if (fn.equals(fileName)) {
            boolean r = Files.deleteIfExists(p);
            if (r) {
              Logger.trace("File " + p.getFileName() + " deleted");
            } else {
              Logger.trace("Could not delete " + p.getFileName() + "!");
            }
          }
        } catch (IOException ex) {
          Logger.warn(ex);
        }
      }
    } catch (IOException ex) {
      Logger.error(ex);
    }
  }

  public static boolean databaseFileExists(boolean test) {
    String pathString = System.getProperty("user.home") + File.separator + "jcs";
    String fileName = (test ? "test-" : "") + JCS_DB_NAME;
    boolean exist = false;
    Path jcsPath = Paths.get(pathString);
    Stream<Path> list;
    try {
      list = Files.list(jcsPath);
      List<Path> files = list.toList();

      for (Path p : files) {
        Path fnp = p.getFileName();
        String fn = fnp.toString();
        if (fn.contains(".")) {
          fn = fn.substring(0, fn.indexOf("."));
        }
        exist = fn.equals(fileName);
        if (exist) {
          Logger.trace("Found database file " + p.toString());
          break;
        }
      }
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return exist;
  }

  public static void main(String[] a) {
    if (H2DatabaseUtil.databaseFileExists(false)) {
      Logger.info("Database files exists");
    } else {
      Logger.info("Creating a new Database...");
      H2DatabaseUtil.createDatabaseUsers(false);
      H2DatabaseUtil.createDatabase();
    }
  }

}
