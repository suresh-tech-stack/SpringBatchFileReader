package com.conduent.plcl.batch.util;

/**
 * This class have all the static Queries and Util Methods .
 * 
 * @author 52058018
 *
 */
public class BatchUtil {

	public static final String MOMS_ALERT_DATA = "select EQUIPMENT_ID,REPAIR_NUMBER,DESCRIPTION  from T_MOMS_MESSAGE_PRIORITY where DESCRIPTION = 'PLCLBATCH - Batch Job Failed'";

	public static final String MOMS_DATA = "select EQUIPMENT_ID,REPAIR_NUMBER,DESCRIPTION  from T_MOMS_MESSAGE_PRIORITY where DESCRIPTION like ";

	public static final String CAPITAL_ZIP = ".ZIP";

	public static final String SMALL_ZIP = ".zip";

	public static final String INTERNAL_FILE_ENDS_WITH = ".PLCL";

	public static final String XFER_FILE_ID = "select XFER_FILE_ID from T_XFER_FILE where XFER_FILE_DESCRIP ='Plate Correction List (PLCL)'";

	public static final String ACK_DATE_UPDATE = "update T_XFER_CONTROL_BATCH set ACK_DATETIME = sysdate where XFER_CONTROL_ID = ?";

	public static final String PROCESS_PARAMETER_ID = "select PROCESS_PARAMETER_ID from t_process_parameters where PARAM_NAME = 'PLCL_INBOUND'";

	private static final String PLCL_INSERT_QUERY = "INSERT into T_BOS_STG_PLATE_CORRECTION(ORIG_LIC_STATE,ORIG_LIC_NUMBER,ORIG_LIC_TYPE,ADJ_LIC_STATE,ADJ_LIC_NUMBER,ADJ_LIC_TYPE,UPDATED_BY,UPDATED_TS) values(:OrigLicState,:OrigLicNumber,:OrigLicType,:AdjLicState,:AdjLicNumber,:AdjLicType,'100',:updatedTs)";

	public static final String INSERT_QUERY = "insert into T_XFER_CONTROL_BATCH (XFER_CONTROL_ID,XFER_FILE_NAME,NUM_RECS,DATE_CREATED,TIME_CREATED,XFER_FILE_ID,XFER_XMIT_STATUS,FILE_TYPE) values(SEQ_XFER_CONTROL_BATCH.nextval,?,?,sysdate,?,?,'S','PLCL')";

	public static final String INSERT_UNPROCESSED_QUERY = "insert into T_XFER_CONTROL_BATCH (XFER_CONTROL_ID,XFER_FILE_NAME,NUM_RECS,DATE_CREATED,TIME_CREATED,XFER_FILE_ID,XFER_XMIT_STATUS,FILE_TYPE) values(SEQ_XFER_CONTROL_BATCH.nextval,?,?,sysdate,?,?,'U','PLCL')";

	public static final String GET_FILE_PATH = "select XFER_LOCAL_PATH from T_XFER_FILE where  XFER_FILE_DESCRIP ='Plate Correction List (PLCL)'";

	public static final String GET_INBOUND_FILE_PATH = "select PARAM_VALUE from t_process_parameters where PARAM_NAME = 'PLCL_INBOUND'";

	public static final String GET_PROCESSED_FILE_PATH = "select PARAM_VALUE from t_process_parameters where PARAM_NAME = 'PLCL_PROCESSED'";

	public static final String GET_UNPROCESSED_FILE_PATH = "select PARAM_VALUE from t_process_parameters where PARAM_NAME =  'PLCL_UNPROCESSED'";

	public static final String GET_ACK_FILE_PATH = "select PARAM_VALUE from t_process_parameters where PARAM_NAME =  'PLCL_ACK'";

	public static final String GET_PLCL_ZIP_PATH = "select PARAM_VALUE from t_process_parameters where PARAM_NAME =  'PLCL_ZIP'";

	public static final String GET_PROCESSED_FILE_NAME = "select max(XFER_FILE_NAME) from T_XFER_CONTROL_BATCH where file_type='PLCL' and XFER_XMIT_STATUS = 'P' order by date_created desc";

	public static final String EXECUTE_CONSTANT = "S"; // Starting

	public static final String JOB_NAME = "PLCLBatchJob";

	public static final String ARCHIVE_CONSTANT = "P"; // Process

	public static final String PARAM_GROUP = "PLCL_PROCESS";

	public static final String PICKEDUP_CONSTANT = "R"; // R-Running Job insatance

	public static final String DUPLICATE_CONSTANT = "D"; // D-Duplicate

	public static final String EXCEPTION_CONSTANT = "X"; // Exception

	public static final String XFERFILE_PROCESSED_CONSTANT = "S"; // Starting

	public static final String XFERFILE_ARCHIVE_CONSTANT = "P"; // Process

	public static final String XFERFILE_EXCEPTION_CONSTANT = "X"; // Exception

	public static final String MOM_MSG_08 = "PLCLBATCH-08";

	public static final String MOM_MSG_01 = "PLCLBATCH-01";

	public static final String MOM_MSG_14 = "PLCLBATCH-14";

	public static final String MOM_01 = "01";

	public static final String MOM_08 = "08";
	
	public static final String MOM_14 = "14";

	public static final String OUT_OF_DATE_FILE = "File Skipped due to out of date ";

	public static final String INVALID_HEADER = "Invalid Header Record ";

	public static final String INVALID_RECORD = "Header record count does not match the number of detail records found in the file";

	public static final String INVALID_MSG = "Invalid Header Data for the File : ";

	public static final String NO_FILE_IN_TABLE = "Given File is Not Available in T_XFER_CONTROL Table ";

	public static final String SELECT_XFERFILE_QUERY = "SELECT * FROM T_PROCESS_PARAMETERS where PARAM_GROUP=? and  "
			+ "PARAM_CODE in ('" + XFERFILE_PROCESSED_CONSTANT + "','" + XFERFILE_ARCHIVE_CONSTANT + "','"
			+ XFERFILE_EXCEPTION_CONSTANT + "')";

	public static final String UPDATE_XFERFILE_QUERY = "update T_XFER_CONTROL_BATCH set XFER_XMIT_STATUS=?,XFER_FILE_ID=? where XFER_CONTROL_ID =?";

	public static final String SELECT_XFERFILE_CNTL_QUERY = "select tpp.PARAM_VALUE,xfercontrl.XFER_FILE_NAME,xfercontrl.XFER_CONTROL_ID, xfercontrl.XFER_XMIT_STATUS,tpp.process_parameter_id from T_PROCESS_PARAMETERS tpp  , T_XFER_CONTROL_BATCH xfercontrl  where tpp.process_parameter_id = xfercontrl.XFER_FILE_ID   and xfercontrl.XFER_XMIT_STATUS=? and tpp.PARAM_CODE=? and tpp.PARAM_GROUP=?";

	public static final String UPDATE_XFERFILECNTL_QUERY = "update T_XFER_CONTROL_BATCH set XFER_XMIT_STATUS=?,XFER_FILE_ID=? where and XFER_CONTROL_ID !=?";

	public static final String UPDATE_T_XFER_CONTROL_DATA = "  update T_XFER_CONTROL set NUM_RECS=?,UPDATE_TS=sysdate where XFER_FILE_NAME =?";

	public static final String SELECT_FILE_IN_T_XFER_CONTROL = "select XFER_FILE_NAME from T_XFER_CONTROL where XFER_FILE_NAME =?";

	public static String getTransDetailsQuery() {

		return PLCL_INSERT_QUERY;

	}

	public static String getXFERFileSelectQuery() {

		return SELECT_XFERFILE_QUERY;

	}

	public static String getXFERUpdateQuery() {

		return UPDATE_XFERFILE_QUERY;

	}

	public static String getXFERFileControlSelectQuery() {

		return SELECT_XFERFILE_CNTL_QUERY;

	}

	public static String getXFERCNTLUpdateQuery() {
		return UPDATE_XFERFILECNTL_QUERY;
	}

	public static final String BATCH_PROCESS_NAME = "BATCH";
	public static final String ALERT_TYPE = "FILE";
	public static final String COMPONENT_NAME = "Plate Correction List(PLCL)";
	public static final String REPORT_USER = "100";

}
