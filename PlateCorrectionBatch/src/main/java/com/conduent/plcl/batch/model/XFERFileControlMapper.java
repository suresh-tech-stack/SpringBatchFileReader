package com.conduent.plcl.batch.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * In this class Mapping the T_PROCESS_PARAMETERS and T_XFER_CONTROL_BATCH data
 * into PLCLParams Object .
 * 
 * @author 52058018
 */
public final class XFERFileControlMapper implements RowMapper<PLCLParams> {

	@Override
	public PLCLParams mapRow(ResultSet rs, int rowNum) throws SQLException {
		PLCLParams tProcessParam = new PLCLParams();
		tProcessParam.setFilePath(rs.getString("PARAM_VALUE"));
		tProcessParam.setFileXferId(rs.getString("PROCESS_PARAMETER_ID"));
		tProcessParam.setFileId(rs.getString("XFER_CONTROL_ID"));
		tProcessParam.setFileName(rs.getString("XFER_FILE_NAME"));
		tProcessParam.setFileStatus(rs.getString("XFER_XMIT_STATUS"));
		return tProcessParam;
	}
}