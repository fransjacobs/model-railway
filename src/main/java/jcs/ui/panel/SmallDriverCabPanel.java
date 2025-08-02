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
package jcs.ui.panel;

import com.twelvemonkeys.image.ImageUtil;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeListener;
import jcs.JCS;
import jcs.commandStation.JCSCommandStation;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.util.LocomotiveSelectionChangedListener;
import org.tinylog.Logger;

/**
 * Small function Panel for use in the small driver Cab
 */
public class SmallDriverCabPanel extends JPanel implements LocomotiveSelectionChangedListener,
        LocomotiveDirectionEventListener, LocomotiveFunctionEventListener, LocomotiveSpeedEventListener {

  private static final long serialVersionUID = -7861849821871355547L;

  private final Map<Integer, JToggleButton> buttons;
  private LocomotiveBean locomotiveBean;
  private boolean initButtons = false;
  private boolean disableListener = false;
  private boolean power;

  private boolean enableEvent = true;
  //private final ExecutorService executor;

  public SmallDriverCabPanel() {
    buttons = new HashMap<>();
    //executor = Executors.newCachedThreadPool();

    initComponents();
    mapButtons();
  }

  private void mapButtons() {
    buttons.put(0, f0TB);
    buttons.put(1, f1TB);
    buttons.put(2, f2TB);
    buttons.put(3, f3TB);
    buttons.put(4, f4TB);
    buttons.put(5, f5TB);
    buttons.put(6, f6TB);
    buttons.put(7, f7TB);
    buttons.put(8, f8TB);
    buttons.put(9, f9TB);
    buttons.put(10, f10TB);
    buttons.put(11, f11TB);
    buttons.put(12, f12TB);
    buttons.put(13, f13TB);
    buttons.put(14, f14TB);
    buttons.put(15, f15TB);

    buttons.put(16, f16TB);
    buttons.put(17, f17TB);
    buttons.put(18, f18TB);
    buttons.put(19, f19TB);
    buttons.put(20, f20TB);
    buttons.put(21, f21TB);
    buttons.put(22, f22TB);
    buttons.put(23, f23TB);
    buttons.put(24, f24TB);
    buttons.put(25, f25TB);
    buttons.put(26, f26TB);
    buttons.put(27, f27TB);
    buttons.put(28, f28TB);
    buttons.put(29, f29TB);
    buttons.put(30, f30TB);
    buttons.put(31, f31TB);

    setEnabled(false);
    if (JCS.getJcsCommandStation() != null) {
      //JCS.getJcsCommandStation().addLocomotiveFunctionEventListener(this);
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    for (int i = 0; i < 32; i++) {
      JToggleButton button = this.buttons.get(i);
      button.setEnabled(enabled);
    }
    buttonsTP.setEnabled(enabled);
  }

  @Override
  public void onFunctionChange(LocomotiveFunctionEvent event) {
    if (locomotiveBean != null && locomotiveBean.getId().equals(event.getFunctionBean().getLocomotiveId())) {
      FunctionBean fb = event.getFunctionBean();
      //this.buttons.get(fb.getNumber()).setSelected(fb.isOn());
      JToggleButton tbtn = buttons.get(fb.getNumber());

      //Temp disable the event handling as this is an external event...
      enableEvent = false;
      tbtn.doClick();
      enableEvent = true;

    } else {
      Logger.trace("Function button for LocomotiveId " + event.getFunctionBean().getLocomotiveId() + " and number " + event.getFunctionBean().getNumber() + " not found");
    }
  }

  private void resetButtons() {
    for (JToggleButton btn : buttons.values()) {
      btn.setIcon(null);
      btn.setSelectedIcon(null);
      btn.setText("");
      btn.setEnabled(false);

      //btn.setForeground(new java.awt.Color(0, 0, 0));
      btn.setForeground(new Color(204, 204, 204));
      btn.setBackground(new Color(204, 204, 204));

      btn.setSelected(false);
    }
  }

  @Override
  public void selectionChanged(Long locomotiveId) {
    boolean selectionChanged;
    if (locomotiveBean == null && locomotiveId != null) {
      selectionChanged = true;
    } else if (locomotiveId != null && locomotiveBean != null && !locomotiveId.equals(locomotiveBean.getId())) {
      Logger.trace("LocomotiveId is changing from : " + locomotiveBean.getId() + " to " + locomotiveId);
      selectionChanged = true;
    } else {
      selectionChanged = false;
      Logger.trace("Selection NOT changed. Current selection: " + locomotiveBean.getId());
    }

    if (selectionChanged) {
      Logger.trace("Change selection to LocomotiveId: " + locomotiveId);
      //Disable listeners!
      disableListener = true;
      if (JCS.getJcsCommandStation() != null) {
        JCSCommandStation cs = JCS.getJcsCommandStation();
        cs.removeLocomotiveDirectionEventListener(this);
        cs.removeLocomotiveFunctionEventListener(this);
        cs.removeLocomotiveSpeedEventListener(this);
      }

      resetButtons();
      initButtons = true;
      if (PersistenceFactory.getService() != null) {
        //Get the locomotive from the persistent layer
        locomotiveBean = PersistenceFactory.getService().getLocomotive(locomotiveId);

        nameLbl.setText(locomotiveBean.getName());
        if (locomotiveBean.getLocIcon() != null) {
          setLocomotiveImage(locomotiveBean.getLocIcon().getImage());
        }

        double max = 100;
        if (locomotiveBean.getTachoMax() != null) {
          max = locomotiveBean.getTachoMax();
        }
        speedSlider.setMaximum((int) max);

        //Calc speed
        int velocity = locomotiveBean.getVelocity();
        int sliderValue = (int) Math.round(max / 1000 * velocity);
        speedSlider.setValue(sliderValue);

        forwardButton.setSelected(LocomotiveBean.Direction.FORWARDS == locomotiveBean.getDirection());
        reverseButton.setSelected(LocomotiveBean.Direction.BACKWARDS == locomotiveBean.getDirection());

        Map<Integer, FunctionBean> functions = locomotiveBean.getFunctions();

        Logger.trace("Loc: " + locomotiveBean.getName() + " has " + functions.size() + " functions");
        for (FunctionBean fb : functions.values()) {
          Integer fnr = fb.getNumber();
          JToggleButton btn = this.buttons.get(fnr);

          //Logger.trace("Function: " + fb.getNumber() + " Type: " + fb.getFunctionType() + " Value: " + fb.getValue() + " isMomentary: " + fb.isMomentary());
          if (fb.getInActiveIconImage() != null) {
            btn.setIcon(fb.getInActiveIconImage());
          } else {
            btn.setText("F" + fb.getNumber());
            //Logger.trace("Missing Icon: " + fb.getInActiveIcon() + " Button Text: " + btn.getText());
          }

          if (fb.getActiveIconImage() != null) {
            btn.setSelectedIcon(fb.getActiveIconImage());
          } else {
            btn.setText("F" + fb.getNumber());
            //Logger.trace("Missing Icon: " + fb.getActiveIcon() + " Button Text: " + btn.getText());
          }

          btn.setActionCommand("F" + fb.getNumber());
          btn.setEnabled(true);

          boolean isOn = fb.getValue() == 1;
          if (isOn) {
            btn.doClick();
          }
          //Logger.trace("Button " + btn.getActionCommand() + " selected: " + btn.isSelected());
        }
        buttonsTP.setEnabled(true);
      }

      if (JCS.getJcsCommandStation() != null) {
        JCSCommandStation cs = JCS.getJcsCommandStation();
        cs.addLocomotiveDirectionEventListener(this);
        cs.addLocomotiveFunctionEventListener(this);
        cs.addLocomotiveSpeedEventListener(this);
      }

      initButtons = false;
      disableListener = false;
    }
  }

  private void setLocomotiveImage(Image img) {
    if (img != null) {
      int size = 40;
      float aspect = (float) img.getHeight(null) / (float) img.getWidth(null);
      img = img.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH);
      BufferedImage bi = ImageUtil.toBuffered(img);
      iconLbl.setIcon(new ImageIcon(bi));
      iconLbl.setHorizontalAlignment(JLabel.CENTER);
      iconLbl.setText("");
    } else {
      iconLbl.setText("");
    }
  }

  /**
   *
   * Sets the LocomotiveBean for this panel. This will update the buttons with the correct icons and states.
   *
   * @param locomotive
   */
  public void setLocomotiveBean(LocomotiveBean locomotive) {
    selectionChanged(locomotive.getId());
  }

  public LocomotiveBean getLocomotiveBean() {
    return locomotiveBean;
  }

  private void buttonActionPerformed(ActionEvent evt) {
    JToggleButton src = (JToggleButton) evt.getSource();
    boolean value = src.isSelected();

    if (src.getIcon() == null) {
      if (value) {
        src.setForeground(new java.awt.Color(0, 0, 0));
        src.setFont(new java.awt.Font("sansserif", 1, 13));
      } else {
        src.setForeground(new java.awt.Color(204, 204, 204));
        src.setFont(new java.awt.Font("sansserif", 0, 13));
      }
    }

    Logger.trace(evt.getActionCommand() + ": " + (value ? "On" : "Off"));
    Integer functionNumber = Integer.decode(evt.getActionCommand().replace("F", ""));

    FunctionBean fb = locomotiveBean.getFunctionBean(functionNumber);

    if (!initButtons && enableEvent) {
      Logger.trace("Function " + fb.getNumber() + " Value: " + fb.isOn() + " Momentary: " + fb.isMomentary());
      //executor.execute(() -> changeFunction(value, functionNumber, locomotiveBean));
      changeFunction(value, functionNumber, locomotiveBean);
    }
  }

  private void changeFunction(boolean newValue, Integer functionNumber, LocomotiveBean locomotiveBean) {
    if (JCS.getJcsCommandStation() != null && locomotiveBean != null) {
      FunctionBean fb = locomotiveBean.getFunctionBean(functionNumber);
      Logger.trace("Function " + fb.getNumber() + " Value: " + fb.isOn() + " new Value: " + newValue + " Momentary: " + fb.isMomentary());

      if (JCS.getJcsCommandStation() != null) {
        JCS.getJcsCommandStation().changeLocomotiveFunction(newValue, functionNumber, locomotiveBean);
      }
      if (fb.isMomentary() && newValue) {
        JToggleButton tb = buttons.get(fb.getNumber());
        tb.doClick();
      }
    }
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    directionBG = new javax.swing.ButtonGroup();
    northPanel = new javax.swing.JPanel();
    iconLbl = new javax.swing.JLabel();
    filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
    nameLbl = new javax.swing.JLabel();
    rightPanel = new javax.swing.JPanel();
    jPanel3 = new javax.swing.JPanel();
    speed1Button = new javax.swing.JButton();
    speed2Button = new javax.swing.JButton();
    speed3Button = new javax.swing.JButton();
    speed4Button = new javax.swing.JButton();
    jPanel4 = new javax.swing.JPanel();
    speedSlider = new javax.swing.JSlider();
    centerPanel = new javax.swing.JPanel();
    buttonsTP = new javax.swing.JTabbedPane();
    f0f7Panel = new javax.swing.JPanel();
    f0TB = new javax.swing.JToggleButton();
    f1TB = new javax.swing.JToggleButton();
    f2TB = new javax.swing.JToggleButton();
    f3TB = new javax.swing.JToggleButton();
    f4TB = new javax.swing.JToggleButton();
    f5TB = new javax.swing.JToggleButton();
    f6TB = new javax.swing.JToggleButton();
    f7TB = new javax.swing.JToggleButton();
    f8f15Panel = new javax.swing.JPanel();
    f8TB = new javax.swing.JToggleButton();
    f9TB = new javax.swing.JToggleButton();
    f10TB = new javax.swing.JToggleButton();
    f11TB = new javax.swing.JToggleButton();
    f12TB = new javax.swing.JToggleButton();
    f13TB = new javax.swing.JToggleButton();
    f14TB = new javax.swing.JToggleButton();
    f15TB = new javax.swing.JToggleButton();
    f16f23Panel = new javax.swing.JPanel();
    f16TB = new javax.swing.JToggleButton();
    f17TB = new javax.swing.JToggleButton();
    f18TB = new javax.swing.JToggleButton();
    f19TB = new javax.swing.JToggleButton();
    f20TB = new javax.swing.JToggleButton();
    f21TB = new javax.swing.JToggleButton();
    f22TB = new javax.swing.JToggleButton();
    f23TB = new javax.swing.JToggleButton();
    f24f31Panel = new javax.swing.JPanel();
    f24TB = new javax.swing.JToggleButton();
    f25TB = new javax.swing.JToggleButton();
    f26TB = new javax.swing.JToggleButton();
    f27TB = new javax.swing.JToggleButton();
    f28TB = new javax.swing.JToggleButton();
    f29TB = new javax.swing.JToggleButton();
    f30TB = new javax.swing.JToggleButton();
    f31TB = new javax.swing.JToggleButton();
    southPanel = new javax.swing.JPanel();
    reverseButton = new javax.swing.JToggleButton();
    forwardButton = new javax.swing.JToggleButton();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(30, 0), new java.awt.Dimension(30, 0), new java.awt.Dimension(30, 32767));
    stopButton = new javax.swing.JButton();

    setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    setMaximumSize(new java.awt.Dimension(300, 200));
    setMinimumSize(new java.awt.Dimension(300, 200));
    setName("Form"); // NOI18N
    setPreferredSize(new java.awt.Dimension(300, 200));
    setLayout(new java.awt.BorderLayout());

    northPanel.setName("northPanel"); // NOI18N
    northPanel.setPreferredSize(new java.awt.Dimension(300, 30));
    java.awt.FlowLayout flowLayout4 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout4.setAlignOnBaseline(true);
    northPanel.setLayout(flowLayout4);

    iconLbl.setName("iconLbl"); // NOI18N
    northPanel.add(iconLbl);

    filler2.setName("filler2"); // NOI18N
    northPanel.add(filler2);

    nameLbl.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
    nameLbl.setName("nameLbl"); // NOI18N
    northPanel.add(nameLbl);

    add(northPanel, java.awt.BorderLayout.NORTH);

    rightPanel.setName("rightPanel"); // NOI18N
    rightPanel.setPreferredSize(new java.awt.Dimension(80, 200));
    java.awt.FlowLayout flowLayout2 = new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 1, 5);
    flowLayout2.setAlignOnBaseline(true);
    rightPanel.setLayout(flowLayout2);

    jPanel3.setName("jPanel3"); // NOI18N
    jPanel3.setPreferredSize(new java.awt.Dimension(38, 200));
    jcs.ui.swing.layout.VerticalFlowLayout verticalFlowLayout1 = new jcs.ui.swing.layout.VerticalFlowLayout();
    verticalFlowLayout1.sethGap(1);
    jPanel3.setLayout(verticalFlowLayout1);

    speed1Button.setText("I");
    speed1Button.setMargin(new java.awt.Insets(2, 2, 2, 2));
    speed1Button.setMinimumSize(new java.awt.Dimension(30, 30));
    speed1Button.setName("speed1Button"); // NOI18N
    speed1Button.setPreferredSize(new java.awt.Dimension(30, 30));
    speed1Button.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        speed1ButtonActionPerformed(evt);
      }
    });
    jPanel3.add(speed1Button);

    speed2Button.setText("II");
    speed2Button.setMargin(new java.awt.Insets(2, 2, 2, 2));
    speed2Button.setMinimumSize(new java.awt.Dimension(30, 30));
    speed2Button.setName("speed2Button"); // NOI18N
    speed2Button.setPreferredSize(new java.awt.Dimension(30, 30));
    speed2Button.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        speed2ButtonActionPerformed(evt);
      }
    });
    jPanel3.add(speed2Button);

    speed3Button.setText("III");
    speed3Button.setMargin(new java.awt.Insets(2, 2, 2, 2));
    speed3Button.setMinimumSize(new java.awt.Dimension(30, 30));
    speed3Button.setName("speed3Button"); // NOI18N
    speed3Button.setPreferredSize(new java.awt.Dimension(30, 30));
    speed3Button.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        speed3ButtonActionPerformed(evt);
      }
    });
    jPanel3.add(speed3Button);

    speed4Button.setText("IIII");
    speed4Button.setMargin(new java.awt.Insets(2, 2, 2, 2));
    speed4Button.setMinimumSize(new java.awt.Dimension(30, 30));
    speed4Button.setName("speed4Button"); // NOI18N
    speed4Button.setPreferredSize(new java.awt.Dimension(30, 30));
    speed4Button.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        speed4ButtonActionPerformed(evt);
      }
    });
    jPanel3.add(speed4Button);

    rightPanel.add(jPanel3);

    jPanel4.setMinimumSize(new java.awt.Dimension(40, 200));
    jPanel4.setName("jPanel4"); // NOI18N
    jPanel4.setPreferredSize(new java.awt.Dimension(38, 200));
    java.awt.FlowLayout flowLayout3 = new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 1, 5);
    flowLayout3.setAlignOnBaseline(true);
    jPanel4.setLayout(flowLayout3);

    speedSlider.setOrientation(javax.swing.JSlider.VERTICAL);
    speedSlider.setValue(0);
    speedSlider.setName("speedSlider"); // NOI18N
    speedSlider.setPreferredSize(new java.awt.Dimension(30, 180));
    speedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        speedSliderStateChanged(evt);
      }
    });
    jPanel4.add(speedSlider);

    rightPanel.add(jPanel4);

    add(rightPanel, java.awt.BorderLayout.EAST);

    centerPanel.setMinimumSize(new java.awt.Dimension(220, 200));
    centerPanel.setName("centerPanel"); // NOI18N
    centerPanel.setPreferredSize(new java.awt.Dimension(220, 200));
    centerPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 1, 5));

    buttonsTP.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
    buttonsTP.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
    buttonsTP.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
    buttonsTP.setMaximumSize(new java.awt.Dimension(210, 140));
    buttonsTP.setMinimumSize(new java.awt.Dimension(210, 140));
    buttonsTP.setName("buttonsTP"); // NOI18N
    buttonsTP.setPreferredSize(new java.awt.Dimension(210, 140));

    f0f7Panel.setName("f0f7Panel"); // NOI18N
    f0f7Panel.setPreferredSize(new java.awt.Dimension(165, 85));
    f0f7Panel.setLayout(new java.awt.GridLayout(2, 4, 1, 1));

    f0TB.setBackground(new java.awt.Color(204, 204, 204));
    f0TB.setText("F0");
    f0TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f0TB.setContentAreaFilled(false);
    f0TB.setDoubleBuffered(true);
    f0TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f0TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f0TB.setName("f0TB"); // NOI18N
    f0TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f0TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f0TBActionPerformed(evt);
      }
    });
    f0f7Panel.add(f0TB);

    f1TB.setText("F2");
    f1TB.setActionCommand("F1");
    f1TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f1TB.setContentAreaFilled(false);
    f1TB.setDoubleBuffered(true);
    f1TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f1TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f1TB.setName("f1TB"); // NOI18N
    f1TB.setOpaque(true);
    f1TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f1TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f1TBActionPerformed(evt);
      }
    });
    f0f7Panel.add(f1TB);

    f2TB.setBackground(new java.awt.Color(204, 204, 204));
    f2TB.setText("F2");
    f2TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f2TB.setContentAreaFilled(false);
    f2TB.setDoubleBuffered(true);
    f2TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f2TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f2TB.setName("f2TB"); // NOI18N
    f2TB.setOpaque(true);
    f2TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f2TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f2TBActionPerformed(evt);
      }
    });
    f0f7Panel.add(f2TB);

    f3TB.setBackground(new java.awt.Color(204, 204, 204));
    f3TB.setText("F3");
    f3TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f3TB.setContentAreaFilled(false);
    f3TB.setDoubleBuffered(true);
    f3TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f3TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f3TB.setName("f3TB"); // NOI18N
    f3TB.setOpaque(true);
    f3TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f3TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f3TBActionPerformed(evt);
      }
    });
    f0f7Panel.add(f3TB);

    f4TB.setBackground(new java.awt.Color(204, 204, 204));
    f4TB.setText("F4");
    f4TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f4TB.setContentAreaFilled(false);
    f4TB.setDoubleBuffered(true);
    f4TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f4TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f4TB.setName("f4TB"); // NOI18N
    f4TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f4TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f4TBActionPerformed(evt);
      }
    });
    f0f7Panel.add(f4TB);

    f5TB.setBackground(new java.awt.Color(204, 204, 204));
    f5TB.setText("F5");
    f5TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f5TB.setContentAreaFilled(false);
    f5TB.setDoubleBuffered(true);
    f5TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f5TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f5TB.setName("f5TB"); // NOI18N
    f5TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f5TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f5TBActionPerformed(evt);
      }
    });
    f0f7Panel.add(f5TB);

    f6TB.setBackground(new java.awt.Color(204, 204, 204));
    f6TB.setText("F6");
    f6TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f6TB.setContentAreaFilled(false);
    f6TB.setDoubleBuffered(true);
    f6TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f6TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f6TB.setName("f6TB"); // NOI18N
    f6TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f6TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f6TBActionPerformed(evt);
      }
    });
    f0f7Panel.add(f6TB);

    f7TB.setBackground(new java.awt.Color(204, 204, 204));
    f7TB.setText("F7");
    f7TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f7TB.setContentAreaFilled(false);
    f7TB.setDoubleBuffered(true);
    f7TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f7TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f7TB.setName("f7TB"); // NOI18N
    f7TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f7TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f7TBActionPerformed(evt);
      }
    });
    f0f7Panel.add(f7TB);

    buttonsTP.addTab("F0 - F7", f0f7Panel);

    f8f15Panel.setName("f8f15Panel"); // NOI18N
    f8f15Panel.setPreferredSize(new java.awt.Dimension(165, 165));
    f8f15Panel.setLayout(new java.awt.GridLayout(2, 4, 1, 1));

    f8TB.setBackground(new java.awt.Color(204, 204, 204));
    f8TB.setText("F8");
    f8TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f8TB.setContentAreaFilled(false);
    f8TB.setDoubleBuffered(true);
    f8TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f8TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f8TB.setName("f8TB"); // NOI18N
    f8TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f8TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f8TBActionPerformed(evt);
      }
    });
    f8f15Panel.add(f8TB);

    f9TB.setBackground(new java.awt.Color(204, 204, 204));
    f9TB.setText("F9");
    f9TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f9TB.setContentAreaFilled(false);
    f9TB.setDoubleBuffered(true);
    f9TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f9TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f9TB.setName("f9TB"); // NOI18N
    f9TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f9TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f9TBActionPerformed(evt);
      }
    });
    f8f15Panel.add(f9TB);

    f10TB.setBackground(new java.awt.Color(204, 204, 204));
    f10TB.setText("F10");
    f10TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f10TB.setContentAreaFilled(false);
    f10TB.setDoubleBuffered(true);
    f10TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f10TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f10TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f10TB.setName("f10TB"); // NOI18N
    f10TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f10TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f10TBActionPerformed(evt);
      }
    });
    f8f15Panel.add(f10TB);

    f11TB.setBackground(new java.awt.Color(204, 204, 204));
    f11TB.setText("F11");
    f11TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f11TB.setContentAreaFilled(false);
    f11TB.setDoubleBuffered(true);
    f11TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f11TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f11TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f11TB.setName("f11TB"); // NOI18N
    f11TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f11TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f11TBActionPerformed(evt);
      }
    });
    f8f15Panel.add(f11TB);

    f12TB.setBackground(new java.awt.Color(204, 204, 204));
    f12TB.setText("F12");
    f12TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f12TB.setContentAreaFilled(false);
    f12TB.setDoubleBuffered(true);
    f12TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f12TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f12TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f12TB.setName("f12TB"); // NOI18N
    f12TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f12TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f12TBActionPerformed(evt);
      }
    });
    f8f15Panel.add(f12TB);

    f13TB.setBackground(new java.awt.Color(204, 204, 204));
    f13TB.setText("F13");
    f13TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f13TB.setContentAreaFilled(false);
    f13TB.setDoubleBuffered(true);
    f13TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f13TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f13TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f13TB.setName("f13TB"); // NOI18N
    f13TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f13TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f13TBActionPerformed(evt);
      }
    });
    f8f15Panel.add(f13TB);

    f14TB.setBackground(new java.awt.Color(204, 204, 204));
    f14TB.setText("F14");
    f14TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f14TB.setContentAreaFilled(false);
    f14TB.setDoubleBuffered(true);
    f14TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f14TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f14TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f14TB.setName("f14TB"); // NOI18N
    f14TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f14TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f14TBActionPerformed(evt);
      }
    });
    f8f15Panel.add(f14TB);

    f15TB.setBackground(new java.awt.Color(204, 204, 204));
    f15TB.setText("F15");
    f15TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f15TB.setContentAreaFilled(false);
    f15TB.setDoubleBuffered(true);
    f15TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f15TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f15TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f15TB.setName("f15TB"); // NOI18N
    f15TB.setPreferredSize(new java.awt.Dimension(40, 40));
    f15TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f15TBActionPerformed(evt);
      }
    });
    f8f15Panel.add(f15TB);

    buttonsTP.addTab("F16 - F23", f8f15Panel);

    f16f23Panel.setName("f16f23Panel"); // NOI18N
    f16f23Panel.setLayout(new java.awt.GridLayout(2, 4, 1, 1));

    f16TB.setBackground(new java.awt.Color(204, 204, 204));
    f16TB.setText("F16");
    f16TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f16TB.setContentAreaFilled(false);
    f16TB.setDoubleBuffered(true);
    f16TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f16TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f16TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f16TB.setName("f16TB"); // NOI18N
    f16TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f16TBActionPerformed(evt);
      }
    });
    f16f23Panel.add(f16TB);

    f17TB.setBackground(new java.awt.Color(204, 204, 204));
    f17TB.setText("F17");
    f17TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f17TB.setContentAreaFilled(false);
    f17TB.setDoubleBuffered(true);
    f17TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f17TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f17TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f17TB.setName("f17TB"); // NOI18N
    f17TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f17TBActionPerformed(evt);
      }
    });
    f16f23Panel.add(f17TB);

    f18TB.setBackground(new java.awt.Color(204, 204, 204));
    f18TB.setText("F18");
    f18TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f18TB.setContentAreaFilled(false);
    f18TB.setDoubleBuffered(true);
    f18TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f18TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f18TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f18TB.setName("f18TB"); // NOI18N
    f18TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f18TBActionPerformed(evt);
      }
    });
    f16f23Panel.add(f18TB);

    f19TB.setBackground(new java.awt.Color(204, 204, 204));
    f19TB.setText("F19");
    f19TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f19TB.setContentAreaFilled(false);
    f19TB.setDoubleBuffered(true);
    f19TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f19TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f19TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f19TB.setName("f19TB"); // NOI18N
    f19TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f19TBActionPerformed(evt);
      }
    });
    f16f23Panel.add(f19TB);

    f20TB.setBackground(new java.awt.Color(204, 204, 204));
    f20TB.setText("F20");
    f20TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f20TB.setContentAreaFilled(false);
    f20TB.setDoubleBuffered(true);
    f20TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f20TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f20TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f20TB.setName("f20TB"); // NOI18N
    f20TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f20TBActionPerformed(evt);
      }
    });
    f16f23Panel.add(f20TB);

    f21TB.setBackground(new java.awt.Color(204, 204, 204));
    f21TB.setText("F21");
    f21TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f21TB.setContentAreaFilled(false);
    f21TB.setDoubleBuffered(true);
    f21TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f21TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f21TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f21TB.setName("f21TB"); // NOI18N
    f21TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f21TBActionPerformed(evt);
      }
    });
    f16f23Panel.add(f21TB);

    f22TB.setBackground(new java.awt.Color(204, 204, 204));
    f22TB.setText("F22");
    f22TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f22TB.setContentAreaFilled(false);
    f22TB.setDoubleBuffered(true);
    f22TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f22TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f22TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f22TB.setName("f22TB"); // NOI18N
    f22TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f22TBActionPerformed(evt);
      }
    });
    f16f23Panel.add(f22TB);

    f23TB.setBackground(new java.awt.Color(204, 204, 204));
    f23TB.setText("F23");
    f23TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f23TB.setContentAreaFilled(false);
    f23TB.setDoubleBuffered(true);
    f23TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f23TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f23TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f23TB.setName("f23TB"); // NOI18N
    f23TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f23TBActionPerformed(evt);
      }
    });
    f16f23Panel.add(f23TB);

    buttonsTP.addTab("F8 - F15", f16f23Panel);

    f24f31Panel.setName("f24f31Panel"); // NOI18N
    f24f31Panel.setLayout(new java.awt.GridLayout(2, 4, 1, 1));

    f24TB.setBackground(new java.awt.Color(204, 204, 204));
    f24TB.setText("F24");
    f24TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f24TB.setContentAreaFilled(false);
    f24TB.setDoubleBuffered(true);
    f24TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f24TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f24TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f24TB.setName("f24TB"); // NOI18N
    f24TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f24TBActionPerformed(evt);
      }
    });
    f24f31Panel.add(f24TB);

    f25TB.setBackground(new java.awt.Color(204, 204, 204));
    f25TB.setText("F25");
    f25TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f25TB.setContentAreaFilled(false);
    f25TB.setDoubleBuffered(true);
    f25TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f25TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f25TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f25TB.setName("f25TB"); // NOI18N
    f25TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f25TBActionPerformed(evt);
      }
    });
    f24f31Panel.add(f25TB);

    f26TB.setBackground(new java.awt.Color(204, 204, 204));
    f26TB.setText("F26");
    f26TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f26TB.setContentAreaFilled(false);
    f26TB.setDoubleBuffered(true);
    f26TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f26TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f26TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f26TB.setName("f26TB"); // NOI18N
    f26TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f26TBActionPerformed(evt);
      }
    });
    f24f31Panel.add(f26TB);

    f27TB.setBackground(new java.awt.Color(204, 204, 204));
    f27TB.setText("F27");
    f27TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f27TB.setContentAreaFilled(false);
    f27TB.setDoubleBuffered(true);
    f27TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f27TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f27TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f27TB.setName("f27TB"); // NOI18N
    f27TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f27TBActionPerformed(evt);
      }
    });
    f24f31Panel.add(f27TB);

    f28TB.setBackground(new java.awt.Color(204, 204, 204));
    f28TB.setText("F28");
    f28TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f28TB.setContentAreaFilled(false);
    f28TB.setDoubleBuffered(true);
    f28TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f28TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f28TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f28TB.setName("f28TB"); // NOI18N
    f28TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f28TBActionPerformed(evt);
      }
    });
    f24f31Panel.add(f28TB);

    f29TB.setBackground(new java.awt.Color(204, 204, 204));
    f29TB.setText("F29");
    f29TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f29TB.setContentAreaFilled(false);
    f29TB.setDoubleBuffered(true);
    f29TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f29TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f29TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f29TB.setName("f29TB"); // NOI18N
    f29TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f29TBActionPerformed(evt);
      }
    });
    f24f31Panel.add(f29TB);

    f30TB.setBackground(new java.awt.Color(204, 204, 204));
    f30TB.setText("F30");
    f30TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f30TB.setContentAreaFilled(false);
    f30TB.setDoubleBuffered(true);
    f30TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f30TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f30TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f30TB.setName("f30TB"); // NOI18N
    f30TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f30TBActionPerformed(evt);
      }
    });
    f24f31Panel.add(f30TB);

    f31TB.setBackground(new java.awt.Color(204, 204, 204));
    f31TB.setText("F31");
    f31TB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    f31TB.setContentAreaFilled(false);
    f31TB.setDoubleBuffered(true);
    f31TB.setMargin(new java.awt.Insets(2, 2, 2, 2));
    f31TB.setMaximumSize(new java.awt.Dimension(40, 40));
    f31TB.setMinimumSize(new java.awt.Dimension(40, 40));
    f31TB.setName("f31TB"); // NOI18N
    f31TB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        f31TBActionPerformed(evt);
      }
    });
    f24f31Panel.add(f31TB);

    buttonsTP.addTab("F24 - F31", f24f31Panel);

    centerPanel.add(buttonsTP);

    southPanel.setMinimumSize(new java.awt.Dimension(300, 50));
    southPanel.setName("southPanel"); // NOI18N
    southPanel.setPreferredSize(new java.awt.Dimension(298, 50));
    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    southPanel.setLayout(flowLayout1);

    directionBG.add(reverseButton);
    reverseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/left-24.png"))); // NOI18N
    reverseButton.setActionCommand("Backwards");
    reverseButton.setName("reverseButton"); // NOI18N
    reverseButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/media/left-Y-24.png"))); // NOI18N
    reverseButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        reverseButtonActionPerformed(evt);
      }
    });
    southPanel.add(reverseButton);

    directionBG.add(forwardButton);
    forwardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/right-24.png"))); // NOI18N
    forwardButton.setSelected(true);
    forwardButton.setActionCommand("Forwards");
    forwardButton.setName("forwardButton"); // NOI18N
    forwardButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/media/right-Y-24.png"))); // NOI18N
    forwardButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        forwardButtonActionPerformed(evt);
      }
    });
    southPanel.add(forwardButton);

    filler1.setName("filler1"); // NOI18N
    southPanel.add(filler1);

    stopButton.setText("Stop");
    stopButton.setMinimumSize(new java.awt.Dimension(75, 30));
    stopButton.setName("stopButton"); // NOI18N
    stopButton.setPreferredSize(new java.awt.Dimension(75, 30));
    stopButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        stopButtonActionPerformed(evt);
      }
    });
    southPanel.add(stopButton);

    centerPanel.add(southPanel);

    add(centerPanel, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

    private void f12TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f12TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f12TBActionPerformed

    private void f16TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f16TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f16TBActionPerformed

    private void f28TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f28TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f28TBActionPerformed

    private void f0TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f0TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f0TBActionPerformed

    private void f1TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f1TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f1TBActionPerformed

    private void f2TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f2TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f2TBActionPerformed

    private void f3TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f3TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f3TBActionPerformed

    private void f4TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f4TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f4TBActionPerformed

    private void f5TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f5TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f5TBActionPerformed

    private void f6TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f6TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f6TBActionPerformed

    private void f7TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f7TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f7TBActionPerformed

    private void f8TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f8TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f8TBActionPerformed

    private void f9TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f9TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f9TBActionPerformed

    private void f10TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f10TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f10TBActionPerformed

    private void f11TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f11TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f11TBActionPerformed

    private void f13TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f13TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f13TBActionPerformed

    private void f14TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f14TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f14TBActionPerformed

    private void f15TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f15TBActionPerformed
      buttonActionPerformed(evt);
    }//GEN-LAST:event_f15TBActionPerformed

  private void f17TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f17TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f17TBActionPerformed

  private void f18TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f18TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f18TBActionPerformed

  private void f19TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f19TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f19TBActionPerformed

  private void f20TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f20TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f20TBActionPerformed

  private void f21TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f21TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f21TBActionPerformed

  private void f22TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f22TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f22TBActionPerformed

  private void f23TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f23TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f23TBActionPerformed

  private void f24TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f24TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f24TBActionPerformed

  private void f25TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f25TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f25TBActionPerformed

  private void f26TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f26TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f26TBActionPerformed

  private void f27TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f27TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f27TBActionPerformed

  private void f29TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f29TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f29TBActionPerformed

  private void f30TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f30TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f30TBActionPerformed

  private void f31TBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f31TBActionPerformed
    buttonActionPerformed(evt);
  }//GEN-LAST:event_f31TBActionPerformed

  private void speed1ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speed1ButtonActionPerformed
    if (locomotiveBean != null) {
      Integer speed1 = locomotiveBean.getSpeedOne();
      if (speed1 == null || speed1 == 0) {
        speed1 = 10;
      }

      int fullscale = locomotiveBean.getTachoMax();
      double velocity = (speed1 / (double) fullscale) * fullscale;
      speedSlider.setValue((int) velocity);
    } else {
      Logger.trace(evt.getActionCommand());
    }
  }//GEN-LAST:event_speed1ButtonActionPerformed

  private void speed2ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speed2ButtonActionPerformed
    if (locomotiveBean != null) {
      Integer speed2 = locomotiveBean.getSpeedTwo();
      if (speed2 == null || speed2 == 0) {
        speed2 = 50;
      }

      int fullscale = locomotiveBean.getTachoMax();
      double velocity = (speed2 / (double) fullscale) * fullscale;
      speedSlider.setValue((int) velocity);
    } else {
      Logger.trace(evt.getActionCommand());
    }
  }//GEN-LAST:event_speed2ButtonActionPerformed

  private void speed3ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speed3ButtonActionPerformed
    if (locomotiveBean != null) {
      Integer speed3 = locomotiveBean.getSpeedThree();
      if (speed3 == null || speed3 == 0) {
        speed3 = 70;
      }

      int fullscale = locomotiveBean.getTachoMax();
      double velocity = (speed3 / (double) fullscale) * fullscale;
      speedSlider.setValue((int) velocity);
    } else {
      Logger.trace(evt.getActionCommand());
    }
  }//GEN-LAST:event_speed3ButtonActionPerformed

  private void speed4ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speed4ButtonActionPerformed
    if (locomotiveBean != null) {
      Integer speed4 = locomotiveBean.getSpeedFour();
      if (speed4 == null || speed4 == 0) {
        speed4 = 90;
      }

      int fullscale = locomotiveBean.getTachoMax();
      double velocity = (speed4 / (double) fullscale) * fullscale;
      speedSlider.setValue((int) velocity);
    } else {
      Logger.trace(evt.getActionCommand());
    }
  }//GEN-LAST:event_speed4ButtonActionPerformed

  private void reverseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reverseButtonActionPerformed
    if (!disableListener) {
      changeDirection(LocomotiveBean.Direction.get(evt.getActionCommand()));
    }
  }//GEN-LAST:event_reverseButtonActionPerformed

  private void forwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardButtonActionPerformed
    if (!disableListener) {
      changeDirection(LocomotiveBean.Direction.get(evt.getActionCommand()));
    }
  }//GEN-LAST:event_forwardButtonActionPerformed

  private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
    speedSlider.setValue(0);
    if (locomotiveBean == null) {
      Logger.trace(evt.getActionCommand());
    }
  }//GEN-LAST:event_stopButtonActionPerformed

  private void speedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speedSliderStateChanged
    JSlider slider = (JSlider) evt.getSource();
    if (!slider.getValueIsAdjusting()) {
      Logger.trace("Change speed of ");
      if (!disableListener && locomotiveBean != null) {
        int max = locomotiveBean.getTachoMax();
        double value = slider.getValue();
        //Velocity is always between 0 and 1000
        int velocity = (int) Math.round(value / max * 1000);
        //executor.execute(() -> changeVelocity(velocity, locomotiveBean));
        Logger.trace("Changing speed to " + velocity + " for " + locomotiveBean.getId());

        changeVelocity(velocity, locomotiveBean);
      }
    }
  }//GEN-LAST:event_speedSliderStateChanged

  private void changeVelocity(int newVelocity, LocomotiveBean locomotiveBean) {
    if (JCS.getJcsCommandStation() != null && locomotiveBean != null && locomotiveBean.getId() != null) {
      Logger.trace("Changing speeed of " + locomotiveBean + " to: " + newVelocity);
      JCS.getJcsCommandStation().changeLocomotiveSpeed(newVelocity, locomotiveBean);
    }
  }

  private void changeDirection(LocomotiveBean.Direction direction) {
    changeDirection(direction, this.locomotiveBean);
  }

  private void changeDirection(LocomotiveBean.Direction newDirection, LocomotiveBean locomotiveBean) {
    if (JCS.getJcsCommandStation() != null && locomotiveBean != null && locomotiveBean.getId() != null) {
      Logger.trace("Changing direction of " + locomotiveBean + " to: " + newDirection);
      JCS.getJcsCommandStation().changeLocomotiveDirection(newDirection, locomotiveBean);
    }

//    if (this.directionListener != null && locomotiveBean != null) {
//      LocomotiveDirectionEvent dme = new LocomotiveDirectionEvent(locomotiveBean);
//      this.directionListener.onDirectionChange(dme);
//    }
  }

  /**
   * Called when the direction is changed from external source
   *
   * @param event
   */
  @Override
  public void onDirectionChange(LocomotiveDirectionEvent event) {
    LocomotiveBean lb = event.getLocomotiveBean();
    Logger.trace("Evt: id: " + lb.getId() + " Addr: " + lb.getAddress() + " cid: " + lb.getCommandStationId() + " dir: " + lb.getDirection());

    if (event.isEventFor(locomotiveBean)) {
      Logger.trace(lb.getName() + " direction changed from " + locomotiveBean.getDirection() + " to " + lb.getDirection());

      locomotiveBean.setDirection(lb.getDirection());

      //Disable the direction buttons so that the change does not retrigger the commandstation
      disableListener = true;
      reverseButton.setSelected(LocomotiveBean.Direction.BACKWARDS == lb.getDirection());
      forwardButton.setSelected(LocomotiveBean.Direction.FORWARDS == lb.getDirection());
      disableListener = false;
    }
  }

  @Override
  public void onSpeedChange(LocomotiveSpeedEvent event) {
    LocomotiveBean lb = event.getLocomotiveBean();

    if (event.isEventFor(locomotiveBean)) {
      Logger.trace(lb.getName() + " Speed changed from " + this.locomotiveBean.getVelocity() + " to " + lb.getVelocity());

      locomotiveBean.setVelocity(lb.getVelocity());

      disableListener = true;

      int velocity = locomotiveBean.getVelocity();
      double max = locomotiveBean.getTachoMax();
      int sliderValue = (int) Math.round(max / 1000 * velocity);

      //set the slider value without triggering a change event
      ChangeListener[] changeListeners = speedSlider.getChangeListeners();
      for (ChangeListener changeListener : changeListeners) {
        speedSlider.removeChangeListener(changeListener);
      }

      speedSlider.setValue(sliderValue);

      disableListener = false;
      for (ChangeListener changeListener : changeListeners) {
        speedSlider.addChangeListener(changeListener);
      }

    }
  }

  public static void main(String args[]) {
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error("Can't set the LookAndFeel: " + ex);
    }

    JFrame testFrame = new JFrame("SmallDriverCapPanel Tester");
    java.awt.EventQueue.invokeLater(() -> {
      testFrame.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
          System.exit(0);
        }
      });

      SmallDriverCabPanel testPanel = new SmallDriverCabPanel();

      testFrame.add(testPanel);

      if (JCS.getJcsCommandStation() != null) {
        //LocomotiveBean loc = PersistenceFactory.getService().getLocomotive(16417L);
        LocomotiveBean loc = PersistenceFactory.getService().getLocomotive(1000L);
        Logger.debug(loc);

        testPanel.setLocomotiveBean(loc);
      }

      testFrame.pack();
      testFrame.setLocationRelativeTo(null);

      testFrame.setVisible(true);
    });
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  javax.swing.JTabbedPane buttonsTP;
  javax.swing.JPanel centerPanel;
  javax.swing.ButtonGroup directionBG;
  javax.swing.JToggleButton f0TB;
  javax.swing.JPanel f0f7Panel;
  javax.swing.JToggleButton f10TB;
  javax.swing.JToggleButton f11TB;
  javax.swing.JToggleButton f12TB;
  javax.swing.JToggleButton f13TB;
  javax.swing.JToggleButton f14TB;
  javax.swing.JToggleButton f15TB;
  javax.swing.JToggleButton f16TB;
  javax.swing.JPanel f16f23Panel;
  javax.swing.JToggleButton f17TB;
  javax.swing.JToggleButton f18TB;
  javax.swing.JToggleButton f19TB;
  javax.swing.JToggleButton f1TB;
  javax.swing.JToggleButton f20TB;
  javax.swing.JToggleButton f21TB;
  javax.swing.JToggleButton f22TB;
  javax.swing.JToggleButton f23TB;
  javax.swing.JToggleButton f24TB;
  javax.swing.JPanel f24f31Panel;
  javax.swing.JToggleButton f25TB;
  javax.swing.JToggleButton f26TB;
  javax.swing.JToggleButton f27TB;
  javax.swing.JToggleButton f28TB;
  javax.swing.JToggleButton f29TB;
  javax.swing.JToggleButton f2TB;
  javax.swing.JToggleButton f30TB;
  javax.swing.JToggleButton f31TB;
  javax.swing.JToggleButton f3TB;
  javax.swing.JToggleButton f4TB;
  javax.swing.JToggleButton f5TB;
  javax.swing.JToggleButton f6TB;
  javax.swing.JToggleButton f7TB;
  javax.swing.JToggleButton f8TB;
  javax.swing.JPanel f8f15Panel;
  javax.swing.JToggleButton f9TB;
  javax.swing.Box.Filler filler1;
  javax.swing.Box.Filler filler2;
  javax.swing.JToggleButton forwardButton;
  javax.swing.JLabel iconLbl;
  javax.swing.JPanel jPanel3;
  javax.swing.JPanel jPanel4;
  javax.swing.JLabel nameLbl;
  javax.swing.JPanel northPanel;
  javax.swing.JToggleButton reverseButton;
  javax.swing.JPanel rightPanel;
  javax.swing.JPanel southPanel;
  javax.swing.JButton speed1Button;
  javax.swing.JButton speed2Button;
  javax.swing.JButton speed3Button;
  javax.swing.JButton speed4Button;
  javax.swing.JSlider speedSlider;
  javax.swing.JButton stopButton;
  // End of variables declaration//GEN-END:variables
}
