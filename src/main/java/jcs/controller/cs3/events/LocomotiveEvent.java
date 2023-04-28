/*
 * Copyright 2023 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.controller.cs3.events;

import java.io.Serializable;
import jcs.entities.LocomotiveBean;
import jcs.entities.enums.Direction;

/**
 *
 * @author Frans Jacobs
 */
public class LocomotiveEvent implements Serializable {

    private final Long id;
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

    public Long getId() {
        return id;
    }

    public boolean isEventFor(Long id) {
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
