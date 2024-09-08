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

  private static DecoderController decoderController;
  private static final Map<String, AccessoryController> accessoryControllers = new HashMap<>();
  private static final Map<String, FeedbackController> feedbackControllers = new HashMap<>();

  private static ControllerFactory instance;

  private ControllerFactory() {
    loadPersistentJCSProperties();
  }

  public static ControllerFactory getInstance() {
    if (instance == null) {
      instance = new ControllerFactory();
    }
    return instance;
  }

  
  static void reset() {
    decoderController = null;
    accessoryControllers.clear();
    feedbackControllers.clear();
  }
  
  public static DecoderController getDecoderController() {
    return ControllerFactory.getDecoderController(null, false);
  }

  public static AccessoryController getAccessoryController(CommandStationBean commandStationBean) {
    if (ControllerFactory.accessoryControllers.isEmpty()) {
      instantiateAccessoryControllers();
    }
    return ControllerFactory.accessoryControllers.get(commandStationBean.getId());
  }

  public static AccessoryController getAccessoryController(String id) {
    return ControllerFactory.accessoryControllers.get(id);
  }

  public static FeedbackController getFeedbackController(String id) {
    return ControllerFactory.feedbackControllers.get(id);
  }

  public static synchronized DecoderController getDecoderController(CommandStationBean commandStationBean, boolean autoConnect) {

    if ((decoderController == null && commandStationBean != null)
            || (decoderController != null && !decoderController.getCommandStationBean().equals(commandStationBean))) {
      decoderController = instantiateDecoderController(commandStationBean, autoConnect);
    }
    return decoderController;
  }

  public static List<AccessoryController> getAccessoryControllers() {
    return ControllerFactory.getInstance().getAccessoryControllerImpls();
  }

  private List<AccessoryController> getAccessoryControllerImpls() {
    if (accessoryControllers.isEmpty()) {
      instantiateAccessoryControllers();
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

  private static DecoderController instantiateDecoderController(CommandStationBean commandStationBean, boolean autoConnect) {
    CommandStationBean bean;
    if (commandStationBean != null) {
      bean = commandStationBean;
    } else {
      bean = PersistenceFactory.getService().getDefaultCommandStation();
      Logger.trace("Default Command station: " + bean.getDescription() + " Decoder Support: " + bean.isDecoderControlSupport());
    }

    if (bean.isDecoderControlSupport() && bean.isEnabled()) {
      String className = bean.getClassName();
      JCS.logProgress("Invoking CommandStation: " + className);
      Logger.trace("Invoking decoderController: " + className);

      try {
        Constructor c = Class.forName(className).getConstructor(CommandStationBean.class, Boolean.TYPE);
        decoderController = (DecoderController) c.newInstance(commandStationBean, autoConnect);
      } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
        Logger.error("Can't instantiate a '" + className + "' " + ex.getMessage());
      }
    }

    if (decoderController != null && bean.isDecoderControlSupport() && bean.isEnabled()) {
      accessoryControllers.put(bean.getId(), (AccessoryController) decoderController);
      Logger.trace("decoderController is also accessoryController.");
    }

    if (decoderController != null && bean.isFeedbackSupport() && bean.isEnabled()) {
      feedbackControllers.put(bean.getId(), (FeedbackController) decoderController);
      Logger.trace("decoderController is also feedbackController.");
    }

    if (decoderController != null) {
      Logger.trace("CommandStationId: " + (decoderController.getCommandStationBean() != null ? decoderController.getCommandStationBean().getId() : "?"));
    } else {
      if (commandStationBean != null) {
        Logger.warn("No Default Command Station found for " + commandStationBean.getShortName());
      } else {
        Logger.warn("Command Station NOT set!");
      }
    }

    return decoderController;
  }

  private static boolean instantiateAccessoryControllers() {
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
            AccessoryController accessoryController = (AccessoryController) c.newInstance(bean, true);
            accessoryControllers.put(bean.getId(), accessoryController);
          } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
            Logger.error("Can't instantiate a '" + className + "' " + ex.getMessage());
          }
        }
      }
    }
    return !accessoryControllers.isEmpty();
  }

  private boolean instantiateFeedbackControllers() {
    List<CommandStationBean> beans = PersistenceFactory.getService().getCommandStations();
    //In case the FeedbackController is the same instance as the DecoderController, which is by example the case for a Marklin CS 3
    for (CommandStationBean bean : beans) {
      if (bean.isFeedbackSupport() && bean.isEnabled()) {
        //Check the decoder controller which might be the same Object
        if (!feedbackControllers.containsKey(bean.getId())) {
          String className = bean.getClassName();
          Logger.trace("Invoking feedbackController: " + className);
          try {
            Constructor c = Class.forName(className).getConstructor(CommandStationBean.class, Boolean.TYPE);
            FeedbackController feedbackController = (FeedbackController) c.newInstance(bean, false);
            feedbackControllers.put(bean.getId(), feedbackController);
          } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
            Logger.error("Can't instantiate a '" + className + "' " + ex.getMessage());
          }
        }
      }
    }
    return !feedbackControllers.isEmpty();
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
