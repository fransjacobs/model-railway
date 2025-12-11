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
import java.util.List;
import java.util.Objects;

/**
 *
 */
@Table(name = "stations")
public class StationBean {

  private String id;
  private String name;
  private Integer locomotiveCount;
  private Integer minLocomotives;
  private boolean fifo;

  private List<StationBlockBean> blocks;

  public StationBean() {
    blocks = new ArrayList<>();
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

  @Column(name = "loc_count", columnDefinition = "integer default 0")
  public Integer getLocomotiveCount() {
    return locomotiveCount;
  }

  public void setLocomotiveCount(Integer locomotiveCount) {
    this.locomotiveCount = locomotiveCount;
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
    return blocks;
  }

  public void setBlocks(List<StationBlockBean> blocks) {
    this.blocks = blocks;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 59 * hash + Objects.hashCode(this.id);
    hash = 59 * hash + Objects.hashCode(this.name);
    hash = 59 * hash + Objects.hashCode(this.locomotiveCount);
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
    if (!Objects.equals(this.locomotiveCount, other.locomotiveCount)) {
      return false;
    }
    return Objects.equals(this.minLocomotives, other.minLocomotives);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("StationBean{");
    sb.append("id=").append(id);
    sb.append(", name=").append(name);
    sb.append('}');
    return sb.toString();
  }

}
