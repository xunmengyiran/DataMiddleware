package com.akazam.idc.dao;

import com.akazam.idc.model.ExportTaskConfig;
import com.akazam.idc.vo.ExportConfigRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ExportTaskConfigDao {

    private Logger log = LoggerFactory.getLogger(ExportTaskConfigDao.class);

    @Autowired
    private JdbcTemplate mysqlTemplate;

    public ExportTaskConfig loadExportTaskConfigByID(String id) {
        String sql = "SELECT id,sqlstr,frequency,url,username,pwd,schema_ ,lasttime,datasource,outputPath,label FROM export_task_config where id = ?";
        RowMapper<ExportTaskConfig> rowMapper = new BeanPropertyRowMapper<ExportTaskConfig>(ExportTaskConfig.class);
        return mysqlTemplate.queryForObject(sql, rowMapper, id);
    }

    public int getExportTaskConfigCount(String username) {
        String sql = "SELECT count(0) FROM export_task_config where lastUpdater = ?";
        int count = mysqlTemplate.queryForObject(sql, Integer.class, username);
        log.info("==count==" + count);
        return count;
    }

    public List<ExportTaskConfig> loadExportTaskConfig(int datasource, String username, int page, int rows) {
        String sql = "SELECT label,id,sqlstr,frequency,url,username,pwd,schema_ ,outputPath ,datasource FROM export_task_config where lastUpdater = ? ORDER BY id DESC limit " + String.valueOf(((page - 1) * rows)) + "," + String.valueOf(rows);
        RowMapper<ExportTaskConfig> rowMapper = new BeanPropertyRowMapper<ExportTaskConfig>(ExportTaskConfig.class);
        List<ExportTaskConfig> result = (List<ExportTaskConfig>) mysqlTemplate.query(sql, rowMapper, username);
        return result;
    }

    public List<ExportConfigRequest> loadAllExportTaskConfig() {
        String sql = "SELECT id,sqlstr,frequency,url,username,pwd,schema_ ,outputPath ,datasource FROM export_task_config";
        RowMapper<ExportConfigRequest> rowMapper = new BeanPropertyRowMapper<ExportConfigRequest>(ExportConfigRequest.class);
        List<ExportConfigRequest> result = (List<ExportConfigRequest>) mysqlTemplate.query(sql, rowMapper);
        return result;
    }

    public Integer updateExportTaskConfig(final ExportTaskConfig saveTO) {

        String sql = "UPDATE export_task_config SET SQLSTR = ?, FREQUENCY = ?, url = ?, username = ?,  pwd = ?, schema_ =  ? WHERE DATASOURCE = ?";
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

    public Integer cancelExportTaskConfig(final String ids) {
        String sql = "delete from export_task_config  WHERE id in (" + ids + ")";
        try {
            mysqlTemplate.execute(sql);
            return 1;
        } catch (Exception e) {
            log.error("删除出错" + ids);
            e.printStackTrace();
        }
        return 0;
    }

    public Integer updateExportTaskLastTime(final ExportTaskConfig saveTO) {
        String sql = "UPDATE export_task_config SET LASTTIME = ? WHERE DATASOURCE = ?";
        mysqlTemplate.update(sql, new PreparedStatementSetter() {
            public void setValues(PreparedStatement arg0) throws SQLException {
                arg0.setObject(1, saveTO.getLasttime());
                arg0.setObject(2, saveTO.getDatasource());
            }
        });
        return saveTO.getId();
    }

    public Integer insertExportTask(ExportConfigRequest saveTO) {
        String sql = "insert into export_task_config(sqlstr,frequency,outputPath,url,username,pwd,schema_,datasource,lasttime,lastUpdater,label)values(";
        sql += "'" + saveTO.getSqlstr() + "','" + saveTO.getFrequency() + "','" + saveTO.getOutputPath() + "','" + saveTO.getUrl() + "','" + saveTO.getUsername() + "','" + saveTO.getPwd() + "','" + saveTO.getSchema() + "','" + saveTO.getDatasource() + "','" + saveTO.getLasttime() + "','" + saveTO.getLastUpdater() + "','" + saveTO.getLabel() + "')";
        return mysqlTemplate.update(sql);
    }

    public Integer updateExportTask(ExportConfigRequest saveTO) {
        String sql = "update export_task_config set sqlstr = ?,frequency = ?,lasttime = ?,url = ?,username = ?,pwd = ?,schema_ =  ? ,outputPath = ?,datasource = ?,lastUpdater = ?,label = ? where id = ?";
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
                arg0.setObject(8, saveTO.getOutputPath());
                arg0.setObject(9, saveTO.getDatasource());
                arg0.setObject(10, saveTO.getLastUpdater());
                arg0.setObject(11, saveTO.getLabel());
                arg0.setObject(12, saveTO.getId());
            }
        });
    }
}
