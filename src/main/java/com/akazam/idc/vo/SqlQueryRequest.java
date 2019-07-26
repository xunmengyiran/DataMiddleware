package com.akazam.idc.vo;

import lombok.Data;

@Data
public class SqlQueryRequest {

    private String option;
    /**
     * 要执行的SQL
     */
    private String sqlStr;
    /**
     * 页码
     */
    private int pageIndex;
    /**
     * 页长
     */
    private int pageSize;
}
