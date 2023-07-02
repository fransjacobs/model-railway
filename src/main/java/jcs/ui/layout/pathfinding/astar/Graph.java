package jcs.ui.layout.pathfinding.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import jcs.entities.enums.AccessoryValue;
import org.tinylog.Logger;

public class Graph<T> {

  private final Map<String, Node> nodes = new HashMap<>();

  private final Heuristic heuristic;

  public Graph(Heuristic heuristic) {
    this.heuristic = heuristic;
  }

  public int size() {
    return this.nodes.size();
  }

  public void addNode(Node node) {
    nodes.put(node.getId(), node);
  }

  public Node getNode(String id) {
    return nodes.get(id);
  }

  public boolean contains(String id) {
    return nodes.containsKey(id);
  }

  public List<Node> getNodes() {
    return nodes.values().stream().collect(Collectors.toList());
  }

  public List<Node> getBlockNodes() {
    return nodes.values().stream().filter(n -> n.isBlock()).collect(Collectors.toList());
  }

  public Heuristic getHeuristic() {
    return heuristic;
  }

  public Edge link(Node from, Node to, double distance) {
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

    if (from.isJunction()) {
      AccessoryValue fromPathStatus = from.getTile().getSwitchValueTo(to.getTile());
      edge.setAccessoryState(fromPathStatus);
    }

    if (to.isJunction()) {
      AccessoryValue toPathStatus = to.getTile().getSwitchValueTo(from.getTile());
      edge.setAccessoryState(toPathStatus);
    }

    return edge;
  }

  AccessoryValue getAccessoryStatus(Node from, Node to) {
    if (from == null || to == null) {
      return AccessoryValue.OFF;
    }
    if (from.getPreviousNode() != null && from.getTile().isJunction()) {
      boolean isParentOnSwitchSide = from.getTile().isSwitchSide(from.getPreviousNode().getTile());
      boolean isParentOnStraightSide = from.getTile().isStraightSide(from.getPreviousNode().getTile());
      boolean isParentOnDivergingSide = from.getTile().isDivergingSide(from.getPreviousNode().getTile());

      boolean isToOnSwitchSide = from.getTile().isSwitchSide(to.getTile());
      boolean isToOnStraightSide = from.getTile().isStraightSide(to.getTile());
      boolean isToOnDivergingSide = from.getTile().isDivergingSide(to.getTile());

      if (isParentOnSwitchSide && (isToOnDivergingSide || isToOnStraightSide)) {
        return (isToOnDivergingSide ? AccessoryValue.RED : AccessoryValue.GREEN);
      } else if (isParentOnStraightSide && isToOnSwitchSide) {
        return AccessoryValue.GREEN;
      } else if (isParentOnDivergingSide && isToOnSwitchSide) {
        return AccessoryValue.RED;
      } else {
        Logger.trace("Path from " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId() + " is NOT possible");
        return AccessoryValue.OFF;
      }
    } else {
      return AccessoryValue.OFF;
    }
  }

//  boolean canTravelTo(Node from, Node to) {
//    if (from == null || to == null) {
//      return false;
//    }
//
//    if (from.getPreviousNode() != null && from.getTile().isJunction()) {
//      //Check is the full path is possible
//      Logger.trace("Checking path from: " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId());
//
//      boolean isParentOnSwitchSide = from.getTile().isSwitchSide(from.getPreviousNode().getTile());
//      boolean isParentOnStraightSide = from.getTile().isStraightSide(from.getPreviousNode().getTile());
//      boolean isParentOnDivergingSide = from.getTile().isDivergingSide(from.getPreviousNode().getTile());
//
//      Logger.trace("From " + from.getPreviousNode().getId() + " switchSide: " + isParentOnSwitchSide + " straightSide: " + isParentOnStraightSide + " divergingSide: " + isParentOnDivergingSide);
//
//      boolean isToOnSwitchSide = from.getTile().isSwitchSide(to.getTile());
//      boolean isToOnStraightSide = from.getTile().isStraightSide(to.getTile());
//      boolean isToOnDivergingSide = from.getTile().isDivergingSide(to.getTile());
//
//      Logger.trace("To " + to.getId() + " switchSide: " + isToOnSwitchSide + " straightSide: " + isToOnStraightSide + " divergingSide: " + isToOnDivergingSide);
//
//      if (isParentOnSwitchSide && (isToOnDivergingSide || isToOnStraightSide)) {
//        Logger.trace("Path from " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId() + " is possible using " + (isToOnDivergingSide ? AccessoryValue.RED : AccessoryValue.GREEN));
//        return from.getTile().isAdjacent(to.getTile());
//      } else if (isParentOnStraightSide && isToOnSwitchSide) {
//        Logger.trace("Path from " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId() + " is possible using " + AccessoryValue.GREEN);
//        return from.getTile().isAdjacent(to.getTile());
//      } else if (isParentOnDivergingSide && isToOnSwitchSide) {
//        Logger.trace("Path from " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId() + " is possible using " + AccessoryValue.RED);
//        return from.getTile().isAdjacent(to.getTile());
//      } else {
//        Logger.trace("Path from " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId() + " is NOT possible");
//        return false;
//      }
//    } else {
//      return from.getTile().isAdjacent(to.getTile());
//    }
//  }
  public List<Node> findPath(Node start, String startSuffix, Node destination, String destSuffix) {
    Logger.trace("Searching for a route from: " + start.getId() + startSuffix + " to: " + destination.getId() + destSuffix);
    List<Node> path = new ArrayList<>();

    nodes.values().forEach(node -> {
      node.setPreviousNode(null);
      node.setG(Double.MAX_VALUE);
    });

    start.setG(0);
    start.setH(heuristic.calculate(start, destination));
    Logger.trace("Start H: " + start.getH());

    PriorityQueue<Node> activeNodes = new PriorityQueue<>();
    activeNodes.add(start);

    while (!activeNodes.isEmpty()) {
      Node currentNode = activeNodes.poll();
      //currentNode.setState(Node.State.CLOSED);

      Logger.trace("Polled " + currentNode.getId() + " from activeNodes. Size: " + activeNodes.size());

      if (currentNode == destination) {
        currentNode.setSuffix(destSuffix);
        Logger.trace("Target node " + destination.getId() + destSuffix + " found");
        path.clear();
        destination.retrievePath(path);
        return path;
      }

      Set<Edge> currentEdges;
      if (currentNode.isBlock() && currentNode.equals(start)) {
        currentEdges = currentNode.getEdges(startSuffix);
        currentNode.setSuffix(startSuffix);
      } else {
        currentEdges = currentNode.getEdges();
      }

      if (currentNode.isJunction()) {
        Logger.trace(currentNode.getId() + "[Junction]");

      }

      if (currentEdges.size() > 6) {
        Logger.trace("CurrentNode " + currentNode.getId() + " has " + currentEdges.size() + " edges:");
        for (Edge edge : currentEdges) {
          Logger.trace(edge);
        }
      }

      for (Edge edge : currentEdges) {
        Node neighborNode = edge.getOppositeNode(currentNode);

        if (neighborNode == null) {
          Logger.error(edge + " Has opposite Null node!");
          return null;
        }

        boolean noBlockOrTarget = neighborNode.equals(destination) || !neighborNode.isBlock();
        boolean noBlockOrTargetSide = !neighborNode.isBlock() || (neighborNode.equals(destination) && (destSuffix.equals(edge.getToSuffix()) || destSuffix.equals(edge.getFromSuffix())));

        double neighborG = currentNode.getG() + edge.getDistance();

        //if (neighborG < neighborNode.getG() && canTravelTo(currentNode, neighborNode) && noBlockOrTarget && noBlockOrTargetSide) {
        if (neighborG < neighborNode.getG() && noBlockOrTarget && noBlockOrTargetSide) {
          neighborNode.setPreviousNode(currentNode);
          if (currentNode.isJunction()) {
            currentNode.setAccessoryState(this.getAccessoryStatus(currentNode, neighborNode));
            Logger.trace("C " + currentNode.getId() + " - [" + currentNode.getAccessoryState() + "] -> " + neighborNode.getId());
          }

          if (neighborNode.isJunction()) {
            currentNode.setAccessoryState(this.getAccessoryStatus(neighborNode, currentNode));
            Logger.trace("N " + currentNode.getId() + " - [" + currentNode.getAccessoryState() + "] -> " + neighborNode.getId());
          }

          neighborNode.setG(neighborG);

          double h = heuristic.calculate(currentNode, neighborNode);
          neighborNode.setH(h);

          Logger.trace("use: " + currentNode.getId() + (currentNode.isJunction() ? "-[" + currentNode.getAccessoryState() + "]" : "") + "->" + neighborNode.getId() + " Neighbor G: " + neighborNode.getG() + " H: " + neighborNode.getH());

          if (!activeNodes.contains(neighborNode)) {

            activeNodes.add(neighborNode);
            //neighborNode.setState(Node.State.OPEN);

            Logger.trace("Added neighbor " + neighborNode.getId() + " to activeNodes. Size: " + activeNodes.size());
          }
        }
      }
    }
    return Collections.EMPTY_LIST;
  }

}
