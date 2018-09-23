/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lan.wervel.jcs.ui.nb;

import java.awt.event.ActionEvent;
import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import lan.wervel.jcs.common.TrackRepository;
import lan.wervel.jcs.repository.model.DriveWay;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class JToggleButtonDriveWayBinder implements Serializable {

  private static final long serialVersionUID = 1373557133137833731L;

  private final TrackRepository repository;
  private final int panelNumber;
  private final int binderNumber;
  private final Integer address;
  private DriveWay dw = null;

  private JToggleButton btn;

  private final static String DEFAULT_ICON = "/media/Button-Grey-20px.png";
  private final static String DEFAULT_SELECTED_ICON = "/media/Button-Purple-20px.png";

  public JToggleButtonDriveWayBinder(TrackRepository repository, int panelNumber, int binderNumber) {
    this.repository = repository;
    this.panelNumber = panelNumber;
    this.binderNumber = binderNumber;
    this.address = panelNumber * 16 + binderNumber;

    if (this.repository != null) {
      Logger.trace("Binder: " + binderNumber + " Retrieving SA: " + address + " PanelNumber " + panelNumber);
      dw = this.repository.getDriveWay(address);
    }
  }

  private void setTextAndIcons() {
    String iconFile = DEFAULT_ICON;
    String selectedIconFile = DEFAULT_SELECTED_ICON;

    if (dw != null) {
      Logger.trace("Setting params for DW: " + dw+" Type: "+dw.getType());
      String type = dw.getType();

      if (type == null) {
        type = "";
      }
      
      switch (type) {
        case "route":
          iconFile = "/media/feed-in-20px.png";
          selectedIconFile = "/media/feed-out-20px.png";
          break;
        case "track":
          iconFile = "/media/wifi-black-20px.png";
          selectedIconFile = "/media/wifi-blue-20px.png";
          break;
        default:
          iconFile = "/media/Button-Green-20px.png";
          selectedIconFile = "/media/Button-Red-20px.png";
          break;
      }
    }

    if (dw != null) {
      int addressOffset = panelNumber * 16;
      int buttonNumber = dw.getAddress() + addressOffset;

      Logger.trace("Binder: " + this.binderNumber + " Btn Nr: " + buttonNumber + " DW: " + dw + " Icon: " + iconFile + " SelIcon: " + selectedIconFile);

      btn.setText(buttonNumber + "");
      btn.setIcon(new ImageIcon(getClass().getResource(iconFile)));
      btn.setSelectedIcon(new ImageIcon(getClass().getResource(selectedIconFile)));
      btn.setSelected(this.isSelected());

      addListener();
    } else {
      //No mapped item, disable
      Logger.trace("No DW for " + this.address + " and Binder nr: " + this.binderNumber + " Disabling Button...");
      btn.setEnabled(false);
    }
  }

  private void toggleStatus(ActionEvent evt) {
    JToggleButton tb = (JToggleButton) evt.getSource();

    if (dw!= null) {
      if(tb.isSelected()) {
      dw.activate();
      }else {
        dw.deActivate();
      }  
    }
  }

  private void addListener() {
    btn.addActionListener((ActionEvent evt) -> {
      toggleStatus(evt);
    });
  }

  public void setSelected(boolean flag) {
    if (this.dw != null) {
      if(flag) {
        dw.activate();
      }
      else {
        dw.deActivate();
      }
    }
  }

  public boolean isSelected() {
    if (dw != null) {
      return dw.isActive();
    }
    return false;
  }

  public void setBtn(JToggleButton btn) {
    this.btn = btn;
    setTextAndIcons();
  }

  public JToggleButton getBtn() {
    return btn;
  }

  public void refresh() {
    this.dw = this.repository.getDriveWay(address);
    //Logger.trace("Refresing address: "+this.address+" SA: " + sa);
    btn.setSelected(isSelected());
  }

  public TrackRepository getRepository() {
    return repository;
  }

  public DriveWay getDwS() {
    return dw;
  }

  public void setDw(DriveWay dw) {
    this.dw = dw;
  }

  public int getPanelNumber() {
    return panelNumber;
  }

  public int getBinderNumber() {
    return binderNumber;
  }
}
