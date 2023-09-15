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
package jcs.controller.cs3;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import org.json.JSONArray;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class LocomotiveBeanJSONParser {

  private final List<LocomotiveBean> locomotives;

  public LocomotiveBeanJSONParser() {
    locomotives = new LinkedList<>();
  }

  public List<LocomotiveBean> parseLocomotives(String json) {
    locomotives.clear();
    JSONArray locArray = new JSONArray(json);

    Logger.trace("Parsing " + locArray.length() + " locomotives");

    for (int i = 0; i < locArray.length(); i++) {
      LocomotiveBean lb = new LocomotiveBean();

      String uid = locArray.getJSONObject(i).getString("uid");
      Long id = Long.decode(uid);

      lb.setId(id);
      lb.setName(locArray.getJSONObject(i).optString("name"));
      lb.setPreviousName(locArray.getJSONObject(i).optString("internname"));
      lb.setUid(id);

      //appearantly the last 2 bytes van uid, so 0x4017 -> 0x17
      if (uid != null && uid.length() > 2) {
        String mfxSid = uid.substring(uid.length() - 2);
        lb.setMfxSid("0x" + mfxSid);
      }

      lb.setAddress(locArray.getJSONObject(i).getInt("address"));

      String icon = locArray.getJSONObject(i).optString("icon");
      if (icon != null && icon.length() > 1) {
        icon = icon.substring(icon.lastIndexOf("/") + 1);
        lb.setIcon(icon);
      }

      lb.setDecoderTypeString(locArray.getJSONObject(i).getString("dectyp"));

      lb.setMfxSid(locArray.getJSONObject(i).getString("uid"));

      lb.setTachoMax(locArray.getJSONObject(i).getJSONObject("tachoLabels").getInt("speed"));

      lb.setvMin(locArray.getJSONObject(i).optInt("vmin"));

      lb.setAccelerationDelay(locArray.getJSONObject(i).optInt("av"));
      lb.setBrakeDelay(locArray.getJSONObject(i).optInt("bv"));
      lb.setVolume(locArray.getJSONObject(i).optInt("volume"));

      lb.setSpm(locArray.getJSONObject(i).optString("spm", null));

      lb.setVelocity(locArray.getJSONObject(i).getInt("speed"));
      lb.setRichtung(locArray.getJSONObject(i).getInt("dir"));

      //Can't find these in the JSON, should it really be stored?
      //lb.setMfxType(locArray.getJSONObject(i).getString(""));
      //lb.setBlock(locArray.getJSONObject(i).getString(""));
      lb.setImported("CS-3 JSON");

      JSONArray functionsArray = locArray.getJSONObject(i).optJSONArray("funktionen");
      if (functionsArray != null) {
        for (int j = 0; j < functionsArray.length(); j++) {
          FunctionBean fb = new FunctionBean();

          fb.setLocomotiveId(id);
          fb.setNumber(functionsArray.getJSONObject(j).optInt("nr", 0));

          int typ = functionsArray.getJSONObject(j).optInt("typ");
          int typ2 = functionsArray.getJSONObject(j).optInt("typ2");
          if (typ2 >= typ) {
            fb.setFunctionType(typ2);
          } else {
            fb.setFunctionType(typ);
          }
          fb.setMomentary(functionsArray.getJSONObject(j).optBoolean("isMoment"));

          fb.setIcon(functionsArray.getJSONObject(j).optString("icon"));

          fb.setValue(functionsArray.getJSONObject(j).optInt("state"));
          //   
          //TODO: Currently not yet supported, I also do not have a valid example...
          //"fs": 0,
          //"dauer": 0,
          //

          //Most mm loks have default functions 0, 3 and 4. So when ther is no icon set set a default one
          if (fb.getFunctionType() != null && fb.getFunctionType() == 0 && fb.getNumber() == 0 && (fb.getIcon() == null || "".equals(fb.getIcon()))) {
            String iconname = "fkticon_" + (fb.isOn() ? "a_" : "i_") + "001";
            fb.setFunctionType(1);
            fb.setIcon(iconname);
          }
          if (fb.getFunctionType() != null && fb.getFunctionType() == 0 && fb.getNumber() == 3 && (fb.getIcon() == null || "".equals(fb.getIcon()))) {
            String iconname = "fkticon_" + (fb.isOn() ? "a_" : "i_") + "008";
            fb.setFunctionType(8);
            fb.setIcon(iconname);
          }
          if (fb.getFunctionType() != null && fb.getFunctionType() == 0 && fb.getNumber() == 4 && (fb.getIcon() == null || "".equals(fb.getIcon()))) {
            String iconname = "fkticon_" + (fb.isOn() ? "a_" : "i_") + "018";
            fb.setFunctionType(18);
            fb.setIcon(iconname);
          }

          if (fb.getFunctionType() != null && fb.getFunctionType() > 0) {
            lb.addFunction(fb);
          }
        }
      }
      this.locomotives.add(lb);
    }
    return this.locomotives;
  }

  public static void main(String[] a) throws Exception {
    Path path = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "locomotives.json");

    String loksFile = Files.readString(path);

    LocomotiveBeanJSONParser lp = new LocomotiveBeanJSONParser();
    List<LocomotiveBean> locs = lp.parseLocomotives(loksFile);

    for (LocomotiveBean loc : locs) {
      Logger.trace(loc);
    }
  }

}
