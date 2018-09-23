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
import lan.wervel.jcs.repository.model.SolenoidAccessoiry;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry.StatusType;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class JToggleButtonSolenoidAccessoiryBinder implements Serializable {

  private static final long serialVersionUID = 1624209523315185971L;

  private final TrackRepository repository;
  private final int panelNumber;
  private final int binderNumber;
  private final Integer address;
  private SolenoidAccessoiry sa = null;

  private JToggleButton btn;

  private final static String DEFAULT_ICON = "/media/Button-Grey-20px.png";
  private final static String DEFAULT_SELECTED_ICON = "/media/Button-Purple-20px.png";

  public JToggleButtonSolenoidAccessoiryBinder(TrackRepository repository, int panelNumber, int binderNumber) {
    this.repository = repository;
    this.panelNumber = panelNumber;
    this.binderNumber = binderNumber;
    this.address = panelNumber * 16 + binderNumber;

    if (this.repository != null) {
      Logger.trace("Binder: "+binderNumber+" Retrieving SA: "+address+" PanelNumber "+panelNumber);
      sa = this.repository.getSolenoidAccessoiry(address);
    }
  }

  private void setTextAndIcons() {
    String iconFile = DEFAULT_ICON;
    String selectedIconFile = DEFAULT_SELECTED_ICON;

    if (sa != null) {
      Logger.trace("Setting params for SA: "+sa);
      if (sa.isSignal()) {
        String desc = sa.getDescription();
        
        if(desc == null) {
          desc = "";
        }

        switch (desc) {
          case "leave":
            iconFile = "/media/signal-leave-g.png";
            selectedIconFile = "/media/signal-leave-r.png";
            break;
          case "block":
            iconFile = "/media/signal-block-g.png";
            selectedIconFile = "/media/signal-block-r.png";
            break;
          default:
            iconFile = "/media/Button-Green-20px.png";
            selectedIconFile = "/media/Button-Red-20px.png";
            break;
        }
      } else if (sa.isTurnout()) {
        String direction = sa.getDescription();

        switch (direction) {
          case "X":
            iconFile = "/media/turnout-x-c.png";
            selectedIconFile = "/media/turnout-x-s.png";
            break;
          case "R":
            iconFile = "/media/turnout-r-s.png";
            selectedIconFile = "/media/turnout-r-c.png";
            break;
          case "L":
            iconFile = "/media/turnout-l-s.png";
            selectedIconFile = "/media/turnout-l-c.png";
            break;
          default:
            iconFile = "/media/Button-Green-20px.png";
            selectedIconFile = "/media/Button-Red-20px.png";
            break;
        }
      }
    }

    if (sa != null) {
      int addressOffset = panelNumber * 16;
      int buttonNumber = sa.getAddress() + addressOffset;

      Logger.trace("Binder: "+this.binderNumber+" Btn Nr: "+ buttonNumber +" SA: " + sa + " Icon: " + iconFile + " SelIcon: " + selectedIconFile);
      
      btn.setText(buttonNumber + "");
      btn.setIcon(new ImageIcon(getClass().getResource(iconFile)));
      btn.setSelectedIcon(new ImageIcon(getClass().getResource(selectedIconFile)));
      btn.setSelected(this.isSelected());

      addListener();
    } else {
      //No mapped item, disable
      Logger.trace("No SA for " + this.address + " and Binder nr: "+this.binderNumber+" Disabling Button...");
      btn.setEnabled(false);
    }
  }

  private void toggleStatus(ActionEvent evt) {
    JToggleButton tb = (JToggleButton) evt.getSource();

    StatusType st = tb.isSelected() ? StatusType.RED : StatusType.GREEN;
    if (sa != null) {
      sa.setStatus(st);
    }
  }

  private void addListener() {
    btn.addActionListener((ActionEvent evt) -> {
      toggleStatus(evt);
    });
  }

  public void setSelected(boolean flag) {
    if (this.sa != null) {
      this.sa.setStatus((flag ? StatusType.RED : StatusType.GREEN));
    }
  }

  public boolean isSelected() {
    if (sa != null) {
      return SolenoidAccessoiry.StatusType.RED.equals(sa.getStatus());
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
    this.sa = this.repository.getSolenoidAccessoiry(this.address);
    //Logger.trace("Refresing address: "+this.address+" SA: " + sa);
    btn.setSelected(isSelected());
  }

  public TrackRepository getRepository() {
    return repository;
  }

  public SolenoidAccessoiry getSa() {
    return sa;
  }

  public void setSa(SolenoidAccessoiry sa) {
    this.sa = sa;
  }

  public int getPanelNumber() {
    return panelNumber;
  }

  public int getBinderNumber() {
    return binderNumber;
  }
}
