/*
 * Copyright 2023 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.ui.layout.tiles;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import javax.swing.UIManager;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.entities.TileBean.TileType;
import jcs.ui.layout.tiles.ui.CurvedUI;
import jcs.ui.layout.tiles.ui.TileUI;

/**
 * Representation of a Curved track on the layout
 */
public class Curved extends Tile {

  public Curved(TileBean tileBean) {
    super(tileBean, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    setModel(new DefaultTileModel(tileBean.getOrientation()));
    initUI();
  }

  public Curved(Orientation orientation, Point center) {
    this(orientation, center.x, center.y);
  }

  public Curved(Orientation orientation, int x, int y) {
    this(orientation, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  public Curved(Orientation orientation, int x, int y, int width, int height) {
    super(TileType.CURVED, orientation, x, y, width, height);
    setModel(new DefaultTileModel(orientation));
    initUI();
  }

  private void initUI() {
    updateUI();
  }

  @Override
  public String getUIClassID() {
    return CurvedUI.UI_CLASS_ID;
  }

  @Override
  public void updateUI() {
    UIManager.put(TileUI.UI_CLASS_ID, "jcs.ui.layout.tiles.ui.CurvedUI");
    setUI((TileUI) UIManager.getUI(this));
    invalidate();
  }

  @Override
  public Map<Orientation, Point> getNeighborPoints() {
    Map<Orientation, Point> neighbors = new HashMap<>();
    Orientation orientation = this.getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (orientation) {
      case SOUTH -> {
        neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
      }
      case WEST -> {
        neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
      }
      case NORTH -> {
        neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
      }
      default -> {
        //EAST
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
        neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
      }
    }
    return neighbors;
  }

  @Override
  public Map<Orientation, Point> getEdgePoints() {
    Map<Orientation, Point> edgeConnections = new HashMap<>();

    Orientation orientation = this.getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (orientation) {
      case SOUTH -> {
        //       |  |
        // b  \  |\ |
        edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
      }
      case WEST -> {
        //       |/ |
        // t /   |  |
        edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
      }
      case NORTH -> {
        //  t \
        edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
        edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
      }
      default -> {
        //EAST b /
        edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
      }
    }
    return edgeConnections;
  }

}
