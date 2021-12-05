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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;
import org.tinylog.Logger;

/**
 *
 * @param <T>
 */
public class RouteFinder<T extends GraphNode> {

    private final Graph<T> graph;
    private final Scorer<T> nextNodeScorer;
    private final Scorer<T> targetScorer;

    public RouteFinder(Graph<T> graph, Scorer<T> nextNodeScorer, Scorer<T> targetScorer) {
        this.graph = graph;
        this.nextNodeScorer = nextNodeScorer;
        this.targetScorer = targetScorer;
    }

    public List<T> findRoute(T from, T to) {
        Map<T, RouteNode<T>> allNodes = new HashMap<>();
        Queue<RouteNode> openSet = new PriorityQueue<>();

        RouteNode<T> start = new RouteNode<>(from, null, 0d, targetScorer.computeCost(from, to));

        allNodes.put(from, start);
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Logger.trace("Open Set contains: " + openSet.stream().map(RouteNode::getCurrent).collect(Collectors.toSet()));

            RouteNode<T> next = openSet.poll();
            Logger.trace("Looking at node: " + next);

            if (next.getCurrent().equals(to)) {
                Logger.trace("Found our destination!");

                List<T> route = new ArrayList<>();
                RouteNode<T> current = next;
                do {
                    route.add(0, current.getCurrent());
                    current = allNodes.get(current.getPrevious());
                } while (current != null);

                Logger.trace("Route: " + route);
                return route;
            }

            graph.getConnections(next.getCurrent()).forEach(connection -> {
                double newScore = next.getRouteScore() + nextNodeScorer.computeCost(next.getCurrent(), connection);
                RouteNode<T> nextNode = allNodes.getOrDefault(connection, new RouteNode<>(connection));
                allNodes.put(connection, nextNode);

                if (nextNode.getRouteScore() > newScore) {
                    nextNode.setPrevious(next.getCurrent());
                    nextNode.setRouteScore(newScore);
                    nextNode.setEstimatedScore(newScore + targetScorer.computeCost(connection, to));
                    openSet.add(nextNode);
                    Logger.trace("Found a better route to node: " + nextNode);
                }
            });
        }

        throw new IllegalStateException("Can't find a route from " + from.getId() + " to " + to.getId() + "!");
    }

}
