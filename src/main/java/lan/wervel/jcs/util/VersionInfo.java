/*
 * Copyright (C) 2019 frans.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package lan.wervel.jcs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import org.pmw.tinylog.Logger;

/**
 * Obtain Version information from the project generated pom.properties file
 */
public class VersionInfo implements Serializable {

  private static final long serialVersionUID = 6990130099411688007L;

  private String artifactId = "NOT SET";
  private String groupId = "NOT SET";
  private String version = "NOT SET";

  public VersionInfo(Class c, String groupId, String artifactId) {
    try {
      InputStream inputStream = c.getResourceAsStream(String.format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId));

      if (inputStream == null) {
        File f = new File("./target/maven-archiver/pom.properties");
        inputStream = new FileInputStream(f);
      }

      Properties prop = new Properties();

      prop.load(inputStream);
      this.artifactId = prop.getProperty("artifactId");
      this.groupId = prop.getProperty("groupId");
      this.version = prop.getProperty("version");
    } catch (IOException | NullPointerException ex) {
      Logger.trace(ex.getMessage());
    }
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getVersion() {
    return version;
  }
}
