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
package jcs.commandStation.marklin.parser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import jcs.commandStation.entities.MeasurementBean;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.commandStation.marklin.cs.can.device.MeasuringChannel;
import org.tinylog.Logger;

/**
 * Convert the SystemStatus Message measured values into a MeasurementBean
 */
public class SystemStatusMessage {

  public SystemStatusMessage() {

  }

  public static MeasurementBean parse(MeasuringChannel channel, CanMessage systemStatusmessage) {
    return parse(channel, systemStatusmessage, System.currentTimeMillis());
  }

  public static MeasurementBean parse(MeasuringChannel channel, CanMessage systemStatusmessage, long measurementMillis) {

    MeasurementBean measurement = null;
    if (systemStatusmessage.getCommand() == CanMessage.SYSTEM_COMMAND && systemStatusmessage.getSubCommand() == CanMessage.SYSTEM_SUB_STATUS) {
      CanMessage response = systemStatusmessage.getResponse();
      byte[] data = response.getData();

      switch (response.getDlc()) {
        case CanMessage.DLC_7 -> {
          int channelNumber = data[5];
          int valid = data[6];
          measurement = new MeasurementBean(channelNumber, channel.getName(), (valid == 1), System.currentTimeMillis());
        }
        case CanMessage.DLC_8 -> {
          int channelNumber = data[5];
          int measuredValue = CanMessage.toInt(new byte[]{data[6], data[7]});

          Double displayValue = calculateDisplayValue(measuredValue, channel);
          measurement = new MeasurementBean(channelNumber, channel.getName(), measurementMillis, measuredValue, channel.getUnit(), displayValue);
        }
        default ->
          Logger.error("Invalid DLC " + response.getDlc() + " response " + response);
      }
    } else {
      Logger.error("Unexpected message " + systemStatusmessage);
    }
    return measurement;
  }

  private static Double calculateDisplayValue(Integer measuredValue, MeasuringChannel channel) {
    Double startVal = channel.getStartValue();
    Double endVal = channel.getEndValue();
    Integer rangeMax = channel.getRangeMax();
    //Logger.trace("Ch: "+channel);

    if (startVal != null && endVal != null && rangeMax != null) {
      //Logger.trace("endVal: "+endVal+" startVal: "+startVal+" rangeMax: "+rangeMax+" measuredValue: "+measuredValue);

      Double displayValue = ((endVal - startVal) / rangeMax * measuredValue) + startVal;
      return round(displayValue, getDigits(channel.getName()));
    }
    return null;
  }

  private static Double round(Double value, int digits) {
    if (digits < 0) {
      throw new IllegalArgumentException();
    }

    BigDecimal bd = new BigDecimal(Double.toString(value));
    bd = bd.setScale(digits, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  private static int getDigits(String channelName) {

    return switch (channelName) {
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
  }

}
