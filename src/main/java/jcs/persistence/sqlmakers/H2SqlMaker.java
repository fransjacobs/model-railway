/*
 * Copyright 2014, Dieselpoint, Inc.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions 
 *  and limitations under the License.
 */
package jcs.persistence.sqlmakers;

import com.dieselpoint.norm.sqlmakers.Property;
import com.dieselpoint.norm.sqlmakers.StandardSqlMaker;
import com.dieselpoint.norm.sqlmakers.StandardPojoInfo;
import jakarta.persistence.Column;

/**
 * Due to a bug in the current SQLMaker: https://github.com/dieselpoint/norm/issues/52 I created this this class to work around the issue
 *
 * @author frans
 */
public class H2SqlMaker extends StandardSqlMaker {

  @Override
  public String getCreateTableSql(Class<?> clazz) {

    StringBuilder buf = new StringBuilder();

    StandardPojoInfo pojoInfo = getPojoInfo(clazz);
    buf.append("create table ");
    buf.append(pojoInfo.table);
    buf.append(" (");

    boolean needsComma = false;
    for (Property prop : pojoInfo.propertyMap.values()) {

      if (needsComma) {
        buf.append(',');
      }
      needsComma = true;

      Column columnAnnot = prop.columnAnnotation;
      if (columnAnnot == null) {

        buf.append(prop.name);
        buf.append(" ");
        if (prop.isGenerated) {
          buf.append(" serial");
        } else {
          buf.append(getColType(prop.dataType, 255, 10, 2));
        }

      } else {
        if (columnAnnot.columnDefinition() != null && columnAnnot.columnDefinition().length() > 0) {
          // let the column def override everything
          buf.append(columnAnnot.columnDefinition());
        } else {
          buf.append(prop.name);
          buf.append(" ");
          if (prop.isGenerated) {
            buf.append(" serial");
          } else {
            buf.append(getColType(prop.dataType, columnAnnot.length(), columnAnnot.precision(), columnAnnot.scale()));
          }

          if (columnAnnot.unique()) {
            buf.append(" unique");
          }

          if (!columnAnnot.nullable()) {
            buf.append(" not null");
          }
        }
      }
    }

    if (!pojoInfo.primaryKeyNames.isEmpty()) {
      buf.append(", primary key (");
      for (int i = 0; i < pojoInfo.primaryKeyNames.size(); i++) {
        if (i > 0) {
          buf.append(",");
        }
        buf.append(pojoInfo.primaryKeyNames.get(i));
      }
      buf.append(")");
    }

    buf.append(")");
    return buf.toString();
  }

}
