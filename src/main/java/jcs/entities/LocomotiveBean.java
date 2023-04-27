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
package jcs.entities;

import java.awt.Image;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import jcs.entities.enums.DecoderType;
import jcs.entities.enums.Direction;

@Table(name = "locomotives")
public class LocomotiveBean implements Serializable {

    private Long id;
    private String name;
    private String previousName;
    private Long uid;
    private Long mfxUid;
    private Integer address;
    private String icon;
    private String decoderTypeString;
    private String mfxSid;
    private Integer tachoMax;
    private Integer vMin;
    private Integer accelerationDelay;
    private Integer brakeDelay;
    private Integer volume;
    private String spm;
    private Integer velocity;
    private Integer richtung;
    private String mfxType;
    private boolean commuter;
    private Integer length;
    private String block;
    private boolean show;

    private Image locIcon;

    private final Map<Integer, FunctionBean> functions;
    //private List<LocomotiveFunction> locomotiveFunctions;

    public LocomotiveBean() {
        functions = new HashMap<>();
    }

    public LocomotiveBean(Long id, String name, Long uid, Long mfxUid, Integer address, String icon, String decoderTypeString,
            String mfxSid, Integer tachoMax, Integer vMin, Integer velocity, Integer direction, boolean commuter, Integer length, boolean show) {

        this(id, name, null, uid, mfxUid, address, icon, decoderTypeString, mfxSid, tachoMax, vMin, null, null, null, null, velocity,
                direction, null, null, commuter, length, show);
    }

    public LocomotiveBean(Long id, String name, String previousName, Long uid,
            Long mfxUid, Integer address, String icon, String decoderTypeString,
            String mfxSid, Integer tachoMax, Integer vMin, Integer accelerationDelay,
            Integer brakeDelay, Integer volume, String spm, Integer velocity,
            Integer direction, String mfxType, String block, boolean commuter, Integer length, boolean show) {

        this.id = id;
        this.name = name;
        this.previousName = previousName;
        this.uid = uid;
        this.mfxUid = mfxUid;
        this.address = address;
        this.icon = icon;
        this.decoderTypeString = decoderTypeString;
        this.mfxSid = mfxSid;
        this.tachoMax = tachoMax;
        this.vMin = vMin;
        this.accelerationDelay = accelerationDelay;
        this.brakeDelay = brakeDelay;
        this.volume = volume;
        this.spm = spm;
        this.velocity = velocity;
        this.richtung = direction;
        this.mfxType = mfxType;
        this.block = block;
        this.commuter = commuter;
        this.length = length;
        this.show = show;

        functions = new HashMap<>();
    }

    @Id
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "name", length = 255, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Transient
    public String getPreviousName() {
        return previousName;
    }

    public void setPreviousName(String previousName) {
        this.previousName = previousName;
    }

    @Column(name = "uid")
    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    @Column(name = "mfx_uid")
    public Long getMfxUid() {
        return mfxUid;
    }

    public void setMfxUid(Long mfxUid) {
        this.mfxUid = mfxUid;
    }

    @Column(name = "address", nullable = false)
    public Integer getAddress() {
        return address;
    }

    public void setAddress(Integer address) {
        this.address = address;
    }

    @Column(name = "icon", length = 255)
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Column(name = "decoder_type", length = 255, nullable = false)
    public String getDecoderTypeString() {
        return decoderTypeString;
    }

    public void setDecoderTypeString(String decoderTypeString) {
        this.decoderTypeString = decoderTypeString;
    }

    @Transient
    public DecoderType getDecoderType() {
        return DecoderType.get(this.decoderTypeString);
    }

    @Column(name = "mfx_sid")
    public String getMfxSid() {
        return mfxSid;
    }

    public void setMfxSid(String mfxSid) {
        this.mfxSid = mfxSid;
    }

    @Column(name = "tacho_max")
    public Integer getTachoMax() {
        return tachoMax;
    }

    public void setTachoMax(Integer tachoMax) {
        this.tachoMax = tachoMax;
    }

    @Column(name = "v_min")
    public Integer getvMin() {
        return vMin;
    }

    public void setvMin(Integer vMin) {
        this.vMin = vMin;
    }

    @Transient
    public Integer getAccelerationDelay() {
        return accelerationDelay;
    }

    public void setAccelerationDelay(Integer accelerationDelay) {
        this.accelerationDelay = accelerationDelay;
    }

    @Transient
    public Integer getBrakeDelay() {
        return brakeDelay;
    }

    public void setBrakeDelay(Integer brakeDelay) {
        this.brakeDelay = brakeDelay;
    }

    @Transient
    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    @Transient
    public String getSpm() {
        return spm;
    }

    @Transient
    public void setSpm(String spm) {
        this.spm = spm;
    }

    @Column(name = "velocity")
    public Integer getVelocity() {
        return velocity;
    }

    public void setVelocity(Integer velocity) {
        this.velocity = velocity;
    }

    @Column(name = "richtung")
    public Integer getRichtung() {
        return richtung;
    }

    public void setRichtung(Integer richtung) {
        this.richtung = richtung;
    }

    @Transient
    public Direction getDirection() {
        if (this.richtung != null) {
            return Direction.getDirection(this.richtung);
        } else {
            return Direction.FORWARDS;
        }
    }

    public void setDirection(Direction direction) {
        this.richtung = direction.getMarklinValue();
    }

    @Transient
    public String getMfxType() {
        return mfxType;
    }

    public void setMfxType(String mfxType) {
        this.mfxType = mfxType;
    }

    @Transient
    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    @Column(name = "commuter", columnDefinition = "commuter bool default '0'")
    public boolean isCommuter() {
        return commuter;
    }

    public void setCommuter(boolean commuter) {
        this.commuter = commuter;
    }

    @Column(name = "length")
    public Integer getLength() {
        return length;
    }

    public void setLength(Integer locLength) {
        this.length = locLength;
    }

    @Column(name = "show", nullable = false, columnDefinition = "show bool default '1'")
    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    @Transient
    public Image getLocIcon() {
        return locIcon;
    }

    public void setLocIcon(Image locIcon) {
        this.locIcon = locIcon;
    }

//    @OneToMany(targetEntity=LocomotiveFunction.class )
//  public List<LocomotiveFunction> getLocomotiveFunctions() {
//    return locomotiveFunctions;
//  }
//  public void setLocomotiveFunctions(List<LocomotiveFunction> locomotiveFunctions) {
//    this.locomotiveFunctions = locomotiveFunctions;
//  }
    @Transient
    public Map<Integer, FunctionBean> getFunctions() {
        return functions;
    }

    public void addFunction(FunctionBean function) {
        this.functions.put(function.getNumber(), function);
    }

    public void addAllFunctions(List<FunctionBean> functions) {
        for (FunctionBean function : functions) {
            this.functions.put(function.getNumber(), function);
        }
    }

    public void replaceAllFunctions(List<FunctionBean> functions) {
        this.functions.clear();
        for (FunctionBean function : functions) {
            this.functions.put(function.getNumber(), function);
        }
    }

    @Transient
    public FunctionBean getFunctionBean(Integer functionNumber) {
        return this.functions.get(functionNumber);
    }

    @Override
    public String toString() {
        //return this.name;
        return toLogString();
    }

    public String toLogString() {
        return "LocomotiveBean{" + "id=" + id + ", name=" + name + ", previousName=" + previousName + ", uid=" + uid + ", mfxUid=" + mfxUid + ", address=" + address + ", icon=" + icon + ", decoderType=" + decoderTypeString + ", mfxSid=" + mfxSid + ", tachoMax=" + tachoMax + ", vMin=" + vMin + ", accelerationDelay=" + accelerationDelay + ", brakeDelay=" + brakeDelay + ", volume=" + volume + ", spm=" + spm + ", velocity=" + velocity + ", richtung=" + richtung + ", mfxType=" + mfxType + ", blocks=" + block + ", locIcon=" + locIcon + '}';
    }

    //Convenience
    @Transient
    public boolean isFunctionValue(Integer number) {
        if (this.functions.containsKey(number)) {
            FunctionBean f = this.functions.get(number);

            return f.getValue() == 1;
        } else {
            return false;
        }
    }

    @Transient
    public boolean hasFunction(Integer number) {
        return this.functions.containsKey(number);
    }

    public void setFunctionValue(Integer number, boolean value) {
        if (this.functions.containsKey(number)) {
            FunctionBean f = this.functions.get(number);
            f.setValue(value ? 1 : 0);
        }
    }

    public void setFunctionValue(Integer number, Integer value) {
        if (this.functions.containsKey(number)) {
            FunctionBean f = this.functions.get(number);
            f.setValue(value);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.previousName);
        hash = 53 * hash + Objects.hashCode(this.uid);
        hash = 53 * hash + Objects.hashCode(this.mfxUid);
        hash = 53 * hash + Objects.hashCode(this.address);
        hash = 53 * hash + Objects.hashCode(this.icon);
        hash = 53 * hash + Objects.hashCode(this.decoderTypeString);
        hash = 53 * hash + Objects.hashCode(this.mfxSid);
        hash = 53 * hash + Objects.hashCode(this.tachoMax);
        hash = 53 * hash + Objects.hashCode(this.vMin);
        hash = 53 * hash + Objects.hashCode(this.accelerationDelay);
        hash = 53 * hash + Objects.hashCode(this.brakeDelay);
        hash = 53 * hash + Objects.hashCode(this.volume);
        hash = 53 * hash + Objects.hashCode(this.spm);
        hash = 53 * hash + Objects.hashCode(this.velocity);
        hash = 53 * hash + Objects.hashCode(this.richtung);
        hash = 53 * hash + Objects.hashCode(this.mfxType);
        hash = 53 * hash + Objects.hashCode(this.block);
        hash = 53 * hash + Objects.hashCode(this.locIcon);
        hash = 53 * hash + Objects.hashCode(this.show);
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
        final LocomotiveBean other = (LocomotiveBean) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
//        if (!Objects.equals(this.previousName, other.previousName)) {
//            return false;
//        }
        if (!Objects.equals(this.icon, other.icon)) {
            return false;
        }
        if (!Objects.equals(this.decoderTypeString, other.decoderTypeString)) {
            return false;
        }
        if (!Objects.equals(this.mfxSid, other.mfxSid)) {
            return false;
        }
//        if (!Objects.equals(this.spm, other.spm)) {
//            return false;
//        }
        if (!Objects.equals(this.mfxType, other.mfxType)) {
            return false;
        }
//        if (!Objects.equals(this.block, other.block)) {
//            return false;
//        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.uid, other.uid)) {
            return false;
        }
        if (!Objects.equals(this.mfxUid, other.mfxUid)) {
            return false;
        }
        if (!Objects.equals(this.address, other.address)) {
            return false;
        }
        if (!Objects.equals(this.tachoMax, other.tachoMax)) {
            return false;
        }
        if (!Objects.equals(this.vMin, other.vMin)) {
            return false;
        }
//        if (!Objects.equals(this.accelerationDelay, other.accelerationDelay)) {
//            return false;
//        }
//        if (!Objects.equals(this.brakeDelay, other.brakeDelay)) {
//            return false;
//        }
//        if (!Objects.equals(this.volume, other.volume)) {
//            return false;
//        }
        if (!Objects.equals(this.velocity, other.velocity)) {
            return false;
        }
        if (!Objects.equals(this.richtung, other.richtung)) {
            return false;
        }
        return Objects.equals(this.show, other.show);
        //return Objects.equals(this.locIcon, other.locIcon);
    }

}
