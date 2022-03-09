/*
 * Copyright (C) 2018 Frans Jacobs.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.tinylog.Logger;

@SuppressWarnings("unused")
public class RunUtil {

    public static final int OS_LINUX = 0;
    public static final int OS_WINDOWS = 1;
    public static final int OS_SOLARIS = 2;
    public static final int OS_MAC_OS_X = 3;

    private static int osType = -1;

    //private static Set<String> serialPorts = new HashSet<String>();
    static {
        String osName = System.getProperty("os.name");
        String architecture = System.getProperty("os.arch");
        String userHome = System.getProperty("user.home");

        String fileSeparator = System.getProperty("file.separator");
        String tmpFolder = System.getProperty("java.io.tmpdir");

        String libRootFolder = new File(userHome).canWrite() ? userHome : tmpFolder;

        String javaLibPath = System.getProperty("java.library.path");

        if (osName.equals("Linux")) {
            osName = "linux";
            osType = OS_LINUX;
        } else if (osName.startsWith("Win")) {
            osName = "windows";
            osType = OS_WINDOWS;
        } else if (osName.equals("SunOS")) {
            osName = "solaris";
            osType = OS_SOLARIS;
        } else if (osName.equals("Mac OS X") || osName.equals("Darwin")) {
            osName = "mac_os_x";
            osType = OS_MAC_OS_X;
        }

        if (architecture.equals("i386") || architecture.equals("i686")) {
            architecture = "x86";
        } else if (architecture.equals("amd64") || architecture.equals("universal")) {
            architecture = "x86_64";
        } else if (architecture.equals("arm")) {
            String floatStr = "sf";
            if (javaLibPath.toLowerCase().contains("gnueabihf") || javaLibPath.toLowerCase().contains("armhf")) {
                floatStr = "hf";
            } else {
                try {
                    Process readelfProcess = Runtime.getRuntime().exec("readelf -A /proc/self/exe");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(readelfProcess.getInputStream()));
                    String buffer = "";
                    while ((buffer = reader.readLine()) != null && !buffer.isEmpty()) {
                        if (buffer.toLowerCase().contains("Tag_ABI_VFP_args".toLowerCase())) {
                            floatStr = "hf";
                            break;
                        }
                    }
                    reader.close();
                } catch (Exception ex) {
                    // Do nothing
                }
            }
            architecture = "arm" + floatStr;
        }

        // Preference storage
        // Mac OS ~/Library/Preferences/com.apple.java.util.prefs.plist
        //Logger.trace("Running on OS: " + osName + " architecture: " + architecture + ".");
    }

    /**
     * Get OS type (OS_LINUX || OS_WINDOWS || OS_SOLARIS || OS_MAC_OS_X)
     *
     *
     * @return
     */
    public static int getOsType() {
        return osType;
    }

//    public static boolean hasSerialPort() {
//        return !serialPorts.isEmpty();
//    }

    public static void loadProperties() {
        Properties prop = new Properties();
        InputStream inputStream = RunUtil.class.getClassLoader().getResourceAsStream("jcs.properties");
        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException ex) {
                Logger.error("Can't read jcs.properties");
            }
        }

        for (Object pk : prop.keySet()) {
            String key = (String) pk;
            String value = (String) prop.get(pk);
            if (System.getProperty(key) == null) {
                System.setProperty(key, value);
            }
        }
    }

}
