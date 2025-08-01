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
package jcs.ui.layout.tiles;

import java.awt.Point;
import javax.swing.UIManager;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import jcs.entities.TileBean.TileType;
import jcs.ui.layout.tiles.ui.SensorUI;
import jcs.ui.layout.tiles.ui.TileUI;

/**
 * Representation of a Sensor in a track on the layout
 */
public class Sensor extends Straight implements SensorEventListener {

  public Sensor(TileBean tileBean) {
    super(tileBean);
  }

  public Sensor(Orientation orientation, Point center) {
    this(orientation, center.x, center.y);
  }

  public Sensor(Orientation orientation, int x, int y) {
    this(orientation, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  public Sensor(Orientation orientation, int x, int y, int width, int height) {
    super(orientation, x, y, width, height);
    this.tileType = TileType.SENSOR;
  }

  @Override
  public String getUIClassID() {
    return SensorUI.UI_CLASS_ID;
  }

  @Override
  public void updateUI() {
    UIManager.put(TileUI.UI_CLASS_ID, "jcs.ui.layout.tiles.ui.SensorUI");
    setUI((TileUI) UIManager.getUI(this));
    invalidate();
  }

  @Override
  public void onSensorChange(SensorEvent event) {
    SensorBean sensor = event.getSensorBean();
    //TODO!
    if (sensor.equalsDeviceIdAndContactId(getSensorBean())) {
      setActive(sensor.isActive());
    }
  }

  //Sensor must also listen to the mouse now it is a component....
  //in UI Delegate TODO!
  @Override
  public String toString() {
    return getClass().getSimpleName() + " {id: " + id + ", orientation: " + getOrientation() + ", direction: " + getDirection() + ", active: " + model.isSensorActive() + ", center: (" + tileX + "," + tileY + ")}";
  }
}
