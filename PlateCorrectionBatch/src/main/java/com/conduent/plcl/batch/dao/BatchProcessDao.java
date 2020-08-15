package com.conduent.plcl.batch.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.conduent.acknowledgment.AcknowledgmentFileGenerator;
import com.conduent.plcl.batch.common.PLCLBatchException;
import com.conduent.plcl.batch.model.MomsData;
import com.conduent.plcl.batch.model.MomsDataMapper;
import com.conduent.plcl.batch.model.PLCLParams;
import com.conduent.plcl.batch.model.PLCLParamsMapper;
import com.conduent.plcl.batch.model.XFERFileControlMapper;
import com.conduent.plcl.batch.util.BatchUtil;
import com.conduent.vector.pmms.genericservice.AlertMessagePersister;
import com.conduent.vector.pmms.genericservice.AlertMessagePersisterImpl;
import com.conduent.vector.pmms.genericservice.vo.AlertRequest;

/**
 * This Class have all the DataBase related operations.
 * 
 * @author 52058018
 */
public class BatchProcessDao {

	private final static Logger LOGGER = Logger.getLogger(BatchProcessDao.class);
	@Autowired
	JdbcTemplate jdbcTemplate;

	private static List<PLCLParams> batchXferFileInfo;

	private AlertMessagePersister pmmsAlert;

	private Resource[] resource;

	@Autowired
	AcknowledgmentFileGenerator ackFileGenerator;

	/**
	 * Fetching the Ready to process files from DB
	 * 
	 * @return
	 */
	public List<PLCLParams> getUnprocessedFileNames() {
		return jdbcTemplate.query(BatchUtil.getXFERFileControlSelectQuery(), new Object[] { BatchUtil.EXECUTE_CONSTANT,
				BatchUtil.XFERFILE_PROCESSED_CONSTANT, BatchUtil.PARAM_GROUP }, new XFERFileControlMapper());

	}

	/**
	 * Fetching all the ACkFile path form DB at the starting of application.
	 * 
	 * @return
	 */
	@Autowired
	public List<PLCLParams> setXFERFileByStatusAndExtension() {
		if (batchXferFileInfo == null) {
			batchXferFileInfo = jdbcTemplate.query(BatchUtil.getXFERFileSelectQuery(),
					new Object[] { BatchUtil.PARAM_GROUP }, new PLCLParamsMapper());
		}
		return batchXferFileInfo;
	}

	public Resource[] getMultiResources() {
		return this.resource;
	}

	/**
	 * Setting all the files into resource
	 * 
	 * @param inputResource
	 * @throws PLCLBatchException
	 */
	public void setResources(String inputResource) throws PLCLBatchException {
		ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
		try {
			resource = patternResolver.getResources("file:" + inputResource);
		} catch (Exception e) {
			LOGGER.error("Unable to get the files from directory:  " + e.getMessage());
			throw new PLCLBatchException("Error while fetching the multiple resources " + e.getMessage());

		}
	}

	/**
	 * Fetching the File Related information from batchXferFileInfo Object
	 * 
	 * @param fileStatus
	 * @return
	 */
	public PLCLParams getXFERFileParamsByfileStatus(String fileStatus) {
		PLCLParams xferParams = new PLCLParams();
		for (PLCLParams xferFile : batchXferFileInfo) {
			xferParams.setFileId(xferFile.getFileId());
			xferParams.setFilePath(xferFile.getFilePath());
			xferParams.setFileXferId(xferFile.getFileXferId());
			if ((BatchUtil.PICKEDUP_CONSTANT.equals(fileStatus) || BatchUtil.EXECUTE_CONSTANT.equals(fileStatus))
					&& BatchUtil.XFERFILE_PROCESSED_CONSTANT.equals(xferFile.getFileStatus())) {
				break;
			} else if (BatchUtil.EXCEPTION_CONSTANT.equals(fileStatus)
					&& BatchUtil.XFERFILE_EXCEPTION_CONSTANT.equals(xferFile.getFileStatus())) {
				break;
			} else if (BatchUtil.ARCHIVE_CONSTANT.equals(fileStatus)
					&& BatchUtil.XFERFILE_ARCHIVE_CONSTANT.equals(xferFile.getFileStatus())) {
				break;
			} else {

			}
		}

		return xferParams;
	}

	/**
	 * Updating the file status in DB based on fileId
	 * 
	 * @param xFERFiles
	 * @param fileStatus
	 */
	public void updateXFERFileByStatus(List<PLCLParams> xFERFiles, String fileStatus) {

		PLCLParams xferFileId = getXFERFileParamsByfileStatus(fileStatus);
		LOGGER.debug("The File Id for XFERFile is:  " + xferFileId.getFileXferId());

		jdbcTemplate.batchUpdate(BatchUtil.getXFERUpdateQuery(), new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				PLCLParams xFERFile = xFERFiles.get(i);
				ps.setString(1, fileStatus);
				ps.setLong(2, getXferFileId());
				ps.setString(3, xFERFile.getFileId());
			}

			@Override
			public int getBatchSize() {
				return xFERFiles.size();
			}
		});
	}

	/**
	 * Updating the file status in DB based on fileId
	 * 
	 * @param xFERFiles
	 * @param fileStatus
	 */
	public void updateXFERFILECNTLBYFileId(PLCLParams xFERFiles, String fileStatus) {

		PLCLParams xferFileId = getXFERFileParamsByfileStatus(fileStatus);
		LOGGER.debug("The file Id for XFERFILECNTL is : " + xferFileId.getFileXferId());

		jdbcTemplate.update(BatchUtil.getXFERCNTLUpdateQuery(), new PreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, fileStatus);
				ps.setLong(2, getXferFileId());
				ps.setString(3, xFERFiles.getFileStatus());
				ps.setString(4, xFERFiles.getFileId());

			}

		});
	}

	public long getXferFileId() {
		return jdbcTemplate.queryForObject(BatchUtil.XFER_FILE_ID, Long.class);
	}

	public long getProcessParamId() {
		return jdbcTemplate.queryForObject(BatchUtil.PROCESS_PARAMETER_ID, Long.class);
	}

	/**
	 * Inserting the File information into T_XFER_CONTROL_BATCH Table
	 * 
	 * @param xferFileName
	 * @param recordCount
	 * @param processParamId
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int insertXferControl(String xferFileName, int recordCount, long processParamId) {
		return jdbcTemplate.update(BatchUtil.INSERT_QUERY,
				new Object[] { xferFileName, recordCount, setPresentTime(), processParamId });
	}

	/**
	 * Insert Data for Unprocessed files into T_XFER_CONTROL_BATCH Table
	 * 
	 * @param xferControlId
	 * @param xferFileName
	 * @param recordCount
	 * @param xferId
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int insertUnProcessedFileInXferControl(String xferFileName, int recordCount, long xferFileId) {
		return jdbcTemplate.update(BatchUtil.INSERT_UNPROCESSED_QUERY,
				new Object[] { xferFileName, recordCount, setPresentTime(), xferFileId });
	}

	/**
	 * Update ACK_DATE_TIME in T_XFER_CONTROL_BATCH Table .
	 * 
	 * @param fileId
	 * @return
	 */
	public int updateAckDateTime(String fileId) {

		return jdbcTemplate.update(BatchUtil.ACK_DATE_UPDATE, new Object[] { fileId });
	}

	/**
	 * Fetching the Original file path from T_XFER_FILE Table
	 * 
	 * @return
	 */
	public String getFilePath() {
		return jdbcTemplate.queryForObject(BatchUtil.GET_FILE_PATH, String.class);
	}

	/**
	 * Fetching the inbound file path from t_process_parameters Table
	 */
	public String getInboundFilePath() {
		return jdbcTemplate.queryForObject(BatchUtil.GET_INBOUND_FILE_PATH, String.class);
	}

	/**
	 * Fetching the Processed file path from t_process_parameters Table
	 * 
	 * @return
	 */
	public String getProcessedFilePath() {
		return jdbcTemplate.queryForObject(BatchUtil.GET_PROCESSED_FILE_PATH, String.class);
	}

	/**
	 * Fetching the UnProcessed file path from t_process_parameters Table
	 * 
	 * @return
	 */
	public String getUnProcessedFilePath() {
		return jdbcTemplate.queryForObject(BatchUtil.GET_UNPROCESSED_FILE_PATH, String.class);
	}

	/**
	 * Fetching the ack file path from t_process_parameters Table
	 * 
	 * @return
	 */
	public String getAckFilePath() {
		return jdbcTemplate.queryForObject(BatchUtil.GET_ACK_FILE_PATH, String.class);
	}

	/**
	 * Fetching the Zip file path from t_process_parameters Table
	 * 
	 * @return
	 */
	public String getZipFilePath() {
		return jdbcTemplate.queryForObject(BatchUtil.GET_PLCL_ZIP_PATH, String.class);
	}

	/**
	 * Method is for get the MomAlert data from the T_MOMS_MESSAGE_PRIORITY Table
	 * 
	 * @return
	 */
	public MomsData getMomsData() {
		return jdbcTemplate.queryForObject(BatchUtil.MOMS_ALERT_DATA, new MomsDataMapper());
	}

	/**
	 * Creating the MomAlerts for given returnCode
	 * 
	 * @param returnCode
	 * @param data
	 */
	public MomsData createMomWorkOrder(String returnCode) {
		return jdbcTemplate.queryForObject(BatchUtil.MOMS_DATA + "'%" + returnCode + "%'", new MomsDataMapper());
	}

	/**
	 * Fetching last processed file from Database
	 * 
	 * @return
	 */
	public String getProcessedFileName() {
		return jdbcTemplate.queryForObject(BatchUtil.GET_PROCESSED_FILE_NAME, String.class);
	}

	/**
	 * Creating the MomAlerts for given returnCode
	 * 
	 * @param returnCode
	 * @param data
	 */
	public void createMomWorkOrder(String returnCode, String data) {
		MomsData momdata = createMomWorkOrder(returnCode);
		getPmmsAlert().createMOMS(momdata.getDescription(), data, momdata.getEquipmentId(), momdata.getRepairNumber());
	}

	/**
	 * Send Data for Creating the ACK file for processing IRAL File
	 * 
	 * @param fileName
	 * @param returnCode
	 * @param ackMessage
	 */
	public void sendDataToAckFile(String fileName, String returnCode, String ackMsg) {
		String ackPath = getAckFilePath();
		ackFileGenerator.createNewAckFile("RTC_", fileName, ackPath, "PLCL", returnCode, ackMsg);
	}

	/**
	 * Updating the NUM_RECS and UPDATE_TS in T_XFER_CONTROL Table
	 * 
	 * @param numRecords
	 * @param fileName
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateXferControlByFileName(int numRecords, String fileName) {
		jdbcTemplate.update(BatchUtil.UPDATE_T_XFER_CONTROL_DATA, numRecords, fileName);
	}

	/**
	 * Checking the Given FileName is available in T_XFER_CONTROL Table or not .
	 * 
	 * @param fileName
	 * @return
	 */
	public int checkFileNameInXferControl(String fileName) {

		int returnVlaue = jdbcTemplate.update(BatchUtil.SELECT_FILE_IN_T_XFER_CONTROL, new PreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, fileName);
			}
		});
		return returnVlaue;

	}

	/**
	 * Fetching the pmmsAlert Object from PMMSGenericService
	 * 
	 * @return
	 */
	public AlertMessagePersister getPmmsAlert() {
		if (pmmsAlert == null) {
			pmmsAlert = new AlertMessagePersisterImpl(jdbcTemplate.getDataSource());
		}
		return pmmsAlert;
	}

	public void setPmmsAlert(AlertMessagePersister pmmsAlert) {
		this.pmmsAlert = pmmsAlert;
	}

	/**
	 * Method is for creating the AlertMessage Object for inserting the data into
	 * T_BATCH_EXCEPTION Table
	 * 
	 * @param alertType
	 * @param message
	 * @param attributeValue
	 * @return
	 */
	public AlertRequest populateAlertMessage(String alertType, String message, String attributeValue) {

		AlertRequest alertRequest = new AlertRequest();
		alertRequest.setXferFileId(getXferFileId());
		alertRequest.setProcessName(alertType);
		alertRequest.setExcepMessage(message);
		alertRequest.setAttributeType(BatchUtil.ALERT_TYPE);
		alertRequest.setComponentName(BatchUtil.COMPONENT_NAME);
		alertRequest.setUserId(BatchUtil.REPORT_USER);
		alertRequest.setAttributeValue(attributeValue);

		return alertRequest;
	}

	/**
	 * Get the presentTime from Data
	 * 
	 * @return
	 */
	public static String setPresentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
		try {
			Date presentdate = new Date();
			String time = sdf.format(presentdate);
			return time;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
