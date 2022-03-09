/*
 * Copyright (C) 2019 Frans Jacobs.
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
package jcs.ui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import jcs.entities.SensorBean;
import jcs.trackservice.TrackServiceFactory;
import jcs.trackservice.events.SensorListener;

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

    /**
     * Create default FeedbackPanel for module number 1
     */
    public FeedbackPanel() {
        this(1);
    }

    /**
     * Create FeedbackPanel for given moduleNumber
     *
     * @param moduleNumber
     */
    public FeedbackPanel(int moduleNumber) {
        this.moduleNumber = moduleNumber;
        initComponents();

        if (TrackServiceFactory.getTrackService() != null) {
            registerForRefresh();
        }
    }

    private void registerForRefresh() {
        if (TrackServiceFactory.getTrackService() != null) {
            FeedbackPort p1 = new FeedbackPort(this.lbl1, SensorBean.calculateContactId(moduleNumber, 1));
            TrackServiceFactory.getTrackService().addSensorListener(p1);
            FeedbackPort p2 = new FeedbackPort(this.lbl2, SensorBean.calculateContactId(moduleNumber, 2));
            TrackServiceFactory.getTrackService().addSensorListener(p2);
            FeedbackPort p3 = new FeedbackPort(this.lbl3, SensorBean.calculateContactId(moduleNumber, 3));
            TrackServiceFactory.getTrackService().addSensorListener(p3);
            FeedbackPort p4 = new FeedbackPort(this.lbl4, SensorBean.calculateContactId(moduleNumber, 4));
            TrackServiceFactory.getTrackService().addSensorListener(p4);
            FeedbackPort p5 = new FeedbackPort(this.lbl5, SensorBean.calculateContactId(moduleNumber, 5));
            TrackServiceFactory.getTrackService().addSensorListener(p5);
            FeedbackPort p6 = new FeedbackPort(this.lbl6, SensorBean.calculateContactId(moduleNumber, 6));
            TrackServiceFactory.getTrackService().addSensorListener(p6);
            FeedbackPort p7 = new FeedbackPort(this.lbl7, SensorBean.calculateContactId(moduleNumber, 7));
            TrackServiceFactory.getTrackService().addSensorListener(p7);
            FeedbackPort p8 = new FeedbackPort(this.lbl8, SensorBean.calculateContactId(moduleNumber, 8));
            TrackServiceFactory.getTrackService().addSensorListener(p8);
            FeedbackPort p9 = new FeedbackPort(this.lbl9, SensorBean.calculateContactId(moduleNumber, 9));
            TrackServiceFactory.getTrackService().addSensorListener(p9);
            FeedbackPort p10 = new FeedbackPort(this.lbl10, SensorBean.calculateContactId(moduleNumber, 10));
            TrackServiceFactory.getTrackService().addSensorListener(p10);
            FeedbackPort p11 = new FeedbackPort(this.lbl11, SensorBean.calculateContactId(moduleNumber, 11));
            TrackServiceFactory.getTrackService().addSensorListener(p11);
            FeedbackPort p12 = new FeedbackPort(this.lbl12, SensorBean.calculateContactId(moduleNumber, 12));
            TrackServiceFactory.getTrackService().addSensorListener(p12);
            FeedbackPort p13 = new FeedbackPort(this.lbl13, SensorBean.calculateContactId(moduleNumber, 13));
            TrackServiceFactory.getTrackService().addSensorListener(p13);
            FeedbackPort p14 = new FeedbackPort(this.lbl14, SensorBean.calculateContactId(moduleNumber, 14));
            TrackServiceFactory.getTrackService().addSensorListener(p14);
            FeedbackPort p15 = new FeedbackPort(this.lbl15, SensorBean.calculateContactId(moduleNumber, 15));
            TrackServiceFactory.getTrackService().addSensorListener(p15);
            FeedbackPort p16 = new FeedbackPort(this.lbl16, SensorBean.calculateContactId(moduleNumber, 16));
            TrackServiceFactory.getTrackService().addSensorListener(p16);

            TrackServiceFactory.getTrackService().notifyAllSensorListeners();
        }
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

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    private class FeedbackPort implements SensorListener {

        private final JLabel label;
        private final Integer contactId;
        private boolean value;

        FeedbackPort(JLabel label, Integer contactId) {
            this.label = label;
            this.contactId = contactId;
        }

        @Override
        public void onChange(SensorBean sensor) {
        }
        
        

        public Integer getContactId() {
            return contactId;
        }

        public void setActive(boolean newValue) {
            value = newValue;
            label.setIcon(value ? ICON_ON : ICON_OFF);
            repaint();
        }
    }

    public static void main(String args[]) {
        //Configurator.defaultConfig().level(org.pmw.tinylog.Level.TRACE).activate();
        JFrame f = new JFrame("FeedbackPanel Tester");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        FeedbackPanel feedbackPanel = new FeedbackPanel(2);

        f.add(feedbackPanel);

        f.pack();
        f.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
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

        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)), "S88 - 1"));
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
