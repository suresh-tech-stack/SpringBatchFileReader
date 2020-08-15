package com.conduent.plcl.batch.common;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import com.conduent.plcl.batch.dao.BatchProcessDao;
import com.conduent.plcl.batch.model.CustomJobData;
import com.conduent.plcl.batch.model.MomsData;
import com.conduent.plcl.batch.model.PLCLParams;
import com.conduent.plcl.batch.util.BatchUtil;
import com.conduent.vector.pmms.genericservice.vo.AlertRequest;

/**
 * This Class is for to place the file in Processed Directory after processing
 * the file .
 * 
 * @author 52058018
 *
 */
public class FileArchivingTasklet implements Tasklet, InitializingBean {
	private final static Logger LOGGER = Logger.getLogger(FileArchivingTasklet.class);
	private Resource[] resources;

	private String fileId;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	BatchProcessDao batchProcessDao;

	@Autowired
	CustomJobData jobData;

	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		LOGGER.debug("In FileDeletingTasklet :: execute method {} to  archiving the processed reources ");
		PLCLParams fileParams = batchProcessDao.getXFERFileParamsByfileStatus(BatchUtil.ARCHIVE_CONSTANT);
		String inputFilePath = jobData.getInputFilePath();
		String unProcessedFilePath = jobData.getUnProcessedFilePath();
		String fileName = null;
		try {
			if (null != resources) {
				for (Resource r : resources) {
					File file = r.getFile();
					fileName = file.getName();
					LOGGER.info("Before Executing PLCLDelta procedure to Move Data from Stage Table to Main Table");
					jdbcTemplate.update("{call P_PLATE_CORRECTION}");
					// Creating the ACK file for successful process PLCL file .
					batchProcessDao.sendDataToAckFile(file.getName(), "00", "");
					batchProcessDao.updateAckDateTime(jobData.getFileIdVlaue());
					if (file.exists()) {
						PLCLParams fileNameStatus = new PLCLParams();
						fileNameStatus.setFileName(StringUtils.stripFilenameExtension(file.getName()));
						fileNameStatus.setFileId(jobData.getFileIdVlaue());
						List<PLCLParams> fileNameStatusList = new ArrayList<>();
						fileNameStatusList.add(fileNameStatus);
						batchProcessDao.updateXFERFileByStatus(fileNameStatusList, BatchUtil.ARCHIVE_CONSTANT);
						LOGGER.debug("FileParams : " + fileParams.getFilePath());
						// Moving the File from inbound Directory to processed Directory .
						Files.move(Paths.get(file.getPath()), Paths.get(fileParams.getFilePath() + "" + file.getName()),
								StandardCopyOption.REPLACE_EXISTING);
						LOGGER.debug("File Moved to Processed directory successfully .");
					}
				}
			}
			LOGGER.info("After Executed PLCLDelta procedure");
		} catch (Exception e) {
			batchProcessDao.updateXferControlByFileName(0, fileName);
			MomsData momsData = batchProcessDao.getMomsData();
			batchProcessDao.getPmmsAlert().createMOMS(momsData.getDescription(), e.getMessage(),
					momsData.getEquipmentId(), momsData.getRepairNumber());
			AlertRequest alertRequest = batchProcessDao.populateAlertMessage(BatchUtil.BATCH_PROCESS_NAME,
					"P_PLATE_CORRECTION Procedure DataBaseException: " + e.getMessage(), fileName);
			batchProcessDao.getPmmsAlert().persistAlerts(alertRequest);
			Files.move(Paths.get(inputFilePath + fileName), Paths.get(unProcessedFilePath + fileName),
					StandardCopyOption.REPLACE_EXISTING);
			LOGGER.error("DataBaseException Message : " + e.getMessage());
		}

		return RepeatStatus.FINISHED;
	}

	public void setResources(Resource[] resources) {
		this.resources = resources;
	}

	public void afterPropertiesSet() throws Exception {
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
}