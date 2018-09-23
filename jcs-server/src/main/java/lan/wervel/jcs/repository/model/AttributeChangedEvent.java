/*
 * Copyright (C) 2018 Frans Jacobs.
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
package lan.wervel.jcs.repository.model;

import java.io.Serializable;
import java.util.Objects;
import lan.wervel.jcs.common.TrackRepository;

/**
 *
 * @author frans
 */
public class AttributeChangedEvent implements Serializable {

  private static final long serialVersionUID = -2223867350604005955L;

  private final ControllableItem source;
  private final String attribute;
  private final Object oldValue;
  private final Object newValue;
  private final Item changedItem;
  private transient TrackRepository repository;

  public enum Item {
    LOCOMOTIVE, CRANE, S88, SOLENOIDACCESSOIRY
  }

  public AttributeChangedEvent(ControllableItem source, String attribute, Object oldValue, Object newValue) {
    this.source = source;
    this.attribute = attribute;
    this.oldValue = oldValue;
    this.newValue = newValue;

    switch (source.getClass().getSimpleName()) {
      case "Locomotive":
        changedItem = Item.LOCOMOTIVE;
        break;
      case "Crane":
        changedItem = Item.CRANE;
        break;
      case "FeedbackModule":
        changedItem = Item.S88;
        break;
      case "SolenoidAccessoiry":
        changedItem = Item.SOLENOIDACCESSOIRY;
        break;
      default:
        changedItem = null;
        break;
    }
  }

  public ControllableItem getSource() {
    return source;
  }

  public String getAttribute() {
    return attribute;
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 47 * hash + Objects.hashCode(this.source);
    hash = 47 * hash + Objects.hashCode(this.attribute);
    hash = 47 * hash + Objects.hashCode(this.oldValue);
    hash = 47 * hash + Objects.hashCode(this.newValue);
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
    final AttributeChangedEvent other = (AttributeChangedEvent) obj;
    if (!Objects.equals(this.attribute, other.attribute)) {
      return false;
    }
    if (!Objects.equals(this.source, other.source)) {
      return false;
    }
    if (!Objects.equals(this.oldValue, other.oldValue)) {
      return false;
    }
    return Objects.equals(this.newValue, other.newValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.source.getClass().getSimpleName());
    sb.append(" [");
    sb.append(this.source.getAddress());
    sb.append("] ");
    sb.append(this.attribute);
    sb.append(": ");
    sb.append(this.oldValue);
    sb.append(" -> ");
    sb.append(this.newValue);
    return sb.toString();
  }

  public Item getItemType() {
    return changedItem;
  }

  public TrackRepository getRepository() {
    return repository;
  }

  public void setRepository(TrackRepository repository) {
    this.repository = repository;
  }
}
