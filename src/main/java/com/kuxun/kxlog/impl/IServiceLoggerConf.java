package com.kuxun.kxlog.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.Appender;

/**
 * 服务日志配置
 * 
 * @author dengzh
 */
public interface IServiceLoggerConf {
	/**
	 * 日志开关
	 * @return true for 开启，false 关闭日志输出
	 */
	public boolean enable();
	/**
	 * 控制日志输出级别
	 * @return
	 */
	public Level getLevel();
	
	/**
	 * 控制日志输出
	 */
	public Appender getAppender();
}
