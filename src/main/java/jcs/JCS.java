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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.trackservice.TrackService;
import jcs.trackservice.TrackServiceFactory;
import jcs.ui.JCSFrame;
import jcs.ui.Refreshable;
import jcs.ui.splash.JCSSplashScreen;
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

    private static boolean headless;
    private static JCSSplashScreen splashScreen;

    private static TrackService trackService;

    private static JCS instance = null;

    private static MacOsAdapter osAdapter;
    private static JCSFrame jcsFrame;

    private static VersionInfo versionInfo;

    private final List<Refreshable> refreshables;

    private JCS() {
        refreshables = new ArrayList<>();
        headless = GraphicsEnvironment.isHeadless();
        versionInfo = new VersionInfo(JCS.class, "lan.wervel.jcs", "gui");

        updateProgress();
    }

    public static void main(String[] args) {
        if (headless) {
            Logger.error("This JDK environment is headless, can't start a GUI!");
            //Quit....
            System.exit(1);
        }
        RunUtil.loadProperties();

        try {
            if (SystemUtils.IS_OS_MAC) {
                MacOsAdapter.setMacOsProperties();
                osAdapter = new MacOsAdapter();
            } else {
                String plaf = System.getProperty("jcs.plaf");
                if (plaf != null) {
                    UIManager.setLookAndFeel(plaf);
                } else {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.error(ex);
        }

        splashScreen = new JCSSplashScreen();
        splashScreen.showSplash();
        splashScreen.setProgressMax(10);
        trackService = TrackServiceFactory.getTrackService();
        if (trackService != null) {
            JCS jcs = JCS.getInstance();
            jcs.init();
        } else {
            Logger.error("Could not obtain a TrackService. Quitting....");
            splashScreen.hideSplash();
            splashScreen.close();
            System.exit(0);
        }
    }

    public static void updateProgress() {
        if (splashScreen != null) {
            splashScreen.updateProgress();
        }
    }

    public static boolean isMacOS() {
        return SystemUtils.IS_OS_MAC_OSX;
    }

    public static void showTouchbar(JCSFrame frame) {
        if (SystemUtils.IS_OS_MAC_OSX) {
            osAdapter.showTouchbar(frame);
        }
    }

    public static boolean isHeadless() {
        return headless;
    }

    public static VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public static JCSFrame getJCSFrame() {
        return jcsFrame;
    }

    public static void addRefreshable(Refreshable refreshable) {
        if (instance != null) {
            instance.refreshables.add(refreshable);
        }
    }

    public static List<Refreshable> getRefreshables() {
        return instance.refreshables;
    }

    private void init() {
        updateProgress();

        java.awt.EventQueue.invokeLater(() -> {
            jcsFrame = new JCSFrame();
            updateProgress();

            if (SystemUtils.IS_OS_MAC_OSX) {
                osAdapter.setUiCallback(jcsFrame);
            }

            jcsFrame.pack();
            jcsFrame.setLocationRelativeTo(null);
            jcsFrame.setVisible(true);
        });

        Logger.info("JCSGUI started...");
        splashScreen.hideSplash();

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
    }

    /**
     * Executed at shutdown in response to a Ctrl-C etc.
     */
    @Override
    public void run() {
        // Perform shutdown methods.
        Logger.debug("Shutting Down...");
        ProcessFactory.getInstance().shutdown();
        //ServicePublisher.stopPublisher();
        Logger.info("Finished...");
    }

    /**
     * Gets us in the singleton pattern.
     *
     * @return Us
     */
    public static JCS getInstance() {
        if (instance == null) {
            instance = new JCS();
            // Prepare for shutdown...
            Runtime.getRuntime().addShutdownHook(instance);
        }
        return instance;
    }
}
