/*
 * Copyright (C) 2019 frans.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package lan.wervel.jcs.ui.options;

import lan.wervel.jcs.ui.options.table.PropertiesTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import lan.wervel.jcs.entities.JCSProperty;
import lan.wervel.jcs.trackservice.TrackServiceFactory;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class PropertiesPanel extends JPanel {

  private final PropertiesTableModel propertiesTableModel;

  public PropertiesPanel() {
    propertiesTableModel = new PropertiesTableModel();
    initComponents();
    alignPropertiesTable();
  }

  private void alignPropertiesTable() {
    //this.propertiesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
    //DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    //centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    //this.propertiesTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
   * method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("deprecation")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    topPanel = new JPanel();
    refreshBtn = new JButton();
    newBtn = new JButton();
    centerPanel = new JPanel();
    propertiesTableScrollPane = new JScrollPane();
    propertiesTable = new JTable();
    bottomPanel = new JPanel();
    deleteBtn = new JButton();
    filler1 = new Box.Filler(new Dimension(50, 0), new Dimension(200, 0), new Dimension(150, 32767));
    saveBtn = new JButton();

    setMinimumSize(new Dimension(1000, 600));
    setPreferredSize(new Dimension(1000, 600));
    setLayout(new BorderLayout());

    topPanel.setMinimumSize(new Dimension(1000, 50));
    topPanel.setPreferredSize(new Dimension(1000, 50));
    topPanel.setRequestFocusEnabled(false);
    FlowLayout flowLayout1 = new FlowLayout(FlowLayout.RIGHT);
    flowLayout1.setAlignOnBaseline(true);
    topPanel.setLayout(flowLayout1);

    refreshBtn.setIcon(new ImageIcon(getClass().getResource("/media/refresh-24.png"))); // NOI18N
    refreshBtn.setText("Refresh");
    refreshBtn.setMargin(new Insets(2, 2, 2, 2));
    refreshBtn.setMaximumSize(new Dimension(120, 36));
    refreshBtn.setMinimumSize(new Dimension(120, 36));
    refreshBtn.setPreferredSize(new Dimension(120, 36));
    refreshBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        refreshBtnActionPerformed(evt);
      }
    });
    topPanel.add(refreshBtn);

    newBtn.setIcon(new ImageIcon(getClass().getResource("/media/add-24.png"))); // NOI18N
    newBtn.setText("New");
    newBtn.setToolTipText("Create new Locomotive");
    newBtn.setMaximumSize(new Dimension(120, 36));
    newBtn.setMinimumSize(new Dimension(120, 36));
    newBtn.setPreferredSize(new Dimension(120, 36));
    newBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        newBtnActionPerformed(evt);
      }
    });
    topPanel.add(newBtn);

    add(topPanel, BorderLayout.NORTH);

    centerPanel.setMinimumSize(new Dimension(1000, 540));
    centerPanel.setPreferredSize(new Dimension(1000, 500));
    centerPanel.setLayout(new BorderLayout());

    propertiesTableScrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    propertiesTableScrollPane.setPreferredSize(new Dimension(500, 1000));

    propertiesTable.setModel(propertiesTableModel);
    propertiesTable.setDoubleBuffered(true);
    propertiesTable.setGridColor(new Color(204, 204, 204));
    propertiesTable.setPreferredSize(new Dimension(480, 470));
    propertiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    propertiesTable.getTableHeader().setReorderingAllowed(false);
    propertiesTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        propertiesTableMouseClicked(evt);
      }
    });
    propertiesTableScrollPane.setViewportView(propertiesTable);

    centerPanel.add(propertiesTableScrollPane, BorderLayout.PAGE_START);

    add(centerPanel, BorderLayout.CENTER);

    bottomPanel.setPreferredSize(new Dimension(1014, 50));
    bottomPanel.setRequestFocusEnabled(false);
    FlowLayout flowLayout12 = new FlowLayout(FlowLayout.RIGHT);
    flowLayout12.setAlignOnBaseline(true);
    bottomPanel.setLayout(flowLayout12);

    deleteBtn.setIcon(new ImageIcon(getClass().getResource("/media/delete-24.png"))); // NOI18N
    deleteBtn.setText("Delete");
    deleteBtn.setMaximumSize(new Dimension(100, 36));
    deleteBtn.setMinimumSize(new Dimension(100, 36));
    deleteBtn.setPreferredSize(new Dimension(100, 36));
    deleteBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        deleteBtnActionPerformed(evt);
      }
    });
    bottomPanel.add(deleteBtn);
    bottomPanel.add(filler1);

    saveBtn.setIcon(new ImageIcon(getClass().getResource("/media/save-24.png"))); // NOI18N
    saveBtn.setText("Save");
    saveBtn.setMaximumSize(new Dimension(100, 36));
    saveBtn.setMinimumSize(new Dimension(100, 36));
    saveBtn.setPreferredSize(new Dimension(100, 36));
    saveBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveBtnActionPerformed(evt);
      }
    });
    bottomPanel.add(saveBtn);

    add(bottomPanel, BorderLayout.SOUTH);
  }// </editor-fold>//GEN-END:initComponents


  private void newBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_newBtnActionPerformed
    propertiesTableModel.refresh();
    alignPropertiesTable();

    Logger.debug("Create new JCSProperty...");
    JCSProperty p = new JCSProperty();
    propertiesTableModel.addRow(p);
  }//GEN-LAST:event_newBtnActionPerformed

  private void propertiesTableMouseClicked(MouseEvent evt) {//GEN-FIRST:event_propertiesTableMouseClicked
    JTable source = (JTable) evt.getSource();
    int row = source.rowAtPoint(evt.getPoint());

    JCSProperty p = propertiesTableModel.getControllableDeviceAt(row);
    if (p != null) {
      Logger.debug("Selected row: " + row + ", Property Key: " + p.getKey());
    }
  }//GEN-LAST:event_propertiesTableMouseClicked

  public void refresh() {
    propertiesTableModel.refresh();
    alignPropertiesTable();
  }

  private void refreshBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
    refresh();
  }//GEN-LAST:event_refreshBtnActionPerformed

  private void saveBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
    int selectedRow = this.propertiesTable.getSelectedRow();
    JCSProperty p = this.propertiesTableModel.getControllableDeviceAt(selectedRow);
    Logger.debug("Save the Property: " + p + " ID: " + p.getId());

    JCSProperty cp = TrackServiceFactory.getTrackService().getProperty(p.getKey());
    if (cp != null) {
      p.setId(cp.getId());
      Logger.debug("Found existing "+cp+" ID: "+cp.getId());
    }
    
    TrackServiceFactory.getTrackService().persist(p);

    propertiesTableModel.refresh();
    alignPropertiesTable();
  }//GEN-LAST:event_saveBtnActionPerformed

  private void deleteBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
    int selectedRow = this.propertiesTable.getSelectedRow();
    JCSProperty p = this.propertiesTableModel.getControllableDeviceAt(selectedRow);
    Logger.debug("Delete the Property: " + p);

    TrackServiceFactory.getTrackService().remove(p);
    propertiesTableModel.refresh();
    alignPropertiesTable();
  }//GEN-LAST:event_deleteBtnActionPerformed

  public static void main(String args[]) {
    Configurator.defaultConfig().level(org.pmw.tinylog.Level.DEBUG).activate();

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.warn("Can't set the LookAndFeel: " + ex);
    }
    java.awt.EventQueue.invokeLater(() -> {

      PropertiesPanel testPanel = new PropertiesPanel();
      JFrame testFrame = new JFrame();
      JDialog testDialog = new JDialog(testFrame, true);

      testDialog.add(testPanel);
      testDialog.setIconImage(Toolkit.getDefaultToolkit().getImage(testDialog.getClass().getResource("/media/jcs-train-64.png")));

      testDialog.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
          System.exit(0);
        }
      });
      testDialog.pack();
      testDialog.setLocationRelativeTo(null);

      testDialog.setVisible(true);
    });
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JPanel bottomPanel;
  private JPanel centerPanel;
  private JButton deleteBtn;
  private Box.Filler filler1;
  private JButton newBtn;
  private JTable propertiesTable;
  private JScrollPane propertiesTableScrollPane;
  private JButton refreshBtn;
  private JButton saveBtn;
  private JPanel topPanel;
  // End of variables declaration//GEN-END:variables
}
