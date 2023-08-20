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
package jcs.controller.cs3.http;

import jcs.controller.cs.Device;
import jcs.controller.cs.SxxBus;
import jcs.controller.cs.MeasurementChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class DeviceJSONParser {

  private Device cs3;
  private Device gfp;
  private Device linkSxx;

  public DeviceJSONParser() {

  }

  private void parseCannels(JSONArray channelArray) {
    int j = -1;
    for (int i = 0; i < channelArray.length(); i++) {
      MeasurementChannel c = new MeasurementChannel();
      if (channelArray.getJSONObject(i).has("einheit")) {
        c.setUnit(channelArray.getJSONObject(i).getString("einheit"));
      }
      if (channelArray.getJSONObject(i).has("startWert")) {
        c.setStartValue(channelArray.getJSONObject(i).getDouble("startWert"));
      }
      if (channelArray.getJSONObject(i).has("endWert")) {
        c.setEndValue(channelArray.getJSONObject(i).getDouble("endWert"));
      }
      if (channelArray.getJSONObject(i).has("value")) {
        c.setValue(channelArray.getJSONObject(i).getInt("value"));
      }
      if (channelArray.getJSONObject(i).has("valueHuman")) {
        c.setHumanValue(channelArray.getJSONObject(i).getDouble("valueHuman"));
      }
      if (channelArray.getJSONObject(i).has("farbeGelb")) {
        c.setColorYellow(channelArray.getJSONObject(i).getInt("farbeGelb"));
      }
      if (channelArray.getJSONObject(i).has("farbeGruen")) {
        c.setColorGreen(channelArray.getJSONObject(i).getInt("farbeGruen"));
      }
      if (channelArray.getJSONObject(i).has("farbeRot")) {
        c.setColorRed(channelArray.getJSONObject(i).getInt("farbeRot"));
      }
      if (channelArray.getJSONObject(i).has("farbeMax")) {
        c.setColorMax(channelArray.getJSONObject(i).getInt("farbeMax"));
      }
      if (channelArray.getJSONObject(i).has("name")) {
        c.setName(channelArray.getJSONObject(i).getString("name"));
      }
      if (channelArray.getJSONObject(i).has("nr")) {
        c.setNumber(channelArray.getJSONObject(i).getInt("nr"));
      }
      if (channelArray.getJSONObject(i).has("potenz")) {
        c.setScale(channelArray.getJSONObject(i).getInt("potenz"));
      }

      if (j != i) {
        this.gfp.setChannel(c);
        j = i;
      }
    }
  }

  private void parseBusses(JSONArray bussesArray) {
    int j = -1;
    for (int i = 0; i < bussesArray.length(); i++) {
      SxxBus b = new SxxBus();
      if (bussesArray.getJSONObject(i).has("endWert")) {
        b.setEndValue(bussesArray.getJSONObject(i).getInt("endWert"));
      }
      if (bussesArray.getJSONObject(i).has("max")) {
        b.setMax(bussesArray.getJSONObject(i).getInt("max"));
      }
      if (bussesArray.getJSONObject(i).has("name")) {
        b.setName(bussesArray.getJSONObject(i).getString("name"));
      }
      if (bussesArray.getJSONObject(i).has("nr")) {
        b.setNumber(bussesArray.getJSONObject(i).getInt("nr"));
      }
      if (bussesArray.getJSONObject(i).has("startWert")) {
        b.setStartValue(bussesArray.getJSONObject(i).getInt("startWert"));
      }
      if (bussesArray.getJSONObject(i).has("typ")) {
        b.setType(bussesArray.getJSONObject(i).getInt("typ"));
      }
      if (bussesArray.getJSONObject(i).has("wert")) {
        b.setLength(bussesArray.getJSONObject(i).getInt("wert"));
      }

      if (j != i) {
        this.linkSxx.addSxxBus(b);
        j = i;
      }
    }
  }

  public void parseDevices(String json) {
    JSONObject jsonObject = new JSONObject(json);

    JSONArray gfpArray = jsonObject.getJSONArray("gfp");
    if (gfpArray.length() > 1) {
      Logger.warn("Found " + gfpArray.length() + " GFP devices, which is currently NOT supported!");
    }

    for (int i = 0; i < gfpArray.length(); i++) {
      this.gfp = new Device();
      int gfpUid = Integer.decode(gfpArray.getJSONObject(i).getString("_uid"));
      gfp.setUid(gfpUid);

      gfp.setName(gfpArray.getJSONObject(i).getString("_name"));
      gfp.setTypeName(gfpArray.getJSONObject(i).getString("_typname"));
      gfp.setIdentifier(gfpArray.getJSONObject(i).getString("_kennung"));

      gfp.setDeviceId(gfpArray.getJSONObject(i).getInt("_typ"));
      gfp.setArticleNumber(gfpArray.getJSONObject(i).getString("_artikelnr"));

      gfp.setSerialNumber(gfpArray.getJSONObject(i).getString("_seriennr"));
      gfp.setQueryInterval(gfpArray.getJSONObject(i).getInt("_queryInterval"));

      String major = gfpArray.getJSONObject(i).getJSONObject("_version").getString("major");
      String minor = gfpArray.getJSONObject(i).getJSONObject("_version").getString("minor");
      gfp.setVersion(major + "." + minor);

      JSONArray chanArr = gfpArray.getJSONObject(i).getJSONArray("_kanal");
      parseCannels(chanArr);
    }

    JSONArray linkSxxArray = jsonObject.getJSONArray("linkSxx");
    if (linkSxxArray.length() > 1) {
      Logger.warn("Found " + linkSxxArray.length() + " linkSxx devices, which is currently NOT supported!");
    }

    for (int i = 0; i < linkSxxArray.length(); i++) {
      this.linkSxx = new Device();
      if (linkSxxArray.getJSONObject(i).has("_uid")) {
        int lnkpUid = Integer.decode(linkSxxArray.getJSONObject(i).getString("_uid"));
        linkSxx.setUid(lnkpUid);
      }
      if (linkSxxArray.getJSONObject(i).has("_name")) {
        linkSxx.setName(linkSxxArray.getJSONObject(i).getString("_name"));
      }
      if (linkSxxArray.getJSONObject(i).has("_typname")) {
        linkSxx.setTypeName(linkSxxArray.getJSONObject(i).getString("_typname"));
      }
      if (linkSxxArray.getJSONObject(i).has("_kennung")) {
        linkSxx.setIdentifier(linkSxxArray.getJSONObject(i).getString("_kennung"));
      }
      if (linkSxxArray.getJSONObject(i).has("_typ")) {
        linkSxx.setDeviceId(linkSxxArray.getJSONObject(i).getInt("_typ"));
      }
      if (linkSxxArray.getJSONObject(i).has("_artikelnr")) {
        linkSxx.setArticleNumber(linkSxxArray.getJSONObject(i).getString("_artikelnr"));
      }
      if (linkSxxArray.getJSONObject(i).has("_seriennr")) {
        linkSxx.setSerialNumber(linkSxxArray.getJSONObject(i).getString("_seriennr"));
      }
      if (linkSxxArray.getJSONObject(i).has("_queryInterval")) {
        linkSxx.setQueryInterval(linkSxxArray.getJSONObject(i).getInt("_queryInterval"));
      }

      if (linkSxxArray.getJSONObject(i).has("_version")) {
        String major = linkSxxArray.getJSONObject(i).getJSONObject("_version").getString("major");
        String minor = linkSxxArray.getJSONObject(i).getJSONObject("_version").getString("minor");
        linkSxx.setVersion(major + "." + minor);
      }

      if (linkSxxArray.getJSONObject(i).has("_kanal")) {
        JSONArray busArr = linkSxxArray.getJSONObject(i).getJSONArray("_kanal");
        parseBusses(busArr);
      }
    }

    JSONArray csNewArray = jsonObject.getJSONArray("csNew");
    if (csNewArray.length() > 1) {
      Logger.warn("Found " + csNewArray.length() + " CS 3 devices, which is currently NOT supported!");
    }

    for (int i = 0; i < csNewArray.length(); i++) {
      this.cs3 = new Device();
      if (csNewArray.getJSONObject(i).has("_uid")) {
        int cs3Uid = Integer.decode(csNewArray.getJSONObject(i).getString("_uid"));
        cs3.setUid(cs3Uid);
      }
      if (csNewArray.getJSONObject(i).has("_name")) {
        cs3.setName(csNewArray.getJSONObject(i).getString("_name"));
      }
      if (csNewArray.getJSONObject(i).has("_typ")) {
        cs3.setArticleNumber(csNewArray.getJSONObject(i).getString("_typ"));
      }
      if (csNewArray.getJSONObject(i).has("isPresent")) {
        cs3.setPresent(csNewArray.getJSONObject(i).getBoolean("isPresent"));
      }
    }
  }

  public Device getCs3() {
    return cs3;
  }

  public Device getGfp() {
    return gfp;
  }

  public Device getLinkSxx() {
    return linkSxx;
  }
}
