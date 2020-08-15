package com.conduent.iral.batch.execute;

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

import com.conduent.iral.batch.common.IRALBatchException;
import com.conduent.iral.batch.dao.BatchProcessDao;
import com.conduent.iral.batch.model.IRALAckFile;
import com.conduent.iral.batch.model.MomsData;
import com.conduent.iral.batch.model.IRALAckFileParams;
import com.conduent.iral.batch.util.BatchUtil;
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
	IRALAckFile iralACkFileVo;

	/**
	 * While executing the job if it's throws any exception it will come to this
	 * method.
	 */
	@Override
	public void afterJob(JobExecution jobExecution) {

		LOGGER.info("In InterceptingJobExecution :: Enter into afterJob method {} ");
		StringBuilder errorMessage = new StringBuilder();
		String fileName = null;
		try {
			for (Throwable t : jobExecution.getAllFailureExceptions()) {
				errorMessage.append(t.getMessage() + "\n");
				errorMessage.append(t.getCause() + "\n");
			}
			if (!StringUtils.isEmpty(errorMessage.toString())) {
				List<IRALAckFileParams> batchXferFile = new ArrayList<>();
				IRALAckFileParams xferFile = new IRALAckFileParams();
				xferFile.setFileName(jobExecution.getJobParameters().getString("fileName"));
				xferFile.setFileId(iralACkFileVo.getFileIdVlaue());
				xferFile.setFileStatus(BatchUtil.EXCEPTION_CONSTANT);
				batchXferFile.add(xferFile);
				// If We get any exception Updating the XFER_XMIT_STATUS = 'X' in
				// T_XFER_CONTROL_BATCH Table .
				batchProcessDao.updateXFERFileByStatus(batchXferFile, BatchUtil.EXCEPTION_CONSTANT);
				IRALAckFileParams files = batchProcessDao.getXFERFileParamsByfileStatus(BatchUtil.EXCEPTION_CONSTANT);

				for (Resource r : batchProcessDao.getMultiResources()) {

					File file = r.getFile();
					fileName = file.getName();
					if (file.exists()) {
						batchProcessDao.sendDataToAckFile(file.getName(), "99", "Other error encountered");
						// Moving the File from inbound folder to exception folder.
						Files.move(Paths.get(file.getPath()),
								Paths.get(files.getFilePath() + "" + StringUtils.getFilename(file.getName())),
								StandardCopyOption.REPLACE_EXISTING);

					}

				}
				throw new IRALBatchException(errorMessage.toString());
			}
		} catch (Exception e) {
			LOGGER.error("InterceptingJobExecution Error Message : " + e.getMessage());
			batchProcessDao.updateXferControlByFileName(0, fileName);
			batchProcessDao.sendDataToAckFile(fileName, "99", "Other error encountered");
			MomsData momsData = batchProcessDao.getMomsData();
			batchProcessDao.getPmmsAlert().createMOMS(momsData.getDescription(), e.getMessage(),
					momsData.getEquipmentId(), momsData.getRepairNumber());
			AlertRequest alertRequest = batchProcessDao.populateAlertMessage(BatchUtil.BATCH_PROCESS_NAME,
					"InterceptingJobExecution Error Message : " + e.getMessage(),
					jobExecution.getJobParameters().getString("fileName"));
			batchProcessDao.getPmmsAlert().persistAlerts(alertRequest);
		}
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
	}

}