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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class LocomotiveBeanParser {

    private static final String LOCOMOTIVES_START = "[lokomotive]";
    private static final String VERSION = "version";
    private static final String MINOR = ".minor";
    private static final String SESSION = "session";
    private static final String ID = ".id";
    private static final String LOCOMOTIVE = "lokomotive";
    private static final String NAME = ".name";
    private static final String VORNAME = ".vorname";
    private static final String SYMBOL = ".symbol";
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
    private static final String PROGMASK = ".progmask";
    private static final String VOLUME = ".volume";
    private static final String TACHOMAX = ".tachomax";
    private static final String VMIN = ".vmin";
    private static final String VMAX = ".vmax";
    private static final String SPM = ".spm";
    private static final String MFXTYPE = ".mfxtyp";
    private static final String BLOCKS = ".blocks";
    private static final String PRG = ".prg";
    private static final String FUNCTIONEN = ".funktionen";
    private static final String FUNCTIONEN_2 = ".funktionen_2";
    private static final String NR = "..nr";
    private static final String TYP = "..typ";
    private static final String WERT = "..wert";

    public List<LocomotiveBean> parseLocomotivesFile(String locofile) {
        List<LocomotiveBean> locs = new LinkedList<>();
        List<String> items = Arrays.asList(locofile.split("\n"));
        Map<String, String> lm = new HashMap<>();
        Map<Integer, FunctionBean> locoFunctions = new HashMap<>();
        FunctionBean locoFunction = null;
        boolean functions = false;

        for (String s : items) {
            switch (s) {
                case LOCOMOTIVES_START:
                    break;
                case VERSION:
                    break;
                case SESSION:
                    break;
                case LOCOMOTIVE:
                    if (lm.containsKey(".uid")) {
                        LocomotiveBean loc = createLoco(lm, locoFunctions);
                        locs.add(loc);
                    }
                    functions = false;
                    lm.clear();
                    locoFunctions.clear();
                    locoFunction = null;
                    break;
                case FUNCTIONEN:
                    functions = true;
                    if (locoFunction != null && locoFunction.getNumber() != null) {
                        locoFunctions.put(locoFunction.getNumber(), locoFunction);
                    }
                    locoFunction = new FunctionBean();
                    break;
                case FUNCTIONEN_2:
                    functions = true;
                    break;
                case PRG:
                    break;
                default:
                    if (s.contains("=")) {
                        String[] kp = s.split("=");
                        String key = kp[0];
                        String val = kp[1];

                        if (functions) {
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
                        } else {
                            lm.put(key, val);
                        }
                    } else {
                        Logger.debug("Tag?: " + s);
                    }
                    break;
            }
        }
        // parse the last loc
        if (lm.containsKey(
                ".uid")) {
            LocomotiveBean loc = createLoco(lm, locoFunctions);
            locs.add(loc);
        }
        return locs;
    }

    private LocomotiveBean createLoco(Map<String, String> locoProps, Map<Integer, FunctionBean> locoFunctions) {
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
            vMin = Integer.parseInt(locoProps.get(VMIN));
        }
        Integer accelerationDelay = null;
        if (locoProps.get(AV) != null) {
            accelerationDelay = Integer.parseInt(locoProps.get(AV));
        }
        Integer brakeDelay = null;
        if (locoProps.get(BV) != null) {
            brakeDelay = Integer.parseInt(locoProps.get(BV));
        }
        Integer volume = null;
        if (locoProps.get(VOLUME) != null) {
            volume = Integer.parseInt(locoProps.get(VOLUME));
        }
        String spm = locoProps.get(SPM);
        Integer velocity = null;
        if (locoProps.get(VELOCITY) != null) {
            velocity = Integer.parseInt(locoProps.get(VELOCITY));
        }
        Integer direction = null;
        if (locoProps.get(RICHTUNG) != null) {
            direction = Integer.parseInt(locoProps.get(RICHTUNG));
        }
        String mfxType = locoProps.get(MFXTYPE);
        String blocks = locoProps.get(BLOCKS);

        BigDecimal id = new BigDecimal(uid);

        LocomotiveBean lb = new LocomotiveBean(id, name, previousName, uid, mfxUid, address, icon, decoderType,
                mfxSid, tachoMax, vMin, accelerationDelay, brakeDelay, volume, spm,
                velocity, direction, mfxType, blocks);

        //Ignore functions which have no functionType
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
}
