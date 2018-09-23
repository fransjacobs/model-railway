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
package lan.wervel.jcs.repository.model;

import org.pmw.tinylog.Logger;

public class Crane extends Locomotive {

  private static final long serialVersionUID = -27030816075397101L;

  public static int DEFAULT_SPEED = 14;
  public static int MIN_SPEED = 0;
  public static int MAX_SPEED = 14;

  private boolean turn = false;
  private boolean turnLeft = true; // Left is default
  private boolean lift = false;
  private boolean liftUp = true; // up is default

  public Crane() {
    this(null, null);
  }

  public Crane(Integer address, String name) {
    this(address, name, null, null, 0);
  }

  public Crane(Integer address, String name, String description, String catalogNumber, Integer minSpeed) {
    this(address, name, description, catalogNumber, minSpeed, false, false, false, false, false);
  }

  public Crane(Integer address, String name, String description, String catalogNumber, Integer minSpeed, boolean f0, boolean f1, boolean f2, boolean f3, boolean f4) {
    this(address, name, description, catalogNumber, minSpeed, f0, f1, f2, f3, f4, DEFAULT_SPEED, null, null);
  }

  private Crane(Integer address, String name, String description, String catalogNumber, Integer minSpeed, boolean f0, boolean f1, boolean f2, boolean f3, boolean f4, Integer speed, Integer throttle, Direction direction) {
    super(address, name, description, catalogNumber, minSpeed, "crane", f0, f1, f2, f3, f4, speed, throttle, direction);
  }

  @Override
  public String toString() {
    if (this.address != null && this.address > 0) {
      return name + " [" + address + "]";
    }
    return null;
  }

  @Override
  public void setSpeed(Integer newSpeed) {
    if (speed >= MIN_SPEED || speed <= MAX_SPEED) {
      Logger.debug("Speed changing from: " + this.speed + " to: " + newSpeed);
      this.speed = newSpeed;
    }
  }

  public boolean isMagnet() {
    return isF0();
  }

  public void setMagnet(boolean newMagnet) {
    Logger.debug("Magnet changing from: " + (this.isF0() ? "On" : "Off") + " to: " + (newMagnet ? "On" : "Off"));
    setF0(newMagnet);
  }

  public void turnLeft(boolean onOff) {
    Logger.debug("Turn Left: " + (onOff ? "On" : "Off") + " Current direction: " + (turnLeft ? "Left" : "Right"));
    this.lift = false; // we can only do one motion per command so not lifting
    this.turn = onOff;

    if (!this.turnLeft && this.turn) {
      // We are turning the other direction...
//			JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
//			JCS.getControlUnit().changeDirection(this.address, this.magnet);
//			JCS.getControlUnit().changeSpeed(this.speed, this.address, this.magnet);
    } else {
//			JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
//			JCS.getControlUnit().changeSpeed(this.turn ? speed : MIN_SPEED, this.address, this.magnet);
    }

    this.turnLeft = true;

    if (this.turn) {
//			JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
//			JCS.getControlUnit().changeSpeed(this.speed, this.address, this.magnet);
    }
  }

  public void turnRight(boolean onOff) {
    Logger.debug("Turn Right: " + (onOff ? "On" : "Off") + " Current direction: " + (turnLeft ? "Left" : "Right"));
    this.lift = false; // we can only do one motion per command so not lifting

    this.turn = onOff;

    if (this.turnLeft && this.turn) {
      // We are turning the other direction...
//			JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
//			JCS.getControlUnit().changeDirection(this.address, this.magnet);
//			JCS.getControlUnit().changeSpeed(this.speed, this.address, this.magnet);
    } else {
//			JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
//			JCS.getControlUnit().changeSpeed(this.turn ? speed : MIN_SPEED, this.address, this.magnet);
    }

    this.turnLeft = false;

    if (this.turn) {
//			JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
//			JCS.getControlUnit().changeSpeed(this.speed, this.address, this.magnet);
    }
  }

  public void up(boolean onOff) {
    Logger.debug("Up: " + (onOff ? "On" : "Off") + " Current direction: " + (liftUp ? "Up" : "Down"));
    this.turn = false; // we can only do one motion per command so not turning

    this.lift = onOff;

    if (!this.liftUp && this.lift) {
      // We are lifting in the other direction...
//			JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
//			JCS.getControlUnit().changeDirection(this.address, this.magnet);
//			JCS.getControlUnit().changeSpeed(this.speed, this.address, this.magnet);
    } else {
//			JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
//			JCS.getControlUnit().changeSpeed(this.turn ? speed : MIN_SPEED, this.address, this.magnet);
    }

    this.liftUp = true;

    if (this.lift) {
//			JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
//			JCS.getControlUnit().changeSpeed(this.speed, this.address, this.magnet);
    }
  }

  public void down(boolean onOff) {
    Logger.debug("Up: " + (onOff ? "On" : "Off") + " Current direction: " + (liftUp ? "Up" : "Down"));
    this.turn = false; // we can only do one motion per command so not turning

    this.lift = onOff;

    if (this.liftUp && this.lift) {
      // We are lifting in the other direction...
//			JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
//			JCS.getControlUnit().changeDirection(this.address, this.magnet);
//			JCS.getControlUnit().changeSpeed(this.speed, this.address, this.magnet);
    } else {
//			JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
//			JCS.getControlUnit().changeSpeed(this.turn ? speed : MIN_SPEED, this.address, this.magnet);
    }

    this.liftUp = false;

    if (this.lift) {
//			JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
//			JCS.getControlUnit().changeSpeed(this.speed, this.address, this.magnet);
    }
  }

  @Override
  public void stop() {
    Logger.debug("STOP");

    this.turn = false;
    this.lift = false;

//		JCS.getControlUnit().changeSpeed(MIN_SPEED, this.address, this.magnet);
//
//		JCS.getControlUnit().setSpecialFunctions(this.address, this.turn, this.lift, false, false);
  }

  @Override
  public Crane copy() {
    return new Crane(this.address, this.name, this.description, this.catalogNumber, this.minSpeed, this.f0, this.f1, this.f2, this.f3, this.f4, this.speed, this.throttle, this.direction);
  }

}
