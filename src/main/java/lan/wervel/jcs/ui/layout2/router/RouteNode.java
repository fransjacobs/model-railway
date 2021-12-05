/*
 * Copyright (C) 2020 frans.
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
package lan.wervel.jcs.ui.layout2.router;

import java.util.StringJoiner;

/**
 *
 */
class RouteNode<T extends GraphNode> implements Comparable<RouteNode> {

    private final T current;
    private T previous;
    private double routeScore;
    private double estimatedScore;

    RouteNode(T current) {
        this(current, null, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    RouteNode(T current, T previous, double routeScore, double estimatedScore) {
        this.current = current;
        this.previous = previous;
        this.routeScore = routeScore;
        this.estimatedScore = estimatedScore;
    }

    T getCurrent() {
        return current;
    }

    T getPrevious() {
        return previous;
    }

    double getRouteScore() {
        return routeScore;
    }

    double getEstimatedScore() {
        return estimatedScore;
    }

    void setPrevious(T previous) {
        this.previous = previous;
    }

    void setRouteScore(double routeScore) {
        this.routeScore = routeScore;
    }

    void setEstimatedScore(double estimatedScore) {
        this.estimatedScore = estimatedScore;
    }

    @Override
    public int compareTo(RouteNode other) {
        if (this.estimatedScore > other.estimatedScore) {
            return 1;
        } else if (this.estimatedScore < other.estimatedScore) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RouteNode.class.getSimpleName() + "[", "]").add("current=" + current)
                .add("previous=" + previous).add("routeScore=" + routeScore).add("estimatedScore=" + estimatedScore)
                .toString();
    }
}
