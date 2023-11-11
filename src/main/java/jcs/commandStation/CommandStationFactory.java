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
package jcs.commandStation;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import jcs.JCS;
import jcs.entities.CommandStationBean;
import jcs.entities.JCSPropertyBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * Factory to create the CommandStation
 */
public class CommandStationFactory {

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

  public static CommandStation getCommandStation() {
    return getCommandStation(null);
  }

  public static CommandStation getCommandStation(CommandStationBean commandStationBean) {
    return CommandStationFactory.getInstance().getCommandStationImpl(commandStationBean);
  }
  
  private CommandStation getCommandStationImpl(CommandStationBean commandStationBean) {
    if (commandStation == null) {
      instance.aquireCommandStation(commandStationBean);
    }
    return commandStation;
  }

  private boolean aquireCommandStation(CommandStationBean commandStationBean) {
    CommandStationBean bean;
    if (commandStationBean != null) {
      bean = commandStationBean;
    } else {
      bean = PersistenceFactory.getService().getDefaultCommandStation();
    }

    String commandStationImplClassName = bean.getClassName();

    JCS.logProgress("Invoking CommandStation: " + commandStationImplClassName);

    Logger.trace("Invoking CommandStation: " + commandStationImplClassName);

    try {
      this.commandStation = (CommandStation) Class.forName(commandStationImplClassName).getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
      Logger.error("Can't instantiate a '" + commandStationImplClassName + "' " + ex.getMessage());
    }

    if (this.commandStation != null) {
      this.commandStation.setCommandStationBean(bean);
    }

    return this.commandStation != null;
  }

  private void loadPersistentJCSProperties() {
    JCS.logProgress("Obtain properties from Persistent store");
    if (PersistenceFactory.getService() != null) {
      List<JCSPropertyBean> props = PersistenceFactory.getService().getProperties();
      props.forEach(p -> {
        System.setProperty(p.getKey(), p.getValue());
      });
    }
  }

}
