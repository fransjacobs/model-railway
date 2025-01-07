/*
 * Copyright 2023 Frans Jacobs
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

import com.dieselpoint.norm.ColumnOrder;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcs.JcsException;

/**
 * Derive Bean info
 *
 * @author frans
 * @param <T>
 */
public class EntityInfo<T> {

  private boolean ignoreTransient;
  private Map<String, EntityAttribute> attributeMap = new HashMap<>();

  List<String> displayColumnList;

  public EntityInfo(Class<?> clazz) {
    this(clazz, Collections.EMPTY_LIST, false);
  }

  public EntityInfo(Class<?> clazz, boolean ignoreTransient) {
    this(clazz, Collections.EMPTY_LIST, ignoreTransient);
  }

  public EntityInfo(Class<?> clazz, String[] displayColumns) {
    this(clazz, Arrays.asList(displayColumns), false);
  }

  public EntityInfo(Class<?> clazz, String[] displayColumns, boolean ignoreTransient) {
    this(clazz, Arrays.asList(displayColumns), ignoreTransient);
  }

  public EntityInfo(Class<?> clazz, List<String> displayColumnList, boolean ignoreTransient) {
    this.displayColumnList = displayColumnList;
    this.ignoreTransient = ignoreTransient;
    try {
      if (!Map.class.isAssignableFrom(clazz)) {
        List<EntityAttribute> attributes = populateAttributes(clazz);
        ColumnOrder colOrder = clazz.getAnnotation(ColumnOrder.class);

        if (colOrder != null) {
          String[] cols = colOrder.value();
          List<EntityAttribute> reordered = new ArrayList<>();

          for (String col : cols) {
            for (EntityAttribute attr : attributes) {
              if (attr.name.equals(col)) {
                reordered.add(attr);
                break;
              }
            }
          }
          attributes = reordered;
        }

        for (EntityAttribute attr : attributes) {
          if (attributeMap.put(attr.name, attr) != null) {
            throw new JcsException("Duplicate bean attribute found: '" + attr.name + "' in " + clazz.getName() + ". There may be both a field and a getter/setter");
          }
        }
      }
      //Apply the show or not to show criteria
      applyDisplayColumnSettings();

    } catch (IntrospectionException | IllegalAccessException | InstantiationException | JcsException t) {
      throw new JcsException(t);
    }
  }

  private void applyDisplayColumnSettings() {
    if (this.displayColumnList != null && !this.displayColumnList.isEmpty()) {
      for (EntityAttribute attr : this.attributeMap.values()) {
        attr.show = displayColumnList.contains(attr.name);
      }
    }
  }

  private List<EntityAttribute> getSortedAttributes() {
    List<EntityAttribute> eal = new ArrayList<>(attributeMap.values());
    Collections.sort(eal, (a1, a2) -> a1.getColumnPosition() - a2.getColumnPosition());
    return eal;
  }

  private List<EntityAttribute> populateAttributes(Class<?> clazz) throws IntrospectionException, InstantiationException, IllegalAccessException {
    List<EntityAttribute> props = new ArrayList<>();

    for (Field field : clazz.getFields()) {
      int modifiers = field.getModifiers();

      if (Modifier.isPublic(modifiers)) {

        if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
          continue;
        }

        if (!ignoreTransient && field.getAnnotation(Transient.class) != null) {
          continue;
        }

        EntityAttribute attr = new EntityAttribute();

        attr.name = field.getName();
        attr.field = field;
        attr.dataType = field.getType();

        applyAnnotations(attr, field);
        props.add(attr);
      }
    }

    BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
    PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();

    for (PropertyDescriptor descriptor : descriptors) {
      Method readMethod = descriptor.getReadMethod();
      if (readMethod == null) {
        continue;
      }

      if (!ignoreTransient && readMethod.getAnnotation(Transient.class) != null) {
        continue;
      }

      EntityAttribute attr = new EntityAttribute();
      attr.name = descriptor.getName();
      attr.readMethod = readMethod;
      attr.writeMethod = descriptor.getWriteMethod();
      attr.dataType = descriptor.getPropertyType();

      applyAnnotations(attr, attr.readMethod);

      props.add(attr);
    }
    return props;
  }

  private void applyAnnotations(EntityAttribute attr, AnnotatedElement ae) throws InstantiationException, IllegalAccessException {
    Column col = ae.getAnnotation(Column.class);
    if (col != null) {
      String name = col.name().trim();
      if (name.length() > 0) {
        attr.name = name;
      }
    }

    if (ae.getAnnotation(Id.class) != null) {
      attr.isPrimaryKey = true;
      //primaryKeyNames.add(attr.name);
    }

    if (attr.dataType.isEnum()) {
      attr.isEnumField = true;
      attr.enumClass = (Class<Enum>) attr.dataType;

      attr.enumType = EnumType.STRING;
      if (ae.getAnnotation(Enumerated.class) != null) {
        attr.enumType = ae.getAnnotation(Enumerated.class).value();
      }
    }

    ColumnPosition colPos = ae.getAnnotation(ColumnPosition.class);
    if (colPos != null) {
      int position = colPos.position();
      attr.columnPosition = position;
    }
  }

  public Object getValue(T bean, String name) {
    try {
      EntityAttribute attr = attributeMap.get(name);
      if (attr == null) {
        throw new JcsException("No such field: " + name);
      }

      Object value = null;

      if (attr.readMethod != null) {
        value = attr.readMethod.invoke(bean);

      } else if (attr.field != null) {
        value = attr.field.get(bean);
      }

      if (value != null) {
        if (attr.isEnumField) {
          if (attr.enumType == EnumType.ORDINAL) {
            value = ((Enum) value).ordinal();
          } else {
            value = value.toString();
          }
        }
      }

      return value;
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException t) {
      throw new JcsException(t);
    }
  }

  public void putValue(T bean, String name, Object value) {
    putValue(bean, name, value, false);
  }

  public void putValue(Object bean, String name, Object value, boolean ignoreIfMissing) {
    EntityAttribute attr = attributeMap.get(name);
    if (attr == null) {
      if (ignoreIfMissing) {
        return;
      }
      throw new JcsException("No such field: " + name);
    }

    if (value != null) {
      if (attr.writeMethod != null) {
        try {
          if (value instanceof BigInteger && attr.writeMethod.getParameterCount() >= 1) {
            Class type = attr.writeMethod.getParameterTypes()[0];

            if (type.equals(Long.TYPE) || type.equals(Long.class
            )) {
              value = ((BigInteger) value).longValue();
            }
          }
          attr.writeMethod.invoke(bean, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          throw new JcsException("Could not write value " + value + " into bean: " + bean.getClass().getSimpleName() + " Attribute: " + attr.name + " method: " + attr.writeMethod.toString() + " value type: " + value.getClass().getSimpleName(), e);
        }
        return;

      }

      if (attr.field != null) {
        try {
          if (value instanceof BigInteger bigInteger) {
            if (attr.field.getType().equals(Long.TYPE) || attr.field.getType().equals(Long.class
            )) {
              value = bigInteger.longValue();
            }
          }
          attr.field.set(bean, value);
        } catch (IllegalAccessException | IllegalArgumentException e) {
          throw new JcsException("Could not set value " + value + " into bean: " + bean.getClass().getSimpleName() + " Field: " + attr.field.toString() + " value type: " + value.getClass().getSimpleName(), e);
        }
      }
    }
  }

  public List<String> getPrimaryKeyNames() {
    List<EntityAttribute> eal = getSortedAttributes();
    List<String> pkColumnsList = new ArrayList<>();
    for (EntityAttribute attr : eal) {
      if (attr.isPrimaryKey) {
        pkColumnsList.add(attr.name);
      }
    }
    return pkColumnsList;
  }

  public List<String> getAllColumnNames() {
    List<EntityAttribute> eal = getSortedAttributes();
    List<String> columnList = new ArrayList<>();
    for (EntityAttribute attr : eal) {
      columnList.add(attr.name);
    }
    return columnList;
  }

  public List<String> getColumnNames() {
    List<EntityAttribute> eal = getSortedAttributes();
    List<String> columnList = new ArrayList<>();
    for (EntityAttribute attr : eal) {
      if (attr.isShow()) {
        columnList.add(attr.name);
      }
    }
    return columnList;
  }

  public Class getColumnDataType(String columnName) {
    EntityAttribute ea = this.attributeMap.get(columnName);
    if (ea != null) {
      return ea.dataType;
    } else {
      return null;
    }
  }

  public List<String> getDisplayColumnList() {
    return this.displayColumnList;
  }

  public void setDisplayColumns(String[] displayColumns) {
    setDisplayColumnList(Arrays.asList(displayColumns));
  }

  public void setDisplayColumnList(List<String> displayColumnList) {
    this.displayColumnList = displayColumnList;
    applyDisplayColumnSettings();
  }

  public boolean isIgnoreTransient() {
    return ignoreTransient;
  }

}
