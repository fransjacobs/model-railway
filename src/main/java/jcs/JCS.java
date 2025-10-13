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

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.Taskbar;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.commandStation.JCSCommandStation;
import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.events.PowerEventListener;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.PersistenceService;
import jcs.persistence.util.H2DatabaseUtil;
import jcs.ui.JCSFrame;
import jcs.ui.splash.JCSSplash;
import jcs.ui.util.FrameMonitor;
import jcs.ui.util.ProcessFactory;
import jcs.ui.util.UICallback;
import jcs.util.RunUtil;
import jcs.util.VersionInfo;
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
  private static JCSCommandStation jcsCommandStation;

  private static JCSFrame jcsFrame;
  private static String version;

  private static UICallback uiCallback;

  private JCS() {
  }

  public static void logProgress(String message) {
    if (splashScreen != null) {
      splashScreen.logProgress(message);
    } else {
      Logger.info(message);
    }
  }

  public static JCSFrame getParentFrame() {
    return jcsFrame;
  }

  public static PersistenceService getPersistenceService() {
    if (persistentStore == null) {
      persistentStore = PersistenceFactory.getService();
    }
    return persistentStore;
  }

  public static JCSCommandStation getJcsCommandStation() {
    if (jcsCommandStation == null) {
      if (getPersistenceService() != null) {
        jcsCommandStation = new JCSCommandStation();
      } else {
        Logger.error("Can't obtain the persistent store!");
      }
    }
    return jcsCommandStation;
  }

  /**
   * Executed at shutdown in response to a Ctrl-C etc.
   */
  @Override
  public void run() {
    // Perform shutdown methods.
    Thread.currentThread().setName("JCS finalize thread");
    Logger.trace("Shutting Down...");
    ProcessFactory.getInstance().shutdown();
    Logger.info("JCS " + VersionInfo.getVersion() + " session finished");
  }

  public static JCS getInstance() {
    if (instance == null) {
      instance = new JCS();

      // Prepare for shutdown...
      Runtime.getRuntime().addShutdownHook(instance);
    }
    return instance;
  }

  private static boolean lockAppInstance() {
    try {
      String lockFilePath = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "jcs.lock";

      final File file = new File(lockFilePath);
      if (file.createNewFile()) {
        file.deleteOnExit();
        return true;
      }
      return false;
    } catch (IOException e) {
      return false;
    }
  }

  public static void main(String[] args) {
    System.setProperty("fazecast.jSerialComm.appid", "JCS");
    version = VersionInfo.getVersion();
    Logger.info("Starting JCS Version " + version + "...");

    if (GraphicsEnvironment.isHeadless()) {
      Logger.error("This JDK environment is headless, can't start a GUI!");
      //Quit....
      System.exit(1);
    }

    //Load properties
    RunUtil.loadProperties();
    RunUtil.loadExternalProperties();

    try {
      String plaf = System.getProperty("jcs.plaf", "com.formdev.flatlaf.FlatLightLaf");
      if (plaf != null) {
        UIManager.setLookAndFeel(plaf);
      } else {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error(ex);
    }

    if (!lockAppInstance()) {
      Logger.warn("Can not obtain a lock. Check if an other instance of JCS is running");
      JOptionPane.showMessageDialog(new JFrame(), "There is another instance of JCS running.", "JCS allready running", JOptionPane.INFORMATION_MESSAGE, null);
      System.exit(0);
    }

    if (RunUtil.isMacOSX()) {
      System.setProperty("apple.awt.application.name", "JCS");
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("apple.awt.application.appearance", "system");
    }

    splashScreen = new JCSSplash();

    if ("true".equalsIgnoreCase(System.getProperty("disable.splash", "false"))) {
      Logger.info("Splasscreen is disabled");
    } else {
      splashScreen.showSplash();
    }

    splashScreen.setProgressMax(25);

    logProgress("JCS is Starting...");

    //Check the persistent properties, prepare environment
    if (!H2DatabaseUtil.databaseFileExists()) {
      //No Database file so maybe first start lets create one
      logProgress("Create new Database...");
      H2DatabaseUtil.createDatabaseUsers();
      H2DatabaseUtil.createDatabase();
    }

    //Database file exist check whether an update is needed
    String dbVersion = H2DatabaseUtil.getDataBaseVersion();

    if (!H2DatabaseUtil.DB_VERSION.equals(dbVersion)) {
      Logger.trace("Current DB Version " + dbVersion + " need to be updated to: " + H2DatabaseUtil.DB_VERSION + "...");
      logProgress("Updating JCS Database to version " + H2DatabaseUtil.DB_VERSION + "...");
      dbVersion = H2DatabaseUtil.updateDatabase();
    }

    logProgress("Connecting to existing Database version " + dbVersion + "...");

    logProgress("Starting JCS Command Station...");
    persistentStore = getPersistenceService();
    jcsCommandStation = getJcsCommandStation();

    if (persistentStore != null) {
      if ("true".equalsIgnoreCase(System.getProperty("commandStation.autoconnect", "false"))) {
        if (jcsCommandStation != null) {
          boolean connected = jcsCommandStation.connectInBackground();
          if (connected) {
            logProgress("Connected with Command Station...");

            boolean power = jcsCommandStation.isPowerOn();
            logProgress("Track Power is " + (power ? "on" : "off"));
            Logger.info("Track Power is " + (power ? "on" : "off"));
            jcsCommandStation.addPowerEventListener(new JCS.Powerlistener());
          } else {
            logProgress("Could NOT connect with Command Station...");
          }
        } else {
          logProgress("NO Default Command Station found...");
        }
      }

      logProgress("Starting UI...");

      JCS jcs = JCS.getInstance();

      jcs.startGui();

      //check the connection to the command station
      if (!JCS.getJcsCommandStation().isConnected()) {
        Logger.info("Not connected to command station...");
        //JCS.getJcsCommandStation().connectInBackground();
      }

    } else {
      Logger.error("Could not obtain a Persistent store. Quitting....");
      logProgress("Error! Can't Obtain a Persistent store!");
      splashScreen.hideSplash(500);
      splashScreen.close();
      System.exit(0);
    }
  }

  //TODO
  private static void shutdown() {

    System.gc();
    if (RunUtil.isMacOSX()) {
      for (Thread t : Thread.getAllStackTraces().keySet()) {
        if (t.getName().startsWith("AWT-")) {
          t.interrupt();
        }
      }
    }
    Thread.currentThread().interrupt();

  }

  private void startGui() {
    JCS.logProgress("Starting UI...");

    if (RunUtil.isMacOSX()) {
      try {
        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(new JCSAboutHandler());
        desktop.setQuitHandler(new JCSQuitHandler());
        desktop.setPreferencesHandler(new JCSPreferencesHandler());

        Taskbar taskbar = Taskbar.getTaskbar();
        try {
          //BufferedImage img = ImageIO.read(JCS.class.getResource("/media/jcs-train-64.png"));
          BufferedImage img = ImageIO.read(JCS.class.getResource("/media/jcs-train-2-512.png"));
          taskbar.setIconImage(img);
        } catch (final UnsupportedOperationException e) {
          Logger.warn("The os does not support: 'taskbar.setIconImage'");
        } catch (final SecurityException e) {
          Logger.warn("There was a security exception for: 'taskbar.setIconImage'");
        }
      } catch (SecurityException | IllegalArgumentException | IOException ex) {
        Logger.warn("Failed to register with MacOS: " + ex);
      }
    }

    java.awt.EventQueue.invokeLater(() -> {
      jcsFrame = new JCSFrame();
      JCS.uiCallback = jcsFrame;

      //URL iconUrl = JCS.class.getResource("/media/jcs-train-64.png");
      URL iconUrl = JCS.class.getResource("/media/jcs-train-2-512.png");
      if (iconUrl != null) {
        jcsFrame.setIconImage(new ImageIcon(iconUrl).getImage());
      }

      FrameMonitor.registerFrame(jcsFrame, JCS.class.getName());

      jcsFrame.setVisible(true);
      jcsFrame.toFront();
      jcsFrame.showOverviewPanel();
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
    splashScreen.hideSplash(200);
    splashScreen.close();
  }

  private static class Powerlistener implements PowerEventListener {

    Powerlistener() {
    }

    @Override
    public void onPowerChange(PowerEvent event) {
      if (event.isOverload()) {
        Logger.warn("Track Power OVERLOAD!");
      } else {
        Logger.info("Track Power is " + (event.isPower() ? "on" : "off"));
      }
    }
  }

  private class JCSQuitHandler implements QuitHandler {

    @Override
    public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
      uiCallback.handleQuitRequest();
    }
  }

  private class JCSAboutHandler implements AboutHandler {

    @Override
    public void handleAbout(AboutEvent e) {
      uiCallback.handleAbout();
    }
  }

  private class JCSPreferencesHandler implements PreferencesHandler {

    @Override
    public void handlePreferences(PreferencesEvent e) {
      uiCallback.handlePreferences();
    }
  }

  private class JCSOpenFilesHandler implements OpenFilesHandler {

    @Override
    public void openFiles(OpenFilesEvent e) {
      //STUB
      uiCallback.openFiles(null);
    }
  }

}
