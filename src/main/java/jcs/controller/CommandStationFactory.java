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
import jcs.entities.CommandStationBean;
import jcs.entities.JCSPropertyBean;
import jcs.persistence.PersistenceFactory;
import jcs.util.RunUtil;
import org.tinylog.Logger;

/**
 *
 * Factory to create the Dispatcher and CommandStation
 */
public class CommandStationFactory {

  private Dispatcher dispatcher;
  private CommandStation commandStation;
  private static CommandStationFactory instance;

  private CommandStationFactory() {
  }

  public static CommandStationFactory getInstance() {
    if (instance == null) {
      instance = new CommandStationFactory();
    }
    return instance;
  }

  public static Dispatcher getDispatcher() {
    return CommandStationFactory.getInstance().getDispatcherImpl();
  }

  private Dispatcher getDispatcherImpl() {
    if (dispatcher == null) {
      instance.aquireDispatcher();
    }
    return dispatcher;
  }

  private boolean aquireDispatcher() {
    RunUtil.loadProperties();
    loadPersistentJCSProperties();
    //Load the external properties
    RunUtil.loadExternalProperties();
    String dispatcherImplClassName = System.getProperty("dispatcher");

    Logger.trace("Try to instantiate: " + dispatcherImplClassName);

    if (dispatcherImplClassName != null) {
      try {
        this.dispatcher = (Dispatcher) Class.forName(dispatcherImplClassName).getDeclaredConstructor().newInstance();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
        Logger.error("Can't instantiate a '" + dispatcherImplClassName + "' " + ex.getMessage());
        Logger.trace(ex);
      }
    } else {
      Logger.error("Can't find implementation class for 'dispatcher'!");
    }

    if (dispatcher != null) {
      Logger.trace("Using " + dispatcher.getClass().getSimpleName() + " as Dispatcher...");
    }
    return dispatcher != null;
  }

  public static CommandStation getCommandStation() {
    return CommandStationFactory.getInstance().getCommandStationImpl();
  }

  private CommandStation getCommandStationImpl() {
    if (commandStation == null) {
      instance.aquireCommandStation();
    }
    return commandStation;
  }

  private boolean aquireCommandStation() {
    CommandStationBean bean = PersistenceFactory.getService().getDefaultCommandStation();
    String commandStationImplClassName = bean.getClassName();

    JCS.logProgress("Invoking CommandStation: " + commandStationImplClassName);

    try {
      this.commandStation = (CommandStation) Class.forName(commandStationImplClassName).getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
      Logger.error("Can't instantiate a '" + commandStationImplClassName + "' " + ex.getMessage());
    }

    return this.commandStation != null;
  }

  private void loadPersistentJCSProperties() {
    JCS.logProgress("Obtain properties from Persistent store");
    List<JCSPropertyBean> props = PersistenceFactory.getService().getProperties();
    props.forEach(p -> {
      System.setProperty(p.getKey(), p.getValue());
    });
  }

}
