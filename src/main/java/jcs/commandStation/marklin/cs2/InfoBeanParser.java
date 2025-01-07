/*
 * Copyright 2024 Frans Jacobs.
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
package jcs.commandStation.marklin.cs2;

import jcs.commandStation.entities.InfoBean;
import org.json.JSONObject;

/**
 * Parse an InfoBean from Marklin CS2 file or CS 3 JSON.
 */
public class InfoBeanParser {

  public static InfoBean parseFile(String file) {
    if (file == null) {
      return null;
    }

    InfoBean ib = new InfoBean();

    String[] lines = file.split("\n");
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
          //this.serialNumber = value;
          ib.setSerialNumber(value);
        }
        case ".gfpuid" -> {
          //this.gfpUid = value;
          ib.setGfpUid(value);
        }
        case ".guiuid" -> {
          //this.guiUid = value;
          ib.setGuiUid(value);
        }
        case ".hardvers" -> {
          //this.hardwareVersion = value;
          ib.setHardwareVersion(value);
        }
        case ".articleno" -> {
          //this.articleNumber = value;
          ib.setArticleNumber(value);
        }
        case ".produkt" -> {
          //this.productName = value;
          ib.setProductName(value);
        }
      }
    }

    String softwareVersion = (major != null ? major : "") + (major != null ? "." : "") + (minor != null ? minor : "");
    ib.setSoftwareVersion(softwareVersion);

    String shortName;
    //String sn = ib.getSerialNumber();
    if (ib.getProductName() != null && ib.getProductName().contains("Central Station 3")) {
      shortName = "CS3";
    } else {
      shortName = "CS2";
    }
    if (ib.getSerialNumber().length() < 5) {
      ib.setSerialNumber("0" + ib.getSerialNumber());
    }
    ib.setHostname(shortName + "-" + ib.getSerialNumber());

    return ib;
  }

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
