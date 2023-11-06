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

import java.util.LinkedList;
import java.util.List;
import jcs.entities.AccessoryBean;
import org.json.JSONArray;
import org.tinylog.Logger;

/**
 * CS-3 only a JSON file is available (undocumented)
 */
public class AccessoryJSONParser {

  private final List<AccessoryBean> turnouts;
  private final List<AccessoryBean> signals;

  public AccessoryJSONParser() {
    turnouts = new LinkedList<>();
    signals = new LinkedList<>();
  }

  public void parseAccessories(String json) {
    this.signals.clear();
    this.turnouts.clear();

    JSONArray accArray = new JSONArray(json);
    for (int i = 0; i < accArray.length(); i++) {
      AccessoryBean ab = new AccessoryBean();

      ab.setName(accArray.getJSONObject(i).getString("name"));
      //TODO !
      //ab.setId(accArray.getJSONObject(i).getLong("id"));
      
      ab.setAddress(accArray.getJSONObject(i).getInt("address"));
      ab.setIcon(accArray.getJSONObject(i).getString("icon"));
      ab.setIconFile(accArray.getJSONObject(i).getString("iconFile"));
      ab.setType(accArray.getJSONObject(i).getString("typ"));
      ab.setGroup(accArray.getJSONObject(i).getString("group"));
      ab.setSwitchTime(accArray.getJSONObject(i).getInt("schaltzeit"));
      ab.setStates(accArray.getJSONObject(i).getInt("states"));
      ab.setPosition(accArray.getJSONObject(i).getInt("state"));

      if (accArray.getJSONObject(i).has("prot")) {
        ab.setDecoderType(accArray.getJSONObject(i).getString("prot"));
      }
      if (accArray.getJSONObject(i).has("dectyp")) {
        ab.setDecoder(accArray.getJSONObject(i).getString("dectyp"));
      }

      if (null == ab.getGroup()) {
        Logger.trace("Unknown Accessory: " + ab.toLogString());
      } else {
        switch (ab.getGroup()) {
          case "weichen" ->
            this.turnouts.add(ab);
          case "lichtsignale" ->
            this.signals.add(ab);
          default ->
            Logger.trace("Unknown Accessory: " + ab.toLogString());
        }
      }
    }
  }

  public List<AccessoryBean> getTurnouts() {
    return turnouts;
  }

  public List<AccessoryBean> getSignals() {
    return signals;
  }

  public List<AccessoryBean> getAccessories() {
    List<AccessoryBean> accessories = new LinkedList<>();
    accessories.addAll(signals);
    accessories.addAll(turnouts);
    return accessories;
  }

}
