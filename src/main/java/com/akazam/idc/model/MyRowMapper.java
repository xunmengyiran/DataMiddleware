package com.akazam.idc.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class MyRowMapper implements RowMapper<ImportTaskConfig>{

	@Override
	public ImportTaskConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		return null;
	}

}
