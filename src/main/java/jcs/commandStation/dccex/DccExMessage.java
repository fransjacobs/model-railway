/*
 * Copyright 2023 frans.
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
package jcs.commandStation.dccex;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author frans
 */
public class DccExMessage implements DccEx {

  private final String message;

  public DccExMessage(byte[] message) {
    this(new String(message).replace("\n", "").replace("\r", ""));
  }

  public DccExMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public String getOpcode() {
    int start = message.indexOf("<") + 1;
    return message.substring(start, start + 1);
  }

  public boolean isSystemMessage() {
    String opcode = this.getOpcode();

    if (opcode != null) {
      return SYSTEM_OPCODES.contains(opcode);
    }

    return false;
  }

  public boolean isValid() {
    return message.startsWith("<") && message.endsWith(">");
  }

  public boolean isNormalMessage() {
    return this.message.substring(0, 1).equals("<") && !this.message.substring(1, 1).equals("*");
  }

  public boolean isDiagnosticMessage() {
    return this.message.substring(0, 2).equals("<*");
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("TX: ");
    sb.append(message);
    return sb.toString();
  }

  public String getFilteredContent() {
    if (message.length() > 1 && message.startsWith("<")) {
      String opcode = message.substring(1, 2);
      String content = message.replaceAll("<", "").replaceAll(">", "").replaceFirst(opcode, "");
      return content.trim();
    } else {
      return message;
    }
  }

  public String getFilteredDiagnosticMessage() {
    if (message.length() > 2 && message.startsWith("<*")) {
      String content = message.replaceAll("<*", "").replaceAll("\\*>", "");
      return content.trim();
    } else {
      return message;
    }
  }
}
