package com.akazam.idc.dao;

import com.akazam.idc.model.ImportTaskConfig;
import com.akazam.idc.util.Utils;
import com.akazam.idc.vo.ExcelImportLog;
import com.akazam.idc.vo.ImportConfigRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class ImportTaskConfigDao {

    private Logger log = LoggerFactory.getLogger(ImportTaskConfigDao.class);

    @Autowired
    private JdbcTemplate mysqlTemplate;

    public ImportTaskConfig loadImportTaskConfigByID(String id) {
        String sql = "SELECT id,sqlstr,frequency,url,username,pwd,schema_ ,lasttime,datasource,tableName,label FROM import_task_config where id = ?";
        RowMapper<ImportTaskConfig> rowMapper = new BeanPropertyRowMapper<ImportTaskConfig>(ImportTaskConfig.class);
        return mysqlTemplate.queryForObject(sql, rowMapper, id);
    }

    public int getImportTaskConfigCount(String username) {
        String sql = "SELECT count(0) FROM import_task_config where lastUpdater = ?";
        int count = mysqlTemplate.queryForObject(sql, Integer.class, username);
        log.info("==count==" + count);
        return count;
    }

    public List<ImportTaskConfig> loadImportTaskConfig(int datasource, String username, int page, int rows) {
        String sql = "SELECT label,id,sqlstr,frequency,url,username,pwd,schema_ ,tableName ,datasource FROM import_task_config where lastUpdater = ? limit " + String.valueOf(((page - 1) * rows)) + "," + String.valueOf(rows);
        RowMapper<ImportTaskConfig> rowMapper = new BeanPropertyRowMapper<ImportTaskConfig>(ImportTaskConfig.class);
        List<ImportTaskConfig> result = (List<ImportTaskConfig>) mysqlTemplate.query(sql, rowMapper, username);
        return result;
    }

    public int getExcelImportLogCount(String username) {
        String sql = "SELECT count(0) FROM excel_import_record where importUser = ?";
        int count = mysqlTemplate.queryForObject(sql, Integer.class, username);
        log.info("==count==" + count);
        return count;
    }

    public List<ExcelImportLog> getExcelImportLog(String username, int page, int rows) {
        String sql = "SELECT * FROM excel_import_record where importUser = ? ORDER BY id DESC limit " + String.valueOf(((page - 1) * rows)) + "," + String.valueOf(rows);
        RowMapper<ExcelImportLog> rowMapper = new BeanPropertyRowMapper<ExcelImportLog>(ExcelImportLog.class);
        List<ExcelImportLog> result = (List<ExcelImportLog>) mysqlTemplate.query(sql, rowMapper, username);
        return result;
    }

    public int deleteExcelImportLogById(String ids) throws Exception {
        String sql = "delete from excel_import_record  WHERE id in (" + ids + ")";
        try {
            mysqlTemplate.execute(sql);
            return 1;
        } catch (Exception e) {
            log.error("删除出错" + ids);
            e.printStackTrace();
        }
        return 0;
    }

    public List<ImportConfigRequest> loadAllImportTaskConfig() {
        String sql = "SELECT id,sqlstr,frequency,url,username,pwd,schema_ ,tableName ,datasource FROM import_task_config";
        RowMapper<ImportConfigRequest> rowMapper = new BeanPropertyRowMapper<ImportConfigRequest>(ImportConfigRequest.class);
        List<ImportConfigRequest> result = (List<ImportConfigRequest>) mysqlTemplate.query(sql, rowMapper);
        return result;
    }

    public Integer updateImportTaskConfig(final ImportTaskConfig saveTO) {

        String sql = "UPDATE import_task_config SET SQLSTR = ?, FREQUENCY = ?, url = ?, username = ?,  pwd = ?, schema_ =  ? WHERE DATASOURCE = ?";
        mysqlTemplate.update(sql, new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement arg0) throws SQLException {
                arg0.setObject(1, saveTO.getSqlstr());
                arg0.setObject(2, saveTO.getFrequency());
                arg0.setObject(3, saveTO.getUrl());
                arg0.setObject(4, saveTO.getUsername());
                arg0.setObject(5, saveTO.getPwd());
                arg0.setObject(6, saveTO.getSchema_());
                arg0.setObject(7, saveTO.getDatasource());
            }
        });
        return saveTO.getId();
    }

    public Integer cancelImportTaskConfig(String ids) {
        String sql = "delete from import_task_config  WHERE id in (" + ids + ")";
        try {
            mysqlTemplate.execute(sql);
            return 1;
        } catch (Exception e) {
            log.error("删除出错" + ids);
            e.printStackTrace();
        }
        return 0;
    }

    public Integer updateImportTaskLastTime(final ImportTaskConfig saveTO) {

        String sql = "UPDATE import_task_config SET LASTTIME = ? WHERE DATASOURCE = ?";
        mysqlTemplate.update(sql, new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement arg0) throws SQLException {
                arg0.setObject(1, saveTO.getLasttime());
                arg0.setObject(2, saveTO.getDatasource());
            }
        });
        return saveTO.getId();
    }

    public Integer insertImportTask(ImportConfigRequest saveTO) {
        String sql = "insert into import_task_config(sqlstr,frequency,tableName,url,username,pwd,schema_,datasource,lasttime,lastUpdater,label)values(";
        sql += "'" + saveTO.getSqlstr() + "','" + saveTO.getFrequency() + "','" + saveTO.getTableName() + "','" + saveTO.getUrl() + "','" + saveTO.getUsername() + "','" + saveTO.getPwd() + "','" + saveTO.getSchema() + "','" + saveTO.getDatasource() + "','" + saveTO.getLasttime() + "','" + saveTO.getLastUpdater() + "','" + saveTO.getLabel() + "')";
        return mysqlTemplate.update(sql);
    }

    public Integer updateImportTask(ImportConfigRequest saveTO) {
        String sql = "update import_task_config set sqlstr = ?,frequency = ?,lasttime = ?,url = ?,username = ?,pwd = ?,schema_ =  ? ,tableName = ?,datasource = ?,lastUpdater = ?,label = ? where id = ?";
        return mysqlTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement arg0) throws SQLException {
                arg0.setObject(1, saveTO.getSqlstr());
                arg0.setObject(2, saveTO.getFrequency());
                arg0.setObject(3, saveTO.getLasttime());
                arg0.setObject(4, saveTO.getUrl());
                arg0.setObject(5, saveTO.getUsername());
                arg0.setObject(6, saveTO.getPwd());
                arg0.setObject(7, saveTO.getSchema());
                arg0.setObject(8, saveTO.getTableName());
                arg0.setObject(9, saveTO.getDatasource());
                arg0.setObject(10, saveTO.getLastUpdater());
                arg0.setObject(11, saveTO.getLabel());
                arg0.setObject(12, saveTO.getId());
            }
        });
    }

    /**
     * 执行一条标准sql
     *
     * @param sql
     */
    public void excute(String sql) {
        mysqlTemplate.execute(sql);
    }

    public void insertObject(String tableName, Object object) throws Exception {
        if (tableIsExist(tableName)) {
            log.info(tableName + "表存在。");
            saveObj(mysqlTemplate, tableName, object);
        } else {
            log.info(tableName + "表不存在！开始创建。。。");
            createTableByObject(tableName, object);
            saveObj(mysqlTemplate, tableName, object);

        }
    }

    /**
     * 查询数据库是否有某表
     *
     * @param cnn
     * @param tableName
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public boolean tableIsExist(String tableName) throws Exception {
        Connection conn = mysqlTemplate.getDataSource().getConnection();
        ResultSet tabs = null;
        try {
            DatabaseMetaData dbMetaData = conn.getMetaData();
            String[] types = {"TABLE"};
            tabs = dbMetaData.getTables(null, null, tableName, types);
            if (tabs.next()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tabs.close();
            conn.close();
        }
        return false;
    }

    /**
     * 根据表名称创建一张表,对象属性
     *
     * @param tableName
     */
    public int createTableByObject(String tableName, Object obj) {
        StringBuffer sb = new StringBuffer("");
        sb.append("CREATE TABLE `" + tableName + "` (");
        sb.append(" `id` int(11) NOT NULL AUTO_INCREMENT,");
        Map<String, Object> map = null;
        try {
            map = Utils.getProperty(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set<String> set = map.keySet();
        for (String key : set) {
            sb.append("`" + key + "` varchar(255) DEFAULT '',");
        }
        sb.append(" `tableName` varchar(255) DEFAULT '',");
        sb.append(" PRIMARY KEY (`id`)");
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        try {
            mysqlTemplate.update(sb.toString());
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int alterTable(String tableName, String fieldName) {
        StringBuffer sb = new StringBuffer("");
        sb.append("alter table ");
        sb.append(tableName);
        sb.append(" add ");
        sb.append(fieldName);
        sb.append(" varchar(255)");
        try {
            mysqlTemplate.update(sb.toString());
            return 1;
        } catch (Exception e) {
            log.error("该字段已存在");
        }
        return 0;
    }

    /**
     * 根据表名称创建一张表,列名
     *
     * @param tableName
     */
    public int createTableByFieldNames(String tableName, List<String> fieldNameList) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `" + tableName + "` (");
        sb.append(" `id` int(11) NOT NULL AUTO_INCREMENT,");
        for (String fieldName : fieldNameList) {
            sb.append("`" + fieldName + "` varchar(255) DEFAULT '',");
        }
        sb.append(" PRIMARY KEY (`id`)");
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        try {
            log.info("建表语句:" + sb.toString());
            mysqlTemplate.update(sb.toString());
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 保存方法，注意这里传递的是实际的表的名称
     */
    public static int saveObj(JdbcTemplate jdbcTemplate, String tableName, Object obj) {
        int re = 0;
        try {
            String sql = " insert into " + tableName + " (";
            Map<String, Object> map = Utils.getProperty(obj);
            Set<String> set = map.keySet();
            for (String key : set) {
                sql += (key + ",");
            }
            sql += " tableName ) ";
            sql += " values ( ";
            for (String key : set) {
                sql += ("'" + map.get(key) + "',");
            }
            sql += ("'" + tableName + "' ) ");
            re = jdbcTemplate.update(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return re;
    }
}


