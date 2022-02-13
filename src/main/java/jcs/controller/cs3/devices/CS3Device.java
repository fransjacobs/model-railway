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
package jcs.controller.cs3.devices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jcs.controller.cs3.can.CanMessage;

/**
 *
 * @author Frans Jacobs
 */
public class CS3Device implements Serializable {

    private String serialNumber;
    private String gfpUid;
    private String cs3Uid;
    private String hardwareVersion;
    private String articleNumber;
    private String producer;
    private String product;

    private CS3 cs3;
    private GFP gfp;
    private LinkSxx linkSxx;

    public CS3Device(CS3 cs3, GFP gfp, LinkSxx linkSxx) {
        this.cs3 = cs3;
        this.gfp = gfp;
        this.linkSxx = linkSxx;
    }

    public CS3Device(CanMessage statusRequest) {
        parseMessage(statusRequest);
    }

    public CS3Device(String serialNumber, String gfpUid, String cs3Uid, String hardwareVersion, String articleNumber, String product, String producer) {
        this.serialNumber = serialNumber;
        this.gfpUid = gfpUid;
        this.cs3Uid = cs3Uid;
        this.hardwareVersion = hardwareVersion;
        this.articleNumber = articleNumber;
        this.product = product;
        this.producer = producer;
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
                articleNumber = dataToString(data2);
            }

            if (rl.size() > 2) {
                //Third is description
                byte[] data3 = rl.get(2).getDataBytes();
                product = dataToString(data3);
            }

            if (rl.size() > 3) {
                //Fourth is description
                byte[] data4 = rl.get(3).getDataBytes();
                product = product + dataToString(data4);
                product = product.trim();
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

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getGfpUid() {
        return gfpUid;
    }

    public void setGfpUid(String gfpUid) {
        this.gfpUid = gfpUid;
    }

    public String getCs3Uid() {
        return cs3Uid;
    }

    public void setCs3Uid(String cs3Uid) {
        this.cs3Uid = cs3Uid;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public CS3 getCs3() {
        return cs3;
    }

    public void setCs3(CS3 cs3) {
        this.cs3 = cs3;
    }

    public void setGfp(GFP gfp) {
        this.gfp = gfp;
    }

    public GFP getGfp() {
        return gfp;
    }

    public void setLinkSxx(LinkSxx linkSxx) {
        this.linkSxx = linkSxx;
    }

    public LinkSxx getLinkSxx() {
        return linkSxx;
    }

    @Override
    public String toString() {
        return "CS3Device{" + "serialNumber=" + serialNumber + ", gfpUid=" + gfpUid + ", guiUid=" + cs3Uid + ", hardwareVersion=" + hardwareVersion + ", articleNumber=" + articleNumber + ", producer=" + producer + ", product=" + product + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.serialNumber);
        hash = 29 * hash + Objects.hashCode(this.gfpUid);
        hash = 29 * hash + Objects.hashCode(this.cs3Uid);
        hash = 29 * hash + Objects.hashCode(this.hardwareVersion);
        hash = 29 * hash + Objects.hashCode(this.articleNumber);
        hash = 29 * hash + Objects.hashCode(this.producer);
        hash = 29 * hash + Objects.hashCode(this.product);
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
        final CS3Device other = (CS3Device) obj;
        if (!Objects.equals(this.serialNumber, other.serialNumber)) {
            return false;
        }
        if (!Objects.equals(this.gfpUid, other.gfpUid)) {
            return false;
        }
        if (!Objects.equals(this.cs3Uid, other.cs3Uid)) {
            return false;
        }
        if (!Objects.equals(this.hardwareVersion, other.hardwareVersion)) {
            return false;
        }
        if (!Objects.equals(this.articleNumber, other.articleNumber)) {
            return false;
        }
        if (!Objects.equals(this.producer, other.producer)) {
            return false;
        }
        return Objects.equals(this.product, other.product);
    }

}
