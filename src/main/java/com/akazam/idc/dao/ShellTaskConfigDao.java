package com.akazam.idc.dao;

import com.akazam.idc.model.ShellTaskConfig;
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
public class ShellTaskConfigDao {

    private Logger log = LoggerFactory.getLogger(ShellTaskConfigDao.class);

    @Autowired
    private JdbcTemplate mysqlTemplate;

    public ShellTaskConfig loadShellTaskConfigByID(String id) {
        String sql = "SELECT id,frequency,lasttime,shell_text as shellText,lastUpdater,label FROM shell_task_config where id = ?";
        RowMapper<ShellTaskConfig> rowMapper = new BeanPropertyRowMapper<ShellTaskConfig>(ShellTaskConfig.class);
        return mysqlTemplate.queryForObject(sql, rowMapper, id);
    }

    public int getShellTaskConfigCount(String username) {
        String sql = "SELECT count(0) FROM shell_task_config where lastUpdater = ?";
        int count = mysqlTemplate.queryForObject(sql, Integer.class, username);
        log.info("==count==" + count);
        return count;
    }

    public List<ShellTaskConfig> loadShellTaskConfig(int datasource, String username, int page, int rows) {
        String sql = "SELECT id,frequency,lasttime,shell_text as shellText,lastUpdater,label FROM shell_task_config where lastUpdater = ? ORDER BY id DESC limit " + String.valueOf(((page - 1) * rows)) + "," + String.valueOf(rows);
        RowMapper<ShellTaskConfig> rowMapper = new BeanPropertyRowMapper<ShellTaskConfig>(ShellTaskConfig.class);
        List<ShellTaskConfig> result = (List<ShellTaskConfig>) mysqlTemplate.query(sql, rowMapper, username);
        return result;
    }

    public List<ShellTaskConfig> loadAllShellTaskConfig() {
        String sql = "SELECT id,frequency,lasttime,shell_text as shellText,lastUpdater,label FROM shell_task_config";
        RowMapper<ShellTaskConfig> rowMapper = new BeanPropertyRowMapper<ShellTaskConfig>(ShellTaskConfig.class);
        List<ShellTaskConfig> result = (List<ShellTaskConfig>) mysqlTemplate.query(sql, rowMapper);
        return result;
    }

    public Integer cancelShellTaskConfig(final String ids) {
        String sql = "delete from shell_task_config  WHERE id in (" + ids + ")";
        try {
            mysqlTemplate.execute(sql);
            return 1;
        } catch (Exception e) {
            log.error("删除出错" + ids);
            e.printStackTrace();
        }
        return 0;
    }


    public Integer insertShellTask(ShellTaskConfig saveTO) {
        String sql = "insert into shell_task_config(frequency,shell_text,lasttime,lastUpdater,label)values(";
        sql += "'" + saveTO.getFrequency() + "','" + saveTO.getShellText() + "','" + saveTO.getLasttime() + "','" + saveTO.getLastUpdater() + "','" + saveTO.getLabel() + "')";
        return mysqlTemplate.update(sql);
    }

    public Integer updateShellTask(ShellTaskConfig saveTO) {
        String sql = "update shell_task_config set frequency = ?,lasttime = ?,shell_text=?,lastUpdater = ?,label = ? where id = ?";
        return mysqlTemplate.update(sql, new PreparedStatementSetter() {
            public void setValues(PreparedStatement arg0) throws SQLException {
                arg0.setObject(1, saveTO.getFrequency());
                arg0.setObject(2, saveTO.getLasttime());
                arg0.setObject(3, saveTO.getShellText());
                arg0.setObject(4, saveTO.getLastUpdater());
                arg0.setObject(5, saveTO.getLabel());
                arg0.setObject(6, saveTO.getId());
            }
        });
    }
}
