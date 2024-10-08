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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;

public class Sensor extends Straight implements SensorEventListener {

  private boolean active;

  Sensor(TileBean tileBean) {
    super(tileBean);
  }

  Sensor(Orientation orientation, int x, int y) {
    this(orientation, new Point(x, y));
  }

  Sensor(Orientation orientation, Point center) {
    super(orientation, center);
    this.type = TileType.SENSOR.getTileType();
  }

  public boolean isActive() {
    return this.active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  private void renderSensor(Graphics2D g2) {
    int xx, yy;
    xx = RENDER_GRID - 75;
    yy = RENDER_GRID - 75;

    Point c = new Point(xx, yy);
    float radius = 300;
    float[] dist = {0.0f, 0.6f};

    if (this.active) {
      Color[] colors = {Color.red.brighter(), Color.red.darker()};
      RadialGradientPaint foreground = new RadialGradientPaint(c, radius, dist, colors, CycleMethod.REFLECT);
      g2.setPaint(foreground);
    } else {
      Color[] colors = {Color.green.darker(), Color.green.brighter()};
      RadialGradientPaint foreground = new RadialGradientPaint(c, radius, dist, colors, CycleMethod.REFLECT);
      g2.setPaint(foreground);
    }

    g2.fill(new Ellipse2D.Double(xx, yy, 0.5f * radius, 0.5f * radius));
  }

  @Override
  public void renderTile(Graphics2D g2) {
    Graphics2D g2d = (Graphics2D) g2.create();

    renderStraight(g2d);

    if (drawRoute) {
      renderTileRoute(g2);
    }

    renderSensor(g2d);

    g2d.dispose();
  }

  @Override
  public void onSensorChange(SensorEvent event) {
    SensorBean sensor = event.getSensorBean();
    if (sensor.equalsDeviceIdAndContactId(getSensorBean())) {
      this.setActive(sensor.isActive());
      repaintTile();
    }
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " {id: " + id + ", orientation: " + getOrientation() + ", direction: " + getDirection() + ", active: " + active + ", center: (" + x + "," + y + ")}";
  }
}
