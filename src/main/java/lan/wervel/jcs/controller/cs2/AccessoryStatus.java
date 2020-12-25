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
package lan.wervel.jcs.controller.cs2;

import java.util.Objects;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.SignalValue;

/**
 *
 * @author fransjacobs
 */
public class AccessoryStatus {

    private final Integer address;
    private final String status;

    public AccessoryStatus(Integer address, String status) {
        this.address = address;
        this.status = status;
    }

    public Integer getAddress() {
        return address;
    }

    public String getStatus() {
        return status;
    }

    public SignalValue getSignalValue() {
        switch (status) {
            case "1":
                return SignalValue.Hp1;
            case "2":
                return SignalValue.Hp0Sh1;
            case "3":
                return SignalValue.Hp2;
            default:
                return SignalValue.Hp0;
        }
    }

    public AccessoryValue getAccessoryValue() {
        switch (status) {
            case "1":
                return AccessoryValue.GREEN;
            default:
                return AccessoryValue.RED;
        }
    }

    @Override
    public String toString() {
        return "AccessoryStatus{" + "address=" + address + ", status=" + status + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.address);
        hash = 17 * hash + Objects.hashCode(this.status);
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
        final AccessoryStatus other = (AccessoryStatus) obj;
        if (!Objects.equals(this.status, other.status)) {
            return false;
        }
        if (!Objects.equals(this.address, other.address)) {
            return false;
        }
        return true;
    }

}
