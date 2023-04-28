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
package jcs.controller.cs3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 *
 * @author fransjacobs
 */
public class MeasurementChannel {

    private String unit;
    private Double endValue;
    private Integer colorYellow;
    private Integer colorGreen;
    private Integer colorMax;
    private Integer colorRed;
    private String name;
    private Integer number;
    private Integer scale;
    private Integer rangeYellow;
    private Integer rangeGreen;
    private Integer rangeMax;
    private Integer rangeRed;
    private Double startValue;
    private Integer value;
    private Double humanValue;

    public MeasurementChannel() {
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

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
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

    public Double getStartValue() {
        return startValue;
    }

    public void setStartValue(Double startValue) {
        this.startValue = startValue;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;

        int digits;
        String n = this.name;
        if (n == null) {
            n = "";
        }
        switch (n) {
            case "MAIN":
                digits = 3;
                break;
            case "PROG":
                digits = 3;
                break;
            case "VOLT":
                digits = 1;
                break;
            case "TEMP":
                digits = 1;
                break;
            default:
                digits = 0;
                break;
        }

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

    public Double getHumanValue() {
        return humanValue;
    }

    public void setHumanValue(Double humanValue) {
        this.humanValue = humanValue;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.unit);
        hash = 29 * hash + Objects.hashCode(this.endValue);
        hash = 29 * hash + Objects.hashCode(this.colorYellow);
        hash = 29 * hash + Objects.hashCode(this.colorGreen);
        hash = 29 * hash + Objects.hashCode(this.colorMax);
        hash = 29 * hash + Objects.hashCode(this.colorRed);
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.number);
        hash = 29 * hash + Objects.hashCode(this.scale);
        hash = 29 * hash + Objects.hashCode(this.rangeYellow);
        hash = 29 * hash + Objects.hashCode(this.rangeGreen);
        hash = 29 * hash + Objects.hashCode(this.rangeMax);
        hash = 29 * hash + Objects.hashCode(this.rangeRed);
        hash = 29 * hash + Objects.hashCode(this.startValue);
        hash = 29 * hash + Objects.hashCode(this.value);
        hash = 29 * hash + Objects.hashCode(this.humanValue);
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
        final MeasurementChannel other = (MeasurementChannel) obj;
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
        if (!Objects.equals(this.number, other.number)) {
            return false;
        }
        if (!Objects.equals(this.scale, other.scale)) {
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
        if (!Objects.equals(this.startValue, other.startValue)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return Objects.equals(this.humanValue, other.humanValue);
    }

//    @Override
//    public String toString() {
//        return "Channel{" + "name=" + name + ", number=" + number + ", humanValue=" + humanValue + ", unit=" + unit + ", scale=" + scale + "}";
//    }
    @Override
    public String toString() {
        return "GFPChannel{" + "unit=" + unit + ", endValue=" + endValue + ", colorYellow=" + colorYellow + ", colorGreen=" + colorGreen + ", colorMax=" + colorMax + ", colorRed=" + colorRed + ", name=" + name + ", number=" + number + ", scale=" + scale + ", rangeYellow=" + rangeYellow + ", rangeGreen=" + rangeGreen + ", rangeMax=" + rangeMax + ", rangeRed=" + rangeRed + ", startValue=" + startValue + ", value=" + value + ", humanValue=" + humanValue + '}';
    }

}
