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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import lan.wervel.jcs.controller.cs2.DeviceInfo;
import lan.wervel.jcs.trackservice.TrackService;
import lan.wervel.jcs.trackservice.TrackServiceFactory;
import org.tinylog.Logger;

/**
 * JCS Server provides Repository and Controller Services for UI. Also it can listen to Feedback providers.
 */
public class JCSServer extends Thread {

  private TrackService trackService;
  private static JCSServer instance;

  private JCSServer() {
    System.setProperty("name", "JCS Server");
    init();
  }

  private void init() {
    System.setProperty("useOnlyLocal", "true");

    trackService = TrackServiceFactory.getTrackService();

    instance = this;

    DeviceInfo ci = trackService.getControllerInfo();
    if (ci != null) {
      Logger.info("Controller: " + ci.getDescription() + " " + ci.getCatalogNumber() + " Serial: " + ci.getSerialNumber());
    }
    //Logger.info("JCSServer Version " + info.getVersion() + " pid: " + info.getPid() + " on " + info.getIp() + " port: " + info.getPort() + " Started.");
  }

  public static void main(String[] args) {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JCS Track Service");

    String userHome = System.getProperty("user.home");
    String fileSeparator = System.getProperty("file.separator");
    String lockFileName = userHome + fileSeparator + ".jcs" + fileSeparator + "jcsserverlock";
    final File file = new File(lockFileName);
    try {
      if (!file.exists()) {
        file.createNewFile();
      }

      final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
      final FileLock fileLock = randomAccessFile.getChannel().tryLock();

      if (fileLock != null) {
        Logger.debug("Create lock file: " + lockFileName);

        // Prepare for shutdown...
        Runtime.getRuntime().addShutdownHook(new JCSServer() {

          @Override
          public void run() {
            Logger.info("JCSServer is Shutting Down...");

            try {
              fileLock.release();
              randomAccessFile.close();
              file.delete();
            } catch (IOException e) {
              Logger.error("Unable to remove lock file: " + lockFileName, e);
            }
          }
        });

      } else {
        Logger.error("Can't obtain a lock. A Server Instance is probably already running...");
      }
    } catch (FileNotFoundException ex) {
      Logger.error(ex);
    } catch (IOException ex) {
      Logger.error(ex);
    }
  }

}
