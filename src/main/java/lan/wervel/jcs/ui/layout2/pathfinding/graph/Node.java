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
package lan.wervel.jcs.ui.layout2.pathfinding.graph;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import lan.wervel.jcs.ui.layout2.Tile;

/**
 *
 * @author fransjacobs
 */
public class Node implements Comparable<Node> {

    private Tile tile;
    private String id;
    private Point p;

    private Node parent = null;

    private Set<Edge> neighbors;

    // Evaluation functions
    public double f = Double.MAX_VALUE;
    public double g = Double.MAX_VALUE;
    // Hardcoded heuristic
    public double h;

    public Node(Tile tile) {
        this(tile, tile.getId());
    }

    public Node(Tile tile, String id) {
        this(tile, id, tile.getCenter());
    }

    public Node(Tile tile, String id, Point p) {
        this.tile = tile;
        this.id = id;
        this.p = p;
        this.neighbors = new HashSet<>();
    }

    @Override
    public int compareTo(Node n) {
        return Double.compare(this.f, n.f);
    }

    public double calculateHeuristic(Node target) {
        return this.h;
    }

    public String getId() {
        return this.id;
    }

    public Point getP() {
        return p;
    }

    public Tile getTile() {
        return tile;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Set<Edge> getNeighbors() {
        return neighbors;
    }

    public void addBranch(double weight, Node node) {
        Edge newEdge = new Edge(weight, node);
        neighbors.add(newEdge);
    }

    public static class Edge {

        private final double weight;
        private final Node node;

        Edge(double weight, Node node) {
            this.weight = weight;
            this.node = node;
        }

        public double getWeight() {
            return weight;
        }

        public Node getNode() {
            return node;
        }

    }

}
