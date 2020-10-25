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
package lan.wervel.jcs.feedback;

import java.io.Serializable;
import java.util.Date;
import lan.wervel.jcs.controller.cs2.FeedbackEventStatus;
import lan.wervel.jcs.entities.FeedbackModule;

/**
 *
 * @author Frans Jacobs
 */
public class FeedbackEvent implements Serializable {

    private int contactId = -1;
    private boolean newValue;
    private boolean oldValue;

    private int moduleNumber;
    private int lsb;
    private int msb;
    private Date changedDate;

    public FeedbackEvent() {
        this(0, 0, 0, null);
    }

    public FeedbackEvent(FeedbackEventStatus feedbackEventStatus) {
        this(feedbackEventStatus.getContactId(), feedbackEventStatus.isNewValue(), feedbackEventStatus.isOldValue());
    }

    public FeedbackEvent(int contactId, boolean newValue, boolean oldValue) {
        this.contactId = contactId;
        this.newValue = newValue;
        this.oldValue = oldValue;
        this.changedDate = new Date();
    }

    public FeedbackEvent(FeedbackModule feedbackModule) {
        this(feedbackModule.getModuleNumber(), feedbackModule.getResponse(), feedbackModule.getLastUpdated());
    }

    public FeedbackEvent(Integer moduleNumber, Integer[] response) {
        this(moduleNumber, response[0], response[1], new Date());
    }

    public FeedbackEvent(Integer moduleNumber, Integer[] response, Date changedDate) {
        this(moduleNumber, response[0], response[1], changedDate);
    }

    public FeedbackEvent(int moduleNumber, int lsb, int msb, Date changedDate) {
        this.moduleNumber = moduleNumber;
        this.lsb = lsb;
        this.msb = msb;
        this.changedDate = changedDate;
    }

    public int getModuleNumber() {
        return moduleNumber;
    }

    public void setModuleNumber(int moduleNumber) {
        this.moduleNumber = moduleNumber;
    }

    public int getLsb() {
        return lsb;
    }

    public void setLsb(int lsb) {
        this.lsb = lsb;
    }

    public int getMsb() {
        return msb;
    }

    public void setMsb(int msb) {
        this.msb = msb;
    }

    public Integer[] getResponse() {
        return new Integer[]{lsb, msb};
    }

    public Date getChangedDate() {
        return changedDate;
    }

    public void setChangedDate(Date changedDate) {
        this.changedDate = changedDate;
    }

    public int getContactId() {
        return contactId;
    }

    public boolean isNewValue() {
        return newValue;
    }

    public boolean isOldValue() {
        return oldValue;
    }

    @Override
    public String toString() {
        if (this.contactId > 0) {
            return "ContactId {" + contactId + ": new: " + newValue + ", old: " + oldValue + "}";
        } else {
            return "S88 {" + moduleNumber + ": [" + msb + "," + lsb + "]}";
        }
    }

}
