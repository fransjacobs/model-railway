/*
 * Copyright (C) 2019 frans.
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
package lan.wervel.jcs.trackservice.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lan.wervel.jcs.entities.JCSProperty;

public class JCSPropertiesDAO extends AbstractDAO<JCSProperty> {

  private static final String INS_PROP_STMT = "insert into JCSPROPERTIES (KEY,VALUE,ID) values(?,?,?)";
  private static final String UPD_PROP_STMT = "update JCSPROPERTIES set KEY = ?, VALUE = ? where ID = ?";

  public JCSPropertiesDAO() {
    super();
  }

  @Override
  protected JCSProperty map(ResultSet rs) throws SQLException {
    String key = rs.getString("KEY");
    String value = rs.getString("VALUE");
    BigDecimal id = rs.getBigDecimal("ID");

    JCSProperty property = new JCSProperty(id, key, value);
    return property;
  }

  @Override
  protected void bind(PreparedStatement ps, JCSProperty property) throws SQLException {
    ps.setString(1, property.getKey());
    ps.setString(2, property.getValue());
    ps.setBigDecimal(3, property.getId());
  }

  @Override
  public List<JCSProperty> findAll() {
    String stmt = "select * from jcsproperties order by id";

    return this.findAll(stmt);
  }

  public JCSProperty find(String key) {
    String stmt = "select * from jcsproperties where key = ?";
    JCSProperty p = this.find(key, stmt);
    return p;
  }

  @Override
  public JCSProperty find(Integer address) {
    String stmt = "select * from jcsproperties where id = ?";
    JCSProperty p = this.find(address, stmt);
    return p;
  }

  @Override
  public BigDecimal persist(JCSProperty property) {
    JCSProperty p = null;
    if (property != null && property.getKey() != null) {
      p = this.find(property.getKey());
    }

    String statement;
    if (p == null) {
      statement = INS_PROP_STMT;
    } else {
      statement = UPD_PROP_STMT;
    }

    upsert(property, statement);

    return property.getId();
  }

  @Override
  public void remove(JCSProperty property) {
    String stmt = "delete from jcsproperties where id = ?";
    this.remove(property, stmt);
  }

}
