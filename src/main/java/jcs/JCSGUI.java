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
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.trackservice.TrackService;
import jcs.trackservice.TrackServiceFactory;
import jcs.ui.JCSFrame;
import jcs.ui.Refreshable;
import jcs.ui.splash.JCSSplashScreen;
import jcs.ui.util.MacOsAdapter;
import jcs.util.VersionInfo;
import org.tinylog.Logger;

/**
 * Single Locomotive Control This is the run time start point for this
 * application. It first figures out what to do (local serial port, act as a
 * server etc)
 *
 * @author frans
 *
 */
public class JCSGUI extends Thread {

    private static boolean headless;
    private static JCSSplashScreen splashScreen;

    private static TrackService trackService;

    private static JCSGUI instance = null;

    private static MacOsAdapter osAdapter;
    private static JCSFrame jcsFrame;

    private static boolean macOS = "Mac OS X".equals(System.getProperty("os.name"));
    private static VersionInfo versionInfo;

    private final List<Refreshable> refreshables;

    private JCSGUI() {
        refreshables = new ArrayList<>();
        headless = GraphicsEnvironment.isHeadless();
        macOS = "Mac OS X".equals(System.getProperty("os.name"));
        versionInfo = new VersionInfo(JCSGUI.class, "lan.wervel.jcs", "gui");

        updateProgress();
    }

    public static void main(String[] args) {
        System.setProperty("name", "Java Command Station");
        System.setProperty("useOnlyRemote", "true");
        System.setProperty("timeoutMillis", "2000");
        System.setProperty("retryCount", "1");

        if (headless) {
            System.setProperty("java.awt.headless", "true");
            Logger.error("This JDK environment is headless, can't start a GUI!");
            //Quit....
            System.exit(1);
        }

        try {
            if (macOS) {
                MacOsAdapter.setMacOsProperties();
                osAdapter = new MacOsAdapter();
            } else {
                //UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                //UIManager.setLookAndFeel("com.formdev.NimbusLookAndFeel");
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.error(ex);
        }

        splashScreen = new JCSSplashScreen();
        splashScreen.showSplash();
        splashScreen.setProgressMax(10);

        trackService = TrackServiceFactory.getTrackService();

//    if (trackService == null) {
//      //start a new track server in a jvm
//      Logger.debug("Start new JVM procees with Track Server");
//      updateProgress();
//      ServiceInfo tsi = startTrackServerJVM();
//
//      if (tsi != null) {
//        Logger.debug("Trackserver has initialized: " + tsi);
//        trackService = TrackServiceFactory.getTrackService(splashScreen);
//      }
//    }
        if (trackService != null) {
            JCSGUI jcs = JCSGUI.getInstance();
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
        return macOS;
    }

    public static void showTouchbar(JCSFrame frame) {
        if (macOS) {
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

//  private static ServiceInfo startTrackServerJVM() {
//    ServiceInfo si = null;
//    Logger.debug("Starting a TrackServer Process...");
//
//    String jvmOptions = "-Xms128m -Xmx256m -Dcallback=true";
//    String mainClass = "lan.wervel.jcs.JCSServer";
//    String[] arguments = new String[]{"callback=" + TrackService.SERVICE_TYPE};
//
//    Process p = ProcessFactory.getInstance().startJVMProcess(jvmOptions, mainClass, arguments);
//
//    if (p != null) {
//      Logger.info("Started TrackServer Process " + p.toString());
//      //Wait for the initialization to finish
//      si = DiscoveryClient.waitForProcessCallback(TrackService.SERVICE_TYPE);
//
//      if (si != null) {
//        Logger.debug("Trackserver has initialized: " + si);
//        updateProgress();
//        ServiceInfo dsi = DiscoveryClient.discover(TrackServiceFactory.createClientInfo(), 5000L, 3);
//
//        Logger.debug("Discovered: " + dsi);
//        si = dsi;
//
//        updateProgress();
//        trackService = TrackServiceFactory.getRemoteTrackService(si);
//
//      } else {
//        Logger.error("Could not obtain a TrackService ServiceInfo object");
//      }
//    } else {
//      Logger.error("Could not start TrackServer process");
//    }
//    return si;
//  }
    private void init() {
        //Logger.debug("Starting UI. Connected with: " + trackService.getServiceInfo().getIp() + "...");
        updateProgress();

        java.awt.EventQueue.invokeLater(() -> {
            jcsFrame = new JCSFrame();
            updateProgress();

            if (macOS) {
                osAdapter.setUiCallback(jcsFrame);
            }

            jcsFrame.setVisible(true);
            jcsFrame.setPreferredSize(new Dimension(1310, 650));
            // X, Y, W, H
            jcsFrame.setBounds(100, 100, 1310, 650);
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
    public static JCSGUI getInstance() {
        if (instance == null) {
            instance = new JCSGUI();
            // Prepare for shutdown...
            Runtime.getRuntime().addShutdownHook(instance);
        }
        return instance;
    }
}
