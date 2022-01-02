/*
 * Copyright (C) 2020 Frans Jacobs.
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
package jcs.controller;

import java.io.Serializable;

/**
 *
 * @author Frans Jacobs
 */
public class ControllerEvent implements Serializable {

    private boolean powerOn;
    private boolean connected;

    public ControllerEvent() {
        this(false, false);
    }

    public ControllerEvent(boolean powerOn, boolean connected) {
        this.powerOn = powerOn;
        this.connected = connected;
    }

    public boolean isPowerOn() {
        return powerOn;
    }

    public void setPowerOn(boolean powerOn) {
        this.powerOn = powerOn;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.powerOn ? 1 : 0);
        hash = 73 * hash + (this.connected ? 1 : 0);
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
        final ControllerEvent other = (ControllerEvent) obj;
        if (this.powerOn != other.powerOn) {
            return false;
        }
        return this.connected == other.connected;
    }

    @Override
    public String toString() {
        return "{ Power: " + (powerOn ? "On" : "Off") + "; Connected: " + (connected ? "Yes" : "No") + "}";
    }

}
