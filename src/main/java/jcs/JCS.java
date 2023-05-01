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
package jcs;

import jcs.ui.util.ProcessFactory;
import java.awt.GraphicsEnvironment;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.PersistenceService;
import jcs.persistence.util.H2DatabaseUtil;
import jcs.ui.JCSFrame;
import jcs.ui.splash.JCSSplash;
import jcs.ui.util.MacOsAdapter;
import jcs.util.RunUtil;
import jcs.util.VersionInfo;
import org.apache.commons.lang3.SystemUtils;
import org.tinylog.Logger;

/**
 *
 * JCS. This is the run time start point for the application.
 *
 */
public class JCS extends Thread {

  private static JCS instance = null;
  private static JCSSplash splashScreen;
  private static PersistenceService persistentStore;

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

  private static boolean isMacOSX() {
    return System.getProperty("os.name").contains("Mac OS X");
  }

  private void startGui() {
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
    //Check the persistent properties prepare environment
    if (H2DatabaseUtil.databaseFileExists(false)) {
      //Database files are there so try to create connection
    } else {
      //No Database file so maybe first start lets creat one
      H2DatabaseUtil.createDatabaseUsers(false);
      H2DatabaseUtil.createDatabase();
    }

    persistentStore = PersistenceFactory.getService();

    if (persistentStore != null) {
      JCS jcs = JCS.getInstance();

      jcs.startGui();
    } else {
      Logger.error("Could not obtain a TrackService. Quitting....");
      logProgress("Error. Can't Obtain a Track Service!");

      splashScreen.hideSplash();
      splashScreen.close();
      System.exit(0);
    }
  }
}
