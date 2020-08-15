package com.conduent.plcl.batch.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.conduent.plcl.batch.dao.BatchProcessDao;

/**
 * This class is for to check the file data
 * 
 * @author 52058018
 */
@Component
public class FileDataValidation {
	private final static Logger LOGGER = Logger.getLogger(FileDataValidation.class);
	@Autowired
	BatchProcessDao batchProcessDao;

	/**
	 * Check the lines count and header record count equal or not
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean checkFileLinesCount(String fileName) {
		boolean status = false;
		int headerCount;
		try {
			headerCount = getHeaderCount(fileName);
			int linesCount = getNoOfLines(fileName);
			LOGGER.debug("Lines Count : " + linesCount);
			if (headerCount == linesCount || headerCount + 1 == linesCount) {
				status = true;
				LOGGER.debug("Header Count and Lines Count are matched ");
				return status;
			}
		} catch (IOException e) {
			LOGGER.error("method checkFileLinesCount  :: " + e.getStackTrace());
		}
		return status;

	}

	/**
	 * Get the header record Count
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public int getHeaderCount(String fileName) throws IOException {
		FileInputStream fstream = null;
		String headerData = null;
		int headerCount = 0;
		BufferedReader br = null;
		try {
			fstream = new FileInputStream(fileName);
			br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;

			if ((strLine = br.readLine()) != null) {
				headerData = strLine;
				String[] tokens = headerData.split(",");
				headerCount = Integer.valueOf(tokens[2]);
				LOGGER.debug(" Header Count : " + headerCount);
			}
		} catch (Exception e) {
			LOGGER.error("method getHeaderCount  :: " + e.getStackTrace());
		} finally {
			fstream.close();
			br.close();
		}
		return headerCount;

	}

	/**
	 * Get the Lines count of the File
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public int getNoOfLines(String fileName) throws IOException {
		LineNumberReader reader = null;
		try {
			reader = new LineNumberReader(new FileReader(fileName));
			reader.skip(Integer.MAX_VALUE);
		} catch (Exception e) {
			LOGGER.error("method getNoOfLines  :: " + e.getStackTrace());
		} finally {
			reader.close();
		}
		LOGGER.debug("getNoOfLines  :: " + reader.getLineNumber());
		return reader.getLineNumber();
	}

	/**
	 * Check the Header data length and file type
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public boolean checkHeaderData(String fileName) throws IOException {
		FileInputStream fstream = null;
		String headerData = null;
		BufferedReader br = null;
		boolean result = false;
		try {
			fstream = new FileInputStream(fileName);
			br = new BufferedReader(new InputStreamReader(fstream));
			String strLine = br.readLine();
			headerData = strLine;
			String[] tokens = headerData.split(",");
			String data = String.valueOf(tokens[0]);
			if (strLine != null && strLine.length() == 28 && data.equalsIgnoreCase("PLCL")) {
				result = true;
			}
		} catch (Exception e) {
			LOGGER.error("Method checkHeaderData  :: " + e.getStackTrace());
		} finally {
			fstream.close();
			br.close();
		}
		return result;

	}

	public boolean checkFileDate(String fileName) {
		boolean isFile = false;
		String processedFileName = batchProcessDao.getProcessedFileName();
		String finalFile = fileName.substring(fileName.lastIndexOf("_"), fileName.lastIndexOf("."));
		String actualFile = finalFile.substring(1);
		if (null == processedFileName) {
			return true;
		}
		String processedFile = processedFileName.substring(processedFileName.lastIndexOf("_"),
				processedFileName.lastIndexOf("."));
		String dbFile = processedFile.substring(1);
		if (getDateTimeFormat(actualFile).compareTo(getDateTimeFormat(dbFile)) >= 0) {
			isFile = true;
			LOGGER.debug("FileName Date is greterthan or equals to Database date     : " + isFile);
		} else {
			isFile = false;
			LOGGER.debug("FileName Date is lessthan currentdate     : " + isFile);
		}
		return isFile;
	}

	public static Date getDateTimeFormat(String date) {
		try {
			Date dateTime = null;
			if (null != (date)) {
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