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
package jcs.commandStation.loconet;

/**
 *
 * @author fransjacobs
 */
public class LoconetCommand {
  
  
  public static byte calculateChecksum(byte[] packet) {
    int xorSum = 0;
    // XOR all bytes except the last placeholder array item
    for (int i = 0; i < packet.length - 1; i++) {
        xorSum ^= packet[i];
    }
    // Bitwise NOT, constrained to 7-bit values
    return (byte) ((~xorSum) & 0x7F);
}
  
}
