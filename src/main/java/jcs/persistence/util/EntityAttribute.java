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
package jcs.persistence.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.EnumType;

public class EntityAttribute {

  String name;
  Method readMethod;
  Method writeMethod;
  Field field;
  Class<?> dataType;
  boolean isPrimaryKey;
  boolean isEnumField;
  Class<Enum> enumClass;
  EnumType enumType;
  boolean show = true;
  int columnPosition;
  int width;
  

  public EntityAttribute() {

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Method getReadMethod() {
    return readMethod;
  }

  public void setReadMethod(Method readMethod) {
    this.readMethod = readMethod;
  }

  public Method getWriteMethod() {
    return writeMethod;
  }

  public void setWriteMethod(Method writeMethod) {
    this.writeMethod = writeMethod;
  }

  public Field getField() {
    return field;
  }

  public void setField(Field field) {
    this.field = field;
  }

  public Class<?> getDataType() {
    return dataType;
  }

  public void setDataType(Class<?> dataType) {
    this.dataType = dataType;
  }

  public boolean isIsPrimaryKey() {
    return isPrimaryKey;
  }

  public void setIsPrimaryKey(boolean isPrimaryKey) {
    this.isPrimaryKey = isPrimaryKey;
  }

  public boolean isIsEnumField() {
    return isEnumField;
  }

  public void setIsEnumField(boolean isEnumField) {
    this.isEnumField = isEnumField;
  }

  public Class<Enum> getEnumClass() {
    return enumClass;
  }

  public void setEnumClass(Class<Enum> enumClass) {
    this.enumClass = enumClass;
  }

  public EnumType getEnumType() {
    return enumType;
  }

  public void setEnumType(EnumType enumType) {
    this.enumType = enumType;
  }

  public boolean isShow() {
    return show;
  }

  public void setShow(boolean show) {
    this.show = show;
  }

  public int getColumnPosition() {
    return columnPosition;
  }

  public void setColumnPosition(int columnPosition) {
    this.columnPosition = columnPosition;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  @Override
  public String toString() {
    return "EntityAttribute{" + "name=" + name + ", readMethod=" + readMethod + ", writeMethod=" + writeMethod + ", field=" + field + ", dataType=" + dataType + ", isPrimaryKey=" + isPrimaryKey + '}';
  }

}
