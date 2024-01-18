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
package jcs.ui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import jcs.JCS;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.SensorBean;
import org.tinylog.Logger;

/**
 * Diagnostic panel for a feedback module
 *
 * @author frans
 */
public class FeedbackPanel extends JPanel {

  private static final String ICON_PATH_ON = "/media/Button-Purple-20px.png";
  private static final String ICON_PATH_OFF = "/media/Button-Grey-20px.png";

  private static final ImageIcon ICON_ON = new ImageIcon(FeedbackPanel.class.getResource(ICON_PATH_ON));
  private static final ImageIcon ICON_OFF = new ImageIcon(FeedbackPanel.class.getResource(ICON_PATH_OFF));

  private int moduleNumber;
  private Integer deviceId;
  private Integer contactIdOffset;
  private Integer startContactId;

  private FeedbackPort p1;
  private FeedbackPort p2;
  private FeedbackPort p3;
  private FeedbackPort p4;
  private FeedbackPort p5;
  private FeedbackPort p6;
  private FeedbackPort p7;
  private FeedbackPort p8;
  private FeedbackPort p9;
  private FeedbackPort p10;
  private FeedbackPort p11;
  private FeedbackPort p12;
  private FeedbackPort p13;
  private FeedbackPort p14;
  private FeedbackPort p15;
  private FeedbackPort p16;

  /**
   * Create default FeedbackPanel for module number 1
   */
  public FeedbackPanel() {
    this(1);
  }

  public FeedbackPanel(int moduleNumber) {
    this(1, 0, 0);
  }

  /**
   * Create FeedbackPanel for given moduleNumber
   *
   * @param moduleNumber
   * @param deviceId
   * @param contactIdOffset
   */
  public FeedbackPanel(int moduleNumber, Integer deviceId, Integer contactIdOffset) {
    this.moduleNumber = moduleNumber;
    this.deviceId = deviceId;
    this.contactIdOffset = contactIdOffset;

    initComponents();
  }

  private void initSensorListeners() {
    if (this.contactIdOffset == null) {
      this.contactIdOffset = 0;
    }
    
    int port = 1;
    p1 = new FeedbackPort(this.lbl1, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));

    Logger.trace("Port "+(port-1)+", device: "+deviceId+" module: "+moduleNumber+" offset: "+contactIdOffset+" Contact Addres: "+p1.contactId);
    p2 = new FeedbackPort(this.lbl2, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p3 = new FeedbackPort(this.lbl3, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p4 = new FeedbackPort(this.lbl4, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p5 = new FeedbackPort(this.lbl5, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p6 = new FeedbackPort(this.lbl6, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p7 = new FeedbackPort(this.lbl7, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p8 = new FeedbackPort(this.lbl8, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p9 = new FeedbackPort(this.lbl9, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p10 = new FeedbackPort(this.lbl10, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p11 = new FeedbackPort(this.lbl11, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p12 = new FeedbackPort(this.lbl12, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p13 = new FeedbackPort(this.lbl13, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p14 = new FeedbackPort(this.lbl14, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p15 = new FeedbackPort(this.lbl15, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
    p16 = new FeedbackPort(this.lbl16, deviceId, calculateContactId(moduleNumber, contactIdOffset, port++));
  }

  public void registerSensorListeners() {
    if (JCS.getJcsCommandStation() != null) {
      initSensorListeners();

      JCS.getJcsCommandStation().addSensorEventListener(p1);
      JCS.getJcsCommandStation().addSensorEventListener(p2);
      JCS.getJcsCommandStation().addSensorEventListener(p3);
      JCS.getJcsCommandStation().addSensorEventListener(p4);
      JCS.getJcsCommandStation().addSensorEventListener(p5);
      JCS.getJcsCommandStation().addSensorEventListener(p6);
      JCS.getJcsCommandStation().addSensorEventListener(p7);
      JCS.getJcsCommandStation().addSensorEventListener(p8);
      JCS.getJcsCommandStation().addSensorEventListener(p9);
      JCS.getJcsCommandStation().addSensorEventListener(p10);
      JCS.getJcsCommandStation().addSensorEventListener(p11);
      JCS.getJcsCommandStation().addSensorEventListener(p12);
      JCS.getJcsCommandStation().addSensorEventListener(p13);
      JCS.getJcsCommandStation().addSensorEventListener(p14);
      JCS.getJcsCommandStation().addSensorEventListener(p15);
      JCS.getJcsCommandStation().addSensorEventListener(p16);
    }
  }

  public void removeSensorListeners() {
    if (JCS.getJcsCommandStation() != null) {
      JCS.getJcsCommandStation().removeSensorEventListener(p1);
      JCS.getJcsCommandStation().removeSensorEventListener(p2);
      JCS.getJcsCommandStation().removeSensorEventListener(p3);
      JCS.getJcsCommandStation().removeSensorEventListener(p4);
      JCS.getJcsCommandStation().removeSensorEventListener(p5);
      JCS.getJcsCommandStation().removeSensorEventListener(p6);
      JCS.getJcsCommandStation().removeSensorEventListener(p7);
      JCS.getJcsCommandStation().removeSensorEventListener(p8);
      JCS.getJcsCommandStation().removeSensorEventListener(p9);
      JCS.getJcsCommandStation().removeSensorEventListener(p10);
      JCS.getJcsCommandStation().removeSensorEventListener(p11);
      JCS.getJcsCommandStation().removeSensorEventListener(p12);
      JCS.getJcsCommandStation().removeSensorEventListener(p13);
      JCS.getJcsCommandStation().removeSensorEventListener(p14);
      JCS.getJcsCommandStation().removeSensorEventListener(p15);
      JCS.getJcsCommandStation().removeSensorEventListener(p16);

      this.p1 = null;
      this.p2 = null;
      this.p3 = null;
      this.p4 = null;
      this.p5 = null;
      this.p6 = null;
      this.p7 = null;
      this.p8 = null;
      this.p9 = null;
      this.p10 = null;
      this.p11 = null;
      this.p12 = null;
      this.p13 = null;
      this.p14 = null;
      this.p15 = null;
      this.p16 = null;
    }
  }

  private static int calculateContactId(int module, int offset, int port) {
    //Bei einer CS2 errechnet sich der richtige Kontakt mit der Formel M - 1 * 16 + N
    module = module - 1;
    int contactId = module * 16;
    return contactId + port + offset;
  }

  public void setTitle(String title) {
    ((TitledBorder) this.getBorder()).setTitle(title);
  }

  public String getTitle() {
    return ((TitledBorder) this.getBorder()).getTitle();
  }

  public int getModuleNumber() {
    return moduleNumber;
  }

  public void setModuleNumber(int moduleNumber) {
    this.moduleNumber = moduleNumber;
  }

  public Integer getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(Integer deviceId) {
    this.deviceId = deviceId;
  }

  public Integer getContactIdOffset() {
    return contactIdOffset;
  }

  public void setContactIdOffset(Integer contactIdOffset) {
    this.contactIdOffset = contactIdOffset;
  }

  public Integer getStartContactId() {
    return startContactId;
  }

  public void setStartContactId(Integer startContactId) {
    this.startContactId = startContactId;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    
  }

  private class FeedbackPort implements SensorEventListener {

    private final JLabel label;
    private final Integer deviceId;
    private final Integer contactId;
    private boolean value;

    FeedbackPort(JLabel label, Integer deviceId, Integer contactId) {
      this.label = label;
      this.deviceId = deviceId;
      this.contactId = contactId;

      this.label.setToolTipText("DeviceId: " + this.deviceId + " ContactId: " + this.contactId);
    }

    @Override
    public void onSensorChange(SensorEvent event) {
      SensorBean sensor = event.getSensorBean();

      if (this.deviceId.equals(sensor.getDeviceId()) && this.contactId.equals(sensor.getContactId())) {
        this.value = sensor.isActive();
        this.label.setIcon(value ? ICON_ON : ICON_OFF);
        this.label.repaint();
      }
    }
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbl1 = new JLabel();
        lbl2 = new JLabel();
        lbl3 = new JLabel();
        lbl4 = new JLabel();
        lbl5 = new JLabel();
        lbl6 = new JLabel();
        lbl7 = new JLabel();
        lbl8 = new JLabel();
        lbl9 = new JLabel();
        lbl10 = new JLabel();
        lbl11 = new JLabel();
        lbl12 = new JLabel();
        lbl13 = new JLabel();
        lbl14 = new JLabel();
        lbl15 = new JLabel();
        lbl16 = new JLabel();

        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)), "S88 - Bus 0 Module 1"));
        setMaximumSize(new Dimension(250, 95));
        setMinimumSize(new Dimension(250, 95));
        setName("Form"); // NOI18N
        setPreferredSize(new Dimension(250, 95));
        setLayout(new GridLayout(2, 8));

        lbl1.setHorizontalAlignment(SwingConstants.CENTER);
        lbl1.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl1.setText("1");
        lbl1.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl1.setDoubleBuffered(true);
        lbl1.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl1.setName("lbl1"); // NOI18N
        add(lbl1);

        lbl2.setHorizontalAlignment(SwingConstants.CENTER);
        lbl2.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl2.setText("2");
        lbl2.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl2.setDoubleBuffered(true);
        lbl2.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl2.setName("lbl2"); // NOI18N
        add(lbl2);

        lbl3.setHorizontalAlignment(SwingConstants.CENTER);
        lbl3.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl3.setText("3");
        lbl3.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl3.setDoubleBuffered(true);
        lbl3.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl3.setName("lbl3"); // NOI18N
        add(lbl3);

        lbl4.setHorizontalAlignment(SwingConstants.CENTER);
        lbl4.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl4.setText("4");
        lbl4.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl4.setDoubleBuffered(true);
        lbl4.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl4.setName("lbl4"); // NOI18N
        add(lbl4);

        lbl5.setHorizontalAlignment(SwingConstants.CENTER);
        lbl5.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl5.setText("5");
        lbl5.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl5.setDoubleBuffered(true);
        lbl5.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl5.setName("lbl5"); // NOI18N
        add(lbl5);

        lbl6.setHorizontalAlignment(SwingConstants.CENTER);
        lbl6.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl6.setText("6");
        lbl6.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl6.setDoubleBuffered(true);
        lbl6.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl6.setName("lbl6"); // NOI18N
        add(lbl6);

        lbl7.setHorizontalAlignment(SwingConstants.CENTER);
        lbl7.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl7.setText("7");
        lbl7.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl7.setDoubleBuffered(true);
        lbl7.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl7.setName("lbl7"); // NOI18N
        add(lbl7);

        lbl8.setHorizontalAlignment(SwingConstants.CENTER);
        lbl8.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl8.setText("8");
        lbl8.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl8.setDoubleBuffered(true);
        lbl8.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl8.setName("lbl8"); // NOI18N
        add(lbl8);

        lbl9.setHorizontalAlignment(SwingConstants.CENTER);
        lbl9.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl9.setText("9");
        lbl9.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl9.setDoubleBuffered(true);
        lbl9.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl9.setName("lbl9"); // NOI18N
        lbl9.setVerifyInputWhenFocusTarget(false);
        add(lbl9);

        lbl10.setHorizontalAlignment(SwingConstants.CENTER);
        lbl10.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl10.setText("10");
        lbl10.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl10.setDoubleBuffered(true);
        lbl10.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl10.setName("lbl10"); // NOI18N
        add(lbl10);

        lbl11.setHorizontalAlignment(SwingConstants.CENTER);
        lbl11.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl11.setText("11");
        lbl11.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl11.setDoubleBuffered(true);
        lbl11.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl11.setName("lbl11"); // NOI18N
        add(lbl11);

        lbl12.setHorizontalAlignment(SwingConstants.CENTER);
        lbl12.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl12.setText("12");
        lbl12.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl12.setDoubleBuffered(true);
        lbl12.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl12.setName("lbl12"); // NOI18N
        add(lbl12);

        lbl13.setHorizontalAlignment(SwingConstants.CENTER);
        lbl13.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl13.setText("13");
        lbl13.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl13.setDoubleBuffered(true);
        lbl13.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl13.setName("lbl13"); // NOI18N
        add(lbl13);

        lbl14.setHorizontalAlignment(SwingConstants.CENTER);
        lbl14.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl14.setText("14");
        lbl14.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl14.setDoubleBuffered(true);
        lbl14.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl14.setName("lbl14"); // NOI18N
        add(lbl14);

        lbl15.setHorizontalAlignment(SwingConstants.CENTER);
        lbl15.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl15.setText("15");
        lbl15.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl15.setDoubleBuffered(true);
        lbl15.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl15.setName("lbl15"); // NOI18N
        add(lbl15);

        lbl16.setHorizontalAlignment(SwingConstants.CENTER);
        lbl16.setIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl16.setText("16");
        lbl16.setDisabledIcon(new ImageIcon(getClass().getResource("/media/Button-Grey-20px.png"))); // NOI18N
        lbl16.setDoubleBuffered(true);
        lbl16.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl16.setName("lbl16"); // NOI18N
        add(lbl16);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel lbl1;
    private JLabel lbl10;
    private JLabel lbl11;
    private JLabel lbl12;
    private JLabel lbl13;
    private JLabel lbl14;
    private JLabel lbl15;
    private JLabel lbl16;
    private JLabel lbl2;
    private JLabel lbl3;
    private JLabel lbl4;
    private JLabel lbl5;
    private JLabel lbl6;
    private JLabel lbl7;
    private JLabel lbl8;
    private JLabel lbl9;
    // End of variables declaration//GEN-END:variables

}
