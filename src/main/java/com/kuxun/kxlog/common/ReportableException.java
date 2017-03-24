package com.kuxun.kxlog.common;

/**
 * 可以在storm ui中显示的异常信息
 */
public class ReportableException extends KxException implements Reportable {

	
	public ReportableException(Throwable t) {
		// TODO Auto-generated constructor stub
		super(t);
	}

	
	public ReportableException(String message) {
		super(message);
	}

	
	public ReportableException(Throwable t, String message) {
		super(t, message);
	}

	
	public ReportableException() {
		super();
	}
}
