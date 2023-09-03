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
package jcs.entities;

import java.awt.Image;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Represents a locomotive function
 *
 * @author frans
 */
@Table(name = "locomotive_functions", indexes = {
  @Index(name = "lofu_loco_func_idx", columnList = "locomotive_id, number", unique = true)})
public class FunctionBean implements Serializable {

  private Long id;
  private Long locomotiveId;
  private Integer number;
  private Integer functionType;
  private Integer value;

  private final String IMG_PREFIX = "FktIcon_";
  private final String IMG_A = "a_";
  private final String IMG_I = "i_";
  private final String IMG_YELLOW = "ge_";
  private final String IMG_WHITE = "we_";
  //private final String IMG_GREY = "gr_";
  private final String NMB_FORMAT = "%02d";
  //private final String EXTENSION = ".png";

  private Image inActiveIconImage;
  private Image activeIconImage;

  public FunctionBean() {
  }

  public FunctionBean(Integer number) {
    this(null, number, null, null);
  }

  public FunctionBean(Integer number, Long locomotiveId) {
    this(locomotiveId, number, null, null);
  }

  public FunctionBean(Long locomotiveId, Integer number, Integer functionType, Integer value) {
    this(null, locomotiveId, number, functionType, value);
  }

  public FunctionBean(Long id, Long locomotiveId, Integer number, Integer functionType, Integer value) {
    this.locomotiveId = locomotiveId;
    this.number = number;
    this.functionType = functionType;
    this.value = value;
  }

  @Id
  @GeneratedValue
  @Column(name = "id")
  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(name = "f_number", nullable = false)
  public Integer getNumber() {
    return number;
  }

  public void setNumber(Integer number) {
    this.number = number;
  }

  public void setNumber(String number) {
    this.number = Integer.valueOf(number);
  }

  @Column(name = "f_type", nullable = false)
  public Integer getFunctionType() {
    return functionType;
  }

  public void setFunctionType(Integer functionType) {
    this.functionType = functionType;
  }

  public void setFunctionType(String functionType) {
    this.functionType = Integer.valueOf(functionType);
  }

  @Column(name = "f_value", nullable = false)
  public Integer getValue() {
    return value;
  }

  @Transient
  public boolean isOn() {
    return this.value >= 1;
  }

  public void setValue(Integer value) {
    this.value = value;
  }

  public void setValue(String value) {
    this.value = Integer.valueOf(value);
  }

  /**
   * The "normal" function numbers (typ) are apparently in the range between 0 and 127. When a number is greater then 128 the MSB is set meaning the the function is momentary. This appears to be right
   * until 150 which are apparently Cab? icons...
   *
   * @return true when the function is momentary
   */
  @Transient
  public boolean isMomentary() {
    //return this.functionType != null && this.functionType > 127 && this.functionType < 150;
    return this.functionType != null && this.functionType > 127;
  }

  @Transient
  public String getIcon() {
    return getIcon(this.isOn());
  }

  private String getIcon(boolean active) {
    String ai;
    String col;
    String icon;
    if (active) {
      ai = IMG_A;
      col = IMG_YELLOW;
    } else {
      ai = IMG_I;
      col = IMG_WHITE;
    }

    if (this.functionType != null) {
      //if (this.functionType < 127 || this.functionType >= 150) {
      if (this.functionType < 127) {
        icon = IMG_PREFIX + ai + col + String.format(NMB_FORMAT, this.functionType);
      } else {
        int typ = this.functionType - 128;
        icon = IMG_PREFIX + ai + col + String.format(NMB_FORMAT, typ);
      }
      return icon;
    } else {
      return null;
    }
  }

  @Transient
  public Image getInActiveIconImage() {
    return inActiveIconImage;
  }

  public void setInActiveIconImage(Image inActiveIconImage) {
    this.inActiveIconImage = inActiveIconImage;
  }

  @Transient
  public Image getActiveIconImage() {
    return activeIconImage;
  }

  public void setActiveIconImage(Image activeIconImage) {
    this.activeIconImage = activeIconImage;
  }

  @Transient
  public String getActiveIcon() {
    return getIcon(true);
  }

  @Transient
  public String getInActiveIcon() {
    return getIcon(false);
  }

  @Column(name = "locomotive_id", nullable = false)
  public Long getLocomotiveId() {
    return locomotiveId;
  }

  public void setLocomotiveId(Long locomotiveId) {
    this.locomotiveId = locomotiveId;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 71 * hash + Objects.hashCode(this.locomotiveId);
    hash = 71 * hash + Objects.hashCode(this.number);
    hash = 71 * hash + Objects.hashCode(this.functionType);
    hash = 71 * hash + Objects.hashCode(this.value);
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
    final FunctionBean other = (FunctionBean) obj;
    if (!Objects.equals(this.locomotiveId, other.locomotiveId)) {
      return false;
    }
    if (!Objects.equals(this.number, other.number)) {
      return false;
    }
    if (!Objects.equals(this.functionType, other.functionType)) {
      return false;
    }
    return Objects.equals(this.value, other.value);
  }

  @Override
  public String toString() {
    return "locoId:" + locomotiveId + ", number:" + number + ";type: " + functionType + ", value: " + value;
  }

  public String toLogString() {
    return toString();
  }

}
