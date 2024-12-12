/*
 * Copyright 2024 Frans Jacobs.
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
package jcs.commandStation.events;

import java.io.Serializable;
import jcs.entities.FunctionBean;

/**
 *
 */
public class LocomotiveFunctionEvent implements Serializable {

  private FunctionBean function;

  public LocomotiveFunctionEvent(FunctionBean changedFunction) {
    this.function = changedFunction;
  }

  public FunctionBean getFunctionBean() {
    return this.function;
  }

  public LocomotiveFunctionEvent(long locomotiveBeanId, int functionNumber, boolean flag) {
    function = new FunctionBean(locomotiveBeanId, functionNumber, flag ? 1 : 0);
  }

  public void setFunctionBean(FunctionBean function) {
    this.function = function;
  }

  public boolean isValid() {
    return this.function != null && this.function.getLocomotiveId() != null && this.function.getNumber() != null;
  }

  public boolean isEventFor(FunctionBean function) {
    if (function != null) {
      return this.function.getNumber().equals(function.getNumber()) && this.function.getLocomotiveId().equals(function.getLocomotiveId());
    } else {
      return false;
    }
  }

}
