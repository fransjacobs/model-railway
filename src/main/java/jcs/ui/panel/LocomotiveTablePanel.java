/*
 * Copyright 2023 Frans Jacobs
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
package jcs.ui.panel;

import com.twelvemonkeys.image.ImageUtil;
import java.awt.Component;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.COPY;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import jcs.commandStation.events.RefreshEvent;
import jcs.commandStation.events.RefreshEventListener;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.DriverCabDialog;
import jcs.ui.table.model.LocomotiveBeanTableModel;
import jcs.ui.util.LocomotiveSelectionChangedListener;
import org.tinylog.Logger;

/**
 * Panel to show locomotives in tabular format
 */
public class LocomotiveTablePanel extends JPanel implements RefreshEventListener {

  private static final long serialVersionUID = 1387464111237136414L;

  private final List<LocomotiveSelectionChangedListener> locomotiveSelectionChangedListeners;

  public LocomotiveTablePanel() {
    locomotiveSelectionChangedListeners = new ArrayList<>();

    locomotiveBeanTableModel = new LocomotiveBeanTableModel();
    initComponents();

    locomotiveTable.setDefaultRenderer(ImageIcon.class, new LocIconRenderer());
    locomotiveTable.getRowSorter().addRowSorterListener((RowSorterEvent e) -> {
      //Logger.trace(e.getType() + "," + e.getSource().getSortKeys());// Sorting changed
    });

    initModel();
  }

  private void initModel() {
    if (PersistenceFactory.getService() != null) {
      refresh();
    }
    locomotiveTable.setDragEnabled(true);

    locomotiveTable.setTransferHandler(new TransferHandler() {
      private static final long serialVersionUID = -7249852729273226500L;

      @Override
      public int getSourceActions(JComponent c) {
        // We specify that the data can be COPIED.
        return COPY;
      }

      @Override
      protected Transferable createTransferable(JComponent c) {
        JTable table = (JTable) c;
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
          return null; // No selection, so nothing to transfer.
        }

        LocomotiveBeanTableModel model = (LocomotiveBeanTableModel) locomotiveTable.getModel();
        LocomotiveBean locomotiveBean = model.getBeanAt(selectedRow);
        
        //refresh the data as it might have changed...
        locomotiveBean = PersistenceFactory.getService().getLocomotive(locomotiveBean.getId());

        if (locomotiveBean.getLocIcon() != null) {
          setDragImage(locomotiveBean.getLocIcon().getImage());
        }

        Logger.trace("LocomotiveBean: " + locomotiveBean);
        return new LocomotiveTablePanel.LocomotiveBeanTransferable(locomotiveBean);
      }
    });

  }

  @Override
  public void onChange(RefreshEvent event) {
    if ("locomotives".equals(event.getSource())) {
      refresh();
    }
  }

  public void refresh() {
    locomotiveBeanTableModel.refresh();
  }

  private class LocIconRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 6174920035602771500L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if (value != null) {
        ImageIcon imgIcon = (ImageIcon) value;
        Image img = imgIcon.getImage();
        float h = img.getHeight(null);
        float w = img.getWidth(null);
        int size = 40;
        float aspect = h / w;
        img = img.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH);
        BufferedImage bi = ImageUtil.toBuffered(img);
        setIcon(new ImageIcon(bi));
        setHorizontalAlignment(JLabel.CENTER);
        setText("");
      }
      return this;
    }
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    locomotiveBeanTableModel = new jcs.ui.table.model.LocomotiveBeanTableModel();
    locomotiveSP = new javax.swing.JScrollPane();
    locomotiveTable = new javax.swing.JTable();

    setPreferredSize(new java.awt.Dimension(300, 410));
    setLayout(new java.awt.BorderLayout());

    locomotiveSP.setViewportView(locomotiveTable);

    locomotiveTable.setModel(locomotiveBeanTableModel);
    locomotiveTable.setDoubleBuffered(true);
    locomotiveTable.setDragEnabled(true);
    locomotiveTable.setRowSorter(new TableRowSorter<>(locomotiveBeanTableModel));
    locomotiveTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    locomotiveTable.setShowGrid(true);
    locomotiveTable.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseReleased(java.awt.event.MouseEvent evt) {
        locomotiveTableMouseReleased(evt);
      }
    });
    locomotiveSP.setViewportView(locomotiveTable);

    add(locomotiveSP, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void locomotiveTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_locomotiveTableMouseReleased
    int row = locomotiveTable.getSelectedRow();

    LocomotiveBean loc = locomotiveBeanTableModel.getBeanAt(row);
    //Logger.trace("Selected " + loc.getName() + " " + evt.getClickCount());
    if (evt.getClickCount() == 2) {
      showDriverCabDialog(loc);
    }

    fireSelectionChangedListeners(loc);
  }//GEN-LAST:event_locomotiveTableMouseReleased

  private void fireSelectionChangedListeners(LocomotiveBean locomotive) {
    if (locomotive.getId() != null) {
      Long locomotiveId = locomotive.getId();
      Logger.trace("Notify " + locomotiveSelectionChangedListeners.size() + " of selection change to locomotiveId: " + locomotiveId);
      for (LocomotiveSelectionChangedListener listener : locomotiveSelectionChangedListeners) {
        listener.selectionChanged(locomotiveId);
      }
    }
  }

  public void addLocomotiveSelectionChangeListener(LocomotiveSelectionChangedListener listener) {
    locomotiveSelectionChangedListeners.add(listener);
  }

  public void removeLocomotiveSelectionChangeListener(LocomotiveSelectionChangedListener listener) {
    locomotiveSelectionChangedListeners.remove(listener);
  }

  private java.awt.Frame getParentFrame() {
    JFrame frame = (JFrame) SwingUtilities.getRoot(this);
    return frame;
  }

  private void showDriverCabDialog(LocomotiveBean locomotiveBean) {
    DriverCabDialog driverDialog = new DriverCabDialog(getParentFrame(), locomotiveBean, false);
    driverDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

    driverDialog.pack();
    driverDialog.setLocationRelativeTo(null);
    driverDialog.setVisible(true);
    driverDialog.setResizable(false);
    driverDialog.toFront();
  }

  static class LocomotiveBeanTransferable implements Transferable {

    private final LocomotiveBean locomotiveBean;

    public LocomotiveBeanTransferable(LocomotiveBean locomotiveBean) {
      this.locomotiveBean = locomotiveBean;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[]{LocomotiveBean.LOCOMOTIVE_BEAN_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return LocomotiveBean.LOCOMOTIVE_BEAN_FLAVOR.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
      if (isDataFlavorSupported(flavor)) {
        return locomotiveBean;
      } else {
        throw new UnsupportedFlavorException(flavor);
      }
    }
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private jcs.ui.table.model.LocomotiveBeanTableModel locomotiveBeanTableModel;
  private javax.swing.JScrollPane locomotiveSP;
  private javax.swing.JTable locomotiveTable;
  // End of variables declaration//GEN-END:variables

}
