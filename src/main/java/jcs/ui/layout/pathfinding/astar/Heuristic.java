package jcs.ui.layout.pathfinding.astar;

public interface Heuristic {

  public double calculate(Node current, Node to);

}
