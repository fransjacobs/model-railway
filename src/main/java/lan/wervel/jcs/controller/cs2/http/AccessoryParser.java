/*
 * Copyright (C) 2020 fransjacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package lan.wervel.jcs.controller.cs2.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lan.wervel.jcs.entities.Signal;
import lan.wervel.jcs.entities.SolenoidAccessory;
import lan.wervel.jcs.entities.Turnout;
import org.pmw.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class AccessoryParser {

    private static final String ACCESSORY_START = "[magnetartikel]";
    private static final String VERSION = "version";
    private static final String MINOR = ".minor";
    private static final String MAJOR = ".major";
    private static final String ARTIKEL = "artikel";
    private static final String ID = ".id";
    private static final String NAME = ".name";
    private static final String TYPE = ".typ";
    private static final String STELLUNG = ".stellung";
    private static final String SCHALTZEIT = ".schaltzeit";
    private static final String DECODER = ".decoder";
    private static final String UNGERADE = ".ungerade";
    private static final String DECTYP =  ".dectyp";
    
    private static final String DEFAULT_TYPE = "std_rot_gruen";
    private static final String DEFAULT_RED_TYPE = "std_rot";
    private static final String DEFAULT_GREEN_TYPE = "std_gruen";

    private static final String TURNOUT_R = "rechtsweiche";
    private static final String TURNOUT_L = "linksweiche";
    private static final String TURNOUT_X = "y_weiche";
    private static final String TURNOUT_3 = "dreiwegweiche";

    private static final String ENTKUPPLUNGSGLEIS =  "entkupplungsgleis";
    private static final String ENTKUPPLUNGSGLEIS_1 =  "entkupplungsgleis_1";

    private static final String LIGHT_SIGNAL_HP01 = "lichtsignal_HP01";
    private static final String LIGHT_SIGNAL_HP02 = "lichtsignal_HP02";
    private static final String LIGHT_SIGNAL_HP012 = "lichtsignal_HP012";
    private static final String LIGHT_SIGNAL_HP012_SH01 = "lichtsignal_HP012_SH01";
    private static final String LIGHT_SIGNAL_SH01 = "lichtsignal_SH01";
    
    private static final String FORM_SIGNAL_HP01 = "formsignal_HP01";
    private static final String FORM_SIGNAL_HP02 = "formsignal_HP02";
    private static final String FORM_SIGNAL_HP012 = "formsignal_HP012";
    private static final String FORM_SIGNAL_HP012_SH01 = "formsignal_HP012_SH01";
    private static final String FORM_SIGNAL_SH01 = "formsignal_SH01";
    
    private static final String URC_SIGNAL_HP01 = "urc_lichtsignal_HP01";
    private static final String URC_SIGNAL_HP012 = "urc_lichtsignal_HP012";
    private static final String URC_SIGNAL_HP012_SH01 = "urc_lichtsignal_HP012_SH01";
    private static final String URC_SIGNAL_SH01 = "urc_lichtsignal_SH01";
    

    public List<SolenoidAccessory> parseAccessoryFile(String gafile) {
        List<SolenoidAccessory> accessories = new ArrayList<>();
        List<String> items = Arrays.asList(gafile.split("\n"));
        Map<String, String> am = new HashMap<>();
        //Map<String, String> fm = new HashMap<>();
        //String fkey = null;
        //boolean functions = false;
        for (String s : items) {
            switch (s) {
                case ACCESSORY_START:
                    break;
                case VERSION:
                    break;
                case ARTIKEL:
                    if (am.containsKey(ID)) {
 //                       Locomotive loc = createLoco(lm, fm);
                        //accessories.add(loc);
                    }
                    //functions = false;
                    am.clear();
                    //fm.clear();
                    break;
                default:
                    if (s.contains("=")) {
                        String[] kp = s.split("=");
                        String key = kp[0];
                        String val = kp[1];
                    } else {
                        Logger.debug("Tag?: " + s);
                    }
                    break;
            }
        }
        // parse the last loc
        if (am.containsKey(ID)) {
            //Locomotive loc = createLoco(lm, fm);
            //locs.add(loc);
        }
        return accessories;
    }

    
    private SolenoidAccessory createAccessory(Map<String, String> accessoryProps) {
        Integer address = null;
        if (accessoryProps.get(ID) != null) {
            address = Integer.parseInt(accessoryProps.get(ID));
        }
        
        
        Signal s = new Signal();
        Turnout t = new Turnout();
        return null; //stub
    }
//    private Locomotive createLoco(Map<String, String> locoProps, Map<String, String> funcProps) {
//        Integer uid = null;
//        if (locoProps.get(UID) != null) {
//            uid = Integer.decode(locoProps.get(UID));
//        }
//        String name = locoProps.get(NAME);
//        String description = name;
//
//        String catalogNumber = null;
//        DecoderType decoderType = DecoderType.get(locoProps.get(TYPE));
//        Direction direction = null;
//        if (locoProps.get(RICHTUNG) != null) {
//            direction = Direction.cs2Get(Integer.parseInt(locoProps.get(RICHTUNG)));
//        }
//        Integer speed = null;
//        if (locoProps.get(VELOCITY) != null) {
//            speed = Integer.parseInt(locoProps.get(VELOCITY));
//        }
//        Integer speedSteps;
//        switch (decoderType) {
//            case MM2:
//                speedSteps = 27;
//                break;
//            case MFX:
//                speedSteps = 126;
//                break;
//            case DCC:
//                speedSteps = 28;
//                break;
//            default:
//                speedSteps = 14;
//                break;
//        }
//        Integer tachoMax = null;
//        if (locoProps.get(TACHOMAX) != null) {
//            tachoMax = Integer.parseInt(locoProps.get(TACHOMAX));
//        }
//        Integer vMin = null;
//        if (locoProps.get(VMIN) != null) {
//            vMin = Integer.parseInt(locoProps.get(VMIN));
//        }
//        Integer vMax = null;
//        if (locoProps.get(VMAX) != null) {
//            vMax = Integer.parseInt(locoProps.get(VMAX));
//        }
//        Direction defaultDirection = null;
//        String iconName = locoProps.get(ICON);
//
//        Locomotive loc = new Locomotive();
//        if (address == null) {
//            loc.setAddress(uid);
//        } else {
//            loc.setAddress(address);
//        }
//        loc.setName(name);
//        loc.setDescription(description);
//        loc.setDecoderType(decoderType);
//        loc.setTachoMax(tachoMax);
//        loc.setvMin(vMin);
//        loc.setvMax(vMax);
//        loc.setSpeedSteps(speedSteps);
//        loc.setIconName(iconName);
//
//        int functionCount;
//        if (funcProps.size() == 1) {
//            functionCount = 1;
//        } else if (funcProps.size() > 1 && funcProps.size() < 6) {
//            functionCount = 5;
//        } else if (funcProps.size() > 5 && funcProps.size() < 9) {
//            functionCount = 8;
//        } else if (funcProps.size() > 8 && funcProps.size() < 17) {
//            functionCount = 16;
//        } else {
//            functionCount = 32;
//        }
//        int[] functionValues = new int[functionCount];
//        StringBuilder ft = new StringBuilder();
//        for (int i = 0; i < functionValues.length; i++) {
//            //Lights always on
//            if (i == 0) {
//                functionValues[i] = 1;
//            } else {
//                functionValues[i] = 0;
//            }
//
//            if (funcProps.containsKey("f" + i)) {
//                ft.append(funcProps.get("f" + i));
//            } else {
//                ft.append("0");
//            }
//            if (i + 1 < functionValues.length) {
//                ft.append(",");
//            }
//        }
//        loc.setFunctionCount(functionCount);
//        loc.setFunctionValues(functionValues);
//        loc.setFunctionTypes(ft.toString());
//
//        return loc;
//    }

}
