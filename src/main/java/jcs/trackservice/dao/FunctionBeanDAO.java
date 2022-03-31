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
package jcs.trackservice.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import jcs.entities.FunctionBean;

/**
 *
 * @author frans
 */
public class FunctionBeanDAO extends AbstractDAO<FunctionBean> {

    private static final String INS_FUN_STMT = "insert into functions (TYPE,FVALUE,NUMBER,LOCOID) values(?,?,?,?)";
    private static final String UPD_FUN_STMT = "update functions set TYPE = ?,FVALUE = ? where LOCOID = ? and NUMBER = ?";

    public FunctionBeanDAO() {
        super();
    }

    @Override
    protected FunctionBean map(ResultSet rs) throws SQLException {
        BigDecimal locoId = new BigDecimal(rs.getLong("LOCOID"));
        Integer number = rs.getInt("NUMBER");
        Integer functionType = rs.getInt("TYPE");
        Integer value = rs.getInt("FVALUE");

        FunctionBean function = new FunctionBean(locoId, number, functionType, value);
        return function;
    }

    @Override
    protected void bind(PreparedStatement ps, FunctionBean function, boolean insert) throws SQLException {
        ps.setInt(1, function.getFunctionType());

        if (function.getValue() != null) {
            ps.setInt(2, function.getValue());
        } else {
            ps.setNull(2, Types.INTEGER);
        }
        ps.setInt(3, function.getNumber());

        ps.setBigDecimal(4, function.getLocomotiveId());
        if (function.getId() != null) {
            ps.setBigDecimal(5, function.getId());
        }
    }

    @Override
    public List<FunctionBean> findAll() {
        String stmt = "select * from functions order by locoid, number asc";
        return this.findAll(stmt);
    }

    public FunctionBean findById(BigDecimal id) {
        String stmt = "select * from functions where id = ?";
        return this.findById(id, stmt);
    }

    public FunctionBean findById(BigDecimal locomotiveId, Integer number) {
        String stmt = "select * from functions where locoid = ? and number = ?";
        return this.findById(locomotiveId, number, stmt);
    }

    public List<FunctionBean> findBy(BigDecimal locomotiveId) {
        String stmt = "select * from functions where locoid = ? order by number";
        return this.findBy(locomotiveId, stmt);
    }

    public void persist(Collection<FunctionBean> functions) {
        for (FunctionBean function : functions) {
            persist(function);
        }
    }

    @Override
    public BigDecimal persist(FunctionBean function) {
        //Check whether the function exists..
        FunctionBean fun = this.findById(function.getLocomotiveId(), function.getNumber());

        String statement;
        if (fun == null) {
            statement = INS_FUN_STMT;
        } else {
            statement = UPD_FUN_STMT;
        }

        upsert(function, statement);

        return function.getId();
    }

    public void remove(Collection<FunctionBean> functions) {
        for (FunctionBean function : functions) {
            remove(function);
        }
    }

    @Override
    public void remove(FunctionBean function) {
        String stmt = "delete from functions where locoid = ? and number = ?";
        this.remove(function.getLocomotiveId(), function.getNumber(), stmt);
    }

}
