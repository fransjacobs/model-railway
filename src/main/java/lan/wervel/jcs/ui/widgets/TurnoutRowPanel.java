/*
 * Copyright (C) 2019 Frans Jacobs <frans.jacobs@gmail.com>.
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
package lan.wervel.jcs.ui.widgets;

import java.awt.GridLayout;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import lan.wervel.jcs.entities.Turnout;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.trackservice.AccessoryEvent;
import lan.wervel.jcs.trackservice.TrackServiceFactory;
import lan.wervel.jcs.trackservice.events.AccessoryListener;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;

/**
 *
 * @author Frans Jacobs <frans.jacobs@gmail.com>
 */
public class TurnoutRowPanel extends JPanel implements AccessoryListener {

    private Turnout turnout;

    private static final String TURNOUT_L = "/media/turnout-l.png";
    private static final String TURNOUT_L_S = "/media/turnout-l-s.png";
    private static final String TURNOUT_L_C = "/media/turnout-l-c.png";

    private static final String TURNOUT_R = "/media/turnout-r.png";
    private static final String TURNOUT_R_S = "/media/turnout-r-s.png";
    private static final String TURNOUT_R_C = "/media/turnout-r-c.png";

    private static final String TURNOUT_X = "/media/turnout-x.png";
    private static final String TURNOUT_X_S = "/media/turnout-x-s.png";
    private static final String TURNOUT_X_C = "/media/turnout-x-c.png";

    public static final int X_AXIS = BoxLayout.X_AXIS;
    public static final int Y_AXIS = BoxLayout.Y_AXIS;

    private final int axis;

    public TurnoutRowPanel() {
        this(null);
    }

    public TurnoutRowPanel(Turnout turnout) {
        this(turnout, X_AXIS);
    }

    /**
     * Creates new form TurnoutRowPanel
     *
     * @param turnout
     * @param axis
     */
    public TurnoutRowPanel(Turnout turnout, int axis) {
        this.turnout = turnout;
        this.axis = axis;

        initComponents();

        postInit();
    }

    private void postInit() {
        setLayout(new javax.swing.BoxLayout(this, axis));

        if (Y_AXIS == this.axis) {
            setMinimumSize(new java.awt.Dimension(55, 175));
            setPreferredSize(new java.awt.Dimension(55, 175));
        }
        if (turnout != null) {
            setButtonImages(turnout.getDescription());
            rowLbl.setText(turnout.getName());
            setButtonStatus();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    signalBG = new javax.swing.ButtonGroup();
    rowLbl = new javax.swing.JLabel();
    btnStraight = new javax.swing.JToggleButton();
    btnCurved = new javax.swing.JToggleButton();

    setMinimumSize(new java.awt.Dimension(175, 55));
    setPreferredSize(new java.awt.Dimension(175, 55));
    setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));

    rowLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    rowLbl.setLabelFor(this);
    rowLbl.setText("T nn");
    rowLbl.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    rowLbl.setMaximumSize(new java.awt.Dimension(60, 16));
    rowLbl.setMinimumSize(new java.awt.Dimension(60, 16));
    rowLbl.setPreferredSize(new java.awt.Dimension(60, 16));
    add(rowLbl);

    signalBG.add(btnStraight);
    btnStraight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/turnout-l.png"))); // NOI18N
    btnStraight.setToolTipText("Hp0");
    btnStraight.setMargin(new java.awt.Insets(1, 1, 1, 1));
    btnStraight.setMaximumSize(new java.awt.Dimension(55, 55));
    btnStraight.setPreferredSize(new java.awt.Dimension(55, 55));
    btnStraight.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/media/turnout-l-s.png"))); // NOI18N
    btnStraight.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnStraightActionPerformed(evt);
      }
    });
    add(btnStraight);

    signalBG.add(btnCurved);
    btnCurved.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/turnout-r.png"))); // NOI18N
    btnCurved.setToolTipText("Hp1");
    btnCurved.setMargin(new java.awt.Insets(1, 1, 1, 1));
    btnCurved.setMaximumSize(new java.awt.Dimension(55, 55));
    btnCurved.setPreferredSize(new java.awt.Dimension(55, 55));
    btnCurved.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/media/turnout-r-c.png"))); // NOI18N
    btnCurved.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnCurvedActionPerformed(evt);
      }
    });
    add(btnCurved);
  }// </editor-fold>//GEN-END:initComponents

  private void btnStraightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStraightActionPerformed
      switchTurnout(this.btnStraight.isSelected() ? AccessoryValue.GREEN : AccessoryValue.OFF);
  }//GEN-LAST:event_btnStraightActionPerformed

  private void btnCurvedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCurvedActionPerformed
      switchTurnout(this.btnCurved.isSelected() ? AccessoryValue.RED : AccessoryValue.OFF);
  }//GEN-LAST:event_btnCurvedActionPerformed

    private void setButtonImages(String type) {
        switch (type) {
            case "X":
                btnStraight.setIcon(new ImageIcon(getClass().getResource(TURNOUT_X)));
                btnStraight.setSelectedIcon(new ImageIcon(getClass().getResource(TURNOUT_X_S)));

                btnCurved.setIcon(new ImageIcon(getClass().getResource(TURNOUT_X)));
                btnCurved.setSelectedIcon(new ImageIcon(getClass().getResource(TURNOUT_X_C)));
                break;
            case "L":
                btnStraight.setIcon(new ImageIcon(getClass().getResource(TURNOUT_L)));
                btnStraight.setSelectedIcon(new ImageIcon(getClass().getResource(TURNOUT_L_S)));

                btnCurved.setIcon(new ImageIcon(getClass().getResource(TURNOUT_L)));
                btnCurved.setSelectedIcon(new ImageIcon(getClass().getResource(TURNOUT_L_C)));
                break;
            default:
                btnStraight.setIcon(new ImageIcon(getClass().getResource(TURNOUT_R)));
                btnStraight.setSelectedIcon(new ImageIcon(getClass().getResource(TURNOUT_R_S)));

                btnCurved.setIcon(new ImageIcon(getClass().getResource(TURNOUT_R)));
                btnCurved.setSelectedIcon(new ImageIcon(getClass().getResource(TURNOUT_R_C)));
                break;
        }
    }

    public Turnout getTurnout() {
        return turnout;
    }

    public void setTurnout(Turnout turnout) {
        this.turnout = turnout;

        if (turnout != null) {
            //setButtonImages(turnout.getLightImages(), turnout.getDescription());
            rowLbl.setText(turnout.getName());
            setButtonStatus();
        }
    }

    @Override
    public void switched(AccessoryEvent event) {
        if (event.isEventFor(turnout)) {
            this.turnout.setValue(event.getValue());
        }
        setButtonStatus();
    }

    private void switchTurnout(AccessoryValue value) {
        Logger.trace("Setting Value: " + value);
        if (this.turnout != null) {
            switch (value) {
                case RED:
                    TrackServiceFactory.getTrackService().switchAccessory(AccessoryValue.RED, turnout, false);
                    break;
                case GREEN:
                    TrackServiceFactory.getTrackService().switchAccessory(AccessoryValue.GREEN, turnout, false);
                    break;
                default:
                    break;
            }
        }
    }

    private void setButtonStatus() {
        if (this.turnout != null) {
            //Logger.trace("Turnout: " + turnout);

            switch (turnout.getValue()) {
                case RED:
                    this.btnCurved.setSelected(true);
                    //Logger.trace("Button Curved: selected -> true.");
                    break;
                case GREEN:
                    this.btnStraight.setSelected(true);
                    //Logger.trace("Button Straight: selected -> true.");
                    break;
                default:
                    Logger.trace("Default called; Value: " + turnout.getValue());
                    break;
            }
        }
    }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JToggleButton btnCurved;
  private javax.swing.JToggleButton btnStraight;
  private javax.swing.JLabel rowLbl;
  private javax.swing.ButtonGroup signalBG;
  // End of variables declaration//GEN-END:variables

    public static void main(String args[]) {
        Configurator.defaultConfig().level(org.pmw.tinylog.Level.TRACE).activate();

        JFrame f = new JFrame("SignalRowPanel Tester");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        List<Turnout> turnouts = TrackServiceFactory.getTrackService().getTurnouts();
        f.setLayout(new GridLayout(turnouts.size(), 1));

        for (Turnout turnout : turnouts) {
            TurnoutRowPanel signalRowPanel = new TurnoutRowPanel(turnout);
            f.add(signalRowPanel);

            TrackServiceFactory.getTrackService().addAccessoiryListener(signalRowPanel);

        }

        f.pack();
        f.setVisible(true);
    }

}
