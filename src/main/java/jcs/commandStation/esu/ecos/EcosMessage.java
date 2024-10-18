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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class EcosMessage implements Ecos {

  private final String message;
  private String response;
  private Map<String, String> valueMap;

  private static final String REPLY = "<REPLY ";
  private static final String END = "<END ";
  private static final String EVENT = "<EVENT ";

//  public EcosMessage() {
//    this(null);
//  }
  public EcosMessage(String message) {
    if (message.startsWith(EVENT)) {
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

  public boolean isEvent() {
    return response != null && response.startsWith(EVENT);
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

  public int getErrorCode() {
    if (response != null && (response.startsWith(REPLY) || response.startsWith(EVENT))) {
      int endStart = response.indexOf(END);
      String endTag = response.substring(endStart);

      endStart = endTag.indexOf(" ") + 1;
      int errorCodeEnd = endTag.indexOf(" (");
      int errorCodeLen = errorCodeEnd - endStart;

      String errorCode = endTag.substring(endStart, errorCodeLen + endStart);
      return Integer.parseInt(errorCode);
    } else {
      return -1;
    }
  }

  public String getResponseCode() {
    if (response != null && (response.startsWith(REPLY) || response.startsWith(EVENT))) {
      int endStart = response.indexOf(END);
      String endTag = response.substring(endStart);

      endStart = endTag.indexOf(" (") + 2;
      int errorCodeEnd = endTag.indexOf(")>");
      int errorCodeLen = errorCodeEnd - endStart;

      String responseCode = endTag.substring(endStart, errorCodeLen + endStart);
      return responseCode;
    } else {
      return "ERROR!";
    }
  }

  public String getResponseContent() {
    if (response != null && (response.startsWith(REPLY) || response.startsWith(EVENT))) {
      int contentStart = response.indexOf(")>\n");
      String content = response.substring(contentStart + ")>\n".length());
      int tagStart = content.indexOf(END);
      String responseContent = content.substring(0, tagStart);
      return responseContent;
    } else {
      return null;
    }
  }

  public Map<String, String> getValueMap() {
    String content = getResponseContent();
    if (content != null) {
      if (valueMap == null) {
        valueMap = new HashMap<>();
        String[] lines = content.split("\n");
        for (String line : lines) {
          int keyStart = line.indexOf(" ") + 1;
          int valStart = line.indexOf("[");
          int valEnd = line.indexOf("]");
          int keyLen = valStart - keyStart;
          int valLen = valEnd - valStart;

          if (valStart >= 0 && valEnd > 0) {
            String key = line.substring(keyStart, keyLen + keyStart);
            String value = line.substring(valStart + 1, valLen + valStart);
            valueMap.put(key, value);
          } else {
            String key = line.substring(keyStart);
            valueMap.put(key, null);
          }
        }
      }
      return valueMap;
    } else {
      return Collections.EMPTY_MAP;
    }
  }

  public boolean isValid() {
    return getErrorCode() == 0;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("TX: ");
    sb.append(message);
    return sb.toString();
  }

}
