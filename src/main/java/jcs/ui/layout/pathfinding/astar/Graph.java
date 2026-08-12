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
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.TileBean.Orientation;
import jcs.ui.layout.tiles.Tile;
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
  
  Node getNode(Node from, Tile to, Point p) {
    if (to.isCrossing()) {
      //find the h or v only node based on the from
      
      Point mp = from.getTile().getSharingPoint(to);
      boolean ve = to.getEdgeConnections(true).containsValue(mp);
      boolean he = to.getEdgeConnections(false).containsValue(mp);
      
      if (ve) {
        Node v = nodes.get(to.getId() + "-v");
        return v;        
      }
      if (he) {
        Node h = nodes.get(to.getId() + "-h");
        return h;        
      }
      
      Logger.warn("No Neighbor for From: {} To: {} and Point ({},{})", from.getId(), to.getId(), p.x, p.y);
      
      return nodes.get(to.getId());      
      
    } else {
      return nodes.get(to.getId());      
    }
  }
  
  List<Node> getNodeList(Tile t) {
    List<Node> nl = new ArrayList<>();
    if (t.isCrossing()) {
      String id = t.getId();
      nl.add(nodes.get(id + "-v"));
      nl.add(nodes.get(id + "-h"));
    } else {
      nl.add(nodes.get(t.getId()));
    }
    return nl;
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
      //Logger.trace("Skipping as is this is going back to previous Node (From: {} via {} to {}).", from.getPreviousNode().getId(), from.getId(), to.getId());
      return false;
    }
    
    if (from.getPreviousNode() != null && from.getTile().isJunction()) {
      AccessoryValue routeValue = from.getAccessoryStatus(from, to);
      Logger.trace("From: {} via {} {} to {}", from.getPreviousNode().getId(), from.getId(), (AccessoryValue.OFF == routeValue ? " Not possible" : " Using " + routeValue), to.getId());
      
      return AccessoryValue.OFF != routeValue;
    } else if (from.getPreviousNode() != null && from.isDirectional()) {
      boolean isToOnArrowSide = from.getTile().isArrowDirection(to.getTile());
      //Logger.trace("From " + from.getId() + " to: " + to.getId() + " isToOnArrowSide: " + isToOnArrowSide);
      return from.getTile().isAdjacent(to.getTile()) && isToOnArrowSide;
    } else if (from.getPreviousNode() != null && from.isCrossing()) {
      Logger.trace("From {} isCrossing {}", from.getId(), from.isCrossing());
      //Find the edge connection point between the previous and the from node
      Point inComingEdgePoint = from.getIncomingPoint();
      
      Orientation inComingSide = from.getConnectingSide(inComingEdgePoint);
      //find the connection edge point on the opposite side
      Orientation exitSide = Node.getOppositeSide(inComingSide);
      
      Point toInComingPoint = from.getTile().getEdgePoints().get(exitSide);
      Logger.trace("From {} inComing point: ({},{}) incoming side: {} exit side: {} toInComingPoint: ({},{}) ", from.getId(), inComingEdgePoint.x, inComingEdgePoint.y, inComingSide.getOrientation(), exitSide.getOrientation(), toInComingPoint.x, toInComingPoint.y);
      
      Map<Orientation, Point> toEgdePoints = to.getTile().getEdgePoints();
      boolean cont = toEgdePoints.containsValue(toInComingPoint);
      
      Logger.trace("To {} edge points (size: {}) contain point: ({},{}): {}) ", to.getId(), toEgdePoints.size(), toInComingPoint.x, toInComingPoint.y, cont);

      //Check if the to has this edgepoint
      return to.getTile().getEdgePoints().containsValue(toInComingPoint);
    } else if (from.getPreviousNode() != null && from.isCross()) {
      //A cross can only connect to diagonal opposite sides
      boolean diagonal = from.isDiagonalOpposite(from, to);
      Logger.trace("#Else if pref from {} to {} diagonal: {}", from.getTile().getId(), to.getTile().getId(), diagonal);
      
      return diagonal;
    } else {
      return from.getTile().isAdjacent(to.getTile());
    }
  }
  
  double calculateHeuristic(Node from, Node to) {
    boolean canTravel = canTravelTo(from, to);
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
  
  static double shortestDistance(Node from, Node to) {
    int dx = Math.abs(to.getX() - from.getX());
    int dy = Math.abs(to.getY() - from.getY());
    return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
  }
  
  static double shortestDistance(Point from, Point to) {
    double dx = to.x - from.x;
    double dy = to.y - from.y;
    
    return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
  }
  
  Edge link(Node from, Node to, double distance) {
    //extra check needed to see is the tracks really connect...
    Edge edge = new Edge(from, to, distance);
    
    Logger.trace("!" + edge);
    
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
    Logger.trace("Searching for a route from: {}{} to: {}{}", start.getId(), startSuffix, destination.getId(), destSuffix);
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
      Logger.trace("Polled {} from activeNodes. Size: {}", current.getId(), activeNodes.size());
      
      if (current == destination) {
        current.setSuffix(destSuffix);
        Logger.trace("Target node {}{} found", destination.getId(), destSuffix);
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
      
      Logger.trace("Current Node {} has {} edges...", current.getId(), currentEdges.size());
      for (Edge edge : currentEdges) {
        Logger.trace("{} -> {}", current.getId(), edge);
      }
      
      for (Edge edge : currentEdges) {
        Node neighbor = edge.getOpposite(current);
        
        if (neighbor != null) {
          Logger.trace("Check {} -> {} ", current.getTile().getId(), neighbor.getId());
          
          boolean noBlockOrTarget = neighbor.equals(destination) || !neighbor.isBlock();
          boolean noBlockOrTargetSide = !neighbor.isBlock() || (neighbor.equals(destination) && (destSuffix.equals(edge.getToSuffix()) || destSuffix.equals(edge.getFromSuffix())));
          
          boolean canGo = canTravelTo(current, neighbor);
          if (!canGo) {
            Logger.trace("##Can't travel from {} to {}", current.getTile().getId(), neighbor.getId());
          }
          
          boolean canTravel = canTravelTo(current, neighbor);
          
          Logger.trace("Can{} travel from {} to {}", (canTravel ? "" : "'t"), current.getTile().getId(), neighbor.getId());
          
          double neighborG = current.getG() + edge.getDistance();
          if (neighborG < neighbor.getG() && noBlockOrTarget && noBlockOrTargetSide && canTravel) {
            neighbor.setPreviousNode(current);
            neighbor.setG(neighborG);
            
            neighbor.setH(calculateHeuristic(current, neighbor));
            
            Logger.trace(current + " -> " + neighbor);
            
            if (!activeNodes.contains(neighbor)) {
              activeNodes.add(neighbor);
            }
          } else {
            Logger.trace("## New neighborG {} Distance {} currentG {} canTravel {}", neighborG, edge.getDistance(), current.getG(), canTravel);
          }
        } else {
          Logger.trace("Edge has no neigbors. From {} To: {}", edge.getFrom().getId(), edge.getTo().getId());
        }
      }
    }
    return Collections.EMPTY_LIST;
  }
}
