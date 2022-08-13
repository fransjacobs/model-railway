/*
 * Copyright (C) 2018 Frans Jacobs.
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
package jcs;

import jcs.ui.util.ProcessFactory;
import java.awt.GraphicsEnvironment;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.trackservice.TrackService;
import jcs.trackservice.TrackServiceFactory;
import jcs.ui.JCSFrame;
import jcs.ui.splash.JCSSplash;
import jcs.ui.util.MacOsAdapter;
import jcs.util.RunUtil;
import jcs.util.VersionInfo;
import org.apache.commons.lang3.SystemUtils;
import org.tinylog.Logger;

/**
 * Single Locomotive Control This is the run time start point for this
 * application. It first figures out what to do (local serial port, act as a
 * server etc)
 *
 * @author frans
 *
 */
public class JCS extends Thread {

    private static JCS instance = null;
    private static JCSSplash splashScreen;
    private static TrackService trackService;

    private static MacOsAdapter osAdapter;
    private static JCSFrame jcsFrame;
    private static VersionInfo versionInfo;

    private JCS() {
        versionInfo = new VersionInfo(JCS.class, "jcs", "ui");
    }

    public static void logProgress(String message) {
        if (splashScreen != null) {
            splashScreen.logProgress(message);
        } else {
            Logger.info(message);
        }
    }

    public static void showTouchbar(JCSFrame frame) {
        if (SystemUtils.IS_OS_MAC_OSX) {
            osAdapter.showTouchbar(frame);
        }
    }

    public static VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public static JCSFrame getJCSFrame() {
        return jcsFrame;
    }

    private void initGui() {
        JCS.logProgress("Starting GUI...");
        
        //Also see https://www.formdev.com/flatlaf/

        try {
            if (SystemUtils.IS_OS_MAC) {
                MacOsAdapter.setMacOsProperties();
                osAdapter = new MacOsAdapter();
            } else {
                String plaf = System.getProperty("jcs.plaf");
                if (plaf != null) {
                    UIManager.setLookAndFeel(plaf);
                } else {
                    //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.error(ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            jcsFrame = new JCSFrame();

            if (SystemUtils.IS_OS_MAC_OSX) {
                osAdapter.setUiCallback(jcsFrame);
            }

            jcsFrame.pack();
            jcsFrame.setLocationRelativeTo(null);
            jcsFrame.setVisible(true);
        });

        JCS.logProgress("JCS started...");

        int mb = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();

        StringBuilder sb = new StringBuilder();
        sb.append("Used Memory: ");
        sb.append((runtime.totalMemory() - runtime.freeMemory()) / mb);
        sb.append(" [MB]. Free Memory: ");
        sb.append(runtime.freeMemory() / mb);
        sb.append(" [MB]. Available Memory: ");
        sb.append(runtime.totalMemory() / mb);
        sb.append(" [MB]. Max Memory: ");
        sb.append(runtime.maxMemory() / mb);
        sb.append(" [MB].");

        Logger.info(sb);
        splashScreen.hideSplash();
        splashScreen.close();
    }

    /**
     * Executed at shutdown in response to a Ctrl-C etc.
     */
    @Override
    public void run() {
        // Perform shutdown methods.
        Logger.trace("Shutting Down...");
        ProcessFactory.getInstance().shutdown();
        Logger.info("Finished...");
    }

    public static JCS getInstance() {
        if (instance == null) {
            instance = new JCS();
            // Prepare for shutdown...
            Runtime.getRuntime().addShutdownHook(instance);
        }
        return instance;
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            Logger.error("This JDK environment is headless, can't start a GUI!");
            //Quit....
            System.exit(1);
        }
        splashScreen = new JCSSplash();
        splashScreen.showSplash();
        splashScreen.setProgressMax(10);

        RunUtil.loadProperties();

        logProgress("Starting...");
        trackService = TrackServiceFactory.getTrackService();

        if (trackService != null) {
            JCS jcs = JCS.getInstance();
            jcs.initGui();
        } else {
            Logger.error("Could not obtain a TrackService. Quitting....");
            logProgress("Error. Can't Obtain a Track Service!");

            splashScreen.hideSplash();
            splashScreen.close();
            System.exit(0);
        }

    }

}
