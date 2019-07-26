package com.akazam.idc.model;

import lombok.Data;

@Data
public class ExportTaskConfig {

	    private int id;
	    
	    private String sqlstr;
	   
	    private String frequency;
	   
	    private Long lasttime;

	    private String datasource;
	    
	    private String url;
	    
	    private String  username;
	    
	    private String  pwd;
	    
	    private String  schema_;
	    
	    private String outputPath;
	    
	    private String label;//标签
	    

  
}
