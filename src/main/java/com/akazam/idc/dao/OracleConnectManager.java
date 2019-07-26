package com.akazam.idc.dao;

import com.akazam.idc.model.ConnConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class OracleConnectManager {
    private static final Logger logger = LoggerFactory.getLogger(OracleConnectManager.class);
    private Connection conn;
    public Connection getConn(ConnConfig connConfig)
    {
        try
        {
            Class.forName(connConfig.getDriverClassName());
            conn = DriverManager.getConnection(connConfig.getUrl(),connConfig.getUsername(),connConfig.getPassword());
        }
        catch (Exception e)
        {
            logger.error("",e);
            return null;
        }
        return conn;
    }
}
