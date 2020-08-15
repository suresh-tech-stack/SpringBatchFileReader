package com.conduent.plcl.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.conduent.plcl.batch.common.FileDataValidation;
import com.conduent.plcl.batch.common.PLCLBatchException;
import com.conduent.plcl.batch.dao.BatchProcessDao;
import com.conduent.plcl.batch.model.CustomJobData;
import com.conduent.plcl.batch.model.MomsData;
import com.conduent.plcl.batch.model.PLCLParams;
import com.conduent.plcl.batch.util.BatchUtil;
import com.conduent.vector.pmms.genericservice.vo.AlertRequest;

/**
 * This is the Main class where the Batch Job starts .
 * 
 * @author 52058018
 *
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = { "com.conduent.plcl.batch", "com.conduent.acknowledgment" })
public class PlateCorrectionBatch {
	private final static Logger LOGGER = Logger.getLogger(PlateCorrectionBatch.class);
	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	Job job;

	@Autowired
	BatchProcessDao batchProcessDao;

	@Autowired
	CustomJobData jobData;

	@Value("${plclendswith}")
	private String plclendswith;

	@Autowired
	FileDataValidation fileDataValidation;

	private String fileName = null;

	public static void main(String[] args) throws Exception {
		LOGGER.debug("Batch Started ... ");
		SpringApplication.run(PlateCorrectionBatch.class, args);

	}

	/**
	 * This Method will execute the Job based on the cronParmater value given in
	 * properties file.
	 */
	@Scheduled(cron = "${cronParameter}")
	public void perform() {
		LOGGER.debug("Job Execution started ...");
		try {
			LOGGER.debug("PLCL Files Ends With :: " + plclendswith);
			String oriFilePath = jobData.getOriFilePath();

			String inputFilePath = jobData.getInputFilePath();

			String plclzip = jobData.getPlclzip();

			String unProcessedFilePath = jobData.getUnProcessedFilePath();

			// UnZipping the given file .
			unZipFiles(oriFilePath, plclzip);

			// checking the PLCL files and validate the Data and inserting Files into table.
			checkPLCLFilesAndvalidate(oriFilePath, inputFilePath);

			List<PLCLParams> unProcessedFilesList = batchProcessDao.getUnprocessedFileNames();
			LOGGER.info("Number of Files has to Process : " + unProcessedFilesList.size());
			if (unProcessedFilesList.size() > 0) {
				// Updating the XFER_XMIT_STATUS while reading is 'R'
				batchProcessDao.updateXFERFileByStatus(unProcessedFilesList, BatchUtil.PICKEDUP_CONSTANT);
				for (PLCLParams pLCLParams : unProcessedFilesList) {
					StringBuilder completeFilePath = new StringBuilder();
					completeFilePath.append(pLCLParams.getFilePath());
					completeFilePath.append(pLCLParams.getFileName());
					jobData.fileIdValue(pLCLParams.getFileId());
					jobData.fileName(pLCLParams.getFileName());
					LOGGER.debug("Complete File Path is : " + completeFilePath.toString());
					JobParameters parameters = new JobParametersBuilder()
							.addString("fileName", completeFilePath.toString()).toJobParameters();
					try {
						LOGGER.debug("Job Parameters : " + parameters);
						jobLauncher.run(job, parameters);
						LOGGER.info(" Job Successfully Completed ..... ");
					} catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException
							| JobRestartException | JobParametersInvalidException e) {
						batchProcessDao.sendDataToAckFile(fileName, "99", "Other error encountered");
						batchProcessDao.updateXferControlByFileName(0, fileName);
						MomsData momsData = batchProcessDao.getMomsData();
						batchProcessDao.getPmmsAlert().createMOMS(momsData.getDescription(), e.getMessage(),
								momsData.getEquipmentId(), momsData.getRepairNumber());
						LOGGER.error("JobInstanceAlreadyCompleteException: " + e.getMessage());
						Files.move(Paths.get(inputFilePath + pLCLParams.getFileName()),
								Paths.get(unProcessedFilePath + pLCLParams.getFileName()),
								StandardCopyOption.REPLACE_EXISTING);
						LOGGER.debug("File already Processed , Moving File to UnProcessed Directory..");
						batchProcessDao.updateXFERFileByStatus(unProcessedFilesList, BatchUtil.DUPLICATE_CONSTANT);
						AlertRequest alertRequest = batchProcessDao.populateAlertMessage(BatchUtil.BATCH_PROCESS_NAME,
								"JobExecutionException : " + e.getMessage(), pLCLParams.getFileName());
						batchProcessDao.getPmmsAlert().persistAlerts(alertRequest);
					}
				}
			}
		} catch (IOException | PLCLBatchException e) {
			MomsData momsData = batchProcessDao.getMomsData();
			batchProcessDao.getPmmsAlert().createMOMS(momsData.getDescription(), e.getMessage(),
					momsData.getEquipmentId(), momsData.getRepairNumber());
			LOGGER.error("IOException :: Unable to locate the file or directory ::");
			AlertRequest alertRequest = batchProcessDao.populateAlertMessage(BatchUtil.BATCH_PROCESS_NAME,
					"IOException: : " + e.getMessage(), "NO FILE");
			batchProcessDao.getPmmsAlert().persistAlerts(alertRequest);
		}
	}

	/**
	 * Placing the PLCL zip file in another directory.
	 * 
	 * @param destinationDir
	 * @param zipEntry
	 * @return
	 * @throws IOException
	 */
	public void unZipFiles(String oriFilePath, String plclzip) throws IOException {
		File dir = new File(oriFilePath);
		String plclZipendswith = plclendswith.substring(plclendswith.lastIndexOf("_"), plclendswith.lastIndexOf("."));
		File[] fileList = dir.listFiles((file) -> {
			return !file.isDirectory() && file.getName().endsWith(plclZipendswith + BatchUtil.CAPITAL_ZIP)
					|| file.getName().endsWith(plclZipendswith + BatchUtil.SMALL_ZIP);
		});
		for (File files : fileList) {
			String file = files.getName();
			byte[] buffer = new byte[1024];
			ZipInputStream zis = new ZipInputStream(new FileInputStream(oriFilePath + File.separator + file));
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				File newFile = newFile(new File(oriFilePath), zipEntry);
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
			Files.move(Paths.get(oriFilePath + File.separator + file), Paths.get(plclzip + File.separator + file),
					StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

	/**
	 * Read PLCL files only and check the header data and no.of lines and file is
	 * available in T_XFER_CONTROL Table .
	 * 
	 * @param oriFilePath
	 * @param inputFilePath
	 * @throws IOException
	 * @throws PLCLBatchException
	 */
	public void checkPLCLFilesAndvalidate(String oriFilePath, String inputFilePath)
			throws IOException, PLCLBatchException {

		String processedFilePath = jobData.getProcessedFilePath();
		String unProcessedFilePath = jobData.getUnProcessedFilePath();

		File dir = new File(oriFilePath);
		File[] fileList = dir.listFiles((file) -> {
			return !file.isDirectory() && file.getName().endsWith(BatchUtil.INTERNAL_FILE_ENDS_WITH);
		});
		Arrays.sort(fileList, Comparator.comparingLong(File::lastModified));
		for (File files : fileList) {
			fileName = files.getName();
			if (!fileDataValidation.checkFileDate(fileName)) {
				LOGGER.info("File Skipped due to out of date , File Moved to UnProcessed Files Directory...");
				batchProcessDao.updateXferControlByFileName(0, fileName);
				batchProcessDao.sendDataToAckFile(fileName, BatchUtil.MOM_14, BatchUtil.OUT_OF_DATE_FILE);
				batchProcessDao.createMomWorkOrder(BatchUtil.MOM_MSG_14, BatchUtil.OUT_OF_DATE_FILE + fileName);
				Files.move(Paths.get(oriFilePath + fileName), Paths.get(processedFilePath + fileName),
						StandardCopyOption.REPLACE_EXISTING);
				continue;
			}
			if (!fileDataValidation.checkHeaderData(dir.getAbsolutePath() + "/" + fileName)) {
				batchProcessDao.updateXferControlByFileName(0, fileName);
				batchProcessDao.insertUnProcessedFileInXferControl(fileName, 0, batchProcessDao.getXferFileId());
				batchProcessDao.sendDataToAckFile(fileName, BatchUtil.MOM_08, BatchUtil.INVALID_HEADER);
				LOGGER.info("Header Data Invalid .. ");
				LOGGER.info("File Moved to UnProcessed Files Directory...");
				batchProcessDao.createMomWorkOrder(BatchUtil.MOM_MSG_08,
						"Invalid Header Data for the File : " + fileName);
				Files.move(Paths.get(oriFilePath + fileName), Paths.get(processedFilePath + fileName),
						StandardCopyOption.REPLACE_EXISTING);
				continue;
			}
			if (!fileDataValidation.checkFileLinesCount(dir.getAbsolutePath() + "/" + fileName)) {
				batchProcessDao.updateXferControlByFileName(0, fileName);
				batchProcessDao.insertUnProcessedFileInXferControl(fileName,
						fileDataValidation.getNoOfLines(dir.getAbsolutePath() + "/" + fileName),
						batchProcessDao.getXferFileId());
				batchProcessDao.sendDataToAckFile(fileName, BatchUtil.MOM_01, BatchUtil.INVALID_RECORD);
				LOGGER.info("Header Line Count and Line count not Matched.. ");
				LOGGER.info("File Moved to UnProcessed Files Directory...");
				batchProcessDao.createMomWorkOrder("PLCLBATCH-01",
						" Header count and record count does not match for the File : " + fileName);
				Files.move(Paths.get(oriFilePath + fileName), Paths.get(processedFilePath + fileName),
						StandardCopyOption.REPLACE_EXISTING);
				continue;
			}
			if (batchProcessDao.checkFileNameInXferControl(fileName) == 0) {
				LOGGER.info("Given File is Not Available in T_XFER_CONTROL Table : " + fileName);
				MomsData momsData = batchProcessDao.getMomsData();
				batchProcessDao.getPmmsAlert().createMOMS(momsData.getDescription(), BatchUtil.NO_FILE_IN_TABLE,
						momsData.getEquipmentId(), momsData.getRepairNumber());
				Files.move(Paths.get(oriFilePath + fileName), Paths.get(unProcessedFilePath + fileName),
						StandardCopyOption.REPLACE_EXISTING);
				throw new PLCLBatchException(BatchUtil.NO_FILE_IN_TABLE);
			}
			batchProcessDao.updateXferControlByFileName(
					fileDataValidation.getHeaderCount(dir.getAbsolutePath() + "/" + fileName), fileName);
			try {
				batchProcessDao.insertXferControl(fileName,
						fileDataValidation.getHeaderCount(dir.getAbsolutePath() + "/" + fileName),
						batchProcessDao.getProcessParamId());
				LOGGER.info("Given File Inserted into T_XFER_CONTROL_BATCH Table for Batch Process : " + fileName);
			} catch (Exception e) {
				MomsData momsData = batchProcessDao.getMomsData();
				batchProcessDao.getPmmsAlert().createMOMS(momsData.getDescription() + "SQL Exception",
						"Unable to insert the File", momsData.getEquipmentId(), momsData.getRepairNumber());
				LOGGER.error("Exception ::Unable to insert the File ::" + e.getMessage());
				AlertRequest alertRequest = batchProcessDao.populateAlertMessage(BatchUtil.BATCH_PROCESS_NAME,
						"Exception:" + e.getMessage(), "DB Exception");
				batchProcessDao.getPmmsAlert().persistAlerts(alertRequest);
				Files.move(Paths.get(oriFilePath + fileName), Paths.get(unProcessedFilePath + fileName),
						StandardCopyOption.REPLACE_EXISTING);
			}
			Files.move(Paths.get(oriFilePath + fileName), Paths.get(inputFilePath + fileName),
					StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Initialize the ACK Batch related File Paths from DB
	 */
	@Autowired
	public void setFilePathDataFromDB() {
		jobData.setOriFilePath(batchProcessDao.getFilePath());
		jobData.setInputFilePath(batchProcessDao.getInboundFilePath());
		jobData.setUnProcessedFilePath(batchProcessDao.getUnProcessedFilePath());
		jobData.setProcessedFilePath(batchProcessDao.getProcessedFilePath());
		jobData.setPlclzip(batchProcessDao.getZipFilePath());
	}
}
