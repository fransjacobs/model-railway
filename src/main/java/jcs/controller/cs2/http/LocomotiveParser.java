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
package jcs.controller.cs2.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcs.entities.Locomotive;
import jcs.entities.enums.DecoderType;
import jcs.entities.enums.Direction;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class LocomotiveParser {

    private static final String LOCOMOTIVE_START = "[lokomotive]";
    private static final String VERSION = "version";
    private static final String MINOR = ".minor";
    private static final String SESSION = "session";
    private static final String ID = ".id";
    private static final String LOCOMOTIVE = "lokomotive";
    private static final String NAME = ".name";
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

    public List<Locomotive> parseLocomotivesFile(String locofile) {
        List<Locomotive> locs = new ArrayList<>();
        List<String> items = Arrays.asList(locofile.split("\n"));
        Map<String, String> lm = new HashMap<>();
        Map<String, String> fm = new HashMap<>();
        String fkey = null;
        boolean functions = false;
        for (String s : items) {
            switch (s) {
                case LOCOMOTIVE_START:
                    break;
                case VERSION:
                    break;
                case SESSION:
                    break;
                case LOCOMOTIVE:
                    if (lm.containsKey(".uid")) {
                        Locomotive loc = createLoco(lm, fm);
                        locs.add(loc);
                    }
                    functions = false;
                    lm.clear();
                    fm.clear();
                    break;
                case FUNCTIONEN:
                    functions = true;
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
                                if (NR.equals(key)) {
                                    fkey = "f" + val;
                                }
                                if (TYP.equals(key)) {
                                    fm.put(fkey, val);
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
        if (lm.containsKey(".uid")) {
            Locomotive loc = createLoco(lm, fm);
            locs.add(loc);
        }
        return locs;
    }

    private Locomotive createLoco(Map<String, String> locoProps, Map<String, String> funcProps) {
        Integer address = null;
        if (locoProps.get(ADDRESSE) != null) {
            address = Integer.decode(locoProps.get(ADDRESSE));
        }
        Integer uid = null;
        if (locoProps.get(UID) != null) {
            uid = Integer.decode(locoProps.get(UID));
        }
        String name = locoProps.get(NAME);
        String description = name;

        DecoderType decoderType = DecoderType.get(locoProps.get(TYPE));
        Direction direction = null;
        if (locoProps.get(RICHTUNG) != null) {
            direction = Direction.cs2Get(Integer.parseInt(locoProps.get(RICHTUNG)));
        }
        Direction defaultDirection = Direction.FORWARDS;

        Integer speed = 0;
        if (locoProps.get(VELOCITY) != null) {
            speed = Integer.parseInt(locoProps.get(VELOCITY));
        }
        Integer speedSteps;
        
        if(decoderType == null) {
            //set a default!
            decoderType = DecoderType.MM;
        }
        
        switch (decoderType) {
            case MM2:
                speedSteps = 27;
                break;
            case MFX:
                speedSteps = 126;
                break;
            case DCC:
                speedSteps = 28;
                break;
            default:
                speedSteps = 14;
                break;
        }
        Integer tachoMax = null;
        if (locoProps.get(TACHOMAX) != null) {
            tachoMax = Integer.parseInt(locoProps.get(TACHOMAX));
        }
        Integer vMin = null;
        if (locoProps.get(VMIN) != null) {
            vMin = Integer.parseInt(locoProps.get(VMIN));
        }
        Integer vMax = null;
        if (locoProps.get(VMAX) != null) {
            vMax = Integer.parseInt(locoProps.get(VMAX));
        }
        String iconName = locoProps.get(ICON);

        Locomotive loc = new Locomotive();
        if (address == null) {
            loc.setAddress(uid);
        } else {
            loc.setAddress(address);
        }
        loc.setName(name);
        loc.setDescription(description);
        loc.setDecoderType(decoderType);
        loc.setTachoMax(tachoMax);
        loc.setvMin(vMin);
        loc.setvMax(vMax);
        loc.setDirection(direction);
        loc.setDefaultDirection(defaultDirection);
        loc.setSpeedSteps(speedSteps);
        loc.setSpeed(speed);
        loc.setIconName(iconName);

        int functionCount;
        if (funcProps.size() == 1) {
            functionCount = 1;
        } else if (funcProps.size() > 1 && funcProps.size() < 6) {
            functionCount = 5;
        } else if (funcProps.size() > 5 && funcProps.size() < 9) {
            functionCount = 8;
        } else if (funcProps.size() > 8 && funcProps.size() < 17) {
            functionCount = 16;
        } else {
            functionCount = 32;
        }
        int[] functionValues = new int[functionCount];
        StringBuilder ft = new StringBuilder();
        for (int i = 0; i < functionValues.length; i++) {
            //Lights always on
            if (i == 0) {
                functionValues[i] = 1;
            } else {
                functionValues[i] = 0;
            }

            if (funcProps.containsKey("f" + i)) {
                ft.append(funcProps.get("f" + i));
            } else {
                ft.append("0");
            }
            if (i + 1 < functionValues.length) {
                ft.append(",");
            }
        }
        loc.setFunctionCount(functionCount);
        loc.setFunctionValues(functionValues);
        loc.setFunctionTypes(ft.toString());

        return loc;
    }

}
