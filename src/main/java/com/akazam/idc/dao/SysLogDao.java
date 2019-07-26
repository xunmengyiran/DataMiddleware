package com.akazam.idc.dao;

import com.akazam.idc.model.User;
import com.akazam.idc.vo.SysLogVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SysLogDao {

    private Logger log = LoggerFactory.getLogger(SysLogDao.class);

    @Autowired
    private JdbcTemplate mysqlTemplate;

    public void saveSysLog(SysLogVO sysLogVO) throws Exception{
        String sql = "insert into tb_sys_log(operationUserName,operationName,operationMethod,operationParams,operationDate) values (?,?,?,?,?)";
        mysqlTemplate.update(sql, new Object[]{sysLogVO.getOperationUserName(), sysLogVO.getOperationName(), sysLogVO.getOperationMethod(), sysLogVO.getOperationParams(), sysLogVO.getOperationDate()});
    }

    public int getSysLogCount(String startDate,String endDate,String username) throws Exception{
        /*String sql = "SELECT count(0) FROM tb_sys_log where operationUserName = ?";
        int count = mysqlTemplate.queryForObject(sql, Integer.class, username);*/
//        String sql = "SELECT COUNT(0) FROM tb_sys_log";
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(0) FROM tb_sys_log WHERE 1=1 ");
        if(startDate != null && !"".equals(startDate)){
            sb.append(" AND operationDate>='"+startDate+" 00:00:00'");
        }
        if(endDate != null && !"".equals(endDate)){
            sb.append(" AND operationDate<='"+endDate+" 23:59:00'");
        }
        int count = mysqlTemplate.queryForObject(sb.toString(), Integer.class);
        log.info("==count==" + count);
        return count;
    }

    public List<SysLogVO> querySysLog(String username, int page, int rows,String startDate, String endDate) throws Exception{
        /*String sql = "SELECT* FROM tb_sys_log where operationUserName = ? ORDER BY id desc limit " + String.valueOf(((page - 1) * rows)) + "," + String.valueOf(rows);
        RowMapper<SysLogVO> rowMapper = new BeanPropertyRowMapper<SysLogVO>(SysLogVO.class);
        List<SysLogVO> result = (List<SysLogVO>) mysqlTemplate.query(sql, rowMapper, username);*/
//        String sql = "SELECT* FROM tb_sys_log ORDER BY id desc limit " + String.valueOf(((page - 1) * rows)) + "," + String.valueOf(rows);
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT* FROM tb_sys_log WHERE 1=1 ");
        if(startDate != null && !"".equals(startDate)){
            sb.append(" AND operationDate>='"+startDate+" 00:00:00'");
        }
        if(endDate != null && !"".equals(endDate)){
            sb.append(" AND operationDate<='"+endDate+" 23:59:00'");
        }
        sb.append(" ORDER BY id DESC LIMIT " + String.valueOf(((page - 1) * rows)) + "," + String.valueOf(rows));
        RowMapper<SysLogVO> rowMapper = new BeanPropertyRowMapper<SysLogVO>(SysLogVO.class);
        log.info("==sql=="+sb.toString());
        List<SysLogVO> result = (List<SysLogVO>) mysqlTemplate.query(sb.toString(), rowMapper);
        return result;
    }
}
