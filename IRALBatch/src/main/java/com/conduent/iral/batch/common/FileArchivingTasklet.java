package com.conduent.iral.batch.common;

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
import org.springframework.util.StringUtils;

import com.conduent.iral.batch.dao.BatchProcessDao;
import com.conduent.iral.batch.model.IRALAckFile;
import com.conduent.iral.batch.model.IRALAckFileParams;
import com.conduent.iral.batch.util.BatchUtil;

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
	BatchProcessDao batchProcessDao;

	@Autowired
	IRALAckFile iralACkFileVo;

	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		LOGGER.debug("In FileArchivingTasklet :: execute method {} to  archiving the processed reources ");
		IRALAckFileParams fileParams = batchProcessDao.getXFERFileParamsByfileStatus(BatchUtil.ARCHIVE_CONSTANT);

		for (Resource r : resources) {
			File file = r.getFile();
			// For Invalid Details records creating the MomAlerts and crating the ACK .
			if (iralACkFileVo.isValidRecord()) {
				batchProcessDao.sendDataToAckFile(iralACkFileVo.getFileName(), "02", "Invalid Detail Records");
				batchProcessDao.createMomWorkOrder("IRALBATCH-02",
						"Invalid Detail records for the File : " + iralACkFileVo.getFileName());
			} else {
				// Creating the ACK file for successful process IRAL file .
				batchProcessDao.sendDataToAckFile(file.getName(), "00", "");
			}
			if (file.exists()) {
				IRALAckFileParams fileNameStatus = new IRALAckFileParams();
				fileNameStatus.setFileName(StringUtils.stripFilenameExtension(file.getName()));
				fileNameStatus.setFileId(iralACkFileVo.getFileIdVlaue());
				List<IRALAckFileParams> fileNameStatusList = new ArrayList<>();
				fileNameStatusList.add(fileNameStatus);
				batchProcessDao.updateXFERFileByStatus(fileNameStatusList, BatchUtil.ARCHIVE_CONSTANT);
				// Moving the File from inbound Directory to processed Directory .
				Files.move(Paths.get(file.getPath()), Paths.get(fileParams.getFilePath() + "" + file.getName()),
						StandardCopyOption.REPLACE_EXISTING);

				LOGGER.info("File Moved to Procesed directory successfully .");
			}
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