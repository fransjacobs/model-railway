/*
 * Copyright 2026 Frans Jacobs.
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
 * Auto-generated class — do NOT edit by hand. Regenerated on every Maven build via the templating-maven-plugin.
 */
public final class JCSVersion {

  public static final String VERSION = "${project.version}";
  public static final String ARTIFACT_ID = "${project.artifactId}";
  public static final String GROUP_ID = "${project.groupId}";
  public static final String BUILD_TIME = "${maven.build.timestamp}";

  private JCSVersion() {
  }

  /**
   * @return true when this is a SNAPSHOT build
   */
  public static boolean isSnapshot() {
    return VERSION.endsWith("-SNAPSHOT");
  }

  /**
   * @return e.g. "JCS 1.4.2"
   */
  public static String getDisplayVersion() {
    return ARTIFACT_ID + " " + VERSION;
  }

  @Override
  public String toString() {
    return GROUP_ID + ":" + ARTIFACT_ID + ":" + VERSION;
  }
}
