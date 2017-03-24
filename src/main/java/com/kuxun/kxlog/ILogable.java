package com.kuxun.kxlog;

import org.slf4j.Logger;


public interface ILogable {
	/**
	 * 将日志实例注入到实例中
	 * @param logger
	 */
	public void setLogger(Logger logger);
}
