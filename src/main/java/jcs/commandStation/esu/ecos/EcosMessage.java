/*
 * Copyright 2024 Frans Jacobs.
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
package jcs.commandStation.esu.ecos;

/**
 *
 */
public class EcosMessage implements Ecos {

  private final String message;
  private String response;

  private static final String REPLY = "<REPLY ";
  private static final String END = "<END ";

//  public EcosMessage() {
//    this(null);
//  }
  public EcosMessage(String message) {
    if (message.startsWith(REPLY)) {
      this.response = message;
      this.message = null;
    } else {
      this.message = message;
    }
  }

  public String getMessage() {
    return message;
  }

  public void setResponse(String response) {
    this.response = response;
  }

  public boolean isResponse() {
    return response != null && response.startsWith(REPLY);
  }

  public int getObjectId() {
    if (response != null && response.startsWith(REPLY)) {
      int idStart = response.indexOf("(") + 1;
      int idEnd = response.indexOf(",");
      int idLen = idEnd - idStart;

      String id = response.substring(idStart, idLen + idStart);
      return Integer.parseInt(id);
    } else {
      int idStart = message.indexOf("(") + 1;
      int idEnd = message.indexOf(",");
      int idLen = idEnd - idStart;

      String id = message.substring(idStart, idLen + idStart);
      return Integer.parseInt(id);
    }
  }

  public String getCommand() {
    if (response != null && response.startsWith(REPLY)) {
      int cmdStart = response.indexOf(" ") + 1;
      int cmdEnd = response.indexOf("(");
      int cmdLen = cmdEnd - cmdStart;

      String cmd = response.substring(cmdStart, cmdLen + cmdStart);
      return cmd;
    } else {
      int cmdStart = 0;
      int cmdEnd = message.indexOf("(");
      int cmdLen = cmdEnd - cmdStart;

      String cmd = message.substring(cmdStart, cmdLen + cmdStart);
      return cmd;
    }
  }

  public boolean isValid() {
    return response != null && response.startsWith("<") && response.endsWith(">");
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("TX: ");
    sb.append(message);
    return sb.toString();
  }

}
