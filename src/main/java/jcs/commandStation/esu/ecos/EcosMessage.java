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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A single ECoS command and its complete REPLY, or one unsolicited EVENT.
 * The receiver is responsible for collecting the entire END-terminated frame.
 */
public class EcosMessage implements Ecos {

  public static final String REPLY = "<REPLY ";
  public static final String EVENT = "<EVENT ";
  public static final String END = "<END ";

  private static final Pattern END_TAG =
      Pattern.compile("<END\\s+(\\d+)\\s+\\(([^)]*)\\)>");
  private static final Pattern OBJECT_ATTRIBUTE =
      Pattern.compile("\\s*(\\d+)\\s+([a-zA-Z][a-zA-Z0-9_ -]*)\\[");
  private static final Pattern OBJECT_ID = Pattern.compile("\\s*(\\d+)\\s*");
  // Real ECoS firmware also emits flag attributes such as "1 list" with no
  // brackets. Only accept these at the end of a line to avoid consuming the
  // next attribute or misinterpreting malformed bracketed values.
  private static final Pattern VALUELESS_ATTRIBUTE =
      Pattern.compile("[ \\t\\r\\n]*(\\d+)[ \\t]+([a-zA-Z][a-zA-Z0-9_-]*)[ \\t]*(?:\\r?\\n|$)");

  private final String message;
  private final StringBuilder response = new StringBuilder();
  private Map<String, Object> valueMap;

  public EcosMessage(String message) {
    if (message == null) {
      throw new IllegalArgumentException("ECoS message cannot be null");
    }
    if (message.startsWith(EVENT)) {
      this.message = null;
      response.append(message);
    } else {
      this.message = message;
    }
  }

  public String getMessage() {
    return message;
  }

  public void addResponse(String text) {
    if (text != null) {
      response.append(text);
      valueMap = null;
    }
  }

  public String getResponse() {
    return response.toString();
  }

  public static boolean isComplete(String text) {
    if (text == null || !(text.startsWith(REPLY) || text.startsWith(EVENT))) {
      return false;
    }
    int headerEnd = text.indexOf('>');
    if (headerEnd < 0) {
      return false;
    }
    Matcher matcher = END_TAG.matcher(text);
    return matcher.find(headerEnd + 1);
  }

  public boolean isResponseComplete() {
    return isComplete(response.toString());
  }

  public boolean isEvent() {
    return response.toString().startsWith(EVENT);
  }

  private String header() {
    String text = response.length() > 0 ? response.toString() : message;
    if (text == null) {
      throw new IllegalStateException("Message has no request or response");
    }
    int end = text.indexOf('>');
    return end < 0 ? text : text.substring(0, end + 1);
  }

  public String getId() {
    String header = header();
    if (header.startsWith(EVENT)) {
      int start = EVENT.length();
      int end = header.indexOf('>', start);
      if (end >= 0) {
        return header.substring(start, end).trim();
      }
    }
    int start = header.indexOf('(');
    if (start >= 0) {
      int end = header.indexOf(',', start + 1);
      int closing = header.indexOf(')', start + 1);
      if (end < 0 || (closing >= 0 && closing < end)) {
        end = closing;
      }
      if (end > start) {
        return header.substring(start + 1, end).trim();
      }
    }
    throw new IllegalStateException("Unable to obtain ECoS object ID from: " + header);
  }

  public int getObjectId() {
    return Integer.parseInt(getId());
  }

  public String getCommand() {
    String text = header();
    if (text.startsWith(EVENT)) {
      return null;
    }
    int start = text.startsWith(REPLY) ? REPLY.length() : 0;
    int end = text.indexOf('(', start);
    if (end < start) {
      throw new IllegalStateException("Missing command name: " + text);
    }
    return text.substring(start, end).trim();
  }

  public int getErrorCode() {
    Matcher matcher = END_TAG.matcher(response);
    return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
  }

  public String getResponseCode() {
    Matcher matcher = END_TAG.matcher(response);
    return matcher.find() ? matcher.group(2) : "ERROR!";
  }

  public String getResponseContent() {
    if (!isResponseComplete()) {
      return null;
    }
    String text = response.toString();
    int headerEnd = text.indexOf('>');
    Matcher matcher = END_TAG.matcher(text);
    if (!matcher.find(headerEnd + 1)) {
      return null;
    }
    return text.substring(headerEnd + 1, matcher.start());
  }

  /**
   * Parse multiple attributes for the same object without overwriting previous
   * values. Handles the newline-separated protocol as well as concatenated
   * attributes emitted by the JCS virtual ECoS.
   */
  public Map<String, Object> getValueMap() {
    if (valueMap != null) {
      return valueMap;
    }
    String content = getResponseContent();
    if (content == null || content.isBlank()) {
      return Collections.emptyMap();
    }

    // Real ECoS replies may put multiple attributes on ONE line:
    // 1003 name["NS 6505"] addr[8] protocol[DCC28]
    // Other versions (and the virtual driver) repeat object IDs:
    // 1003 name["NS 6505"]1003 addr[8]
    // Both forms are legal. An object ID may therefore be omitted before
    // subsequent attributes, but must occur at the beginning of an entry.
    Map<String, Object> parsed = new LinkedHashMap<>();
    String rootId = getId();
    String activeId = null;
    int index = 0;
    while (index < content.length()) {
      while (index < content.length() && Character.isWhitespace(content.charAt(index))) {
        index++;
      }
      if (index >= content.length()) {
        break;
      }

      int tokenStart = index;
      while (index < content.length() && Character.isDigit(content.charAt(index))) {
        index++;
      }
      if (index > tokenStart) {
        // A numeric object identifier (also after a closing bracket with
        // no separator in the virtual driver's response).
        String candidateId = content.substring(tokenStart, index);
        if (index < content.length() && Character.isWhitespace(content.charAt(index))) {
          activeId = candidateId;
          while (index < content.length()
              && (content.charAt(index) == ' ' || content.charAt(index) == '\t')) {
            index++;
          }
          // ID-only list entries are supported.
          if (index >= content.length() || content.charAt(index) == '\r'
              || content.charAt(index) == '\n') {
            parsed.put(activeId, activeId);
            continue;
          }
        } else {
          // e.g. a digit sequence directly before the next attribute is
          // invalid; do not silently swallow malformed data.
          throw new IllegalArgumentException("Malformed ECoS object ID at offset " + tokenStart);
        }
      } else if (activeId == null) {
        throw new IllegalArgumentException("Missing ECoS object ID at offset " + index);
      }

      int keyStart = index;
      if (!Character.isLetter(content.charAt(index))) {
        throw new IllegalArgumentException("Malformed ECoS attribute at offset " + index);
      }
      while (index < content.length()) {
        char ch = content.charAt(index);
        if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '-') {
          index++;
        } else {
          break;
        }
      }
      String key = content.substring(keyStart, index);
      while (index < content.length()
          && (content.charAt(index) == ' ' || content.charAt(index) == '\t')) {
        index++;
      }

      String value = null;
      boolean bracketed = index < content.length() && content.charAt(index) == '[';
      if (bracketed) {
        int valueStart = ++index;
        int valueEnd = findValueEnd(content, valueStart);
        if (valueEnd < 0) {
          throw new IllegalArgumentException("Unterminated ECoS attribute " + key);
        }
        value = unquote(content.substring(valueStart, valueEnd));
        index = valueEnd + 1;
      } else if (index < content.length()
          && content.charAt(index) != '\r' && content.charAt(index) != '\n') {
        // A bare attribute such as "1 list" is valid, but only at EOL.
        throw new IllegalArgumentException("Malformed ECoS attribute at offset " + keyStart + ": " + key);
      }

      final String objectId = activeId;
      Map<String, Object> destination;
      if (objectId.equals(rootId)) {
        destination = parsed;
        parsed.put(Ecos.ID, objectId);
      } else {
        @SuppressWarnings("unchecked")
        Map<String, Object> existing = (Map<String, Object>)
            parsed.computeIfAbsent(objectId, unused -> {
              Map<String, Object> object = new LinkedHashMap<>();
              object.put(Ecos.ID, objectId);
              return object;
            });
        destination = existing;
      }

      if (bracketed && (Ecos.FUNCTION.equals(key) || Ecos.FUNCTION_DESC.equals(key))) {
        Object previous = destination.get(key);
        destination.put(key, previous == null ? "[" + value + "]"
            : previous + ",[" + value + "]");
      } else {
        destination.put(key, value);
      }
    }
    valueMap = parsed;
    return parsed;
  }

  private static int findValueEnd(String text, int from) {
    boolean quoted = false;
    for (int i = from; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (ch == '"') {
        if (quoted && i + 1 < text.length() && text.charAt(i + 1) == '"') {
          i++;
        } else {
          quoted = !quoted;
        }
      } else if (ch == ']' && !quoted) {
        return i;
      }
    }
    return -1;
  }

  private static String unquote(String value) {
    String result = value.trim();
    if (result.length() >= 2 && result.startsWith("\"") && result.endsWith("\"")) {
      return result.substring(1, result.length() - 1).replace("\"\"", "\"");
    }
    return result;
  }

  public boolean isValid() {
    return getErrorCode() == 0;
  }

  @Override
  public String toString() {
    return "TX: " + message;
  }
}
