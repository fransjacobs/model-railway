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

/**
 * Obtain Version information from the project JCSVersion.class
 */
public class VersionInfo {

  private static String artifactId = "NOT SET";
  private static String groupId = "NOT SET";
  private static String version = "NOT SET";
  private static String buildTime = "NOT SET";
  private static boolean snapshot = false;

  static {
    artifactId = JCSVersion.ARTIFACT_ID;
    groupId = JCSVersion.GROUP_ID;
    version = JCSVersion.VERSION;
    buildTime = JCSVersion.BUILD_TIME;
    snapshot = JCSVersion.isSnapshot();
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

  public static String getBuildTime() {
    return buildTime;
  }

  public static boolean isSnapshot() {
    return snapshot;
  }

  public static String getDisplayVersion() {
    return JCSVersion.getDisplayVersion();
  }
}
