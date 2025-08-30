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
package jcs.ui.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.BlockBean;
import jcs.entities.RouteBean;
import jcs.entities.RouteBean.RouteState;
import jcs.entities.RouteElementBean;
import jcs.entities.TileBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Dialog panel for importing and editing locomotive settings
 *
 * @author Frans Jacobs
 */
public class DrivewaySettingsPanel extends JPanel {

  private final RouteBeanListModel routeListModel;

  private RouteBean selectedRoute;

  private BlockBean fromBlock;
  private BlockBean toBlock;

  private final DefaultComboBoxModel<BlockBean> fromBlockCBModel;
  private final DefaultComboBoxModel<BlockBean> toBlockCBModel;
  private final DefaultComboBoxModel<RouteBean.RouteState> routeStateCBModel;
  private final DefaultComboBoxModel<BlockBean.BlockState> fromBlockStateCBModel;
  private final DefaultComboBoxModel<BlockBean.BlockState> toBlockStateCBModel;

  public DrivewaySettingsPanel() {
    routeListModel = new RouteBeanListModel();
    fromBlockCBModel = new DefaultComboBoxModel<>();
    toBlockCBModel = new DefaultComboBoxModel<>();
    routeStateCBModel = new DefaultComboBoxModel<>();
    fromBlockStateCBModel = new DefaultComboBoxModel<>();
    toBlockStateCBModel = new DefaultComboBoxModel<>();

    initComponents();
    //A Route can't be added manual for now
    newBtn.setVisible(false);
    initModels();
  }

  private void initModels() {
    if (PersistenceFactory.getService() != null) {
      routeListModel.clear();
      List<RouteBean> routes = PersistenceFactory.getService().getRoutes();
      routeListModel.addAll(routes);
      routeList.setModel(routeListModel);

      List<BlockBean> fromBlocks = new ArrayList<>();
      fromBlocks.add(new BlockBean());
      fromBlocks.addAll(PersistenceFactory.getService().getBlocks());
      fromBlockCBModel.addAll(fromBlocks);
      fromCB.setModel(fromBlockCBModel);

      List<BlockBean> toBlocks = new ArrayList<>();
      toBlocks.add(new BlockBean());
      toBlocks.addAll(PersistenceFactory.getService().getBlocks());
      toBlockCBModel.addAll(toBlocks);
      toCB.setModel(toBlockCBModel);

      routeStateCBModel.addAll(Arrays.asList(RouteBean.RouteState.values()));
      routeStateCB.setModel(routeStateCBModel);

      fromBlockStateCBModel.addAll(Arrays.asList(BlockBean.BlockState.values()));
      fromBlockStateCB.setModel(fromBlockStateCBModel);

      toBlockStateCBModel.addAll(Arrays.asList(BlockBean.BlockState.values()));
      toBlockStateCB.setModel(toBlockStateCBModel);

      setFieldValues();
    }
  }

  private void setFieldValues() {
    if (selectedRoute != null) {
      String id = selectedRoute.getId();
      idLbl.setText(id);

      String fromTileId = selectedRoute.getFromTileId();
      if (fromTileId != null) {
        fromBlock = PersistenceFactory.getService().getBlockByTileId(fromTileId);
        fromBlockCBModel.setSelectedItem(fromBlock);
        fromBlockStateCBModel.setSelectedItem(fromBlock.getBlockState());
      }

      String fromSuffix = selectedRoute.getFromSuffix();
      if (fromSuffix != null) {
        fromSuffixSpinner.setValue(fromSuffix);
      }

      String toTileId = selectedRoute.getToTileId();
      if (toTileId != null) {
        toBlock = PersistenceFactory.getService().getBlockByTileId(toTileId);
        toBlockCBModel.setSelectedItem(toBlock);
        toBlockStateCBModel.setSelectedItem(toBlock.getBlockState());
      }

      String toSuffix = selectedRoute.getToSuffix();
      if (toSuffix != null) {
        toSuffixSpinner.setValue(toSuffix);
      }

      boolean locked = selectedRoute.isLocked();
      lockedCB.setSelected(locked);

      RouteState routeState = selectedRoute.getRouteState();
      routeStateCBModel.setSelectedItem(routeState);
    } else {
      idLbl.setText(null);
      fromBlock = null;
      fromSuffixSpinner.setValue(" ");
      toBlock = null;
      toSuffixSpinner.setValue(" ");
      lockedCB.setSelected(false);
      selectedRoute = null;
      fromBlockCBModel.setSelectedItem(new BlockBean());
      toBlockCBModel.setSelectedItem(new BlockBean());

    }
    enableFields(selectedRoute != null);
  }

  private void enableFields(boolean enable) {
    this.fromCB.setEnabled(false);
    this.toCB.setEnabled(false);
    this.fromSuffixSpinner.setEnabled(false);
    this.toSuffixSpinner.setEnabled(false);
    this.fromBlockStateCB.setEnabled(false);
    this.toBlockStateCB.setEnabled(false);
    
    this.routeStateCB.setEnabled(enable);
    this.lockedCB.setEnabled(enable);

    this.saveBtn.setEnabled(enable);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings({"unchecked", "deprecation"})
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    topPanel = new JPanel();
    leftPanel = new JPanel();
    idLbl = new JLabel();
    rightPanel = new JPanel();
    newBtn = new JButton();
    deleteBtn = new JButton();
    mainTP = new JTabbedPane();
    detailPanel = new JPanel();
    row0Panel = new JPanel();
    fromLbl = new JLabel();
    fromCB = new JComboBox<>();
    fromSuffixSpinner = new JSpinner();
    toLbl = new JLabel();
    toCB = new JComboBox<>();
    toSuffixSpinner = new JSpinner();
    row1Panel = new JPanel();
    fromLbl1 = new JLabel();
    fromBlockStateCB = new JComboBox<>();
    toLbl1 = new JLabel();
    toBlockStateCB = new JComboBox<>();
    row2Panel = new JPanel();
    statusLbl = new JLabel();
    routeStateCB = new JComboBox<>();
    row3Panel = new JPanel();
    nameLbl = new JLabel();
    lockedCB = new JCheckBox();
    row4Panel = new JPanel();
    row5Panel = new JPanel();
    row6Panel = new JPanel();
    row7Panel = new JPanel();
    row9Panel = new JPanel();
    buttonPanel = new JPanel();
    commandPanel = new JPanel();
    westPanel = new JPanel();
    routesSP = new JScrollPane();
    routeList = new JList<>();
    bottomPanel = new JPanel();
    filler1 = new Box.Filler(new Dimension(100, 0), new Dimension(200, 0), new Dimension(150, 32767));
    saveBtn = new JButton();

    setMinimumSize(new Dimension(1000, 600));
    setPreferredSize(new Dimension(1000, 600));
    setLayout(new BorderLayout());

    topPanel.setMinimumSize(new Dimension(1000, 50));
    topPanel.setPreferredSize(new Dimension(1000, 50));
    topPanel.setRequestFocusEnabled(false);
    topPanel.setLayout(new GridLayout(1, 2));

    FlowLayout flowLayout11 = new FlowLayout(FlowLayout.LEFT);
    flowLayout11.setAlignOnBaseline(true);
    leftPanel.setLayout(flowLayout11);

    idLbl.setPreferredSize(new Dimension(150, 17));
    leftPanel.add(idLbl);

    topPanel.add(leftPanel);

    FlowLayout flowLayout2 = new FlowLayout(FlowLayout.RIGHT);
    flowLayout2.setAlignOnBaseline(true);
    rightPanel.setLayout(flowLayout2);

    newBtn.setIcon(new ImageIcon(getClass().getResource("/media/add-24.png"))); // NOI18N
    newBtn.setToolTipText("Create new Accessory");
    newBtn.setEnabled(false);
    newBtn.setMaximumSize(new Dimension(40, 40));
    newBtn.setMinimumSize(new Dimension(40, 40));
    newBtn.setPreferredSize(new Dimension(40, 40));
    newBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        newBtnActionPerformed(evt);
      }
    });
    rightPanel.add(newBtn);

    deleteBtn.setIcon(new ImageIcon(getClass().getResource("/media/delete-24.png"))); // NOI18N
    deleteBtn.setMaximumSize(new Dimension(40, 40));
    deleteBtn.setMinimumSize(new Dimension(40, 40));
    deleteBtn.setPreferredSize(new Dimension(40, 40));
    deleteBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        deleteBtnActionPerformed(evt);
      }
    });
    rightPanel.add(deleteBtn);

    topPanel.add(rightPanel);

    add(topPanel, BorderLayout.NORTH);

    detailPanel.setMinimumSize(new Dimension(390, 540));
    detailPanel.setPreferredSize(new Dimension(480, 500));
    detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));

    row0Panel.setMinimumSize(new Dimension(380, 30));
    row0Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout8 = new FlowLayout(FlowLayout.LEFT);
    flowLayout8.setAlignOnBaseline(true);
    row0Panel.setLayout(flowLayout8);

    fromLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    fromLbl.setLabelFor(fromCB);
    fromLbl.setText("From:");
    fromLbl.setToolTipText("");
    fromLbl.setPreferredSize(new Dimension(120, 17));
    row0Panel.add(fromLbl);

    fromCB.setPreferredSize(new Dimension(80, 23));
    row0Panel.add(fromCB);

    fromSuffixSpinner.setModel(new SpinnerListModel(new String[] {" ", "+", "-"}));
    row0Panel.add(fromSuffixSpinner);

    toLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    toLbl.setLabelFor(toCB);
    toLbl.setText("To:");
    toLbl.setToolTipText("");
    toLbl.setPreferredSize(new Dimension(120, 17));
    row0Panel.add(toLbl);

    toCB.setPreferredSize(new Dimension(80, 23));
    row0Panel.add(toCB);

    toSuffixSpinner.setModel(new SpinnerListModel(new String[] {" ", "+", "-"}));
    row0Panel.add(toSuffixSpinner);

    detailPanel.add(row0Panel);

    row1Panel.setMinimumSize(new Dimension(380, 30));
    row1Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    row1Panel.setLayout(flowLayout1);

    fromLbl1.setHorizontalAlignment(SwingConstants.TRAILING);
    fromLbl1.setLabelFor(fromBlockStateCB);
    fromLbl1.setText("From Block State:");
    fromLbl1.setToolTipText("");
    fromLbl1.setPreferredSize(new Dimension(120, 17));
    row1Panel.add(fromLbl1);

    fromBlockStateCB.setToolTipText("The Accessory protocol");
    fromBlockStateCB.setDoubleBuffered(true);
    fromBlockStateCB.setPreferredSize(new Dimension(150, 26));
    fromBlockStateCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        fromBlockStateCBActionPerformed(evt);
      }
    });
    row1Panel.add(fromBlockStateCB);

    toLbl1.setHorizontalAlignment(SwingConstants.TRAILING);
    toLbl1.setLabelFor(toBlockStateCB);
    toLbl1.setText("To Block State:");
    toLbl1.setToolTipText("");
    toLbl1.setPreferredSize(new Dimension(120, 17));
    row1Panel.add(toLbl1);

    toBlockStateCB.setToolTipText("The Accessory protocol");
    toBlockStateCB.setDoubleBuffered(true);
    toBlockStateCB.setPreferredSize(new Dimension(150, 26));
    toBlockStateCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        toBlockStateCBActionPerformed(evt);
      }
    });
    row1Panel.add(toBlockStateCB);

    detailPanel.add(row1Panel);

    row2Panel.setMinimumSize(new Dimension(380, 30));
    row2Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout3 = new FlowLayout(FlowLayout.LEFT);
    flowLayout3.setAlignOnBaseline(true);
    row2Panel.setLayout(flowLayout3);

    statusLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    statusLbl.setLabelFor(routeStateCB);
    statusLbl.setText("Route State:");
    statusLbl.setPreferredSize(new Dimension(120, 16));
    row2Panel.add(statusLbl);

    routeStateCB.setToolTipText("The Accessory protocol");
    routeStateCB.setDoubleBuffered(true);
    routeStateCB.setPreferredSize(new Dimension(150, 26));
    routeStateCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        routeStateCBActionPerformed(evt);
      }
    });
    row2Panel.add(routeStateCB);

    detailPanel.add(row2Panel);

    row3Panel.setMinimumSize(new Dimension(380, 30));
    row3Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout4 = new FlowLayout(FlowLayout.LEFT);
    flowLayout4.setAlignOnBaseline(true);
    row3Panel.setLayout(flowLayout4);

    nameLbl.setHorizontalAlignment(SwingConstants.TRAILING);
    nameLbl.setLabelFor(lockedCB);
    nameLbl.setText("Route Locked:");
    nameLbl.setPreferredSize(new Dimension(120, 16));
    row3Panel.add(nameLbl);
    row3Panel.add(lockedCB);

    detailPanel.add(row3Panel);

    row4Panel.setMinimumSize(new Dimension(380, 30));
    row4Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout5 = new FlowLayout(FlowLayout.LEFT);
    flowLayout5.setAlignOnBaseline(true);
    row4Panel.setLayout(flowLayout5);
    detailPanel.add(row4Panel);

    row5Panel.setMinimumSize(new Dimension(380, 30));
    row5Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout6 = new FlowLayout(FlowLayout.LEFT);
    flowLayout6.setAlignOnBaseline(true);
    row5Panel.setLayout(flowLayout6);
    detailPanel.add(row5Panel);

    row6Panel.setMinimumSize(new Dimension(380, 30));
    row6Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout7 = new FlowLayout(FlowLayout.LEFT);
    flowLayout7.setAlignOnBaseline(true);
    row6Panel.setLayout(flowLayout7);
    detailPanel.add(row6Panel);

    row7Panel.setMinimumSize(new Dimension(380, 30));
    row7Panel.setPreferredSize(new Dimension(380, 30));
    FlowLayout flowLayout9 = new FlowLayout(FlowLayout.LEFT);
    flowLayout9.setAlignOnBaseline(true);
    row7Panel.setLayout(flowLayout9);
    detailPanel.add(row7Panel);

    row9Panel.setMinimumSize(new Dimension(380, 30));
    row9Panel.setPreferredSize(new Dimension(380, 30));
    row9Panel.setLayout(new FlowLayout(FlowLayout.LEFT));
    detailPanel.add(row9Panel);

    buttonPanel.setPreferredSize(new Dimension(380, 40));
    FlowLayout flowLayout10 = new FlowLayout();
    flowLayout10.setAlignOnBaseline(true);
    buttonPanel.setLayout(flowLayout10);
    detailPanel.add(buttonPanel);

    mainTP.addTab("Details", detailPanel);
    mainTP.addTab("Commands", commandPanel);

    add(mainTP, BorderLayout.CENTER);

    westPanel.setMinimumSize(new Dimension(175, 500));
    westPanel.setPreferredSize(new Dimension(175, 500));
    westPanel.setLayout(new BorderLayout());

    routesSP.setPreferredSize(new Dimension(175, 130));

    routeList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        routeListValueChanged(evt);
      }
    });
    routesSP.setViewportView(routeList);

    westPanel.add(routesSP, BorderLayout.CENTER);

    add(westPanel, BorderLayout.WEST);

    bottomPanel.setPreferredSize(new Dimension(1000, 50));
    bottomPanel.setRequestFocusEnabled(false);
    FlowLayout flowLayout12 = new FlowLayout(FlowLayout.RIGHT);
    flowLayout12.setAlignOnBaseline(true);
    bottomPanel.setLayout(flowLayout12);
    bottomPanel.add(filler1);

    saveBtn.setIcon(new ImageIcon(getClass().getResource("/media/save-24.png"))); // NOI18N
    saveBtn.setMaximumSize(new Dimension(40, 40));
    saveBtn.setMinimumSize(new Dimension(40, 40));
    saveBtn.setPreferredSize(new Dimension(40, 40));
    saveBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveBtnActionPerformed(evt);
      }
    });
    bottomPanel.add(saveBtn);

    add(bottomPanel, BorderLayout.SOUTH);
  }// </editor-fold>//GEN-END:initComponents

  private void newBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_newBtnActionPerformed
    Logger.trace("Create new Accessory...");
    selectedRoute = new RouteBean();
    setFieldValues();
  }//GEN-LAST:event_newBtnActionPerformed

  private void saveBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
    if (selectedRoute != null) {
      
      selectedRoute.setLocked(lockedCB.isSelected());
      if(RouteBean.RouteState.FREE == selectedRoute.getRouteState()) {
        selectedRoute.setStatus(null);
      }
      

      Logger.trace("Saving: " + selectedRoute.toLogString());
      selectedRoute = PersistenceFactory.getService().persist(selectedRoute);
    }
  }//GEN-LAST:event_saveBtnActionPerformed

  private void deleteBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
    Logger.trace("Delete: " + selectedRoute.toLogString());
    PersistenceFactory.getService().remove(selectedRoute);
    selectedRoute = null;
    initModels();
  }//GEN-LAST:event_deleteBtnActionPerformed

  private void routeListValueChanged(ListSelectionEvent evt) {//GEN-FIRST:event_routeListValueChanged
    if (!evt.getValueIsAdjusting()) {
      selectedRoute = routeList.getSelectedValue();
      Logger.trace(selectedRoute);
      setFieldValues();
    }
  }//GEN-LAST:event_routeListValueChanged

  private void routeStateCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_routeStateCBActionPerformed
    if (selectedRoute != null) {
          selectedRoute.setBlockState((RouteBean.RouteState) routeStateCB.getSelectedItem());
    }
  }//GEN-LAST:event_routeStateCBActionPerformed

  private void fromBlockStateCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_fromBlockStateCBActionPerformed
    
  }//GEN-LAST:event_fromBlockStateCBActionPerformed

  private void toBlockStateCBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_toBlockStateCBActionPerformed
  
  }//GEN-LAST:event_toBlockStateCBActionPerformed

  
  private List<RouteElementBean> getTurnouts(RouteBean routeBean) {
    List<RouteElementBean> rel = routeBean.getRouteElements();
    List<RouteElementBean> turnouts = new ArrayList<>();
    for (RouteElementBean reb : rel) {
      if (reb.isTurnout()) {
        turnouts.add(reb);
        
        AccessoryBean.AccessoryValue av = reb.getAccessoryValue();
        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
        
        TileBean tb = PersistenceFactory.getService().getTileBean(reb.getTileId());
        AccessoryBean accessory = tb.getAccessoryBean();
        
      }
    }
    return turnouts;
  }  
  
  class Turnout implements Comparator<Integer> {
    private String tileId;
    private String accessoryId;
    private Integer address;
    private String protocol;
    private String name;    
    private AccessoryValue value;
    private Integer sortOrder;
    
    Turnout(String tileId,String accessoryId,Integer address,String protocol,String name,AccessoryValue value,Integer sortOrder) {
      this.tileId = tileId;
    } 

    @Override
    public int compare(Integer o1, Integer o2) {
      return 0;
    }
    
  }
  
  
  class RouteBeanByNameSorter implements Comparator<RouteBean> {

    @Override
    public int compare(RouteBean a, RouteBean b) {
      //Avoid null pointers
      String aa = a.getId();
      if (aa == null) {
        aa = "aaa";
      }
      String bb = b.getId();
      if (bb == null) {
        bb = "aaa";
      }

      return aa.compareTo(bb);
    }
  }

  class RouteBeanListModel extends AbstractListModel<RouteBean> {

    private final List<RouteBean> all;

    public RouteBeanListModel() {
      all = new ArrayList<>();
    }

    public void showTurnoutsOnly(boolean flag) {
      fireContentsChanged(this, 0, getSize());
    }

    public void showSignalsOnly(boolean flag) {
      fireContentsChanged(this, 0, getSize());
    }

    public void resetFilters() {
      fireContentsChanged(this, 0, getSize());
    }

    @Override
    public int getSize() {
      return all.size();
    }

    @Override
    public RouteBean getElementAt(int index) {
      return (RouteBean) all.toArray()[index];
    }

    public void add(RouteBean element) {
      if (all.add(element)) {
        Collections.sort(all, new RouteBeanByNameSorter());
        fireContentsChanged(this, 0, getSize());
      }
    }

    public void addAll(RouteBean elements[]) {
      Collection<RouteBean> c = Arrays.asList(elements);
      all.addAll(c);
      Collections.sort(all, new RouteBeanByNameSorter());
      fireContentsChanged(this, 0, getSize());
    }

    public void addAll(Collection<RouteBean> elements) {
      all.addAll(elements);
      Collections.sort(all, new RouteBeanByNameSorter());
      fireContentsChanged(this, 0, getSize());
    }

    public void clear() {
      all.clear();
      fireContentsChanged(this, 0, getSize());
    }

    public boolean contains(RouteBean element) {
      return all.contains(element);
    }

    public RouteBean firstElement() {
      if (!all.isEmpty()) {
        return all.get(0);
      } else {
        return null;
      }
    }

    public Iterator<RouteBean> iterator() {
      return all.iterator();
    }

    public RouteBean lastElement() {
      if (!all.isEmpty()) {
        return all.get(all.size() - 1);
      } else {
        return null;
      }
    }

    public boolean removeElement(RouteBean element) {
      boolean removed = all.remove(element);
      if (removed) {
        Collections.sort(all, new RouteBeanByNameSorter());
        fireContentsChanged(this, 0, getSize());
      }
      return removed;
    }

  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  JPanel bottomPanel;
  JPanel buttonPanel;
  JPanel commandPanel;
  JButton deleteBtn;
  JPanel detailPanel;
  Box.Filler filler1;
  JComboBox<BlockBean.BlockState> fromBlockStateCB;
  JComboBox<BlockBean> fromCB;
  JLabel fromLbl;
  JLabel fromLbl1;
  JSpinner fromSuffixSpinner;
  JLabel idLbl;
  JPanel leftPanel;
  JCheckBox lockedCB;
  JTabbedPane mainTP;
  JLabel nameLbl;
  JButton newBtn;
  JPanel rightPanel;
  JList<RouteBean> routeList;
  JComboBox<RouteBean.RouteState> routeStateCB;
  JScrollPane routesSP;
  JPanel row0Panel;
  JPanel row1Panel;
  JPanel row2Panel;
  JPanel row3Panel;
  JPanel row4Panel;
  JPanel row5Panel;
  JPanel row6Panel;
  JPanel row7Panel;
  JPanel row9Panel;
  JButton saveBtn;
  JLabel statusLbl;
  JComboBox<BlockBean.BlockState> toBlockStateCB;
  JComboBox<BlockBean> toCB;
  JLabel toLbl;
  JLabel toLbl1;
  JSpinner toSuffixSpinner;
  JPanel topPanel;
  JPanel westPanel;
  // End of variables declaration//GEN-END:variables
}
