/*
 * Copyright (C) 2022 fransjacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fransjacobs
 */
public class ByteUtil {

    public static int toInt(int[] value) {
        int val = -1;
        if (value != null) {
            switch (value.length) {
                case 2:
                    val = ((value[0] & 0xFF) << 8) | (value[1] & 0xFF);
                    break;
                case 4:
                    val = ((value[0] & 0xFF) << 24)
                            | ((value[1] & 0xFF) << 16)
                            | ((value[2] & 0xFF) << 8)
                            | (value[3] & 0xFF);
                    break;
                default:
                    val = 0;
                    break;
            }
        }
        return val;
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
