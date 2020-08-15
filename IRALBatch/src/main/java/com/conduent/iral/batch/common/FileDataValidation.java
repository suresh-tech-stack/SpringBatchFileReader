package com.conduent.iral.batch.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * This class is for to check the file data
 * 
 * @author 52058018
 */
@Component
public class FileDataValidation {
	private final static Logger LOGGER = Logger.getLogger(FileDataValidation.class);

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
			if (strLine != null && strLine.length() == 28 && data.equalsIgnoreCase("IRAL")) {
				result = true;
				LOGGER.debug(" Header Data : " + strLine);
			}
		} catch (Exception e) {
			LOGGER.error("Method checkHeaderData  :: " + e.getStackTrace());
		} finally {
			fstream.close();
			br.close();
		}
		return result;
	}

}