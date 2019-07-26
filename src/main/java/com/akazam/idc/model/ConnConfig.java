package com.akazam.idc.model;

import lombok.Data;

@Data
public class ConnConfig {

    private int id;

    /**
     * 资源路径、Excel文件存放路径
     */
    private String url;

    private String driverClassName;

    private String username;

    private String password;

    /**
     * 数据源：1、Oracle，2、Excel，3、MySQL
     */
    private int datasource;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getDatasource() {
		return datasource;
	}

	public void setDatasource(int datasource) {
		this.datasource = datasource;
	}
    
    

}
