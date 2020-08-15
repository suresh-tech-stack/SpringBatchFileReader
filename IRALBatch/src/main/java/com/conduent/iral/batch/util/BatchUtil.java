package com.conduent.iral.batch.util;

import org.apache.log4j.Logger;

/**
 * This class have all the static Queries and Util Methods .
 * 
 * @author 52058018
 *
 */
public class BatchUtil {
	private final static Logger LOGGER = Logger.getLogger(BatchUtil.class);

	public static final String MOMS_ALERT_DATA = "select EQUIPMENT_ID,REPAIR_NUMBER,DESCRIPTION from T_MOMS_MESSAGE_PRIORITY where DESCRIPTION = 'IRALBATCH - Batch Job Failed'";

	public static final String MOMS_DATA = "select EQUIPMENT_ID,REPAIR_NUMBER,DESCRIPTION  from T_MOMS_MESSAGE_PRIORITY where DESCRIPTION like ";

	private static final String UPDATE_T_TRIP_BOS = "update T_TRIP_SENT_TO_BOS set IMG_RECEIVED_TIMESTAMP = ? , IMG_ACK_RET_CODE = ? where IMG_ZIP_FILE_NAME = ?";

	private static final String UPDATE_T_TRIP_BOS_ODS = "update T_TRIP_SENT_TO_BOS set  ODS_RECEIVED_TIMESTAMP=?, ODS_IMG_ACK_RET_CODE=? where ODS_IMG_FILE_NAME= ? ";

	public static final String XFER_FILE_ID = "select XFER_FILE_ID from T_XFER_FILE where XFER_FILE_DESCRIP ='Image Received Acknowledgment List(IRAL)'";

	public static final String PROCESS_PARAMETER_ID = "select PROCESS_PARAMETER_ID from t_process_parameters where PARAM_NAME = 'IRAL_INBOUND'";

	public static final String INSERT_UNPROCESSED_QUERY = "insert into T_XFER_CONTROL_BATCH (XFER_CONTROL_ID,XFER_FILE_NAME,NUM_RECS,DATE_CREATED,TIME_CREATED,XFER_FILE_ID,XFER_XMIT_STATUS,FILE_TYPE) values(SEQ_XFER_CONTROL_BATCH.nextval,?,?,sysdate,?,?,'U','IRAL')";

	public static final String INSERT_QUERY = "insert into T_XFER_CONTROL_BATCH (XFER_CONTROL_ID,XFER_FILE_NAME,NUM_RECS,DATE_CREATED,TIME_CREATED,XFER_FILE_ID,XFER_XMIT_STATUS,FILE_TYPE) values(SEQ_XFER_CONTROL_BATCH.nextval,?,?,sysdate,?,?,'S','IRAL')";

	public static final String GET_FILE_PATH = "select XFER_LOCAL_PATH from T_XFER_FILE where XFER_FILE_DESCRIP ='Image Received Acknowledgment List(IRAL)'";

	public static final String GET_INBOUND_FILE_PATH = "select PARAM_VALUE from t_process_parameters where PARAM_NAME = 'IRAL_INBOUND'";

	public static final String GET_UNPROCESSED_FILE_PATH = "select PARAM_VALUE from t_process_parameters where PARAM_NAME = 'IRAL_UNPROCESSED'";

	public static final String GET_PROCESSED_FILE_PATH = "select PARAM_VALUE from t_process_parameters where PARAM_NAME = 'IRAL_PROCESSED'";

	public static final String GET_ZIP_FILE_PATH = "select PARAM_VALUE from t_process_parameters where PARAM_NAME = 'IRAL_ZIP'";

	public static final String GET_ACK_FILE_PATH = "select PARAM_VALUE from t_process_parameters where PARAM_NAME =  'IRAL_ACK'";

	public static final String EXECUTE_CONSTANT = "S"; // Starting

	public static final String JOB_NAME = "IRALAckFileJob"; // Job Name

	public static final String ARCHIVE_CONSTANT = "P"; // Processed

	public static final String PARAM_GROUP = "IRAL_PROCESS";

	public static final String PICKEDUP_CONSTANT = "R"; // Running

	public static final String DUPLICATE_CONSTANT = "D"; // D-Duplicate

	public static final String EXCEPTION_CONSTANT = "X"; // Exception

	public static final String XFERFILE_PROCESSED_CONSTANT = "S"; // Starting

	public static final String XFERFILE_ARCHIVE_CONSTANT = "P"; // Processed

	public static final String XFERFILE_EXCEPTION_CONSTANT = "X"; // Exception

	public static final String IRAL_FILE_EXT = ".IRAL";

	public static final String MOM_MSG_08 = "IRALBATCH-08";

	public static final String MOM_MSG_01 = "IRALBATCH-01";

	public static final String MOM_01 = "01";

	public static final String MOM_08 = "08";

	public static final String INVALID_HEADER = "Invalid Header Record ";

	public static final String INVALID_RECORD = "Header record count does not match the number of detail records found in the file";

	public static final String INVALID_MSG = "Invalid Header Data for the File : ";
	
	public static final String NO_FILE_IN_TABLE  = "Given File is Not Available in T_XFER_CONTROL Table ";

	public static final String SELECT_XFERFILE_QUERY = "SELECT * FROM T_PROCESS_PARAMETERS where PARAM_GROUP=? and  "
			+ "PARAM_CODE in ('" + XFERFILE_PROCESSED_CONSTANT + "','" + XFERFILE_ARCHIVE_CONSTANT + "','"
			+ XFERFILE_EXCEPTION_CONSTANT + "')";

	public static final String UPDATE_XFERFILE_QUERY = "update T_XFER_CONTROL_BATCH set XFER_XMIT_STATUS=?,XFER_FILE_ID=? where XFER_CONTROL_ID =?";

	public static final String SELECT_XFERFILE_CNTL_QUERY = "select tpp.PARAM_VALUE,xfercontrl.XFER_FILE_NAME,xfercontrl.XFER_CONTROL_ID, xfercontrl.XFER_XMIT_STATUS,tpp.process_parameter_id from T_PROCESS_PARAMETERS tpp  , T_XFER_CONTROL_BATCH xfercontrl  where tpp.process_parameter_id = xfercontrl.XFER_FILE_ID   and xfercontrl.XFER_XMIT_STATUS=? and tpp.PARAM_CODE=? and tpp.PARAM_GROUP=?";

	public static final String UPDATE_XFERFILECNTL_QUERY = "update T_XFER_CONTROL_BATCH set XFER_XMIT_STATUS=?,XFER_FILE_ID=? where XFER_XMIT_STATUS=? and XFER_CONTROL_ID !=?";

	public static final String UPDATE_T_XFER_CONTROL_DATA = "  update T_XFER_CONTROL set NUM_RECS=?,UPDATE_TS=sysdate where XFER_FILE_NAME =?";

	public static final String SELECT_FILE_IN_T_XFER_CONTROL = "select XFER_FILE_NAME from T_XFER_CONTROL where XFER_FILE_NAME =?";

	public static String getTTripToBOSUpdateQuery() {
		LOGGER.debug("UPDATE_T_TRIP_BOS Query for ETC and Voilation Files Query:" + UPDATE_T_TRIP_BOS);
		return UPDATE_T_TRIP_BOS;
	}

	public static String getTTripToBOSODSUpdateQuery() {
		LOGGER.debug("UPDATE_T_TRIP_BOS Query for ODC Files Query : " + UPDATE_T_TRIP_BOS);
		return UPDATE_T_TRIP_BOS_ODS;
	}

	public static String getXFERFileSelectQuery() {
		LOGGER.debug("Fetching the Files Data from T_PROCESS_PARAMETERS Table Query" + SELECT_XFERFILE_QUERY);
		return SELECT_XFERFILE_QUERY;

	}

	public static String getXFERUpdateQuery() {
		LOGGER.debug("T_XFER_CONTROL Table Update Query  : " + UPDATE_XFERFILE_QUERY);
		return UPDATE_XFERFILE_QUERY;

	}

	public static String getXFERFileControlSelectQuery() {
		LOGGER.debug("Fetching the ready to process files Query : " + SELECT_XFERFILE_CNTL_QUERY);
		return SELECT_XFERFILE_CNTL_QUERY;

	}

	public static String getXFERCNTLUpdateQuery() {
		return UPDATE_XFERFILECNTL_QUERY;
	}

	public static final String BATCH_PROCESS_NAME = "BATCH";
	public static final String ALERT_TYPE = "FILE";
	public static final String COMPONENT_NAME = "Image Received Acknowledgment List(IRAL)";
	public static final String REPORT_USER = "100";

}
