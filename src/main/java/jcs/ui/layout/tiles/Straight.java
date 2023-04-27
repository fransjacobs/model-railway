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
import jcs.ui.layout.LayoutUtil;

public class Straight extends AbstractTile implements Tile {

    private static int idSeq;

    public Straight(TileBean tileBean) {
        super(tileBean);
        this.width = LayoutUtil.DEFAULT_WIDTH;
        this.height = LayoutUtil.DEFAULT_HEIGHT;
    }

    public Straight(Orientation orientation, int x, int y) {
        this(orientation, new Point(x, y));
    }

    public Straight(Orientation orientation, Point center) {
        super(orientation, center);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    @Override
    protected String getNewId() {
        idSeq++;
        return "st-" + idSeq;
    }

    protected void renderStraight(Graphics2D g2, Color trackColor, Color backgroundColor) {
        int x, y, w, h;
        x = 0;
        y = 17;
        w = DEFAULT_WIDTH;
        h = 6;

        g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
        g2.setPaint(trackColor);
        g2.fillRect(x, y, w, h);
    }

    @Override
    public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
        renderStraight(g2, trackColor, backgroundColor);
    }

    @Override
    protected void setIdSeq(int id) {
        idSeq = id;
    }
}
