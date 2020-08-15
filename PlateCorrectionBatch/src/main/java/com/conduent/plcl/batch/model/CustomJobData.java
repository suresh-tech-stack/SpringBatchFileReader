/**
 * 
 */
package com.conduent.plcl.batch.model;

import org.springframework.stereotype.Component;

/**
 * Data Transfer Object .
 * 
 * @author 52058018
 *
 */
@Component
public class CustomJobData {

	// The Below fields for passing the data from one place to another place

	private String jobFileId;

	private String fileName;

	private String oriFilePath;

	private String inputFilePath;

	private String plclzip;

	private String unProcessedFilePath;

	private String processedFilePath;

	/**
	 * @param fileId
	 * @return
	 */
	public String fileIdValue(String fileId) {
		this.jobFileId = fileId;
		return fileId;
	}

	/**
	 * @return
	 */
	public String getFileIdVlaue() {
		return jobFileId;
	}

	/**
	 * @param fileName
	 * @return
	 */
	public String fileName(String fileName) {
		this.fileName = fileName;
		return fileName;
	}

	/**
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the oriFilePath
	 */
	public String getOriFilePath() {
		return oriFilePath;
	}

	/**
	 * @param oriFilePath the oriFilePath to set
	 */
	public void setOriFilePath(String oriFilePath) {
		this.oriFilePath = oriFilePath;
	}

	/**
	 * @return the inputFilePath
	 */
	public String getInputFilePath() {
		return inputFilePath;
	}

	/**
	 * @param inputFilePath the inputFilePath to set
	 */
	public void setInputFilePath(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}

	/**
	 * @return the plclzip
	 */
	public String getPlclzip() {
		return plclzip;
	}

	/**
	 * @param plclzip the plclzip to set
	 */
	public void setPlclzip(String plclzip) {
		this.plclzip = plclzip;
	}

	/**
	 * @return the unProcessedFilePath
	 */
	public String getUnProcessedFilePath() {
		return unProcessedFilePath;
	}

	/**
	 * @param unProcessedFilePath the unProcessedFilePath to set
	 */
	public void setUnProcessedFilePath(String unProcessedFilePath) {
		this.unProcessedFilePath = unProcessedFilePath;
	}

	/**
	 * @return the processedFilePath
	 */
	public String getProcessedFilePath() {
		return processedFilePath;
	}

	/**
	 * @param processedFilePath the processedFilePath to set
	 */
	public void setProcessedFilePath(String processedFilePath) {
		this.processedFilePath = processedFilePath;
	}

}
