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
package jcs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Date;
import java.util.Objects;

/**
 *
 */
@Table(name = "station_blocks", indexes = {
  @Index(name = "stbl_stat_blck_un_idx", columnList = "station_id, block_id", unique = true)})
public class StationBlockBean {

  private Long id;
  private String stationId;
  private String blockId;
  private Date lastUpdated;

  private StationBean station;
  private BlockBean block;

  public StationBlockBean() {

  }

  @Id
  @GeneratedValue
  @Column(name = "id")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(name = "station_id", length = 255, nullable = false)
  public String getStationId() {
    return stationId;
  }

  public void setStationId(String stationId) {
    this.stationId = stationId;
  }

  @Column(name = "block_id", length = 255, nullable = false)
  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  @Column(name = "last_updated")
  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Transient
  public StationBean getStation() {
    return station;
  }

  public void setStation(StationBean station) {
    this.station = station;
  }

  @Transient
  public BlockBean getBlock() {
    return block;
  }

  public void setBlock(BlockBean block) {
    this.block = block;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + Objects.hashCode(this.id);
    hash = 59 * hash + Objects.hashCode(this.stationId);
    hash = 59 * hash + Objects.hashCode(this.blockId);
    hash = 59 * hash + Objects.hashCode(this.lastUpdated);
    hash = 59 * hash + Objects.hashCode(this.station);
    hash = 59 * hash + Objects.hashCode(this.block);
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
    final StationBlockBean other = (StationBlockBean) obj;
    if (!Objects.equals(this.stationId, other.stationId)) {
      return false;
    }
    if (!Objects.equals(this.blockId, other.blockId)) {
      return false;
    }
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.lastUpdated, other.lastUpdated)) {
      return false;
    }
    if (!Objects.equals(this.station, other.station)) {
      return false;
    }
    return Objects.equals(this.block, other.block);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("StationBlockBean{");
    sb.append("id=").append(id);
    sb.append(", stationId=").append(stationId);
    sb.append(", blockId=").append(blockId);
    sb.append('}');
    return sb.toString();
  }

}
