package com.conduent.iral.batch.model;
/**
 * @author 52058018
 *
 */
public class IRALAckFileParams {
	private String fileName;
	private String fileExtension;
	private String filePath;
	private String fileId;
	private String fileStatus; // A:Archive E:Execute P:Picked X:Exception
	private String fileXferId;

	public IRALAckFileParams() {
		super();
	}

	public String getFileXferId() {
		return fileXferId;
	}

	public void setFileXferId(String fileXferId) {
		this.fileXferId = fileXferId;
	}

	public IRALAckFileParams(String fileName, String fileExtension, String filePath,
			String fileId, String fileStatus, String fileXferId) {
		super();
		this.fileName = fileName;
		this.fileExtension = fileExtension;
		this.filePath = filePath;
		this.fileId = fileId;
		this.fileStatus = fileStatus;
		this.fileXferId = fileXferId;
	}

	public String getFileStatus() {
		return fileStatus;
	}

	public void setFileStatus(String fileStatus) {
		this.fileStatus = fileStatus;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

}
