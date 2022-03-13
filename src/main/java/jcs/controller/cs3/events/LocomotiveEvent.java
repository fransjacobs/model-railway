/*
 * Copyright (C) 2020 Frans Jacobs <frans.jacobs@gmail.com>.
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
package jcs.controller.cs3.events;

import java.io.Serializable;
import java.math.BigDecimal;
import jcs.entities.LocomotiveBean;
import jcs.entities.enums.Direction;

/**
 *
 * @author Frans Jacobs
 */
public class LocomotiveEvent implements Serializable {

    private final BigDecimal id;
    private final Direction direction;
    private final Integer speed;
    private final boolean f0;
    private final boolean f1;
    private final boolean f2;
    private final boolean f3;
    private final boolean f4;

    public LocomotiveEvent(LocomotiveBean locomotive) {
        this.id = locomotive.getId();
        this.direction = locomotive.getDirection();
        this.speed = locomotive.getVelocity();
        this.f0 = locomotive.isFunctionValue(0);
        this.f1 = locomotive.isFunctionValue(1);
        this.f2 = locomotive.isFunctionValue(2);
        this.f3 = locomotive.isFunctionValue(3);
        this.f4 = locomotive.isFunctionValue(4);
    }

    public BigDecimal getId() {
        return id;
    }

    public boolean isEventFor(BigDecimal id) {
        if (id != null) {
            return id.equals(this.id);
        }
        return false;
    }

    public Direction getDirection() {
        return direction;
    }

    public Integer getSpeed() {
        return speed;
    }

    public boolean isF0() {
        return f0;
    }

    public boolean isF1() {
        return f1;
    }

    public boolean isF2() {
        return f2;
    }

    public boolean isF3() {
        return f3;
    }

    public boolean isF4() {
        return f4;
    }

    @Override
    public String toString() {
        return "LocomotiveEvent{" + "id=" + id + ", direction=" + direction + ", speed=" + speed + ", f0=" + f0 + ", f1=" + f1 + ", f2=" + f2 + ", f3=" + f3 + ", f4=" + f4 + '}';
    }

}
