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

import com.dieselpoint.norm.DbException;
import com.dieselpoint.norm.Query;
import com.dieselpoint.norm.Util;
import com.dieselpoint.norm.sqlmakers.Property;
import com.dieselpoint.norm.sqlmakers.SqlMaker;
import com.dieselpoint.norm.sqlmakers.StandardPojoInfo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.Column;

/**
 * Due to a bug in the current SQLMaker: https://github.com/dieselpoint/norm/issues/52 I created this this class to work around the
 * issue
 *
 * @author frans
 */
public class H2SqlMaker implements SqlMaker {

  private static final ConcurrentHashMap<Class<?>, StandardPojoInfo> map = new ConcurrentHashMap<>();

  @Override
  public synchronized StandardPojoInfo getPojoInfo(Class<?> rowClass) {
    StandardPojoInfo pi = map.get(rowClass);
    if (pi == null) {
      pi = new StandardPojoInfo(rowClass);
      map.put(rowClass, pi);

      makeInsertSql(pi);
      makeUpsertSql(pi);
      makeUpdateSql(pi);
      makeSelectColumns(pi);
    }
    return pi;
  }

  @Override
  public String getInsertSql(Query query, Object row) {
    StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
    return String.format(pojoInfo.insertSql, Objects.requireNonNullElse(query.getTable(), pojoInfo.table));
  }

  @Override
  public Object[] getInsertArgs(Query query, Object row) {
    StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
    Object[] args = new Object[pojoInfo.insertSqlArgCount];
    for (int i = 0; i < pojoInfo.insertSqlArgCount; i++) {
      args[i] = pojoInfo.getValue(row, pojoInfo.insertColumnNames[i]);
    }
    return args;
  }

  @Override
  public String getUpdateSql(Query query, Object row) {
    StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
    if (pojoInfo.primaryKeyNames.isEmpty()) {
      throw new DbException("No primary keys specified in the row. Use the @Id annotation.");
    }
    return String.format(pojoInfo.updateSql, Objects.requireNonNullElse(query.getTable(), pojoInfo.table));
  }

  @Override
  public Object[] getUpdateArgs(Query query, Object row) {
    StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());

    int numKeys = pojoInfo.primaryKeyNames.size();

    Object[] args = new Object[pojoInfo.updateSqlArgCount];
    for (int i = 0; i < pojoInfo.updateSqlArgCount - numKeys; i++) {
      args[i] = pojoInfo.getValue(row, pojoInfo.updateColumnNames[i]);
    }
    // add the value for the where clause to the end
    for (int i = 0; i < numKeys; i++) {
      Object pk = pojoInfo.getValue(row, pojoInfo.primaryKeyNames.get(i));
      args[pojoInfo.updateSqlArgCount - (numKeys - i)] = pk;
    }
    return args;
  }

  public void makeUpdateSql(StandardPojoInfo pojoInfo) {

    ArrayList<String> cols = new ArrayList<>();
    for (Property prop : pojoInfo.propertyMap.values()) {

      if (prop.isPrimaryKey) {
        continue;
      }

      if (prop.isGenerated) {
        continue;
      }

      cols.add(prop.name);
    }
    pojoInfo.updateColumnNames = cols.toArray(String[]::new);
    pojoInfo.updateSqlArgCount = pojoInfo.updateColumnNames.length + pojoInfo.primaryKeyNames.size(); // + # of

    StringBuilder buf = new StringBuilder();
    buf.append("update %s set ");

    for (int i = 0; i < cols.size(); i++) {
      if (i > 0) {
        buf.append(',');
      }
      buf.append(cols.get(i)).append("=?");
    }
    buf.append(" where ");

    for (int i = 0; i < pojoInfo.primaryKeyNames.size(); i++) {
      if (i > 0) {
        buf.append(" and ");
      }
      buf.append(pojoInfo.primaryKeyNames.get(i)).append("=?");
    }

    pojoInfo.updateSql = buf.toString();
  }

  public void makeInsertSql(StandardPojoInfo pojoInfo) {
    ArrayList<String> cols = new ArrayList<>();
    for (Property prop : pojoInfo.propertyMap.values()) {
      if (prop.isGenerated) {
        continue;
      }
      cols.add(prop.name);
    }
    pojoInfo.insertColumnNames = cols.toArray(String[]::new);
    pojoInfo.insertSqlArgCount = pojoInfo.insertColumnNames.length;

    pojoInfo.insertSql = "insert into %s (" + Util.join(pojoInfo.insertColumnNames)
            + // comma sep list?
            ") values (" + Util.getQuestionMarks(pojoInfo.insertSqlArgCount) + ")";
  }

  public void makeUpsertSql(StandardPojoInfo pojoInfo) {
  }

  private void makeSelectColumns(StandardPojoInfo pojoInfo) {
    if (pojoInfo.propertyMap.isEmpty()) {
      // this applies if the rowClass is a Map
      pojoInfo.selectColumns = "*";
    } else {
      ArrayList<String> cols = new ArrayList<>();
      for (Property prop : pojoInfo.propertyMap.values()) {
        cols.add(prop.name);
      }
      pojoInfo.selectColumns = Util.join(cols);
    }
  }

  @Override
  public String getSelectSql(Query query, Class<?> rowClass) {
    // unlike insert and update, this needs to be done dynamically
    // and can't be precalculated because of the where and order by
    StandardPojoInfo pojoInfo = getPojoInfo(rowClass);
    String columns = pojoInfo.selectColumns;

    String where = query.getWhere();
    String table = query.getTable();
    if (table == null) {
      table = pojoInfo.table;
    }
    String orderBy = query.getOrderBy();

    StringBuilder out = new StringBuilder();
    out.append("select ");
    out.append(columns);
    out.append(" from ");
    out.append(table);
    if (where != null) {
      out.append(" where ");
      out.append(where);
    }
    if (orderBy != null) {
      out.append(" order by ");
      out.append(orderBy);
    }
    return out.toString();
  }

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

  protected String getColType(Class<?> dataType, int length, int precision, int scale) {
    String colType;

    if (dataType.equals(Integer.class) || dataType.equals(int.class)) {
      colType = "integer";

    } else if (dataType.equals(Long.class) || dataType.equals(long.class)) {
      colType = "bigint";

    } else if (dataType.equals(Double.class) || dataType.equals(double.class)) {
      colType = "double";

    } else if (dataType.equals(Float.class) || dataType.equals(float.class)) {
      colType = "float";

    } else if (dataType.equals(BigDecimal.class)) {
      colType = "decimal(" + precision + "," + scale + ")";

    } else if (dataType.equals(java.util.Date.class)) {
      colType = "datetime";

    } else if (dataType.equals(Boolean.class) || dataType.equals(boolean.class)) {
      colType = "integer";
    } else {
      colType = "varchar(" + length + ")";
    }
    return colType;
  }

  @Override
  public Object convertValue(Object value, String columnTypeName) {
    return value;
  }

  @Override
  public String getDeleteSql(Query query, Object row) {

    StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());

    String table = query.getTable();
    if (table == null) {
      table = pojoInfo.table;
      if (table == null) {
        throw new DbException("You must specify a table name");
      }
    }

    StringBuilder builder = new StringBuilder("delete from ");
    builder.append(table).append(" where ");
    for (int i = 0; i < pojoInfo.primaryKeyNames.size(); i++) {
      if (i > 0) {
        builder.append(" and ");
      }
      builder.append(pojoInfo.primaryKeyNames.get(i)).append("=?");
    }

    return builder.toString();
  }

  @Override
  public Object[] getDeleteArgs(Query query, Object row) {
    StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
    Object[] args = new Object[pojoInfo.primaryKeyNames.size()];

    for (int i = 0; i < pojoInfo.primaryKeyNames.size(); i++) {
      Object primaryKeyValue = pojoInfo.getValue(row, pojoInfo.primaryKeyNames.get(i));
      args[i] = primaryKeyValue;
    }
    return args;
  }

  @Override
  public String getUpsertSql(Query query, Object row) {
    String msg = "There's no standard upsert implemention.";
    throw new UnsupportedOperationException(msg);
  }

  @Override
  public Object[] getUpsertArgs(Query query, Object row) {
    throw new UnsupportedOperationException();
  }

}
