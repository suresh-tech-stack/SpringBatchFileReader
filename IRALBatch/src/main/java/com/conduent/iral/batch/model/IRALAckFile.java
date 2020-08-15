package com.conduent.iral.batch.model;

import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * In this Model class we have fields related to Table insertion and reusable
 * data passing fields
 * 
 * @author 52058018
 *
 */
@Component
public class IRALAckFile {
	private String recivedDateTime;
	private String ackReturnCode;
	private String imageZipName;
	private Date dateTime;
	// The Below fields for passing the data from one place to another place
	private String jobFileId;
	private String fileName;
	private boolean isValidRecord;
	private int recordCount = 0;

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
	 * @return
	 */
	public boolean isValidRecord() {
		return isValidRecord;
	}

	/**
	 * @param isValidRecord
	 * @return
	 */
	public boolean setValidRecord(boolean isValidRecord) {
		this.isValidRecord = isValidRecord;
		return isValidRecord;
	}

	/**
	 * @return
	 */
	public int getRecordCount() {
		return recordCount;
	}

	/**
	 * @param recordCount
	 * @return
	 */
	public int setRecordCount(int recordCount) {
		this.recordCount = recordCount;
		return recordCount;
	}

	public IRALAckFile() {
		super();
	}

	/**
	 * @param recivedDateTime
	 * @param ackReturnCode
	 * @param imageZipName
	 */
	public IRALAckFile(String recivedDateTime, String ackReturnCode, String imageZipName) {
		super();
		this.recivedDateTime = recivedDateTime;
		this.ackReturnCode = ackReturnCode;
		this.imageZipName = imageZipName;
	}

	/**
	 * @return the dateTime
	 */
	public Date getDateTime() {
		return dateTime;
	}

	/**
	 * @param dateTime the dateTime to set
	 */
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	/**
	 * @return the recivedDateTime
	 */
	public String getRecivedDateTime() {
		return recivedDateTime;
	}

	/**
	 * @param recivedDateTime the recivedDateTime to set
	 */
	public void setRecivedDateTime(String recivedDateTime) {
		this.recivedDateTime = recivedDateTime;
	}

	/**
	 * @return the ackReturnCode
	 */
	public String getAckReturnCode() {
		return ackReturnCode;
	}

	/**
	 * @param ackReturnCode the ackReturnCode to set
	 */
	public void setAckReturnCode(String ackReturnCode) {
		this.ackReturnCode = ackReturnCode;
	}

	/**
	 * @return the imageZipName
	 */
	public String getImageZipName() {
		return imageZipName;
	}

	/**
	 * @param imageZipName the imageZipName to set
	 */
	public void setImageZipName(String imageZipName) {
		this.imageZipName = imageZipName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IRALAckFile [recivedDateTime=");
		builder.append(recivedDateTime);
		builder.append(", ackReturnCode=");
		builder.append(ackReturnCode);
		builder.append(", imageZipName=");
		builder.append(imageZipName);
		builder.append(", dateTime=");
		builder.append(dateTime);
		builder.append("]");
		return builder.toString();
	}

}
