package jcs.ui.layout.pathfinding.astar;

/*
 * World building applet.
 * Created on Oct 30, 2004
 *
 * This demonstrates the use of drag & drop to build roads.  Background:
 * http://simblob.blogspot.com/2004/10/god-games-locomotion.html
 * 
 * The applet uses a square grid and does not constrain the road segments.
 * To be more like Locomotion, we would need to constrain the road segments,
 * allow for diagonal segments, and allow for bridges/tunnels.  Also, the
 * grid should be isometric. 
 */
import java.util.*;
import javax.swing.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

/**
 * Road building applet.
 *
 * @author amitp@cs.stanford.edu
 *
 */
public class World extends JApplet {

  JLabel label;
  JPanel panel;
  CoordinateArea coordinateArea;

  public void init() {
    try {
      CoordinateArea.gridSpace = Integer.parseInt(getParameter("GRIDSIZE"));
    } catch (NumberFormatException e) {
      // Use default size
    }

    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    getContentPane().add(panel);

    label = new JLabel("Drag and drop to build a road.");
    panel.add(label);

    coordinateArea = new CoordinateArea(this);
    coordinateArea.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(coordinateArea);
  }

  /**
   * An Edge represents a line segment of the grid.
   */
  static class Edge {

    Point point; // midpoint of segment
    String orientation; // either "horizontal" or "vertical"
  }

  /**
   * A Segment represents a road segment. It can draw itself.
   */
  static class Segment {

    /**
     * How many layers do we use in painting?
     */
    static public final int numLayers = 5;

    // TODO: make these private, cache the CubicCurve2D, and invalidate the curve
    // when begin or end is changed.
    Edge begin;
    Edge end;

    boolean valid() {
      if (begin == null || end == null) {
        return false;
      }

      float dx = Math.abs((float) (begin.point.x - end.point.x)) / CoordinateArea.gridSpace;
      float dy = Math.abs((float) (begin.point.y - end.point.y)) / CoordinateArea.gridSpace;

      if (begin.orientation == end.orientation) {
        if (begin.orientation == "horizontal" && dy == 0) {
          return false;
        }
        if (begin.orientation == "vertical" && dx == 0) {
          return false;
        }
      }
      if (dx + dy <= 4) {
        return true;
      }
      return false;
    }

    CubicCurve2D createCurve() {
      // Create the path.  This will be a cubic bezier spline, where control points
      // are chosen based on the orientation of the edges.  A vertical edge will
      // be for an east-west road, so the control point will be east or west of the
      // endpoint.  A horizontal edge will have a control point north or south of
      // the endpoint.  Since there are two endpoints and two control points, each
      // control point is determined by the orientation of its closest endpoint.
      CubicCurve2D.Double cubic = new CubicCurve2D.Double();

      Point2D.Double p1, p2, p3, p4;
      p1 = new Point2D.Double(begin.point.x, begin.point.y);
      if (begin.orientation == "vertical") {
        p2 = new Point2D.Double((begin.point.x + end.point.x) / 2, begin.point.y);
      } else {
        p2 = new Point2D.Double(begin.point.x, (begin.point.y + end.point.y) / 2);
      }
      if (end.orientation == "vertical") {
        p3 = new Point2D.Double((begin.point.x + end.point.x) / 2, end.point.y);
      } else {
        p3 = new Point2D.Double(end.point.x, (begin.point.y + end.point.y) / 2);
      }
      p4 = new Point2D.Double(end.point.x, end.point.y);

      cubic.setCurve(p1, p2, p3, p4);
      return cubic;
    }

    void paint(Graphics2D g, int layer) {
      if (begin == null || end == null) {
        return;
      }

      CubicCurve2D cubic = createCurve();

      // Now draw the road.  We draw the same shape with different styles to build
      // up the road markings.
      Color color = Color.RED;
      float width = 1.0f;

      switch (layer) {
        case 0:
          color = Color.DARK_GRAY;
          width = CoordinateArea.gridSpace * 0.8f;
          break;
        case 1:
          color = Color.WHITE;
          width = CoordinateArea.gridSpace * 0.8f - 2.0f;
          break;
        case 2:
          if (valid()) {
            color = Color.DARK_GRAY;
          } else {
            color = Color.RED;
          }
          width = CoordinateArea.gridSpace * 0.8f - 4.0f;
          break;
        case 3:
          color = Color.YELLOW;
          width = 3.0f;
          break;
        case 4:
          color = Color.DARK_GRAY;
          width = 1.0f;
          break;
      }

      g.setColor(color);
      g.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
      g.draw(cubic);
    }
  }

  /**
   * @author amitp
   *
   * The coordinate area draws a grid and responds to mouse events on the grid. TODO: use Graphics2D and AffineTransformation to turn this into an isometric square grid.
   */
  static class CoordinateArea extends JComponent implements
          MouseListener, MouseMotionListener {

    static public int gridSpace = 30;
    Edge highlighted;
    Segment newSegment;
    ArrayList segments = new ArrayList();
    World controller;

    public CoordinateArea(World controller) {
      this.controller = controller;

      addMouseListener(this);
      addMouseMotionListener(this);
      setBackground(Color.WHITE);
      setOpaque(true);
    }

    public Dimension getPreferredSize() {
      return new Dimension(201, 201);
    }

    protected void paintComponent(Graphics g1) {
      Graphics2D g = (Graphics2D) g1;
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (isOpaque()) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
      }

      // Draw the grid.
      paintGrid(g);

      // Draw the roads.  We draw all of layer 0 first, then all of layer 1,
      // etc.  This way, overlapping roads look nice.  For the road under
      // construction, we use transparency.
      Composite composite = g.getComposite();
      for (int layer = 0; layer != Segment.numLayers; layer++) {
        for (int i = 0; i != segments.size(); i++) {
          ((Segment) segments.get(i)).paint(g, layer);
        }
        if (newSegment != null) {
          g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
          newSegment.paint(g, layer);
          g.setComposite(composite);
        }
      }

      // If the mouse pointer is over an edge, highlight it
      if (highlighted != null) {
        g.setColor(Color.BLUE);

        int dx = highlighted.orientation == "horizontal" ? (gridSpace - 5) : 3;
        int dy = highlighted.orientation == "vertical" ? (gridSpace - 5) : 3;
        g.fillRect(highlighted.point.x - dx / 2, highlighted.point.y - dy / 2, dx, dy);
      }
    }

    void paintGrid(Graphics2D g) {
      g.setColor(Color.LIGHT_GRAY);

      int width = (getWidth() - 1) / gridSpace;
      int height = (getHeight() - 1) / gridSpace;

      for (int i = 0; i <= width; i++) {
        int x = i * gridSpace;
        g.drawLine(x, 0, x, height * gridSpace);
      }

      for (int i = 0; i <= height; i++) {
        int y = i * gridSpace;
        g.drawLine(0, y, width * gridSpace, y);
      }
    }

    public void mouseMoved(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();

      /* The approach we use here is to consider candidate points: centers of edges, 
             * corners of tiles, and centers of tiles.  We then choose the one that is
             * closest to the mouse point.  If it is an edge, then we select that edge.
             * If it is not an edge, then we consider the mouse pointer not close enough
             * to select something.
             * 
             * First, compute the four corners of the tile we are in.  Then use those coordinates
             * to compute the centers of the four edges. Then compute the center of the tile.
       */
      int x1 = x / gridSpace * gridSpace;
      int y1 = y / gridSpace * gridSpace;
      int x2 = x1 + gridSpace / 2;
      int y2 = y1 + gridSpace / 2;
      int x3 = x1 + gridSpace;
      int y3 = y1 + gridSpace;

      Point candidates[] = {
        // Center
        new Point(x2, y2),
        // Edges
        new Point(x1, y2), // W
        new Point(x2, y3), // S
        new Point(x3, y2), // E
        new Point(x2, y1), // N
        // Corners, pushed out a bit to decrease their weight
        new Point(x1 - gridSpace / 6, y1 - gridSpace / 6), // NW
        new Point(x1 - gridSpace / 6, y3 + gridSpace / 6), // SW
        new Point(x3 + gridSpace / 6, y1 - gridSpace / 6), // NE
        new Point(x3 + gridSpace / 6, y3 + gridSpace / 6), // SE
      };

      int closest_i = 0;
      int closest_distance = 100000;
      for (int i = 0; i != candidates.length; i++) {
        int distance = Math.abs(x - candidates[i].x) + Math.abs(y - candidates[i].y);
        if (distance < closest_distance) {
          closest_i = i;
          closest_distance = distance;
        }
      }

      if (1 <= closest_i && closest_i <= 4) {
        // It's an edge
        highlighted = new Edge();
        highlighted.point = candidates[closest_i];
        if (closest_i == 1 || closest_i == 3) {
          highlighted.orientation = "vertical";
        } else {
          highlighted.orientation = "horizontal";
        }
      } else {
        highlighted = null;
      }
      repaint();
    }

    public void mouseDragged(MouseEvent e) {
      if (newSegment == null) {
        return;
      }
      mouseMoved(e);
      newSegment.end = highlighted;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
      if (newSegment != null) {
        controller.label.setText("Road construction cancelled.");
        newSegment = null;
      }
    }

    public void mousePressed(MouseEvent e) {
      controller.label.setText("Drag the mouse pointer to draw a road:");
      mouseMoved(e);
      newSegment = new Segment();
      newSegment.begin = highlighted;
      newSegment.end = null;
    }

    public void mouseReleased(MouseEvent e) {
      if (newSegment == null) {
        return;
      }
      mouseMoved(e);
      if (highlighted == null) {
        controller.label.setText("Your road could not be built.");
      } else {
        newSegment.end = highlighted;
        if (newSegment.valid()) {
          controller.label.setText("Admire your new road.");
          segments.add(newSegment);
        } else {
          controller.label.setText("Your road could not be built.");
        }
      }
      newSegment = null;
    }

  }

}
