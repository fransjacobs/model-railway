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
package jcs.controller.cs.can.parser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jcs.entities.AccessoryBean;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class AccessoryBeanParser {

  public final static String MAGNETARTIKEL = "magnetartikel.cs2";

  private static final String ACCESSORY_START = "[magnetartikel]";
  private static final String VERSION = "version";
  //private static final String MINOR = ".minor";
  //private static final String MAJOR = ".major";

  private static final String NAME = ".name";
  private static final String ID = ".id";
  private static final String TYPE = ".typ";
  private static final String STELLUNG = ".stellung";
  private static final String SCHALTZEIT = ".schaltzeit";
  private static final String DECTYP = ".dectyp";
  private static final String DECODER = ".decoder";

  private static final String ARTIKEL = "artikel";
  //private static final String UNGERADE = ".ungerade";

  //private static final String DEFAULT_TYPE = "std_rot_gruen";
  //private static final String DEFAULT_RED_TYPE = "std_rot";
  //private static final String DEFAULT_GREEN_TYPE = "std_gruen";

  //private static final String TURNOUT_R = "rechtsweiche";
  //private static final String TURNOUT_L = "linksweiche";
  //private static final String TURNOUT_X = "y_weiche";
  //private static final String TURNOUT_3 = "dreiwegweiche";

  //private static final String ENTKUPPLUNGSGLEIS = "entkupplungsgleis";
  //private static final String ENTKUPPLUNGSGLEIS_1 = "entkupplungsgleis_1";

  //private static final String LIGHT_SIGNAL_HP01 = "lichtsignal_HP01";
  //private static final String LIGHT_SIGNAL_HP02 = "lichtsignal_HP02";
  //private static final String LIGHT_SIGNAL_HP012 = "lichtsignal_HP012";
  //private static final String LIGHT_SIGNAL_HP012_SH01 = "lichtsignal_HP012_SH01";
  //private static final String LIGHT_SIGNAL_SH01 = "lichtsignal_SH01";

  //private static final String FORM_SIGNAL_HP01 = "formsignal_HP01";
  //private static final String FORM_SIGNAL_HP02 = "formsignal_HP02";
  //private static final String FORM_SIGNAL_HP012 = "formsignal_HP012";
  //private static final String FORM_SIGNAL_HP012_SH01 = "formsignal_HP012_SH01";
  //private static final String FORM_SIGNAL_SH01 = "formsignal_SH01";

  //private static final String URC_SIGNAL_HP01 = "urc_lichtsignal_HP01";
  //private static final String URC_SIGNAL_HP012 = "urc_lichtsignal_HP012";
  //private static final String URC_SIGNAL_HP012_SH01 = "urc_lichtsignal_HP012_SH01";
  //private static final String URC_SIGNAL_SH01 = "urc_lichtsignal_SH01";

  public List<AccessoryBean> parseAccessoryFile(String gafile) {
    List<AccessoryBean> accessories = new LinkedList<>();
    List<String> items = Arrays.asList(gafile.split("\n"));
    Map<String, String> mags = new HashMap<>();

    String ps = "";
    for (String s : items) {
      s = s.trim();
      //Logger.trace("Line: " + s);
      switch (s) {
        case ACCESSORY_START -> {
        }
        case VERSION -> {

        }
        case ARTIKEL -> {
          if (mags.containsKey(NAME)) {
            AccessoryBean sa = createAccessory(mags);
            if (sa != null) {
              accessories.add(sa);
            }
          }
          mags.clear();
        }
        default -> {
          if (s.contains("=")) {
            String[] kp = s.split("=");
            String key = kp[0];
            String val = kp[1];
            mags.put(key, val);

            if (VERSION.equals(ps)) {
              Logger.trace("Version = " + val);
            }

          } else {
            Logger.trace("Tag?: " + s);
          }
        }
      }
      ps = s;
    }
    // parse the last Accessory
    if (mags.containsKey(NAME)) {
      AccessoryBean sa = createAccessory(mags);
      if (sa != null) {
        accessories.add(sa);
      }
    }
    return accessories;
  }

//  public List<AccessoryBean> parseAccessoryStatusFile(String accessoryStatusFile) {
//    List<AccessoryBean> accessories = new LinkedList<>();
//    List<String> items = Arrays.asList(accessoryStatusFile.split("\n"));
//    Map<String, String> ma = new HashMap<>();
//    for (String s : items) {
//      //Logger.trace("Line: " + s);
//      switch (s) {
//        case ACCESSORY_START -> {
//        }
//        case VERSION -> {
//        }
//        case ARTIKEL -> {
//          if (ma.containsKey(ID)) {
//            AccessoryBean ab = createAccessory(ma);
//            accessories.add(ab);
//          }
//          ma.clear();
//        }
//        default -> {
//          if (s.contains("=")) {
//            String[] kp = s.split("=");
//            String key = kp[0];
//            String val = kp[1];
//            ma.put(key, val);
//
//          } else {
//            Logger.debug("Tag?: " + s);
//          }
//        }
//      }
//    }
//    // parse the last Accessory
//    if (ma.containsKey(ID)) {
//      if (ma.containsKey(ID)) {
//        AccessoryBean ab = createAccessory(ma);
//        accessories.add(ab);
//      }
//    }
//    return accessories;
//  }
  private AccessoryBean createAccessory(Map<String, String> ma) {
    Integer address = Integer.valueOf(ma.get(ID));
    String name = ma.get(NAME);
    String type = ma.get(TYPE);

    Integer position = null;
    if (ma.get(STELLUNG) != null) {
      position = Integer.valueOf(ma.get(STELLUNG));
    }

    Integer switchTime = null;
    if (ma.get(SCHALTZEIT) != null) {
      switchTime = Integer.valueOf(ma.get(SCHALTZEIT));
    }

    String decoderType = ma.get(DECTYP);
    String decoder = ma.get(DECODER);

    return new AccessoryBean(address, name, type, position, switchTime, decoderType, decoder);
  }

  public static void main(String[] a) throws Exception {
    Path path = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "magnetartikel.cs2");

    String magsFile = Files.readString(path);

    AccessoryBeanParser lp = new AccessoryBeanParser();
    List<AccessoryBean> accessories = lp.parseAccessoryFile(magsFile);

    for (AccessoryBean accessory : accessories) {
      Logger.trace((accessory.isSignal() ? "Signal" : "Turnout") + ": " + accessory);
    }

  }

}
