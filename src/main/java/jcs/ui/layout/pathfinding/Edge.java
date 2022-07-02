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
package jcs.ui.layout.pathfinding;

/**
 *
 * @author fransjacobs
 */
public class Edge {

    private final String sourceId;
    private final String targetId;
    private final double cost;

    public Edge(String sourceId, String targetId, double cost) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.cost = cost;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return "Edge (" + sourceId + " -> " + targetId + ") distance: " + cost;
    }

}
