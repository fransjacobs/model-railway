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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class HTTPConnection {
  
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
  HTTPConnection(InetAddress csAddress) {
    this.csAddress = csAddress;
    //Assume a CS2
    this.cs3 = false;
  }
  
  public boolean isConnected() {
    return csAddress != null && csAddress.getHostAddress() != null;
  }
  
  public void setCs3(boolean cs3) {
    this.cs3 = cs3;
    Logger.trace("Changed Connection settings for a " + (cs3 ? "CS3" : "CS2"));
  }
  
  private static String fixURL(String url) {
    return url.replace(" ", "%20");
  }
  
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
  
  public String getDevicesJSON() {
    StringBuilder device = new StringBuilder();
    if (this.csAddress != null && csAddress.getHostAddress() != null) {
      try {
        URL url = new URL(HTTP + csAddress.getHostAddress() + DEVICES);
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
  
  public String getInfoFile() {
    StringBuilder device = new StringBuilder();
    try {
      URL url = new URL(HTTP + csAddress.getHostAddress() + CONFIG + DEVICE);
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
    return device.toString();
  }
  
  public String getInfoJSON() {
    StringBuilder device = new StringBuilder();
    if (this.csAddress != null && csAddress.getHostAddress() != null) {
      try {
        URL url = new URL(HTTP + csAddress.getHostAddress() + CS3_INFO_JSON);
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
  
  public static void main(String[] args) throws Exception {
    boolean cs3 = true;
    
    InetAddress inetAddr;
    if (cs3) {
      inetAddr = InetAddress.getByName("192.168.178.180");
    } else {
      inetAddr = InetAddress.getByName("192.168.178.86");
    }
    HTTPConnection hc = new HTTPConnection(inetAddr);
    hc.setCs3(cs3);
    
    String serial;
    if (cs3) {
      serial = "2374";
    } else {
      serial = "13344";
    }

//    Path fPath = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + "zfunctions");
//    if (!Files.exists(fPath)) {
//      Files.createDirectories(fPath);
//      Logger.trace("Created new directory " + fPath);
//    }
//
//    Path aPath = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + "zaccessories");
//    if (!Files.exists(aPath)) {
//      Files.createDirectories(aPath);
//      Logger.trace("Created new directory " + aPath);
//    }
    Path info = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "info.json");
    String json = hc.getInfoJSON();
    Files.writeString(info, json);
    
    Path devices = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "devices.json");
    json = hc.getDevicesJSON();
    Files.writeString(devices, json);
    
    Path locomotives = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "locomotives.json");
    json = hc.getLocomotivesJSON();
    Files.writeString(locomotives, json);
    
    Path accessories = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "mags.json");
    json = hc.getAccessoriesJSON();
    Files.writeString(accessories, json);
    
    Path fcticons = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "fcticons.json");
    json = hc.getFunctionsSvgJSON();
    Files.writeString(fcticons, json);
    
    Path magicons = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "magicons.json");
    json = hc.getAccessoriesSvgJSON();
    Files.writeString(magicons, json);
    
    Path accessoryFile = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "magnetartikel.cs2");
    String file = hc.getAccessoriesFile();
    Logger.trace(file);
    Files.writeString(accessoryFile, file);
    
  }
}
