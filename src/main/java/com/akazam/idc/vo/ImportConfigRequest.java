package com.akazam.idc.vo;

import lombok.Data;

@Data
public class ImportConfigRequest {

	private int id;
    private String  frequency;
    private String  url;
    private String  username;
    private String pwd;
    private String  schema;
    private String  sqlstr;
    private String tableName;  
    private String lasttime;
    private String datasource;
    private String lastUpdater;//最后更新人
    
    private String label;
    
}
