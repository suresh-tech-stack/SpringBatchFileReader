package com.conduent.plcl.batch.common;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;

import com.conduent.plcl.batch.dao.BatchProcessDao;
import com.conduent.plcl.batch.model.CustomJobData;
import com.conduent.plcl.batch.model.PlateCorrection;

/**
 * This Class is the custom Item Processor class to classify the data and
 * inserting into DB .
 * 
 * @author 52058018
 *
 */
public class BatchItemProcessor implements ItemProcessor<PlateCorrection, PlateCorrection> {
	private final static Logger LOGGER = Logger.getLogger(BatchItemProcessor.class);
	@Autowired
	private Validator validator;
	
	@Autowired
	BatchProcessDao batchProcessDao;
	
	@Autowired
	CustomJobData jobData;
	
	/**
	 * This Method will call for every record to processing into DB
	 */
	@Override
	public PlateCorrection process(PlateCorrection batchProcess) throws Exception {
		LOGGER.debug("Data Inserted into database ");
		LOGGER.debug("In BatchItemProcessor::  Process Method {}  : " + batchProcess);
		// Data Length Validation
		if ((batchProcess.getOrigLicState().length() != 2) || (batchProcess.getOrigLicNumber().length() != 10)
				|| (batchProcess.getOrigLicType().length() != 10) || (batchProcess.getAdjLicState().length() != 2)
				|| (batchProcess.getAdjLicNumber().length() != 10) || (batchProcess.getAdjLicType().length() != 10)) {
			// Creating the ACK file for Invalid Detail Records
			batchProcessDao.sendDataToAckFile(jobData.getFileName(), "02", "Invalid Detail Records");
			throw new PLCLBatchException("Data Validation Exception");
		}
		batchProcess.setUpdatedTs(new Date());
		DataBinder binder = new DataBinder(batchProcess);
		binder.setValidator(validator);
		binder.validate();
		BindingResult result = binder.getBindingResult();
		if (result.hasErrors()) {
			StringBuilder msg = new StringBuilder();

			LOGGER.error("Validation  Message {} : " + msg);
			return null; // Invalid data should not be processed into Database.
		}
		return batchProcess;
	}

}