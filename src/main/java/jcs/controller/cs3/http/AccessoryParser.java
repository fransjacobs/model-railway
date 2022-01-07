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
package jcs.controller.cs3.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcs.controller.cs3.AccessoryStatus;
import jcs.entities.SignalBean;
import jcs.entities.SolenoidAccessory;
import jcs.entities.SwitchBean;
import org.tinylog.Logger;

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
    private static final String DECTYP = ".dectyp";

    private static final String DEFAULT_TYPE = "std_rot_gruen";
    private static final String DEFAULT_RED_TYPE = "std_rot";
    private static final String DEFAULT_GREEN_TYPE = "std_gruen";

    private static final String TURNOUT_R = "rechtsweiche";
    private static final String TURNOUT_L = "linksweiche";
    private static final String TURNOUT_X = "y_weiche";
    private static final String TURNOUT_3 = "dreiwegweiche";

    private static final String ENTKUPPLUNGSGLEIS = "entkupplungsgleis";
    private static final String ENTKUPPLUNGSGLEIS_1 = "entkupplungsgleis_1";

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
        for (String s : items) {
            Logger.trace("Line: " + s);
            switch (s) {
                case ACCESSORY_START:
                    break;
                case VERSION:
                    break;
                case ARTIKEL:
                    if (am.containsKey(ID)) {
                        SolenoidAccessory sa = createAccessory(am);
                        if (sa != null) {
                            accessories.add(sa);
                        }
                    }
                    am.clear();
                    break;
                default:
                    if (s.contains("=")) {
                        String[] kp = s.split("=");
                        String key = kp[0];
                        String val = kp[1];
                        am.put(key, val);
                    } else {
                        Logger.debug("Tag?: " + s);
                    }
                    break;
            }
        }
        // parse the last Accessory
        if (am.containsKey(ID)) {
            SolenoidAccessory sa = createAccessory(am);
            if (sa != null) {
                accessories.add(sa);
            }
        }
        return accessories;
    }

    public List<AccessoryStatus> parseAccessoryStatusFile(String gaSrfile) {
        List<AccessoryStatus> accessories = new ArrayList<>();
        List<String> items = Arrays.asList(gaSrfile.split("\n"));
        Map<String, String> am = new HashMap<>();
        for (String s : items) {
            //Logger.trace("Line: " + s);
            switch (s) {
                case ACCESSORY_START:
                    break;
                case VERSION:
                    break;
                case ARTIKEL:
                    if (am.containsKey(ID)) {
                        Integer address = Integer.decode(am.get(ID));
                        String status;
                        if (am.containsKey(STELLUNG)) {
                            status = am.get(STELLUNG);
                        } else {
                            status = "0";
                        }
                        AccessoryStatus as = new AccessoryStatus(address, status);
                        accessories.add(as);
                    }
                    am.clear();
                    break;
                default:
                    if (s.contains("=")) {
                        String[] kp = s.split("=");
                        String key = kp[0];
                        String val = kp[1];
                        am.put(key, val);
                    } else {
                        Logger.debug("Tag?: " + s);
                    }
                    break;
            }
        }
        // parse the last Accessory
        if (am.containsKey(ID)) {
            if (am.containsKey(ID)) {
                Integer address = Integer.decode(am.get(ID));
                String status;
                if (am.containsKey(STELLUNG)) {
                    status = am.get(STELLUNG);
                } else {
                    status = "0";
                }
                AccessoryStatus as = new AccessoryStatus(address, status);
                accessories.add(as);
            }
        }
        return accessories;
    }

    private SolenoidAccessory createAccessory(Map<String, String> accessoryProps) {
        String type = accessoryProps.get(TYPE);

        SolenoidAccessory sa;
        switch (type) {
            case TURNOUT_R:
                sa = createTurnout(accessoryProps, "R");
                break;
            case TURNOUT_L:
                sa = createTurnout(accessoryProps, "L");
                break;
            case TURNOUT_X:
                sa = createTurnout(accessoryProps, "X");
                break;
            case TURNOUT_3:
                sa = createTurnout(accessoryProps, "3");
                break;
            case LIGHT_SIGNAL_HP01:
                sa = createSignal(accessoryProps, "Entry", 2);
                break;
            case LIGHT_SIGNAL_HP02:
                sa = createSignal(accessoryProps, "Entry", 2);
                break;
            case LIGHT_SIGNAL_HP012:
                sa = createSignal(accessoryProps, "Entry", 4);
                break;
            case LIGHT_SIGNAL_HP012_SH01:
                sa = createSignal(accessoryProps, "Leave", 4);
                break;
            case LIGHT_SIGNAL_SH01:
                sa = createSignal(accessoryProps, "Midget", 2);
                break;
            case FORM_SIGNAL_HP01:
                sa = createSignal(accessoryProps, "Block", 2);
                break;
            case FORM_SIGNAL_HP02:
                sa = createSignal(accessoryProps, "Block", 2);
                break;
            case FORM_SIGNAL_HP012:
                sa = createSignal(accessoryProps, "Block", 4);
                break;
            case FORM_SIGNAL_HP012_SH01:
                sa = createSignal(accessoryProps, "Leave", 4);
                break;
            case FORM_SIGNAL_SH01:
                sa = createSignal(accessoryProps, "Midget", 2);
                break;
            case URC_SIGNAL_HP01:
                sa = createSignal(accessoryProps, "Block", 2);
                break;
            case URC_SIGNAL_HP012:
                sa = createSignal(accessoryProps, "Block", 2);
                break;
            case URC_SIGNAL_HP012_SH01:
                sa = createSignal(accessoryProps, "Leave", 4);
                break;
            case URC_SIGNAL_SH01:
                sa = createSignal(accessoryProps, "Midget", 2);
                break;
            default:
                sa = null;
        }

        if (sa != null) {
            if (accessoryProps.get(ID) != null) {
                Integer address = Integer.parseInt(accessoryProps.get(ID));
                sa.setAddress(address);
            }
            String name = accessoryProps.get(NAME);
            sa.setName(name);

            if (accessoryProps.get(SCHALTZEIT) != null) {
                Integer switchTime = Integer.parseInt(accessoryProps.get(SCHALTZEIT));
                sa.setSwitchTime(switchTime);
            }
        }
        return sa;
    }

    private SignalBean createSignal(Map<String, String> accessoryProps, String type, int lightImages) {
        SignalBean s = new SignalBean();
        s.setDescription(type);
        s.setLightImages(lightImages);

        String d = accessoryProps.get(STELLUNG);

        if ("1".equals(d)) {
            s.setGreen();
            if (lightImages > 2) {
                s.setGreen2();
            }
        } else {
            s.setRed();
            if (lightImages > 2) {
                s.setRed2();
            }
        }

        return s;
    }

    private SwitchBean createTurnout(Map<String, String> accessoryProps, String type) {
        SwitchBean t = new SwitchBean();
        t.setDescription(type);
        String d = accessoryProps.get(STELLUNG);

        if ("1".equals(d)) {
            t.setStraight();
        } else {
            t.setCurved();
        }

        return t;
    }

}
