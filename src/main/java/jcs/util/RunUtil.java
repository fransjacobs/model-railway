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
package jcs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.tinylog.Logger;

public class RunUtil {

  public static final String DEFAULT_PATH = "~/jcs/";
  private static boolean propertiesLoaded = false;

  public static boolean isMacOSX() {
    return System.getProperty("os.name").contains("Mac OS X");
  }

  public static boolean isLinux() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("nix") || os.contains("nux");
  }

  public static boolean isWindows() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("windows");
  }

  public static void loadProperties() {
    if (!propertiesLoaded) {
      Properties prop = new Properties();
      InputStream inputStream = RunUtil.class.getClassLoader().getResourceAsStream("jcs.properties");
      if (inputStream != null) {
        try {
          prop.load(inputStream);
        } catch (IOException ex) {
          Logger.error("Can't read jcs.properties");
        }
      }

      for (Object pk : prop.keySet()) {
        String key = (String) pk;
        String value = (String) prop.get(pk);
        if (System.getProperty(key) == null) {
          System.setProperty(key, value);
        }
      }
      propertiesLoaded = true;
      Logger.trace("JCS Properties loaded.");
    }
  }

  public static void loadExternalProperties() {
    Properties prop = new Properties();
    String p = DEFAULT_PATH + "jcs.properties";
    p = p.replace("~", System.getProperty("user.home"));

    File ep = new File(p);
    if (ep.exists()) {
      try {
        prop.load(new FileInputStream(p));
        for (Object pk : prop.keySet()) {
          String key = (String) pk;
          String value = (String) prop.get(pk);
          if (System.getProperty(key) == null) {
            System.setProperty(key, value);
            Logger.trace(key + "=" + value);
          }
        }
      } catch (IOException ex) {
        Logger.trace("Can't read external properties from " + p);
      }
    } else {
      Logger.trace("Optional external properties not available.");
    }
  }

  public static void writeProperty(String filePath, String key, String value) {
    Properties properties = new Properties();
    String p = filePath;
    p = p.replace("~", System.getProperty("user.home"));

    File f = new File(p);
    if (!f.exists()) {
      try {
        f.createNewFile();
      } catch (IOException ex) {
        Logger.trace("Can't create new file: " + p);
      }
    }

    try (OutputStream outputStream = new FileOutputStream(p)) {
      properties.setProperty(key, value);
      properties.store(outputStream, null);
    } catch (IOException e) {
      Logger.warn("Can't write property " + key + ", " + value + " to " + p);
    }
  }

  public static String readProperty(String filePath, String key) {
    String p = filePath;
    p = p.replace("~", System.getProperty("user.home"));
    Properties prop = new Properties();
    try {
      prop.load(new FileInputStream(p));
      //get the property value and print it out
      return (prop.getProperty(key));
    } catch (IOException ex) {
      Logger.warn("Can't read property " + key + " from " + filePath);
    }
    return null;
  }

}
