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
import javax.swing.UIManager;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.entities.AccessoryBean.SignalType;
import jcs.entities.AccessoryBean.SignalValue;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import jcs.entities.TileBean.TileType;
import jcs.ui.layout.tiles.ui.SignalUI;
import jcs.ui.layout.tiles.ui.TileUI;

/**
 * Representation of a Signal besides the track on the layout
 */
public class Signal extends Straight implements AccessoryEventListener {

  Signal(TileBean tileBean) {
    super(tileBean);
  }

  Signal(Orientation orientation, int x, int y, SignalType signalType) {
    this(orientation, new Point(x, y), signalType);
  }

  Signal(Orientation orientation, Point center) {
    this(orientation, center, SignalType.HP01);
  }

  Signal(Orientation orientation, Point center, SignalType signalType) {
    super(orientation, center);
    this.tileType = TileType.SIGNAL;
    this.signalType = signalType;
    model.setSignalValue(SignalValue.OFF);
  }

  @Override
  public String getUIClassID() {
    return SignalUI.UI_CLASS_ID;
  }

  @Override
  public void updateUI() {
    UIManager.put(TileUI.UI_CLASS_ID, "jcs.ui.layout.tiles.ui.SignalUI");
    setUI((TileUI) UIManager.getUI(this));
    invalidate();
  }

  //TODO move to UI delegate
  @Override
  public void onAccessoryChange(AccessoryEvent event) {
    if (getAccessoryBean() != null && event.isEventFor(accessoryBean)) {
      setSignalValue(event.getAccessoryBean().getSignalValue());
    }
  }
}
