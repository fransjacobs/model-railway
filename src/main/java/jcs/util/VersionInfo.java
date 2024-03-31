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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Obtain Version information from the project generated pom.properties file
 */
public class VersionInfo {

  private static String artifactId = "NOT SET";
  private static String groupId = "NOT SET";
  private static String version = "NOT SET";

  static {
    Properties prop = readFromJARFile("META-INF/maven/jcs/jcs/pom.properties");
    if (prop.isEmpty()) {
      prop = readFromSourceFile("target/maven-archiver/pom.properties");
    }
    artifactId = prop.getProperty("artifactId", "NOT SET");
    groupId = prop.getProperty("groupId", "NOT SET");
    version = prop.getProperty("version", "NOT SET");
  }

  private static Properties readFromJARFile(String filename) {
    Properties prop = new Properties();
    try {
      prop.load(new InputStreamReader(VersionInfo.class.getClassLoader().getResourceAsStream(filename)));
    } catch (NullPointerException | IOException ex) {
      //ignore
    }
    return prop;
  }

  private static Properties readFromSourceFile(String filename) {
    Properties prop = new Properties();
    try {
      File p = new File(filename);
      if (p.exists()) {
        FileInputStream inputStream = new FileInputStream(p);
        prop.load(inputStream);
      }
    } catch (NullPointerException | IOException ex) {
      //ignore
    }
    return prop;
  }

  public static String getVersion() {
    return version;
  }

  public static String getArtifactId() {
    return artifactId;
  }

  public static String getGroupId() {
    return groupId;
  }
}
