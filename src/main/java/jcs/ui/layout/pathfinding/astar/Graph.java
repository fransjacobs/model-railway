package jcs.ui.layout.pathfinding.astar;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import jcs.entities.TileBean.Orientation;
import jcs.entities.enums.AccessoryValue;
import org.tinylog.Logger;

public class Graph {

  private final Map<String, Node> nodes;

  Graph() {
    nodes = new HashMap<>();
  }

  int size() {
    return this.nodes.size();
  }

  void addNode(Node node) {
    nodes.put(node.getId(), node);
  }

  Node getNode(String id) {
    return nodes.get(id);
  }

  void clear() {
    this.nodes.clear();
  }

  List<Node> getNodes() {
    return nodes.values().stream().collect(Collectors.toList());
  }

  List<Node> getBlockNodes() {
    return nodes.values().stream().filter(n -> n.isBlock()).collect(Collectors.toList());
  }

  boolean canTravelTo(Node from, Node to) {
    if (from == null || to == null) {
      return false;
    }
    if (from.getPreviousNode() != null && from.getPreviousNode().equals(to)) {
      //Skip going around...
      //Logger.trace("Skip from: " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId());
      return false;
    }

    if (from.getPreviousNode() != null && from.getTile().isJunction()) {
      AccessoryValue routeValue = from.getAccessoryStatus(from, to);
      Logger.trace("From: " + from.getPreviousNode().getId() + " via " + from.getId() + (AccessoryValue.OFF == routeValue ? " Not possible" : " Using " + routeValue) + " to " + to.getId());

      return AccessoryValue.OFF != routeValue;
    } else if (from.getPreviousNode() != null && from.isDirectional()) {
      boolean isToOnArrowSide = from.getTile().isArrowDirection(to.getTile());
      //Logger.trace("From " + from.getId() + " to: " + to.getId() + " isToOnArrowSide: " + isToOnArrowSide);
      return from.getTile().isAdjacent(to.getTile()) && isToOnArrowSide;
    } else if (from.getPreviousNode() != null && from.isCrossing()) {
      //Find the edge connection point between the previous and the from node
      Point inComingEdgePoint = from.getIncomingPoint();
      Orientation inComingSide = from.getConnectingSide(inComingEdgePoint);

      //find the connection edge point on the opposite side
      Orientation exitSide = Node.getOppositeSide(inComingSide);
      Point toInComingPoint = from.getTile().getEdgePoints().get(exitSide);

      //Check if the to has this edgepoint
      return to.getTile().getEdgePoints().containsValue(toInComingPoint);
    } else {
      return from.getTile().isAdjacent(to.getTile());
    }
  }

  double calculateHeuristic(Node from, Node to) {
    boolean canTravel = this.canTravelTo(from, to);
    double h = manhattanDistance(from, to) + (canTravel ? 0D : Double.MAX_VALUE);
    return h;
  }

  static double manhattanDistance(Node from, Node to) {
    int dx = Math.abs(to.getX() - from.getX());
    int dy = Math.abs(to.getY() - from.getY());
    return dx + dy;
  }

  static double manhattanDistance(Point from, Point to) {
    int dx = Math.abs(to.x - from.x);
    int dy = Math.abs(to.y - from.y);
    return dx + dy;
  }

  Edge link(Node from, Node to, double distance) {
    Edge edge = new Edge(from, to, distance);
    from.addEdge(edge);
    to.addEdge(edge);

    if (from.isBlock()) {
      String fromSuffix = from.getTile().getIdSuffix(to.getTile());
      edge.setFromSuffix(fromSuffix);
    }

    if (to.isBlock()) {
      String toSuffix = to.getTile().getIdSuffix(from.getTile());
      edge.setToSuffix(toSuffix);
    }
    return edge;
  }

  List<Node> findPath(Node start, String startSuffix, Node destination, String destSuffix) {
    Logger.trace("Searching for a route from: " + start.getId() + startSuffix + " to: " + destination.getId() + destSuffix);
    List<Node> path = new ArrayList<>();

    nodes.values().forEach(node -> {
      node.setPreviousNode(null);
      node.setAccessoryState(null);
      node.setG(Double.MAX_VALUE);
    });

    start.setG(0);
    start.setH(calculateHeuristic(start, destination));
    PriorityQueue<Node> activeNodes = new PriorityQueue<>();
    activeNodes.add(start);

    while (!activeNodes.isEmpty()) {
      Node current = activeNodes.poll();
      Logger.trace("Polled " + current.getId() + " from activeNodes. Size: " + activeNodes.size());

      if (current == destination) {
        current.setSuffix(destSuffix);
        Logger.trace("Target node " + destination.getId() + destSuffix + " found");
        path.clear();
        destination.retrievePath(path);
        return path;
      }

      Set<Edge> currentEdges;
      if (current.isBlock() && current.equals(start)) {
        currentEdges = current.getEdges(startSuffix);
        current.setSuffix(startSuffix);
      } else {
        currentEdges = current.getEdges();
      }

//      Logger.trace("Current Node " + current.getId() + " has " + currentEdges.size() + " edges...");
//      for (Edge edge : currentEdges) {
//        Logger.trace(current.getId() + " -> " + edge);
//      }
      for (Edge edge : currentEdges) {
        Node neighbor = edge.getOpposite(current);
        if (neighbor != null) {
          boolean noBlockOrTarget = neighbor.equals(destination) || !neighbor.isBlock();
          boolean noBlockOrTargetSide = !neighbor.isBlock() || (neighbor.equals(destination) && (destSuffix.equals(edge.getToSuffix()) || destSuffix.equals(edge.getFromSuffix())));

          boolean canTravel = canTravelTo(current, neighbor);

          double neighborG = current.getG() + edge.getDistance();
          if (neighborG < neighbor.getG() && noBlockOrTarget && noBlockOrTargetSide && canTravel) {
            neighbor.setPreviousNode(current);
            neighbor.setG(neighborG);

            neighbor.setH(calculateHeuristic(current, neighbor));

            Logger.trace(current + " -> " + neighbor);

            if (!activeNodes.contains(neighbor)) {
              activeNodes.add(neighbor);
            }
          }
        }
      }
    }
    return Collections.EMPTY_LIST;
  }
}
