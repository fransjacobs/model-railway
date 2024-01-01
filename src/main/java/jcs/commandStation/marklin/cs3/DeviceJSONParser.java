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
package jcs.commandStation.marklin.cs3;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import jcs.entities.DeviceBean;
import org.json.JSONObject;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class DeviceJSONParser {

  public DeviceJSONParser() {
  }

  public static List<DeviceBean> parse(String json) {
    JSONObject devicesJO = new JSONObject(json);
    String[] names = JSONObject.getNames(devicesJO);
    List<DeviceBean> devices = new ArrayList<>(names.length);
    for (String n : names) {
      JSONObject dev = devicesJO.getJSONObject(n);
      DeviceBean db = new DeviceBean(dev.toString());
      devices.add(db);
    }

    return devices;
  }

  public static void main(String[] a) throws Exception {
    Path path = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "devices.json");

    String devicesFile = Files.readString(path);

    List<DeviceBean> devices = DeviceJSONParser.parse(devicesFile);

    for (DeviceBean dev : devices) {
      Logger.trace(dev);
    }
  }
}
