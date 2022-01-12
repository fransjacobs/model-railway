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

import java.util.ArrayList;
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

    public Node(Tile tile) {
        this(tile.getId(), TileType.SWITCH.equals(tile.getTileType()));
    }

    public Node(String id) {
        this(id, false);
    }

    public Node(String id, boolean junction) {
        this.id = id;
        this.junction = junction;
        edges = new ArrayList<>();
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

    /**
     * Check if traversal is possible to this node from the 'from' Node
     *
     * @param from the traversal start node
     * @return true when traversal is possible
     */
    public boolean canTraverse(Node from) {
        if (from.getParent() != null && from.getParent().isJunction() && isJunction()) {
            String src = from.getParent().getId();
            String srcValue = src.substring(src.length() - 1);
            String tgtValue = id.substring(id.length() - 1);

            return srcValue.equals(tgtValue);

        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        return "Node{" + "id=" + id + '}';
    }

}
