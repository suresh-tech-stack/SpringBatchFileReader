package com.conduent.iral.batch.common;

import org.apache.log4j.Logger;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;

/**
 * This class is to customize the LineMapper class and we can able to customize
 * the input lines received from file .
 */
public class CustomLineMapper<T> extends DefaultLineMapper<T> {
	private final static Logger LOGGER = Logger.getLogger(CustomLineMapper.class);
	private int linesToSkip;

	public CustomLineMapper(int linesToSkip) {
		this.linesToSkip = linesToSkip;
	}

	/** This method is to skip the footer from received file */
	@Override
	public T mapLine(String line, int lineNumber) throws Exception {
		/*
		 * if (lineNumber > linesToSkip && line.startsWith("E")) {
		 * LOGGER.debug("Skip Footer."); return null; }
		 */
		return super.mapLine(line, lineNumber);
	}

}