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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.json.JSONObject;

/**
 * Represents a Configuration Channel of a Marklin Central Station CanDevice<br>
 * There are 2 formatConfig Channel, main difference is de use of a choice list.<br>
 * Quotes from the documentation:<br>
 * Format eines Datenblocks mit der Möglichkeit eine Auswahl zu treffen:<br>
 * - Konfigkanalnummer<br>
 * - Kenner Auswahlliste<br>
 * - Anzahl der Auswahlpunkte<br>
 * - Jetzige (Default) Einstellung<br>
 * - Auswahlbezeichnung<br>
 * - Auswahl 1<br>
 * - Auswahl 2<br>
 * - Auswahl 3<br>
 *
 * Format eines Datenblocks mit der Möglichkeit einen Wert einzustellen:<br>
 * - Konfigirationskanalnummer<br>
 * - Kenner Slider<br>
 * - Unterer Wert<br>
 * - Oberer Wert<br>
 * - Aktuelle Einstellung<br>
 * - Auswahlbezeichnung<br>
 * - Bezeichnung Start<br>
 * - Bezeichnung Ende<br>
 * - Einheit<br>
 *
 */
public class ConfigChannel {

  private Integer number;
  private Integer valueId;
  private Integer choicesCount;
  private String choiceDescription;
  private final List<String> choices;
  private Integer lowValue;
  private Integer highValue;
  private Integer actualValue;
  private String startName;
  private String endName;
  private String unit;

  public ConfigChannel() {
    this(null);
  }

  public ConfigChannel(String json) {
    choices = new ArrayList<>();
    if (json != null) {
      parseJson(json);
    }
  }

  public Integer getNumber() {
    return number;
  }

  public void setNumber(Integer number) {
    this.number = number;
  }

  public Integer getValueId() {
    return valueId;
  }

  public void setValueId(Integer valueId) {
    this.valueId = valueId;
  }

  public Integer getChoicesCount() {
    return choicesCount;
  }

  public void setChoicesCount(Integer choicesCount) {
    this.choicesCount = choicesCount;
  }

  public String getChoiceDescription() {
    return choiceDescription;
  }

  public void setChoiceDescription(String choiceDescription) {
    this.choiceDescription = choiceDescription;
  }

  public void addChoice(String choice) {
    this.choices.add(choice);
  }

  public List<String> getChoices() {
    return choices;
  }

  public Integer getLowValue() {
    return lowValue;
  }

  public void setLowValue(Integer lowValue) {
    this.lowValue = lowValue;
  }

  public Integer getHighValue() {
    return highValue;
  }

  public void setHighValue(Integer highValue) {
    this.highValue = highValue;
  }

  public Integer getActualValue() {
    return actualValue;
  }

  public void setActualValue(Integer actualValue) {
    this.actualValue = actualValue;
  }

  public String getStartName() {
    return startName;
  }

  public void setStartName(String startName) {
    this.startName = startName;
  }

  public String getEndName() {
    return endName;
  }

  public void setEndName(String endName) {
    this.endName = endName;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ConfigChannel{");
    if (number != null) {
      sb.append("number=").append(number);
    }
    if (choicesCount != null) {
      sb.append(", choicesCount=").append(choicesCount);
    }
    if (valueId != null) {
      sb.append(", valueId=").append(valueId);
    }
    if (choiceDescription != null) {
      sb.append(", choiceDescription=").append(choiceDescription);
    }
    if (!choices.isEmpty()) {
      sb.append(", choices=").append(choices);
    }
    if (lowValue != null) {
      sb.append(", lowValue=").append(lowValue);
    }
    if (highValue != null) {
      sb.append(", highValue=").append(highValue);
    }
    if (actualValue != null) {
      sb.append(", actualValue=").append(actualValue);
    }
    if (startName != null) {
      sb.append(", startName=").append(startName);
    }
    if (endName != null) {
      sb.append(", endName=").append(endName);
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
    hash = 83 * hash + Objects.hashCode(this.number);
    hash = 83 * hash + Objects.hashCode(this.valueId);
    hash = 83 * hash + Objects.hashCode(this.choicesCount);
    hash = 83 * hash + Objects.hashCode(this.choiceDescription);
    hash = 83 * hash + Objects.hashCode(this.choices);
    hash = 83 * hash + Objects.hashCode(this.lowValue);
    hash = 83 * hash + Objects.hashCode(this.highValue);
    hash = 83 * hash + Objects.hashCode(this.actualValue);
    hash = 83 * hash + Objects.hashCode(this.startName);
    hash = 83 * hash + Objects.hashCode(this.endName);
    hash = 83 * hash + Objects.hashCode(this.unit);
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
    final ConfigChannel other = (ConfigChannel) obj;
    if (!Objects.equals(this.choiceDescription, other.choiceDescription)) {
      return false;
    }
    if (!Objects.equals(this.startName, other.startName)) {
      return false;
    }
    if (!Objects.equals(this.endName, other.endName)) {
      return false;
    }
    if (!Objects.equals(this.unit, other.unit)) {
      return false;
    }
    if (!Objects.equals(this.number, other.number)) {
      return false;
    }
    if (!Objects.equals(this.valueId, other.valueId)) {
      return false;
    }
    if (!Objects.equals(this.choicesCount, other.choicesCount)) {
      return false;
    }
    if (!Objects.equals(this.choices, other.choices)) {
      return false;
    }
    if (!Objects.equals(this.lowValue, other.lowValue)) {
      return false;
    }
    if (!Objects.equals(this.highValue, other.highValue)) {
      return false;
    }
    return Objects.equals(this.actualValue, other.actualValue);
  }

  private void parseJson(String json) {
    JSONObject kanal = new JSONObject(json);

    //this.defaultValueId = kanal.optInt("auswahl");
    this.actualValue = kanal.optInt("auswahl");
    this.unit = kanal.optString("einheit");
    //this.index = kanal.optInt("index");
    this.lowValue = kanal.optInt("min");
    this.highValue = kanal.optInt("max");
    this.choiceDescription = kanal.optString("name");
    this.number = kanal.optInt("nr");
    this.startName = kanal.optString("startWert");
    //this.type = kanal.optInt("typ");
    this.actualValue = kanal.optInt("wert");
  }

}
