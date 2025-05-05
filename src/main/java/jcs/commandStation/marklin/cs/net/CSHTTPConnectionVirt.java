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
package jcs.commandStation.marklin.cs.net;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import org.tinylog.Logger;

/**
 *
 * Virtual HTTP Connection for Simulator mode
 */
public class CSHTTPConnectionVirt implements CSHTTPConnection {

  private final InetAddress csAddress;
  private boolean cs3;

  private final static String HTTP = "http://";
  private final static String CONFIG = "/config/";
  private final static String LOCOMOTIVE = "lokomotive.cs2";
  private final static String LOCOMOTIVE_JSON = "/app/api/loks";

  private final static String MAGNETARTIKEL = "magnetartikel.cs2";
  private final static String ACCESSORIES_URL = "/app/api/mags";

  //private final static String DEVICE = "geraet.vrs";
  private final static String IMAGE_FOLDER_CS3 = "/app/assets/lok/";
  private final static String IMAGE_FOLDER_CS2 = "/icons/";
  private final static String FUNCTION_IMAGE_FOLDER = "/fcticons/";

  private final static String FUNCTION_SVG_URL = "/images/svgSprites/fcticons.json";
  private final static String ACCESSORIES_SVG_URL = "/images/svgSprites/magicons.json";

  //private final static String CS3_INFO_JSON = "/app/api/info";
  //private final static String DEVICES = "/app/api/devs";
  public CSHTTPConnectionVirt() throws UnknownHostException {
    this(InetAddress.getLocalHost());
  }

  public CSHTTPConnectionVirt(InetAddress csAddress) {
    this.csAddress = csAddress;
    //Assume a CS2
    this.cs3 = false;
  }

  @Override
  public boolean isConnected() {
    return csAddress != null && csAddress.getHostAddress() != null;
  }

  @Override
  public void setCs3(boolean cs3) {
    this.cs3 = cs3;
    Logger.trace("Changed Connection settings for a " + (cs3 ? "CS3" : "CS2"));
  }

  private static String fixURL(String url) {
    return url.replace(" ", "%20");
  }

  @Override
  public String getLocomotivesFile() {
    StringBuilder locs = new StringBuilder();
    try {
      URL cs = new URL(HTTP + csAddress.getHostAddress() + CONFIG + LOCOMOTIVE);
      URLConnection lc = cs.openConnection();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(lc.getInputStream()))) {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          locs.append(inputLine.strip());
          locs.append("\n");
        }
      }
    } catch (MalformedURLException ex) {
      Logger.error(ex);
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return locs.toString();
  }

  @Override
  public String getLocomotivesJSON() {
    StringBuilder loks = new StringBuilder();
    try {
      URL url = new URL(HTTP + csAddress.getHostAddress() + LOCOMOTIVE_JSON);
      URLConnection lc = url.openConnection();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(lc.getInputStream()))) {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          loks.append(inputLine.strip());
          loks.append("\n");
        }
      }
    } catch (MalformedURLException ex) {
      Logger.error(ex);
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return loks.toString();
  }

  @Override
  public String getAccessoriesFile() {
    StringBuilder locs = new StringBuilder();
    try {
      URL url = new URL(HTTP + csAddress.getHostAddress() + CONFIG + MAGNETARTIKEL);
      URLConnection lc = url.openConnection();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(lc.getInputStream()))) {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          locs.append(inputLine.strip());
          locs.append("\n");
        }
      }
    } catch (MalformedURLException ex) {
      Logger.error(ex);
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return locs.toString();
  }

  @Override
  public String getFunctionsSvgJSON() {
    StringBuilder json = new StringBuilder();
    try {
      URL url = new URL(HTTP + csAddress.getHostAddress() + FUNCTION_SVG_URL);
      URLConnection lc = url.openConnection();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(lc.getInputStream()))) {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          json.append(inputLine.strip());
          json.append("\n");
        }
      }
    } catch (MalformedURLException ex) {
      Logger.error(ex);
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return json.toString();
  }

  @Override
  public String getAccessoriesSvgJSON() {
    StringBuilder json = new StringBuilder();
    try {
      URL url = new URL(HTTP + csAddress.getHostAddress() + ACCESSORIES_SVG_URL);
      URLConnection lc = url.openConnection();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(lc.getInputStream()))) {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          json.append(inputLine.strip());
          json.append("\n");
        }
      }
    } catch (MalformedURLException ex) {
      Logger.error(ex);
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return json.toString();
  }

  @Override
  public String getAccessoriesJSON() {
    StringBuilder json = new StringBuilder();
    try {
      URL url = new URL(HTTP + csAddress.getHostAddress() + ACCESSORIES_URL);
      URLConnection lc = url.openConnection();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(lc.getInputStream()))) {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          json.append(inputLine.strip());
          json.append("\n");
        }
      }
    } catch (MalformedURLException ex) {
      Logger.error(ex);
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return json.toString();
  }

  @Override
  public String getDevicesJSON() {
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"0x8\": {");
    json.append("\"_uid\": \"0x8\",");
    json.append("\"_name\": \"USB 0\",");
    json.append("\"_kennung\": \"0xffff\",");
    json.append("\"_typ\": \"65280\",");
    json.append("\"_queryInterval\": \"5\",");
    json.append("\"_version\": {");
    json.append("\"major\": \"16\"");
    json.append("},");
    json.append("\"_kanal\": [],");
    json.append("\"_ready\": true,");
    json.append("\"path\": \"/media/usb0\",");
    json.append("\"isPresent\": true,");
    json.append("\"isMounted\": true");
    json.append("},");
    json.append("\"0x9\": {");
    json.append("\"_uid\": \"0x9\",");
    json.append("\"_name\": \"USB 1\",");
    json.append("\"_kennung\": \"0xffff\",");
    json.append("\"_typ\": \"65280\",");
    json.append("\"_queryInterval\": \"5\",");
    json.append("\"_version\": {");
    json.append("\"major\": \"77\"");
    json.append("},");
    json.append("\"_kanal\": [],");
    json.append("\"_ready\": true,");
    json.append("\"path\": \"/media/usb1\",");
    json.append("\"isPresent\": true,");
    json.append("\"isMounted\": true");
    json.append("},");
    json.append("\"0x53385c41\": {");
    json.append("\"_uid\": \"0x53385c41\",");
    json.append("\"_name\": \"LinkS88-1\",");
    json.append("\"_typname\": \"Link S88\",");
    json.append("\"_kennung\": \"0x41\",");
    json.append("\"_typ\": \"64\",");
    json.append("\"_artikelnr\": \"60883\",");
    json.append("\"_seriennr\": \"9281\",");
    json.append("\"_queryInterval\": \"5\",");
    json.append("\"_version\": {");
    json.append("\"major\": \"1\",");
    json.append("\"minor\": \"1\"");
    json.append("},");
    json.append("\"_kanal\": [");
    json.append("{");
    json.append("\"endWert\": \"8\",");
    json.append("\"max\": \"8\",");
    json.append("\"name\": \"Spalten Tastatur\",");
    json.append("\"nr\": \"11\",");
    json.append("\"startWert\": \"0\",");
    json.append("\"typ\": \"2\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"endWert\": \"15\",");
    json.append("\"max\": \"15\",");
    json.append("\"name\": \"Zeilen Tastatur\",");
    json.append("\"nr\": \"12\",");
    json.append("\"startWert\": \"0\",");
    json.append("\"typ\": \"2\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"auswahl\": \"Einzeln:Tastaturmatrix\",");
    json.append("\"name\": \"Auswertung 1 - 16\",");
    json.append("\"nr\": \"1\",");
    json.append("\"typ\": \"1\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"endWert\": \"31\",");
    json.append("\"max\": \"31\",");
    json.append("\"name\": \"Länge Bus 1 (RJ45-1)\",");
    json.append("\"nr\": \"2\",");
    json.append("\"startWert\": \"0\",");
    json.append("\"typ\": \"2\",");
    json.append("\"wert\": \"1\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"endWert\": \"31\",");
    json.append("\"max\": \"31\",");
    json.append("\"name\": \"Länge Bus 2 (RJ45-2)\",");
    json.append("\"nr\": \"3\",");
    json.append("\"startWert\": \"0\",");
    json.append("\"typ\": \"2\",");
    json.append("\"wert\": \"2\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"endWert\": \"31\",");
    json.append("\"max\": \"31\",");
    json.append("\"name\": \"Länge Bus 3 (6-Polig)\",");
    json.append("\"nr\": \"4\",");
    json.append("\"startWert\": \"0\",");
    json.append("\"typ\": \"2\",");
    json.append("\"wert\": \"1\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"einheit\": \"ms\",");
    json.append("\"endWert\": \"1000\",");
    json.append("\"max\": \"1000\",");
    json.append("\"min\": \"10\",");
    json.append("\"name\": \"Zykluszeit Bus 1 (RJ45-1)\",");
    json.append("\"nr\": \"5\",");
    json.append("\"startWert\": \"10\",");
    json.append("\"typ\": \"2\",");
    json.append("\"wert\": \"100\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"einheit\": \"ms\",");
    json.append("\"endWert\": \"1000\",");
    json.append("\"max\": \"1000\",");
    json.append("\"min\": \"10\",");
    json.append("\"name\": \"Zykluszeit Bus 2 (RJ45-2)\",");
    json.append("\"nr\": \"6\",");
    json.append("\"startWert\": \"10\",");
    json.append("\"typ\": \"2\",");
    json.append("\"wert\": \"100\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"einheit\": \"ms\",");
    json.append("\"endWert\": \"1000\",");
    json.append("\"max\": \"1000\",");
    json.append("\"min\": \"10\",");
    json.append("\"name\": \"Zykluszeit Bus 3 (6-Polig)\",");
    json.append("\"nr\": \"7\",");
    json.append("\"startWert\": \"10\",");
    json.append("\"typ\": \"2\",");
    json.append("\"wert\": \"100\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"einheit\": \"µs\",");
    json.append("\"endWert\": \"1000\",");
    json.append("\"max\": \"1000\",");
    json.append("\"min\": \"100\",");
    json.append("\"name\": \"Bitzeit S88\",");
    json.append("\"nr\": \"8\",");
    json.append("\"startWert\": \"100\",");
    json.append("\"typ\": \"2\",");
    json.append("\"wert\": \"167\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"einheit\": \"ms\",");
    json.append("\"endWert\": \"1000\",");
    json.append("\"max\": \"1000\",");
    json.append("\"min\": \"10\",");
    json.append("\"name\": \"Zykluszeit 1 - 16\",");
    json.append("\"nr\": \"9\",");
    json.append("\"startWert\": \"10\",");
    json.append("\"typ\": \"2\",");
    json.append("\"wert\": \"100\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"einheit\": \"ms\",");
    json.append("\"endWert\": \"100\",");
    json.append("\"max\": \"100\",");
    json.append("\"min\": \"10\",");
    json.append("\"name\": \"Zykluszeit Tastatur\",");
    json.append("\"nr\": \"10\",");
    json.append("\"startWert\": \"10\",");
    json.append("\"typ\": \"2\",");
    json.append("\"wert\": \"37\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("}");
    json.append("],");
    json.append("\"_ready\": false,");
    json.append("\"isPresent\": true,");
    json.append("\"present\": \"1\",");
    json.append("\"config\": \"12;ok\"");
    json.append("},");
    json.append("\"0x6373458c\": {");
    json.append("\"_uid\": \"0x6373458c\",");
    json.append("\"_name\": \"GFP3-1\",");
    json.append("\"_typname\": \"Central Station 3\",");
    json.append("\"_kennung\": \"0xffff\",");
    json.append("\"_typ\": \"80\",");
    json.append("\"_artikelnr\": \"60226\",");
    json.append("\"_seriennr\": \"0000\",");
    json.append("\"_queryInterval\": \"5\",");
    json.append("\"_version\": {");
    json.append("\"major\": \"12\",");
    json.append("\"minor\": \"113\"");
    json.append("},");
    json.append("\"_kanal\": [");
    json.append("{");
    json.append("\"einheit\": \"A\",");
    json.append("\"endWert\": \"5.50\",");
    json.append("\"farbeGelb\": \"240\",");
    json.append("\"farbeGruen\": \"48\",");
    json.append("\"farbeMax\": \"192\",");
    json.append("\"farbeRot\": \"224\",");
    json.append("\"name\": \"MAIN\",");
    json.append("\"nr\": \"1\",");
    json.append("\"potenz\": \"253\",");
    json.append("\"rangeGelb\": \"576\",");
    json.append("\"rangeGruen\": \"552\",");
    json.append("\"rangeMax\": \"660\",");
    json.append("\"rangeRot\": \"600\",");
    json.append("\"startWert\": \"0.00\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"einheit\": \"A\",");
    json.append("\"endWert\": \"2.30\",");
    json.append("\"farbeGelb\": \"240\",");
    json.append("\"farbeGruen\": \"48\",");
    json.append("\"farbeMax\": \"192\",");
    json.append("\"farbeRot\": \"224\",");
    json.append("\"name\": \"PROG\",");
    json.append("\"nr\": \"2\",");
    json.append("\"potenz\": \"253\",");
    json.append("\"rangeGelb\": \"363\",");
    json.append("\"rangeGruen\": \"330\",");
    json.append("\"rangeMax\": \"759\",");
    json.append("\"rangeRot\": \"561\",");
    json.append("\"startWert\": \"0.00\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"einheit\": \"C\",");
    json.append("\"endWert\": \"80.0\",");
    json.append("\"farbeGelb\": \"8\",");
    json.append("\"farbeGruen\": \"12\",");
    json.append("\"farbeMax\": \"192\",");
    json.append("\"farbeRot\": \"240\",");
    json.append("\"name\": \"TEMP\",");
    json.append("\"nr\": \"4\",");
    json.append("\"rangeGelb\": \"145\",");
    json.append("\"rangeGruen\": \"121\",");
    json.append("\"rangeMax\": \"193\",");
    json.append("\"rangeRot\": \"169\",");
    json.append("\"startWert\": \"0.0\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("},");
    json.append("{");
    json.append("\"auswahl\": \"60061:60101:L51095\",");
    json.append("\"index\": \"1\",");
    json.append("\"name\": \"Netzteil:\",");
    json.append("\"nr\": \"1\",");
    json.append("\"typ\": \"1\",");
    json.append("\"valueHuman\": \"0\"");
    json.append("}");
    json.append("],");
    json.append("\"_ready\": true,");
    json.append("\"isPresent\": true");
    json.append("},");
    json.append("\"0x6373458d\": {");
    json.append("\"_uid\": \"0x6373458d\",");
    json.append("\"_name\": \"CS3 0000\",");
    json.append("\"_typ\": \"65504\",");
    json.append("\"_kanal\": [],");
    json.append("\"_ready\": true,");
    json.append("\"ip\": \"127.0.0.1\",");
    json.append("\"isPresent\": true");
    json.append("},");
    json.append("}");

    return json.toString();
  }

  @Override
  public String getInfoFile() {
    StringBuilder geraet = new StringBuilder();
    geraet.append("[geraet]\n");
    geraet.append("version\n");
    geraet.append(".major=0\n");
    geraet.append(".minor=1\n");
    geraet.append("geraet\n");
    geraet.append(".sernum=0000\n");
    geraet.append(".gfpuid=6373458c\n");
    geraet.append(".guiuid=6373458d\n");
    geraet.append(".hardvers=HW:03.04\n");
    geraet.append(".articleno=60226\n");
    geraet.append(".producer=Frans Jacobs.\n");
    geraet.append(".produkt=Virtual Central Station 3\n");

    return geraet.toString();
  }

  @Override
  public String getInfoJSON() {
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"softwareVersion\"");
    json.append(":");
    json.append("\"2.5.1 (0)\"");
    json.append(",");

    json.append("\"hardwareVersion\"");
    json.append(":");
    json.append("\"HW:03.04\"");
    json.append(",");

    json.append("\"serialNumber\"");
    json.append(":");
    json.append("\"0000\"");
    json.append(",");

    json.append("\"productName\"");
    json.append(":");
    json.append("\"Virtual Central Station 3\"");
    json.append(",");

    json.append("\"articleNumber\"");
    json.append(":");
    json.append("\"60226\"");
    json.append(",");

    json.append("\"hostname\"");
    json.append(":");
    json.append("\"CS3-00000\"");
    json.append(",");

    json.append("\"gfpUid\"");
    json.append(":");
    json.append("\"6373458c\"");
    json.append(",");

    json.append("\"guiUid\"");
    json.append(":");
    json.append("\"6373458d\"");
    json.append("}");
    return json.toString();
  }

  @Override
  public Image getLocomotiveImage(String imageName) {
    BufferedImage image = null;
    try {
      URL url;
      if (cs3) {
        url = new URL(fixURL(HTTP + csAddress.getHostAddress() + IMAGE_FOLDER_CS3 + imageName + ".png"));
      } else {
        url = new URL(fixURL(HTTP + csAddress.getHostAddress() + IMAGE_FOLDER_CS2 + imageName + ".png"));
      }

      Logger.trace("image URL: " + url);
      image = ImageIO.read(url);
    } catch (MalformedURLException ex) {
      Logger.error(ex);
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return image;
  }

  @Override
  public Image getFunctionImageCS2(String imageName) {
    BufferedImage image = null;
    String iurl = fixURL(HTTP + csAddress.getHostAddress() + FUNCTION_IMAGE_FOLDER + imageName + ".png");

    try {
      URL url = new URL(iurl);
      image = ImageIO.read(url);
    } catch (IIOException iio) {
      //Image not avalable
      Logger.warn("Image: " + iurl + " is not available");
    } catch (MalformedURLException ex) {
      Logger.error(ex);
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return image;
  }

  @Override
  public void close() throws Exception {
  }

}
