package com.kuxun.kxlog.common;

/**
 * 框架异常
 * @author dengzh
 */
public class KxException extends Exception {

	private Throwable rootCause;

	public KxException() {
		super();
	}

	public KxException(String message) {
		super(message);
	}

	public KxException(Throwable rootCause,String message) {
		super(message, rootCause);
		this.rootCause = rootCause;
	}

	public KxException(Throwable rootCause) {
		super(rootCause);
		this.rootCause = rootCause;
	}

	public Throwable getRootCause() {
		return rootCause;
	}
	
}
