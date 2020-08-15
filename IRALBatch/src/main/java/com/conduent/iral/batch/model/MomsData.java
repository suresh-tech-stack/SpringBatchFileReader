package com.conduent.iral.batch.model;

/**
 * Model class for MomAlert Data .
 * 
 * @author 52058018
 *
 */
public class MomsData {

	private Long equipmentId;
	private Long repairNumber;
	private String description;

	/**
	 * @return the equipmentId
	 */
	public Long getEquipmentId() {
		return equipmentId;
	}

	/**
	 * @param equipmentId the equipmentId to set
	 */
	public void setEquipmentId(Long equipmentId) {
		this.equipmentId = equipmentId;
	}

	/**
	 * @return the repairNumber
	 */
	public Long getRepairNumber() {
		return repairNumber;
	}

	/**
	 * @param repairNumber the repairNumber to set
	 */
	public void setRepairNumber(Long repairNumber) {
		this.repairNumber = repairNumber;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}