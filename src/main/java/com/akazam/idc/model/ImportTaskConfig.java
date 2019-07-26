package com.akazam.idc.model;

import lombok.Data;

@Data
public class ImportTaskConfig {

    private int id;
    
    private String sqlstr;
   
    private String frequency;
   
    private String time;
   
    private Long lasttime;

    private String datasource;
    
    private String url;
    
    private String  username;
    
    private String  pwd;
    
    private String  schema_;
    
    private String tableName;
    
    private String label;//标签
    

}
