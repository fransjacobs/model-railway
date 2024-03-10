/*
 * Copyright 2024 fransjacobs.
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
package jcs.ui.util;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import org.tinylog.Logger;

/**
 * Class to keep track of Frame positions and sizes and persists them in the Java Preferences<br>
 * Inspired from https://www.logicbig.com/tutorials/java-swing/frame-location-size-pref.html
 */
public class FrameMonitor {

  /**
   * Align the frame default in the middle of the screen using then "packed" sizes
   *
   * @param frame the frame to show
   * @param frameUniqueId the id of the the frame
   */
  public static void registerFrame(JFrame frame, String frameUniqueId) {
    frame.pack();
    frame.setLocationRelativeTo(null);
    //int defaultX = frame.getX();
    //int defaultY = frame.getY();
    //int defaultW = frame.getWidth();
    //int defaultH = frame.getHeight();

    Point location = frame.getLocation();
    int defaultX = location.x;
    int defaultY = location.y;
    Dimension size = frame.getSize();
    int defaultW = size.width;
    int defaultH = size.height;

    registerFrame(frame, frameUniqueId, defaultX, defaultY, defaultW, defaultH);
  }

  public static void registerFrame(JFrame frame, String frameUniqueId, int defaultX, int defaultY, int defaultW, int defaultH) {
    Preferences prefs = Preferences.userRoot().node(FrameMonitor.class.getSimpleName() + "-" + frameUniqueId);

    frame.setLocation(getFrameLocation(prefs, defaultX, defaultY));
    frame.setSize(getFrameSize(prefs, defaultW, defaultH));

    PreferencesEventUpdater updater = new PreferencesEventUpdater(400, () -> updatePref(frame, prefs));

    frame.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        updater.update();
      }

      @Override
      public void componentMoved(ComponentEvent e) {
        updater.update();
      }
    });
  }

  private static void updatePref(JFrame frame, Preferences prefs) {
    Logger.trace("Updating preferences for " + frame.getClass().getSimpleName());
    Point location = frame.getLocation();
    prefs.putInt("x", location.x);
    prefs.putInt("y", location.y);
    Dimension size = frame.getSize();
    prefs.putInt("w", size.width);
    prefs.putInt("h", size.height);
  }

  private static Dimension getFrameSize(Preferences pref, int defaultW, int defaultH) {
    int w = pref.getInt("w", defaultW);
    int h = pref.getInt("h", defaultH);
    return new Dimension(w, h);
  }

  private static Point getFrameLocation(Preferences pref, int defaultX, int defaultY) {
    int x = pref.getInt("x", defaultX);
    int y = pref.getInt("y", defaultY);
    return new Point(x, y);
  }
}
