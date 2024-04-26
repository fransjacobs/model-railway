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
import jcs.entities.BlockBean;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import jcs.ui.layout.tiles.Tile;

/**
 *
 * @author Frans Jacobs
 */
public class TileEvent {

  private final String tileId;

  private Color backgroundColor;
  private Color trackColor;
  private Color trackRouteColor;
  private Orientation incomingSide;

  private AccessoryBean.AccessoryValue routeState;
  private Color routeStateColor;

  private BlockBean blockBean;
  private TileBean tileBean;

  public TileEvent(TileBean tileBean, Color backgroundColor, Color trackColor) {
    this.tileId = tileBean.getId();
    this.tileBean = tileBean;
    this.backgroundColor = backgroundColor;
    this.trackColor = trackColor;
  }

  public TileEvent(String tileId, Color trackRouteColor, Orientation incomingSide) {
    this(tileId, trackRouteColor, incomingSide, null, null);
  }

  public TileEvent(String tileId, Color trackRouteColor, Orientation incomingSide, AccessoryBean.AccessoryValue routeState, Color routeStateColor) {
    this.tileId = tileId;
    this.trackRouteColor = trackRouteColor;
    this.incomingSide = incomingSide;
    this.routeState = routeState;
    this.routeStateColor = routeStateColor;
  }

  public TileEvent(BlockBean blockBean) {
    this.tileId = blockBean.getTileId();
    this.blockBean = blockBean;
  }

  public TileEvent(TileBean tileBean) {
    this.tileId = tileBean.getId();
    this.tileBean = tileBean;
  }

  public boolean isEventFor(Tile tile) {
    return this.tileId.equals(tile.getId());
  }

  public String getTileId() {
    return tileId;
  }

  public Color getBackgroundColor() {
    if (backgroundColor != null) {
      return backgroundColor;
    } else {
      return Tile.DEFAULT_BACKGROUND_COLOR;
    }
  }

  public Color getTrackColor() {
    if (trackColor != null) {
      return trackColor;
    } else {
      return Tile.DEFAULT_TRACK_COLOR;
    }
  }

  public Color getTrackRouteColor() {
    return trackRouteColor;
  }

  public Orientation getIncomingSide() {
    return incomingSide;
  }

  public AccessoryBean.AccessoryValue getRouteState() {
    return routeState;
  }

  public Color getRouteStateColor() {
    return routeStateColor;
  }

  public BlockBean getBlockBean() {
    return blockBean;
  }

  public TileBean getTileBean() {
    return tileBean;
  }

}
