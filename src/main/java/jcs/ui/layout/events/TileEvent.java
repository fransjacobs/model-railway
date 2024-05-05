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
package jcs.ui.layout.events;

import java.awt.Color;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.BlockBean;
import jcs.entities.BlockBean.BlockState;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import jcs.ui.layout.tiles.Tile;

/**
 *
 * @author Frans Jacobs
 */
public class TileEvent {

  private final String tileId;

  private final Color backgroundColor;
  private final Color trackColor;
  private final Color trackRouteColor;

  private final Orientation incomingSide;
  private final boolean showRoute;

  private AccessoryBean.AccessoryValue routeState;
  private BlockBean blockBean;
  private TileBean tileBean;

  private BlockState blockState;

  public TileEvent(String tileId, boolean showRoute) {
    this(tileId, showRoute, null, AccessoryValue.OFF);
  }

  public TileEvent(String tileId, boolean showRoute, Orientation incomingSide) {
    this(tileId, showRoute, incomingSide, AccessoryValue.OFF);
  }

  public TileEvent(String tileId, boolean showRoute, Orientation incomingSide, Color trackRouteColor) {
    this(tileId, showRoute, incomingSide, AccessoryValue.OFF, trackRouteColor);
  }

  public TileEvent(String tileId, boolean showRoute, Orientation incomingSide, AccessoryValue routeState) {
    this(tileId, showRoute, incomingSide, routeState, Tile.DEFAULT_ROUTE_TRACK_COLOR);
  }

  public TileEvent(String tileId, boolean showRoute, Orientation incomingSide, AccessoryValue routeState, Color trackRouteColor) {
    this(tileId, showRoute, incomingSide, routeState, Tile.DEFAULT_BACKGROUND_COLOR, Tile.DEFAULT_TRACK_COLOR, trackRouteColor, BlockState.FREE);
  }

  public TileEvent(String tileId, boolean showRoute, BlockState blockState) {
    this(tileId, showRoute, null, AccessoryValue.OFF, Tile.DEFAULT_BACKGROUND_COLOR, Tile.DEFAULT_TRACK_COLOR, Tile.DEFAULT_ROUTE_TRACK_COLOR, blockState);
  }

  public TileEvent(String tileId, boolean showRoute, Orientation incomingSide, AccessoryValue routeState, Color backgroundColor, Color trackColor, Color trackRouteColor, BlockState blockState) {
    this.tileId = tileId;
    this.showRoute = showRoute;
    this.incomingSide = incomingSide;
    this.routeState = routeState;

    this.tileBean = null;
    this.blockBean = null;

    this.backgroundColor = backgroundColor;
    this.trackColor = trackColor;
    this.trackRouteColor = trackRouteColor;
    this.blockState = blockState;
  }

  public TileEvent(BlockBean blockBean) {
    this.tileId = blockBean.getTileId();

    this.showRoute = false;
    this.blockBean = blockBean;

    this.incomingSide = Orientation.EAST;
    this.backgroundColor = Tile.DEFAULT_BACKGROUND_COLOR;
    this.trackColor = Tile.DEFAULT_TRACK_COLOR;
    this.trackRouteColor = Tile.DEFAULT_ROUTE_TRACK_COLOR;
  }

  public TileEvent(TileBean tileBean) {
    this.tileId = tileBean.getId();

    this.showRoute = false;

    this.tileBean = tileBean;
    this.blockBean = tileBean.getBlockBean();

    this.incomingSide = tileBean.getOrientation();
    this.backgroundColor = Tile.DEFAULT_BACKGROUND_COLOR;
    this.trackColor = Tile.DEFAULT_TRACK_COLOR;
    this.trackRouteColor = Tile.DEFAULT_ROUTE_TRACK_COLOR;
  }

  public boolean isEventFor(Tile tile) {
    return this.tileId.equals(tile.getId());
  }

  public String getTileId() {
    return tileId;
  }

  public boolean isShowRoute() {
    return showRoute;
  }

  public Orientation getIncomingSide() {
    return incomingSide;
  }

  public AccessoryValue getRouteState() {
    return routeState;
  }

  public Color getBackgroundColor() {
    return backgroundColor;
  }

  public Color getTrackColor() {
    return trackColor;
  }

  public Color getTrackRouteColor() {
    return trackRouteColor;
  }

  public BlockBean getBlockBean() {
    return blockBean;
  }

  public TileBean getTileBean() {
    return tileBean;
  }

  public BlockState getBlockState() {
    return blockState;
  }

}
