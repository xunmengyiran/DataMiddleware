package com.akazam.idc.model;

import lombok.Data;

@Data
public class User {
    private String username;

    private String password;

    /**
     * 状态
     */
    private Short status;
}
