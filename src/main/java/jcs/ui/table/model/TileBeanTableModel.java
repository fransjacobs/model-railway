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
package jcs.ui.table.model;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

import jcs.entities.TileBean;
import jcs.ui.util.ImageUtil;
import org.tinylog.Logger;

/**
 *
 */
public class TileBeanTableModel extends AbstractBeanTableModel<TileBean> {

  private static final long serialVersionUID = -7463115301886964811L;

  private static final String[] DISPLAY_COLUMNS = new String[]{"name", "image"};

  private final Map<String, String> tileIcons;

  private final static String[] COMPONENTS = new String[]{
    "Straight", "Straight Direction", "Curved", "Sensor", "Block", "Signal", "Turnout Left", "Turnout Right", "Cross Left", "Cross Right", "Crossing", "End Track"};

  public TileBeanTableModel() {
    super(TileBean.class, DISPLAY_COLUMNS, false, true);
    tileIcons = new HashMap<>();
    loadTileIcons();
  }

  @Override
  public void refresh() {
    List<TileBean> tileBeans = new ArrayList<>();
    for (String trackComponent : COMPONENTS) {
      TileBean.TileType tileType = getTileType(trackComponent);
      TileBean.Orientation defaultOrientation = TileBean.Orientation.EAST;
      TileBean.Direction direction;
      String name = trackComponent;
      switch (name) {
        case "Turnout Left" ->
          direction = TileBean.Direction.LEFT;
        case "Cross Left" ->
          direction = TileBean.Direction.LEFT;
        case "Turnout Right" ->
          direction = TileBean.Direction.RIGHT;
        case "Cross Right" ->
          direction = TileBean.Direction.RIGHT;
        default ->
          direction = TileBean.Direction.CENTER;
      }
      TileBean tb = new TileBean(null, tileType, defaultOrientation, direction, 0, 0);
      tb.setName(name);
      tb.setIcon(tileIcons.get(name));
      ImageIcon imageIcon = new ImageIcon(getClass().getResource(tb.getIcon()));
      Image image = imageIcon.getImage();
      if (TileBean.TileType.BLOCK == tileType) {
        //image = ImageUtil.rotate(image, 90);
      } else {
        image = ImageUtil.rotate(image, 90);
      }

      //int height = image.getHeight(null);
      //int width = image.getWidth(null);
      tb.setTileIcon(new ImageIcon(image));

      tileBeans.add(tb);
    }

    Logger.trace("In total there are " + tileBeans.size() + " TileBeans");

    setBeans(tileBeans);
  }

  /**
   * Returns the number of columns in the model.
   *
   * @return The column count.
   */
  @Override
  public int getColumnCount() {
    return DISPLAY_COLUMNS.length;
  }

  @Override
  public String getColumnName(int column) {
    return switch (column) {
      case 0 ->
        "name";
      case 1 ->
        "image";
      default ->
        null;
    };
  }

  @Override
  public Object getValueAt(int row, int column) {
    if (beans != null && row < beans.size()) {
      TileBean tb = beans.get(row);

      return switch (column) {
        case 0 ->
          tb.getName();
        case 1 ->
          tb.getTileIcon();
        default ->
          null;
      };
    }
    return null;
  }

  public TileBean getTileBean(int row) {
    return this.beans.get(row);
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0 ->
        String.class;
      case 1 ->
        ImageIcon.class;
      default ->
        String.class;
    };
  }

  private void loadTileIcons() {
    tileIcons.put("Straight", "/media/new-straight.png");
    tileIcons.put("Straight Direction", "/media/new-straightDirection.png");
    tileIcons.put("Curved", "/media/new-diagonal.png");
    tileIcons.put("Sensor", "/media/new-straight-feedback.png");
    tileIcons.put("Signal", "/media/new-straight-signal.png");
    tileIcons.put("Sensor", "/media/new-straight-feedback.png");
    tileIcons.put("Turnout Left", "/media/new-L-turnout.png");
    tileIcons.put("Turnout Right", "/media/new-R-turnout.png");
    tileIcons.put("Cross Left", "/media/new-cross-L.png");
    tileIcons.put("Cross Right", "/media/new-cross-R.png");
    tileIcons.put("Crossing", "/media/new-crossing.png");
    tileIcons.put("End Track", "/media/new-end-track.png");
    tileIcons.put("Block", "/media/new-block.png");
  }

  private TileBean.TileType getTileType(String name) {
    switch (name) {
      case "Straight" -> {
        return TileBean.TileType.STRAIGHT;
      }
      case "Straight Direction" -> {
        return TileBean.TileType.STRAIGHT_DIR;
      }
      case "Curved" -> {
        return TileBean.TileType.CURVED;
      }
      case "Sensor" -> {
        return TileBean.TileType.SENSOR;
      }
      case "Signal" -> {
        return TileBean.TileType.SIGNAL;
      }
      case "Turnout Left" -> {
        return TileBean.TileType.SWITCH;
      }
      case "Turnout Right" -> {
        return TileBean.TileType.SWITCH;
      }
      case "Cross Left" -> {
        return TileBean.TileType.CROSS;
      }
      case "Cross Right" -> {
        return TileBean.TileType.CROSS;
      }
      case "Crossing" -> {
        return TileBean.TileType.CROSSING;
      }
      case "End Track" -> {
        return TileBean.TileType.END;
      }
      case "Block" -> {
        return TileBean.TileType.BLOCK;
      }
      default -> {
        Logger.warn(name + " is unknown");
        return TileBean.TileType.STRAIGHT;
      }
    }
  }

}
