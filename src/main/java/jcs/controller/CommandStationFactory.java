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
public class CommandStationFactory {

  private CommandStation controller;
  private static CommandStationFactory instance;

  private CommandStationFactory() {
  }

  public static CommandStationFactory getInstance() {
    if (instance == null) {
      instance = new CommandStationFactory();
      instance.aquireCommandStationImpl();
    }
    return instance;
  }

  public static CommandStation getCommandStation() {
    return CommandStationFactory.getInstance().getCommandStationImpl();
  }

  private CommandStation getCommandStationImpl() {
    return controller;
  }

  private boolean aquireCommandStationImpl() {
    RunUtil.loadProperties();
    loadPersistentJCSProperties();
    //Load the external properties
    RunUtil.loadExternalProperties();
    String commandStationImplClassName = System.getProperty("controller");

    Logger.trace("Try to instantiate: " + commandStationImplClassName);

    if (commandStationImplClassName != null) {
      try {
        this.controller = (CommandStation) Class.forName(commandStationImplClassName).getDeclaredConstructor().newInstance();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
        Logger.error("Can't instantiate a '" + commandStationImplClassName + "' " + ex.getMessage());
        Logger.trace(ex);
      }
    } else {
      Logger.error("Can't find implementation class for 'controller'!");
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
