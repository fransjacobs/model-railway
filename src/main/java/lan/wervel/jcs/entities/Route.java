/*
 * Copyright (C) 2018 Frans Jacobs.
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
package lan.wervel.jcs.entities;

import java.math.BigDecimal;
import java.util.Objects;

public class Route extends ControllableDevice {

    BigDecimal drwaId;
    BigDecimal latiId;

    public Route() {
        this(null, null, null, null, null, null);
    }

    public Route(BigDecimal id, Integer address, BigDecimal drwaId, BigDecimal latiId) {
        this(id, address, null, null, drwaId, latiId);
    }

    public Route(Integer address, BigDecimal drwaId, BigDecimal latiId) {
        this(null, address, null, null, drwaId, latiId);
    }

    public Route(Integer address, String name, String description, BigDecimal drwaId, BigDecimal latiId) {
        this(null, address, name, description, drwaId, latiId);
    }

    public Route(BigDecimal id, Integer address, String name, String description, BigDecimal drwaId, BigDecimal latiId) {
        super(id, address, name, description);
        this.drwaId = drwaId;
        this.latiId = latiId;

    }

    public BigDecimal getDrwaId() {
        return drwaId;
    }

    public void setDrwaId(BigDecimal drwaId) {
        this.drwaId = drwaId;
    }

    public BigDecimal getLatiId() {
        return latiId;
    }

    public void setLatiId(BigDecimal latiId) {
        this.latiId = latiId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(super.hashCode());
        hash = 17 * hash + Objects.hashCode(this.drwaId);
        hash = 17 * hash + Objects.hashCode(this.latiId);
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
        final Route other = (Route) obj;
        if (!super.equals(other)) {
            return false;
        }
        if (!Objects.equals(this.drwaId, other.drwaId)) {
            return false;
        }
        return Objects.equals(this.latiId, other.latiId);
    }

    @Override
    public String toString() {
        return "Route{" + "drwaId=" + drwaId + ", latiId=" + latiId + '}';
    }

    @Override
    public String toLogString() {
        return toString();
    }

}
