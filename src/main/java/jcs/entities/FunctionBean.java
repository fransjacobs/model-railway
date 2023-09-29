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

  private Boolean momentary;
  private String icon;

  //Fields for CS 2
  private final static String IMG_PREFIX = "FktIcon_";
  private final static String IMG_A = "a_";
  private final static String IMG_I = "i_";
  private final static String IMG_YELLOW = "ge_";
  private final static String IMG_WHITE = "we_";
  //private final String IMG_GREY = "gr_";
  private final static String NMB_FORMAT = "%02d";

  private Image inActiveIconImage;
  private Image activeIconImage;

  /**
   * <p>
   * Entity to model a Locomotive Function.</p>
   * <p>
   * In Case of a CS 2 the momentary flag is derived from the function type.<br>
   * When the functionType is greater then 127 but lower then 150 a function is momentary.</p>
   * <p>
   * The momentary flag also plays a role in the retrieval of the function icons,<br>
   * so for all numbers above 128, but below 150, 128 has to be subtracted to get the right image number(...)<br></p>
   * <p>
   * In case of a CS 3 the momentary flag an icons are specified in a JSON file.</p>
   *
   */
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
    this(id, locomotiveId, number, functionType, value, (functionType != null && functionType > 127 && functionType < 150), null);
  }

  public FunctionBean(Long locomotiveId, Integer number, Integer functionType, Integer value, Boolean momentary, String icon) {
    this(null, locomotiveId, number, functionType, value, momentary, icon);
  }

  public FunctionBean(Long id, Long locomotiveId, Integer number, Integer functionType, Integer value, Boolean momentary, String icon) {
    this.locomotiveId = locomotiveId;
    this.number = number;
    this.functionType = functionType;
    this.value = value;
    this.momentary = momentary;
    this.icon = icon;
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

  @Column(name = "locomotive_id", nullable = false)
  public Long getLocomotiveId() {
    return locomotiveId;
  }

  public void setLocomotiveId(Long locomotiveId) {
    this.locomotiveId = locomotiveId;
  }

  public void setMomentary(boolean momentary) {
    this.momentary = momentary;
  }

  @Column(name = "momentary", nullable = false, columnDefinition = "momentary bool default '1'")
  public boolean isMomentary() {
    return momentary;
  }

  @Column(name = "f_icon", length = 255)
  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
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

  @Transient
  public String getIconName() {
    return getIconName(this.isOn());
  }

  @Transient
  public String getIconName(boolean active) {
    if (this.icon == null) {
      String ai;
      String col;
      if (active) {
        ai = IMG_A;
        col = IMG_YELLOW;
      } else {
        ai = IMG_I;
        col = IMG_WHITE;
      }
      if (this.functionType == null) {
        return null;
      }
      if (this.functionType < 127 || this.functionType >= 150) {
        return IMG_PREFIX + ai + col + String.format(NMB_FORMAT, this.functionType);
      } else if (this.functionType > 128 || this.functionType < 150) {
        int typ = this.functionType - 128;
        return IMG_PREFIX + ai + col + String.format(NMB_FORMAT, typ);
      } else {
        return null;
      }
    } else {
      //Make the name CS2 compatible
      //a; active -> _a_ge_. i; inactive _i_we_
      if (this.icon.contains("_a_") && active) {
        return this.icon.replace("_a_", "_a_ge_");
      } else if (this.icon.contains("_i_") && active) {
        return this.icon.replaceAll("_i_", "_a_ge_");
      } else if (this.icon.contains("_i_") && !active) {
        return this.icon.replace("_i_", "_i_we_");
      } else if (this.icon.contains("_a_") && !active) {
        return this.icon.replaceAll("_a_", "_i_we_");
      } else {
        return null;
      }
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
    return getIconName(true);
  }

  @Transient
  public String getInActiveIcon() {
    return getIconName(false);
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 71 * hash + Objects.hashCode(this.locomotiveId);
    hash = 71 * hash + Objects.hashCode(this.number);
    hash = 71 * hash + Objects.hashCode(this.functionType);
    hash = 71 * hash + Objects.hashCode(this.value);
    hash = 71 * hash + Objects.hashCode(this.momentary);
    hash = 71 * hash + Objects.hashCode(this.icon);

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
    if (!Objects.equals(this.momentary, other.momentary)) {
      return false;
    }
    if (!Objects.equals(this.icon, other.icon)) {
      return false;
    }
    return Objects.equals(this.value, other.value);
  }

  @Override
  public String toString() {
    return "locoId:" + locomotiveId + ", number:" + number + ", type: " + functionType + ", value: " + value + ", momentary: " + momentary + ", icon: " + icon;
  }

  public String toLogString() {
    return toString();
  }

}
