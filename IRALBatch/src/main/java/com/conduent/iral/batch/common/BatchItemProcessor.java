package com.conduent.iral.batch.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;

import com.conduent.iral.batch.dao.BatchProcessDao;
import com.conduent.iral.batch.model.IRALAckFile;

/**
 * This Class is the custom Item Processor class to classify the data and
 * inserting into DB .
 * 
 * @author 52058018
 *
 */
public class BatchItemProcessor implements ItemProcessor<IRALAckFile, IRALAckFile> {
	private final static Logger LOGGER = Logger.getLogger(BatchItemProcessor.class);
	@Autowired
	private Validator validator;

	@Autowired
	BatchProcessDao batchProcessDao;

	@Value("${odsZipFile}")
	private String odsZipFile;

	@Value("${etcZipFile}")
	private String etcZipFile;

	@Value("${videoZipFile}")
	private String videoZipFile;

	@Autowired
	IRALAckFile iralACkFileVo;

	/**
	 * This Method will call for every record to processing into DB
	 */
	@Override
	public IRALAckFile process(IRALAckFile iRALAckFile) throws Exception {
		iRALAckFile.setDateTime(getDateTimeFormat(iRALAckFile.getRecivedDateTime()));
		int count = 0;
		//Data Length Validation 
		if (iRALAckFile.getImageZipName().length() != 50 || iRALAckFile.getAckReturnCode().length() != 2
				|| iRALAckFile.getRecivedDateTime().length() != 14) {
			iralACkFileVo.setValidRecord(true);
		} else {
			iRALAckFile.setAckReturnCode(iRALAckFile.getAckReturnCode().trim());
			iRALAckFile.setImageZipName(iRALAckFile.getImageZipName().trim());
			if (iRALAckFile.getImageZipName().endsWith(odsZipFile)) {
				// Updating the data into T_TRIP_SENT_TO_BOS table for these two columns
				// ODS_RECEIVED_TIMESTAMP and ODS_IMG_ACK_RET_CODE
				count = batchProcessDao.updateTTripSentToBosODS(iRALAckFile);
				if (count > 0) {
					LOGGER.debug("Data Updated  into database for the file Name : " + iRALAckFile.getImageZipName());
					iralACkFileVo.setRecordCount(iralACkFileVo.getRecordCount() + 1); // move to data model
				}
			}
			if (iRALAckFile.getImageZipName().endsWith(etcZipFile)
					|| iRALAckFile.getImageZipName().endsWith(videoZipFile)) {
				// Updating the data into T_TRIP_SENT_TO_BOS table for these two columns
				// IMG_RECEIVED_TIMESTAMP and IMG_ACK_RET_CODE
				count = batchProcessDao.updateTTripSentToBos(iRALAckFile);
				if (count > 0) {
					LOGGER.debug("Data Updated  into database for the file Name : " + iRALAckFile.getImageZipName());
					iralACkFileVo.setRecordCount(iralACkFileVo.getRecordCount() + 1);
				}
			}
			// Updating the NUM_RECS and UPDATE_TS in T_XFER_CONTROL Table for the given
			// FileName.
			batchProcessDao.updateXferControlByFileName(iralACkFileVo.getRecordCount(), iralACkFileVo.getFileName());
		}
		DataBinder binder = new DataBinder(iRALAckFile);
		binder.setValidator(validator);
		binder.validate();
		BindingResult result = binder.getBindingResult();
		if (result.hasErrors()) {
			StringBuilder msg = new StringBuilder();

			LOGGER.error("BatchItemProcessor Validation  Message {} : " + msg);
			return null; // Invalid data should not be processed into Database.
		}
		return iRALAckFile;
	}

	/**
	 * Getting the dateTme format as yyyyMMddHHmmss
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDateTimeFormat(String date) {
		try {
			Date dateTime = null;
			if (!StringUtils.isEmpty(date)) {
				dateTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(date);
			} else {
				dateTime = new Date();
			}
			return dateTime;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

}