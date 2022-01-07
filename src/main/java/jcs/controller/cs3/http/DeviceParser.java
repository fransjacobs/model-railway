/*
 * Copyright (C) 2020 fransjacobs.
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
package jcs.controller.cs3.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcs.controller.cs3.DeviceInfo;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class DeviceParser {

    private static final String DEVICE_START = "[geraet]";
    private static final String VERSION = "version";
    private static final String MINOR = ".minor";
    private static final String MAJOR = ".major";
    private static final String DEVICE = "geraet";
    private static final String SERIAL = ".sernum";
    private static final String GFP_UID = ".gfpuid";
    private static final String GUI_UID = ".guiuid";
    private static final String HARDWARE_VERSION = ".hardvers";

    public DeviceInfo parseAccessoryFile(String gafile) {
        List<String> items = Arrays.asList(gafile.split("\n"));
        Map<String, String> keyPairs = new HashMap<>();
        for (String s : items) {
            //Logger.trace("Line: " + s);
            switch (s) {
                case DEVICE_START:
                    keyPairs.clear();
                    break;
                case DEVICE:
                    break;
                case VERSION:
                    break;
                case MINOR:
                    break;
                case MAJOR:
                    break;
                default:
                    if (s.contains("=")) {
                        String[] kp = s.split("=");
                        String key = kp[0];
                        String val = kp[1];
                        keyPairs.put(key, val);
                    } else {
                        Logger.debug("Tag?: " + s);
                    }
                    break;
            }
        }

        String gfpUid = keyPairs.get(GFP_UID);
        String guiUid = keyPairs.get(GUI_UID);
        String hardwareVersion = keyPairs.get(HARDWARE_VERSION);
        String serialNumber = keyPairs.get(SERIAL);

        DeviceInfo deviceInfo = new DeviceInfo(gfpUid, guiUid, hardwareVersion, serialNumber);

        return deviceInfo;
    }

}
