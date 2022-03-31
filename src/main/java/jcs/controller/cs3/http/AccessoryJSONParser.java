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

import java.util.LinkedList;
import java.util.List;
import jcs.entities.AccessoryBean;
import org.json.JSONArray;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class AccessoryJSONParser {

    private final List<AccessoryBean> turnouts;
    private final List<AccessoryBean> signals;

    public AccessoryJSONParser() {
        turnouts = new LinkedList<>();
        signals = new LinkedList<>();
    }

    public void parseAccessories(String json) {
        this.signals.clear();
        this.turnouts.clear();

        JSONArray accArray = new JSONArray(json);
        for (int i = 0; i < accArray.length(); i++) {
            AccessoryBean ab = new AccessoryBean();

            ab.setName(accArray.getJSONObject(i).getString("name"));
            ab.setId(accArray.getJSONObject(i).getBigDecimal("id"));
            ab.setAddress(accArray.getJSONObject(i).getInt("address"));
            ab.setIcon(accArray.getJSONObject(i).getString("icon"));
            ab.setIconFile(accArray.getJSONObject(i).getString("iconFile"));
            ab.setType(accArray.getJSONObject(i).getString("typ"));
            ab.setGroup(accArray.getJSONObject(i).getString("group"));
            ab.setSwitchTime(accArray.getJSONObject(i).getInt("schaltzeit"));
            ab.setStates(accArray.getJSONObject(i).getInt("states"));
            ab.setPosition(accArray.getJSONObject(i).getInt("state"));

            if (accArray.getJSONObject(i).has("prot")) {
                ab.setDecoderType(accArray.getJSONObject(i).getString("prot"));
            }
            if (accArray.getJSONObject(i).has("dectyp")) {
                ab.setDecoder(accArray.getJSONObject(i).getString("dectyp"));
            }

            if (null == ab.getGroup()) {
                Logger.trace("Unknown Accessory: " + ab.toLogString());
            } else {
                switch (ab.getGroup()) {
                    case "weichen":
                        this.turnouts.add(ab);
                        break;
                    case "lichtsignale":
                        this.signals.add(ab);
                        break;
                    default:
                        Logger.trace("Unknown Accessory: " + ab.toLogString());
                        break;
                }
            }
        }
    }

    public List<AccessoryBean> getTurnouts() {
        return turnouts;
    }

    public List<AccessoryBean> getSignals() {
        return signals;
    }

}
