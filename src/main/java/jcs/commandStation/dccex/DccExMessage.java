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

  private final String command;
  private final List<String> responses;

  public DccExMessage(String command) {
    this.command = command;
    this.responses = new ArrayList<>();
  }

  public String getCommand() {
    return command;
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

  public boolean isSystemMessage() {
    String txOpcode = this.getTXOpcode();

    if (txOpcode != null) {
      return SYSTEM_OPCODES.contains(txOpcode);
    }

    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("TX: ");
    sb.append(command);
    return sb.toString();
  }

  public static String filterContent(String message) {
    if (message.length() > 1 && message.startsWith("<")) {
      String opcode = message.substring(1, 2);
      String content = message.replaceAll("<", "").replaceAll(">", "").replaceFirst(opcode, "");
      return content.trim();
    } else {
      return message;
    }

  }

}
