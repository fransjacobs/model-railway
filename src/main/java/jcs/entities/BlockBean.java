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
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "blocks")
public class BlockBean {

  private String id;
  private String tileId;
  private String description;
  private String plusSensorId;
  private String minSensorId;
  private String plusSignalId;
  private String minSignalId;
  private Long locomotiveId;

  private TileBean tileBean;

  private SensorBean plusSensorBean;
  private SensorBean minSensorBean;
  private AccessoryBean plusSignal;
  private AccessoryBean minSignal;
  private LocomotiveBean locomotive;

  public BlockBean() {

  }

  public BlockBean(TileBean tileBean) {
    this.tileBean = tileBean;
    if (tileBean != null) {
      this.tileId = tileBean.getId();
    }

  }

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Column(name = "tile_id", nullable = false)
  public String getTileId() {
    return tileId;
  }

  public void setTileId(String tileId) {
    this.tileId = tileId;
  }

  @Transient
  public TileBean getTileBean() {
    return tileBean;
  }

  public void setTile(TileBean tileBean) {
    this.tileBean = tileBean;
    if (tileBean != null) {
      this.tileId = tileBean.getId();
    } else {
      this.tileId = null;
    }
  }

  @Column(name = "description", length = 255)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Column(name = "plus_sensor_id")
  public String getPlusSensorId() {
    return plusSensorId;
  }

  public void setPlusSensorId(String plusSensorId) {
    this.plusSensorId = plusSensorId;
  }

  @Transient
  public SensorBean getPlusSensorBean() {
    return plusSensorBean;
  }

  public void setPlusSensorBean(SensorBean plusSensorBean) {
    this.plusSensorBean = plusSensorBean;
    if (plusSensorBean != null) {
      this.plusSensorId = plusSensorBean.getId();
    } else {
      this.plusSensorId = null;
    }
  }

  @Column(name = "min_sensor_id")
  public String getMinSensorId() {
    return minSensorId;
  }

  public void setMinSensorId(String minSensorId) {
    this.minSensorId = minSensorId;
  }

  @Transient
  public SensorBean getMinSensorBean() {
    return minSensorBean;
  }

  public void setMinSensorBean(SensorBean minSensorBean) {
    this.minSensorBean = minSensorBean;
    if (minSensorBean != null) {
      this.minSensorId = minSensorBean.getId();
    } else {
      this.minSensorId = null;
    }
  }

  @Column(name = "plus_signal_id")
  public String getPlusSignalId() {
    return plusSignalId;
  }

  public void setPlusSignalId(String plusSignalId) {
    this.plusSignalId = plusSignalId;
  }

  @Transient
  public AccessoryBean getPlusSignal() {
    return plusSignal;
  }

  public void setPlusSignal(AccessoryBean plusSignal) {
    this.plusSignal = plusSignal;
    if (plusSignal != null) {
      this.plusSignalId = plusSignal.getId();
    } else {
      this.plusSignalId = null;
    }
  }

  @Column(name = "min_signal_id")
  public String getMinSignalId() {
    return minSignalId;
  }

  public void setMinSignalId(String minSignalId) {
    this.minSignalId = minSignalId;
  }

  @Transient
  public AccessoryBean getMinSignal() {
    return minSignal;
  }

  public void setMinSignal(AccessoryBean minSignal) {
    this.minSignal = minSignal;
    if (minSignal != null) {
      this.minSignalId = minSignal.getId();
    } else {
      this.minSignalId = null;
    }
  }

  @Column(name = "locomotive_id")
  public Long getLocomotiveId() {
    return locomotiveId;
  }

  public void setLocomotiveId(Long locomotiveId) {
    this.locomotiveId = locomotiveId;
  }

  @Transient
  public LocomotiveBean getLocomotive() {
    return locomotive;
  }

  public void setLocomotive(LocomotiveBean locomotive) {
    this.locomotive = locomotive;
    if (locomotive != null) {
      this.locomotiveId = locomotive.getId();
    } else {
      this.locomotiveId = null;
    }
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

  @Override
  public String toString() {
    return "BlockBean{" + "id=" + id + ", tileId=" + tileId + ", description=" + description + ", plusSensorId=" + plusSensorId + ", minSensorId=" + minSensorId + ", plusSignalId=" + plusSignalId + ", minSignalId=" + minSignalId + ", locomotiveId=" + locomotiveId + "}";
  }

}
