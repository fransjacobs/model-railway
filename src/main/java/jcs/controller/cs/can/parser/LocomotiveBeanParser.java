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
import jcs.entities.ConfigurationValueBean;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class LocomotiveBeanParser {

  public final static String LOCOMOTIVE_FILE = "lokomotive.cs2";

  private static final String LOCOMOTIVES_START = "[lokomotive]";
  private static final String VERSION = "version";
  //private static final String MINOR = ".minor";
  private static final String SESSION = "session";
  //private static final String ID = ".id";
  private static final String LOCOMOTIVE = "lokomotive";
  private static final String NAME = ".name";
  private static final String VORNAME = ".vorname";
  //private static final String SYMBOL = ".symbol";
  private static final String UID = ".uid";
  private static final String ADDRESSE = ".adresse";
  private static final String RICHTUNG = ".richtung";
  private static final String VELOCITY = ".velocity";
  private static final String TYPE = ".typ";
  private static final String SID = ".sid";
  private static final String MFXUID = ".mfxuid";
  private static final String ICON = ".icon";
  private static final String AV = ".av";
  private static final String BV = ".bv";
  //private static final String PROGMASK = ".progmask";
  private static final String VOLUME = ".volume";
  private static final String TACHOMAX = ".tachomax";
  private static final String VMIN = ".vmin";
  //private static final String VMAX = ".vmax";
  private static final String SPM = ".spm";
  private static final String MFXTYPE = ".mfxtyp";
  private static final String BLOCKS = ".blocks";
  private static final String PRG = ".prg";

  private static final String FUNCTIONEN = ".funktionen";
  private static final String FUNCTIONEN_2 = ".funktionen_2";
  private static final String NR = "..nr";
  private static final String TYP = "..typ";
  private static final String WERT = "..wert";
  private static final String ADRESSE_2 = "..adresse";
  private static final String NAME_2 = "..name";

  public List<LocomotiveBean> parseLocomotivesFile(String locofile) {
    List<LocomotiveBean> locs = new LinkedList<>();
    List<String> items = Arrays.asList(locofile.split("\n"));
    Map<String, String> lm = new HashMap<>();

    Map<Integer, FunctionBean> locoFunctions = new HashMap<>();
    FunctionBean locoFunction = null;
    boolean functions = false;

    Map<Integer, ConfigurationValueBean> configRegister = new HashMap<>();
    ConfigurationValueBean configurationValue = null;
    boolean prg = false;

    String ps = "";
    for (String s : items) {
      s = s.trim();
      switch (s) {
        case LOCOMOTIVES_START -> {
        }
        case VERSION -> {
        }
        case SESSION -> {
        }
        case LOCOMOTIVE -> {
          if (lm.containsKey(".uid")) {
            LocomotiveBean loc = createLoco(lm, locoFunctions, configRegister);
            locs.add(loc);
          }
          functions = false;
          locoFunctions.clear();
          locoFunction = null;

          prg = false;
          configRegister.clear();
          configurationValue = null;

          lm.clear();
        }
        case FUNCTIONEN -> {
          functions = true;
          if (locoFunction != null && locoFunction.getNumber() != null) {
            locoFunctions.put(locoFunction.getNumber(), locoFunction);
          }
          locoFunction = new FunctionBean();
        }
        case FUNCTIONEN_2 -> {
          functions = true;
          if (locoFunction != null && locoFunction.getNumber() != null) {
            locoFunctions.put(locoFunction.getNumber(), locoFunction);
          }
          locoFunction = new FunctionBean();
        }
        case PRG -> {
          prg = true;
          if (configurationValue != null && configurationValue.getValue() != null) {
            configRegister.put(configurationValue.getNumber(), configurationValue);
          }
          configurationValue = new ConfigurationValueBean();
        }
        default -> {
          if (s.contains("=")) {
            String[] kp = s.split("=");
            String key = kp[0];
            String val = kp[1];

            if (VERSION.equals(ps)) {
              Logger.trace("Version = " + val);
            }

            if (functions && !prg) {
              if (NR.equals(key) || TYP.equals(key) || WERT.equals(key)) {
                if (locoFunction != null) {
                  if (NR.equals(key)) {
                    locoFunction.setNumber(val);
                  }
                  if (TYP.equals(key)) {
                    locoFunction.setFunctionType(val);
                  }
                  if (WERT.equals(key)) {
                    locoFunction.setValue(val);
                  }
                }
              }
            }
            if (functions && prg) {
              if (ADRESSE_2.equals(key) || NAME_2.equals(key) || WERT.equals(key)) {
                if (configurationValue != null) {
                  if (ADRESSE_2.equals(key)) {
                    configurationValue.setNumber(val);
                  }
                  if (NAME_2.equals(key)) {
                    configurationValue.setName(val);
                  }
                  if (WERT.equals(key)) {
                    configurationValue.setValue(val);
                  }
                }
              }
            } else {
              lm.put(key, val);
            }
          } else {
            Logger.debug("Tag?: " + s);
          }
        }
      }
      ps = s;
    }
    // parse the last loc
    if (lm.containsKey(".uid")) {
      LocomotiveBean loc = createLoco(lm, locoFunctions, configRegister);
      locs.add(loc);
    }
    return locs;
  }

  private LocomotiveBean createLoco(Map<String, String> locoProps, Map<Integer, FunctionBean> locoFunctions, Map<Integer, ConfigurationValueBean> configRegister) {
    String name = locoProps.get(NAME);
    String previousName = locoProps.get(VORNAME);
    Long uid = null;
    if (locoProps.get(UID) != null) {
      uid = Long.decode(locoProps.get(UID));
    }
    Long mfxUid = null;
    if (locoProps.get(MFXUID) != null) {
      mfxUid = Long.decode(locoProps.get(MFXUID));
    }
    Integer address = null;
    if (locoProps.get(ADDRESSE) != null) {
      address = Integer.decode(locoProps.get(ADDRESSE));
    }
    String icon = locoProps.get(ICON);
    String decoderType = locoProps.get(TYPE);
    String mfxSid = locoProps.get(SID);
    Integer tachoMax = null;
    if (locoProps.get(TACHOMAX) != null) {
      tachoMax = Integer.decode(locoProps.get(TACHOMAX));
    }
    Integer vMin = null;
    if (locoProps.get(VMIN) != null) {
      vMin = Integer.valueOf(locoProps.get(VMIN));
    }
    Integer accelerationDelay = null;
    if (locoProps.get(AV) != null) {
      accelerationDelay = Integer.valueOf(locoProps.get(AV));
    }
    Integer brakeDelay = null;
    if (locoProps.get(BV) != null) {
      brakeDelay = Integer.valueOf(locoProps.get(BV));
    }
    Integer volume = null;
    if (locoProps.get(VOLUME) != null) {
      volume = Integer.valueOf(locoProps.get(VOLUME));
    }
    String spm = locoProps.get(SPM);
    Integer velocity = null;
    if (locoProps.get(VELOCITY) != null) {
      velocity = Integer.valueOf(locoProps.get(VELOCITY));
    }
    Integer direction = null;
    if (locoProps.get(RICHTUNG) != null) {
      direction = Integer.valueOf(locoProps.get(RICHTUNG));
    }
    String mfxType = locoProps.get(MFXTYPE);
    String block = locoProps.get(BLOCKS);

    Long id = (uid != null ? uid.longValue() : null);

    if (address == null) {
      //Check the CV register for a CV "1" or name addres
      if (configRegister.containsKey(1)) {
        ConfigurationValueBean cvb = configRegister.get(1);
        address = cvb.getValue();
        decoderType = "mm2_prg";
      }

    }

    LocomotiveBean lb = new LocomotiveBean(id, name, previousName, uid, mfxUid, address, icon, decoderType,
            mfxSid, tachoMax, vMin, accelerationDelay, brakeDelay, volume, spm, velocity, direction, mfxType, block,
            false, null, true);

    //Ignore functions which have no functionType
    Logger.trace("Loc: " + name + " has " + locoFunctions.size() + " functions");
    for (FunctionBean function : locoFunctions.values()) {
      if (function.getNumber() != null && function.getFunctionType() != null) {
        if (function.getValue() == null) {
          function.setValue(0);
        }
        function.setLocomotiveId(id);
        lb.getFunctions().put(function.getNumber(), function);
      }
    }
    return lb;
  }

  public static void main(String[] a) throws Exception {
    Path path = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "lokomotives.cs2");

    String loksFile = Files.readString(path);

    LocomotiveBeanParser lp = new LocomotiveBeanParser();
    List<LocomotiveBean> locs = lp.parseLocomotivesFile(loksFile);

    for (LocomotiveBean loc : locs) {
      Logger.trace(loc);
    }

  }

}
