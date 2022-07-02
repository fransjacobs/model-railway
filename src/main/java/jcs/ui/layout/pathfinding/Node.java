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

import java.util.LinkedList;
import java.util.List;
import jcs.entities.enums.TileType;
import jcs.ui.layout.Tile;

/**
 *
 * @author fransjacobs
 */
public class Node {

    private String id;

    private final List<Edge> edges;

    private Node parent;

    private boolean junction;

    private Tile tile;

    public Node(Tile tile) {
        this(tile.getId(), tile);
    }

    public Node(String id) {
        this(id, null);
    }

    public Node(String id, Tile tile) {
        this.id = id;
        this.tile = tile;

        if (tile != null) {
            this.junction = TileType.SWITCH.equals(tile.getTileType());
        } else {
            this.junction = false;
        }
        this.edges = new LinkedList<>();
    }

    public String getId() {
        return id;
    }

    public void addEdge(Edge edge) {
        this.edges.add(edge);
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public boolean isJunction() {
        return junction;
    }

    public boolean contains(Edge edge) {
        List<Edge> snaphot = new LinkedList<>(this.edges);

        for (Edge e : snaphot) {
            if (e.getSourceId().equals(edge.getSourceId()) && e.getTargetId().equals(edge.getTargetId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if travel is possible toNode this node toNode the 'toNode' Node
     *
     * @param toNode the travel destination node
     * @return true when travel is possible
     */
    public boolean canTravel(Node toNode) {
        if (this.isJunction() && this.getParent() != null && this.getParent().isJunction() && toNode.isJunction()) {
            //Logger.trace("Junctions: " + this.getParent().getId() + " -> " + this.getId() + " -> " + toNode.getId());

            String parentId = this.getParent().getId();
            String tgtId = toNode.getId();

            //a path from xx-G via xx to xx-R or vv is not possible
            if (parentId.replace("-G", "").replace("-R", "").equals(tgtId.replace("-G", "").replace("-R", ""))) {
                if (parentId.equals(tgtId)) {
                    //Logger.trace("Can Travel from: " + parentId + this.getId() + " -> " + tgtId);
                    return true;
                } else {
                    //Logger.trace("Can't travel from: " + parentId + " -> " + this.getId() + " -> " + tgtId);
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public Tile getTile() {
        return tile;
    }

    @Override
    public String toString() {
        return "Node{" + "id=" + id + '}';
    }

}
