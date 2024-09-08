/*
 * Copyright 2024 fransjacobs.
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
package jcs.util;

import com.fazecast.jSerialComm.SerialPort;
import java.util.HashSet;
import java.util.Set;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class SerialPortUtil {
  
  public static boolean portSystemNameExists(String systemPortName) {
    Set<String> ports = new HashSet<>();
    SerialPort[] spa = listComPorts();
    for (SerialPort sp : spa) {
      ports.add(sp.getSystemPortName());
    }
    return ports.contains(systemPortName);
  }
  
  public static boolean portDescriptiveNameExists(String systemPortName) {
    Set<String> ports = new HashSet<>();
    SerialPort[] spa = listComPorts();
    for (SerialPort sp : spa) {
      ports.add(sp.getDescriptivePortName());
    }
    return ports.contains(systemPortName);
  }
  
  public static SerialPort[] listComPorts() {
    SerialPort[] comPorts = SerialPort.getCommPorts();
    return comPorts;
  }
  
  public static void closeAllPorts() {
    SerialPort[] ports = SerialPort.getCommPorts();
    for (SerialPort sp : ports) {
      try {
        sp.closePort();
      } catch (Exception e) {
        Logger.error("Can't close port: " + sp.getDescriptivePortName());
      }
    }
  }
  
  public static void logComports() {
    SerialPort[] ports = listComPorts();
    for (int i = 0; i < ports.length; i++) {
      Logger.trace(i + ": " + ports[i].getDescriptivePortName() + "; " + ports[i].getSystemPortName()
              + "; Vendor: " + ports[i].getVendorID() + "; Manufacturer: " + ports[i].getManufacturer()
              + "; ProductID: " + ports[i].getProductID() + "; Open: " + ports[i].isOpen()
              + "; Device Read/Write BufferSize: " + ports[i].getDeviceReadBufferSize() + "/" + ports[i].getDeviceWriteBufferSize());
    }
  }
  
  public static void main(String[] a) {
    logComports();
  }
  
}
