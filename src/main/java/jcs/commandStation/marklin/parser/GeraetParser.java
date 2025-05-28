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

import jcs.commandStation.entities.InfoBean;
import jcs.commandStation.marklin.cs.can.device.CanDevice;
import org.json.JSONObject;

/**
 * Parse an InfoBean from Marklin CS2/3 file.
 */
public class GeraetParser {

  /**
   * The quickest method to obtain basic information of the Central Station is to query the "geraet" file via the http interface.
   *
   * @param commandStationBean
   * @param geraetFile
   * @return
   */
  public static CanDevice parseFile(String geraetFile) {
    if (geraetFile == null) {
      return null;
    }
    
    CanDevice gfp = new CanDevice();
    //InfoBean ib = new InfoBean();
    //ib.copyInto(commandStationBean);

    String[] lines = geraetFile.split("\n");
    String minor = "";
    String major = "";
    for (String line : lines) {
      int eqidx = line.indexOf("=");
      if (eqidx == -1) {
        eqidx = line.length();
      }
      String key = line.substring(0, eqidx).trim();
      String value = line.substring(eqidx).replace("=", "").trim();
      
      switch (key) {
        case "[geraet]" -> {
          
        }
        case ".major" -> {
          major = value;
        }
        case ".minor" -> {
          minor = value;
        }
        case ".sernum" -> {
          //ib.setSerialNumber(value);
          gfp.setSerial(value);
        }
        case ".gfpuid" -> {
          //ib.setGfpUid(value);
          gfp.setUid(value);
        }
        case ".guiuid" -> {
          gfp.setGuiUid(value);
        }
        case ".hardvers" -> {
          //ib.setHardwareVersion(value);
          gfp.setHwVersion(value);
        }
        case ".articleno" -> {
          //ib.setArticleNumber(value);
          gfp.setArticleNumber(value);
        }
        case ".produkt" -> {
          //ib.setProductName(value);
          gfp.setName(value);
        }
      }
    }
    
    String softwareVersion = (major != null ? major : "") + (major != null ? "." : "") + (minor != null ? minor : "");
    //ib.setSoftwareVersion(softwareVersion);
    gfp.setVersion(softwareVersion);
    
    if (gfp.getSerial() != null & gfp.getSerial().length() < 5) {
      gfp.setSerial("0" + gfp.getSerial());
    }
    
    String shortName;
    if (gfp.getName() != null && gfp.getName().contains("Central Station 3")) {
      shortName = "CS3";
    } else {
      shortName = "CS2";
    }
    
    gfp.setShortName(shortName);
    
    gfp.setIdentifier("0x00");
    return gfp;
  }

  /**
   * The CS 3 has JSON files accessible via the web interface which contains lots of info about the CS
   *
   * @param json
   * @return
   */
  public static InfoBean parseJson(String json) {
    if (json == null) {
      return null;
    }
    
    InfoBean ib = new InfoBean();
    
    JSONObject infoObject = new JSONObject(json);
    ib.setSoftwareVersion(infoObject.optString("softwareVersion"));
    ib.setHardwareVersion(infoObject.optString("hardwareVersion"));
    ib.setSerialNumber(infoObject.optString("serialNumber"));
    ib.setProductName(infoObject.optString("productName"));
    ib.setArticleNumber(infoObject.optString("articleNumber"));
    ib.setHostname(infoObject.optString("hostname"));
    ib.setGfpUid(infoObject.optString("gfpUid"));
    ib.setGuiUid(infoObject.optString("guiUid"));
    
    return ib;
  }
  
}
