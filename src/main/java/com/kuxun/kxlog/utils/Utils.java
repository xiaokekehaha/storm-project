package com.kuxun.kxlog.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.common.ReportableException;

public class Utils {

	public static String stackTrace(Exception cause) {
		if (cause == null)
			return "";
		Throwable throwable = cause.getCause();
		if (throwable instanceof Exception)
			cause = (Exception) throwable;
		StringWriter sw = new StringWriter(1024);
		PrintWriter pw = new PrintWriter(sw);
		cause.printStackTrace(pw);
		pw.flush();
		return sw.toString().replace("\r\n", " ").replace("\t", " ");
	}

	public static boolean isNotBlank(String str) {
		if (str == null || "".equals(str.trim()))
			return false;

		return true;
	}

	public static String trim(String source) {

		return null;
	}

	public static boolean isBlank(String str) {
		if (str == null || "".equals(str.trim()))
			return true;
		return false;
	}

	public static boolean isInt(String str) {
		if (isBlank(str))
			return false;
		char[] charArr = str.toCharArray();
		int i = 0;
		if (charArr[i] == '+' || charArr[i] == '-') {
			i++;
		}
		int len = charArr.length;

		while (i < len && charArr[i++] == '0')
			;

		for (; i < len; i++) {
			if (charArr[i] < '0' || charArr[i] > '9')
				return false;
		}

		return true;
	}

	public static boolean isInt(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof Integer)
			return true;

		return isInt(String.valueOf(obj));
	}

	public static boolean isNotBlank(Object obj) {
		return !isBlank(obj);
	}

	public static boolean isBlank(Object obj) {
		if (obj == null)
			return true;
		return isBlank((String) obj);
	}

	/**
	 * 异常包装类
	 * 
	 * @param throwable
	 * @param message
	 * @param reportable
	 * @return KxException
	 */
	public static KxException wrapperException(Throwable throwable, String message, boolean reportable) {
		/*
		 * if (throwable instanceof KxException) { if (!(throwable instanceof
		 * ReportableException) && reportable) { return new
		 * ReportableException(throwable,message); } return (KxException)
		 * throwable; }
		 */

		if (!(throwable instanceof KxException)) {
			throwable.printStackTrace();
		}
		KxException kxe = null;
		if (reportable)
			kxe = new ReportableException(throwable, message);
		else
			kxe = new KxException(throwable, message);
		return kxe;
	}

	public static KxException wrapperException(Throwable throwable, String message) {
		return wrapperException(throwable, message, false);
	}

}
