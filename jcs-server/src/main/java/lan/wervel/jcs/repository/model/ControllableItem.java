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
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public abstract class ControllableItem implements Serializable {

  private static final long serialVersionUID = -4500291960362508720L;

  protected Integer address;
  protected String name;
  protected String description;
  protected String catalogNumber;
  protected transient List<AttributeChangeListener> attributeChangeListeners;
  private boolean handleChange = true;

  protected ControllableItem(Integer address, String catalogNumber) {
    this(address, null, null, catalogNumber);
  }

  protected ControllableItem(Integer address, String name, String description, String catalogNumber) {
    this.name = name;
    this.description = description;
    this.address = address;
    this.catalogNumber = catalogNumber;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCatalogNumber() {
    return catalogNumber;
  }

  public void setCatalogNumber(String catalogNumber) {
    this.catalogNumber = catalogNumber;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setAddress(Integer address) {
    this.address = address;
  }

  public Integer getAddress() {
    return this.address;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 79 * hash + Objects.hashCode(this.address);
    hash = 79 * hash + Objects.hashCode(this.name);
    hash = 79 * hash + Objects.hashCode(this.description);
    hash = 79 * hash + Objects.hashCode(this.catalogNumber);
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
    final ControllableItem other = (ControllableItem) obj;
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.description, other.description)) {
      return false;
    }
    if (!Objects.equals(this.catalogNumber, other.catalogNumber)) {
      return false;
    }
    return Objects.equals(this.address, other.address);
  }

  protected AttributeChangedEvent getAttributeChangedEvent(String attribute, Object oldValue, Object newValue) {
    return new AttributeChangedEvent(this, attribute, oldValue, newValue);
  }

  protected void handleAttributeChange(String attribute, Object oldValue, Object newValue) {
    if (handleChange && this.attributeChangeListeners != null && !attributeChangeListeners.isEmpty()) {
      AttributeChangedEvent evt = getAttributeChangedEvent(attribute, oldValue, newValue);

      attributeChangeListeners.forEach((listener) -> {
        listener.controllableItemChange(evt);
      });
    }
  }

  public void addAttributeChangeListener(AttributeChangeListener listener) {
    if (this.attributeChangeListeners == null) {
      this.attributeChangeListeners = new ArrayList<>();
    }
    this.attributeChangeListeners.add(listener);
  }

  public void removeAttributeChangeListener(AttributeChangeListener listener) {
    if (this.attributeChangeListeners == null) {
      return;
    }

    this.attributeChangeListeners.remove(listener);
  }

  public void setEnableAttributeChangeHandling(boolean flag) {
    this.handleChange = flag;
  }

  public boolean isHandlingChanges() {
    return handleChange && this.attributeChangeListeners != null && !attributeChangeListeners.isEmpty();
  }

  public abstract ControllableItem copy();
}
