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
package jcs.entities;

import org.json.JSONObject;

/**
 * An InfoBean represents the basic information about the connected Command Station.
 */
public class InfoBean {

  private String softwareVersion;
  private String hardwareVersion;
  private String serialNumber;
  private String productName;
  private String articleNumber;
  private String hostname;
  private String gfpUid;
  private String guiUid;

  public InfoBean() {
    this(null, false);
  }

  public InfoBean(String text, boolean file) {
    if (file) {
      parseFile(text);
    } else {
      parseJson(text);
    }
  }

  public String getSoftwareVersion() {
    return softwareVersion;
  }

  public void setSoftwareVersion(String softwareVersion) {
    this.softwareVersion = softwareVersion;
  }

  public String getHardwareVersion() {
    return hardwareVersion;
  }

  public void setHardwareVersion(String hardwareVersion) {
    this.hardwareVersion = hardwareVersion;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public String getArticleNumber() {
    return articleNumber;
  }

  public void setArticleNumber(String articleNumber) {
    this.articleNumber = articleNumber;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getGfpUid() {
    return gfpUid;
  }

  public void setGfpUid(String gfpUid) {
    this.gfpUid = gfpUid;
  }

  public String getGuiUid() {
    return guiUid;
  }

  public void setGuiUid(String guiUid) {
    this.guiUid = guiUid;
  }

  @Override
  public String toString() {
    return "InfoBean{" + "softwareVersion=" + softwareVersion + ", hardwareVersion=" + hardwareVersion + ", serialNumber=" + serialNumber + ", productName=" + productName + ", articleNumber=" + articleNumber + ", hostname=" + hostname + ", gfpUid=" + gfpUid + ", guiUid=" + guiUid + "}";
  }

  private void parseFile(String file) {
    if (file == null) {
      return;
    }

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
          this.serialNumber = value;
        }
        case ".gfpuid" -> {
          this.gfpUid = value;
        }
        case ".guiuid" -> {
          this.guiUid = value;
        }
        case ".hardvers" -> {
          this.hardwareVersion = value;
        }
        case ".articleno" -> {
          this.articleNumber = value;
        }
        case ".produkt" -> {
          this.productName = value;
        }
      }
    }

    softwareVersion = (major != null ? major : "") + (major != null ? "." : "") + (minor != null ? minor : "");

    String shortName;
    String sn = serialNumber;
    if (productName != null && productName.contains("Central Station 3")) {
      shortName = "CS3";
    } else {
      shortName = "CS2";
    }
    if (sn.length() < 5) {
      sn = "0" + sn;
    }
    hostname = shortName + "-" + sn;
  }

  private void parseJson(String json) {
    if (json == null) {
      return;
    }
    JSONObject infoObject = new JSONObject(json);
    this.softwareVersion = infoObject.optString("softwareVersion");
    this.hardwareVersion = infoObject.optString("hardwareVersion");
    this.serialNumber = infoObject.optString("serialNumber");
    this.productName = infoObject.optString("productName");
    this.articleNumber = infoObject.optString("articleNumber");
    this.hostname = infoObject.optString("hostname");
    this.gfpUid = infoObject.optString("gfpUid");
    this.guiUid = infoObject.optString("guiUid");
  }

}
