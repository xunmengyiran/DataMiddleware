package com.akazam.idc.dao;

import com.akazam.idc.model.ConnConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class ConnConfigDao {

    @Autowired
    private JdbcTemplate mysqlTemplate;

    public ConnConfig loadConnConfig(int datasource) {
        String sql = "SELECT * FROM conn_config c WHERE c.datasource = ?";

        return mysqlTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(ConnConfig.class), datasource);
    }

    public Integer saveConnConfig(final ConnConfig saveTO) {
    	
        if (saveTO.getId() == 0 || saveTO.getId() == 0l) {
            final String sql = "INSERT INTO conn_config (URL,DRIVERCLASSNAME,USERNAME,PASSOWRD,TYPE) "
                    + "VALUES(?, ?, ?, ?, ?) ";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            mysqlTemplate.update(new PreparedStatementCreator() {

                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement arg0 = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                    arg0.setObject(1, saveTO.getUrl());
                    arg0.setObject(2, saveTO.getDriverClassName());
                    arg0.setObject(3, saveTO.getUsername());
                    arg0.setObject(4, saveTO.getPassword());
                    arg0.setObject(5, saveTO.getDatasource());
                    return arg0;
                }
            }, keyHolder);

            return keyHolder.getKey().intValue();
        } else {
            String sql = "UPDATE conn_config SET URL = ?, DRIVERCLASSNAME = ?, USERNAME = ?, "
                    + "PASSOWRD = ?, TYPE = ? WHERE ID = ?";
            mysqlTemplate.update(sql, new PreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement arg0) throws SQLException {
                    arg0.setObject(1, saveTO.getUrl());
                    arg0.setObject(2, saveTO.getDriverClassName());
                    arg0.setObject(3, saveTO.getUsername());
                    arg0.setObject(4, saveTO.getPassword());
                    arg0.setObject(5, saveTO.getDatasource());
                    arg0.setObject(6, saveTO.getId());
                }
            });
            return saveTO.getId();
        }
    }
}
