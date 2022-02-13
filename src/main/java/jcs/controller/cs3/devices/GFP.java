/*
 * Copyright (C) 2022 fransjacobs.
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
package jcs.controller.cs3.devices;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author fransjacobs
 */
public class GFP {

    private String uid;
    private String name;
    private String typeName;
    private String identifier;
    private Integer type;
    private String articleNumber;
    private String serial;
    private Integer queryInterval;
    private String version;

    private final Map<String, GFPChannel> channels;

    public static final String MAIN = "MAIN";
    public static final String PROG = "PROG";
    public static final String VOLT = "VOLT";
    public static final String TEMP = "TEMP";

    public GFP() {
        channels = new HashMap<>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public Integer getQueryInterval() {
        return queryInterval;
    }

    public void setQueryInterval(Integer queryInterval) {
        this.queryInterval = queryInterval;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, GFPChannel> getChannels() {
        return channels;
    }

    public GFPChannel getChannel(String channelKey) {
        return this.channels.get(channelKey);
    }

    public void setChannel(GFPChannel channel) {
        if (channel == null) {
            return;
        }
        switch (channel.getName()) {
            case MAIN:
                this.channels.put(MAIN, channel);
                break;
            case PROG:
                this.channels.put(PROG, channel);
                break;
            case VOLT:
                this.channels.put(VOLT, channel);
                break;
            case TEMP:
                this.channels.put(TEMP, channel);
                break;
            default:
                break;
        }
    }

    //Convenience
    public Double getTrackCurrent() {
        GFPChannel m = this.getChannel(MAIN);
        if (m != null) {
            return m.getHumanValue();
        } else {
            return null;
        }
    }

    public Double getProgrammingTrackCurrent() {
        GFPChannel m = this.getChannel(PROG);
        if (m != null) {
            return m.getHumanValue();
        } else {
            return null;
        }
    }

    public Double getTrackVoltage() {
        GFPChannel m = this.getChannel(VOLT);
        if (m != null) {
            return m.getHumanValue();
        } else {
            return null;
        }
    }

    public Double getCS3Temperature() {
        GFPChannel m = this.getChannel(TEMP);
        if (m != null) {
            return m.getHumanValue();
        } else {
            return null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.uid);
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.typeName);
        hash = 97 * hash + Objects.hashCode(this.identifier);
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.articleNumber);
        hash = 97 * hash + Objects.hashCode(this.serial);
        hash = 97 * hash + Objects.hashCode(this.queryInterval);
        hash = 97 * hash + Objects.hashCode(this.version);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GFP other = (GFP) obj;
        if (!Objects.equals(this.uid, other.uid)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.typeName, other.typeName)) {
            return false;
        }
        if (!Objects.equals(this.identifier, other.identifier)) {
            return false;
        }
        if (!Objects.equals(this.articleNumber, other.articleNumber)) {
            return false;
        }
        if (!Objects.equals(this.serial, other.serial)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return Objects.equals(this.queryInterval, other.queryInterval);
    }

    @Override
    public String toString() {
        return "GFP{" + "uid=" + uid + ", name=" + name + ", articleNumber=" + articleNumber + ", serial=" + serial + ", version=" + version + '}';
    }

}
