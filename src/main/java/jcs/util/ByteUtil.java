/*
 * Copyright 2023 Frans Jacobs.
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
package jcs.util;

import java.util.ArrayList;
import java.util.List;

public class ByteUtil {

  public static long toLong(byte[] value) {
    long val = -1;
    if (value != null) {
      val = switch (value.length) {
        case 2 ->
          ((value[0] & 0xFF) << 8) | (value[1] & 0xFF);
        case 4 ->
          ((value[0] & 0xFF) << 24)
          | ((value[1] & 0xFF) << 16)
          | ((value[2] & 0xFF) << 8)
          | (value[3] & 0xFF);
        default ->
          0;
      };
    }
    return val;
  }

  public static int toInt(byte[] value) {
    return (int) toLong(value);
  }

  public static long toLong(int[] value) {
    long val = -1;
    if (value != null) {
      val = switch (value.length) {
        case 2 ->
          ((value[0] & 0xFF) << 8) | (value[1] & 0xFF);
        case 4 ->
          ((value[0] & 0xFF) << 24)
          | ((value[1] & 0xFF) << 16)
          | ((value[2] & 0xFF) << 8)
          | (value[3] & 0xFF);
        default ->
          0;
      };
    }
    return val;
  }

  public static int toInt(int[] value) {
    return (int) toLong(value);
  }

  public static int[] to2ByteArray(int value) {
    int[] bts = new int[]{
      (value >> 8) & 0xFF,
      value & 0XFF};

    return bts;
  }

  public static int[] to4ByteArray(int value) {
    int[] bts = new int[]{
      (value >> 24) & 0xFF,
      (value >> 16) & 0xFF,
      (value >> 8) & 0xFF,
      value & 0XFF};

    return bts;
  }

  public static String toHexString(int b) {
    String h = Integer.toHexString((b & 0xff));
    if (h.length() == 1) {
      h = "0" + h;
    }
    return h;
  }

  public static String toHexString(byte[] bytes) {
    if (bytes == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < bytes.length; i++) {
      sb.append("0x");
      sb.append(toHexString(bytes[i]));
      if (i + 1 < bytes.length) {
        sb.append(" ");
      }
    }
    return sb.toString();
  }

  public static String toHexString(int[] bytes) {
    if (bytes == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < bytes.length; i++) {
      sb.append("0x");
      sb.append(toHexString(bytes[i]));
      if (i + 1 < bytes.length) {
        sb.append(" ");
      }
    }
    return sb.toString();
  }

  public static String bytesToString(byte[] data) {
    List<Byte> bl = new ArrayList<>();
    for (int i = 0; i < data.length; i++) {
      if (data[i] > 0) {
        bl.add(data[i]);
      }
    }
    byte[] bytes = new byte[bl.size()];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = bl.get(i);
    }
    return new String(bytes);
  }

}
