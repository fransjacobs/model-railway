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

import java.util.List;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;

public interface AccessoryController extends GenericController {

  void switchAccessory(int address, AccessoryValue value);

  void switchAccessory(int address, AccessoryValue value, int switchTime);

  void addAccessoryEventListener(AccessoryEventListener listener);

  void removeAccessoryEventListener(AccessoryEventListener listener);

  List<AccessoryBean> getAccessories();

  //List<AccessoryBean> getSwitches();

  //List<AccessoryBean> getSignals();

}
