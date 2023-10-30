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
package jcs.controller.dccex;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author frans
 */
public class DccExMessage implements DccEx {

  private final String command;
  private final List<String> responses;

  public DccExMessage(String command) {
    this.command = command;
    this.responses = new ArrayList<>();
  }

  public String getCommand() {
    return command;
  }

  public String getFirstResponse() {
    if (this.responses.isEmpty()) {
      return null;
    } else {
      return this.responses.get(0);
    }
  }

  public String getFirstResponseContent() {
    if (this.responses.isEmpty()) {
      return null;
    } else {
      String first = this.responses.get(0);
      return first.substring(2).replaceAll(">", "");
    }
  }

  public void addResponse(String response) {
    this.responses.add(response);
  }

  public List<String> getResponses() {
    return responses;
  }

  public boolean isResponseReceived() {
    if (this.responses.isEmpty()) {
      return false;
    } else {
      String first = this.responses.get(0);
      return first.startsWith("<") && first.endsWith(">");
    }
  }

  public String getTXOpcode() {
    //Opcode is the character just after the first "<"
    int start = this.command.indexOf("<") + 1;
    int end = this.command.indexOf(" ");
    if (end == -1) {
      end = this.command.length() - 1;
    }
    String opcode = this.command.substring(start, end);
    return opcode;
  }

  public String getRXOpcode() {
    if (this.responses.isEmpty()) {
      return null;
    }
    String response = this.responses.get(0);
    int start = response.indexOf("<") + 1;
    int end = 2;
    String opcode = response.substring(start, end);
    return opcode;
  }

  public boolean isSystemMessage() {
    String txOpcode = this.getTXOpcode();
    String rxOpcode = this.getRXOpcode();

    if (txOpcode != null) {
      return SYSTEM_OPCODES.contains(txOpcode);
    }
    if (rxOpcode != null) {
      return SYSTEM_OPCODES.contains(rxOpcode);
    }

    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("TX: ");
    sb.append(command);
    sb.append(" # ");
    sb.append(this.responses.size());
    sb.append(" -> ");
    for (String r : this.responses) {
      sb.append(r);
      sb.append(" ");
    }
    return sb.toString();
  }

}
