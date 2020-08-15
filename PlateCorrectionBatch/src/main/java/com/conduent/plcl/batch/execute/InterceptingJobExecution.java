package com.conduent.plcl.batch.execute;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.conduent.plcl.batch.common.PLCLBatchException;
import com.conduent.plcl.batch.dao.BatchProcessDao;
import com.conduent.plcl.batch.model.CustomJobData;
import com.conduent.plcl.batch.model.MomsData;
import com.conduent.plcl.batch.model.PLCLParams;
import com.conduent.plcl.batch.util.BatchUtil;
import com.conduent.vector.pmms.genericservice.vo.AlertRequest;

/**
 * During the course of the execution of a Job, it may be useful to be notified
 * of various Exceptions Handled in this class.
 * 
 * @author 52058018
 */
@Component
public class InterceptingJobExecution implements JobExecutionListener {

	private final static Logger LOGGER = Logger.getLogger(InterceptingJobExecution.class);
	@Autowired
	BatchProcessDao batchProcessDao;

	@Autowired
	CustomJobData jobData;

	/**
	 * While executing the job if it's throws any exception it will come to this
	 * method.
	 */
	@Override
	public void afterJob(JobExecution jobExecution) {
		String inputFilePath = jobData.getInputFilePath();
		String processedFilePath = jobData.getProcessedFilePath();
		LOGGER.info("In InterceptingJobExecution :: Enter into afterJob method {} ");
		StringBuilder errorMessage = new StringBuilder();
		try {
			for (Throwable t : jobExecution.getAllFailureExceptions()) {
				errorMessage.append(t.getMessage() + "\n");
				errorMessage.append(t.getCause() + "\n");
			}
			if (!StringUtils.isEmpty(errorMessage.toString())) {
				String errorMsg = errorMessage.toString();
				if (errorMsg.contains("Data Validation Exception")) {
					batchProcessDao.updateXferControlByFileName(0, jobData.getFileName());
					// Creating the ACK for Invalid Detail records
					batchProcessDao.sendDataToAckFile(jobData.getFileName(), "02", "Invalid Detail records");
					batchProcessDao.createMomWorkOrder("PLCLBATCH-02",
							"Invalid Detail records for the File : " + jobData.getFileName());
					// Moving the File from inbound folder to processed folder.
					Files.move(Paths.get(inputFilePath + jobData.getFileName()),
							Paths.get(processedFilePath + jobData.getFileName()), StandardCopyOption.REPLACE_EXISTING);
				} else {
					List<PLCLParams> batchXferFile = new ArrayList<>();
					PLCLParams xferFile = new PLCLParams();
					xferFile.setFileName(jobExecution.getJobParameters().getString("fileName"));
					xferFile.setFileId(jobData.getFileIdVlaue());
					xferFile.setFileStatus(BatchUtil.EXCEPTION_CONSTANT);
					batchXferFile.add(xferFile);
					// If We get any exception Updating the XFER_XMIT_STATUS = 'X' in
					// T_XFER_CONTROL_BATCH Table .
					batchProcessDao.updateXFERFileByStatus(batchXferFile, BatchUtil.EXCEPTION_CONSTANT);
					PLCLParams files = batchProcessDao.getXFERFileParamsByfileStatus(BatchUtil.EXCEPTION_CONSTANT);

					for (Resource r : batchProcessDao.getMultiResources()) {

						File file = r.getFile();
						batchProcessDao.updateXferControlByFileName(0, file.getName());
						batchProcessDao.sendDataToAckFile(file.getName(), "99", "Other error encountered");
						batchProcessDao.updateAckDateTime(jobData.getFileIdVlaue());
						if (file.exists()) {
							// Moving the File from inbound folder to exception folder.
							Files.move(Paths.get(file.getPath()),
									Paths.get(files.getFilePath() + "" + StringUtils.getFilename(file.getName())),
									StandardCopyOption.REPLACE_EXISTING);
						}
					}
					throw new PLCLBatchException(errorMessage.toString());
				}
			}
		} catch (Exception e) {
			MomsData momsData = batchProcessDao.getMomsData();
			batchProcessDao.getPmmsAlert().createMOMS(momsData.getDescription(), e.getMessage(),
					momsData.getEquipmentId(), momsData.getRepairNumber());
			AlertRequest alertRequest = batchProcessDao.populateAlertMessage(BatchUtil.BATCH_PROCESS_NAME,
					"InterceptingJobExecution Error Message : " + e.getMessage(),
					jobExecution.getJobParameters().getString("fileName"));
			batchProcessDao.getPmmsAlert().persistAlerts(alertRequest);
			LOGGER.error("InterceptingJobExecution Error Message : " + e.getMessage());
		}
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
	}

}