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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.json.JSONObject;

/**
 *
 * @author fransjacobs
 */
public class ChannelBean {

  private String selection;
  private String config;
  private String unit;
  private Double endValue;
  private Integer colorYellow;
  private Integer colorGreen;
  private Integer colorMax;
  private Integer colorRed;
  private Integer index;
  private Boolean present;
  private Integer min;
  private Integer max;
  private String name;
  private Integer number;
  private Boolean ready;
  private Integer rangeYellow;
  private Integer rangeGreen;
  private Integer rangeMax;
  private Integer rangeRed;
  private Integer scale;
  private Double startValue;
  private Integer type;
  private Integer value;
  private Integer previousValue;
  private Double humanValue;

  public ChannelBean() {
  }

  public ChannelBean(String json) {
    parseJson(json);
  }

  public String getSelection() {
    return selection;
  }

  public void setSelection(String selection) {
    this.selection = selection;
  }

  public String getConfig() {
    return config;
  }

  public void setConfig(String config) {
    this.config = config;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public Double getEndValue() {
    return endValue;
  }

  public void setEndValue(Double endValue) {
    this.endValue = endValue;
  }

  public Integer getColorYellow() {
    return colorYellow;
  }

  public void setColorYellow(Integer colorYellow) {
    this.colorYellow = colorYellow;
  }

  public Integer getColorGreen() {
    return colorGreen;
  }

  public void setColorGreen(Integer colorGreen) {
    this.colorGreen = colorGreen;
  }

  public Integer getColorMax() {
    return colorMax;
  }

  public void setColorMax(Integer colorMax) {
    this.colorMax = colorMax;
  }

  public Integer getColorRed() {
    return colorRed;
  }

  public void setColorRed(Integer colorRed) {
    this.colorRed = colorRed;
  }

  public Integer getIndex() {
    return index;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public Boolean getPresent() {
    return present;
  }

  public void setPresent(Boolean present) {
    this.present = present;
  }

  public Integer getMin() {
    return min;
  }

  public void setMin(Integer min) {
    this.min = min;
  }

  public Integer getMax() {
    return max;
  }

  public void setMax(Integer max) {
    this.max = max;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getNumber() {
    return number;
  }

  public void setNumber(Integer number) {
    this.number = number;
  }

  public Boolean getReady() {
    return ready;
  }

  public void setReady(Boolean ready) {
    this.ready = ready;
  }

  public Integer getRangeYellow() {
    return rangeYellow;
  }

  public void setRangeYellow(Integer rangeYellow) {
    this.rangeYellow = rangeYellow;
  }

  public Integer getRangeGreen() {
    return rangeGreen;
  }

  public void setRangeGreen(Integer rangeGreen) {
    this.rangeGreen = rangeGreen;
  }

  public Integer getRangeMax() {
    return rangeMax;
  }

  public void setRangeMax(Integer rangeMax) {
    this.rangeMax = rangeMax;
  }

  public Integer getRangeRed() {
    return rangeRed;
  }

  public void setRangeRed(Integer rangeRed) {
    this.rangeRed = rangeRed;
  }

  public Integer getScale() {
    return scale;
  }

  public void setScale(Integer scale) {
    this.scale = scale;
  }

  public Double getStartValue() {
    return startValue;
  }

  public void setStartValue(Double startValue) {
    this.startValue = startValue;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public Integer getPreviousValue() {
    return previousValue;
  }

  public void setPreviousValue(Integer previousValue) {
    this.previousValue = previousValue;
  }

  public Double getHumanValue() {
    return humanValue;
  }

  public void setHumanValue(Double humanValue) {
    this.humanValue = humanValue;
  }

  public boolean isChanged() {
    return !Objects.equals(this.value, this.previousValue);
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.previousValue = this.value;
    this.value = value;

    int digits;
    String n = this.name;
    if (n == null) {
      n = "";
    }
    digits = switch (n) {
      case "MAIN" ->
        3;
      case "PROG" ->
        3;
      case "VOLT" ->
        1;
      case "TEMP" ->
        1;
      default ->
        0;
    };

    if (this.startValue != null && this.endValue != null && this.rangeRed != null) {
      double hv = ((this.endValue - this.startValue) / this.rangeRed * this.value) + this.startValue;
      this.humanValue = round(hv, digits);
    }
  }

  private static double round(double value, int places) {
    if (places < 0) {
      throw new IllegalArgumentException();
    }

    BigDecimal bd = new BigDecimal(Double.toString(value));
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 43 * hash + Objects.hashCode(this.selection);
    hash = 43 * hash + Objects.hashCode(this.config);
    hash = 43 * hash + Objects.hashCode(this.unit);
    hash = 43 * hash + Objects.hashCode(this.endValue);
    hash = 43 * hash + Objects.hashCode(this.colorYellow);
    hash = 43 * hash + Objects.hashCode(this.colorGreen);
    hash = 43 * hash + Objects.hashCode(this.colorMax);
    hash = 43 * hash + Objects.hashCode(this.colorRed);
    hash = 43 * hash + Objects.hashCode(this.index);
    hash = 43 * hash + Objects.hashCode(this.present);
    hash = 43 * hash + Objects.hashCode(this.min);
    hash = 43 * hash + Objects.hashCode(this.max);
    hash = 43 * hash + Objects.hashCode(this.name);
    hash = 43 * hash + Objects.hashCode(this.number);
    hash = 43 * hash + Objects.hashCode(this.ready);
    hash = 43 * hash + Objects.hashCode(this.rangeYellow);
    hash = 43 * hash + Objects.hashCode(this.rangeGreen);
    hash = 43 * hash + Objects.hashCode(this.rangeMax);
    hash = 43 * hash + Objects.hashCode(this.rangeRed);
    hash = 43 * hash + Objects.hashCode(this.scale);
    hash = 43 * hash + Objects.hashCode(this.startValue);
    hash = 43 * hash + Objects.hashCode(this.type);
    hash = 43 * hash + Objects.hashCode(this.value);
    hash = 43 * hash + Objects.hashCode(this.previousValue);
    hash = 43 * hash + Objects.hashCode(this.humanValue);
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
    final ChannelBean other = (ChannelBean) obj;
    if (!Objects.equals(this.selection, other.selection)) {
      return false;
    }
    if (!Objects.equals(this.config, other.config)) {
      return false;
    }
    if (!Objects.equals(this.unit, other.unit)) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.endValue, other.endValue)) {
      return false;
    }
    if (!Objects.equals(this.colorYellow, other.colorYellow)) {
      return false;
    }
    if (!Objects.equals(this.colorGreen, other.colorGreen)) {
      return false;
    }
    if (!Objects.equals(this.colorMax, other.colorMax)) {
      return false;
    }
    if (!Objects.equals(this.colorRed, other.colorRed)) {
      return false;
    }
    if (!Objects.equals(this.index, other.index)) {
      return false;
    }
    if (!Objects.equals(this.present, other.present)) {
      return false;
    }
    if (!Objects.equals(this.min, other.min)) {
      return false;
    }
    if (!Objects.equals(this.max, other.max)) {
      return false;
    }
    if (!Objects.equals(this.number, other.number)) {
      return false;
    }
    if (!Objects.equals(this.ready, other.ready)) {
      return false;
    }
    if (!Objects.equals(this.rangeYellow, other.rangeYellow)) {
      return false;
    }
    if (!Objects.equals(this.rangeGreen, other.rangeGreen)) {
      return false;
    }
    if (!Objects.equals(this.rangeMax, other.rangeMax)) {
      return false;
    }
    if (!Objects.equals(this.rangeRed, other.rangeRed)) {
      return false;
    }
    if (!Objects.equals(this.scale, other.scale)) {
      return false;
    }
    if (!Objects.equals(this.startValue, other.startValue)) {
      return false;
    }
    if (!Objects.equals(this.type, other.type)) {
      return false;
    }
    if (!Objects.equals(this.value, other.value)) {
      return false;
    }
    if (!Objects.equals(this.previousValue, other.previousValue)) {
      return false;
    }
    return Objects.equals(this.humanValue, other.humanValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ChannelBean{");
    sb.append("selection=").append(selection);
    sb.append(", config=").append(config);
    sb.append(", unit=").append(unit);
    sb.append(", endValue=").append(endValue);
    sb.append(", colorYellow=").append(colorYellow);
    sb.append(", colorGreen=").append(colorGreen);
    sb.append(", colorMax=").append(colorMax);
    sb.append(", colorRed=").append(colorRed);
    sb.append(", index=").append(index);
    sb.append(", present=").append(present);
    sb.append(", min=").append(min);
    sb.append(", max=").append(max);
    sb.append(", name=").append(name);
    sb.append(", number=").append(number);
    sb.append(", ready=").append(ready);
    sb.append(", rangeYellow=").append(rangeYellow);
    sb.append(", rangeGreen=").append(rangeGreen);
    sb.append(", rangeMax=").append(rangeMax);
    sb.append(", rangeRed=").append(rangeRed);
    sb.append(", scale=").append(scale);
    sb.append(", startValue=").append(startValue);
    sb.append(", type=").append(type);
    sb.append(", value=").append(value);
    sb.append(", previousValue=").append(previousValue);
    sb.append(", humanValue=").append(humanValue);
    sb.append("}");
    return sb.toString();
  }

  private void parseJson(String json) {
    JSONObject kanal = new JSONObject(json);

    this.selection = kanal.optString("auswahl");
    this.config = kanal.optString("auswahl");
    this.unit = kanal.optString("einheit");
    this.colorYellow = kanal.optInt("farbeGelb");
    this.colorGreen = kanal.optInt("farbeGruen");
    this.colorMax = kanal.optInt("farbeMax");
    this.colorRed = kanal.optInt("farbeRot");
    this.index = kanal.optInt("index");
    this.present = kanal.optBoolean("isPresent");
    this.min = kanal.optInt("min");
    this.max = kanal.optInt("max");
    this.name = kanal.optString("name");
    this.number = kanal.optInt("nr");
    this.ready = kanal.optBoolean("_ready");
    this.rangeYellow = kanal.optInt("rangeGelb");
    this.rangeGreen = kanal.optInt("rangeGruen");
    this.rangeMax = kanal.optInt("rangeMax");
    this.rangeRed = kanal.optInt("rangeRot");
    this.scale = kanal.optInt("potenz");
    this.startValue = kanal.optDouble("startWert");
    this.type = kanal.optInt("typ");
    this.value = kanal.optInt("wert");
    //this.previousValue;
    this.humanValue = kanal.optDouble("valueHuman");
  }

}
