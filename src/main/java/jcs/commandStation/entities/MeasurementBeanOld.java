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
package jcs.commandStation.entities;

import java.util.Date;

/**
 * Object to hold measured values
 */
@Deprecated
public class MeasurementBeanOld {

  private String name;
  private Double value;
  private String unit;
  private Date measurementTime;

  private String selection;
  private String config;
  private Double endValue;
  private Integer colorYellow;
  private Integer colorGreen;
  private Integer colorMax;
  private Integer colorRed;
  private Integer index;
  private Boolean present;
  private Integer min;
  private Integer max;
  private Integer number;
  private Boolean ready;
  private Integer rangeYellow;
  private Integer rangeGreen;
  private Integer rangeMax;
  private Integer rangeRed;
  private Integer scale;
  private Double startValue;
  private Integer type;
  private Integer previousValue;
  private Double humanValue;

}
