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

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "blocks")
public class BlockBean {

    private Long id;
    private Integer tileId;
    private String description;
    private Integer plusSensorId;
    private Integer minSensorId;
    private Integer plusSignalId;
    private Integer minSignalId;
    private Integer locomotiveId;

    @Id
    @GeneratedValue
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "tile_id", nullable = false)
    public Integer getTileId() {
        return tileId;
    }

    public void setTileId(Integer tileId) {
        this.tileId = tileId;
    }

    @Column(name = "description", length = 255)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "plus_sensor_id")
    public Integer getPlusSensorId() {
        return plusSensorId;
    }

    public void setPlusSensorId(Integer plusSensorId) {
        this.plusSensorId = plusSensorId;
    }

    @Column(name = "min_sensor_id")
    public Integer getMinSensorId() {
        return minSensorId;
    }

    public void setMinSensorId(Integer minSensorId) {
        this.minSensorId = minSensorId;
    }

    @Column(name = "plus_signal_id")
    public Integer getPlusSignalId() {
        return plusSignalId;
    }

    public void setPlusSignalId(Integer plusSignalId) {
        this.plusSignalId = plusSignalId;
    }

    @Column(name = "min_signal_id")
    public Integer getMinSignalId() {
        return minSignalId;
    }

    public void setMinSignalId(Integer minSignalId) {
        this.minSignalId = minSignalId;
    }

    @Column(name = "locomotive_id")
    public Integer getLocomotiveId() {
        return locomotiveId;
    }

    public void setLocomotiveId(Integer locomotiveId) {
        this.locomotiveId = locomotiveId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        hash = 53 * hash + Objects.hashCode(this.tileId);
        hash = 53 * hash + Objects.hashCode(this.description);
        hash = 53 * hash + Objects.hashCode(this.plusSensorId);
        hash = 53 * hash + Objects.hashCode(this.minSensorId);
        hash = 53 * hash + Objects.hashCode(this.plusSignalId);
        hash = 53 * hash + Objects.hashCode(this.minSignalId);
        hash = 53 * hash + Objects.hashCode(this.locomotiveId);
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
        final BlockBean other = (BlockBean) obj;
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.tileId, other.tileId)) {
            return false;
        }
        if (!Objects.equals(this.plusSensorId, other.plusSensorId)) {
            return false;
        }
        if (!Objects.equals(this.minSensorId, other.minSensorId)) {
            return false;
        }
        if (!Objects.equals(this.plusSignalId, other.plusSignalId)) {
            return false;
        }
        if (!Objects.equals(this.minSignalId, other.minSignalId)) {
            return false;
        }
        return Objects.equals(this.locomotiveId, other.locomotiveId);
    }
}
