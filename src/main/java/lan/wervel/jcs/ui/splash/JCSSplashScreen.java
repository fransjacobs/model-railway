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
package lan.wervel.jcs.ui.splash;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import lan.wervel.jcs.util.ProgressUpdater;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class JCSSplashScreen extends JWindow implements Runnable, ProgressUpdater {

    private boolean running;
    private static Thread splashThread;

    public static final int MAX_SPLASH_TIME = 60000;
    private final JLabel imageLabel;

    private final BorderLayout borderLayout;
    private final JPanel southPanel;
    private final FlowLayout southPanelFlowLayout;
    private final JProgressBar progressBar;
    private final ImageIcon imageIcon;

    private int progress;

    public JCSSplashScreen() {
        super();
        borderLayout = new BorderLayout();
        southPanel = new JPanel();
        southPanelFlowLayout = new FlowLayout();
        progressBar = new JProgressBar();
        imageIcon = new ImageIcon(JCSSplashScreen.class.getResource("/media/SteamTrain.png"));
        imageLabel = new JLabel();
        imageLabel.setIcon(imageIcon);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        imageLabel.setName("imageLabel");
        imageLabel.setOpaque(true);

        init();
    }

    private void init() {
        Logger.trace("Splashscreen starting...");
        imageLabel.setIcon(imageIcon);
        this.getContentPane().setLayout(borderLayout);
        southPanel.setLayout(southPanelFlowLayout);
        southPanel.setBackground(Color.BLACK);
        this.getContentPane().add(imageLabel, BorderLayout.CENTER);
        this.getContentPane().add(southPanel, BorderLayout.SOUTH);
        southPanel.add(progressBar, null);

        getContentPane().add(imageLabel, BorderLayout.CENTER);

        setAlwaysOnTop(true);

        Dimension screenSize
                = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = imageLabel.getPreferredSize();
        setLocation(screenSize.width / 2 - (labelSize.width / 2),
                screenSize.height / 2 - (labelSize.height / 2));
        pack();
        running = true;
    }

    public void setProgressMax(int maxProgress) {
        progressBar.setMaximum(maxProgress);
    }

    @Override
    public void updateProgress() {
        progress++;
        setProgress(progress);
    }

    public void setProgress(int progress) {
        final int theProgress = progress;
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(theProgress);
        });
    }

    public void setProgress(int progress, String message) {
        final int theProgress = progress;
        final String theMessage = message;
        setProgress(progress);
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(theProgress);
            setMessage(theMessage);
        });
    }

    private void setMessage(String message) {
        if (message == null) {
            message = "";
            progressBar.setStringPainted(false);
        } else {
            progressBar.setStringPainted(true);
        }
        progressBar.setString(message);
    }

    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            Logger.trace(ie);
        }
    }

    @Override
    public void run() {
        this.setVisible(true);

        long start = System.currentTimeMillis();

        while (running) {
            sleep();

            //check against the max time
            long dur = System.currentTimeMillis() - start;
            if (dur > MAX_SPLASH_TIME) {
                running = false;
            }
        }

        this.dispose();
        Logger.debug("Total progress steps: " + this.progress + " Splashscreen finished...");
    }

    public void close() {
        running = false;
    }

    public void showSplash() {
        splashThread = new Thread(this);
        splashThread.start();
    }

    public void hideSplash() {
        this.running = false;
    }
}
