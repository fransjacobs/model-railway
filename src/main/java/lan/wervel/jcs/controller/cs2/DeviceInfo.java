/*
 * Copyright (C) 2020 Frans Jacobs.
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
package lan.wervel.jcs.controller.cs2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lan.wervel.jcs.controller.cs2.can.CanMessage;

/**
 *
 * @author Frans Jacobs
 */
public class DeviceInfo implements Serializable {

    private String serialNumber;
    private String catalogNumber;
    private String description;

    private String gfpUid;
    private String guiUid;
    private String hardwareVersion;
    private String softwareVersion;

    private String deviceHostName;

    private int maxFunctions;
    private boolean supportMM;
    private boolean supportMFX;
    private boolean supportDCC;
    private boolean supportSX1;

    public DeviceInfo(CanMessage statusRequest) {
        parseMessage(statusRequest);
        this.maxFunctions = 32;
        this.supportDCC = true;
        this.supportMFX = true;
        this.supportMM = true;
        this.supportSX1 = true;
    }

    public DeviceInfo(String gfpUid, String guiUid, String hardwareVersion, String serialNumber) {
        this.gfpUid = gfpUid;
        this.guiUid = guiUid;
        this.hardwareVersion = hardwareVersion;
        this.serialNumber = serialNumber;
    }

    public DeviceInfo(String serialNumber, String catalogNumber, String description, int maxFunctions, boolean supportMM, boolean supportMFX, boolean supportDCC, boolean supportSX1) {
        this.serialNumber = serialNumber;
        this.catalogNumber = catalogNumber;
        this.description = description;

        this.maxFunctions = maxFunctions;
        this.supportDCC = supportDCC;
        this.supportMFX = supportMFX;
        this.supportMM = supportMM;
        this.supportSX1 = supportSX1;
    }

    public void updateFromStatusMessageResponse(CanMessage statusRequest) {
        parseMessage(statusRequest);
    }

    private void parseMessage(CanMessage statusRequest) {
        //First byte holds the serial
        List<CanMessage> rl = statusRequest.getResponses();
        if (!rl.isEmpty()) {
            CanMessage r0 = rl.get(0);

            int[] data1 = r0.getData();
            int[] sn = new int[2];
            System.arraycopy(data1, 6, sn, 0, sn.length);

            int serial = ((sn[0] & 0xFF) << 8) | (sn[1] & 0xFF);
            if (this.serialNumber == null) {
                this.serialNumber = serial + "";
            }

            if (rl.size() > 1) {
                //Second holds the catalog numer is asci
                byte[] data2 = rl.get(1).getDataBytes();
                catalogNumber = dataToString(data2);
            }

            if (rl.size() > 2) {
                //Third is description
                byte[] data3 = rl.get(2).getDataBytes();
                description = dataToString(data3);
            }

            if (rl.size() > 3) {
                //Fourth is description
                byte[] data4 = rl.get(3).getDataBytes();
                description = description + dataToString(data4);
                description = description.trim();
            }
        }
    }

    private static String dataToString(byte[] data) {
        List<Byte> bl = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            if (data[i] > 0) {
                bl.add(data[i]);
            }
        }
        byte[] bytes = new byte[bl.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bl.get(i);
        }
        return new String(bytes);
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxFunctions() {
        return maxFunctions;
    }

    public void setMaxFunctions(int maxFunctions) {
        this.maxFunctions = maxFunctions;
    }

    public boolean isSupportMM() {
        return supportMM;
    }

    public void setSupportMM(boolean supportMM) {
        this.supportMM = supportMM;
    }

    public boolean isSupportMFX() {
        return supportMFX;
    }

    public void setSupportMFX(boolean supportMFX) {
        this.supportMFX = supportMFX;
    }

    public boolean isSupportDCC() {
        return supportDCC;
    }

    public void setSupportDCC(boolean supportDCC) {
        this.supportDCC = supportDCC;
    }

    public boolean isSupportSX1() {
        return supportSX1;
    }

    public void setSupportSX1(boolean supportSX1) {
        this.supportSX1 = supportSX1;
    }

    public String getDeviceHostName() {
        return deviceHostName;
    }

    public void setDeviceHostName(String deviceHostName) {
        this.deviceHostName = deviceHostName;
    }

    public String getGfpUid() {
        return gfpUid;
    }

    public void setGfpUid(String gfpUid) {
        this.gfpUid = gfpUid;
    }

    public String getGuiUid() {
        return guiUid;
    }

    public void setGuiUid(String guiUid) {
        this.guiUid = guiUid;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" + "serialNumber=" + serialNumber + ", catalogNumber=" + catalogNumber + ", description=" + description + ", gfpUid=" + gfpUid + ", guiUid=" + guiUid + ", hardwareVersion=" + hardwareVersion + ", deviceHostName=" + deviceHostName + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.serialNumber);
        hash = 29 * hash + Objects.hashCode(this.catalogNumber);
        hash = 29 * hash + Objects.hashCode(this.description);
        hash = 29 * hash + Objects.hashCode(this.gfpUid);
        hash = 29 * hash + Objects.hashCode(this.guiUid);
        hash = 29 * hash + Objects.hashCode(this.hardwareVersion);
        hash = 29 * hash + Objects.hashCode(this.softwareVersion);
        hash = 29 * hash + this.maxFunctions;
        hash = 29 * hash + (this.supportMM ? 1 : 0);
        hash = 29 * hash + (this.supportMFX ? 1 : 0);
        hash = 29 * hash + (this.supportDCC ? 1 : 0);
        hash = 29 * hash + (this.supportSX1 ? 1 : 0);
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
        final DeviceInfo other = (DeviceInfo) obj;
        if (this.maxFunctions != other.maxFunctions) {
            return false;
        }
        if (this.supportMM != other.supportMM) {
            return false;
        }
        if (this.supportMFX != other.supportMFX) {
            return false;
        }
        if (this.supportDCC != other.supportDCC) {
            return false;
        }
        if (this.supportSX1 != other.supportSX1) {
            return false;
        }
        if (!Objects.equals(this.serialNumber, other.serialNumber)) {
            return false;
        }
        if (!Objects.equals(this.catalogNumber, other.catalogNumber)) {
            return false;
        }
        if (!Objects.equals(this.gfpUid, other.gfpUid)) {
            return false;
        }
        if (!Objects.equals(this.guiUid, other.guiUid)) {
            return false;
        }
        if (!Objects.equals(this.hardwareVersion, other.hardwareVersion)) {
            return false;
        }
        if (!Objects.equals(this.softwareVersion, other.softwareVersion)) {
            return false;
        }
        return Objects.equals(this.description, other.description);
    }

}
