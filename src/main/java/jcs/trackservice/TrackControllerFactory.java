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
package jcs.trackservice;

import java.lang.reflect.InvocationTargetException;
import jcs.util.RunUtil;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class TrackControllerFactory {

  private TrackController trackController;
  private static TrackControllerFactory instance;

  private TrackControllerFactory() {
  }

  public static TrackControllerFactory getInstance() {
    if (instance == null) {
      instance = new TrackControllerFactory();
      instance.aquireTrackServiceImpl();
    }
    return instance;
  }

  public static TrackController getTrackController() {
    return TrackControllerFactory.getInstance().getTrackControllerImpl();
  }

  private TrackController getTrackControllerImpl() {
    return trackController;
  }

  private boolean aquireTrackServiceImpl() {
    String trackServiceImpl = System.getProperty("trackService");

    if (trackServiceImpl == null) {
      RunUtil.loadProperties();
      trackServiceImpl = System.getProperty("trackService");
    }
    Logger.trace("Try to instantiate: " + trackServiceImpl);

    if (trackServiceImpl != null) {
      try {
        this.trackController = (TrackController) Class.forName(trackServiceImpl).getDeclaredConstructor().newInstance();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
        Logger.error("Can't instantiate a '" + trackServiceImpl + "' " + ex.getMessage());
        Logger.trace(ex);
      }
    } else {
      Logger.error("Cant find implementation class for 'trackController'!");
    }

    if (trackController != null) {
      Logger.debug("Using " + trackController.getClass().getSimpleName() + " as TrackController...");
    }
    return trackController != null;
  }
}
