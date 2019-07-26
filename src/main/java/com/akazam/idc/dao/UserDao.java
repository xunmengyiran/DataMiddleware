package com.akazam.idc.dao;

import com.akazam.idc.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {

    @Autowired
    private JdbcTemplate mysqlTemplate;

    public User loadUserByUserName(String username) {
    	

        String sql = "SELECT * FROM user c WHERE c.username = ?";
        List<User> userList = mysqlTemplate.query(sql, new BeanPropertyRowMapper<>(User.class),username);
        return userList.isEmpty() ? null : userList.get(0);
    }
}
