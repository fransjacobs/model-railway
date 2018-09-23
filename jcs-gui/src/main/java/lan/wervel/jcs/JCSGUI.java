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
package lan.wervel.jcs;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import lan.wervel.jcs.common.ControllerProvider;
import lan.wervel.jcs.common.TrackRepository;
import lan.wervel.jcs.ui.nb.JCSFrame;
import lan.wervel.jcs.util.RepoConFactory;

import org.pmw.tinylog.Logger;

/**
 * Single Locomotive Control This is the run time start point for this application. It first figures out what to do (local serial
 * port, act as a server etc)
 *
 * @author frans
 *
 */
public class JCSGUI extends Thread {

  private final boolean headless;

  private ControllerProvider controller;
  private TrackRepository repository;

  private static JCSGUI instance = null;

  private JCSGUI() {
    headless = GraphicsEnvironment.isHeadless();
    System.setProperty("name", "Java Central Station");

    Logger.debug("Using " + (headless ? "headless" : "GUI") + " mode.");
    if (headless) {
      System.setProperty("java.awt.headless", "true");
    } else {
      repository = RepoConFactory.getRepository();
      controller = RepoConFactory.getController();
    }
  }

  public static void main(String[] args) {
    JCSGUI jcs = JCSGUI.getInstance();
    if (!jcs.isHeadless()) {
      jcs.init();
    } else {
      Logger.error("This JDK environment is headless, can't start a UI!");
    }
  }

  public boolean isHeadless() {
    return this.headless;
  }

  private void init() {
    //Manualy...
    //repository.startFeedbackCycle();
    Logger.debug("Starting UI. Connected with: " + this.repository.getServerInfo().getHostName() + "...");

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(() -> {
      JCSFrame frame = new JCSFrame(repository, controller);
      frame.setVisible(true);
      frame.setPreferredSize(new Dimension(1310, 650));
      // X, Y, W, H
      frame.setBounds(100, 100, 1310, 650);
    });

    //JCSFrame.initUI();
    Logger.info("JCS UI started...");
  }
  
  private static boolean exists(String className) {
    try {
      Class.forName(className, false, null);
      return true;
    } catch (ClassNotFoundException exception) {
      return false;
    }
  }

  /**
   * Executed at shutdown in response to a Ctrl-C etc.
   */
  @Override
  public void run() {
    // Perform shutdown methods.
    Logger.info("JCSGUI is Shutting Down...");
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
