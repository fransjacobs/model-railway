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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcs.JCS;
import jcs.entities.CommandStationBean;
import jcs.entities.JCSPropertyBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * Factory to create the different controllers
 */
public class ControllerFactory {

  private DecoderController decoderController;
  private final Map<String, AccessoryController> accessoryControllers;
  private final Map<String, FeedbackController> feedbackControllers;

  private static ControllerFactory instance;

  private ControllerFactory() {
    accessoryControllers = new HashMap<>();
    feedbackControllers = new HashMap<>();
    loadPersistentJCSProperties();
  }

  public static ControllerFactory getInstance() {
    if (instance == null) {
      instance = new ControllerFactory();
    }
    return instance;
  }

  public static DecoderController getDecoderController() {
    return ControllerFactory.getDecoderController(null);
  }

  public static AccessoryController getAccessoryController(String id) {
    return ControllerFactory.instance.accessoryControllers.get(id);
  }

  public static FeedbackController getFeedbackController(String id) {
    return ControllerFactory.instance.feedbackControllers.get(id);
  }

  //Testing
  public static DecoderController getDecoderController(CommandStationBean commandStationBean) {
    return ControllerFactory.getInstance().getDecoderControllerImpl(commandStationBean);
  }

  private DecoderController getDecoderControllerImpl(CommandStationBean commandStationBean) {
    if (decoderController == null || !decoderController.getCommandStationBean().equals(commandStationBean)) {
      instance.instantiateDecoderController(commandStationBean);
    }
    return decoderController;
  }

  public static List<AccessoryController> getAccessoryControllers() {
    return ControllerFactory.getInstance().getAccessoryControllerImpls();
  }

  private List<AccessoryController> getAccessoryControllerImpls() {
    if (accessoryControllers.isEmpty()) {
      instance.instantiateAccessoryControllers();
    }
    return new ArrayList<>(accessoryControllers.values());
  }

  public static List<FeedbackController> getFeedbackControllers() {
    return ControllerFactory.getInstance().getFeedbackControllerImpls();
  }

  private List<FeedbackController> getFeedbackControllerImpls() {
    if (feedbackControllers.isEmpty()) {
      instance.instantiateFeedbackControllers();
    }
    return new ArrayList<>(feedbackControllers.values());
  }

  private boolean instantiateDecoderController(CommandStationBean commandStationBean) {
    CommandStationBean bean;
    if (commandStationBean != null) {
      bean = commandStationBean;
    } else {
      bean = PersistenceFactory.getService().getDefaultCommandStation();
    }

    if (bean.isDecoderControlSupport() && bean.isEnabled()) {
      String className = bean.getClassName();
      JCS.logProgress("Invoking CommandStation: " + className);
      Logger.trace("Invoking decoderController: " + className);

      try {
        Constructor c = Class.forName(className).getConstructor(CommandStationBean.class, Boolean.TYPE);
        decoderController = (DecoderController) c.newInstance(commandStationBean, false);
      } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
        Logger.error("Can't instantiate a '" + className + "' " + ex.getMessage());
      }
    }

    if (decoderController != null && bean.isDecoderControlSupport() && bean.isEnabled()) {
      accessoryControllers.put(bean.getId(), (AccessoryController) this.decoderController);
      Logger.trace("decoderController is also accessoryController.");
    }

    if (decoderController != null && bean.isFeedbackSupport() && bean.isEnabled()) {
      this.feedbackControllers.put(bean.getId(), (FeedbackController) this.decoderController);
      Logger.trace("decoderController is also feedbackController.");
    }

    return this.decoderController != null;
  }

  private boolean instantiateAccessoryControllers() {
    List<CommandStationBean> beans = PersistenceFactory.getService().getCommandStations();
    //In case the AccessoryController is the same instance as the DecoderController, which is by example the case for a Marklin CS 3
    for (CommandStationBean bean : beans) {
      if (bean.isAccessoryControlSupport() && bean.isEnabled()) {
        //Check the decoder controller which might be the same Object
        if (!accessoryControllers.containsKey(bean.getId())) {
          String className = bean.getClassName();
          Logger.trace("Invoking accessoryController: " + className);
          try {
            Constructor c = Class.forName(className).getConstructor(CommandStationBean.class, Boolean.TYPE);
            AccessoryController accessoryController = (AccessoryController) c.newInstance(bean, false);
            this.accessoryControllers.put(bean.getId(), accessoryController);
          } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
            Logger.error("Can't instantiate a '" + className + "' " + ex.getMessage());
          }
        }
      }
    }
    return !this.accessoryControllers.isEmpty();
  }

  private boolean instantiateFeedbackControllers() {
    List<CommandStationBean> beans = PersistenceFactory.getService().getCommandStations();
    //In case the AccessoryController is the same instance as the DecoderController, which is by example the case for a Marklin CS 3
    for (CommandStationBean bean : beans) {
      if (bean.isDecoderControlSupport() && bean.isEnabled()) {
        //Check the decoder controller which might be the same Object
        if (!feedbackControllers.containsKey(bean.getId())) {
          String className = bean.getClassName();
          Logger.trace("Invoking feedbackController: " + className);
          try {
            Constructor c = Class.forName(className).getConstructor(CommandStationBean.class, Boolean.TYPE);
            FeedbackController feedbackController = (FeedbackController) c.newInstance(bean, false);
            this.feedbackControllers.put(bean.getId(), feedbackController);
          } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
            Logger.error("Can't instantiate a '" + className + "' " + ex.getMessage());
          }
        }
      }
    }
    return !this.feedbackControllers.isEmpty();
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
