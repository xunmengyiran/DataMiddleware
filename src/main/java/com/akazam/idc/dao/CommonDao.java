package com.akazam.idc.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class CommonDao {

    @Autowired
    private JdbcTemplate mysqlTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(){
        return new NamedParameterJdbcTemplate(mysqlTemplate);
    }

    public List<Map<String, Object>> query(String sql) {
        return mysqlTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> query(String sql, Map<String, Object> paramsMap) {
        return namedParameterJdbcTemplate.queryForList(sql, paramsMap);
    }

    public void update(String sql) {
        mysqlTemplate.update(sql);
    }

    public void batchUpdate(String sql, List<Object[]> args) {
        mysqlTemplate.batchUpdate(sql, args);
    }

    public boolean isTableExist(String dbName, String tableName) throws SQLException {
        DatabaseMetaData meta = mysqlTemplate.getDataSource().getConnection().getMetaData();
        ResultSet rsTables = meta.getTables(dbName, null, tableName,
                new String[]{"TABLE"});

        return rsTables.next();
    }
    
    public List<Map<String,Object>> jdbcTemplateQuery(String driverName,String url,String userName,String pwd,String sql){
    	DriverManagerDataSource dataSource=new DriverManagerDataSource();
        dataSource.setDriverClassName(driverName);
        dataSource.setUrl(url);
        dataSource.setUsername(userName);
        dataSource.setPassword(pwd);
       // dataSource.setSchema("idcnm");
        JdbcTemplate jdbcTemplate=new JdbcTemplate(dataSource);
        List<Map<String,Object>> result = (List<Map<String, Object>>) jdbcTemplate.queryForList(sql);
        return result;
    }
}
