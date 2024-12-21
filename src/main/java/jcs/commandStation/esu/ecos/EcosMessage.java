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
  private Map<String, Object> valueMap;

  public static final String REPLY = "<REPLY ";
  public static final String END = "<END ";
  public static final String EVENT = "<EVENT ";

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

  public String getId() {
    if (response != null && response.toString().startsWith(REPLY)) {
      int idStart = response.indexOf("(") + 1;
      int idEnd = response.indexOf(",");
      if (idEnd == -1) {
        idEnd = response.indexOf(")");
      }
      int idLen = idEnd - idStart;

      String id = response.substring(idStart, idLen + idStart);
      return id;
    } else if (response != null && response.toString().startsWith(EVENT)) {
      int eventEnd = response.indexOf(">") + 1;
      String replaceString = response.substring(0, eventEnd);
      String content = response.toString().replaceFirst(replaceString, "");
      int idStart = 0;
      int idEnd = content.indexOf(" ");
      int idLen = idEnd - idStart;

      String id = content.substring(idStart, idLen + idStart);
      return id;
    } else {
      //Logger.trace(response);
      if (response.length() == 0) {
        //use the message
        int idStart = message.indexOf("(") + 1;
        int idEnd = message.indexOf(",");
        int idLen = idEnd - idStart;
        String id = message.substring(idStart, idLen + idStart);
        return id;
      } else {
        String msg = response.toString();

        int idStart = msg.indexOf(" ") + 1;
        int idEnd = msg.indexOf(">");
        int idLen = idEnd - idStart;

        String id = msg.substring(idStart, idLen + idStart);
        return id;
      }
    }
  }

  public int getObjectId() {
    String s = getId();
    return Integer.parseInt(s);
  }

  public String getCommand() {
    if (response != null && response.toString().startsWith(REPLY)) {
      int cmdStart = response.indexOf(" ") + 1;
      int cmdEnd = response.indexOf("(");
      int cmdLen = cmdEnd - cmdStart;

      String cmd = response.substring(cmdStart, cmdLen + cmdStart);
      return cmd;
    } else if (response != null && response.toString().startsWith(EVENT)) {
      return null;
    } else {
      //use the message
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
        if (reply.startsWith(REPLY)) {
          contentStart = reply.indexOf(")>");
          offsetLen = ")>".length();
        } else if (reply.startsWith(EVENT)) {
          contentStart = reply.indexOf(">");
          offsetLen = ">".length();
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

  private int getIdLength(String objectId) {
    return switch (objectId) {
      case "1" ->
        1;
      case "10" ->
        4;
      case "26" ->
        3;
      default ->
        0;
    };
  }

  public Map<String, Object> getValueMap() {
    String content = getResponseContent();
    Logger.trace(content);
    if (content != null) {
      if (valueMap == null) {
        valueMap = new HashMap<>();
        String id = getId();
        int idLen = getIdLength(id);
        if (!content.contains(" ") && !content.contains("]")) {
          //List with ID's only. The ObjectIddetermines the individual ID length
          for (int i = 0; i < content.length(); i = i + idLen) {
            String key = content.substring(i, i + idLen);
            //Value is same as valKey in this case
            valueMap.put(key, key);
          }
        } else {
          //The response consist out of a (dynamic) id with a one or more values.
          //values are within "[ ]".
          //Format is id<sp>attribute1[attibute1 value]id<sp>attribute2[attribute2 value].....
          String replacement = "]";
          content = content.replace(replacement, "]\n");
          String[] lines = content.split("\n");
          String dId = null;

          for (String line : lines) {
            int idStart = 0;
            int idEnd = line.indexOf(" ");
            //boolean mapInMap = false;
            if (idEnd - idStart > 0) {
              //There is a ID
              dId = line.substring(idStart, idEnd);
              if (dId.equals(getId())) {
                valueMap.put("id", dId);
              } else {
                //Multiple ID's in reply, add a map
                Map<String, Object> vm = new HashMap<>();
                vm.put("id", dId);
                valueMap.put(dId, vm);
              }
            }

            int valKeyStart = idEnd + 1;
            int valStart = line.indexOf("[");
            int valEnd = line.indexOf("]");
            int valKeyLen = valStart - valKeyStart;
            int valLen = valEnd - valStart;

            if (valStart >= 0 && valEnd > 0) {
              String valKey = line.substring(valKeyStart, valKeyLen + valKeyStart);
              String value = line.substring(valStart + 1, valLen + valStart);
              //replace \"
              value = value.replaceAll("\"", "");
              if (dId != null && !dId.equals(getId())) {
                Map<String, String> vm = (Map<String, String>) valueMap.get(dId);
                vm.put(valKey, value);
                valueMap.put(dId, vm);
              } else {
                if (Ecos.FUNCTION.equals(valKey) || Ecos.FUNCTION_DESC.equals(valKey)) {
                  //Functions; create an array [F #, value],[F#,value]...
                  StringBuilder fb = new StringBuilder();
                  if (valueMap.containsKey(valKey)) {
                    fb.append(valueMap.get(valKey).toString());
                  }
                  String fVal = "[" + value + "]";
                  if (fb.length() > 0) {
                    fb.append(",");
                  }
                  fb.append(fVal);
                  valueMap.put(valKey, fb.toString());
                } else {
                  valueMap.put(valKey, value);
                }
              }
            } else {
              String key = line.substring(valKeyStart);
              valueMap.put(key, null);
            }
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
