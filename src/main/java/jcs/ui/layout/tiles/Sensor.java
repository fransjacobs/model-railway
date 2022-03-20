/*
 * Copyright (C) 2021 fransjacobs.
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
package jcs.ui.layout.tiles;

import jcs.ui.layout.Tile;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;
import jcs.entities.enums.Orientation;
import jcs.trackservice.events.SensorListener;

/**
 *
 * @author fransjacobs
 */
public class Sensor extends Straight implements Tile, SensorListener {

    private static int idSeq;
    private boolean active;

    public Sensor(TileBean tileBean) {
        super(tileBean);
    }

    public Sensor(Orientation orientation, int x, int y) {
        this(orientation, new Point(x, y));
    }

    public Sensor(Orientation orientation, Point center) {
        super(orientation, center);
    }

    @Override
    protected final String getNewId() {
        idSeq++;
        return "se-" + idSeq;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.image = null;
    }

    private void renderSensor(Graphics2D g2) {
        int x, y; //, w, h;
        x = 13;
        y = 13;

        Point c = new Point(x, y);
        float radius = 30;
        float[] dist = {0.0f, 0.6f};

        if (this.active) {
            Color[] colors = {Color.red.brighter(), Color.white};
            RadialGradientPaint foreground = new RadialGradientPaint(c, radius, dist, colors, CycleMethod.NO_CYCLE);
            g2.setPaint(foreground);
        } else {
            Color[] colors = {Color.green.darker(), Color.white};
            RadialGradientPaint foreground = new RadialGradientPaint(c, radius, dist, colors, CycleMethod.NO_CYCLE);
            g2.setPaint(foreground);
        }

        g2.fill(new Ellipse2D.Double(x, y, 0.5f * radius, 0.5f * radius));

    }

    @Override
    public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
        Graphics2D g2d = (Graphics2D) g2.create();

        renderStraight(g2d, trackColor, backgroundColor);
        renderSensor(g2d);

        g2d.dispose();
    }

    @Override
    protected void setIdSeq(int id) {
        idSeq = id;
    }

    public SensorBean getSensorBean() {
        if (this.tileBean != null && this.tileBean.getEntityBean() != null) {
            return (SensorBean) this.tileBean.getEntityBean();
        } else {
            return null;
        }
    }

    public void setSensorBean(SensorBean sensorBean) {
        if (this.tileBean == null) {
            this.tileBean = this.getTileBean();
        }
        this.tileBean.setEntityBean(sensorBean);
    }

    @Override
    public void onChange(SensorBean sensor) {
        if (sensor.equalsDeviceIdAndContactId(getSensorBean())) {
            this.setActive(sensor.isActive());
            repaintTile();
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {id: " + id + ", orientation: " + orientation + ", direction: " + direction + ", active: " + active + ", center: " + center + "}";
    }

}
