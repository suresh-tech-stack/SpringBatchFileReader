package com.conduent.plcl.batch.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * In this class Mapping the T_PROCESS_PARAMETERS data into PLCLParams object
 * 
 * @author 52058018
 */
public class PLCLParamsMapper implements RowMapper<PLCLParams> {

	@Override
	public PLCLParams mapRow(ResultSet rs, int rowNum) throws SQLException {
		PLCLParams tProcess = new PLCLParams();
		tProcess.setFilePath(rs.getString("PARAM_VALUE"));
		tProcess.setFileXferId(rs.getString("PROCESS_PARAMETER_ID"));
		tProcess.setFileStatus(rs.getString("PARAM_CODE"));
		return tProcess;
	}
}
