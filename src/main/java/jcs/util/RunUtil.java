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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.tinylog.Logger;

@SuppressWarnings("unused")
public class RunUtil {

    public static final int OS_LINUX = 0;
    public static final int OS_WINDOWS = 1;
    public static final int OS_SOLARIS = 2;
    public static final int OS_MAC_OS_X = 3;

    private static int osType = -1;

    static {
        String osName = System.getProperty("os.name");
        String architecture = System.getProperty("os.arch");
        String userHome = System.getProperty("user.home");

        String fileSeparator = System.getProperty("file.separator");
        String tmpFolder = System.getProperty("java.io.tmpdir");

        String libRootFolder = new File(userHome).canWrite() ? userHome : tmpFolder;

        String javaLibPath = System.getProperty("java.library.path");

        if (osName.equals("Linux")) {
            osType = OS_LINUX;
        } else if (osName.startsWith("Win")) {
            osType = OS_WINDOWS;
        } else if (osName.equals("SunOS")) {
            osType = OS_SOLARIS;
        } else if (osName.equals("Mac OS X") || osName.equals("Darwin")) {
            osType = OS_MAC_OS_X;
        }

        switch (architecture) {
            case "i386", "i686" ->
                architecture = "x86";
            case "amd64", "universal" ->
                architecture = "x86_64";
            case "arm" -> {
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
                    } catch (IOException ex) {
                        // Do nothing
                    }
                }
                architecture = "arm" + floatStr;
            }
            default -> {
            }
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
