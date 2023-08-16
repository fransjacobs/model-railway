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
package jcs.controller.cs.net;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import jcs.controller.cs3.http.AccessoryJSONParser;
import jcs.entities.AccessoryBean;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class HTTPConnection {

    private final InetAddress csAddress;

    private final static String HTTP = "http://";
    private final static String CONFIG = "/config/";
    private final static String LOCOMOTIVE = "lokomotive.cs2";
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
    HTTPConnection(InetAddress cs3Address) {
        this.csAddress = cs3Address;
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
            URL cs2 = new URL(HTTP + csAddress.getHostAddress() + CONFIG + LOCOMOTIVE);
            URLConnection lc = cs2.openConnection();
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

    public String getAllFunctionsSvgJSON() {
        StringBuilder device = new StringBuilder();
        try {
            URL url = new URL(HTTP + csAddress.getHostAddress() + FUNCTION_SVG_URL);
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

    public String getAccessoriesJSON() {
        StringBuilder mags = new StringBuilder();
        try {
            URL url = new URL(HTTP + csAddress.getHostAddress() + ACCESSORIES_URL);
            URLConnection lc = url.openConnection();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(lc.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    mags.append(inputLine.strip());
                    mags.append("\n");
                }
            }
        } catch (MalformedURLException ex) {
            Logger.error(ex);
        } catch (IOException ex) {
            Logger.error(ex);
        }
        return mags.toString();
    }

    public Image getLocomotiveImage(String imageName) {
        BufferedImage image = null;
        try {
            URL url = new URL(fixURL(HTTP + csAddress.getHostAddress() + IMAGE_FOLDER_CS3 + imageName + ".png"));
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
            Logger.trace("Try to fetch: " + iurl);
            URL url = new URL(iurl);
            image = ImageIO.read(url);
        } catch (IIOException iio) {
            //Image not avalable
            //Logger.trace("Image: " + iurl + " is not available");
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
        InetAddress inetAddr = InetAddress.getByName("192.168.178.180");
        HTTPConnection hc = new HTTPConnection(inetAddr);

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
        /*
         String json = hc.getAllFunctionsSvgJSON();
         SvgIconToPngIconConverter svgp = new SvgIconToPngIconConverter();
         svgp.convertAndCacheAllFunctionsSvgIcons(json);
         */
        /*
        String json = hc.getDevicesJSON();
        DeviceJSONParser dp = new DeviceJSONParser();
        dp.parseAccessories(json);
        */
        
        String json = hc.getAccessoriesJSON();
        //System.out.println(json);
        AccessoryJSONParser ap = new AccessoryJSONParser();
        ap.parseAccessories(json);
        for(AccessoryBean ab : ap.getSignals()) {
            Logger.trace(ab.toLogString());
        }

    }
}
