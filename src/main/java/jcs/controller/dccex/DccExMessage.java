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
public class DccExMessage {

  private final String command;
  private List<String> responses;

  public DccExMessage(String command) {
    this.command = command;
    this.responses = new ArrayList<>();
  }

  public String getCommand() {
    return command;
  }

  public String getFirstResponse() {
    return this.responses.get(0);
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
