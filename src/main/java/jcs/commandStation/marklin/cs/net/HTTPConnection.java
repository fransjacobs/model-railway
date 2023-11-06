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
  private final boolean cs3;

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

  private final static String DEVICES = "/app/api/devs";

  //TODO: investigate the JSON which can be found on     
  //  http://cs3host/images/svgSprites/magicons.json
  //  http://cs3host/app/api/loks
  //  http://cs3host/app/api/mags
  //
  HTTPConnection(InetAddress csAddress) {
    this(csAddress, true);
  }

  HTTPConnection(InetAddress csAddress, boolean cs3) {
    this.csAddress = csAddress;
    this.cs3 = cs3;
  }

  public boolean isConnected() {
    return csAddress != null && csAddress.getHostAddress() != null;
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

  public String getDeviceFile() {
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

  public Image getLocomotiveImage(String imageName) {
    BufferedImage image = null;
    try {
      URL url;
      if (this.cs3) {
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

  public static void main(String[] args) throws Exception {
    boolean cs3 = true;

    InetAddress inetAddr;
    if (cs3) {
      inetAddr = InetAddress.getByName("192.168.178.180");
    } else {
      inetAddr = InetAddress.getByName("192.168.178.86");
    }
    HTTPConnection hc = new HTTPConnection(inetAddr, cs3);

    String serial;
    if (cs3) {
      serial = "2374";
    } else {
      serial = "13344";
    }

//    FunctionBean fb = new FunctionBean();
//    fb.setNumber(0);
//    fb.setFunctionType(1);
//
//    String activeImage = fb.getActiveIcon();
//    String inActiveImage = fb.getInActiveIcon();
//
//    Logger.trace("activeImage: " + activeImage + " inActiveImage: " + inActiveImage);
//
//    Image activeFunctionImage = hc.getFunctionImageCS2(activeImage);
//    Image inActiveFunctionImage = hc.getFunctionImageCS2(inActiveImage);
//
//    Path path = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + serial + File.separator + "functions");
//    if (!Files.exists(path)) {
//      Files.createDirectories(path);
//      Logger.trace("Created new directory " + path);
//    }
//    try {
//      //ImageIO.write((BufferedImage) locImage, "png", new File(path + File.separator + imageName + ".png"));
//      ImageIO.write((BufferedImage) activeFunctionImage, "png", new File(path + File.separator + activeImage + ".png"));
//      Logger.trace("Stored image " + activeImage + ".png in the cache: " + path);
//
//      ImageIO.write((BufferedImage) inActiveFunctionImage, "png", new File(path + File.separator + inActiveImage + ".png"));
//      Logger.trace("Stored image " + inActiveImage + ".png in the cache: " + path);
//      
//      
//    } catch (IOException ex) {
//      Logger.error("Can't store image " + path + "! ", ex.getMessage());
//    }
//    
//        String path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + vendorController.getDevice().getArticleNumber();
//    File cachePath = new File(path);
//    if (cachePath.mkdir()) {
//      Logger.trace("Created new directory " + cachePath);
//    }
//    try {
//      ImageIO.write((BufferedImage) image, "png", new File(path + File.separator + imageName + ".png"));
//    } catch (IOException ex) {
//      Logger.error("Can't store image " + cachePath.getName() + "! ", ex.getMessage());
//    }
//    Logger.trace("Stored image " + imageName + ".png in the cache");
    /*
        String loks = hc.getLocomotivesFile();
        LocomotiveBeanParser lp = new LocomotiveBeanParser();
        List<LocomotiveBean> locList = lp.parseLocomotivesFile(loks);

        for (LocomotiveBean loc : locList) {
            System.out.println(loc.toLogString());

            for (Integer fnnr : loc.getFunctions().keySet()) {
                FunctionBean fn = loc.getFunctions().get(fnnr);

                System.out.println(".Fn: " + fn.getNumber() + ", Type: " + fn.getFunctionType() + ", Value: " + fn.getValue());
            }
        }
     */
 /*
        String accessories = hc.getAccessoriesFile();
        AccessoryBeanParser ap = new AccessoryBeanParser();

        List<AccessoryBean> acList = ap.parseAccessoryFile(accessories);

        for (AccessoryBean sa : acList) {
            System.out.println(sa.toLogString());
        }
     */
 /*
        String deviceFile = hc.getDeviceFile();
        CS3DeviceParser dp = new CS3DeviceParser();
        CS3Device di = dp.parseAccessoryFile(deviceFile);

        System.out.println(di);
     */
//        String accessoryStatuses = hc.getAccessoryStatusesFile();
//        List<AccessoryStatus> acsList = ap.parseAccessoryStatusFile(accessoryStatuses);
//
//        for (AccessoryStatus as : acsList) {
//            System.out.println(as.toString());
//        }
//
    Path fcticons = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "fcticons.json");
    String json = hc.getFunctionsSvgJSON();
    Files.writeString(fcticons, json);

    Path locomotives = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "locomotives.json");
    json = hc.getLocomotivesJSON();

    Logger.trace(json);
    Files.writeString(locomotives, json);

  }
}
