package jcs.ui.table.cuf;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer for the column alignment in tables. Sets the alignment for a column additionally to an optional existing DefaultRenderer
 * of a certain class in that column.
 */
public class ColumnAlignmentRenderer extends DefaultTableCellRenderer {

  /**
   * The ID of the column alignment "left"
   */
  public static final String ALIGN_LEFT = "left";
  /**
   * The ID of the column alignment "right"
   */
  public static final String ALIGN_RIGHT = "right";
  /**
   * The ID of the column alignment "center"
   */
  public static final String ALIGN_CENTER = "center";

  /**
   * alignment for the recent column
   */
  private String mColumnAlignment;

  /**
   * Default-Constructor. Sets default-alignment "left" for this column.
   */
  public ColumnAlignmentRenderer() {
    this(null);
  }

  /**
   * Construct a renderer with the given alingment.
   *
   * @param pColumnAlignment alignment for the recent column, null is treated als "left"
   */
  public ColumnAlignmentRenderer(String pColumnAlignment) {
    if (pColumnAlignment == null) {
      pColumnAlignment = ALIGN_LEFT;
    }

    if (!(pColumnAlignment.equals(ALIGN_LEFT)
            || pColumnAlignment.equals(ALIGN_RIGHT)
            || pColumnAlignment.equals(ALIGN_CENTER))) {
      throw new IllegalArgumentException("alignment >" + pColumnAlignment + "< invalid");
    }
    mColumnAlignment = pColumnAlignment;
  }

  /**
   * Overwrites the method to provide the correct value to the renderer. Sets the alignment for a column additionally to an optional
   * existing DefaultRenderer of a certain class in that column.
   *
   * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean,
   * int, int)
   */
  public Component getTableCellRendererComponent(final JTable pTable, final Object pValue, final boolean pIsSelected, final boolean pHasFocus, final int pRow, final int pColumn) {

    if (pValue != null && !(pValue instanceof Boolean)) {
      try {
        TableCellRenderer renderer = pTable.getDefaultRenderer(pValue.getClass());

        JLabel compLabel = (JLabel) renderer.getTableCellRendererComponent(pTable, pValue, pIsSelected, pHasFocus, pRow, pColumn);

        if (mColumnAlignment.equals(ALIGN_LEFT)) {
          compLabel.setHorizontalAlignment(LEFT);
        } else if (mColumnAlignment.equals(ALIGN_RIGHT)) {
          compLabel.setHorizontalAlignment(RIGHT);
        } else if (mColumnAlignment.equals(ALIGN_CENTER)) {
          compLabel.setHorizontalAlignment(CENTER);
        }

        return compLabel;

      } catch (ClassCastException e) {
        throw new IllegalArgumentException(e.getMessage());
      }

    } else if (pValue != null && pValue instanceof Boolean) {
      try {
        TableCellRenderer renderer = pTable.getDefaultRenderer(pValue.getClass());

        JCheckBox compCheckBox = (JCheckBox) renderer.getTableCellRendererComponent(pTable, pValue, pIsSelected, pHasFocus, pRow, pColumn);

        if (mColumnAlignment.equals(ALIGN_CENTER)) {
          compCheckBox.setHorizontalAlignment(CENTER);
        } else if (mColumnAlignment.equals(ALIGN_LEFT)) {
          compCheckBox.setHorizontalAlignment(LEFT);
        } else if (mColumnAlignment.equals(ALIGN_RIGHT)) {
          compCheckBox.setHorizontalAlignment(RIGHT);
        }

        return compCheckBox;

      } catch (ClassCastException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }

    return super.getTableCellRendererComponent(pTable, pValue, pIsSelected, pHasFocus, pRow, pColumn);
  }
}
