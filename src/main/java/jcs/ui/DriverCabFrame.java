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
package jcs.ui;

import java.awt.Image;
import java.awt.Taskbar;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.JCS;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.entities.CommandStationBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.persistence.PersistenceFactory;
import jcs.ui.util.ImageUtil;
import jcs.util.RunUtil;
import org.tinylog.Logger;

/**
 * Frame to use when you want to simply drive a Locomotive on the track.
 *
 */
public class DriverCabFrame extends JFrame implements LocomotiveDirectionEventListener {

  private static final long serialVersionUID = 6139691226868043462L;

  private List<LocomotiveBean> filteredLocos;
  List<String> locoNames;

  private CommandStationBean commandStation;
  private LocomotiveBeanComboBoxModel locomotiveComboBoxModel;

  public DriverCabFrame() {
    initComponents();

    URL iconUrl = JCS.class.getResource("/media/jcs-train-64.png");
    //URL iconUrl = JCS.class.getResource("/media/jcs-train-2-512.png");
    if (iconUrl != null) {
      this.setIconImage(new ImageIcon(iconUrl).getImage());
    }

    postInit();
  }

  private void postInit() {
    this.driverCabPanel.setLocomotiveBean(null);
    this.driverCabPanel.setDirectionListener(this);
    if (PersistenceFactory.getService() != null) {
      this.commandStation = PersistenceFactory.getService().getDefaultCommandStation();
    }
    loadLocomotives();
  }

  public void loadLocomotives() {
    if (PersistenceFactory.getService() != null) {
      List<LocomotiveBean> locos = new LinkedList<>();
      LocomotiveBean emptyBean = new LocomotiveBean();
      locos.add(emptyBean);
      if (PersistenceFactory.getService() != null) {
        locos.addAll(PersistenceFactory.getService().getLocomotivesByCommandStationId(commandStation.getId(), true));
      }

      locomotiveComboBoxModel = new LocomotiveBeanComboBoxModel();
      locomotiveComboBoxModel.removeAllElements();
      locomotiveComboBoxModel.addAll(locos);

      this.locoCB.setModel(locomotiveComboBoxModel);

      locoNames = new ArrayList<>(locos.size());
      filteredLocos = new ArrayList<>(locos.size());
      for (LocomotiveBean loc : locos) {
        if (loc.isShow()) {
          filteredLocos.add(loc);
          locoNames.add(loc.getName());
        }
      }
    }
  }

  /**
   * Overridden to remove all the listeners
   */
  @Override
  public void dispose() {
    this.driverCabPanel.setVisible(false);
    super.dispose();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    driverCabPanel = new jcs.ui.DriverCabPanel();
    northPanel = new javax.swing.JPanel();
    filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(30, 0), new java.awt.Dimension(35, 0), new java.awt.Dimension(20, 32767));
    locoLabel = new javax.swing.JLabel();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(50, 0), new java.awt.Dimension(35, 0), new java.awt.Dimension(50, 32767));
    locoCB = new javax.swing.JComboBox<>();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    getContentPane().add(driverCabPanel, java.awt.BorderLayout.CENTER);

    northPanel.setMinimumSize(new java.awt.Dimension(500, 60));
    northPanel.setPreferredSize(new java.awt.Dimension(500, 60));
    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 0);
    flowLayout1.setAlignOnBaseline(true);
    northPanel.setLayout(flowLayout1);
    northPanel.add(filler2);

    locoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    locoLabel.setPreferredSize(new java.awt.Dimension(120, 60));
    northPanel.add(locoLabel);
    northPanel.add(filler1);

    locoCB.setPreferredSize(new java.awt.Dimension(150, 30));
    locoCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        locoCBActionPerformed(evt);
      }
    });
    northPanel.add(locoCB);

    getContentPane().add(northPanel, java.awt.BorderLayout.NORTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void locoCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locoCBActionPerformed
    LocomotiveBean selLoc = (LocomotiveBean) locomotiveComboBoxModel.getSelectedItem();
    if (selLoc != null) {
      String name = selLoc.getName();
      long id = selLoc.getId();
      Logger.trace(evt.getActionCommand() + " -> " + name + " id: " + id);
    } else {
      Logger.trace(evt.getActionCommand() + " -> null");
    }

    if (((LocomotiveBean) this.locomotiveComboBoxModel.getSelectedItem()).getName() != null) {
      LocomotiveBean locomotive = (LocomotiveBean) this.locomotiveComboBoxModel.getSelectedItem();
      this.setTitle(locomotive.getName());

      if (locomotive.getLocIcon() != null) {
        Image img = locomotive.getLocIcon();
        if (Direction.BACKWARDS == locomotive.getDirection()) {
          img = ImageUtil.flipVertically(locomotive.getLocIcon());
          this.locoLabel.setIcon(new ImageIcon(img));
        } else {
          this.locoLabel.setIcon(new ImageIcon(img));
        }
        this.locoLabel.setText(null);
      } else {
        this.locoLabel.setText(locomotive.getName());
      }
      this.driverCabPanel.setLocomotiveBean(locomotive);
    } else {
      this.driverCabPanel.setLocomotiveBean(null);
      this.setTitle("Locomotive not Selected");
      this.locoLabel.setIcon(null);
      this.locoLabel.setText(null);
    }
  }//GEN-LAST:event_locoCBActionPerformed

  @Override
  public void onDirectionChange(LocomotiveDirectionEvent directionEvent) {
    LocomotiveBean locomotive = (LocomotiveBean) this.locomotiveComboBoxModel.getSelectedItem();

    Image img = locomotive.getLocIcon();
    if (img != null) {
      if (Direction.BACKWARDS == locomotive.getDirection()) {
        img = ImageUtil.flipVertically(locomotive.getLocIcon());
        this.locoLabel.setIcon(new ImageIcon(img));
      } else {
        this.locoLabel.setIcon(new ImageIcon(img));
      }
    }
  }

  class LocomotiveBeanByNameSorter implements Comparator<LocomotiveBean> {

    @Override
    public int compare(LocomotiveBean a, LocomotiveBean b) {
      //Avoid null pointers
      String aa = a.getName();
      if (aa == null) {
        aa = "000";
      }
      String bb = b.getName();
      if (bb == null) {
        bb = "000";
      }

      return aa.compareTo(bb);
    }
  }

  class LocomotiveBeanComboBoxModel extends DefaultComboBoxModel<LocomotiveBean> {

    private final List<LocomotiveBean> model;

    public LocomotiveBeanComboBoxModel() {
      model = new ArrayList<>();
    }

    @Override
    public void addAll(int index, Collection<? extends LocomotiveBean> elements) {
      model.addAll(index, elements);
      Collections.sort(model, new LocomotiveBeanByNameSorter());

      fireContentsChanged(this, 0, getSize());
    }

    @Override
    public void addAll(Collection<? extends LocomotiveBean> elements) {
      model.addAll(elements);
      Collections.sort(model, new LocomotiveBeanByNameSorter());

      fireContentsChanged(this, 0, getSize());
    }

    @Override
    public void addElement(LocomotiveBean element) {
      if (model.add(element)) {
        Collections.sort(model, new LocomotiveBeanByNameSorter());

        fireContentsChanged(this, 0, getSize());
      }
    }

    @Override
    public LocomotiveBean getElementAt(int index) {
      return (LocomotiveBean) model.toArray()[index];
    }

    @Override
    public int getIndexOf(Object object) {
      return this.model.indexOf(object);
    }

    @Override
    public int getSize() {
      return model.size();
    }

    @Override
    public void insertElementAt(LocomotiveBean locomotiveBean, int index) {
      this.model.add(index, locomotiveBean);
      this.fireContentsChanged(this, 0, getSize());
    }

    @Override
    public void removeAllElements() {
      model.clear();
      fireContentsChanged(this, 0, getSize());
    }

    @Override
    public void removeElement(Object anObject) {
      int index = this.getIndexOf(anObject);
      removeElementAt(index);
    }

    @Override
    public void removeElementAt(int index) {
      this.model.remove(index);
      fireContentsChanged(this, 0, getSize());
    }
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private jcs.ui.DriverCabPanel driverCabPanel;
  private javax.swing.Box.Filler filler1;
  private javax.swing.Box.Filler filler2;
  private javax.swing.JComboBox<LocomotiveBean> locoCB;
  private javax.swing.JLabel locoLabel;
  private javax.swing.JPanel northPanel;
  // End of variables declaration//GEN-END:variables

  public static void main(String args[]) {
    try {
      String plaf = System.getProperty("jcs.plaf", "com.formdev.flatlaf.FlatLightLaf");
      if (plaf != null) {
        UIManager.setLookAndFeel(plaf);
      }
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error(ex);
    }

    //String frameImageUrl = "/media/jcs-train-64.png";
    String frameImageUrl = "/media/jcs-train-2-512.png";

    java.awt.EventQueue.invokeLater(() -> {
      DriverCabFrame driverFrame = new DriverCabFrame();

      if (RunUtil.isMacOSX()) {
        System.setProperty("apple.awt.application.name", "JCS");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.appearance", "system");

        Taskbar taskbar = Taskbar.getTaskbar();
        try {
          BufferedImage img = ImageIO.read(DriverCabFrame.class.getResource(frameImageUrl));
          taskbar.setIconImage(img);
        }
        catch (IOException | UnsupportedOperationException | SecurityException ex) {
          Logger.warn("Error: " + ex.getMessage());
        }
      }
      URL iconUrl = KeyboardSensorPanel.class.getResource(frameImageUrl);
      if (iconUrl != null) {
        driverFrame.setIconImage(new ImageIcon(iconUrl).getImage());
      }

      driverFrame.pack();
      driverFrame.setLocationRelativeTo(null);
      driverFrame.setVisible(true);
      driverFrame.setResizable(false);
    });
  }
}
