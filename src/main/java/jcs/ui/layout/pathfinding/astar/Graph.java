package jcs.ui.layout.pathfinding.astar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

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

  public void link(Node from, Node to, double cost) {
    Edge edge = new Edge(from, to, cost);
    from.addEdge(edge);
    to.addEdge(edge);
  }

  public void findPath(Node start, Node target, List<Node> path) {
    nodes.values().forEach(node -> {
      node.setState(Node.State.UNVISITED);
      node.setPreviousNode(null);
      node.setCost(Double.MAX_VALUE);
    });

    start.setCost(0);
    start.setH(heuristic.calculate(start, target, start));

    PriorityQueue<Node> activeNodes = new PriorityQueue<>();
    activeNodes.add(start);

    while (!activeNodes.isEmpty()) {
      Node currentNode = activeNodes.poll();
      currentNode.setState(Node.State.CLOSED);

      // target node found !
      if (currentNode == target) {
        path.clear();
        target.retrievePath(path);
        return;
      }

      currentNode.getEdges().forEach((edge) -> {
        Node neighborNode = edge.getOppositeNode(currentNode);
        double neighborCost = currentNode.getCost() + edge.getCost();
        //can traverse...
        if (neighborCost < neighborNode.getCost()) {

          neighborNode.setPreviousNode(currentNode);
          neighborNode.setCost(neighborNode.getCost());
          double h = heuristic.calculate(currentNode.getPreviousNode(), currentNode, neighborNode);
          neighborNode.setH(h);
          if (!activeNodes.contains(neighborNode)) {
            activeNodes.add(neighborNode);
            neighborNode.setState(Node.State.OPEN);
          }
        }
      });
    }
  }

}
