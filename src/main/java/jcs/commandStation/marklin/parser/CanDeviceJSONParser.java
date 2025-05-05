/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.commandStation.marklin.parser;

import java.util.ArrayList;
import java.util.List;
import jcs.commandStation.marklin.cs.can.device.CanDevice;
import jcs.commandStation.marklin.cs.can.device.ConfigChannel;
import jcs.commandStation.marklin.cs.can.device.MeasuringChannel;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * CS 3 supports JSON
 */
public class CanDeviceJSONParser {

  public static final String LINK_S88 = "LinkS88-1";
  public static final String GFP = "GFP3-1";
  public static final String CS3 = "CS3 0000";

  public CanDeviceJSONParser() {
  }

  public static List<CanDevice> parse(String json) {
    JSONObject devicesJO = new JSONObject(json);
    String[] ids = JSONObject.getNames(devicesJO);
    List<CanDevice> devices = new ArrayList<>();
    for (String id : ids) {
      JSONObject jo = devicesJO.getJSONObject(id);
      String name = jo.optString("_name");
      if (name.equals(LINK_S88) || name.equals(GFP) || name.equals(CS3)) {
        CanDevice d = parseDevice(devicesJO.getJSONObject(id));
        devices.add(d);
      }
    }

    return devices;
  }

  private static CanDevice parseDevice(JSONObject jo) {
    CanDevice d = new CanDevice();
    d.setUid(jo.optString("_uid"));
    d.setName(jo.optString("_name"));
    d.setIdentifier(jo.optString("_kennung"));
    d.setArticleNumber(jo.optString("_artikelnr"));
    d.setSerial(jo.optString("_seriennr"));

    JSONObject versionObj = jo.optJSONObject("_version");
    if (versionObj != null) {
      String major = versionObj.optString("major");
      String minor = versionObj.optString("minor");
      d.setVersion((major != null ? major : "") + (major != null ? "." : "") + (minor != null ? minor : ""));
    }

    JSONArray channelsJA = jo.optJSONArray("_kanal");
    if (channelsJA != null) {
      for (int i = 0; i < channelsJA.length(); i++) {
        JSONObject cjo = channelsJA.getJSONObject(i);

        if (cjo.has("auswahl")) {
          ConfigChannel cc = parseConfigChannel(cjo);
          d.addConfigChannel(cc);
        } else {
          MeasuringChannel mc = parseMeasuringChannel(cjo);
          d.addMeasuringChannel(mc);
        }
      }
      d.setMeasureChannelCount(d.getMeasuringChannels().size());
      d.setConfigChannelCount(d.getConfigChannels().size());
    }
    return d;
  }

  private static MeasuringChannel parseMeasuringChannel(JSONObject jo) {
    MeasuringChannel mc = new MeasuringChannel();
    mc.setName(jo.optString("name"));
    mc.setNumber(jo.optInt("nr"));
    Integer scale = jo.optInt("potenz");
    if (scale > 0) {
      scale = scale - 256;
    }
    mc.setScale(scale);
    mc.setUnit(jo.optString("einheit"));
    mc.setEndValue(jo.optDouble("endWert"));
    mc.setStartValue(jo.optDouble("startWert"));
    mc.setColorGreen(jo.optInt("farbeGruen"));
    mc.setColorYellow(jo.optInt("farbeGelb"));
    mc.setColorRed(jo.optInt("farbeRot"));
    mc.setColorMax(jo.optInt("farbeMax"));
    mc.setRangeGreen(jo.optInt("rangeGruen"));
    mc.setRangeYellow(jo.optInt("rangeGelb"));
    mc.setRangeRed(jo.optInt("rangeRot"));
    mc.setRangeMax(jo.optInt("rangeMax"));
    mc.setZeroPoint(jo.optInt("valueHuman"));
    return mc;
  }

  private static ConfigChannel parseConfigChannel(JSONObject jo) {
    ConfigChannel cc = new ConfigChannel();

    String choice = jo.optString("auswahl");
    String choices[] = choice.split(":");
    for (String c : choices) {
      cc.addChoice(c);
    }
    cc.setChoicesCount(choices.length);
    cc.setValueId(jo.optInt("index"));

    cc.setNumber(jo.optInt("nr"));
    cc.setChoiceDescription(jo.optString("name"));

    String unit = jo.optString("einheit");
    if (!"".equals(unit)) {
      cc.setUnit(unit);
    }
    cc.setHighValue(jo.optInt("endWert"));
    cc.setLowValue(jo.optInt("startWert"));
    cc.setActualValue(jo.optInt("wert"));
    return cc;
  }

}
