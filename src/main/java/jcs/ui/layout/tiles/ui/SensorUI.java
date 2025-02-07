/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.ui.layout.tiles.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileModel;

public class SensorUI extends StraightUI {

  public SensorUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new SensorUI();
  }

  private void renderSensor(Graphics2D g2, JComponent c) {
    Tile tile = (Tile) c;
    TileModel model = tile.getModel();

    int xx = RENDER_GRID - 75;
    int yy = RENDER_GRID - 75;

    Point cp = new Point(xx, yy);
    float radius = 300;
    float[] dist = {0.0f, 0.6f};

    if (model.isSensorActive()) {
      Color[] colors = {Color.red.brighter(), Color.red.darker()};
      RadialGradientPaint foreground = new RadialGradientPaint(cp, radius, dist, colors, MultipleGradientPaint.CycleMethod.REFLECT);
      g2.setPaint(foreground);
    } else {
      Color[] colors = {Color.green.darker(), Color.green.brighter()};
      RadialGradientPaint foreground = new RadialGradientPaint(cp, radius, dist, colors, MultipleGradientPaint.CycleMethod.REFLECT);
      g2.setPaint(foreground);
    }

    g2.fill(new Ellipse2D.Double(xx, yy, 0.5f * radius, 0.5f * radius));
  }


  @Override
  public void renderTile(Graphics2D g2, JComponent c) {
    renderStraight(g2, c);
    renderSensor(g2, c);
  }

}
