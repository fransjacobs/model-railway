/*
 * Copyright 2024 frans.
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
package jcs.entities;

/**
 *
 * @author frans
 */
public class FeedbackModuleBean {

  private int moduleNumber;
  private int portCount;
  private int addressOffset;
  private int identifier;

  public int getModuleNumber() {
    return moduleNumber;
  }

  public void setModuleNumber(int moduleNumber) {
    this.moduleNumber = moduleNumber;
  }

  public int getPortCount() {
    return portCount;
  }

  public void setPortCount(int portCount) {
    this.portCount = portCount;
  }

  public int getAddressOffset() {
    return addressOffset;
  }

  public void setAddressOffset(int addressOffset) {
    this.addressOffset = addressOffset;
  }

  public int getIdentifier() {
    return identifier;
  }

  public void setIdentifier(int identifier) {
    this.identifier = identifier;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + this.moduleNumber;
    hash = 97 * hash + this.portCount;
    hash = 97 * hash + this.addressOffset;
    hash = 97 * hash + this.identifier;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FeedbackModuleBean other = (FeedbackModuleBean) obj;
    if (this.moduleNumber != other.moduleNumber) {
      return false;
    }
    if (this.portCount != other.portCount) {
      return false;
    }
    if (this.addressOffset != other.addressOffset) {
      return false;
    }
    return this.identifier == other.identifier;
  }

  @Override
  public String toString() {
    return "FeedbackModuleBean{" + "moduleNumber=" + moduleNumber + ", portCount=" + portCount + ", addressOffset=" + addressOffset + ", identifier=" + identifier + "}";
  }

}
