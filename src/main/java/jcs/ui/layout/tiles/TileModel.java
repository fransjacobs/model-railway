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
import java.awt.event.ActionListener;
import java.io.Serializable;
import javax.swing.event.ChangeListener;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.TileBean;

/**
 *
 * @author fransjacobs
 */
public interface TileModel extends Serializable {

  boolean isSelected();

  public void setSelected(boolean selected);

  Color getSelectedColor();

  public void setSelectedColor(Color selectedColor);

  boolean isScaleImage();

  public void setScaleImage(boolean scaleImage);

  boolean isShowCenter();

  public void setShowCenter(boolean showCenter);

  public TileBean.Orientation getTileOrienation();

  void setTileOrienation(TileBean.Orientation tileOrienation);

  public TileBean.Orientation getIncomingSide();

  void setIncomingSide(TileBean.Orientation incomingSide);

  boolean isShowRoute();

  public void setShowRoute(boolean showRoute);

  boolean isShowBlockState();

  public void setShowBlockState(boolean showBlockState);

  boolean isShowLocomotiveImage();

  public void setShowLocomotiveImage(boolean showLocomotiveImage);

  boolean isShowAccessoryValue();

  public void setShowAccessoryValue(boolean showAccessoryValue);

  boolean isShowSignalValue();

  public void setShowSignalValue(boolean showSignalValue);

  boolean isSensorActive();

  public void setSensorActive(boolean active);

  BlockBean.BlockState getBlockState();

  public void setBlockState(BlockBean.BlockState blockState);

  String getArrivalSuffix();

  public void setArrivalSuffix(String arrivalSuffix);

  String getDepartureSuffix();

  public void setDepartureSuffix(String suffix);

  boolean isReverseArrival();

  public void setReverseArrival(boolean reverseArrival);

  LocomotiveBean.Direction getLogicalDirection();

  public void setLogicalDirection(LocomotiveBean.Direction logicalDirection);

  LocomotiveBean getLocomotive();

  public void setLocomotive(LocomotiveBean locomotive);

  boolean isOverlayImage();

  public void setOverlayImage(boolean overlayImage);

  //
  void addChangeListener(ChangeListener l);

  void removeChangeListener(ChangeListener l);

  void addActionListener(ActionListener l);

  void removeActionListener(ActionListener l);

  ActionListener[] getActionListeners();

}
