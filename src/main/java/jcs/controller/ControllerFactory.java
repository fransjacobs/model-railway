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
package jcs.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import jcs.JCS;
import jcs.entities.JCSPropertyBean;
import jcs.persistence.PersistenceFactory;
import jcs.util.RunUtil;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class ControllerFactory {

  private Controller controller;
  private static ControllerFactory instance;

  private ControllerFactory() {
  }

  public static ControllerFactory getInstance() {
    if (instance == null) {
      instance = new ControllerFactory();
      instance.aquireControllerImpl();
    }
    return instance;
  }

  public static Controller getController() {
    return ControllerFactory.getInstance().getTrackControllerImpl();
  }

  private Controller getTrackControllerImpl() {
    return controller;
  }

  private boolean aquireControllerImpl() {
    RunUtil.loadProperties();
    loadPersistentJCSProperties();
    //Load the external properties
    RunUtil.loadExternalProperties();

    String controllerImplClassName = System.getProperty("controller");

    Logger.trace("Try to instantiate: " + controllerImplClassName);

    if (controllerImplClassName != null) {
      try {
        this.controller = (Controller) Class.forName(controllerImplClassName).getDeclaredConstructor().newInstance();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
        Logger.error("Can't instantiate a '" + controllerImplClassName + "' " + ex.getMessage());
        Logger.trace(ex);
      }
    } else {
      Logger.error("Cant find implementation class for 'controller'!");
    }

    if (controller != null) {
      Logger.trace("Using " + controller.getClass().getSimpleName() + " as Controller...");
    }
    return controller != null;
  }

  private void loadPersistentJCSProperties() {
    JCS.logProgress("Obtain properties from Persistent store");
    List<JCSPropertyBean> props = PersistenceFactory.getService().getProperties();
    props.forEach(p -> {
      System.setProperty(p.getKey(), p.getValue());
    });
  }

}
