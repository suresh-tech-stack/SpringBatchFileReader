package com.conduent.iral.batch.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * In this class Mapping the T_PROCESS_PARAMETERS data into IRALAckFileParams
 * object
 * 
 * @author 52058018
 */
public class IRALAckFileParamsMapper implements RowMapper<IRALAckFileParams> {
	@Override
	public IRALAckFileParams mapRow(ResultSet rs, int rowNum) throws SQLException {
		IRALAckFileParams tProcess = new IRALAckFileParams();
		tProcess.setFilePath(rs.getString("PARAM_VALUE"));
		tProcess.setFileXferId(rs.getString("PROCESS_PARAMETER_ID"));
		tProcess.setFileStatus(rs.getString("PARAM_CODE"));
		return tProcess;
	}
}
