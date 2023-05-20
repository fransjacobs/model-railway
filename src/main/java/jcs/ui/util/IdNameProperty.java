/*
 * Copyright 2023 frans.
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
package jcs.ui.util;

import java.util.Objects;

/**
 *
 * @author frans
 */
public class IdNameProperty {

  private Long id;
  private String sid;
  private String name;

  public IdNameProperty() {

  }

  public IdNameProperty(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public IdNameProperty(String sid, String name) {
    this.sid = sid;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSid() {
    return sid;
  }

  public void setSid(String sid) {
    this.sid = sid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + Objects.hashCode(this.id);
    hash = 67 * hash + Objects.hashCode(this.sid);
    hash = 67 * hash + Objects.hashCode(this.name);
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
    final IdNameProperty other = (IdNameProperty) obj;
    if (!Objects.equals(this.sid, other.sid)) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    return Objects.equals(this.id, other.id);
  }

}
