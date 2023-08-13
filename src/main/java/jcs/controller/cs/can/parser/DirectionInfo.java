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
package jcs.controller.cs.can.parser;

import java.io.Serializable;
import jcs.controller.cs.can.CanMessage;
import jcs.entities.enums.Direction;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class DirectionInfo implements Serializable {

    private Direction direction;

    public DirectionInfo(Direction direction) {
        this.direction = direction;
    }

    public DirectionInfo(CanMessage locDirection) {
        parseMessage(locDirection);
    }

    private void parseMessage(CanMessage locDirection) {
        Logger.debug(locDirection);
        int[] data = locDirection.getResponse(0).getData();
        int dir = data[4];

        this.direction = Direction.getDirection(dir);
    }

    @Override
    public String toString() {
        return "DirectionInfo{" + "direction=" + direction + '}';
    }

    public Direction getDirection() {
        return direction;
    }

}
