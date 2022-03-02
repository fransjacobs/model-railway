/*
 * Copyright (C) 2022 fransjacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.controller.cs3;

import java.util.Objects;

/**
 *
 * @author fransjacobs
 */
public class GFPChannel {

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

    public GFPChannel() {
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
        final GFPChannel other = (GFPChannel) obj;
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

    @Override
    public String toString() {
        return "Channel{" + "name=" + name + ", number=" + number + ", humanValue=" + humanValue + ", unit=" + unit + ", scale=" + scale + "}";
    }

}
