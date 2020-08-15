package com.conduent.plcl.batch.common;

/**
 * Custom Exception Class .
 * 
 * @author 52058018
 *
 */
public class PLCLBatchException extends Exception {

	private static final long serialVersionUID = -5038876983117848798L;

	public PLCLBatchException() {
	}

	public PLCLBatchException(String message) {
		super(message);

	}

	public PLCLBatchException(Throwable clause) {
		super(clause);
	}

	public PLCLBatchException(String message, Throwable clause) {
		super(message, clause);
	}

}