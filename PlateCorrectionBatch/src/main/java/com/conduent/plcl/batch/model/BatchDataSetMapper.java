package com.conduent.plcl.batch.model;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

/**
 * Mapping the inside file data into PlateCorrection Object
 * 
 * @author 52058018
 *
 */
public class BatchDataSetMapper implements FieldSetMapper<PlateCorrection> {

	@Override
	public PlateCorrection mapFieldSet(FieldSet fieldSet) throws BindException {
		PlateCorrection batchProcess = new PlateCorrection();
		batchProcess.setOrigLicState(fieldSet.readRawString("OrigLicState"));
		batchProcess.setOrigLicNumber(fieldSet.readRawString("OrigLicNumber"));
		batchProcess.setOrigLicType(fieldSet.readRawString("OrigLicType"));
		batchProcess.setAdjLicState(fieldSet.readRawString("AdjLicState"));
		batchProcess.setAdjLicNumber(fieldSet.readRawString("AdjLicNumber"));
		batchProcess.setAdjLicType(fieldSet.readRawString("AdjLicType"));

		return batchProcess;
	}

}
