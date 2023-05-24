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

import jcs.ui.layout.Tile;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import jcs.entities.TileBean;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;

public class End extends AbstractTile implements Tile {

  private static int idSeq;

  public End(TileBean tileBean) {
    super(tileBean);
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
  }

  public End(Orientation orientation, Point center) {
    this(orientation, center.x, center.y);

  }

  public End(Orientation orientation, int x, int y) {
    super(orientation, x, y);
    this.type = TileType.END.getTileType();
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
  }

  @Override
  protected String getNewId() {
    idSeq++;
    return "et-" + idSeq;
  }

  @Override
  protected void setIdSeq(int id) {
    idSeq = id;
  }

  protected void renderEnd(Graphics2D g2, Color trackColor, Color backgroundColor) {
    int xx, yy, w, h;
    xx = 0;
    yy = 17;

    w = GRID;
    h = 6;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillRect(xx, yy, w, h);

    xx = GRID;
    yy = 10;

    w = 5;
    h = 20;

    g2.setPaint(Color.DARK_GRAY);
    g2.fillRect(xx, yy, w, h);

  }

  @Override
  public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
    renderEnd(g2, trackColor, backgroundColor);
  }

}
