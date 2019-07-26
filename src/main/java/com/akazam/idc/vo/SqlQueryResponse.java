package com.akazam.idc.vo;

import lombok.Data;

import java.util.List;

@Data
public class SqlQueryResponse extends BaseResponse {
    /**
     * 数据总数
     */
    private int totalCount;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 列名列表
     */
    private List<String> columnNameList;

    /**
     * 数据列表
     */
    private List<List<Object>> dataList;
}
