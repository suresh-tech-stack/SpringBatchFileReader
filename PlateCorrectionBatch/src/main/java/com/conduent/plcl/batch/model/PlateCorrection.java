package com.conduent.plcl.batch.model;

import java.util.Date;

/**
 * This Class have the fields that are there in the file to map into this object
 * 
 * @author 52058018
 */
public class PlateCorrection {
	private String OrigLicState;
	private String OrigLicNumber;
	private String OrigLicType;
	private String AdjLicState;
	private String AdjLicNumber;
	private String AdjLicType;
	private String updatedBy;
	private Date updatedTs;
	private String jobName;

	public PlateCorrection() {

	}

	/**
	 * @param origLicState
	 * @param origLicNumber
	 * @param origLicType
	 * @param adjLicState
	 * @param adjLicNumber
	 * @param adjLicType
	 * @param updatedBy
	 * @param updatedTs
	 * @param jobName
	 */
	public PlateCorrection(String origLicState, String origLicNumber, String origLicType, String adjLicState,
			String adjLicNumber, String adjLicType, String updatedBy, Date updatedTs, String jobName) {
		super();
		OrigLicState = origLicState;
		OrigLicNumber = origLicNumber;
		OrigLicType = origLicType;
		AdjLicState = adjLicState;
		AdjLicNumber = adjLicNumber;
		AdjLicType = adjLicType;
		this.updatedBy = updatedBy;
		this.updatedTs = updatedTs;
		this.jobName = jobName;
	}

	public String getOrigLicState() {
		return OrigLicState;
	}

	public void setOrigLicState(String origLicState) {
		OrigLicState = origLicState;
	}

	public String getOrigLicNumber() {
		return OrigLicNumber;
	}

	public void setOrigLicNumber(String origLicNumber) {
		OrigLicNumber = origLicNumber;
	}

	public String getOrigLicType() {
		return OrigLicType;
	}

	public void setOrigLicType(String origLicType) {
		OrigLicType = origLicType;
	}

	public String getAdjLicState() {
		return AdjLicState;
	}

	public void setAdjLicState(String adjLicState) {
		AdjLicState = adjLicState;
	}

	public String getAdjLicNumber() {
		return AdjLicNumber;
	}

	public void setAdjLicNumber(String adjLicNumber) {
		AdjLicNumber = adjLicNumber;
	}

	public String getAdjLicType() {
		return AdjLicType;
	}

	public void setAdjLicType(String adjLicType) {
		AdjLicType = adjLicType;
	}

	/**
	 * @return the updatedBy
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * @param updatedBy the updatedBy to set
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	/**
	 * @return the updatedTs
	 */
	public Date getUpdatedTs() {
		return updatedTs;
	}

	/**
	 * @param updatedTs the updatedTs to set
	 */
	public void setUpdatedTs(Date updatedTs) {
		this.updatedTs = updatedTs;
	}

	/**
	 * @return the jobName
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * @param jobName the jobName to set
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PlateCorrection [OrigLicState=");
		builder.append(OrigLicState);
		builder.append(", OrigLicNumber=");
		builder.append(OrigLicNumber);
		builder.append(", OrigLicType=");
		builder.append(OrigLicType);
		builder.append(", AdjLicState=");
		builder.append(AdjLicState);
		builder.append(", AdjLicNumber=");
		builder.append(AdjLicNumber);
		builder.append(", AdjLicType=");
		builder.append(AdjLicType);
		builder.append(", updatedBy=");
		builder.append(updatedBy);
		builder.append(", updatedTs=");
		builder.append(updatedTs);
		builder.append(", jobName=");
		builder.append(jobName);
		builder.append("]");
		return builder.toString();
	}

}
