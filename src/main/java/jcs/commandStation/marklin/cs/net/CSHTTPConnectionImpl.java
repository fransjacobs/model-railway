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
package jcs.commandStation.marklin.cs.net;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class CSHTTPConnectionImpl implements CSHTTPConnection {

  private final InetAddress csAddress;
  private boolean cs3;

  private final static String HTTP = "http://";
  private final static String CONFIG = "/config/";
  private final static String LOCOMOTIVE = "lokomotive.cs2";
  private final static String LOCOMOTIVE_JSON = "/app/api/loks";

  private final static String MAGNETARTIKEL = "magnetartikel.cs2";
  private final static String ACCESSORIES_URL = "/app/api/mags";

  private final static String DEVICE = "geraet.vrs";

  private final static String IMAGE_FOLDER_CS3 = "/app/assets/lok/";
  private final static String IMAGE_FOLDER_CS2 = "/icons/";
  private final static String FUNCTION_IMAGE_FOLDER = "/fcticons/";

  private final static String FUNCTION_SVG_URL = "/images/svgSprites/fcticons.json";
  private final static String ACCESSORIES_SVG_URL = "/images/svgSprites/magicons.json";

  private final static String CS3_INFO_JSON = "/app/api/info";
  private final static String DEVICES = "/app/api/devs";

  //TODO: investigate the JSON which can be found on     
  //  http://cs3host/images/svgSprites/magicons.json
  //  http://cs3host/app/api/loks
  //  http://cs3host/app/api/mags
  //  http://cs3host/app/api/info
  //
  CSHTTPConnectionImpl(InetAddress csAddress) {
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
      URL cs = URI.create(HTTP + csAddress.getHostAddress() + CONFIG + LOCOMOTIVE).toURL();
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
      URL url = URI.create(HTTP + csAddress.getHostAddress() + LOCOMOTIVE_JSON).toURL();
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
      URL url = URI.create(HTTP + csAddress.getHostAddress() + CONFIG + MAGNETARTIKEL).toURL();
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
      URL url = URI.create(HTTP + csAddress.getHostAddress() + FUNCTION_SVG_URL).toURL();
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
      URL url = URI.create(HTTP + csAddress.getHostAddress() + ACCESSORIES_SVG_URL).toURL();
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
      URL url = URI.create(HTTP + csAddress.getHostAddress() + ACCESSORIES_URL).toURL();
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
    StringBuilder device = new StringBuilder();
    if (this.csAddress != null && csAddress.getHostAddress() != null) {
      try {
        URL url = URI.create(HTTP + csAddress.getHostAddress() + DEVICES).toURL();
        URLConnection lc = url.openConnection();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(lc.getInputStream()))) {
          String inputLine;
          while ((inputLine = in.readLine()) != null) {
            device.append(inputLine.strip());
            device.append("\n");
          }
        }
      } catch (MalformedURLException ex) {
        Logger.error(ex);
      } catch (IOException ex) {
        Logger.error(ex);
      }
    } else {
      Logger.warn("Not Connected to CS3!");
    }
    return device.toString();
  }

  @Override
  public String getInfoFile() {
    StringBuilder device = new StringBuilder();
    try {
      URL url = URI.create(HTTP + csAddress.getHostAddress() + CONFIG + DEVICE).toURL();
      URLConnection lc = url.openConnection();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(lc.getInputStream()))) {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          device.append(inputLine.strip());
          device.append("\n");
        }
      }
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return device.toString();
  }

  @Override
  public String getInfoJSON() {
    StringBuilder device = new StringBuilder();
    if (this.csAddress != null && csAddress.getHostAddress() != null) {
      try {
        URL url = URI.create(HTTP + csAddress.getHostAddress() + CS3_INFO_JSON).toURL();
        URLConnection lc = url.openConnection();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(lc.getInputStream()))) {
          String inputLine;
          while ((inputLine = in.readLine()) != null) {
            device.append(inputLine.strip());
            device.append("\n");
          }
        }
      } catch (MalformedURLException ex) {
        Logger.error(ex);
      } catch (IOException ex) {
        Logger.error(ex);
      }
    } else {
      Logger.warn("Not Connected to CS3!");
    }
    return device.toString();
  }

  @Override
  public Image getLocomotiveImage(String imageName) {
    BufferedImage image = null;
    try {
      URL url;
      if (cs3) {
        url = URI.create(fixURL(HTTP + csAddress.getHostAddress() + IMAGE_FOLDER_CS3 + imageName + ".png")).toURL();
      } else {
        url = URI.create(fixURL(HTTP + csAddress.getHostAddress() + IMAGE_FOLDER_CS2 + imageName + ".png")).toURL();
      }

      Logger.trace("image URL: " + url);
      image = ImageIO.read(url);
    } catch (IOException ex) {
      Logger.error("Image " + imageName + " not found. " + ex.getMessage());
    }
    return image;
  }

  @Override
  public Image getFunctionImageCS2(String imageName) {
    BufferedImage image = null;
    String iurl = fixURL(HTTP + csAddress.getHostAddress() + FUNCTION_IMAGE_FOLDER + imageName + ".png");

    try {
      URL url = URI.create(iurl).toURL();
      image = ImageIO.read(url);
    } catch (IIOException iio) {
      //Image not avalable
      Logger.warn("Image: " + iurl + " is not available");
    } catch (IOException ex) {
      Logger.error("Image " + imageName + " not found. " + ex.getMessage());
    }
    return image;
  }

  @Override
  public void close() throws Exception {
  }

}
