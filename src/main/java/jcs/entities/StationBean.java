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
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
@Table(name = "stations")
public class StationBean {

  private String id;
  private String name;
  private Integer minLocomotives;
  private boolean fifo;

  private final Map<String, StationBlockBean> stationBlockBeans;
  private final List<BlockBean> blockBeans;

  public StationBean() {
    stationBlockBeans = new HashMap<>();
    blockBeans = new ArrayList<>();
  }

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Column(name = "name", length = 255, nullable = false)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Column(name = "min_locs", columnDefinition = "integer default 0")
  public Integer getMinLocomotives() {
    return minLocomotives;
  }

  public void setMinLocomotives(Integer minLocomotives) {
    this.minLocomotives = minLocomotives;
  }

  @Column(name = "use_fifo", columnDefinition = "use_fifo bool default '0'")
  public boolean isFifo() {
    return fifo;
  }

  public void setFifo(boolean fifo) {
    this.fifo = fifo;
  }

  @Transient
  public List<StationBlockBean> getBlocks() {
    List<StationBlockBean> stationBlockList = new ArrayList(stationBlockBeans.values());

    stationBlockList.sort(Comparator.comparing(StationBlockBean::getLastUpdated));
    return stationBlockList;
  }

  @Transient
  public StationBlockBean getStationBlockBean(BlockBean blockBean) {
    return this.stationBlockBeans.get(blockBean.getId());
  }

  @Transient
  public List<BlockBean> getBlockBeans() {
    List<StationBlockBean> stationBlockList = new ArrayList(stationBlockBeans.values());
    List<BlockBean> blockList = new ArrayList<>();
    for (StationBlockBean sbb : stationBlockList) {
      blockList.add(sbb.getBlock());
    }
    return blockList;
  }

  public void setBlocks(List<StationBlockBean> blocks) {
    this.stationBlockBeans.clear();
    this.blockBeans.clear();
    for (StationBlockBean sbb : blocks) {
      this.stationBlockBeans.put(sbb.getBlockId(), sbb);
      this.blockBeans.add(sbb.getBlock());
    }
  }

  public void addBlock(BlockBean block) {
    if (!this.blockBeans.contains(block)) {
      this.blockBeans.add(block);
      StationBlockBean sbb = new StationBlockBean(this, block);
      sbb.setLastUpdated(new Date());
      this.stationBlockBeans.put(sbb.getBlockId(), sbb);
    }
  }

  public void removeBlock(BlockBean block) {
    this.blockBeans.remove(block);
    this.stationBlockBeans.remove(block.getId());
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 59 * hash + Objects.hashCode(this.id);
    hash = 59 * hash + Objects.hashCode(this.name);
    hash = 59 * hash + Objects.hashCode(this.minLocomotives);
    hash = 59 * hash + (this.fifo ? 1 : 0);
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
    final StationBean other = (StationBean) obj;
    if (this.fifo != other.fifo) {
      return false;
    }
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    return Objects.equals(this.minLocomotives, other.minLocomotives);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (name != null && !"".equals(name)) {
      return name;
    } else {
      return id;
    }
  }

  public String toLogString() {
    StringBuilder sb = new StringBuilder();
    sb.append("StationBean{");
    sb.append("id=").append(id);
    sb.append(", name=").append(name);
    sb.append('}');
    return sb.toString();
  }

}
