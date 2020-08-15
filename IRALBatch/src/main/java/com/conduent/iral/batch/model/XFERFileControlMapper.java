package com.conduent.iral.batch.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * In this class Mapping the T_PROCESS_PARAMETERS and T_XFER_CONTROL_BATCH data
 * into AckFileParams Object .
 * 
 * @author 52058018
 */
public final class XFERFileControlMapper implements RowMapper<IRALAckFileParams> {

	@Override
	public IRALAckFileParams mapRow(ResultSet rs, int rowNum) throws SQLException {
		IRALAckFileParams fileNameProcessor = new IRALAckFileParams();
		fileNameProcessor.setFilePath(rs.getString("PARAM_VALUE"));
		fileNameProcessor.setFileXferId(rs.getString("PROCESS_PARAMETER_ID"));
		fileNameProcessor.setFileId(rs.getString("XFER_CONTROL_ID"));
		fileNameProcessor.setFileName(rs.getString("XFER_FILE_NAME"));
		fileNameProcessor.setFileStatus(rs.getString("XFER_XMIT_STATUS"));
		return fileNameProcessor;
	}
}