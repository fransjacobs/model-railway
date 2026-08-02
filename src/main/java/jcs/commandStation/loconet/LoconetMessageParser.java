package jcs.commandStation.loconet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
/**
 *
 * @author fransjacobs
 */
public class LoconetMessageParser {

  private final List<Integer> buffer = new ArrayList<>();
  private int expectedLength = -1;

  Optional<LoconetMessage> accept(int rawByte) {
    int value = rawByte & 0xFF;

    if (buffer.isEmpty()) {
      if (!Opcodes.isOpcodeByte(value)) {
        return Optional.empty(); // garbage or mid-frame byte
      }

      buffer.add(value);

      Opcodes.MessageLengthKind kind = Opcodes.lengthKindFromOpcode(value);
      expectedLength = switch (kind) {
        case FIXED_2 ->
          2;
        case FIXED_4 ->
          4;
        case FIXED_6 ->
          6;
        case VARIABLE ->
          -1;
      };

      return Optional.empty();
    }

    buffer.add(value);

    if (expectedLength < 0 && buffer.size() == 2) {
      expectedLength = value & 0x7F;
      if (expectedLength < 3) {
        reset();
        return Optional.empty();
      }
    }

    if (expectedLength > 0 && buffer.size() == expectedLength) {
      int[] raw = buffer.stream().mapToInt(Integer::intValue).toArray();
      reset();

      try {
        return null; //Optional.of(LoconetMessage.fromReceived(raw));
      } catch (IllegalArgumentException ex) {
        // bad checksum or invalid frame; resync on next opcode
        return Optional.empty();
      }
    }

    return Optional.empty();
  }

  private void reset() {
    buffer.clear();
    expectedLength = -1;
  }

}
