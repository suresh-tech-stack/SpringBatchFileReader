package com.conduent.iral.batch.common;

/**
 * Custom Exception Class .
 * 
 * @author 52058018
 *
 */
public class IRALBatchException extends Exception {

	private static final long serialVersionUID = -5038876983117848798L;

	public IRALBatchException() {
	}

	public IRALBatchException(String message) {
		super(message);

	}

	public IRALBatchException(Throwable clause) {
		super(clause);
	}

	public IRALBatchException(String message, Throwable clause) {
		super(message, clause);
	}
}