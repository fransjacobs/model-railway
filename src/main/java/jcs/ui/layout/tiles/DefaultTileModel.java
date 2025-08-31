/*
 * Copyright 2025 Frans Jacobs.
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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.AccessoryBean.SignalValue;
import jcs.entities.BlockBean;
import jcs.entities.BlockBean.BlockState;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean.Orientation;

/**
 *
 */
public class DefaultTileModel implements TileModel {

  private static final long serialVersionUID = -4394129982618346359L;

  protected transient ChangeEvent changeEvent = null;

  protected EventListenerList listenerList = new EventListenerList();

  protected boolean selected = false;
  protected Color selectedColor;
  protected boolean scaleImage = true;
  protected boolean showCenter = false;
  protected Orientation tileOrienation;
  protected Orientation incomingSide;

  protected boolean showRoute = false;
  protected boolean showBlockState = false;
  protected boolean showLocomotiveImage = false;
  protected boolean showAccessoryValue = false;
  protected boolean showSignalValue = false;
  protected boolean sensorActive = false;
  protected AccessoryValue accessoryValue;
  protected SignalValue signalValue;

  protected boolean showOutline = false;

  protected String arrivalSuffix;
  protected boolean overlayImage = false;
  protected BlockState blockState;
  protected LocomotiveBean locomotive;
  protected LocomotiveBean.Direction logicalDirection;

  public DefaultTileModel() {
    this(Orientation.EAST);
  }

  public DefaultTileModel(Orientation orientation) {
    this.tileOrienation = orientation;
    this.selectedColor = Tile.DEFAULT_SELECTED_COLOR;
    this.blockState = BlockState.FREE;
  }

  @Override
  public boolean isSelected() {
    return selected;
  }

  @Override
  public void setSelected(boolean selected) {
    this.selected = selected;
    fireStateChanged();
  }

  @Override
  public Color getSelectedColor() {
    return selectedColor;
  }

  @Override
  public void setSelectedColor(Color selectedColor) {
    Color prevColor = selectedColor;
    if (selectedColor != null) {
      this.selectedColor = selectedColor;
    } else {
      this.selectedColor = Tile.DEFAULT_SELECTED_COLOR;
    }

    if (!this.selectedColor.equals(prevColor)) {
      fireStateChanged();
    }
  }

  @Override
  public boolean isScaleImage() {
    return scaleImage;
  }

  @Override
  public void setScaleImage(boolean scaleImage) {
    this.scaleImage = scaleImage;
    fireStateChanged();
  }

  @Override
  public boolean isShowCenter() {
    return showCenter;
  }

  @Override
  public void setShowCenter(boolean showCenter) {
    this.showCenter = showCenter;
    fireStateChanged();
  }

  @Override
  public Orientation getTileOrienation() {
    return tileOrienation;
  }

  @Override
  public void setTileOrienation(Orientation tileOrienation) {
    this.tileOrienation = tileOrienation;
    fireStateChanged();
  }

  @Override
  public Orientation getIncomingSide() {
    return incomingSide;
  }

  @Override
  public void setIncomingSide(Orientation incomingSide) {
    this.incomingSide = incomingSide;
  }

  @Override
  public boolean isShowRoute() {
    return showRoute;
  }

  @Override
  public void setShowRoute(boolean showRoute) {
    this.showRoute = showRoute;
    fireStateChanged();
  }

  @Override
  public boolean isShowBlockState() {
    return showBlockState;
  }

  //Set all block properties is one go
  @Override
  public void setBlockBean(BlockBean blockBean) {
    if (blockBean != null) {
      locomotive = blockBean.getLocomotive();
      logicalDirection = LocomotiveBean.Direction.get(blockBean.getLogicalDirection());
      arrivalSuffix = blockBean.getArrivalSuffix();
      setBlockState(blockBean.getBlockState());
    }
  }

  @Override
  public void setShowBlockState(boolean showBlockState) {
    this.showBlockState = showBlockState;
    fireStateChanged();
  }

  @Override
  public boolean isShowLocomotiveImage() {
    return showLocomotiveImage;
  }

  @Override
  public void setShowLocomotiveImage(boolean showLocomotiveImage) {
    this.showLocomotiveImage = showLocomotiveImage;
    fireStateChanged();
  }

  @Override
  public boolean isShowAccessoryValue() {
    return showAccessoryValue;
  }

  @Override
  public void setShowAccessoryValue(boolean showAccessoryValue) {
    this.showAccessoryValue = showAccessoryValue;
    fireStateChanged();
  }

  @Override
  public boolean isShowSignalValue() {
    return showSignalValue;
  }

  @Override
  public void setShowSignalValue(boolean showSignalValue) {
    this.showSignalValue = showSignalValue;
    fireStateChanged();
  }

  @Override
  public boolean isSensorActive() {
    return sensorActive;
  }

  @Override
  public void setSensorActive(boolean sensorActive) {
    this.sensorActive = sensorActive;
    fireStateChanged();
  }

  @Override
  public AccessoryValue getAccessoryValue() {
    return accessoryValue;
  }

  @Override
  public void setAccessoryValue(AccessoryValue accessoryValue) {
    this.accessoryValue = accessoryValue;
    fireStateChanged();
  }

  @Override
  public SignalValue getSignalValue() {
    return signalValue;
  }

  @Override
  public void setSignalValue(SignalValue signalValue) {
    this.signalValue = signalValue;
    fireStateChanged();
  }

  @Override
  public BlockState getBlockState() {
    if (blockState == null) {
      blockState = BlockState.FREE;
    }
    return blockState;
  }

  @Override
  public void setBlockState(BlockState blockState) {
    this.blockState = blockState;
    overlayImage = locomotive != null
            && locomotive.getLocIcon() != null
            && (BlockState.OCCUPIED == blockState || BlockState.INBOUND == blockState || BlockState.OUTBOUND == blockState);

    if (BlockState.FREE == blockState || BlockState.OCCUPIED == blockState) {
      arrivalSuffix = null;
    }

    fireStateChanged();
  }

  @Override
  public String getArrivalSuffix() {
    return arrivalSuffix;
  }

  @Override
  public void setArrivalSuffix(String arrivalSuffix) {
    this.arrivalSuffix = arrivalSuffix;
    fireStateChanged();
  }

  @Override
  public void setDepartureSuffix(String suffix) {
    if (null == suffix) {
      setArrivalSuffix(null);
    } else {
      switch (suffix) {
        case "-" ->
          setArrivalSuffix("+");
        default ->
          setArrivalSuffix("-");
      }
    }
  }

  @Override
  public String getDepartureSuffix() {
    String departureSuffix = null;
    if (arrivalSuffix != null) {
      if ("-".equals(arrivalSuffix)) {
        departureSuffix = "+";
      } else {
        departureSuffix = "-";
      }
    }

    return departureSuffix;
  }

  @Override
  public void reverseArrival() {
    if (arrivalSuffix != null) {
      if ("-".equals(arrivalSuffix)) {
        arrivalSuffix = "+";
      } else {
        arrivalSuffix = "-";
      }
    }
    fireStateChanged();
  }

  @Override
  public LocomotiveBean.Direction getLogicalDirection() {
    return logicalDirection;
  }

  @Override
  public void setLogicalDirection(LocomotiveBean.Direction logicalDirection) {
    this.logicalDirection = logicalDirection;
    fireStateChanged();
  }

  @Override
  public LocomotiveBean getLocomotive() {
    return locomotive;
  }

  @Override
  public void setLocomotive(LocomotiveBean locomotive) {
    this.locomotive = locomotive;
    if (locomotive != null) {
      blockState = BlockState.OCCUPIED;
    } else {
      blockState = BlockState.FREE;
      arrivalSuffix = null;
    }

    this.overlayImage = locomotive != null
            && locomotive.getLocIcon() != null
            && (BlockState.OCCUPIED == blockState || BlockState.INBOUND == blockState || BlockState.OUTBOUND == blockState);

    fireStateChanged();
  }

  @Override
  public boolean isOverlayImage() {
    return overlayImage;
  }

  @Override
  public void setOverlayImage(boolean overlayImage) {
    this.overlayImage = overlayImage;
  }

  @Override
  public void addChangeListener(ChangeListener l) {
    listenerList.add(ChangeListener.class, l);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeChangeListener(ChangeListener l) {
    listenerList.remove(ChangeListener.class, l);
  }

  /**
   * Returns an array of all the change listeners registered on this <code>DefaultButtonModel</code>.
   *
   * @return
   */
  public ChangeListener[] getChangeListeners() {
    return listenerList.getListeners(ChangeListener.class);
  }

  /**
   * Notifies all listeners that have registered interest for notification on this event type.br> The event instance is created lazily.
   */
  protected void fireStateChanged() {
    Object[] listeners = listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ChangeListener.class) {
        if (changeEvent == null) {
          changeEvent = new ChangeEvent(this);
        }
        ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
      }
    }
  }

//  public ItemListener[] getItemListeners() {
//    return listenerList.getListeners(ItemListener.class);
//  }
  @Override
  public void addActionListener(ActionListener l) {
    listenerList.add(ActionListener.class, l);
  }

  @Override
  public void removeActionListener(ActionListener l) {
    listenerList.remove(ActionListener.class, l);
  }

  @Override
  public ActionListener[] getActionListeners() {
    return listenerList.getListeners(ActionListener.class);
  }

  /**
   * Notifies all listeners that have registered interest for notification on this event type.
   *
   * @param e the <code>ActionEvent</code> to deliver to listeners
   */
  protected void fireActionPerformed(ActionEvent e) {
    Object[] listeners = listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ActionListener.class) {

        // if (changeEvent == null)
        // changeEvent = new ChangeEvent(this);
        ((ActionListener) listeners[i + 1]).actionPerformed(e);
      }
    }
  }

}
