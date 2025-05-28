/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.commandStation.marklin.cs.can.device;

import java.util.Objects;
import org.json.JSONObject;

/**
 * Represents a Measurement Channel in the Marklin Central Station
 */
public class MeasuringChannel {

  private Integer number;
  private Integer scale;
  private Integer colorGreen;
  private Integer colorYellow;
  private Integer colorRed;
  private Integer colorMax;
  private Integer zeroPoint;
  private Integer rangeGreen;
  private Integer rangeYellow;
  private Integer rangeRed;
  private Integer rangeMax;
  private String name;
  private Double startValue;
  private Double endValue;
  private String unit;

  public MeasuringChannel() {
  }

  public MeasuringChannel(String json) {
    parseJson(json);
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

  public Integer getZeroPoint() {
    return zeroPoint;
  }

  public void setZeroPoint(Integer zeroPoint) {
    this.zeroPoint = zeroPoint;
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MeasuringChannel{");
    if (number != null) {
      sb.append("number=").append(number);
    }
    if (name != null) {
      sb.append(", name=").append(name);
    }
    if (scale != null) {
      sb.append(", scale=").append(scale);
    }
    if (colorGreen != null) {
      sb.append(", colorGreen=").append(colorGreen);
    }
    if (colorYellow != null) {
      sb.append(", colorYellow=").append(colorYellow);
    }
    if (colorRed != null) {
      sb.append(", colorRed=").append(colorRed);
    }
    if (colorMax != null) {
      sb.append(", colorMax=").append(colorMax);
    }
    if (zeroPoint != null) {
      sb.append(", zero=").append(zeroPoint);
    }
    if (rangeGreen != null) {
      sb.append(", rangeGreen=").append(rangeGreen);
    }
    if (rangeYellow != null) {
      sb.append(", rangeYellow=").append(rangeYellow);
    }
    if (rangeRed != null) {
      sb.append(", rangeRed=").append(rangeRed);
    }
    if (rangeMax != null) {
      sb.append(", rangeMax=").append(rangeMax);
    }
    if (startValue != null) {
      sb.append(", startValue=").append(startValue);
    }
    if (endValue != null) {
      sb.append(", endValue=").append(endValue);
    }
    if (unit != null) {
      sb.append(", unit=").append(unit);
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 23 * hash + Objects.hashCode(this.number);
    hash = 23 * hash + Objects.hashCode(this.scale);
    hash = 23 * hash + Objects.hashCode(this.colorGreen);
    hash = 23 * hash + Objects.hashCode(this.colorYellow);
    hash = 23 * hash + Objects.hashCode(this.colorRed);
    hash = 23 * hash + Objects.hashCode(this.colorMax);
    hash = 23 * hash + Objects.hashCode(this.zeroPoint);
    hash = 23 * hash + Objects.hashCode(this.rangeGreen);
    hash = 23 * hash + Objects.hashCode(this.rangeYellow);
    hash = 23 * hash + Objects.hashCode(this.rangeRed);
    hash = 23 * hash + Objects.hashCode(this.rangeMax);
    hash = 23 * hash + Objects.hashCode(this.name);
    hash = 23 * hash + Objects.hashCode(this.startValue);
    hash = 23 * hash + Objects.hashCode(this.endValue);
    hash = 23 * hash + Objects.hashCode(this.unit);
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
    final MeasuringChannel other = (MeasuringChannel) obj;
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.unit, other.unit)) {
      return false;
    }
    if (!Objects.equals(this.number, other.number)) {
      return false;
    }
    if (!Objects.equals(this.scale, other.scale)) {
      return false;
    }
    if (!Objects.equals(this.colorGreen, other.colorGreen)) {
      return false;
    }
    if (!Objects.equals(this.colorYellow, other.colorYellow)) {
      return false;
    }
    if (!Objects.equals(this.colorRed, other.colorRed)) {
      return false;
    }
    if (!Objects.equals(this.colorMax, other.colorMax)) {
      return false;
    }
    if (!Objects.equals(this.zeroPoint, other.zeroPoint)) {
      return false;
    }
    if (!Objects.equals(this.rangeGreen, other.rangeGreen)) {
      return false;
    }
    if (!Objects.equals(this.rangeYellow, other.rangeYellow)) {
      return false;
    }
    if (!Objects.equals(this.rangeRed, other.rangeRed)) {
      return false;
    }
    if (!Objects.equals(this.rangeMax, other.rangeMax)) {
      return false;
    }
    if (!Objects.equals(this.startValue, other.startValue)) {
      return false;
    }
    return Objects.equals(this.endValue, other.endValue);
  }

  private void parseJson(String json) {
    JSONObject kanal = new JSONObject(json);

//    this.selection = kanal.optString("auswahl");
//    this.config = kanal.optString("auswahl");
    this.unit = kanal.optString("einheit");
    this.colorYellow = kanal.optInt("farbeGelb");
    this.colorGreen = kanal.optInt("farbeGruen");
    this.colorMax = kanal.optInt("farbeMax");
    this.colorRed = kanal.optInt("farbeRot");
//    this.index = kanal.optInt("index");
//    this.present = kanal.optBoolean("isPresent");
//    this.min = kanal.optInt("min");
//    this.max = kanal.optInt("max");
    this.name = kanal.optString("name");
    this.number = kanal.optInt("nr");
//    this.ready = kanal.optBoolean("_ready");
    this.rangeYellow = kanal.optInt("rangeGelb");
    this.rangeGreen = kanal.optInt("rangeGruen");
    this.rangeMax = kanal.optInt("rangeMax");
    this.rangeRed = kanal.optInt("rangeRot");
    this.scale = kanal.optInt("potenz");
    this.startValue = kanal.optDouble("startWert");
//    this.type = kanal.optInt("typ");
//    this.value = kanal.optInt("wert");
    //this.previousValue;
//    this.humanValue = kanal.optDouble("valueHuman");
  }

}
