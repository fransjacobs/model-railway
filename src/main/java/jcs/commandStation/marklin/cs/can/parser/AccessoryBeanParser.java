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
package jcs.commandStation.marklin.cs.can.parser;

import java.util.LinkedList;
import java.util.List;
import jcs.entities.AccessoryBean;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class AccessoryBeanParser {
  
  public final static String MAGNETARTIKEL = "magnetartikel.cs2";
  public final static String MAGS_JSON = "mags.json";
  
  public static List<AccessoryBean> parseAccessoryFile(String file, String commandStationId, String source) {
    List<AccessoryBean> accessories = new LinkedList<>();
    String[] lines = file.split("\n");
    
    AccessoryBean ab = new AccessoryBean();
    for (String line : lines) {
      int eqidx = line.indexOf("=");
      if (eqidx == -1) {
        eqidx = line.length();
      }
      String key = line.substring(0, eqidx).trim();
      String value = line.substring(eqidx).replace("=", "").trim();
      
      Logger.trace("key: " + key + " value: " + value);
      switch (key) {
        case "[magnetartikel]" -> {
          //Start of the file
        }
        case "version" -> {
          //version of the file
        }
        case ".minor" -> {
          //Minor version of the file
        }
        case "artikel" -> {
          //Start of a new article, save the previous if applicable
          if (ab.getId() != null) {
            //TODO: Derive the icon....
            accessories.add(ab);
          }
          ab = new AccessoryBean();
          ab.setSynchronize(true);
          ab.setSource(source + ":" + MAGNETARTIKEL);
          ab.setCommandStationId(commandStationId);
        }
        case ".name" -> {
          ab.setName(value);
        }
        case ".id" -> {
          ab.setId(value);
          int addr = Integer.parseInt(value);
          ab.setAddress(addr);
        }
        case ".typ" -> {
          ab.setType(value);
          ab.setStates(deriveStates(value));
          if (ab.isBiAddress()) {
            ab.setAddress2(ab.getAddress() + 1);
          }
        }
        case ".stellung" -> {
          int pos = Integer.parseInt(value);
          ab.setState(pos);
        }
        case ".schaltzeit" -> {
          int st = Integer.parseInt(value);
          ab.setSwitchTime(st);
        }
        case ".dectyp" -> {
          if (value.toLowerCase().contains("mm")) {
            ab.setDecType("mm");
          } else {
            ab.setDecType("dcc");
          }
        }
        case ".decoder" -> {
          ab.setDecoder(value);
        }
      }
    }
    accessories.add(ab);
    return accessories;
  }
  
  private static int deriveStates(String type) {
    return switch (type) {
      case "std_rot_gruen" ->
        2;
      case "std_rot" ->
        1;
      case "std_gruen" ->
        1;
      case "rechtsweiche" ->
        2;
      case "linksweiche" ->
        2;
      case "y_weiche" ->
        2;
      case "dreiwegweiche" ->
        3;
      case "entkupplungsgleis" ->
        1;
      case "entkupplungsgleis_1" ->
        1;
      case "lichtsignal_HP01" ->
        2;
      case "lichtsignal_HP02" ->
        2;
      case "lichtsignal_HP012" ->
        3;
      case "lichtsignal_HP012_SH01" ->
        4;
      case "lichtsignal_SH01" ->
        2;
      case "formsignal_HP01" ->
        2;
      case "formsignal_HP02" ->
        2;
      case "formsignal_HP012" ->
        3;
      case "formsignal_HP012_SH01" ->
        4;
      case "formsignal_SH01" ->
        2;
      case "urc_lichtsignal_HP01" ->
        2;
      case "urc_lichtsignal_HP012" ->
        3;
      case "urc_lichtsignal_HP012_SH01" ->
        4;
      case "urc_lichtsignal_SH01" ->
        2;
      default ->
        2;
    };
  }
  
  public static List<AccessoryBean> parseAccessoryJSON(String json, String commandStationId, String source) {
    List<AccessoryBean> accessories = new LinkedList<>();
    JSONArray aa = new JSONArray(json);
    for (int i = 0; i < aa.length(); i++) {
      AccessoryBean ab = new AccessoryBean();
      ab.setSynchronize(true);
      ab.setSource(source + ":" + MAGS_JSON);
      ab.setCommandStationId(commandStationId);
      
      JSONObject ajo = aa.getJSONObject(i);
      
      ab.setName(ajo.getString("name"));
      
      ab.setId(ajo.getInt("id") + "");
      
      ab.setAddress(ajo.optInt("address"));
      
      ab.setIcon(ajo.optString("icon"));
      ab.setIconFile(ajo.optString("iconFile"));
      
      ab.setType(ajo.getString("typ"));
      ab.setGroup(ajo.getString("group"));
      
      ab.setSwitchTime(ajo.optInt("schaltzeit"));
      ab.setStates(ajo.getInt("states"));
      if (ab.isBiAddress()) {
        ab.setAddress2(ab.getAddress() + 1);
      }
      
      ab.setState(ajo.getInt("state"));
      
      ab.setDecType(ajo.optString("prot"));
      ab.setDecoder(ajo.optString("dectyp"));

      //For now JCS only support Turnout and Signals
      String grp = ab.getGroup();
      if (grp.equals("weichen") || grp.equals("lichtsignale") | grp.equals("formsignale")) {
        accessories.add(ab);
      }
    }
    
    return accessories;
  }
  
}
