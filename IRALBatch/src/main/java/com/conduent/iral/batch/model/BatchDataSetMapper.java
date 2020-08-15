package com.conduent.iral.batch.model;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;
/**
 * Mapping the inside file data into IRALAckFile Object
 * 
 * @author 52058018
 *
 */
public class BatchDataSetMapper implements FieldSetMapper<IRALAckFile> {

	@Override
	public IRALAckFile mapFieldSet(FieldSet fieldSet) throws BindException {
		IRALAckFile iRALAckFile = new IRALAckFile();
		iRALAckFile.setRecivedDateTime(fieldSet.readRawString("recivedDateTime"));
		iRALAckFile.setAckReturnCode(fieldSet.readRawString("ackReturnCode"));
		iRALAckFile.setImageZipName(fieldSet.readRawString("imageZipName"));
		return iRALAckFile;
	}

}
