/*
 * Copyright 2023 frans.
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
package jcs.ui.table.cuf;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author frans
 */
public class IconTableCellRenderer extends DefaultTableCellRenderer {

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    
    Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    ((JLabel) cell).setIcon((Icon) value);
    ((JLabel) cell).setText("");
    ((JLabel) cell).setHorizontalAlignment(JLabel.CENTER);

    if (isSelected) {
      cell.setBackground(Color.white);
    } else {
      cell.setBackground(null);
    }
    //((AbstractTableModel)table.getModel()).fireTableCellUpdated(row,column);

    return cell;
  }

}
