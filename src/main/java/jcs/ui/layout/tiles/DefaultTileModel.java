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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import jcs.entities.BlockBean.BlockState;
import jcs.entities.LocomotiveBean;

/**
 *
 * @author fransjacobs
 */
@SuppressWarnings("serial") // Same-version serialization only
public class DefaultTileModel implements TileModel {

  protected transient ChangeEvent changeEvent = null;

  /**
   * Stores the listeners on this model.
   */
  protected EventListenerList listenerList = new EventListenerList();

  protected boolean selected = false;
  protected boolean scaleImage = true;
  protected boolean showCenter = false;
  protected boolean showRoute = false;
  protected boolean showBlockState = false;
  protected boolean showLocomotiveImage = false;
  protected boolean showAccessoryValue = false;
  protected boolean showSignalValue = false;
  protected boolean sensorActive = false;
  protected boolean showOutline = false;

  protected BlockState blockState;
  protected boolean reverseArrival;
  protected String arrivalSuffix;
  protected LocomotiveBean.Direction logicalDirection;
  protected LocomotiveBean locomotive;

  public DefaultTileModel() {

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
  public BlockState getBlockState() {
    if (blockState == null) {
      blockState = BlockState.FREE;
    }
    return blockState;
  }

  @Override
  public void setBlockState(BlockState blockState) {
    this.blockState = blockState;
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
  public boolean isReverseArrival() {
    return reverseArrival;
  }

  @Override
  public void setReverseArrival(boolean reverseArrival) {
    this.reverseArrival = reverseArrival;
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
    fireStateChanged();
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
   * @return all of this model's <code>ChangeListener</code>s or an empty array if no change listeners are currently registered
   *
   * @see #addChangeListener
   * @see #removeChangeListener
   *
   * @since 1.4
   */
  public ChangeListener[] getChangeListeners() {
    return listenerList.getListeners(ChangeListener.class);
  }

  /**
   * Notifies all listeners that have registered interest for notification on this event type. The event instance is created lazily.
   *
   * @see EventListenerList
   */
  protected void fireStateChanged() {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ChangeListener.class) {
        // Lazily create the event:
        if (changeEvent == null) {
          changeEvent = new ChangeEvent(this);
        }
        ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
//  @Override
//  public void addItemListener(ItemListener l) {
//    listenerList.add(ItemListener.class, l);
//  }
  /**
   * {@inheritDoc}
   */
//  @Override
//  public void removeItemListener(ItemListener l) {
//    listenerList.remove(ItemListener.class, l);
//  }
  /**
   * Returns an array of all the item listeners registered on this <code>DefaultButtonModel</code>.
   *
   * @return all of this model's <code>ItemListener</code>s or an empty array if no item listeners are currently registered
   *
   * @see #addItemListener
   * @see #removeItemListener
   *
   * @since 1.4
   */
  public ItemListener[] getItemListeners() {
    return listenerList.getListeners(ItemListener.class);
  }

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
   * @see EventListenerList
   */
  protected void fireActionPerformed(ActionEvent e) {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ActionListener.class) {
        // Lazily create the event:
        // if (changeEvent == null)
        // changeEvent = new ChangeEvent(this);
        ((ActionListener) listeners[i + 1]).actionPerformed(e);
      }
    }
  }

  /**
   * Overridden to return <code>null</code>.
   */
//  @Override
//  public Object[] getSelectedObjects() {
//    return null;
//  }
}
