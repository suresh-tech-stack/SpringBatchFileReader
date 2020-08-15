package com.conduent.iral.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

import com.conduent.iral.batch.common.FileDataValidation;
import com.conduent.iral.batch.common.IRALBatchException;
import com.conduent.iral.batch.dao.BatchProcessDao;
import com.conduent.iral.batch.model.IRALAckFile;
import com.conduent.iral.batch.model.IRALAckFileParams;
import com.conduent.iral.batch.model.MomsData;
import com.conduent.iral.batch.util.BatchUtil;
import com.conduent.vector.pmms.genericservice.vo.AlertRequest;

/**
 * This is the Main class where the Batch Job starts .
 * 
 * @author 52058018
 *
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = { "com.conduent.iral.batch", "com.conduent.acknowledgment" })
public class IralBatch {
	private final static Logger LOGGER = Logger.getLogger(IralBatch.class);
	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	Job job;

	@Autowired
	BatchProcessDao batchProcessDao;

	private String oriFilePath;

	private String inputFilePath;

	private String iralzip;

	private String unProcessedFilePath;

	private String processedFilePath;

	@Value("${iralendswith}")
	private String iralendswith;

	@Autowired
	FileDataValidation fileDataValidation;

	@Autowired
	IRALAckFile iralACkFileVo;

	private String fileName = null;

	public static void main(String[] args) throws Exception {
		LOGGER.debug("Batch Started ... ");
		SpringApplication.run(IralBatch.class, args);

	}

	/**
	 * This Method will execute the Job based on the cronParmater value given in
	 * properties file.
	 */
	@Scheduled(cron = "${cronParameter}")
	public void perform() {
		LOGGER.debug("Job Execution started ...");
		try {

			LOGGER.debug("Original File Path : " + oriFilePath);
			LOGGER.debug("Input File Path : " + inputFilePath);
			LOGGER.debug("UnProcessed File Path : " + unProcessedFilePath);
			LOGGER.debug("IRAL Files Ends With :: " + iralendswith);

			// UnZipping the given file .
			unZipFiles(oriFilePath, iralzip);

			// checking the IRAL files and validate the Data.
			checkIRALFilesAndvalidate(oriFilePath, inputFilePath);

			List<IRALAckFileParams> unProcessedFilesList = batchProcessDao.getUnprocessedFileNames();
			LOGGER.info("Number of Files has to Process : " + unProcessedFilesList.size());
			if (unProcessedFilesList.size() > 0) {
				// Updating the XFER_XMIT_STATUS while reading is 'R'
				batchProcessDao.updateXFERFileByStatus(unProcessedFilesList, BatchUtil.PICKEDUP_CONSTANT);
				for (IRALAckFileParams xFERFile : unProcessedFilesList) {
					StringBuilder completeFilePath = new StringBuilder();
					completeFilePath.append(xFERFile.getFilePath());
					completeFilePath.append(xFERFile.getFileName());
					iralACkFileVo.fileIdValue(xFERFile.getFileId());
					LOGGER.debug("Complete File Path is : " + completeFilePath.toString());
					JobParameters parameters = new JobParametersBuilder()
							.addString("fileName", completeFilePath.toString()).toJobParameters();
					iralACkFileVo.fileName(xFERFile.getFileName());

					try {
						LOGGER.debug("Job Parameters : " + parameters);
						jobLauncher.run(job, parameters);
						LOGGER.info(" Job Successfully Completed ..... ");
					} catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException
							| JobRestartException | JobParametersInvalidException e) {
						batchProcessDao.updateXferControlByFileName(0, fileName);
						batchProcessDao.sendDataToAckFile(xFERFile.getFileName(), "99", "Other error encountered");
						MomsData momsData = batchProcessDao.getMomsData();
						batchProcessDao.getPmmsAlert().createMOMS(momsData.getDescription(), e.getMessage(),
								momsData.getEquipmentId(), momsData.getRepairNumber());
						LOGGER.error("JobInstanceAlreadyCompleteException: " + e.getMessage());
						Files.move(Paths.get(inputFilePath + xFERFile.getFileName()),
								Paths.get(unProcessedFilePath + xFERFile.getFileName()),
								StandardCopyOption.REPLACE_EXISTING);
						LOGGER.debug("File already Processed , Moving File to UnProcessed Directory..");
						batchProcessDao.updateXFERFileByStatus(unProcessedFilesList, BatchUtil.DUPLICATE_CONSTANT);
						AlertRequest alertRequest = batchProcessDao.populateAlertMessage(BatchUtil.BATCH_PROCESS_NAME,
								"Unable to process the job : : " + e.getMessage(), xFERFile.getFileName());
						batchProcessDao.getPmmsAlert().persistAlerts(alertRequest);
					}

				}
			}
		} catch (IOException | IRALBatchException e) {
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
	 * UnZipping the given IRAL Files and placing it to another directory .
	 * 
	 * @param oriFilePath
	 * @param iralzip
	 * @throws IOException
	 */
	public void unZipFiles(String oriFilePath, String iralzip) throws IOException {
		File dir = new File(oriFilePath);
		String iralZipendswith = iralendswith.substring(iralendswith.lastIndexOf("_"), iralendswith.lastIndexOf("."));
		File[] fileList = dir.listFiles((file) -> {
			return !file.isDirectory() && file.getName().endsWith(iralZipendswith + ".ZIP")
					|| file.getName().endsWith(iralZipendswith + ".zip");
		});
		for (File files : fileList) {
			String file = files.getName();
			byte[] buffer = new byte[1024];
			ZipInputStream zis = new ZipInputStream(new FileInputStream(oriFilePath + File.separator + file));
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				File newFile = createFile(new File(oriFilePath), zipEntry);
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
			Files.move(Paths.get(oriFilePath + File.separator + file), Paths.get(iralzip + File.separator + file),
					StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Placing the zip file in another directory.
	 * 
	 * @param destinationDir
	 * @param zipEntry
	 * @return
	 * @throws IOException
	 */
	public static File createFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

	/**
	 * Initialize the ACK Batch related File Paths
	 */
	@Autowired
	public void setFilePathDataFromDB() {
		oriFilePath = batchProcessDao.getFilePath();
		inputFilePath = batchProcessDao.getInboundFilePath();
		unProcessedFilePath = batchProcessDao.getUnProcessedFilePath();
		iralzip = batchProcessDao.getZipFilePath();
		processedFilePath = batchProcessDao.getProcessedFilePath();
	}

	/**
	 * Read IRAL files only and check the header data and no.of lines and file is
	 * available in T_XFER_CONTROL Table .
	 * 
	 * @param oriFilePath
	 * @param inputFilePath
	 * @throws IOException
	 * @throws IRALBatchException
	 */
	public void checkIRALFilesAndvalidate(String oriFilePath, String inputFilePath)
			throws IOException, IRALBatchException {
		File dir = new File(oriFilePath);
		File[] fileList = dir.listFiles((file) -> {
			return !file.isDirectory() && file.getName().endsWith(BatchUtil.IRAL_FILE_EXT);
		});
		for (File file : fileList) {
			fileName = file.getName();
			if (!fileDataValidation.checkHeaderData(dir.getAbsolutePath() + "/" + fileName)) {
				batchProcessDao.updateXferControlByFileName(0, fileName);
				batchProcessDao.insertUnProcessedFileInXferControl(fileName, 0, batchProcessDao.getXferFileId());
				LOGGER.debug("Header Data Invalid .. ");
				LOGGER.debug("File Moved to UnProcessed Files Directory...");
				batchProcessDao.sendDataToAckFile(fileName, BatchUtil.MOM_08, BatchUtil.INVALID_HEADER);
				batchProcessDao.createMomWorkOrder(BatchUtil.MOM_MSG_08, BatchUtil.INVALID_MSG + fileName);
				Files.move(Paths.get(oriFilePath + fileName), Paths.get(processedFilePath + fileName),
						StandardCopyOption.REPLACE_EXISTING);
				continue;
			}
			if (!fileDataValidation.checkFileLinesCount(dir.getAbsolutePath() + "/" + fileName)) {
				batchProcessDao.updateXferControlByFileName(0, fileName);
				batchProcessDao.insertUnProcessedFileInXferControl(fileName,
						fileDataValidation.getNoOfLines(dir.getAbsolutePath() + "/" + fileName),
						batchProcessDao.getXferFileId());
				LOGGER.debug("Header record Count and Line count not Matched.. ");
				LOGGER.debug("File Moved to UnProcessed Files Directory...");
				batchProcessDao.sendDataToAckFile(fileName, BatchUtil.MOM_01, BatchUtil.INVALID_RECORD);
				batchProcessDao.createMomWorkOrder(BatchUtil.MOM_MSG_01, BatchUtil.INVALID_MSG + fileName);
				Files.move(Paths.get(oriFilePath + fileName), Paths.get(processedFilePath + fileName),
						StandardCopyOption.REPLACE_EXISTING);
				continue;
			}
			if (batchProcessDao.checkFileNameInXferControl(fileName) == 0) {
				MomsData momsData = batchProcessDao.getMomsData();
				LOGGER.debug(BatchUtil.NO_FILE_IN_TABLE + fileName);
				batchProcessDao.getPmmsAlert().createMOMS(momsData.getDescription(), BatchUtil.NO_FILE_IN_TABLE,
						momsData.getEquipmentId(), momsData.getRepairNumber());
				Files.move(Paths.get(oriFilePath + fileName), Paths.get(unProcessedFilePath + fileName),
						StandardCopyOption.REPLACE_EXISTING);
				throw new IRALBatchException(BatchUtil.NO_FILE_IN_TABLE);
			}
			try {
				int recordCount = fileDataValidation.getHeaderCount(dir.getAbsolutePath() + "/" + fileName);
				batchProcessDao.insertXferControl(fileName, recordCount, batchProcessDao.getProcessParamId());
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
}
