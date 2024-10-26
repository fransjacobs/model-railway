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
import org.tinylog.Logger;

/**
 *
 */
public class EcosMessage implements Ecos {

  private final String message;
  private final StringBuilder response;
  private Map<String, String> valueMap;

  public static final String REPLY = "<REPLY ";
  public static final String END = "<END ";
  public static final String EVENT = "<EVENT ";

//  public EcosMessage() {
//    this(null);
//  }
  public EcosMessage(String message) {
    this.response = new StringBuilder();
    if (message.startsWith(EVENT)) {
      this.response.append(message);
      this.message = null;
    } else {
      this.message = message;
    }
  }

  public String getMessage() {
    return message;
  }

  public void addResponse(String response) {
    this.response.append(response);
  }

  public String getResponse() {
    return this.response.toString();
  }

  public static boolean isComplete(String reply) {
    if (reply == null) {
      return false;
    }
    if (reply.startsWith(REPLY) && reply.contains(END)) {
      return true;
    } else {
      return reply.startsWith(EVENT) && reply.contains(END);
    }
  }

  public boolean isResponseComplete() {
    return response != null && isComplete(response.toString());
  }

  public boolean isEvent() {
    return response != null && response.toString().startsWith(EVENT);
  }

  private String getId() {
    if (response != null && response.toString().startsWith(REPLY)) {
      int idStart = response.indexOf("(") + 1;
      int idEnd = response.indexOf(",");
      int idLen = idEnd - idStart;

      String id = response.substring(idStart, idLen + idStart);
      return id;
    } else {
      Logger.trace(response);
      String msg = response.toString();

      int idStart = msg.indexOf(" ") + 1;
      int idEnd = msg.indexOf(">");
      int idLen = idEnd - idStart;

      String id = msg.substring(idStart, idLen + idStart);
      return id;
    }
  }

  public int getObjectId() {
    return Integer.parseInt(getId());
  }

  public String getCommand() {
    if (response != null && response.toString().startsWith(REPLY)) {
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
    if (response != null && (response.toString().startsWith(REPLY) || response.toString().startsWith(EVENT))) {
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
    if (response != null && (response.toString().startsWith(REPLY) || response.toString().startsWith(EVENT))) {
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
    if (response != null && response.length() > 0) {
      String reply = response.toString();
      if (reply.startsWith(REPLY) || reply.startsWith(EVENT)) {
        int contentStart;
        int offsetLen;
        if (reply.startsWith(REPLY) ) {
          contentStart = reply.indexOf(")>");
          offsetLen =  ")>".length();
        } else if(reply.startsWith(EVENT)) {
          contentStart = reply.indexOf(">");
          offsetLen =  ">".length();
        } else {
          //Should never happen!
          Logger.trace("Error, no content start tag: " + reply);
          return null;
        }

        String content = response.substring(contentStart + offsetLen);
        int endTagStart = content.indexOf(END);
        if (endTagStart == -1) {
          Logger.trace("Error, no tag: " + reply);
          return null;
        }

        String responseContent = content.substring(0, endTagStart);
        return responseContent;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public Map<String, String> getValueMap() {
    String content = getResponseContent();
    if (content != null) {
      if (valueMap == null) {
        valueMap = new HashMap<>();
        String replacement = "]" + getId();
        content = content.replace(replacement, "]\n" + getId());
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
