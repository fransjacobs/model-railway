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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import jcs.entities.TileBean;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;
import org.tinylog.Logger;

public class StraightDirection extends Straight {

  private static int idSeq;

  public StraightDirection(TileBean tileBean) {
    super(tileBean);
  }

  public StraightDirection(Orientation orientation, int x, int y) {
    this(orientation, new Point(x, y));
  }

  public StraightDirection(Orientation orientation, Point center) {
    super(orientation, center);
    this.type = TileType.STRAIGHT_DIR.getTileType();
  }

  @Override
  protected final String getNewId() {
    idSeq++;
    return "sd-" + idSeq;
  }

  @Override
  protected void setIdSeq(int id) {
    idSeq = id;
  }

  private void renderDirectionArrow(Graphics2D g2) {

    //   |\
    // ==|+===
    //   |/
    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(Color.green.darker());

    g2.fillPolygon(new int[]{15, 15, 27}, new int[]{13, 27, 20}, 3);
  }

  @Override
  public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
    Graphics2D g2d = (Graphics2D) g2.create();

    renderStraight(g2d, trackColor, backgroundColor);
    renderDirectionArrow(g2d);
    g2d.dispose();
  }

}
